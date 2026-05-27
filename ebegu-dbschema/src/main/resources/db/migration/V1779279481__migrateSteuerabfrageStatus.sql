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

UPDATE finanzielle_situation set steuerdaten_abfrage_status = 'FAILED_KEINE_NUMMER' where steuerdaten_abfrage_status is not null and steuerdaten_abfrage_status = 'FAILED_KEINE_ZPV_NUMMER';
UPDATE finanzielle_situation set steuerdaten_abfrage_status = 'FAILED_KEINE_NUMMER_GS2' where steuerdaten_abfrage_status is not null and steuerdaten_abfrage_status = 'FAILED_KEINE_ZPV_NUMMER_GS2';
UPDATE steuerdaten_anfrage_log set status = 'FAILED_KEINE_NUMMER' where status = 'FAILED_KEINE_ZPV_NUMMER';
UPDATE steuerdaten_anfrage_log set status = 'FAILED_KEINE_NUMMER_GS2' where status = 'FAILED_KEINE_ZPV_NUMMER_GS2';
