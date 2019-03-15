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
import java.text.NumberFormat;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.types.DateRange;

import static java.util.Objects.requireNonNull;

/**
 * Setzt fuer die Zeitabschnitte das Massgebende Einkommen. Sollte der Maximalwert uebschritte werden so wird das Pensum auf 0 gesetzt
 * ACHTUNG: Diese Regel gilt nur fuer Kita und Tageseltern Kleinkinder.  Bei Tageseltern Schulkinder und Tagesstaetten
 * gibt es keine Reduktion des Anspruchs, sie bezahlen aber den Volltarif
 * Regel 16.7 Maximales Einkommen
 */
public class EinkommenCalcRule extends AbstractCalcRule {

	private final BigDecimal maximalesEinkommen;

	public EinkommenCalcRule(
		DateRange validityPeriod,
		BigDecimal maximalesEinkommen,
		@Nonnull Locale locale
	) {
		super(RuleKey.EINKOMMEN, RuleType.REDUKTIONSREGEL, validityPeriod, locale);
		this.maximalesEinkommen = maximalesEinkommen;
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	@Override
	protected void executeRule(
		@Nonnull Betreuung betreuung,
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt
	) {

		// Es gibt zwei Faelle, in denen die Finanzielle Situation nicht bekannt ist:
		// - Sozialhilfeempfaenger: Wir rechnen mit Einkommen = 0
		// - Keine Vergünstigung gewünscht: Wir rechnen mit dem Maximalen Einkommen
		Familiensituation familiensituation = betreuung.extractGesuch().extractFamiliensituation();
		if (familiensituation != null) {
			int basisjahr = betreuung.extractGesuchsperiode().getBasisJahr();
			if (Boolean.TRUE.equals(familiensituation.getSozialhilfeBezueger())) {
				verfuegungZeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.ZERO);
				verfuegungZeitabschnitt.setAbzugFamGroesse(BigDecimal.ZERO);
				verfuegungZeitabschnitt.setEinkommensjahr(basisjahr);
				verfuegungZeitabschnitt.addBemerkung(RuleKey.EINKOMMEN, MsgKey.EINKOMMEN_SOZIALHILFEEMPFAENGER_MSG, getLocale());
				return;
			}
			if (Boolean.FALSE.equals(familiensituation.getVerguenstigungGewuenscht())) {
				verfuegungZeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(maximalesEinkommen);
				verfuegungZeitabschnitt.setAbzugFamGroesse(BigDecimal.ZERO);
				verfuegungZeitabschnitt.setEinkommensjahr(basisjahr);
				verfuegungZeitabschnitt.addBemerkung(
					RuleKey.EINKOMMEN,
					MsgKey.EINKOMMEN_MSG,
					getLocale(),
					NumberFormat.getInstance().format(maximalesEinkommen));
				return;
			}
		}

		// Die Finanzdaten berechnen
		FinanzDatenDTO finanzDatenDTO;
		if (verfuegungZeitabschnitt.isHasSecondGesuchstellerForFinanzielleSituation()) {
			finanzDatenDTO = betreuung.extractGesuch().getFinanzDatenDTO_zuZweit();
			setMassgebendesEinkommen(
				verfuegungZeitabschnitt.isEkv1ZuZweit(),
				verfuegungZeitabschnitt.isEkv2ZuZweit(),
				finanzDatenDTO,
				verfuegungZeitabschnitt,
				betreuung,
				getLocale());
		} else {
			finanzDatenDTO = betreuung.extractGesuch().getFinanzDatenDTO_alleine();
			setMassgebendesEinkommen(
				verfuegungZeitabschnitt.isEkv1Alleine(),
				verfuegungZeitabschnitt.isEkv2Alleine(),
				finanzDatenDTO,
				verfuegungZeitabschnitt,
				betreuung,
				getLocale());
		}

		// Erst jetzt kann das Maximale Einkommen geprueft werden!
		if (requireNonNull(betreuung.getBetreuungsangebotTyp()).isJugendamt()) {
			if (verfuegungZeitabschnitt.getMassgebendesEinkommen().compareTo(maximalesEinkommen) >= 0) {
				//maximales einkommen wurde ueberschritten
				verfuegungZeitabschnitt.setKategorieMaxEinkommen(true);
				if (betreuung.getBetreuungsangebotTyp().isAngebotJugendamtKleinkind()) {
					reduceAnspruchInNormalCase(betreuung, verfuegungZeitabschnitt);
					verfuegungZeitabschnitt.addBemerkung(
						RuleKey.EINKOMMEN,
						MsgKey.EINKOMMEN_MSG,
						getLocale(),
						NumberFormat.getInstance().format(maximalesEinkommen));
				}
			}
		}
	}

