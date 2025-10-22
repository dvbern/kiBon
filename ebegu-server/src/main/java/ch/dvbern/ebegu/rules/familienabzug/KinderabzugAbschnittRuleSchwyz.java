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

package ch.dvbern.ebegu.rules.familienabzug;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.types.DateRange;

public class KinderabzugAbschnittRuleSchwyz extends
	AbstractKinderabzugAbschnittRule {

	protected KinderabzugAbschnittRuleSchwyz(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		super(validityPeriod, locale);
	}

	@Override
	protected Kinderabzug calculateKinderAbzug(
		KindContainer kindContainer,
		LocalDate stichtag
	) {
		Kinderabzug kinderabzug = kindContainer.getKindJA()
			.getKinderabzugErstesHalbjahr();
		Objects.requireNonNull(kinderabzug);
		return kinderabzug;
	}
}
