/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.util.Objects;

import javax.annotation.Nonnull;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.PersonensucheAuditLog;
import ch.dvbern.ebegu.persistence.Persistence;

/**
 * Service zum Verwalten von PersonensucheAuditLogs
 */
@Stateless
@Local(PersonenSucheAuditLogService.class)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class PersonenSucheAuditLogServiceBean extends AbstractBaseService
	implements
	PersonenSucheAuditLogService {

	@Inject
	private Persistence persistence;

	@Nonnull
	@Override
	public PersonensucheAuditLog savePersonenSucheAuditLog(
		@Nonnull PersonensucheAuditLog logEintrag
	) {
		Objects.requireNonNull(
			logEintrag,
			"Personen Suche Log muss gesetzt sein"
		);
		return persistence.persist(logEintrag);
	}
}
