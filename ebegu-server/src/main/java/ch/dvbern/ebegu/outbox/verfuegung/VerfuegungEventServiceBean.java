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

package ch.dvbern.ebegu.outbox.verfuegung;

import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AbstractPlatz_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.Verfuegung_;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class VerfuegungEventServiceBean {

	private static final Logger LOG = LoggerFactory.getLogger(
		VerfuegungEventServiceBean.class
	);

	@Inject
	private Persistence persistence;

	@Inject
	private VerfuegungEventHelper verfuegungEventHelper;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void publishExistingVerfuegungen(Mandant mandant) {
		if (!applicationPropertyService.isPublishSchnittstelleEventsAktiviert(
			mandant
		)) {
			return;
		}
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<Verfuegung> root = query.from(Verfuegung.class);
		Path<Betreuung> betreuungPath = root.get(Verfuegung_.betreuung);

		ParameterExpression<Betreuungsstatus> statusParam = cb.parameter(
			Betreuungsstatus.class
		);
		Predicate isVerfuegt = cb.equal(
			betreuungPath.get(AbstractPlatz_.betreuungsstatus),
			statusParam
		);

		Predicate isNotPublished = cb.isFalse(
			root.get(Verfuegung_.eventPublished)
		);

		Predicate mandantPredicate = cb.equal(
			betreuungPath.get(Betreuung_.institutionStammdaten)
				.get(InstitutionStammdaten_.institution)
				.get(Institution_.mandant),
			mandant
		);

		query.where(cb.and(isNotPublished, isVerfuegt, mandantPredicate));
		query.select(root.get(AbstractEntity_.ID));

		List<String> verfuegungIds = persistence.getEntityManager()
			.createQuery(query)
			.setParameter(statusParam, Betreuungsstatus.VERFUEGT)
			.getResultList();

		verfuegungIds.forEach(verfuegungId -> {
			verfuegungEventHelper.convertAndFire(verfuegungId);
			LOG.info(
				"New Event for Verfuegung {} has been converted and written in the outbox event table",
				verfuegungId
			);
		});
	}
}
