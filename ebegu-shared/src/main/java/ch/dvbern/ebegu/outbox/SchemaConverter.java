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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.outbox;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.avro.Schema;

@Converter(autoApply = true)
public class SchemaConverter implements AttributeConverter<Schema, String> {

	@Override
	@Nullable
	public String convertToDatabaseColumn(@Nullable Schema attribute) {
		return attribute == null ? null : attribute.toString();
	}

	@Override
	@Nullable
	public Schema convertToEntityAttribute(@Nullable String dbData) {
		return dbData == null ? null : new Schema.Parser().parse(dbData);
	}
}
