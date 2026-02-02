/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.util;

import java.io.Serial;
import java.util.Locale;

import jakarta.ejb.Local;
import jakarta.ejb.Stateless;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

/**
 * Erstellt den Begründungstext für das Stornieren einer Betreuung.
 */
@Stateless
@Local(TextMessageFactory.class)
public class BetreuungStornierenBegruendungFactory implements
	TextMessageFactory {

	/**
	 * Schlüssel, welcher den Begründungstext in den Übersetzungsdateien referenziert.
	 */
	private static final String TXT_KEY_GRUND =
		"ExchangeServce_BetreuungStornieren_Grund";

	/**
	 * Standard-Locale, mit dem die Übersetzungsdatei identifiziert und aus welcher der Begründungstext gelesen wird.
	 */
	private static final Locale DEFAULT_LOCALE = new Locale("de", "CH");

	/**
	 * Standard-Mandant, mit dem die Übersetzungsdatei identifiziert und aus welcher der Begründungstext gelesen wird.
	 */
	private static final MandantIdentifier DEFAULT_MANDANT =
		MandantIdentifier.BERN;

	private static final Mandant MANDANT = new Mandant() {
		@Serial
		private static final long serialVersionUID = 3904947595637667569L;

		@Override
		public MandantIdentifier getMandantIdentifier() {
			return DEFAULT_MANDANT;
		}
	};

	/**
	 * @return Den mit dem Schlüssel {@link BetreuungStornierenBegruendungFactory#TXT_KEY_GRUND} referenzierten Text für
	 * die
	 * Standard-Locale und den Standard-Mandant.
	 */
	@Override
	public String getMessage() {
		return ServerMessageUtil.getMessage(
			TXT_KEY_GRUND,
			DEFAULT_LOCALE,
			MANDANT
		);
	}
}
