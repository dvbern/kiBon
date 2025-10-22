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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.UriBuilder;

import com.google.common.base.Strings;
import org.glassfish.soteria.mechanisms.openid.domain.ClaimsConfiguration;
import org.glassfish.soteria.mechanisms.openid.domain.LogoutConfiguration;
import org.glassfish.soteria.mechanisms.openid.domain.OpenIdConfiguration;
import org.glassfish.soteria.mechanisms.openid.domain.OpenIdProviderData;
import org.wildfly.security.soteria.original.ConfigurationController;

/**
 * Produces an OpenIdConfiguration for each tenant.
 */
@Alternative
@Priority(1)
@ApplicationScoped
public class OpenIdConfigurationProducer {

	@Inject
	private ConfigurationController configurationController;

	@Inject
	private RealmResolver realmResolver;

	private final ConcurrentHashMap<String, OpenIdConfiguration> configCache =
		new ConcurrentHashMap<>();

	@Produces
	@RequestScoped
	public OpenIdConfiguration produceConfiguration(
		OpenIdAuthenticationMechanismDefinition definition,
		HttpServletRequest request
	) {
		return configCache.computeIfAbsent(
			realmResolver.getRealm(request),
			key -> new SchemeReplacingConfigWrapper(
				configurationController.buildConfig(definition)
			)
		);
	}

	/**
	 * Forwards all calls to {@link OpenIdConfiguration} except for
	 * {@link SchemeReplacingConfigWrapper#buildRedirectURI}, because we need to replace the
	 * scheme with https because this Wildfly runs behind a reverse proxy.
	 */
	static class SchemeReplacingConfigWrapper extends OpenIdConfiguration {

		private final OpenIdConfiguration openIdConfiguration;

		public SchemeReplacingConfigWrapper(
			OpenIdConfiguration openIdConfiguration
		) {
			this.openIdConfiguration = openIdConfiguration;
		}

		@Override
		public String buildRedirectURI(HttpServletRequest request) {
			String returnPath = request.getParameter(
				AuthConstants.RETURN_PATH_PARAM
			);
			UriBuilder uriBuilder = UriBuilder.fromUri(
				openIdConfiguration.buildRedirectURI(request)
			)
				.scheme("https");
			if (!Strings.isNullOrEmpty(returnPath)) {
				uriBuilder
					.queryParam(
						AuthConstants.RETURN_PATH_PARAM,
						returnPath
					);
			}
			return uriBuilder
				.build()
				.toString();
		}

		@Override
		public String getClientId() {
			return openIdConfiguration.getClientId();
		}

		@Override
		public OpenIdConfiguration setClientId(String clientId) {
			return openIdConfiguration.setClientId(clientId);
		}

		@Override
		public char[] getClientSecret() {
			return openIdConfiguration.getClientSecret();
		}

		@Override
		public OpenIdConfiguration setClientSecret(char[] clientSecret) {
			return openIdConfiguration.setClientSecret(clientSecret);
		}

		@Override
		public String getRedirectURI() {
			return openIdConfiguration.getRedirectURI();
		}

		@Override
		public OpenIdConfiguration setRedirectURI(String redirectURI) {
			return openIdConfiguration.setRedirectURI(redirectURI);
		}

		@Override
		public boolean isRedirectToOriginalResource() {
			return openIdConfiguration.isRedirectToOriginalResource();
		}

		@Override
		public OpenIdConfiguration setRedirectToOriginalResource(
			boolean redirectToOriginalResource
		) {
			return openIdConfiguration.setRedirectToOriginalResource(
				redirectToOriginalResource
			);
		}

		@Override
		public String getScopes() {
			return openIdConfiguration.getScopes();
		}

		@Override
		public OpenIdConfiguration setScopes(String scopes) {
			return openIdConfiguration.setScopes(scopes);
		}

		@Override
		public String getResponseType() {
			return openIdConfiguration.getResponseType();
		}

