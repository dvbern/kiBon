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

CREATE TABLE meldungsfenster_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	zielgruppe         VARCHAR(510),
	title_de           VARCHAR(255),
	title_fr           VARCHAR(255),
	gueltig_ab         VARCHAR(255),
	gueltig_bis        VARCHAR(255),
	status             VARCHAR(255),
	inhalt_de          TEXT,
	inhalt_fr          TEXT,
	mandant_id         BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE meldungsfenster (
	id                 BINARY(16)   NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	zielgruppe         VARCHAR(510) NOT NULL,
	title_de           VARCHAR(255),
	title_fr           VARCHAR(255),
	gueltig_ab         DATETIME     NOT NULL,
	gueltig_bis        DATETIME     NOT NULL,
	status             VARCHAR(255) NOT NULL,
	inhalt_de          TEXT,
	inhalt_fr          TEXT,
	mandant_id         BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

ALTER TABLE meldungsfenster
ADD CONSTRAINT FK_meldungsfenster_mandant_id
	FOREIGN KEY (mandant_id)
		REFERENCES mandant(id);
