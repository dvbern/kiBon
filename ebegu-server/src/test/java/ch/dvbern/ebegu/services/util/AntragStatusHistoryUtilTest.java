/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AntragStatusHistory;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.enums.AntragStatus.BESCHWERDE_HAENGIG;
import static ch.dvbern.ebegu.enums.AntragStatus.GEPRUEFT;
import static ch.dvbern.ebegu.enums.AntragStatus.GEPRUEFT_STV;
import static ch.dvbern.ebegu.enums.AntragStatus.IN_BEARBEITUNG_JA;
import static ch.dvbern.ebegu.enums.AntragStatus.PRUEFUNG_STV;
import static ch.dvbern.ebegu.enums.AntragStatus.VERFUEGEN;
import static ch.dvbern.ebegu.enums.AntragStatus.VERFUEGT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AntragStatusHistoryUtilTest {

	@Test
	public void findLastStatusChangeBeforeBeschwerde_normalfall() {
		final List<AntragStatusHistory> allStatus = prepareStatusListDesc(
			IN_BEARBEITUNG_JA,
			GEPRUEFT,
			VERFUEGEN,
			VERFUEGT,
			BESCHWERDE_HAENGIG
		);
		final AntragStatusHistory lastStatus = AntragStatusHistoryUtil
			.findLastStatusChangeBeforeBeschwerde(allStatus, "Normalfall");
		assertNotNull(lastStatus);
		assertEquals(VERFUEGT, lastStatus.getStatus());
	}

	@Test
	public void findLastStatusChangeBeforeBeschwerde_falscherStartStatus() {
		assertThrows(EbeguRuntimeException.class, () -> {
			final List<AntragStatusHistory> allStatus = prepareStatusListDesc(
				IN_BEARBEITUNG_JA,
				GEPRUEFT,
				VERFUEGEN,
				VERFUEGT
			);
			final AntragStatusHistory lastStatus = AntragStatusHistoryUtil
				.findLastStatusChangeBeforeBeschwerde(
					allStatus,
					"Aktuell nicht Beschwerde"
				);
			assertNotNull(lastStatus);
			assertEquals(VERFUEGT, lastStatus.getStatus());
		});
	}

	@Test
	public void findLastStatusChangeBeforeBeschwerde_keineHistory() {
		assertThrows(EbeguRuntimeException.class, () -> {
			final List<AntragStatusHistory> allStatus = prepareStatusListDesc();
			final AntragStatusHistory lastStatus = AntragStatusHistoryUtil
				.findLastStatusChangeBeforeBeschwerde(
					allStatus,
					"KeineHistory"
				);
			assertNotNull(lastStatus);
			assertEquals(VERFUEGT, lastStatus.getStatus());
		});
	}

	@Test
	public void findLastStatusChangeBeforeBeschwerde_keinVorgaengerStatusGefunden() {
		assertThrows(EbeguRuntimeException.class, () -> {
			final List<AntragStatusHistory> allStatus = prepareStatusListDesc(
				BESCHWERDE_HAENGIG
			);
			final AntragStatusHistory lastStatus = AntragStatusHistoryUtil
				.findLastStatusChangeBeforeBeschwerde(
					allStatus,
					"KeineHistory"
				);
			assertNotNull(lastStatus);
			assertEquals(VERFUEGT, lastStatus.getStatus());
		});
	}

	@Test
	public void findLastStatusChangeBeforePruefungSTV_normalfall() {
		final List<AntragStatusHistory> allStatus = prepareStatusListDesc(
			IN_BEARBEITUNG_JA,
			GEPRUEFT,
			VERFUEGEN,
			VERFUEGT,
			PRUEFUNG_STV
		);
		final AntragStatusHistory lastStatus = AntragStatusHistoryUtil
			.findLastStatusChangeBeforePruefungSTV(allStatus, "Normalfall");
		assertNotNull(lastStatus);
		assertEquals(VERFUEGT, lastStatus.getStatus());
	}

	@Test
	public void findLastStatusChangeBeforePruefungSTV_zwischendurchBeschwerde() {
		final List<AntragStatusHistory> allStatus = prepareStatusListDesc(
			IN_BEARBEITUNG_JA,
			GEPRUEFT,
			VERFUEGEN,
			VERFUEGT,
			PRUEFUNG_STV,
			BESCHWERDE_HAENGIG,
			PRUEFUNG_STV
		);
		final AntragStatusHistory lastStatus = AntragStatusHistoryUtil
			.findLastStatusChangeBeforePruefungSTV(
				allStatus,
				"Zwischendurch Beschwerde"
			);
		assertNotNull(lastStatus);
		assertEquals(VERFUEGT, lastStatus.getStatus());
	}

	@Test
	public void findLastStatusChangeBeforePruefungSTV_mehrfachePruefung() {
		final List<AntragStatusHistory> allStatus = prepareStatusListDesc(
			IN_BEARBEITUNG_JA,
			GEPRUEFT,
			VERFUEGEN,
			VERFUEGT,
			PRUEFUNG_STV,
			GEPRUEFT_STV,
			BESCHWERDE_HAENGIG,
			PRUEFUNG_STV
		);
		final AntragStatusHistory lastStatus = AntragStatusHistoryUtil
			.findLastStatusChangeBeforePruefungSTV(
				allStatus,
				"Mehrfache Pruefung STV"
			);
		assertNotNull(lastStatus);
		assertEquals(BESCHWERDE_HAENGIG, lastStatus.getStatus());
	}

	@Test
	public void findLastStatusChangeBeforePruefungSTV_falscherStartStatus() {
		assertThrows(EbeguRuntimeException.class, () -> {
			final List<AntragStatusHistory> allStatus = prepareStatusListDesc(
				IN_BEARBEITUNG_JA,
				GEPRUEFT,
				VERFUEGEN,
				VERFUEGT
			);
			final AntragStatusHistory lastStatus = AntragStatusHistoryUtil
				.findLastStatusChangeBeforePruefungSTV(
					allStatus,
					"AktuellNichtInPruefungSTV"
				);
			assertNotNull(lastStatus);
			assertEquals(VERFUEGT, lastStatus.getStatus());
		});
	}

	@Test
	public void findLastStatusChangeBeforePruefungSTV_keineHistory() {
		assertThrows(EbeguRuntimeException.class, () -> {
			final List<AntragStatusHistory> allStatus = prepareStatusListDesc();
			final AntragStatusHistory lastStatus = AntragStatusHistoryUtil
				.findLastStatusChangeBeforePruefungSTV(
					allStatus,
					"KeineHistory"
				);
			assertNotNull(lastStatus);
			assertEquals(VERFUEGT, lastStatus.getStatus());
		});
	}

	@Test
	public void findLastStatusChangeBeforePruefungSTV_keinVorgaengerStatusGefunden() {
		assertThrows(EbeguRuntimeException.class, () -> {
			final List<AntragStatusHistory> allStatus = prepareStatusListDesc(
				PRUEFUNG_STV
			);
			final AntragStatusHistory lastStatus = AntragStatusHistoryUtil
				.findLastStatusChangeBeforePruefungSTV(
					allStatus,
					"KeineHistory"
				);
			assertNotNull(lastStatus);
			assertEquals(VERFUEGT, lastStatus.getStatus());
		});
	}

	private List<AntragStatusHistory> prepareStatusListDesc(
		@Nonnull AntragStatus... statusListAufsteigend
	) {
		List<AntragStatusHistory> allStatus = new LinkedList<>();
		for (AntragStatus antragStatus : statusListAufsteigend) {
			AntragStatusHistory history = new AntragStatusHistory();
			history.setStatus(antragStatus);
			allStatus.add(history);
		}
		Collections.reverse(allStatus);
		return allStatus;
	}
}
