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
package ch.dvbern.ebegu.reporting.verrechnungKibon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.reporting.MergeFieldVerrechnungKibon;
import ch.dvbern.oss.lib.excelmerger.ExcelConverter;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.poi.ss.usermodel.Sheet;

import static com.google.common.base.Preconditions.checkNotNull;

public class VerrechnungKibonExcelConverter implements ExcelConverter {

	@Override
	public void applyAutoSize(@Nonnull Sheet sheet) {
	}

	@Nonnull
	public ExcelMergerDTO toExcelMergerDTO(@Nonnull List<VerrechnungKibonDataRow> data) {
		checkNotNull(data);

		ExcelMergerDTO excelMerger = new ExcelMergerDTO();

		BigDecimal betragProKind = data.isEmpty() ? BigDecimal.ZERO : data.get(0).getBetragProKind();
		excelMerger.addValue(MergeFieldVerrechnungKibon.datumErstellt, LocalDate.now());
		excelMerger.addValue(MergeFieldVerrechnungKibon.betragProKind, betragProKind);

		data.forEach(dataRow -> {
			ExcelMergerDTO fallRowGroup = excelMerger.createGroup(MergeFieldVerrechnungKibon.repeatRow);
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.gesuchsperiode, dataRow.getGesuchsperiode());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.gemeinde, dataRow.getGemeinde());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.kinderKantonTotal, dataRow.getKinderKantonTotal());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.kinderKantonBereitsVerrechnet, dataRow.getKinderKantonBereitsVerrechnet());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.kinderBgTotal, dataRow.getKinderBgTotal());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.kinderBgBereitsVerrechnet, dataRow.getKinderBgBereitsVerrechnet());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.kinderTsTotal, dataRow.getKinderTsTotal());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.kinderTsBereitsVerrechnet, dataRow.getKinderTsBereitsVerrechnet());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.kinderKeinAngebotTotal, dataRow.getKinderKeinAngebotTotal());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.kinderKeinAngebotBereitsVerrechnet, dataRow.getKinderKeinAngebotBereitsVerrechnet());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.kinderGemeindeTotal, dataRow.getKinderGemeindeTotal());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.kinderGemeindeBereitsVerrechnet, dataRow.getKinderGemeindeBereitsVerrechnet());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.kinderFiTotal, dataRow.getKinderFiTotal());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.kinderFiBereitsVerrechnet, dataRow.getKinderFiBereitsVerrechnet());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.kinderTagiTotal, dataRow.getKinderTagiTotal());
			fallRowGroup.addValue(MergeFieldVerrechnungKibon.kinderTagiBereitsVerrechnet, dataRow.getKinderTagiBereitsVerrechnet());
		});

		return excelMerger;
	}
}
