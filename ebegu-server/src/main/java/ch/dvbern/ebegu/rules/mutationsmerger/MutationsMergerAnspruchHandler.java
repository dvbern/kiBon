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
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

public class MutationsMergerAnspruchHandler extends
	AbstractMutationsMergerAnspruchHandler {

	public MutationsMergerAnspruchHandler(Locale locale) {
		super(locale);
	}

	@Override
	public void handleAnpassungAnspruch(
		@Nonnull BGCalculationInput inputData,
		@Nullable BGCalculationResult resultVorangehenderAbschnitt,
		@Nonnull LocalDate mutationsEingansdatum
	) {
		DateRange gueltigkeit = inputData.getParent().getGueltigkeit();

		//Meldung rechtzeitig: In diesem Fall wird der Anspruch zusammen mit dem Ereigniseintritt des Arbeitspensums angepasst. -> keine Aenderungen
		if (!isMeldungZuSpaet(gueltigkeit, mutationsEingansdatum)) {
			return;
		}

		final int anspruchberechtigtesPensum = inputData
			.getAnspruchspensumProzent();
		final int anspruchAufVorgaengerVerfuegung = resultVorangehenderAbschnitt
			== null ?
				0 :
				resultVorangehenderAbschnitt
					.getAnspruchspensumProzent();

		if (anspruchberechtigtesPensum > anspruchAufVorgaengerVerfuegung) {
			//Anspruch wird erhöht
			//Meldung nicht Rechtzeitig: Der Anspruch kann sich erst auf den Folgemonat des Eingangsdatum erhöhen
			inputData.setAnspruchspensumProzent(
				anspruchAufVorgaengerVerfuegung
			);

			if (!isBetreuungAnspruchErhoeht(
				inputData,
				resultVorangehenderAbschnitt
			)) {
				inputData.setRueckwirkendReduziertesPensumRest(
					anspruchberechtigtesPensum - anspruchAufVorgaengerVerfuegung
				);
			}

			//Wenn der Anspruch auf dem Vorgänger 0 ist, weil das Erstgesuch zu spät eingereicht wurde
			//soll die Bemerkung bezüglich der Erhöhung nicht angezeigt werden, da es sich um keine Erhöhung handelt
			if (!isAnspruchZeroBecauseVorgaengerZuSpaet(
				resultVorangehenderAbschnitt
			)) {
				inputData.addBemerkungWithGueltigkeitOfAbschnitt(
					MsgKey.ANSPRUCHSAENDERUNG_MSG,
					locale
				);
			}

		} else if (anspruchberechtigtesPensum
			< anspruchAufVorgaengerVerfuegung) {
			//Meldung nicht Rechtzeitig: Reduktionen des Anspruchs sind auch rückwirkend erlaubt -> keine Aenderungen
			inputData.addBemerkungWithGueltigkeitOfAbschnitt(
				MsgKey.REDUCKTION_RUECKWIRKEND_MSG,
				locale
			);
		}
	}

	/**
	 * compare the current Betreuungspensum to Betreuungspensum from vorgaengerVerfuegung
	 * this check fixes a bug when there is more Anspruch in a mutation and at the same time the betreuungspensum is
	 * increased
	 */
	private boolean isBetreuungAnspruchErhoeht(
		@Nonnull BGCalculationInput inputData,
		@Nullable BGCalculationResult resultVorangehenderAbschnitt
	) {

		if (resultVorangehenderAbschnitt == null) {
			return false;
		}

		BigDecimal a = inputData.getBetreuungspensumProzent();
		BigDecimal b = resultVorangehenderAbschnitt
			.getBetreuungspensumProzent();

		return a.compareTo(b) > 0;
	}

	private boolean isAnspruchZeroBecauseVorgaengerZuSpaet(
		@Nullable BGCalculationResult resultVorangehenderAbschnitt
	) {
		if (resultVorangehenderAbschnitt == null) {
			return false;
		}

		boolean anspruchsPensumZero = resultVorangehenderAbschnitt
			.getAnspruchspensumProzent()
			== 0;
		return anspruchsPensumZero
			&& resultVorangehenderAbschnitt.isZuSpaetEingereicht();
	}
}
