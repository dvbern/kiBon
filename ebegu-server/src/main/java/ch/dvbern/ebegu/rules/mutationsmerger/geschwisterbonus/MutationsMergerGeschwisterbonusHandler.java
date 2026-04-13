package ch.dvbern.ebegu.rules.mutationsmerger.geschwisterbonus;

import java.time.LocalDate;
import java.util.Locale;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;

/**
 * Interface for the {@link MutationsMergerGeschwisterbonusHandlerVisitor}.
 */
public interface MutationsMergerGeschwisterbonusHandler {

	/**
	 * Handles the adjustments of the geschwisterbonus bonus in mutations. Takes over
	 * values from the vorgänger verfügung when necessary based on the eingangsdatum.
	 * Implementations determine based on the eingangsdatum whether and which values
	 * are taken over from the vorgänger verfügung, respectively are not retroactively adjusted in the mutation.
	 *
	 * @param inputAktuel The input of the {@link VerfuegungZeitabschnitt} from the mutation
	 * @param resultVorgaenger The {@link VerfuegungZeitabschnitt} from the vorgänger verfügung
	 * {@link Verfuegung} that matches the {@link VerfuegungZeitabschnitt} from the mutation
	 * @param mutationsEingansdatum The date on which the mutation was created/freigegeben
	 * @param locale The {@link Locale} used for texts
	 */
	void handleGeschwisterbonus(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		LocalDate mutationsEingansdatum,
		Locale locale
	);
}
