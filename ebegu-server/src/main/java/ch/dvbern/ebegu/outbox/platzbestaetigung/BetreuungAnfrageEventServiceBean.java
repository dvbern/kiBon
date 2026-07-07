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

package ch.dvbern.ebegu.outbox.platzbestaetigung;

import java.util.ArrayList;
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
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.services.util.PredicateHelper.NEW;

@Stateless
public class BetreuungAnfrageEventServiceBean {

	private static final Logger LOG = LoggerFactory.getLogger(
		BetreuungAnfrageEventServiceBean.class
	);

	@Inject
	private Persistence persistence;

	@Inject
	private BetreuungAnfrageEventHelper betreuungAnfrageEventHelper;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void publishExistingBetreuungAnfrage(Mandant mandant) {
		if (!applicationPropertyService
			.isPublishSchnittstelleEventsAktiviert(
				mandant
			)) {
			return;
		}
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<Betreuung> root = query.from(Betreuung.class);
		List<Predicate> predicates = new ArrayList<>();

		//Institution Stammdaten Join and check angebot Typ, muss Kita oder TFO sein
		Join<Betreuung, InstitutionStammdaten> institutionStammdatenJoin =
			root.join(Betreuung_.institutionStammdaten);
		Predicate isBetreuungsgutscheinTyp =
			institutionStammdatenJoin.get(
				InstitutionStammdaten_.betreuungsangebotTyp
			)
				.in(BetreuungsangebotTyp.getBetreuungsgutscheinTypes());
		predicates.add(isBetreuungsgutscheinTyp);

		//Event muss noch nicht plubliziert sein
		Predicate isNotPublished = cb.isFalse(
			root.get(Betreuung_.eventPublished)
		);
		predicates.add(isNotPublished);

		//Status muss warten sein
		Predicate statusWarten = cb.equal(
			root.get(AbstractPlatz_.betreuungsstatus),
			Betreuungsstatus.WARTEN
		);
		predicates.add(statusWarten);
		Join<InstitutionStammdaten, Institution> institutionJoin =
			institutionStammdatenJoin.join(InstitutionStammdaten_.institution);
		Predicate mandantPredicate = cb.equal(
			institutionJoin.get(Institution_.mandant),
			mandant
		);
		predicates.add(mandantPredicate);

		query.where(predicates.toArray(NEW));
		query.select(root.get(AbstractEntity_.ID));

		List<String> betreuungIds = persistence.getEntityManager()
			.createQuery(query)
			.getResultList();

		betreuungIds.stream()
			.forEach(betreuungId -> {
				betreuungAnfrageEventHelper.convertAndFire(betreuungId);
				LOG.info(
					"New Event for Betreuung {} has been converted and written in the outbox event table",
					betreuungId
				);
			});
	}
}
