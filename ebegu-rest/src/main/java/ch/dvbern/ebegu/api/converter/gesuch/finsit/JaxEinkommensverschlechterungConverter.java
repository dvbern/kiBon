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

package ch.dvbern.ebegu.api.converter.gesuch.finsit;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterung;
import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterungContainer;
import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterungInfo;
import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.services.EinkommensverschlechterungService;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxEinkommensverschlechterungConverter extends
	AbstractBaseFinanzielleSituationConverter {
	@Inject
	private EinkommensverschlechterungService einkommensverschlechterungService;

	public EinkommensverschlechterungContainer einkommensverschlechterungContainerToStorableEntity(
		@Nonnull final JaxEinkommensverschlechterungContainer containerJAX
	) {

		requireNonNull(containerJAX);

		EinkommensverschlechterungContainer containerToMergeWith =
			new EinkommensverschlechterungContainer();
		if (containerJAX.getId() != null) {
			final Optional<EinkommensverschlechterungContainer> existingEkvC =
				einkommensverschlechterungService
					.findEinkommensverschlechterungContainer(
						containerJAX.getId()
					);
			if (existingEkvC.isPresent()) {
				containerToMergeWith = existingEkvC.get();
			}
		}

		return einkommensverschlechterungContainerToEntity(
			containerJAX,
			containerToMergeWith
		);
	}

	public JaxEinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainerToJAX(
		final EinkommensverschlechterungInfoContainer persistedEinkommensverschlechterungInfo
	) {
		final JaxEinkommensverschlechterungInfoContainer jaxEkvic =
			new JaxEinkommensverschlechterungInfoContainer();
		convertAbstractVorgaengerFieldsToJAX(
			persistedEinkommensverschlechterungInfo,
			jaxEkvic
		);
		if (persistedEinkommensverschlechterungInfo
			.getEinkommensverschlechterungInfoGS()
			!= null) {
			jaxEkvic.setEinkommensverschlechterungInfoGS(
				einkommensverschlechterungInfoToJAX(
					persistedEinkommensverschlechterungInfo
						.getEinkommensverschlechterungInfoGS()
				)
			);
		}
		jaxEkvic.setEinkommensverschlechterungInfoJA(
			einkommensverschlechterungInfoToJAX(
				persistedEinkommensverschlechterungInfo
					.getEinkommensverschlechterungInfoJA()
			)
		);
		return jaxEkvic;
	}

	public EinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainerToEntity(
		@Nonnull final JaxEinkommensverschlechterungInfoContainer containerJAX,
		@Nonnull final EinkommensverschlechterungInfoContainer container
	) {

		requireNonNull(container);
		requireNonNull(containerJAX);

		convertAbstractVorgaengerFieldsToEntity(containerJAX, container);
		EinkommensverschlechterungInfo evkInfoToMergeWith;
		//Im moment kann eine einmal gespeicherte Finanzielle Situation nicht mehr entfernt werden.
		if (containerJAX.getEinkommensverschlechterungInfoGS() != null) {
			evkInfoToMergeWith = Optional.ofNullable(
				container.getEinkommensverschlechterungInfoGS()
			)
				.orElseGet(EinkommensverschlechterungInfo::new);
			container.setEinkommensverschlechterungInfoGS(
				einkommensverschlechterungInfoToEntity(
					containerJAX.getEinkommensverschlechterungInfoGS(),
					evkInfoToMergeWith
				)
			);
		}
		if (containerJAX.getEinkommensverschlechterungInfoJA() != null) {
			evkInfoToMergeWith = Optional.of(
				container.getEinkommensverschlechterungInfoJA()
			)
				.orElseGet(EinkommensverschlechterungInfo::new);
			container.setEinkommensverschlechterungInfoJA(
				einkommensverschlechterungInfoToEntity(
					containerJAX.getEinkommensverschlechterungInfoJA(),
					evkInfoToMergeWith
				)
			);
		}
		return container;
	}

	public EinkommensverschlechterungInfo einkommensverschlechterungInfoToEntity(
		@Nonnull final JaxEinkommensverschlechterungInfo einkommensverschlechterungInfoJAXP,
		@Nonnull final EinkommensverschlechterungInfo einkommensverschlechterungInfo
	) {

		requireNonNull(einkommensverschlechterungInfo);
		requireNonNull(einkommensverschlechterungInfoJAXP);

		convertAbstractVorgaengerFieldsToEntity(
			einkommensverschlechterungInfoJAXP,
			einkommensverschlechterungInfo
		);
		einkommensverschlechterungInfo.setEinkommensverschlechterung(
			einkommensverschlechterungInfoJAXP.getEinkommensverschlechterung()
		);
		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus1(
			einkommensverschlechterungInfoJAXP.getEkvFuerBasisJahrPlus1()
		);
		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus2(
			einkommensverschlechterungInfoJAXP.getEkvFuerBasisJahrPlus2()
		);
		einkommensverschlechterungInfo.setEkvBasisJahrPlus1Annulliert(
			einkommensverschlechterungInfoJAXP.getEkvBasisJahrPlus1Annulliert()
		);
		einkommensverschlechterungInfo.setEkvBasisJahrPlus2Annulliert(
			einkommensverschlechterungInfoJAXP.getEkvBasisJahrPlus2Annulliert()
		);
		return einkommensverschlechterungInfo;
	}

	public JaxEinkommensverschlechterungInfo einkommensverschlechterungInfoToJAX(
		@Nonnull final EinkommensverschlechterungInfo persistedEinkommensverschlechterungInfo
	) {

		final JaxEinkommensverschlechterungInfo ekvi =
			new JaxEinkommensverschlechterungInfo();
		convertAbstractVorgaengerFieldsToJAX(
			persistedEinkommensverschlechterungInfo,
			ekvi
		);

		ekvi.setEinkommensverschlechterung(
			persistedEinkommensverschlechterungInfo
				.getEinkommensverschlechterung()
		);
		ekvi.setEkvFuerBasisJahrPlus1(
			persistedEinkommensverschlechterungInfo.getEkvFuerBasisJahrPlus1()
		);
		ekvi.setEkvFuerBasisJahrPlus2(
			persistedEinkommensverschlechterungInfo.getEkvFuerBasisJahrPlus2()
		);
		ekvi.setEkvBasisJahrPlus1Annulliert(
			persistedEinkommensverschlechterungInfo
				.getEkvBasisJahrPlus1Annulliert()
		);
		ekvi.setEkvBasisJahrPlus2Annulliert(
			persistedEinkommensverschlechterungInfo
				.getEkvBasisJahrPlus2Annulliert()
		);

		return ekvi;
	}

	public EinkommensverschlechterungContainer einkommensverschlechterungContainerToEntity(
		@Nonnull final JaxEinkommensverschlechterungContainer containerJAX,
		@Nonnull final EinkommensverschlechterungContainer container
	) {
		requireNonNull(container);
		requireNonNull(containerJAX);
		convertAbstractVorgaengerFieldsToEntity(containerJAX, container);

		Einkommensverschlechterung einkommensverschlechterung;

		if (containerJAX.getEkvGSBasisJahrPlus1() != null) {
			einkommensverschlechterung = Optional.ofNullable(
				container.getEkvGSBasisJahrPlus1()
			)
				.orElseGet(Einkommensverschlechterung::new);
			container.setEkvGSBasisJahrPlus1(
				einkommensverschlechterungToEntity(
					containerJAX.getEkvGSBasisJahrPlus1(),
					einkommensverschlechterung
				)
			);
		}
		if (containerJAX.getEkvGSBasisJahrPlus2() != null) {
			einkommensverschlechterung = Optional.ofNullable(
				container.getEkvGSBasisJahrPlus2()
			)
				.orElseGet(Einkommensverschlechterung::new);
			container.setEkvGSBasisJahrPlus2(
				einkommensverschlechterungToEntity(
					containerJAX.getEkvGSBasisJahrPlus2(),
					einkommensverschlechterung
				)
			);
		}
		if (containerJAX.getEkvJABasisJahrPlus1() != null) {
			einkommensverschlechterung = Optional.ofNullable(
				container.getEkvJABasisJahrPlus1()
			)
				.orElseGet(Einkommensverschlechterung::new);
			container.setEkvJABasisJahrPlus1(
				einkommensverschlechterungToEntity(
					containerJAX.getEkvJABasisJahrPlus1(),
					einkommensverschlechterung
				)
			);
		}
		if (containerJAX.getEkvJABasisJahrPlus2() != null) {
			einkommensverschlechterung = Optional.ofNullable(
				container.getEkvJABasisJahrPlus2()
			)
				.orElseGet(Einkommensverschlechterung::new);
			container.setEkvJABasisJahrPlus2(
				einkommensverschlechterungToEntity(
					containerJAX.getEkvJABasisJahrPlus2(),
					einkommensverschlechterung
				)
			);
		}

		return container;
	}

	@Nullable
	public JaxEinkommensverschlechterungContainer einkommensverschlechterungContainerToJAX(
		@Nullable final EinkommensverschlechterungContainer persistedEkv
	) {

		if (persistedEkv == null) {
			return null;
		}

		final JaxEinkommensverschlechterungContainer evsc =
			new JaxEinkommensverschlechterungContainer();
		convertAbstractVorgaengerFieldsToJAX(persistedEkv, evsc);
		evsc.setEkvGSBasisJahrPlus1(
			einkommensverschlechterungToJAX(
				persistedEkv.getEkvGSBasisJahrPlus1()
			)
		);
		evsc.setEkvGSBasisJahrPlus2(
			einkommensverschlechterungToJAX(
				persistedEkv.getEkvGSBasisJahrPlus2()
			)
		);
		evsc.setEkvJABasisJahrPlus1(
			einkommensverschlechterungToJAX(
				persistedEkv.getEkvJABasisJahrPlus1()
			)
		);
		evsc.setEkvJABasisJahrPlus2(
			einkommensverschlechterungToJAX(
				persistedEkv.getEkvJABasisJahrPlus2()
			)
		);

		return evsc;
	}

	private Einkommensverschlechterung einkommensverschlechterungToEntity(
		@Nonnull final JaxEinkommensverschlechterung einkommensverschlechterungJAXP,
		@Nonnull final Einkommensverschlechterung einkommensverschlechterung
	) {

		requireNonNull(einkommensverschlechterung);
		requireNonNull(einkommensverschlechterungJAXP);

		abstractFinanzielleSituationToEntity(
			einkommensverschlechterungJAXP,
			einkommensverschlechterung
		);
		einkommensverschlechterung.setBruttolohnAbrechnung1(
			einkommensverschlechterungJAXP.getBruttolohnAbrechnung1()
		);
		einkommensverschlechterung.setBruttolohnAbrechnung2(
			einkommensverschlechterungJAXP.getBruttolohnAbrechnung2()
		);
		einkommensverschlechterung.setBruttolohnAbrechnung3(
			einkommensverschlechterungJAXP.getBruttolohnAbrechnung3()
		);
		einkommensverschlechterung.setExtraLohn(
			einkommensverschlechterungJAXP.getExtraLohn()
		);

		return einkommensverschlechterung;
	}

	@Nullable
	private JaxEinkommensverschlechterung einkommensverschlechterungToJAX(
		@Nullable final Einkommensverschlechterung persistedEinkommensverschlechterung
	) {

		if (persistedEinkommensverschlechterung == null) {
			return null;
		}

		JaxEinkommensverschlechterung eikvs =
			new JaxEinkommensverschlechterung();

		abstractFinanzielleSituationToJAX(
			persistedEinkommensverschlechterung,
			eikvs
		);
		eikvs.setBruttolohnAbrechnung1(
			persistedEinkommensverschlechterung.getBruttolohnAbrechnung1()
		);
		eikvs.setBruttolohnAbrechnung2(
			persistedEinkommensverschlechterung.getBruttolohnAbrechnung2()
		);
		eikvs.setBruttolohnAbrechnung3(
			persistedEinkommensverschlechterung.getBruttolohnAbrechnung3()
		);
		eikvs.setExtraLohn(persistedEinkommensverschlechterung.getExtraLohn());

		return eikvs;
	}
}
