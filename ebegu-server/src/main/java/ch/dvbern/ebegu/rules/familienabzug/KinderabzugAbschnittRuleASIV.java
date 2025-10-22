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
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Umsetzung der ASIV Revision
 * <p>
 * Gem. neuer ASIV Verordnung müssen die Kinder für die Berechnung der Familiengrösse ab dem Beginn den Monats NACH dem
 * Ereigniseintritt (e.g. Geburt) berücksichtigt werden.
 */
@SuppressWarnings("MethodParameterNamingConvention")
public class KinderabzugAbschnittRuleASIV extends
	AbstractKinderabzugAbschnittRule {

	public KinderabzugAbschnittRuleASIV(
		DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		super(validityPeriod, locale);
	}

	@Override
	protected List<DateRange> createZeitabschnittGueltigkeitenBasisForKind(
		Gesuchsperiode gesuchsperiode
	) {
		// Für ASIV müssen zwei Zeitabschnitte pro halbes Jahr erstellt werden,
		// da zwei Kinder-Abzug Fragen existieren eine für das erste Halbjahr und
		// eine für das zweite Halbjahr
		DateRange halbjahr1 = new DateRange(
			gesuchsperiode.getGueltigkeit().getGueltigAb(),
			gesuchsperiode.getGueltigkeit()
				.getGueltigAb()
				.with(TemporalAdjusters.lastDayOfYear())
		);

		DateRange halbjahr2 = new DateRange(
			gesuchsperiode.getGueltigkeit()
				.getGueltigBis()
				.with(TemporalAdjusters.firstDayOfYear()),
			gesuchsperiode.getGueltigkeit().getGueltigBis()
		);
		return List.of(halbjahr1, halbjahr2);
	}

	@Override
	protected Kinderabzug calculateKinderAbzug(
		KindContainer kindContainer,
		LocalDate stichtag
	) {
		boolean isErstesHalbjahr = kindContainer.getGesuch()
			.getGesuchsperiode()
			.getBasisJahrPlus1()
			== stichtag.getYear();
		Kinderabzug kinderabzug =
			isErstesHalbjahr ?
				kindContainer.getKindJA().getKinderabzugErstesHalbjahr() :
				kindContainer.getKindJA().getKinderabzugZweitesHalbjahr();
		Objects.requireNonNull(kinderabzug);
		return kinderabzug;
	}
}
