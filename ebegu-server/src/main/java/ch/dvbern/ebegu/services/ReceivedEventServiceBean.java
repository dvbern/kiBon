/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.ReceivedEvent;
import ch.dvbern.ebegu.entities.ReceivedEvent_;
import ch.dvbern.ebegu.persistence.Persistence;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@Local(ReceivedEventService.class)
public class ReceivedEventServiceBean implements ReceivedEventService {

	private static final Logger LOG = LoggerFactory.getLogger(
		ReceivedEventServiceBean.class
	);

	@Inject
	private Persistence persistence;

	@SuppressWarnings("PMD.CloseResource")
	@Override
	public boolean isSuccessfullyProcessed(@Nonnull String eventId) {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<ReceivedEvent> query = cb.createQuery(
			ReceivedEvent.class
		);
		Root<ReceivedEvent> root = query.from(ReceivedEvent.class);

		ParameterExpression<String> eventIdParam = cb.parameter(
			String.class,
			"eventId"
		);
		Predicate eventIdPred = cb.equal(
			root.get(ReceivedEvent_.eventId),
			eventIdParam
		);

		Predicate successPred = cb.isTrue(root.get(ReceivedEvent_.success));

		query.where(eventIdPred, successPred);

		EntityManager em = persistence.getEntityManager();
		Optional<ReceivedEvent> success = em.createQuery(query)
			.setParameter(eventIdParam, eventId)
			.getResultStream()
			.findAny();

		return success.isPresent();
	}

	@SuppressWarnings("PMD.CloseResource")
	@Override
	public boolean isObsolete(@Nonnull ReceivedEvent receivedEvent) {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<ReceivedEvent> query = cb.createQuery(
			ReceivedEvent.class
		);
		Root<ReceivedEvent> root = query.from(ReceivedEvent.class);

		ParameterExpression<String> eventKeyParam = cb.parameter(
			String.class,
			"eventKey"
		);
		Predicate eventKeyPred = cb.equal(
			root.get(ReceivedEvent_.eventKey),
			eventKeyParam
		);

		ParameterExpression<String> eventTypeParam = cb.parameter(
			String.class,
			"eventType"
		);
		Predicate eventTypePred = cb.equal(
			root.get(ReceivedEvent_.eventType),
			eventTypeParam
		);

		ParameterExpression<LocalDateTime> eventTimestampParam = cb.parameter(
			LocalDateTime.class,
			"eventTimestamp"
		);
		Predicate eventTimestampPred =
			cb.greaterThanOrEqualTo(
				root.get(ReceivedEvent_.eventTimestamp),
				eventTimestampParam
			);

		query.where(eventKeyPred, eventTypePred, eventTimestampPred);

		EntityManager em = persistence.getEntityManager();
		Optional<ReceivedEvent> success = em.createQuery(query)
			.setParameter(eventKeyParam, receivedEvent.getEventKey())
			.setParameter(eventTypeParam, receivedEvent.getEventType())
			.setParameter(
				eventTimestampParam,
				receivedEvent.getEventTimestamp()
			)
			.getResultStream()
			.findAny();

		return success.isPresent();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void processingSuccess(@Nonnull ReceivedEvent receivedEvent) {
		receivedEvent.setSuccess(true);
		persistence.persist(receivedEvent);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void processingFailure(
		@Nonnull ReceivedEvent receivedEvent,
		@Nonnull Throwable e
	) {
		receivedEvent.setSuccess(false);
		try {
			String error = String.format(
				"Message: %s, Cause: %s, Root Cause: %s",
				ExceptionUtils.getMessage(e),
				ExceptionUtils.getMessage(e),
				ExceptionUtils.getRootCauseMessage(e)
			);
			receivedEvent.setError(error);
		} catch (RuntimeException ex) {
			LOG.error(
				"Exctracting error messages for {} failed",
				receivedEvent,
				ex
			);
		}
		persistence.persist(receivedEvent);
	}
}
