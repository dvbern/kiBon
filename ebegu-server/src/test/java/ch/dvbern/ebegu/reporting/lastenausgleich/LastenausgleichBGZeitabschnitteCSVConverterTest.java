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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LastenausgleichBGZeitabschnitteCSVConverterTest {

	LastenausgleichBGZeitabschnitteCSVConverter lastenausgleichBGZeitabschnitteCSVConverter =
		new LastenausgleichBGZeitabschnitteCSVConverter();

	@Test
	void createLastenausgleichBGZeitabschnitteCSVHeaderTest() {
		String headerErwartet =
			"Referenznummer;BFS Nummer;Gemeinde;Nachname;Vorname;Geburtsdatum;Von;Bis;Institution;Angebot;BG-Pensum;Kein Selbstbehalt durch Gemeinde;Gutschein;Korrektur\n";
		String header = lastenausgleichBGZeitabschnitteCSVConverter
			.createLastenausgleichBGZeitabschnitteCSVHeader();
		assertThat(header, is(headerErwartet));
	}

	@Test
	void createLastenausgleichBGZeitabschnitteCSV() {
		String gemeindeName = "Test Gemeinde";
		String referenznummer = "24.000101.01.1";
		String escapeKarakter = ";";
		long bfsNummer = 12345L;
		String kindVorname = "Kind Vorname";
		String kindNachname = "Kind Nachname";
		String institution = "Institution";
		String angebotTranslated = "Kita";
		List<LastenausgleichBGZeitabschnittDataRow> lastenausgleichBGZeitabschnittDataRowList =
			new ArrayList<>();
		LastenausgleichBGZeitabschnittDataRow lastenausgleichBGZeitabschnittDataRow =
			new LastenausgleichBGZeitabschnittDataRow();
		lastenausgleichBGZeitabschnittDataRow.setReferenzNummer(referenznummer);
		lastenausgleichBGZeitabschnittDataRow.setNameGemeinde(gemeindeName);
		lastenausgleichBGZeitabschnittDataRow.setBfsNummer(bfsNummer);
		lastenausgleichBGZeitabschnittDataRow.setGutschein(BigDecimal.TEN);
		lastenausgleichBGZeitabschnittDataRow.setIsKorrektur(true);
		lastenausgleichBGZeitabschnittDataRow.setBgPensum(BigDecimal.ONE);
		lastenausgleichBGZeitabschnittDataRow.setVorname(kindVorname);
		lastenausgleichBGZeitabschnittDataRow.setNachname(kindNachname);
		lastenausgleichBGZeitabschnittDataRow.setBetreuungsangebotTyp(
			BetreuungsangebotTyp.KITA
		);
		lastenausgleichBGZeitabschnittDataRow.setBetreuungsangebotTypTranslated(
			angebotTranslated
		);
		lastenausgleichBGZeitabschnittDataRow.setInstitution(institution);
		lastenausgleichBGZeitabschnittDataRow.setKeinSelbstbehaltDurchGemeinde(
			true
		);
		lastenausgleichBGZeitabschnittDataRow.setGeburtsdatum(
			LocalDate.of(2020, 12, 24)
		);
		lastenausgleichBGZeitabschnittDataRow.setVon(LocalDate.of(2024, 12, 1));
		lastenausgleichBGZeitabschnittDataRow.setBis(
			LocalDate.of(2024, 12, 31)
		);
		lastenausgleichBGZeitabschnittDataRowList.add(
			lastenausgleichBGZeitabschnittDataRow
		);
		String reportLineErwartet = new StringBuilder()
			.append(referenznummer)
			.append(escapeKarakter)
			.append(bfsNummer)
			.append(escapeKarakter)
			.append(gemeindeName)
			.append(escapeKarakter)
			.append(kindNachname)
			.append(escapeKarakter)
			.append(kindVorname)
			.append(escapeKarakter)
			.append("24.12.2020")
			.append(escapeKarakter)
			.append("01.12.2024")
			.append(escapeKarakter)
			.append("31.12.2024")
			.append(escapeKarakter)
			.append(institution)
			.append(escapeKarakter)
			.append(angebotTranslated)
			.append(escapeKarakter)
			.append(BigDecimal.ONE)
			.append(escapeKarakter)
			.append("X")
			.append(escapeKarakter)
			.append(BigDecimal.TEN)
			.append(escapeKarakter)
			.append("X")
			.append("\n")
			.toString();

		String report = lastenausgleichBGZeitabschnitteCSVConverter
			.createLastenausgleichBGZeitabschnitteCSV(
				lastenausgleichBGZeitabschnittDataRowList
			);
		assertThat(report, is(reportLineErwartet));
	}
}
