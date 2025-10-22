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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Named("keycloakConfig")
@ApplicationScoped
@Getter
public class KeycloakConfig {
	@Inject
	@ConfigProperty(name = "ebegu.keycloak.host")
	private String keycloakHost;

	@Inject
	@ConfigProperty(name = "ebegu.keycloak.api.username")
	private String apiUsername;

	@Inject
	@ConfigProperty(name = "ebegu.keycloak.api.password")
	private String apiPassword;
}
