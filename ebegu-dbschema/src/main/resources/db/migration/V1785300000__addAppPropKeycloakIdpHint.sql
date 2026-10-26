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

# Bern uses legacy BE-Login (Alias keycloak-oidc), no other mandant has an external idp.
INSERT INTO application_property (id, mandant_id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert,
                                  version, vorgaenger_id, name, value)
SELECT UNHEX(REPLACE(UUID(), '-', '')), mandant.id, now(), now(), 'flyway', 'flyway', 0, null,
       'KEYCLOAK_IDP_HINT',
       IF(mandant.mandant_identifier = 'BERN', 'BELOGIN_LEGACY', 'NO_HINT')
FROM mandant;
