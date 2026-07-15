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

package ch.dvbern.ebegu.api.resource.authentication;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Response.Status;

import ch.dvbern.ebegu.api.converter.JaxBenutzerConverter;
import ch.dvbern.ebegu.authentication.KibonJwt;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.CreateBenutzerService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.authentication.KeycloakApi;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ExtendWith(EasyMockExtension.class)
class AuthResourceTest extends EasyMockSupport {
	@Mock
	private PrincipalBean principalBean;

	@Mock
	private HttpServletRequest request;

	@Mock
	private JaxBenutzerConverter converter;

	@Mock
	private EbeguConfiguration configuration;

	@Mock
	private MandantService mandantService;

	@Mock
	private KibonJwt kibonJwt;

	@Mock
	private BenutzerService benutzerService;

	@Mock
	private KeycloakApi keycloakApi;

	@Mock
	private CreateBenutzerService createBenutzerService;

	@TestSubject
	AuthResource testee = new AuthResource();

	String KEYCLOAK_UUID = "ee195064-a02a-11ef-bcbb-a32384e48695";

	@Nested
	class Logout {
		@Test
		void mustTerminateExistingSession() {
			// given
			Mandant mandant = EasyMock.createMock(Mandant.class);
			EasyMock.expect(mandant.getMandantIdentifier())
				.andReturn(MandantIdentifier.LUZERN);
			expect(principalBean.getMandant()).andReturn(mandant);
			Benutzer benutzer = new Benutzer();
			expect(principalBean.findBenutzer()).andReturn(
				Optional.of(benutzer)
			);
			HttpSession mockSession = mock(HttpSession.class);
			mockSession.invalidate();
			expectLastCall();
			expect(request.getSession(false)).andReturn(mockSession);

			keycloakApi.logout(benutzer);
			expectLastCall();

			expect(request.getRequestURL()).andReturn(
				new StringBuffer(
					"https://somehost.com/logout"
				)
			);
			expect(request.getContextPath()).andReturn("context");

			replayAll();

			// when
			var response = testee.logout(request, "some@mail.com");

			// then
			assertThat(
				response.getStatusInfo().getStatusCode(),
				Is.is(
					Status.OK.getStatusCode()
				)
			);

			LogoutResponse logoutResponse = (LogoutResponse) response
				.getEntity();

			assertTrue(
				logoutResponse.isLogoutSuccess()
			);

			assertEquals(
				"https://somehost.com/context/api/v1/auth/login?login_hint=some%40mail.com",
				logoutResponse.getLogoutRedirect()
			);
		}

		@Test
		void mustRedirectToBELoginLogoutPage() {
			// given
			Benutzer benutzer = new Benutzer();
			expect(principalBean.findBenutzer()).andReturn(
				Optional.of(benutzer)
			);

			HttpSession mockSession = mock(HttpSession.class);
			mockSession.invalidate();
			expectLastCall();
			expect(request.getSession(false)).andReturn(mockSession);

			keycloakApi.logout(benutzer);
			expectLastCall();

			expect(request.getRequestURL()).andReturn(
				new StringBuffer(
					"https://somehost.com/logout"
				)
			);
			expect(request.getContextPath()).andReturn("context");

			Mandant mandant = createMock(Mandant.class);
			expect(mandant.getMandantIdentifier()).andReturn(
				MandantIdentifier.BERN
			);
			expect(principalBean.getMandant()).andReturn(mandant);

			expect(
				keycloakApi.getIDPLogoutUrl(
					MandantIdentifier.BERN.getRealmName(),
					"keycloak-oidc"
				)
			).andReturn("https://somehost.com/belogin/logout");

			replayAll();

			// when
			var response = testee.logout(request, "some@mail.com");

			// then
			assertThat(
				response.getStatusInfo().getStatusCode(),
				Is.is(
					Status.OK.getStatusCode()
				)
			);

			LogoutResponse logoutResponse = (LogoutResponse) response
				.getEntity();

			assertTrue(
				logoutResponse.isLogoutSuccess()
			);

			assertEquals(
				"https://somehost.com/belogin/logout",
				logoutResponse.getLogoutRedirect()
			);
		}

