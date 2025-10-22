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

package ch.dvbern.ebegu.api.converter;

import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;

import ch.dvbern.ebegu.api.dtos.JaxLastenausgleich;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguFingerWegException;

@Dependent
public class JaxLastenausgleichConverter extends AbstractBaseConverter {
	@Nullable
	public JaxLastenausgleich lastenausgleichToJAX(
		@Nullable final Lastenausgleich persistedLastenausgleich
	) {
		if (persistedLastenausgleich == null) {
			return null;
		}
		JaxLastenausgleich jaxLastenausgleich = new JaxLastenausgleich();
		convertAbstractFieldsToJAX(
			persistedLastenausgleich,
			jaxLastenausgleich
		);
		jaxLastenausgleich.setJahr(persistedLastenausgleich.getJahr());
		jaxLastenausgleich.setTotalAlleGemeinden(
			persistedLastenausgleich.getTotalAlleGemeinden()
		);

		return jaxLastenausgleich;
	}

	public void lastenausgleichGrundlagenToEntity() {
		throw new EbeguFingerWegException(
			"lastenausgleichGrundlagenToEntity",
			ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE
		);
	}

	public void lastenausgleichGrundlagenToJAX() {
		throw new EbeguFingerWegException(
			"lastenausgleichGrundlagenToJAX",
			ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE
		);
	}

	public void lastenausgleichDetailListToEntity() {
		throw new EbeguFingerWegException(
			"lastenausgleichDetailListToEntity",
			ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE
		);
	}

	public void lastenausgleichDetailListToJax() {
		throw new EbeguFingerWegException(
			"lastenausgleichDetailListToJax",
			ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE
		);
	}

	public void lastenausgleichDetailToEntity() {
		throw new EbeguFingerWegException(
			"lastenausgleichDetailToEntity",
			ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE
		);
	}

	public void lastenausgleichDetailToJAX() {
		throw new EbeguFingerWegException(
			"lastenausgleichDetailToJAX",
			ErrorCodeEnum.ERROR_OBJECT_IS_IMMUTABLE
		);
	}

}
