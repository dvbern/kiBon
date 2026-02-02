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
			 '12' as value,
			 NULL                               as gemeinde_id,
			 gp.id                              as gesuchsperiode_id,
			 UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')) as mandant_id -- Bern
	  from gesuchsperiode as gp) as tmp;

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
			 UNHEX(REPLACE('08687de9-b3d0-11ee-829a-0242ac160002', '-','')) as mandant_id -- Schwyz
	  from gesuchsperiode as gp) as tmp;

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
			 UNHEX(REPLACE('485d7483-30a2-11ec-a86f-b89a2ae4a038', '-','')) as mandant_id -- Luzern
	  from gesuchsperiode as gp) as tmp;

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
			 UNHEX(REPLACE('7781a6bb-5374-11ec-98e8-f4390979fa3e', '-','')) as mandant_id -- Solothurn
	  from gesuchsperiode as gp) as tmp;

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
			 UNHEX(REPLACE('5b9e6fa4-3991-11ed-a63d-b05cda43de9c', '-','')) as mandant_id -- AZA
	  from gesuchsperiode as gp) as tmp;
