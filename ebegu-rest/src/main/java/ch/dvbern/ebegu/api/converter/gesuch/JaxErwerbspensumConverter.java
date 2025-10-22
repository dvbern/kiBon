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

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.dtos.JaxErwerbspensum;
import ch.dvbern.ebegu.api.dtos.JaxErwerbspensumContainer;
import ch.dvbern.ebegu.api.dtos.JaxUnbezahlterUrlaub;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.UnbezahlterUrlaub;
import ch.dvbern.ebegu.services.ErwerbspensumService;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxErwerbspensumConverter extends AbstractBaseConverter {
	@Inject
	private ErwerbspensumService erwerbspensumService;

	public ErwerbspensumContainer erwerbspensumContainerToStoreableEntity(
		@Nonnull final JaxErwerbspensumContainer jaxEwpCont
	) {

		requireNonNull(jaxEwpCont);

		ErwerbspensumContainer containerToMergeWith = Optional.ofNullable(
			jaxEwpCont.getId()
		)
			.flatMap(erwerbspensumService::findErwerbspensum)
			.orElseGet(ErwerbspensumContainer::new);

		return erwerbspensumContainerToEntity(jaxEwpCont, containerToMergeWith);
	}

	public ErwerbspensumContainer erwerbspensumContainerToEntity(
		@Nonnull final JaxErwerbspensumContainer jaxEwpCont,
		@Nonnull final ErwerbspensumContainer erwerbspensumCont
	) {

		requireNonNull(jaxEwpCont);
		requireNonNull(erwerbspensumCont);

		convertAbstractVorgaengerFieldsToEntity(jaxEwpCont, erwerbspensumCont);
		if (jaxEwpCont.getErwerbspensumGS() != null) {
			Erwerbspensum pensumToMergeWith = Optional.ofNullable(
				erwerbspensumCont.getErwerbspensumGS()
			)
				.orElseGet(Erwerbspensum::new);
			Erwerbspensum erwerbspensumGS = erwerbspensumToEntity(
				jaxEwpCont.getErwerbspensumGS(),
				pensumToMergeWith
			);
			erwerbspensumCont.setErwerbspensumGS(erwerbspensumGS);
		}
		if (jaxEwpCont.getErwerbspensumJA() != null) {
			Erwerbspensum pensumToMergeWith = Optional.ofNullable(
				erwerbspensumCont.getErwerbspensumJA()
			)
				.orElseGet(Erwerbspensum::new);
			Erwerbspensum erwerbspensumJA = erwerbspensumToEntity(
				jaxEwpCont.getErwerbspensumJA(),
				pensumToMergeWith
			);
			erwerbspensumCont.setErwerbspensumJA(erwerbspensumJA);
		}

		return erwerbspensumCont;
	}

	@Nonnull
	public JaxErwerbspensumContainer erwerbspensumContainerToJAX(
		@Nonnull final ErwerbspensumContainer storedErwerbspensumCont
	) {

		requireNonNull(storedErwerbspensumCont);

		final JaxErwerbspensumContainer jaxEwpCont =
			new JaxErwerbspensumContainer();
		convertAbstractVorgaengerFieldsToJAX(
			storedErwerbspensumCont,
			jaxEwpCont
		);
		jaxEwpCont.setErwerbspensumGS(
			erbwerbspensumToJax(storedErwerbspensumCont.getErwerbspensumGS())
		);
		jaxEwpCont.setErwerbspensumJA(
			erbwerbspensumToJax(storedErwerbspensumCont.getErwerbspensumJA())
		);

		return jaxEwpCont;
	}

	private Erwerbspensum erwerbspensumToEntity(
		@Nonnull final JaxErwerbspensum jaxErwerbspensum,
		@Nonnull final Erwerbspensum erwerbspensum
	) {

		requireNonNull(jaxErwerbspensum);
		requireNonNull(erwerbspensum);

		convertAbstractPensumFieldsToEntity(jaxErwerbspensum, erwerbspensum);
		erwerbspensum.setTaetigkeit(jaxErwerbspensum.getTaetigkeit());
		erwerbspensum.setErwerbspensumInstitution(
			jaxErwerbspensum.getErwerbspensumInstitution()
		);
		erwerbspensum.setBezeichnung(jaxErwerbspensum.getBezeichnung());
		erwerbspensum.setUnregelmaessigeArbeitszeiten(
			jaxErwerbspensum.isUnregelmaessigeArbeitszeiten()
		);
		erwerbspensum.setWegzeit(jaxErwerbspensum.getWegzeit());

		if (jaxErwerbspensum.getUnbezahlterUrlaub() != null) {
			UnbezahlterUrlaub existingUrlaub = new UnbezahlterUrlaub();
			if (jaxErwerbspensum.getUnbezahlterUrlaub().getId() != null) {
				existingUrlaub = erwerbspensumService.findUnbezahlterUrlaub(
					jaxErwerbspensum.getUnbezahlterUrlaub().getId()
				)
					.orElse(new UnbezahlterUrlaub());
			}
			erwerbspensum.setUnbezahlterUrlaub(
				unbezahlterUrlaubToEntity(
					jaxErwerbspensum.getUnbezahlterUrlaub(),
					existingUrlaub
				)
			);
		} else {
			erwerbspensum.setUnbezahlterUrlaub(null);
		}

		return erwerbspensum;
	}

	@Nullable
	private JaxErwerbspensum erbwerbspensumToJax(
		@Nullable final Erwerbspensum pensum
	) {
		if (pensum == null) {
			return null;
		}
		JaxErwerbspensum jaxErwerbspensum = new JaxErwerbspensum();
		convertAbstractPensumFieldsToJAX(pensum, jaxErwerbspensum);
		jaxErwerbspensum.setTaetigkeit(pensum.getTaetigkeit());
		jaxErwerbspensum.setErwerbspensumInstitution(
			pensum.getErwerbspensumInstitution()
		);
		jaxErwerbspensum.setBezeichnung(pensum.getBezeichnung());
		jaxErwerbspensum.setUnbezahlterUrlaub(
			unbezahlterUrlaubToJax(pensum.getUnbezahlterUrlaub())
		);
		jaxErwerbspensum.setUnregelmaessigeArbeitszeiten(
			pensum.getUnregelmaessigeArbeitszeiten()
		);
		jaxErwerbspensum.setWegzeit(pensum.getWegzeit());
		return jaxErwerbspensum;
	}

	private UnbezahlterUrlaub unbezahlterUrlaubToEntity(
		@Nonnull final JaxUnbezahlterUrlaub jaxUrlaub,
		@Nonnull final UnbezahlterUrlaub urlaub
	) {

		requireNonNull(jaxUrlaub);
		requireNonNull(urlaub);
		convertAbstractDateRangedFieldsToEntity(jaxUrlaub, urlaub);
		return urlaub;
	}

	@Nullable
	private JaxUnbezahlterUrlaub unbezahlterUrlaubToJax(
		@Nullable final UnbezahlterUrlaub urlaub
	) {
		if (urlaub == null) {
			return null;
		}
		JaxUnbezahlterUrlaub jaxUrlaub = new JaxUnbezahlterUrlaub();
		convertAbstractDateRangedFieldsToJAX(urlaub, jaxUrlaub);
		return jaxUrlaub;
	}
}
