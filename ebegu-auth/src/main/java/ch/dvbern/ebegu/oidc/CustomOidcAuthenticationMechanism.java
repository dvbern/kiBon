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
import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.CredentialValidationResult.Status;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.security.enterprise.identitystore.openid.RefreshToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import ch.dvbern.ebegu.backchannellogout.SessionRegistry;
import ch.dvbern.ebegu.scolaris.ScolarisAuthentication;
import com.google.common.base.Strings;
import org.glassfish.soteria.mechanisms.openid.OpenIdState;
import org.glassfish.soteria.mechanisms.openid.domain.LogoutConfiguration;
import org.glassfish.soteria.mechanisms.openid.domain.OpenIdConfiguration;
import org.glassfish.soteria.mechanisms.openid.domain.RefreshTokenImpl;
import org.glassfish.soteria.servlet.HttpServletRequestDelegator;
import org.glassfish.soteria.servlet.HttpStorageController;
import org.glassfish.soteria.servlet.RequestData;
import org.wildfly.security.soteria.original.AuthenticationController;
import org.wildfly.security.soteria.original.OpenIdContextImpl;
import org.wildfly.security.soteria.original.OpenIdCredential;
import org.wildfly.security.soteria.original.StateController;
import org.wildfly.security.soteria.original.TokenController;

import static jakarta.security.enterprise.AuthenticationStatus.SEND_FAILURE;
import static jakarta.security.enterprise.AuthenticationStatus.SUCCESS;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.ERROR_DESCRIPTION_PARAM;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.ERROR_PARAM;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.EXPIRES_IN;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.ID_TOKEN_HINT;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.ORIGINAL_REQUEST;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.POST_LOGOUT_REDIRECT_URI;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.REFRESH_TOKEN;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.STATE;
import static jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant.TOKEN_TYPE;
import static jakarta.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import static jakarta.security.enterprise.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;
import static jakarta.ws.rs.core.Response.Status.OK;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.glassfish.soteria.Utils.isEmpty;
import static org.glassfish.soteria.Utils.isOneOf;

/**
 * Same as {@link org.wildfly.security.soteria.original.OpenIdAuthenticationMechanism} except that
 *
 * - It also allows HTTPS redirect URIs (because this Wildfly runs behind a reverse proxy)
 * - It adds try-with-resource statements in two places
 *
 * @see CustomAuthenticationController
 * @see OpenIdConfigurationProducer
 */
