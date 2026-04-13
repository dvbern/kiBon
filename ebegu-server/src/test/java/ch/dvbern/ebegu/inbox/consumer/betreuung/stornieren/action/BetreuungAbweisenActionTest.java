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

package ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.inbox.consumer.betreuung.event.BetreuungEvent;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.util.TextMessageFactory;
import ch.dvbern.ebegu.inbox.handler.EventMonitor;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.util.EasyMockTestSupport;
import org.easymock.EasyMock;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;

class BetreuungAbweisenActionTest extends EasyMockTestSupport {

	@Mock
	private BetreuungService betreuungService;

	@Mock
	private TextMessageFactory messageFactory;

	@Mock
	private GemeindeService gemeindeService;

	@TestSubject
	private BetreuungAbweisenAction action = new BetreuungAbweisenAction();

	@Test
	void execute_betreuungIsAbgewiesenWithCorrectParameters() {

		String clientName = "clientName";
		// EventMonitor lässt sich nicht mocken
		EventMonitor eventMonitor = new EventMonitor(
			createMock(BetreuungMonitoringService.class),
			LocalDateTime.now(),
			"ref-nr",
			clientName
		);

		Betreuung betreuung = createMock(Betreuung.class);
		Gesuch gesuch = createMock(Gesuch.class);
		Dossier dossier = createMock(Dossier.class);
		Gemeinde gemeinde = createMock(Gemeinde.class);
		GemeindeStammdaten gemeindeStammdaten = createMock(
			GemeindeStammdaten.class
		);
		GesuchstellerContainer gs1Container = createMock(
			GesuchstellerContainer.class
		);
		Gesuchsteller gs1 = createMock(Gesuchsteller.class);

		BetreuungEvent event = createMock(BetreuungEvent.class);
		EasyMock.expect(event.getBetreuung()).andReturn(betreuung).anyTimes();
		EasyMock.expect(betreuung.extractGesuch()).andReturn(gesuch);
		EasyMock.expect(gesuch.getDossier()).andReturn(dossier);
		EasyMock.expect(dossier.getGemeinde()).andReturn(gemeinde);
		EasyMock.expect(gemeinde.getId()).andReturn("");
		EasyMock.expect(gemeindeService.getGemeindeStammdatenByGemeindeId(""))
			.andReturn(Optional.ofNullable(gemeindeStammdaten));
		EasyMock.expect(gemeindeStammdaten.getKorrespondenzsprache())
			.andReturn(KorrespondenzSpracheTyp.DE);
		EasyMock.expect(gesuch.getGesuchsteller1())
			.andReturn(gs1Container)
			.times(2);
		EasyMock.expect(gs1Container.getGesuchstellerJA()).andReturn(gs1);
		EasyMock.expect(gs1.getKorrespondenzSprache())
			.andReturn(Sprache.DEUTSCH);
		EasyMock.expect(event.getEventMonitor())
			.andReturn(eventMonitor)
			.anyTimes();

		String begruendung = "Begründung für Abweisung.";
		EasyMock.expect(messageFactory.getMessage(new Locale("de", "CH")))
			.andReturn(begruendung)
			.anyTimes();

		// prüfen, dass die Service-Methode mit den richtigen Parametern aufgerufen wird
		EasyMock.expect(
			betreuungService.betreuungPlatzAbweisen(
				betreuung,
				clientName,
				begruendung
			)
		)
			.andReturn(betreuung)
			.times(1);

		replayAll();

		action.execute(event);

		verifyAll();
	}
}
