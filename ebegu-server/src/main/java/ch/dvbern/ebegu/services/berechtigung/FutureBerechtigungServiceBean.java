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
 */

package ch.dvbern.ebegu.services.berechtigung;

import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.berechtigung.FutureBerechtigung;
import ch.dvbern.ebegu.persistence.Persistence;

/**
 * Stateless implementation of the FutureBerechtigungService interface that provides
 * functionality to manage FutureBerechtigung entities.
 *
 * This class leverages the injected Persistence component to handle database
 * operations related to FutureBerechtigung, specifically for removing entity instances.
 */
@Stateless
@Local
public class FutureBerechtigungServiceBean {

	@Inject
	private Persistence persistence;

	/**
	 * Removes the provided {@code FutureBerechtigung} entity from the persistence context.
	 *
	 * This method deletes the entity specified by the {@code futureBerechtigung} parameter
	 * from the underlying database. The entity should not be detached from the persistence
	 * context at the time of invocation.
	 *
	 * @param futureBerechtigung the {@code FutureBerechtigung} entity to be removed
	 * from the database; must not be {@code null}
	 * @throws IllegalArgumentException if the provided entity is not a managed entity
	 * or if it is already detached
	 * @throws jakarta.persistence.TransactionRequiredException if the method is invoked
	 * without an active transaction in a transactional
	 * context
	 */
	public void removeFutureBerechtigung(
		FutureBerechtigung futureBerechtigung
	) {
		persistence.remove(futureBerechtigung);
	}
}
