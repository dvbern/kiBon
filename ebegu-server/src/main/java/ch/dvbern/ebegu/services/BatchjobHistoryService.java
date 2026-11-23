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
 */

package ch.dvbern.ebegu.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.batchjobhistory.BatchjobHistorySearchParams;
import ch.dvbern.ebegu.entities.BatchjobHistory;
import ch.dvbern.ebegu.entities.BatchjobHistory_;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.util.datetime.DateTimeUtils;

@Stateless
@Local(BatchjobHistoryService.class)
public class BatchjobHistoryService extends AbstractBaseService {

	@Inject
	private Persistence persistence;

	@Inject
	private DateTimeUtils dateTimeUtils;

	/**
	 * Persists a {@link BatchjobHistory} in the database
	 *
	 * @param batchjobHistory the {@link BatchjobHistory} to be persisted
	 * @return the persisted {@link BatchjobHistory}
	 */
	@Nonnull
	public BatchjobHistory saveBatchjobHistory(
		@Nonnull BatchjobHistory batchjobHistory
	) {
		return persistence.persist(batchjobHistory);
	}

	/**
	 * Retrieves all {@link BatchjobHistory} entries matching the specified search, filter,
	 * paging, and sorting parameters.
	 *
	 * @param params the search parameters used to filter, page, and sort the batch job history entries
	 * @return the matching {@link BatchjobHistory} entries
	 */
	@Nonnull
	public Collection<BatchjobHistory> getAll(
		@Nonnull BatchjobHistorySearchParams params
	) {
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<BatchjobHistory> query = builder.createQuery(
			BatchjobHistory.class
		);
		final Root<BatchjobHistory> root = query.from(BatchjobHistory.class);
		query.where(createFilterPredicate(params, builder, root));

		if ("asc".equals(params.getSortDirection())) {
			query.orderBy(builder.asc(root.get(params.getActive())));
		} else {
			query.orderBy(builder.desc(root.get(params.getActive())));
		}

		return persistence.getEntityManager()
			.createQuery(query)
			.setMaxResults(params.getPageSize())
			.setFirstResult(params.getPageIndex() * params.getPageSize())
			.getResultList();
	}

	/**
	 * Counts the {@link BatchjobHistory} entries matching the specified search and filter parameters.
	 *
	 * @param params the search parameters used to filter the batch job history entries
	 * @return the number of matching {@link BatchjobHistory} entries
	 */
	public long countBatchjobHistory(
		@Nonnull BatchjobHistorySearchParams params
	) {
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<Long> query = builder.createQuery(Long.class);
		final Root<BatchjobHistory> root = query.from(BatchjobHistory.class);
		query.where(createFilterPredicate(params, builder, root));
		query.select(builder.count(root));

		return persistence.getEntityManager()
			.createQuery(query)
			.getSingleResult();
	}

	@Nonnull
	private Predicate createFilterPredicate(
		@Nonnull BatchjobHistorySearchParams params,
		@Nonnull CriteriaBuilder builder,
		@Nonnull Root<BatchjobHistory> root
	) {
		List<Predicate> predicates = new ArrayList<>();

		if (params.getStartDate() != null) {
			LocalDateTime startDate = dateTimeUtils.parseOrThrowBadRequest(
				params.getStartDate()
			);
			predicates.add(
				builder.greaterThanOrEqualTo(
					root.get(BatchjobHistory_.timestampStarted),
					startDate
				)
			);
		}
		if (params.getEndDate() != null) {
			LocalDateTime endDate = dateTimeUtils.parseOrThrowBadRequest(
				params.getEndDate()
			);
			predicates.add(
				builder.lessThanOrEqualTo(
					root.get(BatchjobHistory_.timestampStarted),
					endDate
				)
			);
		}
		if (params.getStatus() != null && !params.getStatus().isEmpty()) {
			predicates.add(root.get(BatchjobHistory_.status).in(params.getStatus()));
		}
		if (params.getNameOrResult() != null && !params.getNameOrResult().isBlank()) {
			String wildcardFilter = '%' + params.getNameOrResult() + '%';
			predicates.add(
				builder.or(
					builder.like(
						root.get(BatchjobHistory_.name),
						wildcardFilter
					),
					builder.like(
						root.get(BatchjobHistory_.result),
						wildcardFilter
					)
				)
			);
		}

		return builder.and(predicates.toArray(new Predicate[0]));
	}
}
