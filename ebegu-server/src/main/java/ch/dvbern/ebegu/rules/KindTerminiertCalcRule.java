package ch.dvbern.ebegu.rules;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.util.Constants;

public class KindTerminiertCalcRule extends AbstractCalcRule {

	protected KindTerminiertCalcRule(
		@Nonnull Locale locale
	) {
		super(
			RuleKey.KIND_ANSPRUCH,
			RuleType.GRUNDREGEL_CALC,
			RuleValidity.ASIV,
			Constants.DEFAULT_GUELTIGKEIT,
			locale
		);
	}

	@Override
	protected void executeRule(
		@Nonnull AbstractPlatz platz,
		@Nonnull BGCalculationInput inputData
	) {
		if (inputData.isKindTerminiert()) {
			inputData.setAnspruchZeroAndSaveRestanspruch();
			inputData.addBemerkungWithGueltigkeitOfAbschnitt(
				MsgKey.KEIN_ANSPRUCH_KIND_TERMINIERT,
				getLocale()
			);
			inputData.getParent()
				.getBemerkungenDTOList()
				.removeBemerkungByMsgKey(MsgKey.ERWERBSPENSUM_ANSPRUCH);
		}
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
	}
}
