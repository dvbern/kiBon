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

package ch.dvbern.ebegu.finanziellesituation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinSitZusatzangabenAppenzell;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(EasyMockExtension.class)
class FinanzielleSituationValidationServiceTest extends EasyMockSupport {

	FinanzielleSituationValidationService service =
		new FinanzielleSituationValidationService();

	@Nested
	class BernTest {

		@Nested
		class GemeinsamTest {
			@ParameterizedTest
			@EnumSource(value = SteuerdatenAnfrageStatus.class,
				names = { "OFFEN", "PROVISORISCH", "RECHTSKRAEFTIG",
					"NEUE_VERANLAGUNG" },
				mode = Mode.EXCLUDE)
			void finSitASIVVGemeinsam_failedSteuerdatenabfrageWithoutAutomatischePruefungAnswered_shouldBeVollstaendig(
				SteuerdatenAnfrageStatus failedSteuerdatenAnfrageStatus
			) {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN);
				gesuch.setEingangsart(Eingangsart.ONLINE);

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(true);
				finanzielleSituation.setSteuerdatenAbfrageStatus(
					failedSteuerdatenAnfrageStatus
				);

				assertThat(
					service.isFinanzielleSituationIntroducedAndComplete(gesuch),
					is(true)
				);

			}

			private @NotNull FamiliensituationContainer createFamSit() {
				return createFamiliensituation(
					EnumFamilienstatus.VERHEIRATET,
					true
				);
			}

			@ParameterizedTest
			@EnumSource(value = SteuerdatenAnfrageStatus.class,
				names = { "OFFEN", "PROVISORISCH", "RECHTSKRAEFTIG",
					"NEUE_VERANLAGUNG" },
				mode = Mode.EXCLUDE)
			void finSitFKJVGemeinsam_zugriffAndFailedWithoutAutomatischePruefungAnswered_shouldBeVollstaendig(
				SteuerdatenAnfrageStatus failedSteuerdatenAnfrageStatus
			) {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
				gesuch.setEingangsart(Eingangsart.ONLINE);

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(true);
				finanzielleSituation.setSteuerdatenAbfrageStatus(
					failedSteuerdatenAnfrageStatus
				);
				finanzielleSituation.setAutomatischePruefungErlaubt(null);

				assertThat(
					service.isFinanzielleSituationIntroducedAndComplete(gesuch),
					is(true)
				);
			}

			@Test
			void finSitFKJVGemeinsam_neueVeranlagungSteuerdatenabfrageStatus_shouldNotBeVollstaendig() {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
				gesuch.setEingangsart(Eingangsart.ONLINE);

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(true);
				finanzielleSituation.setNettoVermoegen(BigDecimal.ONE);
				finanzielleSituation.setSteuerdatenAbfrageStatus(
					SteuerdatenAnfrageStatus.NEUE_VERANLAGUNG
				);

				assertThat(
					service.isFinanzielleSituationIntroducedAndComplete(gesuch),
					is(false)
				);
			}

			@Test
			void finSitFKJVGemeinsam_noZugriffWithoutAutomatischePruefungAnswered_shouldNotBeVollstaendig() {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
				gesuch.setEingangsart(Eingangsart.ONLINE);

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(false);
				finanzielleSituation.setAutomatischePruefungErlaubt(null);

				assertThat(
					service.isFinanzielleSituationIntroducedAndComplete(gesuch),
					is(false)
				);
			}

			@Test
			void finSitFKJVGemeinsam_UnterstuetzungDienstFall_withoutSteuerfrageAnswered_shouldBeVollstaendig() {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
				gesuch.setEingangsart(Eingangsart.ONLINE);
				gesuch.getFall().setSozialdienstFall(new SozialdienstFall());

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(null);
				finanzielleSituation.setAutomatischePruefungErlaubt(null);

				assertThat(
					service.isFinanzielleSituationIntroducedAndComplete(gesuch),
					is(true)
				);
			}

			@ParameterizedTest
			@ValueSource(booleans = { true, false })
			void finSitFKJVGemeinsam_noZugriffWithAutomatischePruefungAnswered_shouldBeVollstaendig(
				boolean automatischePruefungErlaubt
			) {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
				gesuch.setEingangsart(Eingangsart.ONLINE);

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(false);
				finanzielleSituation.setAutomatischePruefungErlaubt(
					automatischePruefungErlaubt
				);

				assertThat(
					service.isFinanzielleSituationIntroducedAndComplete(gesuch),
					is(true)
				);
			}

			@ParameterizedTest
			@EnumSource(value = SteuerdatenAnfrageStatus.class,
				names = { "NEUE_VERANLAGUNG" },
				mode = Mode.EXCLUDE)
			void hasFamilienSituationNeueVeranlagungsstandZuAbholen_GS2_shouldBeFalseBeiAlleStatus_ausser_NEUE_VERANLAGUNG(
				SteuerdatenAnfrageStatus status
			) {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
				gesuch.setEingangsart(Eingangsart.ONLINE);

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(true);
				finanzielleSituation.setSteuerdatenAbfrageStatus(
					status
				);

				final GesuchstellerContainer gs2 = setupEmptyGS();
				gesuch.setGesuchsteller2(gs2);

				FinanzielleSituation finanzielleSituationGS2 =
					getFinSitNullsafe(
						gs2
					);
				initAbstractFinSitBern(finanzielleSituationGS2);
				finanzielleSituationGS2.setSteuerdatenZugriff(true);
				finanzielleSituationGS2.setSteuerdatenAbfrageStatus(
					status
				);

				assertThat(
					service.hasFamilienSituationNeueVeranlagungsstandZuAbholen(
						gesuch
					),
					is(false)
				);
			}

