package ch.dvbern.ebegu.reporting.zahlungauftrag;

import java.util.Locale;

import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.reporting.MergeFieldZahlungAuftrag;
import ch.dvbern.ebegu.reporting.zahlungsauftrag.ZahlungDataRow;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;

public class InstitutionZahlungAuftragTotalsConverter extends
	AbstractZahlungAuftragTotalsConverter {

	public InstitutionZahlungAuftragTotalsConverter(
		Locale locale,
		Mandant mandant
	) {
		super(locale, mandant);
	}

	@Override
	public void addHeaders(ExcelMergerDTO excelMerger) {
		super.addHeaders(excelMerger);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.institutionTitle,
			ServerMessageUtil.getMessage(
				"Reports_institutionNameTitle",
				getLocale(),
				getMandant()
			)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.institutionIdTitle,
			ServerMessageUtil.getMessage(
				"Reports_institutionIdTitle",
				getLocale(),
				getMandant()
			)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.betreuungsangebotTypTitle,
			ServerMessageUtil.getMessage(
				"Reports_betreuungsangebotTypTitle",
				getLocale(),
				getMandant()
			)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.traegerschaftTitle,
			ServerMessageUtil.getMessage(
				"Reports_traegerschaftTitle",
				getLocale(),
				getMandant()
			)
		);
	}

	@Override
	public void addDataRow(
		ZahlungDataRow zahlungDataRow,
		ExcelMergerDTO excelRowGroup
	) {
		super.addDataRow(zahlungDataRow, excelRowGroup);

		final Institution institution = zahlungDataRow.getZahlung()
			.extractInstitution();
		final String traegerschaft = zahlungDataRow.getZahlung()
			.getTraegerschaftName();

		excelRowGroup.addValue(
			MergeFieldZahlungAuftrag.institution,
			institution.getName()
		);
		excelRowGroup.addValue(
			MergeFieldZahlungAuftrag.institutionId,
			institution.getId()
		);
		excelRowGroup.addValue(
			MergeFieldZahlungAuftrag.betreuungsangebotTyp,
			ServerMessageUtil.translateEnumValue(
				zahlungDataRow.getZahlung().getBetreuungsangebotTyp(),
				getLocale(),
				getMandant()
			)
		);

		if (traegerschaft != null) {
			excelRowGroup.addValue(
				MergeFieldZahlungAuftrag.traegerschaft,
				traegerschaft
			);
		}
	}
}
