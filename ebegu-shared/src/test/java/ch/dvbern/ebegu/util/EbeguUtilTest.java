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

package ch.dvbern.ebegu.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * test fuer Ebeguutil
 */
class EbeguUtilTest {

	@Test
	void testFromOneGSToTwoGS_From2To1() {

		Familiensituation oldData = new Familiensituation();
		oldData.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		Familiensituation newData = new Familiensituation();
		newData.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assertions.assertFalse(
			EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now())
		);
	}

	@Test
	void testFromOneGSToTwoGS_From2To2() {
		Familiensituation oldData = new Familiensituation();
		oldData.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		Familiensituation newData = new Familiensituation();
		newData.setFamilienstatus(EnumFamilienstatus.KONKUBINAT);

		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assertions.assertFalse(
			EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now())
		);
	}

	@Test
	void testFromOneGSToTwoGS_From1To1() {
		Familiensituation oldData = new Familiensituation();
		oldData.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		oldData.setStartKonkubinat(LocalDate.now());

		Familiensituation newData = new Familiensituation();
		newData.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);

		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assertions.assertFalse(
			EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now())
		);
	}

	@Test
	void testFromOneGSToTwoGS_From1To2() {
		Familiensituation oldData = new Familiensituation();
		oldData.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		Familiensituation newData = new Familiensituation();
		newData.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);

		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assertions.assertTrue(EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now()));
	}

	@Test
	void testFromOneGSToTwoGS_nullFamilienstatus() {
		Familiensituation oldData = new Familiensituation();
		Familiensituation newData = new Familiensituation();

		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assertions.assertFalse(
			EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now())
		);
	}

	@Test
	void toFilename() {
		String filename = EbeguUtil.toFilename(
			"Kita Beundenweg/Crèche Oeuches.pdf"
		);
		Assertions.assertNotNull(filename);
		Assertions.assertEquals("Kita_Beundenweg_Crèche_Oeuches.pdf", filename);
	}

	@Test
	void isErlaeuterungenZurVerfuegungMit0AnspruchRequiredTest() {
		Gesuch gesuch = prepareGesuchForErlaeuterungenZurVerguegungTests(
			MandantIdentifier.BERN,
			0
		);
		// 0 Anspruch sollte keine Erlaeterung sein:
		Assertions.assertFalse(
			EbeguUtil.isErlaeuterungenZurVerfuegungRequired(gesuch)
		);
	}

	@Test
	void isErlaeuterungenZurVerfuegungMit1AnspruchRequiredTest() {
		Gesuch gesuch = prepareGesuchForErlaeuterungenZurVerguegungTests(
			MandantIdentifier.BERN,
			1
		);
		// 0 Anspruch sollte keine Erlaeterung sein:
		Assertions.assertTrue(
			EbeguUtil.isErlaeuterungenZurVerfuegungRequired(gesuch)
		);
	}

	@Test
	void isErlaeuterungenZurVerfuegungMit0AnspruchRequiredFuerAppenzellTest() {
		Gesuch gesuch = prepareGesuchForErlaeuterungenZurVerguegungTests(
			MandantIdentifier.APPENZELL_AUSSERRHODEN,
			0
		);
		// 0 Anspruch aber beim Appenzell ist einer Ausnahme so es muss immer sein:
		Assertions.assertTrue(
			EbeguUtil.isErlaeuterungenZurVerfuegungRequired(gesuch)
		);
	}

	@Test
	void isErlaeuterungenZurVerfuegungMit1AnspruchRequiredFuerAppenzellTest() {
		Gesuch gesuch = prepareGesuchForErlaeuterungenZurVerguegungTests(
			MandantIdentifier.APPENZELL_AUSSERRHODEN,
			1
		);
		Assertions.assertTrue(
			EbeguUtil.isErlaeuterungenZurVerfuegungRequired(gesuch)
		);
	}

	@Test
	void isErlaeuterungenZurVerfuegungFuerSchwyzRequiredTest() {
		Gesuch gesuch = prepareGesuchForErlaeuterungenZurVerguegungTests(
			MandantIdentifier.SCHWYZ,
			1
		);
		// Beim Schwyz sollte immer false sein
		Assertions.assertFalse(
			EbeguUtil.isErlaeuterungenZurVerfuegungRequired(gesuch)
		);
	}

	@Test
	void isFinanzielleSituationRequiredTest() {
		Gesuch gesuch = new Gesuch();
		Assertions.assertFalse(
			EbeguUtil.isFinanzielleSituationRequired(gesuch)
		);

		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setSozialhilfeBezueger(true);
		FamiliensituationContainer familiensituationContainer =
			new FamiliensituationContainer();
		familiensituationContainer.setFamiliensituationJA(familiensituation);
		gesuch.setFamiliensituationContainer(familiensituationContainer);
		Assertions.assertFalse(
			EbeguUtil.isFinanzielleSituationRequired(gesuch)
		);

		gesuch.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.setSozialhilfeBezueger(false);
		gesuch.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.setVerguenstigungGewuenscht(false);
		Assertions.assertFalse(
			EbeguUtil.isFinanzielleSituationRequired(gesuch)
		);

		gesuch.getFamiliensituationContainer()
			.getFamiliensituationJA()
			.setVerguenstigungGewuenscht(true);
		Assertions.assertTrue(EbeguUtil.isFinanzielleSituationRequired(gesuch));
	}

	@Test
	void contentEqualsNull() {
		Assertions.assertTrue(EbeguUtil.contentEquals(null, null));
		Assertions.assertFalse(EbeguUtil.contentEquals("test", null));
		Assertions.assertFalse(EbeguUtil.contentEquals(null, "test"));
	}

	@Test
	void contentEqualsSameContent() {
		Assertions.assertTrue(EbeguUtil.contentEquals("test", "test"));
		Assertions.assertTrue(EbeguUtil.contentEquals("test", " test"));
		Assertions.assertTrue(EbeguUtil.contentEquals("test ", "test"));
		Assertions.assertTrue(EbeguUtil.contentEquals("Test", "test"));
		Assertions.assertTrue(EbeguUtil.contentEquals("Test", "TEST"));
		Assertions.assertTrue(EbeguUtil.contentEquals(" Test ", "TEST"));
	}

	@Test
	void contentEqualsNotSameContent() {
		Assertions.assertFalse(EbeguUtil.contentEquals("test", "test1"));
		Assertions.assertFalse(EbeguUtil.contentEquals("test1", "test"));
		Assertions.assertFalse(EbeguUtil.contentEquals("test", "te st"));
	}

	private Gesuch prepareGesuchForErlaeuterungenZurVerguegungTests(
		MandantIdentifier mandantIdentifier,
		int anspruch
	) {
		Mandant mandant = new Mandant();
		mandant.setMandantIdentifier(mandantIdentifier);
		Fall fall = new Fall();
		fall.setMandant(mandant);
		Dossier dossier = new Dossier();
		dossier.setFall(fall);
		Gesuch gesuch = new Gesuch();
		gesuch.setDossier(dossier);
		gesuch.setStatus(AntragStatus.VERFUEGT);
		Betreuung betreuung = new Betreuung();
		betreuung.setBetreuungsstatus(Betreuungsstatus.VERFUEGT);
		Set<Betreuung> betreuungen = new TreeSet<>();
		betreuungen.add(betreuung);
		final Verfuegung verfuegungPreview = new Verfuegung();
		final VerfuegungZeitabschnitt zeitabschnitt =
			new VerfuegungZeitabschnitt();
		zeitabschnitt.getRelevantBgCalculationResult()
			.setAnspruchspensumProzent(anspruch);
		zeitabschnitt.getRelevantBgCalculationResult()
			.setBetreuungspensumProzent(BigDecimal.valueOf(anspruch));
		verfuegungPreview.setZeitabschnitte(List.of(zeitabschnitt));
		betreuung.setVerfuegung(verfuegungPreview);
		KindContainer kindContainer = new KindContainer();
		kindContainer.setBetreuungen(betreuungen);
		gesuch.addKindContainer(kindContainer);
		return gesuch;
	}

}
