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

package ch.dvbern.ebegu.batchjobhistory;

import java.util.Set;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.QueryParam;

import ch.dvbern.ebegu.entities.BatchjobHistory;
import ch.dvbern.ebegu.enums.BatchjobStatus;
import ch.dvbern.ebegu.validators.params.FieldOfClass;
import ch.dvbern.ebegu.validators.params.ValidLocalDateTime;
import lombok.Getter;

@Getter
public class BatchjobHistorySearchParams {
	@NotNull
	@QueryParam("active")
	@FieldOfClass(targetClass = BatchjobHistory.class)
	private String active;

	@NotNull
	@QueryParam("direction")
	@Pattern(regexp = "asc|desc",
		message = "{sortdirection.invalid}")
	private String sortDirection;

	@NotNull
	@QueryParam("page")
	@Min(0)
	private Integer pageIndex;

	@NotNull
	@QueryParam("size")
	@Min(1)
	private Integer pageSize;

	@QueryParam("nameOrResult")
	private String nameOrResult;

	/**
	 * Optional filter on the status. Multiple values are combined with OR, an empty set means no restriction.
	 */
	@QueryParam("status")
	private Set<BatchjobStatus> status;

	@QueryParam("startDate")
	@ValidLocalDateTime
	private String startDate;

	@QueryParam("endDate")
	@ValidLocalDateTime
	private String endDate;
}
