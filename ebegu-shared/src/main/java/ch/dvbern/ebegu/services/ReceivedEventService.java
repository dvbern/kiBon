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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import javax.annotation.Nonnull;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import ch.dvbern.ebegu.entities.ReceivedEvent;

public interface ReceivedEventService {

	/**
	 * @return TRUE, when ReceivedEvent already existed in database, FALSE otherwise
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	boolean saveReceivedEvent(@Nonnull ReceivedEvent event);
}
