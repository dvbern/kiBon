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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ch.dvbern.ebegu.reporting.zahlungauftrag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.enums.reporting.MergeFieldZahlungAuftrag;
import ch.dvbern.ebegu.reporting.zahlungsauftrag.ZahlungDataRow;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class ZahlungAuftragTotalsExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
		sheet.autoSizeColumn(0); // institution
		sheet.autoSizeColumn(1); // idInstitution
		sheet.autoSizeColumn(2); // angebot
		sheet.autoSizeColumn(3); // traegerschaft
		sheet.autoSizeColumn(4); // betrag
		sheet.autoSizeColumn(5); // iban
		sheet.autoSizeColumn(6); // kontoinhaber
		sheet.autoSizeColumn(7); // anschrift
		sheet.autoSizeColumn(8); // strasse
		sheet.autoSizeColumn(9); // hausnummer
		sheet.autoSizeColumn(10); // plz
		sheet.autoSizeColumn(11); // ort
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(
		@Nonnull List<ZahlungDataRow> zahlungenBerechtigt,
		@Nonnull Locale locale,
		@Nonnull String beschrieb,
		@Nonnull LocalDateTime datumGeneriert,
		@Nonnull LocalDate datumFaellig,
		@Nonnull Gemeinde gemeinde
	) {
		checkNotNull(zahlungenBerechtigt);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		addHeaders(excelMerger, locale);

		excelMerger.addValue(MergeFieldZahlungAuftrag.beschrieb, beschrieb);
		excelMerger.addValue(MergeFieldZahlungAuftrag.generiertAm, datumGeneriert);
		excelMerger.addValue(MergeFieldZahlungAuftrag.faelligAm, datumFaellig);
		excelMerger.addValue(MergeFieldZahlungAuftrag.gemeinde, gemeinde.getName());

		zahlungenBerechtigt.stream()
			.sorted()
			.forEach(zahlungDataRow -> {
				ExcelMergerDTO excelRowGroup = excelMerger.createGroup(MergeFieldZahlungAuftrag.repeatZahlungTotalsRow);
				final String traegerschaft = zahlungDataRow.getZahlung().getTraegerschaftName();

				final Zahlung zahlung = zahlungDataRow.getZahlung();
				final IBAN iban = zahlung.getAuszahlungsdaten().getIban();

				excelRowGroup.addValue(MergeFieldZahlungAuftrag.institution, zahlung.getEmpfaengerName());
				excelRowGroup.addValue(MergeFieldZahlungAuftrag.institutionId, zahlung.getEmpfaengerId());
				excelRowGroup.addValue(MergeFieldZahlungAuftrag.betreuungsangebotTyp,
					ServerMessageUtil.translateEnumValue(zahlung.getBetreuungsangebotTyp(), locale));
				if (traegerschaft != null) {
					excelRowGroup.addValue(MergeFieldZahlungAuftrag.traegerschaft, traegerschaft);
				}
				excelRowGroup.addValue(MergeFieldZahlungAuftrag.betragAusbezahlt, zahlung.getBetragTotalZahlung());
				excelRowGroup.addValue(MergeFieldZahlungAuftrag.iban, EbeguUtil.removeWhiteSpaces(iban.getIban()));
				excelRowGroup.addValue(MergeFieldZahlungAuftrag.kontoinhaber, zahlung.getAuszahlungsdaten().getKontoinhaber());
				Adresse adresse = zahlung.getAuszahlungsdaten().getAdresseKontoinhaber();
				if (adresse == null) {
					adresse = zahlungDataRow.getDefaultAdresseKontoinhaber();
				}
				// Jetzt muss eine Adresse vorhanden sein (die aus den Auszahlungsdaten oder die Defaultadresse
				Objects.requireNonNull(adresse);

				excelRowGroup.addValue(MergeFieldZahlungAuftrag.organisation, adresse.getOrganisation());
				excelRowGroup.addValue(MergeFieldZahlungAuftrag.strasse, adresse.getStrasse());
				excelRowGroup.addValue(MergeFieldZahlungAuftrag.hausnummer, adresse.getHausnummer());
				excelRowGroup.addValue(MergeFieldZahlungAuftrag.plz, adresse.getPlz());
				excelRowGroup.addValue(MergeFieldZahlungAuftrag.ort, adresse.getOrt());
			});
		return excelMerger;
	}

	private void addHeaders(@Nonnull ExcelMergerDTO excelMerger, @Nonnull Locale locale) {
		excelMerger.addValue(MergeFieldZahlungAuftrag.generiertAmTitle, ServerMessageUtil.getMessage("Reports_generiertAmTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.faelligAmTitle, ServerMessageUtil.getMessage("Reports_faelligAmTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.gemeindeTitle, ServerMessageUtil.getMessage("Reports_gemeindeTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.institutionTitle, ServerMessageUtil.getMessage("Reports_institutionNameTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.institutionIdTitle, ServerMessageUtil.getMessage("Reports_institutionIdTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.betreuungsangebotTypTitle, ServerMessageUtil.getMessage("Reports_betreuungsangebotTypTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.traegerschaftTitle, ServerMessageUtil.getMessage("Reports_traegerschaftTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.auszahlungTitle, ServerMessageUtil.getMessage("Reports_auszahlungTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.betragAusbezahltTitle, ServerMessageUtil.getMessage("Reports_betragAusbezahltTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.ibanTitle, ServerMessageUtil.getMessage("Reports_ibanTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.kontoinhaberTitle, ServerMessageUtil.getMessage("Reports_kontoinhaberTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.organisationTitle, ServerMessageUtil.getMessage("Reports_organisationTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.strasseTitle, ServerMessageUtil.getMessage("Reports_strasseTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.hausnummerTitle, ServerMessageUtil.getMessage("Reports_hausnummerTitle", locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.plzTitle, ServerMessageUtil.getMessage("Reports_plzTitle",
			locale).toUpperCase(locale));
		excelMerger.addValue(MergeFieldZahlungAuftrag.ortTitle, ServerMessageUtil.getMessage("Reports_ortTitle", locale));
	}
}
