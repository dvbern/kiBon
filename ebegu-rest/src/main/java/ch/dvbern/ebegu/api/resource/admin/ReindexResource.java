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

import ch.dvbern.ebegu.services.EntityIndexer;
import ch.dvbern.ebegu.util.Constants;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Path("admin/reindex")
@Stateless
@RolesAllowed(SUPER_ADMIN)
public class ReindexResource {

	@Inject
	private EntityIndexer entityIndexer;

	@Operation(summary = "Erstellt den Suchindex neu")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response reindex(@Context HttpServletRequest request) {
		entityIndexer.rebuildSearchIndex();

		String time = LocalDateTime.now().format(Constants.DATE_FORMATTER);
		return Response.ok(time + " Reindex started...").build();
	}
}
