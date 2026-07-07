/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package ch.dvbern.ebegu.outbox.gemeindekennzahlen;

import java.util.Map;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.persistence.Persistence;

@Stateless
public class GemeindeKennzahlenEventHelper {

	@Inject
	private Persistence persistence;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private GemeindeKennzahlenEventConverter gemeindeKennzahlenEventConverter;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void convertAndFire(String id) {
		GemeindeKennzahlen gemeindeKennzahlen = persistence.find(
			GemeindeKennzahlen.class,
			id
		);

		Map<EinstellungKey, Einstellung> gemeindeKonfigurationMap =
			einstellungService
				.getGemeindeEinstellungenOnlyAsMap(
					gemeindeKennzahlen.getGemeinde(),
					gemeindeKennzahlen.getGesuchsperiode()
				);

		Einstellung einstellungBgAusstellenBisStufe =
			gemeindeKonfigurationMap.get(
				EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE
			);
		EinschulungTyp bgAusstellenBisUndMitStufe =
			EinschulungTyp.valueOf(
				einstellungBgAusstellenBisStufe.getValue()
			);

		Einstellung einstellungErwerbspensumZuschlag =
			gemeindeKonfigurationMap.get(
				EinstellungKey.ERWERBSPENSUM_ZUSCHLAG
			);

		event.fire(
			gemeindeKennzahlenEventConverter.of(
				gemeindeKennzahlen,
				bgAusstellenBisUndMitStufe,
				einstellungErwerbspensumZuschlag
					.getValueAsBigDecimal()
			)
		);
		gemeindeKennzahlen.setEventPublished(true);
		persistence.merge(gemeindeKennzahlen);
	}
}
