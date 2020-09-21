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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AbstractPersonEntity_;
import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.entities.ExternalClient_;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.ExternalClientType;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(ExternalClientService.class)
public class ExternalClientServiceBean extends AbstractBaseService implements ExternalClientService {

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private Persistence persistence;

	@Nonnull
	@Override
	public Collection<ExternalClient> getAllForGemeinde() {
		return criteriaQueryHelper.getEntitiesByAttribute(
			ExternalClient.class, ExternalClientType.GEMEINDE_SCOLARIS_SERVICE, ExternalClient_.type);
	}

	@Nonnull
	@Override
	public Collection<ExternalClient> getAllForInstitution() {
		return criteriaQueryHelper.getEntitiesByAttribute(
			ExternalClient.class, ExternalClientType.EXCHANGE_SERVICE_USER, ExternalClient_.type);
	}

	@Override
	public Optional<ExternalClient> findExternalClient(@Nullable String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		ExternalClient externalClient = persistence.find(ExternalClient.class, id);
		return Optional.ofNullable(externalClient);
	}

	/*@Override
	public Optional<ExternalClient> findExternalClient(String clientName, ExternalClientType type) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<ExternalClient> query = cb.createQuery(ExternalClient.class);
		Root<ExternalClient> root = query.from(ExternalClient.class);
		List<Predicate> predicates = new ArrayList<>();
		Predicate predicateClientName = cb.equal(root.get(ExternalClient_.CLIENT_NAME),
			clientName);
		Predicate predicateType = cb.equal(root.get(ExternalClient_.TYPE), type);
		predicates.add(predicateClientName);
		predicates.add(predicateType);
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		ExternalClient externalClient = persistence.getCriteriaSingleResult(query);
		return Optional.ofNullable(externalClient);
	}*/
}
