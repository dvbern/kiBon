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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBaseConverter;
import ch.dvbern.ebegu.api.dtos.JaxAdresse;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.AdresseService;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Adressen
 */
@Path("adressen")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class AdresseResource {

	@Inject
	private AdresseService adresseService;

	@Inject
	private JaxBaseConverter converter;

	@Operation(summary = "Erstellt eine neue Adresse in der Datenbank.")
	@Nullable
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, GESUCHSTELLER, SACHBEARBEITER_TS,
		ADMIN_TS })
	public Response create(
		@Nonnull @NotNull JaxAdresse adresseJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Adresse convertedAdresse = converter.adresseToEntity(
			adresseJAXP,
			new Adresse()
		);
		Adresse persistedAdresse = this.adresseService.createAdresse(
			convertedAdresse
		);

		URI uri = uriInfo.getBaseUriBuilder()
			.path(AdresseResource.class)
			.path('/' + persistedAdresse.getId())
			.build();

		return Response.created(uri)
			.entity(converter.adresseToJAX(persistedAdresse))
			.build();
	}

	@Operation(summary = "Aktualisiert eine Adresse in der Datenbank.")
	@Nullable
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, GESUCHSTELLER, SACHBEARBEITER_TS,
		ADMIN_TS })
	public JaxAdresse update(
		@Nonnull @NotNull JaxAdresse adresseJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Objects.requireNonNull(adresseJAXP.getId());
		Adresse adrFromDB = adresseService.findAdresse(adresseJAXP.getId())
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"update",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					adresseJAXP.getId()
				)
			);
		Adresse adrToMerge = converter.adresseToEntity(adresseJAXP, adrFromDB);
		Adresse modifiedAdresse = this.adresseService.updateAdresse(adrToMerge);

		return converter.adresseToJAX(modifiedAdresse);
	}
}
