/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.kind;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.AbstractAbschnittRule;
import ch.dvbern.ebegu.rules.RuleKey;
import ch.dvbern.ebegu.rules.RuleType;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.types.DateRange;

public abstract class AbstractKindTarifAbschnittRule extends
	AbstractAbschnittRule {

	protected long dauerBabyTarif = 0;

	protected AbstractKindTarifAbschnittRule(
		@Nonnull RuleKey ruleKey,
		@Nonnull RuleType ruleType,
		@Nonnull RuleValidity ruleValidity,
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		super(ruleKey, ruleType, ruleValidity, validityPeriod, locale);
	}

	/**
	 * Berechnet den Zeitabschnitt, in dem der, zusätzlich von der Gemeinde oder Kanton gewährte Babytarif gewährt
	 * werden kann.
	 * Hier wird lediglich das Alter des Kindes berücksichtigt und keine anderen Aspekte, die auf die Gewährung eines BG
	 * generell
	 * Einfluss nehmen würden, wie z.B. Einkommen oder Beschäftigungspensum.
	 *
	 * @param platz Der Betreuungsplatz des Kindes, für das der Zeitabschnitt bestimmt werden soll.
	 * @return Eine Liste, die genau den (einen) Zeitabschnitt enthält, in dem der Babytarif gewährt werden kann.
	 */
	@Override
	@Nonnull
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(
		@Nonnull AbstractPlatz platz
	) {
		List<VerfuegungZeitabschnitt> zeitabschnittList = new ArrayList<>();

		// Relevant sind der Geburtstag des Kindes sowie der Einschulungstyp
		Kind kind = platz.getKind().getKindJA();
		final LocalDate geburtsdatum = kind.getGeburtsdatum();
		LocalDate stichtagBabyTarifEnde = geburtsdatum.plusMonths(
			dauerBabyTarif
		).with(TemporalAdjusters.lastDayOfMonth());
		DateRange gesuchsperiode = platz.extractGesuchsperiode()
			.getGueltigkeit();

		EinschulungTyp einschulungTyp = kind.getEinschulungTyp() != null ?
			kind.getEinschulungTyp() :
			EinschulungTyp.VORSCHULALTER;
		if (gesuchsperiode.contains(stichtagBabyTarifEnde)) {
			DateRange abschnittBaby = new DateRange(
				gesuchsperiode.getGueltigAb(),
				stichtagBabyTarifEnde
			);
			zeitabschnittList.add(
				createZeitabschnitt(abschnittBaby, true, einschulungTyp)
			);

			DateRange abschnittKind = new DateRange(
				stichtagBabyTarifEnde.plusDays(1),
				gesuchsperiode.getGueltigBis()
			);
			VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnitt(
				abschnittKind,
				false,
				einschulungTyp
			);
			zeitabschnitt.getRelevantBgCalculationInput()
				.addBemerkung(
					MsgKey.KLEINKIND_TARIF,
					getLocale(),
					dauerBabyTarif
				);
			zeitabschnittList.add(zeitabschnitt);
		} else {
			boolean baby = stichtagBabyTarifEnde.isAfter(
				gesuchsperiode.getGueltigBis()
			);
			zeitabschnittList.add(
				createZeitabschnitt(gesuchsperiode, baby, einschulungTyp)
			);
		}

		return zeitabschnittList;
	}

	abstract VerfuegungZeitabschnitt createZeitabschnitt(
		@Nonnull DateRange gueltigkeit,
		boolean baby,
		@Nonnull EinschulungTyp einschulungTyp
	);
}
