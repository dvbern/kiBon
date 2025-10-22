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

ALTER TABLE berechtigung_history
	ADD COLUMN IF NOT EXISTS benutzer_id BINARY(16);
ALTER TABLE berechtigung_history_aud
	ADD COLUMN IF NOT EXISTS benutzer_id BINARY(16);

ALTER TABLE berechtigung_history
	ADD CONSTRAINT FK_berechtigung_history_benutzer_id FOREIGN KEY (benutzer_id) REFERENCES benutzer(id);

# Set the benutzer_id for all entries that have only one user with the username
UPDATE
	berechtigung_history bh
		JOIN
		benutzer b
		ON
			bh.username = b.username
SET bh.benutzer_id = b.id
WHERE b.id IN (SELECT id
			   FROM benutzer
			   WHERE username IN
					 (SELECT username
					  FROM benutzer
					  GROUP BY username
					  HAVING COUNT
							 (username) = 1));

# Set the benutzer_id for all entries with multiple benutzer on different mandanten. Use the traegerschaft mandant to select a unique user per berechtigung with traegerschaft
UPDATE berechtigung_history
	INNER JOIN traegerschaft ON traegerschaft.id = berechtigung_history.traegerschaft_id
	INNER JOIN (SELECT *
				FROM benutzer
				WHERE username IN (SELECT username
								   FROM benutzer
								   GROUP BY username
								   HAVING COUNT(username) > 1)) as users
	ON users.username = berechtigung_history.username AND users.mandant_id = traegerschaft.mandant_id
SET benutzer_id = users.id
WHERE benutzer_id IS NULL;

# Set the benutzer_id for all entries with multiple benutzer on different mandanten. Use the institution mandant to select a unique user per berechtigung with institution
UPDATE berechtigung_history
	INNER JOIN institution ON institution.id = berechtigung_history.institution_id
	INNER JOIN (SELECT *
				FROM benutzer
				WHERE username IN (SELECT username
								   FROM benutzer
								   GROUP BY username
								   HAVING COUNT(username) > 1)) as users
	ON users.username = berechtigung_history.username AND users.mandant_id = institution.mandant_id
SET benutzer_id = users.id
WHERE benutzer_id IS NULL;

# Set the benutzer_id for all entries with multiple benutzer on different mandanten. Use the sozialdienst mandant to select a unique user per berechtigung with sozialdienst
UPDATE berechtigung_history
	INNER JOIN sozialdienst ON sozialdienst.id = berechtigung_history.sozialdienst_id
	INNER JOIN (SELECT *
				FROM benutzer
				WHERE username IN (SELECT username
								   FROM benutzer
								   GROUP BY username
								   HAVING COUNT(username) > 1)) as users
	ON users.username = berechtigung_history.username AND users.mandant_id = sozialdienst.mandant_id
SET benutzer_id = users.id
WHERE benutzer_id IS NULL;
