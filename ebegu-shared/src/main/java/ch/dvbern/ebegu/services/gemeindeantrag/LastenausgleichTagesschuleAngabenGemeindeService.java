/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;

/**
 * Service fuer den Lastenausgleich der Tagesschulen
 */
public interface LastenausgleichTagesschuleAngabenGemeindeService {

	/**
	 * Erstellt fuer jede aktive Gemeinde einen LastenausgleichTagesschule fuer die angegebene Periode
	 */
	@Nonnull
	List<? extends GemeindeAntrag> createLastenausgleichTagesschuleGemeinde(
		@Nonnull Gesuchsperiode gesuchsperiode);

	/**
	 * Speichert den LastenausgleichTagesschule, ohne Eintrag in die StatusHistory-Tabelle
	 */
	@Nonnull
	LastenausgleichTagesschuleAngabenGemeindeContainer saveLastenausgleichTagesschuleGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer);

	/**
	 * Speichert den LastenausgleichTagesschule, inklusive Eintrag in die StatusHistory-Tabelle
	 */
	@Nonnull
	LastenausgleichTagesschuleAngabenGemeindeContainer saveLastenausgleichTagesschuleGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer,
		boolean saveInStatusHistory);

	/**
	 * Reicht den Lastenausgleich ein, inkl. kopieren der Daten vom Korrektur- in den Deklarations-Container,
	 * falls die Vorbedingungen dazu erfuellt sind.
	 */
	@Nonnull
	LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeEinreichen(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer);
}
