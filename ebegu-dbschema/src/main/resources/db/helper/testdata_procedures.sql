DELIMITER $$

CREATE OR REPLACE PROCEDURE InsertUser(
	IN p_username   VARCHAR(255),
	IN p_email      VARCHAR(255),
	IN p_nachname   VARCHAR(255),
	IN p_vorname    VARCHAR(255),
	IN p_mandant_id BINARY(16)
)
BEGIN
	DECLARE benutzer_uuid BINARY(16);
	DECLARE berechtigung_uuid BINARY(16);
	DECLARE berechtigung_history_uuid BINARY(16);

	SET benutzer_uuid = UNHEX(REPLACE(UUID(), '-', ''));
	SET berechtigung_uuid = UNHEX(REPLACE(UUID(), '-', ''));
	SET berechtigung_history_uuid = UNHEX(REPLACE(UUID(), '-', ''));

	INSERT IGNORE INTO benutzer
	(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, email, externaluuid,
	 nachname, status, username, vorname, mandant_id, bemerkungen, zpv_nummer)
	VALUES (benutzer_uuid, NOW(), NOW(), 'anonymous', 'anonymous', 0, null, p_email, null, p_nachname,
			'AKTIV', p_username, p_vorname, p_mandant_id, null, null);

	INSERT IGNORE INTO berechtigung
	(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis,
	 role, benutzer_id, institution_id, traegerschaft_id, sozialdienst_id)
	VALUES (berechtigung_uuid, NOW(), NOW(), 'anonymous', 'anonymous', 0, null, '2020-01-09',
			'9999-12-31', 'GESUCHSTELLER', benutzer_uuid, null, null, null);

	INSERT IGNORE INTO berechtigung_history
	(id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, gueltig_ab, gueltig_bis,
	 geloescht, gemeinden, role, status, username, institution_id, traegerschaft_id, sozialdienst_id)
	VALUES (berechtigung_history_uuid, NOW(), NOW(), 'anonymous', 'anonymous', 0, null, '2020-01-09',
			'9999-12-31', false, '', 'GESUCHSTELLER', 'AKTIV', p_username, null, null, null);

END
$$

-- funktion speichert die gesuchsperiode id für eine gesuchsperiode gültig ab (input) in der übergebenen variable gp_id.
-- falls keine periode mit dem übergebenen gültig_ab datum existeirt wird eine neue uuid in die variable gespeichert
CREATE OR REPLACE PROCEDURE select_gesuchsperiode(IN gueltig_ab_input DATE, IN mandant_id_input BINARY(16), OUT gp_id binary(16))
BEGIN
	IF EXISTS(SELECT id FROM gesuchsperiode WHERE mandant_id = mandant_id_input AND gueltig_ab = gueltig_ab_input)
	THEN
		SET gp_id = (SELECT id from gesuchsperiode WHERE mandant_id = mandant_id_input AND gueltig_ab = gueltig_ab_input);
	ELSE
		SET gp_id = UNHEX(REPLACE(UUID(), '-', ''));
	END IF;
END;
$$

