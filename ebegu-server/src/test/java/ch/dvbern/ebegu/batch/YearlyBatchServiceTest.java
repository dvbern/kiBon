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

package ch.dvbern.ebegu.batch;

import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.services.gemeindeantrag.GemeindeKennzahlenMailService;
import ch.dvbern.ebegu.services.gemeindeantrag.GemeindeKennzahlenService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class YearlyBatchServiceTest {

	@Mock
	GemeindeKennzahlenService gemeindeKennzahlenService;

	@Mock
	GemeindeKennzahlenMailService gemeindeKennzahlenMailService;

	@Mock
	ApplicationPropertyService applicationPropertyService;

	@InjectMocks
	YearlyBatchService yearlyBatchService;

	@Nested
	class createGemeindeKennzahlenForCurrentGPForAllActiveGemeindenAndSendReminder {
		@Test
		void shouldReturnWhenFeatureDisabled() {
			Mandant mandant = new Mandant();
			when(
				applicationPropertyService.isGemeindeKennzahlenAktiviert(
					mandant
				)
			)
				.thenReturn(false);

			yearlyBatchService
				.createGemeindeKennzahlenForCurrentGPForAllActiveGemeindenAndSendReminder(
					mandant
				);

			verifyNoInteractions(gemeindeKennzahlenService);
			verifyNoInteractions(gemeindeKennzahlenMailService);
		}

		@Test
		void shouldNotSendMailWhenMailDisabled() {
			Mandant mandant = new Mandant();
			when(
				applicationPropertyService.isGemeindeKennzahlenAktiviert(
					mandant
				)
			)
				.thenReturn(true);
			when(
				applicationPropertyService
					.isReminderGemeindeKennzahlenAktiviert(mandant)
			)
				.thenReturn(false);

			yearlyBatchService
				.createGemeindeKennzahlenForCurrentGPForAllActiveGemeindenAndSendReminder(
					mandant
				);
			verify(gemeindeKennzahlenService, Mockito.times(1))
				.createGemeindeKennzahlenInCurrentGPForActiveBGGemeinden(
					mandant
				);
			verifyNoInteractions(gemeindeKennzahlenMailService);
		}

		@Test
		void shouldSendMailToAllMatchingUsers() {
			Mandant mandant = new Mandant();
			when(
				applicationPropertyService.isGemeindeKennzahlenAktiviert(
					mandant
				)
			)
				.thenReturn(true);
			when(
				applicationPropertyService
					.isReminderGemeindeKennzahlenAktiviert(mandant)
			)
				.thenReturn(true);

			yearlyBatchService
				.createGemeindeKennzahlenForCurrentGPForAllActiveGemeindenAndSendReminder(
					mandant
				);
			verify(gemeindeKennzahlenService, Mockito.times(1))
				.createGemeindeKennzahlenInCurrentGPForActiveBGGemeinden(
					mandant
				);
			verify(gemeindeKennzahlenMailService, Mockito.times(1))
				.sendFirstErinnerungsmailToAllAdminBGOfMandant(mandant);
		}
	}

	@Nested
	class sendGemeindeKennzahlenSecondReminder {
		@Test
		void shouldReturnWhenFeatureDisabled() {
			Mandant mandant = new Mandant();
			when(
				applicationPropertyService.isGemeindeKennzahlenAktiviert(
					mandant
				)
			)
				.thenReturn(false);

			yearlyBatchService.sendGemeindeKennzahlenSecondReminder(mandant);
			verifyNoInteractions(gemeindeKennzahlenMailService);
		}

		@Test
		void shouldNotSendMailWhenMailDisabled() {
			Mandant mandant = new Mandant();
			when(
				applicationPropertyService.isGemeindeKennzahlenAktiviert(
					mandant
				)
			)
				.thenReturn(true);
			when(
				applicationPropertyService
					.isReminderGemeindeKennzahlenAktiviert(mandant)
			)
				.thenReturn(false);

			yearlyBatchService.sendGemeindeKennzahlenSecondReminder(mandant);
			verifyNoInteractions(gemeindeKennzahlenMailService);
		}

		@Test
		void shouldSendMailToAllMatchingUsers() {
			Mandant mandant = new Mandant();
			when(
				applicationPropertyService.isGemeindeKennzahlenAktiviert(
					mandant
				)
			)
				.thenReturn(true);
			when(
				applicationPropertyService
					.isReminderGemeindeKennzahlenAktiviert(mandant)
			)
				.thenReturn(true);

			yearlyBatchService.sendGemeindeKennzahlenSecondReminder(mandant);
			verify(gemeindeKennzahlenMailService, Mockito.times(1))
				.sendSecondErinnerungsmailToAllAdminBGOfMandant(mandant);
		}
	}
}
