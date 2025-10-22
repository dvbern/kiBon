package ch.dvbern.ebegu.util;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;

public class MinPensumEinstellungKeyBetreuungsTypVisitor implements
	BetreuungsangebotTypVisitor<EinstellungKey> {

	@Nullable
	public EinstellungKey getEinstellungenKey(
		@Nullable BetreuungsangebotTyp betreuungsangebotTyp
	) {
		if (betreuungsangebotTyp == null) {
			return null;
		}

		return betreuungsangebotTyp.accept(this);
	}

	@Override
	public EinstellungKey visitKita() {
		return EinstellungKey.PARAM_PENSUM_KITA_MIN;
	}

	@Override
	public EinstellungKey visitTagesfamilien() {
		return EinstellungKey.PARAM_PENSUM_TAGESELTERN_MIN;
	}

	@Override
	public EinstellungKey visitMittagstisch() {
		return EinstellungKey.PARAM_PENSUM_TAGESELTERN_MIN;
	}

	@Override
	public EinstellungKey visitTagesschule() {
		return EinstellungKey.PARAM_PENSUM_TAGESSCHULE_MIN;
	}

	@Nullable
	@Override
	public EinstellungKey visitFerieninsel() {
		return null;
	}
}
