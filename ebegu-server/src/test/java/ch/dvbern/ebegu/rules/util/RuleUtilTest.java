package ch.dvbern.ebegu.rules.util;

import java.time.LocalDate;
import java.time.Month;

import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.util.RuleUtil;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RuleUtilTest {

	private final static LocalDate OCTOBER_15 = LocalDate.of(
		2024,
		Month.OCTOBER,
		15
	);
	private final static LocalDate OCTOBER_31 = LocalDate.of(
		2024,
		Month.OCTOBER,
		31
	);
	private final static LocalDate NOVEMBER_1 = LocalDate.of(
		2024,
		Month.NOVEMBER,
		1
	);
	private final static LocalDate DEZEMBER_1 = LocalDate.of(
		2024,
		Month.DECEMBER,
		1
	);

	private static final Kind k = new Kind();

	@Test
	public void kindNichtTerminiert() {
		k.setGueltigkeitTerminiert(false)
			.setGueltigkeitTerminiertPer(OCTOBER_15);

		boolean result = RuleUtil.isKindTerminiertAnStichtag(k, OCTOBER_15);

		assertThat(result, is(false));
	}

	@Test
	public void stichtagDatumKindTerminiert() {
		k.setGueltigkeitTerminiert(true)
			.setGueltigkeitTerminiertPer(OCTOBER_15);

		boolean result = RuleUtil.isKindTerminiertAnStichtag(k, OCTOBER_15);

		assertThat(result, is(false));
	}

	@Test
	public void stichtagEndeMonatNachKindTerminiert() {
		k.setGueltigkeitTerminiert(true)
			.setGueltigkeitTerminiertPer(OCTOBER_15);

		boolean result = RuleUtil.isKindTerminiertAnStichtag(k, OCTOBER_31);

		assertThat(result, is(false));
	}

	@Test
	public void stichtagFolgemonatNachKindTerminiert() {
		k.setGueltigkeitTerminiert(true)
			.setGueltigkeitTerminiertPer(OCTOBER_15);

		boolean result = RuleUtil.isKindTerminiertAnStichtag(k, NOVEMBER_1);

		assertThat(result, is(true));
	}

	@Test
	public void stichtagSpäterFolgemonatNachKindTerminiert() {
		k.setGueltigkeitTerminiert(true)
			.setGueltigkeitTerminiertPer(OCTOBER_15);

		boolean result = RuleUtil.isKindTerminiertAnStichtag(k, DEZEMBER_1);

		assertThat(result, is(true));
	}
}
