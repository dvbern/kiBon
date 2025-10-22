package ch.dvbern.ebegu.finanziellesituation.validation;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;

public class FinanzielleSituationValidatorSZErweitert extends
	FinanzielleSituationValidatorSZ {

	@Override
	protected boolean isAbstractFinSitComplete(
		@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation
	) {
		return abstractFinanzielleSituation.getBruttoLohn() != null
			||
			(abstractFinanzielleSituation.getSteuerbaresEinkommen() != null
				&& abstractFinanzielleSituation.getEinkaeufeVorsorge() != null
				&& abstractFinanzielleSituation.getAbzuegeLiegenschaft() != null
				&& abstractFinanzielleSituation.getLiegenschaftsErtraege()
					!= null
				&& abstractFinanzielleSituation.getSteuerbaresVermoegen()
					!= null);
	}
}
