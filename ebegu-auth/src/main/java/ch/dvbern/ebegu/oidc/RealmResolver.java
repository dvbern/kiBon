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

package ch.dvbern.ebegu.oidc;

import java.net.URI;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

/**
 * Returns the keycloak realm name for the current request.
 *
 * @see OpenIdConfigurationProducer
 */
@RequestScoped
@Named("realmResolver")
public class RealmResolver {

	@Inject
	private HttpServletRequest currentRequest;

	/**
	 * see usage in EbeguApplicationV1 in Jakarta EL expression
	 */
	public String getRealm() {
		return getRealm(currentRequest);
	}

	public String getRealm(HttpServletRequest request) {
		return MandantIdentifier.getByHostname(
			URI.create(request.getRequestURL().toString())
		).getRealmName();
	}
}
