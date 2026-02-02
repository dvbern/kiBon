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
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.util.EnumTestSupport;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OnlineGesuchConditionTest extends EnumTestSupport<Boolean, Eingangsart> {

	private final List<Eingangsart> STATES_EVALUATING_TO_TRUE = Arrays.asList(
		Eingangsart.ONLINE
	);

	private final OnlineGesuchCondition condition = new OnlineGesuchCondition();

	protected OnlineGesuchConditionTest() {
		super(Eingangsart.class);
	}

	@Test
	void test_isOnlineGesuch_EvaluatesToTrue() {
		super.test();
	}

	@Override
	public void beforeTest(Eingangsart state) {
		// no before actions to run
	}

	@Override
	public Boolean executeTestFor(Eingangsart state) {
		Gesuch gesuch = createMock(Gesuch.class);
		EasyMock.expect(gesuch.getEingangsart()).andReturn(state);
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
	public void assertFor(Eingangsart state, Boolean testResult) {

		verifyAll();

		// Assert true, wenn der aktuelle Status für die hier zu testende Bedingung mit "wahr" evalUiert, sonst assert false..
		if (STATES_EVALUATING_TO_TRUE.contains(state)) {
			Assertions.assertTrue(testResult);
		} else {
			Assertions.assertFalse(testResult);
		}
	}

	@Override
	public void afterTest(Eingangsart state) {
		resetAll();
	}
}
