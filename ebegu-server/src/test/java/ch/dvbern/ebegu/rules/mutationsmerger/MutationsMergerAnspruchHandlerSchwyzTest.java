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

import java.time.LocalDate;
import java.util.Locale;
import java.util.stream.Stream;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;

class MutationsMergerAnspruchHandlerSchwyzTest {

	//01.09.XXXX
	private static final LocalDate START_VERFUEGUNG_ABSCHNITT_ERSTGESUCH =
		START_PERIODE.plusMonths(1);
	//30.09.XXXX
	private static final LocalDate END_VERFUEGUNG_ABSCHNITT_ERSTGESUCH =
		START_PERIODE.plusMonths(2).minusDays(1);

	private static final LocalDate EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT =
		START_PERIODE.plusMonths(1).plusDays(5);

	private static final MutationsMergerAnspruchHandlerSchwyz MUTATIONS_MERGER_ANSPRUCH_HANDLER_SCHWYZ =
		new MutationsMergerAnspruchHandlerSchwyz(
			Locale.GERMAN
		);

	private static final int HUNDERT_PERCENT = 100;

	private static final int ZEHN_PERCENT = 10;

	@Test
	void test_hoere_anspruchspensumProzent_MutationGleicheMonat_keineaenderung() {
		BGCalculationInput bgCalculationInput = initInputData(HUNDERT_PERCENT);
		BGCalculationResult resultVorgaenger = initResultData(ZEHN_PERCENT);
		MUTATIONS_MERGER_ANSPRUCH_HANDLER_SCHWYZ.handleAnpassungAnspruch(
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
	void test_kleinere_anspruchspensumProzent_MutationGleicheMonat_keineaenderung() {
		BGCalculationInput bgCalculationInput = initInputData(ZEHN_PERCENT);
		BGCalculationResult resultVorgaenger = initResultData(HUNDERT_PERCENT);
		MUTATIONS_MERGER_ANSPRUCH_HANDLER_SCHWYZ.handleAnpassungAnspruch(
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
	void test_hoere_anspruchspensumProzent_MutationBevorMonat_aenderung() {
		BGCalculationInput bgCalculationInput = initInputData(HUNDERT_PERCENT);
		BGCalculationResult resultVorgaenger = initResultData(ZEHN_PERCENT);
		MUTATIONS_MERGER_ANSPRUCH_HANDLER_SCHWYZ.handleAnpassungAnspruch(
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
		MUTATIONS_MERGER_ANSPRUCH_HANDLER_SCHWYZ.handleAnpassungAnspruch(
			bgCalculationInput,
			resultVorgaenger,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT.minusMonths(1)
		);
		Assertions.assertEquals(
			ZEHN_PERCENT,
			bgCalculationInput.getAnspruchspensumProzent()
		);
	}

	@ParameterizedTest
	@MethodSource("messageKeyForAnspruch")
	void test_bemerkung_bleibt(MsgKey bemerkung) {
		BGCalculationInput bgCalculationInput = initInputData(ZEHN_PERCENT);
		bgCalculationInput.addBemerkung(bemerkung, Locale.GERMAN);
		MUTATIONS_MERGER_ANSPRUCH_HANDLER_SCHWYZ.handleAnpassungAnspruch(
			bgCalculationInput,
			null,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT.minusMonths(1)
		);
		Assertions.assertTrue(
			bgCalculationInput.getParent()
				.getBemerkungenDTOList()
				.containsMsgKey(bemerkung)
		);
	}

	@ParameterizedTest
	@MethodSource("messageKeyForAnspruch")
	void test_bemerkung_geloescht(MsgKey bemerkung) {
		BGCalculationInput bgCalculationInput = initInputData(ZEHN_PERCENT);
		bgCalculationInput.addBemerkung(bemerkung, Locale.GERMAN);
		MUTATIONS_MERGER_ANSPRUCH_HANDLER_SCHWYZ.handleAnpassungAnspruch(
			bgCalculationInput,
			null,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH_ABSCHNITT
		);
		Assertions.assertFalse(
			bgCalculationInput.getParent()
				.getBemerkungenDTOList()
				.containsMsgKey(bemerkung)
		);
	}

	static Stream<MsgKey> messageKeyForAnspruch() {
		return Stream.of(
			MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH,
			MsgKey.KEIN_ANSPRUCH_NICHT_BEITRAGSBERECHTIGT,
			MsgKey.SCHULSTUFE_VORSCHULE_MSG,
			MsgKey.SCHULSTUFE_KINDERGARTEN_1_MSG,
			MsgKey.SCHULSTUFE_FREIWILLIGER_KINDERGARTEN_MSG,
			MsgKey.SCHULSTUFE_PRIMARSTUFE_MSG,
			MsgKey.SCHULSTUFE_KINDERGARTEN_2_MSG
		);
	}

	private BGCalculationInput initInputData(int anspruchspensumProzent) {
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
		return input;
	}

	private BGCalculationResult initResultData(int anspruchspensumProzent) {
		BGCalculationResult result = new BGCalculationResult();
		result.setAnspruchspensumProzent(anspruchspensumProzent);
		return result;
	}
}
