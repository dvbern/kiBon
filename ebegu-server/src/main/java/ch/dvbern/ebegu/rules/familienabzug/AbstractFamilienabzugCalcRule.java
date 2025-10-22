package ch.dvbern.ebegu.rules.familienabzug;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.FamilienGroesseCalculationInput;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.AbstractCalcRule;
import ch.dvbern.ebegu.rules.RuleKey;
import ch.dvbern.ebegu.rules.RuleType;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.types.DateRange;

public abstract class AbstractFamilienabzugCalcRule extends AbstractCalcRule {

	protected AbstractFamilienabzugCalcRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		super(
			RuleKey.FAMILIENSITUATION,
			RuleType.GRUNDREGEL_CALC,
			RuleValidity.ASIV,
			validityPeriod,
			locale
		);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
	}

	protected double countAnzahlKinderFuerAbzug(
		FamilienGroesseCalculationInput familiengroesseCalculationInput
	) {
		return familiengroesseCalculationInput.getKinderabzugList()
			.values()
			.stream()
			.mapToDouble(kinderabzug -> {
				if (kinderabzug == Kinderabzug.HALBER_ABZUG) {
					return 0.5;
				}

				if (kinderabzug == Kinderabzug.GANZER_ABZUG) {
					return 1;
				}

				return 0;
			})
			.sum();
	}

	@Override
	public boolean isRelevantForFamiliensituation() {
		return true;
	}
}
