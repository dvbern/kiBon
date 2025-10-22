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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBaseConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxMandant;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.services.MandantService;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * REST Resource fuer Mandanten
 */
@Path("mandanten")
@Stateless
@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
public class MandantResource {

	@Inject
	private MandantService mandantService;

	@Inject
	private JaxBaseConverter converter;

	@Operation(summary = "Gibt den Mandanten mit der angegebenen id zurueck.")
	@Nullable
	@GET
	@Path("/id/{mandantId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxMandant findMandant(
		@Nonnull @NotNull @PathParam("mandantId") JaxId mandantJAXPId
	) {
		Objects.requireNonNull(mandantJAXPId.getId());
		String mandantID = converter.toEntityId(mandantJAXPId);
		Optional<Mandant> optional = mandantService.findMandant(mandantID);

		return optional.map(mandant -> converter.mandantToJAX(mandant))
			.orElse(null);
	}

	@Operation(summary = "Gibt alle aktiven Mandanten zurueck.")
	@Nullable
	@GET
	@Path("/all")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<JaxMandant> findAllActive() {
		Collection<Mandant> all = mandantService.getAll();

		return all.stream()
			.filter(Mandant::isActivated)
			.map(mandant -> converter.mandantToJAX(mandant))
			.collect(Collectors.toList());
	}

}
