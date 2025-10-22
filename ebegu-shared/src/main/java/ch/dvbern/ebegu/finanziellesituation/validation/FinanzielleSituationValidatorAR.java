package ch.dvbern.ebegu.finanziellesituation.validation;

import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;

import static java.util.Objects.requireNonNull;

public class FinanzielleSituationValidatorAR implements
	FinanzielleSituationValidator {

	@Override
	public boolean doesFinSitRequireOneGS(Gesuch gesuch) {
		return Boolean.TRUE.equals(
			requireNonNull(
				requireNonNull(
					gesuch.getFamiliensituationContainer()
				).getFamiliensituationJA()
			).getGemeinsameSteuererklaerung()
		);
	}

	@Override
	public boolean isFinanzielleSituationComplete(
		FinanzielleSituation finanzielleSituation,
		Gesuch gesuch
	) {
		if (finanzielleSituation.getFinSitZusatzangabenAppenzell() == null) {
			return false;
		}
		return finanzielleSituation.getFinSitZusatzangabenAppenzell()
			.isVollstaendig()
			&& finanzielleSituation.getSteuerbaresEinkommen() != null
			&& finanzielleSituation.getSteuerbaresVermoegen() != null;
	}

	@Override
	public boolean isEinkommensverschlechterungComplete(
		Einkommensverschlechterung einkommensverschlechterung,
		Gesuch gesuch
	) {
		return einkommensverschlechterung.isVollstaendig(gesuch.getFinSitTyp());
	}
}
