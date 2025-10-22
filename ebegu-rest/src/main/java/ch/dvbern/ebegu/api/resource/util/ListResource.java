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

package ch.dvbern.ebegu.api.resource.util;

import java.util.ArrayList;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.enums.Land;

@Path("lists")
@Stateless
public class ListResource {

	@Path("laender")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCountries(@Context HttpServletRequest request) {

		List<Land> laender = new ArrayList<>();
		for (final Land landIsoCode : Land.values()) {
			if (!(Land.NICHTANERKANNT == landIsoCode
				|| Land.UNBEKANNT == landIsoCode
				|| Land.STAATENLOS == landIsoCode)
				&& landIsoCode.isValid()) {
				laender.add(landIsoCode);
			}
		}

		return Response.ok(laender).build();
	}

}
