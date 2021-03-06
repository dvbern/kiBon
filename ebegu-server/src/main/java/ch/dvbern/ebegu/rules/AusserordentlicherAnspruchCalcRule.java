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

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESFAMILIEN;

/**
 * Regel für den ausserordentlichen Anspruch. Sie beachtet:
 * Ausserordentliches Pensum übersteuert den Anspruch, der aus anderen Reglen berechnet wurde, AUSSER dieser wäre
 * höher. Diese Regel kann also den Anspruch nur hinaufsetzen, nie hinunter.
 */
public class AusserordentlicherAnspruchCalcRule extends AbstractCalcRule {

	public AusserordentlicherAnspruchCalcRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.AUSSERORDENTLICHER_ANSPRUCH, RuleType.GRUNDREGEL_CALC, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(KITA, TAGESFAMILIEN);
	}

	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData) {
		int ausserordentlicherAnspruch = inputData.getAusserordentlicherAnspruch();
		int pensumAnspruch = inputData.getAnspruchspensumProzent();
		// Es wird der grössere der beiden Werte genommen!
		if (ausserordentlicherAnspruch > pensumAnspruch) {
			inputData.setAnspruchspensumProzent(ausserordentlicherAnspruch);
			inputData.addBemerkung(
				MsgKey.AUSSERORDENTLICHER_ANSPRUCH_MSG,
				getLocale());
		}
	}
}
