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
SET @gesuchsperiode_id = UNHEX(REPLACE(UUID(), '-', ''));

SET @mandant_id = UNHEX(REPLACE('76783c4a-def2-4d0c-9e0f-209a7b190d15', '-', ''));
SET @mandant_name = 'DVB';
SET @start_datum_erste_periode = '2025-08-01';
SET @ende_datum_erste_periode = '2026-07-31';
SET @mandant_identifier = 'DVB';
SET @urlCode = 'dv';


SET @mandant_id_schwyz = UNHEX(REPLACE('08687de9-b3d0-11ee-829a-0242ac160002', '-', ''));
SET @aktuelle_gp_schwyz := CAST((SELECT gesuchsperiode.id
                            FROM gesuchsperiode
                            WHERE mandant_id = @mandant_id_schwyz AND gueltig_ab = '2024-08-01') AS BINARY(16));

INSERT INTO mandant
VALUES (@mandant_id, now(), now(), 'flyway', 'flyway', 0, NULL, @mandant_name, @mandant_identifier,
        false,
        1, 1);

# APPLICATION PROPERTIES
INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
                                  version, vorgaenger_id, name, value, mandant_id)
SELECT UNHEX(REPLACE(UUID(), '-', '')), timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
       NULL, name, value, @mandant_id
FROM application_property
WHERE mandant_id = @mandant_id_schwyz AND
    NOT EXISTS(SELECT name
               FROM application_property a_p
               WHERE mandant_id = @mandant_id AND
                   a_p.name = application_property.name);

# APPLICATION PROPERTIES SETZEN FÜR DVB-Mandant
UPDATE application_property SET value = '' WHERE mandant_id = @mandant_id AND name = 'ACTIVATED_DEMO_FEATURES';
UPDATE application_property SET value = 'false' WHERE mandant_id = @mandant_id AND name = 'ANGEBOT_TS_ENABLED';
UPDATE application_property SET value = 'true' WHERE mandant_id = @mandant_id AND name = 'ANGEBOT_TFO_ENABLED';
UPDATE application_property SET value = 'false' WHERE mandant_id = @mandant_id AND name = 'ANGEBOT_MITTAGSTISCH_ENABLED';
UPDATE application_property SET value = 'false' WHERE mandant_id = @mandant_id AND name = 'ANGEBOT_FI_ENABLED';
UPDATE application_property SET value = '60' WHERE mandant_id = @mandant_id AND name = 'ANZAHL_TAGE_BIS_WARNUNG_FREIGABE';
UPDATE application_property SET value = '90' WHERE mandant_id = @mandant_id AND name = 'ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE';
UPDATE application_property SET value = 'false' WHERE mandant_id = @mandant_id AND name = 'ERLAUBEN_INSTITUTIONEN_ZU_WAEHLEN';
UPDATE application_property SET value = 'false' WHERE mandant_id = @mandant_id AND name = 'GERES_ENABLED_FOR_MANDANT';
UPDATE application_property SET value = '#D50025' WHERE name = 'PRIMARY_COLOR' AND mandant_id = @mandant_id;
UPDATE application_property SET value = '#BF0425' WHERE name = 'PRIMARY_COLOR_DARK' AND mandant_id = @mandant_id;
UPDATE application_property SET value = '#F0C3CB' WHERE name = 'PRIMARY_COLOR_LIGHT' AND mandant_id = @mandant_id;
UPDATE application_property SET value = 'logo-kibon-default.svg' WHERE name = 'LOGO_FILE_NAME' AND mandant_id = @mandant_id;
UPDATE application_property SET value = 'logo-kibon-white-default.svg' WHERE name = 'LOGO_WHITE_FILE_NAME' AND mandant_id = @mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'DUMMY_LOGIN_ENABLED' AND mandant_id = @mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'CHECKBOX_AUSZAHLEN_IN_ZUKUNFT' AND mandant_id = @mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'INSTITUTIONEN_DURCH_GEMEINDEN_EINLADEN' AND mandant_id = @mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'GEMEINDE_VEREINFACHTE_KONFIG_AKTIV' AND mandant_id = @mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'SCHNITTSTELLE_EVENTS_AKTIVIERT' AND mandant_id = @mandant_id;
UPDATE application_property SET value = '9999-12-31' WHERE name = 'SCHNITTSTELLE_STEUERSYSTEME_AKTIV_AB' AND mandant_id = @mandant_id;
UPDATE application_property SET value = 'false' WHERE name = 'FRENCH_ENABLED' AND mandant_id = @mandant_id;
UPDATE application_property SET value = 'true' WHERE name = 'AUSZAHLUNGEN_AN_ELTERN' AND mandant_id = @mandant_id;

