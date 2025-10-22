package ch.dvbern.ebegu.finanziellesituation.validation;

import java.util.Objects;

import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;

public class FinanzielleSituationValidatorBernFKJV implements
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

		var steuerdatenVollstaendig = areSteuerdatenAngabenVollstaendig(
			gesuch
		);

		return finanzielleSituation.isVollstaendig(gesuch.getFinSitTyp())
			&& !geschaeftsgewinnInvalid
			&& steuerdatenVollstaendig;
	}

	private static boolean areSteuerdatenAngabenVollstaendig(
		Gesuch gesuch
	) {
		if (gesuch.getEingangsart() == Eingangsart.PAPIER
			|| gesuch.getFall().isSozialdienstFall()) {
			return true;
		}
		FinanzielleSituation finanzielleSituation = Objects.requireNonNull(
			Objects.requireNonNull(gesuch.getGesuchsteller1())
				.getFinanzielleSituationContainer()
		)
			.getFinanzielleSituationJA();
		return (Boolean.TRUE.equals(
			finanzielleSituation.getSteuerdatenZugriff()
		)
			&& (finanzielleSituation.getSteuerdatenAbfrageStatus()
				!= SteuerdatenAnfrageStatus.NEUE_VERANLAGUNG
				||
				(finanzielleSituation.getSteuerdatenAbfrageStatus()
					== SteuerdatenAnfrageStatus.NEUE_VERANLAGUNG
					&& gesuch.getStatus() == AntragStatus.FREIGABEQUITTUNG)))
			||
			(Boolean.FALSE.equals(finanzielleSituation.getSteuerdatenZugriff())
				&& finanzielleSituation.getAutomatischePruefungErlaubt()
					!= null);
	}

	@Override
	public boolean isEinkommensverschlechterungComplete(
		Einkommensverschlechterung einkommensverschlechterung,
		Gesuch gesuch
	) {
		return einkommensverschlechterung.isVollstaendig(gesuch.getFinSitTyp());
	}
}
