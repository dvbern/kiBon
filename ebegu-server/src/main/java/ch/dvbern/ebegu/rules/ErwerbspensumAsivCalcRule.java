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

package ch.dvbern.ebegu.rules;

import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.dto.VerfuegungsBemerkung;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Berechnet die hoehe des ErwerbspensumRule eines bestimmten Erwerbspensums
 * Diese Rule muss immer am Anfang kommen, d.h. sie setzt den initialen Anspruch
 * Die weiteren Rules müssen diesen Wert gegebenenfalls korrigieren.
 * ACHTUNG! Diese Regel gilt nur fuer Angebote vom Typ isAngebotJugendamtKleinkind
 * Verweis 16.9.2
 */
public class ErwerbspensumAsivCalcRule extends ErwerbspensumCalcRule {

	public ErwerbspensumAsivCalcRule(
		@Nonnull DateRange validityPeriod,
		int zuschlagErwerbspensum,
		int minErwerbspensumNichtEingeschult,
		int minErwerbspensumEingeschult,
		@Nonnull Locale locale
	) {
		super(RuleValidity.ASIV, validityPeriod, zuschlagErwerbspensum, minErwerbspensumNichtEingeschult, minErwerbspensumEingeschult, locale);
	}

	@Override
	protected void addVerfuegungsBemerkungIfNecessary(@Nonnull BGCalculationInput inputData) {
		// Die Bemerkung darf nur fuer den ASIV Anteil gelten
		String vorhandeneBeschaeftigungen = getBeschaeftigungsTypen(inputData, getLocale());
		inputData.getParent().getBemerkungenList().addBemerkung(
			new VerfuegungsBemerkung(MsgKey.ERWERBSPENSUM_ANSPRUCH, getLocale(), vorhandeneBeschaeftigungen));
	}

	private String getBeschaeftigungsTypen(@Nonnull BGCalculationInput inputData, @Nonnull Locale locale) {
		StringBuilder sb = new StringBuilder();
		for (Taetigkeit taetigkeit : inputData.getTaetigkeiten()) {
			sb.append(ServerMessageUtil.translateEnumValue(taetigkeit, locale));
			sb.append(", ");
		}
		// Das letzte Komma entfernen
		String taetigkeitenAsString = sb.toString();
		taetigkeitenAsString = StringUtils.removeEnd(taetigkeitenAsString, ", ");
		return taetigkeitenAsString;
	}
}
