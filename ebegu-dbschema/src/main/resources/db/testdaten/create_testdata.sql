UPDATE application_property SET value = 'true' WHERE name = 'DUMMY_LOGIN_ENABLED';
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR';
# noinspection SqlWithoutWhere
UPDATE gesuchsperiode SET status = 'AKTIV';

# Benutzer System erstellen
INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, nachname, username, vorname, mandant_id, externaluuid, status) VALUES (UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'hallo@dvbern.ch', 'System', 'system', '', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), null, 'AKTIV');
INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id) VALUES (UNHEX(REPLACE('2a7b78ec-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '2017-01-01', '9999-12-31', 'SUPER_ADMIN', UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), null, null);

# Gemeinden Bern und Ostermundigen erstellen, inkl. Adressen und Gemeindestammdaten. Sequenz anpassen
INSERT INTO gemeinde (
	id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
	betreuungsgutscheine_startdatum, angebotbg, angebotts, angebotfi)
VALUES (
		   UNHEX(REPLACE('80a8e496-b73c-4a4a-a163-a0b2caf76487', '-', '')), '2018-01-01 00:00:00', '2018-01-01 00:00:00', 'flyway', 'flyway', 0,
		   'London', 2, UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), 'AKTIV', 99999, '2016-01-01', true, false, false);
INSERT INTO gemeinde (
	id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
	betreuungsgutscheine_startdatum, angebotbg, angebotts, angebotfi)
VALUES (
		   UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')), '2018-01-01 00:00:00', '2018-01-01 00:00:00', 'flyway', 'flyway', 0,
		   'Paris', 1, UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), 'AKTIV', 99998, '2016-01-01', true, false, false);

INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde,
					 hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('4a7afba9-4af0-11e9-9a3a-afd41a03c0bb', '-', '')),
																							 '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway',
																							 'flyway', 0, null, '2018-01-01', '9999-01-01', 'Paris', '21',
																							 'CH', 'Jugendamt', 'Paris', '3008', 'Effingerstrasse', null);
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde,
					 hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('4a7d4ba5-4af0-11e9-9a3a-afd41a03c0bb', '-', '')),
																							 '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway',
																							 'flyway', 0, null, '2018-01-01', '9999-01-01', 'London', '1',
																							 'CH', 'Gemeinde', 'London', '3072', 'Schiessplatzweg', null);