		@Test
		void mustRedirectToLoginIfLoginHintIsPresent() {
			// given
			Mandant mandant = EasyMock.createMock(Mandant.class);
			EasyMock.expect(mandant.getMandantIdentifier())
				.andReturn(MandantIdentifier.LUZERN);
			expect(principalBean.getMandant()).andReturn(mandant);
			expect(principalBean.findBenutzer()).andReturn(Optional.empty());
			expect(request.getSession(false)).andReturn(null);

			expect(request.getRequestURL()).andReturn(
				new StringBuffer(
					"https://somehost.com/logout"
				)
			);
			expect(request.getContextPath()).andReturn("context");

			replayAll();

			// when
			var response = testee.logout(request, "some@mail.com");

			// then
			assertThat(
				response.getStatusInfo().getStatusCode(),
				Is.is(
					Status.OK.getStatusCode()
				)
			);

			LogoutResponse logoutResponse = (LogoutResponse) response
				.getEntity();

			assertTrue(
				logoutResponse.isLogoutSuccess()
			);

			assertEquals(
				"https://somehost.com/context/api/v1/auth/login?login_hint=some%40mail.com",
				logoutResponse.getLogoutRedirect()
			);
		}

		@Test
		void mustRedirectToFrontendIfNoLoginHint() {
			// given
			Mandant mandant = EasyMock.createMock(Mandant.class);
			EasyMock.expect(mandant.getMandantIdentifier())
				.andReturn(MandantIdentifier.LUZERN);
			expect(principalBean.getMandant()).andReturn(mandant);
			expect(principalBean.findBenutzer()).andReturn(Optional.empty());
			expect(request.getSession(false)).andReturn(null);

			expect(request.getRequestURL()).andReturn(
				new StringBuffer(
					"https://somehost.com/logout"
				)
			);
			expect(request.getContextPath()).andReturn("context");

			replayAll();

			// when
			var response = testee.logout(request, "");

			// then
			assertThat(
				response.getStatusInfo().getStatusCode(),
				Is.is(
					Status.OK.getStatusCode()
				)
			);
			LogoutResponse logoutResponse = (LogoutResponse) response
				.getEntity();

			assertTrue(
				logoutResponse.isLogoutSuccess()
			);
			assertTrue(
				logoutResponse.isUseDefaultLogoutRedirect()
			);
		}
	}

	@Nested
	class Callback {
		@Test
		void mustInvalidateSessionIfBenutzerIsGesperrt() {
			// given
			HttpSession mockSession = mock(HttpSession.class);
			mockSession.invalidate();
			expectLastCall();
			expect(request.getSession(false)).andReturn(mockSession);

			expect(kibonJwt.hasInvalidMandantClaims()).andReturn(false);
			expect(kibonJwt.getExternalUUID()).andReturn(KEYCLOAK_UUID);
			expect(kibonJwt.getEmail()).andReturn("some@email.com");

			var benutzer = new Benutzer();
			benutzer.setStatus(BenutzerStatus.GESPERRT);

			expect(benutzerService.findUserWithInvitation(KEYCLOAK_UUID))
				.andReturn(Optional.empty());
			expect(benutzerService.findBenutzer(kibonJwt)).andReturn(
				Optional.of(benutzer)
			);

			replayAll();

			// when
			var response = testee.callback(request, "/frontend");

			// then
			assertThat(
				response.getStatusInfo().getStatusCode(),
				Is.is(
					Status.FORBIDDEN.getStatusCode()
				)
			);
		}

