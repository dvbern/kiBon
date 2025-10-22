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

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

@ApplicationScoped
public class KeycloakClientProducer {

	@Inject
	private KeycloakConfig keycloakConfig;

	private Keycloak keycloak;

	@Produces
	@ApplicationScoped
	public Keycloak keycloak() {
		if (keycloak == null) {
			keycloak = KeycloakBuilder.builder()
				.serverUrl(keycloakConfig.getKeycloakHost())
				.username(keycloakConfig.getApiUsername())
				.password(keycloakConfig.getApiPassword())
				.realm("master")
				.clientId("admin-cli")
				.grantType(OAuth2Constants.PASSWORD)
				.build();
		}
		return keycloak;
	}

	@PreDestroy
	public void closeKeycloak() {
		if (keycloak != null) {
			keycloak.close();
		}
	}
}
