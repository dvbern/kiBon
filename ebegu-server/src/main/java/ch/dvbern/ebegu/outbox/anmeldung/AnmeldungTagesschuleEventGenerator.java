/*
 *
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

package ch.dvbern.ebegu.outbox.anmeldung;

import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.security.RunAs;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AbstractPlatz_;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule_;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.persistence.Persistence;
import org.jboss.ejb3.annotation.RunAsPrincipal;
import org.jboss.ejb3.annotation.TransactionTimeout;

@Stateless
@RunAs(UserRoleName.SUPER_ADMIN)
@RunAsPrincipal(PrincipalBean.KIBON_SERVICE_ACCOUNT)
public class AnmeldungTagesschuleEventGenerator {

	@Inject
	private Persistence persistence;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private AnmeldungTagesschuleEventAsyncHelper asyncHelper;

	/**
	 * This is a job starting every night and exports all anmeldungen for which event_published
	 * value is false
	 */
	@Schedule(
		info = "Migration-aid, pushes Anmeldungen waiting for confirmation and not yet published",
		hour = "5")
	@TransactionTimeout(value = 3, unit = TimeUnit.HOURS)
	public void publishWartendeAnmeldungen() {
		if (!ebeguConfiguration.isAnmeldungTagesschuleApiEnabled()) {
			return;
		}

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<AnmeldungTagesschule> root = query.from(
			AnmeldungTagesschule.class
		);

		//Event muss noch nicht plubliziert sein
		Predicate isNotPublished = cb.isFalse(
			root.get(AnmeldungTagesschule_.eventPublished)
		);
		Predicate isInStatusToFireEvent = root.get(
			AbstractPlatz_.betreuungsstatus
		)
			.in(
				Betreuungsstatus
					.getBetreuungsstatusForFireAnmeldungTagesschuleEvent()
			);

		query.where(isNotPublished, isInStatusToFireEvent);
		query.select(root.get(AbstractEntity_.ID));

		List<String> anmeldungTagesschuleList = persistence.getEntityManager()
			.createQuery(query)
			.getResultList();

		anmeldungTagesschuleList.forEach(
			anmeldung -> asyncHelper.convert(anmeldung)
		);
	}
}
