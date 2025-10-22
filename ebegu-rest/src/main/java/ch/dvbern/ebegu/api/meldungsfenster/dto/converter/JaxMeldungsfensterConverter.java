/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.meldungsfenster.dto.converter;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.meldungsfenster.dto.JaxMeldungsfenster;
import ch.dvbern.ebegu.entities.meldungsfenster.Meldungsfenster;
import ch.dvbern.ebegu.types.DateTimeRange;

public class JaxMeldungsfensterConverter extends AbstractBaseConverter {

	public Meldungsfenster jaxMeldungsfensterToEntity(
		JaxMeldungsfenster jaxMeldungsfenster,
		Meldungsfenster meldungsfenster
	) {
		convertAbstractFieldsToEntity(jaxMeldungsfenster, meldungsfenster);
		meldungsfenster.setStatus(jaxMeldungsfenster.getStatus());
		meldungsfenster.setInhaltDe(jaxMeldungsfenster.getInhaltDe());
		meldungsfenster.setInhaltFr(jaxMeldungsfenster.getInhaltFr());
		meldungsfenster.setTitleDe(jaxMeldungsfenster.getTitleDe());
		meldungsfenster.setTitleFr(jaxMeldungsfenster.getTitleFr());
		meldungsfenster.setGueltigkeit(
			new DateTimeRange(
				jaxMeldungsfenster.getGueltigAb(),
				jaxMeldungsfenster.getGueltigBis()
			)
		);
		meldungsfenster.setZielgruppe(jaxMeldungsfenster.getZielgruppe());
		if (meldungsfenster.getMandant() == null) {
			meldungsfenster.setMandant(getPrincipalBean().getMandant());
		}
		return meldungsfenster;
	}

	public JaxMeldungsfenster meldungsfensterToJax(
		Meldungsfenster meldungsfenster
	) {
		JaxMeldungsfenster jaxMeldungsfenster = new JaxMeldungsfenster();
		convertAbstractFieldsToJAX(meldungsfenster, jaxMeldungsfenster);
		jaxMeldungsfenster.setZielgruppe(meldungsfenster.getZielgruppe());
		jaxMeldungsfenster.setStatus(meldungsfenster.getStatus());
		jaxMeldungsfenster.setInhaltDe(meldungsfenster.getInhaltDe());
		jaxMeldungsfenster.setInhaltFr(meldungsfenster.getInhaltFr());
		jaxMeldungsfenster.setTitleDe(meldungsfenster.getTitleDe());
		jaxMeldungsfenster.setTitleFr(meldungsfenster.getTitleFr());
		jaxMeldungsfenster.setGueltigAb(
			meldungsfenster.getGueltigkeit().getGueltigAb()
		);
		jaxMeldungsfenster.setGueltigBis(
			meldungsfenster.getGueltigkeit().getGueltigBis()
		);
		return jaxMeldungsfenster;
	}
}
