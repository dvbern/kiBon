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
import java.util.Objects;
import java.util.function.Consumer;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.GeschwisterbonusTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.EbeguRuleTestsHelper;
import ch.dvbern.ebegu.rules.MonatsRule;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static ch.dvbern.ebegu.enums.MsgKey.BEDARFSSTUFE_AENDERUNG_MSG;
import static ch.dvbern.ebegu.enums.MsgKey.BEDARFSSTUFE_MSG;
import static ch.dvbern.ebegu.enums.MsgKey.BEDARFSSTUFE_NICHT_GEWAEHRT_MSG;
import static ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe.BEDARFSSTUFE_1;
import static ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe.BEDARFSSTUFE_2;
import static ch.dvbern.ebegu.enums.betreuung.Bedarfsstufe.KEINE;
import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;
import static ch.dvbern.ebegu.test.TestDataUtil.getMandantSchwyz;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

/**
 * Tests fuer Verfügungsmuster
 */
class MutationsMergerSchwyzTest {

	private static final LocalDate EINREICHEDATUM_INNERHALB_ERSTGESUCH =
		START_PERIODE.plusMonths(1).plusDays(5);

	private static final MonatsRule MONATS_RULE = new MonatsRule(false);

	private static final MutationsMerger MUTATIONS_MERGER = new MutationsMerger(
		Locale.GERMAN,
		false,
		new MutationsMergerParameter(false, GeschwisterbonusTyp.SCHWYZ_2)
	);

	private static final BigDecimal DEFAULT_MASGEBENDES_EINKOMMEN = Objects
		.requireNonNull(MathUtil.DEFAULT.from(50000));
	private static final BigDecimal ZWEI_HUNDERT_TAUSEND = Objects
		.requireNonNull(MathUtil.DEFAULT.from(200000));

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
		void geschwisterbonus_ShouldHaveAnzahlGeschwisterFromMutation_WhenZeitabschnittBeforeMutationsdatumAndAnzahlGeschwisterSinks() {
			final int ANZAHL_GESCHWISTER_IN_MUTATION = 0;
			var mutation = this.createMutation(OCT_15);
			var tamaraContainer = createKind("Tamara", "Feutz");
			tamaraContainer.setGesuch(mutation);
			var betreuungTamara = createBetreuungForKind(
				new BetreuungPensumTestData(40, GP_START, GP_END),
				tamaraContainer
			);

			Verfuegung verfuegungErstantrag =
				createVerfuegungWithOneAbschnittAndGeschwisterBonus(1);
			betreuungTamara.initVorgaengerVerfuegungen(
				verfuegungErstantrag,
				null
			);

			VerfuegungZeitabschnitt zeitabschnittBeforeMutationsDatum =
				new VerfuegungZeitabschnitt(new DateRange(GP_START, NOV_30));
			zeitabschnittBeforeMutationsDatum.setAnzahlGeschwister(
				ANZAHL_GESCHWISTER_IN_MUTATION
			);

			List<VerfuegungZeitabschnitt> zeitabschnitte = MUTATIONS_MERGER
				.execute(
					betreuungTamara,
					List.of(
						zeitabschnittBeforeMutationsDatum
					)
				);

			assertThat(
				zeitabschnitte.get(0)
					.getBgCalculationInputAsiv()
					.getAnzahlGeschwister(),
				is(ANZAHL_GESCHWISTER_IN_MUTATION)
			);
		}

		@Test
		void geschwisterbonus_ShouldHaveAnzahlGeschwisterFromMutation_WhenZeitabschnittBeforeMutationsdatumAndAnzahlGeschwisterIncreases() {
			final int ANZAHL_GESCHWISTER_IN_MUTATION = 1;
			var mutation = this.createMutation(OCT_15);
			var tamaraContainer = createKind("Tamara", "Feutz");
			tamaraContainer.setGesuch(mutation);
			var betreuungTamara = createBetreuungForKind(
				new BetreuungPensumTestData(40, GP_START, GP_END),
				tamaraContainer
			);

			Verfuegung verfuegungErstantrag =
				createVerfuegungWithOneAbschnittAndGeschwisterBonus(0);
			betreuungTamara.initVorgaengerVerfuegungen(
				verfuegungErstantrag,
				null
			);

			VerfuegungZeitabschnitt zeitabschnittBeforeMutationsDatum =
				new VerfuegungZeitabschnitt(new DateRange(GP_START, GP_END));
			zeitabschnittBeforeMutationsDatum.setAnzahlGeschwister(
				ANZAHL_GESCHWISTER_IN_MUTATION
			);

			List<VerfuegungZeitabschnitt> zeitabschnitte = MUTATIONS_MERGER
				.execute(
					betreuungTamara,
					List.of(zeitabschnittBeforeMutationsDatum)
				);

			assertThat(
				zeitabschnitte.get(0)
					.getBgCalculationInputAsiv()
					.getAnzahlGeschwister(),
				is(ANZAHL_GESCHWISTER_IN_MUTATION)
			);
		}

