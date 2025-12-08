package ch.dvbern.ebegu.dokumente.anlageverzeichnis;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.util.EbeguUtil;

public class FamiliensituationDokumente extends
	AbstractFamiliensituationDokumente {

	@Override
	protected boolean isUnterstuetzungsbestaetigungNeeded(
		Familiensituation familiensituation
	) {
		return !EbeguUtil.isNullOrFalse(
			familiensituation.getSozialhilfeBezueger()
		);
	}
}
