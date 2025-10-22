package ch.dvbern.ebegu.entities.gemeindeantrag;

import java.math.BigDecimal;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;

class FerienbetreuungAngabenNutzungTest {

	@Nested
	class GetAnzahlTageSonderschueler {
		@Test
		void shouldBeZeroIfBothAreNull() {
			var testee = new FerienbetreuungAngabenNutzung();
			testee.setBetreuungstageKinderDieserGemeindeSonderschueler(null);
			testee.setDavonBetreuungstageKinderAndererGemeindenSonderschueler(
				null
			);

			assertThat(
				testee.getAnzahlTageSonderschueler(),
				comparesEqualTo(BigDecimal.ZERO)
			);
		}

		@Test
		void shouldTreatNullValueOfDieserGemeindeAsZero() {
			var testee = new FerienbetreuungAngabenNutzung();
			testee.setBetreuungstageKinderDieserGemeindeSonderschueler(null);
			testee.setDavonBetreuungstageKinderAndererGemeindenSonderschueler(
				BigDecimal.ONE
			);

			assertThat(
				testee.getAnzahlTageSonderschueler(),
				comparesEqualTo(BigDecimal.ONE)
			);
		}

		@Test
		void shouldTreatNullValueOfAndererGemeindeAsZero() {
			var testee = new FerienbetreuungAngabenNutzung();
			testee.setBetreuungstageKinderDieserGemeindeSonderschueler(
				BigDecimal.ONE
			);
			testee.setDavonBetreuungstageKinderAndererGemeindenSonderschueler(
				null
			);

			assertThat(
				testee.getAnzahlTageSonderschueler(),
				comparesEqualTo(BigDecimal.ONE)
			);
		}

		@Test
		void shouldAddValuesOfDieserGemeindeAndAndererGemeinde() {
			var testee = new FerienbetreuungAngabenNutzung();
			testee.setBetreuungstageKinderDieserGemeindeSonderschueler(
				BigDecimal.ONE
			);
			testee.setDavonBetreuungstageKinderAndererGemeindenSonderschueler(
				BigDecimal.ONE
			);

			assertThat(
				testee.getAnzahlTageSonderschueler(),
				comparesEqualTo(BigDecimal.valueOf(2))
			);
		}

	}

	@Nested
	class GetAnzahlTageOhneSonderschueler {

		@Test
		void shouldBeZeroIfAllAreNull() {
			var testee = new FerienbetreuungAngabenNutzung();
			testee.setBetreuungstageKinderDieserGemeinde(null);
			testee.setDavonBetreuungstageKinderAndererGemeinden(null);

			assertThat(
				testee.getAnzahlTageOhneSonderschueler(),
				comparesEqualTo(BigDecimal.ZERO)
			);
		}

		@Test
		void shouldTreatKinderDieserGemeindeAsZeroIfNull() {
			var testee = new FerienbetreuungAngabenNutzung();
			testee.setBetreuungstageKinderDieserGemeinde(null);
			testee.setDavonBetreuungstageKinderAndererGemeinden(
				BigDecimal.ZERO
			);

			assertThat(
				testee.getAnzahlTageOhneSonderschueler(),
				comparesEqualTo(BigDecimal.ZERO)
			);
		}

		@Test
		void shouldTreatKinderAndererGemeindeAsZeroIfNull() {
			var testee = new FerienbetreuungAngabenNutzung();
			testee.setBetreuungstageKinderDieserGemeinde(BigDecimal.ZERO);
			testee.setDavonBetreuungstageKinderAndererGemeinden(null);

			assertThat(
				testee.getAnzahlTageOhneSonderschueler(),
				comparesEqualTo(BigDecimal.ZERO)
			);
		}

		@Test
		void shouldSumKinderDieserAndAnderGemeinde() {
			var testee = new FerienbetreuungAngabenNutzung();
			testee.setBetreuungstageKinderDieserGemeinde(BigDecimal.ONE);
			testee.setDavonBetreuungstageKinderAndererGemeinden(BigDecimal.ONE);

			assertThat(
				testee.getAnzahlTageOhneSonderschueler(),
				comparesEqualTo(BigDecimal.valueOf(2))
			);
		}

		@Test
		void shouldSubtractSonderschuelerFromSum() {
			var testee = new FerienbetreuungAngabenNutzung();

			testee.setBetreuungstageKinderDieserGemeinde(BigDecimal.ONE);
			testee.setDavonBetreuungstageKinderAndererGemeinden(BigDecimal.ONE);

			testee.setBetreuungstageKinderDieserGemeindeSonderschueler(
				BigDecimal.ZERO
			);
			testee.setDavonBetreuungstageKinderAndererGemeindenSonderschueler(
				BigDecimal.ONE
			);

			assertThat(
				testee.getAnzahlTageOhneSonderschueler(),
				comparesEqualTo(BigDecimal.ONE)
			);
		}

	}

}
