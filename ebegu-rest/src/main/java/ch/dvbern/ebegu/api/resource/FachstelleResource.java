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
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.gesuch.JaxFachstelleConverter;
import ch.dvbern.ebegu.api.dtos.JaxFachstelle;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FachstelleName;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.FachstelleService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * REST Resource fuer Fachstellen
 */
@Path("fachstellen")
@Stateless
@PermitAll // Alles oeffentliche Daten
public class FachstelleResource {

	@Inject
	private FachstelleService fachstelleService;

	@Inject
	private JaxFachstelleConverter converter;

	@Inject
	GesuchsperiodeService gesuchsperiodeService;

	@Operation(summary = "Returns Anspruch Fachstellen")
	@Nonnull
	@GET
	@Path("/anspruch")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxFachstelle> getAnspruchFachstellen(
		@NotNull @QueryParam("gesuchsperiodeId") String gesuchsperiodeId
	) {
		Gesuchsperiode gesuchsperiode = findGesuchsperiodeFromIdOrThrow(
			gesuchsperiodeId
		);
		return fachstelleService.getAllFachstellen(gesuchsperiode.getMandant())
			.stream()
			.filter(Fachstelle::isFachstelleAnspruch)
			.filter(
				fachstelle -> fachstelle.isGueltigForGesuchsperiode(
					gesuchsperiode
				)
			)
			.filter(
				fachstelle -> fachstelle.getName()
					!= FachstelleName.KINDES_ERWACHSENEN_SCHUTZBEHOERDE
			)
			.map(ap -> converter.fachstelleToJAX(ap))
			.collect(Collectors.toList());
	}

	@Operation(summary = "Returns erweiterte Betreuung Fachstellen")
	@Nonnull
	@GET
	@Path("/erweiterteBetreuung")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxFachstelle> getErweiterteBetreuungFachstellen(
		@NotNull @QueryParam("gesuchsperiodeId") String gesuchsperiodeId
	) {
		Gesuchsperiode gesuchsperiode = findGesuchsperiodeFromIdOrThrow(
			gesuchsperiodeId
		);
		return fachstelleService.getAllFachstellen(gesuchsperiode.getMandant())
			.stream()
			.filter(Fachstelle::isFachstelleErweiterteBetreuung)
			.filter(
				fachstelle -> fachstelle.isGueltigForGesuchsperiode(
					gesuchsperiode
				)
			)
			.filter(
				fachstelle -> fachstelle.getName()
					!= FachstelleName.KINDES_ERWACHSENEN_SCHUTZBEHOERDE
			)
			.map(ap -> converter.fachstelleToJAX(ap))
			.collect(Collectors.toList());
	}

	private Gesuchsperiode findGesuchsperiodeFromIdOrThrow(
		@NotNull String gesuchsperiodeId
	) {
		return gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId)
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"getAnspruchFachstellen",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					gesuchsperiodeId
				)
			);
	}
}
