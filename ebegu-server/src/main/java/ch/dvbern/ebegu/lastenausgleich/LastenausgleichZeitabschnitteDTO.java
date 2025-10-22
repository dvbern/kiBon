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

package ch.dvbern.ebegu.lastenausgleich;

import java.math.BigDecimal;

import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import lombok.Getter;

@Getter
public class LastenausgleichZeitabschnitteDTO {

	private String verfuegungZeitabschnittId;

	private DateRange verfuegungZeitabschnittGueltigkeit;

	private BigDecimal pensumProzent;

	private BigDecimal verguenstigung;

	private Boolean keinSelbstbehaltDurchGemeinde;

	public LastenausgleichZeitabschnitteDTO(
		String verfuegungZeitabschnittId,
		DateRange verfuegungZeitabschnittGueltigkeit,
		BigDecimal betreuungsPensumProzent,
		int anspruchPensumProzent,
		BigDecimal verguenstigung,
		Boolean keinSelbstbehaltDurchGemeinde
	) {
		this.verfuegungZeitabschnittId = verfuegungZeitabschnittId;
		this.verfuegungZeitabschnittGueltigkeit =
			verfuegungZeitabschnittGueltigkeit;
		this.pensumProzent = betreuungsPensumProzent.min(
			MathUtil.DEFAULT.from(anspruchPensumProzent)
		);
		this.verguenstigung = verguenstigung;
		this.keinSelbstbehaltDurchGemeinde = keinSelbstbehaltDurchGemeinde;
	}
}
