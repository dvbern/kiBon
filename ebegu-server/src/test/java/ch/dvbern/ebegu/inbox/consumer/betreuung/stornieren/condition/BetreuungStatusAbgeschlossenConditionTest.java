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

/**
 * Prüft die Bedingung ob eine Betreuung in einem abgeschlossenen Zustand ist. Dieser Zustant um fasst mehrere
 * eintretbare
 * Zustände, nämlich: {@link Betreuungsstatus#BESTAETIGT}, {@link Betreuungsstatus#NICHT_EINGETRETEN},
 * {@link Betreuungsstatus#GESCHLOSSEN_OHNE_VERFUEGUNG}, {@link Betreuungsstatus#VERFUEGT}
 */
class BetreuungStatusAbgeschlossenConditionTest extends
	EnumTestSupport<Boolean, Betreuungsstatus> {

	/**
	 * Nur für die folgende Zustände evaluiert die Bedingung zu "wahr".
	 */
	private static final List<Betreuungsstatus> STATES_EVALUATING_TO_TRUE =
		Arrays
			.asList(
				Betreuungsstatus.BESTAETIGT,
				Betreuungsstatus.NICHT_EINGETRETEN,
				Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG,
				Betreuungsstatus.VERFUEGT
			);

	private final BetreuungStatusAbgeschlossenCondition condition =
		new BetreuungStatusAbgeschlossenCondition();

	protected BetreuungStatusAbgeschlossenConditionTest() {
		super(Betreuungsstatus.class);
	}

	@Test
	void test_iterateOverAllStates_onlyAllowedStatesEvaluateToTrue() {
		super.test();
	}

	@Override
	public void beforeTest(Betreuungsstatus state) {
		// nothing to do here
	}

	@Override
	public Boolean executeTestFor(Betreuungsstatus state) {

		Betreuung betreuung = createdMock(Betreuung.class);
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
