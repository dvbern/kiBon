package ch.dvbern.ebegu.finanziellesituation.validation;

import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;

import static java.util.Objects.requireNonNull;

public class FinanzielleSituationValidatorLU implements
	FinanzielleSituationValidator {
	@Override
	public boolean doesFinSitRequireOneGS(Gesuch gesuch) {
		final EnumFamilienstatus familienstatus = requireNonNull(
			requireNonNull(gesuch.getFamiliensituationContainer())
				.getFamiliensituationJA()
		).getFamilienstatus();

		return familienstatus == EnumFamilienstatus.VERHEIRATET;
	}

	@Override
	public boolean isFinanzielleSituationComplete(
		FinanzielleSituation finanzielleSituation,
		Gesuch gesuch
	) {
		return isInfomaZahlungenVollstaendig(gesuch)
			&& finanzielleSituation.isVollstaendig(
				FinanzielleSituationTyp.LUZERN
			);
	}

	@Override
	public boolean isEinkommensverschlechterungComplete(
		Einkommensverschlechterung einkommensverschlechterung,
		Gesuch gesuch
	) {
		return isInfomaZahlungenVollstaendig(gesuch)
			&& einkommensverschlechterung.isVollstaendig(
				FinanzielleSituationTyp.LUZERN
			);
	}

	private boolean isInfomaZahlungenVollstaendig(Gesuch gesuch) {
		boolean valid = true;
		if (gesuch.getStatus().isReadableByJugendamtSchulamtSteueramt()
			&&
			Boolean.TRUE.equals(
				gesuch.extractGemeinde().getInfomaZahlungen()
			)) {
			valid = gesuch.getFamiliensituationContainer() != null
				&& gesuch.getFamiliensituationContainer()
					.getFamiliensituationJA()
					!= null
				&& gesuch.getFamiliensituationContainer()
					.getFamiliensituationJA()
					.getAuszahlungsdaten()
					!= null
				&& gesuch.getFamiliensituationContainer()
					.getFamiliensituationJA()
					.getAuszahlungsdaten()
					.getInfomaBankcode()
					!= null
				&& gesuch.getFamiliensituationContainer()
					.getFamiliensituationJA()
					.getAuszahlungsdaten()
					.getInfomaKreditorennummer()
					!= null;
		}
		return valid
			&& gesuch.getFamiliensituationContainer() != null
			&& gesuch.getFamiliensituationContainer()
				.getFamiliensituationJA()
				!= null
			&& gesuch.getFamiliensituationContainer()
				.getFamiliensituationJA()
				.getAuszahlungsdaten()
				!= null
			&& gesuch.getFamiliensituationContainer()
				.getFamiliensituationJA()
				.getAuszahlungsdaten()
				.getIban()
				!= null;
	}

}
