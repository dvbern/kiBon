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

package ch.dvbern.ebegu.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Zahlungsstatus fuer VerfuegungsZeitabschnitte
 */
public enum VerfuegungsZeitabschnittZahlungsstatus {

	NEU, // wurde noch nie verrechnet und wird in nächsten Zahlungslauf aufgenommen
	VERRECHNET, // Die Zahlung wurde ausbezahlt
	VERRECHNEND, // Die Zahlung war schon ausbezahlt, wurde aber mit "uebernehmen" gekennzeichnet.
	VERRECHNET_KEINE_BETREUUNG, // Die Zahlung wurde ausbezahlt, besitzt jedoch keine Betreuung (KIBON-2637)
	VERRECHNET_KORRIGIERT, // Die Zahlung war schon ausbezahlt, wurde aber in einem späteren Zahlungslauf korrigiert
	IGNORIEREND, 	// Zahlung ist markiert zum Ignorieren und wird bei der nächsten Zahlung als ignoriert markiert. Der Entscheid kann bis zur nächsten Zahlung in einer Folge-Mutation geändert werden
	IGNORIEREND_DEFINITIV, // Zahlung ist markiert zum Ignorieren und wird bei der nächsten Zahlung als ignoriert markiert. Der Entscheid kann nicht mehr geändert werden
	IGNORIERT, 		// Zahlung wurde in einem Vorgänger bereits einmal ignoriert und muss daher auch künftig ignoriert werden
	IGNORIERT_KORRIGIERT; // Die Zahlung war schon ignoriert, wurde aber in einem späteren Zahlungslauf korrigiert (und muss weiterhin ignoriert werden)

	public boolean isNeu() {
		return NEU == this;
	}

	public boolean isVerrechnend() {
		return VERRECHNEND == this;
	}

	public boolean isVerrechnet() {
		return isVerrechnetMitBetreuung() || VERRECHNET_KEINE_BETREUUNG == this;
	}

	public boolean isVerrechnetMitBetreuung() {
		return VERRECHNET == this || VERRECHNET_KORRIGIERT == this;
	}

	public boolean isIgnorierend() {
		return IGNORIEREND == this || IGNORIEREND_DEFINITIV == this;
	}

	public boolean isIgnoriert() {
		return IGNORIERT == this || IGNORIERT_KORRIGIERT == this;
	}

	public boolean isIgnoriertIgnorierend() {
		return isIgnorierend() || isIgnoriert();
	}

	/**
	 * Ist diese Instanz eines Verfuegungsabschnittes bereits behandelt?
	 * -> Alles was nicht ausbezahlt werden soll
	 */
	public boolean isBereitsBehandeltInZahlungslauf() {
		return !isZuBehandelnInZahlungslauf();
	}

	private static final Set<VerfuegungsZeitabschnittZahlungsstatus> ZU_BEHANDELN_IN_ZAHLUNGSLAUF =
		Collections.unmodifiableSet(
			EnumSet.of(
				NEU,
				VERRECHNEND,
				IGNORIEREND,
				IGNORIEREND_DEFINITIV,
				IGNORIERT
			)
		);

	public boolean isZuBehandelnInZahlungslauf() {
		return ZU_BEHANDELN_IN_ZAHLUNGSLAUF.contains(this);
	}

	public static Set<VerfuegungsZeitabschnittZahlungsstatus> getStatusForKorrekturUndNachzahlungZahlungslauf() {
		return ZU_BEHANDELN_IN_ZAHLUNGSLAUF;
	}

	public boolean hasZeitabschnittOrVorgaengerBeenAusbezahlt() {
		return !isNeu() && this != VERRECHNET_KEINE_BETREUUNG;
	}
}
