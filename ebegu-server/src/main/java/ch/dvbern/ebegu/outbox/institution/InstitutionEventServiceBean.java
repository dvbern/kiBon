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

package ch.dvbern.ebegu.outbox.institution;

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

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.InstitutionStatus;
import ch.dvbern.ebegu.persistence.Persistence;

@Stateless
public class InstitutionEventServiceBean {

	@Inject
	private Persistence persistence;

	@Inject
	private InstitutionEventHelper institutionEventHelper;

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void publishExistingInstitutionen(Mandant mandant) {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(
			String.class
		);
		Root<InstitutionStammdaten> root = query.from(
			InstitutionStammdaten.class
		);

		Join<InstitutionStammdaten, Institution> institutionJoin = root.join(
			InstitutionStammdaten_.institution
		);

		Predicate isNotPublished = cb.isFalse(
			institutionJoin.get(Institution_.eventPublished)
		);

		Predicate mandantPredicate = cb.equal(
			institutionJoin.get(Institution_.mandant),
			mandant
		);

		var statusParam = cb.parameter(
			InstitutionStatus.class,
			Institution_.STATUS
		);
		Predicate notLatsStatus = cb.notEqual(
			institutionJoin.get(Institution_.status),
			statusParam
		);

		query.where(cb.and(isNotPublished, notLatsStatus, mandantPredicate));
		query.select(root.get(AbstractEntity_.ID));

		List<String> institutionIds = persistence
			.getEntityManager()
			.createQuery(query)
			.setParameter(statusParam, InstitutionStatus.NUR_LATS)
			.getResultList();

		institutionIds.stream()
			.forEach(institutionId -> {
				institutionEventHelper.convertAndFire(institutionId);
			});
	}
}
