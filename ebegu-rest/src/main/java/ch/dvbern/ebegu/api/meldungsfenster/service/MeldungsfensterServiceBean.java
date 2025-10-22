/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.meldungsfenster.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import ch.dvbern.ebegu.api.meldungsfenster.util.MeldungsfensterZielgruppeMapper;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.meldungsfenster.Meldungsfenster;
import ch.dvbern.ebegu.entities.meldungsfenster.Meldungsfenster_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.types.DateTimeRange_;

@Stateless
public class MeldungsfensterServiceBean {

	@Inject
	private Persistence persistence;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private MeldungsfensterZielgruppeMapper meldungsfensterZielgruppeMapper;

	@Nonnull
	public Meldungsfenster createMeldungsfenster(
		@Nonnull Meldungsfenster meldungsfenster
	) {
		Objects.requireNonNull(meldungsfenster);
		return persistence.persist(meldungsfenster);
	}

	@Nonnull
	public Meldungsfenster updateMeldungsfenster(
		@Nonnull Meldungsfenster meldungsfenster
	) {
		Objects.requireNonNull(meldungsfenster);
		return persistence.merge(meldungsfenster);
	}

	@Nonnull
	public Meldungsfenster findMeldungsfenster(
		@Nonnull final String id
	) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		Meldungsfenster meldungsfenster = persistence.find(
			Meldungsfenster.class,
			id
		);
		if (meldungsfenster == null) {
			throw new EbeguEntityNotFoundException(
				"updateMeldungsfenster",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				id
			);
		}
		return meldungsfenster;
	}

	public void delete(@Nonnull Meldungsfenster meldungsfenster) {
		persistence.remove(meldungsfenster);
	}

	public Collection<Meldungsfenster> getAll() {
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<Meldungsfenster> query = builder.createQuery(
			Meldungsfenster.class
		);
		final Root<Meldungsfenster> root = query.from(Meldungsfenster.class);
		Predicate mandantPredicate = builder.equal(
			root.get(Meldungsfenster_.MANDANT),
			principalBean.getMandant()
		);
		query.where(mandantPredicate);

		return persistence.getEntityManager()
			.createQuery(query)
			.getResultList();
	}

	public Collection<Meldungsfenster> getActiveForBenutzer() {
		final CriteriaBuilder builder = persistence.getCriteriaBuilder();
		final CriteriaQuery<Meldungsfenster> query = builder.createQuery(
			Meldungsfenster.class
		);
		final Root<Meldungsfenster> root = query.from(Meldungsfenster.class);
		Predicate mandantPredicate = builder.equal(
			root.get(Meldungsfenster_.MANDANT),
			principalBean.getMandant()
		);
		Predicate gueltigNow =
			builder.between(
				builder.literal(LocalDateTime.now()),
				root.get(
					Meldungsfenster_.gueltigkeit
				).get(DateTimeRange_.gueltigAb),
				root.get(
					Meldungsfenster_.gueltigkeit
				).get(DateTimeRange_.gueltigBis)
			);
		query.where(mandantPredicate, gueltigNow);

		var meldungsfensters = persistence.getEntityManager()
			.createQuery(query)
			.getResultList();
		return meldungsfensters.stream()
			.filter(meldungsfensterZielgruppeMapper::hasUserRoleInZielGruppe)
			.collect(Collectors.toList());
	}

}
