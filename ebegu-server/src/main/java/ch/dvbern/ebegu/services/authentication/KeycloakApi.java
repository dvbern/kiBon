/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.services.authentication;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.locallogin.LocalLoginConfig;
import ch.dvbern.ebegu.locallogin.UserTemplates.UserTemplate;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.services.authentication.RealmRoles.MITARBEITER_ACCESS;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class KeycloakApi {

	private static final Integer SIX_HOURS_IN_SECONDS = 6 * 60 * 60;

	private static final Logger LOGGER = LoggerFactory.getLogger(
		KeycloakApi.class
	);

	private final Keycloak keycloak;
	private final LocalLoginConfig localLoginConfig;

	public void lock(Benutzer benutzer) {
		setEnabled(benutzer, false);
	}

	public void unlock(Benutzer benutzer) {
		setEnabled(benutzer, true);
	}

	private void setEnabled(Benutzer benutzer, boolean enabled) {
		if (benutzer.getExternalUUID() == null) {
			return;
		}

		getUserResourceOptional(benutzer)
			.ifPresent(user -> {
				var representation = user.toRepresentation();
				representation.setEnabled(enabled);
				user.update(representation);
				user.logout();
			});
	}

	public void logout(Benutzer benutzer) {
		if (benutzer.getExternalUUID() == null) {
			return;
		}

		getUserResourceOptional(benutzer)
			.ifPresent(UserResource::logout);
	}

	public void delete(Benutzer benutzer) {
		var realmName = getRealmName(benutzer);
		var uuid = benutzer.getExternalUUID();
		if (uuid == null) {
			return;
		}
		keycloak.realm(realmName).users().delete(uuid).close();
	}

	public void deleteByExternalUUID(
		String uuid,
		MandantIdentifier mandantIdentifier
	) {
		keycloak.realm(mandantIdentifier.getRealmName())
			.users()
			.delete(uuid)
			.close();
	}

	public String create(Benutzer benutzer) {
		var realmName = getRealmName(benutzer);
		var userRepresentation = new UserRepresentation();
		userRepresentation.setEnabled(true);
		userRepresentation.setEmailVerified(true);
		userRepresentation.setRequiredActions(
			getRequiredActions(benutzer.getMandant())
		);
		userRepresentation.setEmail(benutzer.getEmail());
		userRepresentation.setFirstName(benutzer.getVorname());
		userRepresentation.setLastName(benutzer.getNachname());

		getCredential(benutzer)
			.ifPresent(
				credential -> userRepresentation.setCredentials(
					List.of(credential)
				)
			);
		try (var response = keycloak.realm(realmName)
			.users()
			.create(userRepresentation)) {
			if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
				return getExternalUuid(response);
			}
			if (response.getStatusInfo().toEnum() == Response.Status.CONFLICT) {
				throw new EbeguRuntimeException(
					"create",
					ErrorCodeEnum.ERROR_KEYCLOAK_USER_EXISTS
				);

			}
			throw new EbeguRuntimeException(
				"create",
				MessageFormat.format(
					"Creating user in Keycloak failed: {0}, {1}",
					response.getStatusInfo(),
					response.readEntity(String.class)
				)
			);
		}
	}

	public void updateInitialPassword(Benutzer benutzer) {
		if (benutzer.getInitialPassword() == null
			||
			benutzer.getInitialPassword().isEmpty()) {
			return;
		}
		getCredential(benutzer).ifPresent(
			credential -> getUserResourceOptional(benutzer)
				.ifPresent(user -> user.resetPassword(credential))
		);
	}

	/**
	 * Adds the Keycloak realm role {@link RealmRoles#MITARBEITER_ACCESS} to the given user.
	 *
	 * @param benutzer Reference to the user to add the role to.
	 * @return If the role has been successfully added to the given user.
	 */
	public boolean addMitarbeiterAccessBenutzerRole(Benutzer benutzer) {

		Optional<UserResource> optionalBenutzer = getUserResourceOptional(
			benutzer
		);

		if (optionalBenutzer.isPresent()) {

			String realmName = getRealmName(benutzer);
			String roleName = MITARBEITER_ACCESS.name();
			try {
				RoleRepresentation role = keycloak.realm(realmName)
					.roles()
					.get(roleName)
					.toRepresentation();// Remove the role from the user
				optionalBenutzer.get()
					.roles()
					.realmLevel()
					.add(Collections.singletonList(role));

				return true;
			} catch (NotFoundException e) {
				LOGGER.error(
					"Role {} not found in Keycloak",
					MITARBEITER_ACCESS.name(),
					e
				);
				throw e;
			}
		}

		return false;
	}

	/**
	 * Removes the Keycloak realm role {@link RealmRoles#MITARBEITER_ACCESS} from the given user.
	 *
	 * @param benutzer Reference to the user to remove the role from.
	 * @return If the role has been successfully removed from the given user.
	 */
	public boolean deleteMitarbeiterAccessBenutzerRole(Benutzer benutzer) {

		Optional<UserResource> optionalBenutzer = getUserResourceOptional(
			benutzer
		);

		if (optionalBenutzer.isPresent()) {

			String realmName = getRealmName(benutzer);
			String roleName = MITARBEITER_ACCESS.name();
			RoleRepresentation role = keycloak.realm(realmName)
				.roles()
				.get(roleName)
				.toRepresentation();

			// Remove the role from the user
			optionalBenutzer.get()
				.roles()
				.realmLevel()
				.remove(Collections.singletonList(role));

			return true;
		}
		return false;
	}

	private Optional<CredentialRepresentation> getCredential(
		Benutzer benutzer
	) {
		if (benutzer.getMandant()
			.getMandantIdentifier()
			.hasIdentityProvider()) {
			return Optional.empty(); // because users are managed by IdP
		}

		return Optional.of(getPassword(benutzer.getInitialPassword(), true));
	}

	private static List<String> getRequiredActions(Mandant mandant) {
		if (mandant.getMandantIdentifier().hasIdentityProvider()) {
			return List.of(); //Because users are managed by IdP
		}

		return List.of(
			"UPDATE_PASSWORD",
			"UPDATE_PROFILE"
		);
	}

	private CredentialRepresentation getPassword(
		String password,
		boolean temporary
	) {
		CredentialRepresentation credentialRepresentation =
			new CredentialRepresentation();
		credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
		credentialRepresentation.setTemporary(temporary);
		credentialRepresentation.setValue(password);
		return credentialRepresentation;
	}

	public void configureForLocalLogin(
		Benutzer benutzer,
		UserTemplate userTemplate
	) {
		getUserResourceOptional(benutzer)
			.ifPresent(userResource -> {
				var userRepresentation =
					userResource.toRepresentation();

				userRepresentation.setRequiredActions(List.of());
				userRepresentation.setFirstName(userTemplate.vorname());
				userRepresentation.setLastName(userTemplate.name());
				userRepresentation.setCredentials(
					List.of(getPassword(localLoginConfig.getPassword(), false))
				);

				userResource.update(userRepresentation);
			});
	}

	public Optional<String> findByEmail(
		String email,
		MandantIdentifier mandantIdentifier
	) {
		var realmName = mandantIdentifier.getRealmName();

		var response = keycloak.realm(realmName)
			.users()
			.searchByEmail(email, true);

		return response.isEmpty() ?
			Optional.empty() :
			Optional.of(response.get(0).getId());
	}

	public void sendUpdatePasswordEmail(Benutzer benutzer) {
		var realmName = getRealmName(benutzer);
		var uuid = benutzer.getExternalUUID();
		var user = keycloak.realm(realmName).users().get(uuid);

		try {
			user.executeActionsEmail(
				List.of("UPDATE_PASSWORD"),
				SIX_HOURS_IN_SECONDS
			);
			LOGGER.info(
				"Update password email sent to user with externalUuid: {}",
				uuid
			);
		} catch (RuntimeException ex) {
			throw new IllegalStateException(
				"Failed to trigger user action UPDATE_PASSWORD for user with externalUuid: "
					+ uuid,
				ex
			);
		}
	}

	public String getIDPLogoutUrl(String realmName, String idpAlias) {
		IdentityProviderRepresentation idp = keycloak
			.realm(realmName)
			.identityProviders()
			.get(idpAlias)
			.toRepresentation();

		return idp.getConfig().get("logoutUrl");
	}

	private Optional<UserResource> getUserResourceOptional(Benutzer benutzer) {
		var realmName = getRealmName(benutzer);
		var uuid = benutzer.getExternalUUID();

		try {
			var user = keycloak.realm(realmName).users().get(uuid);
			user.toRepresentation();
			return Optional.of(user);
		} catch (RuntimeException e) {
			LOGGER.info(
				"Benutzer UUID {} not found in Keycloak. Most likely the UUID just don't exist. In this case a NPE is thrown. "
					+ "But there might be oder reasons. Exception type was: {}, exception message is: {}.",
				uuid,
				e.getClass().getName(),
				e.getMessage()
			);
			return Optional.empty();
		}
	}

	private static String getExternalUuid(Response response) {
		// keycloak returns HTTP 201, which includes the URL to the new user in the location header
		URI uri = URI.create(response.getHeaderString("Location"));
		return new File(uri.getPath()).getName();
	}

	private static String getRealmName(Benutzer benutzer) {
		return benutzer.getMandant().getMandantIdentifier().getRealmName();
	}

	/**
	 * Updates the user's firstname and last name in Keycloak based on the provided user details.
	 * This call is explicitly not asynchronous, as it is intended to be used within a transactional context.
	 *
	 * @param benutzer The user containing the updated information, such as first name and last name,
	 * which will be applied to the corresponding user record in Keycloak.
	 */
	public void updateUser(Benutzer benutzer) {
		var userResource = getUserResourceOptional(benutzer).orElseThrow();
		var representation = userResource.toRepresentation();
		representation.setFirstName(benutzer.getVorname());
		representation.setLastName(benutzer.getNachname());
		userResource.update(representation);
	}
}
