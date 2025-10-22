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

package ch.dvbern.ebegu.rules.familienabzug;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.FamilienGroesseCalculationInput;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static ch.dvbern.ebegu.enums.EnumFamilienstatus.KONKUBINAT_KEIN_KIND;
import static ch.dvbern.ebegu.rules.familienabzug.FamilienabzugAbschnittRuleTestUtil.assertEqualsNumberValue;
import static ch.dvbern.ebegu.rules.familienabzug.FamilienabzugAbschnittRuleTestUtil.getDefaultEinstellungMap;
import static ch.dvbern.ebegu.test.TestDataUtil.ENDE_PERIODE;
import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;
import static java.time.temporal.TemporalAdjusters.firstDayOfNextMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests fuer FamilienabzugAbschnittRule und die FamilienCalcRule
 */
class FamilienabzugRuleBernTest {

	private final GesuchstellerAbzugAbschnittRule gsAbschnittRule =
		new GesuchstellerAbzugAbschnittRule(
			Constants.DEFAULT_GUELTIGKEIT,
			Constants.DEFAULT_LOCALE
		);

	private final KinderabzugAbschnittRuleASIV kinderAbschnittRule =
		new KinderabzugAbschnittRuleASIV(
			Constants.DEFAULT_GUELTIGKEIT,
			Constants.DEFAULT_LOCALE
		);

	private final FamilienabzugCalcRuleASIV famabCalcRule =
		new FamilienabzugCalcRuleASIV(
			getEinstellungMapForAsiv(),
			Constants.DEFAULT_GUELTIGKEIT,
			Constants.DEFAULT_LOCALE
		);

