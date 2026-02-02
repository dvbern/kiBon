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

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungMonitoring;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.inbox.consumer.betreuung.event.BetreuungEvent;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.util.BetreuungStornierenMitteilungFactory;
import ch.dvbern.ebegu.inbox.handler.EventMonitor;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.util.EasyMockTestSupport;
import org.easymock.EasyMock;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;

class StornierungsMitteilungErstellenActionTest extends EasyMockTestSupport {

	@Mock
	private MitteilungService mitteilungService;

	@Mock
	private BetreuungStornierenMitteilungFactory stornierenMitteilungFactory;

	@TestSubject
	StornierungsMitteilungErstellenAction action =
		new StornierungsMitteilungErstellenAction();

	@Test
	void execute_StornierungsMitteilungIsCreatedWithCorrectParameters() {

		String clientName = "clientName";
		String refNr = "refNr";
		BetreuungMonitoring betreuungMonitoring = EasyMock.createMock(
			BetreuungMonitoring.class
		);
		BetreuungMonitoringService betreuungMonitoringService = createMock(
			BetreuungMonitoringService.class
		);
		EasyMock.expect(
			betreuungMonitoringService.saveBetreuungMonitoring(
				EasyMock.anyObject(BetreuungMonitoring.class)
			)
		)
			.andReturn(betreuungMonitoring)
			.anyTimes();
		// EventMonitor lässt sich nicht mocken
		EventMonitor eventMonitor = new EventMonitor(
			betreuungMonitoringService,
			LocalDateTime.now(),
			refNr,
			clientName
		);

		Betreuung betreuung = createMock(Betreuung.class);
		EasyMock.expect(betreuung.getReferenzNummer())
			.andReturn(refNr)
			.anyTimes();

		BetreuungEvent event = createMock(BetreuungEvent.class);
		EasyMock.expect(event.getBetreuung()).andReturn(betreuung).anyTimes();
		EasyMock.expect(event.getEventMonitor())
			.andReturn(eventMonitor)
			.anyTimes();

		Betreuungsmitteilung betreuungsmitteilung = createMock(
			Betreuungsmitteilung.class
		);
		EasyMock.expect(
			stornierenMitteilungFactory.createBetreuungsStornierenMitteilung(
				betreuung
			)
		)
			.andReturn(betreuungsmitteilung)
			.anyTimes();

		// Dies ist der eigentliche Test: Der Mitteilung-Service muss mit der zuvor erstellen Mitteilung aufgerufen werden.
		mitteilungService
			.replaceOffeneBetreungsmitteilungenWithSameReferenzNummer(
				betreuungsmitteilung,
				refNr
			);
		EasyMock.expectLastCall().once();

		replayAll();

		// Test ausführen
		action.execute(event);

		// Assertions
		verifyAll();
	}
}
