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

import java.math.BigDecimal;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.inbox.consumer.betreuung.event.BetreuungEvent;
import ch.dvbern.ebegu.inbox.consumer.betreuung.pattern.decisiontree.Action;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.util.datetime.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Definiert eine Aktion mit der eine Betreuung als Reaktion auf ein Event storniert wird.
 */
@Stateless
@LocalBean
public class BetreuungStornierenAction implements Action<BetreuungEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(
		BetreuungStornierenAction.class
	);

	/**
	 * Referenz auf einen Service, mit dem das aktuelle Datum bestimmt werden kann.
	 */
	@Inject
	private DateTimeUtils dateTimeUtils;

	/**
	 * Referenz auf den Service mit dem die Stornierung durchgeführt wird.
	 */
	@Inject
	private BetreuungService betreuungService;

	/**
	 * Führt die Stornierung der gegebenen Betreuung durch.
	 *
	 * @param betreuungEvent Das für die Stornierung der Betreuung, erhaltene Event.
	 */
	@Override
	public void execute(@NotNull BetreuungEvent betreuungEvent) {

		Betreuung betreuung = betreuungEvent.getBetreuung();

		betreuung.setDatumBestaetigung(dateTimeUtils.now().toLocalDate());
		betreuung.getBetreuungspensumContainers()
			.forEach(betreuungspensumContainer -> {
				betreuungspensumContainer.getBetreuungspensumJA()
					.setPensum(BigDecimal.ZERO);
				betreuungspensumContainer.getBetreuungspensumJA()
					.setNichtEingetreten(true);
			}
			);
		betreuung.setBetreuungsstatus(Betreuungsstatus.STORNIERT);
		// noinspection ResultOfMethodCallIgnored
		betreuungService.saveBetreuung(
			betreuung,
			false,
			betreuungEvent.getEventMonitor().getClientName()
		);
		LOG.info(
			"Die Betreuung mit derReferenznummer: {} wurde automatisch storniert.",
			betreuungEvent.getEventMonitor().getRefnr()
		);
		betreuungEvent.getEventMonitor()
			.record("Die Betreuung wurde automatisch storniert.");
	}
}
