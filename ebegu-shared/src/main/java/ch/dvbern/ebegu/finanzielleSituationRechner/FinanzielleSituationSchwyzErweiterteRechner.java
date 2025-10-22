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
 */

package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.util.MathUtil;

public class FinanzielleSituationSchwyzErweiterteRechner extends
	FinanzielleSituationSchwyzRechner {

	@Override
	@Nonnull
	protected BigDecimal calcLiegenschaftsaufwand(
		@Nonnull AbstractFinanzielleSituation finanzielleSituation
	) {
		var pauschalabzug = percent(
			finanzielleSituation.getLiegenschaftsErtraege(),
			20
		);
		var effektiverLiegenschaftsAufwand = MathUtil.positiveNonNullAndRound(
			finanzielleSituation.getAbzuegeLiegenschaft()
		);
		if (pauschalabzug.compareTo(
			effektiverLiegenschaftsAufwand
		) >= 0) {
			return BigDecimal.ZERO;
		}
		return effektiverLiegenschaftsAufwand.subtract(pauschalabzug);
	}
}
