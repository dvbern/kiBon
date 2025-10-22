/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
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

import ch.dvbern.ebegu.api.converter.gesuch.JaxFallDossierConverter;
import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.api.dtos.JaxDossier;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.FallService;
import com.google.common.base.Strings;
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
 * Resource fuer Dossier
 */
@Path("dossier")
@Stateless
@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
public class DossierResource {

	@Inject
	private DossierService dossierService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private FallService fallService;

	@Inject
	private JaxFallDossierConverter converter;

	@Operation(summary = "Erstellt ein Dossier in der Datenbank")
	@Nonnull
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public Response create(
		@Nonnull @NotNull JaxDossier dossierJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Dossier dossierToMerge = new Dossier();
		if (dossierJax.getTimestampErstellt() != null) {
			Objects.requireNonNull(dossierJax.getGemeinde());
			Objects.requireNonNull(dossierJax.getGemeinde().getId());
			Objects.requireNonNull(dossierJax.getFall());
			Objects.requireNonNull(dossierJax.getFall().getId());
			Optional<Dossier> dossierByGemeindeAndFall = dossierService
				.findDossierByGemeindeAndFall(
					dossierJax.getGemeinde().getId(),
					dossierJax.getFall().getId()
				);
			if (dossierByGemeindeAndFall.isPresent()) {
				dossierToMerge = dossierByGemeindeAndFall.get();
			}
		}
		Dossier convertedDossier = converter.dossierToEntity(
			dossierJax,
			dossierToMerge
		);
		Dossier persistedDossier = this.dossierService.saveDossier(
			convertedDossier
		);

		URI uri = uriInfo.getBaseUriBuilder()
			.path(DossierResource.class)
			.path('/' + persistedDossier.getId())
			.build();

		JaxDossier jaxDossier = converter.dossierToJAX(persistedDossier);
		return Response.created(uri).entity(jaxDossier).build();
	}

	@Operation(summary = "Returns the Dossier with the given Id.")
	@Nullable
	@GET
	@Path("/id/{dossierId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public JaxDossier findDossier(
		@Nonnull @NotNull @PathParam("dossierId") JaxId dossierJAXPId
	) {
		Objects.requireNonNull(dossierJAXPId.getId());
		String dossierId = converter.toEntityId(dossierJAXPId);
		Optional<Dossier> dossierOptional = dossierService.findDossier(
			dossierId
		);

		return dossierOptional.map(dossier -> converter.dossierToJAX(dossier))
			.orElse(null);
	}

	@Operation(
		summary = "Returns all Dossiers of the given Fall that are visible for the current user")
	@Nullable
	@GET
	@Path("/fall/{fallId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxDossier> findDossiersByFall(
		@Nonnull @NotNull @Valid @PathParam("fallId") JaxId fallJaxId
	) {

		Objects.requireNonNull(fallJaxId.getId());

		String fallId = converter.toEntityId(fallJaxId);
		Collection<Dossier> dossierList = dossierService.findDossiersByFall(
			fallId
		);

		//noinspection ConstantConditions -> here JaxAbstractDTO::getTimestampErstellt cannot be null
		return dossierList.stream()
			.map(dossier -> converter.dossierToJAX(dossier))
			.sorted(
				Comparator.comparing(
					JaxAbstractDTO::getTimestampErstellt
				)
			)
			.collect(Collectors.toList());
	}

