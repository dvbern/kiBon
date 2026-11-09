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

package ch.dvbern.ebegu.nesko;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.HasStekIdentifier;

public abstract class StekUtil {

	/**
	 * Normalizes the provided STEK-identifier by removing all non-digit characters.
	 *
	 * @param identifier the STEK-identifier to be normalized
	 * @return the normalized STEK-identifier containing only numeric characters
	 */
	public static String normalizeStekIdentifier(String identifier) {
		return identifier.replaceAll("\\D", "");
	}

	/**
	 * Sets the STEK identifiers (AHV / ZPV) on the provided {@link Benutzer}. If the value from the token is null, the
	 * value on the {@link Benutzer} is not reset.
	 *
	 * @param hasStekIdentifier to set the STEK identifiers on
	 * @param zpvNummer the ZPV number to set on the {@link HasStekIdentifier}
	 * @param ahvNummer the AHV number to set on the {@link HasStekIdentifier}
	 */
	public static void setStekIdentifierNullsafe(
		HasStekIdentifier hasStekIdentifier,
		@Nullable String zpvNummer,
		@Nullable String ahvNummer
	) {
		if (zpvNummer != null) {
			hasStekIdentifier.setZpvNummer(zpvNummer);
		}
		if (ahvNummer != null) {
			hasStekIdentifier.setAhvNummer(ahvNummer);
		}
	}
}
