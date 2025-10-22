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

package ch.dvbern.ebegu.services.lastenausgleich;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.BGCalculationResult_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.entities.LastenausgleichDetailZeitabschnitt;
import ch.dvbern.ebegu.entities.LastenausgleichDetailZeitabschnitt_;
import ch.dvbern.ebegu.entities.LastenausgleichDetail_;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen;
import ch.dvbern.ebegu.entities.Lastenausgleich_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt_;
import ch.dvbern.ebegu.entities.Verfuegung_;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.lastenausgleich.LastenausgleichZeitabschnitteDTO;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.AbstractBaseService;

/**
 * Service fuer den Lastenausgleich
 */
@Stateless
public class LastenausgleichServiceBean extends AbstractBaseService {

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private LastenausgleichGrundlageServiceBean lastenausgleichGrundlageService;

	@Nonnull
	public Collection<Lastenausgleich> getAllLastenausgleiche(
		@Nonnull Mandant mandant
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Lastenausgleich> query = cb.createQuery(
			Lastenausgleich.class
		);
		Root<Lastenausgleich> root = query.from(Lastenausgleich.class);
		var mandantPredicate = cb.equal(
			root.get(Lastenausgleich_.mandant),
			mandant
		);
		query.where(mandantPredicate);
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	public Collection<Lastenausgleich> getLastenausgleicheForGemeinden(
		@Nonnull Set<Gemeinde> gemeinden,
		@Nonnull Mandant mandant
	) {
		return this.getAllLastenausgleiche(mandant)
			.stream()
			.map(lastenausgleich -> {
				Lastenausgleich clone = new Lastenausgleich();
				// filter gemeinden that are not in the list
				clone.setLastenausgleichDetails(
					lastenausgleich.getLastenausgleichDetails()
						.stream()
						.filter(
							lastenausgleichDetail -> gemeinden
								.contains(
									lastenausgleichDetail
										.getGemeinde()
								)
						)
						.collect(Collectors.toList())
				);
				// set total from filtered gemeinden
				clone.setTotalAlleGemeinden(
					clone.getLastenausgleichDetails()
						.stream()
						.reduce(
							BigDecimal.ZERO,
							(
								subtotal,
								lastenausgleichDetail
							) -> subtotal.add(
								lastenausgleichDetail
									.getBetragLastenausgleich()
							)
								.add(
									lastenausgleichDetail
										.getTotalBetragGutscheineOhneSelbstbehalt()
								),
							BigDecimal::add
						)
				);
				clone.setJahr(lastenausgleich.getJahr());
				clone.setId(lastenausgleich.getId());
				clone.setTimestampErstellt(
					lastenausgleich.getTimestampErstellt() != null ?
						lastenausgleich.getTimestampErstellt() :
						LocalDateTime.MIN
				);

				return clone;
			})
			.collect(Collectors.toSet());
	}

	@Nonnull
	public Lastenausgleich findLastenausgleich(
		@Nonnull String lastenausgleichId
	) {
		return persistence.find(Lastenausgleich.class, lastenausgleichId);
	}

	@Nonnull
	public Optional<Lastenausgleich> findLastenausgleichByJahr(int jahr) {
		return criteriaQueryHelper
			.getEntityByUniqueAttribute(
				Lastenausgleich.class,
				jahr,
				Lastenausgleich_.jahr
			);
	}

	public void removeLastenausgleich(@Nonnull String lastenausgleichId) {
		Lastenausgleich lastenausgleichToRemove = findLastenausgleich(
			lastenausgleichId
		);
		CriteriaBuilder cb = persistence.getCriteriaBuilder();

		CriteriaDelete<LastenausgleichDetailZeitabschnitt> deleteZeitabschnitt =
			createDeleteLastenausgleichDetailZeitabschnittStatement(
				lastenausgleichToRemove
			);

		persistence.getEntityManager()
			.createQuery(deleteZeitabschnitt)
			.executeUpdate();

		CriteriaDelete<LastenausgleichDetail> deleteDetail =
			createDeleteLastenausgleichDetailCriteriaStatement(
				cb,
				lastenausgleichToRemove
			);
		persistence.getEntityManager()
			.createQuery(deleteDetail)
			.executeUpdate();

		Optional<LastenausgleichGrundlagen> lastenausgleichGrundlagen =
			lastenausgleichGrundlageService.findLastenausgleichGrundlagen(
				lastenausgleichToRemove.getJahr()
			);
		lastenausgleichGrundlagen.ifPresent(
			lastenausgleichGrundlagen1 -> persistence.remove(
				lastenausgleichGrundlagen1
			)
		);
		persistence.remove(lastenausgleichToRemove);
	}

	private CriteriaDelete<LastenausgleichDetailZeitabschnitt> createDeleteLastenausgleichDetailZeitabschnittStatement(
		Lastenausgleich lastenausgleichToRemove
	) {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaDelete<LastenausgleichDetailZeitabschnitt> deleteZeitabschnitt =
			cb.createCriteriaDelete(LastenausgleichDetailZeitabschnitt.class);
		Root<LastenausgleichDetailZeitabschnitt> rootZeitabschnitt =
			deleteZeitabschnitt.from(LastenausgleichDetailZeitabschnitt.class);

		Subquery<LastenausgleichDetail> subqueryDetailId = deleteZeitabschnitt
			.subquery(LastenausgleichDetail.class);
		Root<LastenausgleichDetail> rootDetail = subqueryDetailId.from(
			LastenausgleichDetail.class
		);

		subqueryDetailId.select(rootDetail)
			.where(
				cb.in(rootDetail.get(LastenausgleichDetail_.lastenausgleich))
					.value(lastenausgleichToRemove)
			);

		deleteZeitabschnitt.where(
			cb.in(
				rootZeitabschnitt.get(
					LastenausgleichDetailZeitabschnitt_.lastenausgleichDetail
				)
			).value(subqueryDetailId)
		);
		return deleteZeitabschnitt;
	}

	private static CriteriaDelete<LastenausgleichDetail> createDeleteLastenausgleichDetailCriteriaStatement(
		CriteriaBuilder cb,
		Lastenausgleich lastenausgleichToRemove
	) {
		CriteriaDelete<LastenausgleichDetail> deleteDetail = cb
			.createCriteriaDelete(LastenausgleichDetail.class);
		Root<LastenausgleichDetail> rootDetailDelete = deleteDetail.from(
			LastenausgleichDetail.class
		);

		deleteDetail.where(
			cb.in(rootDetailDelete.get(LastenausgleichDetail_.lastenausgleich))
				.value(lastenausgleichToRemove)
		);
		return deleteDetail;
	}

	@Nonnull
	public Optional<Lastenausgleich> findLastenausgleich(
		@Nonnull Integer jahr
	) {
		return criteriaQueryHelper.getEntityByUniqueAttribute(
			Lastenausgleich.class,
			jahr,
			Lastenausgleich_.jahr
		);
	}

	public Collection<LastenausgleichDetail> findLastenausgleichDetailForKorrekturen(
		@Nonnull Gemeinde gemeinde
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<LastenausgleichDetail> query = cb.createQuery(
			LastenausgleichDetail.class
		);
		Root<LastenausgleichDetail> root = query.from(
			LastenausgleichDetail.class
		);

		ParameterExpression<Gemeinde> paramGemeinde = cb.parameter(
			Gemeinde.class,
			"paramGemeinde"
		);

		Predicate predicateGemeinde = cb.equal(
			root.get(LastenausgleichDetail_.gemeinde),
			paramGemeinde
		);
		query.where(predicateGemeinde);

		TypedQuery<LastenausgleichDetail> tq = persistence.getEntityManager()
			.createQuery(query);

		tq.setParameter("paramGemeinde", gemeinde);
		return tq.getResultList();
	}

	public List<String> findVerfuegungZeitabschnittIdsFuerLastenausgleichDetail(
		@Nonnull LastenausgleichDetail lastenausgleichDetail
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<String> query = cb.createQuery(
			String.class
		);
		Root<LastenausgleichDetailZeitabschnitt> root = query.from(
			LastenausgleichDetailZeitabschnitt.class
		);

		Join<LastenausgleichDetailZeitabschnitt, VerfuegungZeitabschnitt> verfuegungJoin =
			root.join(LastenausgleichDetailZeitabschnitt_.zeitabschnitt);

		query.select(
			verfuegungJoin.get(VerfuegungZeitabschnitt_.id)
		);

		Predicate predicateLastenausgleichDetail = cb.equal(
			root.get(LastenausgleichDetailZeitabschnitt_.lastenausgleichDetail),
			lastenausgleichDetail
		);
		query.where(predicateLastenausgleichDetail);

		TypedQuery<String> tq = persistence.getEntityManager()
			.createQuery(query);

		return tq.getResultList();
	}

	public Lastenausgleich createLastenausgleich(
		int jahr,
		@Nullable String selbstbehaltPro100ProzentPlatz,
		Mandant mandant
	) {
		Lastenausgleich lastenausgleich = createAndSaveLastenausgleich(
			jahr,
			mandant
		);
		lastenausgleichGrundlageService.createLastenausgleichGrundlage(
			lastenausgleich,
			selbstbehaltPro100ProzentPlatz
		);
		return lastenausgleich;
	}

	private Lastenausgleich createAndSaveLastenausgleich(
		int jahr,
		Mandant mandant
	) {
		Lastenausgleich lastenausgleich = new Lastenausgleich();
		lastenausgleich.setJahr(jahr);
		lastenausgleich.setMandant(mandant);
		return saveLastenausgleich(lastenausgleich);
	}

	public Lastenausgleich saveLastenausgleich(
		Lastenausgleich lastenausgleich
	) {
		if (lastenausgleich.isNew()) {
			return persistence.persist(lastenausgleich);
		}

		return persistence.merge(lastenausgleich);
	}

	public void assertLastenausgleichNotExistingForYear(String jahr) {
		int jahrInt = Integer.parseInt(jahr);

		if (lastenausgleichGrundlageService.findLastenausgleichGrundlagen(
			jahrInt
		)
			.isPresent()) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"assertUnique",
				ErrorCodeEnum.ERROR_LASTENAUSGLEICH_GRUNDLAGEN_EXISTS
			);
		}
		if (findLastenausgleichByJahr(jahrInt)
			.isPresent()) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"assertUnique",
				ErrorCodeEnum.ERROR_LASTENAUSGLEICH_EXISTS
			);
		}
	}

	@Nonnull
	public List<LastenausgleichZeitabschnitteDTO> findZeitabschnitteByGemeinde(
		@Nonnull Gemeinde gemeinde
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<LastenausgleichZeitabschnitteDTO> query = cb
			.createQuery(
				LastenausgleichZeitabschnitteDTO.class
			);
		List<Predicate> predicatesToUse = new ArrayList<>();

		Root<VerfuegungZeitabschnitt> root = query.from(
			VerfuegungZeitabschnitt.class
		);

		Join<VerfuegungZeitabschnitt, BGCalculationResult> joinResult = root
			.join(
				VerfuegungZeitabschnitt_.bgCalculationResultAsiv
			);

		Join<VerfuegungZeitabschnitt, Verfuegung> joinVerfuegung = root.join(
			VerfuegungZeitabschnitt_.verfuegung
		);
		Join<Verfuegung, Betreuung> joinBetreuung = joinVerfuegung.join(
			Verfuegung_.betreuung
		);

		ParameterExpression<Gemeinde> parameterGemeinde = cb.parameter(
			Gemeinde.class,
			"gemeinde"
		);

		predicatesToUse.add(
			cb.equal(
				joinBetreuung.get(Betreuung_.betreuungsstatus),
				Betreuungsstatus.VERFUEGT
			)
		);

		Join<Betreuung, KindContainer> joinKindContainer = joinBetreuung.join(
			Betreuung_.kind
		);

		predicatesToUse.add(cb.isTrue(joinBetreuung.get(Betreuung_.gueltig)));
		Join<Gesuch, Dossier> joinDossier = joinKindContainer
			.join(KindContainer_.gesuch)
			.join(Gesuch_.dossier);
		predicatesToUse.add(
			cb.equal(joinDossier.get(Dossier_.gemeinde), parameterGemeinde)
		);

		query.select(
			cb.construct(
				LastenausgleichZeitabschnitteDTO.class,
				root.get(VerfuegungZeitabschnitt_.id),
				root.get(VerfuegungZeitabschnitt_.gueltigkeit),
				joinResult.get(BGCalculationResult_.betreuungspensumProzent),
				joinResult.get(BGCalculationResult_.anspruchspensumProzent),
				joinResult.get(BGCalculationResult_.verguenstigung),
				joinKindContainer.get(
					KindContainer_.keinSelbstbehaltDurchGemeinde
				)
			)
		);

		query.where(
			CriteriaQueryHelper.concatenateExpressions(cb, predicatesToUse)
		);

		TypedQuery<LastenausgleichZeitabschnitteDTO> typedQuery = persistence
			.getEntityManager()
			.createQuery(query);
		typedQuery.setParameter(parameterGemeinde, gemeinde);

		return typedQuery.getResultList();
	}
}
