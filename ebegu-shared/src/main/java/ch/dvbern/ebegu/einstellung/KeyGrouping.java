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

public record KeyGrouping(KeyGroup keyGroup,
						  @Nullable SubKeyGroup subKeyGroup) {

	public static KeyGrouping of(KeyGroup keyGroup) {
		return new KeyGrouping(keyGroup, null);
	}

	public static KeyGrouping of(KeyGroup keyGroup, SubKeyGroup subKeyGroup) {
		return new KeyGrouping(keyGroup, subKeyGroup);
	}
}
