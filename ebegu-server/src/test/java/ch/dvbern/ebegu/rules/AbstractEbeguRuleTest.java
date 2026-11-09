/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnittBemerkung;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests für die Hilfsmethoden auf AbstractEbeguRule
 */
public class AbstractEbeguRuleTest {

	private final DateRange defaultGueltigkeit = new DateRange(
		Constants.START_OF_TIME,
		Constants.END_OF_TIME
	);
	private final ErwerbspensumAsivAbschnittRule erwerbspensumRule =
		new ErwerbspensumAsivAbschnittRule(
			defaultGueltigkeit,
			20,
			Constants.DEFAULT_LOCALE
		);

	private static final LocalDate DATUM_1 = LocalDate.of(
		TestDataUtil.PERIODE_JAHR_1,
		Month.APRIL,
		1
	);
	private static final LocalDate DATUM_2 = LocalDate.of(
		TestDataUtil.PERIODE_JAHR_1,
		Month.SEPTEMBER,
		1
	);
	private static final LocalDate DATUM_3 = LocalDate.of(
		TestDataUtil.PERIODE_JAHR_1,
		Month.OCTOBER,
		1
	);
	private static final LocalDate DATUM_4 = LocalDate.of(
		TestDataUtil.PERIODE_JAHR_1,
		Month.DECEMBER,
		1
	);

	@Test
	public void testErwerbspensenUndBetreuungspensen() {

		List<VerfuegungZeitabschnitt> betreuungspensen = new ArrayList<>();
		betreuungspensen.add(
			createBetreuungspensum(
				Constants.START_OF_TIME,
				Constants.END_OF_TIME,
				BigDecimal.valueOf(50)
			)
		);
		betreuungspensen.add(
			createBetreuungspensum(DATUM_2, DATUM_4, BigDecimal.valueOf(20))
		);
		betreuungspensen = erwerbspensumRule.mergeZeitabschnitte(
			betreuungspensen
		);
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : betreuungspensen) {
			verfuegungZeitabschnitt.initBGCalculationResult();
		}
		// 01.01.1900 - DATUM2-1: 50, DATUM2 - DATUM4: 70, DATUM4+1 - 31.12.9999: 50

