/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.services.mitteilung;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.betreuung.BetreuungEinstellungen;
import ch.dvbern.ebegu.betreuung.BetreuungEinstellungenService;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.enums.betreuung.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.util.BetreuungUtil;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.Gueltigkeit;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.betreuungsmitteilung.messages.AnwesenheitstageMessageFactory;
import ch.dvbern.ebegu.util.betreuungsmitteilung.messages.BetreuungsmitteilungPensumMessageFactory;
import ch.dvbern.ebegu.util.betreuungsmitteilung.messages.DefaultMessageFactory;
import ch.dvbern.ebegu.util.betreuungsmitteilung.messages.EingewoehnungMessageFactory;
import ch.dvbern.ebegu.util.betreuungsmitteilung.messages.KostenMessageFactory;
import ch.dvbern.ebegu.util.betreuungsmitteilung.messages.MahlzeitenKostenMessageFactory;
import ch.dvbern.ebegu.util.betreuungsmitteilung.messages.MittagstischMessageFactory;
import ch.dvbern.ebegu.util.betreuungsmitteilung.messages.SchulergaenzendeBetreuungMessageFactory;
import org.apache.commons.lang3.StringUtils;

import static ch.dvbern.ebegu.einstellung.EinstellungKey.*;
import static ch.dvbern.ebegu.enums.DemoFeatureTyp.INSTITUTIONSSCHLIESSUNG_MUTATIONSMELDUNG;
import static ch.dvbern.ebegu.util.betreuungsmitteilung.messages.BetreuungsmitteilungPensumMessageFactory.combine;
import static java.util.Objects.requireNonNull;

@ApplicationScoped
class MitteilungSharedServiceBean {

