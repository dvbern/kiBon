package ch.dvbern.ebegu.finanziellesituation.validation;

import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;

public class FinanzielleSituationValidatorSO implements
	FinanzielleSituationValidator {

	@Override
	public boolean doesFinSitRequireOneGS(Gesuch gesuch) {
		return !gesuch.hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode();
	}

	@Override
	public boolean isFinanzielleSituationComplete(
		FinanzielleSituation finanzielleSituation,
		Gesuch gesuch
	) {
		return (finanzielleSituation.getBruttoLohn() != null
			&& finanzielleSituation.getSteuerbaresVermoegen() != null)
			|| (finanzielleSituation.getNettolohn() != null
				&& finanzielleSituation.getUnterhaltsBeitraege() != null
				&& finanzielleSituation.getAbzuegeKinderAusbildung() != null
				&& finanzielleSituation.getSteuerbaresVermoegen() != null);
	}

	@Override
	public boolean isEinkommensverschlechterungComplete(
		Einkommensverschlechterung einkommensverschlechterung,
		Gesuch gesuch
	) {
		return einkommensverschlechterung.getBruttolohnAbrechnung1() != null
			&& einkommensverschlechterung.getBruttolohnAbrechnung2() != null
			&& einkommensverschlechterung.getBruttolohnAbrechnung3() != null
			&& einkommensverschlechterung.getExtraLohn() != null
			&& einkommensverschlechterung.getNettoVermoegen() != null;
	}
}
