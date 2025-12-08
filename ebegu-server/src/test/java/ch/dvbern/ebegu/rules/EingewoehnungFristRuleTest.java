/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.EingewoehnungTyp;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.rules.util.ZeitabschnittAssertionHelper;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.einstellung.EinstellungKey.EINGEWOEHNUNG_TYP;
import static ch.dvbern.ebegu.enums.MsgKey.ERWERBSPENSUM_EINGEWOEHNUNG;
import static ch.dvbern.ebegu.enums.MsgKey.ERWERBSPENSUM_KEIN_ANSPRUCH;
import static ch.dvbern.ebegu.test.TestDataUtil.ENDE_PERIODE;
import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;
import static java.time.Month.AUGUST;
import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static java.time.Month.NOVEMBER;
import static java.time.Month.OCTOBER;
import static java.time.Month.SEPTEMBER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class EingewoehnungFristRuleTest {

	private static final int START_YEAR = START_PERIODE.getYear();
	private static final int END_YEAR = ENDE_PERIODE.getYear();
	private static final LocalDate AUGUST_31 = LocalDate.of(
		START_YEAR,
		AUGUST,
		31
	);
	private static final LocalDate SEPTEMBER_1 = LocalDate.of(
		START_YEAR,
		SEPTEMBER,
		1
	);
	private static final LocalDate SEPTEMBER_30 = LocalDate.of(
		START_YEAR,
		SEPTEMBER,
		30
	);
	private static final LocalDate OCTOBER_1 = LocalDate.of(
		START_YEAR,
		OCTOBER,
		1
	);
	private static final LocalDate OCTOBER_31 = LocalDate.of(
		START_YEAR,
		OCTOBER,
		31
	);
	private static final LocalDate NOVEMBER_1 = LocalDate.of(
		START_YEAR,
		NOVEMBER,
		1
	);
	private static final LocalDate NOVEMBER_30 = LocalDate.of(
		START_YEAR,
		NOVEMBER,
		30
	);
	private static final LocalDate DECEMBER_1 = LocalDate.of(
		START_YEAR,
		DECEMBER,
		1
	);
	private static final LocalDate DECEMBER_15 = LocalDate.of(
		START_YEAR,
		DECEMBER,
		15
	);
	private static final LocalDate DECEMBER_31 = LocalDate.of(
		START_YEAR,
		DECEMBER,
		31
	);
	private static final LocalDate JANUAR_1 = LocalDate.of(
		END_YEAR,
		JANUARY,
		1
	);

	@Test
	/*
	 * Normalenfall, keine Eingewoehnung
	 */
	void testEingewoehnungFristRule1GesuchstellerOhne() {
		Betreuung betreuung = createGesuch(false, false);
		Gesuch gesuch = betreuung.extractGesuch();

		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						SEPTEMBER_1,
						ENDE_PERIODE,
						100
					)
			);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);

		assertThat(result.size(), is(2));
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(0)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(AUGUST_31);
	}

	@Test
	/*
	 * Normalenfall, Eingewoehnung, 1 Erwerbspensum Begin Anfang September
	 */
	void testEingewoehnungFristRule1Gesuchsteller() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						SEPTEMBER_1,
						ENDE_PERIODE,
						100
					)
			);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);

		assertThat(result.size(), is(2));
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(100)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(AUGUST_31);
	}

	@Test
	/*
	 * Normalenfall, Eingewoehnung, 1 Erwerbspensum Begin Mitte August
	 */
	void testEingewoehnungFristRuleAnspruchAbMitteAugust() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		LocalDate AUG_15 = START_PERIODE.plusDays(15);

		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						AUG_15,
						ENDE_PERIODE,
						100
					)
			);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);

		assertThat(result.size(), is(3));
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(0)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(AUG_15.minusDays(1));

		assertThat(result.get(1).getGueltigkeit().getGueltigAb(), is(AUG_15));
		assertThat(result.get(0).getEinkommensjahr(), is(2016));
	}

	@Test
	/*
	 * Eingewoehnung, 2 Erwerbspensum, beides Begin Anfang September, beides verlaengert
	 */
	void testEingewoehnungFristRule1GesuchstellerManyErwerbspensenGleicheStartdatum() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						SEPTEMBER_1,
						ENDE_PERIODE,
						50
					)
			);
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						SEPTEMBER_1,
						ENDE_PERIODE,
						10
					)
			);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);
		// beide Erwerbspensum fangen gleich an, Sie muessen beide verleangert werden und summiert 50 + 10 +
		// Zuschlag 20
		assertThat(result.size(), is(2));
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(80)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(AUGUST_31);
	}

	@Test
	/*
	 * Eingewoehnung, 2 Erwerbspensum, eine Begin Anfang September, eine spaeter, nur die erste velaengert als genuegen
	 */
	void testEingewoehnungFristRule1GesuchstellerManyErwerbspensenNichtGleicheStartdatum() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						SEPTEMBER_1,
						ENDE_PERIODE,
						50
					)
			);
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						START_PERIODE.plusMonths(
							2
						),
						ENDE_PERIODE,
						10
					)
			);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);
		// nur die erste ist verlaengert: 50  + Zuschlag 20
		assertThat(result.size(), is(3));
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(70)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(AUGUST_31);
	}

	@Test
	/*
	 * Eingewoehnung, 2 Erwerbspensum, Freiwilligarbeit Begin Anfang September, eine andere Taetigkeit spaeter
	 * Es gibt keinen Zusaetzliche Anspruch bei die Gemeinde, so ASIV ignoriert die FreiwilligeArbeit Taetigkeit
	 */
	void testEingewoehnungFristRule1GesuchstellerFreiwilligeArbeitOhneGemeindeZuschlag() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						START_PERIODE,
						AUGUST_31,
						60,
						Taetigkeit.FREIWILLIGENARBEIT
					)
			);
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						SEPTEMBER_1,
						ENDE_PERIODE,
						40
					)
			);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);
		// die Erwerbspensum 2 ist von einer Monat verlaengert anstatt die erste
		assertThat(result.size(), is(2));
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(60)
			.assertBetreuungspensum(80)
			.assertMessageKeyExists(ERWERBSPENSUM_EINGEWOEHNUNG)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(AUGUST_31);

		new ZeitabschnittAssertionHelper(result.get(1))
			.assertAnspruch(60)
			.assertBetreuungspensum(80)
			.assertMessageKeyNotExists(ERWERBSPENSUM_EINGEWOEHNUNG)
			.assertGueltigAb(SEPTEMBER_1)
			.assertGueltigBis(ENDE_PERIODE);
	}

	@Test
	/*
	 * Eingewoehnung, 2 Erwerbspensum, Freiwilligarbeit Begin Anfang September, eine andere Taetigkeit spaeter
	 * Es gibt einen Zusaetzliche Anspruch bei die Gemeinde von 20, so die FreiwilligeArbeit Taetigkeit
	 * ist verlaengert
	 */
	void testEingewoehnungFristRule1GesuchstellerFreiwilligeArbeitMitGemeindeZuschlag() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						START_PERIODE.plusMonths(
							1
						),
						ENDE_PERIODE,
						60,
						Taetigkeit.FREIWILLIGENARBEIT
					)
			);
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						START_PERIODE.plusMonths(
							2
						),
						ENDE_PERIODE,
						40
					)
			);
		Map<EinstellungKey, Einstellung> einstellungenMap = EbeguRuleTestsHelper
			.getAllEinstellungen(betreuung.extractGesuchsperiode());
		einstellungenMap.get(EINGEWOEHNUNG_TYP)
			.setValue(EingewoehnungTyp.FKJV.toString());
		List<VerfuegungZeitabschnitt> result =
			EbeguRuleTestsHelper.calculate(
				betreuung,
				EbeguRuleTestsHelper.getEinstellungenRulesParis(
					gesuch.getGesuchsperiode()
				),
				einstellungenMap
			);
		// Freiwilligenarbeit hat 20 Porcent zuschlag, das heisst das die erste Erwerbspensum ist dieses Mal erweitert
		// von einen Monat
		assertThat(result.size(), is(3));
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(40)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(AUGUST_31);
	}

	@Test
	/**
	 * Eingewoehnung, 2 Gesuchstellende, beide Erwerspensen mit gleiche Startdatum
	 */
	void testEingewoehnungFristRule2GesuchstellerGleicheStart() {
		Betreuung betreuung = createGesuch(true, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						START_PERIODE.plusMonths(
							1
						),
						ENDE_PERIODE,
						100
					)
			);
		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		gesuch.getGesuchsteller2()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						START_PERIODE.plusMonths(
							1
						),
						ENDE_PERIODE,
						40
					)
			);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);
		//2 Gesuchstellende, 140% => 40% brechtigt + 20 zuschlag
		assertThat(result.size(), is(2));
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(60)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(AUGUST_31);
	}

	@Test
	/*
	 * Eingewoehnung, 2 Gesuchstellende
	 */
	void testEingewoehnungFristRule2Gesuchsteller1StartBevor() {
		Betreuung betreuung = createGesuch(true, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						SEPTEMBER_1,
						ENDE_PERIODE,
						100
					)
			);
		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		gesuch.getGesuchsteller2()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						OCTOBER_1,
						ENDE_PERIODE,
						40
					)
			);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);
		//Nur 1 Gesuchsteller Erwerbspensum mit 100 ist verlaengert, minimum 120 nicht erreicht, 0 Anspruch
		assertThat(result.size(), is(3));
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(0)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(AUGUST_31);

		new ZeitabschnittAssertionHelper(result.get(1))
			.assertAnspruch(60)
			.assertGueltigAb(SEPTEMBER_1)
			.assertGueltigBis(SEPTEMBER_30);
	}

	@Test
	/*
	 * Eingewoehnung, 2 Gesuchstellende, unterschiedliche Erwerbspensen, Betreuung startet waehrend Periode
	 */
	void testEingewoehnungFristRuleNachGPStart2Gesuchsteller() {
		Betreuung betreuung = createGesuch(true, true);
		Gesuch gesuch = betreuung.extractGesuch();

		Betreuungspensum eingewoehnung = new Betreuungspensum();
		eingewoehnung.setGueltigkeit(
			new DateRange(
				SEPTEMBER_1,
				SEPTEMBER_30
			)
		);
		eingewoehnung.setPensum(new BigDecimal(40));
		eingewoehnung.setMonatlicheBetreuungskosten(new BigDecimal(1500));

		betreuung.getBetreuungspensumContainers()
			.stream()
			.findFirst()
			.get()
			.getBetreuungspensumJA()
			.getGueltigkeit()
			.setGueltigAb(OCTOBER_1);

		BetreuungspensumContainer eingewoehnungContainer =
			new BetreuungspensumContainer();
		eingewoehnungContainer.setBetreuungspensumJA(eingewoehnung);

		betreuung.getBetreuungspensumContainers().add(eingewoehnungContainer);

		// 1.8 - 31.8. 70% + 20% Zuschlag => 90%, kein Anspruch
		// 1.9 - 30.9. 90% + 20% Zuschlag => 110%, kein Anspruch
		// 1.9 - ...   140% + 20% Zuschlag => 160%, 60% Anspruch
		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						SEPTEMBER_1,
						ENDE_PERIODE,
						20
					)
			);
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						OCTOBER_1,
						ENDE_PERIODE,
						50
					)
			);
		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		gesuch.getGesuchsteller2()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						START_PERIODE,
						ENDE_PERIODE,
						70
					)
			);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);
		//2 Gesuchstellende, 140% => 40% brechtigt + 20 zuschlag
		assertThat(result.size(), is(3));
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(AUGUST_31);

		assertThat(result.get(1).getAnspruchberechtigtesPensum(), is(60));
		assertThat(result.get(2).getAnspruchberechtigtesPensum(), is(60));
	}

	@Test
	void eingewoehungFristRuleEingagsdatumZuSpaet_keineEingewoehnungDaBetreuungsstartUberschritten() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setEingangsdatum(START_PERIODE.plusDays(1));

		LocalDate SEP_06 = START_PERIODE.plusMonths(1).plusDays(5);

		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						SEP_06,
						ENDE_PERIODE,
						40
					)
			);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);

		assertThat(result.size(), is(4));

		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(0)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(AUGUST_31);

		new ZeitabschnittAssertionHelper(result.get(1))
			.assertAnspruch(0)
			.assertGueltigAb(SEPTEMBER_1)
			.assertGueltigBis(SEP_06.minusDays(1));

		new ZeitabschnittAssertionHelper(result.get(2))
			.assertAnspruch(60)
			.assertGueltigAb(SEP_06)
			.assertGueltigBis(SEPTEMBER_30);

		new ZeitabschnittAssertionHelper(result.get(3))
			.assertAnspruch(60)
			.assertGueltigAb(OCTOBER_1)
			.assertGueltigBis(ENDE_PERIODE);
	}

	@Test
	void eingewoehungFristRuleEingagsdatumZuSpaetBetreuungsstartDanach_eingewoehnungGewaehrt() {
		LocalDate AUG_06 = START_PERIODE.plusDays(5);
		LocalDate SEP_06 = AUG_06.plusMonths(1);

		Betreuung betreuung = createGesuchWithBetruungGueltigAb(
			false,
			true,
			AUG_06
		);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setEingangsdatum(START_PERIODE.plusDays(1));

		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						SEP_06,
						ENDE_PERIODE,
						40
					)
			);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);

		assertThat(result.size(), is(5));

		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(0)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(AUG_06.minusDays(1));

		new ZeitabschnittAssertionHelper(result.get(1))
			.assertAnspruch(0)
			.assertGueltigAb(AUG_06)
			.assertGueltigBis(AUGUST_31);

		new ZeitabschnittAssertionHelper(result.get(2))
			.assertAnspruch(0)
			.assertMessageKeyNotExists(ERWERBSPENSUM_EINGEWOEHNUNG)
			.assertGueltigAb(SEPTEMBER_1)
			.assertGueltigBis(SEP_06.minusDays(1));

		new ZeitabschnittAssertionHelper(result.get(3))
			.assertAnspruch(60)
			.assertGueltigAb(SEP_06)
			.assertGueltigBis(SEPTEMBER_30);

		new ZeitabschnittAssertionHelper(result.get(4))
			.assertAnspruch(60)
			.assertGueltigAb(OCTOBER_1)
			.assertGueltigBis(ENDE_PERIODE);
	}

	@Test
	void eingewoehungFristRuleErwerbsensumMitUnterbruch() {
		LocalDate NOV_15 = LocalDate.of(
			START_PERIODE.getYear(),
			NOVEMBER,
			15
		);

		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		//Betreuung ab 15.11.
		betreuung.getBetreuungspensumContainers()
			.stream()
			.findFirst()
			.get()
			.getBetreuungspensumJA()
			.getGueltigkeit()
			.setGueltigAb(NOV_15);

		assertThat(gesuch.getGesuchsteller1(), notNullValue());

		//ewp 1.8. - 30.9
		ErwerbspensumContainer ewp1 = TestDataUtil.createErwerbspensum(
			START_PERIODE,
			SEPTEMBER_30,
			40
		);

		//ewp 15.12 - 31.07
		ErwerbspensumContainer ewp2 = TestDataUtil.createErwerbspensum(
			DECEMBER_15,
			ENDE_PERIODE,
			40
		);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp1);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp2);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);

		assertThat(result.size(), is(6));

		//01.08-30.09 Anspruch 60 (40+ 20 Zuschlag)
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(60)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(SEPTEMBER_30);

		//01.10-14.11, Anspruch 0
		new ZeitabschnittAssertionHelper(result.get(1))
			.assertAnspruch(0)
			.assertGueltigAb(OCTOBER_1)
			.assertGueltigBis(NOV_15.minusDays(1));

		//15.11- 30.11, Anspruch 60 Eingewöhnung
		new ZeitabschnittAssertionHelper(result.get(2))
			.assertAnspruch(60)
			.assertMessageKeyExists(ERWERBSPENSUM_EINGEWOEHNUNG)
			.assertGueltigAb(NOV_15)
			.assertGueltigBis(NOVEMBER_30);

		//01.12- 15.12, Anspruch 60 Eingewöhnung
		new ZeitabschnittAssertionHelper(result.get(3))
			.assertAnspruch(60)
			.assertMessageKeyExists(ERWERBSPENSUM_EINGEWOEHNUNG)
			.assertGueltigAb(DECEMBER_1)
			.assertGueltigBis(DECEMBER_15.minusDays(1));

		//15.12- 31.12, Anspruch 60
		new ZeitabschnittAssertionHelper(result.get(4))
			.assertAnspruch(60)
			.assertGueltigAb(DECEMBER_15)
			.assertGueltigBis(DECEMBER_31);

		//01.1 - 31.07, Anspruch 60
		new ZeitabschnittAssertionHelper(result.get(5))
			.assertAnspruch(60)
			.assertGueltigAb(JANUAR_1)
			.assertGueltigBis(ENDE_PERIODE);
	}

	@Test
	void eingewoehungFristRuleErwerbsensumBetreuungNicht30TageVorher() {
		LocalDate OCT_10 = LocalDate.of(
			START_PERIODE.getYear(),
			OCTOBER,
			10
		);
		LocalDate START_EINGEWOEHNUNG = OCT_10.minusDays(30);
		LocalDate SEP_25 = LocalDate.of(
			START_PERIODE.getYear(),
			SEPTEMBER,
			25
		);

		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		//Betreuung ab 25.9.
		betreuung.getBetreuungspensumContainers()
			.stream()
			.findFirst()
			.get()
			.getBetreuungspensumJA()
			.getGueltigkeit()
			.setGueltigAb(SEP_25);

		assertThat(gesuch.getGesuchsteller1(), notNullValue());

		//ewp ab 10.10
		ErwerbspensumContainer ewp1 = TestDataUtil.createErwerbspensum(
			OCT_10,
			ENDE_PERIODE,
			40
		);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp1);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);

		assertThat(result.size(), is(6));

		//01.08-09.09 Anspruch 0%, Betreuung 0%
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(0)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(START_EINGEWOEHNUNG.minusDays(1))
			.assertMessageKeyNotExists(ERWERBSPENSUM_EINGEWOEHNUNG);

		//10.09.-24.09 Anspruch 60%, Eingewöhnung, Betreuung 0%
		new ZeitabschnittAssertionHelper(result.get(1))
			.assertAnspruch(60)
			.assertGueltigAb(START_EINGEWOEHNUNG)
			.assertGueltigBis(SEP_25.minusDays(1))
			.assertMessageKeyExists(ERWERBSPENSUM_EINGEWOEHNUNG);

		//25.09.-30.09. Anspruch 60%, Eingewöhnung, Betreuung 80%
		new ZeitabschnittAssertionHelper(result.get(2))
			.assertAnspruch(60)
			.assertBetreuungspensum(80)
			.assertGueltigAb(SEP_25)
			.assertGueltigBis(SEPTEMBER_30)
			.assertMessageKeyExists(ERWERBSPENSUM_EINGEWOEHNUNG);

		//01.10.-09.10. Anspruch 60%, Eingewöhnung, Betreuung 80%
		new ZeitabschnittAssertionHelper(result.get(3))
			.assertAnspruch(60)
			.assertBetreuungspensum(80)
			.assertGueltigAb(OCTOBER_1)
			.assertGueltigBis(OCT_10.minusDays(1))
			.assertMessageKeyExists(ERWERBSPENSUM_EINGEWOEHNUNG);

		//11.10.-31.10, Anspruch 60, Betreuung 80%
		new ZeitabschnittAssertionHelper(result.get(4))
			.assertAnspruch(60)
			.assertBetreuungspensum(80)
			.assertGueltigAb(OCT_10)
			.assertGueltigBis(OCTOBER_31)
			.assertMessageKeyNotExists(ERWERBSPENSUM_EINGEWOEHNUNG);

		//01.11-31.07, Anspruch 60, Betreuung 80%
		new ZeitabschnittAssertionHelper(result.get(5))
			.assertAnspruch(60)
			.assertBetreuungspensum(80)
			.assertGueltigAb(NOVEMBER_1)
			.assertGueltigBis(ENDE_PERIODE)
			.assertMessageKeyNotExists(ERWERBSPENSUM_EINGEWOEHNUNG);
	}

	@Test
	void eingewoehungFristRuleBetreuungMitUnterbruchEWPAbZweiterBetreuung() {
		LocalDate NOV_15 = LocalDate.of(
			START_PERIODE.getYear(),
			NOVEMBER,
			15
		);

		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		final BetreuungspensumContainer firstPensum = betreuung
			.getBetreuungspensumContainers()
			.stream()
			.findFirst()
			.get();

		final BetreuungspensumContainer secondPensum =
			firstPensum.copyBetreuungspensumContainer(
				new BetreuungspensumContainer(),
				AntragCopyType.MUTATION,
				betreuung
			);

		// BetreuungsPensum 1 bis 30.9.
		firstPensum
			.getBetreuungspensumJA()
			.getGueltigkeit()
			.setGueltigBis(SEPTEMBER_30);

		//BetreuungsPensum 2 ab 15.12.
		secondPensum
			.getBetreuungspensumJA()
			.getGueltigkeit()
			.setGueltigAb(NOV_15);

		var betreuungsPensen = new HashSet<BetreuungspensumContainer>();
		betreuungsPensen.add(firstPensum);
		betreuungsPensen.add(secondPensum);

		betreuung.setBetreuungspensumContainers(betreuungsPensen);

		assertThat(gesuch.getGesuchsteller1(), notNullValue());

		//ewp 15.12 - 31.07
		ErwerbspensumContainer ewp2 = TestDataUtil.createErwerbspensum(
			DECEMBER_15,
			ENDE_PERIODE,
			40
		);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp2);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);

		assertThat(result.size(), is(6));

		//01.08-30.9, Betreuung 80, Anspruch 0
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(0)
			.assertBetreuungspensum(80)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(SEPTEMBER_30);

		//01.10 - 14.11., Betreuung 0, Anspruch 0
		new ZeitabschnittAssertionHelper(result.get(1))
			.assertAnspruch(0)
			.assertBetreuungspensum(0)
			.assertGueltigAb(OCTOBER_1)
			.assertGueltigBis(NOV_15.minusDays(1))
			.assertMessageKeyNotExists(ERWERBSPENSUM_EINGEWOEHNUNG);

		//15.11- 30.11., Betreuung 0, Anspruch 0
		new ZeitabschnittAssertionHelper(result.get(2))
			.assertAnspruch(60)
			.assertBetreuungspensum(80)
			.assertGueltigAb(NOV_15)
			.assertGueltigBis(NOVEMBER_30)
			.assertMessageKeyExists(ERWERBSPENSUM_EINGEWOEHNUNG);

		new ZeitabschnittAssertionHelper(result.get(3))
			.assertAnspruch(60)
			.assertBetreuungspensum(80)
			.assertGueltigAb(DECEMBER_1)
			.assertGueltigBis(DECEMBER_15.minusDays(1))
			.assertMessageKeyExists(ERWERBSPENSUM_EINGEWOEHNUNG);

		//15.12- 31.12, Anspruch 60
		new ZeitabschnittAssertionHelper(result.get(4))
			.assertBetreuungspensum(80)
			.assertAnspruch(60)
			.assertGueltigAb(DECEMBER_15)
			.assertGueltigBis(DECEMBER_31);

		//01.1 - 31.07, Anspruch 60
		new ZeitabschnittAssertionHelper(result.get(5))
			.assertBetreuungspensum(80)
			.assertAnspruch(60)
			.assertGueltigAb(JANUAR_1)
			.assertGueltigBis(ENDE_PERIODE);
	}

	@Test
	void eingewoehungFristRuleAnspruchOverlappingEingewoehnungAndMonthStartPensumSteigend() {
		LocalDate DEC_8 = LocalDate.of(
			START_PERIODE.getYear(),
			DECEMBER,
			8
		);
		LocalDate DEC_22 = LocalDate.of(
			START_PERIODE.getYear(),
			DECEMBER,
			22
		);

		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		final BetreuungspensumContainer firstPensum = betreuung
			.getBetreuungspensumContainers()
			.stream()
			.findFirst()
			.orElseThrow();

		// BetreuungsPensum 15.12. - 31.7.
		firstPensum
			.getBetreuungspensumJA()
			.getGueltigkeit()
			.setGueltigAb(DECEMBER_15);
		firstPensum
			.getBetreuungspensumJA()
			.getGueltigkeit()
			.setGueltigBis(ENDE_PERIODE);

		var betreuungsPensen = new HashSet<BetreuungspensumContainer>();
		betreuungsPensen.add(firstPensum);
		betreuung.setBetreuungspensumContainers(betreuungsPensen);

		assertThat(gesuch.getGesuchsteller1(), notNullValue());

		gesuch.getGesuchsteller1().getErwerbspensenContainers().clear();
		//ewp 1.8. - 8.12.
		ErwerbspensumContainer ewp = TestDataUtil.createErwerbspensum(
			START_PERIODE,
			DEC_8,
			40
		);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp);
		//ewp 22.12 -
		ErwerbspensumContainer ewp2 = TestDataUtil.createErwerbspensum(
			DEC_22,
			ENDE_PERIODE,
			60
		);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp2);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);

		assertThat(result.size(), is(6));

		//01.08- 21.11., Betreuung 0, Anspruch 60
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(60)
			.assertBetreuungspensum(0)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(DEC_22.minusMonths(1).minusDays(1));

		//22.11. - 8.12.  Betreuung 0, Anspruch 60 Eingewoehnung Anspruch 80
		new ZeitabschnittAssertionHelper(result.get(1))
			.assertAnspruch(80)
			.assertBetreuungspensum(0)
			.assertGueltigAb(DEC_22.minusMonths(1))
			.assertGueltigBis(DEC_8)
			.assertMessageKeyExists(ERWERBSPENSUM_EINGEWOEHNUNG);

		//9.12 - 14.12., kein Anspruch Eingewöhnung Anspruch 80, Betreuung 0
		new ZeitabschnittAssertionHelper(result.get(2))
			.assertAnspruch(80)
			.assertGueltigAb(DEC_8.plusDays(1))
			.assertGueltigBis(DECEMBER_15.minusDays(1))
			.assertMessageKeyExists(ERWERBSPENSUM_EINGEWOEHNUNG)
			.assertMessageKeyExists(ERWERBSPENSUM_KEIN_ANSPRUCH);

		//15.12 - 21.12, kein Anspruch Eingewöhnung Anspruch 80, Betreuung 60
		new ZeitabschnittAssertionHelper(result.get(3))
			.assertAnspruch(80)
			.assertGueltigAb(DECEMBER_15)
			.assertGueltigBis(DEC_22.minusDays(1))
			.assertMessageKeyExists(ERWERBSPENSUM_EINGEWOEHNUNG)
			.assertMessageKeyExists(ERWERBSPENSUM_KEIN_ANSPRUCH);

		// 22.12. Anspruch 80, Betreuung 60
		new ZeitabschnittAssertionHelper(result.get(4))
			.assertAnspruch(80)
			.assertGueltigAb(DEC_22)
			.assertGueltigBis(DECEMBER_31);
	}

	@Test
	void eingewoehungFristRuleAnspruchOverlappingEingewoehnungAndMonthStartPensumSinkend() {
		LocalDate DEC_8 = LocalDate.of(
			START_PERIODE.getYear(),
			DECEMBER,
			8
		);
		LocalDate DEC_22 = LocalDate.of(
			START_PERIODE.getYear(),
			DECEMBER,
			22
		);

		// BetreuungsPensum 15.12. - 31.7.
		Betreuung betreuung = createGesuchWithBetruungGueltigAb(
			false,
			true,
			DECEMBER_15
		);
		Gesuch gesuch = betreuung.extractGesuch();

		assertThat(gesuch.getGesuchsteller1(), notNullValue());

		gesuch.getGesuchsteller1().getErwerbspensenContainers().clear();
		//ewp 1.8. - 8.12.
		ErwerbspensumContainer ewp = TestDataUtil.createErwerbspensum(
			START_PERIODE,
			DEC_8,
			80
		);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp);
		//ewp 22.12 -
		ErwerbspensumContainer ewp2 = TestDataUtil.createErwerbspensum(
			DEC_22,
			ENDE_PERIODE,
			60
		);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp2);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);

		assertThat(result.size(), is(4));

		//01.08- 14.12., Betreuung 0, Anspruch 100
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(100)
			.assertBetreuungspensum(0)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(DEC_22.minusMonths(1).minusDays(1));

		//22.11. - 8.12.  Betreuung 0, Anspruch 100 Eingewoehnung Anspruch 80
		new ZeitabschnittAssertionHelper(result.get(1))
			.assertAnspruch(100)
			.assertBetreuungspensum(0)
			.assertMessageKeyExists(ERWERBSPENSUM_EINGEWOEHNUNG)
			.assertGueltigAb(DEC_22.minusMonths(1))
			.assertGueltigBis(DECEMBER_15.minusDays(1));

		//15.12 - 31.12, Anspruch 100 Eingewöhnung Anspruch 80, Betreuung 60
		new ZeitabschnittAssertionHelper(result.get(2))
			.assertAnspruch(100)
			.assertMessageKeyExists(ERWERBSPENSUM_EINGEWOEHNUNG)
			.assertGueltigAb(DECEMBER_15)
			.assertGueltigBis(DECEMBER_31);

		// 1.1. - 31.7. Anspruch 80, Betreuung 60
		new ZeitabschnittAssertionHelper(result.get(3))
			.assertAnspruch(80)
			.assertGueltigAb(JANUAR_1)
			.assertGueltigBis(ENDE_PERIODE);
	}

	/*
		Eingewöhnung darf nur zu Beginn des Betreuungspensums gewährt werden.
	
		Beispiel:
		- Betreuungstart 01.09.
		- Beschäfitungspensum  01.08.-31.10. & ab 01.12.
		- Eingewöhnung im Dezember und Januar darf nicht gewährt werden
	 */
	@Test
	void eingewoehungBeschaeftigungspensumUnterbrochen_keineEingewoehnungInMitteVonBetreuung() {
		// Betreuung über die ganze Periode
		Betreuung betreuung = createGesuchWithBetruungGueltigAb(
			false,
			true,
			SEPTEMBER_1
		);
		Gesuch gesuch = betreuung.extractGesuch();

		assertThat(gesuch.getGesuchsteller1(), notNullValue());

		gesuch.getGesuchsteller1().getErwerbspensenContainers().clear();
		//ewp 1.8. - 31.10.
		ErwerbspensumContainer ewp = TestDataUtil.createErwerbspensum(
			START_PERIODE,
			OCTOBER_31,
			40
		);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp);
		//ewp ab 01.12
		ErwerbspensumContainer ewp2 = TestDataUtil.createErwerbspensum(
			DECEMBER_1,
			ENDE_PERIODE,
			40
		);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp2);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);

		assertThat(result.size(), is(4));

		//01.08- 31.10., Anspruch 60 (40 + 20 Zuschlag)
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(60)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(AUGUST_31);

		//01.08- 31.10., Anspruch 60 (40 + 20 Zuschlag)
		new ZeitabschnittAssertionHelper(result.get(1))
			.assertAnspruch(60)
			.assertGueltigAb(SEPTEMBER_1)
			.assertGueltigBis(OCTOBER_31);

		//1.11. - 30.11  Anspruch 0, keine Eingewöhnung
		new ZeitabschnittAssertionHelper(result.get(2))
			.assertAnspruch(0)
			.assertGueltigAb(NOVEMBER_1)
			.assertGueltigBis(NOVEMBER_30)
			.assertMessageKeyNotExists(ERWERBSPENSUM_EINGEWOEHNUNG);

		// ab 01.12. Anspruch 40 (40 + 20 Zuschlag)
		new ZeitabschnittAssertionHelper(result.get(3))
			.assertAnspruch(60)
			.assertGueltigAb(DECEMBER_1)
			.assertGueltigBis(ENDE_PERIODE);
	}

	/*
	Eingewöhnung darf nur zu Beginn des Betreuungspensums gewährt werden.
	
	Beispiel:
	- Betreuungstart 01.08.
	- Beschäfitungspensum  ab 01.11
	- Eingewöhnung im Oktober darf nicht gewährt werden, da Betreuung bereits im August gestartet hat
	*/
	@Test
	void eingewoehungBeschaeftigungspensumSpaeterAlsBetreuung_keineEingewoehnungInMitteVonBetreuung() {
		// Betreuung über die ganze Periode
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertThat(gesuch.getGesuchsteller1(), notNullValue());

		gesuch.getGesuchsteller1().getErwerbspensenContainers().clear();
		//ewp ab 01.11
		ErwerbspensumContainer ewp = TestDataUtil.createErwerbspensum(
			NOVEMBER_1,
			ENDE_PERIODE,
			40
		);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(ewp);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);

		assertThat(result.size(), is(3));

		//01.08- 31.09., Anspruch 60 (40 + 20 Zuschlag)
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(0)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(SEPTEMBER_30)
			.assertMessageKeyNotExists(ERWERBSPENSUM_EINGEWOEHNUNG);

		//ab 01.10, Anspruch 60 (40 + 20 Zuschlag)
		new ZeitabschnittAssertionHelper(result.get(1))
			.assertAnspruch(60)
			.assertGueltigAb(OCTOBER_1)
			.assertGueltigBis(OCTOBER_31);
	}

	@Test
	void wohnSitzNotInGemeindeAfterWegzug_noEingewoehnungGewaehrt() {
		Betreuung betreuung = createGesuch(false, true);
		Gesuch gesuch = betreuung.extractGesuch();

		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		List<GesuchstellerAdresseContainer> adressen = new ArrayList<>();

		final GesuchstellerAdresseContainer wegzug = TestDataUtil
			.createDefaultGesuchstellerAdresseContainer(
				gesuch.getGesuchsteller1()
			);
		assertThat(wegzug.getGesuchstellerAdresseJA(), notNullValue());
		wegzug.getGesuchstellerAdresseJA().setNichtInGemeinde(true);
		wegzug.getGesuchstellerAdresseJA()
			.setGueltigkeit(
				new DateRange(
					Constants.START_OF_TIME,
					AUGUST_31
				)
			);

		final GesuchstellerAdresseContainer zuzug = TestDataUtil
			.createDefaultGesuchstellerAdresseContainer(
				gesuch.getGesuchsteller1()
			);
		assertThat(zuzug.getGesuchstellerAdresseJA(), notNullValue());
		zuzug.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		zuzug.getGesuchstellerAdresseJA()
			.setGueltigkeit(
				new DateRange(
					SEPTEMBER_1,
					Constants.END_OF_TIME
				)
			);

		adressen.add(wegzug);
		adressen.add(zuzug);
		gesuch.getGesuchsteller1().setAdressen(adressen);

		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil
					.createErwerbspensum(
						SEPTEMBER_1,
						ENDE_PERIODE,
						100
					)
			);

		List<VerfuegungZeitabschnitt> result = calculateMitEingewoehnung(
			betreuung
		);

		assertThat(result.size(), is(2));
		new ZeitabschnittAssertionHelper(result.get(0))
			.assertAnspruch(0)
			.assertMessageKeyNotExists(ERWERBSPENSUM_EINGEWOEHNUNG)
			.assertGueltigAb(START_PERIODE)
			.assertGueltigBis(AUGUST_31);
	}

	private Betreuung createGesuch(
		final boolean gs2,
		final boolean eingewoehnung
	) {
		return createGesuchWithBetruungGueltigAb(
			gs2,
			eingewoehnung,
			START_PERIODE
		);
	}

	private Betreuung createGesuchWithBetruungGueltigAb(
		final boolean gs2,
		final boolean eingewoehnung,
		LocalDate gueltigAb
	) {
		final Betreuung betreuung = TestDataUtil
			.createGesuchWithBetreuungspensum(gs2);
		betreuung.setEingewoehnung(eingewoehnung);

		BetreuungspensumContainer container = TestDataUtil
			.createBetPensContainer(betreuung);
		container.getGueltigkeit().setGueltigAb(gueltigAb);
		container.getGueltigkeit().setGueltigBis(ENDE_PERIODE);
		betreuung.getBetreuungspensumContainers().add(container);
		betreuung.initVorgaengerVerfuegungen(null, null);

		return betreuung;
	}

	private List<VerfuegungZeitabschnitt> calculateMitEingewoehnung(
		@Nonnull Betreuung betreuung
	) {
		Map<EinstellungKey, Einstellung> einstellungenMap = EbeguRuleTestsHelper
			.getAllEinstellungen(betreuung.extractGesuchsperiode());
		einstellungenMap.get(EINGEWOEHNUNG_TYP)
			.setValue(EingewoehnungTyp.FKJV.toString());
		return EbeguRuleTestsHelper.calculate(
			betreuung,
			EbeguRuleTestsHelper.getAllEinstellungen(
				betreuung.extractGesuchsperiode()
			),
			einstellungenMap
		);
	}
}
