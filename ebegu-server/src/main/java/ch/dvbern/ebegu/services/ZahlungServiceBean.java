/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.Pain001Dokument;
import ch.dvbern.ebegu.entities.Pain001Dokument_;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt_;
import ch.dvbern.ebegu.entities.Verfuegung_;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.Zahlungsauftrag_;
import ch.dvbern.ebegu.entities.Zahlungsposition;
import ch.dvbern.ebegu.entities.Zahlungsposition_;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.VerfuegungsZeitabschnittZahlungsstatus;
import ch.dvbern.ebegu.enums.ZahlungStatus;
import ch.dvbern.ebegu.enums.ZahlungauftragStatus;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.enums.ZahlungspositionStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelper;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelperFactory;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Service fuer Zahlungen. Die Zahlungen werden folgendermassen generiert:
 * Wenn ein neuer Zahlungsauftrag erstellt wird, muss nur ein Faelligkeitsdatum mitgegeben werden. Dieses wird *nur* fuer
 * das Abfuellen des XML Files (ISO-20022) verwendet. Fuer die Ermittlung der einzuschliessenden Zahlungsdetail wird
 * immer der aktuelle Timestamp verwendet. Dies, damit wir fuer den naechsten Zahlungsauftrag immer wissen, welche
 * Zahlungen bereits beruecksichtigt wurden.
 * Wir muessen mit 2 Zeitraeumen arbeiten:
 * |      Jan      |      Feb       |
 * |                  |
 * letzter            aktueller
 * Zahlungslauf		Zahlungslauf
 * Für die Ermittlung der "normalen" Zahlungen wird immer (mind.) ein ganzer Monat berücksichtigt, und zwar der aktuelle
 * Monat des Zahlungslaufes plus fruehere Monate, falls in diesen kein Zahlungslauf stattfand.
 * Für die Ermittlung der Korrektur-Zahlungen muessen alle Verfuegungen berücksichtigt werden, welche seit dem letzten
 * Zahlungslauf bis heute dazugekommen sind.
 */
