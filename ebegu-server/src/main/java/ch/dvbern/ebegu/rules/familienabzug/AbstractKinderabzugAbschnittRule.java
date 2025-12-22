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
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.AbstractAbschnittRule;
import ch.dvbern.ebegu.rules.RuleKey;
import ch.dvbern.ebegu.rules.RuleType;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.types.DateRange;

import static java.time.temporal.TemporalAdjusters.firstDayOfNextMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

public abstract class AbstractKinderabzugAbschnittRule extends
	AbstractAbschnittRule {

	protected AbstractKinderabzugAbschnittRule(
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
		Gesuchsperiode gesuchsperiode = gesuch.getGesuchsperiode();

		return gesuch.getKindContainers()
			.stream()
			.flatMap(
				kindContainer -> createZeitabschnitteForKind(
					kindContainer,
					gesuchsperiode
				).stream()
			)
			.collect(Collectors.toList());
	}

	protected List<DateRange> createZeitabschnittGueltigkeitenBasisForKind(
		Gesuchsperiode gesuchsperiode
	) {
		return List.of(gesuchsperiode.getGueltigkeit());
	}

	private List<VerfuegungZeitabschnitt> createZeitabschnitteForKind(
		KindContainer kindContainer,
		Gesuchsperiode gesuchsperiode
	) {
		List<DateRange> basicGueltigkeitenForKind =
			createZeitabschnittGueltigkeitenBasisForKind(gesuchsperiode);
		return basicGueltigkeitenForKind.stream()
			.flatMap(
				basicGueltigkeit -> createAbschnittForKindAndGueltigkeit(
					kindContainer,
					basicGueltigkeit
				)
			)
			.collect(Collectors.toList());
	}

	private Stream<VerfuegungZeitabschnitt> createAbschnittForKindAndGueltigkeit(
		KindContainer kindContainer,
		DateRange gueltigkeit
	) {
		if (!isKindGueltigInDateRange(kindContainer.getKindJA(), gueltigkeit)) {
			return Stream.of();
		}

		DateRange gueltigkeitWithBeruecksichtigungTerminiert =
			getGueltigkeitWithBeruecksichtigungTerminiert(
				kindContainer.getKindJA(),
				gueltigkeit
			);
		return createAbschnitteForGueltigesKind(
			kindContainer,
			gueltigkeitWithBeruecksichtigungTerminiert
		);
	}

	@NotNull
	private DateRange getGueltigkeitWithBeruecksichtigungTerminiert(
		Kind kind,
		DateRange gueltigkeit
	) {
		if (!kind.isGueltigkeitTerminiert()) {
			return gueltigkeit;
		}

		Objects.requireNonNull(kind.getGueltigkeitTerminiertPer());
		if (!gueltigkeit.contains(kind.getGueltigkeitTerminiertPer())) {
			return gueltigkeit;
		}

		return new DateRange(
			gueltigkeit.getGueltigAb(),
			kind.getGueltigkeitTerminiertPer().with(lastDayOfMonth())
		);
	}

	private Stream<VerfuegungZeitabschnitt> createAbschnitteForGueltigesKind(
		KindContainer kindContainer,
		DateRange gueltigkeit
	) {
		LocalDate geburtsdatum = kindContainer.getKindJA().getGeburtsdatum();
		LocalDate beginMonatNachGeb = getStichtagForEreignis(geburtsdatum);

		// Kind ist während Date-Range geboren -> Zeitabschnitt ab Folgemonat erstellen
		if (gueltigkeit.contains(geburtsdatum)) {
			DateRange nachGeburt = new DateRange(
				beginMonatNachGeb,
				gueltigkeit.getGueltigBis()
			);
			return Stream.of(
				createZeitabschnittWithKinderAbzug(kindContainer, nachGeburt)
			);
		}

		if (turnsVolljaehrigInDateRange(kindContainer, gueltigkeit)) {
			return createZeitabschnitteForVolljaehrigesKind(
				kindContainer,
				gueltigkeit
			);
		}
		// kein spezielles ereigniss innerhalb des Date-Range
		return Stream.of(
			createZeitabschnittWithKinderAbzug(kindContainer, gueltigkeit)
		);
	}

	private boolean isKindGueltigInDateRange(
		Kind kindJA,
		DateRange gueltigkeit
	) {
		// Kind ist nach Date-Range geboren -> Kind zählt nicht
		if (kindJA.getGeburtsdatum().isAfter(gueltigkeit.getGueltigBis())) {
			return false;
		}

		if (!kindJA.isGueltigkeitTerminiert()) {
			return true;
		}
		// Kind ist Terminiert -> nicht gültig wenn Terminiert Datum vor Gültigkeit
		Objects.requireNonNull(kindJA.getGueltigkeitTerminiertPer());
		return gueltigkeit.getGueltigAb()
			.isBefore(kindJA.getGueltigkeitTerminiertPer());
	}

	protected VerfuegungZeitabschnitt createZeitabschnittWithKinderAbzug(
		KindContainer kindContainer,
		DateRange gueltigkeit
	) {
		Kinderabzug abzug = calculateKinderAbzug(
			kindContainer,
			gueltigkeit.getGueltigAb()
		);
		VerfuegungZeitabschnitt zeitabschnitt =
			createZeitabschnittWithinValidityPeriodOfRule(gueltigkeit);
		zeitabschnitt.setKinderabzugForKind(
			kindContainer.getKindNummer(),
			abzug
		);
		return zeitabschnitt;
	}

	private boolean turnsVolljaehrigInDateRange(
		KindContainer kindContainer,
		DateRange dateRange
	) {
		final LocalDate geburtsdatum = kindContainer.getKindJA()
			.getGeburtsdatum();

		final LocalDate dateWith18 = geburtsdatum.plusYears(18);
		return dateRange.contains(dateWith18);
	}

	private Stream<VerfuegungZeitabschnitt> createZeitabschnitteForVolljaehrigesKind(
		KindContainer kindContainer,
		DateRange gueltigkeit
	) {
		//Kind wird volljahrig innerhalb des Date-Range
		// -> Zeitabschnitt 1 bis volljärhigkeit (Ende Monat)
		// -> Zeitabschnitt 2 ab volljärhigkeit (Beginn Folgemonat)
		final LocalDate dateWith18 = kindContainer.getKindJA()
			.getGeburtsdatum()
			.plusYears(18);

		DateRange afterVolljaehrig = new DateRange(
			dateWith18.with(firstDayOfNextMonth()),
			gueltigkeit.getGueltigBis()
		);

		DateRange bisVolljaehrig = new DateRange(
			gueltigkeit.getGueltigAb(),
			dateWith18.with(lastDayOfMonth())
		);

		return Stream.of(
			createZeitabschnittWithKinderAbzug(
				kindContainer,
				afterVolljaehrig
			),
			createZeitabschnittWithKinderAbzug(
				kindContainer,
				bisVolljaehrig
			)
		);
	}

	protected abstract Kinderabzug calculateKinderAbzug(
		KindContainer kindContainer,
		LocalDate stichtag
	);

	@Override
	public boolean isRelevantForFamiliensituation() {
		return true;
	}
}
