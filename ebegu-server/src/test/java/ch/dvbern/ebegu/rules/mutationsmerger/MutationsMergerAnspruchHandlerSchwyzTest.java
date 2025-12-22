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

import java.util.Locale;
import java.util.stream.Stream;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.enums.MsgKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class MutationsMergerAnspruchHandlerSchwyzTest extends
	AbstractMutationsMergerAnspruchVorgaengerHandlerTest {

	static final MutationsMergerAnspruchHandlerSchwyz MUTATIONS_MERGER_ANSPRUCH_HANDLER_SCHWYZ =
		new MutationsMergerAnspruchHandlerSchwyz(
			Locale.GERMAN
		);

	@Override
	protected AbstractMutationsMergerAnspruchVorgaengerHandler createHandler() {
		return MUTATIONS_MERGER_ANSPRUCH_HANDLER_SCHWYZ;
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
}
