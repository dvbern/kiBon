/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Bemerkung: Bei einer KESB-Platzierung wird kein Gutschein ausgestellt. Die Betreuungskosten werden von der KESB
 * übernommen.
 */
public class KesbPlatzierungCalcRule extends AbstractCalcRule {

	public KesbPlatzierungCalcRule(@Nonnull DateRange validityPeriod) {
		super(RuleKey.KESB_PLATZIERUNG, RuleType.REDUKTIONSREGEL, validityPeriod);
	}

	@Override
	protected void executeRule(
		@Nonnull Betreuung betreuung, @Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		if (!betreuung.isAngebotSchulamt() && !betreuung.getKeineKesbPlatzierung()) {
			// KESB Platzierung: Kein Anspruch (Platz wird von KESB bezahlt)
			verfuegungZeitabschnitt.setAnspruchberechtigtesPensum(0);
			verfuegungZeitabschnitt.addBemerkung(RuleKey.KESB_PLATZIERUNG, MsgKey.KESB_PLATZIERUNG_MSG);
		}
	}
}