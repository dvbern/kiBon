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

create table ferienbetreuung_angaben_container_status_history
(
    id                            binary(16) not null,
    timestamp_erstellt            datetime     not null,
    timestamp_mutiert             datetime     not null,
    user_erstellt                 varchar(255) not null,
    user_mutiert                  varchar(255) not null,
    version                       bigint       not null,
    status                        varchar(255) not null,
    timestamp_bis                 datetime,
    timestamp_von                 datetime     not null,
    container_id binary(16) not null,
    benutzer_id                   binary(16) not null,
    primary key (id)
);

alter table ferienbetreuung_angaben_container_status_history
    add constraint FK_fb_statushistory_id
        foreign key (container_id)
            references ferienbetreuung_angaben_container (id);

alter table ferienbetreuung_angaben_container_status_history
    add constraint FK_fb_statushistory_benutzer_id
        foreign key (benutzer_id)
            references benutzer (id);
