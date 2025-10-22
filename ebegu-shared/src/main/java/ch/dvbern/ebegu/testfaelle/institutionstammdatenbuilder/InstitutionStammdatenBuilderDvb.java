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

package ch.dvbern.ebegu.testfaelle.institutionstammdatenbuilder;

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;

public class InstitutionStammdatenBuilderDvb extends
	InstitutionStammdatenBuilder {
	public InstitutionStammdatenBuilderDvb(
		InstitutionStammdatenService institutionStammdatenService
	) {
		super(
			institutionStammdatenService,
			"acbc3c07-0c67-4bf0-9bb1-d4d92f65b3c4",
			"497a70cd-0c22-46e1-9787-1efffc8b73d8",
			"24f9aa31-ced6-42e8-b046-013c61d607dd"
		);
	}

	@Override
	public List<InstitutionStammdaten> buildStammdaten() {
		List<InstitutionStammdaten> institutionStammdatenList =
			new ArrayList<>();
		institutionStammdatenList.add(findInstitutionWeissenstein());
		institutionStammdatenList.add(findInstitutionBruennen());
		institutionStammdatenList.add(findInstitutionTagesfamilien());

		return institutionStammdatenList;
	}
}
