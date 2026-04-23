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

package ch.dvbern.ebegu.backchannellogout;

import java.io.Serial;

public class EventQueueConfigurationProviderException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -1176055736234966210L;

	/**
	 * Creates a new {@link RuntimeException} that is thrown when a configuration property for accessing an event queue
	 * is
	 * missing or not approprietly defined.
	 *
	 * @param message The reason why this execption was thrown.
	 */
	public EventQueueConfigurationProviderException(String message) {
		super(message);
	}
}
