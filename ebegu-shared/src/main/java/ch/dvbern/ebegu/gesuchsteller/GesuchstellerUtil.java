package ch.dvbern.ebegu.gesuchsteller;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;

public final class GesuchstellerUtil {

	/**
	 * Checks whether the GS2 {@link GesuchstellerContainer} is not null. Does not check if GS2 is required.
	 *
	 * @param gesuch the {@link Gesuch} to be checked
	 * @return whether GS2 {@link GesuchstellerContainer} is present
	 */
	public static boolean isSecondGSPresent(Gesuch gesuch) {
		return gesuch.getGesuchsteller2() != null;
	}
}
