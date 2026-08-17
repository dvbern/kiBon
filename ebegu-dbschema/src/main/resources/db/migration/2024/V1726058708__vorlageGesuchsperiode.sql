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

DROP TABLE ebegu_vorlage_aud;
DROP TABLE ebegu_vorlage;
DROP TABLE vorlage_aud;
DROP TABLE vorlage;

CREATE TABLE vorlage (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorlage_dokument   LONGBLOB,
	mandant_id         BINARY(16)   NOT NULL,
	gesuchsperiode_id  BINARY(16),
	vorlage_typ        VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

ALTER TABLE vorlage
ADD CONSTRAINT FK_vorlage_mandant_id
	FOREIGN KEY (mandant_id)
		REFERENCES mandant(id);

ALTER TABLE gesuchsperiode
ADD COLUMN vorlage_merkblatt_ts_fr_id BINARY(16);
ALTER TABLE gesuchsperiode
ADD COLUMN vorlage_merkblatt_ts_de_id BINARY(16);
ALTER TABLE gesuchsperiode
ADD COLUMN verfuegung_erlaeuterungen_de_id BINARY(16);
ALTER TABLE gesuchsperiode
ADD COLUMN verfuegung_erlaeuterungen_fr_id BINARY(16);
ALTER TABLE gesuchsperiode
ADD COLUMN vorlage_verfuegung_lats_de_id BINARY(16);
ALTER TABLE gesuchsperiode
ADD COLUMN vorlage_verfuegung_lats_fr_id BINARY(16);
ALTER TABLE gesuchsperiode
ADD COLUMN vorlage_verfuegung_ferienbetreuung_de_id BINARY(16);
ALTER TABLE gesuchsperiode
ADD COLUMN vorlage_verfuegung_ferienbetreuung_fr_id BINARY(16);

ALTER TABLE gesuchsperiode_aud
ADD COLUMN vorlage_merkblatt_ts_fr_id BINARY(16);
ALTER TABLE gesuchsperiode_aud
ADD COLUMN vorlage_merkblatt_ts_de_id BINARY(16);
ALTER TABLE gesuchsperiode_aud
ADD COLUMN verfuegung_erlaeuterungen_de_id BINARY(16);
ALTER TABLE gesuchsperiode_aud
ADD COLUMN verfuegung_erlaeuterungen_fr_id BINARY(16);
ALTER TABLE gesuchsperiode_aud
ADD COLUMN vorlage_verfuegung_lats_de_id BINARY(16);
ALTER TABLE gesuchsperiode_aud
ADD COLUMN vorlage_verfuegung_lats_fr_id BINARY(16);
ALTER TABLE gesuchsperiode_aud
ADD COLUMN vorlage_verfuegung_ferienbetreuung_de_id BINARY(16);
ALTER TABLE gesuchsperiode_aud
ADD COLUMN vorlage_verfuegung_ferienbetreuung_fr_id BINARY(16);

ALTER TABLE gesuchsperiode
ADD CONSTRAINT FK_gesuchsperiode_vorlage_merkblatt_ts_fr_id
	FOREIGN KEY (vorlage_merkblatt_ts_fr_id)
		REFERENCES vorlage(id);
ALTER TABLE gesuchsperiode
ADD CONSTRAINT FK_gesuchsperiode_vorlage_merkblatt_ts_de_id
	FOREIGN KEY (vorlage_merkblatt_ts_de_id)
		REFERENCES vorlage(id);
ALTER TABLE gesuchsperiode
ADD CONSTRAINT FK_gesuchsperiode_verfuegung_erlaeuterungen_de_id
	FOREIGN KEY (verfuegung_erlaeuterungen_de_id)
		REFERENCES vorlage(id);
ALTER TABLE gesuchsperiode
ADD CONSTRAINT FK_gesuchsperiode_verfuegung_erlaeuterungen_fr_id
	FOREIGN KEY (verfuegung_erlaeuterungen_fr_id)
		REFERENCES vorlage(id);
ALTER TABLE gesuchsperiode
ADD CONSTRAINT FK_gesuchsperiode_vorlage_verfuegung_lats_de_id
	FOREIGN KEY (vorlage_verfuegung_lats_de_id)
		REFERENCES vorlage(id);
ALTER TABLE gesuchsperiode
ADD CONSTRAINT FK_gesuchsperiode_vorlage_verfuegung_lats_fr_id
	FOREIGN KEY (vorlage_verfuegung_lats_fr_id)
		REFERENCES vorlage(id);
ALTER TABLE gesuchsperiode
ADD CONSTRAINT FK_gesuchsperiode_vorlage_verfuegung_ferienbetreuung_de_id
	FOREIGN KEY (vorlage_verfuegung_ferienbetreuung_de_id)
		REFERENCES vorlage(id);
ALTER TABLE gesuchsperiode
ADD CONSTRAINT FK_gesuchsperiode_vorlage_verfuegung_ferienbetreuung_fr_id
	FOREIGN KEY (vorlage_verfuegung_ferienbetreuung_fr_id)
		REFERENCES vorlage(id);

INSERT INTO vorlage
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, vorlage_merkblatt_ts_fr, mandant_id, id,
	'vorlage_merkblatt_ts_fr'
FROM gesuchsperiode
WHERE vorlage_merkblatt_ts_fr IS NOT NULL;
INSERT INTO vorlage
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, vorlage_merkblatt_ts_de, mandant_id, id,
	'vorlage_merkblatt_ts_de'
FROM gesuchsperiode
WHERE vorlage_merkblatt_ts_de IS NOT NULL;
INSERT INTO vorlage
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, verfuegung_erlaeuterungen_de, mandant_id, id,
	'verfuegung_erlaeuterungen_de'
FROM gesuchsperiode
WHERE verfuegung_erlaeuterungen_de IS NOT NULL;
INSERT INTO vorlage
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, verfuegung_erlaeuterungen_fr, mandant_id, id,
	'verfuegung_erlaeuterungen_fr'
FROM gesuchsperiode
WHERE verfuegung_erlaeuterungen_fr IS NOT NULL;
INSERT INTO vorlage
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, vorlage_verfuegung_lats_de, mandant_id, id,
	'vorlage_verfuegung_lats_de'
FROM gesuchsperiode
WHERE vorlage_verfuegung_lats_de IS NOT NULL;
INSERT INTO vorlage
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, vorlage_verfuegung_lats_fr, mandant_id, id,
	'vorlage_verfuegung_lats_fr'
FROM gesuchsperiode
WHERE vorlage_verfuegung_lats_fr IS NOT NULL;
INSERT INTO vorlage
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, vorlage_verfuegung_ferienbetreuung_de, mandant_id,
	id, 'vorlage_verfuegung_ferienbetreuung_de'
FROM gesuchsperiode
WHERE vorlage_verfuegung_ferienbetreuung_de IS NOT NULL;
INSERT INTO vorlage
SELECT UNHEX(REPLACE(UUID(), '-', '')), NOW(), NOW(), 'system', 'system', 0, vorlage_verfuegung_ferienbetreuung_fr, mandant_id,
	id, 'vorlage_verfuegung_ferienbetreuung_fr'
FROM gesuchsperiode
WHERE vorlage_verfuegung_ferienbetreuung_fr IS NOT NULL;

UPDATE gesuchsperiode g JOIN vorlage v ON g.id = v.gesuchsperiode_id
SET g.vorlage_merkblatt_ts_de_id = v.id
WHERE v.vorlage_typ = 'vorlage_merkblatt_ts_de';
UPDATE gesuchsperiode g JOIN vorlage v ON g.id = v.gesuchsperiode_id
SET g.vorlage_merkblatt_ts_fr_id = v.id
WHERE v.vorlage_typ = 'vorlage_merkblatt_ts_fr';
UPDATE gesuchsperiode g JOIN vorlage v ON g.id = v.gesuchsperiode_id
SET g.verfuegung_erlaeuterungen_de_id = v.id
WHERE v.vorlage_typ = 'verfuegung_erlaeuterungen_de';
UPDATE gesuchsperiode g JOIN vorlage v ON g.id = v.gesuchsperiode_id
SET g.verfuegung_erlaeuterungen_fr_id = v.id
WHERE v.vorlage_typ = 'verfuegung_erlaeuterungen_fr';
UPDATE gesuchsperiode g JOIN vorlage v ON g.id = v.gesuchsperiode_id
SET g.vorlage_verfuegung_lats_de_id = v.id
WHERE v.vorlage_typ = 'vorlage_verfuegung_lats_de';
UPDATE gesuchsperiode g JOIN vorlage v ON g.id = v.gesuchsperiode_id
SET g.vorlage_verfuegung_lats_fr_id = v.id
WHERE v.vorlage_typ = 'vorlage_verfuegung_lats_fr';
UPDATE gesuchsperiode g JOIN vorlage v ON g.id = v.gesuchsperiode_id
SET g.vorlage_verfuegung_ferienbetreuung_de_id = v.id
WHERE v.vorlage_typ = 'vorlage_verfuegung_ferienbetreuung_de';
UPDATE gesuchsperiode g JOIN vorlage v ON g.id = v.gesuchsperiode_id
SET g.vorlage_verfuegung_ferienbetreuung_fr_id = v.id
WHERE v.vorlage_typ = 'vorlage_verfuegung_ferienbetreuung_fr';

ALTER TABLE vorlage
DROP COLUMN gesuchsperiode_id;
ALTER TABLE vorlage
DROP COLUMN vorlage_typ;
ALTER TABLE gesuchsperiode
DROP COLUMN vorlage_merkblatt_ts_de;
ALTER TABLE gesuchsperiode
DROP COLUMN vorlage_merkblatt_ts_fr;
ALTER TABLE gesuchsperiode
DROP COLUMN verfuegung_erlaeuterungen_de;
ALTER TABLE gesuchsperiode
DROP COLUMN verfuegung_erlaeuterungen_fr;
ALTER TABLE gesuchsperiode
DROP COLUMN vorlage_verfuegung_lats_de;
ALTER TABLE gesuchsperiode
DROP COLUMN vorlage_verfuegung_lats_fr;
ALTER TABLE gesuchsperiode
DROP COLUMN vorlage_verfuegung_ferienbetreuung_de;
ALTER TABLE gesuchsperiode
DROP COLUMN vorlage_verfuegung_ferienbetreuung_fr;
