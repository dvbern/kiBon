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

package ch.dvbern.ebegu.services.admin;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import jakarta.ejb.AsyncResult;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.authentication.KeycloakApi;
import ch.dvbern.ebegu.services.authentication.RealmRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@Local(KeycloakAdminServiceBean.class)
public class KeycloakAdminServiceBean {

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private KeycloakApi keycloakApi;

	private static final Logger LOGGER = LoggerFactory.getLogger(
		KeycloakAdminServiceBean.class
	);

	@Asynchronous
	public Future<Boolean> createAccessMitarbeiterRechte(Mandant mandant) {
		try {
			LOGGER.info(
				"Erstellung von Mitarbeiter Access Rechte gestartet..."
			);
			Collection<Benutzer> benutzerList = benutzerService
				.getAllUserButGesuchsteller(mandant);
			for (Benutzer user : benutzerList) {
				addMitarbeiterAccessBenutzerRole(user);
			}
			LOGGER.info("... Erstellung von Mitarbeiter Access Rechte beendet");
			return new AsyncResult<>(Boolean.TRUE);
		} catch (RuntimeException e) {
			LOGGER.error(
				"createAccessMitarbeiterRechte konnte nicht durchgefuehrt werden!",
				e
			);
			return new AsyncResult<>(Boolean.FALSE);
		}
	}

	/**
	 * Adds the Keycloak realm role {@link RealmRoles#MITARBEITER_ACCESS} to the given user.
	 * This role is required for accessing the "Local Login" feature.
	 *
	 * @param benutzer The user to remove the role for.
	 * @return If the role has been successfully removed from the given user.
	 */
	@Asynchronous
	@SuppressWarnings("squid:S2139") // The rule says: Either log this exception and handle it, or rethrow it with some contextual information.
	// But this is exactly what we're doing here. Looks like a false positive to me.
	public Future<Boolean> addMitarbeiterAccessBenutzerRole(Benutzer benutzer) {
		try {
			if (keycloakApi.addMitarbeiterAccessBenutzerRole(benutzer)) {
				LOGGER.info(
					"Added role MITARBEITER_ACCESS to user {}",
					benutzer.getUsername()
				);
				return CompletableFuture.completedFuture(true);
			}

			LOGGER.warn(
				"Role MITARBEITER_ACCESS could not be added to user {}, because this user does not exist in Keycloak.",
				benutzer.getUsername()
			);
			return CompletableFuture.completedFuture(false);

		} catch (RuntimeException e) {
			LOGGER.error(
				"Trying to add role MITARBEITER_ACCESS to user {} has failed!",
				benutzer.getUsername(),
				e
			);
			throw new KeycloakAdminServiceException(e);
		}
	}

	/**
	 * Removes the Keycloak realm role {@link RealmRoles#MITARBEITER_ACCESS} from the given user.
	 * This role is required for accessing the "Local Login" feature.
	 *
	 * @param externalUuid The external UUID of the user to remove the role for. This is the ID the user is
	 * identified by in Keycloak.
	 * @return If the role has been successfully removed from the given user.
	 */
	@Asynchronous
	@SuppressWarnings("squid:S2139") // The rule says: Either log this exception and handle it, or rethrow it with some contextual information.
	// But this is exactly what we're doing here. Looks like a false positive to me.
	public Future<Boolean> deleteAccessMitarbeiterRechte(Benutzer benutzer) {
		try {

			if (keycloakApi.deleteMitarbeiterAccessBenutzerRole(benutzer)) {
				LOGGER.info(
					"Removed role MITARBEITER_ACCESS from user {}",
					benutzer.getUsername()
				);
				return CompletableFuture.completedFuture(true);
			}

			LOGGER.warn(
				"Role MITARBEITER_ACCESS could not be removed from user {}, because this user does not exist in Keycloak.",
				benutzer.getUsername()
			);
			return CompletableFuture.completedFuture(false);

		} catch (RuntimeException e) {
			LOGGER.error(
				"Trying to remove role MITARBEITER_ACCESS from user {} has failed!",
				benutzer.getUsername(),
				e
			);
			throw new KeycloakAdminServiceException(e);
		}
	}
}
