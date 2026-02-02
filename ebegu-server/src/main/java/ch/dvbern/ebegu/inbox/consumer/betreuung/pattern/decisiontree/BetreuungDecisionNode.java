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

import java.util.function.Predicate;

import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.inbox.consumer.betreuung.event.BetreuungEvent;

/**
 * Definiert einen binären Entscheidungsknoten für Betreuungen im Entscheidungsbaum. Binär heisst: Die mit diesem Knoten
 * verknüpfte Bedingung
 * kann nur eines von zwei Ergebnissen haben: wahr/falsch (oder ja/nein).
 */
public class BetreuungDecisionNode implements DecisionNode<BetreuungEvent> {

	private final Predicate<Betreuung> condition;
	private final DecisionNode<BetreuungEvent> whenTrue;
	private final DecisionNode<BetreuungEvent> whenFalse;

	/**
	 * Erzeugt einen neuen, binären Entscheidungsknoten im Entscheidungsbaum.
	 *
	 * @param condition Die Bedingung, mit der die Entscheidung auf Basis einer bestimmten Betreuung getroffen wird.
	 * @param whenTrue Der Knoten im Entscheidungsbaum, der als nächstes aufgerufen wird, wenn die gegebene Bedingung zu
	 * wahr
	 * evaluiert.
	 * @param whenFalse Der Knoten im Entscheidungsbaum, der als nächstes aufgerufen wird, wenn die gegebene Bedingung
	 * zu falsch
	 * evaluiert.
	 */
	public BetreuungDecisionNode(
		@NotNull Predicate<Betreuung> condition,
		@NotNull DecisionNode<BetreuungEvent> whenTrue,
		@NotNull DecisionNode<BetreuungEvent> whenFalse
	) {
		this.condition = condition;
		this.whenTrue = whenTrue;
		this.whenFalse = whenFalse;
	}

	/**
	 * Evaluiert die gegebene Betreuung durch Bedingung dieses Knotens. Je nachdem, wie die Bedingung evaluiert, wird
	 * ein
	 * anderer Knoten als nächstes evaluiert und zwar {@link BetreuungDecisionNode#whenTrue}, wenn wahr,
	 * {@link BetreuungDecisionNode#whenTrue}, wenn falsch.
	 *
	 * @param betreuungEvent Das Event, das evaluiert werden soll.
	 */
	@Override
	public void evaluate(@NotNull BetreuungEvent betreuungEvent) {
		if (condition.test(betreuungEvent.getBetreuung())) {
			whenTrue.evaluate(betreuungEvent);
		} else {
			whenFalse.evaluate(betreuungEvent);
		}
	}
}
