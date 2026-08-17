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

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = '52.5'
WHERE einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_TG' AND
	  gesuchsperiode_id = (select gesuchsperiode.id
						   from gesuchsperiode
								join mandant on gesuchsperiode.mandant_id = mandant.id
						   where gesuchsperiode.gueltig_ab = '2026-08-01' and mandant_identifier = 'BERN');

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = '4.45'
WHERE einstellung_key = 'ZUSCHLAG_BEHINDERUNG_PRO_STD' AND
	  gesuchsperiode_id = (select gesuchsperiode.id
						   from gesuchsperiode
								join mandant on gesuchsperiode.mandant_id = mandant.id
						   where gesuchsperiode.gueltig_ab = '2026-08-01' and mandant_identifier = 'BERN');

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = '170000'
WHERE einstellung_key = 'MAX_MASSGEBENDES_EINKOMMEN' AND
	  gesuchsperiode_id = (select gesuchsperiode.id
						   from gesuchsperiode
								join mandant on gesuchsperiode.mandant_id = mandant.id
						   where gesuchsperiode.gueltig_ab = '2026-08-01' and mandant_identifier = 'BERN');

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = '49000'
WHERE einstellung_key = 'MIN_MASSGEBENDES_EINKOMMEN' AND
	  gesuchsperiode_id = (select gesuchsperiode.id
						   from gesuchsperiode
								join mandant on gesuchsperiode.mandant_id = mandant.id
						   where gesuchsperiode.gueltig_ab = '2026-08-01' and mandant_identifier = 'BERN');

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = '18'
WHERE einstellung_key = 'DAUER_BABYTARIF' AND
	  gesuchsperiode_id = (select gesuchsperiode.id
						   from gesuchsperiode
								join mandant on gesuchsperiode.mandant_id = mandant.id
						   where gesuchsperiode.gueltig_ab = '2026-08-01' and mandant_identifier = 'BERN');

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = '157.5'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' AND
	  gesuchsperiode_id = (select gesuchsperiode.id
						   from gesuchsperiode
								join mandant on gesuchsperiode.mandant_id = mandant.id
						   where gesuchsperiode.gueltig_ab = '2026-08-01' and mandant_identifier = 'BERN');

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = '13.4'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' AND
	  gesuchsperiode_id = (select gesuchsperiode.id
						   from gesuchsperiode
								join mandant on gesuchsperiode.mandant_id = mandant.id
						   where gesuchsperiode.gueltig_ab = '2026-08-01' and mandant_identifier = 'BERN');

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = '78.80'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG' AND
	  gesuchsperiode_id = (select gesuchsperiode.id
						   from gesuchsperiode
								join mandant on gesuchsperiode.mandant_id = mandant.id
						   where gesuchsperiode.gueltig_ab = '2026-08-01' and mandant_identifier = 'BERN');

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = '8.9'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD' AND
	  gesuchsperiode_id = (select gesuchsperiode.id
						   from gesuchsperiode
								join mandant on gesuchsperiode.mandant_id = mandant.id
						   where gesuchsperiode.gueltig_ab = '2026-08-01' and mandant_identifier = 'BERN');

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = '105'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' AND
	  gesuchsperiode_id = (select gesuchsperiode.id
						   from gesuchsperiode
								join mandant on gesuchsperiode.mandant_id = mandant.id
						   where gesuchsperiode.gueltig_ab = '2026-08-01' and mandant_identifier = 'BERN');

UPDATE einstellung INNER JOIN gesuchsperiode ON einstellung.gesuchsperiode_id = gesuchsperiode.id INNER JOIN mandant m ON gesuchsperiode.mandant_id = m.id
SET value = '8.9'
WHERE einstellung_key = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD' AND
	  gesuchsperiode_id = (select gesuchsperiode.id
						   from gesuchsperiode
								join mandant on gesuchsperiode.mandant_id = mandant.id
						   where gesuchsperiode.gueltig_ab = '2026-08-01' and mandant_identifier = 'BERN');
