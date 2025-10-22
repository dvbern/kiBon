package ch.dvbern.ebegu.services;

import java.nio.file.Path;

import javax.annotation.Nonnull;

public interface UploadFilePathService {

	Path getValidatedFilePathWithDirectoryPrefix(@Nonnull Path path);

	Path getValidatedFilePath(@Nonnull Path path);
}
