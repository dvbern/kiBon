package ch.dvbern.ebegu.services.reporting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;

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

	/**
	 * this method copies a sheet from a template
	 * this is needed to support creating several sheets inside one excel file based on a template
	 * there is no native method like this so we need a custom copySheet
	 *
	 * @param workbook
	 * @param templateSheet
	 * @param newSheet
	 */
	public static void copySheet(
		Workbook workbook,
		Sheet templateSheet,
		Sheet newSheet
	) {
		Map<CellStyle, CellStyle> styleCache = new HashMap<>();

		// Copy column widths
		for (int i = 0; i <= templateSheet.getRow(0).getLastCellNum(); i++) {
			newSheet.setColumnWidth(i, templateSheet.getColumnWidth(i));
			newSheet.setColumnHidden(i, templateSheet.isColumnHidden(i));
		}

		// Copy merged regions
		for (int i = 0; i < templateSheet.getNumMergedRegions(); i++) {
			newSheet.addMergedRegion(templateSheet.getMergedRegion(i));
		}

		// Copy rows and cells
		for (int rowIndex = 0;
			 rowIndex <= templateSheet.getLastRowNum();
			 rowIndex++) {
			Row srcRow = templateSheet.getRow(rowIndex);
			Row destRow = newSheet.createRow(rowIndex);

			if (srcRow == null) {
				continue;
			}

			destRow.setHeight(srcRow.getHeight());
			destRow.setZeroHeight(srcRow.getZeroHeight());

			for (int colIndex = 0;
				 colIndex < srcRow.getLastCellNum();
				 colIndex++) {
				Cell srcCell = srcRow.getCell(colIndex);
				Cell destCell = destRow.createCell(colIndex);

				if (srcCell == null) {
					continue;
				}

				// reuse styles via cache otherwise 64000 style maximum limit will be reached for excel styles
				CellStyle srcStyle = srcCell.getCellStyle();
				CellStyle cachedStyle = styleCache.get(srcStyle);

				if (cachedStyle == null) {
					cachedStyle = workbook.createCellStyle();
					cachedStyle.cloneStyleFrom(srcStyle);
					styleCache.put(srcStyle, cachedStyle);
				}

				destCell.setCellStyle(cachedStyle);

				// Copy cell value
				switch (srcCell.getCellType()) {
				case STRING:
					destCell.setCellValue(srcCell.getRichStringCellValue());
					break;
				case NUMERIC:
					destCell.setCellValue(srcCell.getNumericCellValue());
					break;
				case BOOLEAN:
					destCell.setCellValue(srcCell.getBooleanCellValue());
					break;
				case FORMULA:
					try {
						destCell.setCellFormula(srcCell.getCellFormula());
					} catch (RuntimeException e) {
						throw new IllegalStateException(e);
					}
					break;
				case BLANK:
				case ERROR:
				default:
					break;
				}
			}
		}
	}

	/**
	 * excel working sheets are not allowed to be named the same.
	 * this method makes sure, same names won't occur
	 *
	 * @param workbook
	 * @param baseName
	 * @return
	 */
	public static String createUniqueSheetName(
		Workbook workbook,
		String baseName
	) {
		String safeName = WorkbookUtil.createSafeSheetName(baseName);
		final int MAX_LEN = 31;

		// truncate if name is too long
		String finalName = truncate(safeName, MAX_LEN);
		int counter = 1;

		while (workbook.getSheet(finalName) != null) {
			String suffix = " (" + counter + ")";
			int maxBaseLength = MAX_LEN - suffix.length();

			String truncatedBase = truncate(safeName, maxBaseLength);
			finalName = truncatedBase + suffix;

			counter++;
		}

		return finalName;
	}

	private static String truncate(String name, int maxLen) {
		if (name.length() <= maxLen) {
			return name;
		}
		return name.substring(0, maxLen);
	}
}
