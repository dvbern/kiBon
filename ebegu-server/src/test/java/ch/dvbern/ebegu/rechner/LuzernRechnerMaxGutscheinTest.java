/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(EasyMockExtension.class)
class LuzernRechnerMaxGutscheinTest extends EasyMockSupport {

	@Test
	void testCalculateGutscheinProZeiteinheitVorZuschlagUndSelbstbehalt_GutscheinIstBegrenzt() {
		AbstractLuzernRechner luzernRechner =
			partialMockBuilder(
				KitaLuzernRechner.class
			)
				.addMockedMethod(
					"getMaximalWertBGProTagAufgrundEinkommen"
				)
				.createMock();

		BigDecimal gutscheinProTag = new BigDecimal("155.00");

		expect(luzernRechner.getMaximalWertBGProTagAufgrundEinkommen())
			.andReturn(new BigDecimal("135.00"));
		replay(luzernRechner);

		BigDecimal result = luzernRechner
			.calculateGutscheinProZeiteinheitVorZuschlagUndSelbstbehalt(
				gutscheinProTag
			);
		assertEquals(new BigDecimal("135.00"), result);
	}

	@Test
	void testCalculateGutscheinProZeiteinheitVorZuschlagUndSelbstbehalt_GutscheinIstBegrenzt_GeschwisterRabatt() {
		AbstractLuzernRechner luzernRechner =
			partialMockBuilder(
				KitaLuzernRechner.class
			).withConstructor(new ArrayList<>())
				.addMockedMethod(
					"getMinimalTarif"
				)
				.addMockedMethod("getVollkostenTarif")
				.createMock();

		expect(luzernRechner.getMinimalTarif())
			.andReturn(new BigDecimal("15.00"))
			.anyTimes();
		expect(luzernRechner.getVollkostenTarif())
			.andReturn(new BigDecimal("150.00"))
			.anyTimes();
		replayAll();

		VerfuegungZeitabschnitt verfuegungZeitabschnitt =
			new VerfuegungZeitabschnitt();
		verfuegungZeitabschnitt.getBgCalculationInputAsiv()
			.setGeschwisternBonusKind2(true);
		verfuegungZeitabschnitt.getBgCalculationInputAsiv()
			.setAnspruchspensumProzent(100);
		verfuegungZeitabschnitt.getBgCalculationInputAsiv()
			.setVerguenstigungGewuenscht(true);
		verfuegungZeitabschnitt.getBgCalculationInputAsiv()
			.setBetreuungspensumProzent(BigDecimal.valueOf(100));
		verfuegungZeitabschnitt.getBgCalculationInputAsiv()
			.setMonatlicheBetreuungskosten(BigDecimal.valueOf(2600));
		verfuegungZeitabschnitt.getBgCalculationInputAsiv()
			.setMassgebendesEinkommenVorAbzugFamgr(BigDecimal.valueOf(30000));
		verfuegungZeitabschnitt.getGueltigkeit()
			.setGueltigAb(LocalDate.of(2025, 8, 1));
		verfuegungZeitabschnitt.getGueltigkeit()
			.setGueltigBis(LocalDate.of(2025, 8, 31));

		BGRechnerParameterDTO bgRechnerParameterDTO =
			new BGRechnerParameterDTO();
		bgRechnerParameterDTO.setMaxMassgebendesEinkommen(
			new BigDecimal(200000)
		);
		bgRechnerParameterDTO.setMinMassgebendesEinkommen(
			new BigDecimal(20000)
		);

		luzernRechner.calculate(verfuegungZeitabschnitt, bgRechnerParameterDTO);

		assertEquals(
			new BigDecimal("135.00"),
			verfuegungZeitabschnitt.getBgCalculationResultAsiv()
				.getVerguenstigung()
		);
	}
}
