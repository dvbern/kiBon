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
 */

package ch.dvbern.ebegu.rules.kind;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.RuleKey;
import ch.dvbern.ebegu.rules.RuleType;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Kann die Zeitabschnitte berechnen, in denen der zusätzlich von der Gemeinde gegebene Babytarif gewährt werden kann.
 */
public class KindTarifGemeindeAbschnittRule extends
	AbstractKindTarifAbschnittRule {

	/**
	 * Erzeugt eine neue Abschnitts-Regel, die Zeitabschnitte berechnen kann, in denen ein Babytarif gewährt werden
	 * muss.
	 *
	 * @param validityPeriod Zeitraum in dem diese Regel gültig ist - i.d.R. ist das die Gesuchsperiode.
	 * @param locale Für Zahlenformate und Textausgaben zu verwendendes Locale.
	 * @param dauerBabyTarif Das maximale Alter des Kindes in <b>Monaten</b>, bis zu dem der Babytarif gewährt wird.
	 */
	public KindTarifGemeindeAbschnittRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Locale locale,
		int dauerBabyTarif
	) {
		super(
			RuleKey.KIND_TARIF_GEMEINDE,
			RuleType.GRUNDREGEL_DATA,
			RuleValidity.GEMEINDE,
			validityPeriod,
			locale
		);
		this.dauerBabyTarif = dauerBabyTarif;
	}

	/**
	 * @return Alle Angebote, für die ein BG grundsätzlich ausgestellt werden kann.
	 */
	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

	/**
	 * Prüft, ob diese Regel für eine bestimmte Gemeinde angewandt werden muss. Das ist nur dann der Fall, wenn die
	 * Gemeindeeinstellung
	 * GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED aktiviert ist.
	 *
	 * @param einstellungMap Alle für eine bestimmte Gemeinde relevanten Einstellungen.
	 * @return Ob diese Regel angewandt werden muss
	 */
	@Override
	public boolean isRelevantForGemeinde(
		@Nonnull Map<EinstellungKey, Einstellung> einstellungMap
	) {
		Einstellung kindTarifEnabledProp = einstellungMap.get(
			EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED
		);
		if (null == kindTarifEnabledProp) {
			throw new IllegalArgumentException(
				"Die Einstellung \"GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED\" in der Parameter-Map fehlt oder ist \"null\"."
			);
		}

		return kindTarifEnabledProp.getValueAsBoolean();
	}

	@Override
	VerfuegungZeitabschnitt createZeitabschnitt(
		@Nonnull DateRange gueltigkeit,
		boolean baby,
		@Nonnull EinschulungTyp einschulungTyp
	) {
		final VerfuegungZeitabschnitt verfuegungZeitabschnitt =
			createZeitabschnittWithinValidityPeriodOfRule(gueltigkeit);
		verfuegungZeitabschnitt.setZusaetzlicherBabyGutscheinForAsivAndGemeinde(
			baby
		);

		return verfuegungZeitabschnitt;
	}
}
