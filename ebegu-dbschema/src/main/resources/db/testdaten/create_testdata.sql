/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

START TRANSACTION;

# Variables definition
SET @bern_mandant_id = UNHEX('E3736EB86EEF40EF9E5296AB48D8F220');
call select_gesuchsperiode('2019-08-01',@bern_mandant_id, @gesuchsperiode_19_20);
call select_gesuchsperiode('2022-08-01',@bern_mandant_id, @gesuchsperiode_22_23);
call select_gesuchsperiode('2023-08-01',@bern_mandant_id, @gesuchsperiode_23_24);
call select_gesuchsperiode('2024-08-01',@bern_mandant_id, @gesuchsperiode_24_25);
call select_gesuchsperiode('2025-08-01',@bern_mandant_id, @gesuchsperiode_25_26);

SET @gemeinde_london = UNHEX(REPLACE('80a8e496-b73c-4a4a-a163-a0b2caf76487', '-', ''));
SET @gemeinde_paris = UNHEX(REPLACE('ea02b313-e7c3-4b26-9ef7-e413f4046db2', '-', ''));

SET @gemeinde_paris_institution = UNHEX(REPLACE(UUID(), '-', ''));
SET @gemeinde_paris_institution_stammdaten_tagesschule = UNHEX(REPLACE(UUID(), '-', ''));
SET @gemeinde_paris_institution_stammdaten = UNHEX(REPLACE(UUID(), '-', ''));

SET @gemeinde_london_institution = UNHEX(REPLACE(UUID(), '-', ''));
SET @gemeinde_london_adresse = UNHEX(REPLACE(UUID(), '-', ''));
SET @gemeinde_london_institution_stammdaten_tagesschule = UNHEX(REPLACE(UUID(), '-', ''));
SET @gemeinde_london_institution_stammdaten = UNHEX(REPLACE(UUID(), '-', ''));
SET @gemeinde_london_eisntellungen_tagesschule = UNHEX(REPLACE(UUID(), '-', ''));
# Application properties
UPDATE application_property SET value = 'true' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id =  @bern_mandant_id;
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id =  @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'FRENCH_ENABLED' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'GERES_ENABLED_FOR_MANDANT' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '2020-04-04' WHERE name = 'SCHNITTSTELLE_STEUERSYSTEME_AKTIV_AB' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'ZUSATZINFORMATIONEN_INSTITUTION' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'STADT_BERN_ASIV_CONFIGURED' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'ALLE_MUTATIONSMELDUNGEN_VERFUEGEN, KIBON_2754, GESUCH_BEENDEN_FAMSIT, ZAHLUNGEN_STATISTIK, BEMERKUNGEN_FALLUEBERGREIFEND, MEHRERE_FACHSTELLENBESTAETIGUNGEN, FACHSTELLEN_UEBERGANGSLOESUNG' WHERE name = 'ACTIVATED_DEMO_FEATURES' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'INSTITUTIONEN_DURCH_GEMEINDEN_EINLADEN' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'ERLAUBEN_INSTITUTIONEN_ZU_WAEHLEN' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'ANGEBOT_TS_ENABLED' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'ANGEBOT_FI_ENABLED' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'ANGEBOT_MITTAGSTISCH_ENABLED' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'ANGEBOT_TFO_ENABLED' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'EVALUATOR_DEBUG_ENABLED' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'yellow' WHERE name = 'BACKGROUND_COLOR' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '60' WHERE name = 'ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '90' WHERE name = 'ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'CHECKBOX_AUSZAHLEN_IN_ZUKUNFT' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'logo-kibon-bern.svg' WHERE name = 'LOGO_FILE_NAME' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '2021-01-01' WHERE name = 'STADT_BERN_ASIV_START_DATUM' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '60' WHERE name = 'ANZAHL_TAGE_BIS_WARNUNG_FREIGABE' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '15' WHERE name = 'ANZAHL_TAGE_BIS_WARNUNG_QUITTUNG' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '#D50025' WHERE name = 'PRIMARY_COLOR' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '#BF0425' WHERE name = 'PRIMARY_COLOR_DARK' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'application/pdf, application/vnd.openxmlformats-officedocument.wordprocessingml.document, image/jpeg, image/png, application/msword, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel, application/vnd.oasis.opendocument.text, image/tiff, text/plain, application/vnd.oasis.opendocument.spreadsheet, text/csv,  application/rtf' WHERE name = 'UPLOAD_FILETYPES_WHITELIST' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '#F0C3CB' WHERE name = 'PRIMARY_COLOR_LIGHT' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'INFOMA_ZAHLUNGEN' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'SCHNITTSTELLE_EVENTS_AKTIVIERT' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'FERIENBETREUUNG_AKTIV' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AKTIV' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '0.2' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_DE' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '1' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_FR' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '100000' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_DE' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = '50000' WHERE name = 'LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_FR' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'GEMEINDE_KENNZAHLEN_AKTIV' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'LASTENAUSGLEICH_AKTIV' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'AUSZAHLUNGEN_AN_ELTERN' AND mandant_id = @bern_mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'ABGELOESTE_VIEW' AND mandant_id = @bern_mandant_id;


