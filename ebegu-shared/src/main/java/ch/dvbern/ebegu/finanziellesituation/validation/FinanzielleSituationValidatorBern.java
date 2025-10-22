package ch.dvbern.ebegu.finanziellesituation.validation;

import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;

public class FinanzielleSituationValidatorBern implements
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
		// Zwingend ist nur das erste Jahr, FALLS ueberhaupt eines ausgefuellt wird.
		// Das einzige, das wir validieren koennen, ist das Jahr+1 bzw. Jahr+2 nicht ausgefuellt sein duerfen, falls
		// Basisjahr null
		var geschaeftsgewinnInvalid = (finanzielleSituation
			.getGeschaeftsgewinnBasisjahrMinus1()
			!= null
			|| finanzielleSituation.getGeschaeftsgewinnBasisjahrMinus2()
				!= null)
			&& finanzielleSituation.getGeschaeftsgewinnBasisjahr() == null;

		return finanzielleSituation.isVollstaendig(gesuch.getFinSitTyp())
			&& !geschaeftsgewinnInvalid;
	}

	@Override
	public boolean isEinkommensverschlechterungComplete(
		Einkommensverschlechterung einkommensverschlechterung,
		Gesuch gesuch
	) {
		return einkommensverschlechterung.isVollstaendig(gesuch.getFinSitTyp());
	}
}
