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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Asynchronous;
import javax.ejb.EJBAccessException;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.JaxAntragDTO;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.AbstractPersonEntity_;
import ch.dvbern.ebegu.entities.AntragStatusHistory;
import ch.dvbern.ebegu.entities.AntragStatusHistory_;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Benutzer_;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Dossier_;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Fall_;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchDeletionLog;
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
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KindContainer_;
import ch.dvbern.ebegu.entities.Massenversand;
import ch.dvbern.ebegu.entities.Massenversand_;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.AnmeldungMutationZustand;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.GesuchBetreuungenStatus;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguExistingAntragException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.interceptors.UpdateStatusInterceptor;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.FreigabeCopyUtil;
import ch.dvbern.ebegu.validationgroups.AntragCompleteValidationGroup;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRole.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRole.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer Gesuch
 */
@Stateless
@Local(GesuchService.class)
@PermitAll
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "LocalVariableNamingConvention", "PMD.NcssTypeCount" })
public class GesuchServiceBean extends AbstractBaseService implements GesuchService {

	private static final Logger LOG = LoggerFactory.getLogger(GesuchServiceBean.class.getSimpleName());

	@Inject
	private Persistence persistence;
	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private BenutzerService benutzerService;
	@Inject
	private WizardStepService wizardStepService;
	@Inject
	private AntragStatusHistoryService antragStatusHistoryService;
	@Inject
	private GesuchsperiodeService gesuchsperiodeService;
	@Inject
	private MahnungService mahnungService;
	@Inject
	private GeneratedDokumentService generatedDokumentService;
	@Inject
	private DokumentGrundService dokumentGrundService;
	@Inject
	private Authorizer authorizer;
	@Inject
	private BooleanAuthorizer booleanAuthorizer;
	@Inject
	private PrincipalBean principalBean;
	@Inject
	private MailService mailService;
	@Inject
	private ApplicationPropertyService applicationPropertyService;
	@Inject
	private ZahlungService zahlungService;
	@Inject
	private SuperAdminService superAdminService;
	@Inject
	private FileSaverService fileSaverService;
	@Inject
	private MitteilungService mitteilungService;
	@Inject
	private BetreuungService betreuungService;
	@Inject
	private GesuchDeletionLogService gesuchDeletionLogService;
	@Inject
	private DossierService dossierService;
	@Inject
	private GemeindeService gemeindeService;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Gesuch createGesuch(@Nonnull Gesuch gesuch) {
		Objects.requireNonNull(gesuch);
		final Gesuch persistedGesuch = persistence.persist(gesuch);
		// Die WizardSteps werden direkt erstellt wenn das Gesuch erstellt wird. So vergewissern wir uns dass es kein
		// Gesuch ohne WizardSteps gibt
		wizardStepService.createWizardStepList(persistedGesuch);
		antragStatusHistoryService.saveStatusChange(persistedGesuch, null);
		return persistedGesuch;
	}

	@Nonnull
	@Override
	@PermitAll
	public Gesuch updateGesuch(@Nonnull Gesuch gesuch, boolean saveInStatusHistory, @Nullable Benutzer saveAsUser) {
		return updateGesuch(gesuch, saveInStatusHistory, saveAsUser, true);
	}

	@Nonnull
	@Override
	@PermitAll
	public Gesuch updateGesuch(@Nonnull Gesuch gesuch, boolean saveInStatusHistory) {
		return updateGesuch(gesuch, saveInStatusHistory, null, true);
	}

	@Nonnull
	@Override
	@PermitAll
	public Gesuch updateGesuch(
		@Nonnull Gesuch gesuch,
		boolean saveInStatusHistory,
		@Nullable Benutzer saveAsUser,
		boolean doAuthCheck) {

		if (doAuthCheck) {
			authorizer.checkWriteAuthorization(gesuch);
		}
		Objects.requireNonNull(gesuch);
		final Gesuch gesuchToMerge = removeFinanzieleSituationIfNeeded(gesuch);
		final Gesuch merged = persistence.merge(gesuchToMerge);
		if (saveInStatusHistory) {
			antragStatusHistoryService.saveStatusChange(merged, saveAsUser);
		}
		return merged;
	}

	private Gesuch removeFinanzieleSituationIfNeeded(@NotNull Gesuch gesuch) {
		if (!EbeguUtil.isFinanzielleSituationRequired(gesuch)) {
			resetFieldsFamiliensituation(gesuch);
			removeFinanzielleSituationGS(gesuch.getGesuchsteller1());
			removeFinanzielleSituationGS(gesuch.getGesuchsteller2());
			gesuch.setEinkommensverschlechterungInfoContainer(null);
		}
		return gesuch;
	}

	private void removeFinanzielleSituationGS(@Nullable GesuchstellerContainer gesuchsteller) {
		if (gesuchsteller != null) {
			gesuchsteller.setFinanzielleSituationContainer(null);
			gesuchsteller.setEinkommensverschlechterungContainer(null);
		}
	}

	private void resetFieldsFamiliensituation(@NotNull Gesuch gesuch) {
		final Familiensituation familiensituation = gesuch.extractFamiliensituation();
		if (familiensituation != null) {
			if (Objects.equals(true, familiensituation.getSozialhilfeBezueger())) {
				familiensituation.setVerguenstigungGewuenscht(null);
			}
			familiensituation.setGemeinsameSteuererklaerung(null);
		}
	}

	@Nonnull
	@Override
	@PermitAll
	@Interceptors(UpdateStatusInterceptor.class)
	public Optional<Gesuch> findGesuch(@Nonnull String key) {
		return findGesuch(key, true);
	}

