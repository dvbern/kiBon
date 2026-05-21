/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.dtos.gemeindeantrag.ferienbetreuung.FerienbetreuungAngabenContainerStatusHistoryDTO;
import ch.dvbern.ebegu.entities.gemeindeantrag.ferienbetreuung.FerienbetreuungAngabenContainerStatusHistory;

@Dependent
public class JaxFerienbetreuungStatusHistoryConverter extends
	AbstractBaseConverter {

	@Inject
	private JaxBenutzerConverter jaxBenutzerConverter;

	public List<FerienbetreuungAngabenContainerStatusHistoryDTO> statusHistoryListToDTO(
		List<FerienbetreuungAngabenContainerStatusHistory> historyList
	) {
		return historyList.stream()
			.map(
				this::toDTO
			)
			.collect(Collectors.toList());
	}

	private FerienbetreuungAngabenContainerStatusHistoryDTO toDTO(
		FerienbetreuungAngabenContainerStatusHistory history
	) {
		return FerienbetreuungAngabenContainerStatusHistoryDTO.builder()
			.benutzer(
				jaxBenutzerConverter.benutzerToJaxBenutzer(
					history.getBenutzer()
				)
			)
			.status(history.getStatus())
			.timestampBis(history.getTimestampBis())
			.timestampVon(history.getTimestampVon())
			.build();
	}
}
