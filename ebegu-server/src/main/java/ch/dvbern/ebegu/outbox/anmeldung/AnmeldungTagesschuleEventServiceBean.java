/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.outbox.anmeldung;

import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AbstractPlatz_;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule_;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class AnmeldungTagesschuleEventServiceBean {

	private static final Logger LOG = LoggerFactory.getLogger(
		AnmeldungTagesschuleEventServiceBean.class
	);

	@Inject
	private Persistence persistence;

	@Inject
	private AnmeldungTagesschuleEventHelper anmeldungTagesschuleEventHelper;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void publishExistingAnmeldungTagesschule(Mandant mandant) {
		if (!applicationPropertyService.isPublishSchnittstelleEventsAktiviert(
			mandant
		)) {
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

		Join<AnmeldungTagesschule, InstitutionStammdaten> institutionStammdatenJoin =
			root.join(AnmeldungTagesschule_.institutionStammdaten);
		Join<InstitutionStammdaten, Institution> institutionJoin =
			institutionStammdatenJoin.join(InstitutionStammdaten_.institution);

		Predicate mandantPredicate = cb.equal(
			institutionJoin.get(Institution_.mandant),
			mandant
		);

		Predicate isInStatusToFireEvent = root.get(
			AbstractPlatz_.betreuungsstatus
		)
			.in(
				Betreuungsstatus
					.getBetreuungsstatusForFireAnmeldungTagesschuleEvent()
			);

		query.where(
			cb.and(isNotPublished, isInStatusToFireEvent, mandantPredicate)
		);
		query.select(root.get(AbstractEntity_.ID));

		List<String> anmeldungTagesschuleList = persistence.getEntityManager()
			.createQuery(query)
			.getResultList();

		anmeldungTagesschuleList.forEach(
			anmeldung -> {
				anmeldungTagesschuleEventHelper.convertAndFire(anmeldung);
				LOG.info(
					"New Event for Anmeldung {} has been converted and written in the outbox event table",
					anmeldung
				);
			}
		);
	}
}
