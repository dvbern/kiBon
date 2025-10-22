/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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
package ch.dvbern.ebegu.ws.converters;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nullable;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * COPY OF ch.dvbern.lib.date.converters.zoned.LocalDateTimeUTCConverter, with jakarta imports.
 *
 * Converts a Time String that is formatted as an Iso-Zoned-Date-Time (preferably UTC) and converts it to the
 * LocalDefaultTimezone
 *
 * Also provides the reverse Functionality wherby the LocalDate (without Timezone information) is
 * interpreted as a Time at the current systemDefault Timezone and then converted into the same
 * time in the UTC Timezone and returned as a String representing that time in the UTC TimeZone
 */
@XmlJavaTypeAdapter(value = LocalDateTimeUTCConverter.class,
	type = ZonedDateTime.class)
public class LocalDateTimeUTCConverter extends
	XmlAdapter<String, LocalDateTime> {

	private final ZoneId localZoneId;

	public LocalDateTimeUTCConverter() {
		this.localZoneId = ZoneId.systemDefault();
	}

	public LocalDateTimeUTCConverter(ZoneId localZoneId) {
		this.localZoneId = localZoneId;
	}

	@Nullable
	@Override
	public LocalDateTime unmarshal(String v) {
		if (isEmpty(v)) {
			return null;
		}
		final ZonedDateTime timeUtc = ZonedDateTime.parse(
			v,
			DateTimeFormatter.ISO_ZONED_DATE_TIME
		);

		final ZonedDateTime localTime = timeUtc.withZoneSameInstant(
			localZoneId
		);
		return localTime.toLocalDateTime();

	}

	private boolean isEmpty(String v) {
		return (v == null || v.isEmpty());
	}

	@Nullable
	@Override
	public String marshal(LocalDateTime v) {
		if (v == null) {
			return null;
		}
		ZonedDateTime zonedDateTime = ZonedDateTime.of(v, localZoneId); // add local zone id to timestamp
		final ZonedDateTime passedTimeInUTC = zonedDateTime.withZoneSameInstant(
			ZoneId.of("UTC")
		); //transfrom to utc
		return passedTimeInUTC.format(
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
		);
	}
}
