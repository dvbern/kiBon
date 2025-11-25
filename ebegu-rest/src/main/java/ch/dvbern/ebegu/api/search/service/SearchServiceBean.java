/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.search.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragPredicateObjectDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragTableFilterDTO;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AbstractPersonEntity_;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Benutzer_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Fall_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsperiode_;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer_;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragStatusDTO;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.GesuchBetreuungenStatus;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.SearchMode;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.services.InternePendenzService;
import ch.dvbern.ebegu.services.util.PredicateHelper;
import ch.dvbern.ebegu.services.util.SearchUtil;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.AntragStatusConverterUtil;
import ch.dvbern.ebegu.util.Constants;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service zum Suchen.
 */
@Stateless
public class SearchServiceBean extends AbstractBaseService {
	private static final Logger LOG = LoggerFactory.getLogger(
		SearchServiceBean.class.getSimpleName()
	);

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private Persistence persistence;

	@Inject
	private InternePendenzService internePendenzService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Authorizer authorizer;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	public List<Gesuch> searchPendenzen(
		@Nonnull AntragTableFilterDTO antragTableFilterDto
	) {
		return searchAntraege(antragTableFilterDto, true);
	}

	public List<Gesuch> searchAllAntraege(
		@Nonnull AntragTableFilterDTO antragTableFilterDto
	) {
		return searchAntraege(antragTableFilterDto, false);
	}

	public Long countPendenzen(
		@Nonnull AntragTableFilterDTO antragTableFilterDto
	) {
		return countAntraege(antragTableFilterDto, true);
	}

	public Long countAllAntraege(
		@Nonnull AntragTableFilterDTO antragTableFilterDto
	) {
		return countAntraege(antragTableFilterDto, false);
	}

	@Nonnull
	private List<Gesuch> searchAntraege(
		@Nonnull AntragTableFilterDTO antragTableFilterDto,
		boolean searchForPendenzen
	) {
		Pair<Long, List<Gesuch>> searchResult =
			searchAntraege(
				antragTableFilterDto,
				SearchMode.SEARCH,
				searchForPendenzen
			);
		return searchResult.getRight();
	}

	@Nonnull
	private Long countAntraege(
		@Nonnull AntragTableFilterDTO antragTableFilterDto,
		boolean searchForPendenzen
	) {
		Long countResult = searchAntraege(
			antragTableFilterDto,
			SearchMode.COUNT,
			searchForPendenzen
		).getLeft();
		return countResult;
	}

