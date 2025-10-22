/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.enums.MsgKey;

public class EinkommenUnbekanntHandler extends AbstractEinkommenHandler {

	private final boolean isSozialhilfeEmpfaenger;
	private final boolean pauschaleNurGeweahrenWennAnspruch;

	protected EinkommenUnbekanntHandler(
		Locale locale,
		@Nullable BigDecimal maxEinkommenEKV,
		BigDecimal maxEinkommen,
		boolean pauschaleNurGeweahrenWennAnspruch,
		boolean isSozialhilfeEmpfaenger
	) {
		super(locale, maxEinkommenEKV, maxEinkommen);
		this.isSozialhilfeEmpfaenger = isSozialhilfeEmpfaenger;
		this.pauschaleNurGeweahrenWennAnspruch =
			pauschaleNurGeweahrenWennAnspruch;
	}

	@Override
	protected void handleEinkommen(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData
	) {
		setBasisJahrAndFamGroesseForUnbekanntesEinkommen(inputData, platz);

		if (!inputData.isFinsitAccepted()) {
			handleFinSitAbgelehnt(platz, inputData);
			return;
		}

		if (!inputData.isVerguenstigungGewuenscht()) {
			handleKeineFinSitErfasst(inputData);
			return;
		}

		if (isSozialhilfeEmpfaenger) {
			handleSozialhilfeEmpfaenger(inputData);
		}
	}

	private void handleFinSitAbgelehnt(
		AbstractPlatz platz,
		BGCalculationInput inputData
	) {
		setMaximalesEinkommen(inputData);
		inputData.addBemerkung(
			MsgKey.EINKOMMEN_FINSIT_ABGELEHNT_ERSTGESUCH_MSG,
			getLocale()
		);

		// wenn die Pauschale nur gewährt wird, wenn auch Anspruch besteht kann der Anspruch immer auf 0 gesetzt werden
		// wenn die Pauschale gewährt wird, wenn kein Anspruch besteht, dann darf der Anspruch nur reseted werden,
		// wenn keine erweiterte Betreuung existiert
		if (this.pauschaleNurGeweahrenWennAnspruch
			|| !this.hasErweiterteBetreuung(platz)) {
			inputData.setAnspruchZeroAndSaveRestanspruch();
			inputData.setBezahltVollkostenKomplett();
		}
	}

	private void handleKeineFinSitErfasst(BGCalculationInput inputData) {
		setMaximalesEinkommen(inputData);
		inputData.addBemerkung(
			MsgKey.EINKOMMEN_KEINE_VERGUENSTIGUNG_GEWUENSCHT_MSG,
			getLocale()
		);
	}

	private void handleSozialhilfeEmpfaenger(
		@Nonnull BGCalculationInput inputData
	) {
		inputData.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.ZERO);
		inputData.addBemerkung(
			MsgKey.EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG,
			getLocale()
		);
	}

	private void setBasisJahrAndFamGroesseForUnbekanntesEinkommen(
		BGCalculationInput inputData,
		AbstractPlatz platz
	) {
		inputData.setAbzugFamGroesse(BigDecimal.ZERO);
		inputData.setEinkommensjahr(
			platz.extractGesuchsperiode().getBasisJahr()
		);
	}

	private void setMaximalesEinkommen(@Nonnull BGCalculationInput inputData) {
		inputData.setMassgebendesEinkommenVorAbzugFamgr(getMaxEinkommen());
		handleMaximalesEinkommenUeberschritten(inputData);
	}

	/**
	 * Gibt zurueck, ob fuer diesen Platz eine erweiterte Betreuung besteht.
	 * Fuer Tagesschulen immer false!
	 */
	private boolean hasErweiterteBetreuung(@Nonnull AbstractPlatz platz) {
		if (platz.getBetreuungsangebotTyp().isJugendamt()) {
			Betreuung betreuung = (Betreuung) platz;
			return Boolean.TRUE.equals(betreuung.hasErweiterteBetreuung());
		}
		return false;
	}
}
