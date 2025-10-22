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

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.GeschwisterbonusTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.DateUtil;
import lombok.Getter;

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
