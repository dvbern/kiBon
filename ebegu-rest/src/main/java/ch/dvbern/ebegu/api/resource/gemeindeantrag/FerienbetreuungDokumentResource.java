/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource.gemeindeantrag;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.api.converter.JaxFerienbetreuungConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungDokument;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.entities.gemeindeantrag.ferienbetreuung.FerienbetreuungDokument;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.gemeindeantrag.FerienbetreuungDokumentService;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.api.resource.util.ResourceConstants.DOCX_FILE_EXTENSION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_FERIENBETREUUNG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_FERIENBETREUUNG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * REST Resource fuer die Ferienbetreuungen
 */
@Path("ferienbetreuung/dokument/")
@Stateless
@DenyAll
public class FerienbetreuungDokumentResource {

	@Inject
	private JaxFerienbetreuungConverter converter;

	@Inject
	private JaxFerienbetreuungConverter ferienbetreuungConverter;

	@Inject
	private FerienbetreuungDokumentService ferienbetreuungDokumentService;

	@Operation(
		summary = "Gibt alle Ferienbetreuungdokumente für den FerienbetreuungContainer zurück")
	@GET
	@Path("/all/{ferienbetreuungId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS, ADMIN_BG, SACHBEARBEITER_BG,
		ADMIN_FERIENBETREUUNG, SACHBEARBEITER_FERIENBETREUUNG })
	public List<JaxFerienbetreuungDokument> getFerienbetreuungDokumente(
		@Nonnull
		@NotNull
		@PathParam("ferienbetreuungId") JaxId ferienbetreuungContainerJaxId
	) {
		Objects.requireNonNull(ferienbetreuungContainerJaxId.getId());
		String ferienbetreuungContainerId = converter.toEntityId(
			ferienbetreuungContainerJaxId
		);
		List<FerienbetreuungDokument> ferienbetreuungDokumente =
			ferienbetreuungDokumentService.findDokumente(
				ferienbetreuungContainerId
			);

		return ferienbetreuungConverter.ferienbetreuungDokumentListToJax(
			ferienbetreuungDokumente
		);
	}

	@Operation(
		summary = "Loescht das Dokument mit der uebergebenen Id in der Datenbank")
	@Nullable
	@DELETE
	@Path("/{ferienbetreuungDokumentId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS, ADMIN_BG, SACHBEARBEITER_BG,
		ADMIN_FERIENBETREUUNG, SACHBEARBEITER_FERIENBETREUUNG })
	public Response removeFerienbetreuungDokument(
		@Nonnull
		@NotNull
		@PathParam("ferienbetreuungDokumentId") JaxId ferienbetreuungDokumentJAXPId
	) {

		requireNonNull(ferienbetreuungDokumentJAXPId.getId());
		String dokumentId = converter.toEntityId(ferienbetreuungDokumentJAXPId);

		FerienbetreuungDokument ferienbetreuungDokument =
			ferienbetreuungDokumentService.findDokument(dokumentId)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"removeFerienbetreuungDokument",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						dokumentId
					)
				);

		ferienbetreuungDokumentService.removeDokument(ferienbetreuungDokument);

		return Response.ok().build();
	}

	@Operation(
		summary = "Erstellt eine Docx Verfügung zum Ferienbetreuung für den übergebenen Gemeindeantrag")
	@POST
	@Path("/docx-erstellen/{containerJaxId}/{sprache}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response dokumentErstellen(
		@Nonnull @NotNull @PathParam("containerJaxId") JaxId containerJaxId,
		@Nonnull @PathParam("sprache") Sprache sprache
	) {
		Objects.requireNonNull(containerJaxId);
		Objects.requireNonNull(containerJaxId.getId());

		byte[] document = ferienbetreuungDokumentService.createDocx(
			containerJaxId.getId(),
			sprache
		);

		if (document.length > 0) {
			try {
				return RestUtil.buildDownloadResponse(
					true,
					DOCX_FILE_EXTENSION,
					"application/octet-stream",
					document
				);

			} catch (IOException e) {
				throw new EbeguRuntimeException(
					"dokumentErstellen",
					"error occured while building response",
					e
				);
			}
		}

		throw new EbeguRuntimeException(
			"dokumentErstellen",
			"Ferienbetreuung Template has no content"
		);

	}

}
