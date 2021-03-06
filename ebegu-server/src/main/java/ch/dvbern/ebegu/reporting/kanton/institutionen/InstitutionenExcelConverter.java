/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.reporting.kanton.institutionen;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.enums.reporting.MergeFieldInstitutionen;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

@Dependent
public class InstitutionenExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(@Nonnull List<InstitutionenDataRow> data, @Nonnull Locale locale) {
		checkNotNull(data);

		ExcelMergerDTO mergerDTO = new ExcelMergerDTO();

		addHeaders(mergerDTO, locale);

		data.forEach(dataRow -> {
			ExcelMergerDTO excelRowGroup = mergerDTO.createGroup(MergeFieldInstitutionen.repeatInstitutionenRow);
			excelRowGroup.addValue(MergeFieldInstitutionen.typ, dataRow.getTyp());
			excelRowGroup.addValue(MergeFieldInstitutionen.traegerschaft, dataRow.getTraegerschaft());
			excelRowGroup.addValue(MergeFieldInstitutionen.name, dataRow.getName());
			excelRowGroup.addValue(MergeFieldInstitutionen.anschrift, dataRow.getAnschrift());
			excelRowGroup.addValue(MergeFieldInstitutionen.strasse, dataRow.getStrasse());
			excelRowGroup.addValue(MergeFieldInstitutionen.plz, dataRow.getPlz());
			excelRowGroup.addValue(MergeFieldInstitutionen.ort, dataRow.getOrt());
			excelRowGroup.addValue(MergeFieldInstitutionen.telefon, dataRow.getTelefon());
			excelRowGroup.addValue(MergeFieldInstitutionen.email, dataRow.getEmail());
			excelRowGroup.addValue(MergeFieldInstitutionen.url, dataRow.getUrl());
			excelRowGroup.addValue(MergeFieldInstitutionen.oeffnungstage, dataRow.getOeffnungstage());
			excelRowGroup.addValue(MergeFieldInstitutionen.gueltigAb, dataRow.getGueltigAb());
			excelRowGroup.addValue(MergeFieldInstitutionen.gueltigBis, dataRow.getGueltigBis());
			excelRowGroup.addValue(MergeFieldInstitutionen.oeffnungszeiten, dataRow.getOeffnungszeiten());
			excelRowGroup.addValue(MergeFieldInstitutionen.oeffnungsAbweichungen, dataRow.getOeffnungsAbweichungen());
			excelRowGroup.addValue(MergeFieldInstitutionen.isBaby, dataRow.getBaby());
			excelRowGroup.addValue(MergeFieldInstitutionen.isVorschulkind, dataRow.getVorschulkind());
			excelRowGroup.addValue(MergeFieldInstitutionen.isKindergarten, dataRow.getKindergarten());
			excelRowGroup.addValue(MergeFieldInstitutionen.isSchulkind, dataRow.getSchulkind());
			excelRowGroup.addValue(MergeFieldInstitutionen.subventioniert, dataRow.getSubventioniert());
			excelRowGroup.addValue(MergeFieldInstitutionen.kapazitaet, dataRow.getKapazitaet());
			excelRowGroup.addValue(MergeFieldInstitutionen.reserviertFuerFirmen, dataRow.getReserviertFuerFirmen());
			excelRowGroup.addValue(MergeFieldInstitutionen.zuletztGeaendert, dataRow.getZuletztGeaendert());
		});
		return mergerDTO;
	}

	private void addHeaders(@Nonnull ExcelMergerDTO mergerDTO, @Nonnull Locale locale) {
		mergerDTO.addValue(MergeFieldInstitutionen.typTitle, ServerMessageUtil.getMessage("Reports_typTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.traegerschaftTitle,
			ServerMessageUtil.getMessage("Reports_traegerschaftTitle", locale));
		mergerDTO.addValue(MergeFieldInstitutionen.nameTitle, ServerMessageUtil.getMessage("Reports_nameTitle", locale));
		mergerDTO.addValue(MergeFieldInstitutionen.anschriftTitle, ServerMessageUtil.getMessage("Reports_anschriftTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.strasseTitle,
			ServerMessageUtil.getMessage("Reports_strasseTitle", locale));
		mergerDTO.addValue(MergeFieldInstitutionen.plzTitle, ServerMessageUtil.getMessage("Reports_plzTitle", locale));
		mergerDTO.addValue(MergeFieldInstitutionen.ortTitle, ServerMessageUtil.getMessage("Reports_ortTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.telefonTitle,
			ServerMessageUtil.getMessage("Reports_telefonTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.emailTitle,
			ServerMessageUtil.getMessage("Reports_emailTitle", locale));
		mergerDTO.addValue(MergeFieldInstitutionen.urlTitle, ServerMessageUtil.getMessage("Reports_urlTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.gueltigAbTitle,
			ServerMessageUtil.getMessage("Reports_gueltigAbTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.gueltigBisTitle,
			ServerMessageUtil.getMessage("Reports_gueltigBisTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.oeffnungstageTitle,
			ServerMessageUtil.getMessage("Reports_oeffnungstageTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.oeffnungszeitenTitle,
			ServerMessageUtil.getMessage("Reports_oeffnungszeitenTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.oeffnungsAbweichungenTitle,
			ServerMessageUtil.getMessage("Reports_oeffnungsabweichungenTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.babyTitle,
			ServerMessageUtil.getMessage("Reports_babyTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.vorschulkindTitle,
			ServerMessageUtil.getMessage("Reports_vorschulkindTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.kindergartenTitle,
			ServerMessageUtil.getMessage("Reports_kindergartenTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.schulkindTitle,
			ServerMessageUtil.getMessage("Reports_schulkindTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.subventioniertTitle,
			ServerMessageUtil.getMessage("Reports_subventioniertTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.kapazitaetTitle,
			ServerMessageUtil.getMessage("Reports_kapazitaetTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.reserviertFuerFirmenTitle,
			ServerMessageUtil.getMessage("Reports_reserviertFuerFirmenTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.zuletztGeaendertTitle,
			ServerMessageUtil.getMessage("Reports_zuletztGeaendertTitle", locale));
		mergerDTO.addValue(
			MergeFieldInstitutionen.reportInstitutionenTitle,
			ServerMessageUtil.getMessage("Reports_reportInstitutionenTitle", locale));
	}
}
