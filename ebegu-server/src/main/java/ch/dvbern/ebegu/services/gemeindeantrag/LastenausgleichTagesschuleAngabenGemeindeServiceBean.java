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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer_;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EntityExistsException;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.lib.cdipersistence.Persistence;
import com.google.common.base.Preconditions;

/**
 * Service fuer den Lastenausgleich der Tagesschulen
 */
@Stateless
@Local(LastenausgleichTagesschuleAngabenGemeindeService.class)
public class LastenausgleichTagesschuleAngabenGemeindeServiceBean extends AbstractBaseService
	implements LastenausgleichTagesschuleAngabenGemeindeService {

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private PrincipalBean principal;

	@Inject
	private LastenausgleichTagesschuleAngabenInstitutionService angabenInstitutionService;

	@Inject
	private LastenausgleichTagesschuleAngabenGemeindeStatusHistoryService historyService;

	@Override
	@Nonnull
	public List<? extends GemeindeAntrag> createLastenausgleichTagesschuleGemeinde(
		@Nonnull Gesuchsperiode gesuchsperiode
	) {
		Objects.requireNonNull(gesuchsperiode);

		List<GemeindeAntrag> result = new ArrayList<>();
		final Collection<Gemeinde> aktiveGemeinden = gemeindeService.getAktiveGemeinden();
		for (Gemeinde gemeinde : aktiveGemeinden) {
			Optional<LastenausgleichTagesschuleAngabenGemeindeContainer> existingOptional =
				findLastenausgleichTagesschuleAngabenGemeindeContainer(gemeinde, gesuchsperiode);
			if (existingOptional.isPresent()) {
				throw new EntityExistsException(
					LastenausgleichTagesschuleAngabenGemeindeContainer.class,
					"LastenausgleichTagesschule Gemeinde Angaben existieren für gemeinde und periode bereits",
					gemeinde.getName() + ' ' + gesuchsperiode.getGesuchsperiodeString());
			}
			LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer =
				new LastenausgleichTagesschuleAngabenGemeindeContainer();
			fallContainer.setGesuchsperiode(gesuchsperiode);
			fallContainer.setGemeinde(gemeinde);
			fallContainer.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.NEU);
			fallContainer.setAngabenKorrektur(null);    // Wird erst mit den Daten initialisiert, da alles zwingend
			fallContainer.setAngabenDeklaration(null);    // Wird bei Freigabe rueberkopiert
			final LastenausgleichTagesschuleAngabenGemeindeContainer saved =
				saveLastenausgleichTagesschuleGemeinde(fallContainer, true);
			angabenInstitutionService.createLastenausgleichTagesschuleInstitution(saved);
			result.add(saved);
		}
		return result;
	}

	@Nonnull
	@Override
	public Optional<LastenausgleichTagesschuleAngabenGemeindeContainer> findLastenausgleichTagesschuleAngabenGemeindeContainer(
		@Nonnull String id
	) {
		Objects.requireNonNull(id, "id muss gesetzt sein");

		LastenausgleichTagesschuleAngabenGemeindeContainer container =
			persistence.find(LastenausgleichTagesschuleAngabenGemeindeContainer.class, id);
		return Optional.ofNullable(container);
	}

	@Nonnull
	@Override
	public Optional<LastenausgleichTagesschuleAngabenGemeindeContainer> findLastenausgleichTagesschuleAngabenGemeindeContainer(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode
	) {
		Objects.requireNonNull(gemeinde, "gemeinde muss gesetzt sein");
		Objects.requireNonNull(gesuchsperiode, "gesuchsperiode muss gesetzt sein");

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<LastenausgleichTagesschuleAngabenGemeindeContainer> query =
			cb.createQuery(LastenausgleichTagesschuleAngabenGemeindeContainer.class);
		Root<LastenausgleichTagesschuleAngabenGemeindeContainer> root =
			query.from(LastenausgleichTagesschuleAngabenGemeindeContainer.class);

		Predicate gemeindePredicate =
			cb.equal(root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.gemeinde), gemeinde);
		Predicate gesuchsperiodePredicate =
			cb.equal(root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.gesuchsperiode), gesuchsperiode);

		query.where(cb.and(gemeindePredicate, gesuchsperiodePredicate));
		return Optional.ofNullable(persistence.getCriteriaSingleResult(query));
	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeContainer saveLastenausgleichTagesschuleGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer
	) {
		return saveLastenausgleichTagesschuleGemeinde(fallContainer, false);
	}

	@Nonnull
	private LastenausgleichTagesschuleAngabenGemeindeContainer saveLastenausgleichTagesschuleGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer,
		boolean saveInStatusHistory
	) {
		Objects.requireNonNull(fallContainer);
		authorizer.checkWriteAuthorization(fallContainer);

		final LastenausgleichTagesschuleAngabenGemeindeContainer saved = persistence.merge(fallContainer);
		if (saveInStatusHistory) {
			historyService.saveLastenausgleichTagesschuleStatusChange(saved);
		}
		return saved;
	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeFuerInstitutionenFreigeben(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer
	) {
		// Nur moeglich, wenn noch OFFEN und die zwingenden Fragen beantwortet
		Preconditions.checkState(
			fallContainer.getStatus() == LastenausgleichTagesschuleAngabenGemeindeStatus.NEU,
			"LastenausgleichAngabenGemeinde muss im Status NEU sein");
		Preconditions.checkNotNull(
			fallContainer.getAlleAngabenInKibonErfasst(),
			"Die zwingenden Fragen muessen zu diesem Zeitpunkt beantwortet sein");

		fallContainer.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE);
		return saveLastenausgleichTagesschuleGemeinde(fallContainer, true);
	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeEinreichen(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer
	) {
		Objects.requireNonNull(fallContainer);
		authorizer.checkWriteAuthorization(fallContainer);

		// Nur moeglich, wenn noch nicht freigegeben und ueberhaupt Daten zum kopieren vorhanden
		Preconditions.checkState(
			fallContainer.getStatus() == LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE,
			"LastenausgleichAngabenGemeinde muss im Status IN_BEARBEITUNG_GEMEINDE sein");
		Preconditions.checkArgument(
			fallContainer.getAngabenInstitutionContainers()
				.stream()
				.allMatch(LastenausgleichTagesschuleAngabenInstitutionContainer::isAntragAbgeschlossen),
			"Alle LastenausgleichAngabenInstitution muessen abgeschlossen sein");
		Objects.requireNonNull(fallContainer.getAngabenKorrektur());

		fallContainer.copyForFreigabe();
		fallContainer.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON);
		return saveLastenausgleichTagesschuleGemeinde(fallContainer, true);
	}

	@Nonnull
	@Override
	public List<LastenausgleichTagesschuleAngabenGemeindeContainer> getAllLastenausgleicheTagesschulen() {
		Set<Gemeinde> gemeinden = principal.getBenutzer().extractGemeindenForUser();

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<LastenausgleichTagesschuleAngabenGemeindeContainer> query =
			cb.createQuery(LastenausgleichTagesschuleAngabenGemeindeContainer.class);
		Root<LastenausgleichTagesschuleAngabenGemeindeContainer> root =
			query.from(LastenausgleichTagesschuleAngabenGemeindeContainer.class);

		if (!principal.isCallerInAnyOfRole(
			UserRole.SUPER_ADMIN,
			UserRole.ADMIN_MANDANT,
			UserRole.SACHBEARBEITER_MANDANT)) {
			Predicate gemeindeIn =
				root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.gemeinde).in(gemeinden);
			query.where(gemeindeIn);
		}

		return persistence.getCriteriaResults(query);
	}

	@Nullable
	@Override
	public List<LastenausgleichTagesschuleAngabenGemeindeContainer> getLastenausgleicheTagesschulen(
		@Nullable String gemeinde,
		@Nullable String periode,
		@Nullable String status
	) {
		Set<Gemeinde> gemeinden = principal.getBenutzer().extractGemeindenForUser();

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<LastenausgleichTagesschuleAngabenGemeindeContainer> query =
			cb.createQuery(LastenausgleichTagesschuleAngabenGemeindeContainer.class);
		Root<LastenausgleichTagesschuleAngabenGemeindeContainer> root =
			query.from(LastenausgleichTagesschuleAngabenGemeindeContainer.class);

		if (!principal.isCallerInAnyOfRole(
			UserRole.SUPER_ADMIN,
			UserRole.ADMIN_MANDANT,
			UserRole.SACHBEARBEITER_MANDANT)) {
			Predicate gemeindeIn =
				root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.gemeinde).in(gemeinden);
			query.where(gemeindeIn);
		}

		if (gemeinde != null) {
			query.where(
				cb.equal(
					root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.gemeinde).get(Gemeinde_.name),
					gemeinde)
			);
		}
		if (periode != null) {
			String[] years = Arrays.stream(periode.split("/"))
				.map(year -> year.length() == 4 ? year : "20".concat(year))
				.collect(Collectors.toList())
				.toArray(String[]::new);
			Path<DateRange> dateRangePath =
				root.join(LastenausgleichTagesschuleAngabenGemeindeContainer_.gesuchsperiode, JoinType.INNER)
					.get(AbstractDateRangedEntity_.gueltigkeit);
			query.where(
				cb.and(
					cb.equal(cb.function("year", Integer.class, dateRangePath.get(DateRange_.gueltigAb)), years[0]),
					cb.equal(cb.function("year", Integer.class, dateRangePath.get(DateRange_.gueltigBis)), years[1])
				)
			);
		}
		if (status != null) {
			query.where(
				cb.equal(
					root.get(LastenausgleichTagesschuleAngabenGemeindeContainer_.status),
					LastenausgleichTagesschuleAngabenGemeindeStatus.valueOf(status))
			);
		}

		return persistence.getCriteriaResults(query);
	}

	@Override
	public void saveKommentar(@Nonnull String containerId, @Nonnull String kommentar) {
		LastenausgleichTagesschuleAngabenGemeindeContainer latsContainer =
			this.findLastenausgleichTagesschuleAngabenGemeindeContainer(containerId)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"saveKommentar",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					containerId)
				);
		latsContainer.setInternerKommentar(kommentar);
		persistence.persist(latsContainer);
	}
}


