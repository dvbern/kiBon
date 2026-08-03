/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.services.admin.KeycloakAdminServiceBean;
import ch.dvbern.ebegu.util.Constants;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Path("admin/keycloak")
@Stateless
@RolesAllowed(SUPER_ADMIN)
public class KeycloakAdminResource {

	@Inject
	private KeycloakAdminServiceBean keycloakAdminServiceBean;

	@Inject
	private PrincipalBean principalBean;

	@Operation(summary = "Erstellt die MitarbeiterRechte")
	@GET
	@Path("/accessrechte/erstellen")
	@Produces(MediaType.TEXT_PLAIN)
	public Response mitarbeiterrechteErstellen(
		@Context HttpServletRequest request
	) {
		var future = keycloakAdminServiceBean.createAccessMitarbeiterRechte(
			principalBean.getMandant()
		);
		String time = LocalDateTime.now().format(Constants.DATE_FORMATTER);
		try {
			var success = future.get();
			if (success) {
				return Response.ok(
					time + " Mitarbeiterrechte erstellen abgeschlossen"
				)
					.build();
			}
			return Response.serverError()
				.entity(
					time
						+ "Der Prozess konnte nicht erfolgreich abgeschlossen werden"
				)
				.build();
		} catch (InterruptedException | ExecutionException e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}
}
