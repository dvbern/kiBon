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
import ch.dvbern.ebegu.inbox.consumer.betreuung.event.BetreuungEvent;
import ch.dvbern.ebegu.inbox.handler.EventMonitor;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.MailService;
import ch.dvbern.ebegu.util.EasyMockTestSupport;
import org.easymock.EasyMock;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;

class SendStornierungsInfoEmailActionTest extends EasyMockTestSupport {

	@Mock
	private MailService mailService;

	@TestSubject
	private SendStornierungsInfoEmailAction action =
		new SendStornierungsInfoEmailAction();

	@Test
	void execute_EmailSentWithCorrectParameters() {

		String clientName = "clientName";
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
			"ref-nr",
			clientName
		);

		Betreuung betreuung = createMock(Betreuung.class);
		BetreuungEvent event = createMock(BetreuungEvent.class);
		EasyMock.expect(event.getBetreuung()).andReturn(betreuung).anyTimes();
		EasyMock.expect(event.getEventMonitor())
			.andReturn(eventMonitor)
			.anyTimes();

		// Wir müssen hier nur verifizieren, dass die Service-Methode richtig aufgerufen wird.
		mailService.sendInfoBetreuungAbgelehnt(betreuung);
		EasyMock.expectLastCall().once();

		replayAll();

		// Test ausführen
		action.execute(event);

		verifyAll();
	}
}
