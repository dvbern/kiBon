package ch.dvbern.ebegu.dokumente.anlageverzeichnis;

import ch.dvbern.ebegu.entities.Familiensituation;

public class LuzernFamiliensituationDokumente extends
	AbstractFamiliensituationDokumente {

	@Override
	protected boolean isUnterstuetzungsbestaetigungNeeded(
		Familiensituation familiensituation
	) {
		return false;
	}
}
