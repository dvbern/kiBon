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

UPDATE dokument_grund
	INNER JOIN gesuch ON dokument_grund.gesuch_id = gesuch.id
	INNER JOIN gesuchsperiode gp ON gesuch.gesuchsperiode_id = gp.id
	INNER JOIN mandant ON gp.mandant_id = mandant.id
SET dokument_grund.tag               = NULL, dokument_grund.version = dokument_grund.version + 1,
	dokument_grund.timestamp_mutiert = NOW(), dokument_grund.user_mutiert = 'flyway'
WHERE mandant.mandant_identifier = 'SCHWYZ' AND dokument_grund.dokument_grund_typ = 'EINKOMMENSVERSCHLECHTERUNG' AND
	  dokument_grund.tag IS NOT NULL;
