/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.util.Locale;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EasyMockExtension.class)
class AbstractErwerbspensumAbschnittRuleTest extends EasyMockSupport {

	@TestSubject
	private AbstractErwerbspensumAbschnittRule erwerbspensumAsivAbschnittRule =
		new ErwerbspensumAsivAbschnittRule(
			Constants.DEFAULT_GUELTIGKEIT,
			1,
			Locale.GERMAN
		);

	@Mock
	private Gesuch gesuch;

	@Mock
	private DateRange gueltigkeit;

	@Mock
	private Familiensituation familiensituationErstgesuch;

	@Mock
	private Familiensituation familiensituation;

	@Test
	void testFamiliensituationTransition1GSTo2GS() {
		LocalDate stichtag = LocalDate.of(2024, 2, 1);
		LocalDate gueltigAb = LocalDate.of(2023, 12, 1);
		LocalDate gueltigBis = LocalDate.of(2024, 12, 31);

		sharedExpectation(gueltigAb, gueltigBis);

		EasyMock.expect(
			familiensituationErstgesuch.hasSecondGesuchsteller(gueltigBis)
		).andReturn(false);

		EasyMock.expect(familiensituation.hasSecondGesuchsteller(gueltigBis))
			.andReturn(true);

		gueltigkeit.setGueltigAb(stichtag);

		EasyMock.expectLastCall();

		replayAll();

		erwerbspensumAsivAbschnittRule.getGueltigkeitFromFamiliensituation(
			gesuch,
			gueltigkeit,
			familiensituationErstgesuch,
			familiensituation
		);

		verifyAll();
	}

	@Test
	void testFamiliensituationTransition2GSTo1GS() {
		LocalDate stichtag = LocalDate.of(2024, 2, 1);
		LocalDate gueltigAb = LocalDate.of(2023, 12, 1);
		LocalDate gueltigBis = LocalDate.of(2024, 12, 31);

		sharedExpectation(gueltigAb, gueltigBis);

		EasyMock.expect(
			familiensituationErstgesuch.hasSecondGesuchsteller(gueltigBis)
		).andReturn(true).times(2);

		EasyMock.expect(familiensituationErstgesuch.getUnterhaltsvereinbarung())
			.andReturn(null);

		EasyMock.expect(familiensituation.hasSecondGesuchsteller(gueltigBis))
			.andReturn(false);

		gueltigkeit.setGueltigBis(stichtag.minusDays(1));

		EasyMock.expectLastCall();

		replayAll();

		erwerbspensumAsivAbschnittRule.getGueltigkeitFromFamiliensituation(
			gesuch,
			gueltigkeit,
			familiensituationErstgesuch,
			familiensituation
		);

		verifyAll();
	}

	@Test
	void testFamiliensituation1GSWithNoChange() {
		LocalDate gueltigAb = LocalDate.of(2024, 1, 1);
		LocalDate gueltigBis = LocalDate.of(2024, 12, 31);

		sharedExpectation(gueltigAb, gueltigBis);

		EasyMock.expect(
			familiensituationErstgesuch.hasSecondGesuchsteller(gueltigBis)
		).andReturn(false).times(2);

		EasyMock.expect(familiensituation.hasSecondGesuchsteller(gueltigBis))
			.andReturn(false);

		replayAll();

		erwerbspensumAsivAbschnittRule.getGueltigkeitFromFamiliensituation(
			gesuch,
			gueltigkeit,
			familiensituationErstgesuch,
			familiensituation
		);

		verifyAll();
	}

	@Test
	void testFamiliensituation2GSWithNoChange() {
		LocalDate gueltigAb = LocalDate.of(2024, 1, 1);
		LocalDate gueltigBis = LocalDate.of(2024, 12, 31);

		sharedExpectation(gueltigAb, gueltigBis);

		EasyMock.expect(
			familiensituationErstgesuch.hasSecondGesuchsteller(gueltigBis)
		).andReturn(true).times(2);

		EasyMock.expect(familiensituation.hasSecondGesuchsteller(gueltigBis))
			.andReturn(true);

		EasyMock.expect(familiensituationErstgesuch.getUnterhaltsvereinbarung())
			.andReturn(null);

		replayAll();

		erwerbspensumAsivAbschnittRule.getGueltigkeitFromFamiliensituation(
			gesuch,
			gueltigkeit,
			familiensituationErstgesuch,
			familiensituation
		);

		verifyAll();
	}

	@Test
	void testFamiliensituation1GSSpecialFall2GS_NEIN_UNTERHALTSVEREINBARUNG_WithNoChange() {
		LocalDate stichtag = LocalDate.of(2024, 2, 1);
		LocalDate gueltigAb = LocalDate.of(2024, 1, 1);
		LocalDate gueltigBis = LocalDate.of(2024, 12, 31);

		sharedExpectation(gueltigAb, gueltigBis);

		EasyMock.expect(
			familiensituationErstgesuch.hasSecondGesuchsteller(gueltigBis)
		).andReturn(true).times(1);
		;

		EasyMock.expect(familiensituation.hasSecondGesuchsteller(gueltigBis))
			.andReturn(true);

		EasyMock.expect(familiensituationErstgesuch.getUnterhaltsvereinbarung())
			.andReturn(
				UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG
			);

		gueltigkeit.setGueltigAb(stichtag);

		replayAll();

		erwerbspensumAsivAbschnittRule.getGueltigkeitFromFamiliensituation(
			gesuch,
			gueltigkeit,
			familiensituationErstgesuch,
			familiensituation
		);

		verifyAll();
	}

	private void sharedExpectation(LocalDate gueltigAb, LocalDate gueltigBis) {
		EasyMock.expect(familiensituation.getAenderungPer())
			.andReturn(LocalDate.of(2024, 1, 15));
		EasyMock.expect(gesuch.extractFamiliensituation())
			.andReturn(new Familiensituation());
		EasyMock.expect(gueltigkeit.getGueltigAb())
			.andReturn(gueltigAb)
			.anyTimes();
		EasyMock.expect(gueltigkeit.getGueltigBis())
			.andReturn(gueltigBis)
			.anyTimes();
	}
}
