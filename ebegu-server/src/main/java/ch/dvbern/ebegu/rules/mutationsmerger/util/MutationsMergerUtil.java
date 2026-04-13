package ch.dvbern.ebegu.rules.mutationsmerger.util;

import java.time.LocalDate;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Gueltigkeit;

public final class MutationsMergerUtil {

	/**
	 * Determines whether a mutation for the {@link Gueltigkeit} of a {@link VerfuegungZeitabschnitt} was submitted
	 * too late.
	 * A mutation is considered too late if the 1st day of the month of the start of the {@link Gueltigkeit} is before
	 * the
	 * Eingangsdatum of the mutation.
	 *
	 * @param gueltigkeit the {@link DateRange} of a {@link VerfuegungZeitabschnitt}
	 * @param mutationsEingansdatum The date on which the mutation was created/freigegeben
	 * @return whether the zeitabschnitt is regared as zuspaet for the mutations merger
	 */
	public static boolean isMeldungZuSpaet(
		@Nonnull DateRange gueltigkeit,
		@Nonnull LocalDate mutationsEingansdatum
	) {
		return !gueltigkeit.getGueltigAb()
			.withDayOfMonth(1)
			.isAfter((mutationsEingansdatum));
	}
}
