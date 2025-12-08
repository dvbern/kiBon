package ch.dvbern.ebegu.services.reporting;

import java.util.Locale;

import javax.annotation.Nonnull;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.file.FileSaverService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.RowFiller;
import lombok.NoArgsConstructor;

import static ch.dvbern.ebegu.services.reporting.ReportUtil.createWorkbook;
import static ch.dvbern.ebegu.services.reporting.ReportUtil.getContentTypeForExport;
import static ch.dvbern.ebegu.services.reporting.ReportUtil.getFileName;

@Stateless
@NoArgsConstructor
public class ExcelFileSaverService {

	private FileSaverService fileSaverService;

	@Inject
	public ExcelFileSaverService(
		FileSaverService fileSaverService
	) {
		this.fileSaverService = fileSaverService;
	}

	/**
	 * Erstellt das Dokument und speichert es im Filesystem
	 */
	@Nonnull
	public UploadFileInfo saveExcelDokument(
		ReportVorlage reportVorlage,
		RowFiller rowFiller,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) {
		byte[] bytes = createWorkbook(rowFiller.getSheet().getWorkbook());

		rowFiller.getSheet().getWorkbook().dispose();

		return fileSaverService.save(
			bytes,
			getFileName(reportVorlage, locale, mandant),
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport()
		);
	}

}
