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

package ch.dvbern.ebegu.api.converter.gemeindeantrag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.converter.JaxBenutzerConverter;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxGemeindeAntrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.WithEinreichedatum;

@Dependent
public class JaxGemeindeAntragConverter extends AbstractBaseConverter {
	@Inject
	private JaxBenutzerConverter jaxBenutzerConverter;

	@Nonnull
	public List<JaxGemeindeAntrag> gemeindeAntragListToJax(
		@Nullable final List<GemeindeAntrag> gemeindeAntragList
	) {
		if (gemeindeAntragList == null) {
			return Collections.emptyList();
		}
		return gemeindeAntragList.stream()
			.map(this::gemeindeAntragToJax)
			.collect(Collectors.toCollection(ArrayList::new));
	}

	@Nonnull
	public JaxGemeindeAntrag gemeindeAntragToJax(
		@Nonnull final GemeindeAntrag gemeindeAntrag
	) {
		JaxGemeindeAntrag jaxGemeindeAntrag = new JaxGemeindeAntrag();

		jaxGemeindeAntrag.setTimestampErstellt(
			gemeindeAntrag.getTimestampErstellt()
		);
		jaxGemeindeAntrag.setTimestampMutiert(
			gemeindeAntrag.getTimestampMutiert()
		);
		jaxGemeindeAntrag.setId(gemeindeAntrag.getId());
		jaxGemeindeAntrag.setVersion(gemeindeAntrag.getVersion());

		jaxGemeindeAntrag.setGemeindeAntragTyp(
			gemeindeAntrag.getGemeindeAntragTyp()
		);
		jaxGemeindeAntrag.setGemeinde(
			gemeindeToJAX(gemeindeAntrag.getGemeinde())
		);
		jaxGemeindeAntrag.setGesuchsperiode(
			gesuchsperiodeToJAX(gemeindeAntrag.getGesuchsperiode())
		);
		jaxGemeindeAntrag.setStatusString(gemeindeAntrag.getStatusString());
		jaxGemeindeAntrag.setAntragAbgeschlossen(
			gemeindeAntrag.isAntragAbgeschlossen()
		);
		if (gemeindeAntrag instanceof WithEinreichedatum) {
			jaxGemeindeAntrag.setEinreichedatum(
				((WithEinreichedatum) gemeindeAntrag).getEinreichedatum()
			);
		}

		if (gemeindeAntrag.getVerantwortlicher() != null) {
			jaxGemeindeAntrag.setVerantwortlicher(
				jaxBenutzerConverter.benutzerToJaxBenutzerNoDetails(
					gemeindeAntrag.getVerantwortlicher()
				)
			);
		}

		return jaxGemeindeAntrag;
	}
}
