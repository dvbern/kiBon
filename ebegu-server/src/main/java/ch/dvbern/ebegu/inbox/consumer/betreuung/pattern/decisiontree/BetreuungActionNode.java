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

import ch.dvbern.ebegu.inbox.consumer.betreuung.event.BetreuungEvent;

/**
 * Definiert einen Aktionsknoten innerhalb eines Entscheidungsbaums.
 * Aktionsknoten haben keine Verzweigungen und bilden die Blätter eines Entscheidungsbaums.
 * Aktionsknoten können allerdings Nachfolger haben, die entweder selbst Aktionsknoten oder Verzweigungen sind.
 */
public class BetreuungActionNode implements DecisionNode<BetreuungEvent> {

	private final Action<BetreuungEvent> action;
	private DecisionNode<BetreuungEvent> successor = null;

	/**
	 * Erzeugt einen neuen Aktionsknoten ohne Nachfolger. Diese Knoten bilden im Entscheidungsbaum immer ein Blatt.
	 *
	 * @param action Die auszuführende Aktion.
	 */
	public BetreuungActionNode(@NotNull Action<BetreuungEvent> action) {
		this.action = action;
	}

	/**
	 * Erzeugt einen neuen Aktionsknoten mit Nachfolger. Diese Knoten bilden keie Blätter im Entscheidungsbaum.
	 * Nachdem sie ihre Aktion ausgeführt haben, rufen sie sie ihren Nachfolger auf
	 * ({@link DecisionNode#evaluate(Object)}.
	 *
	 * @param action Die in diesem Knoten auszuführende Aktion.
	 * @param successor Der nach Ausführen der Aktion aufzurufende Nachfolger dieses Knotens.
	 */
	public BetreuungActionNode(
		@NotNull Action<BetreuungEvent> action,
		@NotNull DecisionNode<BetreuungEvent> successor
	) {
		this.action = action;
		this.successor = successor;
	}

	/**
	 * Führt die Aktion für die gegebene Betreuung aus und übergibt diese anschliessend zuum evaluieren an den
	 * Nachfolger, wenn es einen gibt.
	 *
	 * @param betreuungEvent Das Event, dass die Aktion ausgelöst hat und das (optional) dem Nachfolger übergeben werden
	 * soll.
	 */
	@Override
	public void evaluate(@NotNull BetreuungEvent betreuungEvent) {
		action.execute(betreuungEvent);
		if (null != successor) {
			successor.evaluate(betreuungEvent);
		}
	}
}
