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

package ch.dvbern.ebegu.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.FileMetadata;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.UploadFileInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service zum Speichern von Files auf dem File-System
 * Kann ev. durch modshape ersetzt werden
 */
@SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
@Stateless
public class FileSaverService {

	private static final Logger LOG = LoggerFactory.getLogger(
		FileSaverService.class.getSimpleName()
	);

	@Inject
	private UploadFilePathService uploadFilePathService;

	public void save(UploadFileInfo uploadFileInfo, String folderName) {
		generateFileName(uploadFileInfo, folderName);
		Path absoluteFilePath = uploadFileInfo.getPath();
		try {
			if (!Files.exists(absoluteFilePath.getParent())) {
				Files.createDirectories(absoluteFilePath.getParent());
			}
			uploadFileInfo.setSize(
				Files.size(
					Files.write(
						absoluteFilePath,
						uploadFileInfo.getBytes()
					)
				)
			); //here we write to filesystem
			LOG.info("Save file in FileSystem: {}", absoluteFilePath);

		} catch (IOException e) {
			throw new EbeguRuntimeException(
				"save",
				"Could not save file in filesystem {0}",
				e,
				String.valueOf(absoluteFilePath)
			);
		}
	}

	public void generateFileName(
		UploadFileInfo uploadFileInfo,
		String folderName
	) {
		Objects.requireNonNull(uploadFileInfo);
		Objects.requireNonNull(uploadFileInfo.getFilename());

		String ending = getFileNameEnding(uploadFileInfo.getFilename());
		String filename = getUuidAsFilename(ending);
		// Wir speichern der Name des Files nicht im FS. Kann sonst Probleme mit Umlauten geben
		final Path path = Path.of(folderName, filename);
		final Path absoluteFilePath = uploadFilePathService
			.getValidatedFilePathWithDirectoryPrefix(path);
		uploadFileInfo.setPath(absoluteFilePath);
		uploadFileInfo.setActualFilename(filename);
	}

	@Nonnull
	public UploadFileInfo save(byte[] bytes, String fileName, String folderName)
		throws MimeTypeParseException {
		MimeType contentType = new MimeType("application/pdf");
		return save(bytes, fileName, folderName, contentType);
	}

	@Nonnull
	public UploadFileInfo saveZipFile(
		@Nonnull byte[] bytes,
		@Nonnull String filename
	) throws MimeTypeParseException {

		String filenameWithEnding = filename + ".zip";

		final UploadFileInfo uploadFileInfo = new UploadFileInfo(
			filenameWithEnding,
			new MimeType("application/zip")
		);
		uploadFileInfo.setBytes(bytes);
		Objects.requireNonNull(uploadFileInfo);
		Objects.requireNonNull(uploadFileInfo.getFilename());

		final Path path = Path.of("auftraege", filenameWithEnding);
		final Path file = uploadFilePathService
			.getValidatedFilePathWithDirectoryPrefix(path);
		uploadFileInfo.setPath(file);
		uploadFileInfo.setActualFilename(filenameWithEnding);

		try {
			if (!Files.exists(file.getParent())) {
				Files.createDirectories(file.getParent());
			}
			uploadFileInfo.setSize(
				Files.size(Files.write(file, uploadFileInfo.getBytes()))
			); //here we write to filesystem
			LOG.info("Save file in FileSystem: {}", file);

		} catch (IOException e) {
			throw new EbeguRuntimeException(
				"save",
				"Could not save file in filesystem {0}",
				e,
				String.valueOf(file)
			);
		}
		return uploadFileInfo;
	}

	@Nonnull
	public UploadFileInfo save(
		byte[] bytes,
		String fileName,
		String folderName,
		MimeType contentType
	) {
		final UploadFileInfo uploadFileInfo = new UploadFileInfo(
			fileName,
			contentType
		);
		uploadFileInfo.setBytes(bytes);
		save(uploadFileInfo, folderName);
		return uploadFileInfo;
	}