CREATE OR REPLACE PROCEDURE CreateGemeinde(
	IN p_gemeinde_id   BINARY(16),
	IN p_gemeinde_name VARCHAR(255),
	IN p_mandant_id    BINARY(16),
	IN p_bfs_nummer    INT,
	IN p_angebotbg     BOOLEAN,
	IN p_angebotts     BOOLEAN,
	IN p_angebotfi     BOOLEAN,
	IN p_ort           VARCHAR(255),
	IN p_plz           VARCHAR(10),
	IN p_strasse       VARCHAR(255),
	IN p_mail          VARCHAR(255),
	IN p_webseite      VARCHAR(255),
	IN p_sys_user_id   BINARY(16)
)
BEGIN
	DECLARE v_adresse_id BINARY(16);
	DECLARE v_korrespondenz_id BINARY(16);

	-- Generate UUIDs for Gemeinde, Adresse, and Stammdaten Korrespondenz
	SET v_adresse_id = UNHEX(REPLACE(UUID(), '-', ''));
	SET v_korrespondenz_id = UNHEX(REPLACE(UUID(), '-', ''));

	-- Insert into gemeinde table
	INSERT IGNORE INTO gemeinde (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, name,
								 gemeinde_nummer, mandant_id, status, bfs_nummer,
								 betreuungsgutscheine_startdatum, tagesschulanmeldungen_startdatum,
								 ferieninselanmeldungen_startdatum, angebotbg,
								 angebotts, angebotfi, gueltig_bis, besondere_volksschule, nur_lats, event_published,
								 angebotbgtfo)
	SELECT p_gemeinde_id, NOW(), NOW(), 'system', 'system', 0,
		   p_gemeinde_name, COALESCE(MAX(gemeinde_nummer), 0) +1, p_mandant_id, 'AKTIV', p_bfs_nummer,
		   '2020-01-01', '2020-01-01', '2020-01-01',
		   p_angebotbg, p_angebotts, p_angebotfi, '9999-12-31', FALSE, FALSE, FALSE, FALSE
	FROM gemeinde;

	-- Insert into adresse table
	INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
								gueltig_ab, gueltig_bis, gemeinde,
								hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (v_adresse_id, NOW(), NOW(), 'system', 'system', 0, NULL, NOW(), '9999-01-01',
			p_gemeinde_name,
			1, 'CH', 'Gemeinde', p_ort, p_plz, p_strasse, NULL);

	-- Insert into gemeinde_stammdaten_korrespondenz table
	INSERT IGNORE INTO gemeinde_stammdaten_korrespondenz (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
														  version, logo_content, logo_name, logo_spacing_left, logo_spacing_top,
														  logo_type, logo_width, receiver_address_spacing_left,
														  receiver_address_spacing_top, sender_address_spacing_left,
														  sender_address_spacing_top)
	VALUES (v_korrespondenz_id, NOW(), NOW(), 'system', 'system', 0, null, null, 123, 15, null, null,
			123, 47, 20, 47);

	-- Insert into gemeinde_stammdaten table
	INSERT IGNORE INTO gemeinde_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
											default_benutzer_id, default_benutzerts_id,
											gemeinde_id, adresse_id, mail, telefon, webseite, beschwerde_adresse_id,
											korrespondenzsprache,
											bic, iban, kontoinhaber, standard_rechtsmittelbelehrung,
											benachrichtigung_bg_email_auto,
											benachrichtigung_ts_email_auto, standard_dok_signature,
											ts_verantwortlicher_nach_verfuegung_benachrichtigen,
											gemeinde_stammdaten_korrespondenz_id)
	VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, p_sys_user_id, p_sys_user_id,
			p_gemeinde_id, v_adresse_id, p_mail, '079 999 99 99', p_webseite,
			null,
			'DE',
			'AAAABBCC333', 'CH2089144969768441935', CONCAT('Kontoinhaber ', p_gemeinde_name), true, true, true, true, false,
			v_korrespondenz_id);

END
$$

