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
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.gesuch.JaxFallDossierConverter;
import ch.dvbern.ebegu.api.dtos.JaxFall;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.services.FallService;
import lombok.NoArgsConstructor;
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
 * Resource fuer Fall
 */
@Path("falle")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
@NoArgsConstructor
public class FallResource {

	private FallService fallService;
	private JaxFallDossierConverter converter;

	@Inject
	public FallResource(
		FallService fallService,
		JaxFallDossierConverter converter
	) {
		this.fallService = fallService;
		this.converter = converter;
	}

	@Operation(
		summary = "Creates a new Fall in the database. The transfer object also has a relation to Gesuch "
			+
			"which is stored in the database as well.")
	@Nonnull
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public JaxFall saveFall(
		@Nonnull @NotNull @Valid JaxFall fallJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Fall fall = new Fall();
		if (fallJAXP.getId() != null) {
			Optional<Fall> optional = fallService.findFall(fallJAXP.getId());
			fall = optional.orElse(new Fall());
		}
		Fall convertedFall = converter.fallToEntity(fallJAXP, fall);

		Fall persistedFall = this.fallService.saveFall(convertedFall);
		return converter.fallToJAX(persistedFall);
	}

	@Operation(summary = "Returns the Fall with the given Id.")
	@Nullable
	@GET
	@Path("/id/{fallId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public JaxFall findFall(
		@Nonnull @NotNull @PathParam("fallId") JaxId fallJAXPId
	) {
		Objects.requireNonNull(fallJAXPId.getId());
		String fallID = converter.toEntityId(fallJAXPId);
		Optional<Fall> fallOptional = fallService.findFall(fallID);

		if (!fallOptional.isPresent()) {
			return null;
		}
		Fall fallToReturn = fallOptional.get();
		return converter.fallToJAX(fallToReturn);
	}
}
