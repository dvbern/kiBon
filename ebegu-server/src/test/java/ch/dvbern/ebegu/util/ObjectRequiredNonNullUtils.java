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

package ch.dvbern.ebegu.util;

import java.util.Objects;

import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituationSelbstdeklaration;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;

public final class ObjectRequiredNonNullUtils {

	private ObjectRequiredNonNullUtils() {
	}

	public static Familiensituation getFamiliensituationJA(
		FamiliensituationContainer famSitContainer
	) {
		return Objects.requireNonNull(famSitContainer.getFamiliensituationJA());
	}

	public static Familiensituation getFamiliensituationJA(Gesuch gesuch) {
		return getFamiliensituationJA(
			Objects.requireNonNull(gesuch.getFamiliensituationContainer())
		);
	}

	public static FinanzielleSituation getFinanzielleSituationJA(
		FinanzielleSituationContainer finanzielleSituationContainer
	) {
		return Objects.requireNonNull(
			finanzielleSituationContainer
				.getFinSitJA()
		);
	}

	public static FinanzielleSituation getFinanzielleSituationJA(
		GesuchstellerContainer gesuchstellerContainer
	) {
		return getFinanzielleSituationJA(
			Objects.requireNonNull(
				Objects.requireNonNull(gesuchstellerContainer)
					.getFinanzielleSituationContainer()
			)
		);
	}

	public static FinanzielleSituationContainer getFinanzielleSituationContainer(
		GesuchstellerContainer gesuchstellerContainer
	) {
		return Objects.requireNonNull(
			Objects.requireNonNull(gesuchstellerContainer)
				.getFinanzielleSituationContainer()
		);
	}

	public static EinkommensverschlechterungContainer getEinkommensverschlechterungContainer(
		GesuchstellerContainer gesuchstellerContainer
	) {
		return Objects.requireNonNull(
			gesuchstellerContainer
				.getEinkommensverschlechterungContainer()
		);
	}

	public static Einkommensverschlechterung getEkvJABasisJahrPlus1(
		GesuchstellerContainer gesuchstellerContainer
	) {
		return Objects.requireNonNull(
			getEinkommensverschlechterungContainer(gesuchstellerContainer)
				.getEkvJABasisJahrPlus1()
		);
	}

	public static FinanzielleSituationSelbstdeklaration getEkvJABasisJahrPlus1Selbstdeklaration(
		GesuchstellerContainer gesuchstellerContainer
	) {
		return Objects.requireNonNull(
			getEkvJABasisJahrPlus1(gesuchstellerContainer)
				.getSelbstdeklaration()
		);
	}
}
