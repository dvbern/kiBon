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
-- funktion speichert die gesuchsperiode id für eine gesuchsperiode gültig ab (input) in der übergebenen variable gp_id.
-- falls keine periode mit dem übergebenen gültig_ab datum existeirt wird eine neue uuid in die variable gespeichert
DELIMITER //
create or replace procedure select_gesuchsperiode(IN gueltig_ab_input date,IN mandant_id_input binary(16),OUT gp_id binary(16))
begin
    IF EXISTS(select id from gesuchsperiode where mandant_id = mandant_id_input and gueltig_ab = gueltig_ab_input)
    THEN set gp_id = (select id from gesuchsperiode where mandant_id = mandant_id_input and gueltig_ab = gueltig_ab_input);
    ELSE set gp_id = UNHEX(REPLACE(UUID(), '-', ''));
    END IF;
end;
//

DELIMITER ;

# Variables definition
SET @mandant_id = UNHEX(REPLACE('76783c4a-def2-4d0c-9e0f-209a7b190d15', '-', ''));
SET @mandant_name = 'DVB';
SET @url_code = 'dv';
SET @start_datum_erste_periode = '2025-08-01';
SET @testgemeinde_name = 'Berlin';
SET @second_testgemeinde_name = 'Hamburg';
SET @bfs_nummer_testgemeinde = '9990';
SET @bfs_nummer_second_testgemeinde = '9989';

SET @testgemeinde_id = UNHEX(REPLACE('a4f472fe-a9cf-44c2-a34c-66cb78046a9f', '-', ''));
SET @second_testgemeinde_id = UNHEX(REPLACE('4ec3b3a5-85f8-4e50-a69d-ad0321af0883', '-', ''));
SET @traegerschaft_id = UNHEX(REPLACE('4b2b6105-f6a8-432f-9f42-e7d1c069d411', '-', ''));
SET @bruennen_stammdaten_id = UNHEX(REPLACE('497a70cd-0c22-46e1-9787-1efffc8b73d8', '-', ''));
SET @weissenstein_stammdaten_id = UNHEX(REPLACE('acbc3c07-0c67-4bf0-9bb1-d4d92f65b3c4', '-', ''));
SET @tfo_stammdaten_id = UNHEX(REPLACE('24f9aa31-ced6-42e8-b046-013c61d607dd', '-', ''));
SET @sozialdienst_id =  UNHEX(REPLACE('36b12691-c395-4e21-a412-8e3e69e9fd00', '-', ''));

SET @technical_benutzer_id = UNHEX(REPLACE('99999999-2228-2222-2222-222222222222', '-', ''));
SET @betreuung_mitteilung_user_id = UNHEX(REPLACE('88888888-2228-2222-2222-222222222222', '-', ''));

SET @unbekannte_kita_id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000020', '-', ''));
SET @unbekannte_tfo_id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000021', '-', ''));


START TRANSACTION;

call select_gesuchsperiode(@start_datum_erste_periode, @mandant_id, @gesuchsperiode_id);
SET @gemeinde_adresse_id = UNHEX(REPLACE(UUID(), '-', ''));
SET @gemeinde_stammdaten_korrespondenz_id = UNHEX(REPLACE(UUID(), '-', ''));

SET @gemeinde_second_adresse_id = UNHEX(REPLACE(UUID(), '-', ''));
SET @gemeinde_second_stammdaten_korrespondenz_id = UNHEX(REPLACE(UUID(), '-', ''));

SET @institution_bruennen_id =  UNHEX(REPLACE(UUID(), '-', ''));
SET @adresse_bruennen_id = UNHEX(REPLACE(UUID(), '-', ''));
SET @auszahlungsdaten_bruennen_id = UNHEX(REPLACE(UUID(), '-', ''));
SET @stammdaten_bg_bruennen_id = UNHEX(REPLACE(UUID(), '-', ''));

SET @institution_weissenstein_id =  UNHEX(REPLACE(UUID(), '-', ''));
SET @adresse_weissenstein_id = UNHEX(REPLACE(UUID(), '-', ''));
SET @auszahlungsdaten_weissenstein_id = UNHEX(REPLACE(UUID(), '-', ''));
SET @stammdaten_bg_weissenstein_id = UNHEX(REPLACE(UUID(), '-', ''));

SET @institution_tfo_id =  UNHEX(REPLACE(UUID(), '-', ''));
SET @adresse_tfo_id = UNHEX(REPLACE(UUID(), '-', ''));
SET @auszahlungsdaten_tfo_id = UNHEX(REPLACE(UUID(), '-', ''));
SET @stammdaten_bg_tfo_id = UNHEX(REPLACE(UUID(), '-', ''));

