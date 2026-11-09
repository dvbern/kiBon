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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.einstellung;

/**
 * <p>Determines on which lowest level a {@link Einstellung} can be configured.
 * Order is MANDANT -> GEMEINDE.
 * </p>
 * <p>
 * An Einstellung of EinstellungTyp GEMEINDE can be configured on the level of MANDANT
 * as well as on the leve of GEMEINDE.
 * </p>
 * <p>
 * An Einstellung of EinstellungTyp MANDANT can only be configured on
 * the level of MANDANT
 * </p>
 *
 */
public enum EinstellungTyp {
	MANDANT, GEMEINDE
}
