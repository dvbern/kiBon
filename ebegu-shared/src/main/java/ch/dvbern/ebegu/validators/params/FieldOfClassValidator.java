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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;

/**
 * <p>
 * A constraint validator that checks if a given string value corresponds to a valid field name
 * within a specified target class. This validator uses the {@link FieldOfClass} annotation
 * to determine the target class and whether inherited fields should be included in the validation.
 * </p>
 * The validation process involves the following:
 * <ul>
 * <li>Extracting all non-synthetic field names from the target class</li>
 * <li>Optionally including inherited fields, depending on the {@link FieldOfClass#includeInherited()}
 * configuration</li>
 * <li>Checking if the provided value is null or matches one of the valid field names</li>
 * </ul>
 * <p>
 * This validator supports null values, treating them as valid inputs. Use {@link NotNull} to validate non-null inputs.
 * </p>
 */
public class FieldOfClassValidator implements
	ConstraintValidator<FieldOfClass, String> {

	private Set<String> validFieldNames;

	@Override
	public void initialize(FieldOfClass annotation) {
		Class<?> targetClass = annotation.targetClass();
		this.validFieldNames = getValidFieldNames(
			targetClass,
			annotation.includeInherited()
		);
	}

	private Set<String> getValidFieldNames(
		Class<?> targetClass,
		boolean includeInherited
	) {
		Set<String> names = new HashSet<>();
		for (Field field : targetClass.getDeclaredFields()) {
			if (!field.isSynthetic()) {
				names.add(field.getName());
			}
		}
		if (includeInherited
			&& targetClass != Object.class
			&& targetClass.getSuperclass() != Object.class) {
			names.addAll(getValidFieldNames(targetClass.getSuperclass(), true));
		}
		return names;
	}

	/**
	 * <p>
	 * Validates whether the provided value corresponds to a valid field name within the target class.
	 * The target class and its configuration (e.g., whether inherited fields are included) are
	 * specified during the validator's initialization.
	 * </p>
	 * <p>
	 * Null values are considered valid. Use {@link NotNull} to validate non-null inputs.
	 * </p>
	 *
	 * @param value the field name to validate; can be null
	 * @param context the context in which the constraint is evaluated
	 * @return true if the value is null or matches a valid field name within the target class, false otherwise
	 */
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		return validFieldNames.contains(value);
	}
}
