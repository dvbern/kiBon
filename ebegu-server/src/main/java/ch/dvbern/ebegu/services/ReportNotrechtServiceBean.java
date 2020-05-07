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

package ch.dvbern.ebegu.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.enums.reporting.ReportVorlage;
import ch.dvbern.ebegu.reporting.ReportNotrechtService;
import ch.dvbern.ebegu.reporting.notrecht.NotrechtDataRow;
import ch.dvbern.ebegu.reporting.notrecht.NotrechtExcelConverter;
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
@Local(ReportNotrechtService.class)
@RolesAllowed({SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
public class ReportNotrechtServiceBean extends AbstractReportServiceBean implements ReportNotrechtService {

	private NotrechtExcelConverter excelConverter = new NotrechtExcelConverter();

	@Inject
	private FileSaverService fileSaverService;


	@Nonnull
	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public List<NotrechtDataRow> getReportNotrecht(boolean zahlungenAusloesen) {
		List<NotrechtDataRow> formulare = new ArrayList<>();
		List<RueckforderungFormular> auszuzahlendeFormulare = findAllAuszuzahlendeFormulare();
		// TODO (hefr) das Flag beachten!
		for (RueckforderungFormular rueckforderungFormular : auszuzahlendeFormulare) {
			NotrechtDataRow dataRow = convertToDataRow(rueckforderungFormular);
			formulare.add(dataRow);
		}
		return formulare;
	}

	@Nonnull
	private NotrechtDataRow convertToDataRow(@Nonnull RueckforderungFormular formular) {
		NotrechtDataRow row = new NotrechtDataRow();
		InstitutionStammdaten stammdaten = formular.getInstitutionStammdaten();
		Institution institution = stammdaten.getInstitution();

		row.setInstitution(institution.getName());
		row.setStatus(formular.getStatus());
		row.setBetreuungsangebotTyp(stammdaten.getBetreuungsangebotTyp());
		row.setTraegerschaft(institution.getTraegerschaft() != null ? institution.getTraegerschaft().getName() : null);
		row.setEmail(stammdaten.getMail());

		Adresse adresse = stammdaten.getAdresse();
		row.setAdresseOrganisation(adresse.getOrganisation());
		row.setAdresseStrasse(adresse.getStrasse());
		row.setAdresseHausnummer(adresse.getHausnummer());
		row.setAdressePlz(adresse.getPlz());
		row.setAdresseOrt(adresse.getOrt());

		row.setTelefon(stammdaten.getTelefon());

		row.setStufe1InstitutionKostenuebernahmeAnzahlTage(formular.getStufe1InstitutionKostenuebernahmeAnzahlTage());
		row.setStufe1InstitutionKostenuebernahmeAnzahlStunden(formular.getStufe1InstitutionKostenuebernahmeAnzahlStunden());
		row.setStufe1InstitutionKostenuebernahmeBetreuung(formular.getStufe1InstitutionKostenuebernahmeBetreuung());
		row.setStufe1KantonKostenuebernahmeAnzahlTage(formular.getStufe1KantonKostenuebernahmeAnzahlTage());
		row.setStufe1KantonKostenuebernahmeAnzahlStunden(formular.getStufe1KantonKostenuebernahmeAnzahlStunden());
		row.setStufe1KantonKostenuebernahmeBetreuung(formular.getStufe1KantonKostenuebernahmeBetreuung());

		row.setStufe1FreigabeBetrag(formular.getStufe1FreigabeBetrag());
		row.setStufe1FreigabeDatum(formular.getStufe1FreigabeDatum());
		row.setStufe1FreigabeAusbezahltAm(formular.getStufe1FreigabeAusbezahltAm());

		row.setStufe2InstitutionKostenuebernahmeAnzahlTage(formular.getStufe2InstitutionKostenuebernahmeAnzahlTage());
		row.setStufe2InstitutionKostenuebernahmeAnzahlStunden(formular.getStufe2InstitutionKostenuebernahmeAnzahlStunden());
		row.setStufe2InstitutionKostenuebernahmeBetreuung(formular.getStufe2InstitutionKostenuebernahmeBetreuung());
		row.setStufe2KantonKostenuebernahmeAnzahlTage(formular.getStufe2KantonKostenuebernahmeAnzahlTage());
		row.setStufe2KantonKostenuebernahmeAnzahlStunden(formular.getStufe2KantonKostenuebernahmeAnzahlStunden());
		row.setStufe2KantonKostenuebernahmeBetreuung(formular.getStufe2KantonKostenuebernahmeBetreuung());

		row.setStufe2VerfuegungBetrag(formular.getStufe2VerfuegungBetrag());
		row.setStufe2VerfuegungDatum(formular.getStufe2VerfuegungDatum());
		row.setStufe2VerfuegungAusbezahltAm(formular.getStufe2VerfuegungAusbezahltAm());

		InstitutionStammdatenBetreuungsgutscheine stammdatenBetreuungsgutscheine = stammdaten.getInstitutionStammdatenBetreuungsgutscheine();
		if (stammdatenBetreuungsgutscheine != null) {
			row.setIban(stammdatenBetreuungsgutscheine.getIban().getIban());
			row.setKontoinhaber(stammdatenBetreuungsgutscheine.getKontoinhaber());

			Adresse zahlungsadresse = stammdatenBetreuungsgutscheine.getAdresseKontoinhaber();
			if (zahlungsadresse != null) {
				row.setAuszahlungOrganisation(zahlungsadresse.getOrganisation());
				row.setAuszahlungStrasse(zahlungsadresse.getStrasse());
				row.setAuszahlungHausnummer(zahlungsadresse.getHausnummer());
				row.setAuszahlungPlz(zahlungsadresse.getPlz());
				row.setAuszahlungOrt(zahlungsadresse.getOrt());
			}
		}
		return row;
	}

	@Nonnull
	private List<RueckforderungFormular> findAllAuszuzahlendeFormulare() {
		List<RueckforderungFormular> auszuzahlendeFormulare = new ArrayList<>();
		RueckforderungFormular formular = new RueckforderungFormular();
//		formular.set
		return auszuzahlendeFormulare;
	}

	@Nonnull
	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	@TransactionTimeout(value = Constants.STATISTIK_TIMEOUT_MINUTES, unit = TimeUnit.MINUTES)
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public UploadFileInfo generateExcelReportNotrecht(boolean zahlungenAusloesen) throws ExcelMergeException {

		final ReportVorlage reportVorlage = ReportVorlage.VORLAGE_REPORT_NOTRECHT;

		InputStream is = ReportNotrechtServiceBean.class.getResourceAsStream(reportVorlage.getTemplatePath());
		Validate.notNull(is, VORLAGE + reportVorlage.getTemplatePath() + NICHT_GEFUNDEN);

		Workbook workbook = ExcelMerger.createWorkbookFromTemplate(is);
		Sheet sheet = workbook.getSheet(reportVorlage.getDataSheetName());

		List<NotrechtDataRow> reportData = getReportNotrecht(zahlungenAusloesen);
		ExcelMergerDTO excelMergerDTO = excelConverter.toExcelMergerDTO(reportData, zahlungenAusloesen);

		mergeData(sheet, excelMergerDTO, reportVorlage.getMergeFields());
		excelConverter.applyAutoSize(sheet);

		byte[] bytes = createWorkbook(workbook);

		return fileSaverService.save(bytes,
			ServerMessageUtil.translateEnumValue(reportVorlage.getDefaultExportFilename(), Locale.GERMAN) + ".xlsx",
			Constants.TEMP_REPORT_FOLDERNAME,
			getContentTypeForExport());
	}
}
