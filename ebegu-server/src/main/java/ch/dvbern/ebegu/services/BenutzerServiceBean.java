/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;
import jakarta.persistence.criteria.Subquery;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import ch.dvbern.ebegu.authentication.ExternalUUIDUtil;
import ch.dvbern.ebegu.authentication.KibonJwt;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.filter.suchfilter.smarttable.BenutzerPredicateObjectDTO;
import ch.dvbern.ebegu.dto.filter.suchfilter.smarttable.BenutzerSearchDTO;
import ch.dvbern.ebegu.dto.filter.suchfilter.smarttable.BenutzerTableFilterDTO;
import ch.dvbern.ebegu.dto.filter.suchfilter.smarttable.BenutzerTableMandantFilterDTO;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Benutzer_;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.BerechtigungHistory;
import ch.dvbern.ebegu.entities.BerechtigungHistory_;
import ch.dvbern.ebegu.entities.Berechtigung_;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.GemeindeStammdaten_;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenFerieninsel;
import ch.dvbern.ebegu.entities.InstitutionStammdatenFerieninsel_;
import ch.dvbern.ebegu.entities.InstitutionStammdatenTagesschule;
import ch.dvbern.ebegu.entities.InstitutionStammdatenTagesschule_;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.Institution_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.Traegerschaft_;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.BenutzerQueries_;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.errors.BenutzerExistException;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.authentication.KeycloakApi;
import ch.dvbern.ebegu.services.util.SearchUtil;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Hibernate;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.BenutzerStatus.AKTIV;
import static ch.dvbern.ebegu.enums.BenutzerStatus.GESPERRT;
import static ch.dvbern.ebegu.enums.UserRole.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRole.getBgAndGemeindeRoles;
import static ch.dvbern.ebegu.enums.UserRole.getMandantRoles;
import static ch.dvbern.ebegu.enums.UserRole.getTsAndGemeindeRoles;
import static ch.dvbern.ebegu.enums.UserRole.getTsBgAndGemeindeRoles;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setAntragstellerFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setGemeindeFilterForCurrentFerienbetreuungUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setGemeindeFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setInstitutionFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setMandantFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setRoleFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setSozialdienstFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setSuperAdminFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.FilterFunctions.setTraegerschaftFilterForCurrentUser;
import static ch.dvbern.ebegu.services.util.PredicateHelper.NEW;
import static com.google.common.base.Preconditions.checkArgument;
import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;
import static java.util.Objects.requireNonNull;

/**
 * Service fuer Benutzer
 */
