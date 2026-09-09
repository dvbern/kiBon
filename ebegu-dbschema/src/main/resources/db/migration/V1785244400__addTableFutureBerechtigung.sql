/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

create table future_berechtigung (
	id                 binary(16)   not null primary key,
	timestamp_erstellt datetime     not null,
	timestamp_mutiert  datetime     not null,
	user_erstellt      varchar(255) not null,
	user_mutiert       varchar(255) not null,
	version            bigint       not null,
	vorgaenger_id      varchar(36)  null,
	gueltig_ab         date         not null,
	gueltig_bis        date         not null,
	role               varchar(255) not null,
	institution_id     binary(16)   null,
	traegerschaft_id   binary(16)   null,
	sozialdienst_id    binary(16)   null,
	constraint FK_future_berechtigung_institution_id
		foreign key (institution_id) references institution(id),
	constraint FK_future_berechtigung_traegerschaft_id
		foreign key (traegerschaft_id) references traegerschaft(id),
	constraint FK_future_berechtigung_sozialdienst_id
		foreign key (sozialdienst_id) references sozialdienst(id)
);

create table future_berechtigung_aud (
	id                 binary(16)   not null,
	rev                int          not null,
	revtype            tinyint      not null,
	timestamp_erstellt datetime     null,
	timestamp_mutiert  datetime     null,
	user_erstellt      varchar(255) null,
	user_mutiert       varchar(255) null,
	vorgaenger_id      varchar(36)  null,
	gueltig_ab         date         null,
	gueltig_bis        date         null,
	role               varchar(255) null,
	institution_id     binary(16)   null,
	traegerschaft_id   binary(16)   null,
	sozialdienst_id    binary(16)   null,
	primary key (id, rev),
	constraint FK_future_berechtigung_aud_revinfo
		foreign key (rev) references revinfo(rev)
);

create table future_berechtigung_gemeinde (
	future_berechtigung_id binary(16) not null,
	gemeinde_id            binary(16) not null,
	primary key (future_berechtigung_id, gemeinde_id),
	constraint FK_future_berechtigung_gemeinde_future_berechtigung_id
		foreign key (gemeinde_id) references gemeinde(id),
	constraint FK_future_berechtigung_gemeinde_gemeinde_id
		foreign key (future_berechtigung_id) references future_berechtigung(id)
);

create table future_berechtigung_gemeinde_aud (
	rev                    int        not null,
	revtype                tinyint    not null,
	future_berechtigung_id binary(16) null,
	gemeinde_id            binary(16) null,
	primary key (rev, future_berechtigung_id, gemeinde_id),
	constraint FK_future_berechtigung_gemeinde_aud_revinfo
		foreign key (rev) references revinfo(rev)

);

alter table benutzer
	add future_berechtigung_id binary(16) null;

alter table benutzer
	add constraint FK_benutzer_future_berechtigung
		foreign key (future_berechtigung_id) references future_berechtigung(id);

alter table benutzer_aud
	add future_berechtigung_id binary(16) null;