	private static final String MESSAGE_INSTITUTION_SCHLIESSUNG =
		"mutationsmeldung_message_institution_schliessung";

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Authorizer authorizer;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private BetreuungEinstellungenService betreuungEinstellungenService;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	void setSenderAndEmpfaengerAndCheckAuthorization(
		@Nonnull Mitteilung mitteilung
	) {
		Optional<Benutzer> currentBenutzer = benutzerService
			.getCurrentBenutzer();
		//wenn man direkt aus Kafka Event liest sind man nicht eingeloggt, aber man hat der Rolle SUPER_ADMIN
		if (currentBenutzer.isEmpty()
			&& !principalBean.isCallerInRole(UserRoleName.SUPER_ADMIN)) {
			throw new IllegalStateException("Benutzer ist nicht eingeloggt!");
		}
		if (currentBenutzer.isEmpty()
			&& principalBean.isCallerInRole(UserRoleName.SUPER_ADMIN)) {
			mitteilung.setEmpfaenger(
				getEmpfaengerBeiMitteilungAnGemeinde(mitteilung)
			);
		} else if (currentBenutzer.isPresent()) {
			switch (currentBenutzer.get().getRole()) {
			case GESUCHSTELLER: {
				mitteilung.setEmpfaenger(
					getEmpfaengerBeiMitteilungAnGemeinde(mitteilung)
				);
				mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
				mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.GESUCHSTELLER);
				break;
			}
			case ADMIN_INSTITUTION:
			case SACHBEARBEITER_INSTITUTION:
			case ADMIN_TRAEGERSCHAFT:
			case SACHBEARBEITER_TRAEGERSCHAFT: {
				mitteilung.setEmpfaenger(
					getEmpfaengerBeiMitteilungAnGemeinde(mitteilung)
				);
				mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
				mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.INSTITUTION);
				break;
			}
			case ADMIN_BG:
			case ADMIN_GEMEINDE:
				if (mitteilung instanceof Betreuungsmitteilung) {
					mitteilung.setEmpfaenger(
						getEmpfaengerBeiMitteilungAnGemeinde(mitteilung)
					);
					mitteilung.setEmpfaengerTyp(
						MitteilungTeilnehmerTyp.JUGENDAMT
					);
					mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
					break;
				}
			case SACHBEARBEITER_TS:
			case SACHBEARBEITER_BG:
			case SACHBEARBEITER_GEMEINDE:
			case ADMIN_TS: {
				if (mitteilung.getInstitution() != null) {
					//Bei Institution Mitteilungen sollen schon der Institution ID Bestimmt sein
					mitteilung.setEmpfaengerTyp(
						MitteilungTeilnehmerTyp.INSTITUTION
					);
				} else if (mitteilung.getFall().getSozialdienstFall() != null) {
					// Sozialdienst hat kein Empfanger
					mitteilung.setEmpfaengerTyp(
						MitteilungTeilnehmerTyp.SOZIALDIENST
					);
				} else {
					mitteilung.setEmpfaenger(
						mitteilung.getFall().getBesitzer()
					);
					mitteilung.setEmpfaengerTyp(
						MitteilungTeilnehmerTyp.GESUCHSTELLER
					);
				}
				mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
				break;
			}
			case ADMIN_SOZIALDIENST:
			case SACHBEARBEITER_SOZIALDIENST: {
				mitteilung.setEmpfaenger(
					getEmpfaengerBeiMitteilungAnGemeinde(mitteilung)
				);
				mitteilung.setEmpfaengerTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
				mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.SOZIALDIENST);
				break;
			}
			case SUPER_ADMIN: {
				// Superadmin kann als verschiedene Rollen Mitteilungen schicken
				if (mitteilung instanceof Betreuungsmitteilung) {
					mitteilung.setEmpfaenger(
						getEmpfaengerBeiMitteilungAnGemeinde(mitteilung)
					);
					mitteilung.setEmpfaengerTyp(
						MitteilungTeilnehmerTyp.JUGENDAMT
					);
					mitteilung.setSenderTyp(
						MitteilungTeilnehmerTyp.INSTITUTION
					);
				} else if (mitteilung.getBetreuung() != null) {
					//Die Betreuung ist gesetzt bei Mitteilungen an die Gemeinde, so ruckwirkend wird auch sein
					//es gibt keine Benutzer als empfanger
					mitteilung.setEmpfaengerTyp(
						MitteilungTeilnehmerTyp.INSTITUTION
					);
				} else if (mitteilung.getInstitution() != null) {
					//Bei Institution Mitteilungen sollen schon der Institution ID Bestimmt sein
					mitteilung.setEmpfaengerTyp(
						MitteilungTeilnehmerTyp.INSTITUTION
					);
					mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
				} else if (mitteilung.getFall().getSozialdienstFall() != null) {
					// Sozialdienst hat kein Empfanger
					mitteilung.setEmpfaengerTyp(
						MitteilungTeilnehmerTyp.SOZIALDIENST
					);
					mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
				} else {
					mitteilung.setEmpfaenger(
						mitteilung.getFall().getBesitzer()
					);
					mitteilung.setEmpfaengerTyp(
						MitteilungTeilnehmerTyp.GESUCHSTELLER
					);
					mitteilung.setSenderTyp(MitteilungTeilnehmerTyp.JUGENDAMT);
				}
			}
			}
			authorizer.checkWriteAuthorizationMitteilung(mitteilung);
			// Der Sender darf erst nach dem CHECK gesetzt werden! Sonst kann eine Mitteilung gekaptert werden
			mitteilung.setSender(currentBenutzer.get());
		}
	}

	Benutzer getEmpfaengerBeiMitteilungAnGemeinde(
		@Nonnull Mitteilung mitteilung
	) {
		Benutzer empfaenger = mitteilung.getDossier().getVerantwortlicherBG();
		if (empfaenger == null) {
			empfaenger = mitteilung.getDossier().getVerantwortlicherTS();
		}
		if (empfaenger == null) {
			String gemeindeId = mitteilung.getDossier().getGemeinde().getId();
			Optional<GemeindeStammdaten> stammdatenOptional =
				gemeindeService.getGemeindeStammdatenByGemeindeId(
					gemeindeId
				);
			if (stammdatenOptional.isPresent()) {
				// Wir kontrollieren bei den Mitteilungen explizit nicht, ob die Rolle stimmt!
				// Wir nehmen den Allgemeinen Default, weil wir auf der Mitteilung kein Gesuch haben
				// und daher nicht wissen, ob es ein reines BG- oder TS-Gesuch ist
				empfaenger = stammdatenOptional.get().getDefaultBenutzer();
			}
		}
		if (empfaenger == null) {
			throw new EbeguRuntimeException(
				"getEmpfaengerBeiMitteilungAnGemeinde",
				ErrorCodeEnum.ERROR_VERANTWORTLICHER_NOT_FOUND,
				mitteilung.getId()
			);
		}
		return empfaenger;
	}

	String createNachrichtForMutationsmeldung(
		@Nonnull Betreuungsmitteilung mitteilung,
		@Nonnull Set<BetreuungsmitteilungPensum> changedBetreuungen,
		@Nonnull Locale locale
	) {

		if (shouldSchliessungMessageAddiertWerden(mitteilung)) {
			String message = !mitteilung.isSchliessungMitteilung() ?
				createZeitabschnittNachrichtForMutationsmeldung(
					mitteilung,
					changedBetreuungen,
					locale
				) + StringUtils.LF + StringUtils.LF :
				"";
			return message + createSchliessungMeldung(mitteilung, locale);
		}

		return createZeitabschnittNachrichtForMutationsmeldung(
			mitteilung,
			changedBetreuungen,
			locale
		);
	}

	private String createSchliessungMeldung(
		@Nonnull Betreuungsmitteilung mitteilung,
		@Nonnull Locale locale
	) {
		Betreuung betreuung = requireNonNull(mitteilung.getBetreuung());
		Mandant mandant = betreuung.extractGesuch().extractMandant();
		return ServerMessageUtil.getMessage(
			MESSAGE_INSTITUTION_SCHLIESSUNG,
			locale,
			mandant,
			betreuung.getReferenzNummer(),
			Constants.DATE_FORMATTER.format(
				betreuung.getInstitutionStammdaten()
					.getGueltigkeit()
					.getGueltigBis()
			)
		);
	}

	private boolean shouldSchliessungMessageAddiertWerden(
		@Nonnull Betreuungsmitteilung mitteilung
	) {
		Betreuung betreuung = requireNonNull(mitteilung.getBetreuung());
		return applicationPropertyService.getActivatedDemoFeatures(
			betreuung.extractGesuch().extractMandant()
		)
			.contains(
				INSTITUTIONSSCHLIESSUNG_MUTATIONSMELDUNG
			)
			&&
			mitteilung.getBetreuung()
				.extractGesuchsperiode()
				.getGueltigkeit()
				.getGueltigBis()
				.isAfter(
					mitteilung.getBetreuung()
						.getInstitutionStammdaten()
						.getGueltigkeit()
						.getGueltigBis()
				);
	}

	private String createZeitabschnittNachrichtForMutationsmeldung(
		@Nonnull Betreuungsmitteilung mitteilung,
		@Nonnull Set<BetreuungsmitteilungPensum> changedBetreuungen,
		@Nonnull Locale locale
	) {

		List<BetreuungsmitteilungPensum> sorted = changedBetreuungen.stream()
			.sorted(Gueltigkeit.GUELTIG_AB_COMPARATOR)
			.collect(Collectors.toList());

		BetreuungsmitteilungPensumMessageFactory factory = messageFactory(
			mitteilung,
			locale
		);

		return IntStream.rangeClosed(1, sorted.size())
			.mapToObj(
				index -> factory.messageForPensum(
					index,
					sorted.get(index - 1)
				)
			)
			.collect(Collectors.joining(StringUtils.LF));
	}

	private BetreuungsmitteilungPensumMessageFactory messageFactory(
		@Nonnull Betreuungsmitteilung mitteilung,
		@Nonnull Locale locale
	) {
		Betreuung betreuung = requireNonNull(mitteilung.getBetreuung());
		Mandant mandant = betreuung.extractGesuch().extractMandant();

		if (betreuung.isAngebotMittagstisch()) {
			return new MittagstischMessageFactory(mandant, locale);
		}

		BetreuungEinstellungen einstellungen = betreuungEinstellungenService
			.getEinstellungen(betreuung);
		Einstellung einstellungAnzeigeTyp = einstellungService.findEinstellung(
			EinstellungKey.PENSUM_ANZEIGE_TYP,
			betreuung
		);
		BetreuungspensumAnzeigeTyp betreuungspensumAnzeigeTyp =
			getBetreuungspensumAnzeigeTyp(einstellungAnzeigeTyp);
		BigDecimal multiplier = getMultiplierForMutationsMitteilung(
			mitteilung,
			betreuungspensumAnzeigeTyp
		);

		var pensumFactory = new DefaultMessageFactory(
			mandant,
			locale,
			betreuungspensumAnzeigeTyp,
			multiplier
		);

		var kostenFactory = einstellungen.isMahlzeitenVerguenstigungEnabled() ?
			new MahlzeitenKostenMessageFactory(mandant, locale) :
			new KostenMessageFactory(mandant, locale);

		var anwesenheitstageProMonatFactory = einstellungen
			.isBetreuteTageEnabled() ?
				new AnwesenheitstageMessageFactory(mandant, locale) :
				BetreuungsmitteilungPensumMessageFactory.empty();

		var schulergaenzendeBetreuungFactory = einstellungen
			.isSchulergaenzendeBetreuungEnabled() ?
				new SchulergaenzendeBetreuungMessageFactory(
					mandant,
					locale
				) :
				BetreuungsmitteilungPensumMessageFactory.empty();

		return combine(
			" ",
			combine(
				", ",
				pensumFactory,
				anwesenheitstageProMonatFactory,
				kostenFactory,
				new EingewoehnungMessageFactory(mandant, locale)
			),
			schulergaenzendeBetreuungFactory
		);
	}

	@Nonnull
	private BetreuungspensumAnzeigeTyp getBetreuungspensumAnzeigeTyp(
		Einstellung einstellungAnzeigeTyp
	) {
		return BetreuungspensumAnzeigeTyp.valueOf(
			einstellungAnzeigeTyp.getValue()
		);
	}

	private BigDecimal getMultiplierForMutationsMitteilung(
		@Nonnull Betreuungsmitteilung mitteilung,
		@Nonnull BetreuungspensumAnzeigeTyp betreuungspensumAnzeigeTyp
	) {
		if (betreuungspensumAnzeigeTyp
			!= BetreuungspensumAnzeigeTyp.NUR_STUNDEN) {
			return BigDecimal.ONE;
		}

		Betreuung betreuung = requireNonNull(mitteilung.getBetreuung());
		if (betreuung.isAngebotKita()) {
			BigDecimal oeffnungstageKita = einstellungService
				.getEinstellungAsBigDecimal(OEFFNUNGSTAGE_KITA, betreuung);

			return BetreuungUtil.calculateOeffnungszeitPerMonthProcentual(
				MathUtil.EXACT.multiply(
					oeffnungstageKita,
					BetreuungUtil.ANZAHL_STUNDEN_PRO_TAG_KITA
				)
			);
		}

		BigDecimal oeffnungstageTFO = einstellungService
			.getEinstellungAsBigDecimal(OEFFNUNGSTAGE_TFO, betreuung);
		BigDecimal oeffnungsstundenTFO = einstellungService
			.getEinstellungAsBigDecimal(OEFFNUNGSSTUNDEN_TFO, betreuung);

		return MathUtil.DEFAULT.divide(
			MathUtil.DEFAULT.divide(
				MathUtil.DEFAULT.multiply(
					oeffnungstageTFO,
					oeffnungsstundenTFO
				),
				new BigDecimal(12)
			),
			new BigDecimal(100)
		);
	}

	public void setBetreuungsmitteilungSubject(
		Betreuungsmitteilung betreuungsmitteilung,
		Locale locale
	) {
		Betreuung betreuung = requireNonNull(
			betreuungsmitteilung.getBetreuung()
		);
		betreuungsmitteilung.setSubject(
			ServerMessageUtil.getMessage(
				betreuungsmitteilung.isSchliessungMitteilung() ?
					"mutationsmeldung_betreff_schliessung" :
					"mutationsmeldung_betreff",
				locale,
				betreuung.extractGemeinde().getMandant(),
				betreuung.extractGemeinde()
			)
		);
	}
}
