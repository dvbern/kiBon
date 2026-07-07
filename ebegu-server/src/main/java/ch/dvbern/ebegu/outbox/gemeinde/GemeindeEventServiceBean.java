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

package ch.dvbern.ebegu.outbox.gemeinde;

import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.GemeindeStatus;
import ch.dvbern.ebegu.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class GemeindeEventServiceBean {

	private static final Logger LOG = LoggerFactory.getLogger(
		GemeindeEventServiceBean.class
	);

	@Inject
	private Persistence persistence;

	@Inject
	private GemeindeEventHelper gemeindeEventHelper;

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void publishExistingGemeinden(Mandant mandant) {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<Gemeinde> root = query.from(Gemeinde.class);

		Predicate isNotPublished = cb.isFalse(
			root.get(Gemeinde_.eventPublished)
		);
		Predicate mandantPredicate = cb.equal(
			root.get(Gemeinde_.mandant),
			mandant
		);
		Predicate active = cb.equal(
			root.get(Gemeinde_.status),
			GemeindeStatus.AKTIV
		);

		query.where(cb.and(isNotPublished, mandantPredicate, active));
		query.select(root.get(AbstractEntity_.ID));
		List<String> gemeindenIds = persistence.getEntityManager()
			.createQuery(query)
			.getResultList();

		gemeindenIds.forEach(gemeindeId -> {
			gemeindeEventHelper.convertAndFire(gemeindeId);
			LOG.info(
				"New Event for Gemeinde {} has been converted and written in the outbox event table",
				gemeindeId
			);
		});
	}
}
