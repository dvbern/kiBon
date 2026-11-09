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
 *
 */

package ch.dvbern.ebegu.einstellung;

import org.jetbrains.annotations.Nullable;

/**
 * @param keyGroup the group the key belongs to, null if ungrouped
 * @param subKeyGroup the optional sub group within the group
 * @param keyOrder the position of the key within its group. The backend only determines the order, sorting is done by
 * the consumer. Keys without an explicit order are placed last
 */
public record KeyGrouping(ConfigurableKeyGroup keyGroup,
						  @Nullable SubKeyGroup subKeyGroup,
						  int keyOrder) {

	public static final int DEFAULT_ORDER = 9999;

	public static KeyGrouping of(ConfigurableKeyGroup keyGroup) {
		return new KeyGrouping(keyGroup, null, DEFAULT_ORDER);
	}

	public static KeyGrouping of(ConfigurableKeyGroup keyGroup, int keyOrder) {
		return new KeyGrouping(keyGroup, null, keyOrder);
	}

	public static KeyGrouping of(
		ConfigurableKeyGroup keyGroup,
		SubKeyGroup subKeyGroup
	) {
		return new KeyGrouping(keyGroup, subKeyGroup, DEFAULT_ORDER);
	}

	public static KeyGrouping of(
		ConfigurableKeyGroup keyGroup,
		SubKeyGroup subKeyGroup,
		int keyOrder
	) {
		return new KeyGrouping(keyGroup, subKeyGroup, keyOrder);
	}

	public static KeyGrouping NONE() {
		return new KeyGrouping(null, null, DEFAULT_ORDER);
	}
}