		@Test
		void mustCreateNewBenutzer() {
			// given
			expect(kibonJwt.hasInvalidMandantClaims()).andReturn(false);
			expect(kibonJwt.getExternalUUID()).andReturn(KEYCLOAK_UUID);
			expect(kibonJwt.getEmail()).andReturn("some@email.com");

			expect(request.getRequestURL()).andReturn(
				new StringBuffer(
					"https://be.kibon.ch/ebegu/api/v1/auth/callback"
				)
			);

			createBenutzerService.createNewBenutzerFromJwt();
			expectLastCall();

			expect(benutzerService.findBenutzer(kibonJwt)).andReturn(
				Optional.empty()
			);
			expect(benutzerService.findUserWithInvitation(KEYCLOAK_UUID))
				.andReturn(Optional.empty());

			replayAll();

			// when
			testee.callback(request, "/frontend");
		}

		@Test
		void mustRedirectToUrl_WhenUserIsCreatedAndFragmentReturnPathGiven() {
			// given
			expect(kibonJwt.hasInvalidMandantClaims()).andReturn(false);
			expect(kibonJwt.getExternalUUID()).andReturn(KEYCLOAK_UUID);
			expect(kibonJwt.getEmail()).andReturn("some@email.com");

			expect(request.getRequestURL()).andReturn(
				new StringBuffer(
					"https://be.kibon.ch/ebegu/api/v1/auth/callback"
				)
			);

			createBenutzerService.createNewBenutzerFromJwt();
			expectLastCall();

			expect(benutzerService.findBenutzer(kibonJwt)).andReturn(
				Optional.empty()
			);
			expect(benutzerService.findUserWithInvitation(KEYCLOAK_UUID))
				.andReturn(Optional.empty());

			replayAll();

			// when
			var response = testee.callback(request, "/frontend");

			// then
			assertThat(
				response.getStatusInfo().getStatusCode(),
				Is.is(
					Status.TEMPORARY_REDIRECT.getStatusCode()
				)
			);
			assertThat(
				response.getLocation().toString(),
				Is.is("https://be.kibon.ch/#/frontend")
			);
		}

		@Test
		void mustRedirectToUrl_WhenUserIsCreatedAndFullReturnPathGiven() {
			// given
			expect(kibonJwt.hasInvalidMandantClaims()).andReturn(false);
			expect(kibonJwt.getExternalUUID()).andReturn(KEYCLOAK_UUID);
			expect(kibonJwt.getEmail()).andReturn("some@email.com");

			expect(request.getRequestURL()).andReturn(
				new StringBuffer(
					"https://be.kibon.ch/ebegu/api/v1/auth/callback"
				)
			);

			createBenutzerService.createNewBenutzerFromJwt();
			expectLastCall();

			expect(benutzerService.findBenutzer(kibonJwt)).andReturn(
				Optional.empty()
			);
			expect(benutzerService.findUserWithInvitation(KEYCLOAK_UUID))
				.andReturn(Optional.empty());

			replayAll();

			// when
			var response = testee.callback(
				request,
				"https://be.kibon.ch/#/faelle"
			);

			// then
			assertThat(
				response.getStatusInfo().getStatusCode(),
				Is.is(
					Status.TEMPORARY_REDIRECT.getStatusCode()
				)
			);
			assertThat(
				response.getLocation().toString(),
				Is.is("https://be.kibon.ch/#/faelle")
			);
		}

