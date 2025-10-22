package ch.dvbern.ebegu.finanziellesituation.validation;

import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;

public interface FinanzielleSituationValidator {
	boolean doesFinSitRequireOneGS(Gesuch gesuch);

	boolean isFinanzielleSituationComplete(
		FinanzielleSituation finanzielleSituation,
		Gesuch gesuch
	);

	boolean isEinkommensverschlechterungComplete(
		Einkommensverschlechterung einkommensverschlechterung,
		Gesuch gesuch
	);
}
