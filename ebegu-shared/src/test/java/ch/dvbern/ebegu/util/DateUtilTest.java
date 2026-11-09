/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.util;

import java.time.LocalDate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests fuer DateUtil
 */
class DateUtilTest {

	@Test
	void parseStringToDateOrReturnNowTestNullString() {
		final LocalDate now = LocalDate.now();
		final LocalDate returnedDate = DateUtil.parseStringToDateOrReturnNow(
			null
		);
		assertNotNull(returnedDate);
		assertTrue(now.isEqual(returnedDate));
	}

	@Test
	void parseStringToDateOrReturnNowTestEmptyString() {
		final LocalDate now = LocalDate.now();
		final LocalDate returnedDate = DateUtil.parseStringToDateOrReturnNow(
			""
		);
		assertNotNull(returnedDate);
		assertTrue(now.isEqual(returnedDate));
	}

	@Test
	void parseStringToDateOrReturnNowTest() {
		final LocalDate returnedDate = DateUtil.parseStringToDateOrReturnNow(
			"2015-12-31"
		);
		assertNotNull(returnedDate);
		assertEquals(2015, returnedDate.getYear());
		assertEquals(12, returnedDate.getMonthValue());
		assertEquals(31, returnedDate.getDayOfMonth());
	}

	@Test
	void testIncrementYear() {
		final String oldDate = "2020-05-17";
		final String newDate = DateUtil.incrementYear(oldDate);
		assertEquals("2021-05-17", newDate);
	}

	@Nested
	class isSameOrBeforeTests {

		@Test
		void sameDateShouldBeTruthy() {
			final LocalDate date = LocalDate.of(2017, 1, 1);
			final LocalDate compareTo = LocalDate.from(date);

			assertTrue(DateUtil.isSameDateOrBefore(date, compareTo));
		}

		@ParameterizedTest
		@ValueSource(ints = { 1, 3, 10, 30 })
		void xNon0DaysLaerShouldBeFalsy(int minusDays) {
			final LocalDate date = LocalDate.of(2017, 1, 1);
			final LocalDate compareTo = date.minusDays(minusDays);

			assertFalse(
				DateUtil.isSameDateOrBefore(date, compareTo)
			);
		}

		@ParameterizedTest
		@ValueSource(ints = { 1, 3, 10, 30 })
		void xNon0DaysEarlierShouldBeTruthy(int plusDays) {
			final LocalDate date = LocalDate.of(2017, 1, 1);
			final LocalDate compareTo = date.plusDays(plusDays);

			assertTrue(DateUtil.isSameDateOrBefore(date, compareTo));
		}
	}

	@Nested
	class isSameOrAfterTests {

		@Test
		void sameDateShouldBeTruthy() {
			final LocalDate date = LocalDate.of(2017, 1, 1);
			final LocalDate compareTo = LocalDate.from(date);

			assertTrue(DateUtil.isSameDateOrAfter(date, compareTo));
		}

		@ParameterizedTest
		@ValueSource(ints = { 1, 3, 10, 30 })
		void xNon0DaysEarlierShouldBeFalsy(int days) {
			final LocalDate date = LocalDate.of(2017, 1, 1);
			final LocalDate compareTo = date.plusDays(days);

			assertFalse(DateUtil.isSameDateOrAfter(date, compareTo));
		}

		@ParameterizedTest
		@ValueSource(ints = { 1, 3, 10, 30 })
		void xNon0DaysLaterShouldBeTruthy(int days) {
			final LocalDate date = LocalDate.of(2017, 1, 1);
			final LocalDate compareTo = date.minusDays(days);

			assertTrue(DateUtil.isSameDateOrAfter(date, compareTo));
		}
	}

	@Nested
	class formatDateTests {

		@Test
		void formatDateShouldReturnFormattedDate() {
			final LocalDate date = LocalDate.of(2017, 1, 1);
			final String formattedDate = DateUtil.toFormattedDate(date);

			assertEquals("01.01.2017", formattedDate);
		}
	}
}
