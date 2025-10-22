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

import java.util.Collection;
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
import jakarta.servlet.http.HttpServletResponse;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.institution.JaxTraegerschaftConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxTraegerschaft;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.TraegerschaftService;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Traegerschaft
 */
@Path("traegerschaften")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class TraegerschaftResource {

	@Inject
	private TraegerschaftService traegerschaftService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private JaxTraegerschaftConverter converter;

	@Operation(summary = "Erstellt eine neue Traegerschaft in der Datenbank")
	@Nullable
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public JaxTraegerschaft createTraegerschaft(
		@Nonnull @NotNull @Valid JaxTraegerschaft jaxTraegerschaft,
		@Nonnull @NotNull @Valid @QueryParam("adminMail") String adminMail,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Traegerschaft traegerschaft = converter.traegerschaftToEntity(
			jaxTraegerschaft,
			new Traegerschaft()
		);
		return converter.traegerschaftToJAX(
			traegerschaftService.createTraegerschaft(
				traegerschaft,
				adminMail
			)
		);
	}

	@Operation(
		summary = "Speichert eine bestehende Traegerschaft in der Datenbank")
	@Nullable
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT })
	public JaxTraegerschaft saveTraegerschaft(
		@Nonnull @NotNull @Valid JaxTraegerschaft traegerschaftJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Objects.requireNonNull(traegerschaftJAXP);
		Objects.requireNonNull(traegerschaftJAXP.getId());

		// Diese Methode darf nur fuer eine existierende Traegerschaft verwendet werden. Zum neu Erstellen muss eine
		// Einladung über #createTraegerschaft() erfolgen
		Traegerschaft traegerschaft = traegerschaftService.findTraegerschaft(
			traegerschaftJAXP.getId()
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					traegerschaftJAXP.getId()
				)
			);
		Traegerschaft convertedTraegerschaft = converter.traegerschaftToEntity(
			traegerschaftJAXP,
			traegerschaft
		);
		Traegerschaft persistedTraegerschaft = this.traegerschaftService
			.saveTraegerschaft(convertedTraegerschaft);
		return converter.traegerschaftToJAX(persistedTraegerschaft);
	}

	@Operation(
		summary = "Gibt die Traegerschaft mit der uebergebenen id zurueck.")
	@Nullable
	@GET
	@Path("/id/{traegerschaftId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public JaxTraegerschaft findTraegerschaft(
		@Nonnull
		@NotNull
		@PathParam("traegerschaftId") JaxId traegerschaftJAXPId
	) {

		Objects.requireNonNull(traegerschaftJAXPId.getId());
		String traegerschaftID = converter.toEntityId(traegerschaftJAXPId);
		Optional<Traegerschaft> optional = traegerschaftService
			.findTraegerschaft(traegerschaftID);

		return optional.map(
			traegerschaft -> converter.traegerschaftToJAX(traegerschaft)
		).orElse(null);
	}

	@Operation(
		summary = "Loescht die Traegerschaft mit der uebergebenen id aus der DB. Die dazu gehoerenden Institutionen werden nicht geloescht")
	@Nullable
	@DELETE
	@Path("/{traegerschaftId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Response removeTraegerschaft(
		@Nonnull
		@NotNull
		@PathParam("traegerschaftId") JaxId traegerschaftJAXPId,
		@Context HttpServletResponse response
	) {

		Objects.requireNonNull(traegerschaftJAXPId.getId());
		final String traegerschaftId = converter.toEntityId(
			traegerschaftJAXPId
		);

		Collection<Institution> allInstitutionen = institutionService
			.getAllInstitutionenFromTraegerschaft(traegerschaftId);

		// set null Traegerschaft
		for (Institution institution : allInstitutionen) {
			institution.setTraegerschaft(null);
			institutionService.updateInstitution(institution);
		}

		traegerschaftService.removeTraegerschaft(traegerschaftId);

		return Response.ok().build();
	}

	@Operation(summary = "Gibt alle Traegerschaften zurueck.")
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<JaxTraegerschaft> getAllTraegerschaften() {
		return traegerschaftService.getAllTraegerschaften()
			.stream()
			.map(
				traegerschaft -> converter.traegerschaftToJAX(
					traegerschaft
				)
			)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Find and return a list of all active Traegerschaften. An active Traegerschaft is a "
			+
			"Traegerschaft where the active flag is true. Result will be ordered by name")
	@Nonnull
	@GET
	@Path("/active")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll
	public List<JaxTraegerschaft> getAllActiveTraegerschaften() {
		return traegerschaftService.getAllActiveTraegerschaften()
			.stream()
			.map(
				traegerschaft -> converter.traegerschaftToJAX(
					traegerschaft
				)
			)
			.collect(Collectors.toList());
	}
}
