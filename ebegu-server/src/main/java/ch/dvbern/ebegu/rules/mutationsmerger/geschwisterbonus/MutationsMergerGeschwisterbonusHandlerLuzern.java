package ch.dvbern.ebegu.rules.mutationsmerger.geschwisterbonus;

import java.time.LocalDate;
import java.util.Locale;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;

import static ch.dvbern.ebegu.rules.mutationsmerger.util.MutationsMergerUtil.isMeldungZuSpaet;

public class MutationsMergerGeschwisterbonusHandlerLuzern implements
	MutationsMergerGeschwisterbonusHandler {

	/**
	 * Behandelt die Anpassungen des Geschwisterbonus in Mutationen. Takes over
	 * "Geschwisterbonus Kind 1" and "Geschwisterbonus Kind 2" values from the Vorgänger
	 * when the zeitabschnitt of the input from the mutation starts before the Eingangsdatum
	 * of the mutation.
	 * Concretely, this means that the values from the mutation apply from the month following
	 * the Eingangsdatum of the mutation. Until then, the values from the
	 * Vorgänger-Verfügung apply.
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
		if (!isMeldungZuSpaet(
			inputAktuel.getParent().getGueltigkeit(),
			mutationsEingansdatum
		)) {
			return;
		}

		inputAktuel.setGeschwisternBonusKind2(
			resultVorgaenger.getGeschwisterBonusKind2()
		);
		inputAktuel.setGeschwisternBonusKind3(
			resultVorgaenger.getGeschwisterBonusKind3()
		);
		inputAktuel.getParent()
			.getBemerkungenDTOList()
			.removeBemerkungByMsgKey(MsgKey.GESCHWSTERNBONUS_KIND_2);
		inputAktuel.getParent()
			.getBemerkungenDTOList()
			.removeBemerkungByMsgKey(MsgKey.GESCHWSTERNBONUS_KIND_3);
		if (inputAktuel.isGeschwisternBonusKind2()) {
			inputAktuel.addBemerkung(MsgKey.GESCHWSTERNBONUS_KIND_2, locale);
		} else if (inputAktuel.isGeschwisternBonusKind3()) {
			inputAktuel.addBemerkung(MsgKey.GESCHWSTERNBONUS_KIND_3, locale);
		}
	}
}
