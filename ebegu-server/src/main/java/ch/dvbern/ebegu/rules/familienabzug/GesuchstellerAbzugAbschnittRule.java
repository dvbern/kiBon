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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.AbstractAbschnittRule;
import ch.dvbern.ebegu.rules.RuleKey;
import ch.dvbern.ebegu.rules.RuleType;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.RuleUtil;

import static java.time.temporal.TemporalAdjusters.firstDayOfNextMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

public class GesuchstellerAbzugAbschnittRule extends
	AbstractAbschnittRule {

	protected GesuchstellerAbzugAbschnittRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		super(
			RuleKey.FAMILIENSITUATION,
			RuleType.GRUNDREGEL_DATA,
			RuleValidity.ASIV,
			validityPeriod,
			locale
		);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
	}

	@Override
	@Nonnull
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(
		@Nonnull AbstractPlatz platz
	) {
		Gesuch gesuch = platz.extractGesuch();
		Familiensituation familiensituation = getFamiliensituationOrThrowError(
			gesuch
		);
		return createAbschnitteFamiliensitution(familiensituation, gesuch);
	}

	private Familiensituation getFamiliensituationOrThrowError(Gesuch gesuch) {
		Familiensituation familiensituation = gesuch.extractFamiliensituation();

		if (familiensituation == null) {
			throw new IllegalArgumentException(
				"Die Familiengroesse kann noch nicht richtig berechnet werden weil die Familiensituation nicht richtig "
					+ "ausgefuellt ist. Antragnummer: "
					+ gesuch.getJahrFallAndGemeindenummer()
			);
		}

		return familiensituation;
	}

	protected List<VerfuegungZeitabschnitt> createAbschnitteFamiliensitution(
		Familiensituation familiensituation,
		Gesuch gesuch
	) {
		if (familiensituation.getAenderungPer() != null
			&&
			gesuch.getGesuchsperiode()
				.getGueltigkeit()
				.contains(familiensituation.getAenderungPer())) {
			return createZeitabschnitteForMutierteFamiliensituation(
				familiensituation,
				gesuch
			);
		}

		if (familiensituation.isKonkubinatReachingMinDauerIn(
			gesuch.getGesuchsperiode()
		)) {
			return createZeitabschnitteForKonkubinatReachingMinDauer(
				familiensituation,
				gesuch
			);
		}
		return Stream.of(
			createZeitabschnittWithAnzahlGesuchsteller(
				familiensituation,
				gesuch.getGesuchsperiode().getGueltigkeit()
			)
		)
			.collect(Collectors.toList());
	}

	private List<VerfuegungZeitabschnitt> createZeitabschnitteForKonkubinatReachingMinDauer(
		Familiensituation familiensituation,
		Gesuch gesuch
	) {
		LocalDate startKonkubinatEndOfMonth = familiensituation
			.getStartKonkubinatPlusMindauerEndOfMonth();

		DateRange dateRangeBeforeReachingMinDauer = new DateRange(
			gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
			startKonkubinatEndOfMonth
		);

		DateRange dateRangeAfterReachingMinDauer = new DateRange(
			startKonkubinatEndOfMonth.with(firstDayOfNextMonth()),
			gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis()
		);

		return Stream.of(
			createZeitabschnittWithAnzahlGesuchsteller(
				familiensituation,
				dateRangeBeforeReachingMinDauer
			),
			createZeitabschnittWithAnzahlGesuchsteller(
				familiensituation,
				dateRangeAfterReachingMinDauer
			)
		)
			.collect(Collectors.toList());

	}

	private List<VerfuegungZeitabschnitt> createZeitabschnitteForMutierteFamiliensituation(
		Familiensituation familiensituationAktuell,
		Gesuch gesuch
	) {
		Familiensituation familiensituationErstgesuch = gesuch
			.extractFamiliensituationErstgesuch();
		LocalDate famsitAenderungPer = familiensituationAktuell
			.getAenderungPer();

		Objects.requireNonNull(familiensituationErstgesuch);
		Objects.requireNonNull(famsitAenderungPer);

		LocalDate familiensituationAenderungStichtag = RuleUtil
			.getFamSitAenderungPerDatum(
				gesuch,
				familiensituationAktuell.getAenderungPer()
			);

		DateRange rangeBisAenderung = new DateRange(
			gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb(),
			familiensituationAenderungStichtag.with(lastDayOfMonth())
		);

		DateRange rangeNachAenderung = new DateRange(
			familiensituationAenderungStichtag.with(firstDayOfNextMonth()),
			gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis()
		);

		return Stream.of(
			createZeitabschnittWithAnzahlGesuchsteller(
				familiensituationErstgesuch,
				rangeBisAenderung
			),
			createZeitabschnittWithAnzahlGesuchsteller(
				familiensituationAktuell,
				rangeNachAenderung
			)
		)
			.collect(Collectors.toList());
	}

	private VerfuegungZeitabschnitt createZeitabschnittWithAnzahlGesuchsteller(
		Familiensituation familiensituation,
		DateRange gueltigkeit
	) {
		VerfuegungZeitabschnitt zeitabschnitt =
			createZeitabschnittWithinValidityPeriodOfRule(gueltigkeit);

		if (familiensituation.hasSecondGesuchsteller(
			gueltigkeit.getGueltigAb().plusMonths(1)
		)) {
			zeitabschnitt.setAnzahlGesuchsteller(2);
		} else {
			zeitabschnitt.setAnzahlGesuchsteller(1);
		}

		return zeitabschnitt;
	}

	@Override
	public boolean isRelevantForFamiliensituation() {
		return true;
	}
}
