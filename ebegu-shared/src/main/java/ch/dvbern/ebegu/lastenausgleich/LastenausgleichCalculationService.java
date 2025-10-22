package ch.dvbern.ebegu.lastenausgleich;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Lastenausgleich;

public interface LastenausgleichCalculationService {

	Lastenausgleich calculateLastenausgleichForGemeinde(
		String lastenausgleichId,
		Gemeinde gemeinde
	);

	void calculateTotals(String lastenausgleichId);
}
