package ch.dvbern.ebegu.validators.gemeindeantraege.ferienbetreuung;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.gemeindeantrag.ferienbetreuung.FerienbetreuungAngabenNutzung;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class NutzungPlausibilisierungValidatorTest {

	NutzungPlausibilisierungValidator validator =
		new NutzungPlausibilisierungValidator();

	@Nested
	class NullValues {

		@Test
		void shouldHandleAsZeroIfTotalIsNull() {
			FerienbetreuungAngabenNutzung nutzung = getNutzungWith(
				null,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO
			);
			assertThat(validator.isValid(nutzung, null), is(true));
		}

		@Test
		void shouldHandleAsZeroIfSonderschuelerIsNull() {
			FerienbetreuungAngabenNutzung nutzung = getNutzungWith(
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO
			);
			assertThat(validator.isValid(nutzung, null), is(true));
		}

		@Test
		void shouldHandleAsZeroIfZyklus1IsNull() {
			FerienbetreuungAngabenNutzung nutzung = getNutzungWith(
				BigDecimal.ZERO,
				null,
				BigDecimal.ZERO,
				BigDecimal.ZERO
			);
			assertThat(validator.isValid(nutzung, null), is(true));
		}

		@Test
		void shouldHandleAsZeroIfZyklus2IsNull() {
			FerienbetreuungAngabenNutzung nutzung = getNutzungWith(
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				null,
				BigDecimal.ZERO
			);
			assertThat(validator.isValid(nutzung, null), is(true));
		}

		@Test
		void shouldHandleAsZeroIfZyklus3IsNull() {
			FerienbetreuungAngabenNutzung nutzung = getNutzungWith(
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				null
			);
			assertThat(validator.isValid(nutzung, null), is(true));
		}
	}

	@Test
	void shouldBeTrueIfAllValuesAreZero() {
		FerienbetreuungAngabenNutzung nutzung = getNutzungWith(
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			BigDecimal.ZERO
		);
		assertThat(validator.isValid(nutzung, null), is(true));
	}

	@Test
	void shouldBeTrueIfTotalEqualsSum() {
		FerienbetreuungAngabenNutzung nutzung = getNutzungWith(
			BigDecimal.valueOf(3),
			BigDecimal.ONE,
			BigDecimal.ONE,
			BigDecimal.ONE
		);
		assertThat(validator.isValid(nutzung, null), is(true));
	}

	@Test
	void shouldBeFalseIfTotalDoesNotEqualsSum() {
		FerienbetreuungAngabenNutzung nutzung = getNutzungWith(
			BigDecimal.ONE,
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			BigDecimal.ZERO
		);
		assertThat(validator.isValid(nutzung, null), is(false));
	}

	private FerienbetreuungAngabenNutzung getNutzungWith(
		BigDecimal total,
		BigDecimal ersterZyklus,
		BigDecimal zweiterZyklus,
		BigDecimal dritterZyklus
	) {
		FerienbetreuungAngabenNutzung nutzung =
			new FerienbetreuungAngabenNutzung();

		nutzung.setAnzahlBetreuteKinder(total);
		nutzung.setAnzahlBetreuteKinder1Zyklus(ersterZyklus);
		nutzung.setAnzahlBetreuteKinder2Zyklus(zweiterZyklus);
		nutzung.setAnzahlBetreuteKinder3Zyklus(dritterZyklus);

		return nutzung;
	}
}
