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
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.util.EasyMockTestSupport;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OffeneMutationConditionTest extends EasyMockTestSupport {

	private final OffeneMutationCondition condition =
		new OffeneMutationCondition();

	@Test
	void test_gesuchIsOffeneMutationEvaluatesToTrue() {

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.isMutation()).andReturn(true);
		KindContainer kindContainer = createdMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		Betreuung betreuung = createdMock(Betreuung.class);
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		replayAll();

		boolean result = condition.test(betreuung);

		verifyAll();

		Assertions.assertTrue(result);
	}

	@Test
	void test_gesuchIsNotOffeneMutationEvaluatesToFalse() {

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.isMutation()).andReturn(false);
		KindContainer kindContainer = createdMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		Betreuung betreuung = createdMock(Betreuung.class);
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		replayAll();

		boolean result = condition.test(betreuung);

		verifyAll();

		Assertions.assertFalse(result);
	}
}
