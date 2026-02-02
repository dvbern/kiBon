/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.kind;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.AbstractAbschnittRule;
import ch.dvbern.ebegu.rules.RuleKey;
import ch.dvbern.ebegu.rules.RuleType;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.util.Constants;

public class KindTerminiertAbschnittRule extends AbstractAbschnittRule {

	public KindTerminiertAbschnittRule(
		@Nonnull Locale locale
	) {
		super(
			RuleKey.KIND_ANSPRUCH,
			RuleType.GRUNDREGEL_DATA,
			RuleValidity.ASIV,
			Constants.DEFAULT_GUELTIGKEIT,
			locale
		);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(
		@Nonnull AbstractPlatz platz
	) {
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnitts =
			new ArrayList<>();
		if (platz.getKind().getKindJA().isGueltigkeitTerminiert()) {
			verfuegungZeitabschnitts.add(
				createKindTerminiertAbschnitt(platz.getKind().getKindJA())
			);
		}

		return verfuegungZeitabschnitts;
	}

	@Nonnull
	private VerfuegungZeitabschnitt createKindTerminiertAbschnitt(
		@Nonnull Kind kindJA
	) {
		Objects.requireNonNull(kindJA.getGueltigkeitTerminiertPer());

		LocalDate zeitabschnittKindTerminiertGuetltigAb = kindJA
			.getGueltigkeitTerminiertPer()
			.with(TemporalAdjusters.firstDayOfNextMonth());

		VerfuegungZeitabschnitt zeitabschnittTerminiert =
			createZeitabschnittWithinValidityPeriodOfRule(
				zeitabschnittKindTerminiertGuetltigAb,
				validTo()
			);
		zeitabschnittTerminiert.setKindTerminiert(true);
		return zeitabschnittTerminiert;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
	}
}
