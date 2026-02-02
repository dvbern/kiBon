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
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.util.EnumTestSupport;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BetreuungStatusWartenConditionTest extends
	EnumTestSupport<Boolean, Betreuungsstatus> {

	/**
	 * Nur für die folgende Zustände evaluieren die Bedingung zu "wahr".
	 */
	private static final List<Betreuungsstatus> STATES_EVALUATING_TO_TRUE =
		Arrays.asList(
			Betreuungsstatus.WARTEN
		);

	/**
	 * Die hier zu prüfende Bedingung.
	 */
	private final BetreuungStatusWartenCondition condition =
		new BetreuungStatusWartenCondition();

	public BetreuungStatusWartenConditionTest() {
		super(Betreuungsstatus.class);
	}

	@Test
	void test_iterateOverAllStates_onlyAllowedStatesEvaluateToTrue() {
		super.test();
	}

	@Override
	public void beforeTest(Betreuungsstatus state) {
		// no before actions here
	}

	@Override
	public Boolean executeTestFor(Betreuungsstatus state) {

		Betreuung betreuung = createMock(Betreuung.class);
		EasyMock.expect(betreuung.getBetreuungsstatus())
			.andReturn(state)
			.anyTimes();
		replayAll();

		return condition.test(betreuung);
	}

	@Override
	public void assertFor(Betreuungsstatus state, Boolean testResult) {
		verifyAll();
		// Assert true, wenn der aktuelle Status für die hier zu testende Bedingung mit "wahr" evalUiert, sonst assert false.
		if (STATES_EVALUATING_TO_TRUE.contains(state)) {
			Assertions.assertTrue(testResult);
		} else {
			Assertions.assertFalse(testResult);
		}
	}

	@Override
	public void afterTest(Betreuungsstatus state) {
		resetAll();
	}
}
