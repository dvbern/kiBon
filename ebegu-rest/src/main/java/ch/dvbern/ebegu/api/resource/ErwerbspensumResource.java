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

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.gesuch.JaxErwerbspensumConverter;
import ch.dvbern.ebegu.api.dtos.JaxErwerbspensumContainer;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.resource.util.ResourceHelper;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.ErwerbspensumService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Erwerbspensum
 */
@Path("erwerbspensen")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class ErwerbspensumResource {

	@Inject
	private ErwerbspensumService erwerbspensumService;
	@Inject
	private GesuchstellerService gesuchstellerService;
	@Inject
	private GesuchService gesuchService;

	@SuppressWarnings("CdiInjectionPointsInspection")
	@Inject
	private JaxErwerbspensumConverter converter;

	@Inject
	private ResourceHelper resourceHelper;

	@Operation(
		summary = "Create a new ErwerbspensumContainer in the database. The object also has a relations to "
			+
			"Erwerbspensum data Objects, those will be created as well")
	@Nonnull
	@PUT
	@Path("/{gesuchstellerId}/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public Response saveErwerbspensum(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId,
		@Nonnull
		@NotNull
		@PathParam("gesuchstellerId") JaxId gesuchstellerId,
		@Nonnull
		@NotNull
		@Valid JaxErwerbspensumContainer jaxErwerbspensumContainer,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) throws EbeguEntityNotFoundException {

		Gesuch gesuch =
			gesuchService.findGesuch(gesuchJAXPId.getId())
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"saveErwerbspensum",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						"GesuchId invalid: "
							+ gesuchJAXPId.getId()
					)
				);
		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(gesuch);

		GesuchstellerContainer gesuchsteller =
			gesuchstellerService.findGesuchsteller(gesuchstellerId.getId())
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"saveErwerbspensum",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						"GesuchstellerId invalid: "
							+ gesuchstellerId.getId()
					)
				);
		ErwerbspensumContainer convertedEwpContainer =
			converter.erwerbspensumContainerToStoreableEntity(
				jaxErwerbspensumContainer
			);
		convertedEwpContainer.setGesuchsteller(gesuchsteller);
		ErwerbspensumContainer storedEwpCont = this.erwerbspensumService
			.saveErwerbspensum(
				convertedEwpContainer,
				gesuch
			);

		URI uri = null;
		if (uriInfo != null) {
			uri = uriInfo.getBaseUriBuilder()
				.path(ErwerbspensumResource.class)
				.path('/' + storedEwpCont.getId())
				.build();
		}
		JaxErwerbspensumContainer jaxEwpCont = converter
			.erwerbspensumContainerToJAX(storedEwpCont);
		return Response.created(uri).entity(jaxEwpCont).build();
	}

	@Operation(
		summary = "Returns the ErwerbspensumContainer with the specified ID ")
	@Nullable
	@GET
	@Path("/{erwerbspensumContID}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public JaxErwerbspensumContainer findErwerbspensum(
		@Nonnull
		@NotNull
		@PathParam("erwerbspensumContID") JaxId erwerbspensumContID
	) throws EbeguRuntimeException {

		Objects.requireNonNull(erwerbspensumContID.getId());
		String entityID = converter.toEntityId(erwerbspensumContID);
		Optional<ErwerbspensumContainer> optional = erwerbspensumService
			.findErwerbspensum(entityID);

		if (!optional.isPresent()) {
			return null;
		}
		ErwerbspensumContainer erwerbspenCont = optional.get();
		return converter.erwerbspensumContainerToJAX(erwerbspenCont);
	}

	@Operation(
		summary = "Remove the ErwerbspensumContainer with the specified ID from the database.")
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@Nullable
	@DELETE
	@Path("/gesuchId/{gesuchId}/erwPenId/{erwerbspensumContID}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public Response removeErwerbspensum(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId,
		@Nonnull
		@NotNull
		@PathParam("erwerbspensumContID") JaxId erwerbspensumContIDJAXPId,
		@Context HttpServletResponse response
	) {

		Objects.requireNonNull(erwerbspensumContIDJAXPId.getId());
		Gesuch gesuch =
			gesuchService.findGesuch(gesuchJAXPId.getId())
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"removeErwerbspensum",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						"GesuchId invalid: "
							+ gesuchJAXPId.getId()
					)
				);

		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(gesuch);

		erwerbspensumService.removeErwerbspensum(
			converter.toEntityId(erwerbspensumContIDJAXPId),
			gesuch
		);
		return Response.ok().build();
	}

	@Operation(
		summary = "Returns true, if the declaration of Erwerbspensum is required for the given Gesuch")
	@GET
	@Path("/required/{gesuchId}")
	@Consumes(MediaType.WILDCARD)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public boolean isErwerbspensumRequired(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId
	) {
		Objects.requireNonNull(gesuchJAXPId.getId());
		Gesuch gesuch = gesuchService.findGesuch(gesuchJAXPId.getId())
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"isErwerbspensumRequired",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					"GesuchId invalid: " + gesuchJAXPId.getId()
				)
			);
		return erwerbspensumService.isErwerbspensumRequired(gesuch);
	}
}
