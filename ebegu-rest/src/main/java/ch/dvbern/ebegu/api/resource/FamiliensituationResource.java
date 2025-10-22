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

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.gesuch.JaxFamiliensituationConverter;
import ch.dvbern.ebegu.api.dtos.JaxFamiliensituationContainer;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.resource.util.ResourceHelper;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.FamiliensituationService;
import ch.dvbern.ebegu.services.GesuchService;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Resource fuer Familiensituation
 */
@Path("familiensituation")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class FamiliensituationResource {

	@Inject
	private FamiliensituationService familiensituationService;
	@Inject
	private GesuchService gesuchService;

	@Inject
	private JaxFamiliensituationConverter converter;

	@Inject
	private ResourceHelper resourceHelper;

	@Operation(
		summary = "Speichert eine Familiensituation in der Datenbank"
	)
	@Nullable
	@PUT
	@Path("/adapt-and-save/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
			SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
			SACHBEARBEITER_SOZIALDIENST, ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION,
			ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT }
	)
	public JaxFamiliensituationContainer saveFamiliensituationAndHandleChange(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId,
		@Nonnull
		@NotNull JaxFamiliensituationContainer familiensituationContainerJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Gesuch gesuch = getGesuchAndAssertStatus(gesuchJAXPId.getId());

		FamiliensituationContainer familiensituationContainerToMerge =
			getFamilienSituationToMerge(familiensituationContainerJAXP.getId());
		Familiensituation oldFamiliensituation =
			familiensituationContainerToMerge.isNew() ?
				null :
				new Familiensituation(
					familiensituationContainerToMerge
						.extractFamiliensituation()
				);

		FamiliensituationContainer convertedFamiliensituation = converter
			.familiensituationContainerToEntity(
				familiensituationContainerJAXP,
				familiensituationContainerToMerge
			);

		return converter.familiensituationContainerToJAX(
			familiensituationService.saveFamiliensituationAndHandleChange(
				gesuch,
				convertedFamiliensituation,
				oldFamiliensituation
			)
		);
	}

	@Operation(
		summary = "Speichert eine Familiensituation in der Datenbank"
	)
	@Nullable
	@PUT
	@Path("/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(
		{ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
			SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
			SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
			SACHBEARBEITER_SOZIALDIENST, ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION,
			ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT }
	)
	public JaxFamiliensituationContainer saveFamiliensituation(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId,
		@Nonnull
		@NotNull JaxFamiliensituationContainer familiensituationContainerJAXP

	) {
		Gesuch gesuch = getGesuchAndAssertStatus(gesuchJAXPId.getId());

		FamiliensituationContainer familiensituationContainerToMerge =
			getFamilienSituationToMerge(familiensituationContainerJAXP.getId());
		Familiensituation oldFamiliensituation =
			familiensituationContainerToMerge.isNew() ?
				null :
				new Familiensituation(
					familiensituationContainerToMerge
						.extractFamiliensituation()
				);

		FamiliensituationContainer convertedFamiliensituation = converter
			.familiensituationContainerToEntity(
				familiensituationContainerJAXP,
				familiensituationContainerToMerge
			);

		return converter.familiensituationContainerToJAX(
			familiensituationService.saveFamiliensituation(
				gesuch,
				convertedFamiliensituation,
				oldFamiliensituation
			)
		);
	}

	private FamiliensituationContainer getFamilienSituationToMerge(
		@Nullable String familiensituationContainerId
	) {
		FamiliensituationContainer familiensituationContainerToMerge =
			new FamiliensituationContainer();

		if (familiensituationContainerId != null) {
			Optional<FamiliensituationContainer> loadedFamiliensituation =
				this.familiensituationService
					.findFamiliensituation(
						familiensituationContainerId
					);
			if (loadedFamiliensituation.isPresent()) {
				familiensituationContainerToMerge = loadedFamiliensituation
					.get();
			}
		}
		return familiensituationContainerToMerge;
	}

	private Gesuch getGesuchAndAssertStatus(String gesuchId) {
		Gesuch gesuch = gesuchService.findGesuch(gesuchId)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"saveFamiliensituation",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					gesuchId
				)
			);

		resourceHelper.assertGesuchStatusForBenutzerRole(gesuch);
		return gesuch;
	}
}
