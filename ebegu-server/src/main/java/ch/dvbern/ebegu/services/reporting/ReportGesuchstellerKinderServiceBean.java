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

package ch.dvbern.ebegu.services.reporting;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
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
import javax.annotation.Nullable;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractPlatz_;
import ch.dvbern.ebegu.entities.AntragStatusHistory;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt_;
import ch.dvbern.ebegu.entities.Verfuegung_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.reporting.ReportService;
import ch.dvbern.ebegu.reporting.gesuchstellerKinderBetreuung.GesuchstellerKinderBetreuungDataRow;
import ch.dvbern.ebegu.reporting.gesuchstellerKinderBetreuung.GesuchstellerKinderBetreuungExcelConverter;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.RowFiller;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static ch.dvbern.ebegu.services.reporting.ReportUtil.createWorkbook;
import static ch.dvbern.ebegu.services.reporting.ReportUtil.validateStichtagParam;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setGemeindeFilterForCurrentUser;
import static java.util.Objects.requireNonNull;

/**
 * The Gesuchsteller and Kinder statistik use similar fields. In previous implementations, this led
 * to the reuse of code that should not be reused. In the scope of one task, this reused code was extracted to this
 * service, but it should be split further.
 */
@NoArgsConstructor
@Stateless
public class ReportGesuchstellerKinderServiceBean extends
	AbstractReportServiceBean {

	// Excel kann nicht mit Datum vor 1800 umgehen. Wir setzen auf 1900, wie Minimum im datepicker
	private static final LocalDate MIN_DATE = LocalDate.of(
		1900,
		Month.JANUARY,
		1
	);

	private BenutzerService benutzerService;
	private GesuchstellerKinderBetreuungExcelConverter gesuchstellerKinderBetreuungExcelConverter;
	private PrincipalBean principalBean;
	private InstitutionService institutionService;
	private Persistence persistence;
	private ExcelFileSaverService fileSaverService;
	private GesuchsperiodeService gesuchsperiodeService;
	private EinstellungService einstellungService;
	private ReportService reportService;

	@Inject
	public ReportGesuchstellerKinderServiceBean(
		BenutzerService benutzerService,
		GesuchstellerKinderBetreuungExcelConverter gesuchstellerKinderBetreuungExcelConverter,
		PrincipalBean principalBean,
		InstitutionService institutionService,
		Persistence persistence,
		ExcelFileSaverService fileSaverService,
		GesuchsperiodeService gesuchsperiodeService,
		EinstellungService einstellungService,
		ReportService reportService
	) {
		this.benutzerService = benutzerService;
		this.gesuchstellerKinderBetreuungExcelConverter =
			gesuchstellerKinderBetreuungExcelConverter;
		this.principalBean = principalBean;
		this.institutionService = institutionService;
		this.persistence = persistence;
		this.fileSaverService = fileSaverService;
		this.gesuchsperiodeService = gesuchsperiodeService;
		this.einstellungService = einstellungService;
		this.reportService = reportService;
	}

	@Nonnull
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportGesuchsteller(
		@Nonnull LocalDate stichtag,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) throws ExcelMergeException, IOException {
		validateStichtagParam(stichtag);

		final ReportVorlage reportResource =
			ReportVorlage.VORLAGE_REPORT_GESUCHSTELLER;

		try (
			Workbook workbook = createWorkbook(reportResource);
		) {
			Sheet sheet = workbook.getSheet(reportResource.getDataSheetName());

			List<GesuchstellerKinderBetreuungDataRow> reportData =
				getReportDataGesuchsteller(stichtag, locale, mandant);

			if (reportData.stream().noneMatch(row -> row.getMzvBeantragt())) {
				sheet.setColumnWidth(48, 0);
			}

			final XSSFSheet xsslSheet =
				(XSSFSheet) gesuchstellerKinderBetreuungExcelConverter
					.mergeHeaderFieldsStichtag(
						reportData,
						sheet,
						stichtag,
						locale,
						requireNonNull(principalBean.getMandant())
					);

			final RowFiller rowFiller = fillAndMergeRows(
				reportResource,
				xsslSheet,
				reportData,
				locale
			);
			return fileSaverService.saveExcelDokument(
				reportResource,
				rowFiller,
				locale,
				principalBean.getMandant()
			);
		}
	}

	@SuppressWarnings("Duplicates")
	@Nonnull
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportGesuchstellerKinderBetreuung(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable String gesuchPeriodeId,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) throws ExcelMergeException, IOException {

		validateDateParams(datumVon, datumBis);

		final ReportVorlage reportResource =
			ReportVorlage.VORLAGE_REPORT_GESUCHSTELLER_KINDER_BETREUUNG;

		try (
			Workbook workbook = createWorkbook(reportResource);
		) {
			Sheet sheet = workbook.getSheet(reportResource.getDataSheetName());

			Gesuchsperiode gesuchsperiode = null;
			if (gesuchPeriodeId != null) {
				Optional<Gesuchsperiode> gesuchsperiodeOptional =
					gesuchsperiodeService.findGesuchsperiode(
						gesuchPeriodeId
					);
				if (gesuchsperiodeOptional.isPresent()) {
					gesuchsperiode = gesuchsperiodeOptional.get();
				}
			}

			List<GesuchstellerKinderBetreuungDataRow> reportData =
				getReportDataGesuchstellerKinderBetreuung(
					datumVon,
					datumBis,
					gesuchsperiode,
					locale,
					mandant
				);

			final XSSFSheet xsslSheet =
				(XSSFSheet) gesuchstellerKinderBetreuungExcelConverter
					.mergeHeaderFieldsPeriode(
						reportData,
						sheet,
						datumVon,
						datumBis,
						gesuchsperiode,
						locale,
						requireNonNull(principalBean.getMandant())
					);

			final RowFiller rowFiller = fillAndMergeRows(
				reportResource,
				xsslSheet,
				reportData,
				locale
			);

			return fileSaverService.saveExcelDokument(
				reportResource,
				rowFiller,
				locale,
				principalBean.getMandant()
			);
		}
	}

	@SuppressWarnings("Duplicates")
	@Nonnull
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportKinder(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable String gesuchPeriodeId,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) throws ExcelMergeException, IOException {

		validateDateParams(datumVon, datumBis);

		final ReportVorlage reportResource =
			ReportVorlage.VORLAGE_REPORT_KINDER;

		try (
			Workbook workbook = createWorkbook(reportResource);
		) {
			Sheet sheet = workbook.getSheet(reportResource.getDataSheetName());

			Gesuchsperiode gesuchsperiode = null;
			if (gesuchPeriodeId != null) {
				Optional<Gesuchsperiode> gesuchsperiodeOptional =
					gesuchsperiodeService.findGesuchsperiode(
						gesuchPeriodeId
					);
				if (gesuchsperiodeOptional.isPresent()) {
					gesuchsperiode = gesuchsperiodeOptional.get();
				}
			}

			List<GesuchstellerKinderBetreuungDataRow> reportData =
				getReportDataKinder(
					datumVon,
					datumBis,
					gesuchsperiode,
					locale,
					mandant
				);

			final XSSFSheet xsslSheet =
				(XSSFSheet) gesuchstellerKinderBetreuungExcelConverter
					.mergeHeaderFieldsPeriode(
						reportData,
						sheet,
						datumVon,
						datumBis,
						gesuchsperiode,
						locale,
						requireNonNull(principalBean.getMandant())
					);

			final RowFiller rowFiller = fillAndMergeRows(
				reportResource,
				xsslSheet,
				reportData,
				locale
			);

			return fileSaverService.saveExcelDokument(
				reportResource,
				rowFiller,
				locale,
				principalBean.getMandant()
			);
		}
	}

	@Nonnull
	private List<GesuchstellerKinderBetreuungDataRow> getReportDataGesuchstellerKinderBetreuung(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable Gesuchsperiode gesuchsperiode,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) {
		List<VerfuegungZeitabschnitt> zeitabschnittList =
			getReportDataBetreuungen(
				datumVon,
				datumBis,
				gesuchsperiode,
				mandant
			);
		List<GesuchstellerKinderBetreuungDataRow> dataRows =
			convertToGesuchstellerKinderBetreuungDataRow(
				zeitabschnittList,
				gesuchsperiode,
				locale,
				mandant
			);

		dataRows.sort(
			Comparator.comparing(
				GesuchstellerKinderBetreuungDataRow::getReferenzNummer
			)
				.thenComparing(
					GesuchstellerKinderBetreuungDataRow::getZeitabschnittVon
				)
		);

		return dataRows;
	}

	@Nonnull
	private List<GesuchstellerKinderBetreuungDataRow> getReportDataKinder(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable Gesuchsperiode gesuchsperiode,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) {
		List<VerfuegungZeitabschnitt> zeitabschnittList =
			getReportDataBetreuungen(
				datumVon,
				datumBis,
				gesuchsperiode,
				mandant
			);
		List<GesuchstellerKinderBetreuungDataRow> dataRows =
			convertToKinderDataRow(
				zeitabschnittList,
				locale,
				gesuchsperiode,
				mandant
			);

		dataRows.sort(
			Comparator.comparing(
				GesuchstellerKinderBetreuungDataRow::getReferenzNummer
			)
				.thenComparing(
					GesuchstellerKinderBetreuungDataRow::getZeitabschnittVon
				)
		);

		return dataRows;
	}

	@Nonnull
	private List<VerfuegungZeitabschnitt> getReportDataBetreuungen(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable Gesuchsperiode gesuchsperiode,
		@Nonnull Mandant mandant
	) {
		validateDateParams(datumVon, datumBis);

		Benutzer user = benutzerService.getCurrentBenutzer()
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"getReportDataBetreuungen",
					NO_USER_IS_LOGGED_IN
				)
			);

		// Alle Verfuegungszeitabschnitte zwischen datumVon und datumBis. Aber pro Fall immer nur das zuletzt
		// verfuegte.
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = builder
			.createQuery(VerfuegungZeitabschnitt.class);
		query.distinct(true);
		Root<VerfuegungZeitabschnitt> root = query.from(
			VerfuegungZeitabschnitt.class
		);
		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(
			VerfuegungZeitabschnitt_.verfuegung
		);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(
			Verfuegung_.betreuung
		);
		Join<Betreuung, KindContainer> joinKindContainer = joinBetreuung.join(
			AbstractPlatz_.kind,
			JoinType.LEFT
		);
		Join<KindContainer, Gesuch> joinGesuch = joinKindContainer.join(
			KindContainer_.gesuch,
			JoinType.LEFT
		);
		Join<Gesuch, Dossier> joinDossier = joinGesuch.join(
			Gesuch_.dossier,
			JoinType.LEFT
		);
		Join<Dossier, Gemeinde> joinGemeinde = joinDossier.join(
			Dossier_.gemeinde,
			JoinType.LEFT
		);

		List<Predicate> predicatesToUse = new ArrayList<>();

		// mandant
		Predicate mandantPredicate = builder.equal(
			joinGemeinde.get(Gemeinde_.mandant),
			mandant
		);
		predicatesToUse.add(mandantPredicate);

		// startAbschnitt <= datumBis && endeAbschnitt >= datumVon
		Path<DateRange> dateRangePath = root.get(
			AbstractDateRangedEntity_.gueltigkeit
		);
		Predicate predicateStart = builder.lessThanOrEqualTo(
			dateRangePath.get(DateRange_.gueltigAb),
			datumBis
		);
		predicatesToUse.add(predicateStart);
		Predicate predicateEnd = builder.greaterThanOrEqualTo(
			dateRangePath.get(DateRange_.gueltigBis),
			datumVon
		);
		predicatesToUse.add(predicateEnd);
		// Gesuchsperiode
		if (gesuchsperiode != null) {
			Predicate predicateGesuchsperiode = builder.equal(
				root.get(VerfuegungZeitabschnitt_.verfuegung)
					.get(Verfuegung_.betreuung)
					.get(AbstractPlatz_.kind)
					.get(KindContainer_.gesuch)
					.get(Gesuch_.gesuchsperiode),
				gesuchsperiode
			);
			predicatesToUse.add(predicateGesuchsperiode);
		}
		// Nur neueste Verfuegung jedes Falls beachten
		Predicate predicateGueltig = builder.equal(
			joinBetreuung.get(AbstractPlatz_.gueltig),
			Boolean.TRUE
		);
		predicatesToUse.add(predicateGueltig);

		// Nur Gesuche von Gemeinden, fuer die ich berechtigt bin
		setGemeindeFilterForCurrentUser(user, joinGemeinde, predicatesToUse);

		// Sichtbarkeit nach eingeloggtem Benutzer
		boolean isInstitutionsbenutzer =
			principalBean.isCallerInAnyOfRole(
				UserRole.getInstitutionTraegerschaftRoles()
			);
		if (isInstitutionsbenutzer) {
			Collection<Institution> allowedInstitutionen =
				institutionService
					.getInstitutionenReadableForCurrentBenutzer(false);
			Predicate predicateAllowedInstitutionen = root.get(
				VerfuegungZeitabschnitt_.verfuegung
			)
				.get(Verfuegung_.betreuung)
				.get(AbstractPlatz_.institutionStammdaten)
				.get(InstitutionStammdaten_.institution)
				.in(allowedInstitutionen);
			predicatesToUse.add(predicateAllowedInstitutionen);
		}
		Predicate predicateForBenutzerRole = getPredicateForBenutzerRole(
			builder,
			root
		);
		if (predicateForBenutzerRole != null) {
			predicatesToUse.add(predicateForBenutzerRole);
		}
		query.where(
			CriteriaQueryHelper.concatenateExpressions(
				builder,
				predicatesToUse
			)
		);
		return persistence.getCriteriaResults(query);
	}

	private void addStammdaten(
		GesuchstellerKinderBetreuungDataRow row,
		VerfuegungZeitabschnitt zeitabschnitt,
		Gesuch gesuch,
		@Nonnull Locale locale
	) {

		final Betreuung betreuung = zeitabschnitt.getVerfuegung()
			.getBetreuung();
		Objects.requireNonNull(betreuung);
		row.setInstitution(
			betreuung
				.getInstitutionStammdaten()
				.getInstitution()
				.getName()
		);

		row.setBetreuungsTyp(betreuung.getBetreuungsangebotTyp());
		row.setPeriode(gesuch.getGesuchsperiode().getGesuchsperiodeString());
		String messageKey = AntragStatus.class.getSimpleName()
			+ '_'
			+ gesuch.getStatus().name();
		row.setGesuchStatus(
			ServerMessageUtil.getMessage(
				messageKey,
				locale,
				requireNonNull(gesuch.getFall().getMandant())
			)
		);
		row.setFallId(
			Integer.parseInt(
				String.valueOf(gesuch.getFall().getFallNummer())
			)
		);
		row.setGemeinde(gesuch.getDossier().getGemeinde().getName());
		row.setReferenzNummer(betreuung.getReferenzNummer());
		if (!isAllowedBgDaten(betreuung)) {
			return;
		}
		row.setEingangsdatum(gesuch.getEingangsdatum());
		for (AntragStatusHistory antragStatusHistory : gesuch
			.getAntragStatusHistories()) {
			if (AntragStatus.getAllVerfuegtNotIgnoriertStates()
				.contains(antragStatusHistory.getStatus())) {
				row.setVerfuegungsdatum(
					antragStatusHistory.getTimestampVon().toLocalDate()
				);
			}
		}
	}

	private void addGesuchsteller1ToGesuchstellerKinderBetreuungDataRow(
		@Nonnull GesuchstellerKinderBetreuungDataRow row,
		@Nullable GesuchstellerContainer containerGS1,
		@Nonnull Integer freiwilligenArbeitMax
	) {
		if (containerGS1 == null) {
			return;
		}

		Gesuchsteller gs1 = containerGS1.getGesuchstellerJA();
		row.setGs1Name(gs1.getNachname());
		row.setGs1Vorname(gs1.getVorname());
		GesuchstellerAdresse gs1Adresse = containerGS1.getWohnadresseAm(
			row.getZeitabschnittVon()
		);

		if (gs1Adresse != null) {
			row.setGs1Strasse(gs1Adresse.getStrasse());
			row.setGs1Hausnummer(gs1Adresse.getHausnummer());
			row.setGs1Zusatzzeile(gs1Adresse.getZusatzzeile());
			row.setGs1Plz(gs1Adresse.getPlz());
			row.setGs1Ort(gs1Adresse.getOrt());
		}
		row.setGs1Diplomatenstatus(gs1.isDiplomatenstatus());

		List<Erwerbspensum> erwerbspensenGS1 = containerGS1.getErwerbspensenAm(
			row.getZeitabschnittVon()
		);
		for (Erwerbspensum erwerbspensumJA : erwerbspensenGS1) {
			if (Taetigkeit.ANGESTELLT == erwerbspensumJA.getTaetigkeit()) {
				row.setGs1EwpAngestellt(
					row.getGs1EwpAngestellt() + erwerbspensumJA.getPensum()
				);
			}
			if (Taetigkeit.AUSBILDUNG == erwerbspensumJA.getTaetigkeit()) {
				row.setGs1EwpAusbildung(
					row.getGs1EwpAusbildung() + erwerbspensumJA.getPensum()
				);
			}
			if (Taetigkeit.SELBSTAENDIG == erwerbspensumJA.getTaetigkeit()) {
				row.setGs1EwpSelbstaendig(
					row.getGs1EwpSelbstaendig()
						+ erwerbspensumJA.getPensum()
				);
			}
			if (Taetigkeit.RAV == erwerbspensumJA.getTaetigkeit()) {
				row.setGs1EwpRav(
					row.getGs1EwpRav() + erwerbspensumJA.getPensum()
				);
			}
			if (Taetigkeit.GESUNDHEITLICHE_EINSCHRAENKUNGEN
				== erwerbspensumJA.getTaetigkeit()) {
				row.setGs1EwpGesundhtl(
					row.getGs1EwpGesundhtl() + erwerbspensumJA.getPensum()
				);
			}
			if (Taetigkeit.INTEGRATION_BESCHAEFTIGUNSPROGRAMM
				== erwerbspensumJA.getTaetigkeit()) {
				row.setGs1EwpIntegration(
					row.getGs1EwpIntegration() + erwerbspensumJA.getPensum()
				);
			}
			if (Taetigkeit.FREIWILLIGENARBEIT
				== erwerbspensumJA.getTaetigkeit()) {
				row.setGs1EwpFreiwillig(
					Math.min(
						row.getGs1EwpFreiwillig()
							+ erwerbspensumJA.getPensum(),
						freiwilligenArbeitMax
					)
				);
			}
		}
	}

	private void addGesuchsteller2ToGesuchstellerKinderBetreuungDataRow(
		@Nonnull GesuchstellerKinderBetreuungDataRow row,
		@Nonnull GesuchstellerContainer containerGS2,
		@Nonnull Integer freiwilligenArbeitMax
	) {

		Gesuchsteller gs2 = containerGS2.getGesuchstellerJA();
		row.setGs2Name(gs2.getNachname());
		row.setGs2Vorname(gs2.getVorname());
		GesuchstellerAdresse gs2Adresse = containerGS2.getWohnadresseAm(
			row.getZeitabschnittVon()
		);

		if (gs2Adresse != null) {
			row.setGs2Strasse(gs2Adresse.getStrasse());
			row.setGs2Hausnummer(gs2Adresse.getHausnummer());
			row.setGs2Zusatzzeile(gs2Adresse.getZusatzzeile());
			row.setGs2Plz(gs2Adresse.getPlz());
			row.setGs2Ort(gs2Adresse.getOrt());
		}
		row.setGs2Diplomatenstatus(gs2.isDiplomatenstatus());
		// EWP Gesuchsteller 2
		List<Erwerbspensum> erwerbspensenGS2 = containerGS2.getErwerbspensenAm(
			row.getZeitabschnittVon()
		);
		for (Erwerbspensum erwerbspensumJA : erwerbspensenGS2) {
			if (Taetigkeit.ANGESTELLT == erwerbspensumJA.getTaetigkeit()) {
				row.setGs2EwpAngestellt(
					row.getGs2EwpAngestellt() + erwerbspensumJA.getPensum()
				);
			}
			if (Taetigkeit.AUSBILDUNG == erwerbspensumJA.getTaetigkeit()) {
				row.setGs2EwpAusbildung(
					row.getGs2EwpAusbildung() + erwerbspensumJA.getPensum()
				);
			}
			if (Taetigkeit.SELBSTAENDIG == erwerbspensumJA.getTaetigkeit()) {
				row.setGs2EwpSelbstaendig(
					row.getGs2EwpSelbstaendig()
						+ erwerbspensumJA.getPensum()
				);
			}
			if (Taetigkeit.RAV == erwerbspensumJA.getTaetigkeit()) {
				row.setGs2EwpRav(
					row.getGs2EwpRav() + erwerbspensumJA.getPensum()
				);
			}
			if (Taetigkeit.GESUNDHEITLICHE_EINSCHRAENKUNGEN
				== erwerbspensumJA.getTaetigkeit()) {
				row.setGs2EwpGesundhtl(
					row.getGs2EwpGesundhtl() + erwerbspensumJA.getPensum()
				);
			}
			if (Taetigkeit.INTEGRATION_BESCHAEFTIGUNSPROGRAMM
				== erwerbspensumJA.getTaetigkeit()) {
				row.setGs2EwpIntegration(
					row.getGs2EwpIntegration() + erwerbspensumJA.getPensum()
				);
			}
			if (Taetigkeit.FREIWILLIGENARBEIT
				== erwerbspensumJA.getTaetigkeit()) {
				row.setGs2EwpFreiwillig(
					Math.min(
						row.getGs2EwpFreiwillig()
							+ erwerbspensumJA.getPensum(),
						freiwilligenArbeitMax
					)
				);
			}
		}
	}

	private void addKindToGesuchstellerKinderBetreuungDataRow(
		GesuchstellerKinderBetreuungDataRow row,
		VerfuegungZeitabschnitt zeitabschnitt,
		Betreuung betreuung,
		Locale locale
	) {

		Kind kind = betreuung.getKind().getKindJA();
		row.setKindName(kind.getNachname());
		row.setKindVorname(kind.getVorname());
		if (!isAllowedBgDaten(betreuung)) {
			return;
		}
		row.setKindGeburtsdatum(kind.getGeburtsdatum());
		if (row.getKindGeburtsdatum() == null
			|| row.getKindGeburtsdatum().isBefore(MIN_DATE)) {
			row.setKindGeburtsdatum(MIN_DATE);
		}
		final PensumFachstelle pensumFachstelle =
			getPensumFachstelleForGueltigkeit(
				kind,
				zeitabschnitt.getGueltigkeit()
			);

		row.setKindFachstelle(
			kind.getPensumFachstelle().isEmpty()
				|| pensumFachstelle == null
				|| pensumFachstelle.getFachstelle() == null ?
					StringUtils.EMPTY :
					String.valueOf(
						pensumFachstelle.getFachstelle()
							.getName()
					)
		);

		row.setKindIntegration(
			kind.getPensumFachstelle().isEmpty()
				|| pensumFachstelle == null ?
					StringUtils.EMPTY :
					ServerMessageUtil.translateEnumValue(
						pensumFachstelle.getIntegrationTyp(),
						locale,
						requireNonNull(
							betreuung.extractGemeinde()
								.getMandant()
						)
					)
		);

		row.setKindErwBeduerfnisse(betreuung.hasErweiterteBetreuung());
		row.setKindSprichtAmtssprache(kind.getSprichtAmtssprache());
		row.setKindEinschulungTyp(kind.getEinschulungTyp());
		row.setKeinPlatzImSchulhort(kind.getKeinPlatzInSchulhort());
		if (kind.getPensumAusserordentlicherAnspruch() != null) {
			row.setAusserordentlicherAnspruch(
				BigDecimal.valueOf(
					kind.getPensumAusserordentlicherAnspruch()
						.getPensum()
				)
			);
		}
	}

	@Nullable
	private PensumFachstelle getPensumFachstelleForGueltigkeit(
		Kind kind,
		DateRange gueltigkeit
	) {
		for (PensumFachstelle pensumFachstelle : kind.getPensumFachstelle()) {
			if (pensumFachstelle.getGueltigkeit().intersects(gueltigkeit)) {
				return pensumFachstelle;
			}
		}
		return null;
	}

	private void addBetreuungToGesuchstellerKinderBetreuungDataRow(
		GesuchstellerKinderBetreuungDataRow row,
		VerfuegungZeitabschnitt zeitabschnitt,
		Betreuung betreuung,
		@Nonnull Locale locale
	) {

		row.setZeitabschnittVon(zeitabschnitt.getGueltigkeit().getGueltigAb());
		row.setZeitabschnittBis(zeitabschnitt.getGueltigkeit().getGueltigBis());
		row.setBetreuungsStatus(
			ServerMessageUtil.getMessage(
				Betreuungsstatus.class.getSimpleName()
					+ '_'
					+ betreuung.getBetreuungsstatus().name(),
				locale,
				requireNonNull(betreuung.extractGemeinde().getMandant())
			)
		);
		row.setBetreuungspensum(
			MathUtil.DEFAULT.from(
				zeitabschnitt.getBetreuungspensumProzent()
			)
		);

		if (isAllowedBgDaten(betreuung)) {
			// Normalfall: Kanton=Kanton, Gemeinde=0, Total=Kanton
			BigDecimal anspruchsPensumKanton =
				new BigDecimal(
					zeitabschnitt.getBgCalculationResultAsiv()
						.getAnspruchspensumProzent()
				);
			BigDecimal anspruchsPensumGemeinde = BigDecimal.ZERO;
			BigDecimal anspruchsPensumTotal = anspruchsPensumKanton;
			if (zeitabschnitt.isHasGemeindeSpezifischeBerechnung()
				&& zeitabschnitt.getBgCalculationResultGemeinde() != null) {
				// Spezialfall: Kanton=Kanton, Gemeinde=Gemeinde-Kanton, Total=Gemeinde
				BigDecimal anspruchsPensumTotalGemeinde =
					new BigDecimal(
						zeitabschnitt.getBgCalculationResultGemeinde()
							.getAnspruchspensumProzent()
					);
				anspruchsPensumGemeinde =
					MathUtil.DEFAULT.subtractNullSafe(
						anspruchsPensumTotalGemeinde,
						anspruchsPensumKanton
					);
				anspruchsPensumTotal = anspruchsPensumTotalGemeinde;
			}
			row.setAnspruchsPensumKanton(anspruchsPensumKanton);
			row.setAnspruchsPensumGemeinde(anspruchsPensumGemeinde);
			row.setAnspruchsPensumTotal(anspruchsPensumTotal);

			// Normalfall: Kanton=Kanton, Gemeinde=0, Total=Kanton
			BigDecimal bgPensumKanton = zeitabschnitt
				.getBgCalculationResultAsiv()
				.getBgPensumProzent();
			BigDecimal bgPensumGemeinde = BigDecimal.ZERO;
			BigDecimal bgPensumTotal = bgPensumKanton;
			if (zeitabschnitt.isHasGemeindeSpezifischeBerechnung()
				&& zeitabschnitt.getBgCalculationResultGemeinde() != null) {
				// Spezialfall: Kanton=Kanton, Gemeinde=Gemeinde-Kanton, Total=Gemeinde
				BigDecimal bgPensumTotalGemeinde = zeitabschnitt
					.getBgCalculationResultGemeinde()
					.getBgPensumProzent();
				bgPensumGemeinde = MathUtil.DEFAULT.subtractNullSafe(
					bgPensumTotalGemeinde,
					bgPensumKanton
				);
				bgPensumTotal = bgPensumTotalGemeinde;
			}
			row.setBgPensumKanton(bgPensumKanton);
			row.setBgPensumGemeinde(bgPensumGemeinde);
			row.setBgPensumTotal(bgPensumTotal);
			row.setBgStunden(zeitabschnitt.getBgPensumZeiteinheit());

			// Wir koennen nicht die gespeicherte Zeiteinheit nehmen, da diese entweder Prozent oder Tage/Stunden ist
			// Daher fix TAGE fuer Kita, Mittagstisch und STUNDEN fuer TFO
			PensumUnits zeiteinheit =
				betreuung.getBetreuungsangebotTyp()
					== BetreuungsangebotTyp.TAGESFAMILIEN ?
						PensumUnits.HOURS :
						PensumUnits.DAYS;
			row.setBgPensumZeiteinheit(
				ServerMessageUtil.translateEnumValue(
					zeiteinheit,
					locale,
					requireNonNull(
						betreuung.extractGemeinde().getMandant()
					)
				)
			);

			row.setVollkosten(zeitabschnitt.getVollkosten());
			row.setElternbeitrag(zeitabschnitt.getElternbeitrag());
			// Normalfall: Kanton=Kanton, Gemeinde=0, Total=Kanton
			BigDecimal verguenstigungKanton = zeitabschnitt
				.getBgCalculationResultAsiv()
				.getVerguenstigung();
			BigDecimal verguenstigungGemeinde = BigDecimal.ZERO;
			BigDecimal verguenstigungTotal = verguenstigungKanton;
			if (zeitabschnitt.isHasGemeindeSpezifischeBerechnung()
				&& zeitabschnitt.getBgCalculationResultGemeinde() != null) {
				// Spezialfall: Kanton=Kanton, Gemeinde=Gemeinde-Kanton, Total=Gemeinde
				BigDecimal verguenstigungTotalGemeinde =
					zeitabschnitt.getBgCalculationResultGemeinde()
						.getVerguenstigung();
				verguenstigungGemeinde =
					MathUtil.DEFAULT.subtractNullSafe(
						verguenstigungTotalGemeinde,
						verguenstigungKanton
					);
				verguenstigungTotal = verguenstigungTotalGemeinde;
			}
			row.setVerguenstigungKanton(verguenstigungKanton);
			row.setVerguenstigungGemeinde(verguenstigungGemeinde);
			row.setVerguenstigungTotal(verguenstigungTotal);
		}

	}

	private boolean isAllowedBgDaten(Betreuung betreuung) {
		var auszahlungAnEltern = betreuung.isAuszahlungAnEltern();
		var isInstitutionUser = principalBean.isCallerInAnyOfRole(
			UserRole.getInstitutionTraegerschaftRoles()
		);
		return !(auszahlungAnEltern && isInstitutionUser);
	}

	private List<GesuchstellerKinderBetreuungDataRow> convertToGesuchstellerKinderBetreuungDataRow(
		List<VerfuegungZeitabschnitt> zeitabschnittList,
		@Nullable Gesuchsperiode gesuchsperiode,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) {

		List<GesuchstellerKinderBetreuungDataRow> dataRowList =
			new ArrayList<>();

		Map<Long, Gesuch> neustesVerfuegtesGesuchCache = new HashMap<>();
		List<Gesuch> gesuches = getAllGueltigeGesuch(gesuchsperiode, mandant);
		gesuches.forEach(
			gueltigeGesuch -> neustesVerfuegtesGesuchCache.put(
				gueltigeGesuch.getFall().getFallNummer(),
				gueltigeGesuch
			)
		);
		Map<String, Integer> maxFreiwilligenarbeitCache = new HashMap<>();
		List<Einstellung> einstellungen =
			findAlleMaxFreiwilligenarbeitEinstellungen(gesuchsperiode);
		einstellungen.forEach(
			einstellung -> maxFreiwilligenarbeitCache.put(
				getMaxFreiwilligenarbeitCacheKey(
					einstellung.getGemeinde(),
					einstellung.getGesuchsperiode()
				),
				einstellung.getValueAsInteger()
			)
		);

		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnittList) {
			GesuchstellerKinderBetreuungDataRow row =
				createRowForGesuchstellerKinderBetreuungReport(
					zeitabschnitt,
					neustesVerfuegtesGesuchCache,
					maxFreiwilligenarbeitCache,
					locale
				);
			dataRowList.add(row);
		}

		return dataRowList;
	}

	private List<Einstellung> findAlleMaxFreiwilligenarbeitEinstellungen(
		@Nullable Gesuchsperiode gesuchsperiode
	) {
		return einstellungService.findEinstellungen(
			EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT,
			gesuchsperiode
		);
	}

	@Nonnull
	private String getMaxFreiwilligenarbeitCacheKey(
		@Nullable Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode
	) {
		return gemeinde != null ?
			gemeinde.getId() + "_" + gesuchsperiode.getId() :
			gesuchsperiode.getId();
	}

	@SuppressWarnings("Duplicates")
	private GesuchstellerKinderBetreuungDataRow createRowForGesuchstellerKinderBetreuungReport(
		VerfuegungZeitabschnitt zeitabschnitt,
		Map<Long, Gesuch> neustesVerfuegtesGesuchCache,
		Map<String, Integer> maxFreiwilligenarbeitCache,
		@Nonnull Locale locale
	) {
		Betreuung gueltigeBetreuung = zeitabschnitt.getVerfuegung()
			.getBetreuung();
		Objects.requireNonNull(gueltigeBetreuung);
		Gesuch gesuch = gueltigeBetreuung.extractGesuch();
		Gesuch gueltigeGesuch = null;

		final String maxFreiwilligenarbeitCacheKey =
			getMaxFreiwilligenarbeitCacheKey(
				gesuch.extractGemeinde(),
				gesuch.getGesuchsperiode()
			);
		Integer maxFreiwilligenarbeit = null;
		if (maxFreiwilligenarbeitCache.containsKey(
			maxFreiwilligenarbeitCacheKey
		)) {
			maxFreiwilligenarbeit = maxFreiwilligenarbeitCache.get(
				maxFreiwilligenarbeitCacheKey
			);
		} else {
			maxFreiwilligenarbeit = maxFreiwilligenarbeitCache.get(
				gesuch.getGesuchsperiode().getId()
			);
		}

		//prüfen ob Gesuch ist gültig, und via GesuchService oder Cache holen, inkl. Kind & Betreuung
		if (!gesuch.isGueltig()) {
			gueltigeGesuch = neustesVerfuegtesGesuchCache.getOrDefault(
				gesuch.getFall().getFallNummer(),
				gesuch
			);
			Optional<KindContainer> gueltigeKind = getGueltigesKind(
				zeitabschnitt,
				gueltigeGesuch
			);
			gueltigeBetreuung = getGueltigeBetreuung(
				zeitabschnitt,
				gueltigeBetreuung,
				gueltigeKind
			);
		} else {
			gueltigeGesuch = gesuch;
		}

		GesuchstellerKinderBetreuungDataRow row =
			new GesuchstellerKinderBetreuungDataRow();
		// Betreuung
		addBetreuungToGesuchstellerKinderBetreuungDataRow(
			row,
			zeitabschnitt,
			gueltigeBetreuung,
			locale
		);
		// Stammdaten
		addStammdaten(row, zeitabschnitt, gueltigeGesuch, locale);

		// Gesuchsteller 1: Prozent-Felder initialisieren, damit im Excel das Total sicher berechnet werden kann
		row.setGs1EwpAngestellt(0);
		row.setGs1EwpAusbildung(0);
		row.setGs1EwpSelbstaendig(0);
		row.setGs1EwpRav(0);
		row.setGs1EwpGesundhtl(0);
		row.setGs1EwpIntegration(0);
		row.setGs1EwpFreiwillig(0);
		GesuchstellerContainer gs1Container = gueltigeGesuch
			.getGesuchsteller1();
		if (gs1Container != null) {
			addGesuchsteller1ToGesuchstellerKinderBetreuungDataRow(
				row,
				gs1Container,
				maxFreiwilligenarbeit
			);
		}
		// Gesuchsteller 2: Prozent-Felder initialisieren, damit im Excel das Total sicher berechnet werden kann
		row.setGs2EwpAngestellt(0);
		row.setGs2EwpAusbildung(0);
		row.setGs2EwpSelbstaendig(0);
		row.setGs2EwpRav(0);
		row.setGs2EwpGesundhtl(0);
		row.setGs2EwpIntegration(0);
		row.setGs2EwpFreiwillig(0);
		if (gueltigeGesuch.getGesuchsteller2() != null) {
			addGesuchsteller2ToGesuchstellerKinderBetreuungDataRow(
				row,
				gueltigeGesuch.getGesuchsteller2(),
				maxFreiwilligenarbeit
			);
		}
		// Familiensituation / Einkommen
		FamiliensituationContainer familiensituationContainer = gueltigeGesuch
			.getFamiliensituationContainer();
		if (familiensituationContainer != null) {
			Familiensituation familiensituation =
				familiensituationContainer.getFamiliensituationAm(
					row.getZeitabschnittVon()
				);
			row.setFamiliensituation(familiensituation.getFamilienstatus());
			row.setSozialhilfeBezueger(
				reportService.isSozialhilfeBezueger(
					zeitabschnitt,
					familiensituationContainer,
					familiensituation
				)
			);
			// Auszahlungsdaten
			addAuszahlungsdaten(row, familiensituation);
		}
		row.setFamiliengroesse(zeitabschnitt.getFamGroesse());
		row.setMassgEinkVorFamilienabzug(
			zeitabschnitt.getMassgebendesEinkommenVorAbzFamgr()
		);
		row.setFamilienabzug(zeitabschnitt.getAbzugFamGroesse());
		row.setMassgEink(zeitabschnitt.getMassgebendesEinkommen());
		row.setEinkommensjahr(zeitabschnitt.getEinkommensjahr());
		if (gueltigeGesuch.getEinkommensverschlechterungInfoContainer()
			!= null) {
			row.setEkvVorhandenBasisJahr1(
				gueltigeGesuch.getEinkommensverschlechterungInfoContainer()
					.getEinkommensverschlechterungInfoJA()
					.getEkvFuerBasisJahrPlus1()
			);
			row.setEkvVorhandenBasisJahr2(
				gueltigeGesuch.getEinkommensverschlechterungInfoContainer()
					.getEinkommensverschlechterungInfoJA()
					.getEkvFuerBasisJahrPlus2()
			);
			row.setEkvAnnuliertBasisJahr1(
				gueltigeGesuch.getEinkommensverschlechterungInfoContainer()
					.getEinkommensverschlechterungInfoJA()
					.getEkvBasisJahrPlus1Annulliert()
			);
			row.setEkvAnnuliertBasisJahr2(
				gueltigeGesuch.getEinkommensverschlechterungInfoContainer()
					.getEinkommensverschlechterungInfoJA()
					.getEkvBasisJahrPlus2Annulliert()
			);
		}
		row.setStvGeprueft(gesuch.isGeprueftSTV());
		if (gueltigeGesuch.getGesuchsteller1() != null
			&&
			gueltigeGesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				!= null) {
			row.setVeranlagt(
				gueltigeGesuch.getGesuchsteller1()
					.getFinanzielleSituationContainer()
					.getFinanzielleSituationJA()
					.getSteuerveranlagungErhalten()
			);
		} else {
			row.setVeranlagt(Boolean.FALSE);
		}
		if (gueltigeGesuch.getFamiliensituationContainer() != null
			&&
			gueltigeGesuch.getFamiliensituationContainer()
				.getFamiliensituationJA()
				!= null) {
			row.setMzvBeantragt(
				!gueltigeGesuch.getFamiliensituationContainer()
					.getFamiliensituationJA()
					.isKeineMahlzeitenverguenstigungBeantragt()
			);
		} else {
			row.setMzvBeantragt(Boolean.FALSE);
		}

		// Kind
		addKindToGesuchstellerKinderBetreuungDataRow(
			row,
			zeitabschnitt,
			gueltigeBetreuung,
			locale
		);
		return row;
	}

	private void addAuszahlungsdaten(
		GesuchstellerKinderBetreuungDataRow row,
		Familiensituation familiensituation
	) {
		if (familiensituation.getAuszahlungsdaten() == null) {
			return;
		}

		row.setIban(
			familiensituation.getAuszahlungsdaten().extractIbanAsString()
		);
		row.setKontoinhaber(
			familiensituation.getAuszahlungsdaten().getKontoinhaber()
		);
	}

	private List<GesuchstellerKinderBetreuungDataRow> convertToKinderDataRow(
		List<VerfuegungZeitabschnitt> zeitabschnittList,
		@Nonnull Locale locale,
		@Nullable Gesuchsperiode gesuchsperiode,
		@Nonnull Mandant mandant
	) {

		List<GesuchstellerKinderBetreuungDataRow> dataRowList =
			new ArrayList<>();

		Map<String, Gesuch> neustesVerfuegtesGesuchCache = new HashMap<>();
		List<Gesuch> gesuches = getAllGueltigeGesuch(gesuchsperiode, mandant);
		gesuches.forEach(
			gueltigeGesuch -> neustesVerfuegtesGesuchCache.put(
				constructCacheKey(gueltigeGesuch),
				gueltigeGesuch
			)
		);
		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnittList) {
			GesuchstellerKinderBetreuungDataRow row =
				createRowForKinderReport(
					zeitabschnitt,
					neustesVerfuegtesGesuchCache,
					locale
				);
			dataRowList.add(row);
		}

		return dataRowList;
	}

	private static String constructCacheKey(Gesuch gueltigeGesuch) {
		return gueltigeGesuch.extractGemeinde().getBfsNummer()
			+ ":"
			+ gueltigeGesuch.getFall().getFallNummer();
	}

	private GesuchstellerKinderBetreuungDataRow createRowForKinderReport(
		VerfuegungZeitabschnitt zeitabschnitt,
		Map<String, Gesuch> neustesVerfuegtesGesuchCache,
		@Nonnull Locale locale
	) {
		Betreuung gueltigeBetreuung = zeitabschnitt.getVerfuegung()
			.getBetreuung();
		Objects.requireNonNull(gueltigeBetreuung);
		Gesuch gesuch = gueltigeBetreuung.extractGesuch();
		Gesuch gueltigeGesuch = null;

		//prüfen ob Gesuch ist gültig, und via GesuchService oder Cache holen, inkl. Kind & Betreuung
		if (!gesuch.isGueltig()) {
			gueltigeGesuch = neustesVerfuegtesGesuchCache.getOrDefault(
				constructCacheKey(gesuch),
				gesuch
			);

			Optional<KindContainer> gueltigeKind = getGueltigesKind(
				zeitabschnitt,
				gueltigeGesuch
			);

			gueltigeBetreuung = getGueltigeBetreuung(
				zeitabschnitt,
				gueltigeBetreuung,
				gueltigeKind
			);
		} else {
			gueltigeGesuch = gesuch;
		}

		GesuchstellerKinderBetreuungDataRow row =
			new GesuchstellerKinderBetreuungDataRow();
		addStammdaten(row, zeitabschnitt, gueltigeGesuch, locale);

		// Gesuchsteller 1
		GesuchstellerContainer gs1Container = gueltigeGesuch
			.getGesuchsteller1();
		if (gs1Container != null) {
			Gesuchsteller gs1 = gs1Container.getGesuchstellerJA();
			row.setGs1Name(gs1.getNachname());
			row.setGs1Vorname(gs1.getVorname());
		}
		// Gesuchsteller 2
		if (gueltigeGesuch.getGesuchsteller2() != null) {
			Gesuchsteller gs2 = gueltigeGesuch.getGesuchsteller2()
				.getGesuchstellerJA();
			row.setGs2Name(gs2.getNachname());
			row.setGs2Vorname(gs2.getVorname());
		}

		// Kind
		addKindToGesuchstellerKinderBetreuungDataRow(
			row,
			zeitabschnitt,
			gueltigeBetreuung,
			locale
		);

		// Betreuung
		addBetreuungToGesuchstellerKinderBetreuungDataRow(
			row,
			zeitabschnitt,
			gueltigeBetreuung,
			locale
		);

		row.setShowBgSensitiveData(isAllowedBgDaten(gueltigeBetreuung));

		return row;
	}

	@Nonnull
	private RowFiller fillAndMergeRows(
		ReportVorlage reportResource,
		XSSFSheet sheet,
		List<GesuchstellerKinderBetreuungDataRow> reportData,
		@Nonnull Locale locale
	) {

		RowFiller rowFiller = RowFiller.initRowFiller(
			sheet,
			MergeFieldProvider.toMergeFields(
				reportResource.getMergeFields()
			),
			reportData.size()
		);

		gesuchstellerKinderBetreuungExcelConverter.mergeRows(
			rowFiller,
			reportData,
			locale,
			requireNonNull(principalBean.getMandant())
		);
		gesuchstellerKinderBetreuungExcelConverter.applyAutoSize(sheet);

		return rowFiller;
	}

	@Nonnull
	private List<GesuchstellerKinderBetreuungDataRow> getReportDataGesuchsteller(
		@Nonnull LocalDate stichtag,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) {
		List<VerfuegungZeitabschnitt> zeitabschnittList =
			getReportDataBetreuungen(stichtag, mandant);

		List<GesuchstellerKinderBetreuungDataRow> dataRows =
			convertToGesuchstellerKinderBetreuungDataRow(
				zeitabschnittList,
				null,
				locale,
				mandant
			);

		dataRows.sort(
			Comparator.comparing(
				GesuchstellerKinderBetreuungDataRow::getReferenzNummer
			)
				.thenComparing(
					GesuchstellerKinderBetreuungDataRow::getZeitabschnittVon
				)
		);

		return dataRows;
	}

	@Nonnull
	private List<VerfuegungZeitabschnitt> getReportDataBetreuungen(
		@Nonnull LocalDate stichtag,
		@Nonnull Mandant mandant
	) {
		validateStichtagParam(stichtag);

		Benutzer user = benutzerService.getCurrentBenutzer()
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"getReportDataBetreuungen",
					NO_USER_IS_LOGGED_IN
				)
			);

		// Alle Verfuegungszeitabschnitte zwischen datumVon und datumBis. Aber pro Fall immer nur das zuletzt
		// verfuegte.
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<VerfuegungZeitabschnitt> query = builder
			.createQuery(VerfuegungZeitabschnitt.class);
		query.distinct(true);
		Root<VerfuegungZeitabschnitt> root = query.from(
			VerfuegungZeitabschnitt.class
		);
		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(
			VerfuegungZeitabschnitt_.verfuegung
		);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(
			Verfuegung_.betreuung
		);
		Join<Betreuung, KindContainer> joinKindContainer = joinBetreuung.join(
			AbstractPlatz_.kind,
			JoinType.LEFT
		);
		Join<KindContainer, Gesuch> joinGesuch = joinKindContainer.join(
			KindContainer_.gesuch,
			JoinType.LEFT
		);
		Join<Gesuch, Dossier> joinDossier = joinGesuch.join(
			Gesuch_.dossier,
			JoinType.LEFT
		);
		Join<Dossier, Gemeinde> joinGemeinde = joinDossier.join(
			Dossier_.gemeinde,
			JoinType.LEFT
		);

		List<Predicate> predicatesToUse = new ArrayList<>();

		// mandant
		Predicate mandantPredicate = builder.equal(
			joinGemeinde.get(Gemeinde_.mandant),
			mandant
		);
		predicatesToUse.add(mandantPredicate);

		// Stichtag
		Predicate intervalPredicate = builder.between(
			builder.literal(stichtag),
			root.get(AbstractDateRangedEntity_.gueltigkeit)
				.get(DateRange_.gueltigAb),
			root.get(AbstractDateRangedEntity_.gueltigkeit)
				.get(DateRange_.gueltigBis)
		);
		predicatesToUse.add(intervalPredicate);
		// Nur neueste Verfuegung jedes Falls beachten
		Predicate predicateGueltig = builder.equal(
			joinBetreuung.get(AbstractPlatz_.gueltig),
			Boolean.TRUE
		);

		// Nur Gesuche von Gemeinden, fuer die ich berechtigt bin
		setGemeindeFilterForCurrentUser(user, joinGemeinde, predicatesToUse);

		predicatesToUse.add(predicateGueltig);

		// Sichtbarkeit nach eingeloggtem Benutzer
		boolean isInstitutionsbenutzer =
			principalBean.isCallerInAnyOfRole(
				UserRole.getInstitutionTraegerschaftRoles()
			);
		if (isInstitutionsbenutzer) {
			Collection<Institution> allowedInstitutionen =
				institutionService
					.getInstitutionenReadableForCurrentBenutzer(false);
			Predicate predicateAllowedInstitutionen = root.get(
				VerfuegungZeitabschnitt_.verfuegung
			)
				.get(Verfuegung_.betreuung)
				.get(AbstractPlatz_.institutionStammdaten)
				.get(InstitutionStammdaten_.institution)
				.in(allowedInstitutionen);
			predicatesToUse.add(predicateAllowedInstitutionen);
		}
		Predicate predicateForBenutzerRole = getPredicateForBenutzerRole(
			builder,
			root
		);
		if (predicateForBenutzerRole != null) {
			predicatesToUse.add(predicateForBenutzerRole);
		}
		query.where(
			CriteriaQueryHelper.concatenateExpressions(
				builder,
				predicatesToUse
			)
		);
		return persistence.getCriteriaResults(query);
	}

	private List<Gesuch> getAllGueltigeGesuch(
		Gesuchsperiode gesuchsperiode,
		@Nonnull Mandant mandant
	) {
		Benutzer user = benutzerService.getCurrentBenutzer()
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"getAllGueltigeGesuch",
					NO_USER_IS_LOGGED_IN
				)
			);

		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = builder.createQuery(Gesuch.class);
		query.distinct(true);
		Root<Gesuch> root = query.from(Gesuch.class);
		Join<Gesuch, Dossier> joinDossier = root.join(
			Gesuch_.dossier,
			JoinType.LEFT
		);
		Join<Dossier, Gemeinde> joinGemeinde = joinDossier.join(
			Dossier_.gemeinde,
			JoinType.LEFT
		);

		List<Predicate> predicatesToUse = new ArrayList<>();

		// Nur neueste Verfuegung
		Predicate predicateGueltig = builder.equal(
			root.get(Gesuch_.gueltig),
			Boolean.TRUE
		);

		// Nur Gesuche von Gemeinden, fuer die ich berechtigt bin
		setGemeindeFilterForCurrentUser(user, joinGemeinde, predicatesToUse);

		// Nur Gesuche von Gemeinden die in dasselbe Mandant sind
		Predicate predicateMandantGemeinde = builder.equal(
			joinGemeinde.get(Gemeinde_.mandant),
			mandant
		);
		predicatesToUse.add(predicateMandantGemeinde);

		// Gesuchsperiode
		if (gesuchsperiode != null) {
			Predicate predicateGesuchsperiode = builder.equal(
				root.get(Gesuch_.gesuchsperiode),
				gesuchsperiode
			);
			predicatesToUse.add(predicateGesuchsperiode);
		}

		predicatesToUse.add(predicateGueltig);
		query.where(
			CriteriaQueryHelper.concatenateExpressions(
				builder,
				predicatesToUse
			)
		);
		return persistence.getCriteriaResults(query);
	}

}