		List<VerfuegungZeitabschnitt> erwerbspensen = new ArrayList<>();
		erwerbspensen.add(createErwerbspensum(DATUM_1, DATUM_3, 40));
		erwerbspensen.add(createErwerbspensum(DATUM_2, DATUM_4, 60));
		erwerbspensen = erwerbspensumRule.mergeZeitabschnitte(erwerbspensen);
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : erwerbspensen) {
			verfuegungZeitabschnitt.initBGCalculationResult();
		}
		// DATUM1 - DATUM2-1: 40, DATUM2 - DATUM3: 100, DATUM3+1 - DATUM 4: 60

		List<VerfuegungZeitabschnitt> alles = new ArrayList<>();
		alles.addAll(betreuungspensen);
		alles.addAll(erwerbspensen);
		List<VerfuegungZeitabschnitt> result = erwerbspensumRule
			.mergeZeitabschnitte(alles);
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : result) {
			verfuegungZeitabschnitt.initBGCalculationResult();
		}
		// 01.01.1900 - DATUM1-1, DATUM1 - DATUM2-1, DATUM2 - DATUM3, DATUM3+1 - DATUM 4,  DATUM4+1 - 31.12.9999

		assertNotNull(result);
		assertEquals(5, result.size());
		VerfuegungZeitabschnitt first = result.get(0);
		VerfuegungZeitabschnitt second = result.get(1);
		VerfuegungZeitabschnitt third = result.get(2);
		VerfuegungZeitabschnitt fourth = result.get(3);
		VerfuegungZeitabschnitt fifth = result.get(4);

		assertEquals(
			Constants.START_OF_TIME,
			first.getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_1.minusDays(1),
			first.getGueltigkeit().getGueltigBis()
		);

		assertEquals(
			DATUM_1,
			second.getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_2.minusDays(1),
			second.getGueltigkeit().getGueltigBis()
		);

		assertEquals(DATUM_2, third.getGueltigkeit().getGueltigAb());
		assertEquals(
			DATUM_3,
			third.getGueltigkeit().getGueltigBis()
		);

		assertEquals(
			DATUM_3.plusDays(1),
			fourth.getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_4,
			fourth.getGueltigkeit().getGueltigBis()
		);

		assertEquals(
			DATUM_4.plusDays(1),
			fifth.getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			Constants.END_OF_TIME,
			fifth.getGueltigkeit().getGueltigBis()
		);

		assertNull(
			first.getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(40),
			second.getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(100),
			third.getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(60),
			fourth.getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertNull(
			fifth.getBgCalculationInputAsiv().getErwerbspensumGS1()
		);

		assertEquals(
			50,
			first.getBetreuungspensumProzent().intValue()
		);
		assertEquals(
			50,
			second.getBetreuungspensumProzent().intValue()
		);
		assertEquals(
			70,
			third.getBetreuungspensumProzent().intValue()
		);
		assertEquals(
			70,
			fourth.getBetreuungspensumProzent().intValue()
		);
		assertEquals(
			50,
			fifth.getBetreuungspensumProzent().intValue()
		);
	}

	@Test
	public void testNurEinZeitraum() {
		List<VerfuegungZeitabschnitt> zeitabschnitte = new ArrayList<>();
		zeitabschnitte.add(createErwerbspensum(DATUM_1, DATUM_3, 40));
		List<VerfuegungZeitabschnitt> result = erwerbspensumRule
			.mergeZeitabschnitte(zeitabschnitte);

		assertNotNull(result);
		assertEquals(1, result.size());
		VerfuegungZeitabschnitt next = result.iterator().next();
		assertEquals(DATUM_1, next.getGueltigkeit().getGueltigAb());
		assertEquals(DATUM_3, next.getGueltigkeit().getGueltigBis());
		assertEquals(
			Integer.valueOf(40),
			next.getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
	}

	@Test
	public void testUeberschneidung() {
		List<VerfuegungZeitabschnitt> zeitabschnitte = new ArrayList<>();
		zeitabschnitte.add(createErwerbspensum(DATUM_1, DATUM_3, 40));
		zeitabschnitte.add(createErwerbspensum(DATUM_2, DATUM_4, 60));
		List<VerfuegungZeitabschnitt> result = erwerbspensumRule
			.mergeZeitabschnitte(zeitabschnitte);

		assertNotNull(result);
		assertEquals(3, result.size());
		VerfuegungZeitabschnitt first = result.get(0);
		VerfuegungZeitabschnitt second = result.get(1);
		VerfuegungZeitabschnitt third = result.get(2);
		assertEquals(DATUM_1, first.getGueltigkeit().getGueltigAb());
		assertEquals(
			DATUM_2.minusDays(1),
			first.getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			DATUM_2,
			second.getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_3,
			second.getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			DATUM_3.plusDays(1),
			third.getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_4,
			third.getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			Integer.valueOf(40),
			first.getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(100),
			second.getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(60),
			third.getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
	}

	@Test
	public void testSchnittmenge() {
		List<VerfuegungZeitabschnitt> zeitabschnitte = new ArrayList<>();
		zeitabschnitte.add(
			createBetreuungspensum(
				Constants.START_OF_TIME,
				Constants.END_OF_TIME,
				BigDecimal.valueOf(50)
			)
		);
		zeitabschnitte.add(
			createBetreuungspensum(DATUM_2, DATUM_4, BigDecimal.valueOf(20))
		);
		List<VerfuegungZeitabschnitt> result = erwerbspensumRule
			.mergeZeitabschnitte(zeitabschnitte);
		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : result) {
			verfuegungZeitabschnitt.initBGCalculationResult();
		}

		assertNotNull(result);
		assertEquals(3, result.size());
		VerfuegungZeitabschnitt first = result.get(0);
		VerfuegungZeitabschnitt second = result.get(1);
		VerfuegungZeitabschnitt third = result.get(2);
		assertEquals(
			Constants.START_OF_TIME,
			first.getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_2.minusDays(1),
			first.getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			DATUM_2,
			second.getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_4,
			second.getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			DATUM_4.plusDays(1),
			third.getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			Constants.END_OF_TIME,
			third.getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			50,
			first.getBetreuungspensumProzent().intValue()
		);
		assertEquals(
			70,
			second.getBetreuungspensumProzent().intValue()
		);
		assertEquals(
			50,
			third.getBetreuungspensumProzent().intValue()
		);
	}

	@Test
	public void testGleicherStart() {
		List<VerfuegungZeitabschnitt> zeitabschnitte = new ArrayList<>();
		zeitabschnitte.add(createErwerbspensum(DATUM_1, DATUM_4, 80));
		zeitabschnitte.add(createErwerbspensum(DATUM_1, DATUM_2, 40));
		List<VerfuegungZeitabschnitt> result = erwerbspensumRule
			.mergeZeitabschnitte(zeitabschnitte);

		// Es sollte neu zwei geben
		assertNotNull(result);
		assertEquals(2, result.size());
		VerfuegungZeitabschnitt first = result.get(0);
		VerfuegungZeitabschnitt second = result.get(1);

		assertEquals(DATUM_1, first.getGueltigkeit().getGueltigAb());
		assertEquals(
			DATUM_2,
			first.getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			DATUM_2.plusDays(1),
			second.getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_4,
			second.getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			Integer.valueOf(120),
			first.getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(80),
			second.getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
	}

	@Test
	public void testGleichesEnde() {
		List<VerfuegungZeitabschnitt> zeitabschnitte = new ArrayList<>();
		zeitabschnitte.add(createErwerbspensum(DATUM_1, DATUM_4, 80));
		zeitabschnitte.add(createErwerbspensum(DATUM_2, DATUM_4, 40));
		List<VerfuegungZeitabschnitt> result = erwerbspensumRule
			.mergeZeitabschnitte(zeitabschnitte);

		// Es sollte neu zwei geben
		assertNotNull(result);
		assertEquals(2, result.size());
		VerfuegungZeitabschnitt first = result.get(0);
		VerfuegungZeitabschnitt second = result.get(1);

		assertEquals(DATUM_1, first.getGueltigkeit().getGueltigAb());
		assertEquals(
			DATUM_2.minusDays(1),
			first.getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			DATUM_2,
			second.getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_4,
			second.getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			Integer.valueOf(80),
			first.getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(120),
			second.getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
	}

	@Test
	public void testGleicherStartUndEnde() {
		List<VerfuegungZeitabschnitt> zeitabschnitte = new ArrayList<>();
		zeitabschnitte.add(createErwerbspensum(DATUM_1, DATUM_4, 80));
		zeitabschnitte.add(createErwerbspensum(DATUM_1, DATUM_4, 40));
		List<VerfuegungZeitabschnitt> result = erwerbspensumRule
			.mergeZeitabschnitte(zeitabschnitte);

		// Es sollte neu zwei geben
		assertNotNull(result);
		assertEquals(1, result.size());
		VerfuegungZeitabschnitt first = result.get(0);

		assertEquals(DATUM_1, first.getGueltigkeit().getGueltigAb());
		assertEquals(
			DATUM_4,
			first.getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			Integer.valueOf(120),
			first.getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
	}

	@Test
	public void testBegrenzungAufGesuchsperiode() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			true
		);
		Gesuch gesuch = betreuung.extractGesuch();

		List<VerfuegungZeitabschnitt> zeitabschnitte = new ArrayList<>();
		zeitabschnitte.add(
			createErwerbspensum(
				Constants.START_OF_TIME,
				Constants.END_OF_TIME,
				80
			)
		);
		List<VerfuegungZeitabschnitt> result = erwerbspensumRule.calculate(
			betreuung,
			zeitabschnitte
		);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(
			gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
			result.get(0).getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis(),
			result.get(0).getGueltigkeit().getGueltigBis()
		);
	}

	@Test
	public void testZusammenlegenVonIdentischenPerioden() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			true
		);
		// 2*20%, direkt gefolgt von 1*40% sollte 1 Abschnitt mit 40% geben
		List<VerfuegungZeitabschnitt> zeitabschnitte = new ArrayList<>();
		zeitabschnitte.add(createErwerbspensum(DATUM_2, DATUM_3, 20));
		zeitabschnitte.add(createErwerbspensum(DATUM_2, DATUM_3, 20));
		zeitabschnitte.add(
			createErwerbspensum(DATUM_3.plusDays(1), DATUM_4, 40)
		);
		List<VerfuegungZeitabschnitt> result = erwerbspensumRule.calculate(
			betreuung,
			zeitabschnitte
		);

		assertNotNull(result);
		// Es sind 3 Abschnitte weil es fuer die ganze Periode Zeitabschintte macht, auch fuer die Zeit in der keine
		// Erwerbspensen eingegeben wurden.
		assertEquals(3, result.size());

		assertEquals(
			betreuung.extractGesuch()
				.getGesuchsperiode()
				.getGueltigkeit()
				.getGueltigAb(),
			result.get(0).getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_2.minusDays(1),
			result.get(0).getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			Integer.valueOf(0),
			result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(0),
			result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS2()
		);

		assertEquals(
			DATUM_2,
			result.get(1).getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_4,
			result.get(1).getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			Integer.valueOf(40),
			result.get(1).getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(0),
			result.get(1).getBgCalculationInputAsiv().getErwerbspensumGS2()
		);

		assertEquals(
			DATUM_4.plusDays(1),
			result.get(2).getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			betreuung.extractGesuch()
				.getGesuchsperiode()
				.getGueltigkeit()
				.getGueltigBis(),
			result.get(2).getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			Integer.valueOf(0),
			result.get(2).getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(0),
			result.get(2).getBgCalculationInputAsiv().getErwerbspensumGS2()
		);
	}

	@Test
	public void testNichtZusammenlegenVonIdentischenPeriodenMitAbstand() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			true
		);
		// 2*20%, direkt gefolgt von 1*40% sollte 1 Abschnitt mit 40% geben
		List<VerfuegungZeitabschnitt> zeitabschnitte = new ArrayList<>();
		zeitabschnitte.add(createErwerbspensum(DATUM_2, DATUM_3, 20));
		zeitabschnitte.add(createErwerbspensum(DATUM_2, DATUM_3, 20));
		zeitabschnitte.add(
			createErwerbspensum(DATUM_3.plusDays(2), DATUM_4, 40)
		);
		List<VerfuegungZeitabschnitt> result = erwerbspensumRule.calculate(
			betreuung,
			zeitabschnitte
		);

		assertNotNull(result);
		// Es sind 5 Abschnitte: 1. kein EP, 2. 20+20, 3. wieder kein EP (02.10.), 4. 40 und 5. kein EP
		// Fuer Zeitabschnitte in denen es kein EP eingegeben wurde, wird auch ein Zeitabschnitte berechnet
		assertEquals(5, result.size());

		assertEquals(
			betreuung.extractGesuch()
				.getGesuchsperiode()
				.getGueltigkeit()
				.getGueltigAb(),
			result.get(0).getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_2.minusDays(1),
			result.get(0).getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			Integer.valueOf(0),
			result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(0),
			result.get(0).getBgCalculationInputAsiv().getErwerbspensumGS2()
		);

		assertEquals(
			DATUM_2,
			result.get(1).getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_3,
			result.get(1).getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			Integer.valueOf(40),
			result.get(1).getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(0),
			result.get(1).getBgCalculationInputAsiv().getErwerbspensumGS2()
		);

		assertEquals(
			DATUM_3.plusDays(1),
			result.get(2).getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_3.plusDays(1),
			result.get(2).getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			Integer.valueOf(0),
			result.get(2).getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(0),
			result.get(2).getBgCalculationInputAsiv().getErwerbspensumGS2()
		);

		assertEquals(
			DATUM_3.plusDays(2),
			result.get(3).getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			DATUM_4,
			result.get(3).getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			Integer.valueOf(40),
			result.get(3).getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(0),
			result.get(3).getBgCalculationInputAsiv().getErwerbspensumGS2()
		);

		assertEquals(
			DATUM_4.plusDays(1),
			result.get(4).getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			betreuung.extractGesuch()
				.getGesuchsperiode()
				.getGueltigkeit()
				.getGueltigBis(),
			result.get(4).getGueltigkeit().getGueltigBis()
		);
		assertEquals(
			Integer.valueOf(0),
			result.get(4).getBgCalculationInputAsiv().getErwerbspensumGS1()
		);
		assertEquals(
			Integer.valueOf(0),
			result.get(4).getBgCalculationInputAsiv().getErwerbspensumGS2()
		);
	}

	private VerfuegungZeitabschnitt createErwerbspensum(
		LocalDate von,
		LocalDate bis,
		int pensum
	) {
		VerfuegungZeitabschnitt zeitabschnitt1 = new VerfuegungZeitabschnitt(
			new DateRange(von, bis)
		);
		zeitabschnitt1.getBgCalculationInputAsiv().setErwerbspensumGS1(pensum);
		zeitabschnitt1.getBgCalculationInputAsiv().setErwerbspensumGS2(0);
		return zeitabschnitt1;
	}

	private VerfuegungZeitabschnitt createBetreuungspensum(
		LocalDate von,
		LocalDate bis,
		BigDecimal pensum
	) {
		VerfuegungZeitabschnitt zeitabschnitt1 = new VerfuegungZeitabschnitt(
			new DateRange(von, bis)
		);
		zeitabschnitt1.getBgCalculationInputAsiv()
			.setBetreuungspensumProzent(pensum);
		return zeitabschnitt1;
	}

	@SuppressWarnings("SameParameterValue")
	protected void assertZeitabschnitt(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		int expectedBetreuungspensum,
		int expectedAnspruchsPensum,
		int expectedBgPensum,
		@Nullable MsgKey expectedBemerkungIfAny
	) {

		assertThat(
			zeitabschnitt,
			pojo(VerfuegungZeitabschnitt.class)
				.where(
					VerfuegungZeitabschnitt::getBetreuungspensumProzent,
					comparesEqualTo(
						BigDecimal.valueOf(
							expectedBetreuungspensum
						)
					)
				)
				.where(
					VerfuegungZeitabschnitt::getAnspruchberechtigtesPensum,
					comparesEqualTo(expectedAnspruchsPensum)
				)
				.where(
					VerfuegungZeitabschnitt::getBgPensum,
					comparesEqualTo(
						BigDecimal.valueOf(expectedBgPensum)
					)
				)
		);

		final List<VerfuegungZeitabschnittBemerkung> bemerkungen = zeitabschnitt
			.getVerfuegungZeitabschnittBemerkungList();
		if (expectedBemerkungIfAny != null) {
			assertFalse(
				zeitabschnitt.getBemerkungenDTOList().isEmpty()
			);
			assertTrue(
				zeitabschnitt.getBemerkungenDTOList()
					.containsMsgKey(expectedBemerkungIfAny)
			);
		} else {
			assertNotNull(bemerkungen);
			assertFalse(
				zeitabschnitt.getBemerkungenDTOList().isEmpty()
			);
			assertEquals(
				2,
				zeitabschnitt.getBemerkungenDTOList().uniqueSize()
			);
			assertTrue(
				zeitabschnitt.getBemerkungenDTOList()
					.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH)
			);
			assertTrue(
				zeitabschnitt.getBemerkungenDTOList()
					.containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH)
			);
		}
	}
}
