package ch.dvbern.ebegu.validators.gemeindeantraege.lats;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.gemeindeantrag.lastenausgleichtagesschulen.LastenausgleichTagesschuleAngabenInstitution;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AngabenTagesschulePlausibilisierungValidatorTest {

	AngabenTagesschulePlausibilisierungValidator validator =
		new AngabenTagesschulePlausibilisierungValidator();

	@Nested
	class NullValues {

		@Test
		void shouldBeFalseIfTotalIsNull() {
			var angabenInstitution = createWithValues(
				null,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO
			);
			assertThat(validator.isValid(angabenInstitution, null), is(false));
		}

		@Test
		void shouldBeFalseIfKindergartenIsNull() {
			var angabenInstitution = createWithValues(
				BigDecimal.ZERO,
				null,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO
			);
			assertThat(validator.isValid(angabenInstitution, null), is(false));
		}

		@Test
		void shouldBeFalseIfPrimarstufeIsNull() {
			var angabenInstitution = createWithValues(
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				null,
				BigDecimal.ZERO,
				BigDecimal.ZERO
			);
			assertThat(validator.isValid(angabenInstitution, null), is(false));
		}

		@Test
		void shouldBeFalseIfSekundarIsNull() {
			var angabenInstitution = createWithValues(
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				null,
				BigDecimal.ZERO
			);
			assertThat(validator.isValid(angabenInstitution, null), is(false));
		}

		@Test
		void shouldBeFalseIfBasisIsNull() {
			var angabenInstitution = createWithValues(
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				BigDecimal.ZERO,
				null
			);
			assertThat(validator.isValid(angabenInstitution, null), is(false));
		}
	}

	@Test
	void shouldBeTrueIfAllValuesAreZero() {
		var angabenInstitution = createWithValues(
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			BigDecimal.ZERO,
			BigDecimal.ZERO
		);
		assertThat(validator.isValid(angabenInstitution, null), is(true));
	}

	@Test
	void shouldBeTrueIfValuesSumUpToTotal() {
		var angabenInstitution = createWithValues(
			BigDecimal.valueOf(4),
			BigDecimal.ONE,
			BigDecimal.ONE,
			BigDecimal.ONE,
			BigDecimal.ONE
		);
		assertThat(validator.isValid(angabenInstitution, null), is(true));

	}

	@Test
	void shouldBeFalseIfValuesDoesNotSumUpToTotal() {
		var angabenInstitution = createWithValues(
			BigDecimal.ZERO,
			BigDecimal.ONE,
			BigDecimal.ONE,
			BigDecimal.ONE,
			BigDecimal.ONE
		);
		assertThat(validator.isValid(angabenInstitution, null), is(false));
	}

	private LastenausgleichTagesschuleAngabenInstitution createWithValues(
		BigDecimal total,
		BigDecimal kindergarten,
		BigDecimal primar,
		BigDecimal sekundar,
		BigDecimal basis
	) {
		LastenausgleichTagesschuleAngabenInstitution jaxAngabenInstitution =
			new LastenausgleichTagesschuleAngabenInstitution();

		jaxAngabenInstitution.setAnzahlEingeschriebeneKinder(total);
		jaxAngabenInstitution.setAnzahlEingeschriebeneKinderKindergarten(
			kindergarten
		);
		jaxAngabenInstitution.setAnzahlEingeschriebeneKinderPrimarstufe(primar);
		jaxAngabenInstitution.setAnzahlEingeschriebeneKinderSekundarstufe(
			sekundar
		);
		jaxAngabenInstitution.setAnzahlEingeschriebeneKinderBasisstufe(basis);

		return jaxAngabenInstitution;
	}

}
