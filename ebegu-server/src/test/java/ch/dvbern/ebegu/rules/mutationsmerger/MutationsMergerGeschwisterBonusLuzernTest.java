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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.GeschwisterbonusTyp;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests für Verfügungsmuster
 */
class MutationsMergerGeschwisterBonusLuzernTest {

	private static final MutationsMerger MUTATIONS_MERGER = new MutationsMerger(
		Locale.GERMAN,
		false,
		new MutationsMergerParameter(false, GeschwisterbonusTyp.LUZERN)
	);

	@Nested
	class GeschwisterBonusTest {

		private final LocalDate GP_START = LocalDate.of(2025, 8, 1);
		private final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);
		private final LocalDate NOV_30 = LocalDate.of(2025, 11, 30);
		private final LocalDate GP_END = LocalDate.of(2026, 7, 31);

		private record BetreuungPensumTestData(int pensum, LocalDate gueltigAb,
											   LocalDate gueltigBis) {
		}

		@Test
		void geschwisterbonus_ShouldHaveGeschwisterbonus2FromErstantrag_WhenZeitabschnittBeforeMutationsdatumAndAnzahlGeschwisterSinks() {
			var mutation = this.createMutation(OCT_15);
			var tamaraContainer = createKind("Tamara", "Feutz");
			tamaraContainer.setGesuch(mutation);
			var betreuungTamara = createBetreuungForKind(
				new BetreuungPensumTestData(40, GP_START, GP_END),
				tamaraContainer
			);

			Verfuegung verfuegungErstantrag =
				createVerfuegungWithOneAbschnittAndGeschwisterBonus(true, true);
			betreuungTamara.initVorgaengerVerfuegungen(
				verfuegungErstantrag,
				null
			);

			VerfuegungZeitabschnitt zeitabschnittBeforeMutationsDatum =
				new VerfuegungZeitabschnitt(new DateRange(GP_START, NOV_30));
			zeitabschnittBeforeMutationsDatum
				.setGeschwisternBonusKind2ForAsivAndGemeinde(false);

			List<VerfuegungZeitabschnitt> zeitabschnitte = MUTATIONS_MERGER
				.execute(
					betreuungTamara,
					List.of(zeitabschnittBeforeMutationsDatum)
				);

			assertThat(
				zeitabschnitte.get(0)
					.getBgCalculationInputAsiv()
					.isGeschwisternBonusKind2(),
				is(true)
			);
		}

		@Test
		void geschwisterbonus_ShouldHaveGeschwisterbonus3FromErstantrag_WhenZeitabschnittBeforeMutationsdatumAndAnzahlGeschwisterSinks() {
			var mutation = this.createMutation(OCT_15);
			var tamaraContainer = createKind("Tamara", "Feutz");
			tamaraContainer.setGesuch(mutation);
			var betreuungTamara = createBetreuungForKind(
				new BetreuungPensumTestData(40, GP_START, GP_END),
				tamaraContainer
			);

			Verfuegung verfuegungErstantrag =
				createVerfuegungWithOneAbschnittAndGeschwisterBonus(true, true);
			betreuungTamara.initVorgaengerVerfuegungen(
				verfuegungErstantrag,
				null
			);

			VerfuegungZeitabschnitt zeitabschnittBeforeMutationsDatum =
				new VerfuegungZeitabschnitt(new DateRange(GP_START, NOV_30));
			zeitabschnittBeforeMutationsDatum
				.setGeschwisternBonusKind3ForAsivAndGemeinde(false);

			List<VerfuegungZeitabschnitt> zeitabschnitte = MUTATIONS_MERGER
				.execute(
					betreuungTamara,
					List.of(zeitabschnittBeforeMutationsDatum)
				);

			assertThat(
				zeitabschnitte.get(0)
					.getBgCalculationInputAsiv()
					.isGeschwisternBonusKind3(),
				is(true)
			);
		}