		@Override
		public OpenIdConfiguration setResponseType(String responseType) {
			return openIdConfiguration.setResponseType(responseType);
		}

		@Override
		public String getResponseMode() {
			return openIdConfiguration.getResponseMode();
		}

		@Override
		public OpenIdConfiguration setResponseMode(String responseMode) {
			return openIdConfiguration.setResponseMode(responseMode);
		}

		@Override
		public Map<String, String> getExtraParameters() {
			return openIdConfiguration.getExtraParameters();
		}

		@Override
		public OpenIdConfiguration setExtraParameters(
			Map<String, String> extraParameters
		) {
			return openIdConfiguration.setExtraParameters(extraParameters);
		}

		@Override
		public String getPrompt() {
			return openIdConfiguration.getPrompt();
		}

		@Override
		public OpenIdConfiguration setPrompt(String prompt) {
			return openIdConfiguration.setPrompt(prompt);
		}

		@Override
		public String getDisplay() {
			return openIdConfiguration.getDisplay();
		}

		@Override
		public OpenIdConfiguration setDisplay(String display) {
			return openIdConfiguration.setDisplay(display);
		}

		@Override
		public boolean isUseNonce() {
			return openIdConfiguration.isUseNonce();
		}

		@Override
		public OpenIdConfiguration setUseNonce(boolean useNonce) {
			return openIdConfiguration.setUseNonce(useNonce);
		}

		@Override
		public boolean isUseSession() {
			return openIdConfiguration.isUseSession();
		}

		@Override
		public int getJwksConnectTimeout() {
			return openIdConfiguration.getJwksConnectTimeout();
		}

		@Override
		public OpenIdConfiguration setJwksConnectTimeout(
			int jwksConnectTimeout
		) {
			return openIdConfiguration.setJwksConnectTimeout(
				jwksConnectTimeout
			);
		}

		@Override
		public int getJwksReadTimeout() {
			return openIdConfiguration.getJwksReadTimeout();
		}

		@Override
		public OpenIdConfiguration setJwksReadTimeout(int jwksReadTimeout) {
			return openIdConfiguration.setJwksReadTimeout(jwksReadTimeout);
		}

		@Override
		public OpenIdConfiguration setUseSession(boolean useSession) {
			return openIdConfiguration.setUseSession(useSession);
		}

		@Override
		public OpenIdProviderData getProviderMetadata() {
			return openIdConfiguration.getProviderMetadata();
		}

		@Override
		public OpenIdConfiguration setProviderMetadata(
			OpenIdProviderData providerMetadata
		) {
			return openIdConfiguration.setProviderMetadata(providerMetadata);
		}

		@Override
		public ClaimsConfiguration getClaimsConfiguration() {
			return openIdConfiguration.getClaimsConfiguration();
		}

		@Override
		public OpenIdConfiguration setClaimsConfiguration(
			ClaimsConfiguration claimsConfiguration
		) {
			return openIdConfiguration.setClaimsConfiguration(
				claimsConfiguration
			);
		}

		@Override
		public LogoutConfiguration getLogoutConfiguration() {
			return openIdConfiguration.getLogoutConfiguration();
		}

		@Override
		public OpenIdConfiguration setLogoutConfiguration(
			LogoutConfiguration logoutConfiguration
		) {
			return openIdConfiguration.setLogoutConfiguration(
				logoutConfiguration
			);
		}

		@Override
		public boolean isTokenAutoRefresh() {
			return openIdConfiguration.isTokenAutoRefresh();
		}

		@Override
		public OpenIdConfiguration setTokenAutoRefresh(
			boolean tokenAutoRefresh
		) {
			return openIdConfiguration.setTokenAutoRefresh(tokenAutoRefresh);
		}

		@Override
		public int getTokenMinValidity() {
			return openIdConfiguration.getTokenMinValidity();
		}

		@Override
		public OpenIdConfiguration setTokenMinValidity(int tokenMinValidity) {
			return openIdConfiguration.setTokenMinValidity(tokenMinValidity);
		}
	}

}
