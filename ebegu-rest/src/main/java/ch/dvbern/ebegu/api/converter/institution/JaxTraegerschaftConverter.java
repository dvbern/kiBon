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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.converter.institution;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.dtos.JaxTraegerschaft;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.services.InstitutionService;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxTraegerschaftConverter extends AbstractBaseConverter {
	@Inject
	private InstitutionService institutionService;

	/**
	 * Diese Methode verwenden nur wenn man der Institution Count und InstitutionNamen benoetigt
	 */
	public JaxTraegerschaft traegerschaftToJAX(
		final Traegerschaft persistedTraegerschaft
	) {
		final JaxTraegerschaft jaxTraegerschaft = new JaxTraegerschaft();
		convertAbstractVorgaengerFieldsToJAX(
			persistedTraegerschaft,
			jaxTraegerschaft
		);
		jaxTraegerschaft.setName(persistedTraegerschaft.getName());
		jaxTraegerschaft.setActive(persistedTraegerschaft.getActive());
		jaxTraegerschaft.setEmail(persistedTraegerschaft.getEmail());

		Collection<Institution> institutionen =
			institutionService.getAllInstitutionenFromTraegerschaft(
				persistedTraegerschaft.getId()
			);
		// its enough if we just pass the names here, we only want to display it later
		jaxTraegerschaft.setInstitutionNames(
			institutionen.stream()
				.map(Institution::getName)
				.collect(Collectors.joining(", "))
		);
		jaxTraegerschaft.setInstitutionCount(institutionen.size());
		return jaxTraegerschaft;
	}

	public Traegerschaft traegerschaftToEntity(
		@Nonnull final JaxTraegerschaft traegerschaftJAXP,
		@Nonnull final Traegerschaft traegerschaft
	) {

		requireNonNull(traegerschaft);
		requireNonNull(traegerschaftJAXP);
		convertAbstractVorgaengerFieldsToEntity(
			traegerschaftJAXP,
			traegerschaft
		);
		convertMandantFieldsToEntity(traegerschaft);
		traegerschaft.setName(traegerschaftJAXP.getName());
		traegerschaft.setActive(traegerschaftJAXP.getActive());
		traegerschaft.setEmail(traegerschaftJAXP.getEmail());

		return traegerschaft;
	}
}