# Gesuchsperiode
UPDATE gesuchsperiode SET status = 'INAKTIV' WHERE ID = @gesuchsperiode_19_20;
INSERT IGNORE INTO gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, datum_aktiviert, status, mandant_id) VALUES (@gesuchsperiode_22_23, now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, '2022-08-01', '2023-07-31', '2023-12-07', 'AKTIV', @bern_mandant_id);
INSERT IGNORE INTO gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, datum_aktiviert, status,mandant_id) VALUES (@gesuchsperiode_23_24, now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, '2023-08-01', '2024-07-31', '2023-12-08', 'AKTIV', @bern_mandant_id);
INSERT IGNORE INTO gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, datum_aktiviert, status, mandant_id) VALUES (@gesuchsperiode_24_25, now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, '2024-08-01', '2025-07-31', '2024-01-01', 'AKTIV', @bern_mandant_id);
INSERT IGNORE INTO gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, datum_aktiviert, status, mandant_id) VALUES (@gesuchsperiode_25_26, now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, '2025-08-01', '2026-07-31', '2025-01-01', 'AKTIV', @bern_mandant_id);

UPDATE gesuchsperiode SET status = 'INAKTIV' WHERE id = @gesuchsperiode_22_23;

# Benutzer System erstellen
INSERT IGNORE INTO benutzer (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, nachname, username, vorname, mandant_id, externaluuid, status) VALUES (UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, 'hallo@dvbern.ch', 'System', 'system', '', @bern_mandant_id, null, 'AKTIV');
INSERT IGNORE INTO berechtigung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, role, benutzer_id, institution_id, traegerschaft_id) VALUES (UNHEX(REPLACE('2a7b78ec-4af0-11e9-9a3a-afd41a03c0bb', '-', '')), now(), now(),'flyway', 'flyway', 0, null, '2017-01-01', '9999-12-31', 'SUPER_ADMIN', UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')), null, null);

# Gemeinden Bern und Ostermundigen erstellen, inkl. Adressen und Gemeindestammdaten. Sequenz anpassen
call CreateGemeinde(@gemeinde_london, 'London', @bern_mandant_id, 99999, true, false, false, 'London', '3072', 'Siessplatzweg', 'london@mailbucket.dvbern.ch', 'www.ostermundigen.ch', UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')));
call CreateGemeinde(@gemeinde_paris, 'Paris', @bern_mandant_id, 99998, true, false, false, 'Paris', '3008', 'Effingerstrasse', 'paris@mailbucket.dvbern.ch', 'www.bern.ch', UNHEX(REPLACE('22222222-2222-2222-2222-222222222222', '-', '')));

# Einstellungen 22/23
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, einstellung_key, value, gemeinde_id, @gesuchsperiode_22_23, mandant_id, erklaerung FROM einstellung WHERE gesuchsperiode_id = @gesuchsperiode_19_20;

# Einstellungen
UPDATE einstellung set value = 'ABHAENGING' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ABWESENHEIT_AKTIV' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ANSPRUCH_AB_X_MONATEN' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ANSPRUCH_MONATSWEISE' and gemeinde_id is null;
UPDATE einstellung set value = 'FKJV' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'AUSSERORDENTLICHER_ANSPRUCH_RULE' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'AUSWEIS_NACHWEIS_REQUIRED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'BEGRUENDUNG_MUTATION_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'BESONDERE_BEDUERFNISSE_LUZERN' and gemeinde_id is null;
UPDATE einstellung set value = '12' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'DAUER_BABYTARIF' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'DIPLOMATENSTATUS_DEAKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = 'FKJV' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'EINGEWOEHNUNG_TYP' and gemeinde_id is null;
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ERWERBSPENSUM_ZUSCHLAG' and gemeinde_id is null;
UPDATE einstellung set value = 'BERN' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FACHSTELLEN_TYP' and gemeinde_id is null;
UPDATE einstellung set value = '60' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION' and gemeinde_id is null;
UPDATE einstellung set value = '40' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION' and gemeinde_id is null;
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION' and gemeinde_id is null;
UPDATE einstellung set value = '40' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION' and gemeinde_id is null;
UPDATE einstellung set value = '30' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG' and gemeinde_id is null;
UPDATE einstellung set value = '60' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER' and gemeinde_id is null;
UPDATE einstellung set value = 'BERN_FKJV' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FINANZIELLE_SITUATION_TYP' and gemeinde_id is null;
UPDATE einstellung set value = '80000' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_FAMILIENSITUATION_NEU' and gemeinde_id is null;
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM' and gemeinde_id is null;
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_PAUSCHALE_BEI_ANSPRUCH' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_PAUSCHALE_RUECKWIRKEND' and gemeinde_id is null;
UPDATE einstellung set value = 'KLASSE9' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FKJV_TEXTE' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'FREIGABE_QUITTUNG_EINLESEN_REQUIRED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' and gemeinde_id is null;
UPDATE einstellung set value = 'KINDERGARTEN2' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' and gemeinde_id is null;
UPDATE einstellung set value = '2022-08-01' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_KONTINGENTIERUNG_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = '51000' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '6.00' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT' and gemeinde_id is null;
UPDATE einstellung set value = '70000' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '3.00' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = '0.00' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT' and gemeinde_id is null;
UPDATE einstellung set value = '40' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT' and gemeinde_id is null;
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = '2022-08-01' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id is null;
UPDATE einstellung set value = '2022-08-01' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = '0.00' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA' and gemeinde_id is null;
UPDATE einstellung set value = '0.00' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO' and gemeinde_id is null;
UPDATE einstellung set value = 'VORSCHULALTER' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA' and gemeinde_id is null;
UPDATE einstellung set value = 'VORSCHULALTER' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'GESCHWISTERNBONUS_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'KESB_PLATZIERUNG_DEAKTIVIEREN' and gemeinde_id is null;
UPDATE einstellung set value = 'FKJV' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'KINDERABZUG_TYP' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'KITAPLUS_ZUSCHLAG_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = '10' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'KITA_STUNDEN_PRO_TAG' and gemeinde_id is null;
UPDATE einstellung set value = '10.59' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'LATS_LOHNNORMKOSTEN' and gemeinde_id is null;
UPDATE einstellung set value = '5.30' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'LATS_LOHNNORMKOSTEN_LESS_THAN_50' and gemeinde_id is null;
UPDATE einstellung set value = '2022-09-15' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'LATS_STICHTAG' and gemeinde_id is null;
UPDATE einstellung set value = '160000' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_MASSGEBENDES_EINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '12.40' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG' and gemeinde_id is null;
UPDATE einstellung set value = '6.20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG' and gemeinde_id is null;
UPDATE einstellung set value = '8.50' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '75' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG' and gemeinde_id is null;
UPDATE einstellung set value = '8.50' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '12.75' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '150' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' and gemeinde_id is null;
UPDATE einstellung set value = '8.50' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '100' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' and gemeinde_id is null;
UPDATE einstellung set value = '2' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MINIMALDAUER_KONKUBINAT' and gemeinde_id is null;
UPDATE einstellung set value = '40' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MIN_ERWERBSPENSUM_EINGESCHULT' and gemeinde_id is null;
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MIN_ERWERBSPENSUM_EINGESCHULT' and gemeinde_id is null;
UPDATE einstellung set value = '43000' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MIN_MASSGEBENDES_EINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '0.79' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MIN_TARIF' and gemeinde_id is null;
UPDATE einstellung set value = '0.70' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '7' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_TG' and gemeinde_id is null;
UPDATE einstellung set value = '11' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'OEFFNUNGSSTUNDEN_TFO' and gemeinde_id is null;
UPDATE einstellung set value = '240' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'OEFFNUNGSTAGE_KITA' and gemeinde_id is null;
UPDATE einstellung set value = '240' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'OEFFNUNGSTAGE_TFO' and gemeinde_id is null;
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG' and gemeinde_id is null;
UPDATE einstellung set value = '30' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_MAX_TAGE_ABWESENHEIT' and gemeinde_id is null;
UPDATE einstellung set value = '3800' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3' and gemeinde_id is null;
UPDATE einstellung set value = '6000' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4' and gemeinde_id is null;
UPDATE einstellung set value = '7000' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5' and gemeinde_id is null;
UPDATE einstellung set value = '7700' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_PENSUM_KITA_MIN' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_PENSUM_TAGESELTERN_MIN' and gemeinde_id is null;
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PARAM_PENSUM_TAGESSCHULE_MIN' and gemeinde_id is null;
UPDATE einstellung set value = 'ZEITEINHEIT_UND_PROZENT' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'PENSUM_ANZEIGE_TYP' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'SCHNITTSTELLE_STEUERN_AKTIV' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'SPRACHE_AMTSPRACHE_DISABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'VORSCHULALTER' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'UNBEZAHLTER_URLAUB_AKTIV' and gemeinde_id is null;
UPDATE einstellung set value = '50' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'VERFUEGUNG_EXPORT_ENABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ZEMIS_DISABLED' and gemeinde_id is null;
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ZUSATZLICHE_FELDER_ERSATZEINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = '4.25' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = '50' where gesuchsperiode_id = @gesuchsperiode_22_23 and einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_TG' and gemeinde_id is null;

# Gemeinde Einstellungen müssen inserted werden, da sie in der Periode 19/20 noch nicht gestzt sind und desshalb auch nicht kopiert werden
# Gemeinde Paris
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE', 'KINDERGARTEN2', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB', '2022-05-04', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER', 'false', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_KONTINGENTIERUNG_ENABLED', 'false', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN', '51000', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT', '6.00', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN', '70000', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT', '3.00', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT', '0', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED', 'true', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED', 'false', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT', '2', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT', '5', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT', '5', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB', '2022-05-04', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG', '2022-08-15', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_TAGESSCHULE_TAGIS_ENABLED', 'false', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG', 'false', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED', 'true', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT', '15', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA', '50', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO', '4.94', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED', 'true', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA', '11', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO', '1', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA', 'KINDERGARTEN2', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO', 'KINDERGARTEN2', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED', 'true', @gemeinde_paris, @gesuchsperiode_22_23, @bern_mandant_id, null;

# Gemeinde London (alle Einstellungen von Paris kopieren und dann für London updaten)
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, einstellung_key, value, @gemeinde_london, @gesuchsperiode_22_23, mandant_id, erklaerung FROM einstellung WHERE gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_paris;
UPDATE einstellung set value = '2022-08-01' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB';
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED';
UPDATE einstellung set value = '40' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT';
UPDATE einstellung set value = '20' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT';
UPDATE einstellung set value = '2022-08-01' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB';
UPDATE einstellung set value = '2022-08-01' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG';
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED';
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT';
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA';
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO';
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED';
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA';
UPDATE einstellung set value = '0' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO';
UPDATE einstellung set value = 'VORSCHULALTER' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA';
UPDATE einstellung set value = 'VORSCHULALTER' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO';
UPDATE einstellung set value = 'false' where gesuchsperiode_id = @gesuchsperiode_22_23 and gemeinde_id = @gemeinde_london and einstellung_key = 'GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED';

# Einstellungen Periode 23/24 (Kopieren aus 22/23 und alle Änderungen updaten)
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, einstellung_key, value, gemeinde_id, @gesuchsperiode_23_24, mandant_id, erklaerung FROM einstellung WHERE gesuchsperiode_id = @gesuchsperiode_22_23;
UPDATE einstellung set value = '2023-08-01' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id is null;
UPDATE einstellung set value = '2023-08-01' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id is null;
UPDATE einstellung set value = '2023-08-01' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id is null;
UPDATE einstellung set value = 'FKJV_2' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'KINDERABZUG_TYP' and gemeinde_id is null;
UPDATE einstellung set value = '10.72' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'LATS_LOHNNORMKOSTEN' and gemeinde_id is null;
UPDATE einstellung set value = '5.36' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'LATS_LOHNNORMKOSTEN_LESS_THAN_50' and gemeinde_id is null;
UPDATE einstellung set value = '2023-09-15' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'LATS_STICHTAG' and gemeinde_id is null;
UPDATE einstellung set value = '12.55' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG' and gemeinde_id is null;
UPDATE einstellung set value = '6.27' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG' and gemeinde_id is null;
UPDATE einstellung set value = '0.8' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'MIN_TARIF' and gemeinde_id is null;
UPDATE einstellung set value = '0.7' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_STD' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED' and gemeinde_id is null;

UPDATE einstellung set value = '2023-05-08' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_paris;
UPDATE einstellung set value = '2023-05-08' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_paris;
UPDATE einstellung set value = '2023-08-14' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id = @gemeinde_paris;

UPDATE einstellung set value = '2023-08-01' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_london;
UPDATE einstellung set value = '2023-08-01' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_london;
UPDATE einstellung set value = '2023-08-01' where gesuchsperiode_id = @gesuchsperiode_23_24 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id = @gemeinde_london;

# Einstellungen Periode 24/25 (Kopieren aus 23/24 und alle Änderungen updaten)
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, einstellung_key, value, gemeinde_id, @gesuchsperiode_24_25, mandant_id, erklaerung FROM einstellung WHERE gesuchsperiode_id = @gesuchsperiode_23_24;
UPDATE einstellung set value = '2024-08-01' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id is null;
UPDATE einstellung set value = '2024-08-01' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id is null;
UPDATE einstellung set value = '2024-08-01' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'ZUSATZLICHE_FELDER_ERSATZEINKOMMEN' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'SPRACHFOERDERUNG_BESTAETIGEN' and gemeinde_id is null;
UPDATE einstellung set value = 'true' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GESUCH_BEENDEN_BEI_TAUSCH_GS2' and gemeinde_id is null;

UPDATE einstellung set value = '2024-05-08' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_paris;
UPDATE einstellung set value = '2024-05-08' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_paris;
UPDATE einstellung set value = '2024-08-14' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id = @gemeinde_paris;

UPDATE einstellung set value = '2024-08-01' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_london;
UPDATE einstellung set value = '2024-08-01' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' and gemeinde_id = @gemeinde_london;
UPDATE einstellung set value = '2024-08-01' where gesuchsperiode_id = @gesuchsperiode_24_25 and einstellung_key = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' and gemeinde_id = @gemeinde_london;

# Einstellungen Periode 25/26 (Kopieren aus 24/25 und alle Änderungen updaten)
INSERT IGNORE INTO einstellung(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UUID(), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, einstellung_key, value, gemeinde_id, @gesuchsperiode_25_26, mandant_id, erklaerung FROM einstellung WHERE gesuchsperiode_id = @gesuchsperiode_24_25;

# Test-Institutionen erstellen
INSERT IGNORE INTO traegerschaft (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name, active) VALUES (UNHEX(REPLACE('f9ddee82-81a1-4cda-b273-fb24e9299308', '-', '')), '2016-01-01 00:00:00', '2016-01-01 00:00:00', 'flyway', 'flyway', 0, 'Kitas & Tagis Stadt Bern', true);

# Kita und Tagesfamilien
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('1b6f476f-e0f5-4380-9ef6-836d688853a3', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, 'Brünnen', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), UNHEX(REPLACE('f9ddee82-81a1-4cda-b273-fb24e9299308', '-', '')), 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('3559c33b-1ca1-414d-b227-06affafa0dcd', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, 'Tageseltern Bern', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), UNHEX(REPLACE('f9ddee82-81a1-4cda-b273-fb24e9299308', '-', '')), 'AKTIV', false);
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('ab353df1-47ca-4618-b849-2265cf1c356a', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, 'Weissenstein', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')), UNHEX(REPLACE('f9ddee82-81a1-4cda-b273-fb24e9299308', '-', '')), 'AKTIV', false);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('bc0cbf67-4a68-4e0e-8107-9316ee3f00a3', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '4', 'CH', 'Tageseltern Bern', 'Bern', '3005', 'Gasstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('9d743bc2-8731-47ff-a979-d4bb1d4203c0', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '5', 'CH', 'Weissenstein', 'Bern', '3007', 'Weberstrasse', null);
INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('68992b60-8a1a-415c-a43d-c8c349b73ff8', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '27', 'CH', 'Brünnen', 'Bern', '3027', 'Colombstrasse', null);

INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id) VALUES (UNHEX(REPLACE('37405368-c5b7-4eaf-9a19-536175d3f8fa', '-', '')), now(), now(),  'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Bruennen', null);
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id) VALUES (UNHEX(REPLACE('1b8d2a38-df6b-4a20-9647-aa8b6e6df5a4', '-', '')), now(), now(),  'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Weissenstein', null);
INSERT IGNORE INTO auszahlungsdaten(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban, kontoinhaber, adresse_kontoinhaber_id) VALUES (UNHEX(REPLACE('b4462023-b29c-45cd-921f-0f8a228274c2', '-', '')), now(), now(),  'flyway', 'flyway', 0, 'CH82 0900 0000 1001 5000 6', 'Kontoinhaber Tageseltern Bern', null);

INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, auszahlungsdaten_id, alterskategorie_baby, alterskategorie_vorschule, alterskategorie_kindergarten, alterskategorie_schule, anzahl_plaetze, anzahl_plaetze_firmen, offen_von, offen_bis, oeffnungstage_pro_jahr, anzahl_kinder_warteliste, summe_pensum_warteliste, dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung, wochenende_eroeffnung, uebernachtung_moeglich) VALUES (UNHEX(REPLACE('246b5afc-e3f6-41a6-8a98-cd44310678da', '-', '')), now(), now(), 'flyway', 'flyway', 0, UNHEX(REPLACE('b4462023-b29c-45cd-921f-0f8a228274c2', '-', '')), false, false, false, false, 30, null, '08:00', '18:00', 0,  0.00, 0.00, 0.00, false, false, false, false);
INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, auszahlungsdaten_id, alterskategorie_baby, alterskategorie_vorschule, alterskategorie_kindergarten, alterskategorie_schule, anzahl_plaetze, anzahl_plaetze_firmen, offen_von, offen_bis, oeffnungstage_pro_jahr, anzahl_kinder_warteliste, summe_pensum_warteliste, dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung, wochenende_eroeffnung, uebernachtung_moeglich) VALUES (UNHEX(REPLACE('396a5a9c-7da6-4c25-8e61-34aefdbe722b', '-', '')), now(), now(), 'flyway', 'flyway', 0, UNHEX(REPLACE('1b8d2a38-df6b-4a20-9647-aa8b6e6df5a4', '-', '')), false, false, false, false, 35, null, '08:00', '18:00', 0,  0.00, 0.00, 0.00, false, false, false, false);
INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, auszahlungsdaten_id, alterskategorie_baby, alterskategorie_vorschule, alterskategorie_kindergarten, alterskategorie_schule, anzahl_plaetze, anzahl_plaetze_firmen, offen_von, offen_bis, oeffnungstage_pro_jahr, anzahl_kinder_warteliste, summe_pensum_warteliste, dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung, wochenende_eroeffnung, uebernachtung_moeglich) VALUES (UNHEX(REPLACE('e619ad30-a58a-4b40-aa72-25063145f16b', '-', '')), now(), now(), 'flyway', 'flyway', 0, UNHEX(REPLACE('37405368-c5b7-4eaf-9a19-536175d3f8fa', '-', '')), false, false, false, false, 40, null, '08:00', '18:00', 0,  0.00, 0.00, 0.00, false, false, false, false);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite) VALUES (UNHEX(REPLACE('6b7beb6e-6cf3-49d6-84c0-5818d9215ecd', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-08-01', '9999-12-31', 'TAGESFAMILIEN', UNHEX(REPLACE('bc0cbf67-4a68-4e0e-8107-9316ee3f00a3', '-', '')), UNHEX(REPLACE('3559c33b-1ca1-414d-b227-06affafa0dcd', '-', '')),  null, null, UNHEX(REPLACE('246b5afc-e3f6-41a6-8a98-cd44310678da', '-', '')), 'tagesfamilien@mailbucket.dvbern.ch', null, null);
INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite) VALUES (UNHEX(REPLACE('945e3eef-8f43-43d2-a684-4aa61089684b', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-08-01', '9999-12-31', 'KITA', UNHEX(REPLACE('9d743bc2-8731-47ff-a979-d4bb1d4203c0', '-', '')), UNHEX(REPLACE('ab353df1-47ca-4618-b849-2265cf1c356a', '-', '')), null, null, UNHEX(REPLACE('396a5a9c-7da6-4c25-8e61-34aefdbe722b', '-', '')), 'weissenstein@mailbucket.dvbern.ch', null, null);
INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite) VALUES (UNHEX(REPLACE('9a0eb656-b6b7-4613-8f55-4e0e4720455e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-08-01', '9999-12-31', 'KITA', UNHEX(REPLACE('68992b60-8a1a-415c-a43d-c8c349b73ff8', '-', '')), UNHEX(REPLACE('1b6f476f-e0f5-4380-9ef6-836d688853a3', '-', '')), null,  null, UNHEX(REPLACE('e619ad30-a58a-4b40-aa72-25063145f16b', '-', '')), 'bruennen@mailbucket.dvbern.ch', null, null);

INSERT IGNORE INTO kitax_uebergangsloesung_institution_oeffnungszeiten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name_kibon, name_kitax, oeffnungsstunden, oeffnungstage) VALUES (UNHEX(REPLACE('c93fbba5-91e2-4fac-88a3-a2dc8386d62d', '-', '')), now(), now(), 'flyway', 'flyway', 0, ' Brünnen', 'Brünnen', 11.50, 240.00);
INSERT IGNORE INTO kitax_uebergangsloesung_institution_oeffnungszeiten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name_kibon, name_kitax, oeffnungsstunden, oeffnungstage) VALUES (UNHEX(REPLACE('9a9cc8a2-32b9-4ad0-8f41-ed503f886100', '-', '')), now(), now(), 'flyway', 'flyway', 0, ' Weissenstein', 'Weissenstein', 11.50, 240.00);

# Tagesschule
INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published) VALUES (UNHEX(REPLACE('f7abc530-5d1d-4f1c-a198-9039232974a0', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, 'Tagesschule', @bern_mandant_id, null, 'AKTIV', false);

INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile) VALUES (UNHEX(REPLACE('febf3cd1-4bd9-40eb-b65f-fd9b823b1270', '-', '')),now(), now(), 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', null, '21', 'CH', 'Tagesschule', 'Bern', '3008', 'Effingerstrasse', null);

INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id, institution_id, institution_stammdaten_tagesschule_id, institution_stammdaten_ferieninsel_id, institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite) VALUES (UNHEX(REPLACE('199ac4a1-448f-4d4c-b3a6-5aee21f89613', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '1000-01-01', '9999-12-31', 'TAGESSCHULE', UNHEX(REPLACE('febf3cd1-4bd9-40eb-b65f-fd9b823b1270', '-', '')), UNHEX(REPLACE('f7abc530-5d1d-4f1c-a198-9039232974a0', '-', '')), null, null, null, 'tagesschule@mailbucket.dvbern.ch', null, null);

update gemeinde set angebotts = true, angebotfi = true, angebotbgtfo = true where bfs_nummer in (99999, 99998);


-- Tagesschule Gemeinde Paris
call CreateInstitution(@gemeinde_paris_institution, @gemeinde_paris_institution_stammdaten, 'Tagesschule Paris', @bern_mandant_id, null, 'test@mailbucket.dvbern.ch', 'Paris', 3000, 'Pariser Strasse', 'TAGESSCHULE');

call CreateTagesschule(@gemeinde_paris, @bern_mandant_id, @gemeinde_paris_institution_stammdaten_tagesschule, @gemeinde_paris_institution_stammdaten);

call CreateTagesschuleModule(@gesuchsperiode_22_23, @gemeinde_paris_institution_stammdaten_tagesschule, 'coY1r4cqrK1Aq1HIcgvHwGe9pJnGzffZoZO0', '9vifvte1lth8qn9d3rx1bk6rdjq0ye1udq99');
call CreateTagesschuleModule(@gesuchsperiode_23_24, @gemeinde_paris_institution_stammdaten_tagesschule, 'F8vXs39fkEuXdvi3kjLv5DZn5m4fcVpm27tw', 'Hxttr08V9uANnNSmNnqVXZwcXjg8qWdoSnoK');
call CreateTagesschuleModule(@gesuchsperiode_24_25, @gemeinde_paris_institution_stammdaten_tagesschule, 'C8vXs39fkEuXdvi3kjLv5DZn5m4fcVpm36ww', 'Ereer08V9uANnNSmNnqVXZwcXjg8qWdoKnos');
call CreateTagesschuleModule(@gesuchsperiode_25_26, @gemeinde_paris_institution_stammdaten_tagesschule, 'U6jLr48opLaMdvi3kjLv5DZn5m4fcGrq55bt', 'Spaen41C5uANnNSmNnqVXZwcXjg7ugdhLrtx');


-- Tagesschule Gemeinde London
call CreateInstitution(@gemeinde_london_institution, @gemeinde_london_institution_stammdaten, 'Tageschule London', @bern_mandant_id, null, 'london@mailbucket.dvbern.ch', 'London', 3072, 'Londoner Strasse', 'TAGESSCHULE');

call CreateTagesschule(@gemeinde_london, @bern_mandant_id, @gemeinde_london_institution_stammdaten_tagesschule, @gemeinde_london_institution_stammdaten);

call CreateTagesschuleModule(@gesuchsperiode_22_23, @gemeinde_london_institution_stammdaten_tagesschule, 'Hxttr08V9uANA3dVeaVXZwcXjg8qWd8oSnoK', 'ln9u5opdowot1n9b3zwaepr26gz7865mg9yx');
call CreateTagesschuleModule(@gesuchsperiode_23_24, @gemeinde_london_institution_stammdaten_tagesschule, '9ciaga64xwjq9dbhn4s42oegq7he7y40dk91', 'qdmblj767bcrdxspmxm1kuqobt7v5d5jmvbk');
call CreateTagesschuleModule(@gesuchsperiode_24_25, @gemeinde_london_institution_stammdaten_tagesschule, 'r1m0fq9rk3fsi8uudb3chf4d86qek84im2gi', 'qts3o3mpbyv4fbosunqxsjdzsbs6pv0gh71s');
call CreateTagesschuleModule(@gesuchsperiode_25_26, @gemeinde_london_institution_stammdaten_tagesschule, 'p5c0fq9rk3fsi8uudb3chf4d86qep54mv8ha', 'brp2mpbyv4fbosunqxsjdzsbs6pv3ue64bfs');

UPDATE external_client SET `institution_type` = 'EXCHANGE_SERVICE_INSTITUTION' WHERE external_client.`client_name` = 'kitAdmin';

INSERT IGNORE INTO gemeinde_stammdaten_gesuchsperiode (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, gemeinde_id, gesuchsperiode_id, merkblatt_anmeldung_tagesschule_de, merkblatt_anmeldung_tagesschule_fr) VALUES
# PARIS
(UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, @gemeinde_paris, UNHEX(REPLACE('0621fb5d-a187-5a91-abaf-8a813c4d263a', '-', '')), null, null),
# LONDON
(UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, @gemeinde_london, UNHEX(REPLACE('0621fb5d-a187-5a91-abaf-8a813c4d263a', '-', '')), null, null);

# PARIS
INSERT IGNORE INTO gemeinde_stammdaten_gesuchsperiode_ferieninsel (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, anmeldeschluss, ferienname, gemeinde_stammdaten_gesuchsperiode_id) VALUES
(UNHEX(REPLACE('54086b1a-6901-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-09-01', 'HERBSTFERIEN', UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('4ea68aa1-6901-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-06-01', 'SOMMERFERIEN', UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('9c19b314-6900-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-01-01', 'SPORTFERIEN', UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('36665051-6901-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-04-01', 'FRUEHLINGSFERIEN', UNHEX(REPLACE('b69c7aba-6904-11ea-bbf8-f4390979fa3e', '-', '')));
# LONDON
INSERT IGNORE INTO gemeinde_stammdaten_gesuchsperiode_ferieninsel (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, anmeldeschluss, ferienname, gemeinde_stammdaten_gesuchsperiode_id) VALUES
(UNHEX(REPLACE('a3e774d0-6903-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-09-01', 'HERBSTFERIEN', UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('9ea7ae08-6903-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-06-01', 'SOMMERFERIEN', UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('90cb89be-6903-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-01-01', 'SPORTFERIEN', UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', ''))),
(UNHEX(REPLACE('9989a3f8-6903-11ea-bbf8-f4390979fa3e', '-', '')), now(), now(), 'flyway', 'flyway', 0, null, '2019-04-01', 'FRUEHLINGSFERIEN', UNHEX(REPLACE('cd28e254-6904-11ea-bbf8-f4390979fa3e', '-', '')));

-- Sozialdienst
INSERT IGNORE INTO sozialdienst (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,vorgaenger_id,name,status,mandant_id) VALUES (UNHEX(REPLACE('f44a68f2-dda2-4bf2-936a-68e20264b620', '-', '')),now(), now(),'flyway','flyway',0,null,'BernerSozialdienst','AKTIV', @bern_mandant_id);
INSERT IGNORE INTO adresse (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,vorgaenger_id,gueltig_ab,gueltig_bis,gemeinde,hausnummer,land,organisation,ort,plz,strasse,zusatzzeile) VALUES (UNHEX(REPLACE('a805a101-4200-473a-accc-bbb423ea1999', '-', '')),now(), now(),'flyway','flyway',0,null,'1000-01-01','9999-12-31',null,'2','CH','Bern Sozialdienst','Paris','3000','Sozialdienst Strasse',null);
INSERT IGNORE INTO sozialdienst_stammdaten (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,vorgaenger_id,mail,telefon,webseite,adresse_id,sozialdienst_id) VALUES (UNHEX(REPLACE('0f1c6b9e-37de-4c10-8ddc-9514fb840f5f', '-', '')),now(), now(),'flyway','flyway',0,null,'test@mailbucket.dvbern.ch','078 898 98 98','www.test.dvbern.ch',UNHEX(REPLACE('a805a101-4200-473a-accc-bbb423ea1999', '-', '')),UNHEX(REPLACE('f44a68f2-dda2-4bf2-936a-68e20264b620', '-', '')));

COMMIT;
