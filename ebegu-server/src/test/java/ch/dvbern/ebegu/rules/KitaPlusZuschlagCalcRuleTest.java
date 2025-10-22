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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.einstellung.EinstellungKey.KITAPLUS_ZUSCHLAG_AKTIVIERT;

class KitaPlusZuschlagCalcRuleTest {

	private KitaPlusZuschlagCalcRule ruleToTest;

	private Betreuung betreuung;

	private BGCalculationInput inputData;

	@BeforeEach
	public void setUp() {
		Mandant luzern = new Mandant();
		luzern.setMandantIdentifier(MandantIdentifier.LUZERN);
		DateRange validy = new DateRange(
			LocalDate.of(1000, 1, 1),
			LocalDate.of(3000, 1, 1)
		);
		betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			Constants.DEFAULT_GUELTIGKEIT.getGueltigAb(),
			Constants.DEFAULT_GUELTIGKEIT.getGueltigBis(),
			BetreuungsangebotTyp.KITA,
			60,
			new BigDecimal(2000),
			luzern
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
		ErwerbspensumContainer erwerbspensumContainer = TestDataUtil
			.createErwerbspensumContainer();
		Assertions.assertNotNull(erwerbspensumContainer.getErwerbspensumJA());
		erwerbspensumContainer.getErwerbspensumJA()
			.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		Assertions.assertNotNull(betreuung.extractGesuch().getGesuchsteller1());
		Assertions.assertNotNull(
			Objects.requireNonNull(
				betreuung.extractGesuch().getGesuchsteller1()
			)
				.getErwerbspensenContainers()
		);
		Objects.requireNonNull(betreuung.extractGesuch().getGesuchsteller1())
			.addErwerbspensumContainer(erwerbspensumContainer);
		ruleToTest = new KitaPlusZuschlagCalcRule(
			validy,
			Constants.DEUTSCH_LOCALE
		);
		assert betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			!= null;
		inputData = new BGCalculationInput(
			new VerfuegungZeitabschnitt(),
			RuleValidity.ASIV
		);
	}

	@Test
	void testKitaPlusSet() {
		assert betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			!= null;
		betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			.setKitaPlusZuschlag(true);
		betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			.setKitaPlusZuschlagBestaetigt(true);

		ruleToTest.executeRule(betreuung, inputData);
		Assertions.assertTrue(inputData.isKitaPlusZuschlag());
	}

	@Test
	void testKitaPlusNotSet() {
		assert betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			!= null;
		betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			.setKitaPlusZuschlag(false);

		ruleToTest.executeRule(betreuung, inputData);
		Assertions.assertFalse(inputData.isKitaPlusZuschlag());
	}

	@Test
	void testRuleCalcKitaPlusNotSet() {
		assert betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			!= null;
		betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			.setKitaPlusZuschlag(false);

		Map<EinstellungKey, Einstellung> einstellungenMap = EbeguRuleTestsHelper
			.getAllEinstellungen(betreuung.extractGesuchsperiode());
		einstellungenMap.get(KITAPLUS_ZUSCHLAG_AKTIVIERT).setValue("true");

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			betreuung,
			einstellungenMap
		);
		Assertions.assertEquals(
			result.get(0).getVerguenstigung(),
			new BigDecimal("1414.50")
		);
	}

	@Test
	void testRuleCalcKitaPlusSet() {
		assert betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			!= null;
		betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			.setKitaPlusZuschlag(true);
		betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			.setKitaPlusZuschlagBestaetigt(true);

		Map<EinstellungKey, Einstellung> einstellungenMap = EbeguRuleTestsHelper
			.getAllEinstellungen(betreuung.extractGesuchsperiode());
		einstellungenMap.get(KITAPLUS_ZUSCHLAG_AKTIVIERT).setValue("true");

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			betreuung,
			einstellungenMap
		);
		// 1424.1 + 32 * 12 = 1860
		Assertions.assertEquals(
			result.get(0).getVerguenstigung(),
			new BigDecimal("1808.10")
		);
	}

	@Test
	void testRuleCalcKitaPlusSetEinstellungDeaktiviert() {
		assert betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			!= null;
		betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			.setKitaPlusZuschlag(true);

		Map<EinstellungKey, Einstellung> einstellungenMap = EbeguRuleTestsHelper
			.getAllEinstellungen(betreuung.extractGesuchsperiode());
		einstellungenMap.get(KITAPLUS_ZUSCHLAG_AKTIVIERT).setValue("false");

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			betreuung,
			einstellungenMap
		);
		Assertions.assertEquals(
			new BigDecimal("1414.50"),
			result.get(0).getVerguenstigung()
		);
	}

	@Test
	void testRuleCalcKitaPlusSetNotBestaetigt() {
		assert betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			!= null;
		betreuung.getErweiterteBetreuungContainer()
			.getErweiterteBetreuungJA()
			.setKitaPlusZuschlag(true);

		ruleToTest.executeRule(betreuung, inputData);
		Assertions.assertFalse(inputData.isKitaPlusZuschlag());
	}
}
