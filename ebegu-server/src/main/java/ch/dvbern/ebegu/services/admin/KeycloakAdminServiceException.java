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

/**
 * Exception wrapper being used by keycloak admin clients. Exceptions of this type address any issues using
 * the Keycloak admin API for any kiBon concerns. Such issues might be communication failues like when Keycloak
 * instance is not running or bad parametration of requests like for instance invalid user role identifiers.
 */
public class KeycloakAdminServiceException extends RuntimeException {

	public KeycloakAdminServiceException(String message) {
		super(message);
	}

	public KeycloakAdminServiceException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
}