	@Operation(
		summary = "Returns all Dossiers of the given Fall that are visible for the current user")
	@Nullable
	@GET
	@Path("/newestCurrentBesitzer")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public JaxDossier findNewestDossierByCurrentBenutzerAsBesitzer() {
		Optional<Fall> optFall = fallService
			.findFallByCurrentBenutzerAsBesitzer();
		// Beim ersten Einloggen ist der Fall nie vorhanden, dies ist also ein erwarteter Fehler. Wir loggen es nicht.
		String fallId = optFall
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					KibonLogLevel.NONE,
					"findNewestDossierByCurrentBenutzerAsBesitzer",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND
				)
			)
			.getId();
		Collection<Dossier> dossierList = dossierService.findDossiersByFall(
			fallId
		);

		//noinspection ConstantConditions -> here JaxAbstractDTO::getTimestampErstellt cannot be null
		return dossierList.stream()
			.max(Comparator.comparing(AbstractEntity::getTimestampErstellt))
			.map(dossier -> converter.dossierToJAX(dossier))
			.orElse(null);
	}

	@Operation(
		summary = "Creates a new Dossier in the database if it doesnt exist with the current user as owner.")
	@Nullable
	@PUT
	@Path("/createforcurrentbenutzer/{gemeindeId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public JaxDossier getOrCreateDossierAndFallForCurrentUserAsBesitzer(
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Objects.requireNonNull(gemeindeJaxId.getId());
		String gemeindeId = converter.toEntityId(gemeindeJaxId);

		Dossier dossier = dossierService
			.getOrCreateDossierAndFallForCurrentUserAsBesitzer(gemeindeId);
		return converter.dossierToJAX(dossier);
	}

	@Operation(summary = "Setzt den Verantwortlichen BG fuer dieses Dossier")
	@Nullable
	@PUT
	@Path("/verantwortlicherBG/{dossierId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Response setVerantwortlicherBG(
		@Nonnull @NotNull @PathParam("dossierId") JaxId jaxDossierId,
		@Nullable String username,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		// TODO Wieso hier ist der Gesuchsteller erlaubt???? ist da nicht eine alte Sicherheitlücke?
		Objects.requireNonNull(jaxDossierId.getId());

		Dossier dossier = dossierService.findDossier(jaxDossierId.getId())
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"setVerantwortlicherBG",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					jaxDossierId.getId()
				)
			);

		if (Strings.isNullOrEmpty(username)) {
			this.dossierService.setVerantwortlicherBG(dossier.getId(), null);

		} else {
			Benutzer benutzer = benutzerService.findBenutzer(
				username,
				dossier.getFall().getMandant()
			)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"setVerantwortlicherBG",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						username
					)
				);

			this.dossierService.setVerantwortlicherBG(
				dossier.getId(),
				benutzer
			);
		}
		return Response.ok().build();
	}

	@Operation(summary = "Setzt den Verantwortlichen TS fuer dieses Dossier")
	@Nullable
	@PUT
	@Path("/verantwortlicherTS/{dossierId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Response setVerantwortlicherTS(
		@Nonnull @NotNull @PathParam("dossierId") JaxId jaxDossierId,
		@Nullable String username,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		// TODO Wieso hier ist der Gesuchsteller erlaubt???? ist da nicht eine alte Sicherheitlücke?
		Objects.requireNonNull(jaxDossierId.getId());

		Dossier dossier = dossierService.findDossier(jaxDossierId.getId())
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"setVerantwortlicherTS",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					jaxDossierId.getId()
				)
			);

		if (Strings.isNullOrEmpty(username)) {
			this.dossierService.setVerantwortlicherTS(dossier.getId(), null);

		} else {
			final Benutzer benutzer = benutzerService.findBenutzer(
				username,
				dossier.getFall().getMandant()
			)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"setVerantwortlicherTS",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						username
					)
				);

			this.dossierService.setVerantwortlicherTS(
				dossier.getId(),
				benutzer
			);

		}
		return Response.ok().build();
	}

	@Operation(summary = "Bemerkung auf dem Dossier Speichern")
	@Nullable
	@PUT
	@Path("/bemerkungen/{dossierId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS })
	public Response updateBemerkungen(
		@Nonnull @NotNull @PathParam("dossierId") JaxId dossierJAXPId,
		@Nonnull @NotNull String bemerkungen,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Objects.requireNonNull(dossierJAXPId.getId());
		Dossier dossier = dossierService.findDossier(
			converter.toEntityId(dossierJAXPId)
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"updateBemerkungen",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					dossierJAXPId.getId()
				)
			);

		dossier.setBemerkungen(bemerkungen);

		dossierService.saveDossier(dossier);

		return Response.ok().build();
	}
}
