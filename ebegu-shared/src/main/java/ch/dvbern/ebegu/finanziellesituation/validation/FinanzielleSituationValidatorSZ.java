package ch.dvbern.ebegu.finanziellesituation.validation;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;

import static java.util.Objects.requireNonNull;

public class FinanzielleSituationValidatorSZ implements
	FinanzielleSituationValidator {
	@Override
	public boolean doesFinSitRequireOneGS(Gesuch gesuch) {
		final Familiensituation familiensituation = requireNonNull(
			requireNonNull(
				gesuch.getFamiliensituationContainer()
			).getFamiliensituationJA()
		);

		return Boolean.TRUE.equals(
			familiensituation.getGemeinsameSteuererklaerung()
		)
			|| familiensituation.getGesuchstellerKardinalitaet()
				== EnumGesuchstellerKardinalitaet.ALLEINE;
	}

	@Override
	public boolean isFinanzielleSituationComplete(
		FinanzielleSituation finanzielleSituation,
		Gesuch gesuch
	) {
		return isAbstractFinSitComplete(finanzielleSituation);
	}

	@Override
	public boolean isEinkommensverschlechterungComplete(
		Einkommensverschlechterung einkommensverschlechterung,
		Gesuch gesuch
	) {
		return isAbstractFinSitComplete(einkommensverschlechterung);
	}

	@SuppressWarnings("PMD.UnusedPrivateMethod") //pmd does not find the usages even though it's used
	protected boolean isAbstractFinSitComplete(
		@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation
	) {
		return abstractFinanzielleSituation.getBruttoLohn() != null
			||
			(abstractFinanzielleSituation.getSteuerbaresEinkommen() != null
				&& abstractFinanzielleSituation.getEinkaeufeVorsorge() != null
				&& abstractFinanzielleSituation.getAbzuegeLiegenschaft() != null
				&& abstractFinanzielleSituation.getSteuerbaresVermoegen()
					!= null);
	}
}