			@Test
			void hasFamilienSituationNeueVeranlagungsstandZuAbholen_GS2_shouldBeTrueMitStatus_NEUE_VERANLAGUNG() {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
				gesuch.setEingangsart(Eingangsart.ONLINE);

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(true);
				finanzielleSituation.setSteuerdatenAbfrageStatus(
					SteuerdatenAnfrageStatus.RECHTSKRAEFTIG
				);

				final GesuchstellerContainer gs2 = setupEmptyGS();
				gesuch.setGesuchsteller2(gs2);

				FinanzielleSituation finanzielleSituationGS2 =
					getFinSitNullsafe(
						gs2
					);
				initAbstractFinSitBern(finanzielleSituationGS2);
				finanzielleSituationGS2.setSteuerdatenZugriff(true);
				finanzielleSituationGS2.setSteuerdatenAbfrageStatus(
					SteuerdatenAnfrageStatus.NEUE_VERANLAGUNG
				);

				assertThat(
					service.hasFamilienSituationNeueVeranlagungsstandZuAbholen(
						gesuch
					),
					is(true)
				);
			}
		}

		@Nested
		class SingleGSTest {
			@ParameterizedTest
			@EnumSource(value = SteuerdatenAnfrageStatus.class,
				names = { "OFFEN", "PROVISORISCH", "RECHTSKRAEFTIG",
					"NEUE_VERANLAGUNG" },
				mode = Mode.EXCLUDE)
			void finSitASIVV_failedSteuerdatenabfrageWithoutAutomatischePruefungAnswered_shouldBeVollstaendig(
				SteuerdatenAnfrageStatus failedSteuerdatenAnfrageStatus
			) {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN);
				gesuch.setEingangsart(Eingangsart.ONLINE);

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(true);
				finanzielleSituation.setSteuerdatenAbfrageStatus(
					failedSteuerdatenAnfrageStatus
				);

				assertThat(
					service.isFinanzielleSituationIntroducedAndComplete(gesuch),
					is(true)
				);
			}

			@Test
			void finSitFKJV_neueVeranlagungSteuerdatenabfrageStatus_shouldNotBeVollstaendig() {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
				gesuch.setEingangsart(Eingangsart.ONLINE);

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(true);
				finanzielleSituation.setSteuerdatenAbfrageStatus(
					SteuerdatenAnfrageStatus.NEUE_VERANLAGUNG
				);

				assertThat(
					service.isFinanzielleSituationIntroducedAndComplete(gesuch),
					is(false)
				);
			}

			@ParameterizedTest
			@EnumSource(value = SteuerdatenAnfrageStatus.class,
				names = { "OFFEN", "PROVISORISCH", "RECHTSKRAEFTIG",
					"NEUE_VERANLAGUNG" },
				mode = Mode.EXCLUDE)
			void finSitFKJV_zugriffAndFailedWithoutAutomatischePruefungAnswered_shouldBeVollstaendig(
				SteuerdatenAnfrageStatus failedSteuerdatenAnfrageStatus
			) {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
				gesuch.setEingangsart(Eingangsart.ONLINE);

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(true);
				finanzielleSituation.setSteuerdatenAbfrageStatus(
					failedSteuerdatenAnfrageStatus
				);
				finanzielleSituation.setAutomatischePruefungErlaubt(null);

				assertThat(
					service.isFinanzielleSituationIntroducedAndComplete(gesuch),
					is(true)
				);
			}

			@Test
			void finSitFKJV_noZugriffWithoutAutomatischePruefungAnswered_shouldNotBeVollstaendig() {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
				gesuch.setEingangsart(Eingangsart.ONLINE);

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(false);
				finanzielleSituation.setAutomatischePruefungErlaubt(null);

				assertThat(
					service.isFinanzielleSituationIntroducedAndComplete(gesuch),
					is(false)
				);
			}

			@Test
			void finSitFKJV_UnterstuetzungDienstFall_withoutSteuerfrageAnswered_shouldBeVollstaendig() {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
				gesuch.setEingangsart(Eingangsart.ONLINE);
				gesuch.getFall().setSozialdienstFall(new SozialdienstFall());

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(null);
				finanzielleSituation.setAutomatischePruefungErlaubt(null);

				assertThat(
					service.isFinanzielleSituationIntroducedAndComplete(gesuch),
					is(true)
				);
			}

			@ParameterizedTest
			@ValueSource(booleans = { true, false })
			void finSitFKJV_noZugriffWithAutomatischePruefungAnswered_shouldBeVollstaendig(
				boolean automatischePruefungErlaubt
			) {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
				gesuch.setEingangsart(Eingangsart.ONLINE);

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(false);
				finanzielleSituation.setAutomatischePruefungErlaubt(
					automatischePruefungErlaubt
				);

				assertThat(
					service.isFinanzielleSituationIntroducedAndComplete(gesuch),
					is(true)
				);
			}

			@ParameterizedTest
			@EnumSource(value = SteuerdatenAnfrageStatus.class,
				names = { "NEUE_VERANLAGUNG" },
				mode = Mode.EXCLUDE)
			void hasFamilienSituationNeueVeranlagungsstandZuAbholen_GS1_shouldBeFalseBeiAlleStatus_ausser_NEUE_VERANLAGUNG(
				SteuerdatenAnfrageStatus status
			) {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
				gesuch.setEingangsart(Eingangsart.ONLINE);

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(true);
				finanzielleSituation.setSteuerdatenAbfrageStatus(
					status
				);

				assertThat(
					service.hasFamilienSituationNeueVeranlagungsstandZuAbholen(
						gesuch
					),
					is(false)
				);
			}

