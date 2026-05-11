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

package ch.dvbern.ebegu.outbox.gemeinde;

import java.time.LocalDate;
import java.util.Map;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.gemeinde.GemeindeKonfigurationService;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.kibon.exchange.commons.gemeinde.GemeindeEventDTO;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(EasyMockExtension.class)
public class GemeindeEventConverterTest extends EasyMockSupport {

	@TestSubject
	private final GemeindeEventConverter gemeindeEventConverter =
		new GemeindeEventConverter();

	@Mock
	private GemeindeKonfigurationService gemeindeKonfigurationService;

	@Test
	public void testChangedEvent() {
		Gemeinde gemeinde = new Gemeinde();
		gemeinde.setId("de0fa334-348d-11ef-9502-ef08ef17c01a");
		gemeinde.setName("Test");
		gemeinde.setBetreuungsgutscheineStartdatum(LocalDate.of(2022, 12, 12));
		gemeinde.setBfsNummer(123L);
		gemeinde.setGueltigBis(LocalDate.of(9999, 12, 31));
		gemeinde.setMandant(new Mandant());
		gemeinde.getMandant().setMandantIdentifier(MandantIdentifier.BERN);

		expect(
			gemeindeKonfigurationService
				.loadEinstellungenOfGPRelevantForGemeinde(gemeinde)
		).andReturn(Map.of());

		replayAll();

		GemeindeChangedEvent gemeindeChangedEvent = gemeindeEventConverter.of(
			gemeinde
		);

		//noinspection deprecation
		GemeindeEventDTO specificRecord = AvroConverter.fromAvroBinary(
			gemeindeChangedEvent.getSchema(),
			gemeindeChangedEvent.getPayload()
		);

		assertThat(
			specificRecord,
			is(
				pojo(GemeindeEventDTO.class)
					.where(
						GemeindeEventDTO::getGemeindeUUID,
						is(gemeinde.getId())
					)
					.where(
						GemeindeEventDTO::getName,
						is(gemeinde.getName())
					)
					.where(GemeindeEventDTO::getBfsNummer, is(123L))
					.where(
						GemeindeEventDTO::getBetreuungsgutscheineAnbietenAb,
						is(
							gemeinde.getBetreuungsgutscheineStartdatum()
						)
					)
					.where(
						GemeindeEventDTO::getGueltigBis,
						is(gemeinde.getGueltigBis())
					)
			)
		);
	}
}
