/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.RollbackException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxLastenausgleichConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxLastenausgleich;
import ch.dvbern.ebegu.api.dtos.parameter.JaxLastenausgleichCreateDTO;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.DownloadFile;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.lastenausgleich.WorkjobLastenausgleichService;
import ch.dvbern.ebegu.reporting.ReportKinderMitZemisNummerService;
import ch.dvbern.ebegu.reporting.ReportLastenausgleichBerechnungService;
import ch.dvbern.ebegu.services.lastenausgleich.LastenausgleichServiceBean;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Resource fuer Lastenausgleiche
 */
@Path("lastenausgleich")
@Stateless
@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
public class LastenausgleichResource {

	@Inject
	private LastenausgleichServiceBean lastenausgleichService;

	@Inject
	private ReportLastenausgleichBerechnungService reportService;

	@Inject
	private ReportKinderMitZemisNummerService zemisNummerService;

	@Inject
	private DownloadResource downloadResource;

	@Inject
	private JaxLastenausgleichConverter converter;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private WorkjobLastenausgleichService workjobLastenausgleichService;

	@Operation(summary = "Gibt alle Lastenausgleiche zurueck.")
	@Nullable
	@GET
	@Path("/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		SACHBEARBEITER_GEMEINDE, ADMIN_GEMEINDE,
		SACHBEARBEITER_BG, ADMIN_BG })
	public List<JaxLastenausgleich> getAllLastenausgleiche() {
		if (principalBean.isCallerInAnyOfRole(
			SACHBEARBEITER_GEMEINDE,
			ADMIN_GEMEINDE,
			SACHBEARBEITER_BG,
			ADMIN_BG
		)) {
			Set<Gemeinde> gemeindeList = principalBean.getBenutzer()
				.getCurrentBerechtigung()
				.getGemeindeList();

			return lastenausgleichService.getLastenausgleicheForGemeinden(
				gemeindeList,
				principalBean.getMandant()
			)
				.stream()
				.map(
					lastenausgleich -> converter.lastenausgleichToJAX(
						lastenausgleich
					)
				)
				.collect(Collectors.toList());
		}
		return lastenausgleichService.getAllLastenausgleiche(
			principalBean.getMandant()
		)
			.stream()
			.map(
				lastenausgleich -> converter.lastenausgleichToJAX(
					lastenausgleich
				)
			)
			.collect(Collectors.toList());
	}

	@Operation(summary = "Erstellt einen neuen Workjob für den Lastenausgleich")
	@Nullable
	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response createLastenausgleich(
		@Valid @Nonnull JaxLastenausgleichCreateDTO lastenausgleichCreateDTO
	) throws EbeguRuntimeException {
		lastenausgleichService.assertLastenausgleichNotExistingForYear(
			lastenausgleichCreateDTO.getJahr()
		);
		workjobLastenausgleichService.startLastenausgleichWorkjob(
			lastenausgleichCreateDTO.getJahr(),
			lastenausgleichCreateDTO.getSelbstbehaltPro100ProzentPlatz()
		);
		return Response.ok().build();
	}

	@SuppressWarnings("PMD.PreserveStackTrace")
	@Operation(summary = "Erstellt ein Excel mit der Statistik 'Zahlung'")
	@Nonnull
	@GET
	@Path("/excel/")
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		SACHBEARBEITER_GEMEINDE, ADMIN_GEMEINDE,
		SACHBEARBEITER_BG, ADMIN_BG })
	public Response getLastenausgleichReportExcel(
		@QueryParam("lastenausgleichId") @Nonnull @Valid JaxId jaxId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo
	)
		throws ExcelMergeException, EbeguRuntimeException,
		RollbackException, IOException {

		Objects.requireNonNull(jaxId);
		String lastenausgleichId = converter.toEntityId(jaxId);

		try {
			UploadFileInfo uploadFileInfo =
				reportService.generateExcelReportLastenausgleichKibon(
					lastenausgleichId,
					LocaleThreadLocal.get()
				);
			DownloadFile downloadFileInfo = new DownloadFile(
				uploadFileInfo
			);
			return downloadResource.getFileDownloadResponse(
				uriInfo,
				downloadFileInfo
			);
		} catch (RollbackException rollbackException) {
			RollbackException exceptionWithoutSuppressed =
				new RollbackException(rollbackException.getMessage());
			exceptionWithoutSuppressed.setStackTrace(
				rollbackException.getStackTrace()
			);
			throw exceptionWithoutSuppressed;
		}
	}

	@Operation(
		summary = "Erstellt ein CSV Textdokument für den Lastenausgleich")
	@Nonnull
	@GET
	@Path("/csv/")
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLastenausgleichReportCSV(
		@QueryParam("lastenausgleichId") @Nonnull @Valid JaxId jaxId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo
	)
		throws EbeguRuntimeException {

		Objects.requireNonNull(jaxId);
		String lastenausgleichId = converter.toEntityId(jaxId);

		UploadFileInfo uploadFileInfo = reportService
			.generateCSVReportLastenausgleichKibon(lastenausgleichId);
		DownloadFile downloadFileInfo = new DownloadFile(uploadFileInfo);

		return downloadResource.getFileDownloadResponse(
			uriInfo,
			downloadFileInfo
		);
	}

	@Operation(
		summary = "Erstellt ein Excel mit allen Kinder des angegebenen Jahres mit einer ZEMIS-Nummer")
	@Nonnull
	@GET
	@Path("/zemisexcel/")
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES,
		unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getZemisExcel(
		@QueryParam("jahr") @Nonnull @Valid Integer lastenausgleichJahr,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo
	)
		throws ExcelMergeException, EbeguRuntimeException, IOException {

		Objects.requireNonNull(lastenausgleichJahr);

		UploadFileInfo uploadFileInfo = zemisNummerService.generateZemisReport(
			lastenausgleichJahr,
			Locale.GERMAN
		);
		DownloadFile downloadFileInfo = new DownloadFile(uploadFileInfo);

		return downloadResource.getFileDownloadResponse(
			uriInfo,
			downloadFileInfo
		);
	}

	@Operation(
		summary = "Loescht den Lastenausgleich mit der uebergebenen id aus der DB")
	@Nullable
	@DELETE
	@Path("/{lastenausgleichId}")
	@Consumes(MediaType.WILDCARD)
	public Response removeLastenausgleich(
		@Nonnull
		@NotNull
		@PathParam("lastenausgleichId") JaxId lastenausgleichJAXPId
	) {

		Objects.requireNonNull(lastenausgleichJAXPId.getId());
		final String lastenausgleichId = converter.toEntityId(
			lastenausgleichJAXPId
		);
		lastenausgleichService.removeLastenausgleich(lastenausgleichId);
		return Response.ok().build();
	}
}
