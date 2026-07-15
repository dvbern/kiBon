/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

package ch.dvbern.ebegu.api.resource.authentication;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;

import ch.dvbern.ebegu.api.converter.JaxBenutzerConverter;
import ch.dvbern.ebegu.api.dtos.JaxBenutzer;
import ch.dvbern.ebegu.api.dtos.JaxMandant;
import ch.dvbern.ebegu.authentication.KibonJwt;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.oidc.AuthConstants;
import ch.dvbern.ebegu.oidc.CustomAuthenticationController;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.CreateBenutzerService;
import ch.dvbern.ebegu.services.authentication.KeycloakApi;
import ch.dvbern.ebegu.services.steuerabfrage.nesko.ZPVUpdateResult;
import ch.dvbern.ebegu.services.steuerabfrage.nesko.ZPVUpdateUtil;
import ch.dvbern.ebegu.services.steuerabfrage.nesko.ZpvService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.URLUtil;
import ch.dvbern.ebegu.util.mandant.MandantCookieUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import static ch.dvbern.ebegu.enums.ErrorCodeEnum.ERROR_EMAIL_MISMATCH;

/**
 * This resource has functions to login or logout
 */
@Slf4j
@Stateless
@Path(AuthConstants.AUTH_RESOURCE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@DenyAll
public class AuthResource {

	private static final String COOKIE_PATH = "/";
	private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
	private static final String COOKIE_MANDANT_REDIRECT = "mandantRedirect";

	@Inject
	private PrincipalBean principalBean;

	@Context
	private HttpServletRequest request;

	@Inject
	private JaxBenutzerConverter converter;

	@Inject
	private EbeguConfiguration configuration;

	@Inject
	private KibonJwt kibonJwt;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private KeycloakApi keycloakApi;

	@Inject
	private CreateBenutzerService createBenutzerService;

	@Inject
	private ZpvService zpvService;

	private boolean isCookieSecure() {
		final boolean forceCookieSecureFlag = configuration
			.forceCookieSecureFlag();
		return isRequestProtocolSecure() || forceCookieSecureFlag;
	}

	private boolean isRequestProtocolSecure() {
		// get protocol of original request if present
		final String originalProtocol = request.getHeader(
			X_FORWARDED_PROTO
		);
		if (originalProtocol != null) {
			return originalProtocol.startsWith("https");
		}
		return request.isSecure();
	}

	@Nonnull
	private NewCookie expireCookie(
		@Nonnull String name,
		boolean secure,
		boolean httpOnly,
		@Nullable String domain
	) {
		return new NewCookie.Builder(name).value("")
			.path("/ebegu")
			.domain(domain)
			.maxAge(0)
			.secure(secure)
			.httpOnly(httpOnly)
			.build();
	}

	@Nullable
	@POST
	@PermitAll
	@Path("/set-mandant")
	@Produces(MediaType.TEXT_PLAIN)
	public Response setMandant(@Nonnull final JaxMandant mandant) {
		// expire old mandant cookie for whole domain
		NewCookie expiredCookie = expireCookie(
			MandantCookieUtil.MANDANT_COOKIE_NAME,
			isCookieSecure(),
			true,
			configuration.getHostdomain()
		);
		// Readable Cookie storing the mandant
		NewCookie mandantCookie = new NewCookie(
			MandantCookieUtil.MANDANT_COOKIE_NAME,
			URLEncoder.encode(mandant.getName(), StandardCharsets.UTF_8),
			COOKIE_PATH,
			null,
			"mandant",
			60 * 60 * 24 * 365 * 2,
			isCookieSecure(),
			false
		);

		return Response.noContent()
			.cookie(expiredCookie, mandantCookie)
			.build();
	}

	@Nullable
	@POST
	@PermitAll
	@Path("/set-mandant-redirect")
	@Produces(MediaType.TEXT_PLAIN)
	public Response setMandantRedirect(@Nonnull final JaxMandant mandant) {
		// Readable Cookie storing the mandant used for client redirects
		NewCookie mandantCookie = new NewCookie(
			COOKIE_MANDANT_REDIRECT,
			URLEncoder.encode(mandant.getName(), StandardCharsets.UTF_8),
			COOKIE_PATH,
			configuration.getHostdomain(),
			"mandant",
			60 * 60 * 24 * 365 * 2,
			isCookieSecure(),
			false
		);

		return Response.noContent().cookie(mandantCookie).build();
	}

	@Nullable
	@GET
	@RolesAllowed("**")
	@Path("/authenticated-user")
	@Produces(MediaType.APPLICATION_JSON)
	public JaxBenutzer getAuthenticatedUser() {
		Benutzer benutzer = principalBean.getBenutzer();
		return converter.benutzerToJaxBenutzer(benutzer);
	}

	@GET
	@RolesAllowed("**")
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON)
	public Response logout(
		@Context HttpServletRequest request,
		@QueryParam(
			OpenIdConstant.LOGIN_HINT
		) String loginHint
	) {
		// kiBon-Logout ausführen
		principalBean.findBenutzer().ifPresent(keycloakApi::logout);
		invalidateSession(request);

		if (isUserOfRealmBern()) {
			return redirectToBeLoginLogout();
		}
		return redirectToLogoutTarget(request, loginHint);
	}

	private Response redirectToLogoutTarget(
		HttpServletRequest request,
		String loginHint
	) {
		// zur kiBon Startsetite navigieren
		if (Strings.isNullOrEmpty(loginHint)) {
			return new LogoutResponseBuilder().withStatus(200)
				.withLogoutSuccess(true)
				.withUseDefaultLogoutRedirect()
				.build();
		}
		URI loginWithHint = UriBuilder.fromUri(
			URI.create(request.getRequestURL().toString())
		)
			.replacePath(request.getContextPath())
			.path(Constants.API_ROOT_PATH)
			.path(AuthConstants.LOGIN_PATH)
			.queryParam(OpenIdConstant.LOGIN_HINT, loginHint)
			.scheme("https")
			.build();

		return new LogoutResponseBuilder().withStatus(200)
			.withLogoutSuccess(true)
			.withLogoutRedirect(loginWithHint.toString())
			.build();
	}

	private Response redirectToBeLoginLogout() {
		// für Benutzer des Mandanten Bern soll der kiBon-Logout auch gleichzeitig zum AGOV-Logout führen.
		// dafür wurde im IDP des Mandanten eine URL hinterlegt (diese findet man dann im Keycloak UI,
		// Mandant Bern -> Identity Providers -> BE-Login -> OpenID Connect settings -> Logout URL).
		String logoutURL = keycloakApi.getIDPLogoutUrl(
			MandantIdentifier.BERN.getRealmName(),
			"keycloak-oidc"
		);

		return new LogoutResponseBuilder().withStatus(200)
			.withLogoutSuccess(true)
			.withLogoutRedirect(logoutURL)
			.build();
	}

	private boolean isUserOfRealmBern() {
		return principalBean.getMandant()
			.getMandantIdentifier()
			== MandantIdentifier.BERN;
	}

	/**
	 * Mainly exists to trigger a redirect by {@link CustomAuthenticationController}
	 */
	@GET
	@RolesAllowed("**")
	@Path(AuthConstants.LOGIN_ENDPOINT)
	@Produces(MediaType.TEXT_PLAIN)
	public Response login(
		@QueryParam(
			AuthConstants.RETURN_PATH_PARAM
		) String returnPath
	) {
		return redirectToFrontend(request, returnPath).build();
	}

	/**
	 * Mainly exists to trigger a redirect by {@link CustomAuthenticationController}
	 */
	@GET
	@PermitAll
	@Path(AuthConstants.REGISTER_ENDPOINT)
	@Produces(MediaType.TEXT_PLAIN)
	public Response register() {
		return redirectToFrontend(request, "").build();
	}

	/**
	 * keycloak redirects to here after login.
	 */
	@GET
	@RolesAllowed("**")
	@Path(AuthConstants.CALLBACK_ENDPOINT)
	@Produces(MediaType.TEXT_PLAIN)
	public Response callback(
		@Context HttpServletRequest request,
		@QueryParam(
			AuthConstants.RETURN_PATH_PARAM
		) String returnPath
	) {
		if (kibonJwt.hasInvalidMandantClaims()) {
			invalidateSession(request);
			return Response.status(Status.BAD_REQUEST)
				.entity(
					"Invalid JWT. Some essential claims are missing or invalid."
				)
				.build();
		}

		if (isRequestZpvLinking(returnPath)) {
			var result = linkZpvToGesuchsteller(returnPath);
			invalidateSession(request);
			return ZPVUpdateUtil.redirectToFrontend(request, returnPath, result)
				.build();
		}

		Optional<Benutzer> invitedUserOpt = benutzerService
			.findUserWithInvitation(kibonJwt.getExternalUUID());

		String email = kibonJwt.getEmail();

		if (invitedUserOpt.isPresent()) {
			var benutzer = invitedUserOpt.get();

			if (!benutzer.getEmail().equals(email)) {
				var errorMessage = ServerMessageUtil.translateEnumValue(
					ERROR_EMAIL_MISMATCH,
					LocaleThreadLocal.get(),
					benutzer.getMandant(),
					benutzer.getEmail(),
					email
				);
				invalidateSession(request);
				return Response.status(Status.BAD_REQUEST)
					.entity(errorMessage)
					.build();
			}

			benutzer.setStatus(BenutzerStatus.AKTIV);
			benutzer.setNachname(kibonJwt.getNachname());
			benutzer.setVorname(kibonJwt.getVorname());
		} else {
			Optional<Benutzer> benutzerOptional = benutzerService.findBenutzer(
				kibonJwt
			);

			if (benutzerOptional.isEmpty()) {
				createBenutzerService.createNewBenutzerFromJwt();
			} else {
				var benutzer = benutzerOptional.get();
				if (benutzer.isGesperrt()) {
					invalidateSession(request);
					return Response.status(Status.FORBIDDEN)
						.build();
				}
				migrateExternalUuidIfNecessary(benutzer);
				benutzer.setNachname(
					kibonJwt.getNachname()
				);
				benutzer.setVorname(
					kibonJwt.getVorname()
				);
				benutzer.setEmail(email);
				benutzer.setZpvNummer(kibonJwt.getZpvNummer());
			}
		}

		return redirectToFrontend(request, returnPath).build();
	}

	private void migrateExternalUuidIfNecessary(Benutzer benutzer) {
		Optional<String> beLoginPrimaryId = kibonJwt.getBeLoginPrimaryId();

		if (beLoginPrimaryId.isEmpty()) {
			return;
		}

		if (Objects.equals(
			benutzer.getExternalUUID(),
			beLoginPrimaryId.get()
		)) {
			LOG.info(
				"Migrating BELogin externalUUID from {} to {} for Benutzer"
					+ " {}",
				benutzer.getExternalUUID(),
				kibonJwt.getExternalUUID(),
				benutzer.getId()
			);
			benutzer.setExternalUUID(kibonJwt.getExternalUUID());
		}

		if (!Objects.equals(
			benutzer.getExternalUUID(),
			kibonJwt.getExternalUUID()
		)) {
			LOG.warn(
				"Bern externalUUID does not match beLoginPrimaryId or externalUUID from Keycloak. ExternalUUID needs to be "
					+ "migrated from {} to {} for Benutzer {}",
				benutzer.getExternalUUID(),
				kibonJwt.getExternalUUID(),
				benutzer.getId()
			);
			benutzer.setExternalUUID(kibonJwt.getExternalUUID());
		}
	}

	private ZPVUpdateResult linkZpvToGesuchsteller(String returnPath) {
		var gesuchstellerId = getGeuchstellerIdFromReturnPath(returnPath);
		var result = zpvService.updateGesuchstellerZPVNr(
			gesuchstellerId,
			kibonJwt.getZpvNummer()
		);

		if (result != ZPVUpdateResult.SUCCESS) {
			return result;
		}

		Optional<Benutzer> existingUserOpt = benutzerService
			.findBenutzerByExternalUUID(kibonJwt.getExternalUUID());

		// Wenn noch kein User in kiBon existiert, soll dieser im Keycloak auch wieder gelöscht werden
		if (existingUserOpt.isEmpty()) {
			keycloakApi.deleteByExternalUUID(
				kibonJwt.getExternalUUID(),
				kibonJwt.getMandantIdentifier()
			);
		}
		return result;
	}

	private String getGeuchstellerIdFromReturnPath(String returnPath) {
		return returnPath.substring(returnPath.lastIndexOf('/') + 1);
	}

	private void invalidateSession(HttpServletRequest request) {
		Optional.ofNullable(request.getSession(false))
			.ifPresent(HttpSession::invalidate);
	}

	private boolean isRequestZpvLinking(@Nullable String returnPath) {
		if (returnPath == null) {
			return false;
		}

		return returnPath.contains(Constants.ZPV_LINK_SUCCESSS_PATH);
	}

	/**
	 * Builds a redirect response to the frontend application.
	 * <p>
	 * Note, that {@code frontendPathOrFragment} can be either an absolute URL or a fragment. Which option is used
	 * depends on how the frontend initiates the login process, resp. whether it sends a full URL or a fragment
	 * as return path.
	 * </p>
	 * <p>
	 * If {@code frontendPathOrFragment} is a valid absolute URL (see {@link #isValidURL}),
	 * its path and fragment are appended to the server's own host, with the
	 * scheme forced to {@code https}
	 * <p>
	 * Otherwise, {@code frontendPathOrFragment} is treated as an Angular route and is appended
	 * as the URL fragment (e.g. {@code #/faelle}) to this server's own host, with the
	 * scheme forced to {@code https} and the path reset to {@code /}.
	 * <p>
	 *
	 * <p>
	 * Note, that since {@code frontendPathOrFragment} (doesn't matter if it is a full path or just
	 * the fragment) may contain query params, e.g. https://be.kibon.ch/#/faelle?query=hi or /faelle?query=hi. In
	 * both of these cases, the query params are treated as part of the fragment and are
	 * therefor conserved to be treated by angular in the frontend
	 * </p>
	 *
	 * @param request the current request, used to derive the host
	 * @param frontendPathOrFragment either an absolute URL to derive the fragment from, or a relative
	 * frontend route to be used as the URL fragment (e.g. {@code /faelle})
	 * @return a {@link ResponseBuilder} pre-configured with a 307 Temporary Redirect
	 * to the resolved target
	 */
	private ResponseBuilder redirectToFrontend(
		HttpServletRequest request,
		String frontendPathOrFragment
	) {
		var builder = UriBuilder.fromUri(
			URI.create(request.getRequestURL().toString())
		).scheme("https").replacePath("/");

		if (URLUtil.isValidHttpOrHttpsURL(frontendPathOrFragment)) {
			var fullUri = UriBuilder.fromUri(frontendPathOrFragment).build();
			builder.fragment(fullUri.getFragment());
		} else {
			builder.replacePath("/").fragment(frontendPathOrFragment);
		}
		var url = builder
			.build();
		LOG.info("Redirecting to frontend with URL: {}", url);
		return Response.temporaryRedirect(
			url
		);
	}

}
