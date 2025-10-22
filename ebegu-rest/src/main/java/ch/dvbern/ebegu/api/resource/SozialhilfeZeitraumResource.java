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

package ch.dvbern.ebegu.api.resource;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.api.converter.gesuch.JaxSozialhilfeZeitraumConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxSozialhilfeZeitraumContainer;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraumContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.FamiliensituationService;
import ch.dvbern.ebegu.services.SozialhilfeZeitraumService;
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

@Path("sozialhilfeZeitraeume")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class SozialhilfeZeitraumResource {

	@Inject
	private JaxSozialhilfeZeitraumConverter converter;
	@Inject
	private SozialhilfeZeitraumService sozialhilfeZeitraumService;
	@Inject
	private FamiliensituationService familiensituationService;

	@Operation(
		summary = "Create a new ErwerbspensumContainer in the database. The object also has a relations to "
			+
			"Erwerbspensum data Objects, those will be created as well")
	@Nonnull
	@PUT
	@Path("/{famSitId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public JaxSozialhilfeZeitraumContainer saveSozialhilfeZeitraum(
		@Nonnull @NotNull @PathParam("famSitId") JaxId famSitId,
		@Nonnull
		@NotNull
		@Valid JaxSozialhilfeZeitraumContainer jaxSozialhilfeZeitraumContainer
	) throws EbeguEntityNotFoundException {

		FamiliensituationContainer famSit =
			familiensituationService.findFamiliensituation(famSitId.getId())
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"saveSozialhilfeZeitraum",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						"Familliensituation id invalid: "
							+ famSitId.getId()
					)
				);
		SozialhilfeZeitraumContainer convertedSozialhilfeZeitraumContainer =
			converter.sozialhilfeZeitraumContainerToStorableEntity(
				jaxSozialhilfeZeitraumContainer
			);

		convertedSozialhilfeZeitraumContainer.setFamiliensituationContainer(
			famSit
		);

		SozialhilfeZeitraumContainer storedShZCont =
			sozialhilfeZeitraumService.saveSozialhilfeZeitraum(
				convertedSozialhilfeZeitraumContainer
			);

		JaxSozialhilfeZeitraumContainer jaxShzCont = converter
			.sozialhilfeZeitraumContainerToJAX(storedShZCont);
		return jaxShzCont;
	}

	@Operation(
		summary = "Remove the SozialhilfeZeitraum Container with the specified ID from the database.")
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@Nullable
	@DELETE
	@Path("/sozialhilfeZeitraumId/{sozialhilfeZeitraumContID}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public Response removeSozialhilfeZeitraum(
		@Nonnull
		@NotNull
		@PathParam("sozialhilfeZeitraumContID") JaxId sozialhilfeZeitraumContIDJAXPId
	) {

		Objects.requireNonNull(sozialhilfeZeitraumContIDJAXPId.getId());
		sozialhilfeZeitraumService.removeSozialhilfeZeitraum(
			converter.toEntityId(sozialhilfeZeitraumContIDJAXPId)
		);

		return Response.ok().build();
	}

}
