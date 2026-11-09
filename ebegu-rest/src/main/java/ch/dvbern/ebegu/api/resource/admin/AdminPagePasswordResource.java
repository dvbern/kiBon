/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource.admin;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.adminpagepassword.AdminPagePasswordService;
import ch.dvbern.ebegu.enums.UserRoleName;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("admin/pagePassword")
@Stateless
public class AdminPagePasswordResource {

	@Inject
	private AdminPagePasswordService adminPagePasswordService;

	public record VerifyPasswordRequest(String password) {
	}

	@Operation(
		summary = "returns if the admin pages have a password protection")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/required")
	@RolesAllowed(UserRoleName.SUPER_ADMIN)
	public Response passwordRequired() {
		return Response.ok(
			adminPagePasswordService.isPasswordRequired()
		).build();
	}

	@Operation(
		summary = "checks the password for the admin pages")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/verify")
	@RolesAllowed(UserRoleName.SUPER_ADMIN)
	public Response verifyPassword(VerifyPasswordRequest body) {
		return Response.ok(
			adminPagePasswordService.verifyPassword(
				body == null ? null : body.password()
			)
		).build();
	}
}
