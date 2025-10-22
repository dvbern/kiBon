package ch.dvbern.ebegu.services.reporting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Locale;

import javax.annotation.Nonnull;
import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import org.apache.commons.lang3.Validate;
import org.apache.poi.ss.usermodel.Workbook;

import static ch.dvbern.ebegu.services.reporting.AbstractReportServiceBean.MIME_TYPE_EXCEL;
import static ch.dvbern.ebegu.services.reporting.AbstractReportServiceBean.VALIDIERUNG_STICHTAG;
import static java.util.Objects.requireNonNull;

public final class ReportUtil {

	private ReportUtil() {
	}

	private static final String VORLAGE = "Vorlage '";
	private static final String NICHT_GEFUNDEN = "' nicht gefunden";

	@Nonnull
	public static String getFileName(
		ReportVorlage reportVorlage,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) {
		return ServerMessageUtil.translateEnumValue(
			reportVorlage.getDefaultExportFilename(),
			locale,
			mandant
		) + ".xlsx";
	}

	public static byte[] createWorkbook(@Nonnull Workbook workbook) {
		byte[] bytes;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);
			baos.flush();
			bytes = baos.toByteArray();
			workbook.close();

		} catch (IOException | RuntimeException e) {
			throw new IllegalStateException("Error creating workbook", e);
		}
		return bytes;
	}

	@Nonnull
	public static Workbook createWorkbook(
		@Nonnull ReportVorlage reportVorlage
	) throws IOException {
		try (InputStream is = ReportUtil.class.getResourceAsStream(
			reportVorlage.getTemplatePath()
		)) {
			return ExcelMerger.createWorkbookFromTemplate(
				requireNonNull(
					is,
					VORLAGE
						+ reportVorlage.getTemplatePath()
						+ NICHT_GEFUNDEN
				)
			);
		}
	}

	@Nonnull
	public static MimeType getContentTypeForExport() {
		try {
			return new MimeType(MIME_TYPE_EXCEL);
		} catch (MimeTypeParseException e) {
			throw new EbeguRuntimeException(
				"getContentTypeForExport",
				"could not parse mime type",
				e,
				MIME_TYPE_EXCEL
			);
		}
	}

	@Nonnull
	public static MimeType getContentTypeForCSVExport() {
		try {
			return new MimeType(Constants.TEXT_CSV);
		} catch (MimeTypeParseException e) {
			throw new EbeguRuntimeException(
				"getContentTypeForCSVExport",
				"could not parse mime type",
				e,
				Constants.TEXT_CSV
			);
		}
	}

	public static void validateStichtagParam(LocalDate stichtag) {
		Validate.notNull(stichtag, VALIDIERUNG_STICHTAG);
	}

}
