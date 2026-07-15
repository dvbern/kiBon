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

package ch.dvbern.ebegu.mailing;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.QueryParam;

import ch.dvbern.ebegu.entities.VersendeteMail;
import ch.dvbern.ebegu.validators.params.FieldOfClass;
import ch.dvbern.ebegu.validators.params.ValidLocalDateTime;
import lombok.Getter;

@Getter
public class VersendeteMailSearchParams {
	@NotNull
	@QueryParam("active")
	@FieldOfClass(targetClass = VersendeteMail.class)
	private String active;

	@QueryParam("endDate")
	@ValidLocalDateTime
	private String endDate;

	@NotNull
	@QueryParam("receiverOrSubject")
	private String receiverOrSubject;

	@QueryParam("startDate")
	@ValidLocalDateTime
	private String startDate;

	@NotNull
	@QueryParam("direction")
	@Pattern(regexp = "asc|desc",
		message = "versendetemail.sortdirection.invalid")
	private String sortDirection;

	@NotNull
	@QueryParam("page")
	@Min(0)
	private Integer pageIndex;

	@NotNull
	@QueryParam("size")
	@Min(1)
	private Integer pageSize;

}
