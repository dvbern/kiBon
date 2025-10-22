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

import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.dtos.JaxInstitution;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionListDTO;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionUpdate;
import ch.dvbern.ebegu.api.dtos.JaxTraegerschaft;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.InstitutionStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.TraegerschaftService;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxInstitutionConverter extends AbstractBaseConverter {
	@Inject
	private TraegerschaftService traegerschaftService;

	@Inject
	private MandantService mandantService;

	@Nonnull
	public Institution institutionToNewEntity(
		@Nonnull JaxInstitution institutionJAXP
	) {
		requireNonNull(institutionJAXP);
		Institution institution = new Institution();
		convertAbstractVorgaengerFieldsToEntity(institutionJAXP, institution);
		convertMandantFieldsToEntity(institution);
		institution.setName(institutionJAXP.getName());
		institution.setStatus(institutionJAXP.getStatus());

		if (institutionJAXP.getMandant() != null
			&& institutionJAXP.getMandant().getId() != null) {
			final Mandant mandantFromDB = mandantService.getMandant(
				institutionJAXP.getMandant().getId()
			);

			// Mandant darf nicht vom Client ueberschrieben werden
			institution.setMandant(mandantFromDB);
		}

		// Traegerschaft ist nicht required!
		Traegerschaft traegerschaft = Optional.ofNullable(
			institutionJAXP.getTraegerschaft()
		)
			.map(JaxTraegerschaft::getId)
			.flatMap(id -> traegerschaftService.findTraegerschaft(id))
			.orElse(null);

		// Traegerschaft darf nicht vom Client ueberschrieben werden
		institution.setTraegerschaft(traegerschaft);

		return institution;
	}

	public JaxInstitutionListDTO institutionListDTOToJAX(
		final Entry<Institution, InstitutionStammdaten> entry
	) {
		final JaxInstitutionListDTO jaxInstitutionListDTO =
			new JaxInstitutionListDTO();
		convertAbstractVorgaengerFieldsToJAX(
			entry.getKey(),
			jaxInstitutionListDTO
		);
		jaxInstitutionListDTO.setName(entry.getKey().getName());
		Objects.requireNonNull(entry.getKey().getMandant());
		jaxInstitutionListDTO.setMandant(
			mandantToJAX(entry.getKey().getMandant())
		);
		jaxInstitutionListDTO.setStatus(entry.getKey().getStatus());

		if (entry.getKey().getTraegerschaft() != null) {
			jaxInstitutionListDTO.setTraegerschaft(
				traegerschaftLightToJAX(entry.getKey().getTraegerschaft())
			);
		}

		jaxInstitutionListDTO.setBetreuungsangebotTyp(
			entry.getValue().getBetreuungsangebotTyp()
		);

		Gemeinde gemeinde = null;
		if (entry.getValue().getInstitutionStammdatenTagesschule() != null) {
			gemeinde = entry.getValue()
				.getInstitutionStammdatenTagesschule()
				.getGemeinde();
		}
		if (entry.getValue().getInstitutionStammdatenFerieninsel() != null) {
			gemeinde = entry.getValue()
				.getInstitutionStammdatenFerieninsel()
				.getGemeinde();
		}

		if (gemeinde != null) {
			jaxInstitutionListDTO.setGemeinde(gemeindeToJAX(gemeinde));
		}

		return jaxInstitutionListDTO;
	}

	public boolean institutionToEntity(
		@Nonnull JaxInstitutionUpdate update,
		@Nonnull Institution institution,
		@Nonnull InstitutionStammdaten stammdaten
	) {
		boolean nameUpdated = updateName(update, institution);
		boolean traegerschaftUpdated = updateTraegerschaft(update, institution);
		boolean statusUpdated = updateStatus(institution, stammdaten);

		return nameUpdated || traegerschaftUpdated || statusUpdated;
	}

	/**
	 * @return TRUE when the name of the institution was updated
	 */
	private boolean updateName(
		@Nonnull JaxInstitutionUpdate update,
		@Nonnull Institution institution
	) {
		Optional<String> newName = update.getName()
			// we are only interrested in the value, when it is different
			.filter(name -> !institution.getName().equals(name));

		newName.ifPresent(institution::setName);

		return newName.isPresent();
	}

	/**
	 * @return TRUE when the Traegerschaft of the institution was updated
	 */
	private boolean updateTraegerschaft(
		@Nonnull JaxInstitutionUpdate update,
		@Nonnull Institution institution
	) {
		if (!getPrincipalBean().isCallerInAnyOfRole(
			UserRole.getMandantSuperadminRoles()
		)) {
			// only SUPER_ADMIN or Mandant may change Traegerschaft
			return false;
		}

		Traegerschaft newTraegerschaft = update.getTraegerschaftId()
			.flatMap(id -> traegerschaftService.findTraegerschaft(id))
			.orElse(null);

		if (!Objects.equals(institution.getTraegerschaft(), newTraegerschaft)) {
			institution.setTraegerschaft(newTraegerschaft);

			return true;
		}

		return false;
	}

	/**
	 * @return TRUE when the Status of the institution was updated
	 */
	private boolean updateStatus(
		@Nonnull Institution institution,
		@Nonnull InstitutionStammdaten stammdaten
	) {
		if (institution.getStatus() == InstitutionStatus.EINGELADEN
			||
			(institution.getStatus() == InstitutionStatus.KONFIGURATION
				&& stammdaten.isTagesschuleActivatable())
			||
			(institution.getStatus() == InstitutionStatus.KONFIGURATION
				&& stammdaten.getInstitutionStammdatenFerieninsel() != null)) {
			institution.setStatus(InstitutionStatus.AKTIV);
			return true;
		}

		return false;
	}

}
