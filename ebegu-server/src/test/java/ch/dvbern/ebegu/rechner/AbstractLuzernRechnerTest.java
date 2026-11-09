/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.types.DateRange;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AbstractLuzernRechnerTest extends AbstractBGRechnerTest {

	protected final DateRange dateRangePartFebruary = new DateRange(
		LocalDate.of(2019, Month.FEBRUARY, 10),
		LocalDate.of(2019, Month.FEBRUARY, 28)
	);

	protected final LocalDate geburtstagBaby = LocalDate.of(
		2018,
		Month.OCTOBER,
		15
	);
	protected final LocalDate geburtstagKind = LocalDate.of(
		2016,
		Month.OCTOBER,
		15
	);

	protected VerfuegungZeitabschnitt prepareVerfuegung(TestValues testValues) {

		LocalDate geburtstag = testValues.isBaby ?
			geburtstagBaby :
			geburtstagKind;

		Verfuegung verfuegung = prepareVerfuegungKita(
			geburtstag,
			testValues.gueltigkeit.getGueltigAb(),
			testValues.gueltigkeit.getGueltigBis(),
			EinschulungTyp.VORSCHULALTER,
			false,
			testValues.einkommen,
			testValues.monatlicheBetreuungsKosten
		);

		VerfuegungZeitabschnitt verfuegungZeitabschnitt = verfuegung
			.getZeitabschnitte()
			.get(0);
		verfuegungZeitabschnitt.getBgCalculationInputAsiv()
			.setAnspruchspensumProzent(testValues.anspruchsPensum);
		verfuegungZeitabschnitt.getBgCalculationInputAsiv()
			.setBetreuungspensumProzent(testValues.betreuungsPensum);
		verfuegungZeitabschnitt.getBgCalculationInputAsiv()
			.setBesondereBeduerfnisseZuschlag(
				testValues.besondereBeduerfnisseZuschlag
			);
		verfuegungZeitabschnitt.getBgCalculationInputAsiv()
			.setStuendlicheVollkosten(testValues.stuendlicheVollkosten);
		verfuegungZeitabschnitt.setBabyTarifForAsivAndGemeinde(
			geburtstag.plusYears(1)
				.isAfter(
					verfuegungZeitabschnitt.getGueltigkeit()
						.getGueltigBis()
				)
		);
		return verfuegung.getZeitabschnitte().get(0);
	}

	protected void assertCalculationResultResult(
		BGCalculationResult result,
		TestValues testValues
	) {
		assertEquals(
			testValues.expectedVollkosten.stripTrailingZeros(),
			result.getVollkosten().stripTrailingZeros(),
			"Vollkosten not equal"
		);
		assertEquals(
			testValues.betreuungsPensum.stripTrailingZeros(),
			result.getBetreuungspensumProzent().stripTrailingZeros(),
			"Betreuungspensum not equal"
		);
		assertEquals(
			testValues.anspruchsPensum,
			result.getAnspruchspensumProzent(),
			"Anspruchspensum not equal"
		);
		assertEquals(
			testValues.expectedElternbeitrag.stripTrailingZeros(),
			result.getElternbeitrag().stripTrailingZeros(),
			"Elternebeitrag not equal"
		);
		assertEquals(
			testValues.expectedMinimalerElternbeitrag.stripTrailingZeros(),
			result.getMinimalerElternbeitrag().stripTrailingZeros(),
			"Minimaler Elternbeitrag not equal"
		);
		assertEquals(
			testValues.expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag
				.stripTrailingZeros(),
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag()
				.stripTrailingZeros(),
			"VerguenstigungOhneBeruckesichtigungMinimalbetrag not equal"
		);
		assertEquals(
			testValues.expectedVerguenstigungOhneBeruecksichtigungVollkosten
				.stripTrailingZeros(),
			result.getVerguenstigungOhneBeruecksichtigungVollkosten()
				.stripTrailingZeros(),
			"VerguenstigungOhneBeruecksichtigungVollkosten not equal"
		);
		assertEquals(
			testValues.expectedVerguenstigung.stripTrailingZeros(),
			result.getVerguenstigung().stripTrailingZeros(),
			"Verguenstigung not equal"
		);

		if (testValues.expectedVerguenstigungProZeiteinheit != null) {
			assertNotNull(
				result.getVerguenstigungProZeiteinheit(),
				"Verguentstigung pro Zeiteinheit is null"
			);
			assertEquals(
				testValues.expectedVerguenstigungProZeiteinheit
					.stripTrailingZeros(),
				result.getVerguenstigungProZeiteinheit()
					.stripTrailingZeros(),
				"Verguentstigung pro Zeiteinheit not equal"
			);
		}

		assertEquals(
			testValues.expectedBetreuungsZeiteinheit.stripTrailingZeros(),
			result.getBetreuungspensumZeiteinheit().stripTrailingZeros(),
			"Betreuungszeiteinheit not equal"
		);
		assertEquals(
			testValues.expectedAnspruchsZeiteinheit.stripTrailingZeros(),
			result.getAnspruchspensumZeiteinheit().stripTrailingZeros(),
			"Anspruchszeiteinheit not equal"
		);
		assertEquals(
			testValues.expectedBgZeiteinheit.stripTrailingZeros(),
			result.getBgPensumZeiteinheit().stripTrailingZeros(),
			"BGZeiteinheit not equal"
		);
		BigDecimal bgPensum = testValues.betreuungsPensum.min(
			BigDecimal.valueOf(testValues.anspruchsPensum)
		);
		assertEquals(
			bgPensum.stripTrailingZeros(),
			result.getBgPensumProzent().stripTrailingZeros(),
			"BGPensum not equal"
		);

		if (result.getGutscheinEingewoehnung() == null) {
			assertThat(
				"Gutschein Eingewöhnung not zero",
				BigDecimal.ZERO,
				is(
					testValues.expectedGutscheinEingewoehnung
						.stripTrailingZeros()
				)
			);
		} else {
			assertThat(
				"Gutschein Eingewöhnung not equal",
				result.getGutscheinEingewoehnung().stripTrailingZeros(),
				is(
					testValues.expectedGutscheinEingewoehnung
						.stripTrailingZeros()
				)
			);
		}
	}

	protected static class TestValues {
		protected BigDecimal monatlicheBetreuungsKosten = BigDecimal.ZERO;
		protected BigDecimal betreuungsPensum = BigDecimal.ZERO;
		protected int anspruchsPensum;
		protected BigDecimal einkommen = BigDecimal.ZERO;
		protected BigDecimal besondereBeduerfnisseZuschlag = BigDecimal.ZERO;
		protected boolean isBaby = false;
		protected BigDecimal stuendlicheVollkosten = BigDecimal.ZERO;

		protected BigDecimal expectedVollkosten = BigDecimal.ZERO;
		protected BigDecimal expectedVerguenstigungOhneBeruecksichtigungMinimalbetrag =
			BigDecimal.ZERO;
		protected BigDecimal expectedVerguenstigungOhneBeruecksichtigungVollkosten =
			BigDecimal.ZERO;
		protected BigDecimal expectedVerguenstigung = BigDecimal.ZERO;
		protected BigDecimal expectedVerguenstigungProZeiteinheit;
		protected BigDecimal expectedElternbeitrag = BigDecimal.ZERO;
		protected BigDecimal expectedMinimalerElternbeitrag = BigDecimal.ZERO;
		//Zeiteinheit = Tage bei Kitas, Stunden bei TFOs
		protected BigDecimal expectedBetreuungsZeiteinheit = BigDecimal.ZERO;
		protected BigDecimal expectedAnspruchsZeiteinheit = BigDecimal.ZERO;
		protected BigDecimal expectedBgZeiteinheit = BigDecimal.ZERO;
		protected BigDecimal expectedGutscheinEingewoehnung = BigDecimal.ZERO;

		protected DateRange gueltigkeit = new DateRange(
			LocalDate.of(2019, Month.AUGUST, 1),
			LocalDate.of(2019, Month.AUGUST, 31)
		);
	}
}