CREATE OR REPLACE PROCEDURE CreateInstitution(
	IN p_institution_id            BINARY(16),
	IN p_institution_stammdaten_id BINARY(16),
	IN p_institution_name          VARCHAR(255),
	IN p_mandant_id                BINARY(16),
	IN p_traegerschaft_id          BINARY(16),
	IN p_mail                      VARCHAR(255),
	IN p_ort                       VARCHAR(255),
	IN p_plz                       VARCHAR(255),
	IN p_strasse                   VARCHAR(255),
	IN p_betreuungsangebots_typ	   VARCHAR(255)
)
BEGIN
	DECLARE adresse_id BINARY(16);
	DECLARE auszahlungsdaten_id BINARY(16);
	DECLARE betreuungsgutscheine_id BINARY(16);

	SET adresse_id = UNHEX(REPLACE(UUID(), '-', ''));
	SET auszahlungsdaten_id = UNHEX(REPLACE(UUID(), '-', ''));
	SET betreuungsgutscheine_id = UNHEX(REPLACE(UUID(), '-', ''));

	-- Insert institution
	INSERT IGNORE INTO institution (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
									vorgaenger_id, name, mandant_id, traegerschaft_id, status, event_published)
	VALUES (p_institution_id, NOW(), NOW(), 'system', 'system', 0, NULL, p_institution_name, p_mandant_id,
			p_traegerschaft_id, 'AKTIV', FALSE);

	-- Insert adresse
	INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id,
								gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz, strasse, zusatzzeile)
	VALUES (adresse_id, NOW(), NOW(), 'system', 'system', 0, NULL, '1000-01-01',
			'9999-12-31', NULL, '27', 'CH', p_institution_name, p_ort, p_plz, p_strasse, NULL);

	-- Insert auszahlungsdaten
	INSERT IGNORE INTO auszahlungsdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, iban,
										 kontoinhaber, adresse_kontoinhaber_id)
	VALUES (auszahlungsdaten_id, NOW(), NOW(), 'system', 'system', 0,
			'CH82 0900 0000 1001 5000 6', CONCAT('Kontoinhaber ', p_institution_name), NULL);

	-- Insert institution_stammdaten_betreuungsgutscheine
	INSERT IGNORE INTO institution_stammdaten_betreuungsgutscheine (id, timestamp_erstellt, timestamp_mutiert, user_erstellt,
																	user_mutiert, version, auszahlungsdaten_id,
																	alterskategorie_baby, alterskategorie_vorschule,
																	alterskategorie_kindergarten, alterskategorie_schule,
																	anzahl_plaetze, anzahl_plaetze_firmen, offen_von, offen_bis,
																	oeffnungstage_pro_jahr,
																	anzahl_kinder_warteliste, summe_pensum_warteliste,
																	dauer_warteliste, frueh_eroeffnung, spaet_eroeffnung,
																	wochenende_eroeffnung, uebernachtung_moeglich)
	VALUES (betreuungsgutscheine_id, NOW(), NOW(), 'system', 'system', 0,
			auszahlungsdaten_id, FALSE, FALSE, FALSE, FALSE, 40, NULL, '08:00', '18:00', 0, 0.00,
			0.00, 0.00, FALSE, FALSE, FALSE, FALSE);

	-- Insert institution_stammdaten
	INSERT IGNORE INTO institution_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
											   vorgaenger_id, gueltig_ab, gueltig_bis, betreuungsangebot_typ, adresse_id,
											   institution_id, institution_stammdaten_ferieninsel_id,
	                                           institution_stammdaten_tagesschule_id,
											   institution_stammdaten_betreuungsgutscheine_id, mail, telefon, webseite)
	VALUES (p_institution_stammdaten_id, NOW(), NOW(), 'system', 'system', 0, NULL,
			'2019-08-01', '9999-12-31', p_betreuungsangebots_typ,
			adresse_id, p_institution_id, NULL, NULL, betreuungsgutscheine_id,
			p_mail, NULL, NULL);

END
$$

