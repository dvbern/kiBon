/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.inbox.handler;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.inbox.consumer.betreuung.event.BetreuungEvent;
import ch.dvbern.ebegu.inbox.consumer.betreuung.stornieren.BetreuungStornierenDecisionTree;
import ch.dvbern.ebegu.inbox.services.BetreuungEventHelper;
import ch.dvbern.ebegu.kafka.BaseEventHandler;
import ch.dvbern.ebegu.kafka.EventType;
import ch.dvbern.ebegu.services.BetreuungMonitoringService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.types.DateRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class BetreuungStornierenEventHandler extends BaseEventHandler<String> {

	private static final Logger LOG = LoggerFactory.getLogger(
		BetreuungStornierenEventHandler.class
	);

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private BetreuungEventHelper betreuungEventHelper;

	@Inject
	private BetreuungMonitoringService betreuungMonitoringService;

	@Inject
	private BetreuungStornierenDecisionTree betreuungStornierenDecisionTree;

	@Override
	protected void processEvent(
		@Nonnull LocalDateTime eventTime,
		@Nonnull EventType eventType,
		@Nonnull String key,
		@Nonnull String dto,
		@Nonnull String clientName
	) {

		EventMonitor eventMonitor = new EventMonitor(
			betreuungMonitoringService,
			eventTime,
			key,
			clientName
		);
		Processing processing = attemptProcessing(eventMonitor);

		if (!processing.isProcessingSuccess()) {
			String message = processing.getMessage();
			LOG.warn(
				"Stornierung Event für Betreuung mit RefNr: {} nicht verarbeitet: {}",
				key,
				message
			);
			eventMonitor.record(
				"Stornierung Event wurde nicht verarbeitet: " + message
			);
		}
	}

	@Nonnull
	protected Processing attemptProcessing(@Nonnull EventMonitor eventMonitor) {
		return betreuungService.findBetreuungByReferenzNummer(
			eventMonitor.getRefnr(),
			false
		)
			.map(
				betreuung -> processEventForStornierung(
					eventMonitor,
					betreuung
				)
			)
			.orElseGet(
				() -> Processing.failure("Betreuung nicht gefunden.")
			);
	}

	@Nonnull
	private Processing processEventForStornierung(
		@Nonnull EventMonitor eventMonitor,
		@Nonnull Betreuung betreuung
	) {

		if (betreuung.extractGesuchsperiode().getStatus()
			!= GesuchsperiodeStatus.AKTIV) {
			return Processing.failure("Die Gesuchsperiode ist nicht aktiv.");
		}

		if (eventMonitor.isTooLate(betreuung.getTimestampMutiert())) {
			return Processing.failure(
				"Die Betreuung wurde verändert, nachdem das BetreuungEvent generiert wurde."
			);
		}

		InstitutionExternalClients clients =
			betreuungEventHelper.getExternalClients(
				eventMonitor.getClientName(),
				betreuung
			);

		return clients.getRelevantClient()
			.map(
				client -> processEventForExternalClient(
					eventMonitor,
					betreuung,
					client.getGueltigkeit()
				)
			)
			.orElseGet(
				() -> betreuungEventHelper.clientNotFoundFailure(
					eventMonitor.getClientName(),
					betreuung
				)
			);
	}

	@Nonnull
	private Processing processEventForExternalClient(
		@Nonnull EventMonitor eventMonitor,
		@Nonnull Betreuung betreuung,
		@Nonnull DateRange clientGueltigkeit
	) {

		DateRange gesuchsperiode = betreuung.extractGesuchsperiode()
			.getGueltigkeit();
		Optional<DateRange> overlap = gesuchsperiode.getOverlap(
			clientGueltigkeit
		);
		if (overlap.isEmpty()) {
			return Processing.failure(
				"Der Client hat innerhalb der Periode keine Berechtigung."
			);
		}

		BetreuungEvent betreuungEvent = new BetreuungEvent(
			betreuung,
			eventMonitor
		);
		betreuungStornierenDecisionTree.evaluate(betreuungEvent);

		return Processing.success();
	}

	protected boolean isMutationsMitteilungStatus(
		@Nonnull Betreuungsstatus status
	) {
		return status == Betreuungsstatus.VERFUEGT
			|| status == Betreuungsstatus.BESTAETIGT
			|| status == Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG;
	}
}
