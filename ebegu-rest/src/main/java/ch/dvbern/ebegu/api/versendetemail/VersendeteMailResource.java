package ch.dvbern.ebegu.api.versendetemail;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxVersendeteMailConverter;
import ch.dvbern.ebegu.api.dtos.JaxPaginationDTO;
import ch.dvbern.ebegu.api.dtos.JaxVersendeteMail;
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
		@Nonnull @QueryParam("active") String active,
		@Nonnull @QueryParam("filter") String filter,
		@Nonnull @QueryParam("direction") String sortDirection,
		@Nonnull @QueryParam("page") Integer pageIndex,
		@Nonnull @QueryParam("size") Integer pageSize
	) {
		List<JaxVersendeteMail> mails = versendeteMailsService.getAll(
			active,
			filter,
			sortDirection,
			pageIndex,
			pageSize
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
			versendeteMailsService.countVerendeteMails(filter)
		);

		return paginatedDTO;
	}
}
