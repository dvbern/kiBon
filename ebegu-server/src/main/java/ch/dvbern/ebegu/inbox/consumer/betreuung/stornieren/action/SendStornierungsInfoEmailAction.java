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
import ch.dvbern.ebegu.services.MailService;

/**
 * Definiert eine Aktion mit der eine E-Mail an den Gesuchsteller, mit dem Hinweis, dass die von ihm angefragte
 * Betreung storniert wurde, versandt wird.
 */
@Stateless
@LocalBean
public class SendStornierungsInfoEmailAction implements Action<BetreuungEvent> {

	/**
	 * Referenz auf den Service mit dem eine Info-Mail zur Storierung versandt werden soll..
	 */
	@Inject
	private MailService mailService;

	/**
	 * Versendet eine E-Mail an den Gesuchsteller, mit dem Hinweis, dass die von ihm angefragte Betreung storniert
	 * wurde.
	 *
	 * @param betreuungEvent Das für das erstellen der Info-Mail, erhaltene Event.
	 */
	@Override
	public void execute(@NotNull BetreuungEvent betreuungEvent) {
		// TODO KIBON-3569: Müssen wir hier evtl. noch unterscheiden zwischen Ablehnung und Stornierung?
		mailService.prepareToSendInfoBetreuungAbgelehnt(
			betreuungEvent.getBetreuung()
		);
	}
}
