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
 *
 */

package ch.dvbern.ebegu.services.reporting.zemis;

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * Util for parsing the zemis
 */
public class ZemisExcelParserUtils {

	/**
	 * This method will parse the whole excel Zemis List and return for each row
	 * a new record with the necessary information to update our Zemis for the LAS
	 *
	 * @param sheet the sheet to read
	 * @param firstRelevantRow the first row containing data to work with
	 * @return
	 */
	public static List<ZemisImportRow> readExcel(
		Sheet sheet,
		int firstRelevantRow
	) {
		List<ZemisImportRow> rows = new ArrayList<>();

		for (Row row : sheet) {
			if (row.getRowNum() < firstRelevantRow) {
				continue;
			}

			try {
				rows.add(
					new ZemisImportRow(
						(int) row.getCell(0).getNumericCellValue(),
						(int) row.getCell(5).getNumericCellValue(),
						parseGesuchsperiodeStartJahr(row),
						row.getCell(8).getBooleanCellValue(),
						row.getCell(2).getStringCellValue()
					)
				);
			} catch (RuntimeException e) {
				throw new EbeguRuntimeException(
					"readExcel",
					ErrorCodeEnum.ERROR_WRONG_FORMAT_ZEMIS,
					e,
					row.getRowNum() + 1
				);
			}
		}

		return rows;
	}

	private static int parseGesuchsperiodeStartJahr(Row row) {
		String gesuchsperiodeStr = row.getCell(1).getStringCellValue();
		return Integer.parseInt(
			gesuchsperiodeStr.split("/")[0]
		);
	}
}
