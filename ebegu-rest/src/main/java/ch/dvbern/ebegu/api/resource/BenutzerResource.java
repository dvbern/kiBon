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

package ch.dvbern.ebegu.api.resource;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.api.converter.JaxBenutzerConverter;
import ch.dvbern.ebegu.api.dtos.JaxBenutzer;
import ch.dvbern.ebegu.api.dtos.JaxBenutzerNoDetails;
import ch.dvbern.ebegu.api.dtos.JaxBenutzerSearchresultDTO;
import ch.dvbern.ebegu.api.dtos.JaxBerechtigungHistory;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.filter.suchfilter.smarttable.BenutzerTableFilterDTO;
import ch.dvbern.ebegu.dto.filter.suchfilter.smarttable.BenutzerTableMandantFilterDTO;
import ch.dvbern.ebegu.dto.filter.suchfilter.smarttable.PaginationDTO;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.EntityExistsException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.CreateBenutzerService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.SuperAdminService;
import ch.dvbern.ebegu.services.TraegerschaftService;
import ch.dvbern.ebegu.util.mandant.MandantCookieUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_FERIENBETREUUNG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_FERIENBETREUUNG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.STEUERAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * REST Resource fuer Benutzer (Auf client userRS.rest.ts also eigentlich die UserResources)
 */
