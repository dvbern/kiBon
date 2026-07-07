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

package ch.dvbern.ebegu.outbox.verfuegung;

import jakarta.annotation.security.RunAs;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.MandantService;
import org.jboss.ejb3.annotation.RunAsPrincipal;

@Stateless
@RunAs(UserRoleName.SUPER_ADMIN)
@RunAsPrincipal(PrincipalBean.KIBON_SERVICE_ACCOUNT)
public class VerfuegungEventGenerator extends AbstractBaseService {

	@Inject
	MandantService mandantService;

	@Inject
	VerfuegungEventServiceBean verfuegungEventServiceBean;

	/**
	 * Each new Verfuegung is published to Kafka via the outbox event system. However, there are already Verfuegungn
	 * in the database which have not been published, because the outbox event system has been added later. Thus,
	 * fetch all these Verfuegungen and publish them once.
	 */
	@Schedule(
		info = "Migration-aid, pushes already existing Verfuegungen to outbox",
		hour = "5",
		persistent = true)
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void migrate() {
		mandantService.getAll()
			.forEach(
				mandant -> verfuegungEventServiceBean
					.publishExistingVerfuegungen(
						mandant
					)
			);
	}
}
