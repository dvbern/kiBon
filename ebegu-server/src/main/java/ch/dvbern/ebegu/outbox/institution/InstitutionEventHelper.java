/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.outbox.institution;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.persistence.Persistence;

@Stateless
public class InstitutionEventHelper {

	@Inject
	private Persistence persistence;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private InstitutionEventConverter institutionEventConverter;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void convertAndFire(String id) {
		InstitutionStammdaten stammdaten = persistence.find(
			InstitutionStammdaten.class,
			id
		);
		if (InstitutionEventUtil.isExportable(stammdaten)) {
			event.fire(institutionEventConverter.of(stammdaten));
			Institution institution = stammdaten.getInstitution();
			institution.setSkipPreUpdate(true);
			institution.setEventPublished(true);
			persistence.merge(institution);
		}
	}
}
