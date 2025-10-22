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

package ch.dvbern.ebegu.testfaelle.institutionstammdatenbuilder;

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;

public class InstitutionStammdatenBuilderZg extends
	InstitutionStammdatenBuilder {
	public InstitutionStammdatenBuilderZg(
		InstitutionStammdatenService institutionStammdatenService
	) {
		super(
			institutionStammdatenService,
			"d75b5d65-6436-11ef-8aab-005056bde697",
			"dd7f784f-6436-11ef-8aab-005056bde697",
			""
		);
	}

	@Override
	public List<InstitutionStammdaten> buildStammdaten() {
		List<InstitutionStammdaten> institutionStammdatenList =
			new ArrayList<>();
		institutionStammdatenList.add(findInstitutionWeissenstein());
		institutionStammdatenList.add(findInstitutionBruennen());

		return institutionStammdatenList;
	}
}
