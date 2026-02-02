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

package ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren;

import java.time.LocalDateTime;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungMonitoring;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.inbox.consumer.betreuung.event.BetreuungEvent;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.BetreuungAbweisenAction;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.BetreuungStornierenAction;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.DoNothingAction;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.SendStornierungsInfoEmailAction;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.StornierungsMitteilungErstellenAction;
import ch.dvbern.ebegu.inbox.handler.EventMonitor;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.util.EasyMockTestSupport;
import org.easymock.EasyMock;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;

class BetreuungStornierenDecisionTreeTest extends EasyMockTestSupport {

	@Mock
	private BetreuungAbweisenAction betreuungAbweisenAction;

	@Mock
	private BetreuungStornierenAction betreuungStornierenAction;

	@Mock
	private DoNothingAction doNothingAction;

	@Mock
	private SendStornierungsInfoEmailAction sendStornierungsInfoEmailAction;

	@Mock
	private StornierungsMitteilungErstellenAction stornierungsMitteilungErstellenAction;

	@TestSubject
	private BetreuungStornierenDecisionTree decisionTree;

	@Test
	void evaluate_betreuungStatusWartenUndErstgesuch_BetreuungAbweisenActionIsExecuted() {

		Betreuungsstatus betreuungsstatus = Betreuungsstatus.WARTEN;
		AntragTyp antragTyp = AntragTyp.ERSTGESUCH;

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getTyp()).andReturn(antragTyp);
		EasyMock.expect(gesuch.isMutation()).andReturn(false).anyTimes();

		KindContainer kindContainer = createMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		String vorgaengerId = "abc";
		Betreuung betreuung = createMock(Betreuung.class);
		EasyMock.expect(betreuung.getVorgaengerId())
			.andReturn(vorgaengerId)
			.anyTimes();
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		BetreuungEvent event = createBetruungEventMock(betreuung);

		EasyMock.expect(betreuung.getBetreuungsstatus())
			.andReturn(betreuungsstatus)
			.anyTimes();

		// Erst wird die Betreuung abgewiesen.
		betreuungAbweisenAction.execute(event);
		EasyMock.expectLastCall().once();
		// Dann wird eine Info-Mail geschickt (weil Online-Antrag)
		// Das Senden dieser Mail ist allerdings beim Abweisen einer Betreuung schon in der Service-Methode integriert.
		// In diesem Fall muss also keine weitere Aktion ausgeführt werden.

		replayAll();

		decisionTree.evaluate(event);

