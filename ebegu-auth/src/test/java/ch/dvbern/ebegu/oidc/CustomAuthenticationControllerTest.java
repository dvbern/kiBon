package ch.dvbern.ebegu.oidc;

import jakarta.ws.rs.core.UriBuilder;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CustomAuthenticationControllerTest {

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

}
