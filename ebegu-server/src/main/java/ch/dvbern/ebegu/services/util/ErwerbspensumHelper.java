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

package ch.dvbern.ebegu.services.util;

import java.util.Objects;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;

public final class ErwerbspensumHelper {

	private ErwerbspensumHelper() {
	}

	public static boolean isKonkubinatOhneKindAndGS2ErwerbspensumOmittable(
		Familiensituation familiensituation,
		Gesuchsperiode gesuchsperiode
	) {
		if (familiensituation.getFamilienstatus()
			!= EnumFamilienstatus.KONKUBINAT_KEIN_KIND) {
			return false;
		}

		return (familiensituation.isKonkubinatReachingMinDauerIn(gesuchsperiode)
			|| familiensituation
				.isKonkubinatShorterThanXYearsAtAnyTimeAfterStartOfPeriode(
					gesuchsperiode
				))
			&& Objects.equals(
				familiensituation.getGeteilteObhut(),
				Boolean.FALSE
			)
			&& familiensituation.getUnterhaltsvereinbarung()
				== UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG;
	}

	public static boolean isErwerbespensumContainerEmpty(
		@Nullable GesuchstellerContainer gesuchsteller
	) {
		if (gesuchsteller == null) {
			return true;
		}

		return gesuchsteller.getErwerbspensenContainers().isEmpty();
	}

	public static boolean hasNoKindWithUnterhaltspflichtGS2(Gesuch gesuch) {
		return gesuch.getKindContainers()
			.stream()
			.map(KindContainer::getKindJA)
			.noneMatch(
				kind -> Boolean.TRUE.equals(kind.getGemeinsamesGesuch())
			);
	}

	/**
	 * Prüft, ob ein Erwerbspensum für den GS2 nötig ist.
	 * Falls ein GS2 vorhanden ist, ist ein Erwerbspesnum grundsätzlich nötig.
	 *
	 * Einzige Ausnahmen bieten folgender Spezialfälle innerhalb einer FKJV Periode:
	 * Die elterliche Obhut findet nicht in zwei Haushalten statt (Familiensituation#geteilteObhut)
	 * und es wurde keine Unterhaltsvereinbarung abgeschlossen (Familiensituation#unterhaltsvereinbarung).
	 * Sind diese Bedinungen erfüllt gibt es zwei Gesuschsteller, es ist allerdings nur das Erwerbspensum von GS1
	 * relevant
	 *
	 * Ab Periode 24/25 muss in folgendem Fall nur das Erwerbspensum von GS1 erfasst werden:
	 * Konkubinat ohne Kind wird während Periode 2-jährig (Einstellung), keine geteilte Obhut, keine
	 * Unterhaltsvereinbarung
	 */
	public static boolean isErwerbspensumRequiredForGS2(
		Gesuch gesuch,
		boolean gesuchBeendenBeiTauschGS2Active,
		boolean abhaengigkeitBeschaeftigungspensumAnspruchSchwyz
	) {
		Familiensituation familiensituation = gesuch.extractFamiliensituation();

		if (familiensituation == null) {
			return false;
		}
		if (!gesuch.hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode()) {
			return false;
		}
		if (familiensituation.getUnterhaltsvereinbarung() != null) {
			return false;
		}

		if (gesuchBeendenBeiTauschGS2Active
			&&
			isKonkubinatOhneKindAndGS2ErwerbspensumOmittable(
				familiensituation,
				gesuch.getGesuchsperiode()
			)) {
			return false;
		}

		if (abhaengigkeitBeschaeftigungspensumAnspruchSchwyz
			&& hasNoKindWithUnterhaltspflichtGS2(gesuch)) {
			return false;
		}

		return gesuch.getGesuchsteller2() == null
			|| gesuch.getGesuchsteller2()
				.getErwerbspensenContainers()
				.isEmpty();
	}
}
