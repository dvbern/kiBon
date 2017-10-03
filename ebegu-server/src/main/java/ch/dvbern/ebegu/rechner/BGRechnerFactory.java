package ch.dvbern.ebegu.rechner;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;

/**
 * Factory, welche für eine Betreuung den richtigen BG-Rechner ermittelt
 */
public class BGRechnerFactory {

	public static AbstractBGRechner getRechner(Betreuung betreuung) {
		BetreuungsangebotTyp betreuungsangebotTyp = betreuung.getBetreuungsangebotTyp();
		if (BetreuungsangebotTyp.KITA.equals(betreuungsangebotTyp)) {
			return new KitaRechner();
		}
		if (BetreuungsangebotTyp.TAGI.equals(betreuungsangebotTyp)) {
			return new TagiRechner();
		}
		if (betreuungsangebotTyp.isTageseltern()) {
			return new TageselternRechner();
		}
		// Alle anderen Angebotstypen werden nicht berechnet
		return null;
	}
}
