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

package ch.dvbern.ebegu.scolaris;

import java.io.IOException;
import java.util.Set;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.internal.BasicAuthentication;
import org.jboss.resteasy.util.BasicAuthHelper;

import static jakarta.security.enterprise.AuthenticationStatus.SEND_FAILURE;

@Slf4j
@ApplicationScoped
public class ScolarisAuthentication {

	public static final String COOKIE_AUTHORIZATION_HEADER = "Authorization";

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	private final Client client = ClientBuilder.newClient();

	@PreDestroy
	void closeClient() {
		client.close();
	}

	public boolean isScolarisApi(HttpServletRequest request) {
		String apiBasePath = request.getContextPath()
			+ Constants.API_ROOT_PATH;
		String path = request.getRequestURI();
		return path.startsWith(apiBasePath + "/schulamt");
	}

	public AuthenticationStatus loginScolarisUser(
		HttpServletRequest request,
		HttpMessageContext httpMsgContext
	) {
		if (!isSchulamtApiActive()) {
			LOG.error(
				"Call to Schulamt API even though the properties for username and password were not defined "
					+ " in ebegu. Please check that the system properties for username/password for the schulamt api are "
					+ "set"
			);
			return setResponseUnauthorised(httpMsgContext);
		}

		try {
			String header = request.getHeader(COOKIE_AUTHORIZATION_HEADER);

			if (header == null) {
				return setResponseUnauthorised(httpMsgContext);
			}

			final String[] strings = BasicAuthHelper.parseHeader(header);

			if (strings == null || strings.length != 2) {
				// Basic Auth without username/password
				return setResponseUnauthorised(httpMsgContext);
			}

			final String scolarisGemeindeUsername = strings[0];
			final String scolarisGemeindePasswort = strings[1];

			Form form = new Form()
				.param("grant_type", "password")
				.param("username", scolarisGemeindeUsername)
				.param("password", scolarisGemeindePasswort);

			try (var response = authenticateWithKeycloak(form)) {
				if (isLoginValid(response)) {
					return httpMsgContext.notifyContainerAboutLogin(
						PrincipalBean.KIBON_SERVICE_ACCOUNT,
						Set.of(UserRoleName.SUPER_ADMIN)
					);
				}

				LOG.error(
					"Call to connector API with invalid BasicAuth header credentials"
				);
				return setResponseUnauthorised(httpMsgContext);
			}
		} catch (RuntimeException e) {
			LOG.error("Call to Schulamt API had an unrecoverable error", e);
			throw e;
		} catch (Exception ex) {
			return setResponseUnauthorised(httpMsgContext);
		}
	}

	private Response authenticateWithKeycloak(Form form) {
		return client
			.target(ebeguConfiguration.getKeycloackAuthServer())
			.register(
				new BasicAuthentication(
					ebeguConfiguration.getKeycloackClient(),
					ebeguConfiguration.getKeycloackPassword()
				)
			)
			.request(MediaType.APPLICATION_FORM_URLENCODED)
			.accept(MediaType.APPLICATION_JSON)
			.buildPost(Entity.form(form))
			.invoke();
	}

	private static boolean isLoginValid(Response response) {
		return response.getStatus()
			== Status.OK.getStatusCode();
	}

	private boolean isSchulamtApiActive() {
		return ebeguConfiguration.getKeycloackClient() != null
			&& ebeguConfiguration.getKeycloackPassword() != null
			&& ebeguConfiguration.getKeycloackAuthServer() != null;
	}

	private AuthenticationStatus setResponseUnauthorised(
		HttpMessageContext httpMsgContext
	) {
		try {
			httpMsgContext.getResponse()
				.sendError(Status.UNAUTHORIZED.getStatusCode());
		} catch (IOException e) {
			String message =
				"Error when trying to send 401 back because of missing Authorization";
			throw new IllegalStateException(message, e);
		}
		return SEND_FAILURE;
	}
}
