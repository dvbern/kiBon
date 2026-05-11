/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.gemeinde.GemeindeKonfigurationService;
import ch.dvbern.kibon.exchange.commons.gemeinde.GemeindeEventDTO;
import ch.dvbern.kibon.exchange.commons.gemeinde.GemeindeEventDTO.Builder;
import ch.dvbern.kibon.exchange.commons.types.GemeindeGesuchsperiodeKonfiguration;
import ch.dvbern.kibon.exchange.commons.types.Mandant;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

@ApplicationScoped
public class GemeindeEventConverter {

	@Inject
	private GemeindeKonfigurationService gemeindeKonfigurationService;

	@Nonnull
	public GemeindeChangedEvent of(@Nonnull Gemeinde gemeinde) {
		GemeindeEventDTO dto = toGemeindeEventDTO(gemeinde);

		return toEvent(gemeinde.getId(), dto);
	}

	@Nonnull
	private GemeindeChangedEvent toEvent(
		@Nonnull String gemeindeId,
		GemeindeEventDTO dto
	) {
		byte[] payload = AvroConverter.toAvroBinary(dto);

		return new GemeindeChangedEvent(gemeindeId, payload, dto.getSchema());
	}

	@Nonnull
	private GemeindeEventDTO toGemeindeEventDTO(@Nonnull Gemeinde gemeinde) {
		var gpEinstellungenMap = this.gemeindeKonfigurationService
			.loadEinstellungenOfGPRelevantForGemeinde(gemeinde);

		//noinspection ConstantConditions
		Builder builder = GemeindeEventDTO.newBuilder()
			.setGemeindeUUID(gemeinde.getId())
			.setName(gemeinde.getName())
			.setBfsNummer(gemeinde.getBfsNummer())
			.setBetreuungsgutscheineAnbietenAb(
				gemeinde.getBetreuungsgutscheineStartdatum()
			)
			.setGueltigBis(gemeinde.getGueltigBis())
			.setMandant(
				Mandant.valueOf(
					gemeinde.getMandant()
						.getMandantIdentifier()
						.name()
				)
			)
			.setAngebotBG(gemeinde.isAngebotBG())
			.setAngebotTS(gemeinde.isAngebotTS())
			.setAngebotFI(gemeinde.isAngebotFI())
			.setGemeindeGesuchsperiodeKonfigurationen(
				toGemeindeGesuchsperiodeKonfigurationList(gpEinstellungenMap)
			);
		return builder.build();
	}

	private List<GemeindeGesuchsperiodeKonfiguration> toGemeindeGesuchsperiodeKonfigurationList(
		@MonotonicNonNull Map<Gesuchsperiode, Map<EinstellungKey, Einstellung>> gpEinstellungenMap
	) {
		return gpEinstellungenMap.keySet()
			.stream()
			.map(
				gp -> GemeindeGesuchsperiodeKonfiguration
					.newBuilder()
					.setGesuchsperiodeStart(
						gp
							.getGueltigkeit()
							.getGueltigAb()
					)
					.setGesuchsperiodeEnd(
						gp
							.getGueltigkeit()
							.getGueltigBis()
					)
					.setKontingentierung(
						gpEinstellungenMap.get(gp)
							.get(
								EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED
							)
							.getValueAsBoolean()
					)
					.build()
			)
			.toList();
	}
}
