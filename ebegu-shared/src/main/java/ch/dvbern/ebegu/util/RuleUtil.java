/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.util;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;

public final class RuleUtil {

	private RuleUtil() {
	}

	/**
	 * Berechnet das Datum, ab wann eine Regel aufgrund es übergebenen Datums angewendet werden soll. Aktuell ist dies
	 * der erste
	 * Tag des Folgemonats. Auch bei Ereignis am 1. wird der 1. des Folgemonats genommen. Achtung, dieser Stichtag kommt
	 * nicht
	 * zwingend schlussendlich zum Einsatz, z.B. bei verspäteter Einreichung des Gesuchs.
	 */
	@Nonnull
	public static LocalDate getStichtagForEreignis(
		@Nonnull LocalDate ereignisdatum
	) {
		return ereignisdatum.plusMonths(1)
			.with(TemporalAdjusters.firstDayOfMonth());
	}

	@Nonnull
	public static LocalDate getFamSitAenderungPerDatum(
		@Nonnull Gesuch gesuch,
		@Nonnull LocalDate familiensituationAenderungPer
	) {
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Objects.requireNonNull(familiensituation);
		return EnumFamilienstatus.SCHWYZ.equals(
			familiensituation.getFamilienstatus()
		) && gesuch.getRegelStartDatum() != null ?
			gesuch.getRegelStartDatum() :
			familiensituationAenderungPer;
	}

	/**
	 * Prüft ob ein Kind an einem bestimmten Tag terminiert ist.
	 *
	 * Das Kind ist terminiert, ab Folgemonat nach dem Terminiert-Per Datum.
	 *
	 * e.g.
	 * Kind-Terminiert Per Datum: 31.10.2024 -> Terminiert Ab 01.11.2024
	 * Kind-Terminiert Per Datum: 15.10.2024 -> Terminiert Ab 01.11.2024
	 * Kind-Terminiert Per Datum: 01.10.2024 -> Terminiert Ab 01.11.2024
	 *
	 * Das Kind ist an einem bestimmten Tag terminiert, wenn dieser Tag nach dem Folgemonat
	 * Terminiert-Per-Datum liegt.
	 *
	 * e.g.
	 * Stichtag 15.10.2024 Kind Teminiert Per Datum: 15.10.2024 => false
	 * Stichtag 31.10.2024 Kind Teminiert Per Datum: 15.10.2024 => false
	 * Stichtag 01.11.2024 Kind Teminiert Per Datum: 15.10.2024 => true
	 */
	public static boolean isKindTerminiertAnStichtag(
		Kind kind,
		LocalDate stichtag
	) {
		if (!kind.isGueltigkeitTerminiert()
			|| kind.getGueltigkeitTerminiertPer() == null) {
			return false;
		}

		LocalDate folgeMonatNachTerminiert =
			kind.getGueltigkeitTerminiertPer()
				.with(TemporalAdjusters.firstDayOfNextMonth());
		return stichtag.isEqual(folgeMonatNachTerminiert)
			||
			stichtag.isAfter(folgeMonatNachTerminiert);
	}

}