	private Pair<Long, List<Gesuch>> searchAntraege(
		@Nonnull AntragTableFilterDTO antragTableFilterDto,
		@Nonnull SearchMode mode,
		boolean searchForPendenzen
	) {
		Benutzer user = benutzerService.getCurrentBenutzer()
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"searchAllAntraege",
					"No User is logged in"
				)
			);

		UserRole role = user.getRole();

		Set<AntragStatus> allowedAntragStatus = getAntragStatuses(
			searchForPendenzen,
			role
		);

		if (allowedAntragStatus.isEmpty()) {
			return new ImmutablePair<>(0L, Collections.emptyList());
		}

		CriteriaBuilder cb = persistence.getCriteriaBuilder();

		@SuppressWarnings("rawtypes") // Je nach Abfrage ist es String oder Long
		CriteriaQuery query = SearchUtil.getQueryForSearchMode(
			cb,
			mode,
			"searchAllAntraege"
		);

		// Construct from-clause
		@SuppressWarnings("unchecked") // Je nach Abfrage ist das Query String oder Long
		Root<Gesuch> root = query.from(Gesuch.class);

		SearchServiceContext searchServiceContext = new SearchServiceContext(
			user,
			root
		);

		//prepare predicates
		List<Predicate> predicates = new ArrayList<>();

		// General role based predicates
		Predicate inClauseStatus = root.get(Gesuch_.status)
			.in(allowedAntragStatus);
		List<String> internePendenzGesuchIds = null;
		if (searchForPendenzen) {
			internePendenzGesuchIds = internePendenzService
				.findAlleAbgelaufendeInternePendenzen()
				.stream()
				.map(
					internePendenz -> internePendenz.getGesuch().getId()
				)
				.toList();
			if (!(role.isRoleSozialdienstabhaengig()
				|| internePendenzGesuchIds.isEmpty())) {
				// Verfügte Gesuche mit internen, abgelaufenen Pendenzen sind für Unterstützungsdienste nicht relevant.
				// Darum schliessen wie sie hier aus.
				Predicate extraStatus = root.get(Gesuch_.status)
					.in(AntragStatus.VERFUEGT, AntragStatus.NUR_SCHULAMT);
				Predicate gesuchsIds = root.get(AbstractEntity_.id)
					.in(internePendenzGesuchIds);
				predicates.add(
					cb.and(
						cb.or(
							inClauseStatus,
							cb.and(extraStatus, gesuchsIds)
						)
					)
				);
			} else {
				predicates.add(inClauseStatus);
			}
		} else {
			predicates.add(inClauseStatus);
		}

		setMandantFilterForCurrentUser(cb, predicates, searchServiceContext);
		setGemeindeFilterForCurrentUser(user, predicates, searchServiceContext);
		setOnlyAktivePeriodenPredicate(
			cb,
			searchServiceContext.getJoinGesuchsperiode(),
			predicates,
			antragTableFilterDto.isOnlyAktivePerioden()
		);

		// Predicates derived from PredicateDTO (Filter coming from client)
		AntragPredicateObjectDTO predicateObjectDto =
			antragTableFilterDto.getSearch() != null ?
				antragTableFilterDto.getSearch().getPredicateObject() :
				null;

		// Special role based predicates
		switch (role) {
		case SUPER_ADMIN:
		case ADMIN_GEMEINDE:
		case SACHBEARBEITER_GEMEINDE:
			// Diese Rollen haben keine (rollenspezifischen) Einschränkungen!
			break;
		case SACHBEARBEITER_BG:
		case ADMIN_BG:
		case REVISOR:
		case JURIST:
		case ADMIN_MANDANT:
		case SACHBEARBEITER_MANDANT:
			if (searchForPendenzen) {
				Predicate jaOrMischGesuche = createPredicateJAOrMischGesuche(
					cb,
					searchServiceContext.getJoinDossier()
				);
				if (!internePendenzGesuchIds.isEmpty()) {
					Predicate gesuchsIds = root.get(AbstractEntity_.id)
						.in(internePendenzGesuchIds);
					predicates.add(cb.and(cb.or(jaOrMischGesuche, gesuchsIds)));
				} else {
					predicates.add(jaOrMischGesuche);
				}
			}
			break;
		case STEUERAMT:
			break;
		case ADMIN_SOZIALDIENST:
		case SACHBEARBEITER_SOZIALDIENST:
			predicates.add(
				cb.equal(
					searchServiceContext.getJoinSozialdienstFall()
						.get(
							SozialdienstFall_.sozialdienst
						),
					user.getSozialdienst()
				)
			);
			break;
		case ADMIN_TRAEGERSCHAFT:
		case SACHBEARBEITER_TRAEGERSCHAFT:
			createPredicateForTraegerschaft(
				predicates,
				predicateObjectDto,
				cb,
				searchServiceContext
			);
			break;
		case ADMIN_INSTITUTION:
		case SACHBEARBEITER_INSTITUTION:
			createPredicateForInstitution(
				predicates,
				predicateObjectDto,
				cb,
				searchServiceContext
			);
			break;
		case SACHBEARBEITER_TS:
		case ADMIN_TS:
			if (searchForPendenzen) {
				Predicate schOrMischGesuche = createPredicateSCHOrMischGesuche(
					cb,
					root,
					searchServiceContext.getJoinDossier()
				);
				if (!internePendenzGesuchIds.isEmpty()) {
					Predicate gesuchsIds = root.get(AbstractEntity_.id)
						.in(internePendenzGesuchIds);
					predicates.add(
						cb.and(cb.or(schOrMischGesuche, gesuchsIds))
					);
				} else {
					predicates.add(schOrMischGesuche);
				}
			}
			break;
		default:
			LOG.warn(
				"antragSearch can not be performed by users in role {}",
				role
			);
			predicates.add(cb.isFalse(cb.literal(Boolean.TRUE))); // impossible predicate
			break;
		}

		if (predicateObjectDto != null) {
			if (predicateObjectDto.getFallNummer() != null) {
				// Die Fallnummer muss als String mit LIKE verglichen werden: Bei Eingabe von "14" soll der Fall "114"
				// kommen
				Expression<String> fallNummerAsString = searchServiceContext
					.getJoinFall()
					.get(
						Fall_.fallNummer
					)
					.as(String.class);
				String fallNummerWithWildcards = SearchUtil.withWildcards(
					predicateObjectDto.getFallNummer()
				);
				predicates.add(
					cb.like(fallNummerAsString, fallNummerWithWildcards)
				);
			}
			if (predicateObjectDto.getGemeinde() != null) {
				Expression<String> gemeindeExpression = searchServiceContext
					.getJoinDossier()
					.get(
						Dossier_.gemeinde
					)
					.get(Gemeinde_.name);
				predicates.add(
					cb.like(
						gemeindeExpression,
						predicateObjectDto.getGemeinde()
					)
				);
			}
			if (predicateObjectDto.getFamilienName() != null) {
				Join<Gesuch, GesuchstellerContainer> gesuchsteller1 = root.join(
					Gesuch_.gesuchsteller1,
					JoinType.LEFT
				);
				Join<Gesuch, GesuchstellerContainer> gesuchsteller2 = root.join(
					Gesuch_.gesuchsteller2,
					JoinType.LEFT
				);
				Join<GesuchstellerContainer, Gesuchsteller> gesuchsteller1JA =
					gesuchsteller1.join(
						GesuchstellerContainer_.gesuchstellerJA,
						JoinType.LEFT
					);
				Join<GesuchstellerContainer, Gesuchsteller> gesuchsteller2JA =
					gesuchsteller2.join(
						GesuchstellerContainer_.gesuchstellerJA,
						JoinType.LEFT
					);
				predicates.add(
					cb.or(
						cb.like(
							gesuchsteller1JA.get(
								AbstractPersonEntity_.nachname
							),
							predicateObjectDto
								.getFamilienNameForLike()
						),
						cb.like(
							gesuchsteller2JA.get(
								AbstractPersonEntity_.nachname
							),
							predicateObjectDto
								.getFamilienNameForLike()
						)
					)
				);
			}
			if (predicateObjectDto.getAntragTyp() != null) {
				List<AntragTyp> values = AntragTyp.getValuesForFilter(
					predicateObjectDto.getAntragTyp()
				);

				if (!values.isEmpty()) {
					predicates.add(root.get(Gesuch_.typ).in(values));
				}
			}
			if (predicateObjectDto.getGesuchsperiodeString() != null) {
				Predicate gesuchsperiodeFiler = PredicateHelper
					.getPredicateFilterGesuchsperiode(
						cb,
						searchServiceContext.getJoinGesuchsperiode(),
						predicateObjectDto.getGesuchsperiodeString()
					);
				predicates.add(gesuchsperiodeFiler);
			}
			if (predicateObjectDto.getEingangsdatum() != null) {
				try {
					LocalDate searchDate =
						LocalDate.parse(
							predicateObjectDto.getEingangsdatum(),
							Constants.DATE_FORMATTER
						);
					predicates.add(
						cb.equal(
							root.get(Gesuch_.eingangsdatum),
							searchDate
						)
					);
				} catch (DateTimeParseException e) {
					// Kein gueltiges Datum. Es kann kein Gesuch geben, welches passt. Wir geben leer zurueck
					return new ImmutablePair<>(0L, Collections.emptyList());
				}
			}
			if (predicateObjectDto.getEingangsdatumSTV() != null) {
				try {
					LocalDate searchDate =
						LocalDate.parse(
							predicateObjectDto.getEingangsdatumSTV(),
							Constants.DATE_FORMATTER
						);
					predicates.add(
						cb.equal(
							root.get(Gesuch_.eingangsdatumSTV),
							searchDate
						)
					);
				} catch (DateTimeParseException e) {
					// Kein gueltiges Datum. Es kann kein Gesuch geben, welches passt. Wir geben leer zurueck
					return new ImmutablePair<>(0L, Collections.emptyList());
				}
			}
			if (predicateObjectDto.getAenderungsdatum() != null) {
				try {
					// Wir wollen ohne Zeit vergleichen
					Expression<LocalDate> timestampAsLocalDate =
						root.get(AbstractEntity_.timestampMutiert)
							.as(LocalDate.class);
					LocalDate searchDate =
						LocalDate.parse(
							predicateObjectDto.getAenderungsdatum(),
							Constants.DATE_FORMATTER
						);
					predicates.add(cb.equal(timestampAsLocalDate, searchDate));
				} catch (DateTimeParseException e) {
					// Kein gueltiges Datum. Es kann kein Gesuch geben, welches passt. Wir geben leer zurueck
					return new ImmutablePair<>(0L, Collections.emptyList());
				}
			}
			if (predicateObjectDto.getStatus() != null) {
				createPredicateGesuchBetreuungenStatus(
					cb,
					root,
					predicates,
					predicateObjectDto.getStatus()
				);
				// Achtung, hier muss von Client zu Server Status konvertiert werden!
				Collection<AntragStatus> antragStatus =
					AntragStatusConverterUtil.convertStatusToEntityForRole(
						AntragStatusDTO.valueOf(
							predicateObjectDto.getStatus()
						),
						role
					);
				predicates.add(root.get(Gesuch_.status).in(antragStatus));
			}
			if (predicateObjectDto.getDokumenteHochgeladen() != null) {
				predicates.add(
					cb.equal(
						root.get(Gesuch_.dokumenteHochgeladen),
						predicateObjectDto.getDokumenteHochgeladen()
					)
				);
			}
			if (predicateObjectDto.getAngebote() != null) {
				createPredicateForAngebot(
					predicates,
					predicateObjectDto.getAngebote(),
					cb,
					searchServiceContext
				);
			}
			if (predicateObjectDto.getInstitutionen() != null) {
				predicates.add(
					cb.or(
						cb.equal(
							searchServiceContext.getJoinInstitutionBetreuungen()
								.get(
									Institution_.name
								),
							predicateObjectDto.getInstitutionen()
						),
						cb.equal(
							searchServiceContext.getJoinInstitutionFerieninsel()
								.get(
									Institution_.name
								),
							predicateObjectDto.getInstitutionen()
						),
						cb.equal(
							searchServiceContext.getJoinInstitutionTagesschule()
								.get(
									Institution_.name
								),
							predicateObjectDto.getInstitutionen()
						)
					)
				);
			}
			if (predicateObjectDto.getKinder() != null) {
				predicates.add(
					cb.like(
						searchServiceContext.getJoinKinder()
							.get(AbstractPersonEntity_.vorname),
						predicateObjectDto.getKindNameForLike()
					)
				);
			}
			if (predicateObjectDto.getVerantwortlicherBG() != null
				&& !role.isRoleSozialdienstabhaengig()) {
				predicates.add(
					cb.and(
						cb.equal(
							searchServiceContext.getJoinVerantwortlicherBG()
								.get(
									Benutzer_.fullName
								),
							predicateObjectDto
								.getVerantwortlicherBG()
						)
					)
				);
			}
			if (predicateObjectDto.getVerantwortlicherTS() != null
				&& !role.isRoleSozialdienstabhaengig()) {
				predicates.add(
					cb.and(
						cb.equal(
							searchServiceContext.getJoinVerantwortlicherTS()
								.get(
									Benutzer_.fullName
								),
							predicateObjectDto
								.getVerantwortlicherTS()
						)
					)
				);
			}
			if (predicateObjectDto.getVerantwortlicherGemeinde() != null
				&& !role.isRoleSozialdienstabhaengig()) {
				Predicate predicateBG = cb.equal(
					searchServiceContext.getJoinVerantwortlicherBG()
						.get(Benutzer_.fullName),
					predicateObjectDto.getVerantwortlicherGemeinde()
				);
				Predicate predicateTS = cb.equal(
					searchServiceContext.getJoinVerantwortlicherTS()
						.get(Benutzer_.fullName),
					predicateObjectDto.getVerantwortlicherGemeinde()
				);
				predicates.add(
					cb.and(
						cb.or(predicateBG, predicateTS)
					)
				);
			}
			if (predicateObjectDto.getInternePendenz() != null) {
				predicates.add(
					cb.equal(
						root.get(Gesuch_.internePendenz),
						predicateObjectDto.getInternePendenz()
					)
				);
			}
		}
		// Construct the select- and where-clause
		switch (mode) {
		case SEARCH:
			//noinspection unchecked // Je nach Abfrage ist das Query String oder Long
			query.select(root.get(AbstractEntity_.id))
				.where(
					CriteriaQueryHelper.concatenateExpressions(
						cb,
						predicates
					)
				);
			constructOrderByClause(
				antragTableFilterDto,
				cb,
				query,
				root,
				searchServiceContext.getJoinKinder(),
				searchServiceContext.getJoinGesuchsperiode(),
				searchServiceContext.getJoinInstitutionstammdatenBetreuungen(),
				searchServiceContext.getJoinInstitutionBetreuungen()
			);
			break;
		case COUNT:
			//noinspection unchecked // Je nach Abfrage ist das Query String oder Long
			query.select(cb.countDistinct(root.get(AbstractEntity_.id)))
				.where(
					CriteriaQueryHelper.concatenateExpressions(
						cb,
						predicates
					)
				);
			break;
		}

		// Prepare and execute the query and build the result
		Pair<Long, List<Gesuch>> result = null;
		switch (mode) {
		case SEARCH:
			List<String> gesuchIds =
				persistence.getCriteriaResults(query); //select all ids in order, may contain duplicates
			List<Gesuch> pagedResult;
			if (antragTableFilterDto.getPagination() != null) {
				int firstIndex = antragTableFilterDto.getPagination()
					.getStart();
				Integer maxresults = antragTableFilterDto.getPagination()
					.getNumber();
				List<String> orderedIdsToLoad =
					SearchUtil.determineDistinctIdsToLoad(
						gesuchIds,
						firstIndex,
						maxresults
					);
				pagedResult = findGesuche(orderedIdsToLoad);
			} else {
				pagedResult = findGesuche(gesuchIds);
			}

			pagedResult.forEach(authorizer::checkReadAuthorization);

			result = new ImmutablePair<>(null, pagedResult);
			break;
		case COUNT:
			Long count = (Long) persistence.getCriteriaSingleResult(query);
			result = new ImmutablePair<>(count, null);
			break;
		}
		return result;
	}

	private void createPredicateForAngebot(
		List<Predicate> predicates,
		@Nonnull String angebote,
		CriteriaBuilder cb,
		SearchServiceContext searchServiceContext
	) {
		BetreuungsangebotTyp betreuungsangebotTyp = BetreuungsangebotTyp
			.valueOf(
				angebote
			);
		switch (betreuungsangebotTyp) {
		case KITA:
		case TAGESFAMILIEN:
		case MITTAGSTISCH:
			predicates.add(
				cb.equal(
					searchServiceContext
						.getJoinInstitutionstammdatenBetreuungen()
						.get(
							InstitutionStammdaten_.betreuungsangebotTyp
						),
					betreuungsangebotTyp
				)
			);
			break;
		case TAGESSCHULE:
			predicates.add(
				cb.equal(
					searchServiceContext
						.getJoinInstitutionstammdatenTagesschule()
						.get(
							InstitutionStammdaten_.betreuungsangebotTyp
						),
					betreuungsangebotTyp
				)
			);
			break;
		case FERIENINSEL:
			predicates.add(
				cb.equal(
					searchServiceContext
						.getJoinInstitutionstammdatenFerieninsel()
						.get(
							InstitutionStammdaten_.betreuungsangebotTyp
						),
					betreuungsangebotTyp
				)
			);
			break;
		default:
			throw new NotImplementedException();
		}

	}

	private void createPredicateForInstitution(
		List<Predicate> predicates,
		@Nullable AntragPredicateObjectDTO predicateObjectDto,
		CriteriaBuilder cb,
		SearchServiceContext searchServiceContext
	) {
		// es geht hier nicht um die joinInstitution des zugewiesenen benutzers sondern um die joinInstitution des
		// eingeloggten benutzers
		if (predicateObjectDto != null
			&& predicateObjectDto.getAngebote() != null) {
			switch (BetreuungsangebotTyp.valueOf(
				predicateObjectDto.getAngebote()
			)) {
			case KITA, TAGESFAMILIEN, MITTAGSTISCH:
				predicates.add(
					cb.equal(
						searchServiceContext.getJoinInstitutionBetreuungen(),
						searchServiceContext.getUser().getInstitution()
					)
				);
				break;
			case TAGESSCHULE:
				predicates.add(
					cb.equal(
						searchServiceContext.getJoinInstitutionTagesschule(),
						searchServiceContext.getUser().getInstitution()
					)
				);
				break;
			case FERIENINSEL:
				predicates.add(
					cb.equal(
						searchServiceContext.getJoinInstitutionFerieninsel(),
						searchServiceContext.getUser().getInstitution()
					)
				);
				break;
			default:
				throw new EbeguRuntimeException(
					"searchAntraege",
					"BetreuungsangebotTyp nicht gefunden: "
						+ BetreuungsangebotTyp.valueOf(
							predicateObjectDto.getAngebote()
						)
				);
			}
		} else {
			predicates.add(
				cb.or(
					cb.equal(
						searchServiceContext.getJoinInstitutionBetreuungen(),
						searchServiceContext.getUser().getInstitution()
					),
					cb.equal(
						searchServiceContext.getJoinInstitutionFerieninsel(),
						searchServiceContext.getUser().getInstitution()
					),
					cb.equal(
						searchServiceContext.getJoinInstitutionTagesschule(),
						searchServiceContext.getUser().getInstitution()
					)
				)
			);
		}
		predicates.add(
			createPredicateAusgeloesteSCHJAAngebote(
				cb,
				searchServiceContext.getJoinAnmeldungTagesschule(),
				searchServiceContext.getJoinAnmeldungFerieninsel(),
				searchServiceContext.getJoinInstitutionstammdatenBetreuungen(),
				searchServiceContext.getJoinInstitutionstammdatenTagesschule(),
				searchServiceContext.getJoinInstitutionstammdatenFerieninsel()
			)
		);
	}

	private void createPredicateForTraegerschaft(
		List<Predicate> predicates,
		@Nullable AntragPredicateObjectDTO predicateObjectDto,
		CriteriaBuilder cb,
		SearchServiceContext searchServiceContext
	) {
		if (predicateObjectDto != null
			&& predicateObjectDto.getAngebote() != null) {
			switch (BetreuungsangebotTyp.valueOf(
				predicateObjectDto.getAngebote()
			)) {
			case MITTAGSTISCH, KITA, TAGESFAMILIEN:
				predicates.add(
					cb.equal(
						searchServiceContext.getJoinInstitutionBetreuungen()
							.get(
								Institution_.traegerschaft
							),
						searchServiceContext.getUser().getTraegerschaft()
					)
				);
				break;
			case FERIENINSEL:
				predicates.add(
					cb.equal(
						searchServiceContext.getJoinInstitutionFerieninsel()
							.get(
								Institution_.traegerschaft
							),
						searchServiceContext.getUser().getTraegerschaft()
					)
				);
				break;
			case TAGESSCHULE:
				predicates.add(
					cb.equal(
						searchServiceContext.getJoinInstitutionTagesschule()
							.get(
								Institution_.traegerschaft
							),
						searchServiceContext.getUser().getTraegerschaft()
					)
				);
				break;
			default:
				throw new EbeguRuntimeException(
					"searchAntraege",
					"BetreuungsangebotTyp nicht gefunden: "
						+ BetreuungsangebotTyp.valueOf(
							predicateObjectDto.getAngebote()
						)
				);
			}
		} else {
			predicates.add(
				cb.or(
					cb.equal(
						searchServiceContext.getJoinInstitutionBetreuungen()
							.get(
								Institution_.traegerschaft
							),
						searchServiceContext.getUser().getTraegerschaft()
					),
					cb.equal(
						searchServiceContext.getJoinInstitutionFerieninsel()
							.get(
								Institution_.traegerschaft
							),
						searchServiceContext.getUser().getTraegerschaft()
					),
					cb.equal(
						searchServiceContext.getJoinInstitutionTagesschule()
							.get(
								Institution_.traegerschaft
							),
						searchServiceContext.getUser().getTraegerschaft()
					)
				)
			);
		}
		predicates.add(
			createPredicateAusgeloesteSCHJAAngebote(
				cb,
				searchServiceContext.getJoinAnmeldungTagesschule(),
				searchServiceContext.getJoinAnmeldungFerieninsel(),
				searchServiceContext.getJoinInstitutionstammdatenBetreuungen(),
				searchServiceContext.getJoinInstitutionstammdatenTagesschule(),
				searchServiceContext.getJoinInstitutionstammdatenFerieninsel()
			)
		);
	}

	private void setGemeindeFilterForCurrentUser(
		@Nonnull Benutzer currentBenutzer,
		@Nonnull List<Predicate> predicates,
		@Nonnull SearchServiceContext searchServiceContext
	) {

		if (currentBenutzer.getCurrentBerechtigung()
			.getRole()
			.isRoleGemeindeabhaengig()) {
			Collection<Gemeinde> gemeindenForBenutzer = currentBenutzer
				.extractGemeindenForUser();
			Predicate inGemeinde = searchServiceContext.getJoinGemeinde()
				.in(gemeindenForBenutzer);
			predicates.add(inGemeinde);
		}
	}

	private void setOnlyAktivePeriodenPredicate(
		@Nonnull CriteriaBuilder cb,
		@Nonnull Join<Gesuch, Gesuchsperiode> joinGesuchsperiode,
		@Nonnull List<Predicate> predicates,
		@Nullable Boolean onlyAktivePerioden
	) {
		if (onlyAktivePerioden == null || !onlyAktivePerioden) {
			return;
		}
		predicates.add(
			cb.equal(
				joinGesuchsperiode.get(Gesuchsperiode_.status),
				GesuchsperiodeStatus.AKTIV
			)
		);
	}

	private void setMandantFilterForCurrentUser(
		CriteriaBuilder cb,
		List<Predicate> predicates,
		SearchServiceContext searchServiceContext
	) {
		Predicate mandantPredicate = cb.equal(
			searchServiceContext.getJoinFall().get(Fall_.mandant),
			principalBean.getMandant()
		);
		predicates.add(mandantPredicate);
	}

	private Set<AntragStatus> getAntragStatuses(
		boolean searchForPendenzen,
		UserRole role
	) {
		if (searchForPendenzen) {
			return AntragStatus.pendenzenForRole(role);
		}
		return AntragStatus.allowedforRole(role)
			.stream()
			.filter(antragStatus -> {
				if (antragStatus.isAnyOfInBearbeitungGSOrSZD()) {
					return !institutionStammdatenService
						.getBetreuungsangeboteForInstitutionenOfCurrentBenutzer()
						.stream()
						.allMatch(
							BetreuungsangebotTyp::isTagesschule
						);
				}
				return true;
			})
			.collect(Collectors.toSet());
	}

	/**
	 * Ob es ein BG- TS- oder Mischgesuch ist, wird anhand der Verantwortliche berechnet. BG- oder Mischgesuche sind
	 * alle, die
	 * VerantwortlicherBG gesetzt haben, VerantwortlicherTS interessiert nicht
	 */
	private Predicate createPredicateJAOrMischGesuche(
		CriteriaBuilder cb,
		Join<Gesuch, Dossier> dossier
	) {
		final Predicate predicateIsVerantwortlicherBG = cb.isNotNull(
			dossier.get(Dossier_.verantwortlicherBG)
		);
		return predicateIsVerantwortlicherBG;
	}

	/**
	 * Ob es ein BG- TS- oder Mischgesuch ist, wird anhand der Verantwortliche berechnet. TS- oder Mischgesuche sind
	 * alle, die
	 * VerantwortlicherTS gesetzt haben, VerantwortlicherBG interessiert nicht. Fuer TS sind sie nur so lange in der
	 * Pendenzenliste, wie das FinSit-Flag nicht gesetzt ist
	 */
	private Predicate createPredicateSCHOrMischGesuche(
		CriteriaBuilder cb,
		Root<Gesuch> root,
		Join<Gesuch, Dossier> dossier
	) {
		// Grundsaetzlich gilt: Wenn ein Verantwortlicher TS gesetzt ist, ist es sichtbar bis die FinSit ausgefuellt
		// wurde
		final Predicate predicateIsVerantwortlicherTS = cb.isNotNull(
			dossier.get(Dossier_.verantwortlicherTS)
		);
		final Predicate predicateIsFlagFinSitNotSet = cb.isNull(
			root.get(Gesuch_.finSitStatus)
		);
		final Predicate predicateISVerTSAndFinSitNotSet = cb.and(
			predicateIsVerantwortlicherTS,
			predicateIsFlagFinSitNotSet
		);
		// Aber: Falls KEIN Verantwortlicher BG gesetzt ist, muss es u.U. laenger sichtbar sein
		final Predicate predicateIsNoVerantwortlicherBG = cb.isNull(
			dossier.get(Dossier_.verantwortlicherBG)
		);
		final Predicate predicateISVerTSAndNoBG =
			cb.and(
				predicateIsVerantwortlicherTS,
				predicateIsNoVerantwortlicherBG
			);
		return cb.or(predicateISVerTSAndFinSitNotSet, predicateISVerTSAndNoBG);
	}

	/**
	 * ((TS or FI) and notAusgeloest) or (otherAngebotTyps)
	 */
	private Predicate createPredicateAusgeloesteSCHJAAngebote(
		CriteriaBuilder cb,
		SetJoin<KindContainer, AnmeldungTagesschule> joinAnmeldungTagesschule,
		SetJoin<KindContainer, AnmeldungFerieninsel> joinAnmeldungFerieninsel,
		Join<Betreuung, InstitutionStammdaten> joinInstitutionstammdatenBetreuungen,
		Join<AnmeldungTagesschule, InstitutionStammdaten> joinInstitutionstammdatenTagesschule,
		Join<AnmeldungFerieninsel, InstitutionStammdaten> joinInstitutionstammdatenFerieninsel
	) {
		Predicate predicateTSOderFINotAusgelost =
			createPredicateAusgeloesteSCHAngebote(
				cb,
				joinAnmeldungTagesschule,
				joinAnmeldungFerieninsel,
				joinInstitutionstammdatenTagesschule,
				joinInstitutionstammdatenFerieninsel
			);
		Predicate predicateNotTSOderFI = createPredicateJAAngebote(
			cb,
			joinInstitutionstammdatenBetreuungen
		);
		return cb.or(predicateTSOderFINotAusgelost, predicateNotTSOderFI);
	}

	/**
	 * (TS or FI) and notAusgeloest
	 */
	private Predicate createPredicateAusgeloesteSCHAngebote(
		CriteriaBuilder cb,
		SetJoin<KindContainer, AnmeldungTagesschule> joinAnmeldungTagesschule,
		SetJoin<KindContainer, AnmeldungFerieninsel> joinAnmeldungFerieninsel,
		Join<AnmeldungTagesschule, InstitutionStammdaten> joinInstitutionstammdatenTagesschule,
		Join<AnmeldungFerieninsel, InstitutionStammdaten> joinInstitutionstammdatenFerieninsel
	) {

		//(TS or FI) and notAusgeloest)
		Predicate predicateTagesschule = cb.equal(
			joinInstitutionstammdatenTagesschule.get(
				InstitutionStammdaten_.betreuungsangebotTyp
			),
			BetreuungsangebotTyp.TAGESSCHULE
		);
		Predicate predicateFerieninsel = cb.equal(
			joinInstitutionstammdatenFerieninsel.get(
				InstitutionStammdaten_.betreuungsangebotTyp
			),
			BetreuungsangebotTyp.FERIENINSEL
		);
		Predicate predicateTSOderFI = cb.or(
			predicateTagesschule,
			predicateFerieninsel
		);

		Predicate predicateTagesschuleNotErfasst = cb.notEqual(
			joinAnmeldungTagesschule.get(Betreuung_.betreuungsstatus),
			Betreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST
		);
		Predicate predicateFerieninselNotErfasst = cb.notEqual(
			joinAnmeldungFerieninsel.get(Betreuung_.betreuungsstatus),
			Betreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST
		);
		Predicate predicateTSOderFINotErfasst = cb.or(
			predicateTagesschuleNotErfasst,
			predicateFerieninselNotErfasst
		);

		return cb.and(predicateTSOderFI, predicateTSOderFINotErfasst);
	}

	/**
	 * Alle Angebottypen die nicht Schulamt sind.
	 */
	private Predicate createPredicateJAAngebote(
		CriteriaBuilder cb,
		Join<Betreuung, InstitutionStammdaten> institutionstammdaten
	) {
		//all otherAngebotTyps
		Predicate predicateNotTagesschule = cb.notEqual(
			institutionstammdaten.get(
				InstitutionStammdaten_.betreuungsangebotTyp
			),
			BetreuungsangebotTyp.TAGESSCHULE
		);
		Predicate predicateNotFerieninsel = cb.notEqual(
			institutionstammdaten.get(
				InstitutionStammdaten_.betreuungsangebotTyp
			),
			BetreuungsangebotTyp.FERIENINSEL
		);
		return cb.and(predicateNotTagesschule, predicateNotFerieninsel);
	}

	/**
	 * Adds a predicate to the predicates list if it is needed to filter by gesuchBetreuungenStatus. This will be needed
	 * just
	 * when
	 * the Status is GEPRUEFT, PLATZBESTAETIGUNG_WARTEN or PLATZBESTAETIGUNG_ABGEWIESEN
	 */
	private void createPredicateGesuchBetreuungenStatus(
		CriteriaBuilder cb,
		Root<Gesuch> root,
		List<Predicate> predicates,
		String status
	) {
		if (AntragStatusDTO.PLATZBESTAETIGUNG_WARTEN.toString()
			.equalsIgnoreCase(status)) {
			predicates.add(
				cb.equal(
					root.get(Gesuch_.gesuchBetreuungenStatus),
					GesuchBetreuungenStatus.WARTEN
				)
			);
		} else if (AntragStatusDTO.PLATZBESTAETIGUNG_ABGEWIESEN.toString()
			.equalsIgnoreCase(status)) {
			predicates.add(
				cb.equal(
					root.get(Gesuch_.gesuchBetreuungenStatus),
					GesuchBetreuungenStatus.ABGEWIESEN
				)
			);
		} else if (AntragStatusDTO.GEPRUEFT.toString()
			.equalsIgnoreCase(status)) {
			predicates.add(
				cb.equal(
					root.get(Gesuch_.gesuchBetreuungenStatus),
					GesuchBetreuungenStatus.ALLE_BESTAETIGT
				)
			);
		}
	}

	private void constructOrderByClause(
		@Nonnull AntragTableFilterDTO antragTableFilterDto,
		CriteriaBuilder cb,
		CriteriaQuery query,
		Root<Gesuch> root,
		Join<KindContainer, Kind> kinder,
		Join<Gesuch, Gesuchsperiode> gesuchsperiode,
		Join<Betreuung, InstitutionStammdaten> institutionstammdaten,
		Join<InstitutionStammdaten, Institution> institution
	) {
		Expression<?> expression;
		if (antragTableFilterDto.getSort() != null
			&& antragTableFilterDto.getSort().getPredicate() != null) {
			switch (antragTableFilterDto.getSort().getPredicate()) {
			case "fallNummer":
				expression = root.get(Gesuch_.dossier)
					.get(Dossier_.fall)
					.get(Fall_.fallNummer);
				break;
			case "familienName":
				expression = root.get(Gesuch_.gesuchsteller1)
					.get(GesuchstellerContainer_.gesuchstellerJA)
					.get(AbstractPersonEntity_.nachname);
				break;
			case "antragTyp":
				expression = root.get(Gesuch_.typ);
				break;
			case "gesuchsperiodeString":
				expression = gesuchsperiode.get(
					AbstractDateRangedEntity_.gueltigkeit
				).get(DateRange_.gueltigAb);
				break;
			case "aenderungsdatum":
				expression = root.get(AbstractEntity_.timestampMutiert);
				break;
			case "eingangsdatum":
				expression = root.get(Gesuch_.eingangsdatum);
				break;
			case "eingangsdatumSTV":
				expression = root.get(Gesuch_.eingangsdatumSTV);
				break;
			case "status":
				expression = root.get(Gesuch_.status);
				break;
			case "angebote":
				// Die Angebote sind eigentlich eine Liste innerhalb der Liste (also des Tabelleneintrages).
				// Kinder ohne Angebot sollen egal wie sortiert ist am Schluss kommen!
				if (antragTableFilterDto.getSort().getReverse()) {
					expression = cb.selectCase()
						.when(institutionstammdaten.isNull(), "ZZZZ")
						.otherwise(
							institutionstammdaten.get(
								InstitutionStammdaten_.betreuungsangebotTyp
							)
						);
				} else {
					expression = cb.selectCase()
						.when(institutionstammdaten.isNull(), "0000")
						.otherwise(
							institutionstammdaten.get(
								InstitutionStammdaten_.betreuungsangebotTyp
							)
						);
				}
				break;
			case "institutionen":
				// Die Institutionen sind eigentlich eine Liste innerhalb der Liste (also des Tabelleneintrages).
				// Kinder ohne Angebot sollen egal wie sortiert ist am Schluss kommen!
				if (antragTableFilterDto.getSort().getReverse()) {
					expression = cb.selectCase()
						.when(institution.isNull(), "ZZZZ")
						.otherwise(institution.get(Institution_.name));
				} else {
					expression = cb.selectCase()
						.when(institution.isNull(), "0000")
						.otherwise(institution.get(Institution_.name));
				}
				break;
			case "verantwortlicherBG":
				expression = root.get(Gesuch_.dossier)
					.get(Dossier_.verantwortlicherBG)
					.get(Benutzer_.nachname);
				break;
			case "verantwortlicherTS":
				expression = root.get(Gesuch_.dossier)
					.get(Dossier_.verantwortlicherTS)
					.get(Benutzer_.nachname);
				break;
			case "kinder":
				expression = kinder.get(AbstractPersonEntity_.vorname);
				break;
			case "dokumenteHochgeladen":
				expression = root.get(Gesuch_.dokumenteHochgeladen);
				break;
			case "gemeinde":
				expression = root.get(Gesuch_.dossier)
					.get(Dossier_.gemeinde)
					.get(Gemeinde_.name);
				break;
			default:
				LOG.warn(
					"Using default sort by FallNummer because there is no specific clause for predicate {}",
					antragTableFilterDto.getSort().getPredicate()
				);
				expression = root.get(Gesuch_.dossier)
					.get(Dossier_.fall)
					.get(Fall_.fallNummer);
				break;
			}
			query.orderBy(
				antragTableFilterDto.getSort().getReverse() ?
					cb.asc(expression) :
					cb.desc(expression)
			);
		} else {
			// Default sort when nothing is choosen
			expression = root.get(AbstractEntity_.timestampMutiert);
			query.orderBy(cb.desc(expression));
		}
	}

	private List<Gesuch> findGesuche(@Nonnull List<String> gesuchIds) {
		if (!gesuchIds.isEmpty()) {
			final CriteriaBuilder cb = persistence.getCriteriaBuilder();
			final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);
			Root<Gesuch> root = query.from(Gesuch.class);
			Predicate predicate = root.get(AbstractEntity_.id).in(gesuchIds);
			Fetch<Gesuch, KindContainer> kindContainers = root.fetch(
				Gesuch_.kindContainers,
				JoinType.LEFT
			);
			kindContainers.fetch(KindContainer_.betreuungen, JoinType.LEFT);
			query.where(predicate);
			//reduce to unique gesuche
			List<Gesuch> listWithDuplicates = persistence.getCriteriaResults(
				query
			);
			LinkedHashSet<Gesuch> setOfGesuche = new LinkedHashSet<>();
			//richtige reihenfolge beibehalten
			for (String gesuchId : gesuchIds) {
				listWithDuplicates.stream()
					.filter(gesuch -> gesuch.getId().equals(gesuchId))
					.findFirst()
					.ifPresent(setOfGesuche::add);
			}
			return new ArrayList<>(setOfGesuche);
		}
		return Collections.emptyList();
	}
}
