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

ALTER TABLE verrechnung_kibon
	ADD COLUMN IF NOT EXISTS mandant_id BINARY(16) NOT NULL DEFAULT (UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')));

UPDATE verrechnung_kibon
	JOIN verrechnung_kibon_detail
ON verrechnung_kibon.id = verrechnung_kibon_detail.verrechnung_kibon_id
	JOIN gemeinde ON verrechnung_kibon_detail.gemeinde_id = gemeinde.id
	SET verrechnung_kibon.mandant_id = gemeinde.mandant_id
WHERE verrechnung_kibon_detail.verrechnung_kibon_id = verrechnung_kibon.id;

ALTER TABLE verrechnung_kibon
	ALTER COLUMN mandant_id DROP DEFAULT;

ALTER TABLE verrechnung_kibon
	ADD CONSTRAINT FK_verrechnung_kibon_mandant_id
		FOREIGN KEY (mandant_id) REFERENCES mandant(id);
