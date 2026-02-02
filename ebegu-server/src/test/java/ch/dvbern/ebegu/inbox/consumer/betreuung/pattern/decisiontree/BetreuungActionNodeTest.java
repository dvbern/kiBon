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

package ch.dvbern.ebegu.inbox.consumer.betreuung.pattern.decisiontree;

import ch.dvbern.ebegu.inbox.consumer.betreuung.event.BetreuungEvent;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.jupiter.api.Test;

class BetreuungActionNodeTest {

	@Test
	void test_hasSuccessor_firstTheActionIsExecutedSecondTheSuccessorIsExecuted() {

		IMocksControl control = EasyMock.createStrictControl();

		BetreuungEvent event = control.createMock(BetreuungEvent.class);
		Action<BetreuungEvent> action = control.createMock(Action.class);
		BetreuungDecisionNode decisionNode = control.createMock(
			BetreuungDecisionNode.class
		);

		// Die folgenden Zeilen bilden den eigentlichen Test:
		// zuerst muss die Aktion ausgeführt werden.
		// Danach muss der Entscheidungsknoten aufgerufen werden.
		action.execute(event);
		EasyMock.expectLastCall().once();
		decisionNode.evaluate(event);
		EasyMock.expectLastCall().once();

		control.replay();

		BetreuungActionNode actionNode = new BetreuungActionNode(
			action,
			decisionNode
		);
		actionNode.evaluate(event);

		control.verify();
	}
}
