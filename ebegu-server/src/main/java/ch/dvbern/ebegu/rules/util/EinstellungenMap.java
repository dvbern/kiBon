package ch.dvbern.ebegu.rules.util;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.Gesuchsperiode;

/**
 * Hilfsklasse fuer das Handling von EinstellungenMaps
 */
public class EinstellungenMap {
	private Map<EinstellungKey, Einstellung> einstellungen = new EnumMap<>(
		EinstellungKey.class
	);

	public void addEinstellung(
		@Nonnull EinstellungKey key,
		@Nonnull String value,
		@Nonnull Gesuchsperiode gesuchsperiode
	) {
		Einstellung einstellung = new Einstellung(
			key,
			value,
			gesuchsperiode
		);
		einstellungen.put(key, einstellung);
	}

	@Nonnull
	public Map<EinstellungKey, Einstellung> getEinstellungen() {
		return einstellungen;
	}
}