	/**
	 * If the Betreuung is set as "erweiterteBeduerfniss" there is no need to reduce the Anspruch tu zero when the incomes are too high.
	 * This is because the child still has Anspruch, though it will only get a redutcion of the costs due to this erweiterteBeduerfniss
	 */
	private void reduceAnspruchInNormalCase(@Nonnull Betreuung betreuung, @Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		if (!requireNonNull(betreuung.getErweiterteBetreuungContainer().getErweiterteBetreuungJA()).getErweiterteBeduerfnisse()) {
			verfuegungZeitabschnitt.setAnspruchberechtigtesPensum(0);
		}
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	private void setMassgebendesEinkommen(
		boolean isEkv1,
		boolean isEkv2,
		FinanzDatenDTO finanzDatenDTO,
		VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		Betreuung betreuung,
		@Nonnull Locale locale
	) {
		int basisjahr = betreuung.extractGesuchsperiode().getBasisJahr();
		int basisjahrPlus1 = betreuung.extractGesuchsperiode().getBasisJahrPlus1();
		int basisjahrPlus2 = betreuung.extractGesuchsperiode().getBasisJahrPlus2();

		if (isEkv1) {
			if (finanzDatenDTO.isEkv1AcceptedAndNotAnnuliert()) {
				verfuegungZeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjP1VorAbzFamGr());
				verfuegungZeitabschnitt.setEinkommensjahr(basisjahrPlus1);
				verfuegungZeitabschnitt.addBemerkung(
					RuleKey.EINKOMMEN,
					MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG,
					locale,
					String.valueOf(basisjahrPlus1),
					String.valueOf(basisjahr)
				);
			} else {
				verfuegungZeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr());
				verfuegungZeitabschnitt.setEinkommensjahr(basisjahr);
				// Je nachdem, ob es (manuell) annulliert war oder die 20% nicht erreicht hat, kommt eine andere Meldung
				if (finanzDatenDTO.isEkv1Annulliert()) {
					verfuegungZeitabschnitt.addBemerkung(
						RuleKey.EINKOMMEN,
						MsgKey.EINKOMMENSVERSCHLECHTERUNG_ANNULLIERT_MSG,
						locale,
						String.valueOf(basisjahrPlus1));
				} else {
					verfuegungZeitabschnitt.addBemerkung(
						RuleKey.EINKOMMEN,
						MsgKey.EINKOMMENSVERSCHLECHTERUNG_NOT_ACCEPT_MSG,
						locale,
						String.valueOf(basisjahrPlus1),
						String.valueOf(basisjahr));
				}
			}

		} else if (isEkv2) {
			if (finanzDatenDTO.isEkv2AcceptedAndNotAnnuliert()) {
				// EKV 1 accepted -> basisjahr + 2
				verfuegungZeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjP2VorAbzFamGr());
				verfuegungZeitabschnitt.setEinkommensjahr(basisjahrPlus2);
				verfuegungZeitabschnitt.addBemerkung(
					RuleKey.EINKOMMEN,
					MsgKey.EINKOMMENSVERSCHLECHTERUNG_ACCEPT_MSG,
					locale,
					String.valueOf(basisjahrPlus2),
					String.valueOf(basisjahr));
			} else {
				if (finanzDatenDTO.isEkv1AcceptedAndNotAnnuliert()) {
					verfuegungZeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjP1VorAbzFamGr());
					verfuegungZeitabschnitt.setEinkommensjahr(basisjahrPlus1);
				} else {
					verfuegungZeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr());
					verfuegungZeitabschnitt.setEinkommensjahr(basisjahr);
				}
				if (finanzDatenDTO.isEkv2Annulliert()) {
					verfuegungZeitabschnitt.addBemerkung(
						RuleKey.EINKOMMEN,
						MsgKey.EINKOMMENSVERSCHLECHTERUNG_ANNULLIERT_MSG,
						locale,
						String.valueOf(basisjahrPlus2));
				} else {
					verfuegungZeitabschnitt.addBemerkung(
						RuleKey.EINKOMMEN,
						MsgKey.EINKOMMENSVERSCHLECHTERUNG_NOT_ACCEPT_MSG,
						locale,
						String.valueOf(basisjahrPlus2),
						String.valueOf(basisjahr));
				}
			}
		} else {
			verfuegungZeitabschnitt.setMassgebendesEinkommenVorAbzugFamgr(finanzDatenDTO.getMassgebendesEinkBjVorAbzFamGr());
			verfuegungZeitabschnitt.setEinkommensjahr(basisjahr);
		}
	}

	@Override
	public boolean isRelevantForFamiliensituation() {
		return true;
	}
}
