/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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


/* update all missing mandant_id */
UPDATE einstellung e1
	JOIN (SELECT g.mandant_id, e3.id
	FROM gesuchsperiode g
	JOIN einstellung e3 ON g.id = e3.gesuchsperiode_id) e2
ON e1.id = e2.id SET e1.mandant_id = e2.mandant_id
WHERE einstellung_key LIKE '%GEMEINDE_PAUSCHALBETRAG%';

/* delete all duplicates */
DELETE e1
FROM einstellung e1
	 JOIN einstellung e2
		  ON e1.gesuchsperiode_id = e2.gesuchsperiode_id
			  AND e1.mandant_id = e2.mandant_id
			  AND e1.einstellung_key = e2.einstellung_key
			  AND (
				 e1.gemeinde_id = e2.gemeinde_id
					 OR (e1.gemeinde_id IS NULL AND e2.gemeinde_id IS NULL)
				 )
WHERE e1.id > e2.id
		AND e1.einstellung_key LIKE 'GEMEINDE_PAUSCHALBETRAG%';