		@Test
		void geschwisterbonus_ShouldHaveGeschwisterbonus2FromErstantrag_WhenZeitabschnittBeforeMutationsdatumAndAnzahlGeschwisterIncreases() {
			var mutation = this.createMutation(OCT_15);
			var tamaraContainer = createKind("Tamara", "Feutz");
			tamaraContainer.setGesuch(mutation);
			var betreuungTamara = createBetreuungForKind(
				new BetreuungPensumTestData(40, GP_START, GP_END),
				tamaraContainer
			);

			Verfuegung verfuegungErstantrag =
				createVerfuegungWithOneAbschnittAndGeschwisterBonus(
					false,
					false
				);
			betreuungTamara.initVorgaengerVerfuegungen(
				verfuegungErstantrag,
				null
			);

			VerfuegungZeitabschnitt zeitabschnittBeforeMutationsDatum =
				new VerfuegungZeitabschnitt(new DateRange(GP_START, NOV_30));
			zeitabschnittBeforeMutationsDatum
				.setGeschwisternBonusKind2ForAsivAndGemeinde(true);

			List<VerfuegungZeitabschnitt> zeitabschnitte = MUTATIONS_MERGER
				.execute(
					betreuungTamara,
					List.of(zeitabschnittBeforeMutationsDatum)
				);

			assertThat(
				zeitabschnitte.get(0)
					.getBgCalculationInputAsiv()
					.isGeschwisternBonusKind2(),
				is(false)
			);
		}

		@Test
		void geschwisterbonus_ShouldHaveGeschwisterbonus3FromErstantrag_WhenZeitabschnittBeforeMutationsdatumAndAnzahlGeschwisterIncreases() {
			var mutation = this.createMutation(OCT_15);
			var tamaraContainer = createKind("Tamara", "Feutz");
			tamaraContainer.setGesuch(mutation);
			var betreuungTamara = createBetreuungForKind(
				new BetreuungPensumTestData(40, GP_START, GP_END),
				tamaraContainer
			);

			Verfuegung verfuegungErstantrag =
				createVerfuegungWithOneAbschnittAndGeschwisterBonus(
					false,
					false
				);
			betreuungTamara.initVorgaengerVerfuegungen(
				verfuegungErstantrag,
				null
			);

			VerfuegungZeitabschnitt zeitabschnittBeforeMutationsDatum =
				new VerfuegungZeitabschnitt(new DateRange(GP_START, NOV_30));
			zeitabschnittBeforeMutationsDatum
				.setGeschwisternBonusKind3ForAsivAndGemeinde(true);

			List<VerfuegungZeitabschnitt> zeitabschnitte = MUTATIONS_MERGER
				.execute(
					betreuungTamara,
					List.of(zeitabschnittBeforeMutationsDatum)
				);

			assertThat(
				zeitabschnitte.get(0)
					.getBgCalculationInputAsiv()
					.isGeschwisternBonusKind3(),
				is(false)
			);
		}

		private Verfuegung createVerfuegungWithOneAbschnittAndGeschwisterBonus(
			boolean hasBonusKind2,
			boolean hasBonusKind3
		) {
			VerfuegungZeitabschnitt zeitabschnittErstantrag =
				new VerfuegungZeitabschnitt(new DateRange(GP_START, GP_END));
			BGCalculationResult bgCalculationResult = new BGCalculationResult();
			bgCalculationResult.setGeschwisterBonusKind2(hasBonusKind2);
			bgCalculationResult.setGeschwisterBonusKind3(hasBonusKind3);
			// needs to be set to avoid getting null-pointer
			bgCalculationResult.setAnzahlGeschwisterFuerBonusSchwyz(0);
			zeitabschnittErstantrag.setBgCalculationResultAsiv(
				bgCalculationResult
			);
			Verfuegung verfuegung = new Verfuegung();
			verfuegung.setZeitabschnitte(List.of(zeitabschnittErstantrag));
			return verfuegung;
		}

		private Gesuch createMutation(LocalDate mutationsDatum) {
			Fall fall = new Fall();
			fall.setMandant(TestDataUtil.getMandantLuzern());

			Dossier dossier = new Dossier();
			dossier.setFall(fall);

			var mutation = new Gesuch();
			mutation.setEingangsdatum(mutationsDatum);
			mutation.setTyp(AntragTyp.MUTATION);
			mutation.setDossier(dossier);
			return mutation;
		}

		private KindContainer createKind(String vorname, String nachname) {
			var container = new KindContainer();
			var kindJa = new Kind();
			kindJa.setVorname(vorname);
			kindJa.setNachname(nachname);

			container.setKindJA(kindJa);
			return container;
		}

		private Betreuung createBetreuungForKind(
			BetreuungPensumTestData pensumData,
			KindContainer kindContainer
		) {
			var betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
				pensumData.gueltigAb,
				pensumData.gueltigBis,
				BetreuungsangebotTyp.KITA,
				pensumData.pensum,
				BigDecimal.valueOf(1000)
			);
			betreuung.setKind(kindContainer);
			return betreuung;
		}
	}

}
