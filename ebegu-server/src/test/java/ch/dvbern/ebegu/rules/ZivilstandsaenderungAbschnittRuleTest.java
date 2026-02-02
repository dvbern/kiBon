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

package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(EasyMockExtension.class)
class ZivilstandsaenderungAbschnittRuleTest extends EasyMockSupport {
	@Mock
	private AbstractPlatz mockPlatz;
	@Mock
	private Gesuch mockGesuch;
	@Mock(MockType.NICE)
	private Familiensituation mockFamiliensituation;
	@Mock(MockType.NICE)
	private Familiensituation mockFamiliensituationErstgesuch;
	@Mock(MockType.NICE)
	private Mandant mockMandant;
	@TestSubject
	private ZivilstandsaenderungAbschnittRule zivilstandsaenderungAbschnittRule =
		new ZivilstandsaenderungAbschnittRule(
			Constants.DEFAULT_GUELTIGKEIT,
			2,
			Locale.GERMAN
		);

	private final LocalDate GESUCHSPERIODE_START = LocalDate.of(2025, 8, 1);
	private final LocalDate GESUCHSPERIODE_END = LocalDate.of(2026, 7, 31);

	@BeforeEach
	void setUp() {

		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		DateRange gueltigkeit = new DateRange();
		gueltigkeit.setGueltigAb(
			GESUCHSPERIODE_START
		);
		gueltigkeit.setGueltigBis(GESUCHSPERIODE_END);
		gesuchsperiode.setGueltigkeit(gueltigkeit);

		// Setup common mock expectations
		expect(mockPlatz.extractGesuch()).andReturn(mockGesuch).anyTimes();
		expect(mockGesuch.extractFamiliensituation()).andReturn(
			mockFamiliensituation
		).anyTimes();
		expect(mockGesuch.extractFamiliensituationErstgesuch()).andReturn(
			mockFamiliensituationErstgesuch
		).anyTimes();
		expect(mockGesuch.getGesuchsperiode()).andReturn(gesuchsperiode)
			.anyTimes();
		expect(mockFamiliensituation.isSpezialFallAR()).andReturn(false)
			.anyTimes();
		expect(mockFamiliensituationErstgesuch.isSpezialFallAR()).andReturn(
			false
		).anyTimes();
	}

	@Nested
	class Legacy {

		@BeforeEach
		void setup() {
			expect(
				mockFamiliensituation.hasSecondGesuchsteller(
					GESUCHSPERIODE_END
				)
			).andReturn(true).anyTimes();
			expect(
				mockFamiliensituationErstgesuch.hasSecondGesuchsteller(
					GESUCHSPERIODE_END
				)
			).andReturn(false).anyTimes();

		}

		@ParameterizedTest
		@EnumSource(
			value = EnumFamilienstatus.class,
			names = { "SCHWYZ", },
			mode = Mode.EXCLUDE
		)
		void testCreateVerfuegungsZeitabschnitte_ChangeDetected(
			EnumFamilienstatus enumFamilienstatus
		) {
			var stichtag = GESUCHSPERIODE_START.plusDays(30);
			expect(mockFamiliensituation.getAenderungPer()).andReturn(stichtag)
				.anyTimes();
			expect(mockGesuch.extractMandant()).andReturn(mockMandant)
				.anyTimes();
			expect(mockMandant.getMandantIdentifier()).andReturn(
				MandantIdentifier.BERN
			).anyTimes();
			expect(mockFamiliensituation.getFamilienstatus()).andReturn(
				enumFamilienstatus
			).once();

			replayAll();

			List<VerfuegungZeitabschnitt> result =
				zivilstandsaenderungAbschnittRule
					.createVerfuegungsZeitabschnitte(mockPlatz);

			// Assert that the new FamSit start the month after the AenderungPer Date until the end of the Gesuchsperiode
			assertThat(
				result.get(1).getGueltigkeit(),
				is(
					new DateRange(
						stichtag.plusMonths(1).withDayOfMonth(1),
						GESUCHSPERIODE_END
					)
				)
			);

			verifyAll();
		}

