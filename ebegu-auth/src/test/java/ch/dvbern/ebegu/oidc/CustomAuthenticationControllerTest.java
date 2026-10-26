package ch.dvbern.ebegu.oidc;

import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.UriBuilder;

import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.KeycloakIdpHint;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.util.mandant.MandantCookieUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationControllerTest {

	@Mock
	private MandantService mandantService;

	@Mock
	private ApplicationPropertyService applicationPropertyService;

	@Mock
	private HttpServletRequest request;

	@InjectMocks
	private CustomAuthenticationController testee;

	@Test
	public void uatUrlShouldOnlyAdaptEnding() {
		UriBuilder uriBuilder = UriBuilder.fromUri(
			"https://uat-auth.kibon.ch/realms/bern/protocol/openid-connect/auth"
		);
		assertThat(
			CustomAuthenticationController.constructRegistrationPath(
				uriBuilder
			),
			is(
				"https://uat-auth.kibon.ch/realms/bern/protocol/openid-connect/registrations"
			)
		);
	}

	@Test
	public void prodUrlShouldOnlyAdaptEnding() {
		UriBuilder uriBuilder = UriBuilder.fromUri(
			"https://auth.kibon.ch/realms/bern/protocol/openid-connect/auth"
		);
		assertThat(
			CustomAuthenticationController.constructRegistrationPath(
				uriBuilder
			),
			is(
				"https://auth.kibon.ch/realms/bern/protocol/openid-connect/registrations"
			)
		);
	}

	@Nested
	class IdPHint {

		@Test
		public void noHintShouldNotSendIdpHint() {
			givenMandantWithHint(KeycloakIdpHint.NO_HINT);

			assertThat(testee.findIdpHintAlias(request), is(Optional.empty()));
		}

		@Test
		public void beloginLegacyShouldSendKeycloakOidcAlias() {
			givenMandantWithHint(KeycloakIdpHint.BELOGIN_LEGACY);

			assertThat(
				testee.findIdpHintAlias(request),
				is(Optional.of("keycloak-oidc"))
			);
		}

		@Test
		public void beloginCiamShouldSendBeloginCiamAlias() {
			givenMandantWithHint(KeycloakIdpHint.BELOGIN_CIAM);

			assertThat(
				testee.findIdpHintAlias(request),
				is(Optional.of("belogin-ciam"))
			);
		}

		@Test
		public void localloginShouldNeverSendIdpHint() {
			when(request.getParameterMap()).thenReturn(
				Map.of("login_hint", new String[] { "a.persona@mailbucket.dvbern.ch" })
			);

			assertThat(testee.findIdpHintAlias(request), is(Optional.empty()));
			verifyNoInteractions(mandantService, applicationPropertyService);
		}

		@Test
		public void missingPropertyShouldFail() {
			Mandant mandant = givenMandant();
			when(applicationPropertyService.getKeycloakIdpHint(mandant))
				.thenThrow(
					new EbeguRuntimeException("test", "nicht konfiguriert")
				);

			assertThrows(
				EbeguRuntimeException.class,
				() -> testee.findIdpHintAlias(request)
			);
		}

		private void givenMandantWithHint(KeycloakIdpHint hint) {
			Mandant mandant = givenMandant();
			when(applicationPropertyService.getKeycloakIdpHint(mandant))
				.thenReturn(hint);
		}

		private Mandant givenMandant() {
			Mandant mandant = new Mandant();
			when(request.getParameterMap()).thenReturn(Map.of());
			when(request.getCookies()).thenReturn(
				new Cookie[] {
					new Cookie(
						MandantCookieUtil.MANDANT_COOKIE_NAME,
						"Kanton%20Bern"
					)
				}
			);
			when(mandantService.findMandantByIdentifier(MandantIdentifier.BERN))
				.thenReturn(Optional.of(mandant));
			return mandant;
		}
	}

}
