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

package ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.condition;

import java.util.function.Predicate;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;

/**
 * Definiert ein {@link Predicate} mit dem ermittelt werden kann, ob eine Betreuung einen abgeschlossenen Status hat.
 */
public class BetreuungStatusAbgeschlossenCondition implements
	Predicate<Betreuung> {

	/**
	 * Prüft ob eine Betreuung einen abgeschlossenen Status hat. Abgeschlossen heisst, dass weder GS und Institution
	 * keine
	 * Angaben mehr machen oder Aktionen ausführen müssen.
	 *
	 * @param betreuung Die zu prüfende Betreuung.
	 * @return Ob die gegebene Betreuung einen abgeschlossenen Status ist.
	 */
	@Override
	public boolean test(Betreuung betreuung) {
		return Betreuungsstatus.BESTAETIGT == betreuung.getBetreuungsstatus()
			|| Betreuungsstatus.NICHT_EINGETRETEN
				== betreuung.getBetreuungsstatus()
			|| Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG
				== betreuung.getBetreuungsstatus()
			|| Betreuungsstatus.VERFUEGT == betreuung.getBetreuungsstatus();
	}
}
