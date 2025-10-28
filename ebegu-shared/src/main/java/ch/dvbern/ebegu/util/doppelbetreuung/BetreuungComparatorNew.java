/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.util.doppelbetreuung;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.util.BetreuungspensumContainerComparator;

/**
 * Comparator, der die Betreuungen nach folgender Regel sortiert:
 * 1. Die Kita mit dem früherem Startdatum wird zuerst berücksichtigt.
 * 2. Falls beide Angebote dasselbe Startdatum haben, wird die Kita mit dem höheren Pensum berücksichtigt.
 * 3. Falls beide Angebote dasselbe Startdatum und dasselbe Pensum haben, wird die Kita zuerst berücksichtigt, die als
 * erstes erfasst wurde.
 */
public class BetreuungComparatorNew implements
	Comparator<AbstractPlatz>,
	Serializable {

	private static final long serialVersionUID = -308383917391346314L;

	@Override
	public int compare(AbstractPlatz platz1, AbstractPlatz platz2) {
		// Reihenfolge ist nur fuer Betreuungen relevant für Restanspruch, daher werden nur Betreuungen verglichen.
		if (!(platz1 instanceof Betreuung && platz2 instanceof Betreuung)) {
			return 0;
		}

		Betreuung betreuung1 = (Betreuung) platz1;
		Betreuung betreuung2 = (Betreuung) platz2;

		if (isSameBetreuungsangebotTyp(betreuung1, betreuung2)
			&&
			isEitherDoppelBetreuungPrioSet(betreuung1, betreuung2)) {
			if (isBothDoppelBetreuungPrioSet(betreuung1, betreuung2)) {
				return compareDoppelBetreuungPrio(betreuung1, betreuung2);
			}
			// at this point, either betreuung1 or betreuung2 has the prio set. The one that has it set gets the prio
			return isBetreuungPrioritaetGesetzt(betreuung1) ? -1 : 1;
		}

		if (betreuung1.getBetreuungspensumContainers().isEmpty()
			|| betreuung2.getBetreuungspensumContainers().isEmpty()) {
			return 0;
		}

		Comparator<Betreuung> comparator = Comparator
			// 1. Prio: Kita-Angebot absteigend, kita zuerst
			.comparing(
				(Betreuung b) -> b.getBetreuungsangebotTyp().isKita(),
				Comparator.reverseOrder()
			)
			// 2. Prio: früheres Startdatum
			.thenComparing(
				b -> getFirstBetreuungspensum(b).getGueltigkeit().getGueltigAb()
			)
			// 3. Prio: das höhere Pensum
			.thenComparing(
				b -> getFirstBetreuungspensum(b).getBetreuungspensumJA()
					.getPensum(),
				Comparator.reverseOrder()
			)
			// 4. Prio: Das Angebot, das zuerst erfasst wurde (Datum angefordert der Betreuung)
			.thenComparing(
				b -> Objects.requireNonNull(b.getDatumAngefordert())
			);

		return comparator.compare(betreuung1, betreuung2);
	}

	private BetreuungspensumContainer getFirstBetreuungspensum(Betreuung b) {
		return b.getBetreuungspensumContainers()
			.stream()
			.min(new BetreuungspensumContainerComparator())
			.orElseThrow();
	}

	private boolean isBetreuungPrioritaetGesetzt(@Nonnull Betreuung betreuung) {
		return betreuung.getVorgaengerVerfuegung() != null
			&& betreuung.getVorgaengerVerfuegung().getDoppelBetreuungPrio()
				!= null;
	}

	private static int compareDoppelBetreuungPrio(
		Betreuung betreuung1,
		Betreuung betreuung2
	) {
		return Objects.requireNonNull(
			Objects.requireNonNull(betreuung1.getVorgaengerVerfuegung())
				.getDoppelBetreuungPrio()
		)
			>= Objects.requireNonNull(
				Objects.requireNonNull(betreuung2.getVorgaengerVerfuegung())
					.getDoppelBetreuungPrio()
			) ? 1 : -1;
	}

	private boolean isBothDoppelBetreuungPrioSet(
		Betreuung betreuung1,
		Betreuung betreuung2
	) {
		return isBetreuungPrioritaetGesetzt(betreuung1)
			&&
			isBetreuungPrioritaetGesetzt(betreuung2);
	}

	private boolean isEitherDoppelBetreuungPrioSet(
		Betreuung betreuung1,
		Betreuung betreuung2
	) {
		return isBetreuungPrioritaetGesetzt(betreuung1)
			|| isBetreuungPrioritaetGesetzt(betreuung2);
	}

	private static boolean isSameBetreuungsangebotTyp(
		Betreuung betreuung1,
		Betreuung betreuung2
	) {
		return betreuung1.getInstitutionStammdaten().getBetreuungsangebotTyp()
			==
			betreuung2.getInstitutionStammdaten().getBetreuungsangebotTyp();
	}

}
