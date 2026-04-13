/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.GeschwisterbonusTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.DateUtil;
import lombok.Getter;

/**
 * Erstellt die {@link VerfuegungZeitabschnitt}e, in welchen ein Geschwisterbonus des Typs "Schwyz" gilt.
 * Die Rule ist nur dann relevant, wenn die {@link Einstellung} "GESCHWISTERNBONUS_TYP" entsprechend gesetzt ist.
 *
 * Siehe {@link #createVerfuegungsZeitabschnitte} for more information about how these {@link VerfuegungZeitabschnitt}e
 * are
 * created.
 *
 */
public class GeschwisterbonusSchwyzAbschnittRule extends AbstractAbschnittRule {

	protected GeschwisterbonusSchwyzAbschnittRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		super(
			RuleKey.GESCHWISTERBONUS,
			RuleType.GRUNDREGEL_DATA,
			RuleValidity.ASIV,
			validityPeriod,
			locale
		);
	}

	@Override
	public boolean isRelevantForGemeinde(
		@Nonnull Map<EinstellungKey, Einstellung> einstellungMap
	) {
		Einstellung geschwisterbonus = einstellungMap.get(
			EinstellungKey.GESCHWISTERNBONUS_TYP
		);
		return GeschwisterbonusTyp.getEnumValue(geschwisterbonus)
			== GeschwisterbonusTyp.SCHWYZ
			|| GeschwisterbonusTyp.getEnumValue(geschwisterbonus)
				== GeschwisterbonusTyp.SCHWYZ_2;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

	/**
	 * Creates the {@link VerfuegungZeitabschnitt}e according to the Geschwisterbonus-Schwyz-Rules.
	 *
	 * 1. Filter all other Kinder, which have no {@link Betreuung}en
	 * 2. Filter all Kinder which do not contribute to the geschwisterbonus according to their age
	 * 3. Create {@link VerfuegungZeitabschnitt}e for the times when the Kinder are in Betreuung
	 * 4. Terminate {@link VerfuegungZeitabschnitt} when contribution to geschwisterbonus ends
	 * 5. Set {@link BGCalculationInput} values
	 * 6. Remove invalid {@link VerfuegungZeitabschnitt}e
	 *
	 * @param platz the {@link Betreuung} for which the Abschnitte should be created
	 * @return the {@link VerfuegungZeitabschnitt}e which have a Geschwisterbonus with set values and bemerkungen
	 */
	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(
		@Nonnull AbstractPlatz platz
	) {
		var gesuch = platz.extractGesuch();
		final DateRange gpGueltigkeit = platz.extractGesuchsperiode()
			.getGueltigkeit();
		var createdAbschnitte = gesuch.getKindContainers()
			.stream()
			.filter(kindContainer -> !kindContainer.isSame(platz.getKind()))
			.filter(
				kindContainer -> !kindContainer.getBetreuungen()
					.isEmpty()
			)
			.filter(
				kindContainer -> contributesToGeschwisterbonus(
					kindContainer,
					gpGueltigkeit
				)
			)
			.flatMap(
				kindContainer -> createAbschnittGeburtstagTuplesForBetreuungen(
					kindContainer,
					platz,
					gpGueltigkeit
				)
			)
			.map(this::limitGueltigkeitWithGeburtstagAndTerminiert)
			.map(this::setAnzahlGeschwister)
			.flatMap(
				verfuegungZeitabschnittKindTuple -> setPlatzKindGe(
					platz,
					verfuegungZeitabschnittKindTuple
				)
			)
			.map(this::limitGueltigkeitWithGeburtstagAndTerminiert)
			.map(VerfuegungZeitabschnittKindTuple::getZeitabschnitt)
			.filter(
				verfuegungZeitabschnitt -> verfuegungZeitabschnitt
					.getGueltigkeit()
					.isValid()
			)
			.collect(Collectors.toList());

		final List<VerfuegungZeitabschnitt> mergedAbschnitte =
			mergeZeitabschnitte(createdAbschnitte);
		mergedAbschnitte.forEach(
			abschnitt -> abschnitt.getBgCalculationInputAsiv()
				.addBemerkung(
					MsgKey.GESCHWISTERBONUS_SCHWYZ,
					getLocale(),
					abschnitt.getBgCalculationInputAsiv()
						.getAnzahlGeschwister()
				)
		);
		return mergedAbschnitte;
	}

	private Stream<VerfuegungZeitabschnittKindTuple> setPlatzKindGe(
		AbstractPlatz platz,
		VerfuegungZeitabschnittKindTuple verfuegungZeitabschnittKindTuple
	) {
		verfuegungZeitabschnittKindTuple.geburtsdatum = platz.getKind()
			.getKindJA()
			.getGeburtsdatum();
		return Stream.of(verfuegungZeitabschnittKindTuple);
	}

	/**
	 * A {@link KindContainer} can contribute to the Geschwisterbonus when the Kind is
	 * born before the gesuchsperiode ends and is not yet 18 years old at any point during
	 * the gesuchsperiode.
	 *
	 * @param kindContainer the {@link KindContainer} to check for contribution to the geschwisterbonus
	 * @param gpGueltigkeit the {@link DateRange} of the {@link Gesuchsperiode}
	 * @return whether the Kind contributes to the geschwisterbonus by age
	 */
	private static boolean contributesToGeschwisterbonus(
		KindContainer kindContainer,
		DateRange gpGueltigkeit
	) {
		var geburtsdatum = kindContainer.getKindJA().getGeburtsdatum();
		return DateUtil.isSameDateOrAfter(
			geburtsdatum,
			gpGueltigkeit.getGueltigAb().minusYears(18)
		)
			&& DateUtil.isSameDateOrBefore(
				geburtsdatum,
				gpGueltigkeit.getGueltigBis()
			);
	}

	/**
	 * Creates {@link VerfuegungZeitabschnittKindTuple} for the times where the kind is in betreuung.
	 * A Kind is only then considered in Betreuung when it has a {@link Betreuungspensum} that has not
	 * a 0-Pensum and 0-Kosten.
	 *
	 * @param kindContainer The Kind for which to create the abschnitte
	 * @param platz the Betreuung of the Kind
	 * @param gpGueltigkeit the {@link DateRange} of the {@link Gesuchsperiode}
	 * @return {@link VerfuegungZeitabschnittKindTuple} for the abschnitte where the kind is in Betreuung
	 */
	@Nonnull
	private Stream<VerfuegungZeitabschnittKindTuple> createAbschnittGeburtstagTuplesForBetreuungen(
		KindContainer kindContainer,
		AbstractPlatz platz,
		DateRange gpGueltigkeit
	) {
		List<VerfuegungZeitabschnitt> zeitabschnitts = kindContainer
			.getBetreuungen()
			.stream()
			.flatMap(
				betreuung -> betreuung.getBetreuungspensumContainers()
					.stream()
			)
			.filter(betreuungspensumContainer -> {
				var ja = betreuungspensumContainer.getBetreuungspensumJA();
				return ja.getPensum().compareTo(BigDecimal.ZERO) > 0
					&& ja.getMonatlicheBetreuungskosten()
						.compareTo(BigDecimal.ZERO)
						> 0;
			})
			.map(
				betreuungspensumContainer -> {
					VerfuegungZeitabschnitt zeitabschnitt =
						createZeitabschnittWithinValidityPeriodOfRule(
							DateUtil.limitToDateRange(
								betreuungspensumContainer
									.getGueltigkeit(),
								gpGueltigkeit
							)
						);
					zeitabschnitt.addKind(kindContainer.getKindJA());
					return zeitabschnitt;
				}
			)
			.collect(Collectors.toList());

		Betreuung betreuung = (Betreuung) platz;
		zeitabschnitts = adaptZeitabschnitteWithActualBetreuung(
			zeitabschnitts,
			betreuung.getBetreuungspensumContainers()
		);

		return mergeZeitabschnitte(zeitabschnitts)
			.stream()
			.map(
				zeitabschnitt -> new VerfuegungZeitabschnittKindTuple(
					zeitabschnitt,
					kindContainer.getKindJA()
				)
			);
	}

	private List<VerfuegungZeitabschnitt> adaptZeitabschnitteWithActualBetreuung(
		List<VerfuegungZeitabschnitt> zeitabschnitts,
		Set<BetreuungspensumContainer> betreuungspensumContainers
	) {
		Iterator<VerfuegungZeitabschnitt> iterator1 = zeitabschnitts.iterator();
		List<VerfuegungZeitabschnitt> adaptedAbschnitte = new ArrayList<>();
		while (iterator1.hasNext()) {
			VerfuegungZeitabschnitt verfuegungZeitabschnitt = iterator1.next();

			for (BetreuungspensumContainer betreuungspensumContainer : betreuungspensumContainers) {
				if (!verfuegungZeitabschnitt.getGueltigkeit()
					.getGueltigAb()
					.isBefore(
						betreuungspensumContainer.getGueltigkeit()
							.getGueltigAb()
					)
					&& !verfuegungZeitabschnitt.getGueltigkeit()
						.getGueltigBis()
						.isAfter(
							betreuungspensumContainer.getGueltigkeit()
								.getGueltigBis()
						)) {
					adaptedAbschnitte.add(verfuegungZeitabschnitt);
					break;
				} else {
					VerfuegungZeitabschnitt verfuegungZeitabschnittUeberscheidung =
						createVerfuegungZeitabschnittUeberschneidung(
							verfuegungZeitabschnitt,
							betreuungspensumContainer
						);
					if (verfuegungZeitabschnittUeberscheidung != null) {
						adaptedAbschnitte.add(
							verfuegungZeitabschnittUeberscheidung
						);
					}
				}
			}
		}
		return adaptedAbschnitte;
	}

	private VerfuegungZeitabschnitt createVerfuegungZeitabschnittUeberschneidung(
		VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		BetreuungspensumContainer betreuungspensumContainer
	) {
		VerfuegungZeitabschnitt verfuegungZeitabschnittUeberscheidung =
			null;
		if (verfuegungZeitabschnitt.getGueltigkeit()
			.getGueltigAb()
			.isBefore(
				betreuungspensumContainer.getGueltigkeit()
					.getGueltigAb()
			)
			&& !verfuegungZeitabschnitt.getGueltigkeit()
				.getGueltigBis()
				.isBefore(
					betreuungspensumContainer.getGueltigkeit()
						.getGueltigAb()
				)) {
			verfuegungZeitabschnittUeberscheidung =
				new VerfuegungZeitabschnitt(
					verfuegungZeitabschnitt
				);
			verfuegungZeitabschnittUeberscheidung.getGueltigkeit()
				.setGueltigAb(
					betreuungspensumContainer.getGueltigkeit()
						.getGueltigAb()
				);
		}
		if (verfuegungZeitabschnitt.getGueltigkeit()
			.getGueltigBis()
			.isAfter(
				betreuungspensumContainer.getGueltigkeit()
					.getGueltigBis()
			)
			&& !verfuegungZeitabschnitt.getGueltigkeit()
				.getGueltigAb()
				.isAfter(
					betreuungspensumContainer.getGueltigkeit()
						.getGueltigBis()
				)) {
			if (verfuegungZeitabschnittUeberscheidung == null) {
				verfuegungZeitabschnittUeberscheidung =
					new VerfuegungZeitabschnitt(
						verfuegungZeitabschnitt
					);
			}
			verfuegungZeitabschnittUeberscheidung.getGueltigkeit()
				.setGueltigBis(
					betreuungspensumContainer.getGueltigkeit()
						.getGueltigBis()
				);

		}
		return verfuegungZeitabschnittUeberscheidung;
	}

	@Nonnull
	private VerfuegungZeitabschnittKindTuple limitGueltigkeitWithGeburtstagAndTerminiert(
		VerfuegungZeitabschnittKindTuple zeitabschnittGeburtsdatumTuple
	) {
		limitGueltigkeitWithGeburtstag(zeitabschnittGeburtsdatumTuple);
		limitGueltigkeitWithKindTerminiert(zeitabschnittGeburtsdatumTuple);
		return zeitabschnittGeburtsdatumTuple;
	}

	private void limitGueltigkeitWithKindTerminiert(
		VerfuegungZeitabschnittKindTuple zeitabschnittGeburtsdatumTuple
	) {
		var gueltigkeit = zeitabschnittGeburtsdatumTuple.getZeitabschnitt()
			.getGueltigkeit();
		if (zeitabschnittGeburtsdatumTuple.terminiertPerEndeMonat != null
			&&
			gueltigkeit.contains(
				zeitabschnittGeburtsdatumTuple.terminiertPerEndeMonat
			)) {
			gueltigkeit.setGueltigBis(
				zeitabschnittGeburtsdatumTuple.terminiertPerEndeMonat
			);
		}
	}

	private void limitGueltigkeitWithGeburtstag(
		VerfuegungZeitabschnittKindTuple zeitabschnittGeburtsdatumTuple
	) {
		var geburtsdatum = zeitabschnittGeburtsdatumTuple.getGeburtsdatum();
		var gueltigkeit = zeitabschnittGeburtsdatumTuple.getZeitabschnitt()
			.getGueltigkeit();

		if (isBornDuringGueltigkeit(geburtsdatum, gueltigkeit)) {
			gueltigkeit.setGueltigAb(
				mapDateIntoGueltigkeit(geburtsdatum, gueltigkeit)
			);
		}
		if (reaches18During(geburtsdatum, gueltigkeit)) {
			gueltigkeit.setGueltigBis(
				mapDateIntoGueltigkeit(geburtsdatum, gueltigkeit)
			);
		}
		if (reaches18Before(geburtsdatum, gueltigkeit)) {
			gueltigkeit.setGueltigBis(gueltigkeit.getGueltigAb().minusDays(1));
		}
	}

	private boolean isBornDuringGueltigkeit(
		LocalDate geburtsdatum,
		DateRange gueltigkeit
	) {
		return gueltigkeit.contains(geburtsdatum);
	}

	private boolean reaches18During(
		LocalDate geburtsdatum,
		DateRange gueltigkeit
	) {
		return gueltigkeit.contains(geburtsdatum.plusYears(18));
	}

	private boolean reaches18Before(
		LocalDate geburtsdatum,
		DateRange gueltigkeit
	) {
		return gueltigkeit.getGueltigAb().isAfter(geburtsdatum.plusYears(18));
	}

	private LocalDate mapDateIntoGueltigkeit(
		LocalDate date,
		DateRange gueltigkeit
	) {
		return date.getMonthValue() >= 8 ?
			LocalDate.of(
				gueltigkeit.getGueltigAb().getYear(),
				date.getMonth(),
				date.getDayOfMonth()
			) :
			LocalDate.of(
				gueltigkeit.getGueltigBis().getYear(),
				date.getMonth(),
				date.getDayOfMonth()
			);
	}

	@Nonnull
	private VerfuegungZeitabschnittKindTuple setAnzahlGeschwister(
		VerfuegungZeitabschnittKindTuple zeitabschnittGeburtsdatumTuple
	) {
		zeitabschnittGeburtsdatumTuple.getZeitabschnitt()
			.setAnzahlGeschwister(1);
		return zeitabschnittGeburtsdatumTuple;
	}

	@Getter
	private static class VerfuegungZeitabschnittKindTuple {
		private final VerfuegungZeitabschnitt zeitabschnitt;
		private LocalDate geburtsdatum;
		@Nullable
		private LocalDate terminiertPerEndeMonat;

		private VerfuegungZeitabschnittKindTuple(
			VerfuegungZeitabschnitt zeitabschnitt,
			Kind kind
		) {
			this.zeitabschnitt = zeitabschnitt;
			this.geburtsdatum = kind.getGeburtsdatum();
			if (kind.isGueltigkeitTerminiert()
				&& kind.getGueltigkeitTerminiertPer() != null) {
				this.terminiertPerEndeMonat = kind
					.getGueltigkeitTerminiertPer()
					.with(
						TemporalAdjusters.lastDayOfMonth()
					);
			}
		}
	}
}
