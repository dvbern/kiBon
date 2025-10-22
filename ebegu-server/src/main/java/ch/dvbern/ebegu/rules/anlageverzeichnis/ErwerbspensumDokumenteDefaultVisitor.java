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

package ch.dvbern.ebegu.rules.anlageverzeichnis;

import java.time.LocalDate;

import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.AbstractMandantDefaultVisitor;
import com.sun.istack.NotNull;

public class ErwerbspensumDokumenteDefaultVisitor extends
	AbstractMandantDefaultVisitor<AbstractDokumente<Erwerbspensum, LocalDate>> {

	public AbstractDokumente<Erwerbspensum, LocalDate> getErwerbspensumeDokumenteForMandant(
		@NotNull Mandant mandant
	) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	protected AbstractDokumente<Erwerbspensum, LocalDate> visitDefault() {
		return new ErwerbspensumDokumente();
	}

	@Override
	public AbstractDokumente<Erwerbspensum, LocalDate> visitLuzern() {
		return new LuzernErwerbspensumDokumente();
	}

	@Override
	public AbstractDokumente<Erwerbspensum, LocalDate> visitSchwyz() {
		return new SchwyzErwerbspensumDokumente();
	}

}
