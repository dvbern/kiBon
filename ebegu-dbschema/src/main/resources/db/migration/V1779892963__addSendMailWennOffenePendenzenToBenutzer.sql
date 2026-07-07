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

ALTER TABLE benutzer
    ADD send_mail_wenn_offene_pendenzen BIT DEFAULT TRUE;
ALTER TABLE benutzer_aud
    ADD send_mail_wenn_offene_pendenzen BIT;

/* For Mandant Schwyz we activate it for every users that have or will have a Gemeinde Role */
UPDATE benutzer
set send_mail_wenn_offene_pendenzen = true
where benutzer.id in (select b.id
                      from benutzer b,
                           berechtigung br,
                           mandant m
                      where br.benutzer_id = b.id
                        and b.mandant_id = m.id
                        and m.mandant_identifier = 'SCHWYZ'
                        and br.role in ('ADMIN_BG',
                                        'SACHBEARBEITER_BG',
                                        'ADMIN_GEMEINDE',
                                        'SACHBEARBEITER_GEMEINDE',
                                        'ADMIN_TS',
                                        'SACHBEARBEITER_TS')
                        and br.gueltig_bis > sysdate());
