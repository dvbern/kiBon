package ch.dvbern.ebegu.gemeinde;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;

@ApplicationScoped
public class GemeindeKonfigurationService {

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GemeindeService gemeindeService;

	/**
	 * Loads all {@link Einstellung} applicable to gemeinde in the {@link Gesuchsperiode} gueltig for the gemeinde.
	 *
	 * @param gemeinde the gemeinde the einstellung should be loaded for
	 * @return a map with the {@link Gesuchsperiode} as keys and their einstellungen maps as values
	 *
	 */
	public Map<Gesuchsperiode, Map<EinstellungKey, Einstellung>> loadEinstellungenOfGPRelevantForGemeinde(
		@Nonnull Gemeinde gemeinde
	) {
		var stammdaten = gemeindeService.getGemeindeStammdatenByGemeindeId(
			gemeinde.getId()
		).orElseThrow();

		var gueltigeGesuchsperioden = getGueltigeGesuchsperioden(stammdaten);

		var gpMap =
			new HashMap<Gesuchsperiode, Map<EinstellungKey, Einstellung>>();

		gueltigeGesuchsperioden.forEach(
			gp -> {
				gpMap.put(
					gp,
					einstellungService
						.getGemeindeEinstellungenActiveForMandantOnlyAsMap(
							gemeinde,
							gp
						)
				);
			}
		);

		return gpMap;
	}

	/**
	 * Gets the gesuchsperioden which are gueltig for the {@link GemeindeStammdaten} provided. A {@link Gesuchsperiode}
	 * is considered
	 * gueltig for a gemeinde when the end of the gemeinde gueltigkeit is after the end of the start date of the
	 * {@link Gesuchsperiode}
	 *
	 * @param stammdaten The {@link GemeindeStammdaten} for which the {@link Gesuchsperiode}n should be loaded
	 * @return all {@link Gesuchsperiode} which are considered gueltig for the {@link GemeindeStammdaten}
	 */
	public List<Gesuchsperiode> getGueltigeGesuchsperioden(
		GemeindeStammdaten stammdaten
	) {
		return gesuchsperiodeService
			.getAllGesuchsperioden(
				stammdaten.getGemeinde().getMandant()
			)
			.stream()
			.filter(
				gesuchsperiode -> gesuchsperiode.getMandant()
					.equals(
						stammdaten.getGemeinde()
							.getMandant()
					)
			)
			.filter(
				gesuchsperiode -> stammdaten.getGemeinde()
					.getGueltigBis()
					.isAfter(
						gesuchsperiode.getGueltigkeit()
							.getGueltigAb()
					)
			)
			.toList();
	}
}
