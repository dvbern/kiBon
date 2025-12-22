package ch.dvbern.ebegu.rules.mutationsmerger.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class VerfuegungZeitabschnittSplitterTest {
	private static final LocalDate AUG_1 = LocalDate.of(2024, 8, 1);
	private static final LocalDate AUG_10 = LocalDate.of(2024, 8, 10);
	private static final LocalDate AUG_15 = LocalDate.of(2024, 8, 15);
	private static final LocalDate AUG_16 = LocalDate.of(2024, 8, 16);
	private static final LocalDate AUG_19 = LocalDate.of(2024, 8, 19);
	private static final LocalDate AUG_20 = LocalDate.of(2024, 8, 20);
	private static final LocalDate AUG_22 = LocalDate.of(2024, 8, 22);
	private static final LocalDate AUG_31 = LocalDate.of(2024, 8, 31);

	@Nested
	class OneZeitabschnittToSplitOn {
		LocalDate BASE_START = AUG_10;
		LocalDate BASE_END = AUG_22;
		VerfuegungZeitabschnitt base = setupBase(
			new DateRange(BASE_START, BASE_END)
		);

		@Nested
		class OneDayGueltigkeit {
			LocalDate TO_SPLIT_ON_START = AUG_15;
			LocalDate TO_SPLIT_ON_END = AUG_15;
			List<VerfuegungZeitabschnitt> toSplitOn =
				createZeitabschnittListToSplitOn(
					List.of(new DateRange(TO_SPLIT_ON_START, TO_SPLIT_ON_END))
				);

			@Test
			void splitShouldResultInThreeZeitabschnitte() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(split.size(), is(3));
			}

			@Test
			void firstZeitabschnittShouldStartOnBaseStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigAb(),
					is(BASE_START)
				);
			}

			@Test
			void firstZeitabschnittShouldEndDayBeforeToSplitOnStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigBis(),
					is(TO_SPLIT_ON_START.minusDays(1))
				);
			}

			@ParameterizedTest()
			@ValueSource(ints = { 0, 1, 2 })
			void allZeitabschnitteShouldHaveSameASIVInputAsBase(
				int zeitabschnittIndex
			) {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(zeitabschnittIndex)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}

			@ParameterizedTest()
			@ValueSource(ints = { 0, 1, 2 })
			void allZeitabschnitteShouldHaveSameGemeindeInputAsBase(
				int zeitabschnittIndex
			) {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(zeitabschnittIndex)
						.getBgCalculationInputGemeinde()
						.isSame(base.getBgCalculationInputGemeinde()),
					is(true)
				);
			}

			@Test
			void secondZeitabschnittShouldStartOnToSplitOnStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigAb(),
					is(TO_SPLIT_ON_START)
				);
			}

			@Test
			void secondZeitabschnittShouldEndOnToSplitOnEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigBis(),
					is(TO_SPLIT_ON_END)
				);
			}

			@Test
			void thirdZeitabschnittShouldStartDayAfterOnToSplitOnEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(2).getGueltigkeit().getGueltigAb(),
					is(TO_SPLIT_ON_END.plusDays(1))
				);
			}

			@Test
			void thirdZeitabschnittShouldEndOnBaseEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(2).getGueltigkeit().getGueltigBis(),
					is(BASE_END)
				);
			}
		}

		@Nested
		class SameGueltigkeitAsBase {
			LocalDate TO_SPLIT_ON_START = BASE_START;
			LocalDate TO_SPLIT_ON_END = BASE_END;
			List<VerfuegungZeitabschnitt> toSplitOn =
				createZeitabschnittListToSplitOn(
					List.of(new DateRange(TO_SPLIT_ON_START, TO_SPLIT_ON_END))
				);

			@Test
			void splitShouldResultInTwoZeitabschnitte() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(split.size(), is(1));
			}

			@Test
			void firstZeitabschnittShouldStartOnBaseStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigAb(),
					is(BASE_START)
				);
			}

			@Test
			void firstZeitabschnittShouldEndOnBaseEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigBis(),
					is(BASE_END)
				);
			}

			@Test
			void firstZeitabschnittShouldHaveSameASIVInputAsBase() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}

			@Test
			void firstZeitabschnittShouldHaveSameGemeindeInputAsBase() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}
		}

		@Nested
		class ZeitabschnittToSplitOnEndsOnStart {
			LocalDate TO_SPLIT_ON_START = AUG_1;
			LocalDate TO_SPLIT_ON_END = BASE_START;
			List<VerfuegungZeitabschnitt> toSplitOn =
				createZeitabschnittListToSplitOn(
					List.of(new DateRange(TO_SPLIT_ON_START, TO_SPLIT_ON_END))
				);

			@Test
			void splitShouldResultInTwoZeitabschnitte() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(split.size(), is(2));
			}

			@Test
			void firstZeitabschnittShouldStartOnBaseStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigAb(),
					is(BASE_START)
				);
			}

			@Test
			void firstZeitabschnittShouldEndOnBaseStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigBis(),
					is(BASE_START)
				);
			}

			@Test
			void secondZeitabschnittShouldStartDayAfterBaseStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigAb(),
					is(BASE_START.plusDays(1))
				);
			}

			@Test
			void secondZeitabschnittShouldEndOnBasEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigBis(),
					is(BASE_END)
				);
			}

			@ParameterizedTest()
			@ValueSource(ints = { 0, 1 })
			void allZeitabschnitteShouldHaveSameASIVInputAsBase(
				int zeitabschnittIndex
			) {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(zeitabschnittIndex)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}

			@ParameterizedTest()
			@ValueSource(ints = { 0, 1 })
			void allZeitabschnitteShouldHaveSameGemeindeInputAsBase(
				int zeitabschnittIndex
			) {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(zeitabschnittIndex)
						.getBgCalculationInputGemeinde()
						.isSame(base.getBgCalculationInputGemeinde()),
					is(true)
				);
			}
		}

		@Nested
		class ZeitabschnittToSplitOnStartsDuringAndEndsOnStart {
			LocalDate TO_SPLIT_ON_START = AUG_15;
			LocalDate TO_SPLIT_ON_END = BASE_END;
			List<VerfuegungZeitabschnitt> toSplitOn =
				createZeitabschnittListToSplitOn(
					List.of(new DateRange(TO_SPLIT_ON_START, TO_SPLIT_ON_END))
				);

			@Test
			void splitShouldResultInTwoZeitabschnitte() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(split.size(), is(2));
			}

			@Test
			void firstZeitabschnittShouldStartOnBaseStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigAb(),
					is(BASE_START)
				);
			}

			@Test
			void firstZeitabschnittShouldEndDayBeforeToSplitOnStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigBis(),
					is(TO_SPLIT_ON_START.minusDays(1))
				);
			}

			@Test
			void secondZeitabschnittShouldStartOnToSplitOnStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigAb(),
					is(TO_SPLIT_ON_START)
				);
			}

			@Test
			void secondZeitabschnittShouldEndOnBaseEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigBis(),
					is(BASE_END)
				);
			}

			@ParameterizedTest()
			@ValueSource(ints = { 0, 1 })
			void allZeitabschnitteShouldHaveSameASIVInputAsBase(
				int zeitabschnittIndex
			) {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(zeitabschnittIndex)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}

			@ParameterizedTest()
			@ValueSource(ints = { 0, 1 })
			void allZeitabschnitteShouldHaveSameGemeindeInputAsBase(
				int zeitabschnittIndex
			) {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(zeitabschnittIndex)
						.getBgCalculationInputGemeinde()
						.isSame(base.getBgCalculationInputGemeinde()),
					is(true)
				);
			}
		}

		@Nested
		class ZeitabschnittToSplitOnStartsBeforeAndEndsOnEnd {
			LocalDate TO_SPLIT_ON_START = AUG_1;
			LocalDate TO_SPLIT_ON_END = BASE_END;
			List<VerfuegungZeitabschnitt> toSplitOn =
				createZeitabschnittListToSplitOn(
					List.of(new DateRange(TO_SPLIT_ON_START, TO_SPLIT_ON_END))
				);

			@Test
			void splitShouldResultInTwoZeitabschnitte() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(split.size(), is(1));
			}

			@Test
			void firstZeitabschnittShouldStartOnBaseStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigAb(),
					is(BASE_START)
				);
			}

			@Test
			void firstZeitabschnittShouldEndOnBaseEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigBis(),
					is(BASE_END)
				);
			}

			@Test
			void firstZeitabschnittShouldHaveSameASIVInputAsBase() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}

			@Test
			void firstZeitabschnittShouldHaveSameGemeindeInputAsBase() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}
		}

		@Nested
		class ZeitabschnittToSplitOnStartsBeforeAndEndsDuring {

			LocalDate TO_SPLIT_ON_START = AUG_1;
			LocalDate TO_SPLIT_ON_END = AUG_15;
			List<VerfuegungZeitabschnitt> toSplitOn =
				createZeitabschnittListToSplitOn(
					List.of(new DateRange(TO_SPLIT_ON_START, TO_SPLIT_ON_END))
				);

			@Test
			void splitShouldResultInTwoZeitabschnitte() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(split.size(), is(2));
			}

			@Test
			void shouldStartFirstAbschnittOnBaseStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigAb(),
					equalTo(BASE_START)
				);
			}

			@Test
			void shouldEndFirstAbschnittOnToSplitOnEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigBis(),
					equalTo(TO_SPLIT_ON_END)
				);
			}

			@Test
			void shouldHaveSameAsivInputOnFirstAbschnittAsBase() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}

			@Test
			void shouldHaveSameGemeindeInputOnFirstAbschnittAsBase() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0)
						.getBgCalculationInputGemeinde()
						.isSame(base.getBgCalculationInputGemeinde()),
					is(true)
				);
			}

			@Test
			void shouldStartSecondAbschnittDayAfterToSplitOnEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigAb(),
					equalTo(TO_SPLIT_ON_END.plusDays(1))
				);
			}

			@Test
			void shouldEndSecondAbschnittOnBaseEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigBis(),
					equalTo(BASE_END)
				);
			}

			@Test
			void shouldHaveSameAsivInputOnSecondAbschnittAsBase() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}

			@Test
			void shouldHaveSameGemeindeInputOnSecondAbschnittAsBase() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1)
						.getBgCalculationInputGemeinde()
						.isSame(base.getBgCalculationInputGemeinde()),
					is(true)
				);
			}
		}

		@Nested
		class ZeitabschnittToSplitOnStartsDuringAndEndsAfter {

			LocalDate TO_SPLIT_ON_START = AUG_15;
			LocalDate TO_SPLIT_ON_END = AUG_31;
			List<VerfuegungZeitabschnitt> toSplitOn =
				createZeitabschnittListToSplitOn(
					List.of(new DateRange(TO_SPLIT_ON_START, TO_SPLIT_ON_END))
				);

			@Test
			void splitShouldResultInTwoZeitabschnitte() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(split.size(), is(2));
			}

			@Test
			void shouldStartFirstAbschnittOnBaseStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigAb(),
					equalTo(BASE_START)
				);
			}

			@Test
			void shouldEndFirstAbschnittDayBeforeToSplitOnStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigBis(),
					equalTo(TO_SPLIT_ON_START.minusDays(1))
				);
			}

			@Test
			void shouldHaveSameAsivInputOnFirstAbschnittAsBase() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}

			@Test
			void shouldHaveSameGemeindeInputOnFirstAbschnittAsBase() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0)
						.getBgCalculationInputGemeinde()
						.isSame(base.getBgCalculationInputGemeinde()),
					is(true)
				);
			}

			@Test
			void shouldStartSecondAbschnittOnToSplitOnStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigAb(),
					equalTo(TO_SPLIT_ON_START)
				);
			}

			@Test
			void shouldEndSecondAbschnittOnBaseEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigBis(),
					equalTo(BASE_END)
				);
			}

			@Test
			void shouldHaveSameAsivInputOnSecondAbschnittAsBase() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}

			@Test
			void shouldHaveSameGemeindeInputOnSecondAbschnittAsBase() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1)
						.getBgCalculationInputGemeinde()
						.isSame(base.getBgCalculationInputGemeinde()),
					is(true)
				);
			}
		}

		@Nested
		class ZeitabschnittToSplitOnStartsAndEndsDuring {
			LocalDate TO_SPLIT_ON_START = AUG_15;
			LocalDate TO_SPLIT_ON_END = AUG_19;
			List<VerfuegungZeitabschnitt> toSplitOn =
				createZeitabschnittListToSplitOn(
					List.of(new DateRange(TO_SPLIT_ON_START, TO_SPLIT_ON_END))
				);

			@Test
			void shouldResultInThreeZeitabschnitte() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(split.size(), is(3));
			}

			@Test
			void shouldStartFirstAbschnittOnBaseStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigAb(),
					equalTo(BASE_START)
				);
			}

			@Test
			void shouldEndFirstAbschnittOnDayBeforeToSplitOnStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigBis(),
					equalTo(TO_SPLIT_ON_START.minusDays(1))
				);
			}

			@Test
			void shouldStartSecondAbschnittOnToSplitOnStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigAb(),
					equalTo(TO_SPLIT_ON_START)
				);
			}

			@Test
			void shouldEndSecondAbschnittOnToSplitOnEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigBis(),
					equalTo(TO_SPLIT_ON_END)
				);
			}

			@Test
			void shouldStartThirdAbschnittDayAfterToSplitOnEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(2).getGueltigkeit().getGueltigAb(),
					equalTo(TO_SPLIT_ON_END.plusDays(1))
				);
			}

			@Test
			void shouldEndThirdAbschnittOnBaseEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(2).getGueltigkeit().getGueltigBis(),
					equalTo(BASE_END)
				);
			}

			@ParameterizedTest()
			@ValueSource(ints = { 0, 1, 2 })
			void allZeitabschnitteShouldHaveSameASIVInputAsBase(
				int zeitabschnittIndex
			) {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(zeitabschnittIndex)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}

			@ParameterizedTest()
			@ValueSource(ints = { 0, 1, 2 })
			void allZeitabschnitteShouldHaveSameGemeindeInputAsBase(
				int zeitabschnittIndex
			) {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(zeitabschnittIndex)
						.getBgCalculationInputGemeinde()
						.isSame(base.getBgCalculationInputGemeinde()),
					is(true)
				);
			}
		}

	}

	@Nested
	class NoZeitabschnittToSplitOn {
		VerfuegungZeitabschnitt base = setupBase(new DateRange(AUG_10, AUG_15));
		List<VerfuegungZeitabschnitt> toSplitOn = List.of();

		@Test
		void shouldReturnListOfOneZeitabschnittIfToSplitOnIsEmptyList() {
			List<VerfuegungZeitabschnitt> split =
				VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
			assertThat(split.size(), is(1));
		}

		@Test
		void shouldReturnListOfBaseIfToSplitOnIsEmptyList() {
			List<VerfuegungZeitabschnitt> split =
				VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
			assertThat(split.get(0), equalTo(base));
		}
	}

	@Nested
	class MutlipleZeitabschnitteToSplitOn {
		LocalDate BASE_START = AUG_10;
		LocalDate BASE_END = AUG_22;
		VerfuegungZeitabschnitt base = setupBase(
			new DateRange(BASE_START, BASE_END)
		);

		@Nested
		class ZeitabschnitteStartOnBaseStartAndEndOnBaseEnd {
			LocalDate TO_SPLIT_ON_START_1 = BASE_START;
			LocalDate TO_SPLIT_ON_END_1 = AUG_15;
			LocalDate TO_SPLIT_ON_START_2 = AUG_16;
			LocalDate TO_SPLIT_ON_END_2 = BASE_END;
			List<VerfuegungZeitabschnitt> toSplitOn =
				createZeitabschnittListToSplitOn(
					List.of(
						new DateRange(TO_SPLIT_ON_START_1, TO_SPLIT_ON_END_1),
						new DateRange(TO_SPLIT_ON_START_2, TO_SPLIT_ON_END_2)
					)
				);

			@Test
			void shouldCreateTwoZeitabschnitte() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(split.size(), is(2));
			}

			@Test
			void shouldStartSecondZeitabschnittOnBaseStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigAb(),
					equalTo(BASE_START)
				);
			}

			@Test
			void shouldEndFirstZeitabschnittOnFirstZeitabschnittToSplitOnEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigBis(),
					equalTo(TO_SPLIT_ON_END_1)
				);
			}

			@Test
			void shouldStartSecondZeitabschnittOnDayOnSecondZeitabschnittToSplitOnStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigAb(),
					equalTo(TO_SPLIT_ON_START_2)
				);
			}

			@Test
			void shouldEndSecondZeitabschnittOnSecondZeitabschnittToSplitEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigBis(),
					equalTo(TO_SPLIT_ON_END_2)
				);
			}

			@ParameterizedTest()
			@ValueSource(ints = { 0, 1 })
			void allZeitabschnitteShouldHaveSameASIVInputAsBase(
				int zeitabschnittIndex
			) {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(zeitabschnittIndex)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}

			@ParameterizedTest()
			@ValueSource(ints = { 0, 1 })
			void allZeitabschnitteShouldHaveSameGemeindeInputAsBase(
				int zeitabschnittIndex
			) {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(zeitabschnittIndex)
						.getBgCalculationInputGemeinde()
						.isSame(base.getBgCalculationInputGemeinde()),
					is(true)
				);
			}
		}

		@Nested
		class ZeitabschnitteToSplitIntoAndFromBase {
			LocalDate TO_SPLIT_ON_START_1 = AUG_1;
			LocalDate TO_SPLIT_ON_END_1 = AUG_15;
			LocalDate TO_SPLIT_ON_START_2 = AUG_16;
			LocalDate TO_SPLIT_ON_END_2 = AUG_31;
			List<VerfuegungZeitabschnitt> toSplitOn =
				createZeitabschnittListToSplitOn(
					List.of(
						new DateRange(TO_SPLIT_ON_START_1, TO_SPLIT_ON_END_1),
						new DateRange(TO_SPLIT_ON_START_2, TO_SPLIT_ON_END_2)
					)
				);

			@Test
			void shouldCreateTwoZeitabschnitte() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(split.size(), is(2));
			}

			@Test
			void shouldStartFirstZeitabschnittOnBaseStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigAb(),
					equalTo(BASE_START)
				);
			}

			@Test
			void shouldEndFirstZeitabschnittOnFirstZeitabschnittToSplitOnStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigBis(),
					equalTo(TO_SPLIT_ON_END_1)
				);
			}

			@Test
			void shouldStartSecondZeitabschnittOnDayAfterFirstZeitabschnittEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigAb(),
					equalTo(TO_SPLIT_ON_END_1.plusDays(1))
				);
			}

			@Test
			void shouldEndSecondZeitabschnittOnBaseEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigBis(),
					equalTo(BASE_END)
				);
			}

			@ParameterizedTest()
			@ValueSource(ints = { 0, 1 })
			void allZeitabschnitteShouldHaveSameASIVInputAsBase(
				int zeitabschnittIndex
			) {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(zeitabschnittIndex)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}

			@ParameterizedTest()
			@ValueSource(ints = { 0, 1 })
			void allZeitabschnitteShouldHaveSameGemeindeInputAsBase(
				int zeitabschnittIndex
			) {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(zeitabschnittIndex)
						.getBgCalculationInputGemeinde()
						.isSame(base.getBgCalculationInputGemeinde()),
					is(true)
				);
			}
		}

		@Nested
		class ZeitabschnitteToSplitIntoDuringAndFromBase {
			LocalDate TO_SPLIT_ON_START_1 = AUG_1;
			LocalDate TO_SPLIT_ON_END_1 = AUG_15;
			LocalDate TO_SPLIT_ON_START_2 = AUG_16;
			LocalDate TO_SPLIT_ON_END_2 = AUG_19;
			LocalDate TO_SPLIT_ON_START_3 = AUG_20;
			LocalDate TO_SPLIT_ON_END_3 = AUG_31;
			List<VerfuegungZeitabschnitt> toSplitOn =
				createZeitabschnittListToSplitOn(
					List.of(
						new DateRange(TO_SPLIT_ON_START_1, TO_SPLIT_ON_END_1),
						new DateRange(TO_SPLIT_ON_START_2, TO_SPLIT_ON_END_2),
						new DateRange(TO_SPLIT_ON_START_3, TO_SPLIT_ON_END_3)
					)
				);

			@Test
			void shouldCreateThreeZeitabschnitte() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(split.size(), is(3));
			}

			@Test
			void shouldStartFirstZeitabschnittOnBaseStart() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigAb(),
					equalTo(BASE_START)
				);
			}

			@Test
			void shouldEndFirstZeitabschnittOnFirstZeitabschnittToSplitOnEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(0).getGueltigkeit().getGueltigBis(),
					equalTo(TO_SPLIT_ON_END_1)
				);
			}

			@Test
			void shouldStartSecondZeitabschnittOnDayAfterFirstZeitabschnittEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigAb(),
					equalTo(TO_SPLIT_ON_END_1.plusDays(1))
				);
			}

			@Test
			void shouldEndSecondZeitabschnittOnSecondZeitabschnittToSplitEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(1).getGueltigkeit().getGueltigBis(),
					equalTo(TO_SPLIT_ON_END_2)
				);
			}

			@Test
			void shouldStartThirdZeitabschnittOnDayAfterSecondZeitabschnittEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(2).getGueltigkeit().getGueltigAb(),
					equalTo(TO_SPLIT_ON_END_2.plusDays(1))
				);
			}

			@Test
			void shouldEndThirdZeitabschnittOnBaseEnd() {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(2).getGueltigkeit().getGueltigBis(),
					equalTo(BASE_END)
				);
			}

			@ParameterizedTest()
			@ValueSource(ints = { 0, 1, 2 })
			void allZeitabschnitteShouldHaveSameASIVInputAsBase(
				int zeitabschnittIndex
			) {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(zeitabschnittIndex)
						.getBgCalculationInputAsiv()
						.isSame(base.getBgCalculationInputAsiv()),
					is(true)
				);
			}

			@ParameterizedTest()
			@ValueSource(ints = { 0, 1, 2 })
			void allZeitabschnitteShouldHaveSameGemeindeInputAsBase(
				int zeitabschnittIndex
			) {
				List<VerfuegungZeitabschnitt> split =
					VerfuegungZeitabschnittSplitter.splitOn(base, toSplitOn);
				assertThat(
					split.get(zeitabschnittIndex)
						.getBgCalculationInputGemeinde()
						.isSame(base.getBgCalculationInputGemeinde()),
					is(true)
				);
			}
		}
	}

	private VerfuegungZeitabschnitt setupBase(DateRange gueltigkeit) {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt =
			new VerfuegungZeitabschnitt(gueltigkeit);

		setupInput(verfuegungZeitabschnitt.getBgCalculationInputAsiv());
		setupInput(verfuegungZeitabschnitt.getBgCalculationInputGemeinde());

		return verfuegungZeitabschnitt;
	}

	private void setupInput(BGCalculationInput bgCalculationInputAsiv) {
		bgCalculationInputAsiv.setMassgebendesEinkommenVorAbzugFamgr(
			BigDecimal.valueOf(1000)
		);
		bgCalculationInputAsiv.setAnspruchspensumProzent(80);
		bgCalculationInputAsiv.setAnspruchspensumRest(10);
		bgCalculationInputAsiv.setAnzahlHauptmahlzeiten(BigDecimal.valueOf(4));
		bgCalculationInputAsiv.setAnzahlNebenmahlzeiten(BigDecimal.valueOf(4));
		bgCalculationInputAsiv.setTarifHauptmahlzeit(BigDecimal.valueOf(4));
		bgCalculationInputAsiv.setTarifNebenmahlzeit(BigDecimal.valueOf(4));
		bgCalculationInputAsiv.setAbzugFamGroesseTotal(BigDecimal.valueOf(4));
	}

	private List<VerfuegungZeitabschnitt> createZeitabschnittListToSplitOn(
		List<DateRange> gueltigkeiten
	) {
		return gueltigkeiten.stream()
			.map(VerfuegungZeitabschnitt::new)
			.collect(Collectors.toList());
	}

}
