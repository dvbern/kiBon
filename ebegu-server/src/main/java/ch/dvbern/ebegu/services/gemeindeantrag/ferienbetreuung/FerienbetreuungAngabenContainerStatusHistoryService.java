package ch.dvbern.ebegu.services.gemeindeantrag.ferienbetreuung;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainerStatusHistory;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainerStatusHistory_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.BenutzerService;

@Stateless
public class FerienbetreuungAngabenContainerStatusHistoryService {

	@Inject
	BenutzerService benutzerService;

	@Inject
	Persistence persistence;

	public List<FerienbetreuungAngabenContainerStatusHistory> getHistory(
		FerienbetreuungAngabenContainer container
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<FerienbetreuungAngabenContainerStatusHistory> query =
			cb.createQuery(
				FerienbetreuungAngabenContainerStatusHistory.class
			);

		Root<FerienbetreuungAngabenContainerStatusHistory> root =
			query.from(
				FerienbetreuungAngabenContainerStatusHistory.class
			);

		Predicate predicateContainer = createSameContainerPredicate(
			container,
			cb,
			root
		);
		query.where(predicateContainer);
		return persistence.getEntityManager()
			.createQuery(query)
			.getResultList();
	}

	public Optional<FerienbetreuungAngabenContainerStatusHistory> findLastHistoryOfStatus(
		FerienbetreuungAngabenContainer container,
		FerienbetreuungAngabenStatus status
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<FerienbetreuungAngabenContainerStatusHistory> query =
			cb.createQuery(
				FerienbetreuungAngabenContainerStatusHistory.class
			);

		Root<FerienbetreuungAngabenContainerStatusHistory> root =
			query.from(
				FerienbetreuungAngabenContainerStatusHistory.class
			);

		Predicate predicateContainer = createSameContainerPredicate(
			container,
			cb,
			root
		);

		Predicate predicateStatus = cb.equal(
			root.get(FerienbetreuungAngabenContainerStatusHistory_.status),
			status
		);

		query.where(predicateContainer, predicateStatus);
		orderQueryByTimestampVon(query, cb, root);
		return getSingleResult(query);
	}

	public void updateStatusChangeHistory(
		FerienbetreuungAngabenContainer container
	) {
		endOpenStatusHistory(container);
		this.createStatusChangeHistory(container);
	}

	public void createStatusChangeHistory(
		FerienbetreuungAngabenContainer container
	) {
		final Benutzer currentBenutzer = benutzerService.getCurrentBenutzer()
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"createAndSaveStatusChangeHistory",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND
				)
			);

		var newHistory = new FerienbetreuungAngabenContainerStatusHistory();
		newHistory.setTimestampVon(LocalDateTime.now());
		newHistory.setStatus(container.getStatus());
		newHistory.setBenutzer(currentBenutzer);
		newHistory.setContainer(container);

		persistence.merge(newHistory);
	}

	private void endOpenStatusHistory(
		FerienbetreuungAngabenContainer container
	) {
		findLastStatusChange(container).ifPresent(history -> {
			history.setTimestampBis(LocalDateTime.now());
			persistence.merge(history);
		});
	}

	private Optional<FerienbetreuungAngabenContainerStatusHistory> findLastStatusChange(
		FerienbetreuungAngabenContainer container
	) {
		final CriteriaQuery<FerienbetreuungAngabenContainerStatusHistory> query =
			createQueryAllStatusHistoryForFall(container);

		return getSingleResult(query);
	}

	@Nonnull
	private CriteriaQuery<FerienbetreuungAngabenContainerStatusHistory> createQueryAllStatusHistoryForFall(
		FerienbetreuungAngabenContainer container
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<FerienbetreuungAngabenContainerStatusHistory> query =
			cb.createQuery(
				FerienbetreuungAngabenContainerStatusHistory.class
			);
		Root<FerienbetreuungAngabenContainerStatusHistory> root =
			query.from(
				FerienbetreuungAngabenContainerStatusHistory.class
			);

		Predicate predicateContainer = createSameContainerPredicate(
			container,
			cb,
			root
		);

		query.where(predicateContainer);
		orderQueryByTimestampVon(query, cb, root);
		return query;
	}

	private static Predicate createSameContainerPredicate(
		FerienbetreuungAngabenContainer container,
		CriteriaBuilder cb,
		Root<FerienbetreuungAngabenContainerStatusHistory> root
	) {
		return cb.equal(
			root.get(
				FerienbetreuungAngabenContainerStatusHistory_.container
			),
			container
		);
	}

	private static void orderQueryByTimestampVon(
		CriteriaQuery<FerienbetreuungAngabenContainerStatusHistory> query,
		CriteriaBuilder cb,
		Root<FerienbetreuungAngabenContainerStatusHistory> root
	) {
		query.orderBy(
			cb.desc(
				root.get(
					FerienbetreuungAngabenContainerStatusHistory_.timestampVon
				)
			)
		);
	}

	private Optional<FerienbetreuungAngabenContainerStatusHistory> getSingleResult(
		CriteriaQuery<FerienbetreuungAngabenContainerStatusHistory> query
	) {
		try {
			return Optional.of(
				persistence.getEntityManager()
					.createQuery(query)
					.setFirstResult(0)
					.setMaxResults(1)
					.getSingleResult()
			);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	public void removeHistory(FerienbetreuungAngabenContainer antrag) {
		getHistory(antrag).forEach(history -> persistence.remove(history));
	}
}
