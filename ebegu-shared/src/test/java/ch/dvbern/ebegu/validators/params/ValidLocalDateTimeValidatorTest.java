package ch.dvbern.ebegu.validators.params;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidLocalDateTimeValidatorTest {

	private final ValidLocalDateTimeValidator validator =
		new ValidLocalDateTimeValidator();

	@Test
	void isValidReturnsTrueForNullValue() {
		assertTrue(validator.isValid(null, null));
	}

	@Test
	void isValidReturnsTrueForIsoLocalDateTimeWithoutSeconds() {
		assertTrue(validator.isValid("2026-07-08T14:30", null));
	}

	@Test
	void isValidReturnsTrueForIsoLocalDateTimeWithSeconds() {
		assertTrue(validator.isValid("2026-07-08T14:30:15", null));
	}

	@Test
	void isValidReturnsTrueForIsoLocalDateTimeWithFractionalSeconds() {
		assertTrue(validator.isValid("2026-07-08T14:30:15.123", null));
	}

	@Test
	void isValidReturnsFalseForDateOnly() {
		assertFalse(validator.isValid("2026-07-08", null));
	}

	@Test
	void isValidReturnsFalseForInvalidDate() {
		assertFalse(validator.isValid("2026-02-30T14:30:15", null));
	}

	@Test
	void isValidReturnsFalseForInvalidTime() {
		assertFalse(validator.isValid("2026-07-08T24:00:00", null));
	}

	@Test
	void isValidReturnsFalseForBlankValue() {
		assertFalse(validator.isValid("", null));
	}

	@Test
	void isValidReturnsFalseForNonDateString() {
		assertFalse(validator.isValid("hello", null));
	}

	@Test
	void isValidReturnsFalseForNonIsoDateTimeFormat() {
		assertFalse(validator.isValid("08.07.2026 14:30", null));
	}

	@Test
	void isValidReturnsFalseForDateTimeWithTimezone() {
		assertFalse(validator.isValid("2026-07-08T14:30:15Z", null));
	}
}
