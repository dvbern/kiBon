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

package ch.dvbern.ebegu.services.famsitchangehandler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.dataprovider.SchwyzTestfallDataProvider;
import ch.dvbern.ebegu.testfaelle.dataprovider.TestKindParameter;
import ch.dvbern.ebegu.util.Constants;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static ch.dvbern.ebegu.util.ObjectRequiredNonNullUtils.getEinkommensverschlechterungContainer;
import static ch.dvbern.ebegu.util.ObjectRequiredNonNullUtils.getEkvJABasisJahrPlus1;
import static ch.dvbern.ebegu.util.ObjectRequiredNonNullUtils.getFamiliensituationJA;
import static ch.dvbern.ebegu.util.ObjectRequiredNonNullUtils.getFinanzielleSituationContainer;
import static ch.dvbern.ebegu.util.ObjectRequiredNonNullUtils.getFinanzielleSituationJA;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(EasyMockExtension.class)
class FamSitChangeHandlerSchwyzTest extends EasyMockSupport {

	@Mock
	private EinstellungService einstellungService;

	@Mock
	private GesuchstellerService gesuchstellerService;

	@Mock
	private FinanzielleSituationService finanzielleSituationService;

	private FamSitChangeHandlerSchwyz handler = null;

	@BeforeEach
	void setupTestee() {
		handler = new FamSitChangeHandlerSchwyz(
			gesuchstellerService,
			einstellungService,
			finanzielleSituationService
		);
	}

	@TestInstance(Lifecycle.PER_CLASS)
	@Nested
	class ErstantragTest {

		@ParameterizedTest
		@MethodSource("provideFamSitChangeToRemoveGS2Arguments")
		void changeShouldRemoveGS2Test(
			FamSitChangeTestConfiguration configuration
		) {
			Gesuch gesuch = setupGesuchWith2GS();
			getFamiliensituationContainerNullSafe(gesuch)
				.setFamiliensituationJA(configuration.newFamiliensituation);

			handler.removeGS2DataOnChangeFrom2To1GS(
				gesuch,
				configuration.newFamiliensituation,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation
			);

			assertThat(gesuch.getGesuchsteller2(), nullValue());
		}

		@ParameterizedTest
		@MethodSource("provideFamSitChangeNotToRemoveGS2Arguments")
		void changeShouldNotRemoveGS2Test(
			FamSitChangeTestConfiguration configuration
		) {
			Gesuch gesuch = setupGesuchWith2GS();
			getFamiliensituationContainerNullSafe(gesuch)
				.setFamiliensituationJA(configuration.newFamiliensituation);

			handler.removeGS2DataOnChangeFrom2To1GS(
				gesuch,
				configuration.newFamiliensituation,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation
			);

			assertThat(gesuch.getGesuchsteller2(), notNullValue());
		}

		@ParameterizedTest
		@MethodSource("provideFamSitChangeToRemoveGS1Arguments")
		void changeShouldResetEKVInfoTest(
			FamSitChangeTestConfiguration configuration
		) {
			reset(finanzielleSituationService);

			Gesuch gesuch = setupGesuchWith2GS();
			getFamiliensituationContainerNullSafe(gesuch)
				.setFamiliensituationJA(configuration.newFamiliensituation);

			handler.adaptFinSitDataOnFamSitChange(
				gesuch,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation
			);

			assertThat(
				gesuch.getEinkommensverschlechterungInfoContainer(),
				nullValue()
			);
		}

		@ParameterizedTest
		@MethodSource("provideFamSitChangeNotToRemoveGS1Arguments")
		void changeShouldNotRemoveEKVInfoTest(
			FamSitChangeTestConfiguration configuration
		) {
			Gesuch gesuch = setupGesuchWith2GS();
			getFamiliensituationContainerNullSafe(gesuch)
				.setFamiliensituationJA(configuration.newFamiliensituation);

			handler.adaptFinSitDataOnFamSitChange(
				gesuch,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation
			);

			assertThat(
				gesuch.getEinkommensverschlechterungInfoContainer(),
				notNullValue()
			);
		}

