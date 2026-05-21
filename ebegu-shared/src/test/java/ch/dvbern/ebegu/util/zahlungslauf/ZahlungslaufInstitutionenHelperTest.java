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

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.HoehereBeitraegeTyp;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(EasyMockExtension.class)
public class ZahlungslaufInstitutionenHelperTest extends EasyMockSupport {

	@Test
	public void testIsAuszuzahlen_AuszahlungAnElternTrueOrFalseAndHoehereBeitraegeDeaktiviert_IsAuszuzahlenBecomesTheOppositeOfAuszahlungAnEltern() {
		ZahlungslaufInstitutionenHelper helper =
			new ZahlungslaufInstitutionenHelper(
				HoehereBeitraegeTyp.DEAKTIVIERT
			);
		VerfuegungZeitabschnitt zeitabschnitt = mock(
			VerfuegungZeitabschnitt.class
		);

		// Case 1: isAuszahlungAnEltern = true -> isAuszuzahlen = false
		expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(true);
		replayAll();
		assertFalse(helper.isAuszuzahlen(zeitabschnitt));
		verifyAll();
		resetAll();

		// Case 2: isAuszahlungAnEltern = false -> isAuszuzahlen = true
		expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(false);
		replayAll();
		assertTrue(helper.isAuszuzahlen(zeitabschnitt));
		verifyAll();
	}

	@Test
	public void testIsAuszuzahlen_AuszahlungInstitutionEnabled_IsAuszuzahlenIsTrue() {
		ZahlungslaufInstitutionenHelper helper =
			new ZahlungslaufInstitutionenHelper(
				HoehereBeitraegeTyp.AKTIVIERT_AUSZAHLUNG_INSTITUTION
			);
		VerfuegungZeitabschnitt zeitabschnitt = mock(
			VerfuegungZeitabschnitt.class
		);

		// Gutschein goes to institution, so it's always true regardless of HoehererBeitrag
		expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(false);
		replayAll();
		assertTrue(helper.isAuszuzahlen(zeitabschnitt));
		verifyAll();
	}

	@Test
	public void testIsAuszuzahlen_AuszahlungInstitutionEnabledAndAuszahlungAnElternWanted_IsAuszuzahlenIsTrue() {
		ZahlungslaufInstitutionenHelper helper =
			new ZahlungslaufInstitutionenHelper(
				HoehereBeitraegeTyp.AKTIVIERT_AUSZAHLUNG_INSTITUTION
			);
		VerfuegungZeitabschnitt zeitabschnitt = mock(
			VerfuegungZeitabschnitt.class
		);

		// Gutschein goes to parents, but hoehererBeitrag > 0 goes to institution
		expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(true);
		expect(zeitabschnitt.getHoehererBeitrag()).andReturn(
			new BigDecimal("10.00")
		).anyTimes();
		replayAll();
		assertTrue(helper.isAuszuzahlen(zeitabschnitt));
		verifyAll();
	}

	@Test
	public void testIsAuszuzahlen_AuszahlungInstitutionEnabledAndAuszahlungAnElternWantedButNoHoehereBeitraegeExist_IsAuszuzahlenIsTrue() {
		// Höhere Beiträge must be annotated to be paid out, event if their value results to zero.
		ZahlungslaufInstitutionenHelper helper =
			new ZahlungslaufInstitutionenHelper(
				HoehereBeitraegeTyp.AKTIVIERT_AUSZAHLUNG_INSTITUTION
			);
		VerfuegungZeitabschnitt zeitabschnitt = mock(
			VerfuegungZeitabschnitt.class
		);

		// Gutschein goes to parents, and hoehererBeitrag is null or 0
		expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(true);
		expect(zeitabschnitt.getHoehererBeitrag()).andReturn(null).anyTimes();
		replayAll();
		assertFalse(helper.isAuszuzahlen(zeitabschnitt));
		verifyAll();
		resetAll();

		expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(true);
		expect(zeitabschnitt.getHoehererBeitrag()).andReturn(BigDecimal.ZERO)
			.anyTimes();
		replayAll();
		assertTrue(helper.isAuszuzahlen(zeitabschnitt));
		verifyAll();
	}

	@Test
	public void testGetAuszahlungsbetrag_HoehereBeitraegeDeaktiviert_NoZahlungspositionCreated() {
		ZahlungslaufInstitutionenHelper helper =
			new ZahlungslaufInstitutionenHelper(
				HoehereBeitraegeTyp.DEAKTIVIERT
			);
		VerfuegungZeitabschnitt zeitabschnitt = mock(
			VerfuegungZeitabschnitt.class
		);
		BigDecimal verguenstigung = new BigDecimal("150.00");

		expect(zeitabschnitt.getVerguenstigung()).andReturn(verguenstigung);
		expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(false);
		replayAll();
		BigDecimal result = helper.getAuszahlungsbetrag(zeitabschnitt);
		verifyAll();

		assertEquals(0, verguenstigung.compareTo(result));
	}

	@Test
	public void testGetAuszahlungsbetrag_AuszahlungInstitutionEnabled_ZahlungspostionWithHoehererBeitragCreated() {
		ZahlungslaufInstitutionenHelper helper =
			new ZahlungslaufInstitutionenHelper(
				HoehereBeitraegeTyp.AKTIVIERT_AUSZAHLUNG_INSTITUTION
			);
		VerfuegungZeitabschnitt zeitabschnitt = mock(
			VerfuegungZeitabschnitt.class
		);
		BigDecimal hoehererBeitrag = new BigDecimal("75.00");

		expect(zeitabschnitt.getHoehererBeitrag()).andReturn(hoehererBeitrag)
			.anyTimes();
		expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(true)
			.anyTimes();
		replayAll();
		BigDecimal result = helper.getAuszahlungsbetrag(zeitabschnitt);
		verifyAll();

		assertEquals(0, hoehererBeitrag.compareTo(result));
	}

	@Test
	public void testGetAuszahlungsbetrag_AuszahlungInstitutionEnabled_ZahlungspositionWithBGCreated() {
		// Note: this helper does not decide what to pay out to whom.
		// It will just always pay höhere Beiträge only or everything.
		ZahlungslaufInstitutionenHelper helper =
			new ZahlungslaufInstitutionenHelper(
				HoehereBeitraegeTyp.AKTIVIERT_AUSZAHLUNG_INSTITUTION
			);
		VerfuegungZeitabschnitt zeitabschnitt = mock(
			VerfuegungZeitabschnitt.class
		);
		BigDecimal verguenstigung = new BigDecimal("200.00");

		expect(zeitabschnitt.getHoehererBeitrag()).andReturn(null).anyTimes();
		expect(zeitabschnitt.getVerguenstigung()).andReturn(verguenstigung);
		expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(false);
		replayAll();
		BigDecimal result = helper.getAuszahlungsbetrag(zeitabschnitt);
		verifyAll();

		assertEquals(0, verguenstigung.compareTo(result));
	}
}
