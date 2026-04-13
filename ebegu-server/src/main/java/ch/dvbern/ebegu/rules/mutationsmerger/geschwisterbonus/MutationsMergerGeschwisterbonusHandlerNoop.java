package ch.dvbern.ebegu.rules.mutationsmerger.geschwisterbonus;

import java.time.LocalDate;
import java.util.Locale;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;

public class MutationsMergerGeschwisterbonusHandlerNoop implements
	MutationsMergerGeschwisterbonusHandler {
	/**
	 * Handles the the Geschwisterbonus in Mutationen. Takes no values from the Vorgänger.
	 * Concretely, this means that the values for the Geschwisterbonus are adjusted retroactively.
	 *
	 *
	 * @param inputAktuel The input of the {@link VerfuegungZeitabschnitt} from the mutation
	 * @param resultVorgaenger The {@link VerfuegungZeitabschnitt} from the vorgänger verfügung
	 * {@link Verfuegung} that matches the {@link VerfuegungZeitabschnitt} from the mutation
	 * @param mutationsEingansdatum The date on which the mutation was created/freigegeben
	 * @param locale The {@link Locale} used for texts
	 */
	@Override
	public void handleGeschwisterbonus(
		BGCalculationInput inputAktuel,
		BGCalculationResult resultVorgaenger,
		LocalDate mutationsEingansdatum,
		Locale locale
	) {
		// noop
	}
}
