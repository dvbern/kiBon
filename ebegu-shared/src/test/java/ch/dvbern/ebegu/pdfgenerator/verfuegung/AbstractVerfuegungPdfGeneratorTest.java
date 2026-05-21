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

package ch.dvbern.ebegu.pdfgenerator.verfuegung;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.HoehereBeitraegeTyp;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;

class AbstractVerfuegungPdfGeneratorTest extends EasyMockSupport {

	private AbstractVerfuegungPdfGenerator testee;
	private VerfuegungPdfGeneratorKonfiguration config;

	@BeforeEach
	void setUp() throws Exception {
		config = VerfuegungPdfGeneratorKonfiguration.builder().build();

		// Use partial mock to avoid complex base class initialization
		testee = EasyMock.createMockBuilder(
			AbstractVerfuegungPdfGenerator.class
		)
			.addMockedMethod("getVerfuegungZeitabschnitt") // unrelated to methods under test
			.addMockedMethod("translate", String.class) // unrelated to methods under test
			.mock();

		// Manually inject dependencies that the methods under test access directly
		// Using Reflection to set the final field
		Field field = AbstractVerfuegungPdfGenerator.class.getDeclaredField(
			"verfuegungPdfGeneratorKonfiguration"
		);
		field.setAccessible(true);
		field.set(testee, config);
	}

	@Nested
	class GetVerguenstigungAnInstitution {

		@Test
		void test_NotAuszahlungAnEltern_ReturnVerguenstigung() {
			// Arrange
			VerfuegungZeitabschnitt zeitabschnitt = mock(
				VerfuegungZeitabschnitt.class
			);
			BigDecimal expectedVerguenstigung = new BigDecimal("500.00");

			expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(false);
			expect(zeitabschnitt.getVerguenstigung()).andReturn(
				expectedVerguenstigung
			);

			replayAll();

			// Act
			BigDecimal result = testee.getVerguenstigungAnInstitution(
				zeitabschnitt
			);

			// Assert
			assertThat(result, comparesEqualTo(expectedVerguenstigung));
			verifyAll();
		}

		@Test
		void test_IsAuszahlungAnElternAndHoehererBeitragAnInstitution_ReturnHoehererBeitrag() {
			// Arrange
			VerfuegungZeitabschnitt zeitabschnitt = mock(
				VerfuegungZeitabschnitt.class
			);
			BigDecimal hoehererBeitrag = new BigDecimal("100.00");

			config.setHoehereBeitraegeTyp(
				HoehereBeitraegeTyp.AKTIVIERT_AUSZAHLUNG_INSTITUTION
			);
			expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(true);
			expect(zeitabschnitt.getHoehererBeitrag()).andReturn(
				hoehererBeitrag
			).times(2);

			replayAll();

			// Act
			BigDecimal result = testee.getVerguenstigungAnInstitution(
				zeitabschnitt
			);

			// Assert
			assertThat(result, comparesEqualTo(hoehererBeitrag));
			verifyAll();
		}

		@Test
		void test_IsAuszahlungAnElternAndNotHoehererBeitragAnInstitution_ReturnZero() {
			// Arrange
			VerfuegungZeitabschnitt zeitabschnitt = mock(
				VerfuegungZeitabschnitt.class
			);

			config.setHoehereBeitraegeTyp(HoehereBeitraegeTyp.AKTIVIERT);
			expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(true);

			replayAll();

			// Act
			BigDecimal result = testee.getVerguenstigungAnInstitution(
				zeitabschnitt
			);

			// Assert
			assertThat(result, comparesEqualTo(BigDecimal.ZERO));
			verifyAll();
		}
	}

	@Nested
	class GetVerguenstigungAnEltern {

		@Test
		void test_NotAuszahlungAnEltern_ReturnZero() {
			// Arrange
			VerfuegungZeitabschnitt zeitabschnitt = mock(
				VerfuegungZeitabschnitt.class
			);
			expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(false);

			replayAll();

			// Act
			BigDecimal result = testee.getVerguenstigungAnEltern(zeitabschnitt);

			// Assert
			assertThat(result, comparesEqualTo(BigDecimal.ZERO));
			verifyAll();
		}

		@Test
		void test_IsHoehererBeitragAnInstitution_ReturnAuszahlungsbetragMinusHoehererBeitrag() {
			// Arrange
			VerfuegungZeitabschnitt zeitabschnitt = mock(
				VerfuegungZeitabschnitt.class
			);
			BigDecimal verguenstigung = new BigDecimal("500.00");
			BigDecimal hoehererBeitrag = new BigDecimal("100.00");

			config.setHoehereBeitraegeTyp(
				HoehereBeitraegeTyp.AKTIVIERT_AUSZAHLUNG_INSTITUTION
			);
			expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(true);
			expect(zeitabschnitt.getVerguenstigung()).andReturn(verguenstigung);
			expect(zeitabschnitt.getHoehererBeitrag()).andReturn(
				hoehererBeitrag
			).times(2);

			replayAll();

			// Act
			BigDecimal result = testee.getVerguenstigungAnEltern(zeitabschnitt);

			// Assert
			assertThat(result, comparesEqualTo(new BigDecimal("400.00")));
			verifyAll();
		}

		@Test
		void test_IsNotHoehererBeitragAnInstitution_ReturnVerguenstigung() {
			// Arrange
			VerfuegungZeitabschnitt zeitabschnitt = mock(
				VerfuegungZeitabschnitt.class
			);
			BigDecimal verguenstigung = new BigDecimal("500.00");

			config.setHoehereBeitraegeTyp(HoehereBeitraegeTyp.AKTIVIERT);
			expect(zeitabschnitt.isAuszahlungAnEltern()).andReturn(true);
			expect(zeitabschnitt.getVerguenstigung()).andReturn(verguenstigung);

			replayAll();

			// Act
			BigDecimal result = testee.getVerguenstigungAnEltern(zeitabschnitt);

			// Assert
			assertThat(result, comparesEqualTo(verguenstigung));
			verifyAll();
		}
	}
}
