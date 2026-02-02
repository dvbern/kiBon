package ch.dvbern.ebegu.dokumente;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import static ch.dvbern.ebegu.enums.DokumentTyp.BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND;
import static ch.dvbern.ebegu.enums.DokumentTyp.ERFOLGSRECHNUNGEN_JAHR;
import static ch.dvbern.ebegu.enums.DokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS1;
import static ch.dvbern.ebegu.enums.DokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS2;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS1;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS2;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_FREIWILLIGENARBEIT;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_GETEILTE_OBHUT;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_UNTERHALTSVEREINBARUNG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DokumentUebernehmenCalculatorTest {

	private final List<DokumentTyp> DEFAULT_DOKUMENT_ZU_UEBERNEHMEN_TYPS = List
		.of(
			NACHWEIS_GETEILTE_OBHUT,
			NACHWEIS_UNTERHALTSVEREINBARUNG,
			BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND,
			ERFOLGSRECHNUNGEN_JAHR,
			ERFOLGSRECHNUNGEN_JAHR_MINUS1,
			NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR,
			NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS1
		);

	@Nested
	class AllowedInEinstellung {

		@Test
		void shouldBeEmptyList_WhenNoDokumentTypIsAllowedForErneuerung() {
			List<DokumentGrund> dokumentGrundeVorjahr = List.of(
				createDokumentGrund(
					DokumentGrundTyp.FAMILIENSITUATION,
					NACHWEIS_GETEILTE_OBHUT
				)
			);
			Set<DokumentGrund> uploadableDokumenteCurrentGesuch = Set.of();
			List<DokumentTyp> allowedDokumentTypForErneuerung = List.of();
			Gesuch gesuch = createTestGesuch();
			Gesuch vorjahrGesuch = createTestGesuch();

			var result = DokumentUebernehmenCalculator
				.calculateGrundeZuUbernehmen(
					dokumentGrundeVorjahr,
					allowedDokumentTypForErneuerung,
					uploadableDokumenteCurrentGesuch,
					gesuch,
					vorjahrGesuch
				);

			assertThat(result.isEmpty(), is(true));
		}

		@Test
		void shouldBeInList_WhenSameGrundAndDokumentTypInAllowedList() {
			final DokumentTyp dokumentTyp = NACHWEIS_GETEILTE_OBHUT;
			DokumentGrund grund = createDokumentGrund(
				DokumentGrundTyp.FAMILIENSITUATION,
				dokumentTyp
			);

			List<DokumentGrund> dokumentGrundeVorjahr = List.of(grund);
			Set<DokumentGrund> uploadableDokumenteCurrentGesuch = Set.of(grund);
			List<DokumentTyp> allowedDokumentTypForErneuerung = List.of(
				dokumentTyp
			);
			Gesuch gesuch = createTestGesuch();
			Gesuch vorjahrGesuch = createTestGesuch();

			var result = DokumentUebernehmenCalculator
				.calculateGrundeZuUbernehmen(
					dokumentGrundeVorjahr,
					allowedDokumentTypForErneuerung,
					uploadableDokumenteCurrentGesuch,
					gesuch,
					vorjahrGesuch
				);

			assertThat(result.get(0).getDokumentTyp(), is(dokumentTyp));
		}

		@Nested
		class AdaptGrundForYears {

			static Stream<Arguments> dokTypVorjahrDokTypErneuerungsgesuch() {
				return Stream.of(
					Arguments.of(
						ERFOLGSRECHNUNGEN_JAHR,
						ERFOLGSRECHNUNGEN_JAHR_MINUS1
					),
					Arguments.of(
						ERFOLGSRECHNUNGEN_JAHR_MINUS1,
						ERFOLGSRECHNUNGEN_JAHR_MINUS2
					),
					Arguments.of(
						NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR,
						NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS1
					),
					Arguments.of(
						NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS1,
						NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS2
					)
				);
			}

			@ParameterizedTest
			@MethodSource("dokTypVorjahrDokTypErneuerungsgesuch")
			void shouldBeInGrundList_WhenDokTypOneYearLaterIsInAllowedListAndOtherwiseSameGrund(
				DokumentTyp typVorjahr,
				DokumentTyp typErneuerungsgesuch
			) {
				var grundErneuerungsgesuch =
					createGesuchstellerBezogenerDokumentGrund(
						DokumentGrundTyp.FINANZIELLESITUATION,
						typErneuerungsgesuch,
						1
					);
				var grundVorjahr = createGesuchstellerBezogenerDokumentGrund(
					DokumentGrundTyp.FINANZIELLESITUATION,
					typVorjahr,
					1
				);
				List<DokumentGrund> dokumentGrundeVorjahr = List.of(
					grundVorjahr
				);
				Set<DokumentGrund> uploadableDokumenteCurrentGesuch = Set.of(
					grundErneuerungsgesuch
				);
				List<DokumentTyp> allowedDokumentTypForErneuerung = List.of(
					typVorjahr
				);
				Gesuch gesuch = createTestGesuch();
				Gesuch vorjahrGesuch = createTestGesuch();

				var result = DokumentUebernehmenCalculator
					.calculateGrundeZuUbernehmen(
						dokumentGrundeVorjahr,
						allowedDokumentTypForErneuerung,
						uploadableDokumenteCurrentGesuch,
						gesuch,
						vorjahrGesuch
					);

				assertThat(result.get(0).getDokumentTyp(), is(typVorjahr));
			}

			@ParameterizedTest
			@EnumSource(value = DokumentTyp.class,
				names = { "ERFOLGSRECHNUNGEN_JAHR_MINUS2",
					"NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS2" })
			void shouldBeInGrundList_WhenDokTypLastYearWasMinus2AndMinus2IsAllowd(
				DokumentTyp typErneuerungsgesuch
			) {
				var grundErneuerungsgesuch =
					createGesuchstellerBezogenerDokumentGrund(
						DokumentGrundTyp.FINANZIELLESITUATION,
						typErneuerungsgesuch,
						1
					);
				var grundVorjahr = createGesuchstellerBezogenerDokumentGrund(
					DokumentGrundTyp.FINANZIELLESITUATION,
					typErneuerungsgesuch,
					1
				);
				List<DokumentGrund> dokumentGrundeVorjahr = List.of(
					grundVorjahr
				);
				Set<DokumentGrund> uploadableDokumenteCurrentGesuch = Set.of(
					grundErneuerungsgesuch
				);
				List<DokumentTyp> allowedDokumentTypForErneuerung = List.of(
					typErneuerungsgesuch
				);
				Gesuch gesuch = createTestGesuch();
				Gesuch vorjahrGesuch = createTestGesuch();

				var result = DokumentUebernehmenCalculator
					.calculateGrundeZuUbernehmen(
						dokumentGrundeVorjahr,
						allowedDokumentTypForErneuerung,
						uploadableDokumenteCurrentGesuch,
						gesuch,
						vorjahrGesuch
					);

				assertThat(result.isEmpty(), is(true));
			}
		}
	}

	@Nested
	class DokumentGrundeVorjahrEmpty {
		@Test
		void shouldReturnEmptyList_whenDokumenteGrundVorjahrIsEmpty() {
			List<DokumentGrund> dokumentGrundeVorjahr = List.of();
			Set<DokumentGrund> uploadableDokumenteCurrentGesuch = Set.of();
			Gesuch gesuch = createTestGesuch();
			Gesuch vorjahrGesuch = createTestGesuch();

			var result = DokumentUebernehmenCalculator
				.calculateGrundeZuUbernehmen(
					dokumentGrundeVorjahr,
					DEFAULT_DOKUMENT_ZU_UEBERNEHMEN_TYPS,
					uploadableDokumenteCurrentGesuch,
					gesuch,
					vorjahrGesuch
				);

			assertThat(result.isEmpty(), is(true));
		}

		@Test
		void shouldReturnEmptyList_whenDokumenteGrundVorjahrIsEmptyAndUploadableDokumenteIsNotEmpty() {
			List<DokumentGrund> dokumentGrundeVorjahr = List.of();
			Set<DokumentGrund> uploadableDokumenteCurrentGesuch = Set.of(
				createDokumentGrund(
					DokumentGrundTyp.FAMILIENSITUATION,
					NACHWEIS_GETEILTE_OBHUT
				)
			);
			Gesuch gesuch = createTestGesuch();
			Gesuch vorjahrGesuch = createTestGesuch();

			var result = DokumentUebernehmenCalculator
				.calculateGrundeZuUbernehmen(
					dokumentGrundeVorjahr,
					DEFAULT_DOKUMENT_ZU_UEBERNEHMEN_TYPS,
					uploadableDokumenteCurrentGesuch,
					gesuch,
					vorjahrGesuch
				);

			assertThat(result.isEmpty(), is(true));
		}
	}

	@Nested
	class IsInGrundList {

		@Test
		void shouldBeTrue_WhenDokumentTypDokumentGrundTypAndPersonNumberMatchForOneEntryInList() {
			var grund = createGesuchstellerBezogenerDokumentGrund(
				DokumentGrundTyp.ERWERBSPENSUM,
				DokumentTyp.NACHWEIS_ERWERBSPENSUM,
				1
			);
			var grundInList = createGesuchstellerBezogenerDokumentGrund(
				DokumentGrundTyp.ERWERBSPENSUM,
				DokumentTyp.NACHWEIS_ERWERBSPENSUM,
				1
			);
			var uploadableDokumenteCurrentGesuch = List.of(grundInList);

			var result = DokumentUebernehmenCalculator.isInGrundList(
				grund,
				uploadableDokumenteCurrentGesuch
			);

			assertThat(result, is(true));
		}

		@Test
		void shouldBeTrue_WhenDokumentTypDokumentGrundTypMatchAndPersonNumberIsBothNull() {
			var grund = createDokumentGrund(
				DokumentGrundTyp.FAMILIENSITUATION,
				NACHWEIS_GETEILTE_OBHUT
			);
			var grundInList = createDokumentGrund(
				DokumentGrundTyp.FAMILIENSITUATION,
				NACHWEIS_GETEILTE_OBHUT
			);
			var uploadableDokumenteCurrentGesuch = List.of(grundInList);

			var result = DokumentUebernehmenCalculator.isInGrundList(
				grund,
				uploadableDokumenteCurrentGesuch
			);

			assertThat(result, is(true));
		}

		@Test
		void shouldBeFalse_WhenPersonNumberDoesNotMatch() {
			var grundGS1 = createGesuchstellerBezogenerDokumentGrund(
				DokumentGrundTyp.ERWERBSPENSUM,
				DokumentTyp.NACHWEIS_ERWERBSPENSUM,
				1
			);
			var grundGS2 = createGesuchstellerBezogenerDokumentGrund(
				DokumentGrundTyp.ERWERBSPENSUM,
				DokumentTyp.NACHWEIS_ERWERBSPENSUM,
				2
			);
			var uploadableDokumenteCurrentGesuch = List.of(grundGS2);

			var result = DokumentUebernehmenCalculator.isInGrundList(
				grundGS1,
				uploadableDokumenteCurrentGesuch
			);

			assertThat(result, is(false));
		}

		@Test
		void shouldBeFalse_WhenDokumentGrundTypDoesNotMatch() {
			var grundErwerbspensum = createGesuchstellerBezogenerDokumentGrund(
				DokumentGrundTyp.ERWERBSPENSUM,
				DokumentTyp.NACHWEIS_ERWERBSPENSUM,
				1
			);
			var grundFamiliensituation =
				createGesuchstellerBezogenerDokumentGrund(
					DokumentGrundTyp.FAMILIENSITUATION,
					DokumentTyp.NACHWEIS_ERWERBSPENSUM,
					1
				);
			var uploadableDokumenteCurrentGesuch = List.of(
				grundFamiliensituation
			);

			var result = DokumentUebernehmenCalculator.isInGrundList(
				grundErwerbspensum,
				uploadableDokumenteCurrentGesuch
			);

			assertThat(result, is(false));
		}

		@Test
		void shouldBeFalse_WhenDokumentTypDoesNotMatch() {
			var dokTypNachweisErwerbspensum =
				createGesuchstellerBezogenerDokumentGrund(
					DokumentGrundTyp.ERWERBSPENSUM,
					DokumentTyp.NACHWEIS_ERWERBSPENSUM,
					1
				);
			var dokTypNachweisArbeitssuchend =
				createGesuchstellerBezogenerDokumentGrund(
					DokumentGrundTyp.ERWERBSPENSUM,
					DokumentTyp.NACHWEIS_ARBEITSSUCHEND,
					1
				);
			var grundList = List.of(dokTypNachweisArbeitssuchend);

			var result = DokumentUebernehmenCalculator.isInGrundList(
				dokTypNachweisErwerbspensum,
				grundList
			);

			assertThat(result, is(false));
		}

		@Test
		void shouldBeFalse_WhenTagDoesNotMatch() {
			var dokTypNachweisErwerbspensum =
				new DokumentGrund(
					DokumentGrundTyp.ERWERBSPENSUM,
					"Tag 1",
					DokumentGrundPersonType.GESUCHSTELLER,
					1,
					NACHWEIS_FREIWILLIGENARBEIT
				);
			var dokTypNachweisArbeitssuchend =
				new DokumentGrund(
					DokumentGrundTyp.ERWERBSPENSUM,
					"Tag 2",
					DokumentGrundPersonType.GESUCHSTELLER,
					1,
					NACHWEIS_FREIWILLIGENARBEIT
				);
			var grundList = List.of(dokTypNachweisArbeitssuchend);

			var result = DokumentUebernehmenCalculator.isInGrundList(
				dokTypNachweisErwerbspensum,
				grundList
			);

			assertThat(result, is(false));
		}

		@Nested
		class AdaptGrundForYears {

			static Stream<Arguments> dokTypVorjahrDokTypErneuerungsgesuch() {
				return Stream.of(
					Arguments.of(
						ERFOLGSRECHNUNGEN_JAHR,
						ERFOLGSRECHNUNGEN_JAHR_MINUS1
					),
					Arguments.of(
						ERFOLGSRECHNUNGEN_JAHR_MINUS1,
						ERFOLGSRECHNUNGEN_JAHR_MINUS2
					),
					Arguments.of(
						NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR,
						NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS1
					),
					Arguments.of(
						NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS1,
						NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS2
					)
				);
			}

			@ParameterizedTest
			@MethodSource("dokTypVorjahrDokTypErneuerungsgesuch")
			void shouldBeInGrundList_WhenGrundIsDokTypOneYearLater(
				DokumentTyp typVorjahr,
				DokumentTyp typErneuerungsgesuch
			) {
				var grundErneuerungsgesuch =
					createGesuchstellerBezogenerDokumentGrund(
						DokumentGrundTyp.FINANZIELLESITUATION,
						typErneuerungsgesuch,
						1
					);
				var grundVorjahr = createGesuchstellerBezogenerDokumentGrund(
					DokumentGrundTyp.FINANZIELLESITUATION,
					typVorjahr,
					1
				);
				var uploadableGrundeCurrentGesuch = List.of(
					grundErneuerungsgesuch
				);

				var result = DokumentUebernehmenCalculator.isInGrundList(
					grundVorjahr,
					uploadableGrundeCurrentGesuch
				);

				assertThat(result, is(true));
			}

			@ParameterizedTest
			@EnumSource(value = DokumentTyp.class,
				names = { "ERFOLGSRECHNUNGEN_JAHR_MINUS2",
					"NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS2" })
			void shouldNotBeInGrundList_WhenGrundIsMINUS2Year(
				DokumentTyp typErneuerungsgesuch
			) {
				var grundErneuerungsgesuch =
					createGesuchstellerBezogenerDokumentGrund(
						DokumentGrundTyp.FINANZIELLESITUATION,
						typErneuerungsgesuch,
						1
					);
				var grundeVorjahr = createGesuchstellerBezogenerDokumentGrund(
					DokumentGrundTyp.FINANZIELLESITUATION,
					typErneuerungsgesuch,
					1
				);
				var grundListVorjahr = List.of(grundeVorjahr);

				var result = DokumentUebernehmenCalculator.isInGrundList(
					grundErneuerungsgesuch,
					grundListVorjahr
				);

				assertThat(result, is(false));
			}
		}
	}

	@Nested
	class SamePersonCheck {

		private final LocalDate HANS_ZIMMER_GEBURTSTAG = LocalDate.of(
			1957,
			9,
			12
		);

		@Test
		void shouldBeTrue_WhenSameFullNameAndGeburtsdatum() {
			var gesuch = createTestGesuch();
			gesuch.setGesuchsteller1(
				createGesuchsteller("Hans", "Zimmer", HANS_ZIMMER_GEBURTSTAG)
			);
			var vorjahrGesuch = createTestGesuch();
			vorjahrGesuch.setGesuchsteller1(
				createGesuchsteller("Hans", "Zimmer", HANS_ZIMMER_GEBURTSTAG)
			);
			var dokumentGrundGS1 = createGesuchstellerBezogenerDokumentGrund(
				DokumentGrundTyp.ERWERBSPENSUM,
				DokumentTyp.NACHWEIS_ERWERBSPENSUM,
				1
			);

			var result = DokumentUebernehmenCalculator
				.isSamePersonsOrNotPersonenbezogen(
					dokumentGrundGS1,
					gesuch,
					vorjahrGesuch
				);

			assertThat(result, is(true));
		}

		@Test
		void shouldBeTrue_WhenPersonDiffersButGrundIsNotPersonenbezogen() {
			var gesuch = createTestGesuch();
			gesuch.setGesuchsteller1(
				createGesuchsteller("Hans", "Zimmer", HANS_ZIMMER_GEBURTSTAG)
			);
			var vorjahrGesuch = createTestGesuch();
			vorjahrGesuch.setGesuchsteller1(
				createGesuchsteller(
					"Jean",
					"Chambre",
					HANS_ZIMMER_GEBURTSTAG.plusDays(1)
				)
			);

			var dokumentGrund = createDokumentGrund(
				DokumentGrundTyp.FAMILIENSITUATION,
				NACHWEIS_GETEILTE_OBHUT
			);

			var result = DokumentUebernehmenCalculator
				.isSamePersonsOrNotPersonenbezogen(
					dokumentGrund,
					gesuch,
					vorjahrGesuch
				);

			assertThat(result, is(true));
		}

		@Test
		void shouldBeFalse_WhenDifferentVornameAndPersonenbezogenerGrund() {
			var gesuch = createTestGesuch();
			gesuch.setGesuchsteller1(
				createGesuchsteller("Hans", "Zimmer", HANS_ZIMMER_GEBURTSTAG)
			);
			var vorjahrGesuch = createTestGesuch();
			vorjahrGesuch.setGesuchsteller1(
				createGesuchsteller("Jean", "Zimmer", HANS_ZIMMER_GEBURTSTAG)
			);
			var dokumentGrundGS1 = createGesuchstellerBezogenerDokumentGrund(
				DokumentGrundTyp.ERWERBSPENSUM,
				DokumentTyp.NACHWEIS_ERWERBSPENSUM,
				1
			);

			var result = DokumentUebernehmenCalculator
				.isSamePersonsOrNotPersonenbezogen(
					dokumentGrundGS1,
					gesuch,
					vorjahrGesuch
				);

			assertThat(result, is(false));
		}

		@Test
		void shouldBeFalse_WhenDifferentNachnameAndPersonenbezogenerGrund() {
			var gesuch = createTestGesuch();
			gesuch.setGesuchsteller1(
				createGesuchsteller("Hans", "Zimmer", HANS_ZIMMER_GEBURTSTAG)
			);
			var vorjahrGesuch = createTestGesuch();
			vorjahrGesuch.setGesuchsteller1(
				createGesuchsteller("Hans", "Chambre", HANS_ZIMMER_GEBURTSTAG)
			);
			var dokumentGrundGS1 = createGesuchstellerBezogenerDokumentGrund(
				DokumentGrundTyp.ERWERBSPENSUM,
				DokumentTyp.NACHWEIS_ERWERBSPENSUM,
				1
			);

			var result = DokumentUebernehmenCalculator
				.isSamePersonsOrNotPersonenbezogen(
					dokumentGrundGS1,
					gesuch,
					vorjahrGesuch
				);

			assertThat(result, is(false));
		}

		@Test
		void shouldBeFalse_WhenDifferentGeburtstagAndPersonenbezogenerGrund() {
			var gesuch = createTestGesuch();
			gesuch.setGesuchsteller1(
				createGesuchsteller("Hans", "Zimmer", HANS_ZIMMER_GEBURTSTAG)
			);
			var vorjahrGesuch = createTestGesuch();
			vorjahrGesuch.setGesuchsteller1(
				createGesuchsteller(
					"Hans",
					"Zimmer",
					HANS_ZIMMER_GEBURTSTAG.plusDays(1)
				)
			);
			var dokumentGrundGS1 = createGesuchstellerBezogenerDokumentGrund(
				DokumentGrundTyp.ERWERBSPENSUM,
				DokumentTyp.NACHWEIS_ERWERBSPENSUM,
				1
			);

			var result = DokumentUebernehmenCalculator
				.isSamePersonsOrNotPersonenbezogen(
					dokumentGrundGS1,
					gesuch,
					vorjahrGesuch
				);

			assertThat(result, is(false));
		}
	}

	private GesuchstellerContainer createGesuchsteller(
		String vorname,
		String nachname,
		LocalDate geburtstag
	) {
		GesuchstellerContainer gesuchstellerContainer =
			new GesuchstellerContainer();
		Gesuchsteller gesuchsteller = new Gesuchsteller();
		gesuchsteller.setVorname(vorname);
		gesuchsteller.setNachname(nachname);
		gesuchsteller.setGeburtsdatum(geburtstag);
		gesuchstellerContainer.setGesuchstellerJA(gesuchsteller);
		return gesuchstellerContainer;
	}

	private Gesuch createTestGesuch() {
		return new Gesuch();
	}

	private DokumentGrund createDokumentGrund(
		DokumentGrundTyp dokumentGrundTyp,
		DokumentTyp dokumentTyp
	) {
		return new DokumentGrund(dokumentGrundTyp, dokumentTyp);
	}

	private DokumentGrund createGesuchstellerBezogenerDokumentGrund(
		DokumentGrundTyp dokumentGrundTyp,
		DokumentTyp dokumentTyp,
		Integer personNumber
	) {
		return new DokumentGrund(
			dokumentGrundTyp,
			null,
			DokumentGrundPersonType.GESUCHSTELLER,
			personNumber,
			dokumentTyp
		);
	}
}