INSERT INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, default_benutzer_id,
								 default_benutzerts_id, gemeinde_id, adresse_id, mail, telefon, webseite, beschwerde_adresse_id, korrespondenzsprache,
								 logo_content, bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
								 benachrichtigung_bg_email_auto, benachrichtigung_ts_email_auto,
								 standard_dok_signature) VALUES(UNHEX(REPLACE('4a7d313f-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway', 'flyway', 0, UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')), UNHEX(REPLACE('4a7afba9-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), 'bss@bern.ch', '+41 31 321 61 11', 'https://www.bern.ch', null, 'DE_FR', null, 'BIC', 'CH93 0076 2011 6238 5295 7', 'Paris Kontoinhaber', true, true, true, true);
INSERT INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, default_benutzer_id,
								 default_benutzerts_id, gemeinde_id, adresse_id, mail, telefon, webseite, beschwerde_adresse_id, korrespondenzsprache,
								 logo_content, bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
								 benachrichtigung_bg_email_auto, benachrichtigung_ts_email_auto, standard_dok_signature) VALUES (UNHEX(REPLACE('4a7dc6e5-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), '2018-10-23 00:00:00', '2018-10-23 00:00:00', 'flyway', 'flyway', 0, UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), UNHEX(REPLACE('80a8e496-b73c-4a4a-a163-a0b2caf76487', '-', '')), UNHEX(REPLACE('4a7d4ba5-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), 'info@ostermundigen.ch', '+41 31 930 14 14', 'https://www.ostermundigen.ch', null, 'DE', null, 'BIC', 'CH93 0076 2011 6238 5295 7', 'London Kontoinhaber', true, true, true, true);

UPDATE sequence SET current_value = 2 WHERE sequence_type = 'GEMEINDE_NUMMER';

# Test-Institutionen erstellen
INSERT INTO traegerschaft (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, active) VALUES (UNHEX(REPLACE('f9ddee82-81a1-4cda-b273-fb24e9299308', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, 'Kitas & Tagis Stadt Bern', true);

# Kita und Tagesfamilien
INSERT INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('1b6f476f-e0f5-4380-9ef6-836d688853a3', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Brünnen', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), UNHEX(REPLACE('f9ddee82-81a1-4cda-b273-fb24e9299308', '-', '')), 'AKTIV', false);
INSERT INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('3559c33b-1ca1-414d-b227-06affafa0dcd', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Tageseltern Bern', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), null, 'AKTIV', false);
INSERT INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('ab353df1-47ca-4618-b849-2265cf1c356a', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Weissenstein', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), UNHEX(REPLACE('f9ddee82-81a1-4cda-b273-fb24e9299308', '-', '')), 'AKTIV', false);

INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('bc0cbf67-4a68-4e0e-8107-9316ee3f00a3', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '4', 'CH', 'Tageseltern Bern', 'Bern', '3005', 'Gasstrasse', null);
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('9d743bc2-8731-47ff-a979-d4bb1d4203c0', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '5', 'CH', 'Weissenstein', 'Bern', '3007', 'Weberstrasse', null);
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('68992b60-8a1a-415c-a43d-c8c349b73ff8', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '27', 'CH', 'Brünnen', 'Bern', '3027', 'Colombstrasse', null);

INSERT INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id, alterskategorie_baby, alterskategorie_vorschule, alterskategorie_kindergarten, alterskategorie_schule, subventionierte_plaetze, anzahl_plaetze, anzahl_plaetze_firmen) VALUES (UNHEX(REPLACE('246b5afc-e3f6-41a6-8a98-cd44310678da', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, null, null,false, false, false, false, false, null, null);
INSERT INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id, alterskategorie_baby, alterskategorie_vorschule, alterskategorie_kindergarten, alterskategorie_schule, subventionierte_plaetze, anzahl_plaetze, anzahl_plaetze_firmen) VALUES (UNHEX(REPLACE('396a5a9c-7da6-4c25-8e61-34aefdbe722b', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH33 0900 0000 3000 0823 3',null, null, false, false, false, false, false, null, null );
INSERT INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id, alterskategorie_baby, alterskategorie_vorschule, alterskategorie_kindergarten, alterskategorie_schule, subventionierte_plaetze, anzahl_plaetze, anzahl_plaetze_firmen) VALUES (UNHEX(REPLACE('e619ad30-a58a-4b40-aa72-25063145f16b', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, 'CH33 0900 0000 3000 0823 3',null, null,false, false, false, false, false, null, null);

INSERT INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite, oeffnungszeiten) VALUES (UNHEX(REPLACE ('6b7beb6e-6cf3-49d6-84c0-5818d9215ecd', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', 'TAGESFAMILIEN', UNHEX(REPLACE('bc0cbf67-4a68-4e0e-8107-9316ee3f00a3', '-', '')), UNHEX(REPLACE('3559c33b-1ca1-414d-b227-06affafa0dcd', '-', '')),  null, null, UNHEX(REPLACE('246b5afc-e3f6-41a6-8a98-cd44310678da', '-', '')), 'mail@example.com', null, null, null);
INSERT INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite, oeffnungszeiten) VALUES (UNHEX(REPLACE('945e3eef-8f43-43d2-a684-4aa61089684b', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', 'KITA', UNHEX(REPLACE('9d743bc2-8731-47ff-a979-d4bb1d4203c0', '-', '')), UNHEX(REPLACE('ab353df1-47ca-4618-b849-2265cf1c356a', '-', '')), null, null, UNHEX(REPLACE('396a5a9c-7da6-4c25-8e61-34aefdbe722b', '-', '')), 'mail@example.com', null, null, null);
INSERT INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite, oeffnungszeiten) VALUES (UNHEX(REPLACE('9a0eb656-b6b7-4613-8f55-4e0e4720455e', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', 'KITA', UNHEX(REPLACE('68992b60-8a1a-415c-a43d-c8c349b73ff8', '-', '')), UNHEX(REPLACE('1b6f476f-e0f5-4380-9ef6-836d688853a3', '-', '')), null,  null, UNHEX(REPLACE('e619ad30-a58a-4b40-aa72-25063145f16b', '-', '')), 'mail@example.com', null, null, null);

# Tagesschule
INSERT INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('f7abc530-5d1d-4f1c-a198-9039232974a0', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, 'Tagesschule', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), null, 'AKTIV', false);

INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('febf3cd1-4bd9-40eb-b65f-fd9b823b1270', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '21', 'CH', 'Tagesschule', 'Bern', '3008', 'Effingerstrasse', null);

INSERT INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite, oeffnungszeiten) VALUES (UNHEX(REPLACE('199ac4a1-448f-4d4c-b3a6-5aee21f89613', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', 'TAGESSCHULE', UNHEX(REPLACE('febf3cd1-4bd9-40eb-b65f-fd9b823b1270', '-', '')), UNHEX(REPLACE('f7abc530-5d1d-4f1c-a198-9039232974a0', '-', '')), null, null, null, 'mail@example.com', null, null, null);

update mandant set angebotts = true;
update mandant set angebotfi = true;

update gemeinde set angebotts = true;
update gemeinde set angebotfi = true;

-- Zusatzgutschein-Konfigurationen überschreiben am Beispiel Paris

-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED' as einstellung_key,
			 FALSE 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA' as einstellung_key,
			 '0.00' 							as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO' as einstellung_key,
			 '0.00' 							as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA' as einstellung_key,
			 'VORSCHULALTER' 							as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO' as einstellung_key,
			 'VORSCHULALTER' 							as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED' as einstellung_key,
			 FALSE 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA' as einstellung_key,
			 '0.00' 							as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO' as einstellung_key,
			 '0.00' 							as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED' as einstellung_key,
			 FALSE 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;

-- GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
						 einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-01-01 00:00:00'              as timestamp_erstellt,
			 '2020-01-01 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT' as einstellung_key,
			 '0' 								as value,
			 UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', '')) as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id
	  from gesuchsperiode as gp) as tmp;
