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
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
                         einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID(), '-', '')) as id,
             '2020-01-01 00:00:00' as timestamp_erstellt,
             '2020-01-01 00:00:00' as timestamp_mutiert,
             'flyway' as user_erstellt,
             'flyway' as user_mutiert,
             0 as version,
             'GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_MAX_AGE_OF_CHILD' as einstellung_key,
             '18' as value,
             NULL                               as gemeinde_id,
             gp.id                              as gesuchsperiode_id,
             UNHEX(REPLACE('76783c4a-def2-4d0c-9e0f-209a7b190d15', '-','')) as mandant_id -- Luzern
      from gesuchsperiode as gp where gp.mandant_id = UNHEX(REPLACE('76783c4a-def2-4d0c-9e0f-209a7b190d15', '-',''))) as tmp;
