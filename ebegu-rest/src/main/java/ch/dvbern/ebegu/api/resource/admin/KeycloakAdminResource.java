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

package ch.dvbern.ebegu.api.resource.admin;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.admin.KeycloakAdminServiceBean;
import ch.dvbern.ebegu.services.admin.KeycloakAdminServiceException;
import ch.dvbern.ebegu.services.authentication.RealmRoles;
import ch.dvbern.ebegu.util.Constants;
import org.eclipse.microprofile.openapi.annotations.Operation;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Path("admin/keycloak")
@Stateless
@RolesAllowed(SUPER_ADMIN)
public class KeycloakAdminResource {

	@Inject
	private KeycloakAdminServiceBean keycloakAdminServiceBean;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private BenutzerService benutzerService;

	@Operation(summary = "Erstellt die MitarbeiterRechte")
	@GET
	@Path("/accessrechte/erstellen")
	@Produces(MediaType.TEXT_PLAIN)
	public Response mitarbeiterrechteErstellen(
		@Context HttpServletRequest request
	) {
		var future = keycloakAdminServiceBean.createAccessMitarbeiterRechte(
			principalBean.getMandant()
		);
		String time = LocalDateTime.now().format(Constants.DATE_FORMATTER);
		try {
			var success = future.get();
			if (success) {
				return Response.ok(
					time + " Mitarbeiterrechte erstellen abgeschlossen"
				)
					.build();
			}
			return Response.serverError()
				.entity(
					time
						+ "Der Prozess konnte nicht erfolgreich abgeschlossen werden"
				)
				.build();
		} catch (InterruptedException | ExecutionException e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	/**
	 * Removes the Keycloak realm role {@link RealmRoles#MITARBEITER_ACCESS} from the given user.
	 * This role is required for accessing the "Local Login" feature.
	 *
	 * @param request Reference to the {@link HttpServletRequest} performing this method call.
	 * @param externalUuid The external UUID of the user to remove the role for. This is the ID the user is
	 * identified by in Keycloak.
	 * @return A response containing the success or error note as text message. Possible response codes are:
	 * <b>200</b>: The role has been removed.
	 * <b>404</b>: No user for the given UUID was found.
	 * <b>500</b>: There was an error performing the request, in most cases this addresses misconfigurations of the
	 * keycloak API.
	 */
	@Operation(
		summary = "Löscht die Keycloak-Realm-Rolle MITARBEITER_ACCESS von einem Benutzer.")
	@DELETE
	@Path("/accessrechte/loeschen")
	@Produces(MediaType.TEXT_PLAIN)
	public Future<Response> mitarbeiterrechteLoeschen(
		@Context HttpServletRequest request,
		@QueryParam("externalUuid") String externalUuid
	) {

		Optional<Benutzer> optionalBenutzer = benutzerService
			.findBenutzerByExternalUUID(externalUuid);

		Future<Boolean> result;
		if (optionalBenutzer.isPresent()) {
			result = keycloakAdminServiceBean.deleteAccessMitarbeiterRechte(
				optionalBenutzer.get()
			);
		} else {
			result = CompletableFuture.completedFuture(false);
		}

		return CompletableFuture.supplyAsync(() -> {
			try {
				boolean roleDeleted = result.get();
				if (roleDeleted) {
					return Response.ok(
						"Die Rolle MITARBEITER_ACCESS wurde für den Benutzer mit der externen UUID %s entfernt."
							.formatted(externalUuid)
					)
						.build();
				}
				return Response.status(404)
					.entity(
						"Der Benutzer mit der externen UUID %s wurde nicht gefunden."
							.formatted(externalUuid)
					)
					.build();
			} catch (ExecutionException | KeycloakAdminServiceException e) {
				return Response.serverError()
					.entity(
						"Das Löschen der Rolle MITARBEITER_ACCESS für den Benutzer mit der externen UUID %s ist fehlgeschlagen. Die Nachricht der %s ist: %s."
							.formatted(
								externalUuid,
								e.getClass().getName(),
								e.getMessage()
							)
					)
					.build();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return Response.serverError()
					.entity(
						"Das Löschen der Rolle MITARBEITER_ACCESS für den Benutzer mit der externen UUID %s ist fehlgeschlagen. Die Nachricht der Exception ist: %s."
							.formatted(externalUuid, e.getMessage())
					)
					.build();
			}
		});
	}

	/**
	 * Adds the Keycloak realm role {@link RealmRoles#MITARBEITER_ACCESS} to the given user.
	 * This role is required for accessing the "Local Login" feature.
	 *
	 * @param request Reference to the {@link HttpServletRequest} performing this method call.
	 * @param externalUuid The external UUID of the user to add the role to. This is the ID the user is
	 * identified by in Keycloak.
	 * @return A response containing the success or error note as text message. Possible response codes are:
	 * <b>200</b>: The role has been added.
	 * <b>404</b>: No user for the given UUID was found.
	 * <b>500</b>: There was an error performing the request, in most cases this addresses misconfigurations of the
	 * keycloak API.
	 */
	@Operation(
		summary = "Fügt einem Benutzer die Keycloak-Realm-Rolle MITARBEITER_ACCESS hinzu.")
	@PUT
	@Path("/accessrechte/hinzufuegen")
	@Produces(MediaType.TEXT_PLAIN)
	public Future<Response> mitarbeiterrechteHinzufuegen(
		@Context HttpServletRequest request,
		@QueryParam("externalUuid") String externalUuid
	) {
		Optional<Benutzer> optionalBenutzer = benutzerService
			.findBenutzerByExternalUUID(externalUuid);

		Future<Boolean> result;
		if (optionalBenutzer.isPresent()) {
			result = keycloakAdminServiceBean.addMitarbeiterAccessBenutzerRole(
				optionalBenutzer.get()
			);
		} else {
			result = CompletableFuture.completedFuture(false);
		}
		return CompletableFuture.supplyAsync(() -> {
			try {
				boolean roleAdded = result.get();
				if (roleAdded) {
					return Response.ok(
						"Die Rolle MITARBEITER_ACCESS wurde dem Benutzer mit der externen UUID %s hinzugefügt."
							.formatted(externalUuid)
					)
						.build();
				}
				return Response.status(404)
					.entity(
						"Der Benutzer mit der externen UUID %s wurde nicht gefunden."
							.formatted(externalUuid)
					)
					.build();
			} catch (ExecutionException | KeycloakAdminServiceException e) {
				return Response.serverError()
					.entity(
						"Das Hinzufügen der Rolle MITARBEITER_ACCESS für den Benutzer mit der externen UUID %s ist fehlgeschlagen. Die Nachricht der %s ist: %s."
							.formatted(
								externalUuid,
								e.getClass().getName(),
								e.getMessage()
							)
					)
					.build();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return Response.serverError()
					.entity(
						"Das Hinzufügen der Rolle MITARBEITER_ACCESS für den Benutzer mit der externen UUID %s ist fehlgeschlagen. Die Nachricht der Exception ist: %s."
							.formatted(externalUuid, e.getMessage())
					)
					.build();
			}
		});
	}
}
