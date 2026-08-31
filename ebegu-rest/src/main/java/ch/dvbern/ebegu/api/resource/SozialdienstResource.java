/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxSozialdienstConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.sozialdienst.JaxSozialdienst;
import ch.dvbern.ebegu.api.dtos.sozialdienst.JaxSozialdienstStammdaten;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstStammdaten;
import ch.dvbern.ebegu.enums.SozialdienstStatus;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.SozialdienstService;
import ch.dvbern.ebegu.util.mandant.MandantCookieUtil;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Path("sozialdienst")
@Stateless
@DenyAll
public class SozialdienstResource {

	@Inject
	private JaxSozialdienstConverter jaxSozialdienstConverter;

	@Inject
	private SozialdienstService sozialdienstService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private MandantService mandantService;

	@Inject
	private PrincipalBean principalBean;

	@Operation(summary = "Erstellt eine neue Sozialdienst in der Datenbank")
	@Nullable
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public JaxSozialdienst createSozialdienst(
		@Nonnull @NotNull @Valid JaxSozialdienst sozialdienstJAXP,
		@Nonnull @NotNull @Valid @QueryParam("adminMail") String adminMail,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Sozialdienst convertedSozialdienst =
			jaxSozialdienstConverter.sozialdienstToEntity(
				sozialdienstJAXP,
				new Sozialdienst()
			);

		Sozialdienst persistedSozialdienst = this.sozialdienstService
			.createSozialdienst(adminMail, convertedSozialdienst);

		return jaxSozialdienstConverter.sozialdienstToJAX(
			persistedSozialdienst
		);
	}

	@Operation(summary = "Returns all Sozialdienst")
	@Nullable
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Oeffentliche Daten
	public List<JaxSozialdienst> getAllSozialdienst(
		@CookieParam(MandantCookieUtil.MANDANT_COOKIE_NAME) Cookie mandantCookie
	) {

		var mandant = mandantService.findMandantByCookie(mandantCookie);

		return sozialdienstService.getAllSozialdienste(mandant)
			.stream()
			.map(
				sozialdienst -> jaxSozialdienstConverter
					.sozialdienstToJAX(sozialdienst)
			)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Returns the SozialdienstStammdaten with the given SozialdienstId.")
	@Nullable
	@GET
	@Path("/stammdaten/{sozialdienstId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public JaxSozialdienstStammdaten getSozialdienstStammdaten(
		@Nonnull
		@NotNull
		@PathParam("sozialdienstId") JaxId sozialdienstJaxId
	) {

		String sozialdienstId = jaxSozialdienstConverter.toEntityId(
			sozialdienstJaxId
		);

		Optional<SozialdienstStammdaten> stammdatenFromDB =
			sozialdienstService.getSozialdienstStammdatenBySozialdienstId(
				sozialdienstId
			);
		if (!stammdatenFromDB.isPresent()) {
			stammdatenFromDB = initSozialdienstStammdaten(sozialdienstId);
		}

		authorizer.checkReadAuthorization(
			stammdatenFromDB.get().getSozialdienst()
		);

		return stammdatenFromDB
			.map(
				stammdaten -> jaxSozialdienstConverter
					.sozialdienstStammdatenToJAX(stammdaten)
			)
			.orElse(null);
	}

	private Optional<SozialdienstStammdaten> initSozialdienstStammdaten(
		String sozialdienstId
	) {
		SozialdienstStammdaten stammdaten = new SozialdienstStammdaten();
		Optional<Sozialdienst> sozialdienst = sozialdienstService
			.findSozialdienst(sozialdienstId);
		stammdaten.setSozialdienst(sozialdienst.orElse(new Sozialdienst()));
		stammdaten.setAdresse(new Adresse());
		return Optional.of(stammdaten);
	}

	@Operation(summary = "Speichert die SozialdienstStammdaten")
	@Nullable
	@PUT
	@Path("/stammdaten")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_SOZIALDIENST })
	public JaxSozialdienstStammdaten saveSozialdienstStammdaten(
		@Nonnull @NotNull @Valid JaxSozialdienstStammdaten jaxStammdaten,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		SozialdienstStammdaten stammdaten;
		if (jaxStammdaten.getId() != null) {
			Optional<SozialdienstStammdaten> optional =
				sozialdienstService.getSozialdienstStammdaten(
					jaxStammdaten.getId()
				);
			stammdaten = optional.orElse(new SozialdienstStammdaten());
		} else {
			stammdaten = new SozialdienstStammdaten();
		}
		if (stammdaten.isNew()) {
			stammdaten.setAdresse(new Adresse());
		}
		SozialdienstStammdaten convertedStammdaten =
			jaxSozialdienstConverter.sozialdienstStammdatenToEntity(
				jaxStammdaten,
				stammdaten
			);

		// Statuswechsel
		if (convertedStammdaten.getSozialdienst().getStatus()
			== SozialdienstStatus.EINGELADEN) {
			convertedStammdaten.getSozialdienst()
				.setStatus(SozialdienstStatus.AKTIV);
		}
		// Name ist editierbar in die Stammdaten aber nur fuer Super Admin
		if (principalBean.isCallerInRole(SUPER_ADMIN)) {
			convertedStammdaten.getSozialdienst()
				.setName(jaxStammdaten.getSozialdienst().getName());
		}

		authorizer.checkWriteAuthorization(
			convertedStammdaten
		);

		SozialdienstStammdaten persistedStammdaten =
			sozialdienstService.saveSozialdienstStammdaten(
				convertedStammdaten
			);

		return jaxSozialdienstConverter.sozialdienstStammdatenToJAX(
			persistedStammdaten
		);
	}

	@Operation(summary = "Speichert die SozialdienstStammdaten")
	@Nullable
	@PUT
	@Path("/name")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public JaxSozialdienst saveSozialdienstName(
		@Nonnull @NotNull @Valid JaxSozialdienst jaxSozialDienst,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Sozialdienst sozialdienst =
			sozialdienstService.findSozialdienst(
				jaxSozialDienst.getId()
			)
				.orElseThrow(
					() -> new EntityNotFoundException(
						"Sozialdienst not found for "
							+ jaxSozialDienst.getId()
					)
				);

		// Name ist editierbar in die Stammdaten aber nur fuer Super Admin
		sozialdienst
			.setName(jaxSozialDienst.getName());

		authorizer.checkWriteAuthorization(
			sozialdienst
		);

		Sozialdienst persistedSozialdienst =
			sozialdienstService.saveSozialdienst(
				sozialdienst
			);

		return jaxSozialdienstConverter.sozialdienstToJAX(
			persistedSozialdienst
		);
	}
}
