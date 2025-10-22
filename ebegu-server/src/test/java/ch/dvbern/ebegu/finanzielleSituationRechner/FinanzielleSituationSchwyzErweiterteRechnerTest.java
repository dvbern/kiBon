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

package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FinanzielleSituationSchwyzErweiterteRechnerTest {

	private final FinanzielleSituationSchwyzErweiterteRechner finanzielleSituationSchwyzErweiterteRechner =
		new FinanzielleSituationSchwyzErweiterteRechner();

	private static final BigDecimal TAUSEND = new BigDecimal(1000);

	@Test
	void pauschalabzug_kleiner_als_effektiverLiegenschaftsaufwand_20_Percent_Abgezogen_test() {
		AbstractFinanzielleSituation finanzielleSituation = createFinSitForTest(
			TAUSEND,
			TAUSEND
		);
		assertThat(
			finanzielleSituationSchwyzErweiterteRechner
				.calcLiegenschaftsaufwand(finanzielleSituation),
			is(new BigDecimal(800))
		);
	}

	@Test
	void pauschalabzug_equal_effektiverLiegenschaftsaufwand_test() {
		AbstractFinanzielleSituation finanzielleSituation = createFinSitForTest(
			new BigDecimal(10000),
			TAUSEND
		);
		assertThat(
			finanzielleSituationSchwyzErweiterteRechner
				.calcLiegenschaftsaufwand(finanzielleSituation),
			is(BigDecimal.ZERO)
		);
	}

	@Test
	void pauschalabzug_groesser_als_effektiverLiegenschaftsaufwand_test() {
		AbstractFinanzielleSituation finanzielleSituation = createFinSitForTest(
			new BigDecimal(100000),
			TAUSEND
		);
		assertThat(
			finanzielleSituationSchwyzErweiterteRechner
				.calcLiegenschaftsaufwand(finanzielleSituation),
			is(BigDecimal.ZERO)
		);
	}

	@Test
	void pauschalabzug_alles_null_soll_zero_zuruekgeben_test() {
		AbstractFinanzielleSituation finanzielleSituation = createFinSitForTest(
			null,
			null
		);
		assertThat(
			finanzielleSituationSchwyzErweiterteRechner
				.calcLiegenschaftsaufwand(finanzielleSituation),
			is(BigDecimal.ZERO)
		);
	}

	private AbstractFinanzielleSituation createFinSitForTest(
		@Nullable BigDecimal liegenschaftsErtraege,
		@Nullable BigDecimal effetkiverLiegenschaftsaufwand
	) {
		AbstractFinanzielleSituation abstractFinanzielleSituation =
			new FinanzielleSituation();
		abstractFinanzielleSituation.setLiegenschaftsErtraege(
			liegenschaftsErtraege
		);
		abstractFinanzielleSituation.setAbzuegeLiegenschaft(
			effetkiverLiegenschaftsaufwand
		);
		return abstractFinanzielleSituation;
	}

}
