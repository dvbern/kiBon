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

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.UriBuilder;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.glassfish.soteria.mechanisms.openid.OpenIdState;
import org.glassfish.soteria.mechanisms.openid.controller.NonceController;
import org.glassfish.soteria.mechanisms.openid.domain.OpenIdConfiguration;
import org.glassfish.soteria.mechanisms.openid.domain.OpenIdNonce;
import org.glassfish.soteria.servlet.HttpStorageController;
import org.glassfish.soteria.servlet.RequestData;
import org.wildfly.security.soteria.original.AuthenticationController;
import org.wildfly.security.soteria.original.OpenIdContextImpl;
import org.wildfly.security.soteria.original.StateController;

import static jakarta.security.enterprise.AuthenticationStatus.SEND_CONTINUE;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.CLIENT_ID;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.DISPLAY;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.NONCE;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.ORIGINAL_REQUEST;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.PROMPT;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.REDIRECT_URI;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.RESPONSE_MODE;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.RESPONSE_TYPE;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.SCOPE;
import static java.util.logging.Level.FINEST;
import static org.glassfish.soteria.Utils.isEmpty;
import static org.glassfish.soteria.mechanisms.OpenIdAuthenticationMechanism.ORIGINAL_REQUEST_DATA_JSON;

/**
 * Hacky customization of {@link AuthenticationController}
 *
 * It is identical except that
 * - it only redirects to the OIDC provider on /auth/login and returns HTTP 401
 * otherwise
 * - sets the redirect URI's scheme to HTTPS (because this Wildfly runs behind a
 * reverse proxy)
 *
 * Note:
 * - We extend from {@link AuthenticationController} because there is no
 * interface we can use to inject our implementation into
 * {@link OpenIdContextImpl}
 * - Two private methods were copied verbatim from
 * {@link AuthenticationController}
 *
 * @see CustomAuthenticationController
 * @see OpenIdConfigurationProducer
 */
@ApplicationScoped
@Alternative
@Priority(1)
@Typed(AuthenticationController.class)
public class CustomAuthenticationController
	extends
	AuthenticationController {

	@Inject
	private StateController stateController;

	@Inject
	private NonceController nonceController;

	@Inject
	private OpenIdConfiguration configuration;

	private static final Logger LOGGER = Logger.getLogger(
		CustomAuthenticationController.class.getName()
	);

	@Override
	public AuthenticationStatus authenticateUser(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		if (isMandantUnbekannt(request)) {
			return AuthenticationStatus.NOT_DONE;
		}
		/*
		 * Client prepares an authentication request and redirect to the
		 * Authorization Server. if query param value is invalid then OpenId
		 * Connect provider redirect to error page (hosted in OP domain).
		 */
		UriBuilder authRequest = UriBuilder.fromUri(
			configuration.getProviderMetadata().getAuthorizationEndpoint()
		)
			.queryParam(SCOPE, configuration.getScopes())
			.queryParam(RESPONSE_TYPE, configuration.getResponseType())
			.queryParam(CLIENT_ID, configuration.getClientId())
			.queryParam(
				REDIRECT_URI,
				URLEncoder.encode(
					// otherwise the return_path feature breaks,
					// because the '?' in the redirect_uri is not
					// encoded
					configuration.buildRedirectURI(request),
					StandardCharsets.UTF_8
				)
			);

		if (shouldForwardDirectlyToBelogin(request)) {
			// beim Bern muessen wir noch ein hinweis geben wenn man ins belogin sich direkt einloggen will
			authRequest.queryParam("kc_idp_hint", "keycloak-oidc");
		}

		OpenIdState state = new OpenIdState();
		authRequest.queryParam(OpenIdConstant.STATE, state.getValue());
		stateController.store(state, configuration, request, response);

		storeRequestURL(request, response);

		// Add nonce for replay attack prevention
		if (configuration.isUseNonce()) {
			OpenIdNonce nonce = new OpenIdNonce();
			// Use a cryptographic hash of the value as the nonce parameter
			String nonceHash = nonceController.getNonceHash(nonce);
			authRequest.queryParam(NONCE, nonceHash);
			nonceController.store(nonce, configuration, request, response);

		}
		if (!isEmpty(configuration.getResponseMode())) {
			authRequest.queryParam(
				RESPONSE_MODE,
				configuration.getResponseMode()
			);
		}
		if (!isEmpty(configuration.getDisplay())) {
			authRequest.queryParam(DISPLAY, configuration.getDisplay());
		}
		if (!isEmpty(configuration.getPrompt())) {
			authRequest.queryParam(PROMPT, configuration.getPrompt());
		}

		configuration.getExtraParameters().forEach(authRequest::queryParam);

		try {
			if (request.getPathInfo().equals(AuthConstants.LOGIN_PATH)) {
				var username = request.getParameter(OpenIdConstant.LOGIN_HINT);
				if (username != null) {
					authRequest.queryParam(
						OpenIdConstant.LOGIN_HINT,
						URLEncoder.encode(username, StandardCharsets.UTF_8)
					);
				}
				String authUrl = authRequest.build().toString();
				LOGGER.log(
					FINEST,
					"Redirecting for "
						+ AuthConstants.AUTH_RESOURCE_PATH
						+ "entication to {0}",
					authUrl
				);
				response.sendRedirect(authUrl);
			} else if (request.getPathInfo()
				.equals(AuthConstants.REGISTER_PATH)) {
				// https://keycloak.discourse.group/t/direct-registration-page-link/16026
				String keycloakRegistrationUrl = constructRegistrationPath(
					authRequest
				);
				LOGGER.log(
					FINEST,
					"Redirecting for registration to {0}",
					keycloakRegistrationUrl
				);
				response.sendRedirect(keycloakRegistrationUrl);
			} else {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		return SEND_CONTINUE;
	}

	public static String constructRegistrationPath(UriBuilder authRequest) {
		return authRequest.build()
			.toString()
			.replace(
				"openid-connect/" + AuthConstants.AUTH_RESOURCE_PATH,
				"openid-connect/registrations"
			);
	}

	private static boolean shouldForwardDirectlyToBelogin(
		HttpServletRequest request
	) {
		return (request.getParameterMap().get(OpenIdConstant.LOGIN_HINT) == null
			|| !request.getParameterMap()
				.get(OpenIdConstant.LOGIN_HINT)[0]
				.contains(".persona@mailbucket"))
			&& request.getRequestURL().toString().contains("be.kibon.ch");
	}

	private boolean isMandantUnbekannt(HttpServletRequest request) {
		return MandantIdentifier.findByHostname(
			URI.create(
				request.getRequestURL()
					.toString()
			)
		).isEmpty();
	}

	private void storeRequestURL(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		HttpStorageController storage = HttpStorageController.getInstance(
			configuration,
			request,
			response
		);

		storage.store(ORIGINAL_REQUEST, getFullURL(request));
		if (configuration.isRedirectToOriginalResource()) {
			storage.store(
				ORIGINAL_REQUEST_DATA_JSON,
				RequestData.of(request).toJson()
			);
		}
	}

	private String getFullURL(HttpServletRequest request) {
		StringBuilder requestURL = new StringBuilder(
			request.getRequestURL().toString()
		);
		String queryString = request.getQueryString();

		if (queryString == null) {
			return requestURL.toString();
		}

		return requestURL.append('?').append(queryString).toString();
	}

}
