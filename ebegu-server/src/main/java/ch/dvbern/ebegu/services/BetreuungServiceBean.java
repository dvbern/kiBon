package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.*;
import javax.validation.Valid;
import java.util.*;

/**
 * Service fuer Betreuung
 */
@Stateless
@Local(BetreuungService.class)
public class BetreuungServiceBean extends AbstractBaseService implements BetreuungService {

	@Inject
	private Persistence<Betreuung> persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private InstitutionService institutionService;


	@Override
	@Nonnull
	public Betreuung saveBetreuung(@Valid @Nonnull Betreuung betreuung) {
		Objects.requireNonNull(betreuung);
		return persistence.merge(betreuung);
	}

	@Override
	@Nonnull
	public Optional<Betreuung> findBetreuung(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		Betreuung a = persistence.find(Betreuung.class, key);
		return Optional.ofNullable(a);
	}

	@Override
	@Nonnull
	public Betreuung findBetreuungWithBetreuungsPensen(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuung> query = cb.createQuery(Betreuung.class);
		Root<Betreuung> root = query.from(Betreuung.class);
		root.fetch(Betreuung_.betreuungspensumContainers, JoinType.LEFT);
		query.select(root);
		Predicate idPred = cb.equal(root.get(Betreuung_.id), key);
		query.where(idPred);
		return persistence.getCriteriaSingleResult(query);
	}


	@Override
	public void removeBetreuung(@Nonnull String betreuungId) {
		Objects.requireNonNull(betreuungId);
		Optional<Betreuung> betreuungToRemove = findBetreuung(betreuungId);
		betreuungToRemove.orElseThrow(() -> new EbeguEntityNotFoundException("removeBetreuung", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, betreuungId));
		persistence.remove(betreuungToRemove.get());
	}

	@Override
	@Nonnull
	public Collection<Betreuung> getPendenzenForInstitutionsOrTraegerschaftUser() {
		Optional<Benutzer> benutzerOptional = benutzerService.getCurrentBenutzer();
		if (benutzerOptional.isPresent()) {
			Benutzer benutzer = benutzerOptional.get();
			if (UserRole.SACHBEARBEITER_TRAEGERSCHAFT.equals(benutzer.getRole()) && benutzer.getTraegerschaft() != null) {
				Collection<Institution> allInstitutionenFromTraegerschaft = institutionService.getAllInstitutionenFromTraegerschaft(benutzer.getTraegerschaft().getId());
				return getPendenzenForInstitution((Institution[]) allInstitutionenFromTraegerschaft.toArray(new Institution[allInstitutionenFromTraegerschaft.size()]));
			}
			if (UserRole.SACHBEARBEITER_INSTITUTION.equals(benutzer.getRole()) && benutzer.getInstitution() != null) {
				return getPendenzenForInstitution(benutzer.getInstitution());
			}
		}
		return Collections.emptyList();
	}

	@Nonnull
	private Collection<Betreuung> getPendenzenForInstitution(@Nonnull Institution... institutionen) {
		Objects.requireNonNull(institutionen, "institutionen muss gesetzt sein");
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuung> query = cb.createQuery(Betreuung.class);
		Root<Betreuung> root = query.from(Betreuung.class);
		// Status muss WARTEN sein
		Predicate predicateStatus = cb.equal(root.get(Betreuung_.betreuungsstatus), Betreuungsstatus.WARTEN);
		// Institution
		Predicate predicateInstitution = root.get(Betreuung_.institutionStammdaten).get(InstitutionStammdaten_.institution).in(Arrays.asList(institutionen));

		query.where(predicateStatus, predicateInstitution);
		return persistence.getCriteriaResults(query);
	}
}