INSERT INTO gesuchsperiode
VALUES (@gesuchsperiode_id, NOW(), NOW(), 'flyway', 'flyway', 0,
        NULL, @start_datum_erste_periode, @ende_datum_erste_periode, NULL,
        'ENTWURF', @mandant_id, NULL, NULL,
        NULL, NULL,
        NULL, NULL,
        NULL,NULL);

# Einstellungen für Gesuchsperiode kopieren
INSERT INTO einstellung
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'flyway',
       'flyway', 0, einstellung_key, value, NULL,
       @gesuchsperiode_id, NULL, erklaerung
FROM einstellung
WHERE gesuchsperiode_id = @aktuelle_gp_schwyz AND gemeinde_id IS NULL AND einstellung.mandant_id is NULL;

# Gemeinde Einstellungen für Gesuchsperiode kopieren
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
                         einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id, erklaerung)
SELECT UNHEX(REPLACE(UUID(), '-', '')), timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, 0,
       einstellung_key, value, NULL, @gesuchsperiode_id, @mandant_id, erklaerung
FROM einstellung
WHERE mandant_id = @mandant_id_schwyz AND gesuchsperiode_id = @aktuelle_gp_schwyz AND NOT EXISTS(
    SELECT einstellung_key FROM einstellung e1 WHERE e1.gesuchsperiode_id =  @gesuchsperiode_id
                                                 and e1.mandant_id = @mandant_id AND e1.einstellung_key = einstellung.einstellung_key
) AND gemeinde_id IS NULL;

INSERT INTO sequence(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, sequence_type, current_value, mandant_id)
VALUES (
           UNHEX(REPLACE(UUID(), '-', '')), # id
           now(), # timestamp_erstellt
           now(), # timestamp_mutiert
           'flyway', # user_erstellt
           'flyway', # user_mutiert
           0, # version
           'FALL_NUMMER', # sequence_type
           100, # current_value
           @mandant_id);