		@Test
		void shouldCreateAbschnitte_WhenAnzahlGesuchstellendeChangesOnErstantrag() {
			EnumGesuchstellerKardinalitaet antragKardinalitaetErstrantrag =
				EnumGesuchstellerKardinalitaet.ALLEINE;
			EnumFamilienstatus familienstatusErstantrag =
				EnumFamilienstatus.KONKUBINAT_KEIN_KIND;
			LocalDate konkubinatStartErstantrag = GESUCHSPERIODE_START
				.minusYears(2)
				.plusMonths(2);
			boolean geteilteObhutErstantrag = true;

			var mutationDatum = LocalDate.of(
				GESUCHSPERIODE_END.getYear(),
				2,
				1
			);
			var familiensituationAenderungPer = LocalDate.of(
				GESUCHSPERIODE_END.getYear(),
				2,
				15
			);
			EnumFamilienstatus familienstatusMutation =
				EnumFamilienstatus.ALLEINERZIEHEND;

			expect(mockGesuch.extractMandant()).andReturn(mockMandant)
				.anyTimes();
			expect(mockMandant.getMandantIdentifier()).andReturn(
				MandantIdentifier.BERN
			).anyTimes();
			expect(mockGesuch.getRegelStartDatum()).andReturn(mutationDatum)
				.anyTimes();
			expect(mockFamiliensituation.getFamilienstatus()).andReturn(
				familienstatusMutation
			).anyTimes();
			expect(mockFamiliensituation.getAenderungPer()).andReturn(
				familiensituationAenderungPer
			).anyTimes();
			expect(mockFamiliensituationErstgesuch.getFamilienstatus())
				.andReturn(
					familienstatusErstantrag
				)
				.once();
			expect(mockFamiliensituationErstgesuch.getStartKonkubinat())
				.andReturn(
					konkubinatStartErstantrag
				)
				.anyTimes();
			expect(mockFamiliensituationErstgesuch.getGeteilteObhut())
				.andReturn(
					geteilteObhutErstantrag
				)
				.anyTimes();
			expect(
				mockFamiliensituationErstgesuch.getGesuchstellerKardinalitaet()
			)
				.andReturn(antragKardinalitaetErstrantrag)
				.anyTimes();

			replayAll();

			List<VerfuegungZeitabschnitt> result =
				zivilstandsaenderungAbschnittRule
					.createVerfuegungsZeitabschnitte(mockPlatz);

			// Assert that the new FamSit start the month after the AenderungPer Date until the end of the Gesuchsperiode
			assertThat(
				result.get(0).getGueltigkeit(),
				is(
					new DateRange(
						GESUCHSPERIODE_START,
						LocalDate.of(
							GESUCHSPERIODE_START.getYear(),
							konkubinatStartErstantrag.getMonth(),
							konkubinatStartErstantrag.getDayOfMonth()
						).with(TemporalAdjusters.lastDayOfMonth())
					)
				)
			);

			// Assert that the new FamSit start the month after the AenderungPer Date until the end of the Gesuchsperiode
			assertThat(
				result.get(1).getGueltigkeit(),
				is(
					new DateRange(
						LocalDate.of(
							GESUCHSPERIODE_START.getYear(),
							konkubinatStartErstantrag.getMonth(),
							konkubinatStartErstantrag.getDayOfMonth()
						).with(TemporalAdjusters.firstDayOfNextMonth()),
						familiensituationAenderungPer.with(
							TemporalAdjusters.lastDayOfMonth()
						)
					)
				)
			);

			// Assert that the new FamSit start the month after the AenderungPer Date until the end of the Gesuchsperiode
			assertThat(
				result.get(2).getGueltigkeit(),
				is(
					new DateRange(
						familiensituationAenderungPer.with(
							TemporalAdjusters.firstDayOfNextMonth()
						),
						GESUCHSPERIODE_END
					)
				)
			);

			verifyAll();
		}

