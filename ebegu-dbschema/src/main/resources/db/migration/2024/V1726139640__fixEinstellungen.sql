/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

# Fix bad value in FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF
UPDATE einstellung SET einstellung.value = '99999999'
WHERE einstellung.value = 'null' AND einstellung.einstellung_key = 'FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF';

# Add Erklaerung
UPDATE einstellung SET einstellung.erklaerung = 'Diese Einstellung "FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF" hat Auswirkungen auf alle Mandanten bei der Einkommensveränderung. <br>Ist dieser Wert z.B. 0, erscheint folgender Hinweis: "Achtung: Ein Antrag wegen Einkommensverschlechterung ist nur bis zu einem massgebenden Einkommen von {{maxEinkommenEKV}} CHF möglich."'
WHERE einstellung.einstellung_key = 'FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF';

# Fix bad value in GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN
UPDATE einstellung SET einstellung.value = 'true' WHERE einstellung_key = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' AND einstellung.value = '1';
UPDATE einstellung SET einstellung.value = 'false' WHERE einstellung_key = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' AND einstellung.value = '0';

# Copy missing erklaerungen
UPDATE einstellung
	INNER JOIN gesuchsperiode ON gesuchsperiode_id = gesuchsperiode.id
	INNER JOIN mandant ON gesuchsperiode.mandant_id = mandant.id
	INNER JOIN
	# Join mit den GP-Einstellungen, die Erklärungen gesetzt haben
	(SELECT einstellung_key, erklaerung, mandant.name as mandant_name,
			CONCAT(SUBSTR(gesuchsperiode.gueltig_ab, 1, 4), '/', SUBSTR(gesuchsperiode.gueltig_bis, 1, 4)) as gp
	 FROM einstellung
		  INNER JOIN gesuchsperiode ON gesuchsperiode_id = gesuchsperiode.id
		  INNER JOIN mandant ON gesuchsperiode.mandant_id = mandant.id
	 # über den Einstellungskey und Mandanten
	 WHERE erklaerung IS NOT NULL AND gemeinde_id IS NULL GROUP BY einstellung_key, mandant.name) as with_erklaerung ON
		einstellung.einstellung_key = with_erklaerung.einstellung_key AND mandant.name = with_erklaerung.mandant_name
SET einstellung.erklaerung = with_erklaerung.erklaerung
WHERE einstellung.erklaerung IS NULL AND gemeinde_id IS NULL
