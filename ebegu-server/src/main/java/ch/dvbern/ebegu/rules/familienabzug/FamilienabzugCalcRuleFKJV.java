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
import ch.dvbern.ebegu.dto.FamilienGroesseCalculationInput;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Umsetzung der ASIV Revision
 * <p>
 * 2. Immer aktuelle Familiengrösse
 * <p>
 * Gem. neuer ASIV Verordnung müssen die Kinder für die Berechnung der Familiengrösse ab dem Beginn den Monats NACH dem
 * Ereigniseintritt (e.g. Geburt) berücksichtigt werden. Dasselbe gilt bei der Aenderung des Zivilstands. Bei einer
 * Mutation
 * der Familiensituation ist das Datum "Aendern per" relevant.
 */
@SuppressWarnings("MethodParameterNamingConvention")
public class FamilienabzugCalcRuleFKJV extends
	AbstractFamilienabzugCalcRuleBern {

	public FamilienabzugCalcRuleFKJV(
		@Nonnull Map<EinstellungKey, Einstellung> einstellungMap,
		DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		super(einstellungMap, validityPeriod, locale);
	}

	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData
	) {
		double familiengroesse = countFamiliengroesse(
			inputData.getFamilienCalculationInput()
		);

		inputData.setAbzugFamGroesseTotal(
			calculateAbzugAufgrundFamiliengroesse(
				familiengroesse,
				countAnzahlPersonen(
					inputData.getFamilienCalculationInput()
				)
			)
		);

		inputData.setFamGroesseTotal(BigDecimal.valueOf(familiengroesse));
	}

	@Override
	protected int countAnzahlPersonen(FamilienGroesseCalculationInput input) {
		// in der FKJV Periode 22/23 hatten wir einen Fehler, der die Kinder immer mitzählte, auch wenn es für ein Kind keinen
		// Kinderabzug gab.
		// dies wird auf die Periode 23/24 geändert. Kinder ohne Kinderabzug zählen nicht zur Familiengrösse und Kinder für die
		// der
		// halbe oder der ganze Pauschalabzug abgezogen werden kann, zählen ganz zur Familiengrösse
		int anzahlKinder = input.getKinderabzugList().size();
		return input.getAnzahlGesuchsteller() + anzahlKinder;
	}
}
