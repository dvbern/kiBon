package ch.dvbern.ebegu.validators;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CheckWebseiteValidatorTest {
	CheckWebseiteValidator checkWebseiteValidator =
		new CheckWebseiteValidator();

	@Test
	void isBasicWebseiteValid() {
		assertThat(
			checkWebseiteValidator.isValid("www.ostermundigen.ch", null),
			is(true)
		);
	}

	@Test
	void isHttpsWebseiteValid() {
		assertThat(
			checkWebseiteValidator.isValid(
				"https://www.ostermundigen.ch",
				null
			),
			is(false)
		);
	}

	@Test
	void isWebseiteWithoutWWWValid() {
		assertThat(
			checkWebseiteValidator.isValid("ostermundigen.ch", null),
			is(false)
		);
	}

	@Test
	void isWebseiteWithoutTLDValid() {
		assertThat(
			checkWebseiteValidator.isValid("www.ostermundigen", null),
			is(false)
		);
	}

	@Test
	void isWebseiteWithSubDomainValid() {
		assertThat(
			checkWebseiteValidator.isValid("www.ostermundigen.ch/test", null),
			is(true)
		);
	}
}
