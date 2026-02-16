/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource.authentication;

import jakarta.ws.rs.core.Response;

public class LogoutResponseBuilder {

	private int statusCode = 200;
	private LogoutResponse logoutResponse;

	public LogoutResponseBuilder() {
		logoutResponse = new LogoutResponse();
	}

	public LogoutResponseBuilder withLogoutRedirect(String logoutUrl) {
		this.logoutResponse.setLogoutRedirect(logoutUrl);
		return this;
	}

	public LogoutResponseBuilder withUseDefaultLogoutRedirect() {
		this.logoutResponse.setUseDefaultLogoutRedirect(true);
		return this;
	}

	public LogoutResponseBuilder withLogoutSuccess(boolean logoutSuccess) {
		this.logoutResponse.setLogoutSuccess(logoutSuccess);
		return this;
	}

	public LogoutResponseBuilder withStatus(int httpStatusCode) {
		this.statusCode = httpStatusCode;
		return this;
	}

	public Response build() {
		return Response.status(statusCode).entity(logoutResponse).build();
	}
}
