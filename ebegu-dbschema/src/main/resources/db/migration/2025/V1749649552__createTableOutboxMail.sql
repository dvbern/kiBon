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

CREATE TABLE IF NOT EXISTS outbox_mail
(
    id                 BINARY(16)   NOT NULL,
    timestamp_erstellt DATETIME     NOT NULL,
    timestamp_mutiert  DATETIME     NOT NULL,
    user_erstellt      VARCHAR(255) NOT NULL,
    user_mutiert       VARCHAR(255) NOT NULL,
    version            BIGINT       NOT NULL,
    recipient          VARCHAR(255) NOT NULL,
    subject            TEXT         NOT NULL,
    content            TEXT         NOT NULL,
	retry_count		   INTEGER		NOT NULL,
	status			   VARCHAR(255)  NOT NULL,
    mandant            VARCHAR(255) NOT NULL
);


