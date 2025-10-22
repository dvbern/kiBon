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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.enums.MsgKey;

public class MutationsMergerAnspruchHandlerSchwyz extends
	AbstractMutationsMergerAnspruchVorgaengerHandler {

	public MutationsMergerAnspruchHandlerSchwyz(Locale locale) {
		super(locale);
	}

	@Override
	public void handleAnpassungAnspruch(
		@Nonnull BGCalculationInput inputData,
		@Nullable BGCalculationResult resultVorangehenderAbschnitt,
		@Nonnull LocalDate mutationsEingansdatum
	) {
		super.handleAnpassungAnspruch(
			inputData,
			resultVorangehenderAbschnitt,
			mutationsEingansdatum
		);
		if (isMeldungZuSpaet(
			inputData.getParent().getGueltigkeit(),
			mutationsEingansdatum
		)) {
			removeAllAnspruchBemerkungen(inputData);
		}
	}

	private void removeAllAnspruchBemerkungen(BGCalculationInput inputData) {
		inputData.getParent()
			.getBemerkungenDTOList()
			.removeBemerkungByMsgKey(MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH);
		inputData.getParent()
			.getBemerkungenDTOList()
			.removeBemerkungByMsgKey(
				MsgKey.KEIN_ANSPRUCH_NICHT_BEITRAGSBERECHTIGT
			);
		inputData.getParent()
			.getBemerkungenDTOList()
			.removeBemerkungByMsgKey(MsgKey.SCHULSTUFE_VORSCHULE_MSG);
		inputData.getParent()
			.getBemerkungenDTOList()
			.removeBemerkungByMsgKey(MsgKey.SCHULSTUFE_KINDERGARTEN_1_MSG);
		inputData.getParent()
			.getBemerkungenDTOList()
			.removeBemerkungByMsgKey(
				MsgKey.SCHULSTUFE_FREIWILLIGER_KINDERGARTEN_MSG
			);
		inputData.getParent()
			.getBemerkungenDTOList()
			.removeBemerkungByMsgKey(MsgKey.SCHULSTUFE_PRIMARSTUFE_MSG);
		inputData.getParent()
			.getBemerkungenDTOList()
			.removeBemerkungByMsgKey(MsgKey.SCHULSTUFE_KINDERGARTEN_2_MSG);
	}
}
