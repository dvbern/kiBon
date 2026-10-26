/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.enums;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Which Identity Provider (Keycloak IDP-Alias) to be used at login/logout eines Mandanten.
 * {@link #NO_HINT} means that either not IDP is configured or no IDP is pre-selected
 */
public enum KeycloakIdpHint {

	NO_HINT(null), BELOGIN_LEGACY("keycloak-oidc"), BELOGIN_CIAM(
		"belogin-ciam"
	);

	private final String alias;

	KeycloakIdpHint(String alias) {
		this.alias = alias;
	}

	@Nonnull
	public Optional<String> getIdpAlias() {
		return Optional.ofNullable(alias);
	}
}
