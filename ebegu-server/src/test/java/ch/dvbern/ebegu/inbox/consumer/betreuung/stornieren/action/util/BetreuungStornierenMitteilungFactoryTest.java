/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.inbox.services.BetreuungEventHelper;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.util.EasyMockTestSupport;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.easymock.EasyMock;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BetreuungStornierenMitteilungFactoryTest extends EasyMockTestSupport {

	@TestSubject
	private final BetreuungStornierenMitteilungFactory factory =
		new BetreuungStornierenMitteilungFactory();

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private GemeindeService gemeindeService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private BetreuungEventHelper betreuungEventHelper;

	@Test
	void createBetreuungsStornierenMitteilung_BetreuungsMitteilungCorrectlyCreated() {

		String refNr = "refNr";
		String gemeindeId = "abc";
		Long bfsNr = 123L;

		MandantIdentifier mandantIdentifier = MandantIdentifier.BERN;
		Mandant mandant = createMock(Mandant.class);
		EasyMock.expect(mandant.getMandantIdentifier())
			.andReturn(mandantIdentifier)
			.anyTimes();

		Gemeinde gemeinde = createMock(Gemeinde.class);
		EasyMock.expect(gemeinde.getId()).andReturn(gemeindeId).anyTimes();
		EasyMock.expect(gemeinde.getMandant()).andReturn(mandant).anyTimes();
		EasyMock.expect(gemeinde.getBfsNummer()).andReturn(bfsNr).anyTimes();

		Benutzer empfaenger = createMock(Benutzer.class);
		Fall fall = createMock(Fall.class);
		EasyMock.expect(fall.getBesitzer()).andReturn(empfaenger).anyTimes();

		Dossier dossier = createMock(Dossier.class);
		EasyMock.expect(dossier.getGemeinde()).andReturn(gemeinde).anyTimes();
		EasyMock.expect(dossier.getFall()).andReturn(fall).anyTimes();

		KorrespondenzSpracheTyp korrespondenzSpracheTyp =
			KorrespondenzSpracheTyp.DE;

		GemeindeStammdaten gemeindeStammdaten = createMock(
			GemeindeStammdaten.class
		);
		EasyMock.expect(gemeindeStammdaten.getKorrespondenzsprache())
			.andReturn(korrespondenzSpracheTyp)
			.anyTimes();

		Optional<GemeindeStammdaten> gemeindeStammdatenOpt = Optional.of(
			gemeindeStammdaten
		);

		EasyMock.expect(
			gemeindeService.getGemeindeStammdatenByGemeindeId(gemeindeId)
		)
			.andReturn(gemeindeStammdatenOpt)
			.anyTimes();

		Gesuchsteller gesuchsteller = createMock(Gesuchsteller.class);
		EasyMock.expect(gesuchsteller.getKorrespondenzSprache())
			.andReturn(Sprache.DEUTSCH);
		GesuchstellerContainer gesuchstellerContainer1 = createMock(
			GesuchstellerContainer.class
		);
		EasyMock.expect(gesuchstellerContainer1.getGesuchstellerJA())
			.andReturn(gesuchsteller)
			.anyTimes();

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getDossier()).andReturn(dossier).anyTimes();
		EasyMock.expect(gesuch.getGesuchsteller1())
			.andReturn(gesuchstellerContainer1)
			.anyTimes();
		EasyMock.expect(gesuch.extractGemeinde())
			.andReturn(gemeinde)
			.anyTimes();

		LocalDate datumBestaetigung = LocalDate.now();
		Betreuung betreuung = createMock(Betreuung.class);
		EasyMock.expect(betreuung.getDatumBestaetigung())
			.andReturn(datumBestaetigung)
			.anyTimes();
		EasyMock.expect(betreuung.extractGemeinde())
			.andReturn(gemeinde)
			.anyTimes();
		EasyMock.expect(betreuung.extractGesuch()).andReturn(gesuch).anyTimes();
		EasyMock.expect(betreuung.getReferenzNummer())
			.andReturn(refNr)
			.anyTimes();

		Benutzer sender = createMock(Benutzer.class);
		EasyMock.expect(
			betreuungEventHelper.getMutationsmeldungBenutzer(betreuung)
		).andReturn(sender).anyTimes();

		BigDecimal monatlicheHaputmahlzeiten1 = BigDecimal.TEN;
		BigDecimal monatlicheNebenmahlzeiten1 = BigDecimal.valueOf(9);
		BigDecimal kostenProHauptmahlzeit1 = BigDecimal.valueOf(12);
		BigDecimal kostenProNebenmahlzeit1 = BigDecimal.valueOf(11);

		BigDecimal monatlicheHaputmahlzeiten2 = BigDecimal.valueOf(5);
		BigDecimal monatlicheNebenmahlzeiten2 = BigDecimal.valueOf(4);
		BigDecimal kostenProHauptmahlzeit2 = BigDecimal.valueOf(14);
		BigDecimal kostenProNebenmahlzeit2 = BigDecimal.valueOf(13);

		Betreuungspensum pensum1 = createMock(Betreuungspensum.class);
		EasyMock.expect(pensum1.getMonatlicheHauptmahlzeiten())
			.andReturn(monatlicheHaputmahlzeiten1)
			.anyTimes();
		EasyMock.expect(pensum1.getMonatlicheNebenmahlzeiten())
			.andReturn(monatlicheNebenmahlzeiten1)
			.anyTimes();
		EasyMock.expect(pensum1.getTarifProHauptmahlzeit())
			.andReturn(kostenProHauptmahlzeit1)
			.anyTimes();
		EasyMock.expect(pensum1.getTarifProNebenmahlzeit())
			.andReturn(kostenProNebenmahlzeit1)
			.anyTimes();
		pensum1.copyAbstractBetreuungspensumMahlzeitenEntity(
			EasyMock.anyObject(BetreuungsmitteilungPensum.class),
			EasyMock.anyObject(AntragCopyType.class)
		);
		EasyMock.expectLastCall().once();

		BetreuungspensumContainer pensumContainer1 = createMock(
			BetreuungspensumContainer.class
		);
		EasyMock.expect(pensumContainer1.getBetreuungspensumJA())
			.andReturn(pensum1)
			.anyTimes();

		Betreuungspensum pensum2 = createMock(Betreuungspensum.class);
		EasyMock.expect(pensum2.getMonatlicheHauptmahlzeiten())
			.andReturn(monatlicheHaputmahlzeiten2);
		EasyMock.expect(pensum2.getMonatlicheNebenmahlzeiten())
			.andReturn(monatlicheNebenmahlzeiten2);
		EasyMock.expect(pensum2.getTarifProHauptmahlzeit())
			.andReturn(kostenProHauptmahlzeit2)
			.anyTimes();
		EasyMock.expect(pensum2.getTarifProNebenmahlzeit())
			.andReturn(kostenProNebenmahlzeit2)
			.anyTimes();
		BetreuungspensumContainer pensumContainer2 = createMock(
			BetreuungspensumContainer.class
		);
		EasyMock.expect(pensumContainer2.getBetreuungspensumJA())
			.andReturn(pensum2)
			.anyTimes();
		pensum2.copyAbstractBetreuungspensumMahlzeitenEntity(
			EasyMock.anyObject(BetreuungsmitteilungPensum.class),
			EasyMock.anyObject(AntragCopyType.class)
		);
		EasyMock.expectLastCall().once();

		Set<BetreuungspensumContainer> pensumContainers = new HashSet<>(
			Arrays.asList(pensumContainer1, pensumContainer2)
		);
		EasyMock.expect(betreuung.getBetreuungspensumContainers())
			.andReturn(pensumContainers)
			.anyTimes();

		replayAll();

		Betreuungsmitteilung mitteilung = factory
			.createBetreuungsStornierenMitteilung(betreuung);

		Assertions.assertEquals(empfaenger, mitteilung.getEmpfaenger());
		Assertions.assertEquals(sender, mitteilung.getSender());
		Assertions.assertEquals(betreuung, mitteilung.getBetreuung());
		Assertions.assertTrue(mitteilung.isBetreuungStornieren());
		Assertions.assertFalse(mitteilung.isApplied());
		Assertions.assertEquals(2, mitteilung.getBetreuungenJA().size());
		// TODO KIBON-3569: Müssen wir hier evtl. noch unterscheiden zwischen Ablehnung und Stornierung?
	}

}
