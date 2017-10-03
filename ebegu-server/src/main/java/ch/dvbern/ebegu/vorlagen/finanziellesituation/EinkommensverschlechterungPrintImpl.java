package ch.dvbern.ebegu.vorlagen.finanziellesituation;
/*
* Copyright (c) 2016 DV Bern AG, Switzerland
*
* Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
* geschuetzt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulaessig. Dies gilt
* insbesondere fuer Vervielfaeltigungen, die Einspeicherung und Verarbeitung in
* elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
* Ansicht uebergeben ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
*
* Ersteller: zeab am: 22.08.2016
*/

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.util.FinanzielleSituationRechner;

/**
 * Implementiert den {@link EinkommensverschlechterungPrint}
 */
public class EinkommensverschlechterungPrintImpl extends FinanzDatenPrintImpl implements EinkommensverschlechterungPrint {

	private final String einkommensverschlechterungJahr;
	private final String ereigniseintritt;
	private final String grund;
	private final Einkommensverschlechterung ekvGS1;
	private Einkommensverschlechterung ekvGS2;
	private final int basisJahrPlus;

	/**
	 * Konstruktor
	 *
	 * @param fsGesuchsteller1 das {@link FinanzSituationPrintGesuchsteller}
	 * @param fsGesuchsteller2 das {@link FinanzSituationPrintGesuchsteller}
	 * @param einkommensverschlechterungJahr das Jahr des Einkommenverschleschterung
	 * @param ereigniseintritt Ereingis datum
	 * @param grund Grund
	 * @param basisJahrPlus Anzahl Jahre nach dem Basisjahr
	 */
	public EinkommensverschlechterungPrintImpl(FinanzSituationPrintGesuchsteller fsGesuchsteller1, FinanzSituationPrintGesuchsteller fsGesuchsteller2,
		String einkommensverschlechterungJahr, String ereigniseintritt, String grund, int basisJahrPlus) {

		super(fsGesuchsteller1, fsGesuchsteller2);

		this.basisJahrPlus = basisJahrPlus;
		this.einkommensverschlechterungJahr = einkommensverschlechterungJahr;
		this.ereigniseintritt = ereigniseintritt;
		this.grund = grund;
		if (basisJahrPlus == 1) {
			this.ekvGS1 = fsGesuchsteller1.getEinkommensverschlechterung1();
		} else {
			this.ekvGS1 = fsGesuchsteller1.getEinkommensverschlechterung2();
		}
		if (fsGesuchsteller2 != null) {
			if (basisJahrPlus == 1 && fsGesuchsteller2.getEinkommensverschlechterung1() != null) {
				this.ekvGS2 = fsGesuchsteller2.getEinkommensverschlechterung1();
			} else if (fsGesuchsteller2.getEinkommensverschlechterung2() != null) {
				this.ekvGS2 = fsGesuchsteller2.getEinkommensverschlechterung2();
			}
		}

	}

	@Override
	public String getEinkommensverschlechterungJahr() {
		return einkommensverschlechterungJahr;
	}

	@Override
	public String getEreigniseintritt() {

		return ereigniseintritt;
	}

	@Override
	public String getGrund() {

		return grund;
	}

	@Override
	public boolean isExistEreigniseintritt() {
		return (this.ereigniseintritt != null && !this.ereigniseintritt.isEmpty());
	}

	@Override
	public boolean isExistGrund() {
		return (this.grund != null && !this.grund.isEmpty());
	}

	@Override
	public BigDecimal getGeschaeftsgewinnG1() {
		final FinanzSituationPrintGesuchsteller fsGesuchsteller = fsGesuchsteller1;
		return FinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(fsGesuchsteller.getFinanzielleSituation(),
			fsGesuchsteller.getEinkommensverschlechterung1(),
			fsGesuchsteller.getEinkommensverschlechterung2(),
			fsGesuchsteller.getEinkommensverschlechterungInfo(),
			basisJahrPlus);
	}

	@Override
	public BigDecimal getGeschaeftsgewinnG2() {
		//hier muessen zum berechnen die Einkommensverschlechterung und die finanzielle Situation benutzt werden
		if (fsGesuchsteller2 != null) {
			final FinanzSituationPrintGesuchsteller fsGesuchsteller = fsGesuchsteller2;
			return FinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(fsGesuchsteller.getFinanzielleSituation(),
				fsGesuchsteller.getEinkommensverschlechterung1(),
				fsGesuchsteller.getEinkommensverschlechterung2(),
				fsGesuchsteller.getEinkommensverschlechterungInfo(),
				basisJahrPlus);
		}
		return null;
	}

	@Override
	protected AbstractFinanzielleSituation getFinanzSituationGS1() {
		return ekvGS1;
	}

	@Override
	protected AbstractFinanzielleSituation getFinanzSituationGS2() {
		return ekvGS2;
	}

}