SET @sozialdienst_adresse_id = UNHEX(REPLACE(UUID(), '-', ''));

SET @system_user = UNHEX(REPLACE(UUID(), '-', ''));

# Benutzer System erstellen
INSERT INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, nachname, username, vorname, mandant_id, externaluuid, status)
VALUES (@system_user, now(), now(), 'flyway', 'flyway', 0, null, 'hallo@dvbern.ch', 'System', 'system', '', @mandant_id, null, 'AKTIV');
INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2017-01-01', '9999-12-31', 'SUPER_ADMIN', @system_user, null, null);

# Test Gemeinden erstellen, inkl. Adressen und Gemeindestammdaten. Sequenz anpassen
INSERT INTO gemeinde (
	id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
	betreuungsgutscheine_startdatum, tagesschulanmeldungen_startdatum, ferieninselanmeldungen_startdatum, angebotbg,
                      angebotts, angebotfi, gueltig_bis, besondere_volksschule, nur_lats, event_published, angebotbgtfo)
SELECT @testgemeinde_id,NOW(),NOW(),'flyway','flyway',0,
       @testgemeinde_name,max(gemeinde_nummer)+1,@mandant_id,'AKTIV',@bfs_nummer_testgemeinde,
       '2016-01-01','2020-08-01','2020-08-01',TRUE,FALSE,FALSE,'9999-12-31',FALSE,FALSE,FALSE,TRUE from gemeinde;

INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde,
					 hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (@gemeinde_adresse_id,
																							 NOW(), NOW(), 'flyway',
                                                                                             'flyway', 0, null, '2018-01-01', '9999-01-01',
					                                                                         @testgemeinde_name, '1',
                                                                                             'CH', 'Gemeinde', @testgemeinde_name, '640', 'Bahnhofstrasse', null);

INSERT INTO gemeinde_stammdaten_korrespondenz (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, logo_content, logo_name, logo_spacing_left, logo_spacing_top, logo_type, logo_width, receiver_address_spacing_left, receiver_address_spacing_top, sender_address_spacing_left, sender_address_spacing_top)
VALUES(@gemeinde_stammdaten_korrespondenz_id, NOW(), NOW(), 'flyway', 'flyway', 0, null, null, 123, 15, null, null, 123, 47, 20, 47);

INSERT INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
										default_benutzer_id, default_benutzerts_id, gemeinde_id, adresse_id, mail, telefon, webseite,
										beschwerde_adresse_id, korrespondenzsprache,
										bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
										benachrichtigung_bg_email_auto, benachrichtigung_ts_email_auto,
										standard_dok_signature, ts_verantwortlicher_nach_verfuegung_benachrichtigen, gemeinde_stammdaten_korrespondenz_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway', 'flyway', 0,
        @system_user, @system_user,
        @testgemeinde_id, @gemeinde_adresse_id,
        concat('Gemeinde ', @testgemeinde_name),
        '+41 31 930 15 15', 'https://www.dvbern.ch', null, 'DE', 'BIC', 'CH2089144969768441935',
        'Kontoinhaber', true, true, true,
        true, false, @gemeinde_stammdaten_korrespondenz_id);


# Second Test Gemeinden erstellen, inkl. Adressen und Gemeindestammdaten. Sequenz anpassen
INSERT INTO gemeinde (
    id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, gemeinde_nummer, mandant_id, status, bfs_nummer,
    betreuungsgutscheine_startdatum, tagesschulanmeldungen_startdatum, ferieninselanmeldungen_startdatum, angebotbg,
    angebotts, angebotfi, gueltig_bis, besondere_volksschule, nur_lats, event_published, angebotbgtfo)
SELECT @second_testgemeinde_id,NOW(),NOW(),'flyway','flyway',0,
       @second_testgemeinde_name,max(gemeinde_nummer)+1,@mandant_id,'AKTIV',@bfs_nummer_second_testgemeinde,
       '2016-01-01','2020-08-01','2020-08-01',TRUE,FALSE,FALSE,'9999-12-31',FALSE,FALSE,FALSE,TRUE from gemeinde;

INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde,
                     hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (@gemeinde_second_adresse_id,
                                                                                             NOW(), NOW(), 'flyway',
                                                                                             'flyway', 0, null, '2018-01-01', '9999-01-01',
                                                                                             @second_testgemeinde_name, '1',
                                                                                             'CH', 'Gemeinde', @second_testgemeinde_name, '6700', 'Bahnhofstrasse', null);