@Stateless
@Local(BenutzerService.class)
public class BenutzerServiceBean extends AbstractBaseService implements
	BenutzerService {

	private static final Logger LOG = LoggerFactory.getLogger(
		BenutzerServiceBean.class.getSimpleName()
	);

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private KeycloakApi keycloakApi;

	@Inject
	private MailService mailService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private FallService fallService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private PasswordGenerator passwordGenerator;

	@Inject
	private MandantService mandantService;

	@Nonnull
	@Override
	public Benutzer saveBenutzerAndBerechtigungen(
		@Nonnull Benutzer benutzer,
		boolean currentBerechtigungChanged,
		boolean nameVornameUpdated
	) {
		requireNonNull(benutzer);
		prepareBenutzerForSave(benutzer, currentBerechtigungChanged);
		authorizer.checkWriteAuthorization(benutzer);
		checkSuperuserRoleZuteilung(benutzer);
		Benutzer mergedBenutzer = persistence.merge(benutzer);
		if (currentBerechtigungChanged) {
			//do the mitarbeiter update in KC if needed
			if (mergedBenutzer.getCurrentBerechtigung()
				.getRole()
				.equals(UserRole.GESUCHSTELLER)) {
				keycloakApi.deleteMitarbeiterAccessBenutzerRole(mergedBenutzer);
			} else {
				keycloakApi.addMitarbeiterAccessBenutzerRole(mergedBenutzer);
			}
		}
		if (nameVornameUpdated) {
			keycloakApi.updateUser(mergedBenutzer);
		}
		return mergedBenutzer;
	}

	@Nonnull
	@Override
	public Benutzer saveBenutzer(@Nonnull Benutzer benutzer) {
		requireNonNull(benutzer);
		authorizer.checkWriteAuthorization(benutzer);
		if (benutzer.isNew()) {
			return persistence.persist(benutzer);
		}
		return persistence.merge(benutzer);
	}

	@Nonnull
	@Override
	public Benutzer einladen(
		@Nonnull Einladung einladung,
		@Nonnull Mandant mandant
	) {
		requireNonNull(einladung);

		checkEinladung(einladung, mandant);
		sendBenutzerEinladung(einladung);
		return saveBenutzer(einladung.getEingeladener());
	}

	@Override
	public void erneutEinladen(@Nonnull Benutzer eingeladener) {
		authorizer.checkWriteAuthorization(eingeladener);

		if (eingeladener.getStatus() != BenutzerStatus.EINGELADEN) {
			throw new EbeguRuntimeException(
				KibonLogLevel.INFO,
				eingeladener.getUsername(),
				ErrorCodeEnum.ERROR_BENUTZER_STATUS_NOT_EINGELADEN
			);
		}
		eingeladener.setInitialPassword(
			passwordGenerator.createRandomPassword()
		);

		if (isPreOidcBenutzer(eingeladener)) {
			String externalUuid = keycloakApi.create(eingeladener);
			eingeladener.setExternalUUID(externalUuid);
		} else {
			keycloakApi.updateInitialPassword(eingeladener);
			// make sure user is not (yet) deleted by EingeladeneBenutzerCleanUpJob
			eingeladener.setTimestampMutiert(LocalDateTime.now());
		}
		Einladung einladung = Einladung.forRolle(eingeladener);
		sendBenutzerEinladung(einladung);
	}

	// Refers to users invited before KIBON-2453 was merged and therefore no
	// keycloak user exists at this point
	private static boolean isPreOidcBenutzer(Benutzer eingeladener) {
		return eingeladener.getExternalUUID() == null;
	}

	private void sendBenutzerEinladung(Einladung einladung) {
		mailService.prepareToSendBenutzerEinladung(
			principalBean.getBenutzer(),
			einladung
		);
		einladung.getEingeladener().setInitialPassword(null);
	}

	/**
	 * According to the type of Einladung it checks that the given benutzer meets the conditions required.
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void checkEinladung(
		@Nonnull Einladung einladung,
		@Nonnull Mandant mandant
	) {
		Benutzer benutzer = einladung.getEingeladener();
		checkSuperuserRoleZuteilung(einladung.getEingeladener());
		checkArgument(Objects.equals(benutzer.getMandant(), mandant));

		checkBenutzerIsNotGesuchstellerWithFreigegebenemGesuch(benutzer);

		if (benutzer.isNew()
			&& benutzer.getStatus() != BenutzerStatus.EINGELADEN) {
			throw new EbeguRuntimeException(
				KibonLogLevel.INFO,
				benutzer.getUsername(),
				ErrorCodeEnum.ERROR_BENUTZER_STATUS_NOT_EINGELADEN
			);
		}
	}

	@Override
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	public void checkBenutzerIsNotGesuchstellerWithFreigegebenemGesuch(
		@Nonnull Benutzer benutzer
	) {
		// falls gesuchsteller, und darf einladen
		if (!benutzer.isNew()
			&& benutzer.getCurrentBerechtigung().getRole()
				== GESUCHSTELLER) {
			//check if Gesuch exist
			Optional<Fall> fallOpt = fallService.findFallByBesitzer(benutzer);
			if (!fallOpt.isPresent()) {
				//return error code keinen Gesusch, user can be deleted without warning
				throw new BenutzerExistException(
					KibonLogLevel.NONE,
					benutzer.getUsername(),
					benutzer.getFullName(),
					ErrorCodeEnum.ERROR_GESUCHSTELLER_EXIST_NO_GESUCH,
					null
				);

			} else {
				Fall existingFall = fallOpt.get();
				List<String> gesuchIdList = gesuchService
					.getAllGesuchIDsForFall(existingFall.getId());
				if (gesuchIdList.isEmpty()) {
					//return error code keinen Gesusch, user can be deleted without warning
					throw new BenutzerExistException(
						KibonLogLevel.NONE,
						benutzer.getUsername(),
						benutzer.getFullName(),
						ErrorCodeEnum.ERROR_GESUCHSTELLER_EXIST_NO_GESUCH,
						existingFall.getId()
					);
				}
				if (checkIfGesuchFreigegeben(gesuchIdList)) {
					throw new BenutzerExistException(
						KibonLogLevel.NONE,
						benutzer.getUsername(),
						benutzer.getFullName(),
						ErrorCodeEnum.ERROR_GESUCHSTELLER_EXIST_WITH_FREGEGEBENE_GESUCH,
						existingFall.getId()
					);
				} else {
					throw new BenutzerExistException(
						KibonLogLevel.NONE,
						benutzer.getUsername(),
						benutzer.getFullName(),
						ErrorCodeEnum.ERROR_GESUCHSTELLER_EXIST_WITH_GESUCH,
						existingFall.getId()
					);
				}
			}
		}
	}

	@Override
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	public String findFallIdIfBenutzerIsGesuchstellerWithoutFreigegebenemGesuch(
		@Nonnull Benutzer benutzer
	) {
		// falls gesuchsteller, und darf einladen
		if (!benutzer.isNew()
			&& benutzer.getCurrentBerechtigung().getRole()
				== GESUCHSTELLER) {
			//check if Gesuch exist
			Optional<Fall> fallOpt = fallService.findFallByBesitzer(benutzer);
			if (!fallOpt.isPresent()) {
				return null;
			} else {
				Fall existingFall = fallOpt.get();
				List<String> gesuchIdList = gesuchService
					.getAllGesuchIDsForFall(existingFall.getId());
				if (!gesuchIdList.isEmpty()
					&& checkIfGesuchFreigegeben(gesuchIdList)) {
					throw new BenutzerExistException(
						KibonLogLevel.NONE,
						benutzer.getUsername(),
						benutzer.getFullName(),
						ErrorCodeEnum.ERROR_GESUCHSTELLER_EXIST_WITH_FREGEGEBENE_GESUCH,
						existingFall.getId()
					);
				}
				return existingFall.getId();
			}
		}
		return null;
	}

	private boolean checkIfGesuchFreigegeben(List<String> gesuchIdList) {
		for (String id : gesuchIdList) {
			Gesuch gs = gesuchService.findGesuch(id, false)
				.orElseThrow(
					() -> new EbeguRuntimeException(
						"checkIfGesuchFreigegeben",
						"Gesuch nicht gefunden"
					)
				);
			if (gs.getStatus() != AntragStatus.IN_BEARBEITUNG_GS) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void checkSuperuserRoleZuteilung(@Nonnull Benutzer benutzer) {
		// Nur ein Superadmin kann Superadmin-Rechte vergeben!
		if (benutzer.getRole() == UserRole.SUPER_ADMIN
			&& !principalBean.isCallerInRole(UserRoleName.SUPER_ADMIN)) {
			throw new IllegalStateException(
				"Nur ein Superadmin kann Superadmin-Rechte vergeben. Dies wurde aber versucht durch: "
					+ principalBean.getBenutzer().getUsername()
			);
		}
	}

	@Nonnull
	@Override
	public Optional<Benutzer> findBenutzer(
		@Nonnull String username,
		@Nonnull Mandant mandant
	) {
		requireNonNull(username, "username muss gesetzt sein");
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Predicate usernamePredicate = cb.equal(
			root.get(Benutzer_.username),
			username
		);
		Predicate mandantPredicate = cb.equal(
			root.get(Benutzer_.mandant),
			mandant
		);

		query.where(usernamePredicate, mandantPredicate);

		try {
			return Optional.of(
				persistence.getEntityManager()
					.createQuery(query)
					.getSingleResult()
			);
		} catch (NoResultException nre) {
			return Optional.empty();
		}
	}

	@Nonnull
	@Override
	public Optional<Benutzer> findAndLockBenutzer(
		@Nonnull String username,
		@Nonnull Mandant mandant
	) {
		requireNonNull(username, "username muss gesetzt sein");
		requireNonNull(mandant, "mandant muss gesetzt sein");

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Predicate predicateUsername = cb.equal(
			root.get(Benutzer_.username),
			username
		);
		Predicate mandantPredicate = cb.equal(
			root.get(Benutzer_.mandant),
			mandant
		);

		query.where(predicateUsername, mandantPredicate);
		query.distinct(true);

		try {
			Optional<Benutzer> benutzer = Optional.of(
				persistence.getEntityManager()
					.createQuery(query)
					.setLockMode(PESSIMISTIC_WRITE)
					.getSingleResult()
			);
			return benutzer;
		} catch (NoResultException nre) {
			return Optional.empty();
		}
	}

	@Nonnull
	@Override
	public Optional<Benutzer> findBenutzerById(@Nonnull String id) {
		requireNonNull(id, "id muss gesetzt sein");

		return Optional.ofNullable(persistence.find(Benutzer.class, id));
	}

	@Override
	public Optional<Benutzer> findBenutzer(KibonJwt kibonJwt) {
		return findBenutzerByExternalUUID(kibonJwt.getExternalUUID())
			.or(
				() -> kibonJwt.getBeLoginPrimaryId()
					.flatMap(this::findBenutzerByExternalUUID)
			)
			.or(
				() -> findBenutzer(
					kibonJwt.getEmail(),
					kibonJwt.getMandantIdentifier()
				)
			);
	}

	private Optional<? extends Benutzer> findBenutzer(
		String username,
		MandantIdentifier mandantIdentifier
	) {
		Mandant mandant = mandantService.findMandantByIdentifier(
			mandantIdentifier
		)
			.orElseThrow();
		return findBenutzer(username, mandant);
	}

	@Nonnull
	@Override
	@Transactional(TxType.SUPPORTS)
	public Optional<Benutzer> findBenutzerByExternalUUID(
		@Nonnull String externalUUID
	) {
		Optional<Benutzer> benutzer = criteriaQueryHelper
			.getEntityByUniqueAttribute(
				Benutzer.class,
				requireNonNull(
					ExternalUUIDUtil.addPrefixIfNecessary(
						externalUUID,
						principalBean.getMandant().getMandantIdentifier()
					)
				),
				Benutzer_.externalUUID
			);
		if (benutzer.isPresent()) {
			Hibernate.initialize(
				benutzer.get().getCurrentBerechtigung().getGemeindeList()
			);
			Hibernate.initialize(
				benutzer.get().getCurrentBerechtigung().getTraegerschaft()
			);
			Hibernate.initialize(
				benutzer.get().getCurrentBerechtigung().getInstitution()
			);
			Hibernate.initialize(
				benutzer.get().getCurrentBerechtigung().getSozialdienst()
			);
		}
		return benutzer;
	}

	@Override
	public Optional<Benutzer> findByEmail(String email, Mandant mandant) {
		try {
			return Optional.ofNullable(
				BenutzerQueries_.findByEmail(
					persistence.getEntityManager(),
					email,
					mandant
				)
			);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getAktivGemeindeAdministratoren(
		Gemeinde gemeinde
	) {
		return getActiveBenutzersOfRoles(
			UserRole.getAllGemeindeAdminRoles(),
			gemeinde
		);
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getAktiveGemeindeSachbearbeiter(
		Gemeinde gemeinde
	) {
		return getActiveBenutzersOfRoles(
			UserRole.getAllGemeindeSachbearbeiterRoles(),
			gemeinde
		);
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getInstitutionAdministratoren(
		Institution institution
	) {
		return getBenutzersOfRoles(
			UserRole.getAllInstitutionAdminRoles(),
			institution
		);
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getInstitutionSachbearbeiter(
		Institution institution
	) {
		return getBenutzersOfRoles(
			UserRole.getAllInstitutionSachbearbeiterRoles(),
			institution
		);
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getTraegerschaftAdministratoren(
		final Traegerschaft traegerschaft
	) {
		checkIfUserIsLoggedIn("getTraegerschaftAdministratoren");
		authorizer.checkReadAuthorization(traegerschaft);

		List<Predicate> predicates = new ArrayList<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);

		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(
			Benutzer_.berechtigungen
		);

		query.select(root);

		predicates.add(
			getBerechtigungGueltigPredicate(cb, joinBerechtigungen)
		);
		predicates.add(
			cb.equal(
				joinBerechtigungen.get(Berechtigung_.role),
				UserRole.ADMIN_TRAEGERSCHAFT
			)
		);
		predicates.add(
			cb.equal(
				joinBerechtigungen.get(Berechtigung_.traegerschaft),
				traegerschaft
			)
		);

		query.where(predicates.toArray(NEW));
		query.distinct(true);

		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getActiveBenutzerBgOrGemeinde(
		Gemeinde gemeinde
	) {
		return getActiveBenutzersOfRoles(getBgAndGemeindeRoles(), gemeinde);
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getBenutzerTsBgOrGemeinde(Gemeinde gemeinde) {
		return getBenutzersOfRoles(
			UserRole.getTsBgAndGemeindeRoles(),
			gemeinde
		);
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getActiveBenutzerTsOrGemeinde(
		Gemeinde gemeinde
	) {
		return getActiveBenutzersOfRoles(getTsAndGemeindeRoles(), gemeinde);
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getAllBenutzerBgOrGemeinde() {
		return getBenutzersOfRoles(getBgAndGemeindeRoles());
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getAllBenutzerTsOrGemeinde() {
		return getBenutzersOfRoles(getTsAndGemeindeRoles());
	}

	@Override
	public Collection<Benutzer> getAllActiveBenutzerMandant(
		@Nonnull Mandant mandant
	) {
		return getBenutzersOfRoles(getMandantRoles())
			.stream()
			.filter(
				benutzer -> benutzer.getMandant().getMandantIdentifier()
					== mandant.getMandantIdentifier()
			)
			.filter(benutzer -> !benutzer.isGesperrt())
			.collect(Collectors.toList());
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getAllBenutzerBgTsOrGemeinde() {
		return getBenutzersOfRoles(getTsBgAndGemeindeRoles());
	}

	/**
	 * Gibt alle existierenden Benutzer mit den gewünschten Rollen zurueck. ¡Diese Methode filtert die Gemeinde über den
	 * angemeldeten Benutzer!
	 *
	 * @param roles Die besagten Rollen
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	private Collection<Benutzer> getBenutzersOfRoles(List<UserRole> roles) {
		Benutzer currentBenutzer = checkIfUserIsLoggedIn("getBenutzersOfRole");

		List<Predicate> predicates = new ArrayList<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(
			Benutzer_.berechtigungen
		);
		SetJoin<Berechtigung, Gemeinde> joinGemeinde =
			joinBerechtigungen.join(
				Berechtigung_.gemeindeList,
				JoinType.LEFT
			);
		query.select(root);

		predicates.add(
			getBerechtigungGueltigPredicate(cb, joinBerechtigungen)
		);
		predicates.add(joinBerechtigungen.get(Berechtigung_.role).in(roles));

		setGemeindeFilterForCurrentUser(
			currentBenutzer,
			joinGemeinde,
			predicates
		);
		Predicate predicateMandant = cb.equal(
			root.get(Benutzer_.mandant),
			currentBenutzer.getMandant()
		);
		predicates.add(predicateMandant);

		query.where(predicates.toArray(NEW));
		query.distinct(true);

		final List<Benutzer> benutzerList = persistence.getCriteriaResults(
			query
		);
		return benutzerList;
	}

	/**
	 * Gibt alle existierenden Benutzer mit den gewünschten Rollen zurueck. ¡Diese Methode filtert die Gemeinde über den
	 * Gemeinde-Parameter!
	 *
	 * @param roles Der Rollen Filter
	 * @param gemeinde Der Gemeinde Filter
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	private Collection<Benutzer> getActiveBenutzersOfRoles(
		@Nonnull List<UserRole> roles,
		@Nonnull Gemeinde gemeinde
	) {
		checkIfUserIsLoggedIn("getActiveBenutzersOfRoles");
		authorizer.checkReadAuthorization(gemeinde);

		List<Predicate> predicates = new ArrayList<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(
			Benutzer_.berechtigungen
		);
		Join<Berechtigung, Gemeinde> joinBerechtigungenGemeinde =
			joinBerechtigungen.join(Berechtigung_.gemeindeList);

		query.select(root);
		predicates.add(cb.equal(root.get(Benutzer_.status), AKTIV));
		predicates.add(
			getBerechtigungGueltigPredicate(cb, joinBerechtigungen)
		);
		predicates.add(joinBerechtigungen.get(Berechtigung_.role).in(roles));
		predicates.add(
			cb.equal(
				joinBerechtigungenGemeinde.get(AbstractEntity_.id),
				gemeinde.getId()
			)
		);

		query.where(predicates.toArray(NEW));
		query.distinct(true);

		return persistence.getCriteriaResults(query);
	}

	@Override
	public Collection<Benutzer> getActiveGemeindeBenutzersWithSendMailActivated(
		Mandant mandant
	) {
		List<Predicate> predicates = new ArrayList<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(
			Benutzer_.berechtigungen
		);
		query.select(root);

		// UserRole Gueltig condition
		predicates.add(cb.equal(root.get(Benutzer_.status), AKTIV));
		predicates.add(
			getBerechtigungGueltigPredicate(cb, joinBerechtigungen)
		);

		//Gemeinde Role condition
		Collection<UserRole> gemeindeRoles =
			new ArrayList<>(UserRole.getAllGemeindeAdminRoles());
		gemeindeRoles.addAll(UserRole.getAllGemeindeSachbearbeiterRoles());
		predicates.add(
			joinBerechtigungen.get(Berechtigung_.role).in(gemeindeRoles)
		);

		//Send Email activated condition
		predicates.add(
			cb.isTrue(root.get(Benutzer_.sendMailWennOffenePendenzen))
		);

		predicates.add(cb.equal(root.get(Benutzer_.mandant), mandant));

		query.where(predicates.toArray(NEW));
		query.distinct(true);

		return persistence.getCriteriaResults(query);
	}

	private static Predicate getBerechtigungGueltigPredicate(
		CriteriaBuilder cb,
		Join<Benutzer, Berechtigung> joinBerechtigungen
	) {
		return cb.between(
			cb.literal(LocalDate.now()),
			joinBerechtigungen.get(
				AbstractDateRangedEntity_.gueltigkeit
			).get(DateRange_.gueltigAb),
			joinBerechtigungen.get(
				AbstractDateRangedEntity_.gueltigkeit
			).get(DateRange_.gueltigBis)
		);
	}

	/**
	 * Gibt alle existierenden Benutzer mit den gewünschten Rollen zurueck. ¡Diese Methode filtert die Gemeinde über den
	 * Gemeinde-Parameter!
	 *
	 * @param roles Der Rollen Filter
	 * @param gemeinde Der Gemeinde Filter
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	private Collection<Benutzer> getBenutzersOfRoles(
		@Nonnull List<UserRole> roles,
		@Nonnull Gemeinde gemeinde
	) {
		checkIfUserIsLoggedIn("getBenutzersOfRoles");
		authorizer.checkReadAuthorization(gemeinde);

		List<Predicate> predicates = new ArrayList<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);

		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(
			Benutzer_.berechtigungen
		);
		Join<Berechtigung, Gemeinde> joinBerechtigungenGemeinde =
			joinBerechtigungen.join(Berechtigung_.gemeindeList);

		query.select(root);

		predicates.add(
			getBerechtigungGueltigPredicate(cb, joinBerechtigungen)
		);
		predicates.add(joinBerechtigungen.get(Berechtigung_.role).in(roles));
		predicates.add(
			cb.equal(
				joinBerechtigungenGemeinde.get(AbstractEntity_.id),
				gemeinde.getId()
			)
		);

		query.where(predicates.toArray(NEW));
		query.distinct(true);

		return persistence.getCriteriaResults(query);
	}

	/**
	 * Gibt alle existierenden Benutzer einer Institution mit den gewünschten Rollen zurueck.
	 *
	 * @param roles Das Rollen Filter
	 * @param institution zum Filtern nach der Institution
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	private Collection<Benutzer> getBenutzersOfRoles(
		@Nonnull List<UserRole> roles,
		@Nonnull Institution institution
	) {
		checkIfUserIsLoggedIn("getBenutzersOfRoles");
		authorizer.checkReadAuthorizationInstitution(institution);

		List<Predicate> predicates = new ArrayList<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);

		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(
			Benutzer_.berechtigungen
		);

		query.select(root);

		predicates.add(
			getBerechtigungGueltigPredicate(cb, joinBerechtigungen)
		);
		predicates.add(joinBerechtigungen.get(Berechtigung_.role).in(roles));
		predicates.add(
			cb.equal(
				joinBerechtigungen.get(Berechtigung_.institution),
				institution
			)
		);

		query.where(predicates.toArray(NEW));
		query.distinct(true);

		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getGesuchsteller(@Nonnull Mandant mandant) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(
			Benutzer_.berechtigungen
		);
		query.select(root);

		Predicate predicateMandant = cb.equal(
			root.get(Benutzer_.mandant),
			mandant
		);

		Predicate predicateActive = getBerechtigungGueltigPredicate(
			cb,
			joinBerechtigungen
		);
		Predicate predicateRole = joinBerechtigungen.get(Berechtigung_.role)
			.in(GESUCHSTELLER);
		query.where(predicateActive, predicateRole, predicateMandant);
		query.orderBy(
			cb.asc(root.get(Benutzer_.vorname)),
			cb.asc(root.get(Benutzer_.nachname))
		);
		return persistence.getCriteriaResults(query);
	}

	@Override
	public void removeBenutzer(
		@Nonnull String username,
		@Nonnull Mandant mandant
	) {
		requireNonNull(username);
		Benutzer benutzer = findBenutzer(username, mandant).orElseThrow(
			() -> new EbeguEntityNotFoundException(
				"removeBenutzer",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				username
			)
		);

		try {
			checkBenutzerIsNotGesuchstellerWithFreigegebenemGesuch(benutzer);
			// Keine Exception: Es ist kein Gesuchsteller: Wir können immer löschen
			removeBenutzerForced(benutzer.getUsername(), mandant);
		} catch (BenutzerExistException b) {
			// Es ist ein Gesuchsteller: Wir löschen, solange er keine freigegebenen/verfuegten Gesuche hat
			if (b.getErrorCodeEnum()
				!= ErrorCodeEnum.ERROR_GESUCHSTELLER_EXIST_WITH_FREGEGEBENE_GESUCH) {
				removeBenutzerForced(benutzer.getUsername(), mandant);
			} else {
				throw b;
			}
		}
	}

	@Nonnull
	@Override
	public Collection<Benutzer> getAllUserButGesuchsteller(
		@Nonnull Mandant mandant
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(
			Benutzer_.berechtigungen
		);
		query.select(root);

		Predicate predicateMandant = cb.equal(
			root.get(Benutzer_.mandant),
			mandant
		);

		Predicate predicateActive = getBerechtigungGueltigPredicate(
			cb,
			joinBerechtigungen
		);
		Predicate predicateRole = cb.notEqual(
			joinBerechtigungen.get(Berechtigung_.role),
			GESUCHSTELLER
		);
		query.where(predicateActive, predicateRole, predicateMandant);
		return persistence.getCriteriaResults(query);
	}

	private void removeBenutzerForced(
		@Nonnull String username,
		@Nonnull Mandant mandant
	) {
		requireNonNull(username);
		Benutzer benutzer = findBenutzer(username, mandant).orElseThrow(
			() -> new EbeguEntityNotFoundException(
				"removeBenutzer",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				username
			)
		);

		authorizer.checkWriteAuthorization(benutzer);

		// Den Benutzer ausloggen und seine AuthBenutzer loeschen
		keycloakApi.delete(benutzer);
		benutzer.setMarkedForDeletion(true);
		removeBerechtigungHistoryForBenutzer(benutzer);
		persistence.remove(benutzer);
	}

	private void removeBerechtigungHistoryForBenutzer(
		@Nonnull Benutzer benutzer
	) {
		Collection<BerechtigungHistory> histories =
			getBerechtigungHistoriesForBenutzer(benutzer);
		for (BerechtigungHistory history : histories) {
			persistence.remove(history);
		}
	}

	@Nonnull
	@Override
	public Optional<Benutzer> getCurrentBenutzer() {
		if (principalBean.isKibonServiceAccount()) {
			return Optional.empty();
		}
		return Optional.of(principalBean.getBenutzer());
	}

	@Override
	public Optional<Benutzer> findUserWithInvitation(
		@Nonnull String externalUuid
	) {
		return findBenutzerByExternalUUID(externalUuid)
			.filter(
				benutzerByEmail -> benutzerByEmail.getStatus()
					== BenutzerStatus.EINGELADEN
			);
	}

	@Override
	public boolean hasMoreThanOneMandantUser() {
		return getAllActiveBenutzerMandant(this.principalBean.getMandant())
			.size()
			> 1;
	}

	@Override
	public Collection<Benutzer> getActiveBenutzerInRolesOfActiveGemeinden(
		Mandant mandant,
		UserRole... roles
	) {
		List<Predicate> predicates = new ArrayList<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(
			Benutzer_.berechtigungen
		);
		Join<Berechtigung, Gemeinde> joinBerechtigungenGemeinde =
			joinBerechtigungen.join(Berechtigung_.gemeindeList);

		query.select(root);
		predicates.add(cb.equal(root.get(Benutzer_.status), AKTIV));
		predicates.add(cb.equal(root.get(Benutzer_.mandant), mandant));
		predicates.add(
			getBerechtigungGueltigPredicate(cb, joinBerechtigungen)
		);
		predicates.add(
			cb.equal(joinBerechtigungenGemeinde.get(Gemeinde_.status), AKTIV)
		);
		predicates.add(
			joinBerechtigungen.get(Berechtigung_.role).in(List.of(roles))
		);

		query.where(predicates.toArray(NEW));
		query.distinct(true);

		return persistence.getCriteriaResults(query);
	}

	@Override
	public Collection<Benutzer> getActiveBenutzerInRolesOfGemeinden(
		Mandant mandant,
		List<Gemeinde> gemeinden,
		UserRole... roles
	) {
		List<Predicate> predicates = new ArrayList<>();

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> joinBerechtigungen = root.join(
			Benutzer_.berechtigungen
		);
		Join<Berechtigung, Gemeinde> joinBerechtigungenGemeinde =
			joinBerechtigungen.join(Berechtigung_.gemeindeList);

		query.select(root);
		predicates.add(cb.equal(root.get(Benutzer_.status), AKTIV));
		predicates.add(cb.equal(root.get(Benutzer_.mandant), mandant));
		predicates.add(
			getBerechtigungGueltigPredicate(cb, joinBerechtigungen)
		);
		predicates.add(
			(joinBerechtigungenGemeinde.get(Gemeinde_.id)
				.in(
					gemeinden.stream()
						.map(Gemeinde::getId)
						.collect(Collectors.toList())
				))
		);
		predicates.add(
			joinBerechtigungen.get(Berechtigung_.role).in(List.of(roles))
		);

		query.where(predicates.toArray(NEW));
		query.distinct(true);

		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public Benutzer sperren(
		@Nonnull String username,
		@Nonnull Mandant mandant
	) {
		Benutzer benutzerFromDB = findBenutzer(username, mandant)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"sperren",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					"GesuchId invalid: " + username
				)
			);

		authorizer.checkWriteAuthorization(benutzerFromDB);

		if (isBenutzerDefaultBenutzerOfAnyGemeinde(
			benutzerFromDB.getUsername()
		)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.NONE,
				"sperren",
				ErrorCodeEnum.ERROR_USER_IS_VERANTWORTLICHER
			);
		}
		benutzerFromDB.setStatus(GESPERRT);
		keycloakApi.deleteMitarbeiterAccessBenutzerRole(benutzerFromDB);
		keycloakApi.lock(benutzerFromDB);
		LOG.info(
			"Setze Benutzer auf GESPERRT: {} / Eingeloggt: {}",
			benutzerFromDB.getUsername(),
			principalBean.getBenutzer().getUsername()
		);

		return persistence.merge(
			benutzerFromDB
		);
	}

	@Nonnull
	@Override
	public Benutzer reaktivieren(
		@Nonnull String username,
		@Nonnull Mandant mandant
	) {
		Benutzer benutzerFromDB = findBenutzer(username, mandant)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"reaktivieren",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					"GesuchId invalid: " + username
				)
			);

		authorizer.checkWriteAuthorization(benutzerFromDB);

		benutzerFromDB.setStatus(findLastNotGesperrtStatus(benutzerFromDB));
		if (GESUCHSTELLER != benutzerFromDB.getRole()) {
			// Gesuchsteller dürfen keinen Mitarbeiter-Zugang haben, alle andere schon
			// (auch die Test-Benutzer nicht)
			keycloakApi.addMitarbeiterAccessBenutzerRole(benutzerFromDB);
		}
		keycloakApi.unlock(benutzerFromDB);
		LOG.info(
			"Reaktiviere Benutzer: {} / Eingeloggt: {}",
			benutzerFromDB.getUsername(),
			principalBean.getBenutzer().getUsername()
		);

		return persistence.merge(benutzerFromDB);
	}

	private void prepareBenutzerForSave(
		@Nonnull Benutzer benutzer,
		boolean currentBerechtigungChanged
	) {
		List<Berechtigung> allSortedBerechtigungen = new LinkedList<>(
			benutzer.getBerechtigungen()
		);
		allSortedBerechtigungen.sort(
			Comparator.comparing(o -> o.getGueltigkeit().getGueltigAb())
		);

		final Berechtigung currentBerechtigung = allSortedBerechtigungen.get(0);

		handleGueltigkeitCurrentBerechtigung(
			allSortedBerechtigungen,
			currentBerechtigung,
			currentBerechtigungChanged
		);

		for (Berechtigung berechtigung : allSortedBerechtigungen) {
			prepareBerechtigungForSave(berechtigung);
		}

		// Ausloggen nur, wenn die aktuelle Berechtigung geändert hat
		if (currentBerechtigungChanged) {
			LOG.info(
				"Aktuelle Berechtigung des Benutzers {} hat geändert, Benutzer wird ausgeloggt",
				benutzer.getUsername()
			);
			keycloakApi.logout(benutzer);
		}
	}

	/**
	 * If there are future Berechtigungen it sets the gueltigBis of the currentBerechtigung to one day before the
	 * gueltigAb of
	 * the
	 * futureBerechtigung. For no futureBerechtigungen it sets the gueltigBis of the currentBerechtigung to END_OF_TIME
	 * If the
	 * currentBerechtigung changed it sets the gueltigAb of the currentBerechtigung to now()
	 */
	private void handleGueltigkeitCurrentBerechtigung(
		@Nonnull List<Berechtigung> allSortedBerechtigungen,
		@Nonnull Berechtigung currentBerechtigung,
		boolean currentBerechtigungChanged
	) {

		currentBerechtigung.getGueltigkeit()
			.setGueltigBis(
				allSortedBerechtigungen.size() > 1 ?
					allSortedBerechtigungen.get(1)
						.getGueltigkeit()
						.getGueltigAb()
						.minusDays(1) :
					Constants.END_OF_TIME
			);

		if (currentBerechtigungChanged) {
			currentBerechtigung.getGueltigkeit().setGueltigAb(LocalDate.now());
		}
	}

	private void prepareBerechtigungForSave(
		@Nonnull Berechtigung berechtigung
	) {
		// Es darf nur eine Institution gesetzt sein, wenn die Rolle INSTITUTION ist
		if (EnumUtil.isNoneOf(
			berechtigung.getRole(),
			UserRole.ADMIN_INSTITUTION,
			UserRole.SACHBEARBEITER_INSTITUTION
		)) {
			berechtigung.setInstitution(null);
		}
		// Es darf nur eine Trägerschaft gesetzt sein, wenn die Rolle TRAEGERSCHAFT ist
		if (EnumUtil.isNoneOf(
			berechtigung.getRole(),
			UserRole.ADMIN_TRAEGERSCHAFT,
			UserRole.SACHBEARBEITER_TRAEGERSCHAFT
		)) {
			berechtigung.setTraegerschaft(null);
		}
	}

	@Override
	@Nonnull
	public Pair<Long, List<Benutzer>> searchBenutzer(
		@Nonnull BenutzerTableMandantFilterDTO benutzerTableFilterDTO,
		@Nonnull Boolean forStatistik
	) {
		Pair<Long, List<Benutzer>> benutzer = innerSearchBenutzer(
			benutzerTableFilterDTO,
			forStatistik
		);

		return benutzer;
	}

	public Pair<Long, List<Benutzer>> innerSearchBenutzer(
		@Nonnull BenutzerTableMandantFilterDTO benutzerTableFilterDTO,
		@Nonnull Boolean forStatistik
	) {
		final String methodName = "searchBenutzer";

		// We cast to HibernateCriteriaBuilder because we want to use JpaCriteriaQuery#createCountQuery
		HibernateCriteriaBuilder cb =
			(HibernateCriteriaBuilder) persistence.getCriteriaBuilder();
		JpaCriteriaQuery<String> query = cb.createQuery(String.class);
		// Construct from-clause
		Root<Benutzer> root = query.from(Benutzer.class);

		Join<Benutzer, Berechtigung> currentBerechtigungJoin = root.join(
			Benutzer_.berechtigungen
		);
		Join<Berechtigung, Institution> institutionJoin =
			currentBerechtigungJoin.join(
				Berechtigung_.institution,
				JoinType.LEFT
			);
		Join<Berechtigung, Traegerschaft> traegerschaftJoin =
			currentBerechtigungJoin.join(
				Berechtigung_.traegerschaft,
				JoinType.LEFT
			);
		SetJoin<Berechtigung, Gemeinde> gemeindeSetJoin =
			currentBerechtigungJoin.join(
				Berechtigung_.gemeindeList,
				JoinType.LEFT
			);
		Join<Berechtigung, Sozialdienst> sozialdienstJoin =
			currentBerechtigungJoin.join(
				Berechtigung_.sozialdienst,
				JoinType.LEFT
			);

		List<Predicate> predicates = new ArrayList<>();

		// General role based predicates
		Benutzer user =
			getCurrentBenutzer().orElseThrow(
				() -> new EbeguRuntimeException(
					methodName,
					"No User is logged in"
				)
			);

		// Scheinbar sind die berechtigungen nicht geladen, weswegen ich hier zuerst ein
		// getCurrentBerechtigung() mache. somit werden sie geladen. das könnte aber ein allgemeines problem sein
		user.getCurrentBerechtigung();

		Predicate mandantPredicate = cb.equal(
			root.get(Benutzer_.mandant),
			benutzerTableFilterDTO.getMandant()
		);
		predicates.add(mandantPredicate);

		if (!principalBean.isCallerInRole(UserRole.SUPER_ADMIN)) {
			// Not SuperAdmin users are allowed to see all users of their mandant
			setMandantFilterForCurrentUser(user, root, cb, predicates);

			// They cannot see superadmin users
			setSuperAdminFilterForCurrentUser(
				user,
				currentBerechtigungJoin,
				predicates
			);

			// KIBON-1668: diese 2 Filtern muessen fur Statistik und Mandant Rolle nicht verwendet werden
			// als man als Mandant alle Benutzende lesen wollen in die stat, aber nur die von der Mandant bearbeiten
			if (!(forStatistik
				&& principalBean.isCallerInAnyOfRole(
					UserRole.ADMIN_MANDANT,
					UserRole.SACHBEARBEITER_MANDANT
				))) {
				setGemeindeFilterForCurrentUser(
					user,
					gemeindeSetJoin,
					predicates
				);

				setRoleFilterForCurrentUser(
					user,
					currentBerechtigungJoin,
					predicates
				);
			} else {
				// Mandant Benutzende cannot see Antragstellende users in Statistik
				setAntragstellerFilterForCurrentUser(
					currentBerechtigungJoin,
					predicates
				);
			}
		}

		if (principalBean.isCallerInRole(UserRole.ADMIN_INSTITUTION)) {
			setInstitutionFilterForCurrentUser(
				user,
				currentBerechtigungJoin,
				cb,
				predicates
			);
		}

		if (principalBean.isCallerInAnyOfRole(UserRole.ADMIN_FERIENBETREUUNG)) {
			setGemeindeFilterForCurrentFerienbetreuungUser(
				user,
				currentBerechtigungJoin,
				cb,
				predicates
			);
		}

		if (principalBean.isCallerInRole(UserRole.ADMIN_TRAEGERSCHAFT)) {
			setTraegerschaftFilterForCurrentUser(
				user,
				currentBerechtigungJoin,
				cb,
				predicates,
				institutionJoin
			);
		}

		if (principalBean.isCallerInRole(UserRole.ADMIN_SOZIALDIENST)) {
			setSozialdienstFilterForCurrentUser(
				user,
				currentBerechtigungJoin,
				cb,
				predicates
			);
		}

		//prepare predicates from table filters
		if (benutzerTableFilterDTO.getSearch() != null) {
			try {
				preparePredicateFromTableFilter(
					benutzerTableFilterDTO.getSearch(),
					root,
					cb,
					currentBerechtigungJoin,
					institutionJoin,
					predicates
				);
			} catch (DateTimeParseException e) {
				// Kein gueltiges Datum. Es kann kein Gesuch geben, welches passt. Wir geben leer zurueck
				return new ImmutablePair<>(0L, Collections.emptyList());
			}
			BenutzerPredicateObjectDTO predicateObjectDto =
				benutzerTableFilterDTO.getSearch().getPredicateObject();
			// gemeinde
			if (predicateObjectDto.getGemeinde() != null) {
				predicates.add(
					cb.equal(
						gemeindeSetJoin.get(Gemeinde_.name),
						predicateObjectDto.getGemeinde()
					)
				);
			}
			// traegerschaft
			if (predicateObjectDto.getTraegerschaft() != null) {
				predicates.add(
					cb.equal(
						traegerschaftJoin.get(Traegerschaft_.name),
						predicateObjectDto.getTraegerschaft()
					)
				);
			}
			// sozialdienst
			if (predicateObjectDto.getSozialdienst() != null) {
				predicates.add(
					cb.equal(
						sozialdienstJoin.get(Sozialdienst_.name),
						predicateObjectDto.getSozialdienst()
					)
				);
			}
		}
		query.select(root.get(AbstractEntity_.id)).distinct(true);
		if (!predicates.isEmpty()) {
			query.where(
				CriteriaQueryHelper.concatenateExpressions(cb, predicates)
			);
		}

		constructOrderByClause(
			benutzerTableFilterDTO,
			cb,
			query,
			root,
			currentBerechtigungJoin,
			institutionJoin,
			traegerschaftJoin,
			gemeindeSetJoin
		);

		// Prepare and execute the query and build the result
		//select all ids may contain duplicates
		List<String> benutzerIds = persistence.getCriteriaResults(query);

		boolean addInstitutionUsers = principalBean.isCallerInAnyOfRole(
			UserRole.ADMIN_GEMEINDE,
			UserRole.ADMIN_BG,
			UserRole.ADMIN_TS
		);
		JpaCriteriaQuery<String> queryTS = null;
		if (addInstitutionUsers) {
			try {
				queryTS = createSubqueryFITSBenutzende(
					benutzerTableFilterDTO,
					user
				);
			} catch (DateTimeParseException e) {
				// Kein gueltiges Datum. Es kann kein Gesuch geben, welches passt. Wir geben leer zurueck
				return new ImmutablePair<>(0L, Collections.emptyList());
			}
			benutzerIds.addAll(persistence.getCriteriaResults(queryTS));
		}
		List<Benutzer> pagedResult;
		if (benutzerTableFilterDTO.getPagination() != null) {
			int firstIndex = benutzerTableFilterDTO.getPagination()
				.getStart();
			Integer maxresults = benutzerTableFilterDTO.getPagination()
				.getNumber();
			List<String> orderedIdsToLoad =
				SearchUtil.determineDistinctIdsToLoad(
					benutzerIds,
					firstIndex,
					maxresults
				);
			pagedResult = findBenutzer(orderedIdsToLoad);
		} else {
			pagedResult = findBenutzer(benutzerIds);
		}

		long count = persistence.getCriteriaSingleResult(
			query.createCountQuery()
		);
		if (addInstitutionUsers) {
			count += persistence.getCriteriaSingleResult(
				queryTS.createCountQuery()
			);
		}

		return new ImmutablePair<>(count, pagedResult);
	}

	private JpaCriteriaQuery<String> createSubqueryFITSBenutzende(
		@Nonnull BenutzerTableMandantFilterDTO benutzerTableFilterDTO,
		@Nonnull Benutzer user
	) throws DateTimeParseException {
		// We cast to HibernateCriteriaBuilder because we want to use JpaCriteriaQuery#createCountQuery
		HibernateCriteriaBuilder cb =
			(HibernateCriteriaBuilder) persistence.getCriteriaBuilder();
		JpaCriteriaQuery<String> queryTS = cb.createQuery(String.class);
		Set<Gemeinde> userGemeinden = user.extractGemeindenForUser();
		if (userGemeinden.isEmpty()) {
			throw new EbeguRuntimeException(
				"createSubqueryFITSBenutzende",
				"user does not have any Gemeinde"
			);
		}

		// we need to filter TS users separatly because they do not directly belong to a Gemeinde
		List<Predicate> predicatesTS = new ArrayList<>();

		Root<Benutzer> rootTS = queryTS.from(Benutzer.class);

		Join<Benutzer, Berechtigung> currentBerechtigungJoin = rootTS.join(
			Benutzer_.berechtigungen
		);
		Join<Berechtigung, Institution> institutionJoin =
			currentBerechtigungJoin.join(
				Berechtigung_.institution,
				JoinType.LEFT
			);
		Join<Berechtigung, Traegerschaft> traegerschaftJoin =
			currentBerechtigungJoin.join(
				Berechtigung_.traegerschaft,
				JoinType.LEFT
			);
		SetJoin<Berechtigung, Gemeinde> gemeindeSetJoin =
			currentBerechtigungJoin.join(
				Berechtigung_.gemeindeList,
				JoinType.LEFT
			);

		// BEGIN SUBQUERY
		Subquery<String> subquery = queryTS.subquery(String.class);
		Root<InstitutionStammdaten> sqFrom = subquery.from(
			InstitutionStammdaten.class
		);
		Subquery<String> subqueryFI = queryTS.subquery(String.class);
		Root<InstitutionStammdaten> sqFromFI = subqueryFI.from(
			InstitutionStammdaten.class
		);

		Predicate stammdatenTSPredicate =
			cb.isNotNull(
				sqFrom.get(
					InstitutionStammdaten_.institutionStammdatenTagesschule
				)
			);
		Predicate stammdatenFIPredicate =
			cb.isNotNull(
				sqFromFI.get(
					InstitutionStammdaten_.institutionStammdatenFerieninsel
				)
			);

		Join<InstitutionStammdaten, InstitutionStammdatenTagesschule> instStammdatenTSJoin =
			sqFrom.join(
				InstitutionStammdaten_.institutionStammdatenTagesschule,
				JoinType.INNER
			);

		Join<InstitutionStammdaten, InstitutionStammdatenFerieninsel> instStammdatenFIJoin =
			sqFromFI.join(
				InstitutionStammdaten_.institutionStammdatenFerieninsel,
				JoinType.INNER
			);

		Predicate predicateFI = cb.and(
			stammdatenFIPredicate,
			instStammdatenFIJoin.get(
				InstitutionStammdatenFerieninsel_.gemeinde
			).in(userGemeinden)
		);

		Predicate predicateTS = cb.and(
			stammdatenTSPredicate,
			instStammdatenTSJoin.get(
				InstitutionStammdatenTagesschule_.gemeinde
			).in(userGemeinden)
		);

		subquery.where(predicateTS);

		subqueryFI.where(predicateFI);

		subquery.select(
			sqFrom.get(InstitutionStammdaten_.institution)
				.get(Institution_.id)
		);
		subqueryFI.select(
			sqFromFI.get(InstitutionStammdaten_.institution)
				.get(Institution_.id)
		);
		// END SUBQUERY

		Join<Berechtigung, Institution> instiutionenJoin =
			currentBerechtigungJoin.join(
				Berechtigung_.institution,
				JoinType.INNER
			);

		Predicate inSubquery = cb.in(instiutionenJoin.get(Institution_.id))
			.value(subquery);
		Predicate inSubqueryFI = cb.in(
			instiutionenJoin.get(Institution_.id)
		).value(subqueryFI);
		predicatesTS.add(cb.or(inSubquery, inSubqueryFI));

		//prepare predicates from table filters
		if (benutzerTableFilterDTO.getSearch() != null) {
			preparePredicateFromTableFilter(
				benutzerTableFilterDTO.getSearch(),
				rootTS,
				cb,
				currentBerechtigungJoin,
				institutionJoin,
				predicatesTS
			);
		}

		queryTS.select(rootTS.get(AbstractEntity_.id));
		if (!predicatesTS.isEmpty()) {
			queryTS.where(
				CriteriaQueryHelper.concatenateExpressions(
					cb,
					predicatesTS
				)
			);
		}

		constructOrderByClause(
			benutzerTableFilterDTO,
			cb,
			queryTS,
			rootTS,
			currentBerechtigungJoin,
			institutionJoin,
			traegerschaftJoin,
			gemeindeSetJoin
		);

		return queryTS;
	}

	private void preparePredicateFromTableFilter(
		@Nonnull BenutzerSearchDTO benutzerSearchDTO,
		@Nonnull Root<Benutzer> root,
		@Nonnull HibernateCriteriaBuilder cb,
		@Nonnull Join<Benutzer, Berechtigung> currentBerechtigungJoin,
		@Nonnull Join<Berechtigung, Institution> institutionJoin,
		@Nonnull List<Predicate> predicates
	) throws DateTimeParseException {
		//prepare predicates from table filters
		BenutzerPredicateObjectDTO predicateObjectDto =
			benutzerSearchDTO.getPredicateObject();
		// username
		if (predicateObjectDto.getUsername() != null) {
			Expression<String> expression = root.get(Benutzer_.username)
				.as(String.class);
			String value = SearchUtil.withWildcards(
				predicateObjectDto.getUsername()
			);
			predicates.add(cb.like(expression, value));
		}
		// vorname
		if (predicateObjectDto.getVorname() != null) {
			Expression<String> expression = root.get(Benutzer_.vorname)
				.as(String.class);
			String value = SearchUtil.withWildcards(
				predicateObjectDto.getVorname()
			);
			predicates.add(cb.like(expression, value));
		}
		// nachname
		if (predicateObjectDto.getNachname() != null) {
			Expression<String> expression = root.get(Benutzer_.nachname)
				.as(String.class);
			String value = SearchUtil.withWildcards(
				predicateObjectDto.getNachname()
			);
			predicates.add(cb.like(expression, value));
		}
		// email
		if (predicateObjectDto.getEmail() != null) {
			Expression<String> expression = root.get(Benutzer_.email)
				.as(String.class);
			String value = SearchUtil.withWildcards(
				predicateObjectDto.getEmail()
			);
			predicates.add(cb.like(expression, value));
		}
		// role
		if (predicateObjectDto.getRole() != null) {
			predicates.add(
				cb.equal(
					currentBerechtigungJoin.get(Berechtigung_.role),
					predicateObjectDto.getRole()
				)
			);
		}
		// roleGueltigBis
		if (predicateObjectDto.getRoleGueltigBis() != null) {
			LocalDate searchDate =
				LocalDate.parse(
					predicateObjectDto.getRoleGueltigBis(),
					Constants.DATE_FORMATTER
				);
			predicates.add(
				cb.equal(
					currentBerechtigungJoin.get(
						AbstractDateRangedEntity_.gueltigkeit
					)
						.get(DateRange_.gueltigBis),
					searchDate
				)
			);
		}
		// roleGueltigAb
		if (predicateObjectDto.getRoleGueltigAb() != null) {
			LocalDate searchDate =
				LocalDate.parse(
					predicateObjectDto.getRoleGueltigAb(),
					Constants.DATE_FORMATTER
				);
			predicates.add(
				cb.equal(
					currentBerechtigungJoin.get(
						AbstractDateRangedEntity_.gueltigkeit
					)
						.get(DateRange_.gueltigAb),
					searchDate
				)
			);
		}
		// institution
		if (predicateObjectDto.getInstitution() != null) {
			predicates.add(
				cb.equal(
					institutionJoin.get(Institution_.name),
					predicateObjectDto.getInstitution()
				)
			);
		}
		// gesperrt
		if (predicateObjectDto.getStatus() != null
			&& !predicateObjectDto.getStatus().isEmpty()) {
			predicates.add(
				root.get(Benutzer_.status).in(predicateObjectDto.getStatus())
			);
		}
	}

	private void constructOrderByClause(
		@Nonnull BenutzerTableFilterDTO benutzerTableFilterDto,
		CriteriaBuilder cb,
		CriteriaQuery<String> query,
		Root<Benutzer> root,
		Join<Benutzer, Berechtigung> currentBerechtigung,
		Join<Berechtigung, Institution> institution,
		Join<Berechtigung, Traegerschaft> traegerschaft,
		@Nonnull SetJoin<Berechtigung, Gemeinde> gemeindeSetJoin
	) {
		Expression<?> expression;
		if (benutzerTableFilterDto.getSort() != null
			&& benutzerTableFilterDto.getSort().getPredicate() != null) {
			switch (benutzerTableFilterDto.getSort().getPredicate()) {
			case "username":
				expression = root.get(Benutzer_.username);
				break;
			case "vorname":
				expression = root.get(Benutzer_.vorname);
				break;
			case "nachname":
				expression = root.get(Benutzer_.nachname);
				break;
			case "email":
				expression = root.get(Benutzer_.email);
				break;
			case "role":
				expression = currentBerechtigung.get(Berechtigung_.role);
				break;
			case "roleGueltigAb":
				expression = currentBerechtigung.get(
					AbstractDateRangedEntity_.gueltigkeit
				).get(DateRange_.gueltigAb);
				break;
			case "roleGueltigBis":
				expression = currentBerechtigung.get(
					AbstractDateRangedEntity_.gueltigkeit
				).get(DateRange_.gueltigBis);
				break;
			case "gemeinde":
				// Die Gemeinden sind eine Liste innerhalb der Liste (also des Tabelleneintrages).
				// Berechtigungen ohne Gemeinde sollen egal wie sortiert ist am Schluss kommen!
				if (benutzerTableFilterDto.getSort().getReverse()) {
					expression = cb.selectCase()
						.when(gemeindeSetJoin.isNull(), "ZZZZ")
						.otherwise(gemeindeSetJoin.get(Gemeinde_.name));
				} else {
					expression = cb.selectCase()
						.when(gemeindeSetJoin.isNull(), "0000")
						.otherwise(gemeindeSetJoin.get(Gemeinde_.name));
				}
				break;
			case "institution":
				expression = institution.get(Institution_.name);
				break;
			case "traegerschaft":
				expression = traegerschaft.get(Traegerschaft_.name);
				break;
			case "status":
				expression = root.get(Benutzer_.status);
				break;
			default:
				LOG.warn(
					"Using default sort by Timestamp mutiert because there is no specific clause for predicate {}",
					benutzerTableFilterDto.getSort().getPredicate()
				);
				expression = root.get(AbstractEntity_.timestampMutiert);
				break;
			}
			query.orderBy(
				benutzerTableFilterDto.getSort().getReverse() ?
					cb.asc(expression) :
					cb.desc(expression)
			);
		} else {
			// Default sort when nothing is choosen
			expression = root.get(AbstractEntity_.timestampMutiert);
			query.orderBy(cb.desc(expression));
		}
	}

	private List<Benutzer> findBenutzer(@Nonnull List<String> benutzerIds) {
		requireNonNull(benutzerIds);
		if (!benutzerIds.isEmpty()) {
			final CriteriaBuilder cb = persistence.getCriteriaBuilder();
			final CriteriaQuery<Benutzer> query = cb.createQuery(
				Benutzer.class
			);
			Root<Benutzer> root = query.from(Benutzer.class);
			Predicate predicate = root.get(AbstractEntity_.id).in(benutzerIds);
			query.where(predicate);
			//reduce to unique Benutzer
			List<Benutzer> listWithDuplicates = persistence.getCriteriaResults(
				query
			);
			LinkedHashSet<Benutzer> setOfBenutzer = new LinkedHashSet<>();
			//richtige reihenfolge beibehalten
			for (String userId : benutzerIds) {
				listWithDuplicates.stream()
					.filter(benutzer -> benutzer.getId().equals(userId))
					.findFirst()
					.ifPresent(setOfBenutzer::add);
			}
			return new ArrayList<>(setOfBenutzer);
		}
		return Collections.emptyList();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public int handleAbgelaufeneRollen(@Nonnull LocalDate stichtag) {
		requireNonNull(stichtag);
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Benutzer> query = cb.createQuery(Benutzer.class);
		Root<Benutzer> root = query.from(Benutzer.class);
		Join<Benutzer, Berechtigung> currentBerechtigung = root.join(
			Benutzer_.berechtigungen
		);
		Predicate predicateAbgelaufen =
			cb.lessThan(
				currentBerechtigung.get(
					AbstractDateRangedEntity_.gueltigkeit
				).get(DateRange_.gueltigBis),
				stichtag
			);
		query.where(predicateAbgelaufen);
		List<Benutzer> userMitAbgelaufenerRolle = persistence
			.getCriteriaResults(query);

		for (Benutzer benutzer : userMitAbgelaufenerRolle) {
			List<Berechtigung> abgelaufeneBerechtigungen = new ArrayList<>();
			for (Berechtigung berechtigung : benutzer.getBerechtigungen()) {
				if (berechtigung.isAbgelaufen()) {
					abgelaufeneBerechtigungen.add(berechtigung);
				}
			}
			try {
				Berechtigung aktuelleBerechtigung =
					getAktuellGueltigeBerechtigungFuerBenutzer(benutzer);
				persistence.merge(aktuelleBerechtigung);
			} catch (NoResultException nre) {
				// Sonderfall: Die letzte Berechtigung ist abgelaufen. Wir erstellen sofort eine neue anschliessende
				// Berechtigung als Gesuchsteller
				Berechtigung futureGesuchstellerBerechtigung =
					createFutureBerechtigungAsGesuchsteller(
						LocalDate.now(),
						benutzer
					);
				persistence.persist(futureGesuchstellerBerechtigung);
			}
			// Die abgelaufene Rolle löschen
			for (Berechtigung abgelaufeneBerechtigung : abgelaufeneBerechtigungen) {
				LOG.info(
					"... Benutzerrolle ist abgelaufen: {}, war: {}, abgelaufen: {}",
					benutzer.getUsername(),
					abgelaufeneBerechtigung.getRole(),
					abgelaufeneBerechtigung.getGueltigkeit().getGueltigBis()
				);
				benutzer.getBerechtigungen().remove(abgelaufeneBerechtigung);
				persistence.merge(benutzer);
				removeBerechtigung(abgelaufeneBerechtigung);
			}

		}
		return userMitAbgelaufenerRolle.size();
	}

	private Berechtigung createFutureBerechtigungAsGesuchsteller(
		LocalDate startDatum,
		Benutzer benutzer
	) {
		Berechtigung futureGesuchstellerBerechtigung = new Berechtigung();
		futureGesuchstellerBerechtigung.getGueltigkeit()
			.setGueltigAb(startDatum);
		futureGesuchstellerBerechtigung.getGueltigkeit()
			.setGueltigBis(Constants.END_OF_TIME);
		futureGesuchstellerBerechtigung.setRole(GESUCHSTELLER);
		futureGesuchstellerBerechtigung.setBenutzer(benutzer);
		return futureGesuchstellerBerechtigung;
	}

	@Nonnull
	private Berechtigung getAktuellGueltigeBerechtigungFuerBenutzer(
		@Nonnull Benutzer benutzer
	) {
		requireNonNull(benutzer);
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Berechtigung> query = cb.createQuery(
			Berechtigung.class
		);
		Root<Berechtigung> root = query.from(Berechtigung.class);

		ParameterExpression<Benutzer> benutzerParam = cb.parameter(
			Benutzer.class,
			"benutzer"
		);
		ParameterExpression<LocalDate> dateParam = cb.parameter(
			LocalDate.class,
			"date"
		);

		Predicate predicateBenutzer = cb.equal(
			root.get(Berechtigung_.benutzer),
			benutzerParam
		);
		Predicate predicateZeitraum = cb.between(
			dateParam,
			root.get(AbstractDateRangedEntity_.gueltigkeit)
				.get(DateRange_.gueltigAb),
			root.get(AbstractDateRangedEntity_.gueltigkeit)
				.get(DateRange_.gueltigBis)
		);

		query.where(predicateBenutzer, predicateZeitraum);

		TypedQuery<Berechtigung> q = persistence.getEntityManager()
			.createQuery(query);
		q.setParameter(dateParam, LocalDate.now());
		q.setParameter(benutzerParam, benutzer);
		List<Berechtigung> resultList = q.getResultList();

		if (resultList.isEmpty()) {
			throw new NoResultException(
				"No Berechtigung found for Benutzer"
					+ benutzer.getUsername()
			);
		}
		if (resultList.size() > 1) {
			throw new NonUniqueResultException(
				"More than one Berechtigung found for Benutzer "
					+ benutzer.getUsername()
			);
		}
		return resultList.get(0);
	}

	private void removeBerechtigung(@Nonnull Berechtigung berechtigung) {
		requireNonNull(berechtigung);
		keycloakApi.logout(
			berechtigung.getBenutzer()
		);
		persistence.remove(berechtigung);
	}

	@Override
	public void saveBerechtigungHistory(
		@Nonnull Berechtigung berechtigung,
		boolean deleted
	) {
		requireNonNull(berechtigung);
		BerechtigungHistory newBerechtigungsHistory = new BerechtigungHistory(
			berechtigung,
			deleted
		);
		newBerechtigungsHistory.setTimestampErstellt(LocalDateTime.now());
		String userMutiert =
			berechtigung.getUserMutiert() != null ?
				berechtigung.getUserMutiert() :
				Constants.SYSTEM_USER_USERNAME;
		newBerechtigungsHistory.setUserErstellt(userMutiert);
		persistence.persist(newBerechtigungsHistory);
	}

	@Nonnull
	@Override
	public Collection<BerechtigungHistory> getBerechtigungHistoriesForBenutzer(
		@Nonnull Benutzer benutzer
	) {
		requireNonNull(benutzer);
		authorizer.checkReadAuthorization(benutzer);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BerechtigungHistory> query = cb.createQuery(
			BerechtigungHistory.class
		);
		Root<BerechtigungHistory> root = query.from(BerechtigungHistory.class);
		Join<BerechtigungHistory, Benutzer> benutzerJoin = root.join(
			BerechtigungHistory_.benutzer
		);

		ParameterExpression<String> benutzerParam = cb.parameter(
			String.class,
			"username"
		);
		// Wegen eines Fehlers konnten nicht alle BerechtigungHistory Einträge rückwirkend mit einer Benutzer-Entity
		//verknüpft werden. Daher müssen diese per username suchbar bleiben
		Predicate benutzerByUsername = cb.equal(
			root.get(BerechtigungHistory_.username),
			benutzerParam
		);
		Predicate benutzerById = cb.equal(
			root.get(BerechtigungHistory_.benutzer),
			benutzer
		);
		Predicate mandantPredicate = cb.equal(
			benutzerJoin.get(Benutzer_.mandant),
			benutzer.getMandant()
		);

		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		query.where(
			cb.and(cb.or(benutzerById, benutzerByUsername), mandantPredicate)
		);

		TypedQuery<BerechtigungHistory> q = persistence.getEntityManager()
			.createQuery(query);
		q.setParameter(benutzerParam, benutzer.getUsername());
		return q.getResultList();
	}

	@Override
	public boolean isBenutzerDefaultBenutzerOfAnyGemeinde(
		@Nonnull String username
	) {
		requireNonNull(username);
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<GemeindeStammdaten> query = cb.createQuery(
			GemeindeStammdaten.class
		);
		Root<GemeindeStammdaten> root = query.from(GemeindeStammdaten.class);

		ParameterExpression<String> benutzerParam = cb.parameter(
			String.class,
			"username"
		);
		Predicate predicateDefaultGemeinde = cb.equal(
			root.get(GemeindeStammdaten_.defaultBenutzer)
				.get(Benutzer_.username),
			benutzerParam
		);

		Predicate predicateDefaultBG =
			cb.equal(
				root.get(GemeindeStammdaten_.defaultBenutzerBG)
					.get(Benutzer_.username),
				benutzerParam
			);
		Predicate predicateDefaultTS =
			cb.equal(
				root.get(GemeindeStammdaten_.defaultBenutzerTS)
					.get(Benutzer_.username),
				benutzerParam
			);
		Predicate anyDefault = cb.or(
			predicateDefaultBG,
			predicateDefaultTS,
			predicateDefaultGemeinde
		);
		query.where(anyDefault);

		TypedQuery<GemeindeStammdaten> q = persistence.getEntityManager()
			.createQuery(query);
		q.setParameter(benutzerParam, username);
		return !q.getResultList().isEmpty();
	}

	private BenutzerStatus findLastNotGesperrtStatus(Benutzer benutzer) {
		Collection<BerechtigungHistory> history =
			getBerechtigungHistoriesForBenutzer(benutzer);
		BerechtigungHistory lastNotGesperrtHistory = history.stream()
			.filter(x -> x.getStatus() != GESPERRT)
			.findFirst()
			.get();

		return lastNotGesperrtHistory.getStatus();
	}

	private Benutzer checkIfUserIsLoggedIn(String methodName) {
		return getCurrentBenutzer().orElseThrow(
			() -> new EbeguRuntimeException(
				methodName,
				"Non logged in user should never reach this"
			)
		);
	}

	@Override
	public void sendUpdatePasswordEmail(Benutzer benutzer) {
		keycloakApi.sendUpdatePasswordEmail(benutzer);
	}
}
