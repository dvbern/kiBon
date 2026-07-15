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
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.VersendeteMail;
import ch.dvbern.ebegu.entities.VersendeteMail_;
import ch.dvbern.ebegu.mailing.VersendeteMailSearchParams;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.util.datetime.DateTimeUtils;

@Stateless
public class VersendeteMailsService extends AbstractBaseService {
	@Inject
	private Persistence persistence;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private DateTimeUtils dateTimeUtils;

	@Nonnull
	public VersendeteMail saveVersendeteMail(
		@Nonnull VersendeteMail versendeteMail
	) {
		return persistence.persist(versendeteMail);
	}

	/**
	 * Retrieves a paginated and filtered collection of {@link VersendeteMail} entities based on the provided criteria.
	 *
	 * @param params the filter criteria for narrowing down the results. The filter may include conditions like a date
	 * range or matches
	 * on the recipient address or subject.
	 * @return a collection of VersendeteMail entities that match the specified filter and pagination criteria
	 */
	@Nonnull
	public Collection<VersendeteMail> getAll(
		@Nonnull VersendeteMailSearchParams params
	) {
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<VersendeteMail> query = builder.createQuery(
			VersendeteMail.class
		);
		final Root<VersendeteMail> root = query.from(VersendeteMail.class);
		Predicate mandantPredicate = builder.equal(
			root.get(VersendeteMail_.MANDANT_IDENTIFIER),
			principalBean.getMandant().getMandantIdentifier()
		);
		Predicate filterPredicate = createFilterPredicate(
			params,
			builder,
			root
		);
		query.where(mandantPredicate, filterPredicate);
		if (params.getSortDirection().isEmpty()
			|| params.getSortDirection().equals("desc")) {
			query.orderBy(builder.desc(root.get(params.getActive())));
		} else {
			query.orderBy(builder.asc(root.get(params.getActive())));
		}

		return persistence.getEntityManager()
			.createQuery(query)
			.setMaxResults(params.getPageSize())
			.setFirstResult((params.getPageIndex()) * params.getPageSize())
			.getResultList();
	}

	/**
	 * Counts the number of {@link VersendeteMail} that match the specified filter criteria.
	 *
	 * @param params the filter criteria used for narrowing down the counted emails.
	 * The filter may include conditions like a date range or matches
	 * on the recipient address or subject.
	 * @return the total count of sent emails that match the specified filter criteria.
	 */
	public long countVersendeteMails(VersendeteMailSearchParams params) {
		CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<Long> query = builder.createQuery(Long.class);
		final Root<VersendeteMail> root = query.from(VersendeteMail.class);
		Predicate mandantPredicate = builder.equal(
			root.get(VersendeteMail_.MANDANT_IDENTIFIER),
			principalBean.getMandant().getMandantIdentifier()
		);
		Predicate filterPredicate = createFilterPredicate(
			params,
			builder,
			root
		);
		query.where(mandantPredicate, filterPredicate);

		query.select(builder.count(root));
		return persistence.getEntityManager()
			.createQuery(query)
			.getSingleResult();
	}

	private Predicate createFilterPredicate(
		VersendeteMailSearchParams params,
		CriteriaBuilder builder,
		Root<VersendeteMail> root
	) {
		List<Predicate> predicates = new ArrayList<>();

		LocalDateTime startDateLocalDate = params.getStartDate() == null ?
			null :
			dateTimeUtils.parseOrThrowBadRequest(params.getStartDate());
		LocalDateTime endDateLocalDate = params.getEndDate() == null ?
			null :
			dateTimeUtils.parseOrThrowBadRequest(params.getEndDate());

		// Use comparison operators to make use of the index on the date column
		if (startDateLocalDate != null) {
			predicates.add(
				builder.greaterThanOrEqualTo(
					root.get(VersendeteMail_.ZEITPUNKT_VERSAND),
					startDateLocalDate
				)
			);
		}

		if (endDateLocalDate != null) {
			predicates.add(
				builder.lessThan(
					root.get(VersendeteMail_.ZEITPUNKT_VERSAND),
					endDateLocalDate
				)
			);
		}

		Predicate dateRangePredicate = builder.and(
			predicates.toArray(new Predicate[0])
		);

		if (params.getReceiverOrSubject() == null
			|| params.getReceiverOrSubject().isBlank()) {
			return dateRangePredicate;
		}

		String wildcardFilter = "%" + params.getReceiverOrSubject() + "%";
		Predicate textPredicate = builder.or(
			builder.like(
				root.get(VersendeteMail_.EMPFAENGER_ADRESSE),
				wildcardFilter
			),
			builder.like(root.get(VersendeteMail_.BETREFF), wildcardFilter)
		);

		return builder.and(dateRangePredicate, textPredicate);
	}
}