		@Test
		void testCreateVerfuegungsZeitabschnitte_Schwyz() {
			var stichtag = GESUCHSPERIODE_START.plusDays(30);
			expect(mockGesuch.extractMandant()).andReturn(mockMandant)
				.anyTimes();
			expect(mockMandant.getMandantIdentifier()).andReturn(
				MandantIdentifier.SCHWYZ
			).anyTimes();
			expect(mockFamiliensituation.getFamilienstatus()).andReturn(
				EnumFamilienstatus.SCHWYZ
			).once();
			expect(mockGesuch.getRegelStartDatum()).andReturn(
				stichtag
			).anyTimes();

			replayAll();

			List<VerfuegungZeitabschnitt> result =
				zivilstandsaenderungAbschnittRule
					.createVerfuegungsZeitabschnitte(mockPlatz);

			// Assert that the new FamSit start at the beginning of the Gesuchsperiode until the end of the Gesuchsperiode
			assertThat(
				result.get(0).getGueltigkeit(),
				is(new DateRange(GESUCHSPERIODE_START, GESUCHSPERIODE_END))
			);

			verifyAll();
		}
	}

	@Nested
	class KonkubinatBecomes2JaehrigDuringPeriode {
		LocalDate startdatumKonkubinat = LocalDate.of(
			GESUCHSPERIODE_START.getYear() - 2,
			12,
			15
		);
		LocalDate stichtagKonkubinat = startdatumKonkubinat.plusYears(2);

