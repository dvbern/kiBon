/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

ALTER TABLE gesuch
	ADD COLUMN status_marker TINYINT(1) AS (
  CASE
    WHEN status NOT IN ('VERFUEGT', 'NUR_SCHULAMT', 'BESCHWERDE_HAENGIG', 'PRUEFUNG_STV', 'IN_BEARBEITUNG_STV', 'GEPRUEFT_STV', 'KEIN_ANGEBOT', 'IGNORIERT') THEN 1
    ELSE NULL
  END
) STORED;

CREATE UNIQUE INDEX UK_gesuch_dossier_gesuchsperiode_status
	ON gesuch (dossier_id, gesuchsperiode_id, status_marker);
