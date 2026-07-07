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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.filter.suchfilter.smarttable.BenutzerSearchDTO;
import ch.dvbern.ebegu.dto.filter.suchfilter.smarttable.BenutzerTableMandantFilterDTO;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyKey;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AbstractPlatz_;
import ch.dvbern.ebegu.entities.Abwesenheit;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.AntragStatusHistory;
import ch.dvbern.ebegu.entities.AntragStatusHistory_;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Benutzer_;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Berechtigung_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Fall_;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraum;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraumContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt_;
import ch.dvbern.ebegu.entities.Verfuegung_;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.ferienbetreuung.FerienbetreuungAngaben;
import ch.dvbern.ebegu.entities.gemeindeantrag.ferienbetreuung.FerienbetreuungAngabenAngebot;
import ch.dvbern.ebegu.entities.gemeindeantrag.ferienbetreuung.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.ferienbetreuung.FerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.entities.gemeindeantrag.ferienbetreuung.FerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.entities.gemeindeantrag.ferienbetreuung.FerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.ferienbetreuung.FerienbetreuungBerechnungen;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.enums.reporting.DatumTyp;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.file.FileSaverService;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.reporting.ReportService;
import ch.dvbern.ebegu.reporting.benutzer.BenutzerDataRow;
import ch.dvbern.ebegu.reporting.benutzer.BenutzerExcelConverter;
import ch.dvbern.ebegu.reporting.ferienbetreuung.FerienbetreuungDataRow;
import ch.dvbern.ebegu.reporting.ferienbetreuung.FerienbetreuungExcelConverter;
import ch.dvbern.ebegu.reporting.gesuchstichtag.GesuchStichtagDataRow;
import ch.dvbern.ebegu.reporting.gesuchstichtag.GesuchStichtagExcelConverter;
import ch.dvbern.ebegu.reporting.gesuchzeitraum.GesuchZeitraumDataRow;
import ch.dvbern.ebegu.reporting.gesuchzeitraum.GesuchZeitraumExcelConverter;
import ch.dvbern.ebegu.reporting.kanton.KantonDataRow;
import ch.dvbern.ebegu.reporting.kanton.KantonExcelConverter;
import ch.dvbern.ebegu.reporting.kanton.institutionen.InstitutionenDataRow;
import ch.dvbern.ebegu.reporting.kanton.institutionen.InstitutionenExcelConverter;
import ch.dvbern.ebegu.reporting.kanton.mitarbeiterinnen.MitarbeiterinnenDataRow;
import ch.dvbern.ebegu.reporting.kanton.mitarbeiterinnen.MitarbeiterinnenExcelConverter;
import ch.dvbern.ebegu.reporting.zahlungauftrag.ZahlungAuftragDetailsExcelConverter;
import ch.dvbern.ebegu.reporting.zahlungauftrag.ZahlungAuftragPeriodeExcelConverter;
import ch.dvbern.ebegu.reporting.zahlungauftrag.ZahlungAuftragTotalsExcelConverter;
import ch.dvbern.ebegu.reporting.zahlungsauftrag.ZahlungDataRow;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.services.TraegerschaftService;
import ch.dvbern.ebegu.services.ZahlungService;
import ch.dvbern.ebegu.services.gemeindeantrag.FerienbetreuungService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelper;
import ch.dvbern.ebegu.util.zahlungslauf.ZahlungslaufHelperFactory;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import ch.dvbern.oss.lib.excelmerger.RowFiller;
import ch.dvbern.oss.lib.excelmerger.mergefields.MergeFieldProvider;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.jboss.ejb3.annotation.TransactionTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.services.reporting.ReportUtil.createWorkbook;
import static ch.dvbern.ebegu.services.reporting.ReportUtil.getContentTypeForExport;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setGemeindeFilterForCurrentUser;
import static java.util.Objects.requireNonNull;

