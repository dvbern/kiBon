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

package ch.dvbern.ebegu.pdfgenerator;

import java.util.Collections;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.GemeindeStammdatenKorrespondenz;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.GesuchTypFromAngebotTyp;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FreigabequittungPdfGeneratorTest {

	private Gesuch gesuch;
	private GemeindeStammdaten stammdaten;
	private GemeindeStammdatenKorrespondenz korrespondenz;
	private FreigabequittungPdfGenerator generator;

	@BeforeEach
	void setUp() {
		gesuch = mock(Gesuch.class);
		stammdaten = mock(GemeindeStammdaten.class);
		korrespondenz = mock(GemeindeStammdatenKorrespondenz.class);
		Gemeinde gemeinde = mock(Gemeinde.class);
		Mandant mandant = mock(Mandant.class);
		Adresse adresse = mock(Adresse.class);

		// Setup nested mocks for the constructor and the method under test
		when(stammdaten.getGemeinde()).thenReturn(gemeinde);
		when(gemeinde.getMandant()).thenReturn(mandant);
		when(mandant.getMandantIdentifier()).thenReturn(MandantIdentifier.BERN);
		when(stammdaten.getKorrespondenzsprache()).thenReturn(
			KorrespondenzSpracheTyp.DE
		);
		when(stammdaten.getAdresseForGesuch(gesuch)).thenReturn(adresse);
		when(adresse.getAddressAsString()).thenReturn("Test Address");
		when(adresse.getOrt()).thenReturn("Test Ort");
		when(stammdaten.getGemeindeStammdatenKorrespondenz()).thenReturn(
			korrespondenz
		);
		when(korrespondenz.getLogoContent()).thenReturn(new byte[0]);
		when(korrespondenz.getAlternativesLogoTagesschuleContent()).thenReturn(
			new byte[0]
		);
		when(korrespondenz.getBarcodeSpacingTop()).thenReturn(10);
		when(korrespondenz.getBarcodeSpacingLeft()).thenReturn(10);
		when(korrespondenz.getLogoSpacingTop()).thenReturn(10);
		when(korrespondenz.getLogoSpacingLeft()).thenReturn(10);
		when(korrespondenz.getSenderAddressSpacingTop()).thenReturn(10);
		when(korrespondenz.getSenderAddressSpacingLeft()).thenReturn(10);
		when(korrespondenz.getReceiverAddressSpacingTop()).thenReturn(10);
		when(korrespondenz.getReceiverAddressSpacingLeft()).thenReturn(10);

		// Initialize the generator with mocked dependencies
		generator = new FreigabequittungPdfGenerator(
			gesuch,
			stammdaten,
			Collections.emptyList()
		);
	}

	@Test
	void testUseAlternativeLogoIfPresent_OnlyTS_WithLogo() {
		// Only TS and Logo present
		when(gesuch.calculateGesuchTypFromAngebotTyp()).thenReturn(GesuchTypFromAngebotTyp.TS_GESUCH);
		when(korrespondenz.getAlternativesLogoTagesschuleContent()).thenReturn(new byte[]{ 1, 2, 3 });

		boolean useAlternative = generator.useAlternativeLogoIfPresent();
		assertTrue(useAlternative);
	}

	@Test
	void testUseAlternativeLogoIfPresent_OnlyTS_NoLogo() {
		// Only TS but NO Logo present
		when(gesuch.calculateGesuchTypFromAngebotTyp()).thenReturn(GesuchTypFromAngebotTyp.TS_GESUCH);
		when(korrespondenz.getAlternativesLogoTagesschuleContent()).thenReturn(new byte[0]);

		assertFalse(generator.useAlternativeLogoIfPresent());
	}

	@Test
	void testUseAlternativeLogoIfPresent_Mixed_WithLogo() {
		// Mixed Gesuch and Logo present
		when(gesuch.calculateGesuchTypFromAngebotTyp()).thenReturn(GesuchTypFromAngebotTyp.MISCH_GESUCH);
		when(korrespondenz.getAlternativesLogoTagesschuleContent()).thenReturn(new byte[]{ 1, 2, 3 });

		assertFalse(generator.useAlternativeLogoIfPresent());
	}

	@Test
	void testUseAlternativeLogoIfPresent_BGOnly_WithLogo() {
		// BG Only and Logo present
		when(gesuch.calculateGesuchTypFromAngebotTyp()).thenReturn(GesuchTypFromAngebotTyp.BG_GESUCH);
		when(korrespondenz.getAlternativesLogoTagesschuleContent()).thenReturn(new byte[]{ 1, 2, 3 });

		assertFalse(generator.useAlternativeLogoIfPresent());
	}
}
