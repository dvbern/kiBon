package ch.dvbern.ebegu.rules.familienabzug;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static ch.dvbern.ebegu.enums.KinderabzugTyp.FKJV;
import static ch.dvbern.ebegu.enums.KinderabzugTyp.FKJV_2;
import static ch.dvbern.ebegu.rules.familienabzug.FamilienabzugAbschnittRuleTestUtil.assertEqualsNumberValue;
import static ch.dvbern.ebegu.rules.familienabzug.FamilienabzugAbschnittRuleTestUtil.getDefaultEinstellungMap;
import static ch.dvbern.ebegu.rules.familienabzug.FamiliensitutionTestUtil.getAlleinerziehendUnterhaltsvereinbarungNein;
import static ch.dvbern.ebegu.rules.familienabzug.FamiliensitutionTestUtil.getAlleinerziehendZuZweit;
import static ch.dvbern.ebegu.rules.familienabzug.FamiliensitutionTestUtil.getKonkubinat;
import static ch.dvbern.ebegu.rules.familienabzug.FamiliensitutionTestUtil.getKonkubinatOhneKindOverMinDauer;
import static ch.dvbern.ebegu.rules.familienabzug.FamiliensitutionTestUtil.getKonkubinatOhneKindUnderMinDauerUnterhaltsvereinbarungNein;
import static ch.dvbern.ebegu.rules.familienabzug.FamiliensitutionTestUtil.getKonkubinatOhneKindUnderMinDauerZuZweit;
import static ch.dvbern.ebegu.rules.familienabzug.FamiliensitutionTestUtil.getVerheiratet;
import static ch.dvbern.ebegu.test.TestDataUtil.ENDE_PERIODE;
import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;
import static java.time.temporal.TemporalAdjusters.firstDayOfNextMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FamilienabzugAbschnittRuleFKJVTest {

	private final GesuchstellerAbzugAbschnittRule gsAbzugRule =
		new GesuchstellerAbzugAbschnittRule(
			Constants.DEFAULT_GUELTIGKEIT,
			Constants.DEFAULT_LOCALE
		);

	private final KinderabzugAbschnittRuleFKJV kinderAbzugRule =
		new KinderabzugAbschnittRuleFKJV(
			Constants.DEFAULT_GUELTIGKEIT,
			Constants.DEFAULT_LOCALE
		);

	private final FamilienabzugCalcRuleFKJV famCalcRule_FKJV =
		new FamilienabzugCalcRuleFKJV(
			getEinstellungMapForFKJV(),
			Constants.DEFAULT_GUELTIGKEIT,
			Constants.DEFAULT_LOCALE
		);

	private final FamilienabzugCalcRuleFKJV2 famCalcRule_FKJV2 =
		new FamilienabzugCalcRuleFKJV2(
			getEinstellungMapForFKJV2(),
			Constants.DEFAULT_GUELTIGKEIT,
			Constants.DEFAULT_LOCALE
		);

	private AbstractFamilienabzugCalcRuleBern getFamilienazugRule(
		KinderabzugTyp kinderabzugTyp
	) {
		if (kinderabzugTyp == KinderabzugTyp.FKJV) {
			return famCalcRule_FKJV;
		}
		return famCalcRule_FKJV2;
	}

	@ParameterizedTest
	@EnumSource(
		value = KinderabzugTyp.class,
		names = { "FKJV", "FKJV_2" },
		mode = EnumSource.Mode.INCLUDE
	)
	void obhutNichtAlternierend_shouldCountFull(KinderabzugTyp kinderabzugTyp) {
		Betreuung betreuung = createBetreuungWithOneGS();
		addGanzesGeschwister(betreuung);

		List<VerfuegungZeitabschnitt> zeitabschnitts = calculateAllAbzugRules(
			betreuung,
			kinderabzugTyp
		);

		assertEquals(1, zeitabschnitts.size());
		BGCalculationInput result =
			zeitabschnitts.get(0).getBgCalculationInputAsiv();
		assertEqualsNumberValue(3, result.getFamGroesseTotal());
		assertEqualsNumberValue(11400, result.getAbzugFamGroesseTotal());
	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	class obhutAlernierendTest {

		Betreuung betreuung;

		@BeforeEach
		void initObhutAlternierendeBetreuung() {
			betreuung = createBetreuungWithOneGS();
			betreuung.getKind().getKindJA().setObhutAlternierendAusueben(true);
		}

		@ParameterizedTest
		@EnumSource(
			value = KinderabzugTyp.class,
			names = { "FKJV", "FKJV_2" },
			mode = EnumSource.Mode.INCLUDE
		)
		void nichtFamilienergaenzendBetreuutGemeinsamesGesuch_shouldCountFull(
			KinderabzugTyp kinderabzugTyp
		) {
			betreuung.getKind().getKindJA().setGemeinsamesGesuch(true);
			betreuung.getKind()
				.getKindJA()
				.setFamilienErgaenzendeBetreuung(false);

			addGanzesGeschwister(betreuung);

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					kinderabzugTyp
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(3, result.getFamGroesseTotal());
			assertEqualsNumberValue(11400, result.getAbzugFamGroesseTotal());
		}

		@ParameterizedTest
		@EnumSource(
			value = KinderabzugTyp.class,
			names = { "FKJV", "FKJV_2" },
			mode = EnumSource.Mode.INCLUDE
		)
		void nichtFamilienergaenzendBetreuutKeinGemeinsamesGesuch_shouldCountHalf(
			KinderabzugTyp kinderabzugTyp
		) {
			betreuung.getKind().getKindJA().setGemeinsamesGesuch(false);
			betreuung.getKind()
				.getKindJA()
				.setFamilienErgaenzendeBetreuung(false);

			addGanzesGeschwister(betreuung);

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					kinderabzugTyp
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(2.5, result.getFamGroesseTotal());
			assertEqualsNumberValue(9500, result.getAbzugFamGroesseTotal());
		}

		@ParameterizedTest
		@MethodSource("familiensituation_obhutAlternirend_zweiGS_countsHalf")
		void familienergaenzendBetreuut_zweiGesuchsteller_countsHalf(
			Familiensituation familiensituation
		) {
			betreuung.getKind()
				.getKindJA()
				.setFamilienErgaenzendeBetreuung(true);
			betreuung.extractGesuch()
				.getFamiliensituationContainer()
				.setFamiliensituationJA(familiensituation);

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					FKJV_2
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(2.5, result.getFamGroesseTotal());
			assertEqualsNumberValue(9500, result.getAbzugFamGroesseTotal());
		}

		@ParameterizedTest
		@MethodSource(
			"ch.dvbern.ebegu.rules.familienabzug.FamiliensitutionTestUtil#getAllFamiliensituationsWithOneGesuchstellerBernFKJV"
		)
		void familienergaenzendBetreuut_einGesuchsteller_countsHalf(
			Familiensituation familiensituation
		) {
			betreuung.getKind()
				.getKindJA()
				.setFamilienErgaenzendeBetreuung(true);
			betreuung.extractGesuch()
				.getFamiliensituationContainer()
				.setFamiliensituationJA(familiensituation);

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					FKJV_2
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(1.5, result.getFamGroesseTotal());
			assertEqualsNumberValue(0, result.getAbzugFamGroesseTotal());
		}

		@ParameterizedTest
		@MethodSource(
			"familiensituation_obhutAlternierend_zweiGS_gemeinsamCounts"
		)
		void familienergaenzendBetreuut_zweiGesuchsteller_gemeinsamesGesuch_countsFull(
			Familiensituation familiensituation
		) {
			betreuung.getKind()
				.getKindJA()
				.setFamilienErgaenzendeBetreuung(true);
			betreuung.getKind().getKindJA().setGemeinsamesGesuch(true);
			betreuung.extractGesuch()
				.getFamiliensituationContainer()
				.setFamiliensituationJA(familiensituation);

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					FKJV_2
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(3, result.getFamGroesseTotal());
			assertEqualsNumberValue(11400, result.getAbzugFamGroesseTotal());
		}

		@ParameterizedTest
		@MethodSource(
			"familiensituation_obhutAlternierend_zweiGS_gemeinsamCounts"
		)
		void familienergaenzendBetreuut_zweiGesuchsteller_nichtGemeinsamesGesuch_countsHalf(
			Familiensituation familiensituation
		) {
			betreuung.getKind()
				.getKindJA()
				.setFamilienErgaenzendeBetreuung(true);
			betreuung.getKind().getKindJA().setGemeinsamesGesuch(false);
			betreuung.extractGesuch()
				.getFamiliensituationContainer()
				.setFamiliensituationJA(familiensituation);

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					FKJV_2
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(2.5, result.getFamGroesseTotal());
			assertEqualsNumberValue(9500, result.getAbzugFamGroesseTotal());
		}

		private Stream<Arguments> familiensituation_obhutAlternirend_zweiGS_countsHalf() {
			return Stream.of(
				getVerheiratet(),
				getKonkubinat(),
				getKonkubinatOhneKindOverMinDauer()
			);
		}

		private Stream<Arguments> familiensituation_obhutAlternierend_zweiGS_gemeinsamCounts() {
			return Stream.of(
				getKonkubinatOhneKindUnderMinDauerZuZweit(),
				getKonkubinatOhneKindUnderMinDauerUnterhaltsvereinbarungNein(),
				getAlleinerziehendZuZweit(),
				getAlleinerziehendUnterhaltsvereinbarungNein()
			);
		}
	}

	@Nested
	class inErstausbildungAnswerdedTest {
		@Test
		void kinderAbzugFKJV2_KindUeber18_notInErstusbildung_zaehltNicht() {
			Betreuung betreuung = createBetreuungWithOneGS();

			addVolljaehrigesKind(false, betreuung.extractGesuch());

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					FKJV_2
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(2, result.getFamGroesseTotal());
			assertEqualsNumberValue(0, result.getAbzugFamGroesseTotal());
		}

		@Test
		void kinderAbzugFKJV_KindUeber18_notInErstusbildung_zaehltZuFamilienPauschaleAberNichtZuAbzug() {
			Betreuung betreuung = createBetreuungWithOneGS();

			addVolljaehrigesKind(false, betreuung.extractGesuch());

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					FKJV
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(2, result.getFamGroesseTotal());
			assertEqualsNumberValue(7600, result.getAbzugFamGroesseTotal());
		}

		@ParameterizedTest
		@EnumSource(
			value = KinderabzugTyp.class,
			names = { "FKJV", "FKJV_2" },
			mode = EnumSource.Mode.INCLUDE
		)
		void kindUnter18_notInErstusbildung_zaehltGanz(KinderabzugTyp typ) {
			Betreuung betreuung = createBetreuungWithOneGS();

			addNichtVolljaehigesKind(false, betreuung.extractGesuch());

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					typ
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(3, result.getFamGroesseTotal());
			assertEqualsNumberValue(11400, result.getAbzugFamGroesseTotal());
		}

		@Test
		void kindUeber18_inErstausbildung_keineAlimenteBezahlt_zaehltNicht() {
			Betreuung betreuung = createBetreuungWithOneGS();

			KindContainer kindVollJahrig = addVolljaehrigesKind(
				true,
				betreuung.extractGesuch()
			);
			kindVollJahrig.getKindJA().setAlimenteBezahlen(false);

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					FKJV_2
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(2, result.getFamGroesseTotal());
			assertEqualsNumberValue(0, result.getAbzugFamGroesseTotal());
		}

		@Test
		void kindUnter18_inErstausbildung_keineAlimenteBezahlt_zaehltNicht() {
			Betreuung betreuung = createBetreuungWithOneGS();

			KindContainer kindNichtVollJahrig = addNichtVolljaehigesKind(
				true,
				betreuung.extractGesuch()
			);
			kindNichtVollJahrig.getKindJA().setAlimenteBezahlen(false);

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					FKJV_2
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(2, result.getFamGroesseTotal());
			assertEqualsNumberValue(0, result.getAbzugFamGroesseTotal());
		}

		@Test
		void kindUnter18_inErstausbildung_alimenteBezahlt_zaehltNicht() {
			Betreuung betreuung = createBetreuungWithOneGS();

			KindContainer kindNichtVollJahrig = addNichtVolljaehigesKind(
				true,
				betreuung.extractGesuch()
			);
			kindNichtVollJahrig.getKindJA().setAlimenteBezahlen(true);

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					FKJV_2
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(2, result.getFamGroesseTotal());
			assertEqualsNumberValue(0, result.getAbzugFamGroesseTotal());
		}

		@Test
		void kindUeber18_inErstausbildung_alimenteBezahlt_zaehltGanz() {
			Betreuung betreuung = createBetreuungWithOneGS();

			KindContainer kindVollJahrig = addVolljaehrigesKind(
				true,
				betreuung.extractGesuch()
			);
			kindVollJahrig.getKindJA().setAlimenteBezahlen(true);

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					FKJV_2
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(3, result.getFamGroesseTotal());
			assertEqualsNumberValue(11400, result.getAbzugFamGroesseTotal());
		}

		@Test
		void kindUeber18_inErstausbildung_alimenteErhaltenFalse_zaehltGanz() {
			Betreuung betreuung = createBetreuungWithOneGS();

			KindContainer kindVollJahrig = addVolljaehrigesKind(
				true,
				betreuung.extractGesuch()
			);
			kindVollJahrig.getKindJA().setAlimenteErhalten(false);

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					FKJV_2
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(3, result.getFamGroesseTotal());
			assertEqualsNumberValue(11400, result.getAbzugFamGroesseTotal());
		}

		@Test
		void kindUeber18_inErstausbildung_alimenteErhalten_zaehltNicht() {
			Betreuung betreuung = createBetreuungWithOneGS();

			KindContainer kindVollJahrig = addVolljaehrigesKind(
				true,
				betreuung.extractGesuch()
			);
			kindVollJahrig.getKindJA().setAlimenteErhalten(true);

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					FKJV_2
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(2, result.getFamGroesseTotal());
			assertEqualsNumberValue(0, result.getAbzugFamGroesseTotal());
		}

		@ParameterizedTest
		@ValueSource(booleans = { true, false })
		void kindUnter18_inErstausbildung_alimenteErhaltenAnswered_zaehltImmer(
			boolean alimenteErhalten
		) {
			Betreuung betreuung = createBetreuungWithOneGS();

			KindContainer kindNichtVollJahrig = addNichtVolljaehigesKind(
				true,
				betreuung.extractGesuch()
			);
			kindNichtVollJahrig.getKindJA()
				.setAlimenteErhalten(alimenteErhalten);

			List<VerfuegungZeitabschnitt> zeitabschnitts =
				calculateAllAbzugRules(
					betreuung,
					FKJV_2
				);

			assertEquals(1, zeitabschnitts.size());
			BGCalculationInput result =
				zeitabschnitts.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(3, result.getFamGroesseTotal());
			assertEqualsNumberValue(11400, result.getAbzugFamGroesseTotal());
		}

		@ParameterizedTest
		@EnumSource(
			value = KinderabzugTyp.class,
			names = { "FKJV", "FKJV_2" },
			mode = EnumSource.Mode.INCLUDE
		)
		void kinderAbzug_inErstausbildung_keineFrageZuAlimentenBeantwortet_throwsExcption(
			KinderabzugTyp kinderabzugTyp
		) {
			Betreuung betreuung = createBetreuungWithOneGS();
			addVolljaehrigesKind(true, betreuung.extractGesuch());

			assertThrows(EbeguRuntimeException.class, () -> {
				calculateAllAbzugRules(betreuung, kinderabzugTyp);
			});
		}

		@Test
		void kindWirdVolljaehrigWaherndPeriode_shouldNotCoundAfterGeburtsdatumFolgemonat() {
			Betreuung betreuung = createBetreuungWithOneGS();

			LocalDate geburtsdatumWaehrendPeriode = START_PERIODE.minusYears(18)
				.plusMonths(2);
			LocalDate geburtsdatumWith18 = geburtsdatumWaehrendPeriode
				.plusYears(18);
			KindContainer kindContainer = addVolljaehrigesKind(
				false,
				betreuung.extractGesuch()
			);
			kindContainer.getKindJA()
				.setGeburtsdatum(geburtsdatumWaehrendPeriode);

			List<VerfuegungZeitabschnitt> zeitabschnitte =
				calculateAllAbzugRules(betreuung, FKJV_2);
			Assertions.assertNotNull(zeitabschnitte);
			Assertions.assertEquals(2, zeitabschnitte.size());
			// Kind zählt noch zu Familiengroesse
			final VerfuegungZeitabschnitt bisVolljaehrig = zeitabschnitte.get(
				0
			);
			assertEqualsNumberValue(
				11400,
				bisVolljaehrig.getBgCalculationInputAsiv()
					.getAbzugFamGroesseTotal()
			);
			assertEqualsNumberValue(
				3,
				bisVolljaehrig.getBgCalculationInputAsiv().getFamGroesseTotal()
			);
			assertEquals(
				START_PERIODE,
				bisVolljaehrig.getGueltigkeit().getGueltigAb()
			);
			assertEquals(
				geburtsdatumWith18.with(lastDayOfMonth()),
				bisVolljaehrig.getGueltigkeit().getGueltigBis()
			);

			// Kind zählt nicht mehr zu Familiengroesse
			final VerfuegungZeitabschnitt nachVolljaehrig = zeitabschnitte.get(
				1
			);
			assertEqualsNumberValue(
				0,
				nachVolljaehrig.getBgCalculationInputAsiv()
					.getAbzugFamGroesseTotal()
			);
			assertEqualsNumberValue(
				2,
				nachVolljaehrig.getBgCalculationInputAsiv().getFamGroesseTotal()
			);
			assertEquals(
				geburtsdatumWith18.with(firstDayOfNextMonth()),
				nachVolljaehrig.getGueltigkeit().getGueltigAb()
			);
			assertEquals(
				ENDE_PERIODE,
				nachVolljaehrig.getGueltigkeit().getGueltigBis()
			);
		}

		@CanIgnoreReturnValue
		private KindContainer addVolljaehrigesKind(
			boolean inErstausbildung,
			Gesuch gesuch
		) {
			final LocalDate over18 = LocalDate.of(1995, Month.MARCH, 25);
			KindContainer k = createKind(over18, inErstausbildung);
			addToGesuch(k, gesuch);
			return k;
		}

		@CanIgnoreReturnValue
		private KindContainer addNichtVolljaehigesKind(
			boolean inErstausbildung,
			Gesuch gesuch
		) {
			final LocalDate under18 = LocalDate.of(2002, Month.MARCH, 25);
			KindContainer k = createKind(under18, inErstausbildung);
			addToGesuch(k, gesuch);
			return k;
		}

		private KindContainer createKind(
			LocalDate geburtsdatum,
			boolean inErstausbildung
		) {
			Kind kind = new Kind();
			kind.setGeburtsdatum(geburtsdatum);
			kind.setInErstausbildung(inErstausbildung);
			KindContainer kindContainer = new KindContainer();
			kindContainer.setKindJA(kind);
			return kindContainer;
		}
	}

	@Nested
	class pflegeKindTest {
		@Test
		void entschaedigungErhalten_FKJV_shouldNotCountToFamGroessButToAbzug() {
			// FKJV -> Pflegekind zählt nicht zu Familiengrösse, aber für Abzug
			// Familiengrösse 2 (1 GS und 1 Kind)
			// Familiengrösse für Abzug 3 (1 GS, 1 Kind und 1 Pflegekind) => 3800
			Betreuung betreuung = createBetreuungWithOneGS();
			setPflegeKind(betreuung, true);
			addGanzesGeschwister(betreuung);

			List<VerfuegungZeitabschnitt> zeitabschnittList =
				calculateAllAbzugRules(betreuung, FKJV);

			assertEquals(1, zeitabschnittList.size());

			BGCalculationInput result =
				zeitabschnittList.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(2, result.getFamGroesseTotal());
			assertEqualsNumberValue(7600, result.getAbzugFamGroesseTotal());
		}

		@Test
		void entschaedigungErhalten_FKJV2_shouldNotCount() {
			// FKJV_2 -> Pflegekind zählt nicht zu Familiengrösse
			// Familiengrösse 2 (1 GS und 1 Kind)
			// Familiengrösse für Abzug 2 => 0
			Betreuung betreuung = createBetreuungWithOneGS();
			setPflegeKind(betreuung, true);
			addGanzesGeschwister(betreuung);

			List<VerfuegungZeitabschnitt> zeitabschnittList =
				calculateAllAbzugRules(betreuung, FKJV_2);

			assertEquals(1, zeitabschnittList.size());

			BGCalculationInput result =
				zeitabschnittList.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(2, result.getFamGroesseTotal());
			assertEqualsNumberValue(0, result.getAbzugFamGroesseTotal());
		}

		@ParameterizedTest
		@EnumSource(
			value = KinderabzugTyp.class,
			names = { "FKJV", "FKJV_2" },
			mode = EnumSource.Mode.INCLUDE
		)
		void entschaedigungNichtErhalten_shouldCountFull(
			KinderabzugTyp kinderabzugTyp
		) {
			// Familiengrösse 3 (1 GS und 2 Kinder)
			// Familiengrösse für Abzug 3 => 3800
			Betreuung betreuung = createBetreuungWithOneGS();
			setPflegeKind(betreuung, false);
			addGanzesGeschwister(betreuung);

			List<VerfuegungZeitabschnitt> zeitabschnittList =
				calculateAllAbzugRules(betreuung, kinderabzugTyp);
			assertEquals(1, zeitabschnittList.size());

			BGCalculationInput result =
				zeitabschnittList.get(0).getBgCalculationInputAsiv();
			assertEqualsNumberValue(3, result.getFamGroesseTotal());
			assertEqualsNumberValue(11400, result.getAbzugFamGroesseTotal());
		}

		private void setPflegeKind(
			Betreuung betreuung,
			boolean entschaedigungErhalten
		) {
			betreuung.getKind().getKindJA().setPflegekind(true);
			betreuung.getKind()
				.getKindJA()
				.setPflegeEntschaedigungErhalten(entschaedigungErhalten);
		}
	}

	private List<VerfuegungZeitabschnitt> calculateAllAbzugRules(
		Betreuung betreuung,
		KinderabzugTyp kinderabzugTyp
	) {
		List<VerfuegungZeitabschnitt> zaNachGesuchstellerRule = gsAbzugRule
			.calculate(betreuung, new ArrayList<>());
		List<VerfuegungZeitabschnitt> zaNachKinderRule = kinderAbzugRule
			.calculate(betreuung, zaNachGesuchstellerRule);
		return getFamilienazugRule(kinderabzugTyp).calculate(
			betreuung,
			zaNachKinderRule
		);
	}

	private void addGanzesGeschwister(Betreuung betreuung) {
		KindContainer ganzesKind = TestDataUtil.createDefaultKindContainer();
		ganzesKind.getKindJA().setObhutAlternierendAusueben(false);
		addToGesuch(ganzesKind, betreuung.extractGesuch());
	}

	private void addToGesuch(KindContainer kind, Gesuch gesuch) {
		kind.setKindNummer(gesuch.getKindContainers().size());
		kind.setGesuch(gesuch);
		gesuch.getKindContainers().add(kind);
	}

	private Betreuung createBetreuungWithOneGS() {
		Betreuung betreuung =
			TestDataUtil.createGesuchWithBetreuungspensum(false);
		betreuung.getKind().setKindNummer(0);
		betreuung.getKind().getKindJA().setObhutAlternierendAusueben(false);

		Familiensituation familiensituation =
			betreuung.extractGesuch().extractFamiliensituation();
		Objects.requireNonNull(familiensituation);
		familiensituation.setFkjvFamSit(true);
		familiensituation.setMinDauerKonkubinat(2);
		familiensituation.setGeteilteObhut(false);
		familiensituation.setGesuchstellerKardinalitaet(
			EnumGesuchstellerKardinalitaet.ALLEINE
		);
		return betreuung;
	}

	private Map<EinstellungKey, Einstellung> getEinstellungMapForFKJV2() {
		Map<EinstellungKey, Einstellung> einstellungMapForFKJV2 =
			new EnumMap<>(getEinstellungMapForFKJV());
		einstellungMapForFKJV2.get(EinstellungKey.KINDERABZUG_TYP)
			.setValue(
				FKJV_2.name()
			);
		return einstellungMapForFKJV2;
	}

	private Map<EinstellungKey, Einstellung> getEinstellungMapForFKJV() {

		Map<EinstellungKey, Einstellung> einstellungMapForFKJV =
			new EnumMap<>(getDefaultEinstellungMap());
		Einstellung einstellungMinimalKonkubinat = new Einstellung(
			EinstellungKey.MINIMALDAUER_KONKUBINAT,
			"2",
			new Gesuchsperiode()
		);
		einstellungMapForFKJV.put(
			EinstellungKey.MINIMALDAUER_KONKUBINAT,
			einstellungMinimalKonkubinat
		);
		Einstellung einstellungKinderabzugTyp = new Einstellung(
			EinstellungKey.KINDERABZUG_TYP,
			KinderabzugTyp.FKJV.name(),
			new Gesuchsperiode()
		);
		einstellungMapForFKJV.put(
			EinstellungKey.KINDERABZUG_TYP,
			einstellungKinderabzugTyp
		);

		return einstellungMapForFKJV;
	}
}
