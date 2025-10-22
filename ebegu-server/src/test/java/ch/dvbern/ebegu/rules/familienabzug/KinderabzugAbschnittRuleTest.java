package ch.dvbern.ebegu.rules.familienabzug;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.dto.FamilienGroesseCalculationInput;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static ch.dvbern.ebegu.test.TestDataUtil.ENDE_PERIODE;
import static ch.dvbern.ebegu.test.TestDataUtil.START_PERIODE;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.hamcrest.MatcherAssert.assertThat;

class KinderabzugAbschnittRuleTest {

	private final KinderabzugAbschnittRuleASIV kinderAbschnittRuleASIV =
		new KinderabzugAbschnittRuleASIV(
			Constants.DEFAULT_GUELTIGKEIT,
			Constants.DEFAULT_LOCALE
		);

	private final KinderabzugAbschnittRuleFKJV kinderAbschnittRuleFKJV =
		new KinderabzugAbschnittRuleFKJV(
			Constants.DEFAULT_GUELTIGKEIT,
			Constants.DEFAULT_LOCALE
		);

	private final KinderabzugAbschnittRuleSchwyz kinderAbschnittRuleSchwyz =
		new KinderabzugAbschnittRuleSchwyz(
			Constants.DEFAULT_GUELTIGKEIT,
			Constants.DEFAULT_LOCALE
		);

	private AbstractKinderabzugAbschnittRule getRuleForAbzugTyp(
		KinderabzugTyp typ
	) {
		if (typ == KinderabzugTyp.SCHWYZ) {
			return kinderAbschnittRuleSchwyz;
		}

		if (typ == KinderabzugTyp.ASIV) {
			return kinderAbschnittRuleASIV;
		}

		return kinderAbschnittRuleFKJV;
	}

	@ParameterizedTest
	@EnumSource(value = KinderabzugTyp.class,
		names = "KEINE",
		mode = EnumSource.Mode.EXCLUDE)
	void kindTerminiertBeforeGesuchsperiode(KinderabzugTyp kinderabzugTyp) {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			false
		);

		setKindTerminiertPer(betreuung.getKind(), START_PERIODE.minusDays(1));
		List<VerfuegungZeitabschnitt> result = getRuleForAbzugTyp(
			kinderabzugTyp
		).createVerfuegungsZeitabschnitte(betreuung);
		assertThat(result.size(), Matchers.is(0));
	}

	@ParameterizedTest
	@EnumSource(value = KinderabzugTyp.class,
		names = "KEINE",
		mode = EnumSource.Mode.EXCLUDE)
	void kindTerminiertAfterGesuchsperiode(KinderabzugTyp kinderabzugTyp) {
		Betreuung betreuung = createBetreuungMitKindGanzerAbzug();
		setKindTerminiertPer(betreuung.getKind(), ENDE_PERIODE.plusDays(1));

		List<VerfuegungZeitabschnitt> zeitabschnitte = getRuleForAbzugTyp(
			kinderabzugTyp
		).calculate(betreuung, new ArrayList<>());

		assertThat(zeitabschnitte.size(), Matchers.is(1));

		FamilienGroesseCalculationInput result = zeitabschnitte.get(0)
			.getBgCalculationInputAsiv()
			.getFamilienCalculationInput();
		assertThat(
			result.getKinderabzugList().get(1),
			Matchers.is(Kinderabzug.GANZER_ABZUG)
		);
	}

	@ParameterizedTest
	@EnumSource(value = KinderabzugTyp.class,
		names = "KEINE",
		mode = EnumSource.Mode.EXCLUDE)
	void kindTerminiertDuringGesuchsperiode(KinderabzugTyp kinderabzugTyp) {
		Betreuung betreuung = createBetreuungMitKindGanzerAbzug();

		LocalDate terminiert = START_PERIODE.plusDays(15);
		setKindTerminiertPer(betreuung.getKind(), terminiert);

		List<VerfuegungZeitabschnitt> zeitabschnitte = getRuleForAbzugTyp(
			kinderabzugTyp
		).calculate(betreuung, new ArrayList<>());

		assertThat(zeitabschnitte.size(), Matchers.is(1));

		VerfuegungZeitabschnitt zeitAbschnittNotTerminiert = zeitabschnitte.get(
			0
		);
		assertThat(
			zeitAbschnittNotTerminiert.getGueltigkeit().getGueltigAb(),
			Matchers.is(START_PERIODE)
		);
		assertThat(
			zeitAbschnittNotTerminiert.getGueltigkeit().getGueltigBis(),
			Matchers.is(terminiert.with(lastDayOfMonth()))
		);

		FamilienGroesseCalculationInput resultKindNotTerminiert = zeitabschnitte
			.get(0)
			.getBgCalculationInputAsiv()
			.getFamilienCalculationInput();
		assertThat(
			resultKindNotTerminiert.getKinderabzugList().get(1),
			Matchers.is(Kinderabzug.GANZER_ABZUG)
		);
	}

	private Betreuung createBetreuungMitKindGanzerAbzug() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(
			false
		);
		betreuung.getKind().getKindJA().setObhutAlternierendAusueben(false);
		betreuung.getKind().setKindNummer(1);
		return betreuung;
	}

	private void setKindTerminiertPer(KindContainer kind, LocalDate localDate) {
		kind.getKindJA()
			.setGueltigkeitTerminiert(true)
			.setGueltigkeitTerminiertPer(localDate);

	}
}
