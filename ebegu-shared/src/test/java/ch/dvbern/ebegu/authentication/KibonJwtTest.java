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

package ch.dvbern.ebegu.authentication;

import java.util.stream.Stream;

import jakarta.security.enterprise.identitystore.openid.AccessToken;
import jakarta.security.enterprise.identitystore.openid.OpenIdContext;

import org.apache.commons.lang3.tuple.Pair;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.MockType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.easymock.EasyMock.expect;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(EasyMockExtension.class)
class KibonJwtTest extends EasyMockSupport {

	public static Stream<Arguments> claims() {
		return Stream.of(
			Arguments.of(
				Pair.of("bla", ""),
				Pair.of(KibonJwt.MANDANT_CLAIM, ""),
				true
			),
			Arguments.of(
				Pair.of(KibonJwt.MANDANT_UUID_CLAIM, ""),
				Pair.of("bla", ""),
				true
			),
			Arguments.of(Pair.of("bli", ""), Pair.of("bla", ""), true),
			Arguments.of(
				Pair.of(
					KibonJwt.MANDANT_UUID_CLAIM,
					"81f1dc58-9134-11ef-b484-2ba77cdcaeeb"
				),
				Pair.of(KibonJwt.MANDANT_CLAIM, "KEIN MANDANT"),
				true
			),
			Arguments.of(
				Pair.of(KibonJwt.MANDANT_UUID_CLAIM, "81f1dc58-"),
				Pair.of(KibonJwt.MANDANT_CLAIM, "BERN"),
				true
			),
			Arguments.of(
				Pair.of(
					KibonJwt.MANDANT_UUID_CLAIM,
					"81f1dc58-9134-11ef-b484-2ba77cdcaeeb"
				),
				Pair.of(KibonJwt.MANDANT_CLAIM, "BERN"),
				false
			)
		);
	}

	@ParameterizedTest
	@MethodSource("claims")
	void mustVerifyMandantClaimsExist(
		Pair<String, String> mandantClaim1,
		Pair<String, String> mandantClaim2,
		boolean expectedResult
	) {
		AccessToken token = mock(MockType.NICE, AccessToken.class);
		expect(token.getClaim(mandantClaim1.getKey())).andStubReturn(
			mandantClaim1.getValue()
		);
		expect(token.getClaim(mandantClaim2.getKey())).andStubReturn(
			mandantClaim2.getValue()
		);

		OpenIdContext openIdContext = mock(OpenIdContext.class);
		expect(openIdContext.getAccessToken()).andReturn(token).times(2);

		replayAll();

		var kibonJwt = new KibonJwt(openIdContext);

		assertEquals(expectedResult, kibonJwt.hasInvalidMandantClaims());
	}

}