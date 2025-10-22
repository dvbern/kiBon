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
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import ch.dvbern.ebegu.api.converter.JaxSozialdienstConverter;
import ch.dvbern.ebegu.api.converter.gesuch.JaxFallDossierConverter;
import ch.dvbern.ebegu.api.dtos.JaxFall;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.sozialdienst.JaxSozialdienstFall;
import ch.dvbern.ebegu.api.dtos.sozialdienst.JaxSozialdienstFallDokument;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFallDokument;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.SozialdienstFallStatus;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.FallService;
import ch.dvbern.ebegu.services.SozialdienstFallDokumentService;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * Resource fuer Fall
 */
@Path("unterstuetzungsdienstfall")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
@NoArgsConstructor
public class UnterstuetzungsdienstFallResource {

	private FallService fallService;
	private JaxFallDossierConverter converter;
	private JaxSozialdienstConverter jaxSozialdienstConverter;
	private SozialdienstFallDokumentService sozialdienstFallDokumentService;
	private Authorizer authorizer;

	@Inject
	public UnterstuetzungsdienstFallResource(
		FallService fallService,
		JaxFallDossierConverter converter,
		JaxSozialdienstConverter jaxSozialdienstConverter,
		SozialdienstFallDokumentService sozialdienstFallDokumentService,
		Authorizer authorizer
	) {
		this.fallService = fallService;
		this.converter = converter;
		this.jaxSozialdienstConverter = jaxSozialdienstConverter;
		this.sozialdienstFallDokumentService = sozialdienstFallDokumentService;
		this.authorizer = authorizer;
	}

	@Operation(summary = "return the Vollmacht Dokument for the given language")
	@GET
	@Path("/generateVollmachtDokument/{fallId}/{sprache}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public Response generateVollmachtDokument(
		@Nonnull @PathParam("fallId") String fallId,
		@Nonnull @PathParam("sprache") Sprache sprache,
		@Context HttpServletResponse response
	) throws MergeDocException {
		requireNonNull(fallId);

		final byte[] content = fallService.generateVollmachtDokument(
			fallId,
			sprache
		);

		if (content != null && content.length > 0) {
			try {
				return RestUtil.buildDownloadResponse(
					true,
					"vollmacht.pdf",
					"application/octet-stream",
					content
				);

			} catch (IOException e) {
				return Response.status(Status.NOT_FOUND)
					.entity(
						"Vollmacht Dokument fuer SozialdienstFall: "
							+ fallId
							+ " kann nicht generiert werden"
					)
					.build();
			}
		}

		return Response.status(Status.NO_CONTENT).build();
	}

	@Operation(
		summary = "Gibt alle VollmachtDokumente zurück, die die aktuelle SozialdienstFall gehoeren")
	@GET
	@Path("/vollmachtDokumente/{sozialdienstFallId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST, ADMIN_MANDANT,
		SACHBEARBEITER_MANDANT })
	public List<JaxSozialdienstFallDokument> getVollmachtDokumente(
		@Nonnull
		@NotNull
		@PathParam("sozialdienstFallId") JaxId sozialdienstFallJaxId
	) {
		Objects.requireNonNull(sozialdienstFallJaxId.getId());
		String sozialdienstFallId = converter.toEntityId(sozialdienstFallJaxId);
		List<SozialdienstFallDokument> sozialdienstFallDokumente =
			sozialdienstFallDokumentService.findDokumente(
				sozialdienstFallId
			);

		return jaxSozialdienstConverter.sozialdienstFallDokumentListToJax(
			sozialdienstFallDokumente
		);
	}

	@Operation(
		summary = "Loescht das Dokument mit der uebergebenen Id in der Datenbank")
	@Nullable
	@DELETE
	@Path("/vollmachtDokument/{vollmachtDokumentId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public Response removeVollmachtDokument(
		@Nonnull
		@NotNull
		@PathParam("vollmachtDokumentId") JaxId vollmachtDokumentJAXPId,
		@Context HttpServletResponse response
	) {

		requireNonNull(vollmachtDokumentJAXPId.getId());
		String dokumentId = converter.toEntityId(vollmachtDokumentJAXPId);

		SozialdienstFallDokument sozialdienstFallDokument =
			sozialdienstFallDokumentService.findDokument(dokumentId)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"removeVollmachtDokument",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						dokumentId
					)
				);

		sozialdienstFallDokumentService.removeDokument(
			sozialdienstFallDokument
		);

		return Response.ok().build();
	}

	@Operation(summary = "Setz der SozialdienstFall zu entzogen Status"
	)
	@PUT
	@Path("/sozialdienstFallEntziehen/{fallId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST, ADMIN_MANDANT,
		SACHBEARBEITER_MANDANT })
	public JaxFall sozialdienstFallEntziehen(
		@Nonnull @NotNull @PathParam("fallId") JaxId fallJAXPId
	) {

		Fall fall = getFallFromId(fallJAXPId);
		authorizer.checkWriteAuthorization(fall);
		Objects.requireNonNull(fall.getSozialdienstFall());
		fall.getSozialdienstFall().setStatus(SozialdienstFallStatus.ENTZOGEN);

		Fall persistedFall = this.fallService.saveFall(fall);
		return converter.fallToJAX(persistedFall);
	}

	@Operation(
		summary = "Setzt den SozialdienstFall in den Status INAKTIV zurück")
	@PUT
	@Path("{fallId}/inaktivieren")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public JaxSozialdienstFall sozialdienstFallInaktivieren(
		@Nonnull @NotNull @PathParam("fallId") JaxId fallJAXPId
	) {

		final Fall fall = getFallFromId(fallJAXPId);
		Objects.requireNonNull(fall.getSozialdienstFall());
		fall.getSozialdienstFall().setStatus(SozialdienstFallStatus.INAKTIV);

		Fall persistedFall = this.fallService.saveFall(fall);
		return jaxSozialdienstConverter.sozialdienstFallToJAX(
			requireNonNull(persistedFall.getSozialdienstFall())
		);
	}

	private Fall getFallFromId(JaxId fallJAXPId) {
		Objects.requireNonNull(fallJAXPId.getId());
		String fallID = converter.toEntityId(fallJAXPId);
		Fall fall = fallService.findFall(fallID)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"sozialdienstFallInaktivieren",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					fallID
				)
			);

		Objects.requireNonNull(fall.getSozialdienstFall());
		authorizer.checkWriteAuthorization(fall);
		authorizer.checkWriteAuthorization(fall.getSozialdienstFall());
		return fall;
	}

	@Operation(
		summary = "Setz der SozialdienstFall in den Status INAKTIV zurück"
	)
	@PUT
	@Path("{fallId}/eroeffnen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public JaxSozialdienstFall sozialdienstFallEroeffnen(
		@Nonnull @NotNull @PathParam("fallId") JaxId fallJAXPId
	) {

		final Fall fall = getFallFromId(fallJAXPId);
		Objects.requireNonNull(fall.getSozialdienstFall());
		fall.getSozialdienstFall().setStatus(SozialdienstFallStatus.AKTIV);

		Fall persistedFall = this.fallService.saveFall(fall);
		return jaxSozialdienstConverter.sozialdienstFallToJAX(
			requireNonNull(persistedFall.getSozialdienstFall())
		);
	}
}