INSERT INTO gemeinde_stammdaten_korrespondenz (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, logo_content, logo_name, logo_spacing_left, logo_spacing_top, logo_type, logo_width, receiver_address_spacing_left, receiver_address_spacing_top, sender_address_spacing_left, sender_address_spacing_top)
VALUES(@gemeinde_second_stammdaten_korrespondenz_id, NOW(), NOW(), 'flyway', 'flyway', 0, null, null, 123, 15, null, null, 123, 47, 20, 47);

INSERT INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
                                 default_benutzer_id, default_benutzerts_id, gemeinde_id, adresse_id, mail, telefon, webseite,
                                 beschwerde_adresse_id, korrespondenzsprache,
                                 bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
                                 benachrichtigung_bg_email_auto, benachrichtigung_ts_email_auto,
                                 standard_dok_signature, ts_verantwortlicher_nach_verfuegung_benachrichtigen, gemeinde_stammdaten_korrespondenz_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway', 'flyway', 0,
        @system_user, @system_user,
        @second_testgemeinde_id, @gemeinde_second_adresse_id,
        concat('Gemeinde ', @second_testgemeinde_name),
        '+41 31 930 15 15', 'https://www.dvbern.ch', null, 'DE', 'BIC', 'CH2089144969768441935',
        'Kontoinhaber', true, true, true,
        true, false, @gemeinde_second_stammdaten_korrespondenz_id);



# Test-Institutionen erstellen
INSERT INTO traegerschaft (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, active, email, mandant_id)
	VALUES (@traegerschaft_id, NOW(), NOW(), 'flyway', 'flyway', 0,
	        concat('Kitas & Tagis ', @mandant_name),
	        true,
	        concat('kitastagis-', @url_code, '@mailbucket.dvbern.ch'),
	        @mandant_id);

# Kita und Tagesfamilien
INSERT INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@institution_bruennen_id, NOW(), NOW(), 'flyway', 'flyway', 0, null,
	        concat('Brünnen ', @mandant_name),
            @mandant_id, @traegerschaft_id, 'AKTIV', false);
INSERT INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@institution_tfo_id, NOW(), NOW(), 'flyway', 'flyway', 0, null,
	        concat('Tageseltern ', @mandant_name),
			@mandant_id, @traegerschaft_id, 'AKTIV', false);
INSERT INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (@institution_weissenstein_id, NOW(), NOW(), 'flyway', 'flyway', 0, null,
	        concat('Weissenstein ', @mandant_name),
            @mandant_id, @traegerschaft_id, 'AKTIV', false);

INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (@adresse_tfo_id, NOW(), NOW(), 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null,
	        '4', 'CH', concat('Tageseltern ', @mandant_name), @testgemeinde_name, '4500', 'Gasstrasse', null);
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (@adresse_weissenstein_id, NOW(), NOW(), 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null,
	        '5', 'CH', concat('Weissenstein ', @mandant_name), @testgemeinde_name, '4500', 'Weberstrasse', null);
INSERT INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (@adresse_bruennen_id, NOW(), NOW(), 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '27',
	        'CH', concat('Brünnen ', @mandant_name), @testgemeinde_name , '4500', 'Colombstrasse', null);

INSERT INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (@auszahlungsdaten_bruennen_id, NOW(), NOW(), 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Bruennen', null);
INSERT INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (@auszahlungsdaten_weissenstein_id, NOW(), NOW(), 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Weissenstein', null);
INSERT INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id)
	VALUES (@auszahlungsdaten_tfo_id, NOW(), NOW(), 'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Tageseltern', null);

INSERT INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															   user_mutiert, version, auszahlungsdaten_id,
															   alterskategorie_baby, alterskategorie_vorschule,
															   alterskategorie_kindergarten, alterskategorie_schule,
															   anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (@stammdaten_bg_bruennen_id, NOW(), NOW(),
        'flyway', 'flyway', 0, @auszahlungsdaten_bruennen_id,
        FALSE, FALSE, FALSE,
        FALSE, 30, NULL, '08:00', '18:00',
        0, 0.00, 0.00, 0.00,
        FALSE, FALSE, FALSE, FALSE);

INSERT INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															   user_mutiert, version, auszahlungsdaten_id,
															   alterskategorie_baby, alterskategorie_vorschule,
															   alterskategorie_kindergarten, alterskategorie_schule,
															   anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (@stammdaten_bg_weissenstein_id, NOW(), NOW(),
        'flyway', 'flyway', 0, @auszahlungsdaten_weissenstein_id,
        FALSE, FALSE, FALSE,
        FALSE, 35, NULL, '08:00', '18:00',
        0, 0.00, 0.00, 0.00,
        FALSE, FALSE, FALSE, FALSE);

INSERT INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
															   user_mutiert, version, auszahlungsdaten_id,
															   alterskategorie_baby, alterskategorie_vorschule,
															   alterskategorie_kindergarten, alterskategorie_schule,
															   anzahl_plaetze,
															   anzahl_plaetze_firmen, offen_von, offen_bis,
															   oeffnungstage_pro_jahr,
															   anzahl_kinder_warteliste, summe_pensum_warteliste,
															   dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
															   wochenende_eroeffnung, uebernachtung_moeglich)
