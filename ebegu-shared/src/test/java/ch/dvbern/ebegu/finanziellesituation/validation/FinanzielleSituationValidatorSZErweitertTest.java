/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.finanziellesituation.validation;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FinanzielleSituationValidatorSZErweitertTest {

	FinanzielleSituationValidatorSZErweitert validator =
		new FinanzielleSituationValidatorSZErweitert();

	@Nested
	class FinSitTest {
		@Test
		void allNullShouldNotBeValid() {
			assertThat(
				validator.isFinanzielleSituationComplete(
					new FinanzielleSituation(),
					new Gesuch()
				),
				is(false)
			);
		}

		@Test
		void notQuellenbesteuertFehlendeLiegenschaftertraegeShouldNotBeValid() {
			final FinanzielleSituation finanzielleSituation =
				new FinanzielleSituation();
			finanzielleSituation.setSteuerbaresEinkommen(BigDecimal.ONE);
			finanzielleSituation.setEinkaeufeVorsorge(BigDecimal.ONE);
			finanzielleSituation.setAbzuegeLiegenschaft(BigDecimal.ONE);
			finanzielleSituation.setSteuerbaresVermoegen(BigDecimal.ONE);
			assertThat(
				validator.isFinanzielleSituationComplete(
					finanzielleSituation,
					new Gesuch()
				),
				is(false)
			);
		}

		@Test
		void notQuellenbesteuertAllAnsweredShouldBeValid() {
			final FinanzielleSituation finanzielleSituation =
				new FinanzielleSituation();
			finanzielleSituation.setSteuerbaresEinkommen(BigDecimal.ONE);
			finanzielleSituation.setEinkaeufeVorsorge(BigDecimal.ONE);
			finanzielleSituation.setAbzuegeLiegenschaft(BigDecimal.ONE);
			finanzielleSituation.setLiegenschaftsErtraege(BigDecimal.ONE);
			finanzielleSituation.setSteuerbaresVermoegen(BigDecimal.ONE);
			assertThat(
				validator.isFinanzielleSituationComplete(
					finanzielleSituation,
					new Gesuch()
				),
				is(true)
			);
		}
	}

	@Nested
	class EKVTest {
		@Test
		void allNullShouldNotBeValid() {
			assertThat(
				validator.isEinkommensverschlechterungComplete(
					new Einkommensverschlechterung(),
					new Gesuch()
				),
				is(false)
			);
		}

		@Test
		void notQuellenbesteuertFehlendeLiegenschaftertraegeShouldNotBeValid() {
			final Einkommensverschlechterung einkommensverschlechterung =
				new Einkommensverschlechterung();
			einkommensverschlechterung.setSteuerbaresEinkommen(BigDecimal.ONE);
			einkommensverschlechterung.setEinkaeufeVorsorge(BigDecimal.ONE);
			einkommensverschlechterung.setAbzuegeLiegenschaft(BigDecimal.ONE);
			einkommensverschlechterung.setSteuerbaresVermoegen(BigDecimal.ONE);
			assertThat(
				validator.isEinkommensverschlechterungComplete(
					einkommensverschlechterung,
					new Gesuch()
				),
				is(false)
			);
		}

		@Test
		void notQuellenbesteuertAllAnsweredShouldBeValid() {
			final Einkommensverschlechterung einkommensverschlechterung =
				new Einkommensverschlechterung();
			einkommensverschlechterung.setSteuerbaresEinkommen(BigDecimal.ONE);
			einkommensverschlechterung.setEinkaeufeVorsorge(BigDecimal.ONE);
			einkommensverschlechterung.setAbzuegeLiegenschaft(BigDecimal.ONE);
			einkommensverschlechterung.setLiegenschaftsErtraege(BigDecimal.ONE);
			einkommensverschlechterung.setSteuerbaresVermoegen(BigDecimal.ONE);
			assertThat(
				validator.isEinkommensverschlechterungComplete(
					einkommensverschlechterung,
					new Gesuch()
				),
				is(true)
			);
		}
	}
}
