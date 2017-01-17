package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsperiode_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.lib.cdipersistence.Persistence;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer Gesuchsperiode
 */
@Stateless
@Local(GesuchsperiodeService.class)
@PermitAll
public class GesuchsperiodeServiceBean extends AbstractBaseService implements GesuchsperiodeService {

	@Inject
	private Persistence<Gesuchsperiode> persistence;


	@Nonnull
	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN})
	public Gesuchsperiode saveGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		Objects.requireNonNull(gesuchsperiode);
		return persistence.merge(gesuchsperiode);
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<Gesuchsperiode> findGesuchsperiode(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		Gesuchsperiode gesuchsperiode = persistence.find(Gesuchsperiode.class, key);
		return Optional.ofNullable(gesuchsperiode);
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<Gesuchsperiode> getAllGesuchsperioden() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuchsperiode> query = cb.createQuery(Gesuchsperiode.class);
		Root<Gesuchsperiode> root = query.from(Gesuchsperiode.class);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb)));
		return persistence.getCriteriaResults(query);

	}

	@Override
	@RolesAllowed({SUPER_ADMIN, ADMIN})
	public void removeGesuchsperiode(@Nonnull String gesuchsperiodeId) {
		Objects.requireNonNull(gesuchsperiodeId);
		Optional<Gesuchsperiode> gesuchsperiodeToRemove = findGesuchsperiode(gesuchsperiodeId);
		gesuchsperiodeToRemove.orElseThrow(() -> new EbeguEntityNotFoundException("removeGesuchsperiode", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchsperiodeId));
		persistence.remove(gesuchsperiodeToRemove.get());
	}

	@Override
	@Nonnull
	@PermitAll
	public Collection<Gesuchsperiode> getAllActiveGesuchsperioden() {
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuchsperiode> query = builder.createQuery(Gesuchsperiode.class);
		final Root<Gesuchsperiode> root = query.from(Gesuchsperiode.class);
		query.where(builder.equal(root.get(Gesuchsperiode_.active), Boolean.TRUE));
		query.orderBy(builder.desc(root.get(Gesuchsperiode_.gueltigkeit).get(DateRange_.gueltigAb)));
		return persistence.getCriteriaResults(query);
	}

	/**
	 * @return all Gesuchsperioden that have a gueltigkeitBis Date that is in the future (compared to the current date)
	 */
	@Override
	@Nonnull
	@PermitAll
	public Collection<Gesuchsperiode> getAllNichtAbgeschlosseneGesuchsperioden() {
		// Alle Gesuchsperioden, die aktuell am laufen sind oder in der Zukunft liegen, d.h. deren Ende-Datum nicht in der Vergangenheit liegt
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuchsperiode> query = cb.createQuery(Gesuchsperiode.class);
		Root<Gesuchsperiode> root = query.from(Gesuchsperiode.class);
		query.select(root);

		ParameterExpression<LocalDate> dateParam = cb.parameter(LocalDate.class, "date");
		Predicate predicate = cb.greaterThanOrEqualTo(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis), dateParam);

		query.where(predicate);
		TypedQuery<Gesuchsperiode> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(dateParam, LocalDate.now());
		query.orderBy(cb.asc(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb)));
		return q.getResultList();
	}

}
