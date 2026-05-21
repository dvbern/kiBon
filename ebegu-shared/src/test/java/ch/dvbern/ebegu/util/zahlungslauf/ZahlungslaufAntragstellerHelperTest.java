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

package ch.dvbern.ebegu.util.zahlungslauf;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.HoehereBeitraegeTyp;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(EasyMockExtension.class)
public class ZahlungslaufAntragstellerHelperTest extends EasyMockSupport {

	@Test
	public void testGetAuszahlungsbetrag_BGisZeroForZeitabschnitt_NoZahlungspostionCreated() {
		ZahlungslaufAntragstellerHelper helper =
			new ZahlungslaufAntragstellerHelper(
				HoehereBeitraegeTyp.DEAKTIVIERT
			);
		VerfuegungZeitabschnitt zeitabschnitt = mock(
			VerfuegungZeitabschnitt.class
		);
		Verfuegung verfuegung = mock(Verfuegung.class);
		AbstractPlatz platz = mock(AbstractPlatz.class);
		Gesuch gesuch = mock(Gesuch.class);
		Familiensituation familiensituation = mock(Familiensituation.class);

		expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(false);
		expect(zeitabschnitt.getVerfuegung()).andReturn(verfuegung).anyTimes();
		expect(verfuegung.getPlatz()).andReturn(platz).anyTimes();
		expect(platz.extractGesuch()).andReturn(gesuch).anyTimes();
		expect(gesuch.extractFamiliensituation()).andReturn(familiensituation)
			.anyTimes();
		expect(familiensituation.isKeineMahlzeitenverguenstigungBeantragt())
			.andReturn(true);

		replayAll();
		BigDecimal result = helper.getAuszahlungsbetrag(zeitabschnitt);
		verifyAll();

		assertEquals(0, BigDecimal.ZERO.compareTo(result));
	}

	@Test
	public void testGetAuszahlungsbetrag_AuszahlungAnElternWanted_ZahlungspositionForBGCreated() {
		ZahlungslaufAntragstellerHelper helper =
			new ZahlungslaufAntragstellerHelper(
				HoehereBeitraegeTyp.DEAKTIVIERT
			);
		VerfuegungZeitabschnitt zeitabschnitt = mock(
			VerfuegungZeitabschnitt.class
		);
		Verfuegung verfuegung = mock(Verfuegung.class);
		AbstractPlatz platz = mock(AbstractPlatz.class);
		Gesuch gesuch = mock(Gesuch.class);
		Familiensituation familiensituation = mock(Familiensituation.class);

		BigDecimal gutscheinAmount = new BigDecimal("100.00");

		expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(true);
		expect(zeitabschnitt.getVerguenstigung()).andReturn(gutscheinAmount);

		expect(zeitabschnitt.getVerfuegung()).andReturn(verfuegung).anyTimes();
		expect(verfuegung.getPlatz()).andReturn(platz).anyTimes();
		expect(platz.extractGesuch()).andReturn(gesuch).anyTimes();
		expect(gesuch.extractFamiliensituation()).andReturn(familiensituation)
			.anyTimes();
		expect(familiensituation.isKeineMahlzeitenverguenstigungBeantragt())
			.andReturn(true);

		replayAll();
		BigDecimal result = helper.getAuszahlungsbetrag(zeitabschnitt);
		verifyAll();

		assertEquals(0, gutscheinAmount.compareTo(result));
	}

	@Test
	public void testGetAuszahlungsbetrag_AuszahlungAnElternWantedAndHoehereBeitraegeAusbezahltAnInstitution_ZahlungspositionOnlyForBGCreated() {
		ZahlungslaufAntragstellerHelper helper =
			new ZahlungslaufAntragstellerHelper(
				HoehereBeitraegeTyp.AKTIVIERT_AUSZAHLUNG_INSTITUTION
			);
		VerfuegungZeitabschnitt zeitabschnitt = mock(
			VerfuegungZeitabschnitt.class
		);
		Verfuegung verfuegung = mock(Verfuegung.class);
		AbstractPlatz platz = mock(AbstractPlatz.class);
		Gesuch gesuch = mock(Gesuch.class);
		Familiensituation familiensituation = mock(Familiensituation.class);

		BigDecimal gutscheinAmount = new BigDecimal("500.00");
		BigDecimal hoehererBeitrag = new BigDecimal("50.00");

		expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(true);
		expect(zeitabschnitt.getVerguenstigung()).andReturn(gutscheinAmount);
		expect(zeitabschnitt.getHoehererBeitrag()).andReturn(hoehererBeitrag)
			.anyTimes();

		expect(zeitabschnitt.getVerfuegung()).andReturn(verfuegung).anyTimes();
		expect(verfuegung.getPlatz()).andReturn(platz).anyTimes();
		expect(platz.extractGesuch()).andReturn(gesuch).anyTimes();
		expect(gesuch.extractFamiliensituation()).andReturn(familiensituation)
			.anyTimes();
		expect(familiensituation.isKeineMahlzeitenverguenstigungBeantragt())
			.andReturn(true);

		replayAll();
		BigDecimal result = helper.getAuszahlungsbetrag(zeitabschnitt);
		verifyAll();

		assertEquals(0, new BigDecimal("450.00").compareTo(result));
	}

