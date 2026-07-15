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

package ch.dvbern.ebegu.api.versendetemail;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxVersendeteMailConverter;
import ch.dvbern.ebegu.api.dtos.JaxPaginationDTO;
import ch.dvbern.ebegu.api.dtos.JaxVersendeteMail;
import ch.dvbern.ebegu.mailing.VersendeteMailSearchParams;
import ch.dvbern.ebegu.services.VersendeteMailsService;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Path("versendeteMails")
public class VersendeteMailResource {
	@Inject
	private VersendeteMailsService versendeteMailsService;

	@Inject
	private JaxVersendeteMailConverter converter;

	@Nonnull
	@GET
	@Path("/allMails")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public JaxPaginationDTO<JaxVersendeteMail> getAllMails(
		@BeanParam VersendeteMailSearchParams params
	) {
		List<JaxVersendeteMail> mails = versendeteMailsService.getAll(
			params
		)
			.stream()
			.map(
				versendeteMails -> converter.versendeteMailsToJax(
					versendeteMails
				)
			)
			.collect(Collectors.toList());

		JaxPaginationDTO<JaxVersendeteMail> paginatedDTO =
			new JaxPaginationDTO<>();
		paginatedDTO.setResultList(mails);
		paginatedDTO.setTotalCount(
			versendeteMailsService.countVersendeteMails(params)
		);

		return paginatedDTO;
	}
}
