INSERT INTO application_property (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, name, value)
VALUES (UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')), '2020-05-26 00:00:00', '2020-05-27 00:00:00', 'flyway', 'flyway', 0, null, 'KANTON_NOTVERORDNUNG_PHASE_2_AKTIV', 'false');