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

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBaseConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.entities.DownloadFile;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.reporting.ReportService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Reports
 */
@Path("reporting")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class ReportResource {

	@Inject
	private ReportService reportService;

	@Inject
	private DownloadResource downloadResource;

	@Inject
	private JaxBaseConverter converter;

	@Operation(
		summary = "Erstellt ein Excel mit der Statistik 'Zahlungsauftrag'")
	@Nonnull
	@GET
	@Path("/excel/zahlungsauftrag")
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getZahlungsauftragReportExcel(
		@QueryParam("zahlungsauftragID") @Nonnull @Valid JaxId jaxId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo
	)
		throws ExcelMergeException, EbeguRuntimeException, IOException {

		Objects.requireNonNull(jaxId);
		String ip = downloadResource.getIP(request);
		String id = converter.toEntityId(jaxId);

		UploadFileInfo uploadFileInfo = reportService
			.generateExcelReportZahlungAuftrag(id, LocaleThreadLocal.get());

		DownloadFile downloadFileInfo = new DownloadFile(uploadFileInfo, ip);

		return downloadResource.getFileDownloadResponse(
			uriInfo,
			ip,
			downloadFileInfo
		);
	}

	@Operation(summary = "Erstellt ein Excel mit der Statistik 'Zahlung'")
	@Nonnull
	@GET
	@Path("/excel/zahlung")
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, JURIST, REVISOR,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response getZahlungReportExcel(
		@QueryParam("zahlungID") @Nonnull @Valid JaxId jaxId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo
	)
		throws ExcelMergeException, EbeguRuntimeException, IOException {

		Objects.requireNonNull(jaxId);
		String ip = downloadResource.getIP(request);
		String id = converter.toEntityId(jaxId);

		UploadFileInfo uploadFileInfo = reportService
			.generateExcelReportZahlung(id, LocaleThreadLocal.get());

		DownloadFile downloadFileInfo = new DownloadFile(uploadFileInfo, ip);

		return downloadResource.getFileDownloadResponse(
			uriInfo,
			ip,
			downloadFileInfo
		);
	}
}
