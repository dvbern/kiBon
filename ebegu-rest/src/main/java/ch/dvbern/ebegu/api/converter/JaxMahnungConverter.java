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

package ch.dvbern.ebegu.api.converter;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.gesuch.JaxAntragConverter;
import ch.dvbern.ebegu.api.dtos.JaxMahnung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.GesuchService;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxMahnungConverter extends AbstractBaseConverter {
	@Inject
	private GesuchService gesuchService;
	@Inject
	private JaxAntragConverter antragConverter;

	public Mahnung mahnungToEntity(
		@Nonnull final JaxMahnung jaxMahnung,
		@Nonnull final Mahnung mahnung
	) {
		requireNonNull(mahnung);
		requireNonNull(jaxMahnung);
		requireNonNull(jaxMahnung.getGesuch());
		requireNonNull(jaxMahnung.getGesuch().getId());

		convertAbstractVorgaengerFieldsToEntity(jaxMahnung, mahnung);

		Gesuch gesuchFromDB = gesuchService.findGesuch(
			jaxMahnung.getGesuch().getId()
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"mahnungToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					jaxMahnung.getGesuch()
				)
			);

		// hier laden wir das Gesuch aus der db aber konvertieren die Gesuchsdaten vom Client NICHT
		mahnung.setGesuch(gesuchFromDB);

		mahnung.setMahnungTyp(jaxMahnung.getMahnungTyp());
		mahnung.setDatumFristablauf(jaxMahnung.getDatumFristablauf());
		mahnung.setBemerkungen(jaxMahnung.getBemerkungen());
		mahnung.setTimestampAbgeschlossen(
			jaxMahnung.getTimestampAbgeschlossen()
		);
		mahnung.setAbgelaufen(jaxMahnung.getAbgelaufen());

		return mahnung;
	}

	public JaxMahnung mahnungToJAX(@Nonnull final Mahnung persistedMahnung) {
		final JaxMahnung jaxMahnung = new JaxMahnung();
		convertAbstractVorgaengerFieldsToJAX(persistedMahnung, jaxMahnung);

		jaxMahnung.setGesuch(
			antragConverter.gesuchToJAX(persistedMahnung.getGesuch())
		);
		jaxMahnung.setMahnungTyp(persistedMahnung.getMahnungTyp());
		jaxMahnung.setDatumFristablauf(persistedMahnung.getDatumFristablauf());
		jaxMahnung.setBemerkungen(persistedMahnung.getBemerkungen());
		jaxMahnung.setTimestampAbgeschlossen(
			persistedMahnung.getTimestampAbgeschlossen()
		);
		jaxMahnung.setAbgelaufen(persistedMahnung.getAbgelaufen());

		return jaxMahnung;
	}
}