		@ParameterizedTest
		@MethodSource("provideFamSitChangeToRemoveGS1Arguments")
		void changeShouldResetFinSitEKVDatenTest(
			FamSitChangeTestConfiguration configuration
		) {
			reset(finanzielleSituationService);

			Gesuch gesuch = setupGesuchWith2GS();
			getFamiliensituationContainerNullSafe(gesuch)
				.setFamiliensituationJA(configuration.newFamiliensituation);
			Objects.requireNonNull(gesuch.getGesuchsteller1());
			finanzielleSituationService.resetCompleteSchwyzFinSitData(
				getFinanzielleSituationJA(gesuch.getGesuchsteller1()),
				gesuch.getGesuchsteller1()
			);
			expectLastCall();
			Objects.requireNonNull(
				gesuch.getGesuchsteller1()
					.getEinkommensverschlechterungContainer()
			);
			Objects.requireNonNull(
				gesuch.getGesuchsteller1()
					.getEinkommensverschlechterungContainer()
					.getEkvJABasisJahrPlus1()
			);
			finanzielleSituationService.resetCompleteSchwyzFinSitData(
				gesuch.getGesuchsteller1()
					.getEinkommensverschlechterungContainer()
					.getEkvJABasisJahrPlus1(),
				gesuch.getGesuchsteller1()
			);
			expectLastCall();
			replay(finanzielleSituationService);
			handler.adaptFinSitDataOnFamSitChange(
				gesuch,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation
			);
			verify(finanzielleSituationService);
		}

		@ParameterizedTest
		@MethodSource("provideFamSitChangeNotToRemoveGS1Arguments")
		void changeShouldNotResetFinSitEKVDatenTest(
			FamSitChangeTestConfiguration configuration
		) {
			reset(finanzielleSituationService);
			Gesuch gesuch = setupGesuchWith2GS();
			getFamiliensituationContainerNullSafe(gesuch)
				.setFamiliensituationJA(configuration.newFamiliensituation);

			// Hier sollte keine Aufruf sein, als die FinSit und EKV Feldern von GS1 nicht reseten werden duerfen
			replay(finanzielleSituationService);

			handler.adaptFinSitDataOnFamSitChange(
				gesuch,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation
			);

			verify(finanzielleSituationService);
		}

		private Stream<Arguments> provideFamSitChangeToRemoveGS2Arguments() {
			return Stream.of(
				FamSitChangeTestConfiguration.of(
					"Zu Zweit",
					createFamSitZuZweit(true)
				)
			)
				.flatMap(
					config -> Stream.of(
						FamSitChangeTestConfiguration.builder()
							.name(
								'['
									+ config.name
									+ "] to Alleine"
							)
							.newFamiliensituation(
								createFamSitAlleine()
							)
							.oldFamiliensituation(
								config.oldFamiliensituation
							)
							.build()
					).map(Arguments::of)
				);
		}

		private Stream<Arguments> provideFamSitChangeToRemoveGS1Arguments() {
			return Stream.of(
				FamSitChangeTestConfiguration.of(
					"Zu Zweit",
					createFamSitZuZweit(true)
				)
			)
				.flatMap(
					config -> Stream.of(
						FamSitChangeTestConfiguration.builder()
							.name(
								'['
									+ config.name
									+ "] to Alleine"
							)
							.newFamiliensituation(
								createFamSitAlleine()
							)
							.oldFamiliensituation(
								config.oldFamiliensituation
							)
							.build(),
						FamSitChangeTestConfiguration.builder()
							.name(
								'['
									+ config.name
									+ "] to zu Zweit"
							)
							.newFamiliensituation(
								createFamSitZuZweit(true)
							)
							.oldFamiliensituation(
								config.oldFamiliensituation
							)
							.build()
					).map(Arguments::of)
				);
		}

