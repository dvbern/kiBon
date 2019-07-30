/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.BfsGemeinde;
import ch.dvbern.ebegu.entities.BfsGemeinde_;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.GemeindeStammdaten_;
import ch.dvbern.ebegu.entities.Gemeinde_;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GemeindeStatus;
import ch.dvbern.ebegu.enums.SequenceType;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EntityExistsException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * Service fuer Gemeinden
 */
@Stateless
@Local(GemeindeService.class)
@PermitAll
public class GemeindeServiceBean extends AbstractBaseService implements GemeindeService {

	@Inject
	private Persistence persistence;
	@Inject
	private PrincipalBean principalBean;
	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;
	@Inject
	private SequenceService sequenceService;
	@Inject
	private Authorizer authorizer;
	@Inject
	private EinstellungService einstellungService;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Gemeinde saveGemeinde(@Nonnull Gemeinde gemeinde) {
		requireNonNull(gemeinde);
		authorizer.checkWriteAuthorization(gemeinde);

		if (gemeinde.isNew()) {
			initGemeindeNummerAndMandant(gemeinde);
		}
		return persistence.merge(gemeinde);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Gemeinde createGemeinde(@Nonnull Gemeinde gemeinde) {
		Optional<Gemeinde> gemeindeOpt =
			criteriaQueryHelper.getEntityByUniqueAttribute(Gemeinde.class, gemeinde.getName(), Gemeinde_.name);
		if (gemeindeOpt.isPresent()) {
			throw new EntityExistsException(
				KibonLogLevel.INFO,
				Gemeinde.class,
				"name",
				gemeinde.getName(),
				ErrorCodeEnum.ERROR_DUPLICATE_GEMEINDE_NAME);
		}
		final Long bfsNummer = gemeinde.getBfsNummer();
		if (findGemeindeByBSF(bfsNummer).isPresent()) {
			throw new EntityExistsException(
				KibonLogLevel.INFO,
				Gemeinde.class, "bsf",
				Long.toString(bfsNummer),
				ErrorCodeEnum.ERROR_DUPLICATE_GEMEINDE_BSF);
		}
		return saveGemeinde(gemeinde);
	}

	@Nonnull
	@Override
	public Optional<Gemeinde> findGemeinde(@Nonnull String id) {
		requireNonNull(id, "id muss gesetzt sein");
		Gemeinde gemeinde = persistence.find(Gemeinde.class, id);
		return Optional.ofNullable(gemeinde);
	}

	@Nonnull
	private Optional<Gemeinde> findGemeindeByBSF(@Nullable Long bsf) {
		Optional<Gemeinde> gemeindeOpt =
			criteriaQueryHelper.getEntityByUniqueAttribute(Gemeinde.class, bsf, Gemeinde_.bfsNummer);
		return gemeindeOpt;
	}

	@Nonnull
	@Override
	public Collection<Gemeinde> getAllGemeinden() {
		return criteriaQueryHelper.getAllOrdered(Gemeinde.class, Gemeinde_.name);
	}

	private long getNextGemeindeNummer() {
		Mandant mandant = requireNonNull(principalBean.getMandant());

		return sequenceService.createNumberTransactional(SequenceType.GEMEINDE_NUMMER, mandant);
	}

	private void initGemeindeNummerAndMandant(@Nonnull Gemeinde gemeinde) {
		if (gemeinde.getMandant() == null) {
			gemeinde.setMandant(requireNonNull(principalBean.getMandant()));
		}
		if (gemeinde.getGemeindeNummer() == 0) {
			gemeinde.setGemeindeNummer(getNextGemeindeNummer());
		}
	}

	@Nonnull
	@Override
	public Collection<Gemeinde> getAktiveGemeinden() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gemeinde> query = cb.createQuery(Gemeinde.class);
		Root<Gemeinde> root = query.from(Gemeinde.class);
		List<Predicate> predicates = new ArrayList<>();

		// Status muss aktiv sein
		Predicate predicateStatusActive = cb.equal(root.get(Gemeinde_.status), GemeindeStatus.AKTIV);
		predicates.add(predicateStatusActive);

//		// TODO MANDANTEN when developing kibon for multple mandanten we need to filter the mandanten too. Uncommenting the following code
//		// and taking the FIXME into account should be enough
//		// Nur Gemeinden meines Mandanten zurueckgeben
//		final Principal principal = principalBean.getPrincipal();
//		if (!Constants.ANONYMOUS_USER_USERNAME.equals(principal.getName())) {
//			// user anonymous can get the list of active Gemeinden, though anonymous user doesn't really exist
//			// FIXME MANDANTEN this is actually a problem if we work with different Mandanten because in onBoarding there is no user at all
//			// so we cannot get the mandant out of the user. In this case we need to send the mandant when calling this method
//			Mandant mandant = principalBean.getMandant();
//			if (mandant != null) {
//				Predicate predicateMandant = cb.equal(root.get(Gemeinde_.mandant), mandant);
//				predicates.add(predicateMandant);
//			}
//		}

		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public Optional<GemeindeStammdaten> getGemeindeStammdaten(@Nonnull String id) {
		requireNonNull(id, "id muss gesetzt sein");
		GemeindeStammdaten stammdaten = persistence.find(GemeindeStammdaten.class, id);
		if (stammdaten != null) {
			authorizer.checkReadAuthorization(stammdaten.getGemeinde());
		}
		return Optional.ofNullable(stammdaten);
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<GemeindeStammdaten> getGemeindeStammdatenByGemeindeId(@Nonnull String gemeindeId) {
		requireNonNull(gemeindeId, "id muss gesetzt sein");
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<GemeindeStammdaten> query = cb.createQuery(GemeindeStammdaten.class);
		Root<GemeindeStammdaten> root = query.from(GemeindeStammdaten.class);
		Predicate predicate = cb.equal(root.get(GemeindeStammdaten_.gemeinde).get(AbstractEntity_.id), gemeindeId);
		query.where(predicate);
		GemeindeStammdaten stammdaten = persistence.getCriteriaSingleResult(query);
		if (stammdaten != null) {
			authorizer.checkReadAuthorization(stammdaten.getGemeinde());
		}
		return Optional.ofNullable(stammdaten);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_TS, ADMIN_GEMEINDE, SACHBEARBEITER_BG, SACHBEARBEITER_TS,
		SACHBEARBEITER_GEMEINDE,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public GemeindeStammdaten saveGemeindeStammdaten(@Nonnull GemeindeStammdaten stammdaten) {
		requireNonNull(stammdaten);
		authorizer.checkWriteAuthorization(stammdaten.getGemeinde());
		if (stammdaten.isNew()) {
			initGemeindeNummerAndMandant(stammdaten.getGemeinde());
		}
		return persistence.merge(stammdaten);
	}

	@Nonnull
	@Override
	public GemeindeStammdaten uploadLogo(
		@Nonnull String gemeindeId,
		@Nonnull byte[] content,
		@Nonnull String name,
		@Nonnull String type) {
		requireNonNull(gemeindeId);
		requireNonNull(content);
		requireNonNull(name);
		requireNonNull(type);

		final GemeindeStammdaten stammdaten = getGemeindeStammdatenByGemeindeId(gemeindeId).orElseThrow(
			() -> new EbeguEntityNotFoundException("uploadLogo", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId)
		);
		stammdaten.setLogoContent(content);
		stammdaten.setLogoName(name);
		stammdaten.setLogoType(type);
		return saveGemeindeStammdaten(stammdaten);
	}

	@Nonnull
	@Override
	public Collection<BfsGemeinde> getUnregisteredBfsGemeinden(@Nonnull Mandant mandant) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<BfsGemeinde> query = cb.createQuery(BfsGemeinde.class);
		Root<BfsGemeinde> root = query.from(BfsGemeinde.class);
		List<Predicate> predicates = new ArrayList<>();

		List<Long> registeredBfsNummern = getRegisteredBfsNummern(mandant);
		if (!registeredBfsNummern.isEmpty()) {
			Predicate predicate = root.get(BfsGemeinde_.bfsNummer).in(registeredBfsNummern).not();
			predicates.add(predicate);
		}

		// Wenn das Tagesschule-Flag nicht gesetzt ist, dürfen Verbunds-Gemeinden nicht ausgewählt werden können.
		boolean tagesschuleEnabled = mandant.isAngebotTS();
		if (!tagesschuleEnabled) {
			List<Long> verbundsBfsNummern = getVerbundsBfsNummern(mandant);
			Predicate predicateNoVerbund = root.get(BfsGemeinde_.bfsNummer).in(verbundsBfsNummern).not();
			predicates.add(predicateNoVerbund);
		}

		Predicate predicateMandant = cb.equal(root.get(BfsGemeinde_.mandant), mandant);
		predicates.add(predicateMandant);
		query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicates));
		List<BfsGemeinde> unregisteredBfsGemeinden = persistence.getCriteriaResults(query);
		return unregisteredBfsGemeinden;
	}

	@Nonnull
	private List<Long> getRegisteredBfsNummern(@Nonnull Mandant mandant) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<Gemeinde> root = query.from(Gemeinde.class);

		Predicate predicateMandant = cb.equal(root.get(Gemeinde_.mandant), mandant);
		query.where(predicateMandant);
		query.select(root.get(Gemeinde_.bfsNummer));
		List<Long> registeredBfsNummern = persistence.getCriteriaResults(query);
		return registeredBfsNummern;
	}

	@Nonnull
	private List<Long> getVerbundsBfsNummern(@Nonnull Mandant mandant) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<BfsGemeinde> root = query.from(BfsGemeinde.class);

		Predicate predicateMandant = cb.equal(root.get(BfsGemeinde_.mandant), mandant);
		query.where(predicateMandant);
		query.select(root.get(BfsGemeinde_.verbund).get(BfsGemeinde_.bfsNummer));
		List<Long> registeredBfsNummern = persistence.getCriteriaResults(query);
		return registeredBfsNummern;
	}
}