		@Test
		void mustRedirectToUrl_WhenUserIsFoundAndFragmentReturnPathGiven() {
			var benutzer = new Benutzer();
			benutzer.setStatus(BenutzerStatus.AKTIV);
			// given
			expect(kibonJwt.hasInvalidMandantClaims()).andReturn(false);
			expect(kibonJwt.getExternalUUID()).andReturn(KEYCLOAK_UUID);
			expect(kibonJwt.getBeLoginPrimaryId()).andReturn(Optional.empty());
			expect(kibonJwt.getEmail()).andReturn("some@email.com");
			expect(kibonJwt.getVorname()).andReturn("Jean");
			expect(kibonJwt.getNachname()).andReturn("Room");
			expect(kibonJwt.getZpvNummer()).andReturn(null);

			expect(request.getRequestURL()).andReturn(
				new StringBuffer(
					"https://be.kibon.ch/ebegu/api/v1/auth/callback"
				)
			);

			expect(benutzerService.findBenutzer(kibonJwt)).andReturn(
				Optional.of(benutzer)
			);
			expect(benutzerService.findUserWithInvitation(KEYCLOAK_UUID))
				.andReturn(Optional.empty());

			replayAll();

			// when
			var response = testee.callback(request, "/faelle");

			// then
			assertThat(
				response.getStatusInfo().getStatusCode(),
				Is.is(
					Status.TEMPORARY_REDIRECT.getStatusCode()
				)
			);
			assertThat(
				response.getLocation().toString(),
				Is.is("https://be.kibon.ch/#/faelle")
			);
		}

		@Test
		void mustRedirectToUrl_WhenUserIsFoundAndFullReturnPathGiven() {
			var benutzer = new Benutzer();
			benutzer.setStatus(BenutzerStatus.AKTIV);
			// given
			expect(kibonJwt.hasInvalidMandantClaims()).andReturn(false);
			expect(kibonJwt.getExternalUUID()).andReturn(KEYCLOAK_UUID);
			expect(kibonJwt.getBeLoginPrimaryId()).andReturn(Optional.empty());
			expect(kibonJwt.getEmail()).andReturn("some@email.com");
			expect(kibonJwt.getVorname()).andReturn("Jean");
			expect(kibonJwt.getNachname()).andReturn("Room");
			expect(kibonJwt.getZpvNummer()).andReturn(null);

			expect(request.getRequestURL()).andReturn(
				new StringBuffer(
					"https://be.kibon.ch/ebegu/api/v1/auth/callback"
				)
			);

			expect(benutzerService.findBenutzer(kibonJwt)).andReturn(
				Optional.of(benutzer)
			);
			expect(benutzerService.findUserWithInvitation(KEYCLOAK_UUID))
				.andReturn(Optional.empty());

			replayAll();

			// when
			var response = testee.callback(
				request,
				"https://be.kibon.ch/#/faelle"
			);

			// then
			assertThat(
				response.getStatusInfo().getStatusCode(),
				Is.is(
					Status.TEMPORARY_REDIRECT.getStatusCode()
				)
			);
			assertThat(
				response.getLocation().toString(),
				Is.is("https://be.kibon.ch/#/faelle")
			);
		}

		@Test
		void mustKeepQueryParams_WhenUserIsFoundAndFullReturnPathGiven() {
			var benutzer = new Benutzer();
			benutzer.setStatus(BenutzerStatus.AKTIV);
			// given
			expect(kibonJwt.hasInvalidMandantClaims()).andReturn(false);
			expect(kibonJwt.getExternalUUID()).andReturn(KEYCLOAK_UUID);
			expect(kibonJwt.getBeLoginPrimaryId()).andReturn(Optional.empty());
			expect(kibonJwt.getEmail()).andReturn("some@email.com");
			expect(kibonJwt.getVorname()).andReturn("Jean");
			expect(kibonJwt.getNachname()).andReturn("Room");
			expect(kibonJwt.getZpvNummer()).andReturn(null);

			expect(request.getRequestURL()).andReturn(
				new StringBuffer(
					"https://be.kibon.ch/ebegu/api/v1/auth/callback"
				)
			);

			expect(benutzerService.findBenutzer(kibonJwt)).andReturn(
				Optional.of(benutzer)
			);
			expect(benutzerService.findUserWithInvitation(KEYCLOAK_UUID))
				.andReturn(Optional.empty());

			replayAll();

			// when
			var response = testee.callback(
				request,
				"https://be.kibon.ch/#/faelle?query=hi"
			);

			// then
			assertThat(
				response.getStatusInfo().getStatusCode(),
				Is.is(
					Status.TEMPORARY_REDIRECT.getStatusCode()
				)
			);
			assertThat(
				response.getLocation().toString(),
				Is.is("https://be.kibon.ch/#/faelle?query=hi")
			);
		}

