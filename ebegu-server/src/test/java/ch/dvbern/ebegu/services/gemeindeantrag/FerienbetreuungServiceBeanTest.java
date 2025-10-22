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

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.math.BigDecimal;
import java.util.Optional;

import ch.dvbern.ebegu.einstellung.ApplicationPropertyKey;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngaben;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenAngebot;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungBerechnungen;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungFormularStatus;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.gemeindeantrag.ferienbetreuung.FerienbetreuungAngabenContainerStatusHistoryService;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

@ExtendWith(EasyMockExtension.class)
class FerienbetreuungServiceBeanTest extends
	EasyMockSupport {

	@TestSubject
	private FerienbetreuungServiceBean ferienbetreuungServiceBean;
	@Mock
	private FerienbetreuungAngabenContainer container;
	@Mock
	private Persistence persistence;
	@Mock
	private FerienbetreuungAngaben angabenKorrektur;
	@Mock
	private GemeindeService gemeindeService;
	@Mock
	private ApplicationPropertyService applicationPropertyService;
	@Mock
	private BenutzerService benutzerService;
	@Mock
	private FerienbetreuungAngabenContainerStatusHistoryService statusHistoryService;
	@Mock
	FerienbetreuungBerechnungen ferienbetreuungBerechnungen;

	@BeforeEach
	public void setUp() {
		expect(container.getAngabenKorrektur()).andReturn(angabenKorrektur)
			.atLeastOnce();
		expect(container.getStatus()).andReturn(
			FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON
		).atLeastOnce();
		expect(angabenKorrektur.isReadyForFreigeben()).andReturn(true);
	}

	@Test
	void ferienbetreuungAngabenGeprueft_ZWEITPRUEFUNG_when_Kantonsbeitrag_zu_hoch() {
		GemeindeStammdaten stammdaten = new GemeindeStammdaten();
		stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE);
		Gemeinde gemeinde = new Gemeinde();
		gemeinde.setId("123");
		expect(container.isInZweitpruefung()).andReturn(false);
		expect(container.getGemeinde()).andReturn(gemeinde).atLeastOnce();
		expect(
			gemeindeService.getGemeindeStammdatenByGemeindeId(gemeinde.getId())
		).andReturn(
			Optional.of(
				stammdaten
			)
		);
		expect(
			applicationPropertyService.findApplicationPropertyAsBigDecimal(
				ApplicationPropertyKey.FERIENBETREUUNG_AUTO_ZWEITPRUEFUNG_DE,
				gemeinde
					.getMandant()
			)
		).andReturn(new BigDecimal(100000));
		expect(
			applicationPropertyService.findApplicationPropertyAsBigDecimal(
				ApplicationPropertyKey.FERIENBETREUUNG_ANTEIL_ZWEITPRUEFUNG_DE,
				gemeinde
					.getMandant()
			)
		).andReturn(BigDecimal.ZERO);

		expect(angabenKorrektur.getFerienbetreuungBerechnungen()).andReturn(
			ferienbetreuungBerechnungen
		).anyTimes();
		expect(
			ferienbetreuungBerechnungen.getTotalKantonsbeitrag()
		).andReturn(new BigDecimal(200000)).anyTimes();

		expectResetAllFormular();
		expect(benutzerService.hasMoreThanOneMandantUser()).andReturn(true);

		expect(container.setStatus(FerienbetreuungAngabenStatus.ZWEITPRUEFUNG))
			.andReturn(container);
		statusHistoryService.updateStatusChangeHistory(container);
		expect(
			statusHistoryService.findLastHistoryOfStatus(
				container,
				FerienbetreuungAngabenStatus.GEPRUEFT
			)
		).andReturn(Optional.empty());
		expectLastCall();

		expect(persistence.merge(container)).andReturn(container);

		replayAll();

		ferienbetreuungServiceBean.ferienbetreuungAngabenGeprueft(container);

		verifyAll();
	}

	@Test
	void ferienbetreuungAngabenGeprueft_GEPRUEFT_when_Kantonsbeitrag_zu_hoch_but_only_one_mandant_user() {
		GemeindeStammdaten stammdaten = new GemeindeStammdaten();
		stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE);
		Gemeinde gemeinde = new Gemeinde();
		gemeinde.setId("123");
		expect(container.isInZweitpruefung()).andReturn(false);
		expect(container.getGemeinde()).andReturn(gemeinde).atLeastOnce();
		expect(
			gemeindeService.getGemeindeStammdatenByGemeindeId(gemeinde.getId())
		).andReturn(
			Optional.of(
				stammdaten
			)
		);
		expect(
			applicationPropertyService.findApplicationPropertyAsBigDecimal(
				ApplicationPropertyKey.FERIENBETREUUNG_AUTO_ZWEITPRUEFUNG_DE,
				gemeinde
					.getMandant()
			)
		).andReturn(new BigDecimal(100000));
		expect(
			applicationPropertyService.findApplicationPropertyAsBigDecimal(
				ApplicationPropertyKey.FERIENBETREUUNG_ANTEIL_ZWEITPRUEFUNG_DE,
				gemeinde
					.getMandant()
			)
		).andReturn(BigDecimal.ZERO);

		expect(angabenKorrektur.getFerienbetreuungBerechnungen()).andReturn(
			ferienbetreuungBerechnungen
		).anyTimes();
		expect(
			ferienbetreuungBerechnungen.getTotalKantonsbeitrag()
		).andReturn(new BigDecimal(200000)).anyTimes();

		expect(benutzerService.hasMoreThanOneMandantUser()).andReturn(false);
		statusHistoryService.updateStatusChangeHistory(container);

		expect(container.setStatus(FerienbetreuungAngabenStatus.GEPRUEFT))
			.andReturn(container);
		expect(
			statusHistoryService.findLastHistoryOfStatus(
				container,
				FerienbetreuungAngabenStatus.GEPRUEFT
			)
		).andReturn(Optional.empty());
		expectLastCall();

		expect(persistence.merge(container)).andReturn(container);

		replayAll();

		ferienbetreuungServiceBean.ferienbetreuungAngabenGeprueft(container);

		verifyAll();
	}

	@Test
	void ferienbetreuungAngaben_GEPRUEFT_when_Kantonsbeitrag_zu_klein() {
		GemeindeStammdaten stammdaten = new GemeindeStammdaten();
		stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE);
		Gemeinde gemeinde = new Gemeinde();
		gemeinde.setId("123");
		expect(container.isInZweitpruefung()).andReturn(false);
		expect(container.getGemeinde()).andReturn(gemeinde).atLeastOnce();
		expect(
			gemeindeService.getGemeindeStammdatenByGemeindeId(gemeinde.getId())
		).andReturn(
			Optional.of(
				stammdaten
			)
		);
		expect(
			applicationPropertyService.findApplicationPropertyAsBigDecimal(
				ApplicationPropertyKey.FERIENBETREUUNG_AUTO_ZWEITPRUEFUNG_DE,
				gemeinde
					.getMandant()
			)
		).andReturn(new BigDecimal(100000));
		expect(
			applicationPropertyService.findApplicationPropertyAsBigDecimal(
				ApplicationPropertyKey.FERIENBETREUUNG_ANTEIL_ZWEITPRUEFUNG_DE,
				gemeinde
					.getMandant()
			)
		).andReturn(BigDecimal.ZERO);

		expect(angabenKorrektur.getFerienbetreuungBerechnungen()).andReturn(
			ferienbetreuungBerechnungen
		).anyTimes();
		expect(
			ferienbetreuungBerechnungen.getTotalKantonsbeitrag()
		).andReturn(new BigDecimal(1000)).anyTimes();

		expect(container.setStatus(FerienbetreuungAngabenStatus.GEPRUEFT))
			.andReturn(container);
		statusHistoryService.updateStatusChangeHistory(container);
		expect(
			statusHistoryService.findLastHistoryOfStatus(
				container,
				FerienbetreuungAngabenStatus.GEPRUEFT
			)
		).andReturn(Optional.empty());
		expectLastCall();

		expect(persistence.merge(container)).andReturn(container);

		replayAll();

		ferienbetreuungServiceBean.ferienbetreuungAngabenGeprueft(container);

		verifyAll();
	}

	@Test
	void ferienbetreuungAngaben_GEPRUEFT_when_schon_in_ZWEITPRUEFUNG() {
		expect(container.isInZweitpruefung()).andReturn(true);

		expect(container.setStatus(FerienbetreuungAngabenStatus.GEPRUEFT))
			.andReturn(container);

		expect(persistence.merge(container)).andReturn(container);
		statusHistoryService.updateStatusChangeHistory(container);
		expect(
			statusHistoryService.findLastHistoryOfStatus(
				container,
				FerienbetreuungAngabenStatus.GEPRUEFT
			)
		).andReturn(Optional.empty());

		replayAll();

		ferienbetreuungServiceBean.ferienbetreuungAngabenGeprueft(container);

		verifyAll();
	}

	void expectResetAllFormular() {
		FerienbetreuungAngaben ferienbetreuungAngaben = createMock(
			FerienbetreuungAngaben.class
		);
		expect(container.getAngabenDeklaration()).andReturn(
			ferienbetreuungAngaben
		).anyTimes();
		FerienbetreuungAngabenAngebot ferienbetreuungAngabenAngebot =
			createMock(FerienbetreuungAngabenAngebot.class);
		expect(ferienbetreuungAngaben.getFerienbetreuungAngabenAngebot())
			.andReturn(ferienbetreuungAngabenAngebot);

		ferienbetreuungAngabenAngebot
			.setStatus(
				FerienbetreuungFormularStatus.IN_BEARBEITUNG
			);
		expectLastCall();

		FerienbetreuungAngabenNutzung ferienbetreuungAngabenNutzung =
			createMock(FerienbetreuungAngabenNutzung.class);
		expect(ferienbetreuungAngaben.getFerienbetreuungAngabenNutzung())
			.andReturn(ferienbetreuungAngabenNutzung);

		ferienbetreuungAngabenNutzung
			.setStatus(
				FerienbetreuungFormularStatus.IN_BEARBEITUNG
			);
		expectLastCall();

		FerienbetreuungAngabenStammdaten ferienbetreuungAngabenStammdaten =
			createMock(FerienbetreuungAngabenStammdaten.class);
		expect(ferienbetreuungAngaben.getFerienbetreuungAngabenStammdaten())
			.andReturn(ferienbetreuungAngabenStammdaten);

		ferienbetreuungAngabenStammdaten
			.setStatus(
				FerienbetreuungFormularStatus.IN_BEARBEITUNG
			);

		FerienbetreuungAngabenKostenEinnahmen ferienbetreuungAngabenKostenEinnahmen =
			createMock(FerienbetreuungAngabenKostenEinnahmen.class);
		expect(
			ferienbetreuungAngaben.getFerienbetreuungAngabenKostenEinnahmen()
		).andReturn(ferienbetreuungAngabenKostenEinnahmen);

		ferienbetreuungAngabenKostenEinnahmen
			.setStatus(
				FerienbetreuungFormularStatus.IN_BEARBEITUNG
			);

		FerienbetreuungAngabenAngebot ferienbetreuungAngabenAngebotKorrektur =
			createMock(FerienbetreuungAngabenAngebot.class);
		expect(angabenKorrektur.getFerienbetreuungAngabenAngebot()).andReturn(
			ferienbetreuungAngabenAngebotKorrektur
		);

		ferienbetreuungAngabenAngebotKorrektur
			.setStatus(
				FerienbetreuungFormularStatus.IN_BEARBEITUNG
			);
		expectLastCall();

		FerienbetreuungAngabenNutzung ferienbetreuungAngabenNutzungKorrektur =
			createMock(FerienbetreuungAngabenNutzung.class);
		expect(angabenKorrektur.getFerienbetreuungAngabenNutzung()).andReturn(
			ferienbetreuungAngabenNutzungKorrektur
		);

		ferienbetreuungAngabenNutzungKorrektur
			.setStatus(
				FerienbetreuungFormularStatus.IN_BEARBEITUNG
			);
		expectLastCall();

		FerienbetreuungAngabenStammdaten ferienbetreuungAngabenStammdatenKorrektur =
			createMock(FerienbetreuungAngabenStammdaten.class);
		expect(angabenKorrektur.getFerienbetreuungAngabenStammdaten())
			.andReturn(ferienbetreuungAngabenStammdatenKorrektur);

		ferienbetreuungAngabenStammdatenKorrektur
			.setStatus(
				FerienbetreuungFormularStatus.IN_BEARBEITUNG
			);
		expectLastCall();

		FerienbetreuungAngabenKostenEinnahmen ferienbetreuungAngabenKostenEinnahmenKorrektur =
			createMock(FerienbetreuungAngabenKostenEinnahmen.class);
		expect(angabenKorrektur.getFerienbetreuungAngabenKostenEinnahmen())
			.andReturn(ferienbetreuungAngabenKostenEinnahmenKorrektur);

		ferienbetreuungAngabenKostenEinnahmenKorrektur
			.setStatus(
				FerienbetreuungFormularStatus.IN_BEARBEITUNG
			);
		expectLastCall();
	}
}