	@Test
	public void testGetAuszahlungsbetrag_HasHoehereBeitraegeButFeatureDisabled_ZahlungspositionOnlyForBGAndHoehereBeitraegeCreated() {
		// If HoehereBeitraegeTyp.DEAKTIVIERT we can not have "höhere Beiträge" at all.
		// But because this setting has not been used within the helper before, we just use the default behavior.
		ZahlungslaufAntragstellerHelper helper =
			new ZahlungslaufAntragstellerHelper(
				HoehereBeitraegeTyp.DEAKTIVIERT
			);
		VerfuegungZeitabschnitt zeitabschnitt = mock(
			VerfuegungZeitabschnitt.class
		);
		Verfuegung verfuegung = mock(Verfuegung.class);
		AbstractPlatz platz = mock(AbstractPlatz.class);
		Gesuch gesuch = mock(Gesuch.class);
		Familiensituation familiensituation = mock(Familiensituation.class);
		BGCalculationResult bgResult = mock(BGCalculationResult.class);

		BigDecimal gutscheinAmount = new BigDecimal("100.00");
		BigDecimal mzvAmount = new BigDecimal("25.50");

		expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(true);
		expect(zeitabschnitt.getVerguenstigung()).andReturn(gutscheinAmount);

		expect(zeitabschnitt.getVerfuegung()).andReturn(verfuegung).anyTimes();
		expect(verfuegung.getPlatz()).andReturn(platz).anyTimes();
		expect(platz.extractGesuch()).andReturn(gesuch).anyTimes();
		expect(gesuch.extractFamiliensituation()).andReturn(familiensituation)
			.anyTimes();
		expect(familiensituation.isKeineMahlzeitenverguenstigungBeantragt())
			.andReturn(false);

		expect(zeitabschnitt.getRelevantBgCalculationResult()).andReturn(
			bgResult
		);
		expect(bgResult.getVerguenstigungMahlzeitenTotal()).andReturn(
			mzvAmount
		);

		replayAll();
		BigDecimal result = helper.getAuszahlungsbetrag(zeitabschnitt);
		verifyAll();

		assertEquals(0, new BigDecimal("125.50").compareTo(result));
	}

	@Test
	public void testGetAuszahlungsbetrag_HasMZV_ZahlungspositionForMZVCreated() {
		ZahlungslaufAntragstellerHelper helper =
			new ZahlungslaufAntragstellerHelper(
				HoehereBeitraegeTyp.DEAKTIVIERT
			);
		VerfuegungZeitabschnitt zeitabschnitt = mock(
			VerfuegungZeitabschnitt.class
		);
		Verfuegung verfuegung = mock(Verfuegung.class);
		AbstractPlatz platz = mock(AbstractPlatz.class);
		Gesuch gesuch = mock(Gesuch.class);
		Familiensituation familiensituation = mock(Familiensituation.class);
		BGCalculationResult bgResult = mock(BGCalculationResult.class);

		BigDecimal mzvAmount = new BigDecimal("30.00");

		expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(false);

		expect(zeitabschnitt.getVerfuegung()).andReturn(verfuegung).anyTimes();
		expect(verfuegung.getPlatz()).andReturn(platz).anyTimes();
		expect(platz.extractGesuch()).andReturn(gesuch).anyTimes();
		expect(gesuch.extractFamiliensituation()).andReturn(familiensituation)
			.anyTimes();
		expect(familiensituation.isKeineMahlzeitenverguenstigungBeantragt())
			.andReturn(false);

		expect(zeitabschnitt.getRelevantBgCalculationResult()).andReturn(
			bgResult
		);
		expect(bgResult.getVerguenstigungMahlzeitenTotal()).andReturn(
			mzvAmount
		);

		replayAll();
		BigDecimal result = helper.getAuszahlungsbetrag(zeitabschnitt);
		verifyAll();

		assertEquals(0, mzvAmount.compareTo(result));
	}
}
