/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.kind;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.AbstractCalcRule;
import ch.dvbern.ebegu.rules.RuleKey;
import ch.dvbern.ebegu.rules.RuleType;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.util.Constants;

public class KindTerminiertCalcRule extends AbstractCalcRule {

	public KindTerminiertCalcRule(
		@Nonnull Locale locale
	) {
		super(
			RuleKey.KIND_ANSPRUCH,
			RuleType.GRUNDREGEL_CALC,
			RuleValidity.ASIV,
			Constants.DEFAULT_GUELTIGKEIT,
			locale
		);
	}

	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData
	) {
		if (inputData.isKindTerminiert()) {
			inputData.setAnspruchZeroAndSaveRestanspruch();
			inputData.addBemerkungWithGueltigkeitOfAbschnitt(
				MsgKey.KEIN_ANSPRUCH_KIND_TERMINIERT,
				getLocale()
			);
			inputData.getParent()
				.getBemerkungenDTOList()
				.removeBemerkungByMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH);
		}
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
	}
}
