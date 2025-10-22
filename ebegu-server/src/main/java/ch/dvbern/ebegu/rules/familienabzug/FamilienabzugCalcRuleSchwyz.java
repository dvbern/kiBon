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

package ch.dvbern.ebegu.rules.familienabzug;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;

import static ch.dvbern.ebegu.einstellung.EinstellungKey.SOZIALABZUG_PRO_KIND;

public class FamilienabzugCalcRuleSchwyz extends AbstractFamilienabzugCalcRule {

	private final BigDecimal sozialabzugProKind;

	protected FamilienabzugCalcRuleSchwyz(
		@Nonnull Map<EinstellungKey, Einstellung> einstellungMap,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		super(validityPeriod, locale);
		this.sozialabzugProKind = einstellungMap.get(
			SOZIALABZUG_PRO_KIND
		).getValueAsBigDecimal();
	}

	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData
	) {
		BigDecimal anzahlKinder = BigDecimal.valueOf(
			countAnzahlKinderFuerAbzug(
				inputData.getFamilienCalculationInput()
			)
		);
		inputData.setAbzugFamGroesse(
			MathUtil.GANZZAHL.from(
				anzahlKinder.multiply(
					sozialabzugProKind
				)
			)
		);
	}
}
