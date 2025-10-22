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

import java.util.UUID;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaType;
import org.hibernate.type.descriptor.java.UUIDJavaType;

/**
 * This descriptor defines how to map StrinUUIDType to the Database
 */
public class StringUUIDTypeDescriptor extends AbstractClassJavaType<String> {

	private static final long serialVersionUID = -394259331570702578L;
	public static final StringUUIDTypeDescriptor INSTANCE =
		new StringUUIDTypeDescriptor();

	public StringUUIDTypeDescriptor() {
		super(String.class);
	}

	@Override
	public String toString(String stringInput) {
		return stringInput;
	}

	@Override
	public <X> X unwrap(String value, Class<X> type, WrapperOptions options) {
		if (value == null) {
			return null;
		}
		return UUIDJavaType.INSTANCE.unwrap(
			UUID.fromString(value),
			type,
			options
		);
	}

	@Override
	public <X> String wrap(X value, WrapperOptions options) {
		if (value == null) {
			return null;
		}
		return UUIDJavaType.INSTANCE.wrap(value, options).toString();
	}
}
