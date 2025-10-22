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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
SET @mandant_id_zug = UNHEX(REPLACE('8d24a546-6434-11ef-8aab-005056bde697', '-', ''));
SET @mandant_id_schwyz = UNHEX(REPLACE('08687de9-b3d0-11ee-829a-0242ac160002', '-', ''));
SET @gesuchsperiode_id = UNHEX(REPLACE('be72063e-643b-11ef-8aab-005056bde697', '-', ''));
SET @aktuelle_gp_schwyz := (SELECT gesuchsperiode.id
							  FROM gesuchsperiode
							      WHERE mandant_id = @mandant_id_schwyz AND gueltig_ab = '2024-08-01');

SET @veranlagung_user_id_zg = UNHEX(REPLACE('99999999-2227-2222-2222-222222222222', '-', ''));
SET @mutation_user_id_zg = UNHEX(REPLACE('88888888-2227-2222-2222-222222222222', '-', ''));

INSERT INTO mandant
VALUES (@mandant_id_zug, '2021-11-30 00:00:00', '2021-11-30 00:00:00', 'flyway', 'flyway', 0, NULL, 'Kanton Zug', 'ZUG', false, 1, 1);

# APPLICATION PROPERTIES
INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
								  version, vorgaenger_id, name, value, mandant_id)
SELECT UNHEX(REPLACE(UUID(), '-', '')), timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
	   NULL, name, value, @mandant_id_zug
FROM application_property
WHERE mandant_id = @mandant_id_schwyz AND
	  NOT EXISTS(SELECT name
				 FROM application_property a_p
				 WHERE mandant_id = @mandant_id_zug AND
					   a_p.name = application_property.name);

# BFS Gemeinden
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab)
VALUES
	(UUID(), @mandant_id_zug, 'SZ', 1701, 'Baar', '1991-01-01'),
	(UUID(), @mandant_id_zug, 'SZ', 1702, 'Chame', '1991-01-01'),
	(UUID(), @mandant_id_zug, 'SZ', 1703, 'Hünenberg', '1991-01-01'),
	(UUID(), @mandant_id_zug, 'SZ', 1704, 'Menzingen', '1991-01-01'),
	(UUID(), @mandant_id_zug, 'SZ', 1705, 'Neuheim', '1991-01-01'),
	(UUID(), @mandant_id_zug, 'SZ', 1706, 'Oberägeri', '1991-01-01'),
	(UUID(), @mandant_id_zug, 'SZ', 1707, 'Risch', '1991-01-01'),
	(UUID(), @mandant_id_zug, 'SZ', 1708, 'Steinhausen', '1991-01-01'),
	(UUID(), @mandant_id_zug, 'SZ', 1709, 'Unterägeri', '1991-01-01'),
	(UUID(), @mandant_id_zug, 'SZ', 1710, 'Walchwil', '1991-01-01'),
	(UUID(), @mandant_id_zug, 'SZ', 1711, 'Zug', '1991-01-01');

INSERT INTO gesuchsperiode
VALUES (@gesuchsperiode_id, NOW(), NOW(), 'system_zg', 'system_zg', 0, NULL, '2024-08-01', '2025-07-31', NULL,
		'ENTWURF', NULL, NULL, NULL,
		NULL, NULL, NULL, @mandant_id_zug, NULL,
		NULL);

# Einstellungen für Gesuchsperiode kopieren
INSERT INTO einstellung
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system_zg', 'system_zg', 0, einstellung_key, value, NULL,
	   (SELECT gesuchsperiode.id
		FROM gesuchsperiode
			 INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
		WHERE mandant_identifier = 'ZUG'), NULL, erklaerung
FROM einstellung
WHERE gesuchsperiode_id = @aktuelle_gp_schwyz AND gemeinde_id IS NULL AND einstellung.mandant_id is NULL;

# Gemeinde Einstellungen für Gesuchsperiode kopieren
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UNHEX(REPLACE(UUID(), '-', '')), timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, 0,
	   einstellung_key, value, NULL, @gesuchsperiode_id, @mandant_id_zug, erklaerung
FROM einstellung
WHERE mandant_id = @mandant_id_schwyz AND gesuchsperiode_id = @aktuelle_gp_schwyz AND NOT EXISTS(
	SELECT einstellung_key FROM einstellung e1 WHERE e1.gesuchsperiode_id =  @gesuchsperiode_id
		and e1.mandant_id = @mandant_id_zug AND e1.einstellung_key = einstellung.einstellung_key
) AND gemeinde_id IS NULL;

INSERT INTO sequence(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, sequence_type, current_value, mandant_id)
VALUES (
		   UNHEX(REPLACE(UUID(), '-', '')), # id
		   '2018-01-01 00:00:00', # timestamp_erstellt
		   '2018-01-01 00:00:00', # timestamp_mutiert
		   'flyway', # user_erstellt
		   'flyway', # user_mutiert
		   0, # version
		   'FALL_NUMMER', # sequence_type
		   100, # current_value
		   @mandant_id_zug);


# Unbekannte Institutionen
INSERT INTO institution(id,
						timestamp_erstellt,
						timestamp_mutiert,
						user_erstellt,
						user_mutiert,
						version,
						vorgaenger_id,
						name,
						mandant_id,
						traegerschaft_id,
						status,
						event_published)
SELECT * FROM (SELECT
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   'Unbekannte Kita' as name,
				   @mandant_id_zug as mandant_id,
				   null as traegerschaft_id,
				   'AKTIV' as status,
				   true as event_published
			  ) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', ''))
)
LIMIT 1;



