package ch.dvbern.ebegu.finanziellesituation.validation;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FinanzielleSituationValidatorSZTest {

	FinanzielleSituationValidatorSZ validator =
		new FinanzielleSituationValidatorSZ();

	@Nested
	class FinSitTest {

		@Test
		void allNullShouldNotBeValid() {
			assertThat(
				validator.isFinanzielleSituationComplete(
					new FinanzielleSituation(),
					new Gesuch()
				),
				is(false)
			);
		}

		@Test
		void bruttoLohnAnsweredShouldBeValid() {
			final FinanzielleSituation finanzielleSituation =
				new FinanzielleSituation();
			finanzielleSituation.setBruttoLohn(BigDecimal.ONE);
			assertThat(
				validator.isFinanzielleSituationComplete(
					finanzielleSituation,
					new Gesuch()
				),
				is(true)
			);
		}

		@Test
		void bruttoLohnSteuerbaresEinkommenNotAnsweredShouldNotBeValid() {
			final FinanzielleSituation finanzielleSituation =
				new FinanzielleSituation();
			finanzielleSituation.setSteuerbaresEinkommen(null);
			finanzielleSituation.setEinkaeufeVorsorge(BigDecimal.ONE);
			finanzielleSituation.setAbzuegeLiegenschaft(BigDecimal.ONE);
			finanzielleSituation.setSteuerbaresVermoegen(BigDecimal.ONE);
			assertThat(
				validator.isFinanzielleSituationComplete(
					finanzielleSituation,
					new Gesuch()
				),
				is(false)
			);
		}

		@Test
		void bruttoLohnEinkaeufeVorsorgeNotAnsweredShouldNotBeValid() {
			final FinanzielleSituation finanzielleSituation =
				new FinanzielleSituation();
			finanzielleSituation.setSteuerbaresEinkommen(BigDecimal.ONE);
			finanzielleSituation.setEinkaeufeVorsorge(null);
			finanzielleSituation.setAbzuegeLiegenschaft(BigDecimal.ONE);
			finanzielleSituation.setSteuerbaresVermoegen(BigDecimal.ONE);
			assertThat(
				validator.isFinanzielleSituationComplete(
					finanzielleSituation,
					new Gesuch()
				),
				is(false)
			);
		}

		@Test
		void bruttoLohnAbzuegeLiegenschaftNotAnsweredShouldNotBeValid() {
			final FinanzielleSituation finanzielleSituation =
				new FinanzielleSituation();
			finanzielleSituation.setSteuerbaresEinkommen(BigDecimal.ONE);
			finanzielleSituation.setEinkaeufeVorsorge(BigDecimal.ONE);
			finanzielleSituation.setAbzuegeLiegenschaft(null);
			finanzielleSituation.setSteuerbaresVermoegen(BigDecimal.ONE);
			assertThat(
				validator.isFinanzielleSituationComplete(
					finanzielleSituation,
					new Gesuch()
				),
				is(false)
			);
		}

		@Test
		void bruttoLohnSteuerbaresVermoegenNotAnsweredShouldNotBeValid() {
			final FinanzielleSituation finanzielleSituation =
				new FinanzielleSituation();
			finanzielleSituation.setSteuerbaresEinkommen(BigDecimal.ONE);
			finanzielleSituation.setEinkaeufeVorsorge(BigDecimal.ONE);
			finanzielleSituation.setAbzuegeLiegenschaft(BigDecimal.ONE);
			finanzielleSituation.setSteuerbaresVermoegen(null);
			assertThat(
				validator.isFinanzielleSituationComplete(
					finanzielleSituation,
					new Gesuch()
				),
				is(false)
			);
		}

		@Test
		void notQuellenbesteuertAllAnsweredShouldBeValid() {
			final FinanzielleSituation finanzielleSituation =
				new FinanzielleSituation();
			finanzielleSituation.setSteuerbaresEinkommen(BigDecimal.ONE);
			finanzielleSituation.setEinkaeufeVorsorge(BigDecimal.ONE);
			finanzielleSituation.setAbzuegeLiegenschaft(BigDecimal.ONE);
			finanzielleSituation.setSteuerbaresVermoegen(BigDecimal.ONE);
			assertThat(
				validator.isFinanzielleSituationComplete(
					finanzielleSituation,
					new Gesuch()
				),
				is(true)
			);
		}
	}

	@Nested
	class EKVTest {

		@Test
		void allNullShouldNotBeValid() {
			assertThat(
				validator.isEinkommensverschlechterungComplete(
					new Einkommensverschlechterung(),
					new Gesuch()
				),
				is(false)
			);
		}

		@Test
		void bruttoLohnAnsweredShouldBeValid() {
			final Einkommensverschlechterung einkommensverschlechterung =
				new Einkommensverschlechterung();
			einkommensverschlechterung.setBruttoLohn(BigDecimal.ONE);
			assertThat(
				validator.isEinkommensverschlechterungComplete(
					einkommensverschlechterung,
					new Gesuch()
				),
				is(true)
			);
		}

		@Test
		void bruttoLohnSteuerbaresEinkommenNotAnsweredShouldNotBeValid() {
			final Einkommensverschlechterung einkommensverschlechterung =
				new Einkommensverschlechterung();
			einkommensverschlechterung.setSteuerbaresEinkommen(null);
			einkommensverschlechterung.setEinkaeufeVorsorge(BigDecimal.ONE);
			einkommensverschlechterung.setAbzuegeLiegenschaft(BigDecimal.ONE);
			einkommensverschlechterung.setSteuerbaresVermoegen(BigDecimal.ONE);
			assertThat(
				validator.isEinkommensverschlechterungComplete(
					einkommensverschlechterung,
					new Gesuch()
				),
				is(false)
			);
		}

		@Test
		void bruttoLohnEinkaeufeVorsorgeNotAnsweredShouldNotBeValid() {
			final Einkommensverschlechterung einkommensverschlechterung =
				new Einkommensverschlechterung();
			einkommensverschlechterung.setSteuerbaresEinkommen(BigDecimal.ONE);
			einkommensverschlechterung.setEinkaeufeVorsorge(null);
			einkommensverschlechterung.setAbzuegeLiegenschaft(BigDecimal.ONE);
			einkommensverschlechterung.setSteuerbaresVermoegen(BigDecimal.ONE);
			assertThat(
				validator.isEinkommensverschlechterungComplete(
					einkommensverschlechterung,
					new Gesuch()
				),
				is(false)
			);
		}

		@Test
		void bruttoLohnAbzuegeLiegenschaftNotAnsweredShouldNotBeValid() {
			final Einkommensverschlechterung einkommensverschlechterung =
				new Einkommensverschlechterung();
			einkommensverschlechterung.setSteuerbaresEinkommen(BigDecimal.ONE);
			einkommensverschlechterung.setEinkaeufeVorsorge(BigDecimal.ONE);
			einkommensverschlechterung.setAbzuegeLiegenschaft(null);
			einkommensverschlechterung.setSteuerbaresVermoegen(BigDecimal.ONE);
			assertThat(
				validator.isEinkommensverschlechterungComplete(
					einkommensverschlechterung,
					new Gesuch()
				),
				is(false)
			);
		}

		@Test
		void bruttoLohnSteuerbaresVermoegenNotAnsweredShouldNotBeValid() {
			final Einkommensverschlechterung einkommensverschlechterung =
				new Einkommensverschlechterung();
			einkommensverschlechterung.setSteuerbaresEinkommen(BigDecimal.ONE);
			einkommensverschlechterung.setEinkaeufeVorsorge(BigDecimal.ONE);
			einkommensverschlechterung.setAbzuegeLiegenschaft(BigDecimal.ONE);
			einkommensverschlechterung.setSteuerbaresVermoegen(null);
			assertThat(
				validator.isEinkommensverschlechterungComplete(
					einkommensverschlechterung,
					new Gesuch()
				),
				is(false)
			);
		}

		@Test
		void notQuellenbesteuertAllAnsweredShouldBeValid() {
			final Einkommensverschlechterung einkommensverschlechterung =
				new Einkommensverschlechterung();
			einkommensverschlechterung.setSteuerbaresEinkommen(BigDecimal.ONE);
			einkommensverschlechterung.setEinkaeufeVorsorge(BigDecimal.ONE);
			einkommensverschlechterung.setAbzuegeLiegenschaft(BigDecimal.ONE);
			einkommensverschlechterung.setSteuerbaresVermoegen(BigDecimal.ONE);
			assertThat(
				validator.isEinkommensverschlechterungComplete(
					einkommensverschlechterung,
					new Gesuch()
				),
				is(true)
			);
		}
	}

}
