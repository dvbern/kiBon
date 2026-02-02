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

package ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.condition;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.util.EasyMockTestSupport;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BetreuungNeuErfasstConditionTest extends EasyMockTestSupport {

	private final BetreuungNeuErfasstCondition condition =
		new BetreuungNeuErfasstCondition();

	@Test
	void test_evaluatesToTrue() {

		Betreuung betreuung = createMock(Betreuung.class);
		EasyMock.expect(betreuung.getVorgaengerId()).andReturn(null).anyTimes();

		replayAll();

		boolean neuErfasst = condition.test(betreuung);

		verifyAll();
		Assertions.assertTrue(neuErfasst);
	}

	@Test
	void test_evaluatesToFalse() {

		Betreuung betreuung = createMock(Betreuung.class);
		EasyMock.expect(betreuung.getVorgaengerId())
			.andReturn("123")
			.anyTimes();

		replayAll();

		boolean neuErfasst = condition.test(betreuung);

		verifyAll();
		Assertions.assertFalse(neuErfasst);
	}
}
