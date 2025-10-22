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

package ch.dvbern.ebegu.util.mandant;

import jakarta.ws.rs.NotSupportedException;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.TutorialConstants;

public class TutorialConstantsVisitor implements
	MandantVisitor<TutorialConstants> {

	public TutorialConstants process(final Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public TutorialConstants visitBern() {
		return new TutorialConstants(
			"11111111-1111-4444-4444-111111111111",
			"11111111-1111-4444-4444-111111111112",
			5555L,
			"22222222-1111-1111-1111-444444444444",
			"33333333-1111-1111-1111-444444444444"
		);
	}

	@Override
	public TutorialConstants visitLuzern() {
		return new TutorialConstants(
			"11111112-1112-4444-4444-111111111111",
			"11111111-1112-4444-4444-111111111112",
			5556L,
			"22222222-1112-1111-1111-444444444444",
			"33333333-1112-1111-1111-444444444444"
		);
	}

	@Override
	public TutorialConstants visitSolothurn() {
		return new TutorialConstants(
			"11111111-1113-4444-4444-111111111111",
			"11111111-1113-4444-4444-111111111112",
			5557L,
			"22222222-1113-1111-1111-444444444444",
			"33333333-1113-1111-1111-444444444444"
		);
	}

	@Override
	public TutorialConstants visitAppenzellAusserrhoden() {
		return new TutorialConstants(
			"11111111-1114-4444-4444-111111111111",
			"11111111-1114-4444-4444-111111111112",
			5558L,
			"22222222-1114-1111-1111-444444444444",
			"33333333-1114-1111-1111-444444444444"
		);
	}

	@Override
	public TutorialConstants visitSchwyz() {
		return new TutorialConstants(
			"11111111-1115-4444-4444-111111111111",
			"11111111-1115-4444-4444-111111111112",
			5559L,
			"22222222-1115-1111-1111-444444444444",
			"33333333-1115-1111-1111-222222222222"
		);
	}

	@Override
	public TutorialConstants visitZug() {
		throw new NotSupportedException("Not implemented for Mandant Zug");
	}

	@Override
	public TutorialConstants visitDvb() {
		return new TutorialConstants(
			"11111111-1116-4444-4444-111111111111",
			"11111111-1116-4444-4444-111111111112",
			5560L,
			"22222222-1116-1111-1111-444444444444",
			"33333333-1116-1111-1111-222222222222"
		);
	}
}
