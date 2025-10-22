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

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

class MandantIdentifierTest {
	static Stream<Arguments> urlsWithMandant() {
		return Stream.of(
			Arguments.of(
				URI.create("http://local-so.kibon.ch"),
				MandantIdentifier.SOLOTHURN
			),
			Arguments.of(
				URI.create("http://local-be.kibon.ch"),
				MandantIdentifier.BERN
			),
			Arguments.of(
				URI.create("http://local-ar.kibon.ch"),
				MandantIdentifier.APPENZELL_AUSSERRHODEN
			),
			Arguments.of(
				URI.create("http://local-sz.kibon.ch"),
				MandantIdentifier.SCHWYZ
			),
			Arguments.of(
				URI.create("https://ar.kibon.ch"),
				MandantIdentifier.APPENZELL_AUSSERRHODEN
			),
			Arguments.of(
				URI.create("https://be.kibon.ch"),
				MandantIdentifier.BERN
			),
			Arguments.of(
				URI.create("https://so.kibon.ch"),
				MandantIdentifier.SOLOTHURN
			),
			Arguments.of(
				URI.create("https://sz.kibon.ch"),
				MandantIdentifier.SCHWYZ
			)
		);
	}

	@ParameterizedTest
	@MethodSource("urlsWithMandant")
	void mustReturnOptionalMandant(
		URI givenUrl,
		MandantIdentifier expectedMandant
	) {
		assertThat(
			MandantIdentifier.findByHostname(givenUrl).orElseThrow(),
			is(expectedMandant)
		);
	}

	@ParameterizedTest
	@ValueSource(strings = { "http://local-nooo.kibon.ch", "http://localhost",
		"http://dev.kibon.ch", "http://google.ch" })
	void mustReturnEmptyIfNotMandantUrl(String url) {
		URI uri = URI.create(url);
		assertThat(
			MandantIdentifier.findByHostname(uri),
			is(Optional.empty())
		);
	}

	@ParameterizedTest
	@MethodSource("urlsWithMandant")
	void mustResolveCorrectTenantConfig(
		URI givenUrl,
		MandantIdentifier expectedMandant
	) {
		assertThat(
			MandantIdentifier.getByHostname(givenUrl),
			is(expectedMandant)
		);
	}

	@ParameterizedTest
	@ValueSource(strings = { "http://local-nooo.kibon.ch", "http://localhost",
		"http://dev.kibon.ch", "http://google.ch" })
	void mustThrowIfNotMandantUrl(String url) {
		URI uri = URI.create(url);
		assertThrows(
			EbeguRuntimeException.class,
			() -> MandantIdentifier.getByHostname(uri)
		);
	}

}
