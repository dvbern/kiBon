package ch.dvbern.ebegu.rules.mutationsmerger.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;

public abstract class VerfuegungZeitabschnittSplitter {

	private VerfuegungZeitabschnittSplitter() {
	}

	@Nonnull
	public static List<VerfuegungZeitabschnitt> splitOn(
		VerfuegungZeitabschnitt current,
		List<VerfuegungZeitabschnitt> toSplitOn
	) {
		if (toSplitOn.isEmpty()) {
			return List.of(current);
		}

		Set<LocalDate> boundaries = new TreeSet<>();

		for (VerfuegungZeitabschnitt splitter : toSplitOn) {
			LocalDate from = splitter.getGueltigkeit().getGueltigAb();
			LocalDate to = splitter.getGueltigkeit().getGueltigBis();

			if (!to.isBefore(current.getGueltigkeit().getGueltigAb())
				&&
				!from.isAfter(current.getGueltigkeit().getGueltigBis())) {
				boundaries.add(from);
				boundaries.add(to.plusDays(1));
			}
		}

		boundaries.add(current.getGueltigkeit().getGueltigAb());
		boundaries.add(current.getGueltigkeit().getGueltigBis().plusDays(1));

		List<LocalDate> sorted = new ArrayList<>(boundaries);
		List<VerfuegungZeitabschnitt> result = new ArrayList<>();

		for (int i = 0; i < sorted.size() - 1; i++) {
			LocalDate start = sorted.get(i);
			LocalDate end = sorted.get(i + 1).minusDays(1);

			if (end.isBefore(current.getGueltigkeit().getGueltigAb())
				||
				start.isAfter(current.getGueltigkeit().getGueltigBis())) {
				continue;
			}

			LocalDate segmentStart = start.isBefore(
				current.getGueltigkeit().getGueltigAb()
			) ? current.getGueltigkeit().getGueltigAb() : start;
			LocalDate segmentEnd = end.isAfter(
				current.getGueltigkeit().getGueltigBis()
			) ? current.getGueltigkeit().getGueltigBis() : end;

			if (!segmentStart.isAfter(segmentEnd)) {
				result.add(
					createWithGueltigkeit(current, segmentStart, segmentEnd)
				);
			}
		}

		return result;
	}

	private static VerfuegungZeitabschnitt createWithGueltigkeit(
		VerfuegungZeitabschnitt base,
		LocalDate gueltigAb,
		LocalDate gueltigBis
	) {
		VerfuegungZeitabschnitt verfuegungZeitabschnitt =
			new VerfuegungZeitabschnitt(base);
		verfuegungZeitabschnitt.getGueltigkeit().setGueltigAb(gueltigAb);
		verfuegungZeitabschnitt.getGueltigkeit().setGueltigBis(gueltigBis);
		return verfuegungZeitabschnitt;
	}
}
