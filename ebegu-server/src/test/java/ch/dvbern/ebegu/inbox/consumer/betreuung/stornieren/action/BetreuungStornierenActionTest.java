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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungMonitoring;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.inbox.consumer.betreuung.event.BetreuungEvent;
import ch.dvbern.ebegu.inbox.handler.EventMonitor;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.util.datetime.DateTimeUtils;
import ch.dvbern.ebegu.util.EasyMockTestSupport;
import org.easymock.EasyMock;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BetreuungStornierenActionTest extends EasyMockTestSupport {

	@Mock
	private DateTimeUtils dateTimeUtils;

	@Mock
	private BetreuungService betreuungService;

	@TestSubject
	private BetreuungStornierenAction action = new BetreuungStornierenAction();

	@Test
	void execute_betreuungIsStorniertWithCorrectParameters() {

		// Betreuung vorparametrieren - diese Werte werden mindestens bei einer Stornierung überschrieben
		Betreuungspensum bp1 = new Betreuungspensum();
		bp1.setNichtEingetreten(false);
		bp1.setPensum(BigDecimal.TEN);

		Betreuungspensum bp2 = new Betreuungspensum();
		bp2.setNichtEingetreten(false);
		bp2.setPensum(BigDecimal.TEN);

		BetreuungspensumContainer bpc1 = new BetreuungspensumContainer();
		bpc1.setBetreuungspensumJA(bp1);
		BetreuungspensumContainer bpc2 = new BetreuungspensumContainer();
		bpc2.setBetreuungspensumJA(bp2);

		Betreuung betreuung = new Betreuung();
		betreuung.setDatumBestaetigung(LocalDate.of(2019, 1, 1));
		betreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
		betreuung.setBetreuungspensumContainers(
			new HashSet<>(Arrays.asList(bpc1, bpc2))
		);

		// Dependencies mocken
		LocalDate now = LocalDate.of(2020, 1, 1);
		EasyMock.expect(dateTimeUtils.now())
			.andReturn(now.atStartOfDay())
			.anyTimes();

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

		BetreuungEvent event = createMock(BetreuungEvent.class);
		EasyMock.expect(event.getBetreuung()).andReturn(betreuung).anyTimes();
		EasyMock.expect(event.getEventMonitor())
			.andReturn(eventMonitor)
			.anyTimes();

		// prüfen, dass die Service-Methode mit den richtigen Parametern aufgerufen wird
		EasyMock.expect(
			betreuungService.saveBetreuung(betreuung, false, clientName)
		)
			.andReturn(betreuung)
			.times(1);

		replayAll();

		// Test ausführen
		action.execute(event);

		// Assertions
		verifyAll();

		// sicherstellen, dass die Betreuung richtig parametriert wurde
		Assertions.assertEquals(
			LocalDate.now(),
			betreuung.getDatumBestaetigung()
		);
		Assertions.assertEquals(
			Betreuungsstatus.STORNIERT,
			betreuung.getBetreuungsstatus()
		);
		Assertions.assertTrue(bp1.getNichtEingetreten());
		Assertions.assertEquals(BigDecimal.ZERO, bp1.getPensum());
		Assertions.assertTrue(bp2.getNichtEingetreten());
		Assertions.assertEquals(BigDecimal.ZERO, bp2.getPensum());
	}
}
