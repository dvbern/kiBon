/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.zahlungen;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlung_;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.Zahlungsauftrag_;
import ch.dvbern.ebegu.entities.Zahlungsposition;
import ch.dvbern.ebegu.entities.Zahlungsposition_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.MailService;
import ch.dvbern.ebegu.services.ZahlungService;
import ch.dvbern.ebegu.services.util.ZahlungslaufUtil;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelperFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.util.MathUtil.DEFAULT;
import static ch.dvbern.ebegu.util.MathUtil.isSame;

/**
 * Test-Service fuer Zahlungen. Es wird fuer alle Faelle die letzt gueltige Verfuegung verglichen mit den tatsaechlich
 * erfolgten
 * Zahlungen.
 * Einige Gesuche haben bekanntermassen falsche Auszahlungen gehabt. Diese werden entsprechend behandelt.
 */
@Stateless
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class ZahlungUeberpruefungServiceBean extends AbstractBaseService {

	private static final Logger LOGGER = LoggerFactory.getLogger(
		ZahlungUeberpruefungServiceBean.class.getSimpleName()
	);

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private MailService mailService;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private Persistence persistence;

	@Inject
	private ZahlungService zahlungService;

	public void pruefungZahlungen(
		@Nonnull Gemeinde gemeinde,
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String zahlungsauftragId,
		@Nonnull LocalDateTime datumLetzteZahlung,
		@Nonnull String beschrieb,
		@Nonnull Boolean auszahlungInZukunft
	) {
		ZahlungUeberpruefungContext zahlungUeberpruefungContext =
			new ZahlungUeberpruefungContext(
				gemeinde,
				beschrieb,
				zahlungsauftragId,
				ZahlungslaufHelperFactory
					.getZahlungslaufHelper(zahlungslaufTyp)
			);
		StopWatch stopWatch = logAndStartTimer(
			String.format(
				"Starte Zahlungsüberprüfung für %s",
				gemeinde.getName()
			)
		);

		// Die Whitelist lesen
		final String whitelistString =
			ebeguConfiguration.getEbeguZahlungenUeberpruefungWhitelist();
		if (StringUtils.isNotEmpty(whitelistString)) {
			zahlungUeberpruefungContext.setWhiteListOfReferenzNummmern(
				Arrays.asList(
					whitelistString.split(";")
				)
			);
		}

		Objects.requireNonNull(gemeinde);
		Objects.requireNonNull(zahlungsauftragId);
		Objects.requireNonNull(datumLetzteZahlung);
		zahlungUeberpruefungContext.resetAllPotentielleFehlerData();

		// Alle Gesuchsperioden im Status AKTIV und INAKTIV muessen geprueft werden, da auch rueckwirkend Korrekturen gemacht werden koennen.
		Collection<Gesuchsperiode> aktiveGesuchsperioden = gesuchsperiodeService
			.getAllAktivUndInaktivGesuchsperioden();

		// IST- und SOLL- Zustand lesen, vergleichen
		Zahlungsauftrag zahlungsauftrag = zahlungService.findZahlungsauftrag(
			zahlungsauftragId
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"findZahlungsauftrag",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND
				)
			);
		Collection<Gesuchsperiode> containedGesuchsperioden =
			ZahlungslaufUtil.findGesuchsperiodenContainedInZahlungsauftrag(
				aktiveGesuchsperioden,
				zahlungsauftrag
			);

		// Auf dem Front End gibt es z.B. bei Luzern eine Checkbox, die definiert, ob der Folgemonat auch ausbezahlt werden soll.
		// Falls die Checkbox aktiv ist, wird der Folgemonat auch ausbezahlt. z.B. ausloesen am 15.8. ergibt eine
		// Zahlung bis 30.09.
		zahlungUeberpruefungContext.setAnzahlMonateInZukunft(
			Boolean.TRUE.equals(auszahlungInZukunft) ? 1 : 0
		);

		ermittleIstAndSollAndCheckFuerGPs(
			containedGesuchsperioden,
			datumLetzteZahlung,
			zahlungUeberpruefungContext
		);

		logAndStopTimer(
			stopWatch,
			String.format(
				"Zahlungsüberprüfung für %s beendet: %s",
				gemeinde.getName(),
				(zahlungUeberpruefungContext.getPotentielleFehlerList()
					.isEmpty() ? "OK" : "ERROR")
			)
		);

		if (!zahlungUeberpruefungContext.getPotentielleFehlerList().isEmpty()) {
			sendeMail(zahlungUeberpruefungContext);
		} else {
			String auftragBezeichnung = "Zahlungslauf "
				+ zahlungUeberpruefungContext.getGemeinde().getName();
			LOGGER.info(
				"{}, Bezeichnung: {}: Keine Fehler gefunden",
				auftragBezeichnung,
				zahlungUeberpruefungContext.getBeschrieb()
			);
		}
		zahlungUeberpruefungContext.resetAllPotentielleFehlerData();
	}

	private void ermittleIstAndSollAndCheckFuerGPs(
		@Nonnull Collection<Gesuchsperiode> gesuchesperiodenToCheck,
		@Nonnull LocalDateTime datumLetzteZahlung,
		@Nonnull ZahlungUeberpruefungContext zahlungUeberpruefungContext
	) {
		for (Gesuchsperiode gesuchsperiode : gesuchesperiodenToCheck) {
			final String step = String.format(
				"Kontrolle für Gesuchsperiode %s für Gemeinde %s",
				gesuchsperiode.getGesuchsperiodeString(),
				zahlungUeberpruefungContext.getGemeinde().getName()
			);
			final StopWatch stopWatch = logAndStartTimer(step);
			final Map<String, List<Zahlungsposition>> zahlungenIstFuerGP =
				pruefeZahlungenIst(
					gesuchsperiode,
					zahlungUeberpruefungContext
				);
			pruefungZahlungenSollFuerGesuchsperiode(
				gesuchsperiode,
				datumLetzteZahlung,
				zahlungenIstFuerGP,
				zahlungUeberpruefungContext
			);
			logAndStopTimer(stopWatch, step);
		}
	}

	private void sendeMail(
		@Nonnull ZahlungUeberpruefungContext zahlungUeberpruefungContext
	) {
		LOGGER.info("ZAHLUNGSUEBERPRUEFUNG: Sende Mail...");
		String administratorMail = ebeguConfiguration.getAdministratorMail();
		if (StringUtils.isEmpty(administratorMail)) {
			LOGGER.warn(
				"ZAHLUNGSUEBERPRUEFUNG: Es ist keine Administrator-Email konfiguriert. Sende keine E-Mail ueber den Zahlungspruefungs-Status"
			);
			return;
		}
		try {
			final String serverName = new URI(
				ebeguConfiguration.getFrontendBaseUrl(
					zahlungUeberpruefungContext.getGemeinde()
						.getMandant()
						.getMandantIdentifier()
				)
			).getHost();
			final String typ = ServerMessageUtil.translateEnumValue(
				zahlungUeberpruefungContext.getZahlungslaufHelper()
					.getZahlungslaufTyp(),
				Locale.GERMAN,
				zahlungUeberpruefungContext.getGemeinde().getMandant()
			);
			final MandantIdentifier mandantIdentifier =
				zahlungUeberpruefungContext.getGemeinde()
					.getMandant()
					.getMandantIdentifier();
			String auftragBezeichnung = "Zahlungslauf "
				+ zahlungUeberpruefungContext.getGemeinde().getName()
				+ " ("
				+ serverName
				+ ", "
				+ typ
				+ ')';
			String autragResult = "Pending";

			StringBuilder sb = new StringBuilder();
			sb.append("Zusammenfassung: \n");
			for (String s : zahlungUeberpruefungContext
				.getPotenzielleFehlerListZusammenfassung()) {
				sb.append(s);
				sb.append('\n');
			}
			sb.append("Zusammenfassung Ende\n");

			for (String s : zahlungUeberpruefungContext
				.getPotentielleFehlerList()) {
				sb.append(s);
				sb.append("\n*************************************\n");
			}
			mailService.toOutboxMail(
				auftragBezeichnung
					+ ": Potentieller Fehler im Zahlungslauf",
				sb.toString(),
				administratorMail,
				mandantIdentifier
			);
			autragResult = "Bezeichnung: "
				+ zahlungUeberpruefungContext.getBeschrieb()
				+ ": Potentieller Fehler im Zahlungslauf: "
				+ sb;
			autragResult = StringUtils.abbreviate(
				autragResult,
				Constants.DB_TEXTAREA_LENGTH
			);
			// Erst jetzt den Zahlungsauftrag lesen bzw. updaten, wegen OptimisticLockExceptions,
			updateZahlungsauftragResult(
				zahlungUeberpruefungContext.getZahlungsauftragId(),
				autragResult
			);

		} catch (URISyntaxException e) {
			throw new EbeguRuntimeException(
				"getFrontendBaseUrl",
				"Frontend Basis URL konnte nicht geparst werden",
				e
			);
		}
		LOGGER.info("ZAHLUNGSUEBERPRUEFUNG: ... sende Mail beendet");
	}

	private void updateZahlungsauftragResult(
		@Nonnull String zahlungsauftragId,
		@Nonnull String autragResult
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaUpdate<Zahlungsauftrag> criteriaUpdate = cb
			.createCriteriaUpdate(Zahlungsauftrag.class);
		Root<Zahlungsauftrag> employeeRoot = criteriaUpdate.from(
			Zahlungsauftrag.class
		);
		criteriaUpdate.set(
			employeeRoot.get(Zahlungsauftrag_.result),
			autragResult
		);
		criteriaUpdate.where(
			cb.equal(
				employeeRoot.get(Zahlungsauftrag_.id),
				zahlungsauftragId
			)
		);
		persistence.getEntityManager()
			.createQuery(criteriaUpdate)
			.executeUpdate();
	}

	private void pruefungZahlungenSollFuerGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull LocalDateTime datumLetzteZahlung,
		@Nonnull Map<String, List<Zahlungsposition>> zahlungenIstMap,
		@Nonnull ZahlungUeberpruefungContext zahlungUeberpruefungContext
	) {

		final String step = String.format(
			"Prüfe SOLL für GP %s und Gemeinde %s",
			gesuchsperiode.getGesuchsperiodeString(),
			zahlungUeberpruefungContext.getGemeinde().getName()
		);
		final StopWatch stopWatch = logAndStartTimer(step);
		final Collection<Gesuch> neuesteVerfuegteGesuche =
			gesuchService
				.getNeuesteVerfuegtesGesuchProDossierFuerGemeindeUndGesuchsperiode(
					gesuchsperiode,
					zahlungUeberpruefungContext.getGemeinde()
				);
		for (Gesuch gesuch : neuesteVerfuegteGesuche) {
			pruefeZahlungenSollFuerGesuch(
				gesuch,
				datumLetzteZahlung,
				zahlungenIstMap,
				zahlungUeberpruefungContext
			);
		}
		logAndStopTimer(stopWatch, step);
	}

	private void pruefeZahlungenSollFuerGesuch(
		@Nonnull Gesuch gesuch,
		@Nonnull LocalDateTime datumLetzteZahlung,
		@Nonnull Map<String, List<Zahlungsposition>> zahlungenIstMap,
		@Nonnull ZahlungUeberpruefungContext zahlungUeberpruefungContext
	) {
		Objects.requireNonNull(datumLetzteZahlung);

		if (gesuch.getStatus() == AntragStatus.NUR_SCHULAMT) {
			return;
		}
		if (gesuch.getTimestampVerfuegt() == null) {
			LOGGER.error(
				"ZAHLUNGSUEBERPRUEFUNG: timestampVerfuegt ist null beim Auszahlen: {} - {}",
				gesuch.getId(),
				gesuch.getJahrFallAndGemeindenummer()
			);
			return;
		}
		// Nur Gesuche, die VOR der letzten Zahlung verfuegt wurden, sind relevant
		if (gesuch.getTimestampVerfuegt().isBefore(datumLetzteZahlung)) {
			LocalDate dateAusbezahltBis = datumLetzteZahlung.toLocalDate()
				.plusMonths(
					zahlungUeberpruefungContext.getAnzahlMonateInZukunft()
				)
				.with(TemporalAdjusters.lastDayOfMonth());
			for (Betreuung betreuung : gesuch.extractAllBetreuungen()) {
				pruefeZahlungenSollFuerBetreuung(
					betreuung,
					dateAusbezahltBis,
					zahlungenIstMap,
					zahlungUeberpruefungContext
				);
			}
		}
	}

	private void pruefeZahlungenSollFuerBetreuung(
		@Nonnull Betreuung betreuung,
		@Nonnull LocalDate dateAusbezahltBis,
		@Nonnull Map<String, List<Zahlungsposition>> zahlungenIstMap,
		@Nonnull ZahlungUeberpruefungContext zahlungUeberpruefungContext
	) {
		// Nur die "gueltige" Betreuung beachten und nur, wenn es KITA oder TAGESFAMILIEN ist
		if (betreuung.isAngebotAuszuzahlen()) {
			if (!betreuung.isGueltig()) {
				// Es gibt eine spätere Verfügung, deren Gesuch aber noch nicht (komplett) verfügt ist
				Optional<Betreuung> gueltigeBetreuungOptional =
					betreuungService
						.findSameBetreuungInDifferentGesuchsperiode(
							betreuung.extractGesuchsperiode(),
							betreuung.extractGesuch().getDossier(),
							betreuung.getBetreuungNummer(),
							betreuung.getKind().getKindNummer()
						);
				if (gueltigeBetreuungOptional.isPresent()) {
					betreuung = gueltigeBetreuungOptional.get();
				} else {
					if (betreuung.getBetreuungsstatus()
						== Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG) {
						LOGGER.warn(
							"ZAHLUNGSUEBERPRUEFUNG: Die Betreuung war neu im letzten Antrag, wurde aber ohne Verfuegung geschlossen: {}",
							betreuung.getReferenzNummer()
						);
					} else {
						zahlungUeberpruefungContext.getPotentielleFehlerList()
							.add(
								"Keine gueltige Betreuung gefunden fuer BG "
									+ betreuung.getReferenzNummer()
							);
					}
				}
			}
			// Jetzt kann es immer noch sein, dass es zwar die gueltige Verfuegung, aber mit NICHT_EINTRETEN ist
			if (betreuung.getBetreuungsstatus()
				!= Betreuungsstatus.NICHT_EINGETRETEN) {
				vergleicheSollIst(
					betreuung,
					dateAusbezahltBis,
					zahlungenIstMap,
					zahlungUeberpruefungContext
				);
			}
		}
	}

	private void vergleicheSollIst(
		@Nonnull Betreuung betreuung,
		@Nonnull LocalDate dateAusbezahltBis,
		@Nonnull Map<String, List<Zahlungsposition>> zahlungenIstMap,
		@Nonnull ZahlungUeberpruefungContext zahlungUeberpruefungContext
	) {
		final String referenzNummer = betreuung.getReferenzNummer();
		if (zahlungUeberpruefungContext.getWhiteListOfReferenzNummmern()
			.contains(referenzNummer)) {
			LOGGER.warn(
				"ZAHLUNGSUEBERPRUEFUNG: Betreuung in Whitelist gefunden, breche Ueberpruefung ab: {}",
				betreuung.getReferenzNummer()
			);
			return;
		}

		BigDecimal betragSoll = getBetragSoll(
			betreuung,
			dateAusbezahltBis,
			zahlungUeberpruefungContext
		);
		BigDecimal betragIst = getBetragIst(betreuung, zahlungenIstMap);

		if (!isSame(betragSoll, betragIst)) {
			List<VerfuegungZeitabschnitt> ausbezahlteAbschnitte =
				getAusbezahlteZeitabschnitte(betreuung, dateAusbezahltBis);
			logPossibleError(
				betreuung,
				ausbezahlteAbschnitte,
				betragSoll,
				betragIst,
				zahlungenIstMap,
				zahlungUeberpruefungContext
			);
		}
	}

	private void logPossibleError(
		@Nonnull Betreuung betreuung,
		@Nonnull List<VerfuegungZeitabschnitt> ausbezahlteAbschnitte,
		@Nonnull BigDecimal betragSoll,
		@Nonnull BigDecimal betragIst,
		@Nonnull Map<String, List<Zahlungsposition>> zahlungenIstMap,
		@Nonnull ZahlungUeberpruefungContext zahlungUeberpruefungContext
	) {
		StringBuilder sb = new StringBuilder();
		BigDecimal differenz = DEFAULT.subtract(betragIst, betragSoll);
		sb.append("Soll und Ist nicht identisch: ")
			.append(betreuung.getReferenzNummer())
			.append(" Soll: ")
			.append(betragSoll)
			.append(" Ist: ")
			.append(betragIst)
			.append('\n')
			.append(" Differenz: ")
			.append(differenz)
			.append('\n');
		sb.append("Aktuell gueltige Betreuung: ")
			.append(betreuung.getId())
			.append('\n');
		sb.append("Vergangene Zeitabschnitte").append('\n');
		ausbezahlteAbschnitte.sort(
			Comparator.comparing(o -> o.getGueltigkeit().getGueltigAb())
		);
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : ausbezahlteAbschnitte) {
			sb.append(verfuegungZeitabschnitt.getGueltigkeit().toRangeString())
				.append(", ");
			sb.append(
				zahlungUeberpruefungContext.getZahlungslaufHelper()
					.getAuszahlungsbetrag(
						verfuegungZeitabschnitt
					)
			).append(", ");
			sb.append(
				zahlungUeberpruefungContext.getZahlungslaufHelper()
					.getZahlungsstatus(
						verfuegungZeitabschnitt
					)
			).append('\n');
		}
		sb.append("Zahlungspositionen: \n");
		List<Zahlungsposition> zahlungspositions = zahlungenIstMap.get(
			betreuung.getReferenzNummer()
		);
		if (zahlungspositions != null) {
			zahlungspositions.sort(
				Comparator.comparing(
					o -> o.getVerfuegungZeitabschnitt()
						.getGueltigkeit()
						.getGueltigAb()
				)
			);
			for (Zahlungsposition zahlungsposition : zahlungspositions) {
				sb.append(
					zahlungsposition.getVerfuegungZeitabschnitt()
						.getGueltigkeit()
						.toRangeString()
				).append(", ");
				String trennzeichen = ", \t";
				sb.append(zahlungsposition.getBetrag()).append(trennzeichen);
				sb.append(zahlungsposition.getStatus()).append(trennzeichen);
				sb.append(
					zahlungUeberpruefungContext.getZahlungslaufHelper()
						.getZahlungsstatus(
							zahlungsposition.getVerfuegungZeitabschnitt()
						)
				).append(trennzeichen);
				sb.append("Ausbezahlt am: ")
					.append(
						zahlungsposition.getZahlung()
							.getZahlungsauftrag()
							.getDatumGeneriert()
					)
					.append(trennzeichen);
				sb.append("ignoriert=")
					.append(zahlungsposition.isIgnoriert())
					.append('\n');
			}
		}
		zahlungUeberpruefungContext.getPotentielleFehlerList()
			.add(sb.toString());
		zahlungUeberpruefungContext.getPotenzielleFehlerListZusammenfassung()
			.add(
				betreuung.getReferenzNummer() + ": " + differenz
			);
		LOGGER.warn("ZAHLUNGSUEBERPRUEFUNG: {}", sb);
	}

	@Nonnull
	private List<VerfuegungZeitabschnitt> getAusbezahlteZeitabschnitte(
		@Nonnull Betreuung betreuung,
		@Nonnull LocalDate dateAusbezahltBis
	) {
		List<VerfuegungZeitabschnitt> ausbezahlteAbschnitte = new ArrayList<>();
		if (betreuung.getVerfuegung() != null) {
			for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : betreuung
				.getVerfuegung()
				.getZeitabschnitte()) {
				if (!verfuegungZeitabschnitt.getGueltigkeit()
					.getGueltigBis()
					.isAfter(dateAusbezahltBis)) {
					// Dieser Zeitabschnitt muesste ausbezahlt sein
					ausbezahlteAbschnitte.add(verfuegungZeitabschnitt);
				}
			}
		}
		return ausbezahlteAbschnitte;
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	@Nonnull
	private BigDecimal getBetragSoll(
		@Nonnull Betreuung betreuung,
		@Nonnull LocalDate dateAusbezahltBis,
		@Nonnull ZahlungUeberpruefungContext zahlungUeberpruefungContext
	) {
		BigDecimal betragSoll = BigDecimal.ZERO;
		if (betreuung.getVerfuegung() != null) {
			for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : betreuung
				.getVerfuegung()
				.getZeitabschnitte()) {
				if (zahlungUeberpruefungContext.getZahlungslaufHelper()
					.isAuszuzahlen(verfuegungZeitabschnitt)
					&& !verfuegungZeitabschnitt.getGueltigkeit()
						.getGueltigBis()
						.isAfter(dateAusbezahltBis)) {
					// Dieser Zeitabschnitt muesste ausbezahlt sein
					betragSoll = DEFAULT.add(
						betragSoll,
						zahlungUeberpruefungContext.getZahlungslaufHelper()
							.getAuszahlungsbetrag(
								verfuegungZeitabschnitt
							)
					);

				}
			}
		}
		return betragSoll;
	}

	@Nonnull
	private BigDecimal getBetragIst(
		@Nonnull Betreuung betreuung,
		@Nonnull Map<String, List<Zahlungsposition>> zahlungenIstMap
	) {
		BigDecimal betragIst = BigDecimal.ZERO;
		if (zahlungenIstMap.containsKey(betreuung.getReferenzNummer())) {
			List<Zahlungsposition> zahlungspositionList = zahlungenIstMap.get(
				betreuung.getReferenzNummer()
			);
			for (Zahlungsposition zahlungsposition : zahlungspositionList) {
				betragIst = DEFAULT.add(
					betragIst,
					zahlungsposition.getBetrag()
				);
			}
		}
		return betragIst;
	}

	@Nonnull
	private Map<String, List<Zahlungsposition>> pruefeZahlungenIst(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull ZahlungUeberpruefungContext zahlungUeberpruefungContext
	) {
		Map<String, List<Zahlungsposition>> zahlungenIst = new HashMap<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Zahlungsposition> query = cb.createQuery(
			Zahlungsposition.class
		);

		Root<Zahlungsposition> root = query.from(Zahlungsposition.class);
		Join<Zahlungsposition, Zahlung> joinZahlung = root.join(
			Zahlungsposition_.zahlung
		);
		Join<Zahlung, Zahlungsauftrag> joinZahlungsauftrag = joinZahlung.join(
			Zahlung_.zahlungsauftrag
		);
		Join<Zahlungsposition, VerfuegungZeitabschnitt> joinZeitabschnitt = root
			.join(Zahlungsposition_.verfuegungZeitabschnitt);

		Predicate predicateGemeinde = cb.equal(
			joinZahlungsauftrag.get(Zahlungsauftrag_.gemeinde),
			zahlungUeberpruefungContext.getGemeinde()
		);
		Predicate predicateAuftragTyp = cb.equal(
			joinZahlungsauftrag.get(Zahlungsauftrag_.zahlungslaufTyp),
			zahlungUeberpruefungContext.getZahlungslaufHelper()
				.getZahlungslaufTyp()
		);

		Predicate predicateStart = cb.greaterThanOrEqualTo(
			joinZeitabschnitt.get(AbstractDateRangedEntity_.gueltigkeit)
				.get(DateRange_.gueltigAb),
			gesuchsperiode.getGueltigkeit().getGueltigAb()
		);
		Predicate predicateEnd = cb.lessThanOrEqualTo(
			joinZeitabschnitt.get(AbstractDateRangedEntity_.gueltigkeit)
				.get(DateRange_.gueltigBis),
			gesuchsperiode.getGueltigkeit().getGueltigBis()
		);

		query.where(
			predicateGemeinde,
			predicateAuftragTyp,
			predicateStart,
			predicateEnd
		);
		Collection<Zahlungsposition> zahlungspositionList = persistence
			.getCriteriaResults(query);

		for (Zahlungsposition zahlungsposition : zahlungspositionList) {
			addToZahlungenList(zahlungenIst, zahlungsposition);
		}
		return zahlungenIst;
	}

	private void addToZahlungenList(
		@Nonnull Map<String, List<Zahlungsposition>> zahlungenIst,
		@Nonnull Zahlungsposition zahlungsposition
	) {
		Objects.requireNonNull(
			zahlungsposition.getVerfuegungZeitabschnitt()
				.getVerfuegung()
				.getBetreuung()
		);
		AbstractPlatz abstractPlatz = zahlungsposition
			.getVerfuegungZeitabschnitt()
			.getVerfuegung()
			.getBetreuung();
		String key = abstractPlatz.getReferenzNummer();
		if (!zahlungenIst.containsKey(key)) {
			zahlungenIst.put(key, new ArrayList<>());
		}
		zahlungenIst.get(key).add(zahlungsposition);
	}

	@Nonnull
	private StopWatch logAndStartTimer(@Nonnull String info) {
		LOGGER.info("ZAHLUNGSUEBERPRUEFUNG: Starting Step '{}'", info);
		return StopWatch.createStarted();
	}

	private void logAndStopTimer(
		@Nonnull StopWatch stopWatch,
		@Nonnull String info
	) {
		stopWatch.stop();
		final long millis = stopWatch.getTime(TimeUnit.MILLISECONDS);
		String timeInHHMMSS = DurationFormatUtils.formatDuration(
			millis,
			"HH:mm:ss.SSS",
			true
		);
		LOGGER.info(
			"ZAHLUNGSUEBERPRUEFUNG: Step '{}' took {}",
			info,
			timeInHHMMSS
		);
	}
}
