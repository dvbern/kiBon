/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.reporting;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportUtilTest {

	/**
	 * This test class reproduces and demonstrates a <a
	 * href="https://bz.apache.org/bugzilla/show_bug.cgi?id=68305">known bug in POI</a>.
	 * The bug leads to an IllegalArgumentException when evaluating a formula referencing a sheet with an apostrophe in
	 * its name.
	 * This happens because Excel escapes apostrophes in sheet names by doubling them. So, e.g. "L'embrace" becomes
	 * "L''embrace".
	 * However, POI does not handle this correctly, resolves it to Lembrace and then throws an IllegalArgumentException
	 * because
	 * the sheet is not found.
	 */
	@Nested
	class POIBugReproduction {
		@Test
		void evaluateAll_shouldThrowInvalidSheetIndex_whenNameFormulaReferencesSheetWithApostrophe()
			throws IOException {
			try (Workbook workbook = createWorkbookWithApostropheNameFormula(
				"L'embrace"
			)) {
				FormulaEvaluator evaluator = workbook.getCreationHelper()
					.createFormulaEvaluator();

				IllegalArgumentException exception = assertThrows(
					IllegalArgumentException.class,
					evaluator::evaluateAll
				);

				assertThat(
					exception.getMessage(),
					containsString("Invalid sheetIndex")
				);
			}
		}

		private static Workbook createWorkbookWithApostropheNameFormula(
			String sheetname
		) {
			Workbook workbook = new XSSFWorkbook();

			Sheet dataSheet = workbook.createSheet(sheetname);
			dataSheet.createRow(0).createCell(0).setCellValue(42);

			addFormulaNameToWorkbookForSheet(
				workbook,
				"myLocalName",
				"'L''embrace'!$A$1",
				dataSheet
			);

			dataSheet.getRow(0).createCell(1).setCellFormula("myLocalName");

			return workbook;
		}
	}

	@Nested
	class createUniqueSheetName {
		@Test
		void shouldReturnProposedName_whenProposedNameHasNoSpecialCharactersAndIsNotAlreadyInWorkbook() {
			var name = "TS Paris";

			Workbook workbook = new XSSFWorkbook();

			String uniqueSheetName = ReportUtil.createUniqueSheetName(
				workbook,
				name
			);

			assertThat(uniqueSheetName, is("TS Paris"));
		}

		@Test
		void shouldReturnProposedNameWithCounter_whenProposedNameAlreadyExistsInWorkbook() {
			var name = "TS Paris";

			Workbook workbook = new XSSFWorkbook();
			workbook.createSheet(name);

			String uniqueSheetName = ReportUtil.createUniqueSheetName(
				workbook,
				name
			);

			assertThat(uniqueSheetName, is("TS Paris (1)"));
		}

		@Test
		void shouldReplaceApostrophWithAigu_whenProposedNameHasApostroph() {
			var name = "L'embrace";

			Workbook workbook = new XSSFWorkbook();
			workbook.createSheet(name);

			String uniqueSheetName = ReportUtil.createUniqueSheetName(
				workbook,
				name
			);

			assertThat(uniqueSheetName, is("L´embrace"));
		}

		@Test
		void shouldReplaceMinusWithEmptySpace_whenProposedNameHasMinus() {
			var name = "Tagesschule-test";

			Workbook workbook = new XSSFWorkbook();
			workbook.createSheet(name);

			String uniqueSheetName = ReportUtil.createUniqueSheetName(
				workbook,
				name
			);

			assertThat(uniqueSheetName, is("Tagesschule test"));
		}

		@Test
		void shouldReplacePlusWithEmptySpace_whenProposedNameHasPlus() {
			var name = "Tagesschule+test";

			Workbook workbook = new XSSFWorkbook();
			workbook.createSheet(name);

			String uniqueSheetName = ReportUtil.createUniqueSheetName(
				workbook,
				name
			);

			assertThat(uniqueSheetName, is("Tagesschule test"));
		}
	}

	@Nested
	class CopySheetTest {
		@ParameterizedTest
		@MethodSource("sheetNameScenarios")
		void copySheet_shouldCopyAndRewriteTemplateLocalName_whenTemplateNameExists(
			String newSheetName,
			String expectedFormula
		) throws IOException {
			try (Workbook workbook = new XSSFWorkbook()) {
				Sheet templateSheet = workbook.createSheet("Data");
				templateSheet.createRow(0).createCell(0).setCellValue("value");

				addFormulaNameToWorkbookForSheet(
					workbook,
					"myLocalName",
					"Data!$A$1",
					templateSheet
				);

				Sheet newSheet = workbook.createSheet(newSheetName);

				ReportUtil.copySheet(workbook, templateSheet, newSheet);

				List<? extends Name> copiedNames = getNamesOfSheet(
					workbook,
					newSheet
				);

				assertEquals(1, copiedNames.size());
				assertEquals(
					expectedFormula,
					copiedNames.get(0).getRefersToFormula()
				);
			}
		}

		// Names belong to workbook, not sheets. The link to the sheet is by the sheet index
		private static List<? extends Name> getNamesOfSheet(
			Workbook workbook,
			Sheet newSheet
		) {
			return workbook.getAllNames()
				.stream()
				.filter(
					name -> name.getSheetIndex()
						== workbook.getSheetIndex(newSheet)
				)
				.toList();
		}

		@Test
		void copySheet_shouldCopyOnlyTemplateLocalNames_whenWorkbookContainsLocalAndGlobalNames()
			throws IOException {
			try (Workbook workbook = new XSSFWorkbook()) {
				Sheet templateSheet = workbook.createSheet("Template");
				templateSheet.createRow(0).createCell(0).setCellValue("value");
				Sheet otherSheet = workbook.createSheet("Other");

				addFormulaNameToWorkbookForSheet(
					workbook,
					"templateLocal",
					"Template!$A$1",
					templateSheet
				);

				addFormulaNameToWorkbookForSheet(
					workbook,
					"otherLocal",
					"Other!$A$1",
					otherSheet
				);

				Name globalName = workbook.createName();
				globalName.setNameName("globalName");
				globalName.setRefersToFormula("Template!$A$1");

				Sheet newSheet = workbook.createSheet("Copy");

				ReportUtil.copySheet(workbook, templateSheet, newSheet);

				List<? extends Name> names = getNamesOfSheet(
					workbook,
					newSheet
				);

				assertEquals(1, names.size());
				assertTrue(
					names.stream()
						.allMatch(
							name -> "templateLocal".equals(name.getNameName())
						)
				);
			}
		}

		/**
		 * <p>
		 * Provides a stream of arguments for test scenarios related to sheet names
		 * and their corresponding formulas in an Excel workbook.
		 * </p>
		 * <p>
		 * Each argument consists of a sheet name and the expected formula format,
		 * appropriately handling cases with or without special characters like
		 * apostrophes in sheet names.
		 * </p>
		 *
		 * @return a stream of arguments where each argument is a pair of a sheet name
		 * and its expected formula string representation
		 */
		private static Stream<Arguments> sheetNameScenarios() {
			return Stream.of(
				Arguments.of("Copy", "Copy!$A$1"),
				Arguments.of("Copyé", "Copyé!$A$1"),
				Arguments.of("Copy 2026", "'Copy 2026'!$A$1"),
				Arguments.of("Copyê 2026", "'Copyê 2026'!$A$1")
			);
		}
	}

	@Nested
	class RemoveSheetTest {
		@Test
		void removeSheet_shouldRemoveSheetFromWorkbook() throws IOException {
			try (Workbook workbook = new XSSFWorkbook()) {
				Sheet sheet = workbook.createSheet("ToRemove");
				assertEquals(1, workbook.getNumberOfSheets());

				ReportUtil.removeSheet(workbook, sheet);

				assertEquals(0, workbook.getNumberOfSheets());
			}
		}

		@Test
		void removeSheet_shouldRemoveNamesAssociatedWithSheet()
			throws IOException {
			try (Workbook workbook = new XSSFWorkbook()) {
				Sheet sheet = workbook.createSheet("ToRemove");
				addFormulaNameToWorkbookForSheet(
					workbook,
					"localName",
					"ToRemove!$A$1",
					sheet
				);

				ReportUtil.removeSheet(workbook, sheet);

				assertTrue(workbook.getAllNames().isEmpty());
			}
		}

		@Test
		void removeSheet_shouldThrowException_whenSheetNotInWorkbook()
			throws IOException {
			try (Workbook workbook = new XSSFWorkbook();
				Workbook otherWorkbook = new XSSFWorkbook()) {
				Sheet otherSheet = otherWorkbook.createSheet("Other");

				assertThrows(
					IllegalArgumentException.class,
					() -> ReportUtil.removeSheet(workbook, otherSheet)
				);
			}
		}

		@Test
		void removeSheet_shouldNotRemoveNamesOfOtherSheets()
			throws IOException {
			try (Workbook workbook = new XSSFWorkbook()) {
				Sheet toRemove = workbook.createSheet("ToRemove");
				Sheet toKeep = workbook.createSheet("ToKeep");

				addFormulaNameToWorkbookForSheet(
					workbook,
					"localToRemove",
					"ToRemove!$A$1",
					toRemove
				);
				addFormulaNameToWorkbookForSheet(
					workbook,
					"localToKeep",
					"ToKeep!$A$1",
					toKeep
				);

				ReportUtil.removeSheet(workbook, toRemove);

				List<? extends Name> remainingNames = workbook.getAllNames();
				assertEquals(1, remainingNames.size());
				assertEquals(
					"localToKeep",
					remainingNames.get(0).getNameName()
				);
			}
		}
	}

	private static void addFormulaNameToWorkbookForSheet(
		Workbook workbook,
		String nameName,
		String formulaText,
		Sheet templateSheet
	) {
		Name templateLocalName = workbook.createName();
		templateLocalName.setNameName(nameName);
		templateLocalName.setRefersToFormula(formulaText);
		templateLocalName.setSheetIndex(
			workbook.getSheetIndex(templateSheet)
		);
	}

}