		@BeforeEach
		void setup() {
			expect(mockGesuch.extractMandant()).andReturn(mockMandant)
				.anyTimes();
			expect(mockMandant.getMandantIdentifier()).andReturn(
				MandantIdentifier.BERN
			).anyTimes();
			// set up visible data
			expect(mockFamiliensituationErstgesuch.getFamilienstatus())
				.andReturn(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
			expect(mockFamiliensituationErstgesuch.getStartKonkubinat())
				.andReturn(startdatumKonkubinat)
				.anyTimes();

			// set up calculated background stuff
			expect(
				mockFamiliensituationErstgesuch.hasSecondGesuchsteller(
					stichtagKonkubinat.with(TemporalAdjusters.lastDayOfMonth())
				)
			).andReturn(false).anyTimes();
			expect(
				mockFamiliensituationErstgesuch.hasSecondGesuchsteller(
					GESUCHSPERIODE_END
				)
			).andReturn(true).anyTimes();

		}

		@Nested
		class FamSitChangesToOneGSInMutation {
			LocalDate famSitAenderungPer = LocalDate.of(
				GESUCHSPERIODE_END.getYear(),
				2,
				15
			);

			@BeforeEach
			void setup() {
				// set up visible data
				expect(mockFamiliensituation.getFamilienstatus()).andReturn(
					EnumFamilienstatus.ALLEINERZIEHEND
				);
				expect(mockFamiliensituation.getAenderungPer()).andReturn(
					famSitAenderungPer
				).anyTimes();
				expect(mockFamiliensituation.getGeteilteObhut()).andReturn(
					true
				);
				expect(mockFamiliensituation.getGesuchstellerKardinalitaet())
					.andReturn(EnumGesuchstellerKardinalitaet.ALLEINE);

				// set up background calculation stuff
				expect(
					mockFamiliensituationErstgesuch.hasSecondGesuchsteller(
						famSitAenderungPer.with(
							TemporalAdjusters.lastDayOfMonth()
						)
					)
				).andReturn(true).anyTimes();
				expect(
					mockFamiliensituation.hasSecondGesuchsteller(
						startdatumKonkubinat.with(
							TemporalAdjusters.lastDayOfMonth()
						)
					)
				).andReturn(false).anyTimes();
				expect(
					mockFamiliensituation.hasSecondGesuchsteller(
						famSitAenderungPer.with(
							TemporalAdjusters.lastDayOfMonth()
						)
					)
				).andReturn(true).anyTimes();
				expect(
					mockFamiliensituation.hasSecondGesuchsteller(
						GESUCHSPERIODE_END
					)
				).andReturn(false).anyTimes();
			}

			@Test
			void shouldHaveFirstAbschnittFromGPStartToEndOfMonthOfKonkubinatStichtag() {
				replayAll();

				var createdAbschnitte = zivilstandsaenderungAbschnittRule
					.createVerfuegungsZeitabschnitte(mockPlatz);

				assertThat(
					createdAbschnitte.get(0).getGueltigkeit().getGueltigAb(),
					is(GESUCHSPERIODE_START)
				);
				assertThat(
					createdAbschnitte.get(0).getGueltigkeit().getGueltigBis(),
					is(
						stichtagKonkubinat.with(
							TemporalAdjusters.lastDayOfMonth()
						)
					)
				);
			}

			@Test
			void shouldHaveSecondAbschnittFromNextMonthOfKonkubinatStichtagUntilEndOfMonthOfFamSitAenderungPer() {
				replayAll();

				var createdAbschnitte = zivilstandsaenderungAbschnittRule
					.createVerfuegungsZeitabschnitte(mockPlatz);

				assertThat(
					createdAbschnitte.get(1).getGueltigkeit().getGueltigAb(),
					is(
						stichtagKonkubinat.with(
							TemporalAdjusters.firstDayOfNextMonth()
						)
					)
				);
				assertThat(
					createdAbschnitte.get(1).getGueltigkeit().getGueltigBis(),
					is(
						famSitAenderungPer.with(
							TemporalAdjusters.lastDayOfMonth()
						)
					)
				);
			}

			@Test
			void shouldHaveThirdAbschnittFromNextMonthOfFamSitAenderungPerUntilGPEnd() {
				replayAll();

				var createdAbschnitte = zivilstandsaenderungAbschnittRule
					.createVerfuegungsZeitabschnitte(mockPlatz);

				assertThat(
					createdAbschnitte.get(2).getGueltigkeit().getGueltigAb(),
					is(
						famSitAenderungPer.with(
							TemporalAdjusters.firstDayOfNextMonth()
						)
					)
				);
				assertThat(
					createdAbschnitte.get(2).getGueltigkeit().getGueltigBis(),
					is(GESUCHSPERIODE_END)
				);
			}

			@Test
			void shouldHaveOneGSForFinSitFromGPStartToEndOfMonthOfKonkubinatStichtag() {

				replayAll();

				var createdAbschnitte = zivilstandsaenderungAbschnittRule
					.createVerfuegungsZeitabschnitte(mockPlatz);

				assertThat(
					createdAbschnitte.get(0)
						.getBgCalculationInputAsiv()
						.isHasSecondGesuchstellerForFinanzielleSituation(),
					is(false)
				);
				assertThat(
					createdAbschnitte.get(0)
						.getBgCalculationInputGemeinde()
						.isHasSecondGesuchstellerForFinanzielleSituation(),
					is(false)
				);
			}

			@Test
			void shouldHaveTwoGSForFinSitFromNextMonthOfKonkubinatStichtagUntilEndOfMonthOfFamSitAenderungPer() {

				replayAll();

				var createdAbschnitte = zivilstandsaenderungAbschnittRule
					.createVerfuegungsZeitabschnitte(mockPlatz);

				assertThat(
					createdAbschnitte.get(1)
						.getBgCalculationInputAsiv()
						.isHasSecondGesuchstellerForFinanzielleSituation(),
					is(true)
				);
				assertThat(
					createdAbschnitte.get(1)
						.getBgCalculationInputGemeinde()
						.isHasSecondGesuchstellerForFinanzielleSituation(),
					is(true)
				);
			}

			@Test
			void shouldHaveOneGSForFinSitFromNextMonthOfFamSitAenderungPerUntilGPEnd() {

				replayAll();

				var createdAbschnitte = zivilstandsaenderungAbschnittRule
					.createVerfuegungsZeitabschnitte(mockPlatz);

				assertThat(
					createdAbschnitte.get(2)
						.getBgCalculationInputAsiv()
						.isHasSecondGesuchstellerForFinanzielleSituation(),
					is(false)
				);
				assertThat(
					createdAbschnitte.get(2)
						.getBgCalculationInputGemeinde()
						.isHasSecondGesuchstellerForFinanzielleSituation(),
					is(false)
				);
			}
		}

	}

}
