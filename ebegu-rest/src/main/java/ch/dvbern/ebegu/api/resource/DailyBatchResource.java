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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.annotation.Nullable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.services.DailyBatch;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource fuer DailyBatch. Dies darf nur als SUPERADMIN aufgerufen werden
 */
@Path("dailybatch")
@Stateless
@RolesAllowed(UserRoleName.SUPER_ADMIN)
public class DailyBatchResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(
		DailyBatchResource.class
	);

	@Inject
	private DailyBatch dailyBatch;
	@Inject
	private PrincipalBean principalBean;

	@Operation(summary = "Führt den Job runBatchCleanDownloadFiles aus.")
	@Nullable
	@GET
	@Path("/cleanDownloadFiles")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response runBatchCleanDownloadFiles() {
		Future<Boolean> booleanFuture = dailyBatch.runBatchCleanDownloadFiles();
		return executeFuture(booleanFuture, "CleanDownloadFiles");
	}

	@Operation(summary = "Führt den Job runBatchMahnungFristablauf aus.")
	@Nullable
	@GET
	@Path("/mahnungFristAblauf")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response runBatchMahnungFristablauf() {
		Future<Boolean> booleanFuture = dailyBatch.runBatchMahnungFristablauf();
		return executeFuture(booleanFuture, "MahnungFristablauf");
	}

	@Operation(summary = "Führt den Job UpdateBGInstitutionGemeinden aus.")
	@Nullable
	@GET
	@Path("/updateGemeindeForBGInstitutionen")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response runBatchUpdateGemeindeForBGInstitutionen() {
		Future<Integer> count = dailyBatch
			.runBatchUpdateGemeindeForBGInstitutionen();
		return executeFuture(count, "UpdateGemeindeForBGInstitutionen");
	}

	@Operation(
		summary = "Führt den Job InfoOffenePendenzenNeueMitteilungGemeinde aus.")
	@Nullable
	@GET
	@Path("/runBatchInfoOffenePendenzenNeueMitteilungGemeinde")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public Response runBatchInfoOffenePendenzenNeueMitteilungGemeinde() {
		Future<Boolean> booleanFuture = dailyBatch
			.runBatchInfoOffenePendenzenNeueMitteilungGemeinde(
				principalBean.getMandant()
			);
		return executeFuture(
			booleanFuture,
			"runBatchInfoOffenePendenzenNeueMitteilungGemeinde"
		);
	}

	private static Response executeFuture(
		Future<?> future,
		String batchjobName
	) {
		try {
			var result = future.get();
			String info = String.format(
				"Manuelle ausführung! Batchjob {%s} durchgefuehrt mit Resultat: {%s}",
				batchjobName,
				result
			);
			LOGGER.info(info);
			return Response.ok(info).build();
		} catch (ExecutionException e) {
			return logExceptionAndBuildError(batchjobName, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return logExceptionAndBuildError(batchjobName, e);
		}
	}

	private static Response logExceptionAndBuildError(
		String batchjobName,
		Exception e
	) {
		String errorMessage = String.format(
			"Manuelle ausführung! Batch-Job Mahnung {%s} konnte nicht durchgefuehrt werden!",
			batchjobName
		);
		LOGGER.error(errorMessage, e);
		return Response.serverError().build();
	}
}
