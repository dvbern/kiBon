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

import java.util.Arrays;
import java.util.List;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.util.EnumTestSupport;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ErstantragConditionTest extends EnumTestSupport<Boolean, AntragTyp> {

	private static final List<AntragTyp> STATES_EVALUATING_TO_TRUE = Arrays
		.asList(
			AntragTyp.ERSTGESUCH,
			AntragTyp.ERNEUERUNGSGESUCH
		);

	private ErstantragCondition condition = new ErstantragCondition();

	protected ErstantragConditionTest() {
		super(AntragTyp.class);
	}

	@Test
	void test_gesuchIsNotAMutation_EvaluatesToTrue() {
		super.test();
	}

	@Override
	public void beforeTest(AntragTyp state) {
		// no beforw actions needed
	}

	@Override
	public Boolean executeTestFor(AntragTyp state) {

		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getTyp()).andReturn(state);
		KindContainer kindContainer = createdMock(KindContainer.class);
		EasyMock.expect(kindContainer.getGesuch()).andReturn(gesuch).anyTimes();
		Betreuung betreuung = createdMock(Betreuung.class);
		EasyMock.expect(betreuung.getKind())
			.andReturn(kindContainer)
			.anyTimes();

		replayAll();

		return condition.test(betreuung);
	}

	@Override
	public void assertFor(AntragTyp state, Boolean testResult) {

		verifyAll();

		// Assert true, wenn der aktuelle Status für die hier zu testende Bedingung mit "wahr" evalUiert, sonst assert false..
		if (STATES_EVALUATING_TO_TRUE.contains(state)) {
			Assertions.assertTrue(testResult);
		} else {
			Assertions.assertFalse(testResult);
		}
	}

	@Override
	public void afterTest(AntragTyp state) {
		resetAll();
	}
}
