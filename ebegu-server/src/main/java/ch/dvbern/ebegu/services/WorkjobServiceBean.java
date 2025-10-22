/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Workjob;
import ch.dvbern.ebegu.entities.Workjob_;
import ch.dvbern.ebegu.enums.reporting.BatchJobStatus;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Acess Object Bean zum zugriff auf Workjoben in der DB
 */
@Stateless
@Local(WorkjobService.class)
public class WorkjobServiceBean extends AbstractBaseService implements
	WorkjobService {

	private static final Logger LOG = LoggerFactory.getLogger(
		WorkjobServiceBean.class.getSimpleName()
	);

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@PersistenceContext
	private EntityManager em;

	@Nonnull
	@Override
	public Workjob saveWorkjob(@Nonnull Workjob workJob) {
		return persistence.merge(workJob);
	}

	@Nullable
	@Override
	public Workjob findWorkjobByExecutionId(@Nonnull final Long executionId) {

		final Collection<Workjob> entitiesByAttribute = criteriaQueryHelper
			.getEntitiesByAttribute(
				Workjob.class,
				executionId,
				Workjob_.executionId
			);
		final Optional<Workjob> first = entitiesByAttribute
			.stream()
			.filter(workjob -> workjob.getTimestampErstellt() != null)
			.max(Comparator.comparing(Workjob::getTimestampErstellt));
		return first.orElse(null);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void removeOldWorkjobs() {
		LocalDateTime cutoffDate = LocalDateTime.now()
			.minusMinutes(Constants.MAX_LONGER_TEMP_DOWNLOAD_AGE_MINUTES);
		int i = this.criteriaQueryHelper.deleteAllBefore(
			Workjob.class,
			cutoffDate
		);
		LOG.info("... deleted {} + workjobs", i);
	}

	@Nonnull
	@Override
	public List<Workjob> findWorkjobs(
		@Nonnull String startingUserName,
		@Nonnull Set<BatchJobStatus> statesToSearch
	) {
		Objects.requireNonNull(
			startingUserName,
			"username to search must be set"
		);
		Objects.requireNonNull(statesToSearch, "statesToSearch  must be set");

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Workjob> query = cb.createQuery(Workjob.class);
		Root<Workjob> root = query.from(Workjob.class);

		ParameterExpression<String> startingUsernameParam = cb.parameter(
			String.class,
			"startingUsernameParam"
		);
		Predicate userPredicate = cb.equal(
			root.get(Workjob_.startinguser),
			startingUsernameParam
		);

		ParameterExpression<Collection> statusParam = cb.parameter(
			Collection.class,
			"statusParam"
		);
		Predicate statusPredicate = root.get(Workjob_.status).in(statusParam);

		query.where(userPredicate, statusPredicate);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampMutiert)));
		TypedQuery<Workjob> q = persistence.getEntityManager()
			.createQuery(query);
		q.setParameter(startingUsernameParam, startingUserName);
		q.setParameter(statusParam, statesToSearch);

		return q.getResultList();
	}

	@Nonnull
	@Override
	public List<Workjob> findAllWorkjobs() {
		final Collection<Workjob> all = criteriaQueryHelper.getAll(
			Workjob.class
		);
		return new ArrayList<>(all);
	}

	@Override
	public void changeStateOfWorkjob(
		long executionId,
		@Nonnull BatchJobStatus status
	) {
		persistence.getEntityManager()
			.createNamedQuery(Workjob.Q_WORK_JOB_STATE_UPDATE)
			.setParameter("exId", executionId)
			.setParameter("status", status)
			.executeUpdate();
	}

	@Override
	@Transactional(Transactional.TxType.REQUIRES_NEW)
	public void addResultToWorkjob(
		@Nonnull String workjobID,
		@Nonnull String resultData
	) {
		Objects.requireNonNull(resultData);

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaUpdate<Workjob> update = cb.createCriteriaUpdate(Workjob.class);
		Root<Workjob> root = update.from(Workjob.class);

		update.set(root.get(Workjob_.resultData), resultData);
		update.where(cb.equal(root.get(AbstractEntity_.id), workjobID));

		em.createQuery(update).executeUpdate();
	}

	@Override
	public void removeWorkjob(Workjob workjob) {
		this.persistence.remove(workjob);
	}
}
