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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.dtos.JaxSozialhilfeZeitraum;
import ch.dvbern.ebegu.api.dtos.JaxSozialhilfeZeitraumContainer;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraum;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraumContainer;
import ch.dvbern.ebegu.services.SozialhilfeZeitraumService;
import ch.dvbern.ebegu.util.StreamsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class JaxSozialhilfeZeitraumConverter extends AbstractBaseConverter {
	private static final Logger LOGGER = LoggerFactory.getLogger(
		JaxSozialhilfeZeitraumConverter.class
	);
	@Inject
	private SozialhilfeZeitraumService sozialhilfeZeitraumService;

	protected void sozialhilfeZeitraumContainersToEntity(
		@Nonnull final List<JaxSozialhilfeZeitraumContainer> jaxShZContainers,
		@Nonnull final Collection<SozialhilfeZeitraumContainer> existingSozialhilfeZeitraeume
	) {
		final Set<SozialhilfeZeitraumContainer> transformedShZContainers =
			new HashSet<>();
		for (final JaxSozialhilfeZeitraumContainer jaxShZContainer : jaxShZContainers) {
			final SozialhilfeZeitraumContainer containerToMergeWith =
				existingSozialhilfeZeitraeume
					.stream()
					.filter(
						existingShZEntity -> existingShZEntity.getId()
							.equals(jaxShZContainer.getId())
					)
					.reduce(StreamsUtil.toOnlyElement())
					.orElse(new SozialhilfeZeitraumContainer());
			final SozialhilfeZeitraumContainer contToAdd =
				sozialhilfeZeitraumContainerToEntity(
					jaxShZContainer,
					containerToMergeWith
				);
			final boolean added = transformedShZContainers.add(contToAdd);
			if (!added) {
				LOGGER.warn(DROPPED_DUPLICATE_CONTAINER + "{}", contToAdd);
			}
		}

		existingSozialhilfeZeitraeume.clear();
		existingSozialhilfeZeitraeume.addAll(transformedShZContainers);
	}

	@Nonnull
	protected List<JaxSozialhilfeZeitraumContainer> sozialhilfeZeitraumContainersToJAX(
		@Nullable final Set<SozialhilfeZeitraumContainer> sozialhilfeZeitraumContainers
	) {
		if (sozialhilfeZeitraumContainers == null) {
			return Collections.emptyList();
		}

		return sozialhilfeZeitraumContainers.stream()
			.map(this::sozialhilfeZeitraumContainerToJAX)
			.collect(Collectors.toList());
	}

	@Nonnull
	public SozialhilfeZeitraumContainer sozialhilfeZeitraumContainerToStorableEntity(
		@Nonnull final JaxSozialhilfeZeitraumContainer jaxShZCont
	) {
		SozialhilfeZeitraumContainer containerToMergeWith =
			Optional.ofNullable(jaxShZCont.getId())
				.flatMap(sozialhilfeZeitraumService::findSozialhilfeZeitraum)
				.orElseGet(SozialhilfeZeitraumContainer::new);
		return sozialhilfeZeitraumContainerToEntity(
			jaxShZCont,
			containerToMergeWith
		);
	}

	@Nonnull
	public SozialhilfeZeitraumContainer sozialhilfeZeitraumContainerToEntity(
		@Nonnull final JaxSozialhilfeZeitraumContainer jaxShZCont,
		@Nonnull final SozialhilfeZeitraumContainer sozialhilfeZeitraumCont
	) {

		convertAbstractVorgaengerFieldsToEntity(
			jaxShZCont,
			sozialhilfeZeitraumCont
		);
		if (jaxShZCont.getSozialhilfeZeitraumGS() != null) {
			SozialhilfeZeitraum shzToMergeWith =
				Optional.ofNullable(
					sozialhilfeZeitraumCont.getSozialhilfeZeitraumGS()
				)
					.orElseGet(SozialhilfeZeitraum::new);
			SozialhilfeZeitraum sozialhilfeZeitraumGS =
				sozialhilfeZeitraumToEntity(
					jaxShZCont.getSozialhilfeZeitraumGS(),
					shzToMergeWith
				);
			sozialhilfeZeitraumCont.setSozialhilfeZeitraumGS(
				sozialhilfeZeitraumGS
			);
		}
		if (jaxShZCont.getSozialhilfeZeitraumJA() != null) {
			SozialhilfeZeitraum shzToMergeWith =
				Optional.ofNullable(
					sozialhilfeZeitraumCont.getSozialhilfeZeitraumJA()
				)
					.orElseGet(SozialhilfeZeitraum::new);
			SozialhilfeZeitraum sozialhilfeZeitraumJA =
				sozialhilfeZeitraumToEntity(
					jaxShZCont.getSozialhilfeZeitraumJA(),
					shzToMergeWith
				);
			sozialhilfeZeitraumCont.setSozialhilfeZeitraumJA(
				sozialhilfeZeitraumJA
			);
		}

		return sozialhilfeZeitraumCont;
	}

	@Nonnull
	public JaxSozialhilfeZeitraumContainer sozialhilfeZeitraumContainerToJAX(
		@Nonnull final SozialhilfeZeitraumContainer storedSozialhilfeZeitraumCont
	) {

		final JaxSozialhilfeZeitraumContainer jaxShZCont =
			new JaxSozialhilfeZeitraumContainer();
		convertAbstractVorgaengerFieldsToJAX(
			storedSozialhilfeZeitraumCont,
			jaxShZCont
		);
		jaxShZCont.setSozialhilfeZeitraumGS(
			sozialhilfeZeitraumToJax(
				storedSozialhilfeZeitraumCont.getSozialhilfeZeitraumGS()
			)
		);
		jaxShZCont.setSozialhilfeZeitraumJA(
			sozialhilfeZeitraumToJax(
				storedSozialhilfeZeitraumCont.getSozialhilfeZeitraumJA()
			)
		);

		return jaxShZCont;
	}

	@Nonnull
	private SozialhilfeZeitraum sozialhilfeZeitraumToEntity(
		@Nonnull final JaxSozialhilfeZeitraum jaxSozialhilfeZeitraum,
		@Nonnull final SozialhilfeZeitraum sozialhilfeZeitraum
	) {

		convertAbstractDateRangedFieldsToEntity(
			jaxSozialhilfeZeitraum,
			sozialhilfeZeitraum
		);

		return sozialhilfeZeitraum;
	}

	@Nullable
	private JaxSozialhilfeZeitraum sozialhilfeZeitraumToJax(
		@Nullable final SozialhilfeZeitraum sozialhilfeZeitraum
	) {
		if (sozialhilfeZeitraum == null) {
			return null;
		}
		JaxSozialhilfeZeitraum jaxSozialhilfeZeitraum =
			new JaxSozialhilfeZeitraum();
		convertAbstractDateRangedFieldsToJAX(
			sozialhilfeZeitraum,
			jaxSozialhilfeZeitraum
		);
		return jaxSozialhilfeZeitraum;
	}
}