VALUES (@stammdaten_bg_tfo_id, NOW(), NOW(),
		'flyway', 'flyway', 0, @auszahlungsdaten_tfo_id,
        FALSE, FALSE, FALSE,
		FALSE, 40, NULL, '08:00', '18:00',
        0, 0.00, 0.00, 0.00,
        FALSE, FALSE, FALSE, FALSE);

INSERT INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (@tfo_stammdaten_id, NOW(), NOW(),
		'flyway', 'flyway', 0, NULL, '2019-08-01', '9999-12-31', 'TAGESFAMILIEN',
		@adresse_tfo_id,
		@institution_tfo_id, NULL, NULL,
		@stammdaten_bg_tfo_id,
        concat('tagesfamilien-', @url_code, '@mailbucket.dvbern.ch'), NULL,
		NULL);

INSERT INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (@weissenstein_stammdaten_id, NOW(), NOW(),
        'flyway', 'flyway', 0, NULL, '2019-08-01', '9999-12-31', 'KITA',
        @adresse_weissenstein_id,
        @institution_weissenstein_id, NULL, NULL,
        @stammdaten_bg_weissenstein_id,
        concat('weissenstein-', @url_code, '@mailbucket.dvbern.ch'),
        NULL,
        NULL);

INSERT INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
										   version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ,
										   adresse_id, institution_id, institution_stammdaten_tagesschule_id,
										   institution_stammdaten_ferieninsel_id,
										   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
VALUES (@bruennen_stammdaten_id, NOW(), NOW(),
        'flyway', 'flyway', 0, NULL, '2019-08-01', '9999-12-31', 'KITA',
        @adresse_bruennen_id,
        @institution_bruennen_id, NULL, NULL,
        @stammdaten_bg_bruennen_id,
        concat('bruennen-', @url_code, '@mailbucket.dvbern.ch'), NULL, NULL);


-- Sozialdienst
SET @sozialdienst_name = concat(@mandant_name, ' Sozialdienst');
INSERT INTO sozialdienst (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
                                 vorgaenger_id, name, status, mandant_id)
VALUES (@sozialdienst_id, now(), now(),
        'flyway', 'flyway', 0, NULL,
        @sozialdienst_name,
        'AKTIV',
        @mandant_id);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
                            vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz,
                            strasse, zusatzzeile)
VALUES (@sozialdienst_adresse_id, now(), now(),
        'flyway', 'flyway', 1, NULL,
        '1000-01-01', '9999-12-31', NULL,
        '2', 'CH', @sozialdienst_name, @testgemeinde_name, '6000',
        'Sozialdienst Strasse', NULL);

INSERT IGNORE INTO sozialdienst_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
                                            version, vorgaenger_id, mail, telefon, webseite, adresse_id,
                                            sozialdienst_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), now(), now(),
        'flyway', 'flyway', 0, NULL,
        concat('test-sozialdienst-', @url_code, '@mailbucket.dvbern.ch'),
        '078 898 98 98', 'www.test.dvbern.ch',
        @sozialdienst_adresse_id,
        @sozialdienst_id);


# APPLICATION PROPERTIES
UPDATE application_property SET value = 'true' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id = @mandant_id;
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id = @mandant_id;

# Gesuchsperiode
UPDATE gesuchsperiode SET status = 'AKTIV' WHERE id = @gesuchsperiode_id;



# Unbekannte Institutionen (KITA)
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
                   @unbekannte_kita_id as id,
                   now() as timestamp_erstellt,
                   now() as timestamp_mutiert,
                   'flyway' as user_erstellt,
                   'flyway' as user_mutiert,
                   0 as version,
                   null as vorgaenger_id,
                   'Unbekannte Kita' as name,
                   @mandant_id as mandant_id,
                   null as traegerschaft_id,
                   'AKTIV' as status,
                   true as event_published
              ) AS tmp
