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

UPDATE ferienbetreuung_angaben_stammdaten SET status = 'IN_BEARBEITUNG' where status = 'IN_BEARBEITUNG_GEMEINDE';
UPDATE ferienbetreuung_angaben_stammdaten_aud SET status = 'IN_BEARBEITUNG' where status = 'IN_BEARBEITUNG_GEMEINDE';
UPDATE ferienbetreuung_angaben_angebot SET status = 'IN_BEARBEITUNG' where status = 'IN_BEARBEITUNG_GEMEINDE';
UPDATE ferienbetreuung_angaben_angebot_aud SET status = 'IN_BEARBEITUNG' where status = 'IN_BEARBEITUNG_GEMEINDE';
UPDATE ferienbetreuung_angaben_kosten_einnahmen SET status = 'IN_BEARBEITUNG' where status = 'IN_BEARBEITUNG_GEMEINDE';
UPDATE ferienbetreuung_angaben_kosten_einnahmen_aud SET status = 'IN_BEARBEITUNG' where status = 'IN_BEARBEITUNG_GEMEINDE';
UPDATE ferienbetreuung_angaben_nutzung SET status = 'IN_BEARBEITUNG' where status = 'IN_BEARBEITUNG_GEMEINDE';
UPDATE ferienbetreuung_angaben_nutzung_aud SET status = 'IN_BEARBEITUNG' where status = 'IN_BEARBEITUNG_GEMEINDE';
