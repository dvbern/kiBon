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

package ch.dvbern.ebegu.api.resource;

import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.services.BatchjobHistoryService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBatchjobHistoryConverter;
import ch.dvbern.ebegu.api.dtos.JaxBatchjobHistory;
import ch.dvbern.ebegu.api.dtos.JaxPaginationDTO;
import ch.dvbern.ebegu.batchjobhistory.BatchjobHistorySearchParams;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Path("batchjobHistory")
public class BatchjobHistoryResource {

	@Inject
	private BatchjobHistoryService batchjobHistoryService;

	@Inject
	private JaxBatchjobHistoryConverter converter;

	@Nonnull
	@GET
	@Path("/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public JaxPaginationDTO<JaxBatchjobHistory> getAll(
		@Valid @BeanParam BatchjobHistorySearchParams params
	) {
		List<JaxBatchjobHistory> entries = batchjobHistoryService.getAll(params)
			.stream()
			.map(converter::batchjobHistoryToJax)
			.toList();

		JaxPaginationDTO<JaxBatchjobHistory> paginatedDTO =
			new JaxPaginationDTO<>();
		paginatedDTO.setResultList(entries);
		paginatedDTO.setTotalCount(
			batchjobHistoryService.countBatchjobHistory(params)
		);
		return paginatedDTO;
	}
}
