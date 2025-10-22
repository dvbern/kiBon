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

package ch.dvbern.ebegu.api.converter.gemeinde;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.entities.Gemeinde;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxGemeindeConverter extends AbstractBaseConverter {
	@Nonnull
	public Gemeinde gemeindeToEntity(
		@Nonnull final JaxGemeinde jaxGemeinde,
		@Nonnull final Gemeinde gemeinde
	) {
		requireNonNull(gemeinde);
		requireNonNull(jaxGemeinde);
		requireNonNull(jaxGemeinde.getBetreuungsgutscheineStartdatum());
		requireNonNull(jaxGemeinde.getTagesschulanmeldungenStartdatum());
		requireNonNull(jaxGemeinde.getFerieninselanmeldungenStartdatum());
		convertAbstractFieldsToEntity(jaxGemeinde, gemeinde);
		convertMandantFieldsToEntity(gemeinde);
		gemeinde.setName(jaxGemeinde.getName());
		gemeinde.setStatus(jaxGemeinde.getStatus());
		gemeinde.setBfsNummer(jaxGemeinde.getBfsNummer());
		gemeinde.setBetreuungsgutscheineStartdatum(
			jaxGemeinde.getBetreuungsgutscheineStartdatum()
		);
		gemeinde.setTagesschulanmeldungenStartdatum(
			jaxGemeinde.getTagesschulanmeldungenStartdatum()
		);
		gemeinde.setFerieninselanmeldungenStartdatum(
			jaxGemeinde.getFerieninselanmeldungenStartdatum()
		);
		gemeinde.setGueltigBis(jaxGemeinde.getGueltigBis());
		gemeinde.setAngebotBG(jaxGemeinde.isAngebotBG());
		gemeinde.setAngebotBGTFO(jaxGemeinde.isAngebotBGTFO());
		gemeinde.setAngebotTS(jaxGemeinde.isAngebotTS());
		gemeinde.setAngebotFI(jaxGemeinde.isAngebotFI());
		gemeinde.setBesondereVolksschule(jaxGemeinde.isBesondereVolksschule());
		gemeinde.setNurLats(jaxGemeinde.isNurLats());
		gemeinde.setInfomaZahlungen(jaxGemeinde.getInfomaZahlungen());
		gemeinde.setAdminMutationAbweichungMeldungEnabled(
			jaxGemeinde.getAdminMutationAbweichungMeldungEnabled()
		);
		return gemeinde;
	}
}
