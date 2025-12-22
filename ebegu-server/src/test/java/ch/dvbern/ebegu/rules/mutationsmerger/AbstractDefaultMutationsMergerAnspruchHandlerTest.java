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
import java.time.LocalDate;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;

public abstract class AbstractDefaultMutationsMergerAnspruchHandlerTest<T extends MutationsMergerAnspruchHandler> {
	//01.09.XXXX
	static final LocalDate START_VERFUEGUNG_ABSCHNITT_ERSTGESUCH =
		START_PERIODE.plusMonths(1);
	//30.09.XXXX
	static final LocalDate END_VERFUEGUNG_ABSCHNITT_ERSTGESUCH =
		START_PERIODE.plusMonths(2).minusDays(1);

	static final LocalDate EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT =
		START_PERIODE.plusMonths(1).plusDays(5);

	static final int HUNDERT_PERCENT = 100;

	static final int ZEHN_PERCENT = 10;

	protected T mutationsMergerAnspruchHandler;

	protected abstract T createHandler();

	@BeforeEach
	void setup() {
		mutationsMergerAnspruchHandler = createHandler();
	}

	@Test
	void test_hoere_anspruchspensumProzent_MutationGleicheMonat_keineaenderung() {
		BGCalculationInput bgCalculationInput = initInputData(HUNDERT_PERCENT);
		BGCalculationResult resultVorgaenger = initResultData(ZEHN_PERCENT);
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
	void test_kleinere_anspruchspensumProzent_MutationGleicheMonat_aenderung() {
		BGCalculationInput bgCalculationInput = initInputData(ZEHN_PERCENT);
		BGCalculationResult resultVorgaenger = initResultData(HUNDERT_PERCENT);
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
	void test_hoere_anspruchspensumProzent_MutationBevorMonat_aenderung() {
		BGCalculationInput bgCalculationInput = initInputData(HUNDERT_PERCENT);
		BGCalculationResult resultVorgaenger = initResultData(ZEHN_PERCENT);
		mutationsMergerAnspruchHandler.handleAnpassungAnspruch(
			bgCalculationInput,
			resultVorgaenger,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT.minusMonths(1)
		);
		Assertions.assertEquals(
			HUNDERT_PERCENT,
			bgCalculationInput.getAnspruchspensumProzent()
		);
	}

	@Test
	void test_kleinere_anspruchspensumProzent_MutationBevorMonat_aenderung() {
		BGCalculationInput bgCalculationInput = initInputData(ZEHN_PERCENT);
		BGCalculationResult resultVorgaenger = initResultData(HUNDERT_PERCENT);
		mutationsMergerAnspruchHandler.handleAnpassungAnspruch(
			bgCalculationInput,
			resultVorgaenger,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT.minusMonths(1)
		);
		Assertions.assertEquals(
			ZEHN_PERCENT,
			bgCalculationInput.getAnspruchspensumProzent()
		);
	}

	BGCalculationInput initInputData(int anspruchspensumProzent) {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt =
			new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(
			new DateRange(
				START_VERFUEGUNG_ABSCHNITT_ERSTGESUCH,
				END_VERFUEGUNG_ABSCHNITT_ERSTGESUCH
			)
		);
		BGCalculationInput input = new BGCalculationInput(
			verfuegungZeitabschnitt,
			RuleValidity.ASIV
		);
		input.setAnspruchspensumProzent(anspruchspensumProzent);
		input.getFamilienCalculationInput().setAnzahlGesuchsteller(1);
		return input;
	}

	BGCalculationResult initResultData(int anspruchspensumProzent) {
		BGCalculationResult result = new BGCalculationResult();
		result.setAnspruchspensumProzent(anspruchspensumProzent);
		result.setAnzahlGesuchsteller(BigDecimal.ONE);
		return result;
	}
}
