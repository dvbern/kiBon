package ch.dvbern.ebegu.rules.mutationsmerger.util;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;

public abstract class VorgaengerZeitabschnittFinder {

	private VorgaengerZeitabschnittFinder() {
	}

	@Nonnull
	public static List<VerfuegungZeitabschnitt> findZeitabschnitteInVorgaenger(
		VerfuegungZeitabschnitt current,
		@Nullable Verfuegung vorgaengerVerf
	) {
		if (vorgaengerVerf == null) {
			return List.of();
		}
		return vorgaengerVerf.getZeitabschnitte()
			.stream()
			.filter(
				potentialVorgaenger -> potentialVorgaenger.getGueltigkeit()
					.intersects(current.getGueltigkeit())
			)
			.collect(Collectors.toList());
	}
}
