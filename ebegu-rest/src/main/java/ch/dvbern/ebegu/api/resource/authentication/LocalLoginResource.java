/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.api.resource.authentication;

import java.net.URI;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.locallogin.LocalLoginConfig;
import ch.dvbern.ebegu.locallogin.LocalLoginService;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("locallogin")
@PermitAll
public class LocalLoginResource {

	@Inject
	private LocalLoginConfig localloginConfig;

	@Inject
	private LocalLoginService localLoginService;

	private void assertLocalloginEnabled() {
		if (!localloginConfig.isEnabled()) {
			throw new UnsupportedOperationException();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/createUsers")
	public Response createUsers(@Context HttpServletRequest request) {
		assertLocalloginEnabled();

		var requestUri = URI.create(
			request.getRequestURL().toString()
		);
		MandantIdentifier mandantIdentifier =
			MandantIdentifier.getByHostname(requestUri);

		var results = localLoginService.createPersonaUsers(mandantIdentifier);

		return Response.ok(results).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/listUsers")
	public Response listUsers(@Context HttpServletRequest request) {
		assertLocalloginEnabled();

		var requestUri = URI.create(
			request.getRequestURL().toString()
		);

		MandantIdentifier mandantIdentifier =
			MandantIdentifier.getByHostname(requestUri);

		return Response.ok(
			localLoginService.getLocalLoginUsers(
				mandantIdentifier
			)
		).build();
	}
}