@Path("benutzer")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class BenutzerResource {

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private CreateBenutzerService createBenutzerService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private SuperAdminService superAdminService;

	@Inject
	private JaxBenutzerConverter converter;

	@Inject
	private TraegerschaftService traegerschaftService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private MandantService mandantService;

	@Inject
	private PrincipalBean principalBean;

	@Operation(
		summary = "Erstellt einen Benutzer mit Status EINGELADEN und sendet ihm eine E-Mail"
	)
	@POST
	@Path("/einladen")
	@RolesAllowed(
		{
			SUPER_ADMIN,
			ADMIN_BG,
			ADMIN_GEMEINDE,
			ADMIN_TS,
			ADMIN_MANDANT,
			ADMIN_INSTITUTION,
			ADMIN_TRAEGERSCHAFT,
			ADMIN_FERIENBETREUUNG,
			ADMIN_SOZIALDIENST
		}
	)
	public JaxBenutzer einladen(@NotNull @Valid JaxBenutzer benutzerParam) {
		Mandant mandant = principalBean.getMandant();

		Benutzer benutzer = converter.jaxBenutzerToBenutzer(
			benutzerParam,
			new Benutzer()
		);

		if (benutzerService.findBenutzer(benutzer.getUsername(), mandant)
			.isPresent()) {
			throw new EntityExistsException(
				KibonLogLevel.INFO,
				Benutzer.class,
				"email",
				benutzer.getEmail(),
				ErrorCodeEnum.ERROR_BENUTZER_EXISTS
			);
		}

		benutzer = createBenutzerService.createKeycloakAccount(benutzer);
		return converter.benutzerToJaxBenutzer(
			benutzerService.einladen(
				Einladung.forMitarbeiter(benutzer),
				mandant
			)
		);
	}

	@Operation(
		summary = "Sendet einem Benutzer im Status EINGELADEN erneut den Einladungslink"
	)
	@POST
	@Path("/erneutEinladen")
	@RolesAllowed(SUPER_ADMIN)
	public Response erneutEinladen(@NotNull @Valid JaxBenutzer benutzerParam) {
		Mandant mandant = requireNonNull(principalBean.getMandant());
		Benutzer benutzer = benutzerService.findBenutzer(
			benutzerParam.getEmail(),
			mandant
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"erneutEinladen",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					benutzerParam.getEmail()
				)
			);
		benutzerService.erneutEinladen(benutzer);
		return Response.ok().build();
	}

	@Operation(
		summary = "Gibt alle existierenden Benutzer mit Rolle ADMIN_BG, SACHBEARBEITER_BG, "
			+ "ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE zurueck"
	)
	@Nonnull
	@GET
	@Path("/BgOrGemeinde/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT, JURIST,
			REVISOR, STEUERAMT, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_MANDANT,
			SACHBEARBEITER_MANDANT,
			ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST,
			ADMIN_FERIENBETREUUNG, SACHBEARBEITER_FERIENBETREUUNG }
	)
	public List<JaxBenutzerNoDetails> getAllBenutzerBgOrGemeinde() {
		return benutzerService.getAllBenutzerBgOrGemeinde()
			.stream()
			.map(converter::benutzerToJaxBenutzerNoDetails)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Gibt alle existierenden Benutzer mit Rolle ADMIN_BG, SACHBEARBEITER_BG, "
			+ "ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE zurueck"
	)
	@Nonnull
	@GET
	@Path("/BgOrGemeinde/active/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT, JURIST,
			REVISOR, STEUERAMT, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_MANDANT,
			SACHBEARBEITER_MANDANT }
	)
	public List<JaxBenutzerNoDetails> getActiveBenutzerBgOrGemeindeForGemeinde(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId
	) {

		Objects.requireNonNull(gemeindeJAXPId.getId());
		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					gemeindeId
				)
			);

		return benutzerService.getActiveBenutzerBgOrGemeinde(gemeinde)
			.stream()
			.map(converter::benutzerToJaxBenutzerNoDetails)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Gibt alle existierenden Benutzer mit Rolle ADMIN_BG, SACHBEARBEITER_BG, "
			+ "ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS zurueck"
	)
	@Nonnull
	@GET
	@Path("/TsBgOrGemeinde/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE,
			SACHBEARBEITER_TS, ADMIN_TS }
	)
	public List<JaxBenutzer> getBenutzerTsBgOrGemeindeForGemeinde(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId
	) {

		Objects.requireNonNull(gemeindeJAXPId.getId());
		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					gemeindeId
				)
			);

		return benutzerService.getBenutzerTsBgOrGemeinde(gemeinde)
			.stream()
			.map(converter::benutzerToJaxBenutzer)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Gibt alle existierenden Benutzer mit Rolle ADMIN_BG, SACHBEARBEITER_BG, "
			+ "ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE zurueck"
	)
	@Nonnull
	@GET
	@Path("/BgTsOrGemeinde/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT, JURIST,
			REVISOR, STEUERAMT, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_MANDANT,
			SACHBEARBEITER_MANDANT,
			ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST }
	)
	public List<JaxBenutzerNoDetails> getAllBenutzerBgTsOrGemeinde() {
		return benutzerService.getAllBenutzerBgTsOrGemeinde()
			.stream()
			.map(converter::benutzerToJaxBenutzerNoDetails)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Gibt alle existierenden Benutzer mit Rolle SACHBEARBEITER_MANDANT und ADMIN_MANDANT für den Mandanten zurueck"
	)
	@Nonnull
	@GET
	@Path("/mandant/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public List<JaxBenutzerNoDetails> getAllBenutzerMandant() {
		Mandant mandant = principalBean.getMandant();

		return benutzerService.getAllActiveBenutzerMandant(mandant)
			.stream()
			.map(converter::benutzerToJaxBenutzerNoDetails)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Gibt alle existierenden Benutzer mit Rolle ADMIN_BG, SACHBEARBEITER_BG, "
			+ "ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE zurueck"
	)
	@Nonnull
	@GET
	@Path("/TsOrGemeinde/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT, JURIST,
			REVISOR, STEUERAMT, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_MANDANT,
			SACHBEARBEITER_MANDANT,
			ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST,
			ADMIN_FERIENBETREUUNG, SACHBEARBEITER_FERIENBETREUUNG }
	)
	public List<JaxBenutzerNoDetails> getAllBenutzerTsOrGemeinde() {
		return benutzerService.getAllBenutzerTsOrGemeinde()
			.stream()
			.map(converter::benutzerToJaxBenutzerNoDetails)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Gibt alle aktiven existierenden Benutzer mit Rolle ADMIN_BG, SACHBEARBEITER_BG, "
			+ "ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE zurueck"
	)
	@Nonnull
	@GET
	@Path("/TsOrGemeinde/active/{gemeindeId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT, JURIST,
			REVISOR, STEUERAMT, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_MANDANT,
			SACHBEARBEITER_MANDANT }
	)
	public List<JaxBenutzerNoDetails> getActiveBenutzerTsOrGemeindeForGemeinde(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJAXPId
	) {

		Objects.requireNonNull(gemeindeJAXPId.getId());
		String gemeindeId = converter.toEntityId(gemeindeJAXPId);
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					gemeindeId
				)
			);

		return benutzerService.getActiveBenutzerTsOrGemeinde(gemeinde)
			.stream()
			.map(converter::benutzerToJaxBenutzerNoDetails)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Gibt alle existierenden Benutzer mit Rolle Gesuchsteller zurueck"
	)
	@Nonnull
	@GET
	@Path("/gesuchsteller")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public List<JaxBenutzerNoDetails> getGesuchsteller() {
		requireNonNull(principalBean.getMandant());
		return benutzerService.getGesuchsteller(principalBean.getMandant())
			.stream()
			.map(converter::benutzerToJaxBenutzerNoDetails)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Sucht Benutzer mit den uebergebenen Suchkriterien/Filtern."
	)
	@Nonnull
	@POST
	@Path("/search")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE,
			ADMIN_INSTITUTION, ADMIN_TRAEGERSCHAFT,
			ADMIN_MANDANT, ADMIN_FERIENBETREUUNG, REVISOR, ADMIN_SOZIALDIENST }
	)
	public JaxBenutzerSearchresultDTO searchBenutzer(
		@Nonnull @NotNull @Valid BenutzerTableFilterDTO benutzerSearch
	) {

		Pair<Long, List<Benutzer>> searchResultPair =
			benutzerService.searchBenutzer(
				new BenutzerTableMandantFilterDTO(
					benutzerSearch,
					requireNonNull(
						principalBean.getMandant()
					)
				),
				false
			);
		List<Benutzer> foundBenutzer = searchResultPair.getRight();

		List<JaxBenutzer> benutzerDTOList = foundBenutzer.stream()
			.map(converter::benutzerToJaxBenutzer)
			.collect(Collectors.toList());

		return buildResultDTO(
			benutzerSearch,
			searchResultPair,
			benutzerDTOList
		);
	}

	@Nonnull
	private JaxBenutzerSearchresultDTO buildResultDTO(
		@Nonnull BenutzerTableFilterDTO benutzerSearch,
		Pair<Long, List<Benutzer>> searchResultPair,
		List<JaxBenutzer> benutzerDTOList
	) {

		JaxBenutzerSearchresultDTO resultDTO = new JaxBenutzerSearchresultDTO();
		resultDTO.setBenutzerDTOs(benutzerDTOList);
		PaginationDTO pagination = benutzerSearch.getPagination();
		requireNonNull(pagination).setTotalItemCount(
			searchResultPair.getLeft()
		);
		resultDTO.setPaginationDTO(pagination);

		return resultDTO;
	}

	@Operation(
		summary = "Sucht den Benutzer mit dem uebergebenen Username in der Datenbank."
	)
	@Nullable
	@GET
	@Path("/username/{username}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public JaxBenutzer findBenutzerByUsername(
		@Nonnull @NotNull @PathParam("username") String username,
		@Context HttpServletRequest request
	) {
		MandantIdentifier mandantIdentifier =
			MandantCookieUtil.getMandantFromCookie(request);
		var mandant = mandantService.findMandantByIdentifier(mandantIdentifier)
			.orElseThrow();

		Optional<Benutzer> benutzerOptional = benutzerService.findBenutzer(
			username,
			mandant
		);
		benutzerOptional.ifPresent(
			benutzer -> authorizer.checkReadAuthorization(benutzer)
		);

		return benutzerOptional.map(converter::benutzerToJaxBenutzer)
			.orElse(null);
	}

	@Operation(summary = "Sucht den Benutzer anhand der ID in der Datenbank.")
	@Nullable
	@GET
	@Path("/id/{id}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public JaxBenutzer findBenutzerById(
		@Nonnull @NotNull @PathParam("id") String id
	) {
		Optional<Benutzer> benutzerOptional = benutzerService.findBenutzerById(
			id
		);
		benutzerOptional.ifPresent(
			benutzer -> authorizer.checkReadAuthorization(benutzer)
		);

		return benutzerOptional.map(converter::benutzerToJaxBenutzer)
			.orElse(null);
	}

	@Operation(summary = "Inactivates a Benutzer in the database")
	@Nullable
	@PUT
	@Path("/inactivate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE,
			ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
			ADMIN_MANDANT, ADMIN_FERIENBETREUUNG, ADMIN_SOZIALDIENST }
	)
	public JaxBenutzer inactivateBenutzer(
		@Nonnull @NotNull @Valid JaxBenutzer benutzerJax
	) {

		Benutzer benutzer = benutzerService.sperren(
			benutzerJax.getUsername(),
			requireNonNull(principalBean.getMandant())
		);
		return converter.benutzerToJaxBenutzer(benutzer);
	}

	@Operation(summary = "Reactivates a Benutzer in the database")
	@Nullable
	@PUT
	@Path("/reactivate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE,
			ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
			ADMIN_MANDANT, ADMIN_FERIENBETREUUNG, ADMIN_SOZIALDIENST }
	)
	public JaxBenutzer reactivateBenutzer(
		@Nonnull @NotNull @Valid JaxBenutzer benutzerJax
	) {

		Benutzer benutzer = benutzerService.reaktivieren(
			benutzerJax.getUsername(),
			requireNonNull(principalBean.getMandant())
		);
		return converter.benutzerToJaxBenutzer(benutzer);
	}

	@Operation(summary = "Updates a Benutzer in the database")
	@Nullable
	@PUT
	@Path("/saveBenutzerBerechtigungen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE,
			ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
			ADMIN_MANDANT, ADMIN_FERIENBETREUUNG, ADMIN_SOZIALDIENST }
	)
	public JaxBenutzer saveBenutzerBerechtigungen(
		@Nonnull @NotNull @Valid JaxBenutzer benutzerJax
	) {

		String username = benutzerJax.getUsername();
		Benutzer benutzer = benutzerService.findBenutzer(
			username,
			principalBean.getMandant()
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"saveBenutzerBerechtigungen",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					username
				)
			);

		authorizer.checkWriteAuthorization(benutzer);

		String fallId = benutzerService
			.findFallIdIfBenutzerIsGesuchstellerWithoutFreigegebenemGesuch(
				benutzer
			);
		// Keine Exception: Es ist kein Gesuchsteller: Wir können immer löschen
		return saveBenutzerBerechtigungenForced(benutzer, benutzerJax, fallId);
	}

	@Nonnull
	private JaxBenutzer saveBenutzerBerechtigungenForced(
		@Nonnull Benutzer benutzerFromDB,
		@Nonnull JaxBenutzer benutzerJax,
		@Nullable String fallId
	) {
		boolean currentBerechtigungChanged = hasCurrentBerechtigungChanged(
			benutzerJax,
			benutzerFromDB
		);
		if (fallId != null) {
			superAdminService.removeFallIfExists(fallId);
		}

		Benutzer mergedBenutzer = benutzerService.saveBenutzerBerechtigungen(
			converter.jaxBenutzerToBenutzer(benutzerJax, benutzerFromDB),
			currentBerechtigungChanged
		);

		return converter.benutzerToJaxBenutzer(mergedBenutzer);
	}

	private boolean hasCurrentBerechtigungChanged(
		@Nonnull JaxBenutzer jaxBenutzerNew,
		@Nonnull Benutzer benutzerOld
	) {

		JaxBenutzer jaxBenutzerOld = converter.benutzerToJaxBenutzer(
			benutzerOld
		);
		jaxBenutzerOld.evaluateCurrentBerechtigung();
		jaxBenutzerNew.evaluateCurrentBerechtigung();
		requireNonNull(jaxBenutzerOld.getCurrentBerechtigung());
		requireNonNull(jaxBenutzerNew.getCurrentBerechtigung());

		return !jaxBenutzerOld.getCurrentBerechtigung()
			.isSame(jaxBenutzerNew.getCurrentBerechtigung());
	}

	@Operation(
		summary = "Gibt alle BerechtigungHistory Einträge des übergebenen Benutzers zurück"
	)
	@Nonnull
	@GET
	@Path("/berechtigunghistory/{username}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN })
	public List<JaxBerechtigungHistory> getBerechtigungHistoriesForBenutzer(
		@Nonnull @NotNull @PathParam("username") String username
	) {

		Benutzer benutzer = benutzerService.findBenutzer(
			username,
			principalBean.getMandant()
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"getBerechtigungHistoriesForBenutzer",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					"username invalid: " + username
				)
			);

		return benutzerService.getBerechtigungHistoriesForBenutzer(benutzer)
			.stream()
			.map(history -> converter.berechtigungHistoryToJax(history))
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Gibt true zurueck, wenn der uebergebenen Benutzer in irgendeiner Gemeinde als "
			+ "Defaultbenutzer gesetzt ist"
	)
	@GET
	@Path("/isdefaultuser/{username}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public boolean isBenutzerDefaultBenutzerOfAnyGemeinde(
		@Nonnull @NotNull @PathParam("username") String username
	) {

		return benutzerService.isBenutzerDefaultBenutzerOfAnyGemeinde(username);
	}

	@Operation(summary = "Löscht der Benutzer mit dem gegebenen Benutzername.")
	@DELETE
	@Path("/delete/{username}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response deleteBenutzer(
		@Nonnull @NotNull @PathParam("username") String username
	) {

		Benutzer eingeloggterBenutzer = benutzerService.getCurrentBenutzer()
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"deleteBenutzer",
					"No User is logged in"
				)
			);

		superAdminService.removeFallAndBenutzer(username, eingeloggterBenutzer);
		return Response.ok().build();
	}

	@Operation(summary = "Gibt alle Admin E-Mails einer Trägerschaft zurück.")
	@GET
	@Path("/mailAdminTraegerschaft/{traegerschaftId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
			ADMIN_TRAEGERSCHAFT,
			ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS, SACHBEARBEITER_GEMEINDE,
			SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS }
	)
	public List<String> getAllEmailAdminForTraegerschaft(
		@Nonnull
		@NotNull
		@PathParam("traegerschaftId") String traegerschaftId
	) {
		Traegerschaft traegerschaft = traegerschaftService.findTraegerschaft(
			traegerschaftId
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"getAllEmailAdminForTraegerschaft",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND
				)
			);

		return benutzerService.getTraegerschaftAdministratoren(traegerschaft)
			.stream()
			.map(Benutzer::getEmail)
			.collect(Collectors.toList());
	}

	/**
	 * Triggers the update password action via Keycloak API for the user currently logged in.
	 * This user will then receive an e-mail with instructions on how to change his password.
	 * If this API is called with no existing, validly authenticated user, an {@link EbeguRuntimeException}
	 * will be thrown.
	 * The update password action is not available for users of mandant Berne. This is because Berne
	 * uses an external IDP. If the currently authenticated user is of mandant Berne, an {@link EbeguRuntimeException}
	 * will be thrown, too.
	 *
	 * @return An empty response with HTTP response code 200 - if the update password action has been
	 * successfully initiated - an apropriate error response, especially for the cases described above, otherwise.
	 */
	@Operation(
		summary = "Sendet eine E-Mail mit einem Passwort-Ändern-Link an den, in dieser Session angemeldeten Benutzer.")
	@GET
	@Path("/updatePassword")
	@Consumes(MediaType.WILDCARD)
	@PermitAll
	public Response updatePassword() {

		Benutzer eingeloggterBenutzer = benutzerService.getCurrentBenutzer()
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"changePassword",
					"User not found or user not logged in"
				)
			);

		if (MandantIdentifier.BERN
			== eingeloggterBenutzer.getMandant().getMandantIdentifier()) {
			throw new EbeguRuntimeException(
				"changePassword",
				"The update password action is not available for users of mandant Berne."
			);
		}

		benutzerService.sendUpdatePasswordEmail(eingeloggterBenutzer);

		return Response.ok().build();
	}
}
