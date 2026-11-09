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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

export enum STEKUpdateResult {
    SUCCESS = 'SUCCESS',
    GESUCH_NOT_IN_STATE_FOR_STEK_UPDATE = 'GESUCH_NOT_IN_STATE_FOR_STEK_UPDATE',
    ERROR_NO_STEK_IDENTIFIER = 'ERROR_NO_STEK_IDENTIFIER',
    ERROR_BAD_GESUCHSTELLER = 'ERROR_BAD_GESUCHSTELLER',
    ERROR_STEK_IDENTIFIER_ALREADY_IN_GESUCH = 'ERROR_STEK_IDENTIFIER_ALREADY_IN_GESUCH'
}
