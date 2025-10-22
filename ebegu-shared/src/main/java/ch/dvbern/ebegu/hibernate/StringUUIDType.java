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

package ch.dvbern.ebegu.hibernate;

import java.io.Serial;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType;

/**
 * A type mapping {@link java.sql.Types#BINARY} and to a String repesenting a UUID
 *
 */
public class StringUUIDType extends
	AbstractSingleColumnStandardBasicType<String> {

	public static final StringUUIDType INSTANCE = new StringUUIDType();

	@Serial
	private static final long serialVersionUID = 8299338720540799781L;

	public StringUUIDType() {
		super(BinaryJdbcType.INSTANCE, StringUUIDTypeDescriptor.INSTANCE);
	}

	public String getName() {
		return "string-uuid-binary";
	}

}