INSERT INTO adresse (id,
					 timestamp_erstellt,
					 timestamp_mutiert,
					 user_erstellt,
					 user_mutiert,
					 version,
					 vorgaenger_id,
					 gueltig_ab,
					 gueltig_bis,
					 gemeinde,
					 hausnummer,
					 land,
					 organisation,
					 ort,
					 plz,
					 strasse,
					 zusatzzeile)
SELECT * FROM (SELECT
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   '1000-01-01' as gueltig_ab,
				   '9999-12-31' as gueltig_bis,
				   null as gemeinde,
				   '21.0' as hausnummer,
				   'CH' as land,
				   null as organisation,
				   'Bern' as ort,
				   '3022.0' as plz,
				   'Nussbaumstrasse' as strasse,
				   null as zusatzzeile
			  ) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM adresse WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', ''))
)
LIMIT 1;


INSERT INTO institution_stammdaten (id,
									timestamp_erstellt,
									timestamp_mutiert,
									user_erstellt,
									user_mutiert,
									version,
									vorgaenger_id,
									gueltig_ab,
									gueltig_bis,
									betreuungsangebot_typ,
									adresse_id,
									institution_id,
									institution_stammdaten_tagesschule_id,
									institution_stammdaten_ferieninsel_id,
									mail,
									telefon,
									webseite)
SELECT * FROM (SELECT
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', '')) as id,
				   '2022-01-01 00:00:00' as timestamp_erstellt,
				   '2022-01-01 00:00:00' as timestamp_mutiert,
				   'flyway' as user_erstellt,
				   'flyway' as user_mutiert,
				   0 as version,
				   null as vorgaenger_id,
				   '1000-01-01' as gueltig_ab,
				   '9999-12-31' as gueltig_bis,
				   'KITA' as betreuungsangebot_typ,
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', '')) as adresse_id,
				   UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', '')) as institution_id,
				   null as institution_stammdaten_tagesschule_id,
				   null as institution_stammdaten_ferieninsel_id,
				   'mail@example.com' as mail,
				   null as telefon,
				   null as webseite
			  ) AS tmp
WHERE NOT EXISTS(
	SELECT id FROM institution_stammdaten WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000016', '-', ''))
)
LIMIT 1;


/* Technischer User Veranlagung */
INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
					  email, nachname, username, vorname, mandant_id, externaluuid, status)
VALUES (@veranlagung_user_id_zg, NOW(), NOW(), 'flyway', 'flyway', 0, null, 'kibon.technical.sz@dvbern.ch', 'kibon', 'kibon SZ', '', @mandant_id_zug, null, 'AKTIV');

INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						  vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway', 'flyway', 0, null, '2020-09-01', '9999-12-31', 'SUPER_ADMIN', @veranlagung_user_id_zg, null, null);

# Technischer User Veranlagung
INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
					  email, nachname, username, vorname, mandant_id, externaluuid, status)
VALUES (@mutation_user_id_zg, NOW(), NOW(), 'flyway', 'flyway', 0, null, 'betreuungEvent.sz@dvbern.ch', 'BetreuungsEvent', 'Mutation SZ', '', @mandant_id_zug, null, 'AKTIV');

INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						  vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway', 'flyway', 0, null, '2020-09-01', '9999-12-31', 'SUPER_ADMIN', @mutation_user_id_zg, null, null);

UPDATE application_property SET value = 'true' WHERE mandant_id = @mandant_id_zug AND name = 'ABWEICHUNGEN_ENABLED';
UPDATE application_property SET value = '' WHERE mandant_id = @mandant_id_zug AND name = 'ACTIVATED_DEMO_FEATURES';
UPDATE application_property SET value = 'false' WHERE mandant_id = @mandant_id_zug AND name = 'ANGEBOT_TS_ENABLED';
UPDATE application_property SET value = 'false' WHERE mandant_id = @mandant_id_zug AND name = 'ANGEBOT_TFO_ENABLED';
UPDATE application_property SET value = 'false' WHERE mandant_id = @mandant_id_zug AND name = 'ANGEBOT_MITTAGSTISCH_ENABLED';
UPDATE application_property SET value = 'false' WHERE mandant_id = @mandant_id_zug AND name = 'ANGEBOT_FI_ENABLED';
UPDATE application_property SET value = '90' WHERE mandant_id = @mandant_id_zug AND name = 'ANZAHL_TAGE_BIS_WARNUNG_FREIGABE';
UPDATE application_property SET value = '90' WHERE mandant_id = @mandant_id_zug AND name = 'ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE';
UPDATE application_property SET value = 'true' WHERE mandant_id = @mandant_id_zug AND name = 'ERLAUBEN_INSTITUTIONEN_ZU_WAEHLEN';
UPDATE application_property SET value = 'false' WHERE mandant_id = @mandant_id_zug AND name = 'GERES_ENABLED_FOR_MANDANT';
UPDATE application_property SET value = '#0072bd' WHERE name = 'PRIMARY_COLOR' AND mandant_id = @mandant_id_zug;
UPDATE application_property SET value = '#00466f' WHERE name = 'PRIMARY_COLOR_DARK' AND mandant_id = @mandant_id_zug;
UPDATE application_property SET value = '#C6C6C6' WHERE name = 'PRIMARY_COLOR_LIGHT' AND mandant_id = @mandant_id_zug;
UPDATE application_property SET value = 'logo-kibon-zug.svg' WHERE name = 'LOGO_FILE_NAME' AND mandant_id = @mandant_id_zug;
UPDATE application_property SET value = 'logo-kibon-white-zug.svg' WHERE name = 'LOGO_WHITE_FILE_NAME' AND mandant_id = @mandant_id_zug;
UPDATE application_property SET value = 'false' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id = @mandant_id_zug;
