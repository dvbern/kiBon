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
 alter table gemeinde_stammdaten_korrespondenz
 add barcode_spacing_left INTEGER NOT NULL default 20;

 alter table gemeinde_stammdaten_korrespondenz
 add barcode_spacing_top INTEGER NOT NULL default 14;

alter table gemeinde_stammdaten_korrespondenz_aud
    add barcode_spacing_left INTEGER;

alter table gemeinde_stammdaten_korrespondenz_aud
    add barcode_spacing_top INTEGER;
