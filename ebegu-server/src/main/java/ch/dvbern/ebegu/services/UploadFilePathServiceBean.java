package ch.dvbern.ebegu.services;

import java.nio.file.Path;

import javax.annotation.Nonnull;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;

@Stateless
@Local(UploadFilePathService.class)
public class UploadFilePathServiceBean implements UploadFilePathService {

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Override
	public Path getValidatedFilePathWithDirectoryPrefix(@Nonnull Path path) {
		Path ebeguPath = Path.of(ebeguConfiguration.getDocumentFilePath())
			.resolve(path);
		return getValidatedFilePath(ebeguPath);
	}

	@Override
	public Path getValidatedFilePath(@Nonnull Path path) {
		Path normalizedPath = path.normalize();
		if (!normalizedPath.startsWith(
			ebeguConfiguration.getDocumentFilePath()
		)) {
			throw new EbeguRuntimeException(
				"validate file",
				"illegal document path"
			);
		}
		return normalizedPath;
	}
}
