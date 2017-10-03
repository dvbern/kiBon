package ch.dvbern.ebegu.rules;

import java.util.List;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testet die BetreuungsangebotTyp Regel
 */
public class BetreuungsangebotTypCalcRuleTest {

	@Test
	public void testAngebotKita() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(BetreuungsangebotTyp.KITA));

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(60, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertTrue(result.get(0).getBemerkungen().isEmpty());
	}

	@Test
	public void testAngebotTagi() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(BetreuungsangebotTyp.TAGI));

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(80, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertTrue(result.get(0).getBemerkungen().isEmpty());
	}

	@Test
	public void testAngebotTageselternKleinkind() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(BetreuungsangebotTyp.TAGESELTERN_KLEINKIND));

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(60, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertTrue(result.get(0).getBemerkungen().isEmpty());
	}

	@Test
	public void testAngebotTageselternSchulkind() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(BetreuungsangebotTyp.TAGESELTERN_SCHULKIND));

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(80, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertTrue(result.get(0).getBemerkungen().isEmpty());
	}

	@Test
	public void testAngebotTagesschule() {
		List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(prepareData(BetreuungsangebotTyp.TAGESSCHULE));

		Assert.assertNotNull(result);
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(0, result.get(0).getAnspruchberechtigtesPensum());
		Assert.assertFalse(result.get(0).getBemerkungen().isEmpty());
	}

	private Betreuung prepareData(BetreuungsangebotTyp betreuungsangebotTyp) {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(betreuungsangebotTyp);
		gesuch.getGesuchsteller1().addErwerbspensumContainer(TestDataUtil.createErwerbspensum(TestDataUtil.START_PERIODE, TestDataUtil.ENDE_PERIODE, 60, 0));
		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		betreuungspensumContainer.setBetreuungspensumJA(new Betreuungspensum());
		betreuungspensumContainer.getBetreuungspensumJA().setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		betreuungspensumContainer.getBetreuungspensumJA().setPensum(80);
		betreuung.getBetreuungspensumContainers().add(betreuungspensumContainer);
		return betreuung;
	}
}
