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

import java.util.function.Predicate;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.inbox.consumer.betreuung.event.BetreuungEvent;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BetreuungDecisionNodeTest {

	@Test
	void test_predicateValidatesToTrue_firstNodeIsNext() {

		IMocksControl control = EasyMock.createStrictControl();

		Betreuung betreuung = control.createMock(Betreuung.class);
		BetreuungEvent event = control.createMock(BetreuungEvent.class);
		EasyMock.expect(event.getBetreuung()).andReturn(betreuung).anyTimes();

		Predicate<Betreuung> condition = control.createMock(Predicate.class);
		// Die Bedingung evaluiert zu wahr -> der erste Knpten wird besucht
		EasyMock.expect(condition.test(betreuung)).andReturn(true);

		DecisionNode<BetreuungEvent> nodeWhenTrue = control.createMock(
			DecisionNode.class
		);
		DecisionNode<BetreuungEvent> nodeWhenFalse = control.createMock(
			DecisionNode.class
		);

		// prüfen, dass Knoten 1 aufgerufen wurde.
		nodeWhenTrue.evaluate(event);
		EasyMock.expectLastCall().once();

		// Würde Knoten 2 auch noch aufgerufen werden, würde der Test failen (da nicht expected)
		control.replay();

		BetreuungDecisionNode betreuungDecisionNode = new BetreuungDecisionNode(
			condition,
			nodeWhenTrue,
			nodeWhenFalse
		);
		betreuungDecisionNode.evaluate(event);

		control.verify();
	}

	@Test
	void test_predicateValidatesToFalse_secondNodeIsNext() {

		IMocksControl control = EasyMock.createStrictControl();

		Betreuung betreuung = control.createMock(Betreuung.class);
		BetreuungEvent event = control.createMock(BetreuungEvent.class);
		EasyMock.expect(event.getBetreuung()).andReturn(betreuung).anyTimes();

		Predicate<Betreuung> condition = control.createMock(Predicate.class);
		// Die Bedingung evaluiert zu falsch -> der zweite Knpten wird besucht
		EasyMock.expect(condition.test(betreuung)).andReturn(false);

		DecisionNode<BetreuungEvent> nodeWhenTrue = control.createMock(
			DecisionNode.class
		);
		DecisionNode<BetreuungEvent> nodeWhenFalse = control.createMock(
			DecisionNode.class
		);

		// prüfen, dass Knoten 2 aufgerufen wurde.
		nodeWhenFalse.evaluate(event);
		EasyMock.expectLastCall().once();

		// Würde Knoten 2 auch noch aufgerufen werden, würde der Test failen (da nicht expected)
		control.replay();

		BetreuungDecisionNode betreuungDecisionNode = new BetreuungDecisionNode(
			condition,
			nodeWhenTrue,
			nodeWhenFalse
		);
		betreuungDecisionNode.evaluate(event);

		control.verify();
	}
}
