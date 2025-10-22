package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;

import static ch.dvbern.ebegu.util.MathUtil.EXACT;

public final class RechnerUtil {

	private RechnerUtil() {
	}

	public static BigDecimal calculateVollkostenFuerVerguenstigtesPensum(
		BigDecimal betreuungspensum,
		BigDecimal bgPensum,
		BigDecimal vollkostenProMonat
	) {
		BigDecimal anteilVerguenstigesPensumAmBetreuungspensum =
			BigDecimal.ZERO;
		if (betreuungspensum.compareTo(BigDecimal.ZERO) > 0) {
			anteilVerguenstigesPensumAmBetreuungspensum =
				EXACT.divide(bgPensum, betreuungspensum);
		}
		return EXACT.multiply(
			vollkostenProMonat,
			anteilVerguenstigesPensumAmBetreuungspensum
		);
	}
}
