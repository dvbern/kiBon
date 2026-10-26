/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
 */
package ch.dvbern.ebegu.familiensituation;

import java.time.LocalDate;
import java.util.stream.Stream;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FamiliensituationUtilTest {

	private static final LocalDate GP_START = LocalDate.of(2025, 8, 1);
	private static final LocalDate GP_END = LocalDate.of(2026, 7, 31);

	@ParameterizedTest(name = "{0}")
	@MethodSource("mindestdauerInGesuchsperiodeErreicht")
	void isKonkubinatOhneKindSpezialfall_shouldReturnTrue_WhenMindestdauerReachedWithinGesuchsperiode(
		String szenario,
		LocalDate startKonkubinat,
		int paramMinDauerKonkubinat
	) {
		assertThat(
			isSpezialfall(
				EnumFamilienstatus.KONKUBINAT_KEIN_KIND,
				startKonkubinat,
				paramMinDauerKonkubinat
			),
			is(true)
		);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("mindestdauerAusserhalbGesuchsperiodeErreicht")
	void isKonkubinatOhneKindSpezialfall_shouldReturnFalse_WhenMindestdauerReachedOutsideGesuchsperiode(
		String szenario,
		LocalDate startKonkubinat,
		int paramMinDauerKonkubinat
	) {
		assertThat(
			isSpezialfall(
				EnumFamilienstatus.KONKUBINAT_KEIN_KIND,
				startKonkubinat,
				paramMinDauerKonkubinat
			),
			is(false)
		);
	}

	@Test
	void isKonkubinatOhneKindSpezialfall_shouldReturnFalse_WhenStartKonkubinatIsNull() {
		assertThat(
			isSpezialfall(EnumFamilienstatus.KONKUBINAT_KEIN_KIND, null, 2),
			is(false)
		);
	}

	@ParameterizedTest(name = "Familienstatus {0}")
	@EnumSource(
		value = EnumFamilienstatus.class,
		names = "KONKUBINAT_KEIN_KIND",
		mode = Mode.EXCLUDE
	)
	void isKonkubinatOhneKindSpezialfall_shouldReturnFalse_WhenFamilienstatusIsNotKonkubinatKeinKind(
		EnumFamilienstatus familienstatus
	) {
		assertThat(
			isSpezialfall(familienstatus, LocalDate.of(2023, 12, 15), 2),
			is(false)
		);
	}

	private static Stream<Arguments> mindestdauerInGesuchsperiodeErreicht() {
		return Stream.of(
			Arguments.of(
				"2 Jahre: erreicht am ersten Tag der Gesuchsperiode",
				GP_START.minusYears(2),
				2
			),
			Arguments.of(
				"2 Jahre: erreicht mitten in der Gesuchsperiode",
				LocalDate.of(2023, 12, 15),
				2
			),
			Arguments.of(
				"2 Jahre: erreicht am letzten Tag der Gesuchsperiode",
				GP_END.minusYears(2),
				2
			),
			Arguments.of(
				"5 Jahre: erreicht am ersten Tag der Gesuchsperiode",
				GP_START.minusYears(5),
				5
			),
			Arguments.of(
				"5 Jahre: erreicht mitten in der Gesuchsperiode",
				LocalDate.of(2020, 12, 15),
				5
			),
			Arguments.of(
				"5 Jahre: erreicht am letzten Tag der Gesuchsperiode",
				GP_END.minusYears(5),
				5
			)
		);
	}

	private static Stream<Arguments> mindestdauerAusserhalbGesuchsperiodeErreicht() {
		return Stream.of(
			Arguments.of(
				"2 Jahre: erreicht einen Tag vor der Gesuchsperiode",
				GP_START.minusYears(2).minusDays(1),
				2
			),
			Arguments.of(
				"2 Jahre: erreicht einen Tag nach der Gesuchsperiode",
				GP_END.minusYears(2).plusDays(1),
				2
			),
			Arguments.of(
				"5 Jahre: erreicht einen Tag vor der Gesuchsperiode",
				GP_START.minusYears(5).minusDays(1),
				5
			),
			Arguments.of(
				"5 Jahre: erreicht einen Tag nach der Gesuchsperiode",
				GP_END.minusYears(5).plusDays(1),
				5
			),
			Arguments.of(
				"5 Jahre verlangt, aber Konkubinat erst 2 Jahre in der Gesuchsperiode",
				LocalDate.of(2023, 12, 15),
				5
			),
			Arguments.of(
				"2 Jahre verlangt, aber Konkubinat schon 5 Jahre in der Gesuchsperiode",
				LocalDate.of(2020, 12, 15),
				2
			)
		);
	}

	private static boolean isSpezialfall(
		EnumFamilienstatus familienstatus,
		LocalDate startKonkubinat,
		int paramMinDauerKonkubinat
	) {
		var familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(familienstatus);
		familiensituation.setStartKonkubinat(startKonkubinat);

		var gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setGueltigkeit(new DateRange(GP_START, GP_END));
		var gesuch = new Gesuch();
		gesuch.setGesuchsperiode(gesuchsperiode);

		return FamiliensituationUtil.isKonkubinatOhneKindSpezialfall(
			familiensituation,
			gesuch,
			paramMinDauerKonkubinat
		);
	}
}
