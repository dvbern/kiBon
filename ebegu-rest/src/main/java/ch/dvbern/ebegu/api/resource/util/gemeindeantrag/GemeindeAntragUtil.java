package ch.dvbern.ebegu.api.resource.util.gemeindeantrag;

import java.util.Objects;

import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.WithEinreichedatum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;

public final class GemeindeAntragUtil {

	private GemeindeAntragUtil() {
	}

	public static int compareEinreichedatum(
		GemeindeAntrag a,
		GemeindeAntrag b,
		int reverseMultiplicator
	) {
		if (hasNoEinreichedatum(a) && hasNoEinreichedatum(b)) {
			return 0;
		}
		if ((a instanceof WithEinreichedatum) && hasNoEinreichedatum(b)) {
			return -1;
		}
		if (hasNoEinreichedatum(a)) {
			return 1;
		}
		if ((a instanceof WithEinreichedatum aWithEinreichedatum
			&& (b instanceof WithEinreichedatum bWithEinreichedatum))) {
			return Objects.requireNonNull(
				aWithEinreichedatum.getEinreichedatum()
			)
				.compareTo(
					Objects.requireNonNull(
						bWithEinreichedatum.getEinreichedatum()
					)
				)
				* reverseMultiplicator;
		}

		throw new EbeguRuntimeException(
			"compareEinreichedatum",
			"Arguments must be of type WithEinreichedatum"
		);
	}

	private static boolean hasNoEinreichedatum(GemeindeAntrag a) {
		return !(a instanceof WithEinreichedatum)
			|| ((WithEinreichedatum) a).getEinreichedatum() == null;
	}
}
