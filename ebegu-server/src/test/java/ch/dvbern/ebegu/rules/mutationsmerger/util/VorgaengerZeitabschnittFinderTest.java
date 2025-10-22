package ch.dvbern.ebegu.rules.mutationsmerger.util;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class VorgaengerZeitabschnittFinderTest {

	private static final LocalDate AUG_1 = LocalDate.of(2024, 8, 1);
	private static final LocalDate AUG_9 = LocalDate.of(2024, 8, 9);
	private static final LocalDate AUG_10 = LocalDate.of(2024, 8, 10);
	private static final LocalDate AUG_15 = LocalDate.of(2024, 8, 15);
	private static final LocalDate AUG_16 = LocalDate.of(2024, 8, 16);
	private static final LocalDate AUG_18 = LocalDate.of(2024, 8, 18);
	private static final LocalDate AUG_19 = LocalDate.of(2024, 8, 19);
	private static final LocalDate AUG_20 = LocalDate.of(2024, 8, 20);
	private static final LocalDate AUG_22 = LocalDate.of(2024, 8, 22);
	private static final LocalDate AUG_31 = LocalDate.of(2024, 8, 31);

	@Nested
	class CurrentZeitabschnittSpansEntireMonthTest {
		VerfuegungZeitabschnitt current = new VerfuegungZeitabschnitt(
			new DateRange(AUG_1, AUG_31)
		);

		@Test
		void shouldFindNoZeitabschnittIfNoVorgaengerVerfuegung() {
			List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					current,
					null
				);
			assertThat(vorgaengerZeitabschnitte.isEmpty(), is(true));
		}

		@Test
		void shouldFindOneZeitabschnittIfVorgaengerHasEntireMonthZeitabschnitt() {
			Verfuegung verfuegung = setupEmptyVerfuegung(
				List.of(new DateRange(AUG_1, AUG_31))
			);
			List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					current,
					verfuegung
				);
			assertThat(vorgaengerZeitabschnitte.size(), is(1));
		}

		@Test
		void shouldFindZeitabschnittOfEntireMonthIfVorgaengerHasEntireMonthZeitabschnitt() {
			Verfuegung verfuegung = setupEmptyVerfuegung(
				List.of(new DateRange(AUG_1, AUG_31))
			);
			List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					current,
					verfuegung
				);
			assertThat(
				vorgaengerZeitabschnitte.get(0).getGueltigkeit().getGueltigAb(),
				is(AUG_1)
			);
			assertThat(
				vorgaengerZeitabschnitte.get(0)
					.getGueltigkeit()
					.getGueltigBis(),
				is(AUG_31)
			);
		}

		@Test
		void shouldFindOneZeitabschnittIfVorgaengerHasOnePartMonthZeitabschnitt() {
			Verfuegung verfuegung = setupEmptyVerfuegung(
				List.of(new DateRange(AUG_15, AUG_31))
			);
			List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					current,
					verfuegung
				);
			assertThat(vorgaengerZeitabschnitte.size(), is(1));
		}

		@Test
		void shouldFindOneZeitabschnittOfPartMonthIfVorgaengerHasOnePartMonthZeitabschnitt() {
			Verfuegung verfuegung = setupEmptyVerfuegung(
				List.of(new DateRange(AUG_15, AUG_31))
			);
			List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					current,
					verfuegung
				);
			assertThat(
				vorgaengerZeitabschnitte.get(0).getGueltigkeit().getGueltigAb(),
				is(AUG_15)
			);
			assertThat(
				vorgaengerZeitabschnitte.get(0)
					.getGueltigkeit()
					.getGueltigBis(),
				is(AUG_31)
			);
		}

		@Test
		void shouldFindTwoZeitabschnittIfVorgaengerHasMultiplePartMonthZeitabschnitt() {
			Verfuegung verfuegung = setupEmptyVerfuegung(
				List.of(
					new DateRange(AUG_1, AUG_15),
					new DateRange(AUG_16, AUG_31)
				)
			);
			List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					current,
					verfuegung
				);
			assertThat(vorgaengerZeitabschnitte.size(), is(2));
		}

		@Test
		void shouldFindTwoZeitabschnittOfPartMonthIfVorgaengerHasMultiplePartMonthZeitabschnitt() {
			Verfuegung verfuegung = setupEmptyVerfuegung(
				List.of(
					new DateRange(AUG_1, AUG_15),
					new DateRange(AUG_16, AUG_31)
				)
			);
			List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					current,
					verfuegung
				);
			assertThat(
				vorgaengerZeitabschnitte.get(0).getGueltigkeit().getGueltigAb(),
				is(AUG_1)
			);
			assertThat(
				vorgaengerZeitabschnitte.get(0)
					.getGueltigkeit()
					.getGueltigBis(),
				is(AUG_15)
			);
			assertThat(
				vorgaengerZeitabschnitte.get(1).getGueltigkeit().getGueltigAb(),
				is(AUG_16)
			);
			assertThat(
				vorgaengerZeitabschnitte.get(1)
					.getGueltigkeit()
					.getGueltigBis(),
				is(AUG_31)
			);
		}

	}

	@Nested
	class CurrentZeitabschnittSpansPartMonthTest {
		VerfuegungZeitabschnitt current = new VerfuegungZeitabschnitt(
			new DateRange(AUG_10, AUG_20)
		);

		@Test
		void shouldFindNoZeitabschnittIfNoVorgaengerVerfuegung() {
			List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					current,
					null
				);
			assertThat(vorgaengerZeitabschnitte.isEmpty(), is(true));
		}

		@Test
		void shouldFindOneZeitabschnittIfVorgaengerHasEntireMonthZeitabschnitt() {
			Verfuegung verfuegung = setupEmptyVerfuegung(
				List.of(new DateRange(AUG_1, AUG_31))
			);
			List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					current,
					verfuegung
				);
			assertThat(vorgaengerZeitabschnitte.size(), is(1));
		}

		@Test
		void shouldFindOneZeitabschnittIfVorgaengerHasOnePartMonthZeitabschnittStartingDuring() {
			Verfuegung verfuegung = setupEmptyVerfuegung(
				List.of(new DateRange(AUG_15, AUG_31))
			);
			List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					current,
					verfuegung
				);
			assertThat(vorgaengerZeitabschnitte.size(), is(1));
		}

		@Test
		void shouldFindOneZeitabschnittIfVorgaengerHasOnePartMonthZeitabschnittEndingBeforeAndOneStartingDuring() {
			Verfuegung verfuegung = setupEmptyVerfuegung(
				List.of(
					new DateRange(AUG_1, AUG_9),
					new DateRange(AUG_15, AUG_31)
				)
			);
			List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					current,
					verfuegung
				);
			assertThat(vorgaengerZeitabschnitte.size(), is(1));
		}

		@Test
		void shouldFindOneZeitabschnittIfVorgaengerHasOnePartMonthZeitabschnittEndingDuringAndOneStartingAfter() {
			Verfuegung verfuegung = setupEmptyVerfuegung(
				List.of(
					new DateRange(AUG_1, AUG_16),
					new DateRange(AUG_22, AUG_31)
				)
			);
			List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					current,
					verfuegung
				);
			assertThat(vorgaengerZeitabschnitte.size(), is(1));
		}

		@Test
		void shouldFindTwoZeitabschnittIfVorgaengerHasOnePartMonthZeitabschnittEndingDuringAndOneStartingDuring() {
			Verfuegung verfuegung = setupEmptyVerfuegung(
				List.of(
					new DateRange(AUG_1, AUG_15),
					new DateRange(AUG_16, AUG_31)
				)
			);
			List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					current,
					verfuegung
				);
			assertThat(vorgaengerZeitabschnitte.size(), is(2));
		}

		@Test
		void shouldFindThreeZeitabschnittIfVorgaengerHasOnePartMonthZeitabschnittEndingDuringOneInsideAndOneStartingDuring() {
			Verfuegung verfuegung = setupEmptyVerfuegung(
				List.of(
					new DateRange(AUG_1, AUG_15),
					new DateRange(AUG_16, AUG_18),
					new DateRange(AUG_19, AUG_31)
				)
			);
			List<VerfuegungZeitabschnitt> vorgaengerZeitabschnitte =
				VorgaengerZeitabschnittFinder.findZeitabschnitteInVorgaenger(
					current,
					verfuegung
				);
			assertThat(vorgaengerZeitabschnitte.size(), is(3));
		}

	}

	private Verfuegung setupEmptyVerfuegung(
		List<DateRange> zeitabschnittGueltigkeiten
	) {
		Verfuegung verfuegung = new Verfuegung();
		verfuegung.setZeitabschnitte(
			zeitabschnittGueltigkeiten.stream()
				.map(VerfuegungZeitabschnitt::new)
				.collect(Collectors.toList())
		);

		return verfuegung;
	}

}
