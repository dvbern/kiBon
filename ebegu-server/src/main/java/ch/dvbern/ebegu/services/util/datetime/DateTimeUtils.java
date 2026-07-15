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

package ch.dvbern.ebegu.services.util.datetime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

import jakarta.ejb.Stateless;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

/**
 * Erweitert {@link LocalDate} und {@link LocalDateTime} um häufig verwendete, Zeit- und Datumsspezifische Funktionen.
 * Darüber hinaus kann diese Klasse auch verwendet werden, um solche Funktoinen in Unit-Tests zu mocken.
 * Da {@link LocalDate} und {@link LocalDateTime} als final deklariert sind, ist das mit diesen Klassen nämlich nicht
 * möglich.
 */
@Stateless
public class DateTimeUtils {

	private final ZoneId zoneId = ZoneId.of("Europe/Zurich");

	public LocalDateTime now() {
		return LocalDateTime.now(zoneId);
	}

	/**
	 * Parses the given string into a {@link LocalDateTime}. If the string cannot be parsed,
	 * a {@link WebApplicationException} with a {@link Status#BAD_REQUEST} status is thrown.
	 *
	 * @param dateTimeString the string representation of the date and time to be parsed
	 * @return the parsed {@link LocalDateTime} object
	 * @throws WebApplicationException with {@link Status#BAD_REQUEST} if the input string cannot be parsed into a valid
	 * {@link LocalDateTime}
	 */
	public LocalDateTime parseOrThrowBadRequest(String dateTimeString)
		throws WebApplicationException {
		try {
			return LocalDateTime.parse(dateTimeString);
		} catch (DateTimeParseException e) {
			throw new WebApplicationException(
				"Invalid date time string",
				e,
				Status.BAD_REQUEST
			);
		}
	}
}
