/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
package ch.dvbern.ebegu.reporting.zahlungauftrag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.enums.reporting.MergeFieldZahlungAuftrag;
import ch.dvbern.ebegu.reporting.zahlungsauftrag.ZahlungDataRow;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class ZahlungAuftragTotalsExcelConverter implements ExcelConverter {

	public static final String EMPTY_STRING = "";

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
		sheet.autoSizeColumn(0); // institution
		sheet.autoSizeColumn(1); // idInstitution
		sheet.autoSizeColumn(2); // angebot
		sheet.autoSizeColumn(3); // traegerschaft
		sheet.autoSizeColumn(4); // antragsteller 1
		sheet.autoSizeColumn(5); // antragsteller 2
		sheet.autoSizeColumn(6); // betrag
		sheet.autoSizeColumn(7); // iban
		sheet.autoSizeColumn(8); // kontoinhaber
		sheet.autoSizeColumn(9); // anschrift
		sheet.autoSizeColumn(10); //strasse
		sheet.autoSizeColumn(11); //hausnummer
		sheet.autoSizeColumn(12); // plz
		sheet.autoSizeColumn(13); // ort
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<ZahlungDataRow> zahlungenBerechtigt,
		@Nonnull Locale locale,
		@Nonnull String beschrieb,
		@Nonnull LocalDateTime datumGeneriert,
		@Nonnull LocalDate datumFaellig,
		@Nonnull Gemeinde gemeinde,
		@Nonnull ZahlungslaufTyp zahlungslaufTyp
	) {
		checkNotNull(zahlungenBerechtigt);
		Objects.requireNonNull(gemeinde.getMandant());

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		AbstractZahlungAuftragTotalsConverter converter =
			getConverterByZahlunglauftyp(
				zahlungslaufTyp,
				locale,
				gemeinde.getMandant()
			);
		converter.addHeaders(excelMerger);

		excelMerger.addValue(MergeFieldZahlungAuftrag.beschrieb, beschrieb);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.generiertAm,
			datumGeneriert
		);
		excelMerger.addValue(MergeFieldZahlungAuftrag.faelligAm, datumFaellig);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.gemeinde,
			gemeinde.getName()
		);

		zahlungenBerechtigt.stream()
			.sorted()
			.forEach(zahlungDataRow -> {
				ExcelMergerDTO excelRowGroup = excelMerger.createGroup(
					MergeFieldZahlungAuftrag.repeatZahlungTotalsRow
				);
				converter.addDataRow(zahlungDataRow, excelRowGroup);
			});
		return excelMerger;
	}

	public AbstractZahlungAuftragTotalsConverter getConverterByZahlunglauftyp(
		ZahlungslaufTyp zahlungslaufTyp,
		Locale locale,
		Mandant mandant
	) {
		return zahlungslaufTyp == ZahlungslaufTyp.GEMEINDE_INSTITUTION ?
			new InstitutionZahlungAuftragTotalsConverter(locale, mandant) :
			new AntragstellerZahlungAuftragTotalsConverter(locale, mandant);
	}

	public void hideColumnsIfNecessary(
		@Nonnull Sheet sheet,
		ZahlungslaufTyp zahlungslaufTyp
	) {
		if (zahlungslaufTyp == ZahlungslaufTyp.GEMEINDE_ANTRAGSTELLER) {
			sheet.setColumnHidden(2, true); // column Betreuungsangebot
			sheet.setColumnHidden(3, true); // column Traegerschaft
		}
	}
}
