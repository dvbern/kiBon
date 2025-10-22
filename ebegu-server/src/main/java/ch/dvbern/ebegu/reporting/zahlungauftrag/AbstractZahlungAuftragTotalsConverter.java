package ch.dvbern.ebegu.reporting.zahlungauftrag;

import java.util.Locale;
import java.util.Objects;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Zahlung;
import ch.dvbern.ebegu.enums.reporting.MergeFieldZahlungAuftrag;
import ch.dvbern.ebegu.reporting.zahlungsauftrag.ZahlungDataRow;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.oss.lib.excelmerger.ExcelMergerDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public abstract class AbstractZahlungAuftragTotalsConverter {

	private Locale locale;
	private Mandant mandant;

	public void addHeaders(ExcelMergerDTO excelMerger) {
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.generiertAmTitle,
			ServerMessageUtil.getMessage(
				"Reports_generiertAmTitle",
				locale,
				mandant
			)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.faelligAmTitle,
			ServerMessageUtil.getMessage(
				"Reports_faelligAmTitle",
				locale,
				mandant
			)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.gemeindeTitle,
			ServerMessageUtil.getMessage(
				"Reports_gemeindeTitle",
				locale,
				mandant
			)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.auszahlungTitle,
			ServerMessageUtil.getMessage(
				"Reports_auszahlungTitle",
				locale,
				mandant
			)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.betragAusbezahltTitle,
			ServerMessageUtil.getMessage(
				"Reports_betragAusbezahltTitle",
				locale,
				mandant
			)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.ibanTitle,
			ServerMessageUtil.getMessage(
				"Reports_ibanTitle",
				locale,
				mandant
			)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.kontoinhaberTitle,
			ServerMessageUtil.getMessage(
				"Reports_kontoinhaberTitle",
				locale,
				mandant
			)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.organisationTitle,
			ServerMessageUtil.getMessage(
				"Reports_organisationTitle",
				locale,
				mandant
			)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.strasseTitle,
			ServerMessageUtil.getMessage(
				"Reports_strasseTitle",
				locale,
				mandant
			)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.hausnummerTitle,
			ServerMessageUtil.getMessage(
				"Reports_hausnummerTitle",
				locale,
				mandant
			)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.plzTitle,
			ServerMessageUtil.getMessage(
				"Reports_plzTitle",
				locale,
				mandant
			).toUpperCase(locale)
		);
		excelMerger.addValue(
			MergeFieldZahlungAuftrag.ortTitle,
			ServerMessageUtil.getMessage(
				"Reports_ortTitle",
				locale,
				mandant
			)
		);
	}

	public void addDataRow(
		ZahlungDataRow zahlungDataRow,
		ExcelMergerDTO excelRowGroup
	) {
		final Zahlung zahlung = zahlungDataRow.getZahlung();
		final Auszahlungsdaten auszahlungsdaten = zahlung.getAuszahlungsdaten();
		// "IBAN" ist entweder die tatsaechliche IBAN oder die InfomaKontonummer
		String kontonummer = auszahlungsdaten.getIbanOrInfomaKreditorennummer();
		excelRowGroup.addValue(
			MergeFieldZahlungAuftrag.betragAusbezahlt,
			zahlung.getBetragTotalZahlung()
		);
		excelRowGroup.addValue(
			MergeFieldZahlungAuftrag.iban,
			EbeguUtil.removeWhiteSpaces(kontonummer)
		);
		excelRowGroup.addValue(
			MergeFieldZahlungAuftrag.kontoinhaber,
			auszahlungsdaten.getKontoinhaber()
		);
		Adresse adresse = auszahlungsdaten.getAdresseKontoinhaber();
		if (adresse == null) {
			adresse = zahlungDataRow.getAdresseKontoinhaber();
		}
		// Jetzt muss eine Adresse vorhanden sein (die aus den Auszahlungsdaten oder die Defaultadresse
		Objects.requireNonNull(adresse);

		excelRowGroup.addValue(
			MergeFieldZahlungAuftrag.organisation,
			adresse.getOrganisation()
		);
		excelRowGroup.addValue(
			MergeFieldZahlungAuftrag.strasse,
			adresse.getStrasse()
		);
		excelRowGroup.addValue(
			MergeFieldZahlungAuftrag.hausnummer,
			adresse.getHausnummer()
		);
		excelRowGroup.addValue(MergeFieldZahlungAuftrag.plz, adresse.getPlz());
		excelRowGroup.addValue(MergeFieldZahlungAuftrag.ort, adresse.getOrt());
	}

}
