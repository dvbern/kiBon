package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.rules.EbeguRuleTestsHelper.calculate;
import static ch.dvbern.ebegu.test.TestDataUtil.ENDE_PERIODE;
import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class KindTerminiertRuleTest {

	private Betreuung betreuung;

	@BeforeEach
	public void setUp() {
		betreuung = EbeguRuleTestsHelper.createBetreuungWithPensum(
			START_PERIODE,
			ENDE_PERIODE,
			BetreuungsangebotTyp.KITA,
			80,
			BigDecimal.valueOf(800)
		);
		betreuung.initVorgaengerVerfuegungen(null, null);
	}

	@Test
	void kindNotTerminiert_shouldCreateNoZeitabschnitt() {
		betreuung.getKind().getKindJA().setGueltigkeitTerminiert(false);

		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		assertThat(result, notNullValue());
		result.forEach(this::assertKindNotTerminiert);
	}

	@Test
	void kindTerminiert_ganzePeriode_shouldBeTerminiertInAllenAbschnitten() {
		betreuung.getKind().getKindJA().setGueltigkeitTerminiert(true);
		betreuung.getKind()
			.getKindJA()
			.setGueltigkeitTerminiertPer(START_PERIODE.minusDays(1));

		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		assertThat(result, notNullValue());
		result.forEach(this::assertKindTerminiert);
	}

	@Test
	void kindTerminiert_nachPeriode_shouldBeTerminiertInKeinemAbschnitt() {
		betreuung.getKind().getKindJA().setGueltigkeitTerminiert(true);
		betreuung.getKind()
			.getKindJA()
			.setGueltigkeitTerminiertPer(ENDE_PERIODE.plusDays(1));

		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		assertThat(result, notNullValue());
		result.forEach(this::assertKindNotTerminiert);
	}

	@Test
	void kindTerminiert_duringPeriode_shouldBeTerminiertAbFolgemonat() {
		LocalDate terminiertPer = START_PERIODE.plusMonths(1);
		betreuung.getKind().getKindJA().setGueltigkeitTerminiert(true);
		betreuung.getKind()
			.getKindJA()
			.setGueltigkeitTerminiertPer(terminiertPer);

		List<VerfuegungZeitabschnitt> result = calculate(betreuung);

		assertThat(result, notNullValue());
		assertThat(result.size(), is(2));
		result.forEach(zeitabschnitt -> {
			if (zeitabschnitt.getGueltigkeit()
				.isBefore(
					terminiertPer.with(
						TemporalAdjusters.firstDayOfNextMonth()
					)
				)) {
				assertKindNotTerminiert(zeitabschnitt);
			} else {
				assertKindTerminiert(zeitabschnitt);
			}
		});
	}

	private void assertKindNotTerminiert(
		VerfuegungZeitabschnitt zeitabschnitt
	) {
		assertThat(
			zeitabschnitt.getRelevantBgCalculationInput().isKindTerminiert(),
			is(false)
		);
		assertThat(
			zeitabschnitt.getAnspruchberechtigtesPensum(),
			notNullValue()
		);
		assertThat(
			zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.KEIN_ANSPRUCH_KIND_TERMINIERT),
			is(false)
		);
	}

	private void assertKindTerminiert(VerfuegungZeitabschnitt zeitabschnitt) {
		assertThat(
			zeitabschnitt.getRelevantBgCalculationInput().isKindTerminiert(),
			is(true)
		);
		assertThat(
			zeitabschnitt.getRelevantBgCalculationInput()
				.getAnspruchspensumProzent(),
			is(0)
		);
		assertThat(
			zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.KEIN_ANSPRUCH_KIND_TERMINIERT),
			is(true)
		);
		assertThat(
			zeitabschnitt.getBemerkungenDTOList()
				.containsMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH),
			is(false)
		);
	}
}