CREATE OR REPLACE PROCEDURE CreateTagesschule(
	IN p_gemeinde_id 		BINARY(16),
	IN p_mandant_id			BINARY(16),
	IN p_institution_stammdaten_tagesschule_id BINARY(16),
	IN p_institution_stammdaten_id BINARY(16)
)
BEGIN
	DECLARE v_einstellungen_tagesschule_id BINARY(16);

	SET v_einstellungen_tagesschule_id = UNHEX(REPLACE(UUID(), '-', ''));

	INSERT IGNORE INTO institution_stammdaten_tagesschule (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,gemeinde_id)
	VALUES (p_institution_stammdaten_tagesschule_id,NOW(), NOW(),'system','system',0, p_gemeinde_id);

	UPDATE institution_stammdaten
	SET institution_stammdaten_tagesschule_id = p_institution_stammdaten_tagesschule_id
	WHERE id = p_institution_stammdaten_id;

	INSERT IGNORE INTO einstellungen_tagesschule (id,timestamp_erstellt,timestamp_mutiert,user_erstellt,user_mutiert,version,
										   modul_tagesschule_typ,gesuchsperiode_id,institution_stammdaten_tagesschule_id,
										   erlaeuterung)
	SELECT *
	FROM (SELECT v_einstellungen_tagesschule_id    	as id,
				 now()              				as timestamp_erstellt,
				 now()              				as timestamp_mutiert,
				 'system'                           as user_erstellt,
				 'system'                           as user_mutiert,
				 0                                  as version,
				 'DYNAMISCH'						as modul_tagesschule_typ,
				 gp.id   							as gesuchsperiode_id,
				 p_institution_stammdaten_tagesschule_id as institution_stammdaten_tagesschule_id,
				 null as erlaeuterung
		  from gesuchsperiode as gp where gp.mandant_id = p_mandant_id) as tmp;
END $$

CREATE OR REPLACE PROCEDURE CreateTagesschuleModule(
	In p_gesuchsperiode_id		BINARY(16),
	IN p_institution_stammdaten_tagesschule_id	BINARY(16),
	IN p_tagesschul_modul_identifier_morgen VARCHAR(255),
	IN p_tagesschul_modul_identifier_nachmittag VARCHAR(255)
)
BEGIN
	DECLARE v_institution_einstellungen_tagesschule_id BINARY(16);
	DECLARE v_text_ressource_tagesschule_Morgen_id BINARY(16);
	DECLARE v_text_ressource_tagesschule_Nachmittag_id BINARY(16);
	DECLARE v_tagesschule_modul_group_Morgen_id BINARY(16);
	DECLARE v_tagesschule_modul_group_Nachmittag_id BINARY(16);

	SET v_institution_einstellungen_tagesschule_id = UNHEX(REPLACE(UUID(), '-', ''));
	SET v_text_ressource_tagesschule_Morgen_id = UNHEX(REPLACE(UUID(), '-', ''));
	SET v_text_ressource_tagesschule_Nachmittag_id = UNHEX(REPLACE(UUID(), '-', ''));
	SET v_tagesschule_modul_group_Morgen_id = UNHEX(REPLACE(UUID(), '-', ''));
	SET v_tagesschule_modul_group_Nachmittag_id = UNHEX(REPLACE(UUID(), '-', ''));

	INSERT IGNORE INTO einstellungen_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, modul_tagesschule_typ, gesuchsperiode_id, institution_stammdaten_tagesschule_id, erlaeuterung, tagi)
	VALUES (v_institution_einstellungen_tagesschule_id, now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'DYNAMISCH', p_gesuchsperiode_id, p_institution_stammdaten_tagesschule_id, null, false);

	-- Morgen
	INSERT IGNORE INTO text_ressource (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, text_deutsch, text_franzoesisch)
	VALUES (v_text_ressource_tagesschule_Morgen_id, now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, 'Morgen', 'Matin');

	INSERT IGNORE INTO modul_tagesschule_group (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, identifier, intervall, modul_tagesschule_name, reihenfolge, verpflegungskosten, wird_paedagogisch_betreut, zeit_bis, zeit_von, einstellungen_tagesschule_id, bezeichnung_id, fremd_id)
	VALUES (v_tagesschule_modul_group_Morgen_id, now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, p_tagesschul_modul_identifier_morgen, 'WOECHENTLICH', 'DYNAMISCH', 0, 3.00, true, '12:00:00', '08:00:00', v_institution_einstellungen_tagesschule_id, v_text_ressource_tagesschule_Morgen_id, null);

	INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX(REPLACE(UUID(), '-', '')), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'MONDAY', v_tagesschule_modul_group_Morgen_id);
	INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX(REPLACE(UUID(), '-', '')), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'TUESDAY', v_tagesschule_modul_group_Morgen_id);
	INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX(REPLACE(UUID(), '-', '')), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'WEDNESDAY', v_tagesschule_modul_group_Morgen_id);
	INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX(REPLACE(UUID(), '-', '')), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'THURSDAY', v_tagesschule_modul_group_Morgen_id);
	INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX(REPLACE(UUID(), '-', '')), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'FRIDAY', v_tagesschule_modul_group_Morgen_id);

	-- Nachmittag
	INSERT IGNORE INTO text_ressource (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, text_deutsch, text_franzoesisch)
	VALUES (v_text_ressource_tagesschule_Nachmittag_id, now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, null, 'Nachmittag', 'Après-midi');

	INSERT IGNORE INTO modul_tagesschule_group (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, identifier, intervall, modul_tagesschule_name, reihenfolge, verpflegungskosten, wird_paedagogisch_betreut, zeit_bis, zeit_von, einstellungen_tagesschule_id, bezeichnung_id, fremd_id)
	VALUES (v_tagesschule_modul_group_Nachmittag_id, now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, p_tagesschul_modul_identifier_nachmittag, 'WOECHENTLICH', 'DYNAMISCH', 0, 2.00, true, '17:00:00', '13:00:00', v_institution_einstellungen_tagesschule_id, v_text_ressource_tagesschule_Nachmittag_id, null);

	INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX(REPLACE(UUID(), '-', '')), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'MONDAY', v_tagesschule_modul_group_Nachmittag_id);
	INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX(REPLACE(UUID(), '-', '')), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'TUESDAY', v_tagesschule_modul_group_Nachmittag_id);
	INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX(REPLACE(UUID(), '-', '')), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'WEDNESDAY', v_tagesschule_modul_group_Nachmittag_id);
	INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX(REPLACE(UUID(), '-', '')), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'THURSDAY', v_tagesschule_modul_group_Nachmittag_id);
	INSERT IGNORE INTO modul_tagesschule (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, wochentag, modul_tagesschule_group_id) VALUES (UNHEX(REPLACE(UUID(), '-', '')), now(), now(), 'ebegu:Kanton Bern', 'ebegu:Kanton Bern', 0, 'FRIDAY', v_tagesschule_modul_group_Nachmittag_id);