		private Stream<Arguments> provideFamSitChangeNotToRemoveGS1Arguments() {
			return Stream.of(
				FamSitChangeTestConfiguration.of(
					"Zu Zweit",
					createFamSitZuZweit(false)
				)
			)
				.flatMap(
					config -> Stream.of(
						FamSitChangeTestConfiguration.builder()
							.name(
								'['
									+ config.name
									+ "] to Alleine"
							)
							.newFamiliensituation(
								createFamSitAlleine()
							)
							.oldFamiliensituation(
								config.oldFamiliensituation
							)
							.build(),
						FamSitChangeTestConfiguration.builder()
							.name(
								'['
									+ config.name
									+ "] to Zu Zweit"
							)
							.newFamiliensituation(
								createFamSitZuZweit(false)
							)
							.oldFamiliensituation(
								config.oldFamiliensituation
							)
							.build()
					).map(Arguments::of)
				);
		}

		private Stream<Arguments> provideFamSitChangeNotToRemoveGS2Arguments() {
			return Stream.of(
				FamSitChangeTestConfiguration.of(
					"Zu Zweit",
					createFamSitZuZweit(true)
				)
			)
				.flatMap(
					config -> Stream.of(
						FamSitChangeTestConfiguration.builder()
							.name(
								'['
									+ config.name
									+ "] to Zu Zweit"
							)
							.newFamiliensituation(
								createFamSitZuZweit(true)
							)
							.oldFamiliensituation(
								config.oldFamiliensituation
							)
							.build()
					).map(Arguments::of)
				);
		}

		@Nonnull
		private Gesuch setupGesuchWith2GS() {
			Gesuch gesuch = TestDataUtil.createDefaultGesuch();
			final GesuchstellerContainer gs1Container = TestDataUtil
				.createDefaultGesuchstellerContainer();
			gs1Container.getErwerbspensenContainers()
				.add(TestDataUtil.createErwerbspensumContainer());
			gs1Container.setFinanzielleSituationContainer(
				new FinanzielleSituationContainer()
			);
			getFinanzielleSituationContainer(gs1Container)
				.setFinanzielleSituationJA(new FinanzielleSituation());
			getFinanzielleSituationJA(gs1Container)
				.setBruttoLohn(BigDecimal.ZERO);
			gs1Container.setEinkommensverschlechterungContainer(
				new EinkommensverschlechterungContainer()
			);
			getEinkommensverschlechterungContainer(gs1Container)
				.setEkvJABasisJahrPlus1(new Einkommensverschlechterung());
			getEkvJABasisJahrPlus1(gs1Container)
				.setBruttoLohn(BigDecimal.ZERO);
			final GesuchstellerContainer gs2Container = TestDataUtil
				.createDefaultGesuchstellerContainer();
			gs2Container.getErwerbspensenContainers()
				.add(TestDataUtil.createErwerbspensumContainer());
			gesuch.setGesuchsteller1(gs1Container);
			gesuch.setGesuchsteller2(gs2Container);
			gesuch.setFinSitTyp(FinanzielleSituationTyp.SCHWYZ);
			gesuch.setEinkommensverschlechterungInfoContainer(
				new EinkommensverschlechterungInfoContainer()
			);
			return gesuch;
		}

	}

