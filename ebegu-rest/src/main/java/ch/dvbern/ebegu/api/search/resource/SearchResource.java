/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.search.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.api.converter.gesuch.JaxAntragConverter;
import ch.dvbern.ebegu.api.dtos.JaxAntragSearchresultDTO;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.search.service.SearchServiceBean;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.JaxAntragDTO;
import ch.dvbern.ebegu.dto.filter.suchfilter.smarttable.AntragTableFilterDTO;
import ch.dvbern.ebegu.dto.filter.suchfilter.smarttable.PaginationDTO;
import ch.dvbern.ebegu.dto.pendenz.PendenzBetreuungDTO;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.InstitutionService;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Pendenzen
 */
@Path("search")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class SearchResource {

	@Inject
	private JaxAntragConverter converter;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private SearchServiceBean searchService;

	@Inject
	private DossierService dossierService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private InstitutionService institutionService;

	/**
	 * Gibt eine Liste mit allen Pendenzen des Jugendamtes zurueck. Sollte keine Pendenze gefunden werden oder ein
	 * Fehler
	 * passieren, wird eine leere Liste zurueckgegeben.
	 */
	@Operation(
		summary = "Gibt eine Liste mit allen Pendenzen des Jugendamtes zurueck"
	)
	@Nonnull
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/jugendamt")
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE,
			SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
			SACHBEARBEITER_SOZIALDIENST }
	)
	public Response getAllPendenzenJA(
		@Nonnull @NotNull AntragTableFilterDTO antragSearch
	) {

		List<Gesuch> foundAntraege = searchService.searchPendenzen(
			antragSearch
		);

		List<JaxAntragDTO> antragDTOList = convertAntraegeToDTO(
			foundAntraege
		);
		JaxAntragSearchresultDTO resultDTO = buildResultDTO(
			antragSearch,
			antragDTOList
		);
		return Response.ok(resultDTO).build();
	}

	/**
	 * Count allen Pendenzen des Jugendamtes zurueck.
	 */
	@Operation(
		summary = "Gibt der Count von allen Pendenzen des Jugendamtes zurueck"
	)
	@Nonnull
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/jugendamt/count")
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE,
			SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
			SACHBEARBEITER_SOZIALDIENST }
	)
	public Response countAllPendenzenJA(
		@Nonnull @NotNull AntragTableFilterDTO antragSearch
	) {

		Long count = searchService.countPendenzen(antragSearch);
		return Response.ok(count).build();
	}

	@Operation(
		summary = "Gibt eine Liste mit allen Betreuungen die pendent sind und zur Institution oder Traegerschaft des "
			+ "eingeloggten Benutzers gehoeren zurueck. "
			+ "Fuer das Schulamt werden alle SCH-Anmeldungen zurueckgegeben"
	)
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/pendenzenBetreuungen")
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT,
			SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION,
			ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS,
			ADMIN_TS }
	)
	public List<PendenzBetreuungDTO> getAllPendenzenBetreuungen() {
		return betreuungService
			.getPendenzenBetreuungen()
			.stream()
			.toList();
	}

	@Operation(
		summary = "Gibt alle Antraege des eingegebenen Dossiers fuer den eingeloggten Gesuchsteller zurueck."
	)
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/gesuchsteller/{dossierId}")
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxAntragDTO> getAllAntraegeOfDossier(
		@Nonnull @NotNull @PathParam("dossierId") JaxId dossierJAXPId
	) {
		Objects.requireNonNull(dossierJAXPId.getId());
		Dossier dossier = dossierService.findDossier(dossierJAXPId.getId())
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"getAllAntraegeOfDossier",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					dossierJAXPId.getId()
				)
			);

		List<Gesuch> antraege = gesuchService.getAntraegeOfDossier(dossier);
		final List<JaxAntragDTO> jaxAntragDTOS = new ArrayList<>();
		final UserRole userRole = principalBean.discoverMostPrivilegedRole();

		antraege.forEach(gesuch -> {
			final JaxAntragDTO jaxAntragDTO = converter.gesuchToAntragDTO(
				gesuch,
				userRole
			);
			jaxAntragDTOS.add(jaxAntragDTO);
		});

		return jaxAntragDTOS;

	}

	@Operation(
		summary = "Sucht Antraege mit den uebergebenen Suchkriterien/Filtern. Es werden nur Antraege zurueck"
			+ "gegeben, fuer die der eingeloggte Benutzer berechtigt ist."
	)
	@Nonnull
	@POST
	@Path("/search")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response searchAntraege(
		@Nonnull @NotNull AntragTableFilterDTO antragSearch
	) {

		List<Gesuch> foundAntraege = searchService
			.searchAllAntraege(antragSearch);

		List<JaxAntragDTO> antragDTOList = convertAntraegeToDTO(
			foundAntraege
		);
		JaxAntragSearchresultDTO resultDTO = buildResultDTO(
			antragSearch,
			antragDTOList
		);
		return Response.ok(resultDTO).build();
	}

	@Operation(
		summary = "Count Antraege mit den uebergebenen Suchkriterien/Filtern. Es werden nur Antraege zurueck"
			+ "gegeben, fuer die der eingeloggte Benutzer berechtigt ist."
	)
	@Nonnull
	@POST
	@Path("/search/count")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response countAntraege(
		@Nonnull @NotNull AntragTableFilterDTO antragSearch
	) {

		Long count = searchService.countAllAntraege(antragSearch);

		return Response.ok(count).build();

	}

	@Nonnull
	private List<JaxAntragDTO> convertAntraegeToDTO(
		List<Gesuch> foundAntraege
	) {
		Collection<Institution> allowedInst = institutionService
			.getInstitutionenReadableForCurrentBenutzer(false);

		List<JaxAntragDTO> antragDTOList = new ArrayList<>(
			foundAntraege.size()
		);
		foundAntraege.forEach(gesuch -> {
			JaxAntragDTO antragDTO =
				converter.gesuchToAntragDTO(
					gesuch,
					principalBean.discoverMostPrivilegedRole(),
					allowedInst
				);
			antragDTOList.add(antragDTO);
		});
		return antragDTOList;
	}

	@Nonnull
	private JaxAntragSearchresultDTO buildResultDTO(
		@Nonnull @NotNull AntragTableFilterDTO antragSearch,
		List<JaxAntragDTO> antragDTOList
	) {
		JaxAntragSearchresultDTO resultDTO = new JaxAntragSearchresultDTO();
		resultDTO.setAntragDTOs(antragDTOList);
		PaginationDTO pagination = antragSearch.getPagination();
		resultDTO.setPaginationDTO(pagination);
		return resultDTO;
	}
}
