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

package ch.dvbern.ebegu.tests.validations;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;

import ch.dvbern.ebegu.batchjobhistory.BatchjobHistorySearchParams;
import ch.dvbern.ebegu.mailing.VersendeteMailSearchParams;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

class SearchParamsValidationTest extends AbstractValidatorTest {

	private static final String SORT_DIRECTION = "sortDirection";

	@ParameterizedTest
	@ValueSource(classes = { BatchjobHistorySearchParams.class,
		VersendeteMailSearchParams.class })
	void invalidSortDirectionResolvesMessage(Class<?> paramsClass) {
		Set<String> messages = validateSortDirection(paramsClass, "foo");

		assertThat(
			messages,
			contains("Invalid sort direction: foo. Valid values are: asc, desc")
		);
	}

	@ParameterizedTest
	@ValueSource(classes = { BatchjobHistorySearchParams.class,
		VersendeteMailSearchParams.class })
	void validSortDirectionsPass(Class<?> paramsClass) {
		assertThat(validateSortDirection(paramsClass, "asc"), empty());
		assertThat(validateSortDirection(paramsClass, "desc"), empty());
	}

	private static <T> Set<String> validateSortDirection(
		Class<T> paramsClass,
		String value
	) {
		Set<ConstraintViolation<T>> violations = validatorFactory.getValidator()
			.validateValue(paramsClass, SORT_DIRECTION, value);
		return violations.stream()
			.map(ConstraintViolation::getMessage)
			.collect(Collectors.toSet());
	}
}