			@Test
			void hasFamilienSituationNeueVeranlagungsstandZuAbholen_GS1_shouldBeTrueMitStatus_NEUE_VERANLAGUNG() {
				Gesuch gesuch = initGesuchWitGP();
				gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
				gesuch.setEingangsart(Eingangsart.ONLINE);

				FamiliensituationContainer familiensituationContainer =
					createFamSit();
				gesuch.setFamiliensituationContainer(
					familiensituationContainer
				);

				final GesuchstellerContainer gs1 = setupEmptyGS();
				gesuch.setGesuchsteller1(gs1);

				FinanzielleSituation finanzielleSituation = getFinSitNullsafe(
					gs1
				);
				initAbstractFinSitBern(finanzielleSituation);
				finanzielleSituation.setSteuerdatenZugriff(true);
				finanzielleSituation.setSteuerdatenAbfrageStatus(
					SteuerdatenAnfrageStatus.NEUE_VERANLAGUNG
				);

				assertThat(
					service.hasFamilienSituationNeueVeranlagungsstandZuAbholen(
						gesuch
					),
					is(true)
				);
			}

			private FamiliensituationContainer createFamSit() {
				FamiliensituationContainer familiensituationContainer =
					createFamiliensituation(
						EnumFamilienstatus.ALLEINERZIEHEND,
						true
					);
				final Familiensituation familiensituation = Objects
					.requireNonNull(
						familiensituationContainer.getFamiliensituationJA()
					);
				familiensituation.setGeteilteObhut(false);
				familiensituation.setGesuchstellerKardinalitaet(
					EnumGesuchstellerKardinalitaet.ALLEINE
				);

				return familiensituationContainer;
			}
		}

		@ParameterizedTest
		@EnumSource(value = FinanzielleSituationTyp.class,
			names = { "BERN", "BERN_FKJV" },
			mode = Mode.INCLUDE)
		void isFinanzielleSituationIntroducedAndComplete_EKV_Vollstaendig_Test(
			FinanzielleSituationTyp finanzielleSituationTyp
		) {
			Gesuch gesuch = initGesuchWitGP();
			gesuch.setFinSitTyp(finanzielleSituationTyp);
			gesuch.setFamiliensituationContainer(
				createFamiliensituation(EnumFamilienstatus.VERHEIRATET, true)
			);
			final EinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainer =
				new EinkommensverschlechterungInfoContainer();
			gesuch.setEinkommensverschlechterungInfoContainer(
				einkommensverschlechterungInfoContainer
			);
			final EinkommensverschlechterungInfo einkommensverschlechterungInfo =
				new EinkommensverschlechterungInfo();
			einkommensverschlechterungInfoContainer
				.setEinkommensverschlechterungInfoJA(
					einkommensverschlechterungInfo
				);
			einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus1(true);
			einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus2(false);

			final GesuchstellerContainer gesuchsteller1 =
				new GesuchstellerContainer();
			gesuch.setGesuchsteller1(gesuchsteller1);
			final FinanzielleSituationContainer finSitGS1 =
				new FinanzielleSituationContainer();
			gesuchsteller1.setFinanzielleSituationContainer(finSitGS1);
			Assertions.assertTrue(
				service.isEKVIntroducedAndComplete(gesuch)
			);

			einkommensverschlechterungInfo.setEinkommensverschlechterung(true);
			einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus1(true);
			Assertions.assertFalse(
				service.isEKVIntroducedAndComplete(gesuch)
			);

			final EinkommensverschlechterungContainer einkommensverschlechterungContainer =
				new EinkommensverschlechterungContainer();
			gesuchsteller1.setEinkommensverschlechterungContainer(
				einkommensverschlechterungContainer
			);
			einkommensverschlechterungContainer.setEkvJABasisJahrPlus1(
				initEKVBern()
			);
			Assertions.assertTrue(
				service.isEKVIntroducedAndComplete(gesuch)
			);

			final GesuchstellerContainer gesuchsteller2 =
				new GesuchstellerContainer();
			gesuch.setGesuchsteller2(gesuchsteller2);
			final EinkommensverschlechterungContainer einkommensverschlechterungContainerGS2 =
				new EinkommensverschlechterungContainer();
			gesuchsteller2.setEinkommensverschlechterungContainer(
				einkommensverschlechterungContainerGS2
			);
			einkommensverschlechterungContainerGS2.setEkvJABasisJahrPlus1(
				new Einkommensverschlechterung()
			);
			Assertions.assertFalse(
				service.isEKVIntroducedAndComplete(gesuch)
			);

			einkommensverschlechterungContainerGS2.setEkvJABasisJahrPlus1(
				initEKVBern()
			);
			Assertions.assertTrue(
				service.isEKVIntroducedAndComplete(gesuch)
			);

			einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus2(true);
			Assertions.assertFalse(
				service.isEKVIntroducedAndComplete(gesuch)
			);
			einkommensverschlechterungContainer.setEkvJABasisJahrPlus2(
				initEKVBern()
			);
			Assertions.assertTrue(
				service.isEKVIntroducedAndComplete(gesuch)
			);

			einkommensverschlechterungContainerGS2.setEkvJABasisJahrPlus2(
				initEKVBern()
			);
			Assertions.assertTrue(
				service.isEKVIntroducedAndComplete(gesuch)
			);
		}

		@ParameterizedTest
		@EnumSource(value = FinanzielleSituationTyp.class,
			names = { "BERN", "BERN_FKJV" },
			mode = Mode.INCLUDE)
		void isFinanzielleSituationIntroduceAndComplete_BERN_Test(
			FinanzielleSituationTyp finanzielleSituationTyp
		) {
			Gesuch gesuch = initGesuchWitGP();
			gesuch.setFinSitTyp(finanzielleSituationTyp);
			gesuch.setFamiliensituationContainer(
				createFamiliensituation(EnumFamilienstatus.VERHEIRATET, true)
			);
			Assertions.assertFalse(
				service.isFinanzielleSituationIntroducedAndComplete(gesuch)
			);

			final GesuchstellerContainer gesuchsteller1 =
				new GesuchstellerContainer();
			final FinanzielleSituationContainer finSitGS1 =
				new FinanzielleSituationContainer();
			gesuch.setGesuchsteller1(gesuchsteller1);
			gesuchsteller1.setFinanzielleSituationContainer(finSitGS1);

			Assertions.assertFalse(
				service.isFinanzielleSituationIntroducedAndComplete(gesuch)
			);
			finSitGS1.setFinanzielleSituationJA(initFinSitBern());
			Assertions.assertTrue(
				service.isFinanzielleSituationIntroducedAndComplete(gesuch)
			);

			final GesuchstellerContainer gesuchsteller2 =
				new GesuchstellerContainer();
			gesuch.setGesuchsteller2(gesuchsteller2);
			final FinanzielleSituationContainer finSitGS2 =
				new FinanzielleSituationContainer();
			gesuchsteller2.setFinanzielleSituationContainer(finSitGS2);
			Assertions.assertFalse(
				service.isFinanzielleSituationIntroducedAndComplete(gesuch)
			);
			finSitGS2.setFinanzielleSituationJA(initFinSitBern());
			Assertions.assertTrue(
				service.isFinanzielleSituationIntroducedAndComplete(gesuch)
			);
		}

		private Gesuch initGesuchWitGP() {
			Gesuch gesuch = new Gesuch();
			final Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
			gesuchsperiode.setGueltigkeit(
				new DateRange(
					LocalDate.of(2023, 8, 1),
					LocalDate.of(2024, 7, 31)
				)
			);
			gesuch.setGesuchsperiode(gesuchsperiode);
			gesuch.setDossier(new Dossier());
			gesuch.getDossier().setFall(new Fall());
			return gesuch;
		}

		private FinanzielleSituation getFinSitNullsafe(
			GesuchstellerContainer gs1
		) {
			return Objects.requireNonNull(
				gs1.getFinanzielleSituationContainer()
			).getFinanzielleSituationJA();
		}

		private FamiliensituationContainer createFamiliensituation(
			EnumFamilienstatus familienstatus,
			@Nullable Boolean gemeinsameSteuererklaerung
		) {
			final FamiliensituationContainer familiensituationContainer =
				new FamiliensituationContainer();
			final Familiensituation familiensituationJA =
				new Familiensituation();
			familiensituationJA.setFamilienstatus(familienstatus);
			familiensituationJA.setGemeinsameSteuererklaerung(
				gemeinsameSteuererklaerung
			);
			familiensituationContainer.setFamiliensituationJA(
				familiensituationJA
			);
			return familiensituationContainer;
		}

		private GesuchstellerContainer setupEmptyGS() {
			final GesuchstellerContainer gs1Container =
				new GesuchstellerContainer();
			final FinanzielleSituationContainer finSitContainer =
				new FinanzielleSituationContainer();
			final FinanzielleSituation finanzielleSituation =
				new FinanzielleSituation();
			gs1Container.setFinanzielleSituationContainer(finSitContainer);
			finSitContainer.setFinanzielleSituationJA(finanzielleSituation);
			return gs1Container;
		}
	}

	@Test
	void isFinanzielleSituationIntroducedAndComplete_SOLOTHURN_Test() {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(FinanzielleSituationTyp.SOLOTHURN);
		final FamiliensituationContainer familiensituationContainer =
			new FamiliensituationContainer();
		gesuch.setFamiliensituationContainer(familiensituationContainer);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		final GesuchstellerContainer gesuchsteller1 =
			new GesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		final FinanzielleSituationContainer finSitGS1 =
			new FinanzielleSituationContainer();
		gesuchsteller1.setFinanzielleSituationContainer(finSitGS1);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);
		FinanzielleSituation finSitSolothurn = new FinanzielleSituation();

		finSitGS1.setFinanzielleSituationJA(finSitSolothurn);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		finSitGS1.getFinanzielleSituationJA().setBruttoLohn(BigDecimal.ZERO);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		finSitGS1.getFinanzielleSituationJA()
			.setSteuerbaresVermoegen(BigDecimal.ZERO);
		Assertions.assertTrue(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		finSitGS1.getFinanzielleSituationJA().setBruttoLohn(null);
		finSitGS1.getFinanzielleSituationJA().setNettolohn(BigDecimal.ZERO);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		finSitGS1.getFinanzielleSituationJA()
			.setUnterhaltsBeitraege(BigDecimal.ZERO);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		finSitGS1.getFinanzielleSituationJA()
			.setAbzuegeKinderAusbildung(BigDecimal.ZERO);
		Assertions.assertTrue(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);
	}

	@Test
	void isFinanzielleSituationIntroducedAndComplete_LUZERN_Infoma_Test() {
		Gesuch gesuch = createGesuchWithDossierAndGemeinde(true);
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);
		gesuch.setFinSitTyp(FinanzielleSituationTyp.LUZERN);

		final FamiliensituationContainer familiensituationContainer =
			new FamiliensituationContainer();
		gesuch.setFamiliensituationContainer(familiensituationContainer);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		final GesuchstellerContainer gesuchsteller1 =
			new GesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		final FinanzielleSituationContainer finSitGS1 =
			new FinanzielleSituationContainer();
		gesuchsteller1.setFinanzielleSituationContainer(finSitGS1);

		finSitGS1.setFinanzielleSituationJA(initFinSitLuzern());
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		final FamiliensituationContainer familiensituationContainer2 =
			new FamiliensituationContainer();
		gesuch.setFamiliensituationContainer(familiensituationContainer2);
		final Familiensituation familiensituation = new Familiensituation();
		familiensituationContainer2.setFamiliensituationJA(familiensituation);
		final Auszahlungsdaten auszahlungsdaten = new Auszahlungsdaten();
		familiensituation.setAuszahlungsdaten(auszahlungsdaten);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		auszahlungsdaten.setIban(new IBAN());
		Assertions.assertTrue(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_JA);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		auszahlungsdaten.setInfomaBankcode("test");
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		auszahlungsdaten.setInfomaKreditorennummer("test");
		Assertions.assertTrue(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);
	}

	@Test
	void isFinanzielleSituationIntroducedAndComplete_LUZERN_No_Infoma_Test() {
		Gesuch gesuch = createGesuchWithDossierAndGemeinde(false);
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);
		gesuch.setFinSitTyp(FinanzielleSituationTyp.LUZERN);

		final FamiliensituationContainer familiensituationContainer =
			new FamiliensituationContainer();
		gesuch.setFamiliensituationContainer(familiensituationContainer);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		final GesuchstellerContainer gesuchsteller1 =
			new GesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		final FinanzielleSituationContainer finSitGS1 =
			new FinanzielleSituationContainer();
		gesuchsteller1.setFinanzielleSituationContainer(finSitGS1);

		finSitGS1.setFinanzielleSituationJA(initFinSitLuzern());
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		final FamiliensituationContainer familiensituationContainer2 =
			new FamiliensituationContainer();
		gesuch.setFamiliensituationContainer(familiensituationContainer2);
		final Familiensituation familiensituation = new Familiensituation();
		familiensituationContainer2.setFamiliensituationJA(familiensituation);
		final Auszahlungsdaten auszahlungsdaten = new Auszahlungsdaten();
		familiensituation.setAuszahlungsdaten(auszahlungsdaten);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		auszahlungsdaten.setIban(new IBAN());
		Assertions.assertTrue(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_JA);
		Assertions.assertTrue(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);
	}

	private Gesuch createGesuchWithDossierAndGemeinde(boolean hasInfoma) {
		Gesuch gesuch = new Gesuch();
		gesuch.setDossier(new Dossier());
		gesuch.getDossier().setGemeinde(new Gemeinde());
		gesuch.extractGemeinde().setInfomaZahlungen(hasInfoma);
		return gesuch;
	}

	@ParameterizedTest
	@EnumSource(value = FinanzielleSituationTyp.class,
		names = { "APPENZELL", "APPENZELL_FOLGEMONAT" },
		mode = Mode.INCLUDE)
	void isFinanzielleSituationIntroducedAndComplete_AR_Test(
		FinanzielleSituationTyp finanzielleSituationTyp
	) {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(finanzielleSituationTyp);
		final FamiliensituationContainer familiensituationContainer =
			new FamiliensituationContainer();
		gesuch.setFamiliensituationContainer(familiensituationContainer);
		familiensituationContainer.setFamiliensituationJA(
			new Familiensituation()
		);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		final GesuchstellerContainer gesuchsteller1 =
			new GesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		final FinanzielleSituationContainer finSitGS1 =
			new FinanzielleSituationContainer();
		gesuchsteller1.setFinanzielleSituationContainer(finSitGS1);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		FinanzielleSituation finSit = new FinanzielleSituation();
		finSitGS1.setFinanzielleSituationJA(finSit);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		final FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell =
			initFinSitAppenzell();
		finSitGS1.getFinanzielleSituationJA()
			.setFinSitZusatzangabenAppenzell(finSitZusatzangabenAppenzell);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		finSitZusatzangabenAppenzell.setSteuerbaresVermoegen(BigDecimal.TEN);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		finSitZusatzangabenAppenzell.setSteuerbaresEinkommen(BigDecimal.TEN);
		Assertions.assertTrue(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);
	}

	@ParameterizedTest
	@EnumSource(value = FinanzielleSituationTyp.class,
		names = { "APPENZELL", "APPENZELL_FOLGEMONAT" },
		mode = Mode.INCLUDE)
	void isFinanzielleSituationIntroducedAndComplete_isMandantSpecificFinSitGemeinsam_AR_Test(
		FinanzielleSituationTyp finanzielleSituationTyp
	) {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(finanzielleSituationTyp);
		final FamiliensituationContainer familiensituationContainer =
			new FamiliensituationContainer();
		gesuch.setFamiliensituationContainer(familiensituationContainer);
		final Familiensituation familiensituation = new Familiensituation();
		familiensituationContainer.setFamiliensituationJA(familiensituation);
		familiensituation.setGemeinsameSteuererklaerung(false);
		final GesuchstellerContainer gesuchsteller1 =
			new GesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		final FinanzielleSituationContainer finSitGS1 =
			new FinanzielleSituationContainer();
		gesuchsteller1.setFinanzielleSituationContainer(finSitGS1);
		final GesuchstellerContainer gesuchsteller2 =
			new GesuchstellerContainer();
		gesuch.setGesuchsteller2(gesuchsteller2);
		gesuchsteller2.setFinanzielleSituationContainer(
			new FinanzielleSituationContainer()
		);
		FinanzielleSituation finSit = new FinanzielleSituation();
		finSitGS1.setFinanzielleSituationJA(finSit);
		final FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell =
			initFinSitAppenzell();
		finSitGS1.getFinanzielleSituationJA()
			.setFinSitZusatzangabenAppenzell(finSitZusatzangabenAppenzell);
		finSitZusatzangabenAppenzell.setSteuerbaresVermoegen(BigDecimal.TEN);
		finSitZusatzangabenAppenzell.setSteuerbaresEinkommen(BigDecimal.TEN);
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		familiensituation.setGemeinsameSteuererklaerung(true);
		Assertions.assertTrue(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);
	}

	@Test
	void isFinanzielleSituationIntroducedAndComplete_isMandantSpecificFinSitGemeinsam_LU_Test() {
		Gesuch gesuch = new Gesuch();
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);
		gesuch.setFinSitTyp(FinanzielleSituationTyp.LUZERN);
		final FamiliensituationContainer familiensituationContainer =
			new FamiliensituationContainer();
		gesuch.setFamiliensituationContainer(familiensituationContainer);
		final Familiensituation familiensituation = new Familiensituation();
		familiensituationContainer.setFamiliensituationJA(familiensituation);
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		final Auszahlungsdaten auszahlungsdaten = new Auszahlungsdaten();
		familiensituation.setAuszahlungsdaten(auszahlungsdaten);
		auszahlungsdaten.setIban(new IBAN());
		final GesuchstellerContainer gesuchsteller1 =
			new GesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		final FinanzielleSituationContainer finSitGS1 =
			new FinanzielleSituationContainer();
		gesuchsteller1.setFinanzielleSituationContainer(finSitGS1);
		final GesuchstellerContainer gesuchsteller2 =
			new GesuchstellerContainer();
		gesuch.setGesuchsteller2(gesuchsteller2);
		gesuchsteller2.setFinanzielleSituationContainer(
			new FinanzielleSituationContainer()
		);
		finSitGS1.setFinanzielleSituationJA(initFinSitLuzern());
		Assertions.assertFalse(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);

		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		Assertions.assertTrue(
			service.isFinanzielleSituationIntroducedAndComplete(gesuch)
		);
	}

	@Test
	void isFinanzielleSituationIntroducedAndComplete_ekvVollstaendig_SO_Test() {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(FinanzielleSituationTyp.SOLOTHURN);
		final EinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainer =
			new EinkommensverschlechterungInfoContainer();
		gesuch.setEinkommensverschlechterungInfoContainer(
			einkommensverschlechterungInfoContainer
		);
		einkommensverschlechterungInfoContainer
			.setEinkommensverschlechterungInfoJA(
				new EinkommensverschlechterungInfo()
			);
		einkommensverschlechterungInfoContainer
			.getEinkommensverschlechterungInfoJA()
			.setEkvFuerBasisJahrPlus1(true);
		einkommensverschlechterungInfoContainer
			.getEinkommensverschlechterungInfoJA()
			.setEkvFuerBasisJahrPlus2(false);

		final GesuchstellerContainer gesuchsteller1 =
			new GesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		final FinanzielleSituationContainer finSitGS1 =
			new FinanzielleSituationContainer();
		gesuchsteller1.setFinanzielleSituationContainer(finSitGS1);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungInfoContainer
			.getEinkommensverschlechterungInfoJA()
			.setEinkommensverschlechterung(true);
		Assertions.assertFalse(
			service.isEKVIntroducedAndComplete(gesuch)
		);
		final EinkommensverschlechterungContainer einkommensverschlechterungContainer =
			new EinkommensverschlechterungContainer();
		gesuchsteller1.setEinkommensverschlechterungContainer(
			einkommensverschlechterungContainer
		);
		einkommensverschlechterungContainer.setEkvJABasisJahrPlus1(
			new Einkommensverschlechterung()
		);
		Assertions.assertFalse(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungContainer.getEkvJABasisJahrPlus1()
			.setBruttolohnAbrechnung1(BigDecimal.ONE);
		Assertions.assertFalse(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungContainer.getEkvJABasisJahrPlus1()
			.setBruttolohnAbrechnung2(BigDecimal.ONE);
		Assertions.assertFalse(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungContainer.getEkvJABasisJahrPlus1()
			.setBruttolohnAbrechnung3(BigDecimal.ONE);
		Assertions.assertFalse(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungContainer.getEkvJABasisJahrPlus1()
			.setNettoVermoegen(BigDecimal.ONE);
		Assertions.assertFalse(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungContainer.getEkvJABasisJahrPlus1()
			.setExtraLohn(true);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		final GesuchstellerContainer gesuchsteller2 =
			new GesuchstellerContainer();
		gesuch.setGesuchsteller2(gesuchsteller2);
		final EinkommensverschlechterungContainer einkommensverschlechterungContainerGS2 =
			new EinkommensverschlechterungContainer();
		gesuchsteller2.setEinkommensverschlechterungContainer(
			einkommensverschlechterungContainerGS2
		);
		einkommensverschlechterungContainerGS2.setEkvJABasisJahrPlus1(
			new Einkommensverschlechterung()
		);
		Assertions.assertFalse(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungContainerGS2.setEkvJABasisJahrPlus1(
			createEkvSolothurn()
		);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungInfoContainer
			.getEinkommensverschlechterungInfoJA()
			.setEkvFuerBasisJahrPlus2(true);
		Assertions.assertFalse(
			service.isEKVIntroducedAndComplete(gesuch)
		);
		einkommensverschlechterungContainer.setEkvJABasisJahrPlus2(
			createEkvSolothurn()
		);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungContainerGS2.setEkvJABasisJahrPlus2(
			createEkvSolothurn()
		);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);
	}

	@Test
	void isFinanzielleSituationIntroducedAndComplete_EKV_Vollstaendig_LUZERN_Test() {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(FinanzielleSituationTyp.LUZERN);
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);
		final FamiliensituationContainer familiensituationContainer =
			new FamiliensituationContainer();
		gesuch.setFamiliensituationContainer(familiensituationContainer);
		final Familiensituation familiensituation = new Familiensituation();
		familiensituationContainer.setFamiliensituationJA(familiensituation);
		final Auszahlungsdaten auszahlungsdaten = new Auszahlungsdaten();
		familiensituation.setAuszahlungsdaten(auszahlungsdaten);
		auszahlungsdaten.setIban(new IBAN());
		final EinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainer =
			new EinkommensverschlechterungInfoContainer();
		gesuch.setEinkommensverschlechterungInfoContainer(
			einkommensverschlechterungInfoContainer
		);
		final EinkommensverschlechterungInfo einkommensverschlechterungInfo =
			new EinkommensverschlechterungInfo();
		einkommensverschlechterungInfoContainer
			.setEinkommensverschlechterungInfoJA(
				einkommensverschlechterungInfo
			);
		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus1(true);
		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus2(false);

		final GesuchstellerContainer gesuchsteller1 =
			new GesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		final FinanzielleSituationContainer finSitGS1 =
			new FinanzielleSituationContainer();
		gesuchsteller1.setFinanzielleSituationContainer(finSitGS1);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungInfo.setEinkommensverschlechterung(true);
		Assertions.assertFalse(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		final EinkommensverschlechterungContainer einkommensverschlechterungContainerGS1 =
			new EinkommensverschlechterungContainer();
		gesuchsteller1.setEinkommensverschlechterungContainer(
			einkommensverschlechterungContainerGS1
		);
		einkommensverschlechterungContainerGS1.setEkvJABasisJahrPlus1(
			initEKVLuzern()
		);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		final GesuchstellerContainer gesuchsteller2 =
			new GesuchstellerContainer();
		gesuch.setGesuchsteller2(gesuchsteller2);
		final EinkommensverschlechterungContainer einkommensverschlechterungContainerGS2 =
			new EinkommensverschlechterungContainer();
		gesuchsteller2.setEinkommensverschlechterungContainer(
			einkommensverschlechterungContainerGS2
		);
		einkommensverschlechterungContainerGS2.setEkvJABasisJahrPlus1(
			new Einkommensverschlechterung()
		);
		Assertions.assertFalse(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungContainerGS2.setEkvJABasisJahrPlus1(
			initEKVLuzern()
		);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus2(true);
		Assertions.assertFalse(
			service.isEKVIntroducedAndComplete(gesuch)
		);
		einkommensverschlechterungContainerGS1.setEkvJABasisJahrPlus2(
			initEKVLuzern()
		);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungContainerGS2.setEkvJABasisJahrPlus2(
			initEKVLuzern()
		);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);
	}

	@ParameterizedTest
	@EnumSource(value = FinanzielleSituationTyp.class,
		names = { "APPENZELL", "APPENZELL_FOLGEMONAT" },
		mode = Mode.INCLUDE)
	void isFinanzielleSituationIntroducedAndComplete_EKV_Vollstaendig_AR_Test(
		FinanzielleSituationTyp finanzielleSituationTyp
	) {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(finanzielleSituationTyp);
		final FamiliensituationContainer familiensituationContainer =
			new FamiliensituationContainer();
		gesuch.setFamiliensituationContainer(familiensituationContainer);
		final Familiensituation familiensituation = new Familiensituation();
		familiensituationContainer.setFamiliensituationJA(familiensituation);
		familiensituation.setGemeinsameSteuererklaerung(false);
		final EinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainer =
			new EinkommensverschlechterungInfoContainer();
		gesuch.setEinkommensverschlechterungInfoContainer(
			einkommensverschlechterungInfoContainer
		);
		final EinkommensverschlechterungInfo einkommensverschlechterungInfo =
			new EinkommensverschlechterungInfo();
		einkommensverschlechterungInfoContainer
			.setEinkommensverschlechterungInfoJA(
				einkommensverschlechterungInfo
			);
		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus1(true);
		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus2(false);

		final GesuchstellerContainer gesuchsteller1 =
			new GesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		final FinanzielleSituationContainer finSitGS1 =
			new FinanzielleSituationContainer();
		gesuchsteller1.setFinanzielleSituationContainer(finSitGS1);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungInfo.setEinkommensverschlechterung(true);
		Assertions.assertFalse(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		final EinkommensverschlechterungContainer einkommensverschlechterungContainerGS1 =
			new EinkommensverschlechterungContainer();
		gesuchsteller1.setEinkommensverschlechterungContainer(
			einkommensverschlechterungContainerGS1
		);
		einkommensverschlechterungContainerGS1.setEkvJABasisJahrPlus1(
			initEKVAppenzell()
		);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		final GesuchstellerContainer gesuchsteller2 =
			new GesuchstellerContainer();
		gesuch.setGesuchsteller2(gesuchsteller2);
		final EinkommensverschlechterungContainer einkommensverschlechterungContainerGS2 =
			new EinkommensverschlechterungContainer();
		gesuchsteller2.setEinkommensverschlechterungContainer(
			einkommensverschlechterungContainerGS2
		);
		einkommensverschlechterungContainerGS2.setEkvJABasisJahrPlus1(
			new Einkommensverschlechterung()
		);
		Assertions.assertFalse(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		familiensituation.setGemeinsameSteuererklaerung(true);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		familiensituation.setGemeinsameSteuererklaerung(false);
		einkommensverschlechterungContainerGS2.setEkvJABasisJahrPlus1(
			initEKVAppenzell()
		);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus2(true);
		Assertions.assertFalse(
			service.isEKVIntroducedAndComplete(gesuch)
		);
		einkommensverschlechterungContainerGS1.setEkvJABasisJahrPlus2(
			initEKVAppenzell()
		);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);

		einkommensverschlechterungContainerGS2.setEkvJABasisJahrPlus2(
			initEKVAppenzell()
		);
		Assertions.assertTrue(
			service.isEKVIntroducedAndComplete(gesuch)
		);
	}

	private Einkommensverschlechterung createEkvSolothurn() {
		Einkommensverschlechterung einkommensverschlechterung =
			new Einkommensverschlechterung();
		einkommensverschlechterung.setBruttolohnAbrechnung1(BigDecimal.ONE);
		einkommensverschlechterung.setBruttolohnAbrechnung2(BigDecimal.ONE);
		einkommensverschlechterung.setBruttolohnAbrechnung3(BigDecimal.ONE);
		einkommensverschlechterung.setExtraLohn(true);
		einkommensverschlechterung.setNettoVermoegen(BigDecimal.ONE);
		return einkommensverschlechterung;
	}

	private FinSitZusatzangabenAppenzell initFinSitAppenzell() {
		FinSitZusatzangabenAppenzell finSitAR =
			new FinSitZusatzangabenAppenzell();
		finSitAR.setSaeule3a(BigDecimal.TEN);
		finSitAR.setSaeule3aNichtBvg(BigDecimal.TEN);
		finSitAR.setBeruflicheVorsorge(BigDecimal.TEN);
		finSitAR.setLiegenschaftsaufwand(BigDecimal.TEN);
		finSitAR.setEinkuenfteBgsa(BigDecimal.TEN);
		finSitAR.setVorjahresverluste(BigDecimal.TEN);
		finSitAR.setPolitischeParteiSpende(BigDecimal.TEN);
		finSitAR.setLeistungAnJuristischePersonen(BigDecimal.TEN);
		return finSitAR;
	}

	private Einkommensverschlechterung initEKVAppenzell() {
		Einkommensverschlechterung einkommensverschlechterung =
			new Einkommensverschlechterung();
		einkommensverschlechterung.setFinSitZusatzangabenAppenzell(
			initFinSitAppenzell()
		);
		return einkommensverschlechterung;
	}

	private FinanzielleSituation initFinSitLuzern() {
		FinanzielleSituation finSitLuzern = new FinanzielleSituation();
		initAbstractFinSitLuzern(finSitLuzern);
		return finSitLuzern;
	}

	private Einkommensverschlechterung initEKVLuzern() {
		Einkommensverschlechterung einkommensverschlechterung =
			new Einkommensverschlechterung();
		initAbstractFinSitLuzern(einkommensverschlechterung);
		return einkommensverschlechterung;
	}

	private void initAbstractFinSitLuzern(
		AbstractFinanzielleSituation abstractFinanzielleSituation
	) {
		abstractFinanzielleSituation.setSteuerbaresEinkommen(BigDecimal.ONE);
		abstractFinanzielleSituation.setSteuerbaresVermoegen(BigDecimal.ONE);
		abstractFinanzielleSituation.setAbzuegeLiegenschaft(BigDecimal.ONE);
		abstractFinanzielleSituation.setGeschaeftsverlust(BigDecimal.ONE);
		abstractFinanzielleSituation.setEinkaeufeVorsorge(BigDecimal.ONE);
	}

	private FinanzielleSituation initFinSitBern() {
		FinanzielleSituation finSit = new FinanzielleSituation();
		initAbstractFinSitBern(finSit);
		return finSit;
	}

	private Einkommensverschlechterung initEKVBern() {
		Einkommensverschlechterung einkommensverschlechterung =
			new Einkommensverschlechterung();
		initAbstractFinSitBern(einkommensverschlechterung);
		return einkommensverschlechterung;
	}

	private void initAbstractFinSitBern(
		AbstractFinanzielleSituation abstractFinanzielleSituation
	) {
		abstractFinanzielleSituation.setNettolohn(BigDecimal.ONE);
		abstractFinanzielleSituation.setFamilienzulage(BigDecimal.ONE);
		abstractFinanzielleSituation.setErsatzeinkommen(BigDecimal.ONE);
		abstractFinanzielleSituation.setErhalteneAlimente(BigDecimal.ONE);
		abstractFinanzielleSituation.setGeleisteteAlimente(BigDecimal.ONE);
		abstractFinanzielleSituation.setSchulden(BigDecimal.ONE);
		abstractFinanzielleSituation.setBruttovermoegen(BigDecimal.ONE);
	}

}