	public boolean copy(FileMetadata fileToCopy, String folderName) {
		Objects.requireNonNull(fileToCopy);
		Objects.requireNonNull(folderName);

		Path oldfile = Paths.get(fileToCopy.getFilepfad());
		// Wir speichern der Name des Files nicht im FS. Kann sonst Probleme mit Umlauten geben
		String filename = getUuidAsFilename(
			getFileNameEnding(fileToCopy.getFilename())
		);
		final Path newfile = uploadFilePathService
			.getValidatedFilePathWithDirectoryPrefix(
				Path.of(folderName, filename)
			);
		fileToCopy.setFilepfad(String.valueOf(filename));

		try {
			if (!Files.exists(newfile.getParent())) {
				Files.createDirectories(newfile.getParent());
				LOG.info("Save file in FileSystem: {}", newfile);
			}
			Files.copy(oldfile, newfile);

		} catch (IOException e) {
			LOG.error(
				"Can't save file in FileSystem: {}",
				fileToCopy.getFilename(),
				e
			);
			return false;
		}
		return true;
	}

	private String getFileNameEnding(String filename) {
		return FilenameUtils.getExtension(filename);
	}

	public boolean remove(String dokumentPaths) {
		final Path path = uploadFilePathService
			.getValidatedFilePathWithDirectoryPrefix(
				Path.of(dokumentPaths)
			);
		try {
			if (Files.exists(path)) {
				Files.delete(path);
				LOG.info("Delete file in FileSystem: {}", dokumentPaths);
			}
		} catch (IOException e) {
			LOG.error("Can't remove file in FileSystem: {}", dokumentPaths, e);
			return false;
		}
		return true;
	}

	public boolean removeAllFromSubfolder(@Nonnull String subfolder) {
		Path file = uploadFilePathService
			.getValidatedFilePathWithDirectoryPrefix(Path.of(subfolder));
		try {
			if (Files.exists(file) && Files.isDirectory(file)) {
				FileUtils.cleanDirectory(file.toFile());
				Files.deleteIfExists(file);
				LOG.info("Deleting directory : {}", file);
			}
			return true;
		} catch (IOException e) {
			LOG.error("Can't delete directory: {}", file, e);
			return false;
		}
	}

	public void deleteAllFilesInTempReportsFolder() {
		deleteAllFilesInTempFolder(Constants.TEMP_REPORT_FOLDERNAME);
	}

	private void deleteAllFilesInTempFolder(@Nonnull String folder) {
		final Path tempFolder = uploadFilePathService
			.getValidatedFilePathWithDirectoryPrefix(Path.of(folder));
		if (Files.exists(tempFolder) && Files.isDirectory(tempFolder)) {
			try (Stream<Path> files = Files.walk(tempFolder)) {
				files
					.filter(Files::isRegularFile)
					.forEach(file -> deleteFileIfTokenExpired(file));
			} catch (IOException e) {
				throw new EbeguRuntimeException(
					"save",
					"Could not save file in filesystem {0}",
					e,
					String.valueOf(tempFolder)
				);
			}
		}
	}

	private void deleteFileIfTokenExpired(Path path) {
		LocalDateTime deleteBefore = LocalDateTime.now()
			.minusMinutes(Constants.MAX_LONGER_TEMP_DOWNLOAD_AGE_MINUTES);
		LocalDateTime lastModified =
			LocalDateTime.ofInstant(
				Instant.ofEpochMilli(path.toFile().lastModified()),
				ZoneId.systemDefault()
			);
		if (lastModified.isBefore(deleteBefore)) {
			LOG.info(
				"Deleting File {}, lastModified on {}",
				path.getFileName(),
				lastModified
			);
			try {
				Files.delete(path);
			} catch (IOException e) {
				LOG.error(
					"Can't delete file in FileSystem: {}",
					path.getFileName(),
					e
				);
			}
		}
	}

	@Nonnull
	private String getUuidAsFilename(String filenameEnding) {
		UUID uuid = UUID.randomUUID();
		return uuid.toString() + '.' + filenameEnding;
	}
}
