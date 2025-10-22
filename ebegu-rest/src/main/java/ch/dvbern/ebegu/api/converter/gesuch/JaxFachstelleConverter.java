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
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.dtos.JaxFachstelle;
import ch.dvbern.ebegu.api.dtos.JaxPensumFachstelle;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.FachstelleService;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxFachstelleConverter extends AbstractBaseConverter {
	@Inject
	private FachstelleService fachstelleService;

	public Fachstelle fachstelleToEntity(
		final JaxFachstelle fachstelleJAXP,
		final Fachstelle fachstelle
	) {
		requireNonNull(fachstelleJAXP);
		requireNonNull(fachstelle);
		convertAbstractVorgaengerFieldsToEntity(fachstelleJAXP, fachstelle);
		convertMandantFieldsToEntity(fachstelle);
		fachstelle.setName(fachstelleJAXP.getName());
		fachstelle.setFachstelleAnspruch(fachstelleJAXP.isFachstelleAnspruch());
		fachstelle.setFachstelleErweiterteBetreuung(
			fachstelleJAXP.isFachstelleErweiterteBetreuung()
		);
		return fachstelle;
	}

	public JaxFachstelle fachstelleToJAX(
		@Nonnull final Fachstelle persistedFachstelle
	) {
		final JaxFachstelle jaxFachstelle = new JaxFachstelle();
		convertAbstractVorgaengerFieldsToJAX(
			persistedFachstelle,
			jaxFachstelle
		);
		jaxFachstelle.setName(persistedFachstelle.getName());
		jaxFachstelle.setFachstelleAnspruch(
			persistedFachstelle.isFachstelleAnspruch()
		);
		jaxFachstelle.setFachstelleErweiterteBetreuung(
			persistedFachstelle.isFachstelleErweiterteBetreuung()
		);
		return jaxFachstelle;
	}

	public Collection<JaxPensumFachstelle> pensumFachstellenListToJax(
		final Set<PensumFachstelle> persistedPensumFachstellenList
	) {
		return persistedPensumFachstellenList.stream()
			.map(this::pensumFachstelleToJax)
			.collect(Collectors.toList());
	}

	@Nonnull
	public JaxPensumFachstelle pensumFachstelleToJax(
		PensumFachstelle pensumFachstelle
	) {
		final JaxPensumFachstelle jaxPensumFachstelle =
			new JaxPensumFachstelle();
		convertAbstractPensumFieldsToJAX(pensumFachstelle, jaxPensumFachstelle);
		if (pensumFachstelle.getFachstelle() != null) {
			jaxPensumFachstelle.setFachstelle(
				fachstelleToJAX(pensumFachstelle.getFachstelle())
			);
		}
		jaxPensumFachstelle.setIntegrationTyp(
			pensumFachstelle.getIntegrationTyp()
		);
		jaxPensumFachstelle.setGruendeZusatzleistung(
			pensumFachstelle.getGruendeZusatzleistung()
		);
		return jaxPensumFachstelle;
	}

	public PensumFachstelle jaxPensumFachstelleToEntity(
		final JaxPensumFachstelle pensumFachstelleJAXP,
		final PensumFachstelle pensumFachstelle
	) {
		convertAbstractPensumFieldsToEntity(
			pensumFachstelleJAXP,
			pensumFachstelle
		);

		if (pensumFachstelleJAXP.getFachstelle() != null
			&& pensumFachstelleJAXP.getFachstelle().getId() != null) {
			final Optional<Fachstelle> fachstelleFromDB =
				fachstelleService.findFachstelle(
					pensumFachstelleJAXP.getFachstelle().getId()
				);
			if (fachstelleFromDB.isPresent()) {
				// Fachstelle darf nicht vom Client ueberschrieben werden
				pensumFachstelle.setFachstelle(fachstelleFromDB.get());
			} else {
				throw new EbeguEntityNotFoundException(
					"pensumFachstelleToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					pensumFachstelleJAXP.getFachstelle()
						.getId()
				);
			}
		}
		pensumFachstelle.setIntegrationTyp(
			pensumFachstelleJAXP.getIntegrationTyp()
		);
		pensumFachstelle.setGruendeZusatzleistung(
			pensumFachstelleJAXP.getGruendeZusatzleistung()
		);

		return pensumFachstelle;
	}

	public void pensumFachstellenToEntity(
		final Kind kind,
		final Collection<JaxPensumFachstelle> pensumFsToSave
	) {
		final Set<PensumFachstelle> transformedKindPensumFachstellen =
			new TreeSet<>();
		for (JaxPensumFachstelle jaxPensumFachstelle : pensumFsToSave) {
			if (jaxPensumFachstelle.getId() != null) {
				final PensumFachstelle pensumFachstelleToMergeWith = kind
					.getPensumFachstelle()
					.stream()
					.filter(
						existingPensumFachstelle -> existingPensumFachstelle
							.getId()
							.equals(jaxPensumFachstelle.getId())
					)
					.findFirst()
					.orElseThrow(
						() -> new EbeguEntityNotFoundException(
							"toStorablePensumFachstelle",
							ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
							jaxPensumFachstelle.getId()
						)
					);
				transformedKindPensumFachstellen.add(
					jaxPensumFachstelleToEntity(
						jaxPensumFachstelle,
						pensumFachstelleToMergeWith
					)
				);
			} else {
				transformedKindPensumFachstellen.add(
					jaxPensumFachstelleToEntity(
						jaxPensumFachstelle,
						new PensumFachstelle(kind)
					)
				);
			}
		}
		kind.getPensumFachstelle().clear();
		kind.getPensumFachstelle().addAll(transformedKindPensumFachstellen);
	}
}