@Stateless
@Local(ReportService.class)
public class ReportServiceBean extends AbstractReportServiceBean implements
	ReportService {

	@Inject
	private BenutzerService benutzerService;

	private static final Logger LOGGER = LoggerFactory.getLogger(
		ReportServiceBean.class
	);

	// Excel kann nicht mit Datum vor 1800 umgehen. Wir setzen auf 1900, wie Minimum im datepicker
	private static final LocalDate MIN_DATE = LocalDate.of(
		1900,
		Month.JANUARY,
		1
	);

	@Inject
	private GesuchStichtagExcelConverter gesuchStichtagExcelConverter;

	@Inject
	private GesuchZeitraumExcelConverter gesuchZeitraumExcelConverter;

	@Inject
	private KantonExcelConverter kantonExcelConverter;

	@Inject
	private MitarbeiterinnenExcelConverter mitarbeiterinnenExcelConverter;

	@Inject
	private BenutzerExcelConverter benutzerExcelConverter;

	@Inject
	private InstitutionenExcelConverter institutionenExcelConverter;

	@Inject
	private ZahlungAuftragDetailsExcelConverter zahlungAuftragDetailsExcelConverter;

	@Inject
	private ZahlungAuftragTotalsExcelConverter zahlungAuftragTotalsExcelConverter;

	@Inject
	private ZahlungAuftragPeriodeExcelConverter zahlungAuftragPeriodeExcelConverter;

	@Inject
	private FerienbetreuungExcelConverter ferienbetreuungExcelConverter;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private TraegerschaftService traegerschaftService;

	@Inject
	private Persistence persistence;

	@Inject
	private ZahlungService zahlungService;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	ExcelFileSaverService excelFileSaverService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private KindService kindService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private FerienbetreuungService ferienbetreuungService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@SuppressWarnings({ "Duplicates", "PMD.CloseResource" })
	@Nonnull
	@Override
	public List<GesuchStichtagDataRow> getReportDataGesuchStichtag(
		@Nonnull LocalDate date,
		@Nullable String gesuchPeriodeID,
		@Nonnull Mandant mandant
	) {

		requireNonNull(date, "Das Argument 'date' darf nicht leer sein");

		EntityManager em = persistence.getEntityManager();

		//noinspection JpaQueryApiInspection
		TypedQuery<GesuchStichtagDataRow> query =
			em.createNamedQuery(
				"GesuchStichtagNativeSQLQuery",
				GesuchStichtagDataRow.class
			);

		// Wir rechnen zum Stichtag einen Tag dazu, damit es bis 24.00 des Vorabends gilt.
		query.setParameter(
			"stichTagDate",
			Constants.SQL_DATE_FORMAT.format(date.plusDays(1))
		);
		query.setParameter("gesuchPeriodeID", gesuchPeriodeID);
		query.setParameter("onlySchulamt", onlySchulamt());
		query.setParameter("mandant", mandant.getId().replace("-", ""));
		final List<String> berechtigteGemeinden =
			getListOfBerechtigteGemeinden();
		// we need to remove the extra - as in the query they are not working and we cannot use a REPLACE function on
		// a list in a native query
		final List<String> berechtigeGemeindenUnhex = new ArrayList<>();
		if (berechtigteGemeinden != null) {
			berechtigteGemeinden.forEach(s -> {
				berechtigeGemeindenUnhex.add(s.replace("-", ""));
			});
		}

		// pass a boolean param to indicate if it has to take all Gemeinden or just those of the user
		// this is easier than checking the list within the sql-query
		query.setParameter("allGemeinden", berechtigteGemeinden == null);
		query.setParameter(
			"gemeindeIdList",
			berechtigteGemeinden == null ?
				null :
				berechtigeGemeindenUnhex
		);
		List<GesuchStichtagDataRow> glist = query.getResultList();

		return glist;
	}

	@Nullable
	private List<String> getListOfBerechtigteGemeinden() {
		Benutzer benutzer = benutzerService.getCurrentBenutzer()
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"getListOfBerechtigteGemeinden",
					"User not logged in"
				)
			);

		if (!benutzer.getCurrentBerechtigung()
			.getRole()
			.isRoleGemeindeabhaengig()) {
			return null;
		}

		return benutzer.extractGemeindenForUser()
			.stream()
			.map(AbstractEntity::getId)
			.collect(Collectors.toList());
	}

	@SuppressWarnings("Duplicates")
	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportGesuchStichtag(
		@Nonnull LocalDate date,
		@Nullable String gesuchPeriodeID,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) throws ExcelMergeException, IOException {

		requireNonNull(date, "Das Argument 'date' darf nicht leer sein");

		final ReportVorlage reportVorlage = locale.equals(Locale.FRENCH) ?
			ReportVorlage.VORLAGE_REPORT_GESUCH_STICHTAG_FR :
			ReportVorlage.VORLAGE_REPORT_GESUCH_STICHTAG_DE;

		try (
			Workbook workbook = createWorkbook(reportVorlage);
		) {
			Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

			List<GesuchStichtagDataRow> reportData =
				getReportDataGesuchStichtag(date, gesuchPeriodeID, mandant);
			ExcelMergerDTO excelMergerDTO = gesuchStichtagExcelConverter
				.toExcelMergerDTO(reportData, locale, mandant);

			mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
			gesuchStichtagExcelConverter.applyAutoSize(sheet);

			byte[] bytes = createWorkbook(workbook);

			return fileSaverService.save(
				bytes,
				getFileName(reportVorlage, locale, mandant),
				Constants.TEMP_REPORT_FOLDERNAME,
				getContentTypeForExport()
			);
		}
	}

	@SuppressWarnings({ "Duplicates", "PMD.CloseResource" })
	@Nonnull
	@Override
	public List<GesuchZeitraumDataRow> getReportDataGesuchZeitraum(
		@Nonnull LocalDate dateVon,
		@Nonnull LocalDate dateBis,
		@Nonnull DatumTyp datumTyp,
		@Nullable String gesuchPeriodeID,
		@Nonnull Mandant mandant
	) {

		validateDateParams(dateVon, dateBis);

		// Bevor wir die Statistik starten, muessen gewissen Werte nachgefuehrt werden
		runStatisticsBetreuung(mandant);
		runStatisticsAbwesenheiten(mandant);
		runStatisticsKinder(mandant);

		EntityManager em = persistence.getEntityManager();

		//noinspection JpaQueryApiInspection
		TypedQuery<GesuchZeitraumDataRow> query =
			em.createNamedQuery(
				datumTyp.getQueryName(),
				GesuchZeitraumDataRow.class
			);

		query.setParameter(
			"fromDateTime",
			Constants.SQL_DATE_FORMAT.format(dateVon)
		);
		query.setParameter(
			"fromDate",
			Constants.SQL_DATE_FORMAT.format(dateVon)
		);
		query.setParameter(
			"toDateTime",
			Constants.SQL_DATE_FORMAT.format(dateBis)
		);
		query.setParameter("toDate", Constants.SQL_DATE_FORMAT.format(dateBis));
		query.setParameter("gesuchPeriodeID", gesuchPeriodeID);
		query.setParameter("onlySchulamt", onlySchulamt());
		query.setParameter("mandant", mandant.getId().replace("-", ""));
		final List<String> berechtigteGemeinden =
			getListOfBerechtigteGemeinden();
		// we need to remove the extra - as in the query they are not working and we cannot use a REPLACE function on
		// a list in a native query
		final List<String> berechtigeGemeindenUnhex = new ArrayList<>();
		if (berechtigteGemeinden != null) {
			berechtigteGemeinden.forEach(s -> {
				berechtigeGemeindenUnhex.add(s.replace("-", ""));
			});
		}
		// pass a boolean param to indicate if it has to take all Gemeinden are just those of the user
		// this is easier than checking the list within the sql-query
		query.setParameter("allGemeinden", berechtigteGemeinden == null);
		query.setParameter(
			"gemeindeIdList",
			berechtigteGemeinden == null ?
				null :
				berechtigeGemeindenUnhex
		);

		return query.getResultList();
	}

	@SuppressWarnings("Duplicates")
	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportGesuchZeitraum(
		@Nonnull LocalDate dateVon,
		@Nonnull LocalDate dateBis,
		@Nonnull DatumTyp datumTyp,
		@Nullable String gesuchPeriodeID,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) throws ExcelMergeException, IOException {

		validateDateParams(dateVon, dateBis);
		validateDateParams(dateVon, dateBis);

		final ReportVorlage reportVorlage = locale.equals(Locale.FRENCH) ?
			ReportVorlage.VORLAGE_REPORT_GESUCH_ZEITRAUM_FR :
			ReportVorlage.VORLAGE_REPORT_GESUCH_ZEITRAUM_DE;

		try (
			Workbook workbook = createWorkbook(reportVorlage);
		) {
			Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

			List<GesuchZeitraumDataRow> reportData =
				getReportDataGesuchZeitraum(
					dateVon,
					dateBis,
					datumTyp,
					gesuchPeriodeID,
					mandant
				);
			ExcelMergerDTO excelMergerDTO = gesuchZeitraumExcelConverter
				.toExcelMergerDTO(reportData, locale, mandant);

			mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
			gesuchZeitraumExcelConverter.applyAutoSize(sheet);

			byte[] bytes = createWorkbook(workbook);

			return fileSaverService.save(
				bytes,
				getFileName(reportVorlage, locale, mandant),
				Constants.TEMP_REPORT_FOLDERNAME,
				getContentTypeForExport()
			);
		}
	}

	@Nonnull
	@SuppressWarnings("PMD.NcssMethodCount, PMD.AvoidDuplicateLiterals")
	@Override
	public List<KantonDataRow> getReportDataKanton(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) {
		validateDateParams(datumVon, datumBis);

		Benutzer user = benutzerService.getCurrentBenutzer()
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"getReportDataKanton",
					NO_USER_IS_LOGGED_IN
				)
			);

		Collection<Gesuchsperiode> relevanteGesuchsperioden =
			gesuchsperiodeService.getGesuchsperiodenBetween(
				datumVon,
				datumBis
			);
		if (relevanteGesuchsperioden.isEmpty()) {
			return Collections.emptyList();
		}
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

		// startAbschnitt <= datumBis && endeAbschnitt >= datumVon
		Predicate predicateStart = builder.lessThanOrEqualTo(
			root.get(AbstractDateRangedEntity_.gueltigkeit)
				.get(DateRange_.gueltigAb),
			datumBis
		);
		predicatesToUse.add(predicateStart);
		Predicate predicateEnd = builder.greaterThanOrEqualTo(
			root.get(AbstractDateRangedEntity_.gueltigkeit)
				.get(DateRange_.gueltigBis),
			datumVon
		);
		predicatesToUse.add(predicateEnd);
		Predicate mandantPredicate = builder.equal(
			joinGemeinde.get(Gemeinde_.mandant),
			mandant
		);
		predicatesToUse.add(mandantPredicate);

		Predicate predicateGesuchsperiode = root.get(
			VerfuegungZeitabschnitt_.verfuegung
		)
			.get(Verfuegung_.betreuung)
			.get(Betreuung_.kind)
			.get(KindContainer_.gesuch)
			.get(Gesuch_.gesuchsperiode)
			.in(relevanteGesuchsperioden);
		predicatesToUse.add(predicateGesuchsperiode);

		// Nur neueste Verfuegung jedes Falls beachten
		Predicate predicateGueltig = builder.equal(
			joinBetreuung.get(Betreuung_.gueltig),
			Boolean.TRUE
		);
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
				.get(Betreuung_.institutionStammdaten)
				.get(InstitutionStammdaten_.institution)
				.in(allowedInstitutionen);
			predicatesToUse.add(predicateAllowedInstitutionen);
		}

		// Nur Gesuche von Gemeinden, fuer die ich berechtigt bin
		setGemeindeFilterForCurrentUser(user, joinGemeinde, predicatesToUse);

		query.where(
			CriteriaQueryHelper.concatenateExpressions(
				builder,
				predicatesToUse
			)
		);
		List<VerfuegungZeitabschnitt> zeitabschnittList = persistence
			.getCriteriaResults(query);
		List<KantonDataRow> kantonDataRowList = convertToKantonDataRow(
			zeitabschnittList
		);
		kantonDataRowList.sort(
			Comparator.comparing(KantonDataRow::getReferenzNummer)
				.thenComparing(KantonDataRow::getZeitabschnittVon)
		);
		return kantonDataRowList;
	}

	@Nonnull
	private List<KantonDataRow> convertToKantonDataRow(
		List<VerfuegungZeitabschnitt> zeitabschnittList
	) {
		List<KantonDataRow> kantonDataRowList = new ArrayList<>();
		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnittList) {
			KantonDataRow row = new KantonDataRow();
			Betreuung betreuung = zeitabschnitt.getVerfuegung().getBetreuung();
			Objects.requireNonNull(betreuung);
			final Gesuch gesuch = betreuung.extractGesuch();

			row.setGemeinde(gesuch.extractGemeinde().getName());
			row.setReferenzNummer(betreuung.getReferenzNummer());
			row.setGesuchId(gesuch.getId());
			row.setName(betreuung.getKind().getKindJA().getNachname());
			row.setVorname(betreuung.getKind().getKindJA().getVorname());
			row.setGeburtsdatum(
				betreuung.getKind().getKindJA().getGeburtsdatum()
			);
			if (row.getGeburtsdatum() == null
				|| row.getGeburtsdatum().isBefore(MIN_DATE)) {
				row.setGeburtsdatum(MIN_DATE);
			}
			row.setZeitabschnittVon(
				zeitabschnitt.getGueltigkeit().getGueltigAb()
			);
			row.setZeitabschnittBis(
				zeitabschnitt.getGueltigkeit().getGueltigBis()
			);

			// Normalfall: Kanton=Kanton, Gemeinde=0, Total=Kanton
			BigDecimal pensumKanton = zeitabschnitt.getBgCalculationResultAsiv()
				.getBgPensumProzent();
			BigDecimal pensumGemeinde = BigDecimal.ZERO;
			BigDecimal pensumTotal = pensumKanton;
			if (zeitabschnitt.isHasGemeindeSpezifischeBerechnung()
				&& zeitabschnitt.getBgCalculationResultGemeinde() != null) {
				// Spezialfall: Kanton=Kanton, Gemeinde=Gemeinde-Kanton, Total=Gemeinde
				BigDecimal pensumTotalGemeinde = zeitabschnitt
					.getBgCalculationResultGemeinde()
					.getBgPensumProzent();
				pensumGemeinde = MathUtil.DEFAULT.subtractNullSafe(
					pensumTotalGemeinde,
					pensumKanton
				);
				pensumTotal = pensumTotalGemeinde;
			}
			row.setBgPensumKanton(pensumKanton);
			row.setBgPensumGemeinde(pensumGemeinde);
			row.setBgPensumTotal(pensumTotal);

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

			row.setInstitution(
				betreuung.getInstitutionStammdaten()
					.getInstitution()
					.getName()
			);
			row.setBetreuungsTyp(betreuung.getBetreuungsangebotTyp().name());
			row.setBabyTarif(
				zeitabschnitt.getBgCalculationResultAsiv().isBabyTarif()
			);
			kantonDataRowList.add(row);
		}
		return kantonDataRowList;
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportKanton(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nullable BigDecimal kantonSelbstbehalt,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) throws ExcelMergeException, IOException {

		validateDateParams(datumVon, datumBis);

		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_KANTON;

		try (
			Workbook workbook = createWorkbook(reportVorlage);
		) {
			Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

			List<KantonDataRow> reportData = getReportDataKanton(
				datumVon,
				datumBis,
				locale,
				mandant
			);

			final XSSFSheet xsslSheet =
				(XSSFSheet) kantonExcelConverter.mergeHeaderFieldsStichtag(
					reportData,
					sheet,
					locale,
					datumVon,
					datumBis,
					kantonSelbstbehalt,
					requireNonNull(principalBean.getMandant())
				);

			final RowFiller rowFiller = fillAndMergeRows(
				reportVorlage,
				xsslSheet,
				reportData
			);
			return excelFileSaverService.saveExcelDokument(
				reportVorlage,
				rowFiller,
				locale,
				principalBean.getMandant()
			);
		}
	}

	// MitarbeterInnen
	@Nonnull
	@Override
	public List<MitarbeiterinnenDataRow> getReportMitarbeiterinnen(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull Mandant mandant
	) {
		validateDateParams(datumVon, datumBis);

		List<Tuple> numberVerantwortlicheGesuche = getAllVerantwortlicheGesuche(
			mandant
		);
		List<Tuple> numberVerfuegteGesuche = getAllVerfuegteGesuche(
			datumVon,
			datumBis,
			mandant
		);

		return convertToMitarbeiterinnenDataRow(
			numberVerantwortlicheGesuche,
			numberVerfuegteGesuche
		);
	}

	/**
	 * Gibt eine tuple zurueck mit dem ID, dem Nachnamen und Vornamen des Benutzers und die Anzahl Gesuche
	 * bei denen er verantwortlich ist. Group by Verantwortlicher und oder by Verantwortlicher-nachname
	 */
	@Nonnull
	private List<Tuple> getAllVerantwortlicheGesuche(@Nonnull Mandant mandant) {
		Benutzer user = benutzerService.getCurrentBenutzer()
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"getAllVerantwortlicheGesuche",
					NO_USER_IS_LOGGED_IN
				)
			);

		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<Tuple> query = builder.createTupleQuery();
		query.distinct(true);

		Root<Gesuch> root = query.from(Gesuch.class);

		final Join<Gesuch, Dossier> dossierJoin = root.join(
			Gesuch_.dossier,
			JoinType.INNER
		);
		final Join<Dossier, Benutzer> verantwortlicherJoin =
			dossierJoin.join(Dossier_.verantwortlicherBG, JoinType.INNER);
		SetJoin<Benutzer, Berechtigung> verantwortlicherBerechtigungenJoin =
			verantwortlicherJoin.join(
				Benutzer_.berechtigungen,
				JoinType.INNER
			);
		SetJoin<Berechtigung, Gemeinde> gemeindeSetJoin =
			verantwortlicherBerechtigungenJoin
				.join(Berechtigung_.gemeindeList, JoinType.LEFT);

		query.multiselect(
			verantwortlicherJoin.get(AbstractEntity_.id)
				.alias(AbstractEntity_.id.getName()),
			verantwortlicherJoin.get(Benutzer_.nachname)
				.alias(Benutzer_.nachname.getName()),
			verantwortlicherJoin.get(Benutzer_.vorname)
				.alias(Benutzer_.vorname.getName()),
			builder.count(root).alias("allVerantwortlicheGesuche")
		);

		query.groupBy(
			verantwortlicherJoin.get(AbstractEntity_.id),
			verantwortlicherJoin.get(Benutzer_.nachname),
			verantwortlicherJoin.get(Benutzer_.vorname)
		);
		query.orderBy(
			builder.asc(verantwortlicherJoin.get(Benutzer_.nachname))
		);

		List<Predicate> predicates = new ArrayList<>();
		// Der Benutzer muss eine aktive Berechtigung mit Rolle ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE oder
		// SACHBEARBEITER_GEMEINDE haben
		Path<DateRange> dateRange = verantwortlicherBerechtigungenJoin.get(
			AbstractDateRangedEntity_.gueltigkeit
		);
		Predicate predicateActive = builder.between(
			builder.literal(LocalDate.now()),
			dateRange.get(DateRange_.gueltigAb),
			dateRange.get(DateRange_.gueltigBis)
		);
		predicates.add(predicateActive);

		Predicate mandantPredicate = builder.equal(
			dossierJoin.get(Dossier_.fall).get(Fall_.mandant),
			mandant
		);
		predicates.add(mandantPredicate);

		Set<UserRole> requiredRoles = Sets.newHashSet(
			UserRole.ADMIN_BG,
			UserRole.SACHBEARBEITER_BG,
			UserRole.ADMIN_GEMEINDE,
			UserRole.SACHBEARBEITER_GEMEINDE
		);

		Predicate isRolleCorrect =
			verantwortlicherBerechtigungenJoin.get(Berechtigung_.role)
				.in(requiredRoles);
		predicates.add(isRolleCorrect);

		if (principalBean.discoverMostPrivilegedRole()
			!= UserRole.SUPER_ADMIN) {
			// for others than superadmin, Superadmin cannot be listed
			predicates.add(
				builder.notEqual(
					verantwortlicherBerechtigungenJoin.get(
						Berechtigung_.role
					),
					UserRole.SUPER_ADMIN
				)
			);
		}

		// Nur Benutzer von Gemeinden, fuer die ich berechtigt bin
		setGemeindeFilterForCurrentUser(user, gemeindeSetJoin, predicates);

		query.where(
			CriteriaQueryHelper.concatenateExpressions(builder, predicates)
		);
		return persistence.getCriteriaResults(query);
	}

	/**
	 * Gibt eine tuple zurueck mit dem ID, dem Nachnamen und Vornamen des Benutzers und die Anzahl Gesuche
	 * die er im gegebenen Zeitraum verfuegt hat. Group by Verantwortlicher und oder by Verantwortlicher-nachname
	 */
	@Nonnull
	private List<Tuple> getAllVerfuegteGesuche(
		LocalDate datumVon,
		LocalDate datumBis,
		Mandant mandant
	) {
		Benutzer user = benutzerService.getCurrentBenutzer()
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"getAllVerfuegteGesuche",
					NO_USER_IS_LOGGED_IN
				)
			);

		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<Tuple> query = builder.createTupleQuery();
		query.distinct(true);

		Root<AntragStatusHistory> root = query.from(AntragStatusHistory.class);
		final Join<AntragStatusHistory, Benutzer> benutzerJoin =
			root.join(AntragStatusHistory_.benutzer, JoinType.INNER);
		SetJoin<Benutzer, Berechtigung> joinBerechtigungen = benutzerJoin.join(
			Benutzer_.berechtigungen
		);
		SetJoin<Berechtigung, Gemeinde> gemeindeSetJoin = joinBerechtigungen
			.join(Berechtigung_.gemeindeList, JoinType.LEFT);

		query.multiselect(
			benutzerJoin.get(AbstractEntity_.id)
				.alias(AbstractEntity_.id.getName()),
			benutzerJoin.get(Benutzer_.nachname)
				.alias(Benutzer_.nachname.getName()),
			benutzerJoin.get(Benutzer_.vorname)
				.alias(Benutzer_.vorname.getName()),
			builder.count(root).alias("allVerfuegteGesuche")
		);

		List<Predicate> predicates = new ArrayList<>();
		if (principalBean.discoverMostPrivilegedRole()
			!= UserRole.SUPER_ADMIN) {
			// for others than superadmin, Superadmin cannot be listed
			predicates.add(
				builder.notEqual(
					joinBerechtigungen.get(Berechtigung_.role),
					UserRole.SUPER_ADMIN
				)
			);
		}
		// mandant
		Predicate mandantPredicate = builder.equal(
			root.get(AntragStatusHistory_.gesuch)
				.get(Gesuch_.dossier)
				.get(Dossier_.fall)
				.get(Fall_.mandant),
			mandant
		);
		predicates.add(mandantPredicate);

		// Status ist verfuegt
		predicates.add(
			root.get(AntragStatusHistory_.status)
				.in(AntragStatus.getAllVerfuegtNotIgnoriertStates())
		);
		// Datum der Verfuegung muss nach (oder gleich) dem Anfang des Abfragezeitraums sein
		predicates.add(
			builder.greaterThanOrEqualTo(
				root.get(AntragStatusHistory_.timestampVon),
				datumVon.atStartOfDay()
			)
		);
		// Datum der Verfuegung muss vor (oder gleich) dem Ende des Abfragezeitraums sein
		predicates.add(
			builder.lessThanOrEqualTo(
				root.get(AntragStatusHistory_.timestampVon),
				datumBis.atTime(LocalTime.MAX)
			)
		);
		// Der Benutzer muss eine aktive Berechtigung mit Rolle ADMIN_BG oder SACHBEARBEITER_BG haben
		predicates.add(
			builder.between(
				builder.literal(LocalDate.now()),
				joinBerechtigungen.get(
					AbstractDateRangedEntity_.gueltigkeit
				).get(DateRange_.gueltigAb),
				joinBerechtigungen.get(
					AbstractDateRangedEntity_.gueltigkeit
				).get(DateRange_.gueltigBis)
			)
		);
		predicates.add(
			joinBerechtigungen.get(Berechtigung_.role)
				.in(UserRole.getJugendamtSuperadminRoles())
		);

		// Nur Benutzer von Gemeinden, fuer die ich berechtigt bin
		setGemeindeFilterForCurrentUser(user, gemeindeSetJoin, predicates);

		query.where(
			CriteriaQueryHelper.concatenateExpressions(builder, predicates)
		);

		query.groupBy(
			benutzerJoin.get(AbstractEntity_.id),
			benutzerJoin.get(Benutzer_.nachname),
			benutzerJoin.get(Benutzer_.vorname)
		);
		query.orderBy(builder.asc(benutzerJoin.get(Benutzer_.nachname)));

		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	private List<MitarbeiterinnenDataRow> convertToMitarbeiterinnenDataRow(
		List<Tuple> numberVerantwortlicheGesuche,
		List<Tuple> numberVerfuegteGesuche
	) {

		final Map<String, MitarbeiterinnenDataRow> result = new HashMap<>();

		for (Tuple tupleVerant : numberVerantwortlicheGesuche) {
			MitarbeiterinnenDataRow row = createMitarbeiterinnenDataRow(
				tupleVerant,
				new BigDecimal(
					(Long) tupleVerant.get("allVerantwortlicheGesuche")
				),
				BigDecimal.ZERO
			);
			result.put(
				(String) tupleVerant.get(AbstractEntity_.id.getName()),
				row
			);
		}

		for (Tuple tupleVerfuegte : numberVerfuegteGesuche) {
			final BigDecimal numberVerfuegte = new BigDecimal(
				(Long) tupleVerfuegte.get("allVerfuegteGesuche")
			);
			final MitarbeiterinnenDataRow existingRow = result.get(
				tupleVerfuegte.get(AbstractEntity_.id.getName())
			);
			if (existingRow != null) {
				existingRow.setVerfuegungenAusgestellt(numberVerfuegte);
			} else {
				MitarbeiterinnenDataRow row =
					createMitarbeiterinnenDataRow(
						tupleVerfuegte,
						BigDecimal.ZERO,
						numberVerfuegte
					);
				result.put(
					(String) tupleVerfuegte.get(
						AbstractEntity_.id.getName()
					),
					row
				);
			}
		}

		return new ArrayList<>(result.values());
	}

	@Nonnull
	private MitarbeiterinnenDataRow createMitarbeiterinnenDataRow(
		Tuple tuple,
		BigDecimal numberVerant,
		BigDecimal numberVerfuegte
	) {

		MitarbeiterinnenDataRow row = new MitarbeiterinnenDataRow();
		row.setName((String) tuple.get(Benutzer_.nachname.getName()));
		row.setVorname((String) tuple.get(Benutzer_.vorname.getName()));
		row.setVerantwortlicheGesuche(numberVerant);
		row.setVerfuegungenAusgestellt(numberVerfuegte);

		return row;
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportMitarbeiterinnen(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) throws ExcelMergeException, IOException {

		validateDateParams(datumVon, datumBis);

		final ReportVorlage reportVorlage =
			ReportVorlage.VORLAGE_REPORT_MITARBEITERINNEN;

		try (
			Workbook workbook = createWorkbook(reportVorlage);
		) {
			Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

			List<MitarbeiterinnenDataRow> reportData =
				getReportMitarbeiterinnen(datumVon, datumBis, mandant);
			ExcelMergerDTO excelMergerDTO =
				mitarbeiterinnenExcelConverter.toExcelMergerDTO(
					reportData,
					locale,
					datumVon,
					datumBis,
					requireNonNull(principalBean.getMandant())
				);

			mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
			mitarbeiterinnenExcelConverter.applyAutoSize(sheet);

			byte[] bytes = createWorkbook(workbook);

			return fileSaverService.save(
				bytes,
				getFileName(
					reportVorlage,
					locale,
					principalBean.getMandant()
				),
				Constants.TEMP_REPORT_FOLDERNAME,
				getContentTypeForExport()
			);
		}
	}

	@Nonnull
	private String getFileName(
		ReportVorlage reportVorlage,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) {
		return ServerMessageUtil.translateEnumValue(
			reportVorlage.getDefaultExportFilename(),
			locale,
			mandant
		) + ".xlsx";
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportZahlungAuftrag(
		@Nonnull String auftragId,
		@Nonnull Locale locale
	) throws ExcelMergeException, IOException {

		Zahlungsauftrag zahlungsauftrag = zahlungService.findZahlungsauftrag(
			auftragId
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"generateExcelReportZahlungAuftrag",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					auftragId
				)
			);

		// Je nach Rolle duerfen im Excel nicht alle Institutionen aufgefuehrt werden
		final UserRole userRole = principalBean.discoverMostPrivilegedRole();
		Collection<Institution> allowedInst = institutionService
			.getInstitutionenReadableForCurrentBenutzer(false);

		final ZahlungslaufHelper zahlungslaufHelper =
			ZahlungslaufHelperFactory.getZahlungslaufHelper(
				zahlungsauftrag.getZahlungslaufTyp()
			);
		List<ZahlungDataRow> zahlungDataRows = new ArrayList<>();
		for (Zahlung zahlung : zahlungsauftrag.getZahlungen()) {
			if (!EnumUtil.isOneOf(
				userRole,
				UserRole.getInstitutionTraegerschaftRoles()
			)
				||
				allowedInst
					.stream()
					.anyMatch(
						institution -> institution.getId()
							.equals(zahlung.getEmpfaengerId())
					)) {
				Adresse adresseKontoinhaber = zahlungslaufHelper
					.getAuszahlungsadresseOrDefaultadresse(zahlung);
				ZahlungDataRow row = new ZahlungDataRow(
					zahlung,
					adresseKontoinhaber
				);
				zahlungDataRows.add(row);
			}
		}

		return getUploadFileInfoZahlung(
			zahlungDataRows,
			zahlungsauftrag.getFilename(),
			zahlungsauftrag.getBeschrieb(),
			zahlungsauftrag.getDatumGeneriert(),
			zahlungsauftrag.getDatumFaellig(),
			zahlungsauftrag.getGemeinde(),
			zahlungsauftrag.getZahlungslaufTyp(),
			locale
		);
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportZahlung(
		@Nonnull String zahlungId,
		@Nonnull Locale locale
	) throws ExcelMergeException, IOException {

		List<ZahlungDataRow> reportData = new ArrayList<>();

		Zahlung zahlung = zahlungService.findZahlung(zahlungId)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"generateExcelReportZahlung",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					zahlungId
				)
			);

		final ZahlungslaufHelper zahlungslaufHelper =
			ZahlungslaufHelperFactory.getZahlungslaufHelper(
				zahlung.getZahlungsauftrag().getZahlungslaufTyp()
			);
		Adresse adresseKontoinhaber = zahlungslaufHelper
			.getAuszahlungsadresseOrDefaultadresse(zahlung);
		ZahlungDataRow dataRow = new ZahlungDataRow(
			zahlung,
			adresseKontoinhaber
		);

		reportData.add(dataRow);

		Zahlungsauftrag zahlungsauftrag = zahlung.getZahlungsauftrag();

		String fileName = zahlungsauftrag.getFilename()
			+ '_'
			+ zahlung.getEmpfaengerName();

		return getUploadFileInfoZahlung(
			reportData,
			fileName,
			zahlungsauftrag.getBeschrieb(),
			zahlungsauftrag.getDatumGeneriert(),
			zahlungsauftrag.getDatumFaellig(),
			zahlungsauftrag.getGemeinde(),
			zahlungsauftrag.getZahlungslaufTyp(),
			locale
		);
	}

	@Nonnull
	private UploadFileInfo getUploadFileInfoZahlung(
		@Nonnull List<ZahlungDataRow> reportData,
		@Nonnull String excelFileName,
		@Nonnull String bezeichnung,
		@Nonnull LocalDateTime datumGeneriert,
		@Nonnull LocalDate datumFaellig,
		@Nonnull Gemeinde gemeinde,
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull Locale locale
	) throws ExcelMergeException, IOException {

		final ReportVorlage reportVorlage =
			ReportVorlage.VORLAGE_REPORT_ZAHLUNG_AUFTRAG;

		try (
			Workbook workbook = createWorkbook(reportVorlage);
		) {
			final UserRole userRole = principalBean
				.discoverMostPrivilegedRole();
			Collection<Institution> allowedInst = institutionService
				.getInstitutionenReadableForCurrentBenutzer(false);
			List<ZahlungDataRow> zahlungenBerechtigt = reportData.stream()
				.filter(zahlungDataRow -> {
					// Filtere nur die erlaubten Instituionsdaten
					// User mit der Rolle Institution oder Traegerschaft dürfen nur "Ihre" Institutionsdaten sehen.
					return !EnumUtil.isOneOf(
						userRole,
						UserRole.getInstitutionTraegerschaftRoles()
					)
						||
						allowedInst.stream()
							.anyMatch(
								institution -> institution
									.getId()
									.equals(
										zahlungDataRow
											.getZahlung()
											.getEmpfaengerId()
									)
							);
				})
				.collect(Collectors.toList());

			// Blatt Details
			Sheet sheetDetails = workbook.getSheet(
				reportVorlage.getDataSheetName()
			);
			ExcelMergerDTO excelMergerDTO = zahlungAuftragDetailsExcelConverter
				.toExcelMergerDTO(
					zahlungenBerechtigt,
					locale,
					ServerMessageUtil.getMessage(
						"Reports_detailpositionenTitle",
						locale,
						requireNonNull(gemeinde.getMandant()),
						bezeichnung
					),
					datumGeneriert,
					datumFaellig,
					gemeinde
				);
			mergeData(
				sheetDetails,
				excelMergerDTO,
				reportVorlage.getMergeFields()
			);
			zahlungAuftragDetailsExcelConverter.applyAutoSize(sheetDetails);

			if (isZahlungslaufTypGemeindeAntragsteller(zahlungslaufTyp)) {
				sheetDetails.setColumnHidden(11, true);
			}

			// Blatt Totals
			Sheet sheetTotals = workbook.getSheet("Totals");
			ExcelMergerDTO excelMergerTotalsDTO =
				zahlungAuftragTotalsExcelConverter.toExcelMergerDTO(
					zahlungenBerechtigt,
					locale,
					ServerMessageUtil.getMessage(
						"Reports_totalZahlungenTitle",
						locale,
						requireNonNull(gemeinde.getMandant()),
						bezeichnung
					),
					datumGeneriert,
					datumFaellig,
					gemeinde,
					zahlungslaufTyp
				);
			mergeData(
				sheetTotals,
				excelMergerTotalsDTO,
				reportVorlage.getMergeFields()
			);
			zahlungAuftragTotalsExcelConverter.applyAutoSize(sheetTotals);
			zahlungAuftragTotalsExcelConverter.hideColumnsIfNecessary(
				sheetTotals,
				zahlungslaufTyp
			);

			byte[] bytes = createWorkbook(workbook);

			return fileSaverService.save(
				bytes,
				excelFileName + ".xlsx",
				Constants.TEMP_REPORT_FOLDERNAME,
				getContentTypeForExport()
			);
		}
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportZahlungPeriode(
		@Nonnull String gesuchsperiodeId,
		@Nonnull Locale locale
	) throws ExcelMergeException, IOException {

		Gesuchsperiode gesuchsperiode = gesuchsperiodeService
			.findGesuchsperiode(gesuchsperiodeId)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"generateExcelReportZahlungPeriode",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					gesuchsperiodeId
				)
			);

		final Collection<Zahlungsauftrag> zahlungsauftraegeInPeriode =
			zahlungService.getZahlungsauftraegeInPeriode(
				gesuchsperiode.getGueltigkeit().getGueltigAb(),
				gesuchsperiode.getGueltigkeit().getGueltigBis()
			);

		final ReportVorlage reportVorlage =
			ReportVorlage.VORLAGE_REPORT_ZAHLUNG_AUFTRAG_PERIODE;

		try (
			Workbook workbook = createWorkbook(reportVorlage);
		) {
			Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

			final List<Zahlung> allZahlungen = zahlungsauftraegeInPeriode
				.stream()
				.flatMap(
					zahlungsauftrag -> zahlungsauftrag.getZahlungen()
						.stream()
				)
				.collect(Collectors.toList());

			ExcelMergerDTO excelMergerDTO = zahlungAuftragPeriodeExcelConverter
				.toExcelMergerDTO(
					allZahlungen,
					gesuchsperiode.getGesuchsperiodeString(),
					locale,
					requireNonNull(gesuchsperiode.getMandant())
				);

			mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
			zahlungAuftragPeriodeExcelConverter.applyAutoSize(sheet);

			byte[] bytes = createWorkbook(workbook);

			return fileSaverService.save(
				bytes,
				getFileName(
					reportVorlage,
					locale,
					gesuchsperiode.getMandant()
				),
				Constants.TEMP_REPORT_FOLDERNAME,
				getContentTypeForExport()
			);
		}
	}

	@Override
	public boolean isSozialhilfeBezueger(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull FamiliensituationContainer familiensituationContainer,
		@Nonnull Familiensituation familiensituation
	) {
		if (familiensituation.getSozialhilfeBezueger() == null
			|| !familiensituation.getSozialhilfeBezueger()) {
			return false;
		}

		// falls keine sozialhilfeContainer existieren, Sozialhilfe von Familiensituation nehmen
		Set<SozialhilfeZeitraumContainer> sozialhilfeZeitraumContainers =
			familiensituationContainer.getSozialhilfeZeitraumContainers();
		if (sozialhilfeZeitraumContainers.isEmpty()) {
			return familiensituation.getSozialhilfeBezueger();
		}

		// falls sozialhilfeContainer existieren, überprüfen ob diese für den aktuellen Zeitabschnitt gelten
		return sozialhilfeZeitraumContainers.stream()
			.anyMatch(sozialhilfeZeitraumContainer -> {
				SozialhilfeZeitraum sozialhilfeZeitraumJA =
					sozialhilfeZeitraumContainer
						.getSozialhilfeZeitraumJA();
				return sozialhilfeZeitraumJA != null
					&&
					zeitabschnitt.getGueltigkeit()
						.getGueltigAb()
						.compareTo(
							sozialhilfeZeitraumJA
								.getGueltigkeit()
								.getGueltigAb()
						)
						>= 0
					&&
					zeitabschnitt.getGueltigkeit()
						.getGueltigBis()
						.compareTo(
							sozialhilfeZeitraumJA
								.getGueltigkeit()
								.getGueltigBis()
						)
						<= 0;
			});
	}

	@Nonnull
	private RowFiller fillAndMergeRows(
		ReportVorlage reportResource,
		XSSFSheet sheet,
		List<KantonDataRow> reportData
	) {

		RowFiller rowFiller = RowFiller.initRowFiller(
			sheet,
			MergeFieldProvider.toMergeFields(
				reportResource.getMergeFields()
			),
			Math.max(reportData.size(), 1)
		);

		kantonExcelConverter.mergeRows(
			rowFiller,
			reportData
		);
		kantonExcelConverter.applyAutoSize(sheet);

		return rowFiller;
	}

	private void runStatisticsBetreuung(Mandant mandant) {
		List<Betreuung> allBetreuungen = betreuungService
			.getAllBetreuungenWithMissingStatistics(mandant);
		for (Betreuung betreuung : allBetreuungen) {
			if (betreuung.hasVorgaenger()) {
				Betreuung vorgaengerBetreuung = persistence.find(
					Betreuung.class,
					betreuung.getVorgaengerId()
				);
				if (!betreuung.isSame(vorgaengerBetreuung, false, false)) {
					betreuung.setBetreuungMutiert(Boolean.TRUE);
					LOGGER.info(
						"Betreuung hat geändert: {}",
						betreuung.getId()
					);
				} else {
					betreuung.setBetreuungMutiert(Boolean.FALSE);
					LOGGER.info(
						"Betreuung hat nicht geändert: {}",
						betreuung.getId()
					);
				}
			} else {
				// Betreuung war auf dieser Mutation neu
				LOGGER.info("Betreuung ist neu: {}", betreuung.getId());
				betreuung.setBetreuungMutiert(Boolean.TRUE);
			}
		}
	}

	private void runStatisticsAbwesenheiten(Mandant mandant) {
		List<Abwesenheit> allAbwesenheiten = betreuungService
			.getAllAbwesenheitenWithMissingStatistics(mandant);
		for (Abwesenheit abwesenheit : allAbwesenheiten) {
			Betreuung betreuung = abwesenheit.getAbwesenheitContainer()
				.getBetreuung();
			if (abwesenheit.hasVorgaenger()) {
				Abwesenheit vorgaengerAbwesenheit = persistence.find(
					Abwesenheit.class,
					abwesenheit.getVorgaengerId()
				);
				if (!abwesenheit.isSame(vorgaengerAbwesenheit)) {
					betreuung.setAbwesenheitMutiert(Boolean.TRUE);
					LOGGER.info(
						"Abwesenheit hat geändert: {}",
						abwesenheit.getId()
					);
				} else {
					betreuung.setAbwesenheitMutiert(Boolean.FALSE);
					LOGGER.info(
						"Abwesenheit hat nicht geändert: {}",
						abwesenheit.getId()
					);
				}
			} else {
				// Abwesenheit war auf dieser Mutation neu
				LOGGER.info("Abwesenheit ist neu: {}", abwesenheit.getId());
				betreuung.setAbwesenheitMutiert(Boolean.TRUE);
			}
		}
	}

	private void runStatisticsKinder(Mandant mandant) {
		List<KindContainer> allKindContainer = kindService
			.getAllKinderWithMissingStatistics(mandant);
		for (KindContainer kindContainer : allKindContainer) {
			Kind kind = kindContainer.getKindJA();
			if (kind.hasVorgaenger()) {
				Kind vorgaengerKind = persistence.find(
					Kind.class,
					kind.getVorgaengerId()
				);
				if (!kind.isSame(vorgaengerKind)) {
					kindContainer.setKindMutiert(Boolean.TRUE);
					LOGGER.info("Kind hat geändert: {}", kindContainer.getId());
				} else {
					kindContainer.setKindMutiert(Boolean.FALSE);
					LOGGER.info(
						"Kind hat nicht geändert: {}",
						kindContainer.getId()
					);
				}
			} else {
				// Kind war auf dieser Mutation neu
				LOGGER.info("Kind ist neu: {}", kindContainer.getId());
				kindContainer.setKindMutiert(Boolean.TRUE);
			}
		}
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportBenutzer(
		@Nonnull Locale locale,
		@Nonnull Mandant mandant,
		boolean includeGesperrte
	) throws ExcelMergeException, IOException {
		final ReportVorlage reportVorlage =
			ReportVorlage.VORLAGE_REPORT_BENUTZER;

		try (
			Workbook workbook = createWorkbook(reportVorlage);
		) {
			Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

			List<BenutzerDataRow> reportData = getReportDataBenutzer(
				locale,
				mandant,
				includeGesperrte
			);

			ExcelMergerDTO excelMergerDTO = benutzerExcelConverter
				.toExcelMergerDTO(reportData, locale, mandant);

			mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
			benutzerExcelConverter.applyAutoSize(sheet);

			byte[] bytes = createWorkbook(workbook);

			return fileSaverService.save(
				bytes,
				getFileName(reportVorlage, locale, mandant),
				Constants.TEMP_REPORT_FOLDERNAME,
				getContentTypeForExport()
			);
		}
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public List<BenutzerDataRow> getReportDataBenutzer(
		@Nonnull Locale locale,
		@Nonnull Mandant mandant,
		boolean includeGesperrte
	) {
		List<BenutzerStatus> benutzerStatuses =
			Arrays.stream(BenutzerStatus.values())
				.filter(
					status -> !status.equals(BenutzerStatus.GESPERRT)
						|| includeGesperrte
				)
				.toList();
		BenutzerTableMandantFilterDTO benutzerTableFilterDto =
			new BenutzerTableMandantFilterDTO(mandant);
		benutzerTableFilterDto.setSearch(new BenutzerSearchDTO());
		benutzerTableFilterDto.getSearch()
			.getPredicateObject()
			.setStatus(benutzerStatuses);
		Pair<Long, List<Benutzer>> searchResultPair = benutzerService
			.searchBenutzer(
				benutzerTableFilterDto,
				true
			);
		List<Benutzer> benutzerList = searchResultPair.getRight();

		Map<String, EnumSet<BetreuungsangebotTyp>> betreuungsangebotMap =
			new HashMap<>();
		return convertToBenutzerDataRow(
			benutzerList,
			betreuungsangebotMap,
			locale
		);
	}

	@Nonnull
	private List<BenutzerDataRow> convertToBenutzerDataRow(
		@Nonnull List<Benutzer> benutzerList,
		Map<String, EnumSet<BetreuungsangebotTyp>> betreuungsangebotMap,
		@Nonnull Locale locale
	) {
		return benutzerList.stream()
			.map(
				benutzer -> benutzerToDataRow(
					benutzer,
					betreuungsangebotMap,
					locale
				)
			)
			.collect(Collectors.toList());
	}

	@Nonnull
	private BenutzerDataRow benutzerToDataRow(
		@Nonnull Benutzer benutzer,
		Map<String, EnumSet<BetreuungsangebotTyp>> betreuungsangebotMap,
		@Nonnull Locale locale
	) {
		BenutzerDataRow row = new BenutzerDataRow();
		row.setUsername(benutzer.getUsername());

		row.setNachname(benutzer.getNachname());
		row.setVorname(benutzer.getVorname());
		row.setEmail(benutzer.getEmail());
		row.setRole(
			ServerMessageUtil.translateEnumValue(
				benutzer.getRole(),
				locale,
				benutzer.getMandant()
			)
		);
		LocalDate gueltigAb = benutzer.getCurrentBerechtigung()
			.getGueltigkeit()
			.getGueltigAb();
		if (gueltigAb.isAfter(Constants.START_OF_TIME)) {
			row.setRoleGueltigAb(gueltigAb);
		}
		LocalDate gueltigBis = benutzer.getCurrentBerechtigung()
			.getGueltigkeit()
			.getGueltigBis();
		if (gueltigBis.isBefore(Constants.END_OF_TIME)) {
			row.setRoleGueltigBis(gueltigBis);
		}
		String institution = benutzer.getInstitution() != null ?
			benutzer.getInstitution().getName() :
			null;
		String traegerschaft = getTraegerschaftForBenutzer(benutzer);
		row.setGemeinden(
			benutzer.getCurrentBerechtigung()
				.extractGemeindenForBerechtigungAsString()
		);
		row.setAngebotGemeinden(getAngebotGemeindenString(benutzer));
		row.setInstitution(institution);
		row.setTraegerschaft(traegerschaft);
		row.setStatus(benutzer.getStatus());
		setBetreuungsangebote(row, benutzer, betreuungsangebotMap);

		return row;
	}

	/**
	 * The Traegerschaft comes directly from the user when it has one. If it has an Institution the tragerschaft will
	 * be the one
	 * the institution belongs to.
	 * Nuull is returned when the user has no traegerschaft and no institution or this one has no traegerschaft.
	 * The role isn't taken into account!
	 */
	@Nullable
	private String getTraegerschaftForBenutzer(@Nonnull Benutzer benutzer) {
		if (benutzer.getTraegerschaft() != null) {
			return benutzer.getTraegerschaft().getName();
		}
		if (benutzer.getInstitution() != null
			&& benutzer.getInstitution().getTraegerschaft() != null) {
			return benutzer.getInstitution().getTraegerschaft().getName();
		}
		return null;
	}

	public void setBetreuungsangebote(
		@Nonnull BenutzerDataRow row,
		@Nonnull Benutzer benutzer,
		Map<String, EnumSet<BetreuungsangebotTyp>> betreuungsangebotMap
	) {
		// we go through all Traegerschaft/Inst/InstStammdaten and check which kind of Angebot they offer.
		// We don't get this information directly from the sql-query because it would be quite difficult and the
		// result very long
		// since it is a report and the users allow them to take long to execute, this shouldn't be any problem.

		// to improve performance we have a Map where we save already calculated results. We use the ID so we can have
		// a Map for both traegerschaft and Institutionen
		// traegerschaft has a higher priority than institution
		if (benutzer.getTraegerschaft() != null) {
			if (!betreuungsangebotMap.containsKey(
				benutzer.getTraegerschaft().getId()
			)) {
				EnumSet<BetreuungsangebotTyp> allAngeboteTraegerschaft =
					traegerschaftService
						.getAllAngeboteFromTraegerschaft(
							benutzer.getTraegerschaft().getId()
						);
				betreuungsangebotMap.put(
					benutzer.getTraegerschaft().getId(),
					allAngeboteTraegerschaft
				);
			}
			setBetreuungsangebotValues(
				row,
				betreuungsangebotMap.get(
					benutzer.getTraegerschaft().getId()
				)
			);

		} else if (benutzer.getInstitution() != null) {
			if (!betreuungsangebotMap.containsKey(
				benutzer.getInstitution().getId()
			)) {
				BetreuungsangebotTyp angebotInstitution = institutionService
					.getAngebotFromInstitution(
						benutzer.getInstitution().getId()
					);
				betreuungsangebotMap.put(
					benutzer.getInstitution().getId(),
					EnumSet.of(angebotInstitution)
				);
			}
			setBetreuungsangebotValues(
				row,
				betreuungsangebotMap.get(benutzer.getInstitution().getId())
			);
		}
	}

	public void setBetreuungsangebotValues(
		@Nonnull BenutzerDataRow row,
		@Nonnull EnumSet<BetreuungsangebotTyp> angebote
	) {

		row.setKita(angebote.stream().anyMatch(BetreuungsangebotTyp::isKita));
		row.setTagesfamilien(
			angebote.stream()
				.anyMatch(BetreuungsangebotTyp::isTagesfamilien)
		);
		row.setTagesschule(
			angebote.stream().anyMatch(BetreuungsangebotTyp::isTagesschule)
		);
		row.setFerieninsel(
			angebote.stream().anyMatch(BetreuungsangebotTyp::isFerieninsel)
		);
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportInstitutionen(
		@Nonnull Locale locale
	) throws ExcelMergeException, IOException {
		final ReportVorlage reportVorlage =
			ReportVorlage.VORLAGE_REPORT_INSTITUTIONEN;

		try (
			Workbook workbook = createWorkbook(reportVorlage);
		) {
			Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

			List<InstitutionenDataRow> reportData = getReportDataInstitutionen(
				locale
			);

			ExcelMergerDTO excelMergerDTO = institutionenExcelConverter
				.toExcelMergerDTO(
					reportData,
					locale,
					requireNonNull(principalBean.getMandant())
				);

			mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());

			institutionenExcelConverter.applyAutoSize(sheet);
			Boolean zusatzinformationenInstitution =
				applicationPropertyService.findApplicationPropertyAsBoolean(
					ApplicationPropertyKey.ZUSATZINFORMATIONEN_INSTITUTION,
					principalBean.getMandant()
				);
			institutionenExcelConverter.hideColumnsIfNecessary(
				sheet,
				zusatzinformationenInstitution
			);

			byte[] bytes = createWorkbook(workbook);

			return fileSaverService.save(
				bytes,
				getFileName(
					reportVorlage,
					locale,
					principalBean.getMandant()
				),
				Constants.TEMP_REPORT_FOLDERNAME,
				getContentTypeForExport()
			);
		}
	}

	private List<InstitutionenDataRow> getReportDataInstitutionen(
		@Nonnull Locale locale
	) {
		Benutzer currentBenutzer = benutzerService.getCurrentBenutzer()
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"getReportDataInstitutionen",
					NO_USER_IS_LOGGED_IN
				)
			);

		Collection<InstitutionStammdaten> stammdaten =
			institutionStammdatenService.getAllInstitutionStammdaten()
				.stream()
				.filter(
					institution -> isCurrentBenutzerZustaendigForInstitution(
						currentBenutzer,
						institution
					)
				)
				.collect(Collectors.toList());
		return convertToInstitutionenDataRow(stammdaten, locale);
	}

	private boolean isCurrentBenutzerZustaendigForInstitution(
		@Nonnull Benutzer currentBenutzer,
		@Nonnull InstitutionStammdaten institution
	) {
		if (currentBenutzer.getRole().isRoleTsOnly()) {
			return institution.getBetreuungsangebotTyp().isSchulamt();
		}
		if (currentBenutzer.getRole().isRoleBgOnly()) {
			return institution.getBetreuungsangebotTyp().isJugendamt();
		}
		return true;
	}

	@Nonnull
	private List<InstitutionenDataRow> convertToInstitutionenDataRow(
		@Nonnull Collection<InstitutionStammdaten> stammdaten,
		@Nonnull Locale locale
	) {
		return stammdaten.stream()
			.map(institution -> institutionToDataRow(institution, locale))
			.collect(Collectors.toList());
	}

	@Nonnull
	private InstitutionenDataRow institutionToDataRow(
		@Nonnull InstitutionStammdaten institutionStammdaten,
		@Nonnull Locale locale
	) {
		Institution institution = institutionStammdaten.getInstitution();
		Adresse adresse = institutionStammdaten.getAdresse();
		List<LocalDateTime> zuletztGeandertList = new ArrayList<>();
		LocalDateTime zuletztGeandert;
		InstitutionenDataRow row = new InstitutionenDataRow();

		String angebotTyp =
			ServerMessageUtil.translateEnumValue(
				institutionStammdaten.getBetreuungsangebotTyp(),
				locale,
				requireNonNull(institution.getMandant())
			);
		row.setTyp(angebotTyp);
		if (institution.getTraegerschaft() != null) {
			row.setTraegerschaft(institution.getTraegerschaft().getName());
			row.setTraegerschaftEmail(
				institution.getTraegerschaft().getEmail()
			);
		}
		row.setEmailBenachrichtigungenKiBon(
			institutionStammdaten.getSendMailWennOffenePendenzen()
		);
		if (institutionStammdaten.getErinnerungMail() != null) {
			row.setEmailBenachrichtigungKiBonMail(
				institutionStammdaten.getErinnerungMail()
			);
		}
		row.setName(institution.getName());
		row.setStatus(
			ServerMessageUtil.getMessage(
				"InstitutionStatus_" + institution.getStatus(),
				locale,
				institution.getMandant()
			)
		);
		if (adresse.getOrganisation() != null) {
			row.setAnschrift(adresse.getOrganisation());
		}
		if (institutionStammdaten.getTelefon() != null) {
			row.setTelefon(institutionStammdaten.getTelefon());
		}
		if (institutionStammdaten.getWebseite() != null) {
			row.setUrl(institutionStammdaten.getWebseite());
		}
		row.setStrasse(adresse.getStrasseAndHausnummer());
		row.setPlz(adresse.getPlz());
		row.setOrt(adresse.getOrt());
		row.setEmail(institutionStammdaten.getMail());
		if (!institutionStammdaten.getGueltigkeit()
			.getGueltigAb()
			.isEqual(Constants.START_OF_TIME)) {
			row.setGueltigAb(
				institutionStammdaten.getGueltigkeit().getGueltigAb()
			);
		}
		if (!institutionStammdaten.getGueltigkeit()
			.getGueltigBis()
			.isEqual(Constants.END_OF_TIME)) {
			row.setGueltigBis(
				institutionStammdaten.getGueltigkeit().getGueltigBis()
			);
		}
		row.setGrundSchliessung(institutionStammdaten.getGrundSchliessung());

		InstitutionStammdatenBetreuungsgutscheine institutionStammdatenBG =
			institutionStammdaten
				.getInstitutionStammdatenBetreuungsgutscheine();
		if (institutionStammdatenBG != null) {
			row.setFamilienportalEmail(
				institutionStammdatenBG.getAlternativeEmailFamilienportal()
			);
			if (institutionStammdatenBG.getOffenVon() != null
				&& institutionStammdatenBG.getOffenBis() != null) {
				row.setOeffnungszeitAb(
					institutionStammdatenBG.getOffenVon().toString()
				);
				row.setOeffnungszeitBis(
					institutionStammdatenBG.getOffenBis().toString()
				);
			}
			row.setOeffnungVor630(institutionStammdatenBG.isFruehEroeffnung());
			row.setOeffnungNach1830(
				institutionStammdatenBG.isSpaetEroeffnung()
			);
			row.setOeffnungAnWochenenden(
				institutionStammdatenBG.isWochenendeEroeffnung()
			);
			row.setUebernachtungMoeglich(
				institutionStammdatenBG.isUebernachtungMoeglich()
			);
			row.setOeffnungstage(
				institutionStammdatenBG.getOeffnungsTage()
					.stream()
					.sorted()
					.map(
						tag -> tag.getDisplayName(
							TextStyle.FULL,
							locale
						)
					)
					.collect(Collectors.joining(", "))
			);
			row.setOeffnungsAbweichungen(
				institutionStammdatenBG.getOeffnungsAbweichungen()
			);
			if (institutionStammdatenBG.getOeffnungstageProJahr() != null) {
				row.setOeffnungstageProJahr(
					institutionStammdatenBG.getOeffnungstageProJahr()
				);
			}
		}
		row.setBaby(
			institutionStammdatenBG != null
				&& institutionStammdatenBG.getAlterskategorieBaby()
		);
		row.setVorschulkind(
			institutionStammdatenBG != null
				&& institutionStammdatenBG.getAlterskategorieVorschule()
		);
		row.setKindergarten(
			institutionStammdatenBG != null
				&& institutionStammdatenBG
					.getAlterskategorieKindergarten()
		);
		row.setSchulkind(
			institutionStammdatenBG != null
				&& institutionStammdatenBG.getAlterskategorieSchule()
		);
		if (!institutionStammdaten.getBetreuungsangebotTyp()
			.isTagesfamilien()) {
			if (institutionStammdatenBG != null
				&& institutionStammdatenBG.getAnzahlPlaetze() != null) {
				row.setKapazitaet(institutionStammdatenBG.getAnzahlPlaetze());
			}
			if (institutionStammdatenBG != null
				&& institutionStammdatenBG.getAnzahlPlaetzeFirmen() != null
				&&
				institutionStammdatenBG.getAnzahlPlaetzeFirmen()
					.compareTo(BigDecimal.ZERO)
					!= 0) {
				row.setReserviertFuerFirmen(
					institutionStammdatenBG.getAnzahlPlaetzeFirmen()
				);
			}
		}
		Gemeinde gemeinde = null;
		if (institutionStammdaten.getBetreuungsangebotTyp().isTagesschule()
			&& institutionStammdaten.getInstitutionStammdatenTagesschule()
				!= null) {
			gemeinde = institutionStammdaten
				.getInstitutionStammdatenTagesschule()
				.getGemeinde();
		}

		if (institutionStammdaten.getBetreuungsangebotTyp().isTagesfamilien()
			&& institutionStammdaten.getInstitutionStammdatenFerieninsel()
				!= null) {
			gemeinde = institutionStammdaten
				.getInstitutionStammdatenFerieninsel()
				.getGemeinde();
		}
		if (gemeinde != null) {
			row.setTraegergemeinde(gemeinde.getName());
			row.setBfsTraegergemeinde(gemeinde.getBfsNummer());
		}

		if (institutionStammdaten.getBetreuungsangebotTyp()
			.isAngebotJugendamtKleinkind()
			&& institutionStammdaten
				.getInstitutionStammdatenBetreuungsgutscheine()
				!= null) {
			row.setStandortgemeinde(
				institutionStammdaten.getAdresse().getGemeinde()
			);
			row.setBfsStandortgemeinde(
				institutionStammdaten.getAdresse().getBfsNummer()
			);
		}
		zuletztGeandertList.add(institutionStammdaten.getTimestampMutiert());
		zuletztGeandertList.add(institution.getTimestampMutiert());
		zuletztGeandertList.add(adresse.getTimestampMutiert());

		zuletztGeandert = zuletztGeandertList.stream()
			.max(LocalDateTime::compareTo)
			.get();
		row.setZuletztGeaendert(zuletztGeandert);
		return row;
	}

	@Override
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Nonnull
	public UploadFileInfo generateExcelReportFerienbetreuung(
		@Nonnull Locale locale
	) throws ExcelMergeException, IOException {
		final ReportVorlage reportVorlage =
			ReportVorlage.VORLAGE_REPORT_FERIENBETREUUNG;

		try (
			Workbook workbook = createWorkbook(reportVorlage);
		) {
			Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

			List<FerienbetreuungDataRow> reportData =
				getReportDataFerienbetreuung();

			ExcelMergerDTO excelMergerDTO = ferienbetreuungExcelConverter
				.toExcelMergerDTO(
					reportData,
					requireNonNull(principalBean.getMandant())
				);

			mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
			ferienbetreuungExcelConverter.applyAutoSize(sheet);

			byte[] bytes = createWorkbook(workbook);

			return fileSaverService.save(
				bytes,
				getFileName(
					reportVorlage,
					locale,
					principalBean.getMandant()
				),
				Constants.TEMP_REPORT_FOLDERNAME,
				getContentTypeForExport()
			);
		}
	}

	private List<FerienbetreuungDataRow> getReportDataFerienbetreuung() {
		return ferienbetreuungService.getAllFerienbetreuungAntraege()
			.stream()
			.filter(
				FerienbetreuungAngabenContainer::isAtLeastInPruefungKantonOrZurueckAnGemeinde
			)
			.map(this::convertFerienbetreungToDataRow)
			.collect(Collectors.toList());
	}

	private FerienbetreuungDataRow convertFerienbetreungToDataRow(
		FerienbetreuungAngabenContainer ferienbetreuungAngabenContainer
	) {
		FerienbetreuungDataRow ferienbetreuungDataRow =
			new FerienbetreuungDataRow();
		FerienbetreuungAngaben ferienbetreuungAngaben =
			getFerienbetreuungAngabenBasedOnStatus(
				ferienbetreuungAngabenContainer
			);

		ferienbetreuungDataRow.setGemeinde(
			ferienbetreuungAngabenContainer.getGemeinde().getName()
		);
		ferienbetreuungDataRow.setBfsNummerGemeinde(
			ferienbetreuungAngabenContainer.getGemeinde().getBfsNummer()
		);
		ferienbetreuungDataRow.setPeriode(
			ferienbetreuungAngabenContainer.getGesuchsperiode()
				.getGesuchsperiodeString()
		);
		ferienbetreuungDataRow.setStatus(
			ferienbetreuungAngabenContainer.getStatus()
		);
		ferienbetreuungDataRow.setTimestampMutiert(
			ferienbetreuungAngabenContainer.getTimestampMutiert()
		);
		ferienbetreuungDataRow.setFirstEinreicheDatum(
			ferienbetreuungAngabenContainer.getEinreichedatum()
		);
		ferienbetreuungDataRow.setKommentar(
			ferienbetreuungAngabenContainer.getInternerKommentar()
		);

		setStammdatenValues(
			ferienbetreuungDataRow,
			ferienbetreuungAngaben.getFerienbetreuungAngabenStammdaten()
		);
		setAngebotsValues(
			ferienbetreuungDataRow,
			ferienbetreuungAngaben.getFerienbetreuungAngabenAngebot()
		);
		setNutzungsValues(
			ferienbetreuungDataRow,
			ferienbetreuungAngaben.getFerienbetreuungAngabenNutzung()
		);
		setKostenEinnahmenValues(
			ferienbetreuungDataRow,
			ferienbetreuungAngaben
				.getFerienbetreuungAngabenKostenEinnahmen()
		);
		setBerechnungenValues(
			ferienbetreuungDataRow,
			ferienbetreuungAngaben.getFerienbetreuungBerechnungen()
		);

		return ferienbetreuungDataRow;
	}

	private FerienbetreuungAngaben getFerienbetreuungAngabenBasedOnStatus(
		FerienbetreuungAngabenContainer ferienbetreuungAngabenContainer
	) {
		// falls Antrag zurück an Gemeinde gegeben wurde, sollen der Antrag im Report sichtbar sein. Allerdings nur
		// die Deklaration, um zu verhindern, dass nicht freigegebene Änderungen schon für den Kanton sichtbar sind.
		if (ferienbetreuungAngabenContainer.getStatus()
			== FerienbetreuungAngabenStatus.ZURUECK_AN_GEMEINDE) {
			return ferienbetreuungAngabenContainer.getAngabenDeklaration();
		}
		// sonst zeigen wir immer die Korrektur, falls vorhanden
		return ferienbetreuungAngabenContainer.getAngabenKorrektur() != null ?
			ferienbetreuungAngabenContainer.getAngabenKorrektur() :
			ferienbetreuungAngabenContainer.getAngabenDeklaration();
	}

	private void setStammdatenValues(
		FerienbetreuungDataRow row,
		FerienbetreuungAngabenStammdaten stammdaten
	) {

		row.setTraegerschaft(stammdaten.getTraegerschaft());

		String weitereGemeinden = String.join(
			", ",
			stammdaten.getAmAngebotBeteiligteGemeinden()
		);
		row.setWeitereGemeinden(weitereGemeinden);

		row.setSeitWannFerienbetreuungen(
			stammdaten.getSeitWannFerienbetreuungen()
		);

		if (stammdaten.getStammdatenAdresse() != null) {
			row.setGemeindeAnschrift(
				stammdaten.getStammdatenAdresse().getOrganisation()
			);
			row.setGemeindeStrasse(
				stammdaten.getStammdatenAdresse().getStrasse()
			);
			row.setGeimeindeHausnummer(
				stammdaten.getStammdatenAdresse().getHausnummer()
			);
			row.setGemeindeZusatz(
				stammdaten.getStammdatenAdresse().getZusatzzeile()
			);
			row.setGemeindePlz(stammdaten.getStammdatenAdresse().getPlz());
			row.setGemeindeOrt(stammdaten.getStammdatenAdresse().getOrt());
		}

		row.setStammdatenKontaktpersonVorname(
			stammdaten.getStammdatenKontaktpersonVorname()
		);
		row.setStammdatenKontaktpersonName(
			stammdaten.getStammdatenKontaktpersonNachname()
		);
		row.setStammdatenKontaktpersonFunktion(
			stammdaten.getStammdatenKontaktpersonFunktion()
		);
		row.setStammdatenKontaktpersonTelefon(
			stammdaten.getStammdatenKontaktpersonTelefon()
		);
		row.setStammdatenKontaktpersonEmail(
			stammdaten.getStammdatenKontaktpersonEmail()
		);

		final Auszahlungsdaten auszahlungsdaten = stammdaten
			.getAuszahlungsdaten();
		if (auszahlungsdaten != null) {
			row.setKontoinhaber(auszahlungsdaten.getKontoinhaber());
			// "IBAN" ist entweder die tatsaechliche IBAN oder die InfomaKontonummer
			row.setIban(auszahlungsdaten.getIbanOrInfomaKreditorennummer());
			row.setKontoVermerk(stammdaten.getVermerkAuszahlung());

			if (auszahlungsdaten.getAdresseKontoinhaber() != null) {
				row.setKontoStrasse(
					auszahlungsdaten.getAdresseKontoinhaber().getStrasse()
				);
				row.setKontoHausnummer(
					auszahlungsdaten.getAdresseKontoinhaber()
						.getHausnummer()
				);
				row.setKontoZusatz(
					auszahlungsdaten.getAdresseKontoinhaber()
						.getZusatzzeile()
				);
				row.setKontoPlz(
					auszahlungsdaten.getAdresseKontoinhaber().getPlz()
				);
				row.setKontoOrt(
					auszahlungsdaten.getAdresseKontoinhaber().getOrt()
				);
			}
		}
	}

	private void setAngebotsValues(
		FerienbetreuungDataRow row,
		FerienbetreuungAngabenAngebot angebot
	) {
		row.setAngebot(angebot.getAngebot());
		row.setAngebotKontaktpersonVorname(
			angebot.getAngebotKontaktpersonVorname()
		);
		row.setAngebotKontaktpersonNachname(
			angebot.getAngebotKontaktpersonNachname()
		);

		if (angebot.getAngebotAdresse() != null) {
			row.setAngebotKontaktpersonStrasse(
				angebot.getAngebotAdresse().getStrasse()
			);
			row.setAngebotKontaktpersonHausnummer(
				angebot.getAngebotAdresse().getHausnummer()
			);
			row.setAngebotKontaktpersonZusatz(
				angebot.getAngebotAdresse().getZusatzzeile()
			);
			row.setAngebotKontaktpersonPlz(
				angebot.getAngebotAdresse().getPlz()
			);
			row.setAngebotKontaktpersonOrt(
				angebot.getAngebotAdresse().getOrt()
			);
		}

		row.setAnzahlFerienwochenHerbstferien(
			angebot.getAnzahlFerienwochenHerbstferien()
		);
		row.setAnzahlFerienwochenWinterferien(
			angebot.getAnzahlFerienwochenWinterferien()
		);
		row.setAnzahlFerienwochenSportferien(
			angebot.getAnzahlFerienwochenSportferien()
		);
		row.setAnzahlFerienwochenFruehlingsferien(
			angebot.getAnzahlFerienwochenFruehlingsferien()
		);
		row.setAnzahlFerienwochenSommerferien(
			angebot.getAnzahlFerienwochenSommerferien()
		);
		row.setBemerkungAnzahlFerienwochen(
			angebot.getBemerkungenAnzahlFerienwochen()
		);
		row.setAnzahlStundenProBetreuungstag(
			angebot.getAnzahlStundenProBetreuungstag()
		);
		row.setBetreuungErfolgtTagsueber(
			angebot.getBetreuungErfolgtTagsueber()
		);
		row.setBemerkungOeffnungszeiten(
			angebot.getBemerkungenOeffnungszeiten()
		);
		row.setAnzahlTageGesamt(angebot.getAnzahlTage());
		row.setFinanziellBeteiligteGemeinden(
			String.join(", ", angebot.getFinanziellBeteiligteGemeinden())
		);
		row.setGemeindeFuehrtAngebotSelber(
			angebot.getGemeindeFuehrtAngebotSelber()
		);
		row.setGemeindeFuehrtAngebotInKooperation(
			angebot.getGemeindeFuehrtAngebotInKooperation()
		);
		row.setGemeindeBeauftragtExterneAnbieter(
			angebot.getGemeindeBeauftragtExterneAnbieter()
		);
		row.setAngebotVereineUndPrivateIntegriert(
			angebot.getAngebotVereineUndPrivateIntegriert()
		);
		row.setBemerkungenKooperation(angebot.getBemerkungenKooperation());
		row.setLeitungDurchPersonMitAusbildung(
			angebot.getLeitungDurchPersonMitAusbildung()
		);
		row.setBetreuungDurchPersonenMitErfahrung(
			angebot.getBetreuungDurchPersonenMitErfahrung()
		);
		row.setAnzahlKinderAngemessen(angebot.getAnzahlKinderAngemessen());
		row.setBetreuungsschluessel(angebot.getBetreuungsschluessel());
		row.setBemerkungenPersonal(angebot.getBemerkungenPersonal());
		row.setFixerTarifKinderDerGemeinde(
			angebot.getFixerTarifKinderDerGemeinde()
		);
		row.setBemerkungenTarifsystem(angebot.getBemerkungenTarifsystem());
		row.setEinkommensabhaengigerTarifKinderDerGemeinde(
			angebot.getEinkommensabhaengigerTarifKinderDerGemeinde()
		);
		row.setTagesschuleTarifGiltFuerFerienbetreuung(
			angebot.getTagesschuleTarifGiltFuerFerienbetreuung()
		);
		row.setFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet(
			angebot.getFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet()
		);
		row.setKinderAusAnderenGemeindenZahlenAnderenTarif(
			angebot.getKinderAusAnderenGemeindenZahlenAnderenTarif()
		);
		row.setBemerkungenTarifsystem(row.getBemerkungenTarifsystem());
	}

	private void setNutzungsValues(
		FerienbetreuungDataRow row,
		FerienbetreuungAngabenNutzung nutzung
	) {
		row.setAnzahlBetreuungstageKinderBern(
			nutzung.getAnzahlBetreuungstageKinderBern()
		);
		row.setBetreuungstageKinderDieserGemeinde(
			nutzung.getBetreuungstageKinderDieserGemeinde()
		);
		row.setBetreuungstageKinderDieserGemeindeSonderschueler(
			nutzung.getBetreuungstageKinderDieserGemeindeSonderschueler()
		);
		row.setDavonBetreuungstageKinderAndererGemeinden(
			nutzung.getDavonBetreuungstageKinderAndererGemeinden()
		);
		row.setDavonBetreuungstageKinderAndererGemeindenSonderschueler(
			nutzung.getDavonBetreuungstageKinderAndererGemeindenSonderschueler()
		);
		row.setAnzahlBetreuteKinder(nutzung.getAnzahlBetreuteKinder());
		row.setAnzahlBetreuteKinderSonderschueler(
			nutzung.getAnzahlBetreuteKinderSonderschueler()
		);
		row.setAnzahlBetreuteKinder1Zyklus(
			nutzung.getAnzahlBetreuteKinder1Zyklus()
		);
		row.setAnzahlBetreuteKinder2Zyklus(
			nutzung.getAnzahlBetreuteKinder2Zyklus()
		);
		row.setAnzahlBetreuteKinder3Zyklus(
			nutzung.getAnzahlBetreuteKinder3Zyklus()
		);
	}

	private void setKostenEinnahmenValues(
		FerienbetreuungDataRow row,
		FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen
	) {
		row.setPersonalkosten(kostenEinnahmen.getPersonalkosten());
		row.setPersonalkostenLeitungAdmin(
			kostenEinnahmen.getPersonalkostenLeitungAdmin()
		);
		row.setSachkosten(kostenEinnahmen.getSachkosten());
		row.setVerpflegungskosten(kostenEinnahmen.getVerpflegungskosten());
		row.setWeitereKosten(kostenEinnahmen.getWeitereKosten());
		row.setBemerkungenKosten(kostenEinnahmen.getBemerkungenKosten());
		row.setElterngebuehren(kostenEinnahmen.getElterngebuehren());
		row.setWeitereEinnahmen(kostenEinnahmen.getWeitereEinnahmen());
		row.setSockelbeitrag(kostenEinnahmen.getSockelbeitrag());
		row.setBeitraegeNachAnmeldungen(
			kostenEinnahmen.getBeitraegeNachAnmeldungen()
		);
		row.setVorfinanzierteKantonsbeitraege(
			kostenEinnahmen.getVorfinanzierteKantonsbeitraege()
		);
		row.setEigenleistungenGemeinde(
			kostenEinnahmen.getEigenleistungenGemeinde()
		);
	}

	private void setBerechnungenValues(
		FerienbetreuungDataRow row,
		FerienbetreuungBerechnungen berechnungen
	) {

		if (berechnungen == null) {
			return;
		}

		row.setTotalKantonsbeitrag(berechnungen.getTotalKantonsbeitrag());
		row.setBeitragKinderAnbietendenGemeinde(
			berechnungen.getBeitragKinderAnbietendenGemeinde()
		);
		row.setBeteiligungAnbietendenGemeinde(
			berechnungen.getBeteiligungAnbietendenGemeinde()
		);
	}

	private int onlySchulamt() {
		String[] schulamtRoles = { SACHBEARBEITER_TS, ADMIN_TS };

		return principalBean.isCallerInAnyOfRole(schulamtRoles) ? 1 : 0;
	}

	/**
	 * Returns if Angebote of Gemeinden of a Benutzer as a comma separated list
	 * E.g.
	 * - benutzer has Berechtigung for Gemeinde London and Gemeinde Paris
	 * - Gemeinde London has BG
	 * - Gemeinde Paris has BG and TS
	 * - this function would return "BG, TS"
	 */
	private String getAngebotGemeindenString(@Nonnull Benutzer benutzer) {
		Set<Gemeinde> gemeinden = benutzer.getCurrentBerechtigung()
			.getGemeindeList();
		Set<String> angebote = new HashSet<>();
		gemeinden.forEach(g -> {
			if (g.isAngebotBG()) {
				angebote.add("BG");
			}
			if (g.isAngebotFI()) {
				angebote.add("FI");
			}
			if (g.isAngebotTS()) {
				angebote.add("TS");
			}
		});
		return angebote.stream()
			.sorted()
			.collect(Collectors.joining(", "));
	}

	private boolean isZahlungslaufTypGemeindeAntragsteller(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp
	) {
		return zahlungslaufTyp == ZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER;
	}
}
