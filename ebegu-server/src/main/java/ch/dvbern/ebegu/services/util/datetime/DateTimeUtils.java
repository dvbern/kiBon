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

import jakarta.ejb.Stateless;

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
}
