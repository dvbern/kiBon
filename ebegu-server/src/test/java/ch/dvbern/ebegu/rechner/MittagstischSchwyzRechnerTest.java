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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.TestUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MittagstischSchwyzRechnerTest {

	@Test
	void testKind1() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setTarifHauptmahlzeit(BigDecimal.valueOf(15));
		input.setAnzahlGeschwister(4);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(10_000));
		input.setAnspruchspensumProzent(80);
		input.setBetreuungspensumProzent(BigDecimal.valueOf(80));
		input.setAbzugFamGroesse(BigDecimal.ZERO);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(
			new BigDecimal("54.67"),
			result.getAnspruchspensumZeiteinheit()
		);
		assertEquals(new BigDecimal("54.67"), result.getBgPensumZeiteinheit());
		assertEquals(
			new BigDecimal("54.67"),
			result.getBetreuungspensumZeiteinheit()
		);

		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("814.45");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten()
		);
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag()
		);
		assertEquals(new BigDecimal("410.00"), result.getVerguenstigung());
		assertEquals(
			new BigDecimal("410.00"),
			result.getMinimalerElternbeitrag()
		);
		assertEquals(
			new BigDecimal("404.45"),
			result.getMinimalerElternbeitragGekuerzt()
		);
		assertEquals(new BigDecimal("5.55"), result.getElternbeitrag());
	}

	@Test
	void testKind1HalberMonat() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(
			new DateRange(
				LocalDate.of(2024, Month.APRIL, 16),
				LocalDate.of(2024, Month.APRIL, 30)
			)
		);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setTarifHauptmahlzeit(BigDecimal.valueOf(15));
		input.setAnzahlGeschwister(4);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(10_000));
		input.setAnspruchspensumProzent(80);
		input.setBetreuungspensumProzent(BigDecimal.valueOf(80));
		input.setAbzugFamGroesse(BigDecimal.ZERO);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(
			new BigDecimal("27.33"),
			result.getAnspruchspensumZeiteinheit()
		);
		assertEquals(new BigDecimal("27.33"), result.getBgPensumZeiteinheit());
		assertEquals(new BigDecimal("27.33"), result.getBgPensumZeiteinheit());
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("407.25");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten()
		);
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag()
		);
		assertEquals(new BigDecimal("205.00"), result.getVerguenstigung());
		assertEquals(
			new BigDecimal("205.00"),
			result.getMinimalerElternbeitrag()
		);
		assertEquals(
			new BigDecimal("202.25"),
			result.getMinimalerElternbeitragGekuerzt()
		);
		assertEquals(new BigDecimal("2.75"), result.getElternbeitrag());
	}

	@Test
	void testKind2() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnzahlGeschwister(1);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal("53300"));
		input.setAnspruchspensumProzent(50);
		input.setBetreuungspensumProzent(BigDecimal.valueOf(50));
		input.setTarifHauptmahlzeit(BigDecimal.valueOf(12));
		input.setAbzugFamGroesse(BigDecimal.ZERO);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(
			new BigDecimal("34.17"),
			result.getAnspruchspensumZeiteinheit()
		);
		assertEquals(new BigDecimal("34.17"), result.getBgPensumZeiteinheit());
		assertEquals(
			new BigDecimal("34.17"),
			result.getBetreuungspensumZeiteinheit()
		);

		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("232.95");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten()
		);
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag()
		);
		assertEquals(new BigDecimal("153.75"), result.getVerguenstigung());
		assertEquals(
			new BigDecimal("256.25"),
			result.getMinimalerElternbeitrag()
		);
		assertEquals(
			new BigDecimal("79.20"),
			result.getMinimalerElternbeitragGekuerzt()
		);
		assertEquals(new BigDecimal("177.05"), result.getElternbeitrag());
	}

	@Test
	void testKind3() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(
			new BigDecimal("68.33"),
			result.getAnspruchspensumZeiteinheit()
		);
		assertEquals(new BigDecimal("68.33"), result.getBgPensumZeiteinheit());
		assertEquals(
			new BigDecimal("68.33"),
			result.getBetreuungspensumZeiteinheit()
		);

		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("401.85");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten()
		);
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag()
		);
		assertEquals(new BigDecimal("273.35"), result.getVerguenstigung());
		assertEquals(
			new BigDecimal("512.50"),
			result.getMinimalerElternbeitrag()
		);
		assertEquals(
			new BigDecimal("128.50"),
			result.getMinimalerElternbeitragGekuerzt()
		);
		assertEquals(new BigDecimal("384.00"), result.getElternbeitrag());
	}

	@Test
	void testKind4() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnspruchspensumProzent(40);
		input.setBetreuungspensumProzent(BigDecimal.valueOf(40));
		input.setTarifHauptmahlzeit(BigDecimal.valueOf(15));
		input.setAnzahlGeschwister(2);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(136_600));
		input.setAbzugFamGroesse(BigDecimal.ZERO);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(
			new BigDecimal("27.33"),
			result.getAnspruchspensumZeiteinheit()
		);
		assertEquals(new BigDecimal("27.33"), result.getBgPensumZeiteinheit());
		assertEquals(
			new BigDecimal("27.33"),
			result.getBetreuungspensumZeiteinheit()
		);

		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("41.55");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten()
		);
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag()
		);
		assertEquals(new BigDecimal("41.55"), result.getVerguenstigung());
		assertEquals(
			new BigDecimal("205.00"),
			result.getMinimalerElternbeitrag()
		);
		assertEquals(
			new BigDecimal("0.00"),
			result.getMinimalerElternbeitragGekuerzt()
		);
		assertEquals(new BigDecimal("368.45"), result.getElternbeitrag());
	}

	@Test
	void testKind5_MassgebendesEinkommenNaheObergrenze() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		input.setAnspruchspensumProzent(60);
		input.setBetreuungspensumProzent(BigDecimal.valueOf(60));
		input.setTarifHauptmahlzeit(BigDecimal.valueOf(18.5));
		input.setAnzahlGeschwister(3);
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(59_900));
		input.setAbzugFamGroesse(BigDecimal.ZERO);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		checkMappedInputs(input, result);
		assertEquals(
			new BigDecimal("41.00"),
			result.getAnspruchspensumZeiteinheit()
		);
		assertEquals(new BigDecimal("41.00"), result.getBgPensumZeiteinheit());
		assertEquals(
			new BigDecimal("41.00"),
			result.getBetreuungspensumZeiteinheit()
		);

		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("424.00");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten()
		);
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag()
		);
		assertEquals(new BigDecimal("389.50"), result.getVerguenstigung());
		assertEquals(
			new BigDecimal("307.50"),
			result.getMinimalerElternbeitrag()
		);
		assertEquals(
			new BigDecimal("34.50"),
			result.getMinimalerElternbeitragGekuerzt()
		);
		assertEquals(new BigDecimal("273.00"), result.getElternbeitrag());
	}

	@ParameterizedTest
	@EnumSource(value = Bedarfsstufe.class,
		names = { "KEINE" },
		mode = Mode.EXCLUDE)
	void testHoereBeitragBedarfsstufe_NurGutscheinErhoert(
		Bedarfsstufe bedarfsstufe
	) {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(bedarfsstufe);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		var gutscheinVorAbzugSelbstbehalt = new BigDecimal("401.85");
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungVollkosten()
		);
		assertEquals(
			gutscheinVorAbzugSelbstbehalt,
			result.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag()
		);
		assertEquals(
			new BigDecimal("512.50"),
			result.getMinimalerElternbeitrag()
		);
		assertEquals(
			new BigDecimal("128.50"),
			result.getMinimalerElternbeitragGekuerzt()
		);
		assertEquals(new BigDecimal("384.00"), result.getElternbeitrag());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_1() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_1);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then 273.35 + 352
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("625.35"), result.getVerguenstigung());
		assertEquals(new BigDecimal("352.00"), result.getHoehererBeitrag());
		assertEquals(Bedarfsstufe.BEDARFSSTUFE_1, result.getBedarfsstufe());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_1_untermonatlich() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(
			new DateRange(
				LocalDate.of(2024, Month.APRIL, 1),
				LocalDate.of(2024, Month.APRIL, 15)
			)
		);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_1);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then (273.35 * 0.5) + (352 * 0.5)
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("312.65"), result.getVerguenstigung());
		assertEquals(new BigDecimal("176.00"), result.getHoehererBeitrag());
		assertEquals(Bedarfsstufe.BEDARFSSTUFE_1, result.getBedarfsstufe());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_2() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_2);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then 273.35 + 352 + (68.3333333 * 9.90)
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("1301.85"), result.getVerguenstigung());
		assertEquals(new BigDecimal("1028.50"), result.getHoehererBeitrag());
		assertEquals(Bedarfsstufe.BEDARFSSTUFE_2, result.getBedarfsstufe());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_2_untermonatlich() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(
			new DateRange(
				LocalDate.of(2024, Month.APRIL, 1),
				LocalDate.of(2024, Month.APRIL, 15)
			)
		);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_2);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then (273.35 * 0.5) + (352 * 0.5) + (34.166666 * 9.9)
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("650.90"), result.getVerguenstigung());
		assertEquals(new BigDecimal("514.25"), result.getHoehererBeitrag());
		assertEquals(Bedarfsstufe.BEDARFSSTUFE_2, result.getBedarfsstufe());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_3() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		setGueltigkeitGanzerApril(verfuegungZeitabschnitt);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_3);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then 273.35 + 352 + (68.3333333 * 19.80)
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("1978.35"), result.getVerguenstigung());
		assertEquals(new BigDecimal("1705.00"), result.getHoehererBeitrag());
		assertEquals(Bedarfsstufe.BEDARFSSTUFE_3, result.getBedarfsstufe());
	}

	@Test
	void testHoereBeitragBedarfsstufe_BEDARFSSTUFE_3_untermonatlich() {
		// given
		var testee = new MittagstischSchwyzRechner();
		var parameter = TestUtils.getRechnerParamterSchwyz();
		var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.setGueltigkeit(
			new DateRange(
				LocalDate.of(2024, Month.APRIL, 1),
				LocalDate.of(2024, Month.APRIL, 15)
			)
		);

		var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
		setDefaultInputs(input);
		input.setBedarfsstufe(Bedarfsstufe.BEDARFSSTUFE_3);

		// when
		testee.calculate(verfuegungZeitabschnitt, parameter);

		// then (273.35 * 0.5) + (352 * 0.5) + (34.166666 * 19.80)
		var result = verfuegungZeitabschnitt.getRelevantBgCalculationResult();
		assertEquals(new BigDecimal("989.15"), result.getVerguenstigung());
		assertEquals(new BigDecimal("852.50"), result.getHoehererBeitrag());
		assertEquals(Bedarfsstufe.BEDARFSSTUFE_3, result.getBedarfsstufe());
	}

	@Nested
	class VollkostenKuerzungTest {

		@ParameterizedTest()
		@ValueSource(ints = { 81, 90, 100 })
		void anspruchHigherThanBetreuung_ShouldNotHaveVollkostenGekuerzt(
			int anspruch
		) {
			final BigDecimal monatlicheBetreuungskosten = new BigDecimal(2000)
				.setScale(2, RoundingMode.UNNECESSARY);
			var testee = new KitaTagestrukturenSchwyzRechner();
			var parameter = TestUtils.getRechnerParamterSchwyz();
			var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
			verfuegungZeitabschnitt.setGueltigkeit(
				new DateRange(
					LocalDate.of(2024, Month.AUGUST, 1),
					LocalDate.of(2024, Month.AUGUST, 31)
				)
			);
			var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
			setDefaultInputs(input);
			input.setMonatlicheBetreuungskosten(monatlicheBetreuungskosten);
			input.setAnspruchspensumProzent(anspruch);
			input.setBetreuungspensumProzent(new BigDecimal(80));

			// when
			testee.calculate(verfuegungZeitabschnitt, parameter);
			final BigDecimal vollkosten = verfuegungZeitabschnitt
				.getRelevantBgCalculationResult()
				.getVollkosten();
			assertEquals(
				monatlicheBetreuungskosten,
				vollkosten
			);
		}

		@Test
		void anspruchEqualToBetreuung_ShouldNotHaveVollkostenGekuerzt() {
			final BigDecimal monatlicheBetreuungskosten = new BigDecimal(2000)
				.setScale(2, RoundingMode.UNNECESSARY);
			var testee = new KitaTagestrukturenSchwyzRechner();
			var parameter = TestUtils.getRechnerParamterSchwyz();
			var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
			verfuegungZeitabschnitt.setGueltigkeit(
				new DateRange(
					LocalDate.of(2024, Month.AUGUST, 1),
					LocalDate.of(2024, Month.AUGUST, 31)
				)
			);
			var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
			setDefaultInputs(input);
			input.setMonatlicheBetreuungskosten(monatlicheBetreuungskosten);
			input.setAnspruchspensumProzent(80);
			input.setBetreuungspensumProzent(new BigDecimal(80));

			// when
			testee.calculate(verfuegungZeitabschnitt, parameter);
			final BigDecimal vollkosten = verfuegungZeitabschnitt
				.getRelevantBgCalculationResult()
				.getVollkosten();
			assertEquals(
				monatlicheBetreuungskosten,
				vollkosten
			);
		}

		@ParameterizedTest()
		@CsvSource({
			"60, 80",
			"40, 80",
			"75, 80",
		})
		void anspruchLowerThanBetreuung_ShouldHaveVollkostenGekuerztProportionally(
			int anspruchsPensum,
			int betreuungsPensum
		) {
			final BigDecimal monatlicheBetreuungskosten = new BigDecimal(2000)
				.setScale(2, RoundingMode.UNNECESSARY);
			var testee = new KitaTagestrukturenSchwyzRechner();
			var parameter = TestUtils.getRechnerParamterSchwyz();
			var verfuegungZeitabschnitt = new VerfuegungZeitabschnitt();
			verfuegungZeitabschnitt.setGueltigkeit(
				new DateRange(
					LocalDate.of(2024, Month.AUGUST, 1),
					LocalDate.of(2024, Month.AUGUST, 31)
				)
			);
			var input = verfuegungZeitabschnitt.getRelevantBgCalculationInput();
			setDefaultInputs(input);
			input.setMonatlicheBetreuungskosten(monatlicheBetreuungskosten);
			input.setAnspruchspensumProzent(anspruchsPensum);
			input.setBetreuungspensumProzent(new BigDecimal(betreuungsPensum));
			var factor = MathUtil.EXACT.divide(
				new BigDecimal(anspruchsPensum),
				new BigDecimal(betreuungsPensum)
			);

			// when
			testee.calculate(verfuegungZeitabschnitt, parameter);
			final BigDecimal vollkosten = verfuegungZeitabschnitt
				.getRelevantBgCalculationResult()
				.getVollkosten();
			assertEquals(
				monatlicheBetreuungskosten.multiply(factor)
					.setScale(2, RoundingMode.UNNECESSARY),
				vollkosten
			);
		}
	}

	private void setDefaultInputs(BGCalculationInput input) {
		input.setAnspruchspensumProzent(100);
		input.setBetreuungspensumProzent(BigDecimal.valueOf(100));
		input.setMassgebendesEinkommenVorAbzugFamgr(new BigDecimal(63_300));
		input.setAnzahlGeschwister(1);
		input.setTarifHauptmahlzeit(BigDecimal.valueOf(11.5));
		input.setAbzugFamGroesse(BigDecimal.ZERO);
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

	private void checkMappedInputs(
		BGCalculationInput input,
		BGCalculationResult result
	) {
		assertEquals(
			MathUtil.ZWEI_NACHKOMMASTELLE.from(
				input.getBetreuungspensumProzent()
			),
			result.getBetreuungspensumProzent()
		);
		assertEquals(
			input.getAnspruchspensumProzent(),
			result.getAnspruchspensumProzent()
		);
		assertEquals(
			MathUtil.ZWEI_NACHKOMMASTELLE.from(input.getBgPensumProzent()),
			result.getBgPensumProzent()
		);
	}
}
