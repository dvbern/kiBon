package ch.dvbern.ebegu.validators;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CheckEmailValidatorTest {
	CheckEmailValidator checkEmailValidator = new CheckEmailValidator();

	@Test
	void isMultipleDotsEmailValid() {
		assertThat(
			checkEmailValidator.isValid("test@mailbucket.dvbern.ch", null),
			is(true)
		);
	}

	@Test
	void isMailbucketEmailValid() {
		assertThat(
			checkEmailValidator.isValid("test@mailbucket.ch", null),
			is(true)
		);
	}
}
