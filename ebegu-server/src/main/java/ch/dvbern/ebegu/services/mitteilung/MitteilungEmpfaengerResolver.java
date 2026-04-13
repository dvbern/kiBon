package ch.dvbern.ebegu.services.mitteilung;

import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.GemeindeService;

/**
 * Dedicated class to resolve the {@link Benutzer}(s) to become an empfaenger for a {@link Mitteilung}.
 */
@ApplicationScoped
public class MitteilungEmpfaengerResolver {

	@Inject
	private GemeindeService gemeindeService;

	/**
	 * Resolves the {@link Benutzer} to be empfaenger for a {@link Mitteilung} to a Gemeinde
	 * <p>
	 * The Empfaenger is resolved by priority:
	 * 1. The BG-Verantwortliche set on the dossier of the mitteilung
	 * 2. The TS-Verantwortliche set on the dossier of the mitteilung
	 * 3. The Standard-Verantwortliche of the gemeinde of the dossier of the mitteilung
	 *
	 * @throws EbeguRuntimeException if no verantwortliche is set on dossier and gemeinde
	 * @param mitteilung the {@link Mitteilung} the empfaenger is to be resolved for
	 * @return the {@link Benutzer} resolved as empfaenger in the provided {@link Mitteilung} to a gemeinde
	 */
	public Benutzer getEmpfaengerBeiMitteilungAnGemeinde(
		@Nonnull Mitteilung mitteilung
	) {
		Benutzer empfaenger = mitteilung.getDossier().getVerantwortlicherBG();
		if (empfaenger == null) {
			empfaenger = mitteilung.getDossier().getVerantwortlicherTS();
		}
		if (empfaenger == null) {
			String gemeindeId = mitteilung.getDossier().getGemeinde().getId();
			Optional<GemeindeStammdaten> stammdatenOptional =
				gemeindeService.getGemeindeStammdatenByGemeindeId(
					gemeindeId
				);
			if (stammdatenOptional.isPresent()) {
				// Wir kontrollieren bei den Mitteilungen explizit nicht, ob die Rolle stimmt!
				// Wir nehmen den Allgemeinen Default, weil wir auf der Mitteilung kein Gesuch haben
				// und daher nicht wissen, ob es ein reines BG- oder TS-Gesuch ist
				empfaenger = stammdatenOptional.get().getDefaultBenutzer();
			}
		}
		if (empfaenger == null) {
			throw new EbeguRuntimeException(
				"getEmpfaengerBeiMitteilungAnGemeinde",
				ErrorCodeEnum.ERROR_VERANTWORTLICHER_NOT_FOUND,
				mitteilung.getId()
			);
		}
		return empfaenger;
	}

}
