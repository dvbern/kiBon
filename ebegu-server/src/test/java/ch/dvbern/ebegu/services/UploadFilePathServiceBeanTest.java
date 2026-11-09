package ch.dvbern.ebegu.services;

import java.nio.file.Path;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(EasyMockExtension.class)
public class UploadFilePathServiceBeanTest {

	@TestSubject
	private final UploadFilePathServiceBean filePathValidator =
		new UploadFilePathServiceBean();

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private EbeguConfiguration ebeguConfiguration;

	private static final String FILE_PATH_CONFIG = "/kibon/data";

	@BeforeEach
	public void init() {
		expect(ebeguConfiguration.getDocumentFilePath()).andReturn(
			FILE_PATH_CONFIG
		).anyTimes();
		replay(ebeguConfiguration);
	}

	@Test
	public void ebeguFilePath() {
		Path filePath = Path.of(FILE_PATH_CONFIG, "test");
		Path validatedFilePath = filePathValidator.getValidatedFilePath(
			filePath
		);
		assertThat(validatedFilePath, is(filePath));
	}

	@Test
	public void noEbeguFilePath() {
		assertThrows(EbeguRuntimeException.class, () -> {
			Path filePath = Path.of("hello", "test");
			Path validatedFilePath = filePathValidator.getValidatedFilePath(
				filePath
			);
		});
	}

	@Test
	public void validFilePath() {
		Path validatedFilePath = filePathValidator
			.getValidatedFilePathWithDirectoryPrefix(Path.of("test"));
		assertThat(validatedFilePath, is(Path.of(FILE_PATH_CONFIG, "test")));
	}

	@Test
	public void validFilePathWithEnding() {
		Path validatedFilePath = filePathValidator
			.getValidatedFilePathWithDirectoryPrefix(
				Path.of("test", "file.txt")
			);
		assertThat(
			validatedFilePath,
			is(Path.of(FILE_PATH_CONFIG, "/test/file.txt"))
		);
	}

	@Test
	public void filePathWithDoubleDotPathStep() {
		assertThrows(EbeguRuntimeException.class, () -> {
			Path validatedFilePath = filePathValidator
				.getValidatedFilePathWithDirectoryPrefix(Path.of("/../test"));
		});
	}

}
