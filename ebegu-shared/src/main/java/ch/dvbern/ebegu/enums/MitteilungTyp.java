package ch.dvbern.ebegu.enums;

import java.util.Objects;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;

public enum MitteilungTyp {
	BETREUUNGSMITTEILUNG, NEUE_VERANLAGUNGS_MITTEILUNG;

	@Nullable
	public static MitteilungTyp getMitteilungTypByClass(
		Class<? extends Mitteilung> clazz
	) {
		if (Objects.equals(clazz, Betreuungsmitteilung.class)) {
			return BETREUUNGSMITTEILUNG;
		}

		if (Objects.equals(clazz, NeueVeranlagungsMitteilung.class)) {
			return NEUE_VERANLAGUNGS_MITTEILUNG;
		}

		return null;
	}
}
