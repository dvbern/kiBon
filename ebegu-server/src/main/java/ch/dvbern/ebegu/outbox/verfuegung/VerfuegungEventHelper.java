/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.outbox.verfuegung;

import java.util.Optional;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionSynchronizationRegistry;

import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

@Stateless
public class VerfuegungEventHelper {

	private static final Logger LOG = LoggerFactory.getLogger(
		VerfuegungEventHelper.class
	);

	@Resource
	private TransactionSynchronizationRegistry txReg;

	@Inject
	private Persistence persistence;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private VerfuegungEventConverter verfuegungEventConverter;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void convertAndFire(String id) {
		Verfuegung verfuegung = persistence.find(Verfuegung.class, id);

		LOG.info(
			"Converting {} in Thread {} and Transaction {}",
			requireNonNull(verfuegung.getBetreuung()).getReferenzNummer(),
			Thread.currentThread(),
			txReg.getTransactionKey()
		);

		Optional<VerfuegungVerfuegtEvent> eventOpt = verfuegungEventConverter
			.of(verfuegung);

		eventOpt.ifPresent(verfuegungVerfuegtEvent -> {
			this.event.fire(verfuegungVerfuegtEvent);
			verfuegung.setSkipPreUpdate(true);
			verfuegung.setEventPublished(true);
			persistence.merge(verfuegung);
		});
	}
}
