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

package ch.dvbern.ebegu.inbox.consumer.betreuung.pattern.decisiontree;

import jakarta.validation.constraints.NotNull;

/**
 * Schnittstelle um im Entscheidungsbaum Entscheidungen für Betreuungen zu treffen. Entscheidungsknoten bilden
 * Verzweigungen und
 * Teilen einen Pfad in Alternativen.
 * 
 * @param <T> Der Datentyp der Objekte, für die eine Entscheidung getroffen werden soll.
 */
public interface DecisionNode<T> {
	/**
	 * Trifft eine Entscheidung für das gegebene Objekt.
	 * 
	 * @param objToEvaluate Die Objekt für das eine Entscheidung getroffen werden soll.
	 */
	void evaluate(@NotNull T objToEvaluate);
}
