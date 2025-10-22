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
 *
 */

package ch.dvbern.ebegu.services.einladung;

import java.util.List;

import jakarta.annotation.security.RunAs;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.BenutzerQueries_;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.EinladungService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import lombok.extern.slf4j.Slf4j;
import org.jboss.ejb3.annotation.RunAsPrincipal;

@Slf4j
@Singleton
@RunAs(UserRoleName.SUPER_ADMIN)
@RunAsPrincipal(PrincipalBean.KIBON_SERVICE_ACCOUNT)
public class EingeladeneBenutzerCleanUpJob {

	@Inject
	private Persistence persistence;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private MandantService mandantService;

	@Inject
	private EinladungService einladungService;

	@Schedule(
		info = "Delete Benutzer with expired Einladungen",
		hour = "4",
		minute = "0",
		second = "0",
		persistent = true
	)
	public void deleteBenutzerWithExpiredEinladungen() {
		for (MandantIdentifier mandantIdentifier : MandantIdentifier.values()) {
			Mandant mandant =
				mandantService.findMandantByIdentifier(mandantIdentifier)
					.orElseThrow();

			List<Benutzer> benutzersToDelete =
				BenutzerQueries_.getExpiredEingeladeneBenutzer(
					persistence.getEntityManager(),
					mandant,
					BenutzerStatus.EINGELADEN,
					einladungService.getExpirationThreshold()
				);

			for (Benutzer benutzerToDelete : benutzersToDelete) {
				benutzerService.removeBenutzer(
					benutzerToDelete.getUsername(),
					mandant
				);
			}

			LOG.info(
				"Deleted {} users with expired invitations for tenant {}",
				benutzersToDelete.size(),
				mandantIdentifier
			);
		}
	}
}
