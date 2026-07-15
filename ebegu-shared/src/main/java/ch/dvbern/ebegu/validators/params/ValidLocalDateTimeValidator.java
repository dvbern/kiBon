/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.validators.params;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;

public class ValidLocalDateTimeValidator implements
	ConstraintValidator<ValidLocalDateTime, String> {

	/**
	 * <p>
	 * Validates whether the given string represents a valid {@link LocalDateTime}
	 * in the ISO_LOCAL_DATE_TIME format. Null values are considered valid.
	 * Use the {@link NotNull} annotation to ensure that the value is not null.
	 * </p>
	 * <p>
	 * The UselessOperationOnImmutable PMD rule is suppressed because the LocalDateTime.parse method
	 * is called to validate the input string and the result is not used. Therefore, the method call is considered
	 * useless
	 * by PMD, but it is necessary for the validation process.
	 * </p>
	 * 
	 * @param value the string value to validate; can be null
	 * @param context the context in which the constraint is evaluated
	 * @return true if the value is null or a valid {@link LocalDateTime}, false otherwise
	 */
	@Override
	@SuppressWarnings("PMD.UselessOperationOnImmutable")
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		try {
			LocalDateTime.parse(value);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}
}
