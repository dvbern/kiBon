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

package ch.dvbern.ebegu.familiensituation;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class FamiliensituationUtil {

	public static boolean isGemeinsameSteuererklaerung(@Nonnull Gesuch gesuch) {
		final Familiensituation familiensituation = gesuch
			.extractFamiliensituation();

		return familiensituation != null
			&&
			familiensituation.getGemeinsameSteuererklaerung() != null
			&&
			familiensituation.getGemeinsameSteuererklaerung();
	}

	/**
	 * Determines whether the given familiensituation represents a specific case of
	 * "Konkubinat ohne Kind" where the Konkubinat becomes a certain number of years old.
	 * The number of years is defined by the parameter paramMinDauerKonkubinat.
	 *
	 * @param familiensituation the {@link Familiensituation} to check
	 * @param gesuch the {@link Gesuch} of the {@link Familiensituation} containing the {@link Gesuchsperiode}
	 * @param paramMinDauerKonkubinat the duration (in years) to qualify as a special case
	 * @return true if the {@link Familiensituation} satisfies the special case criteria, otherwise false
	 */
	public static boolean isKonkubinatOhneKindSpezialfall(
		Familiensituation familiensituation,
		Gesuch gesuch,
		int paramMinDauerKonkubinat
	) {
		return familiensituation.getFamilienstatus()
			== EnumFamilienstatus.KONKUBINAT_KEIN_KIND
			&& familiensituation.getStartKonkubinat() != null
			&& gesuch.getGesuchsperiode()
				.getGueltigkeit()
				.contains(
					familiensituation.getStartKonkubinat()
						.plusYears(paramMinDauerKonkubinat)
				);
	}
}
