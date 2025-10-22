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

package ch.dvbern.ebegu.entities.meldungsfenster.converters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import ch.dvbern.ebegu.entities.meldungsfenster.MeldungsfensterRole;

@Converter(autoApply = true)
public class MeldungsfensterRoleListConverter implements
	AttributeConverter<List<MeldungsfensterRole>, String> {
	@Override
	public String convertToDatabaseColumn(
		List<MeldungsfensterRole> meldungsfensterRole
	) {
		// that's the case in an audit delete entry
		if (meldungsfensterRole == null) {
			return null;
		}
		return meldungsfensterRole.stream()
			.map(Enum::name)
			.collect(Collectors.joining(","));
	}

	@Override
	public List<MeldungsfensterRole> convertToEntityAttribute(
		String meldungsfensterRole
	) {
		return Arrays.stream(meldungsfensterRole.split(","))
			.map(MeldungsfensterRole::valueOf)
			.collect(Collectors.toList());
	}
}
