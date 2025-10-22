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
 *
 */

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.GeschwisterbonusTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KitaTagestrukturenSchwyzNeuerElternbeitragRechnerTest {

	public static final BigDecimal MINIMALER_ELTERNBEITRAG = new BigDecimal(
		"492.00"
	);

	@Test
	void testKind1() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		parameter.setGeschwisterbonusTyp(GeschwisterbonusTyp.SCHWYZ_2);
		parameter.setSchulergaenzendeBetreuung(true);
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(
			MINIMALER_ELTERNBEITRAG,
			result.getMinimalerElternbeitrag()
		);
	}

	@Test
	void testKind1HalberMonat() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		parameter.setGeschwisterbonusTyp(GeschwisterbonusTyp.SCHWYZ_2);
		parameter.setSchulergaenzendeBetreuung(true);
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(
			new DateRange(
				LocalDate.of(2024, Month.APRIL, 16),
				LocalDate.of(2024, Month.APRIL, 30)
			)
		);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();

		assertEquals(
			new BigDecimal("246.00"),
			result.getMinimalerElternbeitrag()
		);
		assertEquals(
			new BigDecimal("0.00"),
			result.getMinimalerElternbeitragGekuerzt()
		);
	}

	@Test
	void testKind2() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		parameter.setGeschwisterbonusTyp(GeschwisterbonusTyp.SCHWYZ_2);
		parameter.setSchulergaenzendeBetreuung(true);
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setAnzahlGeschwister(0);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();

		assertEquals(new BigDecimal("558.80"), result.getVerguenstigung());
		assertEquals(
			MINIMALER_ELTERNBEITRAG,
			result.getMinimalerElternbeitrag()
		);
		assertEquals(
			new BigDecimal("0.00"),
			result.getMinimalerElternbeitragGekuerzt()
		);
	}

	@Test
	void testKind3() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		parameter.setGeschwisterbonusTyp(GeschwisterbonusTyp.SCHWYZ_2);
		parameter.setSchulergaenzendeBetreuung(true);
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setAnzahlGeschwister(11);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();

		assertEquals(
			MINIMALER_ELTERNBEITRAG,
			result.getMinimalerElternbeitrag()
		);
		assertEquals(
			new BigDecimal("0.00"),
			result.getMinimalerElternbeitragGekuerzt()
		);
		assertEquals(new BigDecimal("0.00"), result.getElternbeitrag());
	}

	@Test
	void testKind4() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		parameter.setGeschwisterbonusTyp(GeschwisterbonusTyp.SCHWYZ_2);
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setEinschulungTyp(null);
		input.setBetreuungInFerienzeit(false);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(
			MINIMALER_ELTERNBEITRAG,
			result.getMinimalerElternbeitrag()
		);
		assertEquals(
			new BigDecimal("0.00"),
			result.getMinimalerElternbeitragGekuerzt()
		);
		assertEquals(new BigDecimal("343.85"), result.getElternbeitrag());
	}

	@Test
	void testKind5_MassgebendesEinkommenNaheObergrenze() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		parameter.setGeschwisterbonusTyp(GeschwisterbonusTyp.SCHWYZ_2);
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setEinschulungTyp(null);
		input.setBetreuungInFerienzeit(true);
		input
			.setMassgebendesEinkommenVorAbzugFamgr(
				parameter.getMaxMassgebendesEinkommen()
					.subtract(BigDecimal.TEN)
			);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("0.20"), result.getVerguenstigung());
		assertEquals(
			MINIMALER_ELTERNBEITRAG,
			result.getMinimalerElternbeitrag()
		);
		assertEquals(
			new BigDecimal("0.00"),
			result.getMinimalerElternbeitragGekuerzt()
		);
		assertEquals(new BigDecimal("2131.80"), result.getElternbeitrag());
	}

	@Test
	void testKind6_MassgebendesEinkommenNaheUntergrenze() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		parameter.setGeschwisterbonusTyp(GeschwisterbonusTyp.SCHWYZ_2);
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);

		input.setEinschulungTyp(null);
		input.setBetreuungInFerienzeit(true);
		input
			.setMassgebendesEinkommenVorAbzugFamgr(
				parameter.getMinMassgebendesEinkommen()
					.add(BigDecimal.TEN)
			);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();

		assertEquals(
			MINIMALER_ELTERNBEITRAG,
			result.getMinimalerElternbeitrag()
		);
		assertEquals(
			new BigDecimal("0.00"),
			result.getMinimalerElternbeitragGekuerzt()
		);
		assertEquals(new BigDecimal("295.35"), result.getElternbeitrag());
	}

	@Test
	void testKind7_TagestarifTieferAlsNormkosten() {
		// given
		var testee = new KitaTagestrukturenSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		parameter.setGeschwisterbonusTyp(GeschwisterbonusTyp.SCHWYZ_2);
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setEinschulungTyp(null);
		input.setBetreuungInFerienzeit(true);
		input.setMonatlicheBetreuungskosten(new BigDecimal(2000));

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();

		assertEquals(
			MINIMALER_ELTERNBEITRAG,
			result.getMinimalerElternbeitrag()
		);
		assertEquals(
			new BigDecimal("0.00"),
			result.getMinimalerElternbeitragGekuerzt()
		);
		assertEquals(new BigDecimal("322.55"), result.getElternbeitrag());
	}

	private void setGueltigkeitGanzerApril(
		VerfuegungZeitabschnitt verfuegungZeitabschnitt
	) {
		verfuegungZeitabschnitt.setGueltigkeit(
			new DateRange(
				LocalDate.of(2024, Month.APRIL, 1),
				LocalDate.of(2024, Month.APRIL, 30)
			)
		);
	}

	private void setDefaultInputs(BGCalculationInput input) {
		input.setBabyTarif(false);
		input.setEinschulungTyp(EinschulungTyp.KLASSE1);
		input.setBetreuungInFerienzeit(false);
		input.setAnzahlGeschwister(4);
		input.setBetreuungspensumProzent(new BigDecimal(80));
		input.setAnspruchspensumProzent(100);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(50_000));
		input.setAbzugFamGroesse(BigDecimal.ZERO);
		input.setMonatlicheBetreuungskosten(new BigDecimal(20 * 200));
	}

}
