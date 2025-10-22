/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.reporting.lastenausgleich;

import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.CsvCreator;

import static com.google.common.base.Preconditions.checkNotNull;

public class LastenausgleichBGZeitabschnitteCSVConverter {
	private CsvCreator csvHelper = new CsvCreator();

	@Nonnull
	public String createLastenausgleichBGZeitabschnitteCSVHeader() {
		StringBuilder csvString = new StringBuilder();

		String header = generateCSVHeader();
		csvString.append(appendNewLine(header));

		return csvString.toString();
	}

	@Nonnull
	public String createLastenausgleichBGZeitabschnitteCSV(
		@Nonnull List<LastenausgleichBGZeitabschnittDataRow> data
	) {
		checkNotNull(data);

		StringBuilder csvString = new StringBuilder();

		data.stream()
			.map(this::dataRowToCSV)
			.map(this::appendNewLine)
			.forEach(csvString::append);

		return csvString.toString();
	}

	private String generateCSVHeader() {
		return csvHelper.convertToCSVLine(
			new String[] {
				"Referenznummer",
				"BFS Nummer",
				"Gemeinde",
				"Nachname",
				"Vorname",
				"Geburtsdatum",
				"Von",
				"Bis",
				"Institution",
				"Angebot",
				"BG-Pensum",
				"Kein Selbstbehalt durch Gemeinde",
				"Gutschein",
				"Korrektur",
			}
		);
	}

	private String dataRowToCSV(LastenausgleichBGZeitabschnittDataRow dataRow) {
		return csvHelper.convertToCSVLine(
			new String[] {
				dataRow.getReferenzNummer(),
				String.valueOf(dataRow.getBfsNummer()),
				dataRow.getNameGemeinde(),
				dataRow.getNachname(),
				dataRow.getVorname(),
				dataRow.getGeburtsdatum().format(Constants.DATE_FORMATTER),
				dataRow.getVon().format(Constants.DATE_FORMATTER),
				dataRow.getBis().format(Constants.DATE_FORMATTER),
				dataRow.getInstitution(),
				dataRow.getBetreuungsangebotTypTranslated(),
				dataRow.getBgPensum().toString(),
				Boolean.TRUE.equals(
					dataRow.getKeinSelbstbehaltDurchGemeinde()
				) ?
					"X" :
					"",
				dataRow.getGutschein() != null ?
					dataRow.getGutschein().toString() :
					"0",
				Boolean.TRUE.equals(dataRow.getIsKorrektur()) ?
					"X" :
					""
			}
		);
	}

	private String appendNewLine(String csvString) {
		return csvString + Constants.CSV_NEW_LINE;
	}
}