		@Test
		void mustKeepQueryParams_WhenUserIsFoundAndFragmentReturnPathGiven() {
			var benutzer = new Benutzer();
			benutzer.setStatus(BenutzerStatus.AKTIV);
			// given
			expect(kibonJwt.hasInvalidMandantClaims()).andReturn(false);
			expect(kibonJwt.getExternalUUID()).andReturn(KEYCLOAK_UUID);
			expect(kibonJwt.getBeLoginPrimaryId()).andReturn(Optional.empty());
			expect(kibonJwt.getEmail()).andReturn("some@email.com");
			expect(kibonJwt.getVorname()).andReturn("Jean");
			expect(kibonJwt.getNachname()).andReturn("Room");
			expect(kibonJwt.getZpvNummer()).andReturn(null);

			expect(request.getRequestURL()).andReturn(
				new StringBuffer(
					"https://be.kibon.ch/ebegu/api/v1/auth/callback"
				)
			);

			expect(benutzerService.findBenutzer(kibonJwt)).andReturn(
				Optional.of(benutzer)
			);
			expect(benutzerService.findUserWithInvitation(KEYCLOAK_UUID))
				.andReturn(Optional.empty());

			replayAll();

			// when
			var response = testee.callback(
				request,
				"/faelle?query=hi"
			);

			// then
			assertThat(
				response.getStatusInfo().getStatusCode(),
				Is.is(
					Status.TEMPORARY_REDIRECT.getStatusCode()
				)
			);
			assertThat(
				response.getLocation().toString(),
				Is.is("https://be.kibon.ch/#/faelle?query=hi")
			);
		}

		@Nested
		class Invitation {
			Benutzer benutzer;

			@BeforeEach
			void setUp() {
				benutzer = new Benutzer();

				expect(kibonJwt.hasInvalidMandantClaims()).andReturn(false);
			}

			@Test
			void mustInvalidateSessionAndReturnErrorIfBenutzerEmailDoesNotMatchTokenEmail() {
				// given
				expect(kibonJwt.hasInvalidMandantClaims()).andReturn(true);
				HttpSession mockSession = mock(HttpSession.class);
				mockSession.invalidate();
				expectLastCall();
				expect(request.getSession(false)).andReturn(mockSession);

				benutzer.setEmail("one@mail.com");
				Mandant mandant = new Mandant();
				mandant.setMandantIdentifier(MandantIdentifier.BERN);
				benutzer.setMandant(mandant);
				expect(kibonJwt.getExternalUUID()).andReturn(KEYCLOAK_UUID);
				expect(kibonJwt.getEmail()).andReturn("other@mail.com");
				expect(benutzerService.findUserWithInvitation(KEYCLOAK_UUID))
					.andReturn(
						Optional.of(benutzer)
					);

				replayAll();

				// when
				var response = testee.callback(request, "/irrelevant");

				// then
				assertThat(
					response.getStatusInfo().getStatusCode(),
					Is.is(Status.BAD_REQUEST.getStatusCode())
				);
			}

			@Test
			void mustUpdateBenutzer() {
				// given
				benutzer.setNachname("unknown");
				benutzer.setVorname("unknown");
				benutzer.setEmail("one@mail.com");
				benutzer.setStatus(BenutzerStatus.EINGELADEN);

				expect(kibonJwt.getNachname()).andReturn("nachname");
				expect(kibonJwt.getVorname()).andReturn("vorname");
				expect(kibonJwt.getExternalUUID()).andReturn(KEYCLOAK_UUID);
				expect(kibonJwt.getEmail()).andReturn("one@mail.com");
				expect(benutzerService.findUserWithInvitation(KEYCLOAK_UUID))
					.andReturn(
						Optional.of(benutzer)
					);
				expect(request.getRequestURL()).andReturn(
					new StringBuffer(
						"https://somehost.com/web"
					)
				);

				replayAll();

				// when
				var response = testee.callback(request, "/start");

				// then
				assertThat(benutzer.getStatus(), Is.is(BenutzerStatus.AKTIV));
				assertThat(benutzer.getNachname(), Is.is("nachname"));
				assertThat(benutzer.getVorname(), Is.is("vorname"));
				assertThat(
					response.getStatusInfo().getStatusCode(),
					Is.is(
						Status.TEMPORARY_REDIRECT.getStatusCode()
					)
				);
				assertThat(
					response.getLocation().toString(),
					Is.is("https://somehost.com/#/start")
				);
			}
		}

