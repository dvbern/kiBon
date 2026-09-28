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

package ch.dvbern.ebegu.services;

import java.time.LocalDateTime;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class VersendeteMailsServiceTest {

	private static final LocalDateTime CUTOFF = LocalDateTime.of(
		2025,
		7,
		31,
		0,
		0
	);
	private static final MandantIdentifier MANDANT = MandantIdentifier.BERN;

	@Test
	void asyncDelete_delegatesToSyncDelete_withSameArgs() {
		VersendeteMailsService service = EasyMock
			.partialMockBuilder(VersendeteMailsService.class)
			.addMockedMethod("deleteVersendeteMailsBefore")
			.createMock();

		EasyMock
			.expect(service.deleteVersendeteMailsBefore(CUTOFF, MANDANT))
			.andReturn(42);
		EasyMock.replay(service);

		service.deleteVersendeteMailsBeforeAsync(CUTOFF, MANDANT);

		EasyMock.verify(service);
	}

	@Test
	void asyncDelete_dontShowRuntimeException() {
		VersendeteMailsService service = EasyMock
			.partialMockBuilder(VersendeteMailsService.class)
			.addMockedMethod("deleteVersendeteMailsBefore")
			.createMock();

		EasyMock
			.expect(service.deleteVersendeteMailsBefore(CUTOFF, MANDANT))
			.andThrow(new RuntimeException("boom"));
		EasyMock.replay(service);

		assertDoesNotThrow(
			() -> service.deleteVersendeteMailsBeforeAsync(CUTOFF, MANDANT)
		);

		EasyMock.verify(service);
	}
}
