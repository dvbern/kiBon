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
 *
 */

package ch.dvbern.ebegu.rules.mutationsmerger;

import java.math.BigDecimal;
import java.util.Locale;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MutationsMergerAnspruchHandlerFKJVFristenTest extends
	AbstractDefaultMutationsMergerAnspruchHandlerTest {

	static final MutationsMergerAnspruchHandlerFKJVFristen MUTATIONS_MERGER_ANSPRUCH_HANDLER_DEFAULT =
		new MutationsMergerAnspruchHandlerFKJVFristen(
			Locale.GERMAN
		);

	@Override
	protected MutationsMergerAnspruchHandler createHandler() {
		return MUTATIONS_MERGER_ANSPRUCH_HANDLER_DEFAULT;
	}

	@Test
	void test_hoere_anspruchspensumProzent_MutationGleicheMonat_AnzahlGSSteigt_aenderung() {
		BGCalculationInput bgCalculationInput = initInputData(
			HUNDERT_PERCENT,
			2
		);
		BGCalculationResult resultVorgaenger = initResultData(
			ZEHN_PERCENT,
			BigDecimal.ONE
		);
		mutationsMergerAnspruchHandler.handleAnpassungAnspruch(
			bgCalculationInput,
			resultVorgaenger,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT
		);
		Assertions.assertEquals(
			HUNDERT_PERCENT,
			bgCalculationInput.getAnspruchspensumProzent()
		);
	}

	@Test
	void test_kleinere_anspruchspensumProzent_MutationGleicheMonat_AnzahlGSSteigt_aenderung() {
		BGCalculationInput bgCalculationInput = initInputData(ZEHN_PERCENT, 2);
		BGCalculationResult resultVorgaenger = initResultData(
			HUNDERT_PERCENT,
			BigDecimal.ONE
		);
		mutationsMergerAnspruchHandler.handleAnpassungAnspruch(
			bgCalculationInput,
			resultVorgaenger,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT
		);
		Assertions.assertEquals(
			ZEHN_PERCENT,
			bgCalculationInput.getAnspruchspensumProzent()
		);
	}

	@Test
	void test_hoere_anspruchspensumProzent_MutationGleicheMonat_AnzahlGSSink_aenderung() {
		BGCalculationInput bgCalculationInput = initInputData(
			HUNDERT_PERCENT,
			1
		);
		BGCalculationResult resultVorgaenger = initResultData(
			ZEHN_PERCENT,
			new BigDecimal(2)
		);
		mutationsMergerAnspruchHandler.handleAnpassungAnspruch(
			bgCalculationInput,
			resultVorgaenger,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT
		);
		Assertions.assertEquals(
			HUNDERT_PERCENT,
			bgCalculationInput.getAnspruchspensumProzent()
		);
	}

	@Test
	void test_kleinere_anspruchspensumProzent_MutationGleicheMonat_AnzahlGSSink_aenderung() {
		BGCalculationInput bgCalculationInput = initInputData(ZEHN_PERCENT, 1);
		BGCalculationResult resultVorgaenger = initResultData(
			HUNDERT_PERCENT,
			new BigDecimal(2)
		);
		mutationsMergerAnspruchHandler.handleAnpassungAnspruch(
			bgCalculationInput,
			resultVorgaenger,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT
		);
		Assertions.assertEquals(
			ZEHN_PERCENT,
			bgCalculationInput.getAnspruchspensumProzent()
		);
	}

	private BGCalculationInput initInputData(
		int anspruchspensumProzent,
		int anzahlGesuchsteller
	) {
		BGCalculationInput bgCalculationInput = initInputData(
			anspruchspensumProzent
		);
		bgCalculationInput.getFamilienCalculationInput()
			.setAnzahlGesuchsteller(anzahlGesuchsteller);
		return bgCalculationInput;
	}

	private BGCalculationResult initResultData(
		int anspruchspensumProzent,
		BigDecimal anzahlGesuchsteller
	) {
		BGCalculationResult resultVorgaenger = initResultData(
			anspruchspensumProzent
		);
		resultVorgaenger.setAnzahlGesuchsteller(anzahlGesuchsteller);
		return resultVorgaenger;
	}

}