		@Nested
		class ValidateClaims {
			@Test
			void mustInvalidateSessionAndReturnErrorResponseIfClaimsInvalid() {
				// given
				expect(kibonJwt.hasInvalidMandantClaims()).andReturn(true);
				HttpSession mockSession = mock(HttpSession.class);
				mockSession.invalidate();
				expectLastCall();

				expect(request.getSession(false)).andReturn(mockSession);
				replayAll();

				// when
				var response = testee.callback(request, "/irrelevant");

				// then
				assertThat(
					response.getStatusInfo().getStatusCode(),
					Is.is(Status.BAD_REQUEST.getStatusCode())
				);
			}

			@Test
			void mustReturnErrorResponseIfClaimsInvalid() {
				// given
				expect(kibonJwt.hasInvalidMandantClaims()).andReturn(true);
				expect(request.getSession(false)).andReturn(null);
				replayAll();

				// when
				var response = testee.callback(request, "/irrelevant");

				// then
				assertThat(
					response.getStatusInfo().getStatusCode(),
					Is.is(Status.BAD_REQUEST.getStatusCode())
				);
			}
		}

		@Nested
		class BELoginUUIDMigration {
			String BELOGIN_PRIMARY_ID = "5244c45e-a019-11ef-ae6c-03b6cd91d2dc";
			Benutzer benutzer;

			@BeforeEach
			void setup() {
				benutzer = new Benutzer();
				Mandant mandant = new Mandant();
				mandant.setMandantIdentifier(MandantIdentifier.BERN);
				benutzer.setMandant(mandant);
				expect(kibonJwt.getZpvNummer()).andReturn("123123");
				expect(kibonJwt.getEmail()).andReturn("email");
				expect(kibonJwt.getNachname()).andReturn("nachname");
				expect(kibonJwt.getVorname()).andReturn("nachname");
				expect(kibonJwt.getBeLoginPrimaryId()).andReturn(
					Optional.of(
						BELOGIN_PRIMARY_ID
					)
				);
				expect(kibonJwt.getExternalUUID()).andReturn(KEYCLOAK_UUID)
					.anyTimes();
				expect(kibonJwt.hasInvalidMandantClaims()).andReturn(false);
				expect(benutzerService.findUserWithInvitation(KEYCLOAK_UUID))
					.andReturn(
						Optional.empty()
					);
				expect(benutzerService.findBenutzer(kibonJwt)).andReturn(
					Optional.of(benutzer)
				);
				expect(request.getRequestURL()).andReturn(
					new StringBuffer(
						"https://somehost.com/"
					)
				);
			}

			@Test
			void mustUpdateExternalUUIDtoKeycloakUUID() {
				// given
				benutzer.setExternalUUID(BELOGIN_PRIMARY_ID);
				replayAll();

				// when
				testee.callback(request, "/irrelevant");

				// then
				assertThat(benutzer.getExternalUUID(), Is.is(KEYCLOAK_UUID));
			}

			@Test
			void mustNotUpdateExternalUUID() {
				// given
				benutzer.setExternalUUID(KEYCLOAK_UUID);
				replayAll();

				// when
				testee.callback(request, "/irrelevant");

				// then
				assertThat(benutzer.getExternalUUID(), Is.is(KEYCLOAK_UUID));
			}
		}
	}
}
