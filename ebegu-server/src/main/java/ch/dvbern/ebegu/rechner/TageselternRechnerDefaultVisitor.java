/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rechner;

import java.util.List;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.util.mandant.AbstractMandantDefaultVisitor;

public class TageselternRechnerDefaultVisitor extends
	AbstractMandantDefaultVisitor<AbstractRechner> {

	private final List<RechnerRule> rechnerRulesForGemeinde;

	public TageselternRechnerDefaultVisitor(
		List<RechnerRule> rechnerRulesForGemeinde
	) {
		this.rechnerRulesForGemeinde = rechnerRulesForGemeinde;
	}

	public AbstractRechner getTageselternRechnerForMandant(Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	protected AbstractRechner visitDefault() {
		return new TageselternRechner(rechnerRulesForGemeinde);
	}

	@Override
	public AbstractRechner visitLuzern() {
		return new TageselternLuzernRechner(rechnerRulesForGemeinde);
	}

	@Override
	public AbstractRechner visitSchwyz() {
		return new TagesfamilienSchwyzRechner();
	}
}
