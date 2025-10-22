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

public final class AuthConstants {
	private AuthConstants() {
	}

	public static final String AUTH_RESOURCE_PATH = "auth";
	public static final String LOGIN_ENDPOINT = "login";
	public static final String LOGIN_PATH =
		("/%s/" + LOGIN_ENDPOINT).formatted(AUTH_RESOURCE_PATH);
	public static final String REGISTER_ENDPOINT = "register";
	public static final String REGISTER_PATH =
		("/%s/" + REGISTER_ENDPOINT).formatted(AUTH_RESOURCE_PATH);
	public static final String CALLBACK_ENDPOINT = "callback";

	public static final String CALLBACK_PATH =
		"/" + AUTH_RESOURCE_PATH + "/" + CALLBACK_ENDPOINT;

	public static final String RETURN_PATH_PARAM = "return_path";
	public static final int TOKEN_MIN_VALIDITY = 10_000;
}
