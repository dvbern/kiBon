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

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

class ExternalUUIDUtilTest {

	@Nested
	class AddPrefixIfNecessary {
		@Test
		void mustThrowIfCalledWithMandantNull() {
			assertThrows(
				NullPointerException.class,
				() -> ExternalUUIDUtil.addPrefixIfNecessary(
					"keycloak:*:649b4908-a10d-11ef-bc50-63b70087ae2c",
					null
				)
			);
		}

		@Test
		void mustThrowIfCalledWithMandantBernAndPrefixedUUID() {
			assertThrows(
				IllegalStateException.class,
				() -> ExternalUUIDUtil.addPrefixIfNecessary(
					"keycloak:*:649b4908-a10d-11ef-bc50-63b70087ae2c",
					MandantIdentifier.BERN
				)
			);
		}

		@Test
		void mustNotPrefixWhenAlreadyPrefixed() {
			String input =
				"keycloak:zug:649b4908-a10d-11ef-bc50-63b70087ae2c";
			assertThat(
				ExternalUUIDUtil.addPrefixIfNecessary(
					input,
					MandantIdentifier.ZUG
				),
				Is.is(input)
			);
		}

		@Test
		void mustPrefixWhenNotAlreadyPrefixed() {
			assertThat(
				ExternalUUIDUtil.addPrefixIfNecessary(
					"649b4908-a10d-11ef-bc50-63b70087ae2c",
					MandantIdentifier.SCHWYZ
				),
				Is.is("keycloak:schwyz:649b4908-a10d-11ef-bc50-63b70087ae2c")
			);
		}

		@Test
		void mustNotPrefixIfUUIDIsNull() {
			assertThat(
				ExternalUUIDUtil.addPrefixIfNecessary(
					null,
					MandantIdentifier.SCHWYZ
				),
				Matchers.nullValue()
			);
		}
	}

	static Stream<Arguments> removeCases() {
		return Stream.of(
			Arguments.of(
				"keycloak:schwyz:649b4908-a10d-11ef-bc50-63b70087ae2c",
				"649b4908-a10d-11ef-bc50-63b70087ae2c"
			),
			Arguments.of(
				"keycloak:solothurn:f175f3a8-a10f-11ef-a3e8-339f1363f727",
				"f175f3a8-a10f-11ef-a3e8-339f1363f727"
			),
			Arguments.of(
				"keycloak:luzern:22d2a428-a110-11ef-89ec-63eb211bbfc2",
				"22d2a428-a110-11ef-89ec-63eb211bbfc2"
			),
			Arguments.of(
				"keycloak:appenzell_ausserrhoden:5ca8b9d0-a110-11ef-af07-73c49df143bd",
				"5ca8b9d0-a110-11ef-af07-73c49df143bd"
			),
			Arguments.of(
				"keycloak:zug:8d342daa-a110-11ef-9533-574866d612ce",
				"8d342daa-a110-11ef-9533-574866d612ce"
			),
			Arguments.of(
				"eb960c0c-a10f-11ef-b1d1-57a5773572c1",
				"eb960c0c-a10f-11ef-b1d1-57a5773572c1"
			),
			Arguments.of(
				null,
				null
			)
		);
	}

	@MethodSource("removeCases")
	@ParameterizedTest
	void removePrefixIfNecessary(String input, String expectedOutput) {
		// given
		Benutzer benutzer = new Benutzer();
		Mandant mandant = new Mandant();
		mandant.setMandantIdentifier(MandantIdentifier.SCHWYZ);
		benutzer.setMandant(mandant);
		benutzer.setExternalUUID(input);

		// when
		// then
		assertThat(
			ExternalUUIDUtil.removePrefixIfNecessary(input),
			Is.is(expectedOutput)
		);
	}

	static Stream<Arguments> equalsCases() {
		return Stream.of(
			Arguments.of(
				"keycloak:schwyz:649b4908-a10d-11ef-bc50-63b70087ae2c",
				"649b4908-a10d-11ef-bc50-63b70087ae2c",
				true
			),
			Arguments.of(
				"f175f3a8-a10f-11ef-a3e8-339f1363f727",
				"keycloak:solothurn:f175f3a8-a10f-11ef-a3e8-339f1363f727",
				true
			),
			Arguments.of(
				"keycloak:luzern:22d2a428-a110-11ef-89ec-63eb211bbfc2",
				"22d2a428-a110-11ef-89ec-63eb211bbfc2",
				true
			),
			Arguments.of(
				"keycloak:appenzell_ausserrhoden:5ca8b9d0-a110-11ef-af07-73c49df143bd",
				"keycloak:appenzell_ausserrhoden:5ca8b9d0-a110-11ef-af07-73c49df143bd",
				true
			),
			Arguments.of(
				"8d342daa-a110-11ef-9533-574866d612ce",
				"keycloak:zug:8d342daa-a110-11ef-9533-574866d612ce",
				true
			),
			Arguments.of(
				"eb960c0c-a10f-11ef-b1d1-57a5773572c1",
				"eb960c0c-a10f-11ef-b1d1-57a5773572c1",
				true
			),
			Arguments.of(
				"eb960c0c-a10f-11ef-b1d1-57a5773572c1",
				"8d342daa-a110-11ef-9533-574866d612ce",
				false
			)
		);
	}

	@MethodSource("equalsCases")
	@ParameterizedTest
	void equals(String uuid1, String uuid2, boolean equals) {
		assertThat(
			ExternalUUIDUtil.equals(uuid1, uuid2),
			Is.is(equals)
		);
	}
}