/* Perioden Einstellungen */
UPDATE einstellung SET value = 'true' WHERE einstellung_key = 'ABWEICHUNGEN_ENABLED' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'ABHAENGING' where einstellung_key = 'ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'true' where einstellung_key = 'GESUCH_BEENDEN_BEI_TAUSCH_GS2' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'ANWESENHEITSTAGE_PRO_MONAT_AKTIVIERT' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'KINDERGARTEN2' where einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'KITA, TAGESFAMILIEN' where einstellung_key = 'ANGEBOT_SCHULSTUFE' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '18' where einstellung_key = 'DAUER_BABYTARIF' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'ERWEITERTE_BEDUERFNISSE_AKTIV' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'KEINE' where einstellung_key = 'FACHSTELLEN_TYP' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '99999999' where einstellung_key = 'FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '100' where einstellung_key = 'FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '100' where einstellung_key = 'FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'FKJV_FAMILIENSITUATION_NEU' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'FKJV_PAUSCHALE_RUECKWIRKEND' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'FKJV_PAUSCHALE_BEI_ANSPRUCH' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'VORSCHULALTER' where einstellung_key = 'FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'FKJV_TEXTE' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'NONE' where einstellung_key = 'GESCHWISTERNBONUS_TYP' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'AUSWEIS_NACHWEIS_REQUIRED' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'true' where einstellung_key = 'KESB_PLATZIERUNG_DEAKTIVIEREN' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'KEINE' where einstellung_key = 'KINDERABZUG_TYP' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '10' where einstellung_key = 'KITA_STUNDEN_PRO_TAG' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'GEMEINDE_KONTINGENTIERUNG_ENABLED' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'BESONDERE_BEDUERFNISSE_LUZERN' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'true' where einstellung_key = 'DIPLOMATENSTATUS_DEAKTIVIERT' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'true' where einstellung_key = 'SPRACHE_AMTSPRACHE_DISABLED' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'true' where einstellung_key = 'ZEMIS_DISABLED' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'PARAM_MAX_TAGE_ABWESENHEIT' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '160000' where einstellung_key = 'MAX_MASSGEBENDES_EINKOMMEN' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'PARAM_PENSUM_KITA_MIN' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'PARAM_PENSUM_TAGESELTERN_MIN' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'PARAM_PENSUM_TAGESSCHULE_MIN' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '48000' where einstellung_key = 'MIN_MASSGEBENDES_EINKOMMEN' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '2' where einstellung_key = 'MINIMALDAUER_KONKUBINAT' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '20' where einstellung_key = 'PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'MIN_TARIF' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '20' where einstellung_key = 'MIN_ERWERBSPENSUM_EINGESCHULT' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '20' where einstellung_key = 'MIN_ERWERBSPENSUM_NICHT_EINGESCHULT' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'BEGRUENDUNG_MUTATION_AKTIVIERT' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'LATS_LOHNNORMKOSTEN_LESS_THAN_50' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'LATS_LOHNNORMKOSTEN' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '10' where einstellung_key = 'OEFFNUNGSSTUNDEN_TFO' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '240' where einstellung_key = 'OEFFNUNGSTAGE_KITA' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '240' where einstellung_key = 'OEFFNUNGSTAGE_TFO' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'true' where einstellung_key = 'GESUCHFREIGABE_ONLINE' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'ZEITEINHEIT_UND_PROZENT' where einstellung_key = 'PENSUM_ANZEIGE_TYP' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'SCHNITTSTELLE_STEUERN_AKTIV' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'SCHULERGAENZENDE_BETREUUNGEN' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'FREIGABE_QUITTUNG_EINLESEN_REQUIRED' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'KLASSE9' where einstellung_key = 'SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'KEINE' where einstellung_key = 'EINGEWOEHNUNG_TYP' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'SOLOTHURN' where einstellung_key = 'FINANZIELLE_SITUATION_TYP' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'UNBEZAHLTER_URLAUB_AKTIV' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'VERFUEGUNG_EXPORT_ENABLED' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'KEINE' where einstellung_key = 'AUSSERORDENTLICHER_ANSPRUCH_RULE' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'ABWESENHEIT_AKTIV' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'SPRACHFOERDERUNG_BESTAETIGEN' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_STD' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_TG' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'ERWERBSPENSUM_ZUSCHLAG' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'SOZIALVERSICHERUNGSNUMMER_PERIODE' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = 'false' where einstellung_key = 'WEGZEIT_ERWERBSPENSUM' and gesuchsperiode_id = @gesuchsperiode_id;

-- Rechner Einstellungen analog zu Bern
UPDATE einstellung set value = '0.7' where einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_STD' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '7' where einstellung_key = 'MIN_VERGUENSTIGUNG_PRO_TG' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '8.50' where einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '75' where einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '8.50' where einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '100' where einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '12.75' where einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '150' where einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' and gesuchsperiode_id = @gesuchsperiode_id;
UPDATE einstellung set value = '0' where einstellung_key = 'MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD' and gesuchsperiode_id = @gesuchsperiode_id; -- wird nicht gebraucht, da vergünstigung standardmässig nur bis Kindergarten2
