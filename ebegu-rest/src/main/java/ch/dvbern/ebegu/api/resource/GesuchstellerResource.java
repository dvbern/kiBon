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

import java.util.Objects;
import java.util.Optional;

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
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.gesuch.JaxGesuchstellerConverter;
import ch.dvbern.ebegu.api.dtos.JaxGesuchstellerContainer;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.resource.util.ResourceHelper;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.steuerabfrage.nesko.ZpvService;
import ch.dvbern.ebegu.util.mandant.MandantCookieUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.ebegu.validators.CheckEmail;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Gesuchsteller
 */
@Path("gesuchsteller")
@Stateless
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class GesuchstellerResource {

	@Inject
	private GesuchstellerService gesuchstellerService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private ResourceHelper resourceHelper;

	@Inject
	private JaxGesuchstellerConverter converter;

	@Inject
	private MandantService mandantService;

	@Inject
	private ZpvService zpvService;

	@Operation(
		summary = "Updates a Gesuchsteller or creates it if it doesn't exist in the database. The transfer "
			+
			"object also has a relation to adressen (wohnadresse, umzugadresse, korrespondenzadresse, rechnungsadresse) "
			+
			"these are stored in the database as well. Note that wohnadresse and umzugadresse are both stored as consecutive "
			+
			"wohnadressen in the database. Umzugs flag wird gebraucht, um WizardSteps richtig zu setzen.")
	@Nullable
	@PUT
	@Path("/{gesuchId}/gsNumber/{gsNumber}/{umzug}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public JaxGesuchstellerContainer saveGesuchsteller(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchContJAXPId,
		@Nonnull @NotNull @PathParam("gsNumber") Integer gsNumber,
		@Nonnull @NotNull @PathParam("umzug") Boolean umzug,
		@Nonnull
		@NotNull
		@Valid JaxGesuchstellerContainer gesuchstellerJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Gesuch gesuch = gesuchService.findGesuch(gesuchContJAXPId.getId())
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"createGesuchsteller",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					"GesuchId invalid: " + gesuchContJAXPId.getId()
				)
			);

		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(gesuch);

		GesuchstellerContainer gesuchstellerToMerge =
			new GesuchstellerContainer();
		if (gesuchstellerJAXP.getId() != null) {
			Optional<GesuchstellerContainer> optional = gesuchstellerService
				.findGesuchsteller(gesuchstellerJAXP.getId());
			gesuchstellerToMerge = optional.orElse(
				new GesuchstellerContainer()
			);
		}

		GesuchstellerContainer convertedGesuchsteller = converter
			.gesuchstellerContainerToEntity(
				gesuchstellerJAXP,
				gesuchstellerToMerge
			);
		GesuchstellerContainer persistedGesuchsteller =
			this.gesuchstellerService.saveGesuchsteller(
				convertedGesuchsteller,
				gesuch,
				gsNumber,
				umzug
			);

		return converter.gesuchstellerContainerToJAX(persistedGesuchsteller);
	}

	@Operation(
		summary = "Sucht den Gesuchsteller mit der uebergebenen Id in der Datenbank.")
	@Nullable
	@GET
	@Path("/id/{gesuchstellerId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public JaxGesuchstellerContainer findGesuchsteller(
		@Nonnull
		@NotNull
		@PathParam("gesuchstellerId") JaxId gesuchstellerJAXPId
	) {

		Objects.requireNonNull(gesuchstellerJAXPId.getId());
		String gesuchstellerID = converter.toEntityId(gesuchstellerJAXPId);
		Optional<GesuchstellerContainer> optional = gesuchstellerService
			.findGesuchsteller(gesuchstellerID);

		if (!optional.isPresent()) {
			return null;
		}
		GesuchstellerContainer gesuchstellerToReturn = optional.get();

		return converter.gesuchstellerContainerToJAX(gesuchstellerToReturn);
	}

	@Operation(
		summary = "Send mail to provided email to init connecting GS with ZPV Nr from BE-Login.")
	@Nullable
	@GET
	@Path("/initZPVNr/{gesuchstellerId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed(GESUCHSTELLER)
	public Response initZPVNr(
		@Nonnull
		@QueryParam("email")
		@CheckEmail String email,
		@Nonnull @QueryParam("language") String korrespondenzSprache,
		@Nonnull @PathParam("gesuchstellerId") JaxId gesuchstellerJAXPId,
		@CookieParam(MandantCookieUtil.MANDANT_COOKIE_NAME) Cookie mandantCookie
	) {

		Objects.requireNonNull(gesuchstellerJAXPId.getId());
		String gesuchstellerID = converter.toEntityId(gesuchstellerJAXPId);
		Optional<GesuchstellerContainer> gesuchstellerOpt = gesuchstellerService
			.findGesuchsteller(gesuchstellerID);

		if (gesuchstellerOpt.isEmpty()) {
			throw new EbeguEntityNotFoundException(
				"initZPVNr",
				gesuchstellerID
			);
		}

		MandantIdentifier mandant = getMandantBernFromCookieOrThrow(
			mandantCookie
		);
		zpvService.sendInitUpdateZPVNr(
			email,
			korrespondenzSprache,
			gesuchstellerOpt.get(),
			mandant
		);
		return Response.ok().build();
	}

	private MandantIdentifier getMandantBernFromCookieOrThrow(
		Cookie mandantCookie
	) {
		Mandant mandant = mandantService.findMandantByCookie(mandantCookie);

		if (mandant.getMandantIdentifier() != MandantIdentifier.BERN) {
			throw new EbeguRuntimeException(
				"initZPVNr",
				"Linking ZPV-Nr not allowed for Mandant ",
				mandant.getMandantIdentifier()
			);
		}

		return mandant.getMandantIdentifier();
	}

	@Operation(
		summary = "Returns whether the ZPV number is set on the Gesuchsteller.")
	@Nullable
	@GET
	@Path("/{gesuchstellerId}/is-stek-identifier-set")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed(GESUCHSTELLER)
	public Response isStekIdentifierSetOnGS(
		@Nonnull @PathParam("gesuchstellerId") JaxId gesuchstellerJAXPId
	) {

		Objects.requireNonNull(gesuchstellerJAXPId.getId());
		String gesuchstellerID = converter.toEntityId(gesuchstellerJAXPId);
		Optional<GesuchstellerContainer> gesuchstellerOpt = gesuchstellerService
			.findGesuchsteller(gesuchstellerID);

		if (gesuchstellerOpt.isEmpty()) {
			throw new EbeguEntityNotFoundException(
				"isStekIdentifierSetOnGS",
				gesuchstellerID
			);
		}

		return Response.ok(
			gesuchstellerOpt.get().getGesuchstellerJA().getZpvNummer() != null
				|| gesuchstellerOpt.get().getGesuchstellerJA().getAhvNummer()
					!= null
		).build();
	}

	@Operation(
		summary = "Resets the stek identifier number on the Gesuchsteller by setting all possible identifiers to null")
	@Nullable
	@PUT
	@Path("/{gesuchstellerId}/reset-stek-identifier")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed(GESUCHSTELLER)
	public Response resetGSZPVNummer(
		@Nonnull @PathParam("gesuchstellerId") JaxId gesuchstellerJAXPId
	) {

		Objects.requireNonNull(gesuchstellerJAXPId.getId());
		String gesuchstellerID = converter.toEntityId(gesuchstellerJAXPId);
		Optional<GesuchstellerContainer> gesuchstellerOpt = gesuchstellerService
			.findGesuchsteller(gesuchstellerID);

		if (gesuchstellerOpt.isEmpty()) {
			throw new EbeguEntityNotFoundException(
				"isZPVNummerSetOnGS",
				gesuchstellerID
			);
		}

		this.zpvService.resetStekIdentifier(gesuchstellerOpt.get());

		return Response.ok().build();
	}
}
