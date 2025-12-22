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

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.enums.MsgKey;

public class MutationsMergerFinanzielleSituationBernFKJVFristen extends
	MutationsMergerFinanzielleSituationBernFKJV {

	public MutationsMergerFinanzielleSituationBernFKJVFristen(Locale local) {
		super(local);
	}

	@Override
	protected void handleEinkommen(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz,
		LocalDate mutationsEingansdatum
	) {
		if (inputAktuel.getParent()
			.getGueltigkeit()
			.getGueltigAb()
			.isAfter(mutationsEingansdatum)) {
			if (isZeitabschnittVorAenderungPerDatum(platz, inputAktuel)) {
				handleFamiliengroesse(inputAktuel, resultVorgaenger);
			}
			return;
		}
		handleVerminderungEinkommen(
			inputAktuel,
			resultVorgaenger,
			platz,
			mutationsEingansdatum
		);
		handleFinanzielleSituationRueckwirkendAnpassen(
			inputAktuel,
			resultVorgaenger,
			platz
		);
	}

	private boolean isZeitabschnittVorAenderungPerDatum(
		AbstractPlatz platz,
		BGCalculationInput inputAktuel
	) {
		return platz.extractGesuch()
			.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.getAenderungPer()
			!= null
			&& !inputAktuel.getParent()
				.getGueltigkeit()
				.getGueltigAb()
				.isAfter(
					platz.extractGesuch()
						.getFamiliensituationContainer()
						.getFamiliensituationJA()
						.getAenderungPer()
				);
	}

	private void handleFamiliengroesse(
		BGCalculationInput inputData,
		BGCalculationResult resultVorgaenger
	) {
		inputData.setFamGroesseTotal(resultVorgaenger.getFamGroesse());
		inputData.setAbzugFamGroesseTotal(
			resultVorgaenger.getAbzugFamGroesse()
		);
	}

	private void handleFinanzielleSituationRueckwirkendAnpassen(
		BGCalculationInput inputData,
		BGCalculationResult resultVorgaenger,
		AbstractPlatz platz
	) {
		// wenn einkommensjahr nicht gleich basisjahr, kommt das Einkommen aus EKV,
		// dies soll nie rückwirkend überschrieben werden
		if (platz.extractGesuchsperiode().getBasisJahr()
			!= inputData.getEinkommensjahr()) {
			return;
		}

		BigDecimal massgebendesEinkommenFinSit =
			getMassgebendesEinkommenFromFinSit(inputData, platz);

		if (isFinSitRueckwirkendAnzupassen(
			inputData,
			massgebendesEinkommenFinSit,
			resultVorgaenger,
			platz
		)) {
			finsitRueckwirkendAnpassen(
				inputData,
				massgebendesEinkommenFinSit,
				platz
			);
		} else {
			handleFamiliengroesse(inputData, resultVorgaenger);
		}

		if (massgebendesEinkommenFinSit.compareTo(
			resultVorgaenger.getMassgebendesEinkommenVorAbzugFamgr()
		) < 0) {
			inputData.addBemerkungWithGueltigkeitOfAbschnitt(
				MsgKey.ANSPRUCHSAENDERUNG_MSG,
				getLocale()
			);
		}
	}

	protected void setFinSitDataFromVorgaengerToInput(
		@Nonnull BGCalculationInput input,
		@Nonnull BGCalculationResult result
	) {
		input.setMassgebendesEinkommenVorAbzugFamgr(
			result.getMassgebendesEinkommenVorAbzugFamgr()
		);
		input.setEinkommensjahr(result.getEinkommensjahr());
		input.setSozialhilfeempfaenger(result.isSozialhilfeAkzeptiert());
		input.setVerguenstigungGewuenscht(result.isVerguenstigungGewuenscht());

		input.setKeinAnspruchAufgrundEinkommen(
			result.isKategorieMaxEinkommen()
		);
		input.setKategorieMaxEinkommen(
			result.isKategorieMaxEinkommen()
		);
	}
}
