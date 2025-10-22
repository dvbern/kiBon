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

package ch.dvbern.ebegu.api.meldungsfenster.resource;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.annotation.Nonnull;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.meldungsfenster.dto.JaxMeldungsfenster;
import ch.dvbern.ebegu.api.meldungsfenster.dto.converter.JaxMeldungsfensterConverter;
import ch.dvbern.ebegu.api.meldungsfenster.service.MeldungsfensterServiceBean;
import ch.dvbern.ebegu.api.resource.AdresseResource;
import ch.dvbern.ebegu.entities.meldungsfenster.Meldungsfenster;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Path("/meldungsfenster")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.WILDCARD)
@Stateless
@DenyAll
public class MeldungsfensterResource {

	@Inject
	private MeldungsfensterServiceBean meldungsfensterServiceBean;

	@Inject
	private JaxMeldungsfensterConverter converter;

	@GET
	@Path("/{id}")
	@RolesAllowed(SUPER_ADMIN)
	public JaxMeldungsfenster getMeldungsfensterById(
		@PathParam("id") String id
	) {
		Meldungsfenster meldungsfenster = meldungsfensterServiceBean
			.findMeldungsfenster(id);
		return converter.meldungsfensterToJax(meldungsfenster);
	}

	@POST
	@RolesAllowed(SUPER_ADMIN)
	public Response createMeldungsfenster(
		@Valid JaxMeldungsfenster jaxMeldungsfenster,
		@Context UriInfo uriInfo
	) {
		Meldungsfenster meldungsfensterToPersist =
			converter.jaxMeldungsfensterToEntity(
				jaxMeldungsfenster,
				new Meldungsfenster()
			);

		Meldungsfenster persistedMeldungsfenster =
			meldungsfensterServiceBean.createMeldungsfenster(
				meldungsfensterToPersist
			);

		URI uri = uriInfo.getBaseUriBuilder()
			.path(AdresseResource.class)
			.path('/' + persistedMeldungsfenster.getId())
			.build();
		return Response.created(uri)
			.entity(converter.meldungsfensterToJax(persistedMeldungsfenster))
			.build();
	}

	@PUT
	@RolesAllowed(SUPER_ADMIN)
	public Response updateMeldungsfenster(
		@Valid @Nonnull JaxMeldungsfenster updatedJaxMeldungsfenster
	) {
		Objects.requireNonNull(updatedJaxMeldungsfenster.getId());
		Meldungsfenster existingMeldungsfenster =
			meldungsfensterServiceBean.findMeldungsfenster(
				updatedJaxMeldungsfenster.getId()
			);
		Meldungsfenster meldungsfensterToMerge =
			converter.jaxMeldungsfensterToEntity(
				updatedJaxMeldungsfenster,
				existingMeldungsfenster
			);

		Meldungsfenster meldungsfenster = meldungsfensterServiceBean
			.updateMeldungsfenster(meldungsfensterToMerge);
		return Response.ok(converter.meldungsfensterToJax(meldungsfenster))
			.build();
	}

	@DELETE
	@Path("/{id}")
	@RolesAllowed(SUPER_ADMIN)
	public Response deleteMeldungsfenster(@PathParam("id") String id) {
		Meldungsfenster existingMeldungsfenster = meldungsfensterServiceBean
			.findMeldungsfenster(id);
		meldungsfensterServiceBean.delete(existingMeldungsfenster);
		return Response.noContent().build();
	}

	@GET
	@Path("/all")
	@RolesAllowed(SUPER_ADMIN)
	public List<JaxMeldungsfenster> getAllMeldungsfenster() {
		return meldungsfensterServiceBean.getAll()
			.stream()
			.map(
				meldungsfenster -> converter.meldungsfensterToJax(
					meldungsfenster
				)
			)
			.collect(Collectors.toList());
	}

	@GET
	@Path("/public/aktive")
	@PermitAll
	public List<JaxMeldungsfenster> getAktiveMeldungsfenster() {
		return meldungsfensterServiceBean.getActiveForBenutzer()
			.stream()
			.map(
				meldungsfenster -> converter.meldungsfensterToJax(
					meldungsfenster
				)
			)
			.collect(Collectors.toList());
	}
}
