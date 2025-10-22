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

package ch.dvbern.ebegu.gesuch.validators;

import java.util.Objects;

import javax.annotation.Nullable;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Gesuch;

public class CheckGesuchGesuchstellerContainerValidator implements
	ConstraintValidator<CheckGesuchGesuchstellerContainer, Gesuch> {

	@Override
	public boolean isValid(
		Gesuch gesuch,
		@Nullable ConstraintValidatorContext constraintValidatorContext
	) {
		if (gesuch.getGesuchsteller2() == null
			|| gesuch.getGesuchsteller1() == null) {
			return true;
		}
		return !Objects.equals(
			gesuch.getGesuchsteller2().getId(),
			gesuch.getGesuchsteller1().getId()
		);
	}
}
