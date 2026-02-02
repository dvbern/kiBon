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

/**
 * Definiert ein {@link Predicate} mit dem ermittelt werden kann, ob der Antrag zu dem eine Betreuung gehört, eine
 * offene (also
 * noch nicht verfügte) Mutation ist.
 */
public class OffeneMutationCondition implements Predicate<Betreuung> {

	/**
	 * Prüft, ob der Antrag zu dem die gegebene Betreuung gehört, eine offene (also noch nicht verfügte) Mutation ist.
	 * 
	 * @param betreuung Die zu prüfende Betreuung.
	 * @return Ob der Antrag zu dem die gegebene Betreuung gehört, eine offene (also noch nicht verfügte) Mutation ist.
	 */
	@Override
	public boolean test(Betreuung betreuung) {
		return betreuung.getKind().getGesuch().isMutation();
	}
}
