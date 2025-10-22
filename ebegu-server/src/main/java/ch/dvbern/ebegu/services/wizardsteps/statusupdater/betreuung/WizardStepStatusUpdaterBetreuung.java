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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater.betreuung;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractMutableEntity;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.finanziellesituation.FinanzielleSituationValidationService;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.AbstractStatusUpdater;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.erwerbspensum.WizardStepStatusUpdaterErwerbspensum;
import ch.dvbern.ebegu.util.EbeguUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
public class WizardStepStatusUpdaterBetreuung extends AbstractStatusUpdater {

	private PrincipalBean principalBean;
	private WizardStepStatusUpdaterErwerbspensum wizardStepStatusUpdaterErwerbspensum;
	private FinanzielleSituationValidationService finanzielleSituationValidationService;

	@Inject
	public WizardStepStatusUpdaterBetreuung(
		PrincipalBean principalBean,
		WizardStepStatusUpdaterErwerbspensum wizardStepStatusUpdaterErwerbspensum,
		FinanzielleSituationValidationService finanzielleSituationValidationService,
		GesuchService gesuchService,
		Persistence persistence
	) {
		super(gesuchService, persistence);
		this.principalBean = principalBean;
		this.wizardStepStatusUpdaterErwerbspensum =
			wizardStepStatusUpdaterErwerbspensum;
		this.finanzielleSituationValidationService =
			finanzielleSituationValidationService;
	}

	/**
	 * We ensure that the no args proxy bean is never used
	 */
	@PostConstruct
	@SuppressWarnings("PMD.UnusedPrivateMethod") // false postive
	@SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD") // false postive
	private void init() {
		if (this.principalBean == null) {
			throw new IllegalStateException("principalBean must not be null");
		}
		if (this.wizardStepStatusUpdaterErwerbspensum == null) {
			throw new IllegalStateException(
				"wizardStepStatusUpdaterErwerbspensum must not be null"
			);
		}
		if (this.finanzielleSituationValidationService == null) {
			throw new IllegalStateException(
				"finanzielleSituationValidationService must not be null"
			);
		}
	}

	public void updateStepStatusForBetreuungChange(
		@Nonnull WizardStep wizardStep,
		boolean changesBecauseOtherStates
	) {
		List<AbstractPlatz> allPlaetze = wizardStep.getGesuch()
			.extractAllPlaetze();
		WizardStepStatus status;
		if (changesBecauseOtherStates
			&& wizardStep.getWizardStepStatus()
				!= WizardStepStatus.MUTIERT) {
			status = WizardStepStatus.OK;
		} else {
			status = getWizardStepStatusOkOrMutiert(
				wizardStep
			);
		}

		if (allPlaetze.isEmpty()) {
			status = WizardStepStatus.NOK;
			wizardStep.setWizardStepStatus(status);
			return;
		}
		for (AbstractPlatz betreuung : allPlaetze) {
			if (Betreuungsstatus.ABGEWIESEN
				== betreuung.getBetreuungsstatus()) {
				status = WizardStepStatus.NOK;
				break;
			}
			if (Betreuungsstatus.WARTEN == betreuung.getBetreuungsstatus()) {
				status = WizardStepStatus.PLATZBESTAETIGUNG;
			}

			if (Betreuungsstatus.UNBEKANNTE_INSTITUTION
				== betreuung.getBetreuungsstatus()) {
				status = WizardStepStatus.WARTEN;
			}
		}
		wizardStep.setWizardStepStatus(status);
	}

	/**
	 * Updates the Status of the Steps FINANZIELLE_SITUATION_X or EINKOMMENSVERSCHLECHTERUNG depending on the kind of
	 * the
	 * betreuungen.
	 * This should be called after removing or adding a Betreuung. It is also called after a change in the famsit
	 */
	private void updateFinSitStepStatusForBetreuungChange(
		@Nonnull WizardStep wizardStep
	) {
		boolean isEkvOrFinSitStep = (wizardStep.getWizardStepName()
			.isEKVWizardStepName()
			|| wizardStep.getWizardStepName().isFinSitWizardStepName());
		if (!isEkvOrFinSitStep) {
			return;
		}
		if (wizardStep.getWizardStepStatus() == WizardStepStatus.IN_BEARBEITUNG
			|| wizardStep.getWizardStepStatus()
				== WizardStepStatus.UNBESUCHT) {
			return;
		}
		boolean finSitIntroducedAndComplete =
			finanzielleSituationValidationService
				.financialDataOfStepIntroducedAndComplete(wizardStep);
		if (finSitIntroducedAndComplete) {
			return;
		}
		boolean finSitRequired = EbeguUtil.isFinanzielleSituationRequired(
			wizardStep.getGesuch()
		);

		// da FinSit nicht vollständig ist, wird wizardStep immer invalidiert, wenn benötigt
		if (finSitRequired) {
			wizardStep.setWizardStepStatus(WizardStepStatus.NOK);
		}
	}

	@Override
	protected List<AbstractMutableEntity> getStepRelatedObjects(Gesuch gesuch) {
		return new ArrayList<>(gesuch.extractAllBetreuungen());
	}

	@Override
	protected void updateAllStatus(
		List<WizardStep> wizardSteps,
		@Nullable AbstractEntity oldEntity,
		@Nullable AbstractEntity newEntity,
		@Nullable Integer substep
	) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepStatus.UNBESUCHT
				!= wizardStep.getWizardStepStatus()) {

				if (WizardStepName.BETREUUNG
					== wizardStep.getWizardStepName()) {
					updateStepStatusForBetreuungChange(wizardStep, false);

				} else if (!this.principalBean.isCallerInAnyOfRole(
					UserRole.getInstitutionTraegerschaftRoles()
				)
					&& WizardStepName.ERWERBSPENSUM
						== wizardStep.getWizardStepName()) {
					// SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION duerfen beim Aendern einer Betreuung
					// den Status von ERWERBPENSUM nicht aendern
					wizardStepStatusUpdaterErwerbspensum
						.updateErwerbspensumStepStatus(wizardStep, true);

				} else if (wizardStep.getWizardStepName()
					.isFinSitWizardStepName()) {
					updateFinSitStepStatusForBetreuungChange(wizardStep);

				} else if (wizardStep.getWizardStepName()
					.isEKVWizardStepName()) {
					updateFinSitStepStatusForBetreuungChange(wizardStep);
				}
			}
		}
	}
}
