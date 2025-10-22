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

package ch.dvbern.ebegu.tests;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.gesuch.validators.CheckGesuchGesuchstellerContainerValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CheckGesuchGesuchstellerContainerValidatorTest {

	private CheckGesuchGesuchstellerContainerValidator checkGesuchGesuchstellerContainerValidator =
		new CheckGesuchGesuchstellerContainerValidator();

	@Test
	void checkGesuchGesuchstellerContainerValidator_GS1Container_null() {
		var gesuch = new Gesuch();
		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		Assertions.assertTrue(
			checkGesuchGesuchstellerContainerValidator.isValid(gesuch, null)
		);
	}

	@Test
	void checkGesuchGesuchstellerContainerValidator_GS2Container_null() {
		var gesuch = new Gesuch();
		gesuch.setGesuchsteller2(new GesuchstellerContainer());
		Assertions.assertTrue(
			checkGesuchGesuchstellerContainerValidator.isValid(gesuch, null)
		);
	}

	@Test
	void checkGesuchGesuchstellerContainerValidator_GS1Container_GS2Container_null() {
		var gesuch = new Gesuch();
		Assertions.assertTrue(
			checkGesuchGesuchstellerContainerValidator.isValid(gesuch, null)
		);
	}

	@Test
	void checkGesuchGesuchstellerContainerValidator_not_equal() {
		var gesuch = new Gesuch();
		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.setGesuchsteller2(new GesuchstellerContainer());
		Assertions.assertTrue(
			checkGesuchGesuchstellerContainerValidator.isValid(gesuch, null)
		);
	}

	@Test
	void checkGesuchGesuchstellerContainerValidator_equal() {
		var gesuch = new Gesuch();
		var gesuchsteller = new GesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller);
		gesuch.setGesuchsteller2(gesuchsteller);
		Assertions.assertFalse(
			checkGesuchGesuchstellerContainerValidator.isValid(gesuch, null)
		);
	}
}