END $$

CREATE OR REPLACE PROCEDURE CreateSozialdienst(
	IN p_sozialdienst_id BINARY(16),
	IN p_name            VARCHAR(255),
	IN p_mandant_id      BINARY(16),
	IN p_ort             VARCHAR(255),
	IN p_plz             VARCHAR(255)
)
BEGIN
	DECLARE adresse_id BINARY(16);
	SET adresse_id = UNHEX(REPLACE(UUID(), '-', ''));

	-- Insert into sozialdienst
	INSERT IGNORE INTO sozialdienst (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
									 vorgaenger_id, name, status, mandant_id)
	VALUES (p_sozialdienst_id, NOW(), NOW(), 'system', 'system', 0, NULL, p_name, 'AKTIV', p_mandant_id);

	-- Insert into adresse
	INSERT IGNORE INTO adresse (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
								vorgaenger_id, gueltig_ab, gueltig_bis, gemeinde, hausnummer, land, organisation, ort, plz,
								strasse, zusatzzeile)
	VALUES (adresse_id, NOW(), NOW(), 'system', 'system', 0, NULL, '1000-01-01', '9999-12-31',
			NULL, '2', 'CH', p_name, p_ort, p_plz, 'Sozialdienststrasse', NULL);

	-- Insert into sozialdienst_stammdaten
	INSERT IGNORE INTO sozialdienst_stammdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
												version, vorgaenger_id, mail, telefon, webseite, adresse_id, sozialdienst_id)
	VALUES (UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, NULL,
			CONCAT('sozialdienst-', LOWER(p_name), '@mailbucket.dvbern.ch'), '078 898 98 98',
			CONCAT('www.sozialdienst-', LOWER(p_name), '.ch'),
			adresse_id, p_sozialdienst_id);

END

$$

DELIMITER ;
