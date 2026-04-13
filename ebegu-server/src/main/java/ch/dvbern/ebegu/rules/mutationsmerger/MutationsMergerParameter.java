package ch.dvbern.ebegu.rules.mutationsmerger;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.enums.GeschwisterbonusTyp;

/**
 * Includes values of the {@link Einstellung}en which are relevant for the MutationsMerger.
 *
 * @param pauschaleRueckwirkendAuszahlen value of the {@link Einstellung} "FKJV_PAUSCHALE_RUECKWIRKEND"
 * @param geschwisterbonusTyp value of the {@link Einstellung} "GESCHWISTERNBONUS_TYP"
 */
public record MutationsMergerParameter(
									   Boolean pauschaleRueckwirkendAuszahlen,
									   GeschwisterbonusTyp geschwisterbonusTyp
) {
}
