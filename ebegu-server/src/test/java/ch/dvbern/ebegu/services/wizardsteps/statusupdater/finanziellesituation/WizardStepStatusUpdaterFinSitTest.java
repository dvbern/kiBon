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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater.finanziellesituation;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.persistence.Persistence;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(EasyMockExtension.class)
class WizardStepStatusUpdaterFinSitTest extends EasyMockSupport {

	@TestSubject
	private WizardStepStatusUpdaterFinSit wizardStepStatusUpdaterFinSit;

	@Mock
	private Persistence persistence;

	@Nested
	class IsObjectMutiertTest {

		@Test
		void isGesuchObject_Mutiert() {
			Gesuch gesuch = initGesuch();
			expect(persistence.find(Gesuch.class, gesuch.getVorgaengerId()))
				.andReturn(new Gesuch());
			replayAll();
			boolean isObjectMutiert = wizardStepStatusUpdaterFinSit
				.isObjectMutiert(List.of(gesuch), List.of(gesuch));
			assertThat(isObjectMutiert, is(true));
		}

		@Test
		void isGesuchObject_NichtMutiert() {
			Gesuch gesuch = initGesuch();
			expect(persistence.find(Gesuch.class, gesuch.getVorgaengerId()))
				.andReturn(gesuch);
			replayAll();
			boolean isObjectMutiert = wizardStepStatusUpdaterFinSit
				.isObjectMutiert(List.of(gesuch), List.of(gesuch));
			assertThat(isObjectMutiert, is(false));
		}

		private Gesuch initGesuch() {
			Gesuch gesuch = new Gesuch();
			gesuch.setVorgaengerId("TEST");
			gesuch.setFinSitAenderungGueltigAbDatum(LocalDate.now());
			return gesuch;
		}

		@Test
		void isFamiliensituationObject_Mutiert() {
			Familiensituation familiensituation = initFamiliensituation();
			expect(
				persistence.find(
					Familiensituation.class,
					familiensituation.getVorgaengerId()
				)
			).andReturn(new Familiensituation());
			replayAll();
			boolean isObjectMutiert = wizardStepStatusUpdaterFinSit
				.isObjectMutiert(
					List.of(familiensituation),
					List.of(familiensituation)
				);
			assertThat(isObjectMutiert, is(true));
		}

		@Test
		void isFamiliensituationObject_NichtMutiert_KeineAenderung() {
			Familiensituation familiensituation = initFamiliensituation();
			expect(
				persistence.find(
					Familiensituation.class,
					familiensituation.getVorgaengerId()
				)
			).andReturn(familiensituation);
			replayAll();
			boolean isObjectMutiert = wizardStepStatusUpdaterFinSit
				.isObjectMutiert(
					List.of(familiensituation),
					List.of(familiensituation)
				);

			assertThat(isObjectMutiert, is(false));
		}

		@Test
		void isFamiliensituationObject_NichtMutiert_AenderungPerGeandert() {
			Familiensituation familiensituation = initFamiliensituation();
			expect(
				persistence.find(
					Familiensituation.class,
					familiensituation.getVorgaengerId()
				)
			).andReturn(familiensituation);
			replayAll();
			familiensituation.setAenderungPer(LocalDate.now());
			boolean isObjectMutiert = wizardStepStatusUpdaterFinSit
				.isObjectMutiert(
					List.of(familiensituation),
					List.of(familiensituation)
				);

			assertThat(isObjectMutiert, is(false));
		}

		private Familiensituation initFamiliensituation() {
			Familiensituation familiensituation = new Familiensituation();
			familiensituation.setVorgaengerId("TEST");
			familiensituation.setGemeinsameSteuererklaerung(true);
			return familiensituation;
		}
	}
}