WHERE NOT EXISTS(
    SELECT id FROM institution WHERE id = @unbekannte_kita_id
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
                   @unbekannte_kita_id as id,
                   now() as timestamp_erstellt,
                   now() as timestamp_mutiert,
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
    SELECT id FROM adresse WHERE id = @unbekannte_kita_id
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
                   @unbekannte_kita_id as id,
                   now() as timestamp_erstellt,
                   now() as timestamp_mutiert,
                   'flyway' as user_erstellt,
                   'flyway' as user_mutiert,
                   0 as version,
                   null as vorgaenger_id,
                   '1000-01-01' as gueltig_ab,
                   '9999-12-31' as gueltig_bis,
                   'KITA' as betreuungsangebot_typ,
                   @unbekannte_kita_id as adresse_id,
                   @unbekannte_kita_id as institution_id,
                   null as institution_stammdaten_tagesschule_id,
                   null as institution_stammdaten_ferieninsel_id,
                   'mail@example.com' as mail,
                   null as telefon,
                   null as webseite
              ) AS tmp
WHERE NOT EXISTS(
    SELECT id FROM institution_stammdaten WHERE id = @unbekannte_kita_id
)
LIMIT 1;

# Unbekannte Institutionen (TFO)
#####

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
                   @unbekannte_tfo_id as id,
                   now() as timestamp_erstellt,
                   now() as timestamp_mutiert,
                   'flyway' as user_erstellt,
                   'flyway' as user_mutiert,
                   0 as version,
                   null as vorgaenger_id,
                   'Unbekannte TFO' as name,
                   @mandant_id as mandant_id,
                   null as traegerschaft_id,
                   'AKTIV' as status,
                   true as event_published
              ) AS tmp
WHERE NOT EXISTS(
    SELECT id FROM institution WHERE id = @unbekannte_tfo_id
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
                   @unbekannte_tfo_id as id,
                   now() as timestamp_erstellt,
                   now() as timestamp_mutiert,
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
    SELECT id FROM adresse WHERE id = @unbekannte_tfo_id
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
                   @unbekannte_tfo_id as id,
                   '2022-01-01 00:00:00' as timestamp_erstellt,
                   '2022-01-01 00:00:00' as timestamp_mutiert,
                   'flyway' as user_erstellt,
                   'flyway' as user_mutiert,
                   0 as version,
                   null as vorgaenger_id,
                   '1000-01-01' as gueltig_ab,
                   '9999-12-31' as gueltig_bis,
                   'KITA' as betreuungsangebot_typ,
                   @unbekannte_tfo_id as adresse_id,
                   @unbekannte_tfo_id as institution_id,
                   null as institution_stammdaten_tagesschule_id,
                   null as institution_stammdaten_ferieninsel_id,
                   'mail@example.com' as mail,
                   null as telefon,
                   null as webseite
              ) AS tmp
WHERE NOT EXISTS(
    SELECT id FROM institution_stammdaten WHERE id = @unbekannte_tfo_id
)
LIMIT 1;

/* Technischer User*/
INSERT INTO benutzer
(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
 email, nachname, username, vorname, mandant_id, externaluuid, status)
VALUES (@technical_benutzer_id, NOW(), NOW(), 'flyway', 'flyway', 0,
        null,
        concat('kibon.technical.', @url_code, '@dvbern.ch'),
        'kibon',
        concat('kibon ',@url_code),
        '', @mandant_id, null, 'AKTIV');

INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
                          vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway', 'flyway',
        0, null, '2020-09-01', '9999-12-31', 'SUPER_ADMIN', @technical_benutzer_id, null, null);

INSERT INTO benutzer
(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
 email, nachname, username, vorname, mandant_id, externaluuid, status)
VALUES (@betreuung_mitteilung_user_id, NOW(), NOW(), 'flyway', 'flyway', 0, null,
        concat('betreuungEvent.', @url_code , '@dvbern.ch'),
        'BetreuungsEvent',
        concat('Mutation ', @url_code),
        '', @mandant_id, null, 'AKTIV');

INSERT INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
                          vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id)
VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway', 'flyway', 0, null,
        '2020-09-01', '9999-12-31', 'SUPER_ADMIN', @betreuung_mitteilung_user_id, null, null);

# Mandant aktivieren
UPDATE mandant SET mandant.activated=true where id = @mandant_id;
COMMIT;
