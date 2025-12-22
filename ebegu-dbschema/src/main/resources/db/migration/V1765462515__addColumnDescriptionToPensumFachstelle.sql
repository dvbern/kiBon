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


ALTER TABLE pensum_fachstelle
	ADD COLUMN description TEXT DEFAULT NULL;
ALTER TABLE pensum_fachstelle_aud
	ADD COLUMN description TEXT DEFAULT NULL;

UPDATE einstellung
SET erklaerung = concat('"Kanton Bern: Fachstellenname" aktiviert die Fachstellentypen für den Kanton Bern und ',
						'ativiert darüber hinaus auch das Feature zur Eingabe einer, von der Gemeinde bezeichneten ',
						'Fachstelle.')
WHERE einstellung_key = 'FACHSTELLEN_TYP';
