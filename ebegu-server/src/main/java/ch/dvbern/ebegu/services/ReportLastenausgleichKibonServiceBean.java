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

package ch.dvbern.ebegu.services;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.reporting.ReportLastenausgleichKibonService;
import ch.dvbern.ebegu.reporting.lastenausgleich.LastenausgleichKibonDataRow;
import ch.dvbern.ebegu.reporting.lastenausgleich.LastenausgleichKibonExcelConverter;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.excelmerger.ExcelMergeException;
import ch.dvbern.oss.lib.excelmerger.ExcelMerger;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import org.apache.commons.lang3.Validate;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jboss.ejb3.annotation.TransactionTimeout;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Stateless
@Local(ReportLastenausgleichKibonService.class)
@PermitAll
public class ReportLastenausgleichKibonServiceBean extends AbstractReportServiceBean implements ReportLastenausgleichKibonService {

	private LastenausgleichKibonExcelConverter lastenausgleichKibonExcelConverter = new LastenausgleichKibonExcelConverter();

	@Inject
	private VerfuegungService verfuegungService;

	@Inject
	private FileSaverService fileSaverService;

	@Nonnull
	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportLastenausgleichKibon(
		@Nonnull LocalDate dateFrom, @Nonnull Locale locale
	) throws ExcelMergeException {

		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_LASTENAUSGLEICH_KIBON;

		InputStream is = ReportMassenversandServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		Validate.notNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		List<LastenausgleichKibonDataRow> reportData = getReportLastenausgleichKibon(dateFrom);
		ExcelMergerDTO excelMergerDTO = lastenausgleichKibonExcelConverter
			.toExcelMergerDTO(reportData, dateFrom.getYear(), locale);

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		lastenausgleichKibonExcelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(bytes,
			ServerMessageUtil.translateEnumValue(reportVorlage.getDefaultExportFilename(), locale) + ".xlsx",
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}

	private List<LastenausgleichKibonDataRow> getReportLastenausgleichKibon(LocalDate dateFrom) {
		final List<VerfuegungZeitabschnitt> zeitabschnitteByYear = verfuegungService.findZeitabschnitteByYear(dateFrom.getYear());

		List<LastenausgleichKibonDataRow> allLastenausgleich = zeitabschnitteByYear.stream()
			.map(zeitabschnitt -> {
				final Betreuung betreuung = zeitabschnitt.getVerfuegung().getBetreuung();
				final Kind kindJA = betreuung.getKind().getKindJA();

				LastenausgleichKibonDataRow dataRow = new LastenausgleichKibonDataRow();

				dataRow.setBgNummer(betreuung.getBGNummer());
				dataRow.setKindName(kindJA.getNachname());
				dataRow.setKindVorname(kindJA.getVorname());
				dataRow.setKindGeburtsdatum(kindJA.getGeburtsdatum());
				dataRow.setZeitabschnittVon(zeitabschnitt.getGueltigkeit().getGueltigAb());
				dataRow.setZeitabschnittBis(zeitabschnitt.getGueltigkeit().getGueltigBis());
				dataRow.setBgPensum(zeitabschnitt.getBgPensum());
				dataRow.setInstitution(betreuung.getInstitutionStammdaten().getInstitution().getName());
				dataRow.setBetreuungsTyp(betreuung.getInstitutionStammdaten().getBetreuungsangebotTyp());
				dataRow.setTarif(kindJA.getEinschulungTyp());
				dataRow.setZusatz(betreuung.hasErweiterteBetreuung());
				dataRow.setGutschein(zeitabschnitt.getVerguenstigung());

				return dataRow;
			})
			.collect(Collectors.toList());

		return allLastenausgleich;
	}
}