		private Verfuegung createVerfuegungWithOneAbschnittAndGeschwisterBonus(
			int anzahlGeschwister
		) {
			VerfuegungZeitabschnitt zeitabschnittErstantrag =
				new VerfuegungZeitabschnitt(new DateRange(GP_START, GP_END));
			BGCalculationResult bgCalculationResult = new BGCalculationResult();
			bgCalculationResult.setAnzahlGeschwisterFuerBonusSchwyz(
				anzahlGeschwister
			);
			zeitabschnittErstantrag.setBgCalculationResultAsiv(
				bgCalculationResult
			);
			Verfuegung verfuegung = new Verfuegung();
			verfuegung.setZeitabschnitte(List.of(zeitabschnittErstantrag));
			return verfuegung;
		}

		private Gesuch createMutation(LocalDate mutationsDatum) {
			Fall fall = new Fall();
			fall.setMandant(TestDataUtil.getMandantSchwyz());

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

	@Test
	void test_hoereMassgegebeneseinkommens_steigtFolgeMonatNachMutation() {
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil
			.prepareErstGesuchVerfuegung(
				100,
				DEFAULT_MASGEBENDES_EINKOMMEN
			);
		//EK Mutation = 50000
		Betreuung mutierteBetreuung =
			MutationsMergerTestUtil.prepareData(
				ZWEI_HUNDERT_TAUSEND,
				AntragTyp.MUTATION
			);
		mutierteBetreuung.extractGesuch()
			.getFall()
			.setMandant(
				TestDataUtil.createMandant(MandantIdentifier.SCHWYZ)
			);
		//Mutation im August eingereicht, BG soltle von 17.9 zu 1.9 nicht rueckwirkend angepasst werden
		mutierteBetreuung.extractGesuch()
			.setEingangsdatum(EINREICHEDATUM_INNERHALB_ERSTGESUCH);
		mutierteBetreuung.initVorgaengerVerfuegungen(
			verfuegungErstGesuch,
			null
		);
		List<VerfuegungZeitabschnitt> zeitabschnitts = EbeguRuleTestsHelper
			.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> zeitabschnitteAfterMonatsRule =
			EbeguRuleTestsHelper.runSingleAbschlussRule(
				MONATS_RULE,
				mutierteBetreuung,
				zeitabschnitts
			);
		mutierteBetreuung.extractGesuch()
			.getDossier()
			.getFall()
			.setMandant(getMandantSchwyz());

		// Der FinSit Rechner von Bern ist verwendet, wir wollen hier aber die MonatMerger Von Schwyz testen
		mutierteBetreuung.extractGesuch()
			.setFinSitTyp(FinanzielleSituationTyp.SCHWYZ);
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.runSingleAbschlussRule(
				MUTATIONS_MERGER,
				mutierteBetreuung,
				zeitabschnitteAfterMonatsRule
			);

		//Zeitabschnitt Flag zuSpät = false
		assertThat(zeitabschnitte.size(), is(12));
		checkAllMassgegebenesEinommenBefore(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH,
			MathUtil.GANZZAHL.from(DEFAULT_MASGEBENDES_EINKOMMEN)
		);
		checkAllMassgegebenesEinkommenAfter(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH,
			MathUtil.GANZZAHL.from(ZWEI_HUNDERT_TAUSEND)
		);
	}

	@Test
	void test_kleinereMassgegebeneseinkommens_steigtFolgeMonatNachMutation() {
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil
			.prepareErstGesuchVerfuegung(100, ZWEI_HUNDERT_TAUSEND);
		//EK Mutation = 50000
		Betreuung mutierteBetreuung =
			MutationsMergerTestUtil.prepareData(
				DEFAULT_MASGEBENDES_EINKOMMEN,
				AntragTyp.MUTATION
			);
		mutierteBetreuung.extractGesuch()
			.getFall()
			.setMandant(
				TestDataUtil.createMandant(MandantIdentifier.SCHWYZ)
			);
		//Mutation im August eingereicht, BG soltle von 17.9 zu 1.9 rueckwirkend angepasst werden
		mutierteBetreuung.extractGesuch()
			.setEingangsdatum(EINREICHEDATUM_INNERHALB_ERSTGESUCH);
		mutierteBetreuung.initVorgaengerVerfuegungen(
			verfuegungErstGesuch,
			null
		);
		mutierteBetreuung.extractGesuch()
			.getDossier()
			.getFall()
			.setMandant(getMandantSchwyz());

		List<VerfuegungZeitabschnitt> zeitabschnitts = EbeguRuleTestsHelper
			.calculate(mutierteBetreuung);
		final List<VerfuegungZeitabschnitt> zeitabschnitteAfterMonatsRule =
			EbeguRuleTestsHelper.runSingleAbschlussRule(
				MONATS_RULE,
				mutierteBetreuung,
				zeitabschnitts
			);

		// Der FinSit Rechner von Bern ist verwendet, wir wollen hier aber die MonatMerger Von Schwyz testen
		mutierteBetreuung.extractGesuch()
			.setFinSitTyp(FinanzielleSituationTyp.SCHWYZ);
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.runSingleAbschlussRule(
				MUTATIONS_MERGER,
				mutierteBetreuung,
				zeitabschnitteAfterMonatsRule
			);

		//Zeitabschnitt Flag zuSpät = false
		assertThat(zeitabschnitte.size(), is(12));
		checkAllMassgegebenesEinommenBefore(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH,
			MathUtil.GANZZAHL.from(ZWEI_HUNDERT_TAUSEND)
		);
		checkAllMassgegebenesEinkommenAfter(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH,
			MathUtil.GANZZAHL.from(DEFAULT_MASGEBENDES_EINKOMMEN)
		);
	}

	@ParameterizedTest
	@EnumSource(value = Bedarfsstufe.class, names = { "KEINE" }, mode = EXCLUDE)
	void mutationHoehereBeitrageGewaerhtErstantragKeineHoeherenBeitraege(
		Bedarfsstufe bedarfsstufe
	) {
		//Erstantrag keine höheren Beiträge
		//Mutations mit höheren Beiträge
		Betreuung erstGesuchBetreuung = MutationsMergerTestUtil.prepareData(
			DEFAULT_MASGEBENDES_EINKOMMEN,
			AntragTyp.ERSTGESUCH
		);
		erstGesuchBetreuung.extractGesuch()
			.getDossier()
			.getFall()
			.setMandant(getMandantSchwyz());
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil
			.prepareVerfuegungForBetreuung(erstGesuchBetreuung);

		Betreuung mutierteBetreuung = prepareDataMitBedarfsstufe(
			AntragTyp.MUTATION,
			bedarfsstufe
		);
		mutierteBetreuung.extractGesuch()
			.setEingangsdatum(EINREICHEDATUM_INNERHALB_ERSTGESUCH);
		mutierteBetreuung.initVorgaengerVerfuegungen(
			verfuegungErstGesuch,
			null
		);
		mutierteBetreuung.extractGesuch()
			.getDossier()
			.getFall()
			.setMandant(getMandantSchwyz());

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculateInklAllgemeineRegeln(mutierteBetreuung);
		assertNotNull(zeitabschnitte);
		zeitabschnitte.forEach(zeitabschnitt -> {
			if (zeitabschnitt.isZuSpaetEingereicht()) {
				assertThat(
					zeitabschnitt.getBgCalculationInputAsiv()
						.getBedarfsstufe(),
					nullValue()
				);
				assertBedarfsstufeAenderungMsg(zeitabschnitt);
			} else {
				assertThat(
					zeitabschnitt.getBgCalculationInputAsiv()
						.getBedarfsstufe(),
					is(bedarfsstufe)
				);
				assertBedarfsstufeMsg(zeitabschnitt);
			}
		});
	}

	@Test
	void mutationHoehereBeitrageNichtGewaerhtErstantragKeineHoeherenBeitraege() {
		//Erstantrag keine höheren Beiträge
		//Mutations höheren Beiträge nicht gewährt (Bedarfsstufe = KEINE)
		Betreuung erstGesuchBetreuung = MutationsMergerTestUtil.prepareData(
			DEFAULT_MASGEBENDES_EINKOMMEN,
			AntragTyp.ERSTGESUCH
		);
		erstGesuchBetreuung.extractGesuch()
			.getDossier()
			.getFall()
			.setMandant(getMandantSchwyz());
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil
			.prepareVerfuegungForBetreuung(erstGesuchBetreuung);

		Betreuung mutierteBetreuung = prepareDataMitBedarfsstufe(
			AntragTyp.MUTATION,
			KEINE
		);
		mutierteBetreuung.extractGesuch()
			.setEingangsdatum(EINREICHEDATUM_INNERHALB_ERSTGESUCH);
		mutierteBetreuung.initVorgaengerVerfuegungen(
			verfuegungErstGesuch,
			null
		);
		mutierteBetreuung.extractGesuch()
			.getDossier()
			.getFall()
			.setMandant(getMandantSchwyz());

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculateInklAllgemeineRegeln(mutierteBetreuung);
		assertNotNull(zeitabschnitte);
		zeitabschnitte.forEach(zeitabschnitt -> {
			if (zeitabschnitt.isZuSpaetEingereicht()) {
				assertThat(
					zeitabschnitt.getBgCalculationInputAsiv()
						.getBedarfsstufe(),
					nullValue()
				);
				assertBedarfsstufeAenderungMsg(zeitabschnitt);
			} else {
				assertThat(
					zeitabschnitt.getBgCalculationInputAsiv()
						.getBedarfsstufe(),
					is(Bedarfsstufe.KEINE)
				);
				assertBedarfsstufeNichtGewaehrtMsg(zeitabschnitt);
			}
		});
	}

	@Test
	void mutationHoehereBeitrageWechselDerBeadarfsstufe() {
		//Erstantrag mit höheren Beiträge Bedarfsstufe 1
		//Mutations mit höheren Beiträge Bedarfsstufe 2
		Betreuung erstGesuchBetreuung = prepareDataMitBedarfsstufe(
			AntragTyp.ERSTGESUCH,
			BEDARFSSTUFE_1
		);
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil
			.prepareVerfuegungForBetreuung(erstGesuchBetreuung);

		Betreuung mutierteBetreuung = prepareDataMitBedarfsstufe(
			AntragTyp.MUTATION,
			BEDARFSSTUFE_2
		);
		mutierteBetreuung.extractGesuch()
			.setEingangsdatum(EINREICHEDATUM_INNERHALB_ERSTGESUCH);
		mutierteBetreuung.initVorgaengerVerfuegungen(
			verfuegungErstGesuch,
			null
		);
		mutierteBetreuung.extractGesuch()
			.getDossier()
			.getFall()
			.setMandant(getMandantSchwyz());

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculateInklAllgemeineRegeln(mutierteBetreuung);
		assertNotNull(zeitabschnitte);
		zeitabschnitte.forEach(zeitabschnitt -> {
			if (zeitabschnitt.isZuSpaetEingereicht()) {
				assertThat(
					zeitabschnitt.getBgCalculationInputAsiv()
						.getBedarfsstufe(),
					is(BEDARFSSTUFE_1)
				);
				assertBedarfsstufeAenderungMsg(zeitabschnitt);
			} else {
				assertThat(
					zeitabschnitt.getBgCalculationInputAsiv()
						.getBedarfsstufe(),
					is(BEDARFSSTUFE_2)
				);
				assertBedarfsstufeMsg(zeitabschnitt);
			}
		});
	}

	@Test
	void mutationHoehereBeitrageKeinWechselDerBeadarfsstufe() {
		//Erstantrag mit höheren Beiträge Bedarfsstufe 1
		//Mutations mit höheren Beiträge Bedarfsstufe 1
		Betreuung erstGesuchBetreuung = prepareDataMitBedarfsstufe(
			AntragTyp.ERSTGESUCH,
			BEDARFSSTUFE_1
		);
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil
			.prepareVerfuegungForBetreuung(erstGesuchBetreuung);

		Betreuung mutierteBetreuung = prepareDataMitBedarfsstufe(
			AntragTyp.MUTATION,
			BEDARFSSTUFE_1
		);
		mutierteBetreuung.extractGesuch()
			.setEingangsdatum(EINREICHEDATUM_INNERHALB_ERSTGESUCH);
		mutierteBetreuung.initVorgaengerVerfuegungen(
			verfuegungErstGesuch,
			null
		);
		mutierteBetreuung.extractGesuch()
			.getDossier()
			.getFall()
			.setMandant(getMandantSchwyz());

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculateInklAllgemeineRegeln(mutierteBetreuung);
		assertNotNull(zeitabschnitte);
		zeitabschnitte.forEach(zeitabschnitt -> {
			assertThat(
				zeitabschnitt.getBgCalculationInputAsiv().getBedarfsstufe(),
				is(BEDARFSSTUFE_1)
			);
			assertBedarfsstufeMsg(zeitabschnitt);
		});
	}

	@Test
	void mutationHoehereBeitrageWechselDerBeadarfsstufeAufNichtGewaehrt() {
		//Erstantrag mit höheren Beiträge Bedarfsstufe 1
		//Mutations mit höheren Beiträge Bedarfsstufe KEINE
		Betreuung erstGesuchBetreuung = prepareDataMitBedarfsstufe(
			AntragTyp.ERSTGESUCH,
			BEDARFSSTUFE_1
		);
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil
			.prepareVerfuegungForBetreuung(erstGesuchBetreuung);

		Betreuung mutierteBetreuung = prepareDataMitBedarfsstufe(
			AntragTyp.MUTATION,
			KEINE
		);
		mutierteBetreuung.extractGesuch()
			.setEingangsdatum(EINREICHEDATUM_INNERHALB_ERSTGESUCH);
		mutierteBetreuung.initVorgaengerVerfuegungen(
			verfuegungErstGesuch,
			null
		);
		mutierteBetreuung.extractGesuch()
			.getDossier()
			.getFall()
			.setMandant(getMandantSchwyz());

		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculateInklAllgemeineRegeln(mutierteBetreuung);
		assertNotNull(zeitabschnitte);
		zeitabschnitte.forEach(zeitabschnitt -> {
			if (zeitabschnitt.isZuSpaetEingereicht()) {
				assertThat(
					zeitabschnitt.getBgCalculationInputAsiv()
						.getBedarfsstufe(),
					is(BEDARFSSTUFE_1)
				);
				assertBedarfsstufeAenderungMsg(zeitabschnitt);
			} else {
				assertThat(
					zeitabschnitt.getBgCalculationInputAsiv()
						.getBedarfsstufe(),
					is(KEINE)
				);
				assertBedarfsstufeNichtGewaehrtMsg(zeitabschnitt);
			}
		});
	}

	private void assertBedarfsstufeMsg(VerfuegungZeitabschnitt zeitabschnitt) {
		assertTrue(
			zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(BEDARFSSTUFE_MSG)
		);
		assertFalse(
			zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(BEDARFSSTUFE_NICHT_GEWAEHRT_MSG)
		);
		assertFalse(
			zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(BEDARFSSTUFE_AENDERUNG_MSG)
		);
	}

	private void assertBedarfsstufeAenderungMsg(
		VerfuegungZeitabschnitt zeitabschnitt
	) {
		assertTrue(
			zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(BEDARFSSTUFE_AENDERUNG_MSG)
		);
		assertFalse(
			zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(BEDARFSSTUFE_MSG)
		);
		assertFalse(
			zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(BEDARFSSTUFE_NICHT_GEWAEHRT_MSG)
		);
	}

	private void assertBedarfsstufeNichtGewaehrtMsg(
		VerfuegungZeitabschnitt zeitabschnitt
	) {
		assertFalse(
			zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(BEDARFSSTUFE_AENDERUNG_MSG)
		);
		assertFalse(
			zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(BEDARFSSTUFE_MSG)
		);
		assertTrue(
			zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(BEDARFSSTUFE_NICHT_GEWAEHRT_MSG)
		);
	}

	@Test
	void mutationAnpassungHoehereBeitrag_steigt_folgeMonat() {
		Bedarfsstufe bedarfsstufeErstGesuch = Bedarfsstufe.BEDARFSSTUFE_1;
		Bedarfsstufe bedarfsstufeMutation = Bedarfsstufe.BEDARFSSTUFE_2;

		Betreuung betreuung = prepareDataMitBedarfsstufe(
			AntragTyp.ERSTGESUCH,
			bedarfsstufeErstGesuch
		);
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil
			.prepareVerfuegungForBetreuung(betreuung);

		Betreuung mutierteBetreuung = prepareDataMitBedarfsstufe(
			AntragTyp.MUTATION,
			bedarfsstufeMutation
		);
		mutierteBetreuung.extractGesuch()
			.setEingangsdatum(EINREICHEDATUM_INNERHALB_ERSTGESUCH);
		mutierteBetreuung.initVorgaengerVerfuegungen(
			verfuegungErstGesuch,
			null
		);
		mutierteBetreuung.extractGesuch()
			.getDossier()
			.getFall()
			.setMandant(getMandantSchwyz());
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculateInklAllgemeineRegeln(mutierteBetreuung);

		checkAllBedarfsstufeBefore(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH,
			bedarfsstufeErstGesuch
		);
		checkAllBedarfsstufeAfter(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH,
			bedarfsstufeMutation
		);
	}

	@Test
	void mutationAnpassungHoehereBeitrag_sink_folgeMonat() {
		Bedarfsstufe bedarfsstufeErstGesuch = Bedarfsstufe.BEDARFSSTUFE_2;
		Bedarfsstufe bedarfsstufeMutation = Bedarfsstufe.BEDARFSSTUFE_1;

		Betreuung betreuung = prepareDataMitBedarfsstufe(
			AntragTyp.ERSTGESUCH,
			bedarfsstufeErstGesuch
		);
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil
			.prepareVerfuegungForBetreuung(betreuung);

		Betreuung mutierteBetreuung = prepareDataMitBedarfsstufe(
			AntragTyp.MUTATION,
			bedarfsstufeMutation
		);
		mutierteBetreuung.extractGesuch()
			.setEingangsdatum(EINREICHEDATUM_INNERHALB_ERSTGESUCH);
		mutierteBetreuung.initVorgaengerVerfuegungen(
			verfuegungErstGesuch,
			null
		);
		mutierteBetreuung.extractGesuch()
			.getDossier()
			.getFall()
			.setMandant(getMandantSchwyz());
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculateInklAllgemeineRegeln(mutierteBetreuung);

		checkAllBedarfsstufeBefore(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH,
			bedarfsstufeErstGesuch
		);
		checkAllBedarfsstufeAfter(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH,
			bedarfsstufeMutation
		);
	}

	@Test
	void mutationEinreichfristBemerkungTest() {
		//Erstantrag keine höheren Beiträge
		//Mutations mit höheren Beiträge
		Betreuung erstGesuchBetreuung = MutationsMergerTestUtil.prepareData(
			DEFAULT_MASGEBENDES_EINKOMMEN,
			AntragTyp.ERSTGESUCH
		);
		erstGesuchBetreuung.extractGesuch()
			.getDossier()
			.getFall()
			.setMandant(getMandantSchwyz());
		Verfuegung verfuegungErstGesuch = MutationsMergerTestUtil
			.prepareVerfuegungForBetreuung(erstGesuchBetreuung);

		Betreuung mutierteBetreuung = MutationsMergerTestUtil.prepareData(
			DEFAULT_MASGEBENDES_EINKOMMEN,
			AntragTyp.MUTATION
		);
		mutierteBetreuung.extractGesuch()
			.setEingangsdatum(EINREICHEDATUM_INNERHALB_ERSTGESUCH);
		mutierteBetreuung.initVorgaengerVerfuegungen(
			verfuegungErstGesuch,
			null
		);
		mutierteBetreuung.extractGesuch()
			.getDossier()
			.getFall()
			.setMandant(getMandantSchwyz());
		List<VerfuegungZeitabschnitt> zeitabschnitte = EbeguRuleTestsHelper
			.calculateInklAllgemeineRegeln(mutierteBetreuung);
		checkAllBefore(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH,
			za -> assertThat(
				"Einreinchungsfrist Bemerkung fehlt in die Zeitabschnitt "
					+ za,
				za.getBemerkungenDTOList()
					.containsMsgKey(MsgKey.MUTATION_FOLGEMONAT_MSG),
				is(true)
			)
		);

		checkAllAfter(
			zeitabschnitte,
			EINREICHEDATUM_INNERHALB_ERSTGESUCH,
			za -> assertThat(
				"Einreinchungsfrist Bemerkung sollte nicht in die Zeitabschnitt sein "
					+ za,
				za.getBemerkungenDTOList()
					.containsMsgKey(MsgKey.MUTATION_FOLGEMONAT_MSG),
				is(false)
			)
		);
	}

	private Betreuung prepareDataMitBedarfsstufe(
		AntragTyp antragTyp,
		Bedarfsstufe bedarfsstufe
	) {
		Mandant schwyz = TestDataUtil.createMandant(MandantIdentifier.SCHWYZ);
		Betreuung betreuung = MutationsMergerTestUtil.prepareData(
			DEFAULT_MASGEBENDES_EINKOMMEN,
			antragTyp
		);
		betreuung.extractGesuch().getFall().setMandant(schwyz);
		betreuung.getInstitutionStammdaten()
			.getInstitution()
			.setMandant(schwyz);
		betreuung.getKind()
			.getKindJA()
			.setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
		betreuung.setBedarfsstufe(bedarfsstufe);
		Objects.requireNonNull(
			betreuung.getErweiterteBetreuungContainer()
				.getErweiterteBetreuungJA()
		).setErweiterteBeduerfnisseBestaetigt(true);
		return betreuung;
	}

	private void checkAllMassgegebenesEinommenBefore(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate mutationDatum,
		BigDecimal massgegebenesEinkommenVorAbzugFamgr
	) {

		checkAllBefore(
			zeitabschnitte,
			mutationDatum,
			za -> assertThat(
				"Falsches MassgebendesEinkommenVorAbzFamgr in Zeitabschnitt "
					+ za,
				za.getMassgebendesEinkommenVorAbzFamgr(),
				is(massgegebenesEinkommenVorAbzugFamgr)
			)
		);
	}

	private void checkAllBedarfsstufeBefore(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate mutationDatum,
		Bedarfsstufe bedarfsstufe
	) {

		checkAllBefore(
			zeitabschnitte,
			mutationDatum,
			za -> assertThat(
				"Falsches HoehereBeitrag in Zeitabschnitt " + za,
				za.getBedarfsstufe(),
				is(bedarfsstufe)
			)
		);
	}

	private void checkAllBefore(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate mutationDatum,
		Consumer<? super VerfuegungZeitabschnitt> assertion
	) {

		zeitabschnitte.stream()
			.filter(
				za -> za.getGueltigkeit()
					.getGueltigAb()
					.getMonth()
					.getValue()
					<= mutationDatum.getMonth().getValue()
					&&
					za.getGueltigkeit().getGueltigAb().getYear()
						== mutationDatum.getYear()
			)
			.forEach(assertion);
	}

	private void checkAllMassgegebenesEinkommenAfter(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate mutationDatum,
		BigDecimal massgegebenesEinkommenVorAbzugFamgr
	) {

		checkAllAfter(
			zeitabschnitte,
			mutationDatum,
			za -> assertThat(
				"Falsches MassgebendesEinkommenVorAbzFamgr in Zeitabschnitt "
					+ za,
				za.getMassgebendesEinkommenVorAbzFamgr(),
				is(massgegebenesEinkommenVorAbzugFamgr)
			)
		);
	}

	private void checkAllBedarfsstufeAfter(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate mutationDatum,
		Bedarfsstufe bedarfsstufe
	) {
		checkAllAfter(
			zeitabschnitte,
			mutationDatum,
			za -> assertThat(
				"Falsches Bedarfsstufe in Zeitabschnitt " + za,
				za.getBedarfsstufe(),
				is(bedarfsstufe)
			)
		);
	}

	private void checkAllAfter(
		List<VerfuegungZeitabschnitt> zeitabschnitte,
		LocalDate mutationDatum,
		java.util.function.Consumer<? super VerfuegungZeitabschnitt> assertion
	) {
		zeitabschnitte.stream()
			.filter(
				za -> za.getGueltigkeit()
					.getGueltigAb()
					.getMonth()
					.getValue()
					> mutationDatum.getMonth().getValue()
					&&
					za.getGueltigkeit().getGueltigAb().getYear()
						== mutationDatum.getYear()
			)
			.forEach(assertion);
	}
}
