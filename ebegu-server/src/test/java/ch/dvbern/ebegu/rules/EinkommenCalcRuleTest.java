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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.VerfuegungsBemerkungDTOList;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationBernRechner;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.einstellung.EinstellungKey.FKJV_PAUSCHALE_BEI_ANSPRUCH;
import static ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.EINSTELLUNG_MAX_EINKOMMEN;
import static ch.dvbern.ebegu.util.Constants.EinstellungenDefaultWerteAsiv.MAX_EINKOMMEN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Testet die MaximalesEinkommen-Regel
 */
class EinkommenCalcRuleTest {

	private final BigDecimal EINKOMMEN = MathUtil.DEFAULT.fromNullSafe(100000);
	private final BigDecimal EINKOMMEN_HOCH = MathUtil.DEFAULT.fromNullSafe(
		180000
	);

	private Mandant mandant;

	@BeforeEach
	public void setUp() {
		mandant = new Mandant();
		mandant.setMandantIdentifier(MandantIdentifier.BERN);
	}

	@Test
	void testNormalfallKita() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			prepareBetreuungKita(
				EINKOMMEN,
				false,
				false,
				false,
				FinSitStatus.AKZEPTIERT
			)
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertThat(
			EINKOMMEN.compareTo(abschnitt.getMassgebendesEinkommen()),
			is(0)
		);
		assertThat(abschnitt.getAnspruchberechtigtesPensum(), is(100));
		assertThat(
			abschnitt.getBgCalculationInputAsiv()
				.isKeinAnspruchAufgrundEinkommen(),
			is(false)
		);
		assertThat(abschnitt.getBemerkungenDTOList().isEmpty(), is(false));
		assertThat(abschnitt.getBemerkungenDTOList().uniqueSize(), is(2));
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH),
			is(true)
		);
		assertThat(
			result.get(0)
				.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH),
			is(true)
		);
	}

	@Test
	void testNormalfallTagesschule() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			prepareBetreuungTagesschule(
				EINKOMMEN,
				false,
				false,
				FinSitStatus.AKZEPTIERT
			)
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertThat(
			EINKOMMEN.compareTo(abschnitt.getMassgebendesEinkommen()),
			is(0)
		);
		assertThat(abschnitt.getAnspruchberechtigtesPensum(), is(100));
		assertThat(
			abschnitt.getBgCalculationInputAsiv()
				.isKeinAnspruchAufgrundEinkommen(),
			is(false)
		);
		assertThat(abschnitt.getBemerkungenDTOList().isEmpty(), is(false));
		assertThat(abschnitt.getBemerkungenDTOList().uniqueSize(), is(1));
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.BETREUUNGSANGEBOT_MSG),
			is(true)
		);
	}

	@Test
	void testEinkommenZuHochKita() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			prepareBetreuungKita(
				EINKOMMEN_HOCH,
				false,
				false,
				false,
				FinSitStatus.AKZEPTIERT
			)
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertThat(
			EINKOMMEN_HOCH.compareTo(abschnitt.getMassgebendesEinkommen()),
			is(0)
		);
		assertThat(abschnitt.getAnspruchberechtigtesPensum(), is(100));
		assertThat(
			abschnitt.getBgCalculationInputAsiv()
				.isKeinAnspruchAufgrundEinkommen(),
			is(true)
		);
		assertThat(abschnitt.getBemerkungenDTOList().isEmpty(), is(false));
		assertThat(abschnitt.getBemerkungenDTOList().uniqueSize(), is(3));
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.EINKOMMEN_MAX_MSG),
			is(true)
		);
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH),
			is(true)
		);
	}

	@Test
	void testEinkommenZuHochTagesschule() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			prepareBetreuungTagesschule(
				EINKOMMEN_HOCH,
				false,
				false,
				FinSitStatus.AKZEPTIERT
			)
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertThat(
			EINKOMMEN_HOCH.compareTo(abschnitt.getMassgebendesEinkommen()),
			is(0)
		);
		assertThat(abschnitt.getAnspruchberechtigtesPensum(), is(100));
		assertThat(
			abschnitt.getBgCalculationInputAsiv()
				.isKeinAnspruchAufgrundEinkommen(),
			is(true)
		);
		assertThat(abschnitt.getBemerkungenDTOList().isEmpty(), is(false));
		assertThat(abschnitt.getBemerkungenDTOList().uniqueSize(), is(2));
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.EINKOMMEN_MAX_MSG),
			is(true)
		);
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.BETREUUNGSANGEBOT_MSG),
			is(true)
		);
	}

	/**
	 * Erstellt einen Testfall mit 2 EKV.
	 * Am Ende schaut es dass die Bemerkungen richtig geschrieben wurden
	 */
	@Test
	void testAcceptedEKV() {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE,
			KITA,
			100,
			BigDecimal.ZERO
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
		Gesuch gesuch = betreuung.extractGesuch();

		gesuch.setEinkommensverschlechterungInfoContainer(
			new EinkommensverschlechterungInfoContainer()
		);
		final EinkommensverschlechterungInfo einkommensverschlechterungInfoJA =
			new EinkommensverschlechterungInfo();
		einkommensverschlechterungInfoJA.setEinkommensverschlechterung(true);
		einkommensverschlechterungInfoJA.setEkvFuerBasisJahrPlus1(true);
		einkommensverschlechterungInfoJA.setEkvFuerBasisJahrPlus2(true);
		assertThat(
			gesuch.getEinkommensverschlechterungInfoContainer(),
			notNullValue()
		);
		gesuch.getEinkommensverschlechterungInfoContainer()
			.setEinkommensverschlechterungInfoJA(
				einkommensverschlechterungInfoJA
			);

		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil.createErwerbspensum(
					TestDataUtil.START_PERIODE,
					TestDataUtil.ENDE_PERIODE,
					100
				)
			);
		gesuch.getGesuchsteller1()
			.setFinanzielleSituationContainer(
				new FinanzielleSituationContainer()
			);
		assertThat(
			gesuch.getGesuchsteller1().getFinanzielleSituationContainer(),
			notNullValue()
		);
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setNettolohn(new BigDecimal(50000));
		TestDataUtil.calculateFinanzDaten(
			gesuch,
			new FinanzielleSituationBernRechner()
		);

		gesuch.getGesuchsteller1()
			.setEinkommensverschlechterungContainer(
				new EinkommensverschlechterungContainer()
			);
		final Einkommensverschlechterung ekvJABasisJahrPlus1 =
			new Einkommensverschlechterung();
		ekvJABasisJahrPlus1.setNettolohn(new BigDecimal(25000));
		assertThat(
			gesuch.getGesuchsteller1()
				.getEinkommensverschlechterungContainer(),
			notNullValue()
		);
		gesuch.getGesuchsteller1()
			.getEinkommensverschlechterungContainer()
			.setEkvJABasisJahrPlus1(ekvJABasisJahrPlus1);
		final Einkommensverschlechterung ekvJABasisJahrPlus2 =
			new Einkommensverschlechterung();
		ekvJABasisJahrPlus2.setNettolohn(new BigDecimal(20000));
		gesuch.getGesuchsteller1()
			.getEinkommensverschlechterungContainer()
			.setEkvJABasisJahrPlus2(ekvJABasisJahrPlus2);

		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			betreuung
		);
		assertThat(result.size(), is(2));

		VerfuegungZeitabschnitt abschnittErstesHalbjahrEKV1 = result.get(0);
		assertThat(
			abschnittErstesHalbjahrEKV1.getMassgebendesEinkommen().intValue(),
			is(25000)
		);
		VerfuegungsBemerkungDTOList bemerkungenAbschnitt2 =
			abschnittErstesHalbjahrEKV1.getBemerkungenDTOList();
		assertThat(bemerkungenAbschnitt2, notNullValue());
		assertThat(bemerkungenAbschnitt2.uniqueSize(), is(3));
		assertThat(
			bemerkungenAbschnitt2.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH),
			is(true)
		);
		assertThat(
			bemerkungenAbschnitt2.containsMsgKey(
				MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG
			),
			is(true)
		);
		assertThat(
			bemerkungenAbschnitt2.containsMsgKey(
				MsgKey.VERFUEGUNG_MIT_ANSPRUCH
			),
			is(true)
		);

		VerfuegungZeitabschnitt abschnittZweitesHalbjahrEKV1 = result.get(1);
		assertThat(
			abschnittZweitesHalbjahrEKV1.getMassgebendesEinkommen().intValue(),
			is(20000)
		);
		VerfuegungsBemerkungDTOList bemerkungenAbschnitt3 =
			abschnittZweitesHalbjahrEKV1.getBemerkungenDTOList();
		assertThat(bemerkungenAbschnitt3, notNullValue());
		assertThat(bemerkungenAbschnitt3.uniqueSize(), is(3));
		assertThat(
			bemerkungenAbschnitt3.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH),
			is(true)
		);
		assertThat(
			bemerkungenAbschnitt3.containsMsgKey(
				MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG
			),
			is(true)
		);
		assertThat(
			bemerkungenAbschnitt3.containsMsgKey(
				MsgKey.VERFUEGUNG_MIT_ANSPRUCH
			),
			is(true)
		);
	}

	@Test
	void testSozialhilfebezueger() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			prepareBetreuungKita(
				EINKOMMEN,
				true,
				false,
				false,
				FinSitStatus.AKZEPTIERT
			)
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertThat(
			(new BigDecimal("0.00")).compareTo(
				abschnitt.getMassgebendesEinkommen()
			),
			is(0)
		);
		assertThat(abschnitt.getAnspruchberechtigtesPensum(), is(100));
		assertThat(
			abschnitt.getBgCalculationInputAsiv()
				.isBezahltKompletteVollkosten(),
			is(false)
		);
		assertThat(
			abschnitt.getBgCalculationInputAsiv()
				.isKeinAnspruchAufgrundEinkommen(),
			is(false)
		);
		assertThat(abschnitt.getBemerkungenDTOList().isEmpty(), is(false));
		assertThat(abschnitt.getBemerkungenDTOList().uniqueSize(), is(3));
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG),
			is(true)
		);
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH),
			is(true)
		);
		assertThat(
			result.get(0)
				.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH),
			is(true)
		);
	}

	@Test
	void testNurPauschaleFuerErweiterteBeduernisse() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			prepareBetreuungKita(
				EINKOMMEN_HOCH,
				false,
				true,
				true,
				FinSitStatus.AKZEPTIERT
			)
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), Matchers.is(1));
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertThat(
			(new BigDecimal(EINSTELLUNG_MAX_EINKOMMEN)).compareTo(
				result.get(0).getMassgebendesEinkommen()
			),
			is(0)
		);
		assertThat(
			"Anspruch wird wegen Pauschale bes. Bed. nicht auf 0 gesetzt",
			abschnitt.getAnspruchberechtigtesPensum(),
			is(100)
		);
		assertThat(
			"erweiterteBetreuung: BezahltVollkosten nicht gesetzt",
			abschnitt.getBgCalculationInputAsiv()
				.isBezahltKompletteVollkosten(),
			is(false)
		);
		assertThat(
			abschnitt.getBgCalculationInputAsiv()
				.isKeinAnspruchAufgrundEinkommen(),
			is(true)
		);
		assertThat(abschnitt.getBemerkungenDTOList().isEmpty(), is(false));
		assertThat(abschnitt.getBemerkungenDTOList().uniqueSize(), is(4));
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(
					MsgKey.EINKOMMEN_KEINE_VERGUENSTIGUNG_GEWUENSCHT_MSG
				),
			is(true)
		);
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH),
			is(true)
		);
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.ERWEITERTE_BEDUERFNISSE_MSG),
			is(true)
		);
		assertThat(
			result.get(0)
				.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH),
			is(true)
		);
	}

	@Test
	void testKeinePauschaleFuerErweiterteBeduernisseWennEinkommenZuHoch() {
		Betreuung betreuung = prepareBetreuungKita(
			EINKOMMEN_HOCH,
			false,
			true,
			true,
			FinSitStatus.AKZEPTIERT
		);
		Map<EinstellungKey, Einstellung> einstellungenMap = EbeguRuleTestsHelper
			.getAllEinstellungen(betreuung.extractGesuchsperiode());
		einstellungenMap.get(FKJV_PAUSCHALE_BEI_ANSPRUCH).setValue("true");
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			betreuung,
			einstellungenMap
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertThat(
			result.get(0).getMassgebendesEinkommen(),
			is(new BigDecimal(EINSTELLUNG_MAX_EINKOMMEN))
		);
		assertThat(
			result.get(0)
				.getRelevantBgCalculationResult()
				.getVerguenstigung()
				.stripTrailingZeros(),
			is(BigDecimal.ZERO)
		);
		assertThat(
			abschnitt.getBgCalculationInputAsiv().hasAnspruch(),
			is(false)
		);
		assertThat(
			abschnitt.getBgCalculationInputAsiv()
				.isKeinAnspruchAufgrundEinkommen(),
			is(true)
		);
		assertThat(abschnitt.getBemerkungenDTOList().isEmpty(), is(false));
		assertThat(abschnitt.getBemerkungenDTOList().uniqueSize(), is(3));
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(
					MsgKey.EINKOMMEN_KEINE_VERGUENSTIGUNG_GEWUENSCHT_MSG
				),
			is(true)
		);
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH),
			is(true)
		);
		assertThat(
			result.get(0)
				.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH),
			is(true)
		);
		assertThat(
			result.get(0)
				.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.ERWEITERTE_BEDUERFNISSE_MSG),
			is(false)
		);
	}

	@Test
	void testKeineFinSitErfasstOhneErweiterteBeduerfnisse() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			prepareBetreuungKita(
				EINKOMMEN,
				false,
				true,
				false,
				FinSitStatus.AKZEPTIERT
			)
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertThat(abschnitt.getMassgebendesEinkommen(), is(MAX_EINKOMMEN));
		assertThat(
			abschnitt.getBgCalculationInputAsiv().hasAnspruch(),
			is(false)
		);
		assertThat(
			abschnitt.getBgCalculationInputAsiv()
				.isKeinAnspruchAufgrundEinkommen(),
			is(true)
		);
		assertThat(abschnitt.getBemerkungenDTOList().isEmpty(), is(false));
		assertThat(abschnitt.getBemerkungenDTOList().uniqueSize(), is(3));
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(
					MsgKey.EINKOMMEN_KEINE_VERGUENSTIGUNG_GEWUENSCHT_MSG
				),
			is(true)
		);
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH),
			is(true)
		);
		assertThat(
			result.get(0)
				.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH),
			is(true)
		);
	}

	@Test
	void testFinSitStatusNullKita() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			prepareBetreuungKita(
				EINKOMMEN,
				false,
				false,
				false,
				null
			)
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertThat(
			EINKOMMEN.compareTo(abschnitt.getMassgebendesEinkommen()),
			is(0)
		);
		assertThat(abschnitt.getAnspruchberechtigtesPensum(), is(100));
		assertThat(
			abschnitt.getBgCalculationInputAsiv()
				.isBezahltKompletteVollkosten(),
			is(false)
		);
		assertThat(
			abschnitt.getBgCalculationInputAsiv()
				.isKeinAnspruchAufgrundEinkommen(),
			is(false)
		);
		assertThat(abschnitt.getBemerkungenDTOList().isEmpty(), is(false));
		assertThat(abschnitt.getBemerkungenDTOList().uniqueSize(), is(2));
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH),
			is(true)
		);
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH),
			is(true)
		);
	}

	@Test
	void testFinSitStatusAbgelehntOhneBesondereBeduerfnisse() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			prepareBetreuungKita(
				EINKOMMEN,
				false,
				false,
				false,
				FinSitStatus.ABGELEHNT
			)
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertThat(abschnitt.getMassgebendesEinkommen(), is(MAX_EINKOMMEN));
		assertThat(
			abschnitt.getRelevantBgCalculationResult()
				.getVerguenstigung()
				.stripTrailingZeros(),
			is(BigDecimal.ZERO)
		);
		assertThat(
			abschnitt.getBgCalculationInputAsiv().hasAnspruch(),
			is(false)
		);
		assertThat(
			abschnitt.getBgCalculationInputAsiv()
				.isKeinAnspruchAufgrundEinkommen(),
			is(true)
		);
		assertThat(abschnitt.getBemerkungenDTOList().isEmpty(), is(false));
		assertThat(abschnitt.getBemerkungenDTOList().uniqueSize(), is(2));
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH),
			is(true)
		);
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(
					MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_ERSTGESUCH_MSG
				),
			is(true)
		);
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH),
			is(false)
		);
	}

	@Test
	void testFinSitStatusAbgelehntMitBesondereBeduerfnisse() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			prepareBetreuungKita(
				EINKOMMEN,
				false,
				false,
				true,
				FinSitStatus.ABGELEHNT
			)
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		final VerfuegungZeitabschnitt abschnitt = result.get(0);
		assertThat(abschnitt.getMassgebendesEinkommen(), is(MAX_EINKOMMEN));
		assertThat(
			"Anspruch wird wegen Pauschale bes. Bed. nicht auf 0 gesetzt",
			abschnitt.getAnspruchberechtigtesPensum(),
			is(100)
		);
		assertThat(
			"erweiterteBetreuung: BezahltVollkosten nicht gesetzt",
			abschnitt.getBgCalculationInputAsiv()
				.isBezahltKompletteVollkosten(),
			is(false)
		);
		assertThat(
			"keinAnspruchAufgrundEinkommen gilt auch wenn erweiterteBetreuung",
			abschnitt.getBgCalculationInputAsiv()
				.isKeinAnspruchAufgrundEinkommen(),
			is(true)
		);
		assertThat(
			abschnitt.getBgCalculationInputAsiv().hasAnspruch(),
			is(false)
		);
		assertThat(abschnitt.getBemerkungenDTOList().isEmpty(), is(false));
		assertThat(abschnitt.getBemerkungenDTOList().uniqueSize(), is(4));
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH),
			is(true)
		);
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(
					MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_ERSTGESUCH_MSG
				),
			is(true)
		);
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.ERWEITERTE_BEDUERFNISSE_MSG),
			is(true)
		);
		assertThat(
			abschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.VERFUEGUNG_MIT_ANSPRUCH),
			is(true)
		);
	}

	@Test
	void finSitAbgelehentForSozialhilfeEmpfaenger() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			prepareBetreuungKita(
				EINKOMMEN,
				true,
				false,
				false,
				FinSitStatus.ABGELEHNT
			)
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		assertThat(result.get(0).getMassgebendesEinkommen(), is(MAX_EINKOMMEN));
		assertThat(
			result.get(0).getAbzugFamGroesse().stripTrailingZeros(),
			is(BigDecimal.ZERO.stripTrailingZeros())
		);
		assertThat(
			result.get(0)
				.getRelevantBgCalculationInput()
				.isKeinAnspruchAufgrundEinkommen(),
			is(true)
		);
		assertThat(
			result.get(0)
				.getRelevantBgCalculationInput()
				.isKategorieMaxEinkommen(),
			is(true)
		);
		assertThat(
			result.get(0)
				.getBemerkungenDTOList()
				.containsMsgKey(
					MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_ERSTGESUCH_MSG
				),
			is(true)
		);
		assertThat(
			result.get(0)
				.getBemerkungenDTOList()
				.containsMsgKey(
					MsgKey.EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG_FKJV
				),
			is(false)
		);
		assertThat(
			result.get(0)
				.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG),
			is(false)
		);
	}

	@Test
	void finSitAbgelehentForSozialhilfeEmpfaengerAndTagesschulanmeldung() {
		AnmeldungTagesschule anmeldungTagesschule = prepareBetreuungTagesschule(
			EINKOMMEN,
			true,
			false,
			FinSitStatus.ABGELEHNT
		);
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			anmeldungTagesschule
		);

		BigDecimal maxTsTarif = EbeguRuleTestsHelper
			.getAllEinstellungen(
				anmeldungTagesschule.extractGesuchsperiode()
			)
			.get(EinstellungKey.MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG)
			.getValueAsBigDecimal();

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		assertThat(result.get(0).getMassgebendesEinkommen(), is(MAX_EINKOMMEN));
		assertThat(
			result.get(0).getAbzugFamGroesse().stripTrailingZeros(),
			is(BigDecimal.ZERO.stripTrailingZeros())
		);
		assertThat(
			result.get(0)
				.getRelevantBgCalculationInput()
				.isKeinAnspruchAufgrundEinkommen(),
			is(true)
		);
		assertThat(
			result.get(0)
				.getRelevantBgCalculationInput()
				.isKategorieMaxEinkommen(),
			is(true)
		);
		assertThat(
			maxTsTarif,
			is(
				result.get(0)
					.getTsCalculationResultMitPaedagogischerBetreuung()
					.getGebuehrProStunde()
			)
		);
	}

	@Test
	void finSitAkzeptiertForSozialhilfeEmpfaenger() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			prepareBetreuungKita(
				EINKOMMEN,
				true,
				false,
				false,
				FinSitStatus.AKZEPTIERT
			)
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		assertThat(
			result.get(0).getMassgebendesEinkommen().stripTrailingZeros(),
			is(BigDecimal.ZERO.stripTrailingZeros())
		);
		assertThat(
			result.get(0)
				.getBemerkungenDTOList()
				.containsMsgKey(
					MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_ERSTGESUCH_MSG
				),
			is(false)
		);
		assertThat(
			result.get(0)
				.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG),
			is(true)
		);
	}

	@Test
	void finSitAkzeptiert() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			prepareBetreuungKita(
				EINKOMMEN,
				true,
				false,
				false,
				FinSitStatus.AKZEPTIERT
			)
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		assertThat(
			result.get(0).getBgCalculationInputAsiv().isFinsitAccepted(),
			is(true)
		);
	}

	@Test
	void finSitAbgelehnt() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(
			prepareBetreuungKita(
				EINKOMMEN,
				true,
				false,
				false,
				FinSitStatus.ABGELEHNT
			)
		);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(1));
		assertThat(
			result.get(0).getBgCalculationInputAsiv().isFinsitAccepted(),
			is(false)
		);
	}

	private Betreuung prepareBetreuungKita(
		@Nonnull BigDecimal massgebendesEinkommen,
		boolean sozialhilfeempfaenger,
		boolean keineVerguenstigungGewuenscht,
		boolean erweiterteBeduerfnisse,
		@Nullable FinSitStatus finSitStatus
	) {
		Betreuung betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			TestDataUtil.START_PERIODE,
			TestDataUtil.ENDE_PERIODE,
			KITA,
			100,
			MathUtil.DEFAULT.fromNullSafe(2000)
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
		prepareGesuch(
			betreuung,
			massgebendesEinkommen,
			sozialhilfeempfaenger,
			keineVerguenstigungGewuenscht,
			finSitStatus
		);
		if (erweiterteBeduerfnisse) {
			betreuung.setErweiterteBetreuungContainer(
				new ErweiterteBetreuungContainer()
			);
			betreuung.getErweiterteBetreuungContainer()
				.setErweiterteBetreuungJA(new ErweiterteBetreuung());
			ErweiterteBetreuung erweiterteBetreuungJA = betreuung
				.getErweiterteBetreuungContainer()
				.getErweiterteBetreuungJA();
			Objects.requireNonNull(erweiterteBetreuungJA);
			erweiterteBetreuungJA.setErweiterteBeduerfnisse(true);
			erweiterteBetreuungJA.setFachstelle(new Fachstelle());
			erweiterteBetreuungJA.setErweiterteBeduerfnisseBestaetigt(true);
			erweiterteBetreuungJA.setKeineKesbPlatzierung(true);
			erweiterteBetreuungJA.setAnspruchFachstelleWennPensumUnterschritten(
				false
			);
		}
		return betreuung;
	}

	private AnmeldungTagesschule prepareBetreuungTagesschule(
		@Nonnull BigDecimal massgebendesEinkommen,
		boolean sozialhilfeempfaenger,
		boolean keineVerguenstigungGewuenscht,
		@Nullable FinSitStatus finSitStatus
	) {
		final AnmeldungTagesschule anmeldung = TestDataUtil
			.createGesuchWithAnmeldungTagesschule();
		prepareGesuch(
			anmeldung,
			massgebendesEinkommen,
			sozialhilfeempfaenger,
			keineVerguenstigungGewuenscht,
			finSitStatus
		);
		return anmeldung;
	}

	private void prepareGesuch(
		@Nonnull AbstractPlatz platz,
		@Nonnull BigDecimal massgebendesEinkommen,
		boolean sozialhilfeempfaenger,
		boolean keineVerguenstigungGewuenscht,
		@Nullable FinSitStatus finSitStatus
	) {
		Gesuch gesuch = platz.extractGesuch();
		gesuch.setFinSitStatus(finSitStatus);
		TestDataUtil.calculateFinanzDaten(
			gesuch,
			new FinanzielleSituationBernRechner()
		);
		assertThat(gesuch.getGesuchsteller1(), notNullValue());
		gesuch.getGesuchsteller1()
			.addErwerbspensumContainer(
				TestDataUtil.createErwerbspensum(
					TestDataUtil.START_PERIODE,
					TestDataUtil.ENDE_PERIODE,
					100
				)
			);
		gesuch.getGesuchsteller1()
			.setFinanzielleSituationContainer(
				new FinanzielleSituationContainer()
			);
		assertThat(
			gesuch.getGesuchsteller1().getFinanzielleSituationContainer(),
			notNullValue()
		);
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setNettolohn(massgebendesEinkommen);
		if (sozialhilfeempfaenger) {
			Objects.requireNonNull(gesuch.extractFamiliensituation())
				.setSozialhilfeBezueger(true);
		}
		Objects.requireNonNull(gesuch.extractFamiliensituation())
			.setVerguenstigungGewuenscht(!keineVerguenstigungGewuenscht);
	}
}