	private Familiensituation createFamSitAlleine() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.SCHWYZ);
		familiensituation.setGesuchstellerKardinalitaet(
			EnumGesuchstellerKardinalitaet.ALLEINE
		);
		familiensituation.setGemeinsameSteuererklaerung(false);
		return familiensituation;
	}

	private Familiensituation createFamSitZuZweit(boolean gemeinsamStek) {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.SCHWYZ);
		familiensituation.setGesuchstellerKardinalitaet(
			EnumGesuchstellerKardinalitaet.ZU_ZWEIT
		);
		familiensituation.setGemeinsameSteuererklaerung(gemeinsamStek);
		familiensituation.setAenderungPer(LocalDate.now());
		return familiensituation;
	}

	@TestInstance(Lifecycle.PER_CLASS)
	@Nested
	class MutationBeforeGPTest {

		private final LocalDate eingangsdatum =
			Constants.GESUCHSPERIODE_17_18_AB.minusMonths(1);

		@ParameterizedTest
		@MethodSource("provideFamSitChangeToRemoveGS2Arguments")
		void changeShouldRemoveGS2Test(
			FamSitChangeTestConfiguration configuration
		) {
			Gesuch gesuch = setupMutationWith2GSWithEingangsdatum(
				eingangsdatum
			);
			getFamiliensituationContainerNullSafe(gesuch)
				.setFamiliensituationJA(configuration.newFamiliensituation);

			handler.removeGS2DataOnChangeFrom2To1GS(
				gesuch,
				configuration.newFamiliensituation,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation
			);

			assertThat(gesuch.getGesuchsteller2(), nullValue());
		}

		@ParameterizedTest
		@MethodSource("provideFamSitChangeToRemoveGS2Arguments")
		void changeShouldRemoveEKVInfoTest(
			FamSitChangeTestConfiguration configuration
		) {
			Gesuch gesuch = setupMutationWith2GSWithEingangsdatum(
				eingangsdatum
			);
			getFamiliensituationContainerNullSafe(gesuch)
				.setFamiliensituationJA(configuration.newFamiliensituation);

			handler.adaptFinSitDataOnFamSitChange(
				gesuch,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.newFamiliensituation
			);

			assertThat(
				gesuch.getEinkommensverschlechterungInfoContainer(),
				nullValue()
			);
		}

		private Stream<Arguments> provideFamSitChangeToRemoveGS2Arguments() {
			return Stream.of(
				FamSitChangeTestConfiguration.of(
					"Zu Zweit",
					createFamSitZuZweit(true)
				)
			)
				.flatMap(
					config -> Stream.of(
						FamSitChangeTestConfiguration.builder()
							.name(
								'['
									+ config.name
									+ "] to Alleine"
							)
							.newFamiliensituation(
								createFamSitAlleine(
									eingangsdatum
								)
							)
							.oldFamiliensituation(
								config.oldFamiliensituation
							)
							.build(),
						FamSitChangeTestConfiguration.builder()
							.name(
								'['
									+ config.name
									+ "] to Zu Zweit"
							)
							.newFamiliensituation(
								createFamSitZuZweit(
									eingangsdatum
								)
							)
							.oldFamiliensituation(
								config.oldFamiliensituation
							)
							.build()
					).map(Arguments::of)
				);
		}

	}

	@TestInstance(Lifecycle.PER_CLASS)
	@Nested
	class MutationDuringGPTest {

		private final LocalDate eingangsdatum =
			Constants.GESUCHSPERIODE_17_18_AB.plusMonths(1);

		@ParameterizedTest
		@MethodSource("provideFamSitChangeToRemoveGS2Arguments")
		void changeShouldRemoveGS2Test(
			FamSitChangeTestConfiguration configuration
		) {
			Gesuch gesuch = setupMutationWith2GSWithEingangsdatum(
				eingangsdatum
			);
			getFamiliensituationContainerNullSafe(gesuch)
				.setFamiliensituationJA(configuration.newFamiliensituation);

			handler.removeGS2DataOnChangeFrom2To1GS(
				gesuch,
				configuration.newFamiliensituation,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation
			);

			assertThat(gesuch.getGesuchsteller2(), nullValue());
		}

		@ParameterizedTest
		@MethodSource("provideFamSitChangeToRemoveGS2Arguments")
		void changeShouldResetEKVInfoTest(
			FamSitChangeTestConfiguration configuration
		) {
			Gesuch gesuch = setupMutationWith2GSWithEingangsdatum(
				eingangsdatum
			);
			getFamiliensituationContainerNullSafe(gesuch)
				.setFamiliensituationJA(configuration.newFamiliensituation);

			handler.adaptFinSitDataOnFamSitChange(
				gesuch,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation
			);

			assertThat(
				gesuch.getEinkommensverschlechterungInfoContainer(),
				nullValue()
			);
		}

		@ParameterizedTest
		@MethodSource("provideFamSitChangeToAlleine")
		void aenderungPerChangeShouldRemoveGS2Test(
			FamSitChangeTestConfiguration configuration
		) {
			Gesuch gesuch = setupMutationWith2GSWithEingangsdatum(
				eingangsdatum
			);
			getFamiliensituationContainerNullSafe(gesuch)
				.setFamiliensituationJA(configuration.newFamiliensituation);

			handler.removeGS2DataOnChangeFrom2To1GS(
				gesuch,
				configuration.newFamiliensituation,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation
			);

			assertThat(gesuch.getGesuchsteller2(), nullValue());
		}

		@ParameterizedTest
		@MethodSource("provideFamSitChangeToZuZweit")
		void aenderungPerChangeShouldNotRemoveGS2Test(
			FamSitChangeTestConfiguration configuration
		) {
			Gesuch gesuch = setupMutationWith2GSWithEingangsdatum(
				eingangsdatum
			);
			getFamiliensituationContainerNullSafe(gesuch)
				.setFamiliensituationJA(configuration.newFamiliensituation);

			handler.removeGS2DataOnChangeFrom2To1GS(
				gesuch,
				configuration.newFamiliensituation,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation
			);

			assertThat(gesuch.getGesuchsteller2(), nullValue());
		}

		private Stream<Arguments> provideFamSitChangeToRemoveGS2Arguments() {
			return Stream.concat(
				provideFamSitChangeToAlleine(),
				provideFamSitChangeToZuZweit()
			);
		}

		private Stream<Arguments> provideFamSitChangeToAlleine() {
			return Stream.of(
				FamSitChangeTestConfiguration.of(
					"Zu Zweit",
					createFamSitZuZweit(true)
				)
			)
				.map(
					config -> FamSitChangeTestConfiguration.builder()
						.name('[' + config.name + "] to Alleine")
						.newFamiliensituation(
							createFamSitAlleine(eingangsdatum)
						)
						.oldFamiliensituation(config.oldFamiliensituation)
						.build()
				)
				.map(Arguments::of);
		}

		private Stream<Arguments> provideFamSitChangeToZuZweit() {
			return Stream.of(
				FamSitChangeTestConfiguration.of(
					"Zu Zweit",
					createFamSitZuZweit(true)
				)
			)
				.map(
					config -> FamSitChangeTestConfiguration.builder()
						.name('[' + config.name + "] to Zu Zweit")
						.newFamiliensituation(
							createFamSitZuZweit(eingangsdatum)
						)
						.oldFamiliensituation(config.oldFamiliensituation)
						.build()
				)
				.map(Arguments::of);
		}
	}

	@Nonnull
	private static Gesuch setupMutationWith2GSWithEingangsdatum(
		LocalDate eingangsdatum
	) {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		Gesuch mutation =
			TestDataUtil.createMutation(
				gesuch.getDossier(),
				gesuch.getGesuchsperiode(),
				AntragStatus.IN_BEARBEITUNG_GS,
				1
			);
		final FamiliensituationContainer familiensituationContainer =
			new FamiliensituationContainer();
		familiensituationContainer.setFamiliensituationErstgesuch(
			gesuch.extractFamiliensituation()
		);
		mutation.setFamiliensituationContainer(familiensituationContainer);
		mutation.setRegelnGueltigAb(mutation.getEingangsdatum());
		final GesuchstellerContainer gs1Container = TestDataUtil
			.createDefaultGesuchstellerContainer();
		gs1Container.getErwerbspensenContainers()
			.add(TestDataUtil.createErwerbspensumContainer());
		FinanzielleSituationContainer finanzielleSituationContainer =
			new FinanzielleSituationContainer();
		finanzielleSituationContainer.setFinanzielleSituationJA(
			new FinanzielleSituation()
		);
		gs1Container.setFinanzielleSituationContainer(
			finanzielleSituationContainer
		);
		gesuch.setEinkommensverschlechterungInfoContainer(
			new EinkommensverschlechterungInfoContainer()
		);
		final GesuchstellerContainer gs2Container = TestDataUtil
			.createDefaultGesuchstellerContainer();
		gs2Container.getErwerbspensenContainers()
			.add(TestDataUtil.createErwerbspensumContainer());
		mutation.setGesuchsteller1(gs1Container);
		mutation.setGesuchsteller2(gs2Container);
		mutation.setFinSitTyp(FinanzielleSituationTyp.SCHWYZ);
		mutation.setEingangsdatum(eingangsdatum);
		return mutation;
	}

	private Familiensituation createFamSitAlleine(LocalDate eingangsdatum) {
		Familiensituation familiensituation = createFamSitAlleine();
		familiensituation.setAenderungPer(eingangsdatum);
		return familiensituation;
	}

	private Familiensituation createFamSitZuZweit(LocalDate eingangsdatum) {
		Familiensituation familiensituation = createFamSitZuZweit(true);
		familiensituation.setAenderungPer(eingangsdatum);
		familiensituation.setGemeinsameSteuererklaerung(true);
		return familiensituation;
	}

	@Nonnull
	private static FamiliensituationContainer getFamiliensituationContainerNullSafe(
		Gesuch gesuch
	) {
		return Objects.requireNonNull(gesuch.getFamiliensituationContainer());
	}

	@Nested
	class KinderabzugResetTest {

		@Test
		void shouldNotsetInPruefungIfOldFamSitIsNull() {
			Gesuch gesuch = new Gesuch();
			gesuch.setFamiliensituationContainer(
				createFamSitContainer(
					EnumGesuchstellerKardinalitaet.ALLEINE
				)
			);
			KindContainer kind = createDefaultKind();
			KindContainer before = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			handler.handlePossibleKinderabzugFragenReset(
				gesuch,
				getFamiliensituationJA(gesuch),
				null
			);

			assertThat(before.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(false));
		}

		@Test
		void shouldNotsetInPruefungIfAlleineToAlleine() {
			Gesuch gesuch = new Gesuch();
			gesuch.setFamiliensituationContainer(
				createFamSitContainer(
					EnumGesuchstellerKardinalitaet.ALLEINE
				)
			);
			KindContainer kind = createDefaultKind();
			KindContainer before = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			handler.handlePossibleKinderabzugFragenReset(
				gesuch,
				getFamiliensituationJA(gesuch),
				getFamiliensituationJA(
					createFamSitContainer(
						EnumGesuchstellerKardinalitaet.ALLEINE
					)
				)
			);

			assertThat(before.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(false));
		}

		@Test
		void shouldNotSetInPruefungIfZuZweitToZuZweitNoChange() {
			Gesuch gesuch = new Gesuch();
			gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
			gesuch.setFamiliensituationContainer(
				createFamSitContainer(
					EnumGesuchstellerKardinalitaet.ZU_ZWEIT
				)
			);
			KindContainer kind = createDefaultKind();
			KindContainer before = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			handler.handlePossibleKinderabzugFragenReset(
				gesuch,
				getFamiliensituationJA(gesuch),
				getFamiliensituationJA(
					createFamSitContainer(
						EnumGesuchstellerKardinalitaet.ZU_ZWEIT
					)
				)
			);

			assertThat(before.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(false));
		}

		@Test
		void shouldSetInPruefungIfZuZweitToZuZweitInPeriode() {
			Gesuch gesuch = new Gesuch();
			gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
			FamiliensituationContainer famsitNew = createFamSitContainer(
				EnumGesuchstellerKardinalitaet.ZU_ZWEIT
			);
			getFamiliensituationJA(famsitNew).setAenderungPer(
				TestDataUtil.START_PERIODE.plusMonths(1)
			);
			gesuch.setFamiliensituationContainer(famsitNew);
			KindContainer kind = createDefaultKind();
			KindContainer before = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			handler.handlePossibleKinderabzugFragenReset(
				gesuch,
				getFamiliensituationJA(gesuch),
				getFamiliensituationJA(
					createFamSitContainer(
						EnumGesuchstellerKardinalitaet.ZU_ZWEIT
					)
				)
			);

			assertThat(before.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(true));
		}

		@Test
		void shouldSetInPruefungIfZuZweitToZuZweitVorPeriode() {
			Gesuch gesuch = new Gesuch();
			gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
			FamiliensituationContainer famsitNew = createFamSitContainer(
				EnumGesuchstellerKardinalitaet.ZU_ZWEIT
			);
			getFamiliensituationJA(famsitNew).setAenderungPer(
				TestDataUtil.START_PERIODE.minusMonths(1)
			);
			gesuch.setFamiliensituationContainer(famsitNew);
			KindContainer kind = createDefaultKind();
			KindContainer before = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			handler.handlePossibleKinderabzugFragenReset(
				gesuch,
				getFamiliensituationJA(gesuch),
				getFamiliensituationJA(
					createFamSitContainer(
						EnumGesuchstellerKardinalitaet.ZU_ZWEIT
					)
				)
			);

			assertThat(before.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(true));
		}

		@Test
		void shouldResetOnChangeFromAlleineToZuZweit() {
			Gesuch gesuch = new Gesuch();
			gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
			gesuch.setFamiliensituationContainer(
				createFamSitContainer(
					EnumGesuchstellerKardinalitaet.ALLEINE
				)
			);
			KindContainer kind = createDefaultKind();
			KindContainer before = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			handler.handlePossibleKinderabzugFragenReset(
				gesuch,
				getFamiliensituationJA(gesuch),
				getFamiliensituationJA(
					createFamSitContainer(
						EnumGesuchstellerKardinalitaet.ZU_ZWEIT
					)
				)
			);

			assertThat(before.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(true));
		}

		@Test
		void shouldResetOnChangeFromZuZweitToAlleine() {
			Gesuch gesuch = new Gesuch();
			gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
			gesuch.setFamiliensituationContainer(
				createFamSitContainer(
					EnumGesuchstellerKardinalitaet.ZU_ZWEIT
				)
			);
			KindContainer kind = createDefaultKind();
			KindContainer copy = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			handler.handlePossibleKinderabzugFragenReset(
				gesuch,
				getFamiliensituationJA(gesuch),
				getFamiliensituationJA(
					createFamSitContainer(
						EnumGesuchstellerKardinalitaet.ALLEINE
					)
				)
			);

			assertThat(copy.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(true));
		}

		private KindContainer createDefaultKind() {
			KindContainer kindContainer = new KindContainer();
			Kind kind = new Kind();
			SchwyzTestfallDataProvider.setSchwyzKindData(
				TestKindParameter.builder()
					.kind(kind)
					.geschlecht(Geschlecht.WEIBLICH)
					.name("Testkind")
					.vorname("Lara")
					.geburtsdatum(
						TestDataUtil.START_PERIODE.minusYears(5)
					)
					.betreuung(true)
					.build()
			);
			kindContainer.setKindJA(kind);
			return kindContainer;
		}

	}

	@Nonnull
	private static FamiliensituationContainer createFamSitContainer(
		EnumGesuchstellerKardinalitaet gesuchstellerKardinalitaet
	) {
		final FamiliensituationContainer familiensituationContainer =
			TestDataUtil.createDefaultFamiliensituationContainer();
		Objects.requireNonNull(
			familiensituationContainer.getFamiliensituationJA()
		);
		familiensituationContainer.getFamiliensituationJA()
			.setFamilienstatus(EnumFamilienstatus.SCHWYZ);
		familiensituationContainer.getFamiliensituationJA()
			.setGesuchstellerKardinalitaet(gesuchstellerKardinalitaet);
		return familiensituationContainer;
	}

}
