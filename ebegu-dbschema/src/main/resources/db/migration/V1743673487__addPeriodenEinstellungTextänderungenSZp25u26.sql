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

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gesuchsperiode_id, erklaerung)
	(
		SELECT UNHEX(REPLACE(UUID(), '-', '')) AS id,
			   NOW() AS timestamp_erstellt,
			   NOW() AS timestamp_muiert,
			   'ebegu' AS user_erstellt,
			   'ebegu' AS user_mutiert,
			   '0' AS version,
			   'TEXTE_SZ_25' AS einstellungkey,
			   'false' AS value,
			   id AS gesuchsperiode_id,
			   '<p class="margin-bottom-10">Diese periodenabhängigen Textanpassungen sind für den Mandanten SZ ab der Periode 25/26 relevant, weil sich auf der Platzbestätigungsmaske einiges ändert:</p><ul><li>Unterscheidung zwischen Betreuungen während der Schulzeit & schulfreien Zeit wird deaktiviert und es gilt ein einheitlicher Normkostentarif pro Betreuungstyp</li><li>Neuer Monatsfaktor von 4 (20 Tage pro Monat)</li><li>Keine Monatsstückelung - die Anspruchsberechnung erfolgt monatsweise und es gibt nur noch ein Zeitabschnitt pro Monat auf der Verfügung</li><li>Abweichungsmaske wird aktiviert, damit Zusatztage/Minustage oder zusätzliche Tage während der schulfreien Zeit gemeldet werden können</li></ul>' AS erklaerung
		FROM gesuchsperiode
	);
