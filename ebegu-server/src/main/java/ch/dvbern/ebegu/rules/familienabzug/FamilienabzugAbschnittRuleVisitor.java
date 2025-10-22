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

import java.util.List;
import java.util.Locale;

import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.rules.AbstractAbschnittRule;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.KinderabzugTypVisitor;
import com.sun.istack.NotNull;

public class FamilienabzugAbschnittRuleVisitor implements
	KinderabzugTypVisitor<List<AbstractAbschnittRule>> {

	private final DateRange validityPeriod;
	private final Locale locale;

	public FamilienabzugAbschnittRuleVisitor(
		DateRange validityPeriod,
		Locale locale
	) {
		this.validityPeriod = validityPeriod;
		this.locale = locale;
	}

	public List<AbstractAbschnittRule> getFamilienabzugAbschnittRules(
		@NotNull KinderabzugTyp kinderabzugTyp
	) {
		return kinderabzugTyp.accept(this);
	}

	@Override
	public List<AbstractAbschnittRule> visitASIV() {
		return List.of(
			new KinderabzugAbschnittRuleASIV(validityPeriod, locale),
			new GesuchstellerAbzugAbschnittRule(validityPeriod, locale)
		);
	}

	@Override
	public List<AbstractAbschnittRule> visitFKJV() {
		return List.of(
			new KinderabzugAbschnittRuleFKJV(validityPeriod, locale),
			new GesuchstellerAbzugAbschnittRule(validityPeriod, locale)
		);
	}

	@Override
	public List<AbstractAbschnittRule> visitFKJV2() {
		return List.of(
			new KinderabzugAbschnittRuleFKJV(validityPeriod, locale),
			new GesuchstellerAbzugAbschnittRule(validityPeriod, locale)
		);
	}

	@Override
	public List<AbstractAbschnittRule> visitSchwyz() {
		return List.of(
			new KinderabzugAbschnittRuleSchwyz(validityPeriod, locale)
		);
	}

	@Override
	public List<AbstractAbschnittRule> visitKeine() {
		return List.of();
	}
}
