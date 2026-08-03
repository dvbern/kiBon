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

import javax.annotation.Nonnull;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.dto.SupportAnfrageDTO;
import ch.dvbern.ebegu.services.MailService;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * Resource fuer Supportanfragen
 */
@Path("support")
@Stateless
@PermitAll
public class SupportResource {

	@Inject
	private MailService mailService;

	@Operation(
		summary = "Sendet eine Supportanfrage an die definierte Supportadresse")
	@Nonnull
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendSupportAnfrage(
		@Nonnull @NotNull @Valid SupportAnfrageDTO fallJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		mailService.prepareToSendSupportAnfrage(fallJAXP);
		return Response.ok().build();
	}
}
