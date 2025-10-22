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

package ch.dvbern.ebegu.api.converter.gesuch.betreuung;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.converter.gesuch.JaxFachstelleConverter;
import ch.dvbern.ebegu.api.dtos.JaxErweiterteBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.FachstelleService;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxErweiterteBetreuungConverter extends AbstractBaseConverter {
	@Inject
	private JaxFachstelleConverter fachstelleConverter;
	@Inject
	private FachstelleService fachstelleService;

	private ErweiterteBetreuung erweiterteBetreuungToEntity(
		@Nonnull final JaxErweiterteBetreuung erweiterteBetreuungJAXP,
		@Nonnull final ErweiterteBetreuung erweiterteBetreuung
	) {

		requireNonNull(erweiterteBetreuung);
		requireNonNull(erweiterteBetreuungJAXP);

		convertAbstractVorgaengerFieldsToEntity(
			erweiterteBetreuungJAXP,
			erweiterteBetreuung
		);
		if (erweiterteBetreuungJAXP.getErweiterteBeduerfnisse() != null) {
			erweiterteBetreuung.setErweiterteBeduerfnisse(
				erweiterteBetreuungJAXP.getErweiterteBeduerfnisse()
			);
		}
		erweiterteBetreuung.setErweiterteBeduerfnisseBestaetigt(
			erweiterteBetreuungJAXP.isErweiterteBeduerfnisseBestaetigt()
		);
		erweiterteBetreuung.setKeineKesbPlatzierung(
			erweiterteBetreuungJAXP.getKeineKesbPlatzierung()
		);
		erweiterteBetreuung.setKitaPlusZuschlag(
			erweiterteBetreuungJAXP.getKitaPlusZuschlag()
		);
		erweiterteBetreuung.setKitaPlusZuschlagBestaetigt(
			erweiterteBetreuungJAXP.getKitaPlusZuschlagBestaetigt()
		);
		erweiterteBetreuung.setBetreuungInGemeinde(
			erweiterteBetreuungJAXP.getBetreuungInGemeinde()
		);
		erweiterteBetreuung.setErweitereteBeduerfnisseBetrag(
			erweiterteBetreuungJAXP.getErweitereteBeduerfnisseBetrag()
		);
		if (erweiterteBetreuungJAXP.getSprachfoerderungBestaetigt() != null) {
			erweiterteBetreuung.setSprachfoerderungBestaetigt(
				erweiterteBetreuungJAXP.getSprachfoerderungBestaetigt()
			);
		}

		// flag kann auf GUI auch Null sein, auf entity ist es defaultmässig false
		if (erweiterteBetreuungJAXP
			.getAnspruchFachstelleWennPensumUnterschritten()
			!= null) {
			erweiterteBetreuung.setAnspruchFachstelleWennPensumUnterschritten(
				erweiterteBetreuungJAXP
					.getAnspruchFachstelleWennPensumUnterschritten()
			);
		}

		//falls Erweiterte Beduerfnisse true ist, muss eine Fachstelle gesetzt sein
		if (Boolean.TRUE.equals(erweiterteBetreuung.getErweiterteBeduerfnisse())
			&& erweiterteBetreuungJAXP.getFachstelle() != null) {
			final Optional<Fachstelle> fachstelleFromDB =
				fachstelleService.findFachstelle(
					erweiterteBetreuungJAXP.getFachstelle().getId()
				);

			if (!fachstelleFromDB.isPresent()) {
				throw new EbeguEntityNotFoundException(
					"erweiterteBetreuungToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					erweiterteBetreuungJAXP.getFachstelle().getId()
				);
			}
			// Fachstelle darf nicht vom Client ueberschrieben werden
			erweiterteBetreuung.setFachstelle(fachstelleFromDB.get());
		}

		return erweiterteBetreuung;
	}

	@Nonnull
	public ErweiterteBetreuungContainer erweiterteBetreuungContainerToEntity(
		@Nonnull final JaxErweiterteBetreuungContainer containerJAX,
		@Nullable ErweiterteBetreuungContainer container
	) {
		requireNonNull(containerJAX);

		container = container == null ?
			new ErweiterteBetreuungContainer() :
			container;

		convertAbstractVorgaengerFieldsToEntity(containerJAX, container);

		if (containerJAX.getErweiterteBetreuungGS() != null) {
			ErweiterteBetreuung erwBetToMergeWith =
				Optional.ofNullable(container.getErweiterteBetreuungGS())
					.orElse(new ErweiterteBetreuung());
			container.setErweiterteBetreuungGS(
				erweiterteBetreuungToEntity(
					containerJAX.getErweiterteBetreuungGS(),
					erwBetToMergeWith
				)
			);
		}
		if (containerJAX.getErweiterteBetreuungJA() != null) {
			ErweiterteBetreuung erwBetToMergeWith =
				Optional.ofNullable(container.getErweiterteBetreuungJA())
					.orElse(new ErweiterteBetreuung());
			container.setErweiterteBetreuungJA(
				erweiterteBetreuungToEntity(
					containerJAX.getErweiterteBetreuungJA(),
					erwBetToMergeWith
				)
			);
		}
		return container;
	}

	@Nonnull
	protected JaxErweiterteBetreuungContainer erweiterteBetreuungContainerToJax(
		@Nonnull ErweiterteBetreuungContainer erweiterteBetreuungContainer
	) {

		JaxErweiterteBetreuungContainer jaxErweiterteBetreuungContainer =
			new JaxErweiterteBetreuungContainer();
		convertAbstractVorgaengerFieldsToJAX(
			erweiterteBetreuungContainer,
			jaxErweiterteBetreuungContainer
		);

		if (erweiterteBetreuungContainer.getErweiterteBetreuungGS() != null) {
			JaxErweiterteBetreuung jaxErweiterteBetreuung =
				erweiterteBetreuungToJax(
					erweiterteBetreuungContainer.getErweiterteBetreuungGS()
				);
			jaxErweiterteBetreuungContainer.setErweiterteBetreuungGS(
				jaxErweiterteBetreuung
			);
		}

		if (erweiterteBetreuungContainer.getErweiterteBetreuungJA() != null) {
			JaxErweiterteBetreuung jaxErweiterteBetreuung =
				erweiterteBetreuungToJax(
					erweiterteBetreuungContainer.getErweiterteBetreuungJA()
				);
			jaxErweiterteBetreuungContainer.setErweiterteBetreuungJA(
				jaxErweiterteBetreuung
			);
		}

		return jaxErweiterteBetreuungContainer;
	}

	@Nonnull
	private JaxErweiterteBetreuung erweiterteBetreuungToJax(
		@Nonnull ErweiterteBetreuung erweiterteBetreuung
	) {

		requireNonNull(
			erweiterteBetreuung,
			"Erweiterte Betreuung muss gesetzt sein"
		);

		JaxErweiterteBetreuung jaxErweiterteBetreuung =
			new JaxErweiterteBetreuung();
		convertAbstractVorgaengerFieldsToJAX(
			erweiterteBetreuung,
			jaxErweiterteBetreuung
		);
		jaxErweiterteBetreuung.setErweiterteBeduerfnisse(
			erweiterteBetreuung.getErweiterteBeduerfnisse()
		);
		jaxErweiterteBetreuung.setErweiterteBeduerfnisseBestaetigt(
			erweiterteBetreuung.isErweiterteBeduerfnisseBestaetigt()
		);
		jaxErweiterteBetreuung.setKeineKesbPlatzierung(
			erweiterteBetreuung.getKeineKesbPlatzierung()
		);
		jaxErweiterteBetreuung.setKitaPlusZuschlag(
			erweiterteBetreuung.getKitaPlusZuschlag()
		);
		jaxErweiterteBetreuung.setKitaPlusZuschlagBestaetigt(
			erweiterteBetreuung.isKitaPlusZuschlagBestaetigt()
		);
		jaxErweiterteBetreuung.setBetreuungInGemeinde(
			erweiterteBetreuung.getBetreuungInGemeinde()
		);
		jaxErweiterteBetreuung.setErweitereteBeduerfnisseBetrag(
			erweiterteBetreuung.getErweitereteBeduerfnisseBetrag()
		);
		jaxErweiterteBetreuung.setAnspruchFachstelleWennPensumUnterschritten(
			erweiterteBetreuung.isAnspruchFachstelleWennPensumUnterschritten()
		);
		jaxErweiterteBetreuung.setSprachfoerderungBestaetigt(
			erweiterteBetreuung.isSprachfoerderungBestaetigt()
		);

		if (erweiterteBetreuung.getFachstelle() != null) {
			jaxErweiterteBetreuung.setFachstelle(
				fachstelleConverter.fachstelleToJAX(
					erweiterteBetreuung.getFachstelle()
				)
			);
		}

		return jaxErweiterteBetreuung;
	}
}
