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

package ch.dvbern.ebegu.api.converter.gesuch;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.converter.JaxBenutzerConverter;
import ch.dvbern.ebegu.api.dtos.JaxAntragStatusHistory;
import ch.dvbern.ebegu.entities.AntragStatusHistory;
import ch.dvbern.ebegu.util.AntragStatusConverterUtil;

@Dependent
public class JaxAntragStatusHistoryConverter extends AbstractBaseConverter {
	@Inject
	private JaxBenutzerConverter jaxBenutzerConverter;

	public JaxAntragStatusHistory antragStatusHistoryToJAX(
		AntragStatusHistory antragStatusHistory
	) {
		final JaxAntragStatusHistory jaxAntragStatusHistory =
			convertAbstractVorgaengerFieldsToJAX(
				antragStatusHistory,
				new JaxAntragStatusHistory()
			);
		jaxAntragStatusHistory.setGesuchId(
			antragStatusHistory.getGesuch().getId()
		);
		jaxAntragStatusHistory.setStatus(
			AntragStatusConverterUtil.convertStatusToDTO(
				antragStatusHistory.getGesuch(),
				antragStatusHistory.getStatus()
			)
		);
		jaxAntragStatusHistory.setBenutzer(
			jaxBenutzerConverter.benutzerToJaxBenutzer(
				antragStatusHistory.getBenutzer()
			)
		);
		jaxAntragStatusHistory.setTimestampVon(
			antragStatusHistory.getTimestampVon()
		);
		jaxAntragStatusHistory.setTimestampBis(
			antragStatusHistory.getTimestampBis()
		);
		return jaxAntragStatusHistory;

	}

	@Nonnull
	public Collection<JaxAntragStatusHistory> antragStatusHistoryCollectionToJAX(
		@Nullable Collection<AntragStatusHistory> antragStatusHistoryCollection
	) {

		if (antragStatusHistoryCollection == null) {
			return Collections.emptyList();
		}

		return antragStatusHistoryCollection.stream()
			.map(this::antragStatusHistoryToJAX)
			.collect(Collectors.toList());
	}

}