		verifyAll();
	}

	@Test
	void evaluate_betreuungStatusBestaetigUndInVerfuegung_StornierungsMitteilungErstellenActionIsExecuted() {

		Betreuungsstatus betreuungsstatus = Betreuungsstatus.BESTAETIGT;
		AntragTyp antragTyp = AntragTyp.ERSTGESUCH;
		AntragStatus antragStatus = AntragStatus.VERFUEGT;

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getTyp()).andReturn(antragTyp);
		EasyMock.expect(gesuch.getStatus()).andReturn(antragStatus).anyTimes();
		EasyMock.expect(gesuch.isMutation()).andReturn(false).anyTimes();

		KindContainer kindContainer = createMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		String vorgaengerId = "abc";
		Betreuung betreuung = createMock(Betreuung.class);
		EasyMock.expect(betreuung.getVorgaengerId())
			.andReturn(vorgaengerId)
			.anyTimes();
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		BetreuungEvent event = createBetruungEventMock(betreuung);

		EasyMock.expect(betreuung.getBetreuungsstatus())
			.andReturn(betreuungsstatus)
			.anyTimes();

		stornierungsMitteilungErstellenAction.execute(event);
		EasyMock.expectLastCall().once();

		replayAll();

		decisionTree.evaluate(event);

		verifyAll();
	}

	@Test
	void evaluate_betreuungStatusBestaetigUndNichtInVerfuegung_BetreuungAbgeweisenActionIsExecuted() {

		Betreuungsstatus betreuungsstatus = Betreuungsstatus.BESTAETIGT;
		AntragTyp antragTyp = AntragTyp.ERSTGESUCH;
		AntragStatus antragStatus = AntragStatus.IN_BEARBEITUNG_JA;
		Eingangsart eingangsart = Eingangsart.PAPIER; // Keine Infomail senden

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getTyp()).andReturn(antragTyp);
		EasyMock.expect(gesuch.getStatus()).andReturn(antragStatus).anyTimes();
		EasyMock.expect(gesuch.getEingangsart())
			.andReturn(eingangsart)
			.anyTimes();
		EasyMock.expect(gesuch.isMutation()).andReturn(false).anyTimes();

		KindContainer kindContainer = createMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		String vorgaengerId = "abc";
		Betreuung betreuung = createMock(Betreuung.class);
		EasyMock.expect(betreuung.getVorgaengerId())
			.andReturn(vorgaengerId)
			.anyTimes();
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		BetreuungEvent event = createBetruungEventMock(betreuung);

		EasyMock.expect(betreuung.getBetreuungsstatus())
			.andReturn(betreuungsstatus)
			.anyTimes();

		betreuungAbweisenAction.execute(event);
		EasyMock.expectLastCall().once();

		replayAll();

		decisionTree.evaluate(event);

		verifyAll();
	}

	@Test
	void evaluate_betreuungStatusBestaetigUndKeinErstantragUndKeineOffeneMutationUndInVerfuegung_StornierungsMitteilungErstellenActionIsExecuted() {

		Betreuungsstatus betreuungsstatus = Betreuungsstatus.BESTAETIGT;
		AntragTyp antragTyp = AntragTyp.MUTATION;
		AntragStatus antragStatus = AntragStatus.VERFUEGT;
		Eingangsart eingangsart = Eingangsart.PAPIER; // Keine Infomail senden
		boolean offeneMutation = false;

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getTyp()).andReturn(antragTyp);
		EasyMock.expect(gesuch.getStatus()).andReturn(antragStatus).anyTimes();
		EasyMock.expect(gesuch.getEingangsart())
			.andReturn(eingangsart)
			.anyTimes();
		EasyMock.expect(gesuch.isMutation())
			.andReturn(offeneMutation)
			.anyTimes();

		KindContainer kindContainer = createMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		String vorgaengerId = "abc";
		Betreuung betreuung = createMock(Betreuung.class);
		EasyMock.expect(betreuung.getVorgaengerId())
			.andReturn(vorgaengerId)
			.anyTimes();
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		BetreuungEvent event = createBetruungEventMock(betreuung);

		EasyMock.expect(betreuung.getBetreuungsstatus())
			.andReturn(betreuungsstatus)
			.anyTimes();

		stornierungsMitteilungErstellenAction.execute(event);
		EasyMock.expectLastCall().once();

		replayAll();

		decisionTree.evaluate(event);

		verifyAll();
	}

	@Test
	void evaluate_betreuungStatusBestaetigUndKeinErstantragUndKeineOffeneMutationUndNichtInVerfuegung_BetreuungAbweisenActionIsExecuted() {

		Betreuungsstatus betreuungsstatus = Betreuungsstatus.BESTAETIGT;
		AntragTyp antragTyp = AntragTyp.MUTATION;
		AntragStatus antragStatus = AntragStatus.IN_BEARBEITUNG_SOZIALDIENST;
		Eingangsart eingangsart = Eingangsart.PAPIER; // Keine Infomail senden
		boolean offeneMutation = false;

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getTyp()).andReturn(antragTyp);
		EasyMock.expect(gesuch.getStatus()).andReturn(antragStatus).anyTimes();
		EasyMock.expect(gesuch.getEingangsart())
			.andReturn(eingangsart)
			.anyTimes();
		EasyMock.expect(gesuch.isMutation())
			.andReturn(offeneMutation)
			.anyTimes();

		KindContainer kindContainer = createMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		String vorgaengerId = "abc";
		Betreuung betreuung = createMock(Betreuung.class);
		EasyMock.expect(betreuung.getVorgaengerId())
			.andReturn(vorgaengerId)
			.anyTimes();
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		BetreuungEvent event = createBetruungEventMock(betreuung);

		EasyMock.expect(betreuung.getBetreuungsstatus())
			.andReturn(betreuungsstatus)
			.anyTimes();

		betreuungAbweisenAction.execute(event);
		EasyMock.expectLastCall().once();

		replayAll();

		decisionTree.evaluate(event);

		verifyAll();
	}

	@Test
	void evaluate_betreuungStatusWartenUndOffeneMutationUndBetreuungNeuErfasst_BetreuungAbweisenActionIsExecuted() {

		Betreuungsstatus betreuungsstatus = Betreuungsstatus.WARTEN;
		AntragTyp antragTyp = AntragTyp.MUTATION;
		AntragStatus antragStatus = AntragStatus.IN_BEARBEITUNG_SOZIALDIENST;
		Eingangsart eingangsart = Eingangsart.PAPIER; // Keine Infomail senden
		boolean offeneMutation = true;

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getTyp()).andReturn(antragTyp);
		EasyMock.expect(gesuch.getStatus()).andReturn(antragStatus).anyTimes();
		EasyMock.expect(gesuch.getEingangsart())
			.andReturn(eingangsart)
			.anyTimes();
		EasyMock.expect(gesuch.isMutation())
			.andReturn(offeneMutation)
			.anyTimes();

		KindContainer kindContainer = createMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		Betreuung betreuung = createMock(Betreuung.class);
		// Keine Vorgänger = Betreuung neu erfasst
		EasyMock.expect(betreuung.getVorgaengerId()).andReturn(null).anyTimes();
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		BetreuungEvent event = createBetruungEventMock(betreuung);

		EasyMock.expect(betreuung.getBetreuungsstatus())
			.andReturn(betreuungsstatus)
			.anyTimes();

		betreuungAbweisenAction.execute(event);
		EasyMock.expectLastCall().once();

		replayAll();

		decisionTree.evaluate(event);

		verifyAll();
	}

	@Test
	void evaluate_betreuungStatusBestoetigtUndOffeneMutationUndBetreuungNeuErfasstUndGesuchNichtInVerfuegung_BetreuungAbweisenActionIsExecuted() {

		Betreuungsstatus betreuungsstatus = Betreuungsstatus.BESTAETIGT;
		AntragTyp antragTyp = AntragTyp.MUTATION;
		AntragStatus antragStatus = AntragStatus.IN_BEARBEITUNG_SOZIALDIENST;
		Eingangsart eingangsart = Eingangsart.PAPIER; // Keine Infomail senden
		boolean offeneMutation = true;

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getTyp()).andReturn(antragTyp);
		EasyMock.expect(gesuch.getStatus()).andReturn(antragStatus).anyTimes();
		EasyMock.expect(gesuch.getEingangsart())
			.andReturn(eingangsart)
			.anyTimes();
		EasyMock.expect(gesuch.isMutation())
			.andReturn(offeneMutation)
			.anyTimes();

		KindContainer kindContainer = createMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		Betreuung betreuung = createMock(Betreuung.class);
		// Keine Vorgänger = Betreuung neu erfasst
		EasyMock.expect(betreuung.getVorgaengerId()).andReturn(null).anyTimes();
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		BetreuungEvent event = createBetruungEventMock(betreuung);

		EasyMock.expect(betreuung.getBetreuungsstatus())
			.andReturn(betreuungsstatus)
			.anyTimes();

		betreuungAbweisenAction.execute(event);
		EasyMock.expectLastCall().once();

		replayAll();

		decisionTree.evaluate(event);

		verifyAll();
	}

	@Test
	void evaluate_betreuungStatusBestaetigtUndOffeneMutationUndBetreuungNeuErfasstUndGesuchInVerfuegung_StornierungsmitteilungErstellenIsExecuted() {

		Betreuungsstatus betreuungsstatus = Betreuungsstatus.BESTAETIGT;
		AntragTyp antragTyp = AntragTyp.MUTATION;
		AntragStatus antragStatus = AntragStatus.VERFUEGEN;
		Eingangsart eingangsart = Eingangsart.PAPIER; // Keine Infomail senden
		boolean offeneMutation = true;

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getTyp()).andReturn(antragTyp);
		EasyMock.expect(gesuch.getStatus()).andReturn(antragStatus).anyTimes();
		EasyMock.expect(gesuch.getEingangsart())
			.andReturn(eingangsart)
			.anyTimes();
		EasyMock.expect(gesuch.isMutation())
			.andReturn(offeneMutation)
			.anyTimes();

		KindContainer kindContainer = createMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		Betreuung betreuung = createMock(Betreuung.class);
		// Keine Vorgänger = Betreuung neu erfasst
		EasyMock.expect(betreuung.getVorgaengerId()).andReturn(null).anyTimes();
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		BetreuungEvent event = createBetruungEventMock(betreuung);

		EasyMock.expect(betreuung.getBetreuungsstatus())
			.andReturn(betreuungsstatus)
			.anyTimes();

		stornierungsMitteilungErstellenAction.execute(event);
		EasyMock.expectLastCall().once();

		replayAll();

		decisionTree.evaluate(event);

		verifyAll();
	}

	@Test
	void evaluate_betreuungStatusAbgewiesenUndOffeneMutationUndBetreuungNeuErfasst_DoNothingActionIsExecuted() {

		Betreuungsstatus betreuungsstatus = Betreuungsstatus.ABGEWIESEN;
		AntragTyp antragTyp = AntragTyp.MUTATION;
		AntragStatus antragStatus = AntragStatus.VERFUEGEN;
		Eingangsart eingangsart = Eingangsart.PAPIER; // Keine Infomail senden
		boolean offeneMutation = true;

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getTyp()).andReturn(antragTyp);
		EasyMock.expect(gesuch.getStatus()).andReturn(antragStatus).anyTimes();
		EasyMock.expect(gesuch.getEingangsart())
			.andReturn(eingangsart)
			.anyTimes();
		EasyMock.expect(gesuch.isMutation())
			.andReturn(offeneMutation)
			.anyTimes();

		KindContainer kindContainer = createMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		Betreuung betreuung = createMock(Betreuung.class);
		// Keine Vorgänger = Betreuung neu erfasst
		EasyMock.expect(betreuung.getVorgaengerId()).andReturn(null).anyTimes();
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		BetreuungEvent event = createBetruungEventMock(betreuung);

		EasyMock.expect(betreuung.getBetreuungsstatus())
			.andReturn(betreuungsstatus)
			.anyTimes();

		// Diese Konstellation darf keine Aktion auslösen (do nothing)
		doNothingAction.execute(event);
		EasyMock.expectLastCall().once();

		replayAll();

		decisionTree.evaluate(event);

		verifyAll();
	}

	@Test
	void evaluate_betreuungStatusWartenUndOffeneMutationUndBetreuungNichtNeuErfasstAndOnline_BetreuungStornierenActionIsExecuted() {

		Betreuungsstatus betreuungsstatus = Betreuungsstatus.WARTEN;
		AntragTyp antragTyp = AntragTyp.MUTATION;
		AntragStatus antragStatus = AntragStatus.IN_BEARBEITUNG_JA;
		Eingangsart eingangsart = Eingangsart.ONLINE;
		boolean offeneMutation = true;

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getTyp()).andReturn(antragTyp);
		EasyMock.expect(gesuch.getStatus()).andReturn(antragStatus).anyTimes();
		EasyMock.expect(gesuch.getEingangsart())
			.andReturn(eingangsart)
			.anyTimes();
		EasyMock.expect(gesuch.isMutation())
			.andReturn(offeneMutation)
			.anyTimes();

		KindContainer kindContainer = createMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		Betreuung betreuung = createMock(Betreuung.class);
		EasyMock.expect(betreuung.getVorgaengerId())
			.andReturn("123")
			.anyTimes();
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		BetreuungEvent event = createBetruungEventMock(betreuung);

		EasyMock.expect(betreuung.getBetreuungsstatus())
			.andReturn(betreuungsstatus)
			.anyTimes();

		betreuungStornierenAction.execute(event);
		EasyMock.expectLastCall().once();

		sendStornierungsInfoEmailAction.execute(event);
		EasyMock.expectLastCall().once();

		replayAll();

		decisionTree.evaluate(event);

		verifyAll();
	}

	@Test
	void evaluate_betreuungStatusWartenUndOffeneMutationUndBetreuungNichtNeuErfasstAndPapier_BetreuungStornierenActionIsExecuted() {

		Betreuungsstatus betreuungsstatus = Betreuungsstatus.WARTEN;
		AntragTyp antragTyp = AntragTyp.MUTATION;
		AntragStatus antragStatus = AntragStatus.IN_BEARBEITUNG_JA;
		Eingangsart eingangsart = Eingangsart.PAPIER;
		boolean offeneMutation = true;

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getTyp()).andReturn(antragTyp);
		EasyMock.expect(gesuch.getStatus()).andReturn(antragStatus).anyTimes();
		EasyMock.expect(gesuch.getEingangsart())
			.andReturn(eingangsart)
			.anyTimes();
		EasyMock.expect(gesuch.isMutation())
			.andReturn(offeneMutation)
			.anyTimes();

		KindContainer kindContainer = createMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		Betreuung betreuung = createMock(Betreuung.class);
		EasyMock.expect(betreuung.getVorgaengerId())
			.andReturn("123")
			.anyTimes();
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		BetreuungEvent event = createBetruungEventMock(betreuung);

		EasyMock.expect(betreuung.getBetreuungsstatus())
			.andReturn(betreuungsstatus)
			.anyTimes();

		betreuungStornierenAction.execute(event);
		EasyMock.expectLastCall().once();

		doNothingAction.execute(event);
		EasyMock.expectLastCall().once();

		replayAll();

		decisionTree.evaluate(event);

		verifyAll();
	}

	@Test
	void evaluate_betreuungStatusAbgeschlossenUndOffeneMutationUndBetreuungNichtNeuErfasst_StornierungsMitteilungErstellenActionIsExecuted() {

		Betreuungsstatus betreuungsstatus = Betreuungsstatus.NICHT_EINGETRETEN;
		AntragTyp antragTyp = AntragTyp.MUTATION;
		AntragStatus antragStatus = AntragStatus.IN_BEARBEITUNG_JA;
		Eingangsart eingangsart = Eingangsart.PAPIER;
		boolean offeneMutation = true;

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getTyp()).andReturn(antragTyp);
		EasyMock.expect(gesuch.getStatus()).andReturn(antragStatus).anyTimes();
		EasyMock.expect(gesuch.getEingangsart())
			.andReturn(eingangsart)
			.anyTimes();
		EasyMock.expect(gesuch.isMutation())
			.andReturn(offeneMutation)
			.anyTimes();

		KindContainer kindContainer = createMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		Betreuung betreuung = createMock(Betreuung.class);
		EasyMock.expect(betreuung.getVorgaengerId())
			.andReturn("123")
			.anyTimes();
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		BetreuungEvent event = createBetruungEventMock(betreuung);

		EasyMock.expect(betreuung.getBetreuungsstatus())
			.andReturn(betreuungsstatus)
			.anyTimes();

		stornierungsMitteilungErstellenAction.execute(event);
		EasyMock.expectLastCall().once();

		replayAll();

		decisionTree.evaluate(event);

		verifyAll();
	}

	@Test
	void evaluate_betreuungStatusSchulamtUndOffeneMutationUndBetreuungNichtNeuErfasst_DoNothingActionIsExecuted() {

		Betreuungsstatus betreuungsstatus =
			Betreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST;
		AntragTyp antragTyp = AntragTyp.MUTATION;
		AntragStatus antragStatus = AntragStatus.IN_BEARBEITUNG_JA;
		Eingangsart eingangsart = Eingangsart.PAPIER;
		boolean offeneMutation = true;

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getTyp()).andReturn(antragTyp);
		EasyMock.expect(gesuch.getStatus()).andReturn(antragStatus).anyTimes();
		EasyMock.expect(gesuch.getEingangsart())
			.andReturn(eingangsart)
			.anyTimes();
		EasyMock.expect(gesuch.isMutation())
			.andReturn(offeneMutation)
			.anyTimes();

		KindContainer kindContainer = createMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		Betreuung betreuung = createMock(Betreuung.class);
		EasyMock.expect(betreuung.getVorgaengerId())
			.andReturn("123")
			.anyTimes();
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		BetreuungEvent event = createBetruungEventMock(betreuung);

		EasyMock.expect(betreuung.getBetreuungsstatus())
			.andReturn(betreuungsstatus)
			.anyTimes();

		doNothingAction.execute(event);
		EasyMock.expectLastCall().once();

		replayAll();

		decisionTree.evaluate(event);

		verifyAll();
	}

	private BetreuungEvent createBetruungEventMock(Betreuung betreuung) {

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

		return event;
	}
}
