package ch.dvbern.ebegu.validators.bicswift;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CheckBicSwiftValidatorTest {
	CheckBicSwiftValidator checkBicSwiftValidator =
		new CheckBicSwiftValidator();

	@Test
	void tooShortString_ShouldBe_Invalid() {
		String tooShortString = "ttt";

		Boolean response = this.checkBicSwiftValidator.isValid(
			tooShortString,
			null
		);
		assertThat(response, is(false));
	}

	@Test
	void tooLongString_ShouldBe_Invalid() {
		String tooLongString = "tttttttttttttttttt";

		Boolean response = this.checkBicSwiftValidator.isValid(
			tooLongString,
			null
		);
		assertThat(response, is(false));
	}

	@Test
	void validBic_ShouldBe_Valid() {
		String validBic = "AAAABBCC333";

		Boolean response = this.checkBicSwiftValidator.isValid(validBic, null);
		assertThat(response, is(true));
	}

	@Test
	void emptyString_ShouldBe_Invalid() {
		String emptyString = "";

		Boolean response = this.checkBicSwiftValidator.isValid(
			emptyString,
			null
		);
		assertThat(response, is(false));
	}

	@Test
	void blankString_ShouldBe_Invalid() {
		String blankString = "    ";

		Boolean response = this.checkBicSwiftValidator.isValid(
			blankString,
			null
		);
		assertThat(response, is(false));
	}

	@Test
	void null_ShouldBe_Valid() {
		String nullBic = null;

		Boolean response = this.checkBicSwiftValidator.isValid(nullBic, null);
		assertThat(response, is(true));
	}

	@Test
	void validBicWithSpaces_ShouldBe_inValid() {
		String validBicWithSpaces = "AA   A  AB    B C C3   3   3";

		Boolean response = this.checkBicSwiftValidator.isValid(
			validBicWithSpaces,
			null
		);
		assertThat(response, is(false));
	}
}
