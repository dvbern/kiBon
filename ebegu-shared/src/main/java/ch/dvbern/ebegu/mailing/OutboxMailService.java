package ch.dvbern.ebegu.mailing;

import java.util.List;

import javax.annotation.Nonnull;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.Valid;

import ch.dvbern.ebegu.persistence.Persistence;

@Stateless
public class OutboxMailService {

	@Inject
	private Persistence persistence;

	public OutboxMail findOutboxMailById(@Nonnull String id) {
		return persistence.find(OutboxMail.class, id);
	}

	public void saveOutboxMail(@Valid OutboxMail outboxMail) {
		this.persistence.persist(outboxMail);
	}

	public void udpateOutboxMail(@Valid OutboxMail outboxMail) {
		this.persistence.merge(outboxMail);
	}

	public List<OutboxMail> getNextN(int n) {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<OutboxMail> query = cb.createQuery(OutboxMail.class);
		Root<OutboxMail> from = query.from(OutboxMail.class);
		Predicate statusPredicate = cb.notEqual(
			from.get(OutboxMail_.status),
			OutboxMailStatus.FAILED
		);
		query.select(from)
			.where(statusPredicate)
			.orderBy(cb.asc(from.get(OutboxMail_.retryCount)));

		return persistence.getEntityManager()
			.createQuery(query)
			.setMaxResults(n)
			.getResultList();
	}

	public void remove(OutboxMail outboxMail) {
		this.persistence.remove(outboxMail);
	}
}
