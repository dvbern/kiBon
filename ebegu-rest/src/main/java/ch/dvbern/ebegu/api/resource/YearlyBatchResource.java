/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import javax.annotation.Nullable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.batch.YearlyBatchService;
import ch.dvbern.ebegu.enums.UserRoleName;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * Resource fuer DailyBatch. Dies darf nur als SUPERADMIN aufgerufen werden
 */
@Path("yearly-batch")
@Stateless
@RolesAllowed(UserRoleName.SUPER_ADMIN)
public class YearlyBatchResource {

	@Inject
	private YearlyBatchService yearlyBatchService;

	@Inject
	private PrincipalBean principalBean;

	@Operation(
		summary = "Führt den Job createGemeindeKennzahlenForCurrentGPForAllActiveGemeindenAndSendReminder aus.")
	@Nullable
	@POST
	@Path("create-gemeinde-kennzahlen-active-gemeinden")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createGemeindeKennzahlenForCurrentGPForAllActiveGemeindenAndSendReminder() {
		var result = yearlyBatchService
			.createGemeindeKennzahlenForCurrentGPForAllActiveGemeindenAndSendReminder(
				principalBean.getMandant()
			);
		return Response.ok(result).build();
	}

	@Operation(
		summary = "Führt den Job sendGemeindeKennzahlenSecondReminder aus.")
	@Nullable
	@POST
	@Path("send-gemeinde-kennzahlen-second-reminder")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response sendGemeindeKennzahlenSecondReminder() {
		var result = yearlyBatchService.sendGemeindeKennzahlenSecondReminder(
			principalBean.getMandant()
		);
		return Response.ok(result).build();
	}
}
