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

package ch.dvbern.ebegu.testfaelle.dataprovider;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;

public class SchwyzErweitertTestfallDataProvider extends
	SchwyzTestfallDataProvider {

	protected SchwyzErweitertTestfallDataProvider(
		Gesuchsperiode gesuchsperiode
	) {
		super(gesuchsperiode);
	}

	@Override
	public FinanzielleSituation createFinanzielleSituation(
		BigDecimal vermoegen,
		BigDecimal einkommen
	) {
		FinanzielleSituation finanzielleSituation =
			super.createFinanzielleSituation(vermoegen, einkommen);
		finanzielleSituation.setLiegenschaftsErtraege(BigDecimal.ZERO);
		return finanzielleSituation;
	}

	@Override
	public FinanzielleSituationTyp getFinanzielleSituationTyp() {
		return FinanzielleSituationTyp.SCHWYZ_ERWEITERT;
	}
}
