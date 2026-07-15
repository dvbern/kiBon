package ch.dvbern.ebegu.services.util.datetime;

import java.time.LocalDateTime;

import jakarta.ws.rs.WebApplicationException;

import org.easymock.EasyMockExtension;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EasyMockExtension.class)
class DateTimeUtilsTest {

	@TestSubject
	DateTimeUtils dateTimeUtils;

	@Test
	void shouldThrowBadRequest_WhenParsingInvalidDateTime() {
		assertThrows(
			WebApplicationException.class,
			() -> dateTimeUtils.parseOrThrowBadRequest("invalidDateTime")
		);
	}

	@Test
	void shouldNotThrowBadRequest_WhenParsingValidDateTime() {
		assertDoesNotThrow(
			() -> dateTimeUtils.parseOrThrowBadRequest(
				LocalDateTime.now().toString()
			)
		);
	}
}
