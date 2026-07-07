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

package ch.dvbern.ebegu.outbox.institution;

import jakarta.annotation.security.RunAs;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.services.MandantService;
import org.jboss.ejb3.annotation.RunAsPrincipal;

@Stateless
@RunAs(UserRoleName.SUPER_ADMIN)
@RunAsPrincipal(PrincipalBean.KIBON_SERVICE_ACCOUNT)
public class InstitutionEventGenerator {

	@Inject
	MandantService mandantService;

	@Inject
	InstitutionEventServiceBean institutionEventServiceBean;

	@Schedule(
		info = "Migration-aid, pushes already existing institutions to outbox",
		hour = "5",
		minute = "10",
		persistent = true)
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void publishExistingInstitutionen() {
		mandantService.getAll()
			.forEach(
				mandant -> institutionEventServiceBean
					.publishExistingInstitutionen(
						mandant
					)
			);
	}
}
