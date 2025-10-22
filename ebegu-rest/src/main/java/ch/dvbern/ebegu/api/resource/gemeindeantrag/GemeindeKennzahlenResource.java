/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource.gemeindeantrag;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.gemeindeantrag.JaxGemeindeKennzahlenConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.gemeindekennzahlen.JaxGemeindeKennzahlen;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.authentication.AuthorizerImpl;
import ch.dvbern.ebegu.services.gemeindeantrag.GemeindeKennzahlenService;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer die Ferienbetreuungen
 */
@Path("gemeindekennzahlen")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class GemeindeKennzahlenResource {

	@Inject
	private GemeindeKennzahlenService gemeindeKennzahlenService;

	@Inject
	private JaxGemeindeKennzahlenConverter converter;

	@Inject
	private AuthorizerImpl authorizer;

	@Operation(
		summary = "Gibt die GemeindeKennzahlen mit der uebergebenen Id zurueck")
	@Nullable
	@GET
	@Path("/{id}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT,
		ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG })
	public JaxGemeindeKennzahlen findGemeindeKennzahlen(
		@Nonnull @NotNull @PathParam("id") JaxId id
	) {
		Objects.requireNonNull(id);
		Objects.requireNonNull(id.getId());

		authorizer.checkReadAuthorization(id.getId());

		final Optional<GemeindeKennzahlen> gemeindeKennzahlenOpt =
			gemeindeKennzahlenService.findGemeindeKennzahlen(
				converter.toEntityId(id)
			);

		return gemeindeKennzahlenOpt
			.map(container -> converter.gemeindeKennzahlenToJax(container))
			.orElse(null);
	}

	@Operation(
		summary = "Schliesst den GemeindeKennzahlen-Antrag als Gemeinde ab und gibt ihn zur Prüfung durch die "
			+ "Kantone "
			+ "frei")
	@POST
	@Path("/{id}/abschliessen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG })
	public JaxGemeindeKennzahlen gemeindeKennzahlenAbschliessen(
		@Nonnull @Valid JaxGemeindeKennzahlen jaxStammdaten,
		@Nonnull @NotNull @PathParam("id") JaxId id
	) {
		Objects.requireNonNull(id);
		Objects.requireNonNull(id.getId());

		GemeindeKennzahlen gemeindeKennzahlen =
			gemeindeKennzahlenService.findGemeindeKennzahlen(id.getId())
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"gemeindeKennzahlenAbschliessen",
						id.getId()
					)
				);

		authorizer.checkWriteAuthorization(gemeindeKennzahlen);

		gemeindeKennzahlen = converter.gemeindeKennzahlenToEntity(
			jaxStammdaten,
			gemeindeKennzahlen
		);

		GemeindeKennzahlen persisted =
			gemeindeKennzahlenService.saveGemeindeKennzahlen(
				gemeindeKennzahlen
			);

		GemeindeKennzahlen abgeschlosseneKennzahlen = gemeindeKennzahlenService
			.gemeindeKennzahlenAbschliessen(persisted);

		return converter.gemeindeKennzahlenToJax(abgeschlosseneKennzahlen);
	}

	@Operation(
		summary = "Speichert den GemeindeKennzahlen-Antrag als Gemeinde ab")
	@POST
	@Path("/{id}/save")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG })
	public JaxGemeindeKennzahlen gemeindeKennzahlenSpeichern(
		@Nonnull @Valid JaxGemeindeKennzahlen jaxStammdaten,
		@Nonnull @NotNull @PathParam("id") JaxId id
	) {
		Objects.requireNonNull(jaxStammdaten.getId());
		Objects.requireNonNull(id.getId());

		GemeindeKennzahlen gemeindeKennzahlen =
			gemeindeKennzahlenService.findGemeindeKennzahlen(id.getId())
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"saveGemeindeKennzahlen",
						id.getId()
					)
				);

		authorizer.checkWriteAuthorization(gemeindeKennzahlen);

		gemeindeKennzahlen = converter.gemeindeKennzahlenToEntity(
			jaxStammdaten,
			gemeindeKennzahlen
		);

		GemeindeKennzahlen persisted =
			gemeindeKennzahlenService.saveGemeindeKennzahlen(
				gemeindeKennzahlen
			);

		return converter.gemeindeKennzahlenToJax(persisted);
	}

	@Operation(summary = "Gibt die Gemeinde Kennzahlen zurück an die Gemeinde")
	@PUT
	@Path("/{id}/zurueck-an-gemeinde")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, SACHBEARBEITER_MANDANT, ADMIN_MANDANT })
	public JaxGemeindeKennzahlen gemeindeKennzahlenZurueckAnDieGemeinde(
		@Nonnull @NotNull @PathParam("id") JaxId id
	) {
		Objects.requireNonNull(id);
		Objects.requireNonNull(id.getId());

		GemeindeKennzahlen gemeindeKennzahlen =
			gemeindeKennzahlenService.findGemeindeKennzahlen(id.getId())
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"gemeindeKennzahlenZurueckAnDieGemeinde",
						id.getId()
					)
				);

		authorizer.checkWriteAuthorization(gemeindeKennzahlen);

		GemeindeKennzahlen persisted =
			gemeindeKennzahlenService.gemeindeKennzahlenZurueckAnGemeinde(
				gemeindeKennzahlen
			);
		return converter.gemeindeKennzahlenToJax(persisted);
	}

}
