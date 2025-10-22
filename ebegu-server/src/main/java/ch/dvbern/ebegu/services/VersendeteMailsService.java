package ch.dvbern.ebegu.services;

import java.util.Collection;

import javax.annotation.Nonnull;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.VersendeteMail;
import ch.dvbern.ebegu.entities.VersendeteMail_;
import ch.dvbern.ebegu.persistence.Persistence;

@Stateless
public class VersendeteMailsService extends AbstractBaseService {
	@Inject
	private Persistence persistence;

	@Inject
	private PrincipalBean principalBean;

	@Nonnull
	public VersendeteMail saveVersendeteMail(
		@Nonnull VersendeteMail versendeteMail
	) {
		return persistence.persist(versendeteMail);
	}

	@Nonnull
	public Collection<VersendeteMail> getAll(
		@Nonnull String active,
		@Nonnull String filter,
		@Nonnull String sortDirection,
		@Nonnull Integer pageIndex,
		@Nonnull Integer pageSize
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
			filter,
			builder,
			root
		);
		query.where(mandantPredicate, filterPredicate);
		if (sortDirection.isEmpty() || sortDirection.equals("desc")) {
			query.orderBy(builder.desc(root.get(active)));
		} else {
			query.orderBy(builder.asc(root.get(active)));
		}

		return persistence.getEntityManager()
			.createQuery(query)
			.setMaxResults(pageSize)
			.setFirstResult((pageIndex) * pageSize)
			.getResultList();
	}

	public long countVerendeteMails(String filter) {
		CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<Long> query = builder.createQuery(Long.class);
		final Root<VersendeteMail> root = query.from(VersendeteMail.class);
		Predicate mandantPredicate = builder.equal(
			root.get(VersendeteMail_.MANDANT_IDENTIFIER),
			principalBean.getMandant().getMandantIdentifier()
		);
		Predicate filterPredicate = createFilterPredicate(
			filter,
			builder,
			root
		);
		query.where(mandantPredicate, filterPredicate);

		query.select(builder.count(root));
		return persistence.getEntityManager()
			.createQuery(query)
			.getSingleResult();
	}

	private static Predicate createFilterPredicate(
		String filter,
		CriteriaBuilder builder,
		Root<VersendeteMail> root
	) {
		Expression<String> formattedDate = builder.function(
			"DATE_FORMAT",
			String.class,
			root.get(VersendeteMail_.ZEITPUNKT_VERSAND),
			builder.literal("%d.%m.%Y %H:%i:%s")
		);
		String wildcardFilter = "%" + filter + "%";
		return builder.or(
			builder.like(
				root.get(VersendeteMail_.EMPFAENGER_ADRESSE),
				wildcardFilter
			),
			builder.like(root.get(VersendeteMail_.BETREFF), wildcardFilter),
			builder.like(formattedDate, wildcardFilter)
		);
	}
}
