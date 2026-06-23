/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.inbox.handler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.entities.VeranlagungEventLog;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.GesuchstellerTyp;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.OIDCServiceException;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageContext;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageHandler;
import ch.dvbern.ebegu.nesko.utils.KibonAnfrageUtil;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.services.WizardStepService;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.kibon.exchange.commons.neskovanp.NeueVeranlagungEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.AntragStatus.FREIGABEQUITTUNG;
import static ch.dvbern.ebegu.enums.AntragStatus.FREIGEGEBEN;
import static ch.dvbern.ebegu.enums.AntragStatus.IN_BEARBEITUNG_GS;

@ApplicationScoped
public class NeueVeranlagungEventHandler extends
	BaseEventHandler<NeueVeranlagungEventDTO> {

	private static final Logger LOG = LoggerFactory.getLogger(
		NeueVeranlagungEventHandler.class
	);
	private static final String BETREFF_KEY =
		"neue_veranlagung_mitteilung_betreff";
	private static final String BETREFF_KEY_MARKIERT =
		"neue_veranlagung_mitteilung_betreff_markiert";
	private static final String MESSAGE_KEY =
		"neue_veranlagung_mitteilung_message";
	private static final String MESSAGE_KEY_MARKIERT =
		"neue_veranlagung_mitteilung_message_markiert";
	private static final String MESSAGE_KEY_ANTRAGSTELLENDE_TITEL =
		"neue_veranlagung_mitteilung_antragstellende_titel";

	@Inject
	private GesuchService gesuchService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private FinanzielleSituationService finanzielleSituationService;

	@Inject
	private KibonAnfrageHandler kibonAnfrageHandler;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private Persistence persistence;

	@Inject
	private MitteilungService mitteilungService;

	@Inject
	private WizardStepService wizardStepService;

	@Override
	protected void processEvent(
		@Nonnull LocalDateTime eventTime,
		@Nonnull EventType eventType,
		@Nonnull String key,
		@Nonnull NeueVeranlagungEventDTO dto,
		@Nonnull String clientName
	) {
		NeueVeranlagungEventDomainDTO neueVeranlagungEventDomainDTO =
			convertNeueVeranlagungEventDTOToDomainDTO(dto);
		processEvent(
			key,
			neueVeranlagungEventDomainDTO
		);
	}

	/**
	 * Converts the AVRO DTO into the internal domain representation.
	 *
	 * <p>For schema compatibility reasons, the AVRO contract requires the ZPV
	 * number to be present. Since the field is defined as an integer, its default
	 * value is {@code 0}. However, a ZPV number must be unique, therefore
	 * {@code 0} cannot be used to represent an unknown or unspecified value.
	 *
	 * <p>To model the business semantics correctly, the domain object allows both
	 * the ZPV number and the AHV number to be {@code null}. A {@code null} value
	 * indicates that the identifier is not specified, whereas {@code 0} would be
	 * interpreted as an actual identifier value.
	 *
	 * @param dto the AVRO DTO received from the event
	 * @return the domain DTO representing the business model
	 */
	protected NeueVeranlagungEventDomainDTO convertNeueVeranlagungEventDTOToDomainDTO(
		NeueVeranlagungEventDTO dto
	) {
		return new NeueVeranlagungEventDomainDTO(
			dto.getZpvNummer() != 0 ? dto.getZpvNummer() : null,
			dto.getSozialversicherungsNummer(),
			dto.getGeburtsdatum(),
			dto.getKibonAntragId(),
			dto.getGesuchsperiodeBeginnJahr()
		);
	}

	private void processEvent(
		@Nonnull String key,
		@Nonnull NeueVeranlagungEventDomainDTO dto
	) {
		Processing processing = attemptProcessing(key, dto);
		VeranlagungEventLog veranlagungEventLog = new VeranlagungEventLog(
			key,
			dto.zpvNummer(),
			dto.sozialversicherungsNummer(),
			dto.geburtsdatum(),
			dto.gesuchsperiodeBeginnJahr()
		);
		if (!processing.isProcessingSuccess()) {
			String message = processing.getMessage();
			LOG.warn(
				"NeueVeranlagungEventHandler: Neue Veranlagung Event für ZPV-Nummer {} or AHV Nummer {} und Gesuch: {} nicht "
					+ "verarbeitet: {}",
				dto.zpvNummer(),
				dto.sozialversicherungsNummer(),
				key,
				message
			);
			veranlagungEventLog.setResult(processing.getMessage());
		} else {
			LOG.info(
				"NeueVeranlagungEventHandler: Neue Veranlagung Event für ZPV-Nummer {} or AHV-Nummer {} und Gesuch: {} "
					+ "verarbeitet",
				dto.zpvNummer(),
				dto.sozialversicherungsNummer(),
				key
			);
			veranlagungEventLog.setResult(
				"Veranlagung erfolgreich verarbeitet"
			);
		}
		persistence.persist(veranlagungEventLog);
	}

	@Nonnull
	protected Processing attemptProcessing(
		@Nonnull String key,
		@Nonnull NeueVeranlagungEventDomainDTO dto
	) {
		Optional<Gesuch> gesuchOpt = gesuchService.findGesuch(key);

		if (gesuchOpt.isEmpty()) {
			return Processing.failure(
				"Kein Gesuch für Key gefunden. Key: " + key
			);
		}
		Gesuch gesuch = gesuchOpt.get();

		if (gesuch.getStatus()
			.equals(AntragStatus.IN_BEARBEITUNG_SOZIALDIENST)) {
			return Processing.failure(
				"Gesuch ist in Bearbeitung bei der Sozialdienst: " + key
			);
		}

		if (!KibonAnfrageUtil.hasGesuchSteuerdatenResponseWithZpvOrAHVNummer(
			gesuch,
			dto.zpvNummer(),
			dto.sozialversicherungsNummer()
		)) {
			String message = String.format(
				"Die neue Veranlagung mit ZPV-Nummer: %s und mit AHV-Nummer: %s, konnte nicht mit einem gültigen Antragstellenden verlinkt werden.",
				dto.zpvNummer() != null ? dto.zpvNummer() : "",
				dto.sozialversicherungsNummer() != null ?
					dto.sozialversicherungsNummer().toString() :
					""
			);
			return Processing.failure(
				message
			);
		}

		GesuchstellerTyp gesuchstellerTyp = KibonAnfrageUtil
			.getGesuchstellerTypByGeburtsdatum(
				gesuch,
				dto.geburtsdatum()
			);

		if (gesuchstellerTyp == null) {
			return Processing.failure(
				"Die neue Veranlagung mit Geburtsdatum: "
					+ dto.geburtsdatum()
					+ ", konnte nicht mit einer gueltige Antragstellende verlinkt werden."
			);
		}

		if (hasNoSteuerschnittstelleZugriff(gesuchstellerTyp, gesuch)) {
			return Processing.failure(
				"Die Veranlagungsmitteilung wird nicht erstellt, weil die Zustimmung der Erziehungsberechtigten im aktuellen "
					+ "Gesuch/Mutation "
					+ gesuch.getId()
					+ " nicht gegeben ist."
			);
		}

		// erst die Massgegebenes Einkommens fuer das betroffenes Gesuch berechnen
		FinanzielleSituationResultateDTO finSitOriginalResult =
			finanzielleSituationService.calculateResultate(gesuch);
		KibonAnfrageContext kibonAnfrageContext = null;
		try {
			kibonAnfrageContext = kibonAnfrageHandler.handleKibonAnfrage(
				gesuch,
				gesuchstellerTyp
			);
		} catch (OIDCServiceException e) {
			return Processing.failure(
				"OIDC Server koennte nicht erreicht werden: " + key
			);
		}

		if (kibonAnfrageContext.getSteuerdatenAnfrageStatus() == null
			|| !kibonAnfrageContext.getSteuerdatenAnfrageStatus()
				.isSteuerdatenAbfrageErfolgreich()) {
			return Processing.failure("Keine neue Veranlagung gefunden");
		}

		// Nur RECHTSKRAEFTIGE SteuerResponse sind zu betrachten
		if (kibonAnfrageContext.getSteuerdatenAnfrageStatus()
			!= SteuerdatenAnfrageStatus.RECHTSKRAEFTIG) {
			return Processing.failure(
				"Die neue Veranlagung ist noch nicht Rechtskraeftig"
			);
		}

		FinanzielleSituationResultateDTO finSitNeuResult =
			finanzielleSituationService.calculateResultate(
				kibonAnfrageContext.getGesuch()
			);

		BigDecimal minUnterschiedEinkommen =
			getEinstelungMinUnterschiedEinkommen(
				kibonAnfrageContext.getGesuch().getGesuchsperiode()
			);
		BigDecimal unterschiedEinkommen = MathUtil.EXACT.subtract(
			finSitNeuResult.getMassgebendesEinkVorAbzFamGr(),
			finSitOriginalResult.getMassgebendesEinkVorAbzFamGr()
		);

		boolean isMarkierFuerKontroll = kibonAnfrageContext.getGesuch()
			.getMarkiertFuerKontroll();
		if (!checkBenachrichtigungRequired(
			isMarkierFuerKontroll,
			unterschiedEinkommen,
			minUnterschiedEinkommen
		)) {
			String unterschiedEinkommenString = unterschiedEinkommen
				.stripTrailingZeros()
				.toPlainString();
			return Processing.failure(
				String.format(
					"Keine Meldung erstellt. Das massgebende Einkommen hat sich um %s Franken verändert. Der "
						+ "konfigurierte Schwellenwert zur Benachrichtigung liegt bei %s Franken",
					unterschiedEinkommenString,
					minUnterschiedEinkommen
				)
			);
		}
		if (isAnyOfInBearbeitungGSOrFreigegeben(gesuch.getStatus())) {
			return updateWizardStepStatusAndGS(
				key,
				dto.zpvNummer(),
				dto.sozialversicherungsNummer()
			);
		}

		getAndRefreschGesuchFromDB(key);

		return createAndSendNeueVeranlagungsMitteilung(
			kibonAnfrageContext,
			dto.zpvNummer(),
			dto.sozialversicherungsNummer()
		);
	}

	private Gesuch getAndRefreschGesuchFromDB(@Nonnull String gesuchId) {
		Gesuch gesuchFromDB = persistence.find(Gesuch.class, gesuchId);
		persistence.getEntityManager().refresh(gesuchFromDB);
		return gesuchFromDB;
	}

	private Processing updateWizardStepStatusAndGS(
		@Nonnull String gesuchId,
		Integer zpvNummer,
		Long sozialversicherungsNummer
	) {
		WizardStep wizardStep = wizardStepService.findWizardStepFromGesuch(
			gesuchId,
			WizardStepName.FINANZIELLE_SITUATION
		);
		if (wizardStep.getWizardStepStatus().equals(WizardStepStatus.OK)) {
			wizardStep.setWizardStepStatus(WizardStepStatus.IN_BEARBEITUNG);
			wizardStepService.saveWizardStep(wizardStep);
		}
		Gesuch gesuchFromDB = getAndRefreschGesuchFromDB(gesuchId);
		if (Boolean.TRUE
			.equals(
				gesuchFromDB.getFamiliensituationContainer()
					.getFamiliensituationJA()
					.getGemeinsameSteuererklaerung()
			)
			|| (gesuchFromDB.getGesuchsteller1().getGesuchstellerJA() != null
				&& compareZpvAndAHVNummerWithGesuchsteller(
					gesuchFromDB.getGesuchsteller1().getGesuchstellerJA(),
					zpvNummer,
					sozialversicherungsNummer
				))) {
			gesuchFromDB.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.setSteuerdatenAbfrageStatus(
					SteuerdatenAnfrageStatus.NEUE_VERANLAGUNG
				);
			persistence.merge(
				gesuchFromDB.getGesuchsteller1()
					.getFinanzielleSituationContainer()
					.getFinanzielleSituationJA()
			);
		} else {
			gesuchFromDB.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.setSteuerdatenAbfrageStatus(
					SteuerdatenAnfrageStatus.NEUE_VERANLAGUNG
				);
			persistence.merge(
				gesuchFromDB.getGesuchsteller2()
					.getFinanzielleSituationContainer()
					.getFinanzielleSituationJA()
			);
		}

		return Processing.success();
	}

	private boolean compareZpvAndAHVNummerWithGesuchsteller(
		Gesuchsteller gesuchsteller,
		Integer zpvNummer,
		Long sozialversicherungsNummer
	) {
		return (gesuchsteller
			.getZpvNummer()
			!= null
			&& zpvNummer != null
			&& Integer.parseInt(
				gesuchsteller
					.getZpvNummer()
			) == zpvNummer)
			|| (gesuchsteller
				.getAhvNummer()
				!= null
				&& sozialversicherungsNummer != null
				&& Long.parseLong(
					gesuchsteller
						.getAhvNummer()
				) == sozialversicherungsNummer);
	}

	private boolean hasNoSteuerschnittstelleZugriff(
		GesuchstellerTyp gesuchstellerTyp,
		Gesuch gesuch
	) {
		var current = gesuchService.getNeustesVerfuegtesGesuchFuerGesuch(gesuch)
			.orElse(gesuch);
		var container = gesuchstellerTyp == GesuchstellerTyp.GESUCHSTELLER_1 ?
			current.getGesuchsteller1() :
			current.getGesuchsteller2();
		if (container == null) {
			throw new EbeguRuntimeException(
				"hasNoSteuerschnittstelleZugriff",
				"gesuchstellerTyp must point to a existing gesuchsteller"
			);
		}
		if (container.getFinanzielleSituationContainer() == null) {
			return true;
		}
		return Boolean.FALSE.equals(
			container.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getSteuerdatenZugriff()
		);
	}

	private boolean checkBenachrichtigungRequired(
		boolean isMarkierFuerKontroll,
		@Nonnull BigDecimal unterschiedEinkommen,
		@Nonnull BigDecimal minUnterschiedEinkommen
	) {
		// falls das Gesuch für die Kontrolle markiert ist, dann immer benachrichtigen
		if (isMarkierFuerKontroll) {
			return true;
		}
		// Falls neues Einkommen - altes Einkommen < 0 ist, dann würde der BG steigen. Immer benachrichtigen.
		if (unterschiedEinkommen.compareTo(BigDecimal.ZERO) < 0) {
			return true;
		}
		// falls neues Einkommen - altes Einkommen > 0 ist, dann würde der BG sinken.
		// nur benachrichtigen, wenn der Schwellenwert überschritten wird
		return unterschiedEinkommen.compareTo(minUnterschiedEinkommen) > 0;

	}

	private Processing createAndSendNeueVeranlagungsMitteilung(
		@Nonnull KibonAnfrageContext kibonAnfrageContext,
		Integer zpvNummer,
		Long ahvNummer
	) {
		Gesuch gesuch = kibonAnfrageContext.getGesuch();
		List<String> gesuchIds = gesuchService
			.getAllGesucheIdsForDossierAndPeriod(
				gesuch.getDossier(),
				gesuch.getGesuchsperiode()
			);

		Collection<NeueVeranlagungsMitteilung> open =
			mitteilungService
				.findOffeneNeueVeranlagungsmitteilungenForGesuch(
					gesuchIds
				);

		Optional<NeueVeranlagungsMitteilung> latest =
			findRelevantNeueVeranlagungsMitteilung(open, zpvNummer, ahvNummer);

		Locale locale = EbeguUtil.extractKorrespondenzsprache(
			gesuch,
			gemeindeService
		).getLocale();
		if (latest.isPresent()) {
			return Processing.failure(
				"Es wurde bereits eine offene Veranlagungsmitteilung"
					+ " zu dieser ZPV Nummer gefunden."
			);
		}

		NeueVeranlagungsMitteilung neueVeranlagungsMitteilung =
			new NeueVeranlagungsMitteilung();
		neueVeranlagungsMitteilung.setDossier(gesuch.getDossier());
		Objects.requireNonNull(kibonAnfrageContext.getSteuerdatenResponse());
		String betreffKey = gesuch.getMarkiertFuerKontroll() ?
			BETREFF_KEY_MARKIERT :
			BETREFF_KEY;
		String messageKey = gesuch.getMarkiertFuerKontroll() ?
			MESSAGE_KEY_MARKIERT :
			MESSAGE_KEY;
		neueVeranlagungsMitteilung.setSubject(
			ServerMessageUtil.getMessage(
				betreffKey,
				locale,
				gesuch.extractMandant(),
				gesuch.extractGemeinde(),
				String.valueOf(
					kibonAnfrageContext.getGesuch()
						.getGesuchsperiode()
						.getBasisJahr()
				),
				getGesuchstellendenAlsString(
					kibonAnfrageContext.getSteuerdatenResponse(),
					gesuch,
					locale
				),
				gesuch.getGesuchsperiode().getGesuchsperiodeString()
			)
		);
		neueVeranlagungsMitteilung.setMessage(
			ServerMessageUtil.getMessage(
				messageKey,
				locale,
				gesuch.extractMandant(),
				gesuch.extractGemeinde()
			)
		);
		neueVeranlagungsMitteilung.setSteuerdatenResponse(
			kibonAnfrageContext.getSteuerdatenResponse()
		);
		mitteilungService.sendNeueVeranlagungsmitteilung(
			neueVeranlagungsMitteilung
		);
		return Processing.success();
	}

	private String getGesuchstellendenAlsString(
		SteuerdatenResponse steuerdatenResponse,
		Gesuch gesuch,
		Locale locale
	) {
		if (Boolean.TRUE
			.equals(
				gesuch.getFamiliensituationContainer()
					.getFamiliensituationJA()
					.getGemeinsameSteuererklaerung()
			)) {
			return ServerMessageUtil.getMessage(
				MESSAGE_KEY_ANTRAGSTELLENDE_TITEL,
				locale,
				gesuch.extractMandant(),
				gesuch.extractGemeinde(),
				gesuch.getGesuchsteller1().getGesuchstellerJA().getFullName(),
				gesuch.getGesuchsteller2().getGesuchstellerJA().getFullName()
			);
		}
		if (gesuch.getGesuchsteller1() != null
			&& checkGS1MatchWithZPVOrAHVNummer(
				gesuch.getGesuchsteller1().getGesuchstellerJA(),
				steuerdatenResponse
			)) {
			return gesuch.getGesuchsteller1()
				.getGesuchstellerJA()
				.getFullName();
		}
		return gesuch.getGesuchsteller2().getGesuchstellerJA().getFullName();
	}

	private boolean checkGS1MatchWithZPVOrAHVNummer(
		@Nonnull Gesuchsteller gs1,
		SteuerdatenResponse steuerdatenResponse
	) {
		return (gs1.getZpvNummer() != null
			&&
			steuerdatenResponse.getZpvNrAntragsteller() != null
			&&
			steuerdatenResponse.getZpvNrAntragsteller()
				.equals(
					Integer.parseInt(
						gs1.getZpvNummer()
					)
				))
			|| (gs1.getAhvNummer() != null
				&&
				steuerdatenResponse.getSozialversicherungsNrAntragsteller()
					!= null
				&&
				steuerdatenResponse.getSozialversicherungsNrAntragsteller()
					.equals(
						Long.parseLong(
							gs1.getAhvNummer()
						)
					));
	}

	private Optional<NeueVeranlagungsMitteilung> findRelevantNeueVeranlagungsMitteilung(
		@Nonnull Collection<NeueVeranlagungsMitteilung> open,
		Integer zpvNummer,
		Long ahvNummer
	) {
		return open.stream()
			.filter(
				neueVeranlagungsMitteilung -> filterNeueVeranlagungsMitteilungByZPVOrAHVNummer(
					neueVeranlagungsMitteilung,
					zpvNummer,
					ahvNummer
				)
			)
			.findFirst();
	}

	private boolean filterNeueVeranlagungsMitteilungByZPVOrAHVNummer(
		NeueVeranlagungsMitteilung neueVeranlagungsMitteilung,
		Integer zpvNummer,
		Long ahvNummer
	) {
		return (zpvNummer != null
			&& zpvNummer.equals(
				neueVeranlagungsMitteilung
					.getSteuerdatenResponse()
					.getZpvNrAntragsteller()
			))
			|| (ahvNummer != null
				&& ahvNummer.equals(
					neueVeranlagungsMitteilung
						.getSteuerdatenResponse()
						.getSozialversicherungsNrAntragsteller()
				));
	}

	private BigDecimal getEinstelungMinUnterschiedEinkommen(
		Gesuchsperiode gesuchsperiode
	) {
		List<Einstellung> einstellungList = einstellungService
			.findEinstellungen(
				EinstellungKey.VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK,
				gesuchsperiode
			);

		if (einstellungList.size() != 1) {
			throw new EbeguRuntimeException(
				"NeueVeranlagungEventHandler: ",
				"Es sollte exakt eine Einstellung für den VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK und die "
					+ "Gesuchsperiode "
					+ gesuchsperiode.getGesuchsperiodeString()
					+ " gefunden werden"
			);
		}

		return einstellungList.get(0).getValueAsBigDecimal();
	}

	private boolean isAnyOfInBearbeitungGSOrFreigegeben(AntragStatus status) {
		return status == FREIGABEQUITTUNG
			|| status == IN_BEARBEITUNG_GS
			|| status == FREIGEGEBEN;
	}
}
