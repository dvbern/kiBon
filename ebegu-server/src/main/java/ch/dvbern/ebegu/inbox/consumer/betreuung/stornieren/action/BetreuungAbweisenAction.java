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

package ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.inbox.consumer.betreuung.event.BetreuungEvent;
import ch.dvbern.ebegu.inbox.consumer.betreuung.pattern.decisiontree.Action;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.action.util.TextMessageFactory;
import ch.dvbern.ebegu.services.BetreuungService;

/**
 * Definiert eine Aktion mit der eine Betreuung als Reaktion auf ein Event abgewiesen wird.
 */
@Stateless
@LocalBean
public class BetreuungAbweisenAction implements Action<BetreuungEvent> {

	/**
	 * Referenz auf den Service mit dem die Abweisung durchgeführt wird.
	 */
	@Inject
	private BetreuungService betreuungService;

	/**
	 * Referenz auf die Message-Factory, welche den Begründungstext für das Abweisen einer Betreuung liefert.
	 */
	@Inject
	private TextMessageFactory messageFactory;

	/**
	 * Führt die Abweisung der gegebenen Betreuung durch.
	 *
	 * @param betreuungEvent Das für die Abweisung der Betreuung, erhaltene Event..
	 */
	@Override
	public void execute(@NotNull BetreuungEvent betreuungEvent) {

		betreuungService.betreuungPlatzAbweisen(
			betreuungEvent.getBetreuung(),
			betreuungEvent.getEventMonitor().getClientName(),
			messageFactory.getMessage()
		);
	}
}