	@Nonnull
	@Override
	@PermitAll
	@Interceptors(UpdateStatusInterceptor.class)
	public Optional<Gesuch> findGesuch(@Nonnull String key, boolean doAuthCheck) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		Gesuch gesuch = persistence.find(Gesuch.class, key);
		if (doAuthCheck) {
			authorizer.checkReadAuthorization(gesuch);
		}
		return Optional.ofNullable(gesuch);
	}

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS, GESUCHSTELLER })
	public Optional<Gesuch> findGesuchForFreigabe(@Nonnull String gesuchId) {
		Objects.requireNonNull(gesuchId, "gesuchId muss gesetzt sein");
		Gesuch gesuch = persistence.find(Gesuch.class, gesuchId);
		if (gesuch != null) {
			authorizer.checkReadAuthorizationForFreigabe(gesuch);
			return Optional.of(gesuch);
		}
		return Optional.empty();
	}

	@PermitAll
	@Override
	public List<Gesuch> findReadableGesuche(@Nullable Collection<String> gesuchIds) {
		if (gesuchIds == null || gesuchIds.isEmpty()) {
			return Collections.emptyList();
		}
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		Predicate predicateId = root.get(AbstractEntity_.id).in(gesuchIds);
		query.where(predicateId);
		query.orderBy(cb.asc(root.get(Gesuch_.dossier).get(Dossier_.fall).get(AbstractEntity_.id)));
		List<Gesuch> criteriaResults = persistence.getCriteriaResults(query);
		return criteriaResults.stream()
			.filter(gesuch -> this.booleanAuthorizer.hasReadAuthorization(gesuch))
			.collect(Collectors.toList());
	}

	@Nonnull
	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN })
	public Collection<Gesuch> getAllGesuche() {
		return new ArrayList<>(criteriaQueryHelper.getAll(Gesuch.class));
	}

	@Nonnull
	@Override
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public Collection<Gesuch> getGesucheForBenutzerPendenzenBG(@Nonnull String benutzername) {
		Objects.requireNonNull(benutzername);
		Benutzer benutzer = benutzerService.findBenutzer(benutzername)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"getGesucheForBenutzerPendenzenBG",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				benutzername));

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		Predicate predicateStatus = root.get(Gesuch_.status).in(AntragStatus.FOR_SACHBEARBEITER_JUGENDAMT_PENDENZEN);
		Path<Dossier> dossierPath = root.get(Gesuch_.dossier);
		Predicate predicateVerantwortlicher = cb.equal(dossierPath.get(Dossier_.verantwortlicherBG), benutzer);
		// Gesuchsperiode darf nicht geschlossen sein
		Predicate predicateGesuchsperiode = root.get(Gesuch_.gesuchsperiode).get(Gesuchsperiode_.status)
			.in(GesuchsperiodeStatus.AKTIV, GesuchsperiodeStatus.INAKTIV);

		query.where(predicateStatus, predicateVerantwortlicher, predicateGesuchsperiode);
		query.orderBy(cb.asc(dossierPath.get(Dossier_.fall).get(Fall_.fallNummer)));
		return persistence.getCriteriaResults(query);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	public void removeGesuch(@Nonnull String gesuchId, GesuchDeletionCause deletionCause) {
		Objects.requireNonNull(gesuchId);
		Gesuch gesToRemove = findGesuch(gesuchId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"removeGesuch",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchId));
		authorizer.checkWriteAuthorization(gesToRemove);
		//Remove all depending objects
		wizardStepService.removeSteps(gesToRemove);  //wizard steps removen
		mahnungService.removeAllMahnungenFromGesuch(gesToRemove);
		generatedDokumentService.removeAllGeneratedDokumenteFromGesuch(gesToRemove);
		dokumentGrundService.removeAllDokumentGrundeFromGesuch(gesToRemove);
		fileSaverService.removeAllFromSubfolder(gesToRemove.getId());
		antragStatusHistoryService.removeAllAntragStatusHistoryFromGesuch(gesToRemove);
		zahlungService.deleteZahlungspositionenOfGesuch(gesToRemove);
		mitteilungService.removeAllBetreuungMitteilungenForGesuch(gesToRemove);

		resetMutierteAnmeldungen(gesToRemove);

		// Jedes Loeschen eines Gesuchs muss protokolliert werden
		GesuchDeletionLog gesuchDeletionLog = new GesuchDeletionLog(gesToRemove, deletionCause);
		gesuchDeletionLogService.saveGesuchDeletionLog(gesuchDeletionLog);

		//Finally remove the Gesuch when all other objects are really removed
		persistence.remove(gesToRemove);
	}

	/**
	 * Nimmt alle Anmeldungen vom eingegebenen Gesuch und setzt alle vorgaengerAnmeldungen auf
	 * AnmeldungMutationZustand.AKTUELLE_ANMELDUNG.
	 * Wenn eine Mutation geloescht wird, ist das vorgaengerGesuch und deshalb auch die vorgaengerAnmeldungen AKTUELL
	 *
	 * @param currentGesuch das zurzeit neueste Gesuch
	 */
	private void resetMutierteAnmeldungen(@Nonnull Gesuch currentGesuch) {
		currentGesuch.extractAllBetreuungen().stream()
			.filter(betreuung -> betreuung.isAngebotSchulamt() && betreuung.getVorgaengerId() != null)
			.forEach(betreuung -> {
				Betreuung vorgaengerBetreuung = betreuungService.findBetreuung(betreuung.getVorgaengerId())
					.orElseThrow(() -> new EbeguEntityNotFoundException(
						"resetMutierteAnmeldungen",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						betreuung.getVorgaengerId()));
				vorgaengerBetreuung.setAnmeldungMutationZustand(AnmeldungMutationZustand.AKTUELLE_ANMELDUNG);
				persistence.merge(vorgaengerBetreuung);
			});
	}

	@Nonnull
	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN })
	public List<Gesuch> findGesuchByGSName(String nachname, String vorname) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		ParameterExpression<String> nameParam = cb.parameter(String.class, "nachname");
		Path<Gesuchsteller> gsJAPath = root.get(Gesuch_.gesuchsteller1).get(GesuchstellerContainer_.gesuchstellerJA);
		Predicate namePredicate = cb.equal(gsJAPath.get(AbstractPersonEntity_.nachname), nameParam);

		ParameterExpression<String> vornameParam = cb.parameter(String.class, "vorname");
		Predicate vornamePredicate = cb.equal(gsJAPath.get(AbstractPersonEntity_.vorname), vornameParam);

		query.where(namePredicate, vornamePredicate);
		TypedQuery<Gesuch> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(nameParam, nachname);
		q.setParameter(vornameParam, vorname);

		return q.getResultList();
	}

	@Override
	@Nonnull
	@RolesAllowed({ GESUCHSTELLER, SUPER_ADMIN })
	public List<Gesuch> getAntraegeOfDossier(@Nonnull Dossier dossier) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		// Fall
		Predicate predicate = cb.equal(root.get(Gesuch_.dossier), dossier);
		// Keine Papier-Antraege, die noch nicht verfuegt sind
		Predicate predicatePapier = cb.equal(root.get(Gesuch_.eingangsart), Eingangsart.PAPIER); // todo remove ???
		Predicate predicateStatus = root.get(Gesuch_.status).in(AntragStatus.getAllVerfuegtStates()).not();
		Predicate predicateUnverfuegtesPapiergesuch =
			CriteriaQueryHelper.concatenateExpressions(cb, predicatePapier, predicateStatus);
		if (predicateUnverfuegtesPapiergesuch != null) {
			Predicate predicateNichtUnverfuegtePapierGesuch = predicateUnverfuegtesPapiergesuch.not();
			query.orderBy(cb.desc(root.get(Gesuch_.laufnummer)));
			query.where(predicate, predicateNichtUnverfuegtePapierGesuch);

			List<Gesuch> gesuche = persistence.getCriteriaResults(query);
			authorizer.checkReadAuthorizationGesuche(gesuche);
			return gesuche;
		}
		return Collections.emptyList();
	}

	/**
	 * Diese Methode sucht alle Antraege die zu dem gegebenen Dossier gehoeren.
	 * Die Antraege werden aber je nach Benutzerrolle gefiltert.
	 * - SACHBEARBEITER_TRAEGERSCHAFT oder SACHBEARBEITER_INSTITUTION - werden nur diejenige Antraege zurueckgegeben,
	 * die mindestens ein Angebot fuer die InstituionEn des Benutzers haben
	 * - SACHBEARBEITER_TS/ADMIN_TS - werden nur diejenige Antraege zurueckgegeben, die mindestens ein Angebot von Typ
	 * Schulamt haben
	 * - SACHBEARBEITER_BG oder ADMIN_BG - werden nur diejenige Antraege zurueckgegeben, die ein Angebot von einem
	 * anderen
	 * Typ als Schulamt haben oder ueberhaupt kein Angebot
	 */
	@Nonnull
	@Override
	@PermitAll
	public List<JaxAntragDTO> getAllAntragDTOForDossier(String dossierId) {
		authorizer.checkReadAuthorizationDossier(dossierId);

		final Optional<Benutzer> optBenutzer = benutzerService.getCurrentBenutzer();
		if (optBenutzer.isPresent()) {
			final Benutzer benutzer = optBenutzer.get();

			final CriteriaBuilder cb = persistence.getCriteriaBuilder();
			final CriteriaQuery<JaxAntragDTO> query = cb.createQuery(JaxAntragDTO.class);
			Root<Gesuch> root = query.from(Gesuch.class);

			Join<InstitutionStammdaten, Institution> institutionJoin = null;
			Join<Betreuung, InstitutionStammdaten> institutionstammdatenJoin = null;

			if (principalBean.isCallerInAnyOfRole(
				UserRole.ADMIN_TRAEGERSCHAFT,
				UserRole.SACHBEARBEITER_TRAEGERSCHAFT,
				UserRole.ADMIN_INSTITUTION,
				UserRole.SACHBEARBEITER_INSTITUTION,
				UserRole.SACHBEARBEITER_TS,
				UserRole.ADMIN_TS,
				UserRole.ADMIN_BG,
				UserRole.SACHBEARBEITER_BG,
				UserRole.ADMIN_GEMEINDE,
				UserRole.SACHBEARBEITER_GEMEINDE)) {
				// Join all the relevant relations only when the User belongs to Admin, JA, Schulamt, Institution or
				// Traegerschaft
				SetJoin<Gesuch, KindContainer> kindContainers = root.join(Gesuch_.kindContainers, JoinType.LEFT);
				SetJoin<KindContainer, Betreuung> betreuungen =
					kindContainers.join(KindContainer_.betreuungen, JoinType.LEFT);
				institutionstammdatenJoin = betreuungen.join(Betreuung_.institutionStammdaten, JoinType.LEFT);
				institutionJoin = institutionstammdatenJoin.join(InstitutionStammdaten_.institution, JoinType.LEFT);
			}
			Join<Gesuch, Dossier> dossierJoin = root.join(Gesuch_.dossier);
			Join<Dossier, Fall> fallJoin = dossierJoin.join(Dossier_.fall);

			Join<Fall, Benutzer> besitzerJoin = fallJoin.join(Fall_.besitzer, JoinType.LEFT);

			query.multiselect(
				root.get(AbstractEntity_.id),
				root.get(Gesuch_.gesuchsperiode).get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
				root.get(Gesuch_.gesuchsperiode).get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis),
				root.get(Gesuch_.eingangsdatum),
				root.get(Gesuch_.eingangsdatumSTV),
				root.get(Gesuch_.typ),
				root.get(Gesuch_.status),
				root.get(Gesuch_.laufnummer),
				root.get(Gesuch_.eingangsart),
				besitzerJoin.get(Benutzer_.username) //wir machen hier extra vorher einen left join
			).distinct(true);

			ParameterExpression<String> dossierIdParam = cb.parameter(String.class, "dossierId");

			List<Predicate> predicatesToUse = new ArrayList<>();
			Predicate dossierPredicate = cb.equal(root.get(Gesuch_.dossier).get(AbstractEntity_.id), dossierIdParam);
			predicatesToUse.add(dossierPredicate);

			// Alle AUSSER Gesuchsteller, Institution und Trägerschaft muessen im Status eingeschraenkt werden,
			// d.h. sie duerfen IN_BEARBEITUNG_GS und FREIGABEQUITTUNG NICHT sehen
			if (!(principalBean.isCallerInAnyOfRole(
				UserRole.GESUCHSTELLER,
				UserRole.ADMIN_TRAEGERSCHAFT,
				UserRole.SACHBEARBEITER_TRAEGERSCHAFT,
				UserRole.ADMIN_INSTITUTION,
				UserRole.SACHBEARBEITER_INSTITUTION))) {
				// Nur GS darf ein Gesuch sehen, das sich im Status BEARBEITUNG_GS oder FREIGABEQUITTUNG befindet
				predicatesToUse.add(root.get(Gesuch_.status)
					.in(AntragStatus.IN_BEARBEITUNG_GS, AntragStatus.FREIGABEQUITTUNG)
					.not());
			}

			if (institutionJoin != null) {
				// only if the institutionJoin was set
				if (principalBean.isCallerInAnyOfRole(ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT)) {
					predicatesToUse.add(cb.equal(
						institutionJoin.get(Institution_.traegerschaft),
						benutzer.getTraegerschaft()));
				}
				if (principalBean.isCallerInAnyOfRole(ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION)) {
					// es geht hier nicht um die institutionJoin des zugewiesenen benutzers sondern um die
					// institutionJoin des eingeloggten benutzers
					predicatesToUse.add(cb.equal(institutionJoin, benutzer.getInstitution()));
				}
			}
			if (principalBean.isCallerInRole(UserRole.GESUCHSTELLER)) {
				// Keine Papier-Antraege, die noch nicht verfuegt sind
				Predicate predicatePapier = cb.equal(root.get(Gesuch_.eingangsart), Eingangsart.PAPIER);
				Predicate predicateStatus = root.get(Gesuch_.status).in(AntragStatus.getAllVerfuegtStates()).not();
				Predicate predicateUnverfuegtesPapiergesuch =
					CriteriaQueryHelper.concatenateExpressions(cb, predicatePapier, predicateStatus);
				if (predicateUnverfuegtesPapiergesuch != null) {
					Predicate predicateNichtUnverfuegtePapierGesuch = predicateUnverfuegtesPapiergesuch.not();
					predicatesToUse.add(predicateNichtUnverfuegtePapierGesuch);
				}
			}

			query.where(CriteriaQueryHelper.concatenateExpressions(cb, predicatesToUse));

			query.orderBy(cb.asc(root.get(Gesuch_.laufnummer)));
			TypedQuery<JaxAntragDTO> q = persistence.getEntityManager().createQuery(query);
			q.setParameter(dossierIdParam, dossierId);

			return q.getResultList();
		}
		return new ArrayList<>();
	}

	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN })
	@Nonnull
	public List<String> getAllGesuchIDsForFall(String fallId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<Gesuch> root = query.from(Gesuch.class);

		query.select(root.get(AbstractEntity_.id));

		ParameterExpression<String> fallIdParam = cb.parameter(String.class, "fallId");

		Path<Fall> fallPath = root.get(Gesuch_.dossier).get(Dossier_.fall);
		Predicate fallPredicate = cb.equal(fallPath.get(AbstractEntity_.id), fallIdParam);
		query.where(fallPredicate);
		query.orderBy(cb.desc(root.get(Gesuch_.laufnummer)));
		TypedQuery<String> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(fallIdParam, fallId);

		return q.getResultList();
	}

	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN })
	@Nonnull
	public List<String> getAllGesuchIDsForDossier(@Nonnull String dossierId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<Gesuch> root = query.from(Gesuch.class);

		query.select(root.get(AbstractEntity_.id));

		ParameterExpression<String> dossierIdParam = cb.parameter(String.class, "dossierId");

		Predicate dossierPredicate = cb.equal(root.get(Gesuch_.dossier).get(AbstractEntity_.id), dossierIdParam);
		query.where(dossierPredicate);
		query.orderBy(cb.desc(root.get(Gesuch_.laufnummer)));
		TypedQuery<String> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(dossierIdParam, dossierId);

		return q.getResultList();
	}

	@Override
	@PermitAll
	@Nonnull
	public List<Gesuch> getAllGesuchForDossier(@Nonnull String dossierId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);
		Root<Gesuch> root = query.from(Gesuch.class);

		ParameterExpression<String> dossierIdParam = cb.parameter(String.class, "dossierId");

		Predicate dossierPredicate = cb.equal(root.get(Gesuch_.dossier).get(AbstractEntity_.id), dossierIdParam);
		query.where(dossierPredicate);
		query.orderBy(cb.desc(root.get(Gesuch_.laufnummer)));
		TypedQuery<Gesuch> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(dossierIdParam, dossierId);

		return q.getResultList();
	}

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS })
	public List<Gesuch> getAllGesucheForDossierAndPeriod(
		@Nonnull Dossier dossier,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		authorizer.checkReadAuthorizationDossier(dossier);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		Predicate dossierPredicate = cb.equal(root.get(Gesuch_.dossier), dossier);
		Predicate gesuchsperiodePredicate = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);

		query.where(dossierPredicate, gesuchsperiodePredicate);
		return persistence.getCriteriaResults(query);
	}

	@Override
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS, GESUCHSTELLER })
	public Gesuch antragFreigabequittungErstellen(@Nonnull Gesuch gesuch, AntragStatus statusToChangeTo) {
		authorizer.checkWriteAuthorization(gesuch);

		// Zum jetzigen Zeitpunkt muss das Gesuch zwingend in einem kompletten / gueltigen Zustand sein
		validateGesuchComplete(gesuch);

		gesuch.setFreigabeDatum(LocalDate.now());

		if (AntragStatus.FREIGEGEBEN == statusToChangeTo) {
			// Nur wenn das Gesuch direkt freigegeben wird, muessen wir das Eingangsdatum auch setzen
			gesuch.setEingangsdatum(LocalDate.now());
		}

		gesuch.setStatus(statusToChangeTo);

		// Step Freigabe gruen
		wizardStepService.setWizardStepOkay(gesuch.getId(), WizardStepName.FREIGABE);

		return updateGesuch(gesuch, true, null);
	}

	@SuppressWarnings("PMD.CollapsibleIfStatements")
	@Nonnull
	@Override
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS, GESUCHSTELLER })
	public Gesuch antragFreigeben(
		@Nonnull String gesuchId, @Nullable String usernameJA,
		@Nullable String usernameSCH) {
		//direkt ueber persistence da wir eigentlich noch nicht leseberechtigt sind)
		Optional<Gesuch> gesuchOptional = Optional.ofNullable(persistence.find(Gesuch.class, gesuchId));
		if (gesuchOptional.isPresent()) {
			Gesuch gesuch = gesuchOptional.get();

			// Zum jetzigen Zeitpunkt muss das Gesuch zwingend in einem kompletten / gueltigen Zustand sein
			validateGesuchComplete(gesuch);

			if (gesuch.getStatus() != AntragStatus.FREIGABEQUITTUNG && gesuch.getStatus() != AntragStatus
				.IN_BEARBEITUNG_GS) {
				throw new EbeguRuntimeException(
					"antragFreigeben",
					"Gesuch war im falschen Status: "
						+ gesuch.getStatus()
						+ " wir erwarten aber nur Freigabequittung oder In Bearbeitung GS",
					"Das Gesuch wurde bereits freigegeben");
			}

			this.authorizer.checkWriteAuthorization(gesuch);

			// Die Daten des GS in die entsprechenden Containers kopieren
			FreigabeCopyUtil.copyForFreigabe(gesuch);
			// Je nach Status
			if (gesuch.getStatus() != AntragStatus.FREIGABEQUITTUNG) {
				// Es handelt sich um eine Mutation ohne Freigabequittung: Wir setzen das Tagesdatum als FreigabeDatum
				// an dem es der Gesuchsteller einreicht
				gesuch.setFreigabeDatum(LocalDate.now());
			}

			// Eventuelle Schulamt-Anmeldungen auf AUSGELOEST setzen
			for (Betreuung betreuung : gesuch.extractAllBetreuungen()) {
				if (betreuung.getBetreuungsstatus() == Betreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST) {
					betreuung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST);
				}
				// Set noch nicht freigegebene Betreuungen to aktuelle Anmeldung bei Freigabe
				if (betreuung.isAngebotSchulamt()) {
					if (betreuung.getAnmeldungMutationZustand() == AnmeldungMutationZustand.NOCH_NICHT_FREIGEGEBEN) {
						betreuung.setAnmeldungMutationZustand(AnmeldungMutationZustand.AKTUELLE_ANMELDUNG);
						if (betreuung.getVorgaengerId() != null) {
							betreuungService.changeAnmeldungMutationZustand(
								betreuung.getVorgaengerId(),
								AnmeldungMutationZustand.MUTIERT);
						}
					}
				}
			}

			// Den Gesuchsstatus auf Freigageben setzen (auch bei nur Schulamt-Gesuchen)
			gesuch.setStatus(AntragStatus.FREIGEGEBEN);

			// Step Freigabe gruen
			wizardStepService.setWizardStepOkay(gesuch.getId(), WizardStepName.FREIGABE);

			//VERANTWORTLICHE
			if (!gesuch.isMutation()) {
				// in case of erstgesuch: Verantwortliche werden beim einlesen gesetzt und kommen vom client
				setVerantwortliche(usernameJA, usernameSCH, gesuch);
			} else {
				// in case of mutation, we take default Verantwortliche and set them only if not set...
				Optional<GemeindeStammdaten> gemeindeStammdaten =
					gemeindeService.getGemeindeStammdatenByGemeindeId(gesuchId);

				setVerantwortliche(
					gemeindeStammdaten.map(GemeindeStammdaten::getDefaultBenutzerBG).orElse(null),
					gemeindeStammdaten.map(GemeindeStammdaten::getDefaultBenutzerTS).orElse(null),
					gesuch,
					true,
					false);
			}

			// Falls es ein OnlineGesuch war: Das Eingangsdatum setzen
			if (Eingangsart.ONLINE == gesuch.getEingangsart()) {
				gesuch.setEingangsdatum(LocalDate.now());
			}

			final Gesuch merged = persistence.merge(gesuch);
			antragStatusHistoryService.saveStatusChange(merged, null);
			return merged;
		}

		throw new EbeguEntityNotFoundException("antragFreigeben", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchId);
	}

	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS, GESUCHSTELLER })
	private boolean setVerantwortliche(
		@Nullable String usernameBG,
		@Nullable String usernameTS,
		@Nonnull Gesuch gesuch) {

		Benutzer verantwortlicherBG = null;
		Benutzer verantwortlicherTS = null;

		if (usernameBG != null) {
			verantwortlicherBG = benutzerService.findBenutzer(usernameBG).orElse(null);
		}
		if (usernameTS != null) {
			verantwortlicherTS = benutzerService.findBenutzer(usernameTS).orElse(null);
		}

		return setVerantwortliche(verantwortlicherBG, verantwortlicherTS, gesuch, false, false);
	}

	@Override
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS, GESUCHSTELLER })
	public boolean setVerantwortliche(
		@Nullable Benutzer verantwortlicherBG,
		@Nullable Benutzer verantwortlicherTS,
		@Nonnull Gesuch gesuch,
		boolean onlyIfNotSet,
		boolean persist) {

		boolean hasVerantwortlicheChanged = false;

		if (verantwortlicherBG != null && gesuch.hasBetreuungOfJugendamt()) {
			hasVerantwortlicheChanged =
				setVerantwortlicherIfNecessaryBG(verantwortlicherBG, gesuch, onlyIfNotSet, persist);
		}
		if (verantwortlicherTS != null && gesuch.hasBetreuungOfSchulamt()) {
			hasVerantwortlicheChanged =
				setVerantwortlicherIfNecessaryTS(verantwortlicherTS, gesuch, onlyIfNotSet, persist);
		}
		return hasVerantwortlicheChanged;
	}

	private boolean setVerantwortlicherIfNecessaryBG(
		Benutzer user,
		Gesuch gesuch,
		boolean onlyIfNotSet,
		boolean persist) {

		if (user.getRole().isRoleJugendamt() && (gesuch.getDossier().getVerantwortlicherBG() == null
			|| !onlyIfNotSet)) {
			if (persist) {
				dossierService.setVerantwortlicherBG(gesuch.getDossier().getId(), user);
			}
			gesuch.getDossier().setVerantwortlicherBG(user);
			return true;
		}
		return false;
	}

	private boolean setVerantwortlicherIfNecessaryTS(
		Benutzer user,
		Gesuch gesuch,
		boolean onlyIfNotSet,
		boolean persist) {

		if (user.getRole().isRoleSchulamt() && (gesuch.getDossier().getVerantwortlicherTS() == null || !onlyIfNotSet)) {
			if (persist) {
				dossierService.setVerantwortlicherTS(gesuch.getDossier().getId(), user);
			}
			gesuch.getDossier().setVerantwortlicherTS(user);
			return true;
		}
		return false;
	}

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Gesuch setKeinKontingent(@Nonnull Gesuch gesuch) {
		gesuch.setStatus(AntragStatus.KEIN_KONTINGENT);
		return updateGesuch(gesuch, true, null);
	}

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Gesuch setBeschwerdeHaengigForPeriode(@Nonnull Gesuch gesuch) {
		final List<Gesuch> allGesucheForDossier =
			getAllGesucheForDossierAndPeriod(gesuch.getDossier(), gesuch.getGesuchsperiode());
		allGesucheForDossier.iterator().forEachRemaining(gesuchLoop -> {
			if (gesuch.equals(gesuchLoop)) {
				gesuchLoop.setStatus(AntragStatus.BESCHWERDE_HAENGIG);
				updateGesuch(gesuchLoop, true, null);
			}
			// Flag nicht über Service setzen, da u.U. Gesuch noch inBearbeitungGS
			gesuchLoop.setGesperrtWegenBeschwerde(true);
			persistence.merge(gesuchLoop);
		});
		return gesuch;
	}

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Gesuch setAbschliessen(@Nonnull Gesuch gesuch) {
		if (gesuch.hasOnlyBetreuungenOfSchulamt()) {
			gesuch.setTimestampVerfuegt(LocalDateTime.now());
			gesuch.setStatus(AntragStatus.NUR_SCHULAMT);
			wizardStepService.setWizardStepOkay(gesuch.getId(), WizardStepName.VERFUEGEN);

			if (gesuch.getVorgaengerId() != null) {
				final Optional<Gesuch> vorgaengerOpt = findGesuch(gesuch.getVorgaengerId());
				vorgaengerOpt.ifPresent(this::setGesuchUngueltig);
			}

			// neues Gesuch erst nachdem das andere auf ungültig gesetzt wurde setzen wegen unique key
			gesuch.setGueltig(true);

			final Gesuch persistedGesuch = updateGesuch(gesuch, true);

			createFinSitDokument(persistedGesuch, "setAbschliessen");

			return persistedGesuch;
		}
		throw new EbeguRuntimeException(
			"setAbschliessen",
			ErrorCodeEnum.ERROR_INVALID_EBEGUSTATE,
			"Nur reine Schulamt-Gesuche können abgeschlossen werden");
	}

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Gesuch removeBeschwerdeHaengigForPeriode(@Nonnull Gesuch gesuch) {
		Dossier dossier = gesuch.getDossier();
		Gesuchsperiode gesuchsperiode = gesuch.getGesuchsperiode();
		final List<Gesuch> allGesucheForDossier = getAllGesucheForDossierAndPeriod(dossier, gesuchsperiode);
		allGesucheForDossier.iterator().forEachRemaining(gesuchLoop -> {
			if (gesuch.equals(gesuchLoop) && AntragStatus.BESCHWERDE_HAENGIG == gesuchLoop.getStatus()) {
				final AntragStatusHistory lastStatusChange =
					antragStatusHistoryService.findLastStatusChangeBeforeBeschwerde(gesuchLoop);
				gesuchLoop.setStatus(lastStatusChange.getStatus());
				updateGesuch(gesuchLoop, true, null);
			}
			// Flag nicht über Service setzen, da u.U. Gesuch noch inBearbeitungGS
			gesuchLoop.setGesperrtWegenBeschwerde(false);
			persistence.merge(gesuchLoop);
		});
		return gesuch;
	}

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Optional<Gesuch> antragMutieren(@Nonnull String antragId, @Nullable LocalDate eingangsdatum) {
		// Mutiert wird immer das Gesuch mit dem letzten Verfügungsdatum
		Optional<Gesuch> gesuchOptional = findGesuch(antragId);
		if (gesuchOptional.isPresent()) {
			Gesuch gesuch = gesuchOptional.get();
			Dossier dossier = gesuch.getDossier();
			Gesuchsperiode gesuchsperiode = gesuch.getGesuchsperiode();

			if (!isThereAnyOpenMutation(dossier, gesuchsperiode)) {
				authorizer.checkReadAuthorization(gesuch);
				Optional<Gesuch> gesuchForMutationOpt =
					getNeustesVerfuegtesGesuchFuerGesuch(gesuchsperiode, dossier, true);
				Gesuch gesuchForMutation = gesuchForMutationOpt.orElseThrow(() -> new EbeguEntityNotFoundException(
					"antragMutieren",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					"Kein Verfuegtes Gesuch fuer ID " + antragId));
				return getGesuchMutation(eingangsdatum, gesuchForMutation);
			}

			throw new EbeguExistingAntragException("antragMutieren", ErrorCodeEnum.ERROR_EXISTING_ONLINE_MUTATION,
				dossier.getId(), gesuchsperiode.getId());
		}

		throw new EbeguEntityNotFoundException(
			"antragMutieren",
			"Es existiert kein Antrag mit ID, kann keine Mutation erstellen " + antragId,
			antragId);
	}

	@Override
	@Nonnull
	@RolesAllowed(SUPER_ADMIN)
	public Optional<Gesuch> testfallMutieren(
		@Nonnull String dossierID,
		@Nonnull String gesuchsperiodeId,
		@Nonnull LocalDate eingangsdatum) {
		// Mutiert wird immer das Gesuch mit dem letzten Verfügungsdatum
		final Optional<Dossier> dossier = dossierService.findDossier(dossierID);
		final Optional<Gesuchsperiode> gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId);

		if (dossier.isPresent() && gesuchsperiode.isPresent()) {
			if (!isThereAnyOpenMutation(dossier.get(), gesuchsperiode.get())) {
				Gesuch gesuchForMutation =
					getNeustesVerfuegtesGesuchFuerGesuch(gesuchsperiode.get(), dossier.get(), true)
						.orElseThrow(() -> new EbeguEntityNotFoundException(
							"antragMutieren",
							ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
							"Kein Verfuegtes Gesuch fuer Fallnummer " + dossierID));
				return getGesuchMutation(eingangsdatum, gesuchForMutation);
			}

			throw new EbeguExistingAntragException(
				"antragMutieren",
				ErrorCodeEnum.ERROR_EXISTING_ONLINE_MUTATION,
				dossier.get().getId(),
				gesuchsperiodeId);
		}

		throw new EbeguEntityNotFoundException(
			"antragMutieren",
			"dossier oder gesuchsperiode konnte nicht geladen werden  fallNr:"
				+ dossierID
				+ "gsPerID"
				+ gesuchsperiodeId);
	}

	private boolean isThereAnyOpenMutation(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		List<Gesuch> criteriaResults = findExistingOpenMutationen(dossier, gesuchsperiode);
		return !criteriaResults.isEmpty();
	}

	/**
	 * Diese Methode gibt eine Liste zurueck. Diese Liste sollte aber maximal eine Mutation enthalten, da es
	 * unmoeglich ist,
	 * mehrere offene Mutationen in einem Dossier fuer dieselbe Gesuchsperiode zu haben. Rechte werden nicht
	 * beruecksichtigt
	 * d.h. alle Gesuche werden geguckt und daher die richtige letzte Mutation wird zurueckgegeben.
	 */
	private List<Gesuch> findExistingOpenMutationen(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		Predicate predicateMutation = root.get(Gesuch_.typ).in(AntragTyp.MUTATION);
		Predicate predicateStatus = root.get(Gesuch_.status).in(AntragStatus.getAllVerfuegtStates()).not();
		Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);
		Predicate predicateDossier = cb.equal(root.get(Gesuch_.dossier), dossier);

		query.where(predicateMutation, predicateStatus, predicateGesuchsperiode, predicateDossier);
		query.select(root);
		return persistence.getCriteriaResults(query);
	}

	private List<Gesuch> findExistingFolgegesuch(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		Predicate predicateMutation = root.get(Gesuch_.typ).in(AntragTyp.ERNEUERUNGSGESUCH);
		Predicate predicateStatus = root.get(Gesuch_.status).in(AntragStatus.getInBearbeitungGSStates());
		Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);
		Predicate predicateDossier = cb.equal(root.get(Gesuch_.dossier), dossier);

		query.where(predicateMutation, predicateStatus, predicateGesuchsperiode, predicateDossier);
		query.select(root);
		return persistence.getCriteriaResults(query);
	}

	private Optional<Gesuch> getGesuchMutation(@Nullable LocalDate eingangsdatum, @Nonnull Gesuch gesuchForMutation) {
		Eingangsart eingangsart = calculateEingangsart();
		Gesuch mutation = gesuchForMutation.copyForMutation(new Gesuch(), eingangsart);
		if (eingangsdatum != null) {
			mutation.setEingangsdatum(eingangsdatum);
		}
		return Optional.of(mutation);
	}

	@Nonnull
	private Eingangsart calculateEingangsart() {
		Eingangsart eingangsart;
		if (this.principalBean.isCallerInRole(UserRole.GESUCHSTELLER)) {
			eingangsart = Eingangsart.ONLINE;
		} else {
			eingangsart = Eingangsart.PAPIER;
		}
		return eingangsart;
	}

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Optional<Gesuch> antragErneuern(
		@Nonnull String antragId,
		@Nonnull String gesuchsperiodeId,
		@Nullable LocalDate eingangsdatum) {

		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"findGesuchsperiode",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeId));
		Gesuch gesuch = findGesuch(antragId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"antragErneuern",
				"Es existiert kein Antrag mit ID, kann kein Erneuerungsgesuch erstellen " + antragId,
				antragId));
		List<Gesuch> allGesucheForDossierAndPeriod =
			getAllGesucheForDossierAndPeriod(gesuch.getDossier(), gesuchsperiode);
		if (allGesucheForDossierAndPeriod.isEmpty()) {
			Gesuch gesuchForErneuerung = getGesuchFuerErneuerungsantrag(gesuch.getDossier())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"antragErneuern",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					"Kein Verfuegtes Gesuch fuer ID " + antragId));
			return getErneuerungsgesuch(eingangsdatum, gesuchForErneuerung, gesuchsperiode);
		}

		// must have the gesuchsperiodeID as first item in the arguments list
		throw new EbeguExistingAntragException("antragErneuern", ErrorCodeEnum.ERROR_EXISTING_ERNEUERUNGSGESUCH,
			gesuch.getDossier().getId(), gesuchsperiodeId);
	}

	private Optional<Gesuch> getErneuerungsgesuch(
		@Nullable LocalDate eingangsdatum,
		@Nonnull Gesuch gesuchForErneuerung,
		@Nonnull Gesuchsperiode gesuchsperiode) {

		Eingangsart eingangsart = calculateEingangsart();
		Gesuch erneuerungsgesuch = gesuchForErneuerung.copyForErneuerung(new Gesuch(), gesuchsperiode, eingangsart);
		if (eingangsdatum != null) {
			erneuerungsgesuch.setEingangsdatum(eingangsdatum);
		}
		return Optional.of(erneuerungsgesuch);
	}

	@Override
	@Nonnull
	public Optional<Gesuch> getNeustesVerfuegtesGesuchFuerGesuch(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Dossier dossier,
		boolean doAuthCheck) {

		if (doAuthCheck) {
			authorizer.checkReadAuthorizationDossier(dossier);
		}

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		ParameterExpression<Dossier> dossierParam = cb.parameter(Dossier.class, "dossierId");
		ParameterExpression<Gesuchsperiode> gesuchsperiodeParam = cb.parameter(Gesuchsperiode.class, "gp");

		Predicate predicateStatus = root.get(Gesuch_.status).in(AntragStatus.getAllVerfuegtStates());
		Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiodeParam);
		Predicate predicateDossier = cb.equal(root.get(Gesuch_.dossier), dossierParam);
		Predicate predicateGueltig = cb.equal(root.get(Gesuch_.gueltig), Boolean.TRUE);

		query.where(predicateStatus, predicateGesuchsperiode, predicateGueltig, predicateDossier);
		query.select(root);

		TypedQuery<Gesuch> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(dossierParam, dossier);
		typedQuery.setParameter(gesuchsperiodeParam, gesuchsperiode);

		List<Gesuch> criteriaResults = typedQuery.getResultList();

		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}

		Gesuch gesuch = criteriaResults.get(0);
		if (doAuthCheck) {
			authorizer.checkReadAuthorization(gesuch);
		}
		return Optional.of(gesuch);
	}

	@Override
	@Nonnull
	@PermitAll
	public Optional<Gesuch> getNeustesGesuchFuerGesuch(@Nonnull Gesuch gesuch) {
		authorizer.checkReadAuthorization(gesuch);
		return getNeustesGesuchForDossierAndGesuchsperiode(gesuch.getGesuchsperiode(), gesuch.getDossier(), true);
	}

	@Override
	@PermitAll
	public boolean isNeustesGesuch(@Nonnull Gesuch gesuch) {
		final Optional<Gesuch> neustesGesuchFuerGesuch =
			getNeustesGesuchForDossierAndGesuchsperiode(gesuch.getGesuchsperiode(), gesuch.getDossier(), false);
		return neustesGesuchFuerGesuch.isPresent() && Objects.equals(
			neustesGesuchFuerGesuch.get().getId(),
			gesuch.getId());
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<String> getIdOfNeuestesGesuchForDossierAndGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Dossier dossier) {
		// Da wir nur die ID zurueckgeben, koennen wir den AuthCheck weglassen
		Optional<Gesuch> gesuchOptional = getNeustesGesuchForDossierAndGesuchsperiode(gesuchsperiode, dossier, false);
		return gesuchOptional.map(AbstractEntity::getId);
	}

	/**
	 * Da es eine private Methode ist, ist es sicher, als Parameter zu fragen, ob man nach ReadAuthorization pruefen
	 * muss.
	 * Das Interface sollte aber diese Moeglichkeit nur versteckt durch bestimmte Methoden anbieten.
	 */
	@Nonnull
	private Optional<Gesuch> getNeustesGesuchForDossierAndGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Dossier dossier,
		boolean checkReadAuthorization) {

		authorizer.checkReadAuthorizationDossier(dossier);
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);
		Predicate predicateDossier = cb.equal(root.get(Gesuch_.dossier), dossier);

		query.where(predicateGesuchsperiode, predicateDossier);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));

		List<Gesuch> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}

		Gesuch gesuch = criteriaResults.get(0);
		if (checkReadAuthorization) {
			authorizer.checkReadAuthorization(gesuch);
		}
		return Optional.of(gesuch);
	}

	/**
	 * Sucht
	 */
	@Override
	@Nonnull
	public Optional<String> getIdOfNeuestesGesuchForDossier(@Nonnull Dossier dossier) {
		authorizer.checkReadAuthorizationDossier(dossier);
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		Predicate predicateFall = cb.equal(root.get(Gesuch_.dossier), dossier);

		query.select(root);
		query.where(predicateFall);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));

		List<Gesuch> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}

		// look for the first gesuch which the user can read
		for (Gesuch gesuch : criteriaResults) {
			try {
				authorizer.checkReadAuthorization(gesuch);
				return Optional.of(gesuch.getId());

			} catch (EJBAccessException e) {
				// nop
			}
		}

		return Optional.empty();
	}

	@Override
	@Nonnull
	public Optional<Gesuch> getNeustesGesuchFuerFallnumerForSchulamtInterface(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Long fallnummer) {

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);
		Predicate predicateFallNummer = cb.equal(
			root.get(Gesuch_.dossier).get(Dossier_.fall).get(Fall_.fallNummer),
			fallnummer); // TODO KIBON es sollte pro Dossier sein <- existiert schulamtInterface in Kibon? wie???
		// zuerst dies klaeren
		Predicate predicateFinSit = root.get(Gesuch_.finSitStatus).isNotNull();

		query.where(predicateGesuchsperiode, predicateFallNummer, predicateFinSit);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		List<Gesuch> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}
		Gesuch gesuch = criteriaResults.get(0);
		return Optional.of(gesuch);
	}

	@Nonnull
	private Optional<Gesuch> getGesuchFuerErneuerungsantrag(@Nonnull Dossier dossier) {
		authorizer.checkReadAuthorizationDossier(dossier);
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		Predicate predicateDossier = cb.equal(root.get(Gesuch_.dossier), dossier);

		query.where(predicateDossier);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));

		List<Gesuch> criteriaResults = persistence.getCriteriaResults(query, 1);
		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}

		Gesuch gesuch = criteriaResults.get(0);
		return Optional.of(gesuch);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed(SUPER_ADMIN)
	public int warnGesuchNichtFreigegeben() {

		Integer anzahlTageBisWarnungFreigabe =
			applicationPropertyService.findApplicationPropertyAsInteger(ApplicationPropertyKey.ANZAHL_TAGE_BIS_WARNUNG_FREIGABE);
		Integer anzahlTageBisLoeschungNachWarnungFreigabe =
			applicationPropertyService.findApplicationPropertyAsInteger(ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE);
		if (anzahlTageBisWarnungFreigabe == null || anzahlTageBisLoeschungNachWarnungFreigabe == null) {
			throw new EbeguRuntimeException(
				"warnGesuchNichtFreigegeben",
				ApplicationPropertyKey.ANZAHL_TAGE_BIS_WARNUNG_FREIGABE.name() + " or " +
					ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE.name() + " not defined");
		}

		// Stichtag ist EndeTag -> Plus 1 Tag und dann less statt lessOrEqual
		LocalDateTime stichtag = LocalDate.now().minusDays(anzahlTageBisWarnungFreigabe).atStartOfDay().plusDays(1);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		// Status in Bearbeitung GS
		Predicate predicateStatus = cb.equal(root.get(Gesuch_.status), AntragStatus.IN_BEARBEITUNG_GS);
		// Irgendwann am Stichtag erstellt:
		Predicate predicateDatum = cb.lessThan(root.get(AbstractEntity_.timestampErstellt), stichtag);
		// Noch nicht gewarnt
		Predicate predicateNochNichtGewarnt = cb.isNull(root.get(Gesuch_.datumGewarntNichtFreigegeben));

		query.where(predicateStatus, predicateDatum, predicateNochNichtGewarnt);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		List<Gesuch> gesucheNichtAbgeschlossenSeit = persistence.getCriteriaResults(query);

		int anzahl = gesucheNichtAbgeschlossenSeit.size();
		for (Gesuch gesuch : gesucheNichtAbgeschlossenSeit) {
			try {
				mailService.sendWarnungGesuchNichtFreigegeben(gesuch, anzahlTageBisLoeschungNachWarnungFreigabe);
				gesuch.setDatumGewarntNichtFreigegeben(LocalDate.now());
				updateGesuch(gesuch, false, null);
			} catch (MailException e) {
				logExceptionAccordingToEnvironment(e,
					"Mail WarnungGesuchNichtFreigegeben konnte nicht verschickt werden fuer Gesuch",
					gesuch.getId());
				anzahl--;
			}
		}
		return anzahl;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed(SUPER_ADMIN)
	public int warnFreigabequittungFehlt() {

		Integer anzahlTageBisWarnungQuittung =
			applicationPropertyService.findApplicationPropertyAsInteger(ApplicationPropertyKey.ANZAHL_TAGE_BIS_WARNUNG_QUITTUNG);
		Integer anzahlTageBisLoeschungNachWarnungFreigabe =
			applicationPropertyService.findApplicationPropertyAsInteger(ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG);
		if (anzahlTageBisWarnungQuittung == null) {
			throw new EbeguRuntimeException(
				"warnFreigabequittungFehlt",
				ApplicationPropertyKey.ANZAHL_TAGE_BIS_WARNUNG_QUITTUNG.name() + " not defined");
		}
		if (anzahlTageBisLoeschungNachWarnungFreigabe == null) {
			throw new EbeguRuntimeException(
				"warnFreigabequittungFehlt",
				ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG.name() + " not defined");
		}

		LocalDate stichtag = LocalDate.now().minusDays(anzahlTageBisWarnungQuittung);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		Predicate predicateStatus = cb.equal(root.get(Gesuch_.status), AntragStatus.FREIGABEQUITTUNG);
		Predicate predicateDatum = cb.lessThanOrEqualTo(root.get(Gesuch_.freigabeDatum), stichtag);
		// Noch nicht gewarnt
		Predicate predicateNochNichtGewarnt = cb.isNull(root.get(Gesuch_.datumGewarntFehlendeQuittung));

		query.where(predicateStatus, predicateDatum, predicateNochNichtGewarnt);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		List<Gesuch> gesucheNichtAbgeschlossenSeit = persistence.getCriteriaResults(query);

		int anzahl = gesucheNichtAbgeschlossenSeit.size();
		for (Gesuch gesuch : gesucheNichtAbgeschlossenSeit) {
			try {
				mailService.sendWarnungFreigabequittungFehlt(gesuch, anzahlTageBisLoeschungNachWarnungFreigabe);
				gesuch.setDatumGewarntFehlendeQuittung(LocalDate.now());
				updateGesuch(gesuch, false, null);
			} catch (MailException e) {
				logExceptionAccordingToEnvironment(e,
					"Mail WarnungFreigabequittungFehlt konnte nicht verschickt werden fuer Gesuch",
					gesuch.getId());
				anzahl--;
			}
		}
		return anzahl;
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@RolesAllowed(SUPER_ADMIN)
	public int deleteGesucheOhneFreigabeOderQuittung() {

		List<Gesuch> criteriaResults = getGesuchesOhneFreigabeOderQuittung();
		int anzahl = criteriaResults.size();
		List<Betreuung> betreuungen = new ArrayList<>();
		for (Gesuch gesuch : criteriaResults) {
			try {
				mailService.sendInfoGesuchGeloescht(gesuch);
				betreuungen.addAll(gesuch.extractAllBetreuungen());
				GesuchDeletionCause typ;
				if (gesuch.getStatus() == AntragStatus.IN_BEARBEITUNG_GS) {
					typ = GesuchDeletionCause.BATCHJOB_NICHT_FREIGEGEBEN;
				} else {
					typ = GesuchDeletionCause.BATCHJOB_KEINE_QUITTUNG;
				}
				removeGesuch(gesuch.getId(), typ);
			} catch (MailException e) {
				logExceptionAccordingToEnvironment(e,
					"Mail InfoGesuchGeloescht konnte nicht verschickt werden fuer Gesuch",
					gesuch.getId());
				anzahl--;
			}
		}
		mailService.sendInfoBetreuungGeloescht(betreuungen);
		return anzahl;
	}

	@Override
	@RolesAllowed(SUPER_ADMIN)
	public List<Gesuch> getGesuchesOhneFreigabeOderQuittung() {
		Integer anzahlTageBisLoeschungNachWarnungFreigabe =
			applicationPropertyService.findApplicationPropertyAsInteger(ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE);
		Integer anzahlTageBisLoeschungNachWarnungQuittung =
			applicationPropertyService.findApplicationPropertyAsInteger(ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG);
		if (anzahlTageBisLoeschungNachWarnungFreigabe == null || anzahlTageBisLoeschungNachWarnungQuittung == null) {
			throw new EbeguRuntimeException(
				"warnGesuchNichtFreigegeben",
				ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE.name() + " or " +
					ApplicationPropertyKey.ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG.name() + " not defined");
		}

		// Stichtag ist EndeTag -> Plus 1 Tag und dann less statt lessOrEqual
		LocalDate stichtagFehlendeFreigabe = LocalDate.now()
			.minusDays(anzahlTageBisLoeschungNachWarnungFreigabe).plusDays(1);
		LocalDate stichtagFehlendeQuittung = LocalDate.now()
			.minusDays(anzahlTageBisLoeschungNachWarnungQuittung);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		// Entweder IN_BEARBEITUNG_GS und vor stichtagFehlendeFreigabe erstellt
		Predicate predicateStatusNichtFreigegeben = cb.equal(root.get(Gesuch_.status), AntragStatus.IN_BEARBEITUNG_GS);
		Predicate predicateGewarntNichtFreigegeben = cb.isNotNull(root.get(Gesuch_.datumGewarntNichtFreigegeben));
		Predicate predicateDatumNichtFreigegeben =
			cb.lessThan(root.get(Gesuch_.datumGewarntNichtFreigegeben), stichtagFehlendeFreigabe);
		Predicate predicateNichtFreigegeben =
			cb.and(predicateStatusNichtFreigegeben, predicateDatumNichtFreigegeben, predicateGewarntNichtFreigegeben);

		// Oder FREIGABEQUITTUNG und vor stichtagFehlendeQuittung freigegeben
		Predicate predicateStatusFehlendeQuittung = cb.equal(root.get(Gesuch_.status), AntragStatus.FREIGABEQUITTUNG);
		Predicate predicateGewarntFehlendeQuittung = cb.isNotNull(root.get(Gesuch_.datumGewarntFehlendeQuittung));
		Predicate predicateDatumFehlendeQuittung =
			cb.lessThanOrEqualTo(root.get(Gesuch_.datumGewarntFehlendeQuittung), stichtagFehlendeQuittung);

		Predicate predicateFehlendeQuittung =
			cb.and(predicateStatusFehlendeQuittung, predicateDatumFehlendeQuittung, predicateGewarntFehlendeQuittung);

		Predicate predicateFehlendeFreigabeOrQuittung = cb.or(predicateNichtFreigegeben, predicateFehlendeQuittung);

		query.where(predicateFehlendeFreigabeOrQuittung);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));

		return persistence.getCriteriaResults(query);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	public boolean canGesuchsperiodeBeClosed(@Nonnull Gesuchsperiode gesuchsperiode) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		// Status verfuegt
		Predicate predicateStatus = root.get(Gesuch_.status).in(AntragStatus.ERLEDIGTE_PENDENZ).not();
		// Gesuchsperiode
		final Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);

		query.where(predicateStatus, predicateGesuchsperiode);
		query.select(root);
		List<Gesuch> criteriaResults = persistence.getCriteriaResults(query);
		return criteriaResults.isEmpty();
	}

	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TS, SUPER_ADMIN })
	public void removeOnlineMutation(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		logDeletingOfGesuchstellerAntrag(dossier, gesuchsperiode);
		final Gesuch onlineMutation = findOnlineMutation(dossier, gesuchsperiode);
		moveBetreuungmitteilungenToPreviousAntrag(onlineMutation);
		List<Betreuung> betreuungen = new ArrayList<>(onlineMutation.extractAllBetreuungen());
		superAdminService.removeGesuch(onlineMutation.getId());

		mailService.sendInfoBetreuungGeloescht(betreuungen);
	}

	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN })
	public Gesuch findOnlineMutation(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		List<Gesuch> criteriaResults = findExistingOpenMutationen(dossier, gesuchsperiode);
		if (criteriaResults.size() > 1) {
			// It should be impossible that there are more than one open Mutation
			throw new EbeguRuntimeException("findOnlineMutation", ErrorCodeEnum.ERROR_TOO_MANY_RESULTS);
		}
		if (criteriaResults.size() <= 0) {
			throw new EbeguEntityNotFoundException("findOnlineMutation", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);
		}
		return criteriaResults.get(0);
	}

	/**
	 * Takes all Betreuungsmitteilungen of the given Gesuch and links them to the previous corresponding Betreuung
	 * (vorgaengerId),
	 * so that the mitteilungen don't get lost.
	 */
	private void moveBetreuungmitteilungenToPreviousAntrag(@Nonnull Gesuch onlineMutation) {
		if (onlineMutation.hasVorgaenger()) {
			for (Betreuung betreuung : onlineMutation.extractAllBetreuungen()) {
				if (betreuung.hasVorgaenger()) {
					@SuppressWarnings("ConstantConditions") // wird in hasVorgaenger() geprueft
					final Optional<Betreuung> optVorgaengerBetreuung =
						betreuungService.findBetreuung(betreuung.getVorgaengerId());
					final Betreuung vorgaengerBetreuung = optVorgaengerBetreuung
						.orElseThrow(() -> new EbeguEntityNotFoundException(
							"moveBetreuungmitteilungenToPreviousAntrag",
							ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
							betreuung.getVorgaengerId()));

					final Collection<Betreuungsmitteilung> mitteilungen =
						mitteilungService.findAllBetreuungsmitteilungenForBetreuung(betreuung);
					mitteilungen.forEach(mitteilung -> mitteilung.setBetreuung(vorgaengerBetreuung)); // should be
					// saved automatically
				}
			}
		} else {
			throw new EbeguEntityNotFoundException(
				"moveBetreuungmitteilungenToPreviousAntrag",
				ErrorCodeEnum.ERROR_VORGAENGER_MISSING,
				onlineMutation.getId());
		}
	}

	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TS, SUPER_ADMIN })
	public void removeOnlineFolgegesuch(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		logDeletingOfGesuchstellerAntrag(dossier, gesuchsperiode);
		Gesuch gesuch = findOnlineFolgegesuch(dossier, gesuchsperiode);
		List<Betreuung> betreuungen = new ArrayList<>(gesuch.extractAllBetreuungen());
		superAdminService.removeGesuch(gesuch.getId());

		mailService.sendInfoBetreuungGeloescht(betreuungen);
	}

	@Override
	public Gesuch findOnlineFolgegesuch(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		List<Gesuch> criteriaResults = findExistingFolgegesuch(dossier, gesuchsperiode);
		if (criteriaResults.size() > 1) {
			// It should be impossible that there are more than one open Folgegesuch for one period
			throw new EbeguRuntimeException("findOnlineFolgegesuch", ErrorCodeEnum.ERROR_TOO_MANY_RESULTS);
		}
		return criteriaResults.get(0);
	}

	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TS, SUPER_ADMIN })
	public void removePapiergesuch(@Nonnull Gesuch gesuch) {
		logDeletingOfAntrag(gesuch);
		// Antrag muss Papier sein, und darf noch nicht verfuegen/verfuegt sein
		if (gesuch.getEingangsart().isOnlineGesuch()) {
			throw new EbeguRuntimeException("removeAntrag", ErrorCodeEnum.ERROR_DELETION_NOT_ALLOWED_FOR_JA);
		}
		if (gesuch.getStatus().isAnyStatusOfVerfuegtOrVefuegen()) {
			throw new EbeguRuntimeException(
				"removeAntrag",
				ErrorCodeEnum.ERROR_DELETION_ANTRAG_NOT_ALLOWED,
				gesuch.getStatus());
		}
		List<Betreuung> betreuungen = new ArrayList<>(gesuch.extractAllBetreuungen());
		// Bei Erstgesuch wird auch der Fall mitgelöscht
		if (gesuch.getTyp() == AntragTyp.ERSTGESUCH) {
			superAdminService.removeFall(gesuch.getFall());
		} else {
			superAdminService.removeGesuch(gesuch.getId());
		}

		mailService.sendInfoBetreuungGeloescht(betreuungen);
	}

	@Override
	@RolesAllowed(GESUCHSTELLER)
	public void removeGesuchstellerAntrag(@Nonnull Gesuch gesuch) {
		logDeletingOfAntrag(gesuch);
		// Antrag muss Online sein, und darf noch nicht freigegeben sein
		if (gesuch.getEingangsart().isPapierGesuch()) {
			throw new EbeguRuntimeException(
				"removeGesuchstellerAntrag",
				ErrorCodeEnum.ERROR_DELETION_NOT_ALLOWED_FOR_GS);
		}
		if (gesuch.getStatus() != AntragStatus.IN_BEARBEITUNG_GS) {
			throw new EbeguRuntimeException(
				"removeGesuchstellerAntrag",
				ErrorCodeEnum.ERROR_DELETION_ANTRAG_NOT_ALLOWED,
				gesuch.getStatus());
		}
		List<Betreuung> betreuungen = new ArrayList<>(gesuch.extractAllBetreuungen());
		superAdminService.removeGesuch(gesuch.getId());

		mailService.sendInfoBetreuungGeloescht(betreuungen);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public Gesuch closeWithoutAngebot(@Nonnull Gesuch gesuch) {
		if (gesuch.getStatus() != AntragStatus.GEPRUEFT) {
			throw new EbeguRuntimeException("closeWithoutAngebot", ErrorCodeEnum.ERROR_ONLY_IN_GEPRUEFT_ALLOWED);
		}
		if (!gesuch.extractAllBetreuungen().isEmpty()) {
			throw new EbeguRuntimeException("closeWithoutAngebot", ErrorCodeEnum.ERROR_ONLY_IF_NO_BETERUUNG);
		}
		gesuch.setStatus(AntragStatus.KEIN_ANGEBOT);
		postGesuchVerfuegen(gesuch);
		wizardStepService.setWizardStepOkay(gesuch.getId(), WizardStepName.VERFUEGEN);
		Gesuch persistedGesuch = updateGesuch(gesuch, true, null);
		// Das Dokument der Finanziellen Situation erstellen
		createFinSitDokument(persistedGesuch, "closeWithoutAngebot");
		return persistedGesuch;
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public Gesuch verfuegenStarten(@Nonnull Gesuch gesuch) {
		if (gesuch.getStatus() != AntragStatus.GEPRUEFT) {
			throw new EbeguRuntimeException("verfuegenStarten", ErrorCodeEnum.ERROR_ONLY_IN_GEPRUEFT_ALLOWED);
		}
		// Zum jetzigen Zeitpunkt muss das Gesuch zwingend in einem kompletten / gueltigen Zustand sein
		validateGesuchComplete(gesuch);

		if (gesuch.hasOnlyBetreuungenOfSchulamt()) {
			wizardStepService.setWizardStepOkay(gesuch.getId(), WizardStepName.VERFUEGEN);
			gesuch.setStatus(AntragStatus.NUR_SCHULAMT);
			postGesuchVerfuegen(gesuch);
		} else {
			gesuch.setStatus(AntragStatus.VERFUEGEN);
		}

		// In Fall von NUR_SCHULAMT-Angeboten werden diese nicht verfügt, d.h. ab "Verfügen starten"
		// sind diese Betreuungen die letzt gültigen
		final List<Betreuung> betreuungen = gesuch.extractAllBetreuungen();
		for (Betreuung betreuung : betreuungen) {
			if (betreuung.getInstitutionStammdaten().getBetreuungsangebotTyp().isSchulamt()) {
				betreuung.setGueltig(true);
				if (betreuung.getVorgaengerId() != null) {
					Optional<Betreuung> vorgaengerBetreuungOptional =
						betreuungService.findBetreuung(betreuung.getVorgaengerId(), false);
					vorgaengerBetreuungOptional.ifPresent(vorgaenger -> vorgaenger.setGueltig(false));
				}
			}
		}

		Gesuch persistedGesuch = superAdminService.updateGesuch(gesuch, true, principalBean.getBenutzer());

		// Das Dokument der Finanziellen Situation erstellen
		createFinSitDokument(persistedGesuch, "verfuegenStarten");

		return persistedGesuch;
	}

	private void validateGesuchComplete(@Nonnull Gesuch gesuch) {
		// Gesamt-Validierung durchführen
		Validator validator = Validation.byDefaultProvider().configure().buildValidatorFactory().getValidator();
		Set<ConstraintViolation<Gesuch>> constraintViolations =
			validator.validate(gesuch, AntragCompleteValidationGroup.class);
		if (!constraintViolations.isEmpty()) {
			throw new ConstraintViolationException(constraintViolations);
		}
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public void postGesuchVerfuegen(@Nonnull Gesuch gesuch) {
		Optional<Gesuch> neustesVerfuegtesGesuchFuerGesuch =
			getNeustesVerfuegtesGesuchFuerGesuch(gesuch.getGesuchsperiode(), gesuch.getDossier(), false);
		if (AntragStatus.FIRST_STATUS_OF_VERFUEGT.contains(gesuch.getStatus())
			&& gesuch.getTimestampVerfuegt() == null) {
			// Status ist neuerdings verfuegt, aber das Datum noch nicht gesetzt -> dies war der Statuswechsel
			gesuch.setTimestampVerfuegt(LocalDateTime.now());
			if (neustesVerfuegtesGesuchFuerGesuch.isPresent() && !neustesVerfuegtesGesuchFuerGesuch.get()
				.getId()
				.equals(gesuch.getId())) {
				setGesuchUngueltig(neustesVerfuegtesGesuchFuerGesuch.get());
			}

			// neues Gesuch erst nachdem das andere auf ungültig gesetzt wurde setzen wegen unique key
			gesuch.setGueltig(true);
		}
	}

	/**
	 * Setzt das Gesuch auf ungueltig, falls es gueltig ist
	 */
	private void setGesuchUngueltig(@Nonnull Gesuch gesuch) {
		if (gesuch.isGueltig()) {
			gesuch.setGueltig(null);
			updateGesuch(gesuch, false, null, false);
			// Sicherstellen, dass das Gesuch welches nicht mehr gültig ist zuerst gespeichert wird da sonst unique
			// key Probleme macht!
			persistence.getEntityManager().flush();
		}
	}

	@Override
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED) // wir brauchen keine transaktion fuer das mailen
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public void sendMailsToAllGesuchstellerOfLastGesuchsperiode(
		@Nonnull Gesuchsperiode lastGesuchsperiode,
		@Nonnull Gesuchsperiode nextGesuchsperiode) {
		Collection<Dossier> allDossiers = dossierService.getAllDossiers(true);
		List<Gesuch> antraegeOfLastYear = allDossiers.stream()
			.map(dossier -> getNeuestesGesuchForDossierAndPeriod(dossier, lastGesuchsperiode))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		mailService.sendInfoFreischaltungGesuchsperiode(nextGesuchsperiode, antraegeOfLastYear);
	}

	@Override
	public Gesuch updateBetreuungenStatus(@NotNull Gesuch gesuch) {
		gesuch.setGesuchBetreuungenStatus(GesuchBetreuungenStatus.ALLE_BESTAETIGT);
		for (Betreuung betreuung : gesuch.extractAllBetreuungen()) {
			if (Betreuungsstatus.ABGEWIESEN == betreuung.getBetreuungsstatus()) {
				gesuch.setGesuchBetreuungenStatus(GesuchBetreuungenStatus.ABGEWIESEN);
				break;
			}
			if (Betreuungsstatus.WARTEN == betreuung.getBetreuungsstatus() ||
				Betreuungsstatus.UNBEKANNTE_INSTITUTION == betreuung.getBetreuungsstatus()) {
				gesuch.setGesuchBetreuungenStatus(GesuchBetreuungenStatus.WARTEN);
			}
		}
		return persistence.merge(gesuch);
	}

	@Override
	public void gesuchVerfuegen(@NotNull Gesuch gesuch) {
		if (gesuch.getStatus() != AntragStatus.VERFUEGT) {
			final WizardStep verfuegenStep =
				wizardStepService.findWizardStepFromGesuch(gesuch.getId(), WizardStepName.VERFUEGEN);
			if (verfuegenStep.getWizardStepStatus() == WizardStepStatus.OK) {
				final List<Betreuung> allBetreuungen = gesuch.extractAllBetreuungen();
				if (allBetreuungen.stream().allMatch(betreuung -> betreuung.getBetreuungsstatus().isGeschlossen())) {
					wizardStepService.gesuchVerfuegen(verfuegenStep);
				}
			}
		}
	}

	@Nonnull
	private Optional<Gesuch> getNeuestesGesuchForDossierAndPeriod(
		@Nonnull Dossier dossier,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		authorizer.checkReadAuthorizationDossier(dossier);

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);
		Predicate dossierPredicate = cb.equal(root.get(Gesuch_.dossier), dossier);
		Predicate gesuchsperiodePredicate = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiode);

		query.where(dossierPredicate, gesuchsperiodePredicate);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));
		List<Gesuch> criteriaResults = persistence.getCriteriaResults(query, 1);

		if (criteriaResults.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(criteriaResults.get(0));
	}

	private void logDeletingOfGesuchstellerAntrag(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode) {
		LOG.info("****************************************************");
		LOG.info("Online Gesuch wird gelöscht:");
		LOG.info("Benutzer: {}", principalBean.getBenutzer().getUsername());
		LOG.info("Fall: {}", dossier.getFall().getFallNummer());
		LOG.info("Gemeinde: {}", dossier.getGemeinde().getGemeindeNummer());
		LOG.info("Gesuchsperiode: {}", gesuchsperiode.getGesuchsperiodeString());
		LOG.info("****************************************************");
	}

	private void logDeletingOfAntrag(@Nonnull Gesuch gesuch) {
		LOG.info("****************************************************");
		LOG.info("Gesuch wird gelöscht:");
		LOG.info("Benutzer: {}", principalBean.getBenutzer().getUsername());
		LOG.info("Fall: {}", gesuch.getFall().getFallNummer());
		LOG.info("Gesuchsperiode: {}", gesuch.getGesuchsperiode().getGesuchsperiodeString());
		LOG.info("Gesuch-Id: {}", gesuch.getId());
		LOG.info("****************************************************");
	}

	@Override
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public int changeFinSitStatus(@Nonnull String antragId, @Nonnull FinSitStatus finSitStatus) {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaUpdate<Gesuch> update = cb.createCriteriaUpdate(Gesuch.class);
		Root<Gesuch> root = update.from(Gesuch.class);
		update.set(Gesuch_.finSitStatus, finSitStatus);
		if (finSitStatus == FinSitStatus.ABGELEHNT) {
			update.set(Gesuch_.hasFSDokument, false); // immer auf false wenn ABGELEHNT
		}

		Predicate predGesuch = cb.equal(root.get(AbstractEntity_.id), antragId);
		update.where(predGesuch);

		return persistence.getEntityManager().createQuery(update).executeUpdate();
	}

	@Override
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Gesuch sendGesuchToSTV(@Nonnull Gesuch gesuch, @Nullable String bemerkungen) {
		if (AntragStatus.VERFUEGT != gesuch.getStatus() && AntragStatus.NUR_SCHULAMT != gesuch.getStatus()) {
			// Wir vergewissern uns dass das Gesuch im Status VERFUEGT ist, da sonst kann es nicht zum STV geschickt
			// werden
			throw new EbeguRuntimeException(
				"sendGesuchToSTV",
				ErrorCodeEnum.ERROR_ONLY_VERFUEGT_OR_SCHULAMT_ALLOWED,
				"Status ist: " + gesuch.getStatus());
		}
		gesuch.setStatus(AntragStatus.PRUEFUNG_STV);
		gesuch.setEingangsdatumSTV(LocalDate.now());
		if (StringUtils.isNotEmpty(bemerkungen)) {
			gesuch.setBemerkungenSTV(bemerkungen);
		}
		return updateGesuch(gesuch, true, null);
	}

	@Override
	public Gesuch gesuchBySTVFreigeben(@Nonnull Gesuch gesuch) {
		if (AntragStatus.IN_BEARBEITUNG_STV != gesuch.getStatus()) {
			// Wir vergewissern uns dass das Gesuch im Status IN_BEARBEITUNG_STV ist, da sonst kann es nicht fuer das
			// JA freigegeben werden
			throw new EbeguRuntimeException(
				"gesuchBySTVFreigeben",
				ErrorCodeEnum.ERROR_ONLY_IN_BEARBEITUNG_STV_ALLOWED,
				"Status ist: " + gesuch.getStatus());
		}

		gesuch.setStatus(AntragStatus.GEPRUEFT_STV);
		gesuch.setGeprueftSTV(true);

		return updateGesuch(gesuch, true, null);
	}

	@Override
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Gesuch stvPruefungAbschliessen(@Nonnull Gesuch gesuch) {
		final AntragStatusHistory lastStatusChange =
			antragStatusHistoryService.findLastStatusChangeBeforePruefungSTV(gesuch);
		gesuch.setStatus(lastStatusChange.getStatus());
		return updateGesuch(gesuch, true, null);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, ADMIN_BG, ADMIN_TS })
	public Massenversand createMassenversand(@Nonnull Massenversand massenversand) {
		return persistence.persist(massenversand);
	}

	@Override
	@PermitAll
	public List<String> getMassenversandTexteForGesuch(@Nonnull String gesuchId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Massenversand> query = cb.createQuery(Massenversand.class);

		Root<Massenversand> root = query.from(Massenversand.class);
		ListJoin<Massenversand, Gesuch> gesuche = root.join(Massenversand_.gesuche);

		Predicate predicate = cb.equal(gesuche.get(AbstractEntity_.id), gesuchId);
		query.where(predicate);
		query.select(root);
		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));

		List<Massenversand> massenversaende = persistence.getCriteriaResults(query);
		List<String> result = massenversaende.stream()
			.map(Massenversand::getDescription)
			.collect(Collectors.toList());

		return result;
	}

	@Override
	@Nonnull
	public List<Gesuch> getGepruefteFreigegebeneGesucheForGesuchsperiode(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull String gesuchsperiodeId
	) {

		final Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId)
			.orElseThrow(() ->
				new EbeguEntityNotFoundException("getGepruefteFreigegebeneGesucheForGesuchsperiode",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchsperiodeId)
			);

		// We first look for all Gesuche that belongs to the gesuchsperiode and were geprueft/nurschulamt/freigegeben between
		// the given dates.
		final List<Tuple> allTuples =
			getGepruefteFreigegebeneGesucheForGesuchsperiodeTuples(datumVon, datumBis, gesuchsperiode);

		List<Gesuch> gesuche = new ArrayList<>();
		allTuples.forEach(tuple -> {
			final String dossierIdValue = String.valueOf(tuple.get(0));
			final String gesuchsperiodeIdValue = String.valueOf(tuple.get(1));

			Optional<Gesuch> gesuch =
				getNeustesGeprueftesFreigegebensGesuchFuerDossierPeriode(gesuchsperiodeIdValue, dossierIdValue);
			gesuch.ifPresent(gesuche::add);
		});

		return gesuche;
	}

	private List<Tuple> getGepruefteFreigegebeneGesucheForGesuchsperiodeTuples(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis,
		@Nonnull Gesuchsperiode gesuchsperiode
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createTupleQuery();
		Root<Gesuch> root = query.from(Gesuch.class);

		Join<Gesuch, AntragStatusHistory> antragStatusHistoryJoin = root.join(Gesuch_.antragStatusHistories, JoinType.LEFT);

		// Prepare TypedParameters
		ParameterExpression<Gesuchsperiode> gesuchsperiodeIdParam = cb.parameter(Gesuchsperiode.class, "gesuchsperiode");
		ParameterExpression<LocalDateTime> datumVonParam = cb.parameter(LocalDateTime.class, "datumVon");
		ParameterExpression<LocalDateTime> datumBisParam = cb.parameter(LocalDateTime.class, "datumBis");
		ParameterExpression<AntragStatus> geprueftParam = cb.parameter(AntragStatus.class, "geprueft");
		ParameterExpression<AntragStatus> freigegebenParam = cb.parameter(AntragStatus.class, "freigegeben");
		ParameterExpression<AntragStatus> nurSchulamtParam = cb.parameter(AntragStatus.class, "nurschulamt");
		ParameterExpression<Eingangsart> papierParam = cb.parameter(Eingangsart.class, "papier");
		ParameterExpression<Eingangsart> onlineParam = cb.parameter(Eingangsart.class, "online");

		// Predicates
		Predicate predicateStatusTransition = getStatusTransitionPredicate(cb, root, antragStatusHistoryJoin, datumVonParam, datumBisParam,
			geprueftParam, freigegebenParam, nurSchulamtParam, papierParam, onlineParam);
		Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode), gesuchsperiodeIdParam);
		// An Erstgesuch is not MUTATION (i.e. ERSTGESUCH or ERNEUERUNGSGESUCH)
		Predicate predicateErstgesuch = cb.equal(root.get(Gesuch_.typ), AntragTyp.MUTATION).not();

		query.where(predicateGesuchsperiode, predicateStatusTransition, predicateErstgesuch);

		query.groupBy(
			root.get(Gesuch_.dossier).get(AbstractEntity_.id),
			root.get(Gesuch_.gesuchsperiode).get(AbstractEntity_.id)
		);
		query.multiselect(
			root.get(Gesuch_.dossier).get(AbstractEntity_.id),
			root.get(Gesuch_.gesuchsperiode).get(AbstractEntity_.id),
			cb.max(root.get(Gesuch_.laufnummer))
		);

		TypedQuery<Tuple> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(geprueftParam, AntragStatus.GEPRUEFT);
		typedQuery.setParameter(freigegebenParam, AntragStatus.FREIGEGEBEN);
		typedQuery.setParameter(nurSchulamtParam, AntragStatus.NUR_SCHULAMT);
		typedQuery.setParameter(papierParam, Eingangsart.PAPIER);
		typedQuery.setParameter(onlineParam, Eingangsart.ONLINE);
		typedQuery.setParameter(gesuchsperiodeIdParam, gesuchsperiode);
		typedQuery.setParameter(datumVonParam, datumVon.atStartOfDay());
		typedQuery.setParameter(datumBisParam, datumBis.atStartOfDay().plusDays(1));

		return typedQuery.getResultList();
	}

	/**
	 * Returns the newest Gesuch for the given dossier and period that has been at least freigegeben (for Onlinegesuche)
	 * or at least Geprueft (for Papiergesuche)
	 */
	@Nonnull
	public Optional<Gesuch> getNeustesGeprueftesFreigegebensGesuchFuerDossierPeriode(
		@Nonnull String gesuchsperiodeId,
		@Nonnull String dossierId
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Gesuch> query = cb.createQuery(Gesuch.class);

		Root<Gesuch> root = query.from(Gesuch.class);

		ParameterExpression<String> dossierParam = cb.parameter(String.class, "dossierId");
		ParameterExpression<String> gesuchsperiodeParam = cb.parameter(String.class, "gesuchsperiodeId");
		ParameterExpression<Eingangsart> papierParam = cb.parameter(Eingangsart.class, "papier");
		ParameterExpression<Eingangsart> onlineParam = cb.parameter(Eingangsart.class, "online");
		//noinspection rawtypes
		ParameterExpression<Collection> geprueftParam = cb.parameter(Collection.class, "geprueft");
		//noinspection rawtypes
		ParameterExpression<Collection> freigegebenParam = cb.parameter(Collection.class, "freigegeben");

		Predicate predicateStatus = getStatusFromEingangsartPredicate(cb, root, papierParam, onlineParam, geprueftParam, freigegebenParam);
		Predicate predicateFall = cb.equal(root.get(Gesuch_.dossier).get(AbstractEntity_.id), dossierParam);
		Predicate predicateGesuchsperiode = cb.equal(root.get(Gesuch_.gesuchsperiode).get(AbstractEntity_.id), gesuchsperiodeParam);

		query.where(predicateStatus, predicateGesuchsperiode, predicateFall);
		query.select(root);

		query.orderBy(cb.desc(root.get(AbstractEntity_.timestampErstellt)));

		TypedQuery<Gesuch> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(dossierParam, dossierId);
		typedQuery.setParameter(gesuchsperiodeParam, gesuchsperiodeId);
		typedQuery.setParameter(papierParam, Eingangsart.PAPIER);
		typedQuery.setParameter(onlineParam, Eingangsart.ONLINE);
		typedQuery.setParameter(geprueftParam, AntragStatus.getAllGepruefteStatus());
		typedQuery.setParameter(freigegebenParam, AntragStatus.getAllFreigegebeneStatus());

		final List<Gesuch> gesuche = typedQuery.getResultList();
		if (gesuche.isEmpty()) {
			return Optional.empty();
		}
		Gesuch gesuch = gesuche.get(0);
		authorizer.checkReadAuthorization(gesuch);
		return Optional.of(gesuch);
	}

	private Predicate getStatusFromEingangsartPredicate(
		@Nonnull CriteriaBuilder cb,
		@Nonnull Root<Gesuch> root,
		@Nonnull ParameterExpression<Eingangsart> papierParam,
		@Nonnull ParameterExpression<Eingangsart> onlineParam,
		@SuppressWarnings("rawtypes") @Nonnull ParameterExpression<Collection> geprueftParam,
		@SuppressWarnings("rawtypes") @Nonnull ParameterExpression<Collection> freigegebenParam
	) {
		final Predicate predicateGeprueft = root.get(Gesuch_.status).in(geprueftParam);
		final Predicate predicateFreigegeben = root.get(Gesuch_.status).in(freigegebenParam);

		final Predicate predicatePapier = cb.equal(root.get(Gesuch_.eingangsart), papierParam);
		final Predicate predicateOnline = cb.equal(root.get(Gesuch_.eingangsart), onlineParam);

		return cb.or(
			cb.and(predicateGeprueft, predicatePapier),
			cb.and(predicateFreigegeben, predicateOnline)
		);
	}

	/**
	 * Will create a Predicate to look for all Onlinegesuche that marked as FREIGEGEBEN between Von and Bis
	 * and all Papiergesuche that were marked as GEPRUEFT between Von and Bis
	 */
	private Predicate getStatusTransitionPredicate(
		@Nonnull CriteriaBuilder cb,
		@Nonnull Root<Gesuch> root,
		@Nonnull Join<Gesuch, AntragStatusHistory> antragStatusHistoryJoin,
		@Nonnull ParameterExpression<LocalDateTime> datumVonParam,
		@Nonnull ParameterExpression<LocalDateTime> datumBisParam,
		@Nonnull ParameterExpression<AntragStatus> geprueftParam,
		@Nonnull ParameterExpression<AntragStatus> freigegebenParam,
		@Nonnull ParameterExpression<AntragStatus> nurSchulamtParam,
		@Nonnull ParameterExpression<Eingangsart> papierParam,
		@Nonnull ParameterExpression<Eingangsart> onlineParam
	) {
		final Predicate predicateStatusSetBetweenVonAndBis = cb.between(
			antragStatusHistoryJoin.get(AntragStatusHistory_.timestampVon),
			datumVonParam,
			datumBisParam
		);
		final Predicate predicateGeprueft = cb.and(
			cb.or(
				cb.equal(antragStatusHistoryJoin.get(AntragStatusHistory_.status), geprueftParam),
				cb.equal(antragStatusHistoryJoin.get(AntragStatusHistory_.status), nurSchulamtParam)
			),
			predicateStatusSetBetweenVonAndBis
		);
		final Predicate predicateFreigegeben = cb.and(
			cb.equal(antragStatusHistoryJoin.get(AntragStatusHistory_.status), freigegebenParam),
			predicateStatusSetBetweenVonAndBis
		);

		final Predicate predicatePapier = cb.equal(root.get(Gesuch_.eingangsart), papierParam);
		final Predicate predicateOnline = cb.equal(root.get(Gesuch_.eingangsart), onlineParam);

		return cb.or(
			cb.and(predicateGeprueft, predicatePapier),
			cb.and(predicateFreigegeben, predicateOnline)
		);
	}

	@Override
	public boolean hasFolgegesuchForAmt(@Nonnull String gesuchId) {
		final Optional<Gesuch> optGesuch = findGesuch(gesuchId);
		final Gesuch gesuch = optGesuch.orElseThrow(()
			-> new EbeguEntityNotFoundException("hasFolgegesuchForAmt", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchId));


		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<Gesuch> root = query.from(Gesuch.class);

		query.select(root.get(AbstractEntity_.id));

		ParameterExpression<String> dossierIdParam = cb.parameter(String.class, "dossierId");
		ParameterExpression<LocalDate> gesuchsperiodeGueltigAbParam = cb.parameter(LocalDate.class, "gueltigAb");
		//noinspection rawtypes
		ParameterExpression<Collection> freigegebenParam = cb.parameter(Collection.class, "freigegeben");

		Predicate freigegebenPredicate = root.get(Gesuch_.status).in(freigegebenParam);
		Predicate fallPredicate = cb.equal(root.get(Gesuch_.dossier).get(AbstractEntity_.id), dossierIdParam);
		Predicate gesuchsperiodePredicate = cb.greaterThan(
			root.get(Gesuch_.gesuchsperiode).get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb),
			gesuchsperiodeGueltigAbParam);

		query.where(fallPredicate, gesuchsperiodePredicate, freigegebenPredicate);

		query.orderBy(cb.desc(root.get(Gesuch_.laufnummer)));

		TypedQuery<String> typedQuery = persistence.getEntityManager().createQuery(query);
		typedQuery.setParameter(dossierIdParam, gesuch.getDossier().getId());
		typedQuery.setParameter(gesuchsperiodeGueltigAbParam, gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb());
		typedQuery.setParameter(freigegebenParam, AntragStatus.getAllFreigegebeneStatus());

		final List<String> resultList = typedQuery.getResultList();
		return !resultList.isEmpty();
	}

	private void createFinSitDokument(Gesuch persistedGesuch, String methodname) {
		if (EbeguUtil.isFinanzielleSituationRequired(persistedGesuch)) {
			try {
				// Das Erstellen des FinSitDokumentes wirft eine Exception, wenn die FinSit nicht benötigt wird
				generatedDokumentService.getFinSitDokumentAccessTokenGeneratedDokument(persistedGesuch, true);
			} catch (MimeTypeParseException | MergeDocException e) {
				throw new EbeguRuntimeException(methodname, "FinSit-Dokument konnte nicht erstellt werden"
					+ persistedGesuch.getId(), e);
			}
		}
	}
}


