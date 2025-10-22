package ch.dvbern.ebegu.validators;

import java.time.LocalDate;

import ch.dvbern.ebegu.entities.Kind;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class CheckKindGueltigkeitTerminierungValidatorTest {

	CheckKindGueltigkeitTerminierungValidator validator =
		new CheckKindGueltigkeitTerminierungValidator();

	@Test
	void withGueltigkeitTerminiertOhneDatum_should_NotBeValid() {
		Kind kind = new Kind();
		kind.setGueltigkeitTerminiert(true);
		kind.setGueltigkeitTerminiertPer(null);
		assertThat(validator.isValid(kind, null), is(false));
	}

	@Test
	void withoutGueltigkeitTerminiertWithDatum_should_NotBeValid() {
		Kind kind = new Kind();
		kind.setGueltigkeitTerminiert(false);
		kind.setGueltigkeitTerminiertPer(LocalDate.now());
		assertThat(validator.isValid(kind, null), is(false));
	}

	@Test
	void withoutGueltigkeitTerminiertOhneDatum_should_BeValid() {
		Kind kind = new Kind();
		kind.setGueltigkeitTerminiert(false);
		kind.setGueltigkeitTerminiertPer(null);
		assertThat(validator.isValid(kind, null), is(true));
	}

	@Test
	void withGueltigkeitTerminiertWithDatum_should_beValid() {
		Kind kind = new Kind();
		kind.setGueltigkeitTerminiert(true);
		kind.setGueltigkeitTerminiertPer(LocalDate.now());
		assertThat(validator.isValid(kind, null), is(true));
	}
}
