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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rules.RuleKey;
import ch.dvbern.ebegu.rules.RuleType;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Bis 12 Monate gilt der BabyTarif (1.5), danach der "Normaltarif" (1.0) bis zum Schuleintritt, nach Schuleintritt 0.75
 */
@SuppressWarnings("MethodParameterNamingConvention")
public class KindTarifAbschnittRule extends AbstractKindTarifAbschnittRule {

	public KindTarifAbschnittRule(
		DateRange validityPeriod,
		@Nonnull Locale locale,
		long dauerBabyTarif
	) {
		super(
			RuleKey.KIND_TARIF,
			RuleType.GRUNDREGEL_DATA,
			RuleValidity.ASIV,
			validityPeriod,
			locale
		);
		this.dauerBabyTarif = dauerBabyTarif;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBetreuungsgutscheinTypes();
	}

	@Override
	VerfuegungZeitabschnitt createZeitabschnitt(
		@Nonnull DateRange gueltigkeit,
		boolean baby,
		@Nonnull EinschulungTyp einschulungTyp
	) {
		final VerfuegungZeitabschnitt verfuegungZeitabschnitt =
			createZeitabschnittWithinValidityPeriodOfRule(gueltigkeit);
		verfuegungZeitabschnitt.setBabyTarifForAsivAndGemeinde(baby);
		verfuegungZeitabschnitt.setEinschulungTypForAsivAndGemeinde(
			einschulungTyp
		);
		return verfuegungZeitabschnitt;
	}
}
