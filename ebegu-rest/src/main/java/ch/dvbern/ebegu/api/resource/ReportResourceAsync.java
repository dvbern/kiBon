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

package ch.dvbern.ebegu.api.resource;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.dtos.JaxDownloadFile;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Workjob;
import ch.dvbern.ebegu.enums.WorkJobType;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.services.WorkjobService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer asynchrone Reports
 */
@Path("reporting/async")
@Stateless
@Api(description = "Resource für Statistiken und Reports")
@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS,
	ADMIN_TS, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT,
	REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
public class ReportResourceAsync {

	public static final String DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN =
		"Das von-Datum muss vor dem bis-Datum sein.";
	public static final String URL_PART_EXCEL = "excel/";

	@Inject
	private DownloadResource downloadResource;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private WorkjobService workjobService;

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Gesuch-Stichtag'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/gesuchStichtag")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getGesuchStichtagReportExcel(
		@QueryParam("dateTimeStichtag") @Nonnull String dateTimeStichtag,
		@QueryParam("gesuchPeriodeID") @Nullable @Valid JaxId gesuchPeriodIdParam,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo) {

		String ip = downloadResource.getIP(request);
		Objects.requireNonNull(dateTimeStichtag);
		LocalDate datumVon = DateUtil.parseStringToDateOrReturnNow(dateTimeStichtag);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		String periodeId = gesuchPeriodIdParam != null ? gesuchPeriodIdParam.getId() : null;
		workJob = workjobService.createNewReporting(
			workJob,
			LocaleThreadLocal.get().equals(Locale.FRENCH)
				? ReportVorlage.VORLAGE_REPORT_GESUCH_STICHTAG_FR
				: ReportVorlage.VORLAGE_REPORT_GESUCH_STICHTAG_DE,
			datumVon,
			null,
			periodeId,
			LocaleThreadLocal.get()
		);

		return Response.ok(workJob.getId()).build();
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Gesuch-Zeitraum'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/gesuchZeitraum")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getGesuchZeitraumReportExcel(
		@QueryParam("dateTimeFrom") @Nonnull String dateTimeFromParam,
		@QueryParam("dateTimeTo") @Nonnull String dateTimeToParam,
		@QueryParam("gesuchPeriodeID") @Nullable @Valid JaxId gesuchPeriodIdParam,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		String ip = downloadResource.getIP(request);

		Objects.requireNonNull(dateTimeFromParam);
		Objects.requireNonNull(dateTimeToParam);
		LocalDate dateFrom = DateUtil.parseStringToDateOrReturnNow(dateTimeFromParam);
		LocalDate dateTo = DateUtil.parseStringToDateOrReturnNow(dateTimeToParam);

		if (!dateTo.isAfter(dateFrom)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"getGesuchZeitraumReportExcel",
				"Fehler beim erstellen Report Gesuch Zeitraum",
				DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN);
		}

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		String periodeId = gesuchPeriodIdParam != null ? gesuchPeriodIdParam.getId() : null;
		workJob = workjobService.createNewReporting(
			workJob,
			LocaleThreadLocal.get().equals(Locale.FRENCH)
				? ReportVorlage.VORLAGE_REPORT_GESUCH_ZEITRAUM_FR
				: ReportVorlage.VORLAGE_REPORT_GESUCH_ZEITRAUM_DE,
			dateFrom,
			dateTo,
			periodeId,
			LocaleThreadLocal.get()
		);

		return Response.ok(workJob.getId()).build();
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Kanton'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/kanton")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getKantonReportExcel(
		@QueryParam("auswertungVon") @Nonnull String auswertungVon,
		@QueryParam("auswertungBis") @Nonnull String auswertungBis,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		String ip = downloadResource.getIP(request);

		Objects.requireNonNull(auswertungVon);
		Objects.requireNonNull(auswertungBis);
		LocalDate dateAuswertungVon = DateUtil.parseStringToDateOrReturnNow(auswertungVon);
		LocalDate dateAuswertungBis = DateUtil.parseStringToDateOrReturnNow(auswertungBis);

		if (!dateAuswertungBis.isAfter(dateAuswertungVon)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"getKantonReportExcel",
				"Fehler beim erstellen Report Kanton",
				DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN);
		}

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_KANTON,
			dateAuswertungVon,
			dateAuswertungBis,
			null,
			LocaleThreadLocal.get()
		);

		return Response.ok(workJob.getId()).build();
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'MitarbeiterInnen'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/mitarbeiterinnen")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getMitarbeiterinnenReportExcel(
		@QueryParam("auswertungVon") @Nonnull String auswertungVon,
		@QueryParam("auswertungBis") @Nonnull String auswertungBis,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		String ip = downloadResource.getIP(request);

		Objects.requireNonNull(auswertungVon);
		Objects.requireNonNull(auswertungBis);
		LocalDate dateAuswertungVon = DateUtil.parseStringToDateOrReturnNow(auswertungVon);
		LocalDate dateAuswertungBis = DateUtil.parseStringToDateOrReturnNow(auswertungBis);

		if (!dateAuswertungBis.isAfter(dateAuswertungVon)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"getMitarbeiterinnenReportExcel",
				"Fehler beim erstellen Report Mitarbeiterinnen",
				DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN);
		}

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_MITARBEITERINNEN,
			dateAuswertungVon,
			dateAuswertungBis,
			null,
			LocaleThreadLocal.get()
		);

		return Response.ok(workJob.getId()).build();
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Benutzer'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/benutzer")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE, REVISOR, ADMIN_TS, ADMIN_TRAEGERSCHAFT, ADMIN_MANDANT,
		ADMIN_INSTITUTION })
	public Response getBenutzerReportExcel(
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo) {

		String ip = downloadResource.getIP(request);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_BENUTZER,
			null,
			null,
			null,
			LocaleThreadLocal.get()
		);

		return Response.ok(workJob.getId()).build();
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Institutionen'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/institutionen")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN })
	public Response getInstitutionenReportExcel(
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo) {

		String ip = downloadResource.getIP(request);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_INSTITUTIONEN,
			null,
			null,
			null,
			LocaleThreadLocal.get()
		);

		return Response.ok(workJob.getId()).build();
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Zahlungen pro Periode'",
		response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/zahlungperiode")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getZahlungPeriodReportExcel(
		@QueryParam("gesuchsperiodeID") @Nonnull @Valid JaxId gesuchPeriodIdParam,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		Objects.requireNonNull(gesuchPeriodIdParam);
		String ip = downloadResource.getIP(request);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		String periodeId = gesuchPeriodIdParam.getId();
		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_ZAHLUNG_AUFTRAG_PERIODE,
			null,
			null,
			periodeId,
			LocaleThreadLocal.get()
		);

		return Response.ok(workJob.getId()).build();
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Gesuchsteller-Kinder-Betreuung'",
		response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/gesuchstellerkinderbetreuung")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getGesuchstellerKinderBetreuungReportExcel(
		@QueryParam("auswertungVon") @Nonnull String auswertungVon,
		@QueryParam("auswertungBis") @Nonnull String auswertungBis,
		@QueryParam("gesuchPeriodeID") @Nullable @Valid JaxId gesuchPeriodIdParam,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		String ip = downloadResource.getIP(request);

		Objects.requireNonNull(auswertungVon);
		Objects.requireNonNull(auswertungBis);
		LocalDate dateFrom = DateUtil.parseStringToDateOrReturnNow(auswertungVon);
		LocalDate dateTo = DateUtil.parseStringToDateOrReturnNow(auswertungBis);

		if (!dateTo.isAfter(dateFrom)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"getGesuchstellerKinderBetreuungReportExcel",
				"Fehler beim erstellen Report Gesuchsteller-Kinder-Betreuung",
				DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN);
		}

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		String periodeId = gesuchPeriodIdParam != null ? gesuchPeriodIdParam.getId() : null;
		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_GESUCHSTELLER_KINDER_BETREUUNG,
			dateFrom,
			dateTo,
			periodeId,
			LocaleThreadLocal.get()
		);

		return Response.ok(workJob.getId()).build();
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Kinder'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/kinder")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getKinderReportExcel(
		@QueryParam("auswertungVon") @Nonnull String auswertungVon,
		@QueryParam("auswertungBis") @Nonnull String auswertungBis,
		@QueryParam("gesuchPeriodeID") @Nullable @Valid JaxId gesuchPeriodIdParam,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		String ip = downloadResource.getIP(request);

		Objects.requireNonNull(auswertungVon);
		Objects.requireNonNull(auswertungBis);
		LocalDate dateFrom = DateUtil.parseStringToDateOrReturnNow(auswertungVon);
		LocalDate dateTo = DateUtil.parseStringToDateOrReturnNow(auswertungBis);
		String periodeId = gesuchPeriodIdParam != null ? gesuchPeriodIdParam.getId() : null;

		if (!dateTo.isAfter(dateFrom)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"getKinderReportExcel",
				"Fehler beim erstellen Report Kinder"
				, DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN);
		}
		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_KINDER,
			dateFrom,
			dateTo,
			periodeId,
			LocaleThreadLocal.get()
		);

		return Response.ok(workJob.getId()).build();
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Gesuchsteller'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/gesuchsteller")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getGesuchstellerReportExcel(
		@QueryParam("stichtag") @Nonnull String stichtag,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo) {

		String ip = downloadResource.getIP(request);

		Objects.requireNonNull(stichtag);
		LocalDate date = DateUtil.parseStringToDateOrReturnNow(stichtag);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_GESUCHSTELLER,
			date,
			null,
			null,
			LocaleThreadLocal.get()
		);

		return Response.ok(workJob.getId()).build();
	}

	@ApiOperation(value = "Erstellt ein Excel mit der Statistik 'Massenversand'", response = JaxDownloadFile.class)
	@Nonnull
	@GET
	@Path("/excel/massenversand")
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response getMassenversandReportExcel(
		@QueryParam("auswertungVon") @Nullable String auswertungVon,
		@QueryParam("auswertungBis") @Nonnull String auswertungBis,
		@QueryParam("gesuchPeriodeID") @Nonnull @Valid JaxId gesuchPeriodIdParam,
		@QueryParam("inklBgGesuche") @Nullable String inklBgGesuche,
		@QueryParam("inklMischGesuche") @Nullable String inklMischGesuche,
		@QueryParam("inklTsGesuche") @Nullable String inklTsGesuche,
		@QueryParam("ohneErneuerungsgesuch") @Nullable String ohneErneuerungsgesuch,
		@QueryParam("text") @Nullable String text,
		@Context HttpServletRequest request, @Context UriInfo uriInfo)
		throws EbeguRuntimeException {

		String ip = downloadResource.getIP(request);

		Validate.notNull(auswertungBis);

		// auswertungVon might be null
		LocalDate dateFrom = auswertungVon == null
			? Constants.START_OF_TIME
			: DateUtil.parseStringToDateNullSafe(auswertungVon);

		LocalDate dateTo = DateUtil.parseStringToDateOrReturnNow(auswertungBis);
		String periodeId = gesuchPeriodIdParam.getId();

		if (!dateTo.isAfter(dateFrom)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"getMassenversandReportExcel",
				"Fehler beim erstellen Report Massenversand",
				DAS_VON_DATUM_MUSS_VOR_DEM_BIS_DATUM_SEIN);
		}

		final boolean inklBgGesucheBoolean = Boolean.parseBoolean(inklBgGesuche);
		final boolean inklMischGesucheBoolean = Boolean.parseBoolean(inklMischGesuche);
		final boolean inklTsGesucheBoolean = Boolean.parseBoolean(inklTsGesuche);
		Validate.isTrue(inklBgGesucheBoolean || inklMischGesucheBoolean || inklTsGesucheBoolean);

		Workjob workJob = createWorkjobForReport(request, uriInfo, ip);

		workJob = workjobService.createNewReporting(
			workJob,
			ReportVorlage.VORLAGE_REPORT_MASSENVERSAND,
			dateFrom,
			dateTo,
			periodeId,
			inklBgGesucheBoolean,
			inklMischGesucheBoolean,
			inklTsGesucheBoolean,
			Boolean.valueOf(ohneErneuerungsgesuch),
			text,
			LocaleThreadLocal.get()
		);

		return Response.ok(workJob.getId()).build();
	}

	@Nonnull
	private Workjob createWorkjobForReport(@Context HttpServletRequest request, @Context UriInfo uriInfo, String ip) {
		Workjob workJob = new Workjob();
		workJob.setWorkJobType(WorkJobType.REPORT_GENERATION);
		workJob.setStartinguser(principalBean.getPrincipal().getName());
		workJob.setTriggeringIp(ip);
		workJob.setRequestURI(uriInfo.getRequestUri().toString());
		String param = StringUtils.substringAfterLast(request.getRequestURI(), URL_PART_EXCEL);
		workJob.setParams(param);
		return workJob;
	}
}
