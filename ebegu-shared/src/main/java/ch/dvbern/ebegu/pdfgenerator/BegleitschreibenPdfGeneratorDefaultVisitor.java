/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.util.mandant.AbstractMandantDefaultVisitor;

public class BegleitschreibenPdfGeneratorDefaultVisitor extends
	AbstractMandantDefaultVisitor<BegleitschreibenPdfGenerator> {

	private final GesuchsperiodeService gesuchsperiodeService;

	private final Gesuch gesuch;
	private final GemeindeStammdaten gemeindeStammdaten;

	public BegleitschreibenPdfGeneratorDefaultVisitor(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten gemeindeStammdaten,
		@Nonnull GesuchsperiodeService gesuchsperiodeService
	) {
		this.gesuch = gesuch;
		this.gemeindeStammdaten = gemeindeStammdaten;
		this.gesuchsperiodeService = gesuchsperiodeService;
	}

	@Override
	protected BegleitschreibenPdfGenerator visitDefault() {
		return new BegleitschreibenPdfGenerator(
			gesuch,
			gemeindeStammdaten,
			gesuchsperiodeService
		);
	}

	@Override
	public BegleitschreibenPdfGenerator visitSchwyz() {
		return new BegleitschreibenPdfGeneratorSchwyz(
			gesuch,
			gemeindeStammdaten,
			gesuchsperiodeService
		);
	}
}
