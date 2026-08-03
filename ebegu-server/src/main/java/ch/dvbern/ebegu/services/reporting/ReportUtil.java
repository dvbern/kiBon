package ch.dvbern.ebegu.services.reporting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.apache.poi.ss.formula.SheetNameFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Name;
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

		copyTemplateNames(workbook, templateSheet, newSheet);

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
	 * Copies defined names (such as named ranges or formulas) from a template sheet to a new sheet within the same
	 * workbook.
	 * The method adapts the formulas to reference the new sheet instead of the template sheet.
	 *
	 * @param workbook the workbook containing the sheets and defined names
	 * @param templateSheet the template sheet whose defined names will be copied
	 * @param newSheet the target sheet where the copied names will refer
	 */
	private static void copyTemplateNames(
		Workbook workbook,
		Sheet templateSheet,
		Sheet newSheet
	) {
		List<Name> existingNames = new ArrayList<>(workbook.getAllNames());
		existingNames.stream()
			.filter(
				name -> name.getSheetIndex()
					== workbook.getSheetIndex(templateSheet)
			)
			.forEach(name -> {
				Name localName = workbook.createName();
				localName.setNameName(name.getNameName());
				var replaced = name.getRefersToFormula()
					.replace(
						templateSheet.getSheetName() + "!",
						SheetNameFormatter.format(newSheet.getSheetName()) + "!"
					);
				localName.setRefersToFormula(replaced);
				localName.setComment(name.getComment());
				localName.setFunction(name.isFunctionName());
				localName.setSheetIndex(workbook.getSheetIndex(newSheet));
			});
	}

	/**
	 * <p>
	 * Creates a unique sheet name within a workbook by ensuring the proposed name
	 * does not conflict with existing sheet names. If the proposed name is too long,
	 * it is truncated. If after truncating a sheet with the proposed name already exists,
	 * it will generate a version with a numerical suffix.
	 * </p>
	 *
	 * <p>
	 * To avoid running into the POI-bug, where names with apostrophes in sheet names are not parsed correctly
	 * when evaluating formulars, apostrophes are replaced with aigus. You can find more information about this bug in
	 * the kiBon-Task, the linked bugzilla bug or the test of this class.
	 * </p>
	 *
	 * @param workbook the {@code Workbook} instance where the sheet will be created
	 * @param proposedName the desired name for the sheet
	 * @return a unique and valid sheet name that can safely be used in the provided workbook
	 * @see <a href="https://bz.apache.org/bugzilla/show_bug.cgi?id=68305">Bugzilla-Bug-68305</a>
	 *
	 */
	public static String createUniqueSheetName(
		Workbook workbook,
		String proposedName
	) {
		String replacedAiguName = proposedName.replace('\'', '´');
		String safeName = WorkbookUtil.createSafeSheetName(replacedAiguName);
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

	/**
	 * Removes the specified sheet from the workbook and deletes all defined names
	 * associated with the sheet.
	 *
	 * @param workbook the workbook from which the sheet will be removed
	 * @param sheet the sheet to be removed from the workbook
	 *
	 * @throws IllegalArgumentException if the sheet is not found in the workbook
	 */
	public static void removeSheet(Workbook workbook, Sheet sheet) {
		if (workbook.getSheetIndex(sheet) < 0) {
			throw new IllegalArgumentException("Sheet not found in workbook");
		}
		String sheetName = sheet.getSheetName();
		workbook.removeSheetAt(workbook.getSheetIndex(sheet));
		workbook.getAllNames()
			.stream()
			.filter(n -> n.getSheetName().equals(sheetName))
			.forEach(workbook::removeName);
	}
}
