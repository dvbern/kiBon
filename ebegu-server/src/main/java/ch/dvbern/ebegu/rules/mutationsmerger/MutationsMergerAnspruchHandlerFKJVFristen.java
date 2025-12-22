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

package ch.dvbern.ebegu.rules.mutationsmerger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;

public class MutationsMergerAnspruchHandlerFKJVFristen extends
	MutationsMergerAnspruchHandler {

	public MutationsMergerAnspruchHandlerFKJVFristen(Locale locale) {
		super(locale);
	}

	@Override
	public void handleAnpassungAnspruch(
		@Nonnull BGCalculationInput inputData,
		@Nullable BGCalculationResult resultVorangehenderAbschnitt,
		@Nonnull LocalDate mutationsEingansdatum
	) {
		// Wenn sich die Anzahl der Gesuchsteller gegenüber dem vorherigen Abschnitt verändert hat,
		// liegt eine Änderung der Familiensituation in diesem Mutation vor.
		// In diesem Fall übernehmen wir die neuen Werte unverändert – unabhängig davon,
		// ob wir zeitlich vor oder nach dem Mutationsdatum liegen.
		if (resultVorangehenderAbschnitt != null
			&&
			areAnzahlGesuchstellerGroesserOderKleinerAlsFrueher(
				inputData,
				resultVorangehenderAbschnitt
			)) {
			return;
		}
		super.handleAnpassungAnspruch(
			inputData,
			resultVorangehenderAbschnitt,
			mutationsEingansdatum
		);
	}

	private boolean areAnzahlGesuchstellerGroesserOderKleinerAlsFrueher(
		BGCalculationInput inputData,
		BGCalculationResult resultVorgaenger
	) {
		BigDecimal inputValue = BigDecimal.valueOf(
			inputData.getFamilienCalculationInput().getAnzahlGesuchsteller()
		);

		return inputValue.compareTo(resultVorgaenger.getAnzahlGesuchsteller())
			!= 0;
	}
}
