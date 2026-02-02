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

package ch.dvbern.ebegu.inbox.consumer.betreuung.event;

import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.inbox.handler.EventMonitor;
import lombok.Getter;

/**
 * Stellt die Verknüpfung von {@link Betreuung} und {@link EventMonitor} dar und repräsentiert das Event die gegebene
 * Betreuung betreffend. Z.B. kann das Event eine Stornierungsanfrage für diese Betreuung sein.
 */
@Getter
public class BetreuungEvent {
	private final Betreuung betreuung;
	private final EventMonitor eventMonitor;

	/**
	 * Erzeugt eine neue Verknüfung von {@link Betreuung} und {@link EventMonitor}.
	 *
	 * @param betreuung Die Betreuung die das ausgelöste Event betrifft.
	 * @param eventMonitor Referenz auf ein Betreuungsevent, z.B. eine Stornierung.
	 */
	public BetreuungEvent(
		@NotNull Betreuung betreuung,
		@NotNull EventMonitor eventMonitor
	) {
		this.betreuung = betreuung;
		this.eventMonitor = eventMonitor;
	}
}
