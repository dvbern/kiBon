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

package ch.dvbern.ebegu.rules.mutationsmerger;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.AbstractAbschlussRule;
import ch.dvbern.ebegu.rules.mutationsmerger.util.VerfuegungZeitabschnittSplitter;
import ch.dvbern.ebegu.rules.mutationsmerger.util.VorgaengerZeitabschnittFinder;

import static ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp.getBerechnetesAngebotTypes;

/**
 *
 * Bei untermonatigen Zeitabschnitten kann es vorkommen, dass ein Zeitabschnitt mehrere Vorgänger auf der
 * Vorgänger-Verfügung hat
 * Um das zu verhindern, werden die Zeitabschnitte so geschnitten, dass sie garantiert einen Vorgänger finden.
 *
 */
public final class OneVorgaengerEnsurer extends AbstractAbschlussRule {

	public OneVorgaengerEnsurer(
		boolean isDebug
	) {
		super(isDebug);
	}

	@Override
	protected List<BetreuungsangebotTyp> getApplicableAngebotTypes() {
		return getBerechnetesAngebotTypes();
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> execute(
		@Nonnull AbstractPlatz platz,
		@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte
	) {

		List<VerfuegungZeitabschnitt> split = new ArrayList<>();

		for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : zeitabschnitte) {
			final Verfuegung vorgaenger = platz.getVorgaengerVerfuegung();

			List<VerfuegungZeitabschnitt> allVorgaenger =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					verfuegungZeitabschnitt,
					vorgaenger
				);
			List<VerfuegungZeitabschnitt> zeitabschnittSplitOnVorgaenger =
				VerfuegungZeitabschnittSplitter.splitOn(
					verfuegungZeitabschnitt,
					allVorgaenger
				);

			split.addAll(zeitabschnittSplitOnVorgaenger);
		}
		return split;
	}
}
