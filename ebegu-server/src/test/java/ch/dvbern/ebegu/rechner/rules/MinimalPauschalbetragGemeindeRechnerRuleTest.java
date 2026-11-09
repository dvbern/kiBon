/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rechner.rules;

import java.math.BigDecimal;
import java.util.Locale;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.BGRechnerParameterGemeindeDTO;
import ch.dvbern.ebegu.rechner.RechnerRuleParameterDTO;
import ch.dvbern.ebegu.rules.RuleValidity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp.KITA;
import static ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp.TAGESSCHULE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MinimalPauschalbetragGemeindeRechnerRuleTest {

	private MinimalPauschalbetragGemeindeRechnerRule rule =
		new MinimalPauschalbetragGemeindeRechnerRule(Locale.GERMAN);
	private BGRechnerParameterDTO unaktivRule = new BGRechnerParameterDTO();
	private BGRechnerParameterDTO aktivRule = new BGRechnerParameterDTO();

	@BeforeEach
	public void init() {
		BGRechnerParameterGemeindeDTO unaktivRuleGemeinde =
			new BGRechnerParameterGemeindeDTO();
		unaktivRuleGemeinde.setGemeindePauschalbetragEnabled(false);
		this.unaktivRule.setGemeindeParameter(unaktivRuleGemeinde);

		BGRechnerParameterGemeindeDTO aktivRuleGemeinde =
			new BGRechnerParameterGemeindeDTO();
		aktivRuleGemeinde.setGemeindePauschalbetragEnabled(true);
		aktivRuleGemeinde.setGemeindePauschalbetragKita(BigDecimal.TEN);
		aktivRuleGemeinde
			.setGemeindePauschalbetragMaxMassgebendenEinkommenFuerBerechnung(
				new BigDecimal(200000)
			);

		aktivRule.setGemeindeParameter(aktivRuleGemeinde);
	}

	@Test
	public void isConfigueredForGemeinde() {
		assertFalse(rule.isConfigueredForGemeinde(unaktivRule));
		assertTrue(rule.isConfigueredForGemeinde(aktivRule));
	}

	@Test
	public void isRelevantForVerfuegung() {
		assertFalse(
			rule.isRelevantForVerfuegung(
				prepareInput(KITA, new BigDecimal(260000), 0),
				aktivRule
			)
		);
		assertTrue(
			rule.isRelevantForVerfuegung(
				prepareInput(KITA, new BigDecimal(100000), 50),
				aktivRule
			)
		);
	}

	@Test
	public void isRelevantForZeroBetruungspensum() {
		BGCalculationInput input = prepareInput(
			KITA,
			new BigDecimal(100000),
			80
		);
		input.setBetreuungspensumProzent(BigDecimal.ZERO);
		assertFalse(rule.isRelevantForVerfuegung(input, aktivRule));
	}

	@Test
	public void isRelevantForVerfuegungUngueltigesAngebot() {
		assertFalse(
			rule.isRelevantForVerfuegung(
				prepareInput(TAGESSCHULE, BigDecimal.ZERO, 80),
				aktivRule
			)
		);
	}

	@Test
	public void prepareParameter() {
		RechnerRuleParameterDTO result = new RechnerRuleParameterDTO();
		// Rule inaktiv: Nichts gesetzt
		rule.prepareParameter(
			prepareInput(KITA, BigDecimal.ZERO, 0),
			unaktivRule,
			result
		);
		assertNull(result.getMinimalPauschalBetrag());
		// Rule Aktiv: 10
		rule.prepareParameter(
			prepareInput(KITA, BigDecimal.ZERO, 10),
			aktivRule,
			result
		);
		assertEquals(
			BigDecimal.TEN,
			result.getMinimalPauschalBetrag()
		);
	}

	private BGCalculationInput prepareInput(
		@Nonnull BetreuungsangebotTyp betreuungsangebotTyp,
		@Nonnull BigDecimal massgebendenEinkommen,
		int anspruchspensum
	) {
		BGCalculationInput input = new BGCalculationInput(
			new VerfuegungZeitabschnitt(),
			RuleValidity.ASIV
		);
		input.setBetreuungsangebotTyp(betreuungsangebotTyp);
		input.setMassgebendesEinkommenVorAbzugFamgr(massgebendenEinkommen);
		input.setBetreuungInGemeinde(true);
		input.setBetreuungspensumProzent(BigDecimal.TEN);
		input.setAnspruchspensumProzent(anspruchspensum);
		return input;
	}
}
