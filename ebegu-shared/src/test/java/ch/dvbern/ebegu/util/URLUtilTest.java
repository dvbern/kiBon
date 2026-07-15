package ch.dvbern.ebegu.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class URLUtilTest {

	@Test
	void isValidHttpOrHttpsURL_shouldReturnTrue_whenHttpsSchemeGivenAndUrlValid() {
		var url = "https://be.kibon.ch/faelle";

		var result = URLUtil.isValidHttpOrHttpsURL(url);

		assertThat(result, is(true));
	}

	@Test
	void isValidHttpOrHttpsURL_shouldReturnTrue_whenHttpSchemeGivenAndUrlValid() {
		var url = "http://be.kibon.ch/faelle";

		var result = URLUtil.isValidHttpOrHttpsURL(url);

		assertThat(result, is(true));
	}

	@Test
	void isValidHttpOrHttpsURL_shouldReturnFalse_whenFtpSchemeGivenAndUrlValid() {
		var url = "ftp://be.kibon.ch/faelle";

		var result = URLUtil.isValidHttpOrHttpsURL(url);

		assertThat(result, is(false));
	}

	@Test
	void isValidHttpOrHttpsURL_shouldReturnFalse_whenFileSchemeGiven() {
		var url = "file:///etc/passwd";

		var result = URLUtil.isValidHttpOrHttpsURL(url);

		assertThat(result, is(false));
	}

	@Test
	void isValidHttpOrHttpsURL_shouldReturnFalse_whenNoSchemeGiven() {
		var url = "/faelle";

		var result = URLUtil.isValidHttpOrHttpsURL(url);

		assertThat(result, is(false));
	}

	@Test
	void isValidHttpOrHttpsURL_shouldReturnFalse_whenFragmentOnlyGiven() {
		var url = "/#/faelle?abc=xy";

		var result = URLUtil.isValidHttpOrHttpsURL(url);

		assertThat(result, is(false));
	}

	@Test
	void isValidHttpOrHttpsURL_shouldReturnFalse_whenEmptyStringGiven() {
		var url = "";

		var result = URLUtil.isValidHttpOrHttpsURL(url);

		assertThat(result, is(false));
	}

	@Test
	void isValidHttpOrHttpsURL_shouldReturnFalse_whenNullGiven() {
		String url = null;

		var result = URLUtil.isValidHttpOrHttpsURL(url);

		assertThat(result, is(false));
	}

	@Test
	void isValidHttpOrHttpsURL_shouldReturnFalse_whenGarbageStringGiven() {
		var url = "not a url at all";

		var result = URLUtil.isValidHttpOrHttpsURL(url);

		assertThat(result, is(false));
	}

	@Test
	void isValidHttpOrHttpsURL_shouldReturnTrue_whenSchemeCaseDiffersFromLowercase() {
		var url = "HTTPS://be.kibon.ch/faelle";

		var result = URLUtil.isValidHttpOrHttpsURL(url);

		assertThat(result, is(true));
	}
}
