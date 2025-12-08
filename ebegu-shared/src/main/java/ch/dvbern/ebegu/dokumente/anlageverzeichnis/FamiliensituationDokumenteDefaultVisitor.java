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

package ch.dvbern.ebegu.dokumente.anlageverzeichnis;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.AbstractMandantDefaultVisitor;
import com.sun.istack.NotNull;

public class FamiliensituationDokumenteDefaultVisitor extends
	AbstractMandantDefaultVisitor<AbstractDokumente<Familiensituation, Familiensituation>> {

	public AbstractDokumente<Familiensituation, Familiensituation> getFamiliensituationDokumenteForMandant(
		@NotNull Mandant mandant
	) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	protected AbstractDokumente<Familiensituation, Familiensituation> visitDefault() {
		return new FamiliensituationDokumente();
	}

	@Override
	public AbstractDokumente<Familiensituation, Familiensituation> visitLuzern() {
		return new LuzernFamiliensituationDokumente();
	}
}
