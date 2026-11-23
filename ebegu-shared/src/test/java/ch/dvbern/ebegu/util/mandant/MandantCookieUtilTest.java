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

package ch.dvbern.ebegu.util.mandant;

import java.util.Optional;
import java.util.stream.Stream;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static ch.dvbern.ebegu.util.mandant.MandantCookieUtil.COOKIE_VALUE_APPENZELL_AUSSERRHODEN;
import static ch.dvbern.ebegu.util.mandant.MandantCookieUtil.COOKIE_VALUE_KANTON_BERN;
import static ch.dvbern.ebegu.util.mandant.MandantCookieUtil.COOKIE_VALUE_KANTON_SCHWYZ;
import static ch.dvbern.ebegu.util.mandant.MandantCookieUtil.COOKIE_VALUE_KANTON_SOLOTHURN;
import static ch.dvbern.ebegu.util.mandant.MandantCookieUtil.COOKIE_VALUE_STADT_LUZERN;
import static ch.dvbern.ebegu.util.mandant.MandantCookieUtil.MANDANT_COOKIE_NAME;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MandantCookieUtilTest {
	static Stream<Arguments> cookieMandantSource() {
		return Stream.of(
			Arguments.of(COOKIE_VALUE_KANTON_BERN, MandantIdentifier.BERN),
			Arguments.of(
				COOKIE_VALUE_APPENZELL_AUSSERRHODEN,
				MandantIdentifier.APPENZELL_AUSSERRHODEN
			),
			Arguments.of(
				COOKIE_VALUE_KANTON_SOLOTHURN,
				MandantIdentifier.SOLOTHURN
			),
			Arguments.of(
				COOKIE_VALUE_STADT_LUZERN,
				MandantIdentifier.LUZERN
			),
			Arguments.of(
				COOKIE_VALUE_KANTON_SCHWYZ,
				MandantIdentifier.SCHWYZ
			)
		);
	}

	@ParameterizedTest
	@MethodSource("cookieMandantSource")
	void convertCookieNameToMandantIdentifierMustMapCookieNameToMandantIdentifier(
		String cookieValue,
		MandantIdentifier mandantIdentifier
	) {
		assertThat(
			MandantCookieUtil.convertCookieNameToMandantIdentifier(
				cookieValue
			),
			org.hamcrest.Matchers.is(mandantIdentifier)
		);
	}

	@Test
	void convertCookieNameToMandantIdentifierMustThrowIfUnknownMandant() {
		assertThrows(
			IllegalStateException.class,
			() -> MandantCookieUtil.convertCookieNameToMandantIdentifier(
				"fantasie"
			)
		);
	}

	@ParameterizedTest
	@MethodSource("cookieMandantSource")
	void getMandantFromCookieMustMapCookieNameToMandantIdentifier(
		String cookieValue,
		MandantIdentifier mandantIdentifier
	) {
		HttpServletRequest request = mock(HttpServletRequest.class);
		expect(request.getCookies()).andReturn(
			new Cookie[] { new Cookie(MANDANT_COOKIE_NAME, cookieValue) }
		).times(2);
		replay(request);
		assertThat(
			MandantCookieUtil.getMandantFromCookie(request),
			org.hamcrest.Matchers.is(mandantIdentifier)
		);
	}

	@Test
	void getMandantFromCookieMustThrowIfCookieNotSet() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		expect(request.getCookies()).andReturn(new Cookie[] {}).times(2);
		replay(request);

		assertThrows(
			IllegalStateException.class,
			() -> MandantCookieUtil.getMandantFromCookie(request)
		);
	}

	@Test
	void getMandantFromCookieMustThrowIfNoCookies() {
		HttpServletRequest request = mock(HttpServletRequest.class);
		expect(request.getCookies()).andReturn(null);
		replay(request);

		assertThrows(
			IllegalStateException.class,
			() -> MandantCookieUtil.getMandantFromCookie(request)
		);
	}

	@Test
	void getMandantCookieMustReturnMandantCookie() {
		Cookie mandantCookie = new Cookie(
			MANDANT_COOKIE_NAME,
			COOKIE_VALUE_KANTON_BERN
		);
		HttpServletRequest request = mockRequestWithCookies(
			new Cookie[] { mandantCookie }
		);

		Optional<Cookie> result = MandantCookieUtil.getMandantCookie(request);

		assertThat(result.isPresent(), is(true));
		assertThat(result.get(), sameInstance(mandantCookie));
	}

	@Test
	void getMandantCookieMustFindMandantCookieAmongOtherCookies() {
		Cookie mandantCookie = new Cookie(
			MANDANT_COOKIE_NAME,
			COOKIE_VALUE_KANTON_SOLOTHURN
		);
		HttpServletRequest request = mockRequestWithCookies(
			new Cookie[] {
				new Cookie("JSESSIONID", "abc"),
				mandantCookie,
				new Cookie("XSRF-TOKEN", "xyz")
			}
		);

		Optional<Cookie> result = MandantCookieUtil.getMandantCookie(request);

		assertThat(result.isPresent(), is(true));
		assertThat(result.get(), sameInstance(mandantCookie));
	}

	@Test
	void getMandantCookieMustReturnFirstMandantCookieIfMultiplePresent() {
		Cookie first = new Cookie(
			MANDANT_COOKIE_NAME,
			COOKIE_VALUE_KANTON_BERN
		);
		Cookie second = new Cookie(
			MANDANT_COOKIE_NAME,
			COOKIE_VALUE_STADT_LUZERN
		);
		HttpServletRequest request = mockRequestWithCookies(
			new Cookie[] { first, second }
		);

		Optional<Cookie> result = MandantCookieUtil.getMandantCookie(request);

		assertThat(result.isPresent(), is(true));
		assertThat(result.get(), sameInstance(first));
	}

	@Test
	void getMandantCookieMustNotDecodeCookieValue() {
		String encodedValue = "Kanton%20Bern";
		HttpServletRequest request = mockRequestWithCookies(
			new Cookie[] { new Cookie(MANDANT_COOKIE_NAME, encodedValue) }
		);

		Optional<Cookie> result = MandantCookieUtil.getMandantCookie(request);

		assertThat(result.isPresent(), is(true));
		assertThat(result.get().getValue(), is(encodedValue));
	}

	@Test
	void getMandantCookieMustBeEmptyIfNoCookies() {
		HttpServletRequest request = mockRequestWithCookies(null);

		assertThat(
			MandantCookieUtil.getMandantCookie(request),
			is(Optional.empty())
		);
	}

	@Test
	void getMandantCookieMustBeEmptyIfCookieArrayIsEmpty() {
		HttpServletRequest request = mockRequestWithCookies(new Cookie[] {});

		assertThat(
			MandantCookieUtil.getMandantCookie(request),
			is(Optional.empty())
		);
	}

	@Test
	void getMandantCookieMustBeEmptyIfMandantCookieNotSet() {
		HttpServletRequest request = mockRequestWithCookies(
			new Cookie[] {
				new Cookie("JSESSIONID", "abc"),
				new Cookie("Mandant", COOKIE_VALUE_KANTON_BERN)
			}
		);

		assertThat(
			MandantCookieUtil.getMandantCookie(request),
			is(Optional.empty())
		);
	}

	private static HttpServletRequest mockRequestWithCookies(Cookie[] cookies) {
		HttpServletRequest request = mock(HttpServletRequest.class);
		expect(request.getCookies()).andReturn(cookies).anyTimes();
		replay(request);
		return request;
	}
}
