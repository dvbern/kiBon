package ch.dvbern.ebegu.api.resource.util.gemeindeantrag;

import java.time.LocalDate;

import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GemeindeAntragUtilTest {

	@Nested
	class FirstEinreichedatumSorting {

		@Nested
		class BothNotOfWithEinreichedatum {
			@Test
			void shouldReturn0WhenNoneAreInstanceOfWithEinreichedatum() {
				GemeindeKennzahlen a = new GemeindeKennzahlen();
				GemeindeKennzahlen b = new GemeindeKennzahlen();

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, 1),
					is(0)
				);
			}

			@Test
			void shouldReturn0WhenNoneAreInstanceOfWithEinreichedatumAndReversed() {
				GemeindeKennzahlen a = new GemeindeKennzahlen();
				GemeindeKennzahlen b = new GemeindeKennzahlen();

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, -1),
					is(0)
				);
			}
		}

		@Nested
		class AOfWithEinreichedatum {

			@Test
			void shouldReturnMinus1WhenAIsInstanceOfWithEinreichedatumAndBNot() {
				FerienbetreuungAngabenContainer a =
					new FerienbetreuungAngabenContainer();
				a.setEinreichedatum(LocalDate.now());
				GemeindeKennzahlen b = new GemeindeKennzahlen();

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, 1),
					is(-1)
				);
			}

			@Test
			void shouldReturnMinus1WhenAIsInstanceOfWithEinreichedatumAndBNotReversed() {
				FerienbetreuungAngabenContainer a =
					new FerienbetreuungAngabenContainer();
				a.setEinreichedatum(LocalDate.now());
				GemeindeKennzahlen b = new GemeindeKennzahlen();

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, -1),
					is(-1)
				);
			}
		}

		@Nested
		class BOfWithEinreichedatum {

			@Test
			void shouldReturn1WhenBIsInstanceOfWithEinreichedatumAndANot() {
				GemeindeKennzahlen a = new GemeindeKennzahlen();
				FerienbetreuungAngabenContainer b =
					new FerienbetreuungAngabenContainer();
				b.setEinreichedatum(LocalDate.now());

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, 1),
					is(1)
				);
			}

			@Test
			void shouldReturn1WhenBIsInstanceOfWithEinreichedatumAndANotReversed() {
				GemeindeKennzahlen a = new GemeindeKennzahlen();
				FerienbetreuungAngabenContainer b =
					new FerienbetreuungAngabenContainer();
				b.setEinreichedatum(LocalDate.now());

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, -1),
					is(1)
				);
			}
		}

		@Nested
		class BothOfWithEinreichedatum {

			final LocalDate EARLIER_EINREICHEDATUM = LocalDate.of(2024, 9, 1);
			final LocalDate LATER_EINREICHEDATUM = LocalDate.of(2024, 10, 1);

			@Test
			void shouldReturn0WhenEinreichedatumIsNullForBoth() {
				FerienbetreuungAngabenContainer a =
					new FerienbetreuungAngabenContainer();
				FerienbetreuungAngabenContainer b =
					new FerienbetreuungAngabenContainer();

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, 1),
					is(0)
				);
			}

			@Test
			void shouldReturn0WhenEinreichedatumIsNullForBothReversed() {
				FerienbetreuungAngabenContainer a =
					new FerienbetreuungAngabenContainer();
				FerienbetreuungAngabenContainer b =
					new FerienbetreuungAngabenContainer();

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, -1),
					is(0)
				);
			}

			@Test
			void shouldReturnMinus1WhenEinreichedatumAIsNotNullAndEinreichedatumBIsNull() {
				FerienbetreuungAngabenContainer a =
					new FerienbetreuungAngabenContainer();
				a.setEinreichedatum(EARLIER_EINREICHEDATUM);
				FerienbetreuungAngabenContainer b =
					new FerienbetreuungAngabenContainer();
				b.setEinreichedatum(null);

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, 1),
					is(-1)
				);
			}

			@Test
			void shouldReturnMinus1WhenEinreichedatumAIsNotNullAndEinreichedatumBIsNullReversed() {
				FerienbetreuungAngabenContainer a =
					new FerienbetreuungAngabenContainer();
				a.setEinreichedatum(EARLIER_EINREICHEDATUM);
				FerienbetreuungAngabenContainer b =
					new FerienbetreuungAngabenContainer();
				b.setEinreichedatum(null);

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, -1),
					is(-1)
				);
			}

			@Test
			void shouldReturn1WhenEinreichedatumAIsNullAndEinreichedatumBIsNotNull() {
				FerienbetreuungAngabenContainer a =
					new FerienbetreuungAngabenContainer();
				a.setEinreichedatum(null);
				FerienbetreuungAngabenContainer b =
					new FerienbetreuungAngabenContainer();
				b.setEinreichedatum(EARLIER_EINREICHEDATUM);

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, 1),
					is(1)
				);
			}

			@Test
			void shouldReturn1WhenEinreichedatumAIsNullAndEinreichedatumBIsNotNullReversed() {
				FerienbetreuungAngabenContainer a =
					new FerienbetreuungAngabenContainer();
				a.setEinreichedatum(null);
				FerienbetreuungAngabenContainer b =
					new FerienbetreuungAngabenContainer();
				b.setEinreichedatum(EARLIER_EINREICHEDATUM);

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, -1),
					is(1)
				);
			}

			@Test
			void shouldReturnMinus1WhenEinreichedatumAIsEarlierThanEinreichedatumB() {
				FerienbetreuungAngabenContainer a =
					new FerienbetreuungAngabenContainer();
				a.setEinreichedatum(EARLIER_EINREICHEDATUM);
				FerienbetreuungAngabenContainer b =
					new FerienbetreuungAngabenContainer();
				b.setEinreichedatum(LATER_EINREICHEDATUM);

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, 1),
					is(-1)
				);
			}

			@Test
			void shouldReturn1WhenEinreichedatumAIsEarlierThanEinreichedatumBReversed() {
				FerienbetreuungAngabenContainer a =
					new FerienbetreuungAngabenContainer();
				a.setEinreichedatum(EARLIER_EINREICHEDATUM);
				FerienbetreuungAngabenContainer b =
					new FerienbetreuungAngabenContainer();
				b.setEinreichedatum(LATER_EINREICHEDATUM);

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, -1),
					is(1)
				);
			}

			@Test
			void shouldReturn0WhenEinreichedatumAIsSameAsEinreichedatumB() {
				FerienbetreuungAngabenContainer a =
					new FerienbetreuungAngabenContainer();
				a.setEinreichedatum(EARLIER_EINREICHEDATUM);
				FerienbetreuungAngabenContainer b =
					new FerienbetreuungAngabenContainer();
				b.setEinreichedatum(EARLIER_EINREICHEDATUM);

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, 1),
					is(0)
				);
			}

			@Test
			void shouldReturn0WhenEinreichedatumAIsSameAsEinreichedatumBReversed() {
				FerienbetreuungAngabenContainer a =
					new FerienbetreuungAngabenContainer();
				a.setEinreichedatum(EARLIER_EINREICHEDATUM);
				FerienbetreuungAngabenContainer b =
					new FerienbetreuungAngabenContainer();
				b.setEinreichedatum(EARLIER_EINREICHEDATUM);

				assertThat(
					GemeindeAntragUtil.compareEinreichedatum(a, b, -1),
					is(0)
				);
			}
		}
	}
}
