package ch.dvbern.ebegu.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang3.Validate;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_JA;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer familiensituation
 */
@Stateless
@Local(FamiliensituationService.class)
public class FamiliensituationServiceBean extends AbstractBaseService implements FamiliensituationService {

	@Inject
	private Persistence persistence;
	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;
	@Inject
	private GesuchstellerService gesuchstellerService;
	@Inject
	private WizardStepService wizardStepService;

	@Override
	@RolesAllowed({ ADMIN, SUPER_ADMIN, SACHBEARBEITER_JA, GESUCHSTELLER })
	public FamiliensituationContainer saveFamiliensituation(Gesuch gesuch,
		FamiliensituationContainer familiensituationContainer,
		Familiensituation loadedFamiliensituation) {
		Objects.requireNonNull(familiensituationContainer);
		Objects.requireNonNull(gesuch);

		// Falls noch nicht vorhanden, werden die GemeinsameSteuererklaerung fuer FS und EV auf false gesetzt
		Familiensituation newFamiliensituation = familiensituationContainer.extractFamiliensituation();
		Objects.requireNonNull(newFamiliensituation);

		if (gesuch.isMutation() && EbeguUtil.fromOneGSToTwoGS(familiensituationContainer)) {

			if (newFamiliensituation.getGemeinsameSteuererklaerung() == null) {
				newFamiliensituation.setGemeinsameSteuererklaerung(false);
			}
			if (gesuch.extractEinkommensverschlechterungInfo() != null) { //eigentlich darf es bei einer Mutation nie
				// null sein. Trotzdem zur Sicherheit...
				if (gesuch.extractEinkommensverschlechterungInfo().getGemeinsameSteuererklaerung_BjP1() == null) {
					gesuch.extractEinkommensverschlechterungInfo().setGemeinsameSteuererklaerung_BjP1(false);
				}
				if (gesuch.extractEinkommensverschlechterungInfo().getGemeinsameSteuererklaerung_BjP2() == null) {
					gesuch.extractEinkommensverschlechterungInfo().setGemeinsameSteuererklaerung_BjP2(false);
				}
				//noinspection ConstantConditions (ist mit extractEinkommensverschlechterungInfo().isPresent() sichergestellt)einkommensverschlechterungInfoService.updateEinkommensverschlechterungInfo(gesuch.getEinkommensverschlechterungInfoContainer());
			}
		} else {
			Familiensituation familiensituationErstgesuch = familiensituationContainer
				.getFamiliensituationErstgesuch();
			if (familiensituationErstgesuch != null &&
				(!familiensituationErstgesuch.hasSecondGesuchsteller() && !newFamiliensituation.hasSecondGesuchsteller
					())) {
				// if there is no GS2 the field gemeinsameSteuererklaerung must be set to null
				newFamiliensituation.setGemeinsameSteuererklaerung(null);
			}
		}

		final FamiliensituationContainer mergedFamiliensituationContainer = persistence.merge
			(familiensituationContainer);
		gesuch.setFamiliensituationContainer(mergedFamiliensituationContainer);

		// get old FamSit to compare with
		Familiensituation oldFamiliensituation;
		if (mergedFamiliensituationContainer != null && mergedFamiliensituationContainer
			.getFamiliensituationErstgesuch() != null) {
			oldFamiliensituation = mergedFamiliensituationContainer.getFamiliensituationErstgesuch();  //bei mutation
			// immer die Situation vom Erstgesuch als  Basis fuer Wizardstepanpassung
		} else {
			oldFamiliensituation = loadedFamiliensituation;
		}

		//Alle Daten des GS2 loeschen wenn man von 2GS auf 1GS wechselt und GS2 bereits erstellt wurde
		if (gesuch.getGesuchsteller2() != null && isNeededToRemoveGesuchsteller2(gesuch,
			mergedFamiliensituationContainer.extractFamiliensituation(), oldFamiliensituation)) {
			gesuchstellerService.removeGesuchsteller(gesuch.getGesuchsteller2());
			gesuch.setGesuchsteller2(null);
			newFamiliensituation.setGemeinsameSteuererklaerung(false);
		}

		wizardStepService.updateSteps(gesuch.getId(), oldFamiliensituation, newFamiliensituation, WizardStepName
			.FAMILIENSITUATION);
		return mergedFamiliensituationContainer;
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<FamiliensituationContainer> findFamiliensituation(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		FamiliensituationContainer a = persistence.find(FamiliensituationContainer.class, key);
		return Optional.ofNullable(a);
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<FamiliensituationContainer> getAllFamiliensituatione() {
		return new ArrayList<>(criteriaQueryHelper.getAll(FamiliensituationContainer.class));
	}

	@Override
	@RolesAllowed({ ADMIN, SUPER_ADMIN, SACHBEARBEITER_JA, GESUCHSTELLER })
	public void removeFamiliensituation(@Nonnull FamiliensituationContainer familiensituation) {
		Validate.notNull(familiensituation);
		Optional<FamiliensituationContainer> familiensituationToRemove = findFamiliensituation(familiensituation.getId());
		familiensituationToRemove.orElseThrow(() -> new EbeguEntityNotFoundException("removeFall", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, familiensituation));
		familiensituationToRemove.ifPresent(familiensituationContainer -> persistence.remove
			(familiensituationContainer));
	}

	/**
	 * Wenn die neue Familiensituation nur 1GS hat und der zweite GS schon existiert, wird dieser
	 * und seine Daten endgueltig geloescht. Dies gilt aber nur fuer ERSTGESUCH. Bei Mutationen wird
	 * der 2GS nie geloescht
	 */
	private boolean isNeededToRemoveGesuchsteller2(Gesuch gesuch, Familiensituation newFamiliensituation,
		Familiensituation familiensituationErstgesuch) {
		return (!gesuch.isMutation() && gesuch.getGesuchsteller2() != null && !newFamiliensituation.hasSecondGesuchsteller())
			|| (gesuch.isMutation() && isChanged1To2Reverted(gesuch, newFamiliensituation, familiensituationErstgesuch));
	}

	private boolean isChanged1To2Reverted(Gesuch gesuch, Familiensituation newFamiliensituation, Familiensituation familiensituationErstgesuch) {
		return gesuch.getGesuchsteller2() != null && !familiensituationErstgesuch.hasSecondGesuchsteller()
			&& !newFamiliensituation.hasSecondGesuchsteller();
	}
}