@Alternative
@Priority(1)
@ApplicationScoped
public class CustomOidcAuthenticationMechanism implements
	HttpAuthenticationMechanism {
	private static final Logger LOGGER = Logger.getLogger(
		CustomOidcAuthenticationMechanism.class.getName()
	);

	public static final String ORIGINAL_REQUEST_DATA_JSON =
		"org.glassfish.soteria.original.request.json";

	@Inject
	private OpenIdConfiguration configuration;

	@Inject
	private OpenIdContextImpl context;

	private IdentityStoreHandler identityStoreHandler;

	@Inject
	private AuthenticationController authenticationController;

	@Inject
	private TokenController tokenController;

	@Inject
	private StateController stateController;

	@Inject
	Instance<IdentityStoreHandler> storeHandlerInstance;

	@Inject
	private ScolarisAuthentication scolarisAuthentication;

	@Inject
	private SessionRegistry sessionRegistry;

	private static final Map<String, Object> SESSION_LOCKS =
		new ConcurrentHashMap<>();

	private static class Lock implements Serializable {
		private static final long serialVersionUID = 1L;
	}

	@PostConstruct
	void init() {
		if (storeHandlerInstance.isResolvable()) {
			identityStoreHandler = storeHandlerInstance.get();
			return;
		}

		throw new IllegalStateException(
			"Cannot get instance of IdentityStoreHandler\n"
				+
				"@Inject IdentityStoreHandler is unsatisfied."
		);
	}

	@Override
	public AuthenticationStatus validateRequest(
		HttpServletRequest request,
		HttpServletResponse response,
		HttpMessageContext httpContext
	) throws AuthenticationException {
		if (scolarisAuthentication.isScolarisApi(request)) {
			return scolarisAuthentication.loginScolarisUser(
				request,
				httpContext
			);
		}

		if (isNull(request.getUserPrincipal())) {
			LOGGER.fine(
				"UserPrincipal is not set, authenticate user using OpenId Connect protocol."
			);

			// User is not authenticated
			// Perform steps (1) to (6)
			return authenticate(request, response, httpContext);
		}

		// User has been authenticated in request before

		// Try-catch-block taken from AutoApplySessionInterceptor
		// We cannot use @AutoApplySession, because validateRequest(...) must be called on every request
		// to handle re-authentication (refreshing tokens)
		// https://stackoverflow.com/questions/51678821/soteria-httpmessagecontext-setregistersession-not-working-as-expected/51819055
		// https://github.com/javaee/security-soteria/blob/master/impl/src/main/java/org/glassfish/soteria/cdi/AutoApplySessionInterceptor.java
		try {
			httpContext.getHandler()
				.handle(
					new Callback[] {
						new CallerPrincipalCallback(
							httpContext.getClientSubject(),
							request.getUserPrincipal()
						) }
				);
		} catch (IOException | UnsupportedCallbackException ex) {
			throw new AuthenticationException(
				"Failed to register CallerPrincipalCallback.",
				ex
			);
		}

		// 1. Check for refreshing token
		boolean accessTokenExpired = context.getAccessToken().isExpired();
		boolean identityTokenExpired = context.getIdentityToken().isExpired();

		if ((accessTokenExpired || identityTokenExpired)
			&& configuration.isTokenAutoRefresh()) {
			if (accessTokenExpired) {
				LOGGER.fine(
					"Access Token is expired. Request new Access Token with Refresh Token."
				);
			}
			if (identityTokenExpired) {
				LOGGER.fine(
					"Identity Token is expired. Request new Identity Token with Refresh Token."
				);
			}
			return this.reAuthenticate(httpContext);
		}

		// 2. Check for logging out
		LogoutConfiguration logout = configuration.getLogoutConfiguration();
		if (logout.isIdentityTokenExpiry()) {
			LOGGER.log(
				FINE,
				"UserPrincipal is set, check if Identity Token is valid."
			);
		}

		if (logout.isAccessTokenExpiry()) {
			LOGGER.log(
				FINE,
				"UserPrincipal is set, check if Access Token is valid."
			);
		}

		if ((logout.isAccessTokenExpiry() && accessTokenExpired)
			|| (logout.isIdentityTokenExpiry() && identityTokenExpired)) {
			logout(request, response);
			return SEND_FAILURE;
		}

		// 3. Ignore token expired, or token is not expired
		return SUCCESS;
	}

	@Override
	public void cleanSubject(
		HttpServletRequest request,
		HttpServletResponse response,
		HttpMessageContext httpMessageContext
	) {
		logout(request, response);
	}

	private AuthenticationStatus authenticate(
		HttpServletRequest request,
		HttpServletResponse response,
		HttpMessageContext httpContext
	) {
		Optional<OpenIdState> receivedState = OpenIdState.from(
			request.getParameter(STATE)
		);

		if (receivedState.isEmpty()
			&& httpContext.isProtected()
			&& isNull(request.getUserPrincipal())) {
			// (1) The End-User is not authenticated.
			return authenticationController.authenticateUser(request, response);
		}

		if (receivedState.isPresent()) {
			String callbackUrl = configuration.buildRedirectURI(request);
			String originalUrl = getOriginalUrl(request, response);
			UriBuilder uriBuilder = UriBuilder.fromUri(
				URI.create(request.getRequestURL().toString())
			);
			var returnPath = request.getParameter(
				AuthConstants.RETURN_PATH_PARAM
			);
			if (!Strings.isNullOrEmpty(returnPath)) {
				uriBuilder.queryParam(
					AuthConstants.RETURN_PATH_PARAM,
					returnPath
				);
			}
			var requestUri = uriBuilder.build();
			String requestUrlWithHttps = UriBuilder.fromUri(
				requestUri
			).scheme("https").build().toString();
			var requestUrl = requestUri.toString();

			if (configuration.isRedirectToOriginalResource()) {
				if (!isOneOf(requestUrl, originalUrl, callbackUrl)
					&& !isOneOf(
						requestUrlWithHttps,
						originalUrl,
						callbackUrl
					)) {
					LOGGER.log(
						INFO,
						"OpenID request URL {0} not matched with either callback {1} or original URL {2}",
						new Object[] { requestUrl, callbackUrl, originalUrl }
					);
					return httpContext.notifyContainerAboutLogin(
						NOT_VALIDATED_RESULT
					);
				}
			} else {
				if (!isOneOf(requestUrl, callbackUrl)
					&& !isOneOf(requestUrlWithHttps, callbackUrl)) {
					LOGGER.log(
						INFO,
						"OpenID request URL {0} not matched with callback URL {1}",
						new Object[] { requestUrl, callbackUrl, originalUrl }
					);
					return httpContext.notifyContainerAboutLogin(
						NOT_VALIDATED_RESULT
					);
				}
			}

			Optional<OpenIdState> expectedState = stateController.get(
				request,
				response
			);
			if (!expectedState.isPresent()) {
				LOGGER.fine("Expected state not found");
				return httpContext.notifyContainerAboutLogin(
					NOT_VALIDATED_RESULT
				);
			}

			if (!expectedState.equals(receivedState)) {
				LOGGER.fine("Inconsistent received state, value not matched");
				return httpContext.notifyContainerAboutLogin(INVALID_RESULT);
			}

			// (3) Successful Authentication Response : redirect_uri?code=abc&state=123
			if (configuration.isRedirectToOriginalResource()
				&& !isOnOriginalURL(request, response)) {
				return httpContext.redirect(
					getOriginalRedirectUrl(request, response)
				);
			}

			// (3b) original_uri?code=abc&state=123 or redirect_uri?code=abc&state=123
			return validateAuthorizationCode(httpContext);
		}

		return httpContext.doNothing();
	}

	private boolean isOnOriginalURL(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		Optional<String> optionalOrginalUrl =
			HttpStorageController.getInstance(configuration, request, response)
				.getAsString(ORIGINAL_REQUEST);

		if (optionalOrginalUrl.isEmpty()) {
			// If no original url, return true so we don't redirect.
			return true;
		}

		String originalUrl = optionalOrginalUrl.get();

		if (originalUrl.contains("?")) {
			originalUrl = originalUrl.substring(0, originalUrl.indexOf('?'));
		}

		return request.getRequestURL().toString().equals(originalUrl);
	}

	private String getOriginalRedirectUrl(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		return getOriginalUrl(request, response)
			+ "?"
			+ request.getQueryString();
	}

	private String getOriginalUrl(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		String originalUrl =
			HttpStorageController.getInstance(configuration, request, response)
				.getAsString(ORIGINAL_REQUEST)
				.get(); // checked before

		if (originalUrl.contains("?")) {
			originalUrl = originalUrl.substring(0, originalUrl.indexOf('?'));
		}

		return originalUrl;
	}

	private RequestData getRequestData(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		String requestJson =
			HttpStorageController.getInstance(configuration, request, response)
				.getAsString(ORIGINAL_REQUEST_DATA_JSON)
				.get();

		return RequestData.of(requestJson);
	}

	/**
	 * (3) & (4-6) An Authorization Code returned to Client (RP) via Authorization Code Flow must be validated and
	 * exchanged for
	 * an ID Token, an Access Token and optionally a Refresh Token directly.
	 *
	 * @param httpContext the {@link HttpMessageContext} to validate authorization code from
	 * @return the authentication status.
	 */
	private AuthenticationStatus validateAuthorizationCode(
		HttpMessageContext httpContext
	) {
		HttpServletRequest request = httpContext.getRequest();
		HttpServletResponse response = httpContext.getResponse();
		String error = request.getParameter(ERROR_PARAM);
		String errorDescription = request.getParameter(ERROR_DESCRIPTION_PARAM);

		if (!isEmpty(error)) {
			// Error responses sent to the redirect_uri
			LOGGER.log(
				WARNING,
				"Error occurred in receiving Authorization Code : {0} caused by {1}",
				new Object[] { error, errorDescription }
			);
			return httpContext.notifyContainerAboutLogin(INVALID_RESULT);
		}

		stateController.remove(request, response);

		LOGGER.finer(
			"Authorization Code received, now fetching Access token & Id token"
		);

		JsonObject tokensObject;
		try (Response tokenResponse = tokenController.getTokens(request)) {
			tokensObject = readJsonObject(
				tokenResponse.readEntity(String.class)
			);
			if (tokenResponse.getStatus() == OK.getStatusCode()) {
				// Successful Token Response
				updateContext(tokensObject);
				OpenIdCredential credential = new OpenIdCredential(
					tokensObject,
					httpContext,
					configuration.getTokenMinValidity()
				);
				CredentialValidationResult validationResult =
					identityStoreHandler
						.validate(credential);

				// Register session manually (if @AutoApplySession used, this would be done by its interceptor)
				httpContext.setRegisterSession(
					validationResult.getCallerPrincipal().getName(),
					validationResult.getCallerGroups()
				);

				// registerSession here
				HttpSession session = request.getSession(false); // false, um keine neue zu erstellen, wenn keine da ist

				String subject = validationResult.getCallerPrincipal()
					.getName();

				if (subject != null && !subject.isEmpty()) {
					// Session in der Registry speichern
					sessionRegistry.registerSession(session, subject);
				}

				if (configuration.isRedirectToOriginalResource()) {
					// Restore request manually (if @LoginToContinue used, this would be done by its interceptor)
					httpContext.withRequest(
						new HttpServletRequestDelegator(
							request,
							getRequestData(request, response)
						)
					);
				}

				return httpContext.notifyContainerAboutLogin(validationResult);
			}
		}

		// Token Request is invalid or unauthorized
		LOGGER.log(
			WARNING,
			"Error occurred in validating Authorization Code : {0} caused by {1}",
			new Object[] {
				tokensObject.getString(ERROR_PARAM, "Unknown Error"),
				tokensObject.getString(ERROR_DESCRIPTION_PARAM, "Unknown") }
		);

		return httpContext.notifyContainerAboutLogin(INVALID_RESULT);

	}

	private AuthenticationStatus reAuthenticate(
		HttpMessageContext httpContext
	) {
		HttpServletRequest request = httpContext.getRequest();
		HttpServletResponse response = httpContext.getResponse();
		synchronized (getSessionLock(httpContext.getRequest())) {
			boolean accessTokenExpired = context.getAccessToken().isExpired();
			boolean identityTokenExpired = context.getIdentityToken()
				.isExpired();
			if (accessTokenExpired || identityTokenExpired) {

				if (accessTokenExpired) {
					LOGGER.fine(
						"Access Token is expired. Request new Access Token with Refresh Token."
					);
				}
				if (identityTokenExpired) {
					LOGGER.fine(
						"Identity Token is expired. Request new Identity Token with Refresh Token."
					);
				}

				AuthenticationStatus refreshStatus = context.getRefreshToken()
					.map(rt -> this.refreshTokens(httpContext, rt))
					.orElse(SEND_FAILURE);

				if (refreshStatus != SUCCESS) {
					LOGGER.log(
						FINE,
						"Failed to refresh token (Refresh Token might be invalid)."
					);
					logout(request, response);
				}
				return refreshStatus;
			}
		}

		return SUCCESS;
	}

	private AuthenticationStatus refreshTokens(
		HttpMessageContext httpContext,
		RefreshToken refreshToken
	) {
		JsonObject tokensObject;
		try (Response response = tokenController.refreshTokens(refreshToken)) {
			tokensObject = readJsonObject(
				response.readEntity(String.class)
			);

			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				// Successful Token Response
				updateContext(tokensObject);
				OpenIdCredential credential = new OpenIdCredential(
					tokensObject,
					httpContext,
					configuration.getTokenMinValidity()
				);
				CredentialValidationResult validationResult =
					identityStoreHandler
						.validate(credential);
				return validationResult.getStatus() == Status.VALID ?
					SUCCESS :
					SEND_FAILURE;
			}
		}

		// Token Request is invalid (refresh token invalid or expired)
		String error = tokensObject.getString(ERROR_PARAM, "Unknown Error");
		String errorDescription = tokensObject.getString(
			ERROR_DESCRIPTION_PARAM,
			"Unknown"
		);
		LOGGER.log(
			FINE,
			"Error occurred in refreshing Access Token and Refresh Token : {0} caused by {1}",
			new Object[] { error, errorDescription }
		);

		return SEND_FAILURE;
	}

	private void logout(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		LogoutConfiguration logout = configuration.getLogoutConfiguration();

		if (logout == null) {
			LOGGER.log(
				WARNING,
				"Logout invoked on session without OpenID session"
			);
			redirect(response, request.getContextPath());
			return;
		}

		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}

		/*
		 * See section 5. RP-Initiated Logout
		 * https://openid.net/specs/openid-connect-session-1_0.html#RPLogout
		 */
		if (logout.isNotifyProvider()
			&& !isEmpty(
				configuration.getProviderMetadata().getEndSessionEndpoint()
			)) {
			UriBuilder logoutURI =
				UriBuilder.fromUri(
					configuration.getProviderMetadata().getEndSessionEndpoint()
				)
					.queryParam(
						ID_TOKEN_HINT,
						context.getIdentityToken().getToken()
					);

			if (!isEmpty(logout.getRedirectURI())) {
				// User Agent redirected to POST_LOGOUT_REDIRECT_URI after a logout operation performed in OP.
				logoutURI.queryParam(
					POST_LOGOUT_REDIRECT_URI,
					logout.buildRedirectURI(request)
				);
			}

			redirect(response, logoutURI.build().toString());
		} else if (!isEmpty(logout.getRedirectURI())) {
			redirect(response, logout.buildRedirectURI(request));
		} else {
			// Redirect user to OpenID connect provider for re-authentication
			authenticationController.authenticateUser(request, response);
		}
	}

	private static void redirect(HttpServletResponse response, String uri) {
		try {
			response.sendRedirect(uri);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private JsonObject readJsonObject(String tokensBody) {
		LOGGER.fine("tokensBody = " + tokensBody);
		try (
			JsonReader reader = Json.createReader(
				new StringReader(tokensBody)
			)
		) {
			return reader.readObject();
		}
	}

	private void updateContext(JsonObject tokensObject) {
		context.setTokenType(tokensObject.getString(TOKEN_TYPE, null));

		String refreshToken = tokensObject.getString(REFRESH_TOKEN, null);
		if (nonNull(refreshToken)) {
			context.setRefreshToken(new RefreshTokenImpl(refreshToken));
		}
		JsonNumber expiresIn = tokensObject.getJsonNumber(EXPIRES_IN);
		if (nonNull(expiresIn)) {
			context.setExpiresIn(expiresIn.longValue());
		}
	}

	private Object getSessionLock(HttpServletRequest request) {
		String sessionId = request.getSession().getId();
		return SESSION_LOCKS.computeIfAbsent(sessionId, key -> new Object());
	}
}