	@Test
	void test2PKeinAbzug() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			false
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());
		addDefaultKindToGesuch(gesuch);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(1, zeitabschnitte.size());
		final VerfuegungZeitabschnitt zeitabschnitt = zeitabschnitte.get(0);
		assertEqualsNumberValue(0, zeitabschnitt.getAbzugFamGroesse());
		assertEqualsNumberValue(2, zeitabschnitt.getFamGroesse());
	}

	@Test
	void test3P_Abzug() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			false
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());

		addDefaultKindToGesuch(gesuch);
		addDefaultKindToGesuch(gesuch);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(1, zeitabschnitte.size());
		final VerfuegungZeitabschnitt zeitabschnitt = zeitabschnitte.get(0);
		// 3 * pauschale für 3 Personen (3*3800)
		assertEqualsNumberValue(11400, zeitabschnitt.getAbzugFamGroesse());
		assertEqualsNumberValue(3, zeitabschnitt.getFamGroesse());
	}

	@Test
	void test4P_Abzug() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			true
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());
		addDefaultKindToGesuch(gesuch);
		addDefaultKindToGesuch(gesuch);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(1, zeitabschnitte.size());
		final VerfuegungZeitabschnitt zeitabschnitt = zeitabschnitte.get(0);
		// 4 * pauschale für 4 Personen (4*6000)
		assertEqualsNumberValue(24000, zeitabschnitt.getAbzugFamGroesse());
		assertEqualsNumberValue(4, zeitabschnitt.getFamGroesse());
	}

	@Test
	void kindGeburtWaehrendPeriode_shouldCountAfterBirthdateFolgemonat() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			true
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());
		KindContainer kindContainer = addDefaultKindToGesuch(gesuch);
		addDefaultKindToGesuch(gesuch);

		final LocalDate geburtsdatum = LocalDate.of(
			TestDataUtil.PERIODE_JAHR_2,
			1,
			10
		);
		kindContainer.getKindJA().setGeburtsdatum(geburtsdatum);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(2, zeitabschnitte.size());

		// Zeitabschnitt 1 bis Ende-Monat nach Geburtstag
		// 3 * pauschale für 3 Personen (3*3800)
		VerfuegungZeitabschnitt zeitabschnitt1 = zeitabschnitte.get(0);
		Assertions.assertEquals(
			START_PERIODE,
			zeitabschnitt1.getGueltigkeit().getGueltigAb()
		);
		Assertions.assertEquals(
			geburtsdatum.with(TemporalAdjusters.lastDayOfMonth()),
			zeitabschnitt1.getGueltigkeit().getGueltigBis()
		);
		assertEqualsNumberValue(11400, zeitabschnitt1.getAbzugFamGroesse());
		assertEqualsNumberValue(3, zeitabschnitt1.getFamGroesse());

		// Zeitabschnitt 2 ab Monat nach Geburtstag
		// 4 * pauschale für 4 Personen (4*6000)
		VerfuegungZeitabschnitt zeitabschnitt2 = zeitabschnitte.get(1);
		Assertions.assertEquals(
			geburtsdatum.with(TemporalAdjusters.firstDayOfNextMonth()),
			zeitabschnitt2.getGueltigkeit().getGueltigAb()
		);
		Assertions.assertEquals(
			ENDE_PERIODE,
			zeitabschnitt2.getGueltigkeit().getGueltigBis()
		);
		assertEqualsNumberValue(24000, zeitabschnitt2.getAbzugFamGroesse());
		assertEqualsNumberValue(4, zeitabschnitt2.getFamGroesse());
	}

	@Test
	void zwillingeGeborenWaehrendPeriode_shouldCoundAfterBirthFolgemonat() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			true
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());

		addDefaultKindToGesuch(gesuch);
		KindContainer zwiling1 = addDefaultKindToGesuch(gesuch);
		KindContainer zwiling2 = addDefaultKindToGesuch(gesuch);

		final LocalDate geburtsdatum = LocalDate.of(
			TestDataUtil.PERIODE_JAHR_2,
			1,
			10
		);
		zwiling1.getKindJA().setGeburtsdatum(geburtsdatum);
		zwiling2.getKindJA().setGeburtsdatum(geburtsdatum);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(2, zeitabschnitte.size());

		// Zeitabschnitt 1 bis Ende-Monat nach Geburtstag
		// 3 * pauschale für 3 Personen (3*3800)
		VerfuegungZeitabschnitt zeitabschnitt1 = zeitabschnitte.get(0);
		Assertions.assertEquals(
			START_PERIODE,
			zeitabschnitt1.getGueltigkeit().getGueltigAb()
		);
		Assertions.assertEquals(
			geburtsdatum.with(TemporalAdjusters.lastDayOfMonth()),
			zeitabschnitt1.getGueltigkeit().getGueltigBis()
		);
		assertEqualsNumberValue(11400, zeitabschnitt1.getAbzugFamGroesse());
		assertEqualsNumberValue(3, zeitabschnitt1.getFamGroesse());

		// Zeitabschnitt 2 ab Monat nach Geburtstag
		// 5 * pauschale für 5 Personen (5*7000)
		VerfuegungZeitabschnitt zeitabschnitt2 = zeitabschnitte.get(1);
		Assertions.assertEquals(
			geburtsdatum.with(TemporalAdjusters.firstDayOfNextMonth()),
			zeitabschnitt2.getGueltigkeit().getGueltigAb()
		);
		Assertions.assertEquals(
			ENDE_PERIODE,
			zeitabschnitt2.getGueltigkeit().getGueltigBis()
		);
		assertEqualsNumberValue(35000, zeitabschnitt2.getAbzugFamGroesse());
		assertEqualsNumberValue(5, zeitabschnitt2.getFamGroesse());
	}

	@ParameterizedTest
	@CsvSource({
		"ALLEINERZIEHEND, 2, 0, VERHEIRATET, 3, 11400",
		"ALLEINERZIEHEND, 2, 0, KONKUBINAT, 3, 11400",
		"VERHEIRATET, 3, 11400, ALLEINERZIEHEND, 2, 0",
		"KONKUBINAT, 3, 11400, ALLEINERZIEHEND, 2, 0",
	})
	void familiensituationWechsel(
		EnumFamilienstatus familienstatusAktuell,
		int familiengroesseAktuell,
		double familienabzugAktuell,
		EnumFamilienstatus familienstatusErstgesuch,
		int familiengroesseErstgesuch,
		double familienabzugErstgesuch
	) {
		// SETUP
		LocalDate OCTOBER_15 = LocalDate.of(2017, Month.OCTOBER, 15);
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		betreuung.initVorgaengerVerfuegungen(null, null);

		Gesuch gesuch = createGesuchWithFamilienstatusAndKind(
			familienstatusAktuell,
			betreuung.getKind()
		);
		gesuch.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.setAenderungPer(OCTOBER_15);

		Familiensituation erstFamiliensituation = new Familiensituation();
		erstFamiliensituation.setFamilienstatus(familienstatusErstgesuch);
		gesuch.getFamiliensituationContainer()
			.setFamiliensituationErstgesuch(erstFamiliensituation);

		// RUN TEST-CASE
		List<VerfuegungZeitabschnitt> result = calculateAllAbschnittRules(
			betreuung
		);

		// RESULT
		Assertions.assertNotNull(result);
		assertEquals(2, result.size());

		assertEquals(
			START_PERIODE,
			result.get(0).getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			OCTOBER_15.with(lastDayOfMonth()),
			result.get(0).getGueltigkeit().getGueltigBis()
		);
		assertEqualsNumberValue(
			familiengroesseErstgesuch,
			result.get(0).getBgCalculationInputAsiv().getFamGroesse()
		);
		assertEqualsNumberValue(
			familienabzugErstgesuch,
			result.get(0).getBgCalculationInputAsiv().getAbzugFamGroesse()
		);

		assertEquals(
			OCTOBER_15.with(firstDayOfNextMonth()),
			result.get(1).getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			ENDE_PERIODE,
			result.get(1).getGueltigkeit().getGueltigBis()
		);
		assertEqualsNumberValue(
			familiengroesseAktuell,
			result.get(1).getBgCalculationInputAsiv().getFamGroesse()
		);
		assertEqualsNumberValue(
			familienabzugAktuell,
			result.get(1).getBgCalculationInputAsiv().getAbzugFamGroesse()
		);
	}

	@Test
	void konkubinatOhneKindMinDauerNichtErreicht_shouldCountOneGesuchstelle() {
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = createGesuchWithFamilienstatusAndKind(
			KONKUBINAT_KEIN_KIND,
			betreuung.getKind()
		);
		// Konkubinat Min Dauer = 5 Jahre
		// Letztes Datum, damit Min Dauer in der ganzen Periode nicht erreicht ist = 01.08.2013
		LocalDate startKonkubinat = LocalDate.of(2013, Month.AUGUST, 1);
		gesuch.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.setStartKonkubinat(startKonkubinat);

		List<VerfuegungZeitabschnitt> result = calculateAllAbschnittRules(
			betreuung
		);

		Assertions.assertNotNull(result);
		assertEquals(1, result.size());

		assertEqualsNumberValue(
			2,
			result.get(0).getBgCalculationInputAsiv().getFamGroesse()
		);
		assertEqualsNumberValue(
			0,
			result.get(0).getBgCalculationInputAsiv().getAbzugFamGroesse()
		);
	}

	@Test
	void konkubinatOhneKindMinDauerErreicht_shouldCountTwoGesuchsteller() {
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = createGesuchWithFamilienstatusAndKind(
			KONKUBINAT_KEIN_KIND,
			betreuung.getKind()
		);
		// Konkubinat Min Dauer = 5 Jahre
		// Erstes Datum, damit Min Dauer in der ganzen Periode erreicht ist = 31.07.2012
		LocalDate startKonkubinat = LocalDate.of(2012, Month.JULY, 31);
		gesuch.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.setStartKonkubinat(startKonkubinat);

		List<VerfuegungZeitabschnitt> result = calculateAllAbschnittRules(
			betreuung
		);

		Assertions.assertNotNull(result);
		assertEquals(1, result.size());

		assertEqualsNumberValue(
			3,
			result.get(0).getBgCalculationInputAsiv().getFamGroesse()
		);
		assertEqualsNumberValue(
			11400,
			result.get(0).getBgCalculationInputAsiv().getAbzugFamGroesse()
		); //3 x 3'800
	}

	@Test
	void konkubinatOhneKindReachesMinDauerInPeriode_shouldCountOneGSTillMinDauerErreicht() {
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = createGesuchWithFamilienstatusAndKind(
			KONKUBINAT_KEIN_KIND,
			betreuung.getKind()
		);
		// Konkubinat Min Dauer = 5 Jahre
		// Erstes Datum, damit Min Dauer in der ganzen Periode erreicht ist = 31.10.2012
		LocalDate startKonkubinat = LocalDate.of(2012, Month.DECEMBER, 31);
		gesuch.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.setStartKonkubinat(startKonkubinat);

		List<VerfuegungZeitabschnitt> result = calculateAllAbschnittRules(
			betreuung
		);

		Assertions.assertNotNull(result);
		assertEquals(2, result.size());

		assertEquals(
			START_PERIODE,
			result.get(0).getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			startKonkubinat.plusYears(5).with(lastDayOfMonth()),
			result.get(0).getGueltigkeit().getGueltigBis()
		);
		assertEqualsNumberValue(
			2,
			result.get(0).getBgCalculationInputAsiv().getFamGroesse()
		);
		assertEqualsNumberValue(
			0,
			result.get(0).getBgCalculationInputAsiv().getAbzugFamGroesse()
		);

		assertEquals(
			startKonkubinat.plusYears(5).with(firstDayOfNextMonth()),
			result.get(1).getGueltigkeit().getGueltigAb()
		);
		assertEquals(
			ENDE_PERIODE,
			result.get(1).getGueltigkeit().getGueltigBis()
		);
		assertEqualsNumberValue(
			3,
			result.get(1).getBgCalculationInputAsiv().getFamGroesse()
		);
		assertEqualsNumberValue(
			11400,
			result.get(1).getBgCalculationInputAsiv().getAbzugFamGroesse()
		); //3 x 3'800
	}

	@Test
	void zweiHalbeKinderZweiGesuchsteller_shouldCount4ForPauschaleAnd3ForFamiliengroesse() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			true
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());
		addKindWithKinderabzugToGesuch(gesuch, Kinderabzug.HALBER_ABZUG);
		addKindWithKinderabzugToGesuch(gesuch, Kinderabzug.HALBER_ABZUG);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(1, zeitabschnitte.size());
		final VerfuegungZeitabschnitt zeitabschnitt = zeitabschnitte.get(0);
		// Familiengrösse = 3 => 2 GS + 2 x Halbes Kind
		// Pauschale von 4 Personen nehmen => 6'000
		// Familien abzug = 3 * 6'000 => 18000
		assertEqualsNumberValue(18000, zeitabschnitt.getAbzugFamGroesse());
		assertEqualsNumberValue(3, zeitabschnitt.getFamGroesse());
	}

	@Test
	void einHalbesKindEinGanzesKindZweiGesuchsteller_shouldCount4ForPauschaleAnd3AndAHalfForFamiliengroesse() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			true
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());
		addKindWithKinderabzugToGesuch(gesuch, Kinderabzug.HALBER_ABZUG);
		addKindWithKinderabzugToGesuch(gesuch, Kinderabzug.GANZER_ABZUG);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(1, zeitabschnitte.size());
		final VerfuegungZeitabschnitt zeitabschnitt = zeitabschnitte.get(0);
		// Familiengrösse = 3.5 => 2 GS + 1 x Ganzes Kind + 1 x halbes Kind
		// Pauschale von 5 Personen nehmen => 7'000
		// Familien abzug = 3 * 7'000 => 21000
		assertEqualsNumberValue(21000, zeitabschnitt.getAbzugFamGroesse());
		assertEqualsNumberValue(3.5, zeitabschnitt.getFamGroesse());
	}

	@Test
	void einGanzesKindEinKindOhneAbzugZweiGesuchsteller_shouldCount3ForPauschaleAndFamilengroesse() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			true
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());
		addKindWithKinderabzugToGesuch(gesuch, Kinderabzug.KEIN_ABZUG);
		addKindWithKinderabzugToGesuch(gesuch, Kinderabzug.GANZER_ABZUG);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(1, zeitabschnitte.size());
		final VerfuegungZeitabschnitt zeitabschnitt = zeitabschnitte.get(0);
		// Familiengrösse = 3 => 2 GS + 1 x Ganzes Kind
		// Pauschale von 3 Personen nehmen => 3800
		// Familien abzug = 3 * 3800 => 11400
		assertEqualsNumberValue(11400, zeitabschnitt.getAbzugFamGroesse());
		assertEqualsNumberValue(3, zeitabschnitt.getFamGroesse());
	}

	@Test
	void einGanzesKindEinKindGeborenInZukunf_shouldNotCountBeforeBirth() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			true
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());
		addKindWithKinderabzugToGesuch(gesuch, Kinderabzug.GANZER_ABZUG);

		LocalDate geburtsdatumNachEndePeriode = ENDE_PERIODE.plusMonths(1);
		KindContainer kindContainer = addKindWithKinderabzugToGesuch(
			gesuch,
			Kinderabzug.GANZER_ABZUG
		);
		kindContainer.getKindJA().setGeburtsdatum(geburtsdatumNachEndePeriode);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(1, zeitabschnitte.size());
		final VerfuegungZeitabschnitt zeitabschnitt = zeitabschnitte.get(0);
		// Familiengrösse = 3 => 2 GS + 1 x Ganzes Kind
		// Pauschale von 3 Personen nehmen => 3800
		// Familien abzug = 3 * 3800 => 11400
		assertEqualsNumberValue(11400, zeitabschnitt.getAbzugFamGroesse());
		assertEqualsNumberValue(3, zeitabschnitt.getFamGroesse());
	}

	@Test
	void einGanzesKindVolljahrig_shouldCountFull() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			true
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());

		LocalDate geburtsdatumNachEndePeriode = START_PERIODE.minusYears(19);
		KindContainer kindContainer = addKindWithKinderabzugToGesuch(
			gesuch,
			Kinderabzug.GANZER_ABZUG
		);
		kindContainer.getKindJA().setGeburtsdatum(geburtsdatumNachEndePeriode);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(1, zeitabschnitte.size());
		final VerfuegungZeitabschnitt zeitabschnitt = zeitabschnitte.get(0);
		// Familiengrösse = 3 => 2 GS + 1 x Ganzes Kind
		// Pauschale von 3 Personen nehmen => 3800
		// Familien abzug = 3 * 3800 => 11400
		assertEqualsNumberValue(11400, zeitabschnitt.getAbzugFamGroesse());
		assertEqualsNumberValue(3, zeitabschnitt.getFamGroesse());
	}

	@Test
	void abzugWechseltWaehrndPeriode_zweiGesuchsteller() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			true
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setKindContainers(new HashSet<>());
		addKindWithKinderabzugToGesuch(gesuch, Kinderabzug.GANZER_ABZUG);
		KindContainer kindContainer = addKindWithKinderabzugToGesuch(
			gesuch,
			Kinderabzug.GANZER_ABZUG
		);
		kindContainer.getKindJA()
			.setKinderabzugZweitesHalbjahr(Kinderabzug.HALBER_ABZUG);

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculate(betreuung);
		Assertions.assertNotNull(zeitabschnitte);
		Assertions.assertEquals(2, zeitabschnitte.size());
		// Familiengrösse = 4 => 2 GS + 2 x Ganzes Kind
		// Pauschale von 4 Personen nehmen => 6000
		// Familien abzug = 4 * 6000 => 24000
		assertEqualsNumberValue(
			24000,
			zeitabschnitte.get(0).getAbzugFamGroesse()
		);
		assertEqualsNumberValue(4, zeitabschnitte.get(0).getFamGroesse());

		// Familiengrösse = 3.5 => 2 GS + 1 x Ganzes Kind + 1 halbes Kind
		// Pauschale von 4 Personen nehmen => 6000
		// Familien abzug = 3.5 * 6000 => 24000
		assertEqualsNumberValue(
			21000,
			zeitabschnitte.get(1).getAbzugFamGroesse()
		);
		assertEqualsNumberValue(3.5, zeitabschnitte.get(1).getFamGroesse());
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void countAnzahlPersonen_gesuchsteller(int anzahlGesuchsteller) {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(anzahlGesuchsteller);

		int anzahlPersonenCounted = famabCalcRule.countAnzahlPersonen(input);
		assertEquals(anzahlGesuchsteller, anzahlPersonenCounted);
	}

	@ParameterizedTest
	@EnumSource(value = Kinderabzug.class,
		names = "KEIN_ABZUG",
		mode = EnumSource.Mode.EXCLUDE)
	void countAnzahlPersonen_kindCount(Kinderabzug kinderabzug) {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput();
		input.setAnzahlGesuchsteller(0);
		input.addKindToAbzugList(0, kinderabzug);

		int anzahlPersonenCounted = famabCalcRule.countAnzahlPersonen(input);
		assertEquals(1, anzahlPersonenCounted);
	}

	@Test
	void countAnzahlPersonen_kindNotCount() {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(0);

		input.addKindToAbzugList(0, Kinderabzug.KEIN_ABZUG);

		int anzahlPersonenCounted = famabCalcRule.countAnzahlPersonen(input);
		assertEquals(0, anzahlPersonenCounted);
	}

	@Test
	void countAnzahlPersonen_mehrerePersonen() {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(1);

		input.addKindToAbzugList(0, Kinderabzug.KEIN_ABZUG);
		input.addKindToAbzugList(1, Kinderabzug.HALBER_ABZUG);
		input.addKindToAbzugList(2, Kinderabzug.GANZER_ABZUG);

		int anzahlPersonenCounted = famabCalcRule.countAnzahlPersonen(input);
		assertEquals(3, anzahlPersonenCounted);
	}

	@ParameterizedTest
	@CsvSource({ "KEIN_ABZUG, 0", "HALBER_ABZUG, 0.5", "GANZER_ABZUG, 1" })
	void countAnzahlKinderFuerAbzug(
		Kinderabzug kinderabzug,
		double expectedCount
	) {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput();

		input.addKindToAbzugList(0, kinderabzug);

		double anzahlKinderCounted = famabCalcRule
			.countAnzahlKinderFuerAbzug(input);
		assertEquals(expectedCount, anzahlKinderCounted);
	}

	@Test
	void countAnzahlKinderFuerAbzug_mehrereKinder() {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput();

		input.addKindToAbzugList(0, Kinderabzug.KEIN_ABZUG);
		input.addKindToAbzugList(1, Kinderabzug.HALBER_ABZUG);
		input.addKindToAbzugList(2, Kinderabzug.GANZER_ABZUG);

		double anzahlKinderCounted = famabCalcRule
			.countAnzahlKinderFuerAbzug(input);
		assertEquals(1.5, anzahlKinderCounted);
	}

	@CanIgnoreReturnValue
	private KindContainer addKindWithKinderabzugToGesuch(
		Gesuch gesuch,
		Kinderabzug kinderabzug
	) {
		KindContainer kindContainer = addDefaultKindToGesuch(gesuch);
		kindContainer.getKindJA().setKinderabzugErstesHalbjahr(kinderabzug);
		kindContainer.getKindJA().setKinderabzugZweitesHalbjahr(kinderabzug);
		return kindContainer;
	}

	@CanIgnoreReturnValue
	private KindContainer addDefaultKindToGesuch(Gesuch gesuch) {
		KindContainer kindContainer = TestDataUtil.createDefaultKindContainer();
		kindContainer.setGesuch(gesuch);
		kindContainer.setKindNummer(gesuch.getKindContainers().size());
		gesuch.getKindContainers().add(kindContainer);
		return kindContainer;
	}

	@Nonnull
	private Gesuch createGesuchWithFamilienstatusAndKind(
		EnumFamilienstatus familienstatus,
		KindContainer kind
	) {
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		Familiensituation famSit = new Familiensituation();
		famSit.setFamilienstatus(familienstatus);
		gesuch.setFamiliensituationContainer(new FamiliensituationContainer());
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(famSit);
		gesuch.getKindContainers().add(kind);
		kind.setGesuch(gesuch);
		return gesuch;
	}

	private List<VerfuegungZeitabschnitt> calculateAllAbschnittRules(
		Betreuung betreuung
	) {
		List<VerfuegungZeitabschnitt> zaNachGesuchstellerRule =
			gsAbschnittRule.calculate(betreuung, new ArrayList<>());
		List<VerfuegungZeitabschnitt> zaNachKinderRule =
			kinderAbschnittRule.calculate(
				betreuung,
				zaNachGesuchstellerRule
			);
		return famabCalcRule.calculate(betreuung, zaNachKinderRule);
	}

	private Map<EinstellungKey, Einstellung> getEinstellungMapForAsiv() {
		Map<EinstellungKey, Einstellung> einstellungMapForAsiv =
			new HashMap<>(getDefaultEinstellungMap());
		Einstellung einstellungMinimalKonkubinat = new Einstellung(
			EinstellungKey.MINIMALDAUER_KONKUBINAT,
			"5",
			new Gesuchsperiode()
		);
		einstellungMapForAsiv.put(
			EinstellungKey.MINIMALDAUER_KONKUBINAT,
			einstellungMinimalKonkubinat
		);
		Einstellung einstellungKinderabzugTyp = new Einstellung(
			EinstellungKey.KINDERABZUG_TYP,
			KinderabzugTyp.ASIV.name(),
			new Gesuchsperiode()
		);
		einstellungMapForAsiv.put(
			EinstellungKey.KINDERABZUG_TYP,
			einstellungKinderabzugTyp
		);

		return einstellungMapForAsiv;
	}
}
