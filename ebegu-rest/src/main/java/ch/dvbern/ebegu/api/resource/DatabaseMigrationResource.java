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

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.services.DatabaseMigrationService;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Resource zum Ausfuehren von manuellen DB-Migrationen
 */
@Path("dbmigration")
@Stateless
@RolesAllowed(SUPER_ADMIN)
public class DatabaseMigrationResource {

	@Inject
	private DatabaseMigrationService databaseMigrationService;

	@Operation(summary = "Führt das Skript mit der übergebenen Nummer durch")
	@GET
	@Path("/{scriptNr}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@RolesAllowed(SUPER_ADMIN)
	public Response processScript(@PathParam("scriptNr") String scriptNr) {
		Objects.requireNonNull(scriptNr, "scriptNr muss gesetzt sein");
		databaseMigrationService.processScript(scriptNr);
		return Response.ok().build();
	}
}
