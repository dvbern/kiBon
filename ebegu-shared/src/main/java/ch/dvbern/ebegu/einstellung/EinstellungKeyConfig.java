/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.einstellung;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

/**
 * A configuration class used to encapsulate configuration details associated with EinstellungKeys.
 * This record provides utility methods for creating specific configurations for mandants
 * and gemeinde types without having to declare all variables.
 *
 * @param mandants a list of {@link MandantIdentifier} objects representing the mandants
 * applicable to this configuration. Must not be null
 * @param typ the type of configuration as defined by {@link EinstellungTyp},
 * denoting if the configuration is on the level of a mandant or gemeinde. Must not be null
 * @param grouping an instance of {@link KeyGrouping}, specifying grouping details
 * for the configuration or none if not applicable. Must not be null
 */
public record EinstellungKeyConfig(
								   @Nonnull List<MandantIdentifier> mandants,
								   @Nonnull EinstellungTyp typ,
								   @Nonnull KeyGrouping grouping
) {

	public EinstellungKeyConfig {
		mandants = List.copyOf(mandants);
		Objects.requireNonNull(typ);
		Objects.requireNonNull(grouping);
	}

	/**
	 * Creates an EinstellungKeyConfig instance for a given MandantIdentifier with the "MANDANT" type.
	 *
	 * @param mandant the {@link MandantIdentifier} of the {@link Mandant} for which the configuration is applicable
	 * @return an EinstellungKeyConfig instance associated with the specified {@link MandantIdentifier}
	 */
	public static EinstellungKeyConfig mandantEinstellung(
		@Nonnull MandantIdentifier mandant
	) {
		return mandantEinstellung(List.of(mandant));
	}

	/**
	 * Creates an EinstellungKeyConfig instance for the provided list of MandantIdentifiers with the "MANDANT" type.
	 *
	 * @param mandants the list of {@link MandantIdentifier} objects representing the mandants for which the
	 * configuration is applicable
	 * @return an EinstellungKeyConfig instance configured with the provided list of mandants and the "MANDANT" type
	 */
	public static EinstellungKeyConfig mandantEinstellung(
		@Nonnull List<MandantIdentifier> mandants
	) {
		return of(mandants, EinstellungTyp.MANDANT);
	}

	/**
	 * Creates an EinstellungKeyConfig instance for a given MandantIdentifier with the "GEMEINDE" type.
	 *
	 * @param mandant the {@link MandantIdentifier} of the {@link Mandant} for which the gemeinde-configuration is
	 * applicable
	 * @return an EinstellungKeyConfig instance associated with the specified {@link MandantIdentifier} and the
	 * "GEMEINDE" type
	 */
	public static EinstellungKeyConfig gemeindeEinstellung(
		@Nonnull MandantIdentifier mandant
	) {
		return gemeindeEinstellung(List.of(mandant));
	}

	/**
	 * Creates an EinstellungKeyConfig instance for the provided list of MandantIdentifiers with the "GEMEINDE" type.
	 *
	 * @param mandants the list of {@link MandantIdentifier} objects representing the mandants for which the
	 * gemeinde-configuration is applicable
	 * @return an EinstellungKeyConfig instance configured with the provided list of mandants and the "GEMEINDE" type
	 */
	public static EinstellungKeyConfig gemeindeEinstellung(
		@Nonnull List<MandantIdentifier> mandants
	) {
		return of(mandants, EinstellungTyp.GEMEINDE);
	}

	/**
	 * Creates a new EinstellungKeyConfig instance using the specified configurable key group.
	 *
	 * @param group the {@link ConfigurableKeyGroup} representing the key group for which the configuration should be
	 * created
	 * @param order the order in which the configuration should be displayed
	 * @return a new EinstellungKeyConfig instance associated with the provided key group
	 */
	public EinstellungKeyConfig inGroup(
		@Nonnull ConfigurableKeyGroup group,
		int order
	) {
		return new EinstellungKeyConfig(
			mandants,
			typ,
			KeyGrouping.of(group, order)
		);
	}

	private static EinstellungKeyConfig of(
		@Nonnull List<MandantIdentifier> mandants,
		@Nonnull EinstellungTyp typ
	) {
		return new EinstellungKeyConfig(mandants, typ, KeyGrouping.NONE());
	}
}
