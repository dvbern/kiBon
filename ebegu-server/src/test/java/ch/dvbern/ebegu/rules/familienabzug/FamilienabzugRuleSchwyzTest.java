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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FamilienabzugRuleSchwyzTest {
	private static final BigDecimal SOZIALABZUG_PRO_KIND = BigDecimal.valueOf(
		6700
	);
	private static final BigDecimal HALBER_SOZIALABZUG_PRO_KIND = BigDecimal
		.valueOf(3350);

	@Test
	void singleGS_kinderZaehltGanz_test() {
		Betreuung betreuung =
			createGesuchWithOneGesuchstellerAndOneKinderWithAbzug(
				Kinderabzug.GANZER_ABZUG
			);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			betreuung,
			getEinstellungMapForSchwyz()
		);

		Assertions.assertEquals(1, result.size());
		VerfuegungZeitabschnitt zeitabschnitt = result.get(0);
		Assertions.assertEquals(
			SOZIALABZUG_PRO_KIND,
			zeitabschnitt.getBgCalculationInputAsiv().getAbzugFamGroesse()
		);
	}

	@Test
	void singleGS_kinderZaehltHalb_test() {
		Betreuung betreuung =
			createGesuchWithOneGesuchstellerAndOneKinderWithAbzug(
				Kinderabzug.HALBER_ABZUG
			);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			betreuung,
			getEinstellungMapForSchwyz()
		);

		Assertions.assertEquals(1, result.size());
		VerfuegungZeitabschnitt zeitabschnitt = result.get(0);
		Assertions.assertEquals(
			HALBER_SOZIALABZUG_PRO_KIND,
			zeitabschnitt.getBgCalculationInputAsiv().getAbzugFamGroesse()
		);
	}

	@Test
	void zweiGS_kinderZaehltGanz_test() {
		Betreuung betreuung =
			createGesuchWithTwoGesuchstellerAndOneKinderWithAbzug(
				Kinderabzug.GANZER_ABZUG
			);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			betreuung,
			getEinstellungMapForSchwyz()
		);

		Assertions.assertEquals(1, result.size());
		VerfuegungZeitabschnitt zeitabschnitt = result.get(0);
		Assertions.assertEquals(
			SOZIALABZUG_PRO_KIND,
			zeitabschnitt.getBgCalculationInputAsiv().getAbzugFamGroesse()
		);
	}

	@Test
	void zweiGS_kinderZaehltHalb_test() {
		Betreuung betreuung =
			createGesuchWithTwoGesuchstellerAndOneKinderWithAbzug(
				Kinderabzug.HALBER_ABZUG
			);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			betreuung,
			getEinstellungMapForSchwyz()
		);

		Assertions.assertEquals(1, result.size());
		VerfuegungZeitabschnitt zeitabschnitt = result.get(0);
		Assertions.assertEquals(
			HALBER_SOZIALABZUG_PRO_KIND,
			zeitabschnitt.getBgCalculationInputAsiv().getAbzugFamGroesse()
		);
	}

	@Test
	void singleGSZweiKinder_einHalbes_einGanzes() {
		Betreuung betreuung =
			createGesuchWithTwoGesuchstellerAndOneKinderWithAbzug(
				Kinderabzug.HALBER_ABZUG
			);
		final KindContainer kind = TestDataUtil.createDefaultKindContainer();
		kind.setKindNummer(2);
		kind.getKindJA().setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		betreuung.extractGesuch().getKindContainers().add(kind);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			betreuung,
			getEinstellungMapForSchwyz()
		);

		Assertions.assertEquals(1, result.size());
		VerfuegungZeitabschnitt zeitabschnitt = result.get(0);

		BigDecimal expectedResult = BigDecimal.valueOf(0)
			.add(HALBER_SOZIALABZUG_PRO_KIND)
			.add(SOZIALABZUG_PRO_KIND);
		Assertions.assertEquals(
			expectedResult,
			zeitabschnitt.getBgCalculationInputAsiv().getAbzugFamGroesse()
		);
	}

	@Test
	void zweiGS_zweiKinderZaehltGanz_test() {
		Betreuung betreuung =
			createGesuchWithTwoGesuchstellerAndOneKinderWithAbzug(
				Kinderabzug.GANZER_ABZUG
			);
		final KindContainer kind = TestDataUtil.createDefaultKindContainer();
		kind.getKindJA().setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		kind.setKindNummer(2);
		betreuung.extractGesuch().getKindContainers().add(kind);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			betreuung,
			getEinstellungMapForSchwyz()
		);

		Assertions.assertEquals(1, result.size());
		VerfuegungZeitabschnitt zeitabschnitt = result.get(0);

		BigDecimal expectedResult = BigDecimal.valueOf(0)
			.add(SOZIALABZUG_PRO_KIND)
			.add(SOZIALABZUG_PRO_KIND);
		Assertions.assertEquals(
			expectedResult,
			zeitabschnitt.getBgCalculationInputAsiv().getAbzugFamGroesse()
		);
	}

	@Test
	void zweiGS_zweiKinderEineZaehltHalb_test() {
		Betreuung betreuung =
			createGesuchWithTwoGesuchstellerAndOneKinderWithAbzug(
				Kinderabzug.GANZER_ABZUG
			);
		final KindContainer kind = TestDataUtil.createDefaultKindContainer();
		kind.getKindJA().setKinderabzugErstesHalbjahr(Kinderabzug.HALBER_ABZUG);
		kind.setKindNummer(2);
		betreuung.extractGesuch().getKindContainers().add(kind);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			betreuung,
			getEinstellungMapForSchwyz()
		);

		Assertions.assertEquals(1, result.size());
		VerfuegungZeitabschnitt zeitabschnitt = result.get(0);

		BigDecimal expectedResult = BigDecimal.valueOf(0)
			.add(HALBER_SOZIALABZUG_PRO_KIND)
			.add(SOZIALABZUG_PRO_KIND);
		Assertions.assertEquals(
			expectedResult,
			zeitabschnitt.getBgCalculationInputAsiv().getAbzugFamGroesse()
		);
	}

	@Nonnull
	private Betreuung createGesuchWithOneGesuchstellerAndOneKinderWithAbzug(
		Kinderabzug kinderabzug
	) {
		return createGesuchWithKinderWithAbzug(false, kinderabzug);
	}

	@Nonnull
	private Betreuung createGesuchWithTwoGesuchstellerAndOneKinderWithAbzug(
		Kinderabzug kinderabzug
	) {
		return createGesuchWithKinderWithAbzug(true, kinderabzug);
	}

	@Nonnull
	private Betreuung createGesuchWithKinderWithAbzug(
		boolean zweiGS,
		Kinderabzug kinderabzug
	) {

		Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		GesuchstellerContainer gesuchsteller = new GesuchstellerContainer();
		gesuchsteller.setGesuchstellerJA(new Gesuchsteller());
		gesuch.setGesuchsteller1(gesuchsteller);
		if (zweiGS) {
			gesuch.setGesuchsteller2(gesuchsteller);
		}
		Familiensituation famSit = new Familiensituation();
		famSit.setFamilienstatus(EnumFamilienstatus.SCHWYZ);
		famSit.setGesuchstellerKardinalitaet(
			zweiGS ?
				EnumGesuchstellerKardinalitaet.ZU_ZWEIT :
				EnumGesuchstellerKardinalitaet.ALLEINE
		);
		final FamiliensituationContainer familiensituationContainer =
			new FamiliensituationContainer();
		familiensituationContainer.setFamiliensituationJA(famSit);
		gesuch.setFamiliensituationContainer(familiensituationContainer);
		gesuch.setKindContainers(new HashSet<>());
		final KindContainer kind = TestDataUtil.createDefaultKindContainer();
		kind.setGesuch(gesuch);
		kind.getKindJA().setKinderabzugErstesHalbjahr(kinderabzug);
		kind.setKindNummer(1);
		gesuch.getKindContainers().add(kind);
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		betreuung.setKind(kind);
		betreuung.initVorgaengerVerfuegungen(null, null);
		return betreuung;
	}

	private Map<EinstellungKey, Einstellung> getEinstellungMapForSchwyz() {
		Map<EinstellungKey, Einstellung> einstellungMapForSchwyz =
			EbeguRuleTestsHelper.getEinstellungenConfiguratorAsiv(
				TestDataUtil.createGesuchsperiode1718()
			);
		einstellungMapForSchwyz.get(EinstellungKey.KINDERABZUG_TYP)
			.setValue(KinderabzugTyp.SCHWYZ.name());
		einstellungMapForSchwyz.get(EinstellungKey.MINIMALDAUER_KONKUBINAT)
			.setValue("2");
		return einstellungMapForSchwyz;
	}
}
