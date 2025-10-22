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

package ch.dvbern.ebegu.authentication;

import java.util.Objects;

import jakarta.annotation.Nullable;

import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

/*
 * Non-BERN externalUUIDs are prefixed with 'keycloak:solothurn:' etc.
 *
 * BERN externalUUIDs are NOT prefixed.
 *
 * The difference stems from the following lines in the belogin-connector:
 * - KeycloakExternalBenutzerConverter.java:50
 * - BELoginExternalBenutzerConverter.java:73
 *
 */
public final class ExternalUUIDUtil {

	public static final String PREFIX = "keycloak:";

	private ExternalUUIDUtil() {
	}

	@Nullable
	public static String addPrefixIfNecessary(
		String externalUUID,
		MandantIdentifier mandantIdentifier
	) {
		Objects.requireNonNull(mandantIdentifier);

		// might be the case for older/pre-OIDC users with pending invitations
		// or technical users like ch.dvbern.ebegu.inbox.util.TechnicalUserConfiguration
		if (externalUUID == null) {
			return null;
		}

		if (mandantIdentifier == MandantIdentifier.BERN
			&& externalUUID.startsWith(PREFIX)) {
			throw new IllegalStateException(
				"externalUUIDs must not be prefixed in Bern"
			);
		}

		if (mandantIdentifier == MandantIdentifier.BERN
			|| externalUUID.startsWith(PREFIX)) {
			return externalUUID;
		}

		String lowerCaseMandantIdentifier = mandantIdentifier
			.name()
			.toLowerCase(Constants.DEFAULT_LOCALE);

		return "%s%s:%s".formatted(
			PREFIX,
			lowerCaseMandantIdentifier,
			externalUUID
		);
	}

	public static String removePrefixIfNecessary(
		String externalUUID
	) {
		// might be the case for older/pre-OIDC users with pending invitations
		// or technical users like ch.dvbern.ebegu.inbox.util.TechnicalUserConfiguration
		if (externalUUID == null) {
			return null;
		}

		if (externalUUID.startsWith(PREFIX)) {
			int beginIndex = externalUUID.lastIndexOf(':') + 1;
			return externalUUID.substring(beginIndex);
		}
		return externalUUID;
	}

	public static boolean equals(String externalUUID1, String externalUUID2) {
		return Objects.equals(
			removePrefixIfNecessary(externalUUID1),
			removePrefixIfNecessary(externalUUID2)
		);
	}
}
