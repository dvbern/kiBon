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

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.operations.NoSuchJobExecutionException;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.JobExecution;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.api.converter.BatchJaxBConverter;
import ch.dvbern.ebegu.api.dtos.batch.JaxBatchJobList;
import ch.dvbern.ebegu.api.dtos.batch.JaxWorkJob;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Workjob;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.enums.reporting.BatchJobStatus;
import ch.dvbern.ebegu.services.WorkjobService;

@Path("admin/batch")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class BatchResource {

	@Inject
	private BatchJaxBConverter converter;

	@Inject
	private WorkjobService workjobService;

	@Inject
	private PrincipalBean principalBean;

	@GET
	@Path("/jobs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(UserRoleName.SUPER_ADMIN)
	public JaxBatchJobList getAllJobs(
		@Valid @MatrixParam("start") @DefaultValue("0") int start,
		@Valid @MatrixParam("count") @DefaultValue("100") int count
	) {

		JobOperator operator = BatchRuntime.getJobOperator();
		final List<JaxWorkJob> resultlist = operator.getJobNames()
			.stream()
			.flatMap(
				name -> operator.getJobInstances(name, start, count)
					.stream()
					.flatMap(
						inst -> operator.getJobExecutions(inst)
							.stream()
					)
					.map(
						(ele) -> new JaxWorkJob(
							ele.getJobName(),
							ele
						)
					)
			)
			.collect(Collectors.toList());

		return new JaxBatchJobList(resultlist);
	}

	@GET
	@Path("/userjobs/notokenrefresh") //wir pollen diesen endpunkt daher notokenrefresh
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public Response getBatchJobsOfUser() {

		// Fuer Gesuchsteller gibt es keine BatchJobs
		if (principalBean.isCallerInRole(UserRole.GESUCHSTELLER)) {
			return Response.ok().build();
		}

		Set<BatchJobStatus> all = Arrays.stream(BatchJobStatus.values())
			.collect(Collectors.toSet());
		final List<Workjob> jobs = workjobService.findWorkjobs(
			principalBean.getPrincipal().getName(),
			all
		);

		final JobOperator jobOperator = BatchRuntime.getJobOperator();

		final List<JaxWorkJob> jobList = jobs.stream()
			.map(job -> converter.toBatchJobInformation(job))
			.peek((jaxWorkJob) -> {
				JobExecution jobExecution = null;
				try {
					if (jaxWorkJob.getExecutionId() != null) {
						jobExecution = jobOperator.getJobExecution(
							jaxWorkJob.getExecutionId()
						);
						jaxWorkJob.setExecutionRelevantValue(
							jobExecution
						);
					}
				} catch (NoSuchJobExecutionException ex) {
					//ignroe, not a problem
				}
			})
			.collect(Collectors.toList());

		return Response.ok(new JaxBatchJobList(jobList)).build();
	}
}
