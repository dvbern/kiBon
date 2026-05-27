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

ALTER TABLE benutzer ADD COLUMN ahv_nummer VARCHAR(34) NULL;
ALTER TABLE benutzer_aud ADD COLUMN ahv_nummer VARCHAR(34) NULL;
ALTER TABLE gesuchsteller ADD COLUMN ahv_nummer VARCHAR(34) NULL;
ALTER TABLE gesuchsteller_aud ADD COLUMN ahv_nummer VARCHAR(34) NULL;

ALTER TABLE steuerdaten_request ADD COLUMN ahv_nummer bigint NULL;
ALTER TABLE steuerdaten_request MODIFY COLUMN zpv_nummer bigint NULL;

ALTER TABLE steuerdaten_response ADD COLUMN sozialversicherungs_nr_antragsteller bigint NULL;
ALTER TABLE steuerdaten_response ADD COLUMN sozialversicherungs_nr_dossiertraeger bigint NULL;
ALTER TABLE steuerdaten_response ADD COLUMN sozialversicherungs_nr_partner bigint NULL;