@Stateless
@Local(ZahlungService.class)
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "SpringAutowiredFieldsWarningInspection", "InstanceMethodNamingConvention", "PMD.NcssMethodCount" })
public class ZahlungServiceBean extends AbstractBaseService implements ZahlungService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZahlungServiceBean.class.getSimpleName());

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private VerfuegungService verfuegungService;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private GeneratedDokumentService generatedDokumentService;

	@Inject
	private ZahlungUeberpruefungServiceBean zahlungUeberpruefungServiceBean;

	@Inject
	private Authorizer authorizer;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private GemeindeService gemeindeService;


	@Override
	@Nonnull
	public Zahlungsauftrag zahlungsauftragErstellen(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String gemeindeId,
		@Nonnull LocalDate datumFaelligkeit,
		@Nonnull String beschreibung
	) {
		return zahlungsauftragErstellen(
			zahlungslaufTyp,
			gemeindeId,
			datumFaelligkeit,
			beschreibung,
			LocalDateTime.now());
	}

	@Override
	@Nonnull
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public Zahlungsauftrag zahlungsauftragErstellen(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String gemeindeId,
		@Nonnull LocalDate datumFaelligkeit,
		@Nonnull String beschreibung,
		@Nonnull LocalDateTime datumGeneriert
	) {
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId).orElseThrow(() -> new EbeguEntityNotFoundException("zahlungsauftragErstellen",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId));
		authorizer.checkWriteAuthorization(gemeinde);

		// Es darf immer nur ein Zahlungsauftrag im Status ENTWURF sein
		Optional<Zahlungsauftrag> lastZahlungsauftrag = findLastZahlungsauftrag(zahlungslaufTyp, gemeinde);
		if (lastZahlungsauftrag.isPresent() && lastZahlungsauftrag.get().getStatus().isEntwurf()) {
			throw new EbeguRuntimeException(KibonLogLevel.DEBUG, "zahlungsauftragErstellen", ErrorCodeEnum.ERROR_ZAHLUNG_ERSTELLEN);
		}

		LOGGER.info("Erstelle Zahlungsauftrag mit Faelligkeit: {}", Constants.DATE_FORMATTER.format(datumFaelligkeit));
		Zahlungsauftrag zahlungsauftrag = new Zahlungsauftrag();
		zahlungsauftrag.setZahlungslaufTyp(zahlungslaufTyp);
		zahlungsauftrag.setStatus(ZahlungauftragStatus.ENTWURF);
		zahlungsauftrag.setBeschrieb(beschreibung);
		zahlungsauftrag.setDatumFaellig(datumFaelligkeit);
		zahlungsauftrag.setDatumGeneriert(datumGeneriert);
		zahlungsauftrag.setGemeinde(gemeinde);

		// Alle aktuellen (d.h. der letzte Antrag jedes Falles) Verfuegungen suchen, welche ein Kita-Angebot haben
		// Wir brauchen folgende Daten:
		// - Zeitraum, welcher fuer die (normale) Auszahlung gilt: Immer ganzer Monat, mindestens der Monat des DatumFaellig,
		// 		jedoch seit Ende Monat des letzten Auftrags -> 1 oder mehrere ganze Monate
		// - Zeitraum, welcher fuer die Berechnung der rueckwirkenden Korrekturen gilt: Zeitpunkt der letzten Zahlungserstellung bis aktueller Zeitpunkt
		// 		(Achtung: Es ist *nicht* das Faelligkeitsdatum relevant, sondern das Erstellungsdatum des letzten Auftrags!)
		// Den letzten Zahlungsauftrag lesen
		LocalDateTime lastZahlungErstellt = Constants.START_OF_DATETIME; // Default, falls dies der erste Auftrag ist
		// Falls es eine Wiederholung des Auftrags ist, muessen nur noch die Korrekturen beruecksichtigt werden, welche
		// seit dem letzten Auftrag erstellt wurden
		boolean isRepetition = false;
		// Falls fuer denselben Zeitraum (oder den letzten Teil davon) schon ein Auftrag vorhanden ist, kann das DatumVon nicht
		// einfach an den letzten Auftrag anschliessen
		LocalDate zeitabschnittVon;
		LocalDate zeitabschnittBis = zahlungsauftrag.getDatumGeneriert().toLocalDate().with(TemporalAdjusters.lastDayOfMonth());

		if (lastZahlungsauftrag.isPresent()) {
			lastZahlungErstellt = lastZahlungsauftrag.get().getDatumGeneriert();
			if (zahlungsauftrag.getDatumGeneriert().toLocalDate().isAfter(lastZahlungsauftrag.get().getGueltigkeit().getGueltigBis())) {
				// Wir beginnen am Anfang des Folgemonats des letzten Auftrags
				zeitabschnittVon = lastZahlungErstellt.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).toLocalDate();
			} else {
				// Repetition, dh.der Monat ist schon ausgeloest. Wir nehmen den Anfang des Monats
				zeitabschnittVon = lastZahlungErstellt.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate();
				isRepetition = true;
			}
		} else {
			zeitabschnittVon = lastZahlungErstellt.toLocalDate();
		}

		zahlungsauftrag.setGueltigkeit(new DateRange(zeitabschnittVon, zeitabschnittBis));

		Map<String, Zahlung> zahlungProInstitution = new HashMap<>();
		ZahlungslaufHelper zahlungslaufHelper = ZahlungslaufHelperFactory.getZahlungslaufHelper(zahlungslaufTyp);

		// "Normale" Zahlungen
		if (!isRepetition) {
			LOGGER.info("Ermittle normale Zahlungen im Zeitraum {}", zahlungsauftrag.getGueltigkeit().toRangeString());
			Collection<VerfuegungZeitabschnitt> gueltigeVerfuegungZeitabschnitte = getGueltigeVerfuegungZeitabschnitte(gemeinde, zeitabschnittVon,
				zeitabschnittBis);
			for (VerfuegungZeitabschnitt zeitabschnitt : gueltigeVerfuegungZeitabschnitte) {
				if (zahlungslaufHelper.isAuszuzahlen(zeitabschnitt) && zahlungslaufHelper.getZahlungsstatus(zeitabschnitt).isNeu()) {
					createZahlungsposition(zahlungslaufHelper, zeitabschnitt, zahlungsauftrag, zahlungProInstitution);
				}
			}
		}
		// Korrekturen und Nachzahlungen
		// Stichtag: Falls es eine Wiederholung des Auftrags ist, wurde der aktuelle Monat bereits ausbezahlt.
		LocalDate stichtagKorrekturen = isRepetition ? zeitabschnittBis : zeitabschnittBis.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
		// Die Korrekturzahlungen werden seit dem letzten Zahlungsauftrag beruecksichtigt. Falls wir im TEST-Mode sind
		// und ein fiktives "DatumGeneriert" gewaehlt haben, nehmen wir als Datum des letzten Auftrags das timestampErstellt
		// und nicht das (eventuell ebenfalls fiktive) datumGeneriert.
		boolean isTestMode = ebeguConfiguration.getIsZahlungenTestMode();
		if (isTestMode) {
			lastZahlungErstellt = Constants.START_OF_DATETIME;
		}
		Collection<VerfuegungZeitabschnitt> verfuegungsZeitabschnitte = getVerfuegungsZeitabschnitteNachVerfuegungDatum(gemeinde, lastZahlungErstellt,
			zahlungsauftrag.getDatumGeneriert(), stichtagKorrekturen);
		for (VerfuegungZeitabschnitt zeitabschnitt : verfuegungsZeitabschnitte) {
			// Zu behandeln sind alle, die NEU, VERRECHNEND oder IGNORIEREND sind
			if (zahlungslaufHelper.isAuszuzahlen(zeitabschnitt)
				&& (zahlungslaufHelper.getZahlungsstatus(zeitabschnitt).isZuBehandelnInZahlungslauf()
					|| zahlungslaufHelper.getZahlungsstatus(zeitabschnitt) == VerfuegungsZeitabschnittZahlungsstatus.IGNORIERT)) {
				createZahlungspositionenKorrekturUndNachzahlung(zahlungslaufHelper, zeitabschnitt, zahlungsauftrag, zahlungProInstitution);
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Zahlungsauftrag generiert: ").append(zahlungsauftrag.getGueltigkeit().toRangeString());
		if (isRepetition) {
			sb.append(" [Repetition]");
		}
		LOGGER.info(sb.toString());
		calculateZahlungsauftrag(zahlungsauftrag);
		Zahlungsauftrag persistedAuftrag = persistence.merge(zahlungsauftrag);

		return persistedAuftrag;
	}

	/**
	 * Zahlungsauftrag wird einmalig berechnet. Danach koennen nur noch die Stammdaten der Institutionen
	 * geaendert werden.
	 */
	private void calculateZahlungsauftrag(Zahlungsauftrag zahlungsauftrag) {
		BigDecimal totalAuftrag = BigDecimal.ZERO;
		boolean hasAnyZahlungWithNegativTotal = false;
		for (Zahlung zahlung : zahlungsauftrag.getZahlungen()) {
			BigDecimal totalZahlung = BigDecimal.ZERO;
			for (Zahlungsposition zahlungsposition : zahlung.getZahlungspositionen()) {
				if (!zahlungsposition.isIgnoriert()) {
					totalZahlung = MathUtil.DEFAULT.add(totalZahlung, zahlungsposition.getBetrag());
				}
			}
			if (MathUtil.isNegative(totalZahlung)) {
				hasAnyZahlungWithNegativTotal = true;
			}
			zahlung.setBetragTotalZahlung(totalZahlung);
			totalAuftrag = MathUtil.DEFAULT.add(totalAuftrag, totalZahlung);
		}
		zahlungsauftrag.setHasNegativeZahlungen(hasAnyZahlungWithNegativTotal);
		zahlungsauftrag.setBetragTotalAuftrag(totalAuftrag);
	}

	/**
	 * Ermittelt die aktuell gueltigen Verfuegungszeitabschnitte fuer die normale monatliche Zahlung (keine Korrekturen).
	 */
	@Nonnull
	private Collection<VerfuegungZeitabschnitt> getGueltigeVerfuegungZeitabschnitte(@Nonnull Gemeinde gemeinde, @Nonnull LocalDate zeitabschnittVon,
		@Nonnull LocalDate zeitabschnittBis) {
		requireNonNull(zeitabschnittVon, "zeitabschnittVon muss gesetzt sein");
		requireNonNull(zeitabschnittBis, "zeitabschnittBis muss gesetzt sein");

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = cb.createQuery(VerfuegungZeitabschnitt.class);
		Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);
		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(VerfuegungZeitabschnitt_.verfuegung);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(Verfuegung_.betreuung);
		Join<Betreuung, KindContainer> joinKindContainer = joinBetreuung.join(Betreuung_.kind);
		Join<KindContainer, Gesuch> joinGesuch = joinKindContainer.join(KindContainer_.gesuch);
		Join<Gesuch, Dossier> joinDossier = joinGesuch.join(Gesuch_.dossier);

		List<Predicate> predicates = new ArrayList<>();

		// Datum Von
		Predicate predicateStart = cb.lessThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb), zeitabschnittBis);
		predicates.add(predicateStart);
		// Datum Bis
		Predicate predicateEnd = cb.greaterThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis), zeitabschnittVon);
		predicates.add(predicateEnd);
		// Nur Angebot KITA und TAGESFAMILIEN
		Predicate predicateAngebot = joinBetreuung.get(Betreuung_.institutionStammdaten).get(InstitutionStammdaten_.betreuungsangebotTyp)
			.in(BetreuungsangebotTyp.KITA, BetreuungsangebotTyp.TAGESFAMILIEN);
		predicates.add(predicateAngebot);
		// Nur neueste Verfuegung jedes Falls beachten
		Predicate predicateGueltig = cb.equal(joinBetreuung.get(Betreuung_.gueltig), Boolean.TRUE);
		predicates.add(predicateGueltig);
		// Status der Betreuung muss VERFUEGT sein
		Predicate predicateStatus = joinBetreuung.get(Betreuung_.betreuungsstatus).in(Betreuungsstatus.VERFUEGT);
		predicates.add(predicateStatus);
		// Das Dossier muss der uebergebenen Gemeinde zugeordnet sein
		Predicate predicateGemeinde = cb.equal(joinDossier.get(Dossier_.gemeinde), gemeinde);
		predicates.add(predicateGemeinde);

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaResults(query);
	}

	/**
	 * Ermittelt alle Zeitabschnitte, welche zu Antraegen gehoeren, die seit dem letzten Zahlungslauf verfuegt wurden.
	 */
	@Nonnull
	private Collection<VerfuegungZeitabschnitt> getVerfuegungsZeitabschnitteNachVerfuegungDatum(
		@Nonnull Gemeinde gemeinde,
		@Nonnull LocalDateTime datumVerfuegtVon,
		@Nonnull LocalDateTime datumVerfuegtBis,
		@Nonnull LocalDate zeitabschnittBis
	) {
		requireNonNull(datumVerfuegtVon, "datumVerfuegtVon muss gesetzt sein");
		requireNonNull(datumVerfuegtBis, "datumVerfuegtBis muss gesetzt sein");
		requireNonNull(zeitabschnittBis, "zeitabschnittBis muss gesetzt sein");

		LOGGER.info("Ermittle Korrekturzahlungen:");
		LOGGER.info("Zeitabschnitt endet vor: {}", Constants.DATE_FORMATTER.format(zeitabschnittBis));
		LOGGER.info("Gesuch verfuegt zwischen: {} - {}", Constants.DATE_FORMATTER.format(datumVerfuegtVon), Constants.DATE_FORMATTER.format(datumVerfuegtBis));

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = cb.createQuery(VerfuegungZeitabschnitt.class);
		Root<VerfuegungZeitabschnitt> root = query.from(VerfuegungZeitabschnitt.class);
		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(VerfuegungZeitabschnitt_.verfuegung);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(Verfuegung_.betreuung);
		Join<Betreuung, KindContainer> joinKindContainer = joinBetreuung.join(Betreuung_.kind);
		Join<KindContainer, Gesuch> joinGesuch = joinKindContainer.join(KindContainer_.gesuch);
		Join<Gesuch, Dossier> joinDossier = joinGesuch.join(Gesuch_.dossier);

		List<Predicate> predicates = new ArrayList<>();

		// Datum Bis muss VOR dem regulaeren Auszahlungszeitraum sein (sonst ist es keine Korrektur und schon im obigen Statement enthalten)
		Predicate predicateStart = cb.lessThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis), zeitabschnittBis);
		predicates.add(predicateStart);
		// Nur Angebot KITA und TAGESFAMILIEN
		Predicate predicateAngebot = joinBetreuung.get(Betreuung_.institutionStammdaten).get(InstitutionStammdaten_.betreuungsangebotTyp)
			.in(BetreuungsangebotTyp.KITA, BetreuungsangebotTyp.TAGESFAMILIEN);
		predicates.add(predicateAngebot);
		// Gesuche, welche seit dem letzten Zahlungslauf verfuegt wurden. Nur neueste Verfuegung jedes Falls beachten
		Predicate predicateDatum = cb.between(joinGesuch.get(Gesuch_.timestampVerfuegt), cb.literal(datumVerfuegtVon), cb.literal(datumVerfuegtBis));
		predicates.add(predicateDatum);
		// Nur neueste Verfuegung jedes Falls beachten
		Predicate predicateGueltig = cb.equal(joinBetreuung.get(Betreuung_.gueltig), Boolean.TRUE);
		predicates.add(predicateGueltig);
		// Status der Betreuung muss VERFUEGT sein
		Predicate predicateStatus = joinBetreuung.get(Betreuung_.betreuungsstatus).in(Betreuungsstatus.VERFUEGT);
		predicates.add(predicateStatus);
		// Das Dossier muss der uebergebenen Gemeinde zugeordnet sein
		Predicate predicateGemeinde = cb.equal(joinDossier.get(Dossier_.gemeinde), gemeinde);
		predicates.add(predicateGemeinde);

		query.orderBy(cb.asc(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb)));
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaResults(query);
	}

	/**
	 * Erstellt eine Zahlungsposition fuer den uebergebenen Zeitabschnitt. Normalfall bei "Erstbuchung"
	 */
	private void createZahlungsposition(
		@Nonnull ZahlungslaufHelper helper,
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull Map<String, Zahlung> zahlungProInstitution
	) {
		Zahlungsposition zahlungsposition = new Zahlungsposition();
		zahlungsposition.setVerfuegungZeitabschnitt(zeitabschnitt);
		zahlungsposition.setBetrag(helper.getAuszahlungsbetrag(zeitabschnitt));
		zahlungsposition.setStatus(ZahlungspositionStatus.NORMAL);
		Zahlung zahlung = helper.findZahlungForEmpfaenger(zeitabschnitt, zahlungsauftrag, zahlungProInstitution);
		zahlungsposition.setZahlung(zahlung);
		zahlung.getZahlungspositionen().add(zahlungsposition);
		helper.setZahlungsstatus(zeitabschnitt, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET);
	}

	/**
	 * Erstellt alle notwendigen Zahlungspositionen fuer die Korrektur des uebergebenen Zeitabschnitts.
	 * Bisherige Positionen werden in Abzug gebracht und die neuen hinzugefuegt
	 */
	private void createZahlungspositionenKorrekturUndNachzahlung(
		@Nonnull ZahlungslaufHelper helper,
		@Nonnull VerfuegungZeitabschnitt zeitabschnittNeu,
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull Map<String, Zahlung> zahlungProInstitution
	) {
		// Ermitteln, ob die Vollkosten geaendert haben, seit der letzten Verfuegung, die auch verrechnet wurde!
		List<VerfuegungZeitabschnitt> zeitabschnittOnVorgaengerVerfuegung = new ArrayList<>();
		final Verfuegung verfuegung = zeitabschnittNeu.getVerfuegung();
		Objects.requireNonNull(verfuegung);
		Objects.requireNonNull(verfuegung.getBetreuung());

		verfuegungService.findVerrechnetenZeitabschnittOnVorgaengerVerfuegung(
			helper.getZahlungslaufTyp(),
			zeitabschnittNeu,
			verfuegung.getBetreuung(),
			zeitabschnittOnVorgaengerVerfuegung);

		if (!zeitabschnittOnVorgaengerVerfuegung.isEmpty()) { // Korrekturen
			Zahlung zahlung = helper.findZahlungForEmpfaenger(zeitabschnittNeu, zahlungsauftrag, zahlungProInstitution);
			createZahlungspositionKorrekturNeuerWert(helper, zeitabschnittNeu, zahlung); // Dies braucht man immer
			for (VerfuegungZeitabschnitt vorgaengerZeitabschnitt : zeitabschnittOnVorgaengerVerfuegung) {
				// Fuer die "alten" Verfuegungszeitabschnitte muessen Korrekturbuchungen erstellt werden
				// Wenn die neuen Zeitabschnitte ignoriert sind, setzen wir die alten Zeitabschnitte auch als ignoriert
				// wir muessen noch aufpassen, das dieser Korrektur nicht schon drin ist falls man einen Monat
				// in mehrer Zeitabschnitten migriert haben.
				boolean alreadyCorrected = false;
				for(Zahlungsposition zahlungsposition : zahlung.getZahlungspositionen()){
					if(zahlungsposition.getVerfuegungZeitabschnitt().equals(vorgaengerZeitabschnitt)){
						alreadyCorrected = true;
						break;
					}
				}
				if(!alreadyCorrected) {
					createZahlungspositionKorrekturAlterWert(helper, vorgaengerZeitabschnitt, zahlung,
						helper.getZahlungsstatus(zeitabschnittNeu).isIgnoriertIgnorierend());
				}
			}
		} else { // Nachzahlungen bzw. Erstgesuche die rueckwirkend ausbezahlt werden muessen
			createZahlungsposition(helper, zeitabschnittNeu, zahlungsauftrag, zahlungProInstitution);
		}
	}

	/**
	 * Erstellt eine Zahlungsposition fuer eine Korrekturzahlung mit dem *neu gueltigen* Wert
	 */
	private void createZahlungspositionKorrekturNeuerWert(
		@Nonnull ZahlungslaufHelper helper,
		@Nonnull VerfuegungZeitabschnitt zeitabschnittNeu,
		@Nonnull Zahlung zahlung
	) {
		Zahlungsposition zahlungsposition = new Zahlungsposition();
		zahlungsposition.setVerfuegungZeitabschnitt(zeitabschnittNeu);
		zahlungsposition.setBetrag(helper.getAuszahlungsbetrag(zeitabschnittNeu));
		zahlungsposition.setZahlung(zahlung);
		zahlungsposition.setIgnoriert(helper.getZahlungsstatus(zeitabschnittNeu).isIgnoriertIgnorierend());
		ZahlungspositionStatus status = ZahlungspositionStatus.KORREKTUR;
		zahlungsposition.setStatus(status);
		if (helper.getZahlungsstatus(zeitabschnittNeu).isIgnoriertIgnorierend()) {
			helper.setZahlungsstatus(zeitabschnittNeu, VerfuegungsZeitabschnittZahlungsstatus.IGNORIERT_KORRIGIERT);
		} else {
			helper.setZahlungsstatus(zeitabschnittNeu, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET_KORRIGIERT);
		}
		zahlung.getZahlungspositionen().add(zahlungsposition);
	}

	/**
	 * Erstellt eine Zahlungsposition fuer eine Korrekturzahlung mit der Korrektur des *alten Wertes* (negiert)
	 */
	private void createZahlungspositionKorrekturAlterWert(
		@Nonnull ZahlungslaufHelper helper,
		@Nonnull VerfuegungZeitabschnitt vorgaengerZeitabschnitt,
		@Nonnull Zahlung zahlung,
		boolean ignoriert
	) {
		Zahlungsposition korrekturPosition = new Zahlungsposition();
		korrekturPosition.setVerfuegungZeitabschnitt(vorgaengerZeitabschnitt);
		korrekturPosition.setBetrag(helper.getAuszahlungsbetrag(vorgaengerZeitabschnitt).negate());
		korrekturPosition.setZahlung(zahlung);
		korrekturPosition.setIgnoriert(ignoriert); // ignoriert kommt vom neuen Zeitabschnitt
		ZahlungspositionStatus status = ZahlungspositionStatus.KORREKTUR;
		korrekturPosition.setStatus(status);
		zahlung.getZahlungspositionen().add(korrekturPosition);
		if (helper.getZahlungsstatus(vorgaengerZeitabschnitt).isIgnoriertIgnorierend()) {
			helper.setZahlungsstatus(vorgaengerZeitabschnitt, VerfuegungsZeitabschnittZahlungsstatus.IGNORIERT_KORRIGIERT);
		} else {
			helper.setZahlungsstatus(vorgaengerZeitabschnitt, VerfuegungsZeitabschnittZahlungsstatus.VERRECHNET_KORRIGIERT);
		}
	}

	/**
	 * Ermittelt den zuletzt durchgefuehrten Zahlungsauftrag des entsprechenden Typs
	 */
	@Nonnull
	private Optional<Zahlungsauftrag> findLastZahlungsauftrag(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull Gemeinde gemeinde
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Zahlungsauftrag> query = cb.createQuery(Zahlungsauftrag.class);
		Root<Zahlungsauftrag> root = query.from(Zahlungsauftrag.class);

		// Der Zahlungsauftrag muss der uebergebenen Gemeinde zugeordnet sein
		Predicate predicateGemeinde = cb.equal(root.get(Zahlungsauftrag_.gemeinde), gemeinde);
		// und den richtigen Typ haben
		Predicate predicateTyp = cb.equal(root.get(Zahlungsauftrag_.zahlungslaufTyp), zahlungslaufTyp);
		query.where(predicateGemeinde, predicateTyp);

		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		List<Zahlungsauftrag> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (!criteriaResults.isEmpty()) {
			return Optional.of(criteriaResults.get(0));
		}
		return Optional.empty();
	}

	@Override
	@Nonnull
	public Zahlungsauftrag zahlungsauftragAktualisieren(@Nonnull String auftragId, @Nonnull LocalDate datumFaelligkeit, @Nonnull String beschreibung) {
		requireNonNull(auftragId, "auftragId muss gesetzt sein");
		requireNonNull(datumFaelligkeit, "datumFaelligkeit muss gesetzt sein");
		requireNonNull(beschreibung, "beschreibung muss gesetzt sein");

		Zahlungsauftrag auftrag = findZahlungsauftrag(auftragId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"zahlungsauftragAktualisieren", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, auftragId));

		authorizer.checkWriteAuthorizationZahlungsauftrag(auftrag);
		// Auftrag kann nur im Status ENTWURF veraendert werden
		if (auftrag.getStatus().isEntwurf()) {
			auftrag.setBeschrieb(beschreibung);
			auftrag.setDatumFaellig(datumFaelligkeit);
			return persistence.merge(auftrag);
		}
		throw new IllegalStateException("Auftrag kann nicht mehr veraendert werden: " + auftragId);
	}

	@Override
	@Nonnull
	public Zahlungsauftrag zahlungsauftragAusloesen(@Nonnull String auftragId) {
		requireNonNull(auftragId, "auftragId muss gesetzt sein");

		Zahlungsauftrag zahlungsauftrag = findZahlungsauftrag(auftragId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"zahlungsauftragAktualisieren", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, auftragId));

		authorizer.checkWriteAuthorizationZahlungsauftrag(zahlungsauftrag);

		zahlungsauftrag.setStatus(ZahlungauftragStatus.AUSGELOEST);
		// Jetzt muss noch das PAIN File erstellt werden. Nach dem Ausloesen kann dieses nicht mehr veraendert werden
		try {
			generatedDokumentService.getPain001DokumentAccessTokenGeneratedDokument(zahlungsauftrag, true);
		} catch (MimeTypeParseException e) {
			throw new IllegalStateException("Pain-File konnte nicht erstellt werden: " + auftragId, e);
		}
		for (Zahlung zahlung : zahlungsauftrag.getZahlungen()) {
			if (ZahlungStatus.ENTWURF != zahlung.getStatus()) {
				throw new IllegalArgumentException("Zahlung muss im Status ENTWURF sein, wenn der Auftrag ausgelöst wird: " + zahlung.getId());
			}
			zahlung.setStatus(ZahlungStatus.AUSGELOEST);
			persistence.merge(zahlung);
		}
		return persistence.merge(zahlungsauftrag);
	}

	@Override
	@Nonnull
	public Optional<Zahlungsauftrag> findZahlungsauftrag(@Nonnull String auftragId) {
		requireNonNull(auftragId, "auftragId muss gesetzt sein");
		Zahlungsauftrag zahlungsauftrag = persistence.find(Zahlungsauftrag.class, auftragId);
		authorizer.checkReadAuthorizationZahlungsauftrag(zahlungsauftrag);
		return Optional.ofNullable(zahlungsauftrag);
	}

	@Override
	@Nonnull
	public Optional<Zahlung> findZahlung(@Nonnull String zahlungId) {
		requireNonNull(zahlungId, "zahlungId muss gesetzt sein");
		Zahlung zahlung = persistence.find(Zahlung.class, zahlungId);
		authorizer.checkReadAuthorizationZahlung(zahlung);
		return Optional.ofNullable(zahlung);
	}

	@Override
	@Nonnull
	public Collection<Zahlungsauftrag> getAllZahlungsauftraege() {
		Benutzer currentBenutzer = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getBenutzersOfRole", "Non logged in user should never reach this"));

		if (currentBenutzer.getCurrentBerechtigung().getRole().isRoleGemeindeabhaengig()) {
			final CriteriaBuilder cb = persistence.getCriteriaBuilder();
			final CriteriaQuery<Zahlungsauftrag> query = cb.createQuery(Zahlungsauftrag.class);

			Root<Zahlungsauftrag> root = query.from(Zahlungsauftrag.class);
			Join<Zahlungsauftrag, Gemeinde> joinGemeinde = root.join(Zahlungsauftrag_.gemeinde);

			List<Predicate> predicates = new ArrayList<>();
			Collection<Gemeinde> gemeindenForUser = currentBenutzer.extractGemeindenForUser();
			Predicate inGemeinde = joinGemeinde.in(gemeindenForUser);
			predicates.add(inGemeinde);

			query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
			return persistence.getCriteriaResults(query);
		}
		// Nicht Gemeinde-abhängige duerfen alle Auftraege sehen
		return new ArrayList<>(criteriaQueryHelper.getAll(Zahlungsauftrag.class));
	}

	@Override
	@Nonnull
	public Zahlung zahlungBestaetigen(@Nonnull String zahlungId) {
		requireNonNull(zahlungId, "zahlungId muss gesetzt sein");
		Zahlung zahlung = findZahlung(zahlungId).orElseThrow(() -> new EbeguEntityNotFoundException("zahlungBestaetigen",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, zahlungId));
		zahlung.setStatus(ZahlungStatus.BESTAETIGT);
		Zahlung persistedZahlung = persistence.merge(zahlung);
		zahlungauftragBestaetigenIfAllZahlungenBestaetigt(zahlung.getZahlungsauftrag());
		return persistedZahlung;
	}

	@Nonnull
	@Override
	public Collection<Zahlungsauftrag> getZahlungsauftraegeInPeriode(@Nonnull LocalDate von, @Nonnull LocalDate bis) {

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Zahlungsauftrag> query = cb.createQuery(Zahlungsauftrag.class);

		Root<Zahlungsauftrag> root = query.from(Zahlungsauftrag.class);
		List<Predicate> predicatesToUse = new ArrayList<>();

		// Zeitraum
		Predicate predicateZeitraum = cb.between(root.get(Zahlungsauftrag_.datumGeneriert), cb.literal(von.atStartOfDay()), cb.literal(bis.atTime(LocalTime.MAX)));
		predicatesToUse.add(predicateZeitraum);
		// Dieser Report betrifft nur Institutionszahlungen
		Predicate predicateAuftragTyp = cb.equal(root.get(Zahlungsauftrag_.zahlungslaufTyp), ZahlungslaufTyp.GEMEINDE_INSTITUTION);
		predicatesToUse.add(predicateAuftragTyp);

		// Gemeinde
		Benutzer currentBenutzer = benutzerService.getCurrentBenutzer().orElseThrow(() -> new EbeguRuntimeException(
			"getBenutzersOfRole", "Non logged in user should never reach this"));

		if (currentBenutzer.getCurrentBerechtigung().getRole().isRoleGemeindeabhaengig()) {
			Join<Zahlungsauftrag, Gemeinde> joinGemeinde = root.join(Zahlungsauftrag_.gemeinde);

			Collection<Gemeinde> gemeindenForUser = currentBenutzer.extractGemeindenForUser();
			Predicate inGemeinde = joinGemeinde.in(gemeindenForUser);
			predicatesToUse.add(inGemeinde);
		}
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicatesToUse));
		return persistence.getCriteriaResults(query);

	}

	@Override
	public void deleteZahlungspositionenOfGesuch(@Nonnull Gesuch gesuch) {
		requireNonNull(gesuch, "gesuch muss gesetzt sein");
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Zahlungsposition> query = cb.createQuery(Zahlungsposition.class);

		Root<Zahlungsposition> root = query.from(Zahlungsposition.class);
		Predicate predicates = cb.equal(root.get(Zahlungsposition_.verfuegungZeitabschnitt)
			.get(VerfuegungZeitabschnitt_.verfuegung)
			.get(Verfuegung_.betreuung)
			.get(Betreuung_.kind)
			.get(KindContainer_.gesuch), gesuch);

		query.where(predicates);
		List<Zahlungsposition> zahlungspositionList = persistence.getCriteriaResults(query);

		//remove Zahlungspositionen
		Set<Zahlung> potenziellZuLoeschenZahlungenList = new HashSet<>();
		for (Zahlungsposition zahlungsposition : zahlungspositionList) {
			potenziellZuLoeschenZahlungenList.add(zahlungsposition.getZahlung()); // add the Zahlung to the set
			zahlungsposition.getZahlung().getZahlungspositionen().remove(zahlungsposition);
			persistence.remove(Zahlungsposition.class, zahlungsposition.getId());
		}
		Set<Zahlungsauftrag> zahlungsauftraegeList = removeAllEmptyZahlungen(potenziellZuLoeschenZahlungenList);
		removeAllEmptyZahlungsauftraege(zahlungsauftraegeList);
	}

	/**
	 * Goes through the given list and check whether the given Zahlungsauftrag is empty or not.
	 * All empty Zahlungsauftraege are removed.
	 */
	private void removeAllEmptyZahlungsauftraege(Set<Zahlungsauftrag> zahlungsauftraegeList) {
		for (Zahlungsauftrag zahlungsauftrag : zahlungsauftraegeList) {
			if (zahlungsauftrag.getZahlungen().isEmpty()) {
				removePAIN001FromZahlungsauftrag(zahlungsauftrag);
				persistence.remove(Zahlungsauftrag.class, zahlungsauftrag.getId());
			}
		}
	}

	/**
	 * Removes the Pain001Dokument that is linked with the given Zahlungsauftrag if it exists.
	 */
	private void removePAIN001FromZahlungsauftrag(Zahlungsauftrag zahlungsauftrag) {
		final Collection<Pain001Dokument> pain001Dokument = criteriaQueryHelper.getEntitiesByAttribute(Pain001Dokument.class, zahlungsauftrag, Pain001Dokument_.zahlungsauftrag);
		pain001Dokument.forEach(pain -> {
			fileSaverService.removeAllFromSubfolder(pain.getZahlungsauftrag().getId());
			persistence.remove(Pain001Dokument.class, pain.getId());
		});
	}

	/**
	 * Goes through the given list and check whether the given Zahlung is empty or not.
	 * All empty Zahlungen are removed and all corresponding Zahlungsauftraege are added to the
	 * Set that will be returned at the end of the function
	 */
	@Nonnull
	private Set<Zahlungsauftrag> removeAllEmptyZahlungen(Set<Zahlung> potenziellZuLoeschenZahlungenList) {
		Set<Zahlungsauftrag> potenziellZuLoeschenZahlungsauftraegeList = new HashSet<>();
		for (Zahlung zahlung : potenziellZuLoeschenZahlungenList) {
			if (zahlung.getZahlungspositionen().isEmpty()) {
				potenziellZuLoeschenZahlungsauftraegeList.add(zahlung.getZahlungsauftrag());
				zahlung.getZahlungsauftrag().getZahlungen().remove(zahlung);
				persistence.remove(Zahlung.class, zahlung.getId());
			}
		}
		return potenziellZuLoeschenZahlungsauftraegeList;
	}

	private void zahlungauftragBestaetigenIfAllZahlungenBestaetigt(@Nonnull Zahlungsauftrag zahlungsauftrag) {
		requireNonNull(zahlungsauftrag, "zahlungsauftrag darf nicht null sein");
		if (zahlungsauftrag.getZahlungen().stream().allMatch(zahlung -> zahlung.getStatus() == ZahlungStatus.BESTAETIGT)) {
			zahlungsauftrag.setStatus(ZahlungauftragStatus.BESTAETIGT);
			persistence.merge(zahlungsauftrag);
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void zahlungenKontrollieren(@Nonnull ZahlungslaufTyp zahlungslaufTyp, @Nonnull String gemeindeId) {
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId).orElseThrow(() -> new EbeguEntityNotFoundException("zahlungenKontrollieren",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId));
		Optional<Zahlungsauftrag> lastZahlungsauftrag = findLastZahlungsauftrag(zahlungslaufTyp, gemeinde);
		lastZahlungsauftrag.ifPresent(zahlungsauftrag -> zahlungUeberpruefungServiceBean.pruefungZahlungen(
			gemeinde,
			zahlungslaufTyp,
			zahlungsauftrag.getId(),
			zahlungsauftrag.getDatumGeneriert(),
			zahlungsauftrag.getBeschrieb()));
	}
}


