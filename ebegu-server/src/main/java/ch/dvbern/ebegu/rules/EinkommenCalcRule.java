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
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Setzt fuer die Zeitabschnitte das Massgebende Einkommen. Sollte der Maximalwert uebschritte werden so wird das Pensum
 * auf 0 gesetzt
 * ACHTUNG: Diese Regel gilt nur fuer Kita und Tageseltern Kleinkinder. Bei Tageseltern Schulkinder und Tagesstaetten
 * gibt es keine Reduktion des Anspruchs, sie bezahlen aber den Volltarif
 * Regel 16.7 Maximales Einkommen
 */
public class EinkommenCalcRule extends AbstractCalcRule {

	private final BigDecimal maximalesEinkommen;
	@Nullable
	private final BigDecimal maxEinkommenEKV;
	private final boolean pauschaleNurGeweahrenWennAnspruch;

	public EinkommenCalcRule(
		DateRange validityPeriod,
		BigDecimal maximalesEinkommen,
		@Nullable BigDecimal maxEinkommenEKV,
		@Nonnull Boolean pauschaleNurGeweahrenWennAnspruch,
		@Nonnull Locale locale
	) {
		super(
			RuleKey.EINKOMMEN,
			RuleType.REDUKTIONSREGEL,
			RuleValidity.ASIV,
			validityPeriod,
			locale
		);
		this.maximalesEinkommen = maximalesEinkommen;
		this.maxEinkommenEKV = maxEinkommenEKV;
		this.pauschaleNurGeweahrenWennAnspruch =
			pauschaleNurGeweahrenWennAnspruch;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
	}

	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData
	) {
		// Es gibt zwei Faelle, in denen die Finanzielle Situation nicht bekannt ist:
		// - Sozialhilfeempfaenger: Wir rechnen mit Einkommen = 0
		// - Keine Vergünstigung gewünscht / FinSit abgelehnt: Wir rechnen mit dem Maximalen Einkommen

		// Sonderfall Keine Verguenstigung gewuenscht oder FinSit abgelehnt
		// - Wir rechnen mit dem Max. Einkommen
		// - Der Anspruch wird auf 0 gesetzt, AUSSER das Kind hat erweiterte Beduerfnisse
		// - "Bezahlt Vollkosten" darf nur gesetzt werden, wenn KEINE erweiterten Beduerfnisse
		Familiensituation familiensituation = platz.extractGesuch()
			.extractFamiliensituation();

		boolean sozialhilfeEmpfaenger = familiensituation != null
			&& Boolean.TRUE.equals(
				familiensituation.getSozialhilfeBezueger()
			);
		//hier muss explizit geprüft werden, ob nicht abgelehnt... wenn die FinSit noch nicht akzeptiert oder abgelehnt wurde,
		//machen wir die berechnung als ob die finsit akzeptiert wurde
		inputData.setFinsitAccepted(
			FinSitStatus.ABGELEHNT
				!= platz.extractGesuch().getFinSitStatus()
		);

		// FinSit abgelehnt muss nur bei Erstgesuch beachtet werden. In einer Mutation wird es im Mutationsmerger abgehandelt
		boolean finSitAbgelehnt = !inputData.isFinsitAccepted()
			&& platz.extractGesuch().getTyp().isGesuch();

		boolean keineFinSitErfasst = familiensituation != null
			&& Boolean.FALSE.equals(
				familiensituation.getVerguenstigungGewuenscht()
			);
		inputData.setVerguenstigungGewuenscht(!keineFinSitErfasst);

		getEinkommenHandler(inputData, sozialhilfeEmpfaenger, finSitAbgelehnt)
			.handleEinkommen(platz, inputData);
	}

	private AbstractEinkommenHandler getEinkommenHandler(
		BGCalculationInput inputData,
		boolean sozialhilfeEmpfaenger,
		boolean finSitAbgelehnt
	) {
		if (!inputData.isVerguenstigungGewuenscht()
			|| sozialhilfeEmpfaenger
			|| finSitAbgelehnt) {
			return new EinkommenUnbekanntHandler(
				getLocale(),
				maxEinkommenEKV,
				maximalesEinkommen,
				pauschaleNurGeweahrenWennAnspruch,
				sozialhilfeEmpfaenger
			);
		}
		return new EinkommenBekanntHandler(
			getLocale(),
			maxEinkommenEKV,
			maximalesEinkommen
		);
	}

	@Override
	public boolean isRelevantForFamiliensituation() {
		return true;
	}
}
