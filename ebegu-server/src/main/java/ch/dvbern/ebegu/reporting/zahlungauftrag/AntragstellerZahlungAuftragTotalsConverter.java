package ch.dvbern.ebegu.reporting.zahlungauftrag;

import java.util.Locale;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.reporting.MergeFieldZahlungAuftrag;
import ch.dvbern.ebegu.reporting.zahlungsauftrag.ZahlungDataRow;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;

public class AntragstellerZahlungAuftragTotalsConverter extends
	AbstractZahlungAuftragTotalsConverter {

	public AntragstellerZahlungAuftragTotalsConverter(
		Locale locale,
		Mandant mandant
	) {
		super(locale, mandant);
	}

	@Override
	public void addHeaders(ExcelMergerDTO excelMerger) {
		super.addHeaders(excelMerger);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.antragstellerTitle,
			ServerMessageUtil.getMessage(
				"Reports_antragstellerTitle",
				getLocale(),
				getMandant()
			)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.antragsteller2Title,
			ServerMessageUtil.getMessage(
				"Reports_antragsteller2Title",
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
		excelRowGroup.addValue(
			MergeFieldZahlungAuftrag.antragsteller,
			zahlungDataRow.getZahlung().getEmpfaengerName()
		);
		excelRowGroup.addValue(
			MergeFieldZahlungAuftrag.antragsteller2,
			zahlungDataRow.getZahlung().getEmpfaenger2Name()
		);

	}

}
