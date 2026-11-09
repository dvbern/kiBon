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

package ch.dvbern.ebegu.tests.validations;

import ch.dvbern.ebegu.validators.iban.CheckIBANNotQRValidator;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests fuer CheckDateRangeValidator
 */
class CheckIBANNotQRValidatorTest {

	private CheckIBANNotQRValidator validator;

	@BeforeEach
	void setUp() {
		validator = new CheckIBANNotQRValidator();
	}

	@Test
	void test_shouldBeInvalidIfQRIBAN() {
		IBAN iban = new IBAN("CH4431999123000889012");
		assertFalse(validator.isValid(iban, null));
	}

	@Test
	void test_shouldBeValidIfNormalIBAN() {
		IBAN iban = new IBAN("CH9300762011623852957");
		assertTrue(validator.isValid(iban, null));
	}

}
