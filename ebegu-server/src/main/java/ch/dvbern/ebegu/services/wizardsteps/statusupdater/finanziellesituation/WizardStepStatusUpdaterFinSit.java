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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater.finanziellesituation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractMutableEntity;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.finanziellesituation.FinanzielleSituationValidationService;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.AbstractStatusUpdater;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.einkommensverschlechterung.WizardStepStatusUpdaterEinkommensverschlechterung;
import ch.dvbern.ebegu.util.EbeguUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
public class WizardStepStatusUpdaterFinSit extends AbstractStatusUpdater {
	private FinanzielleSituationValidationService finanzielleSituationValidationService;
	private WizardStepStatusUpdaterEinkommensverschlechterung wizardStepStatusUpdaterEinkommensverschlechterung;

	@Inject
	public WizardStepStatusUpdaterFinSit(
		FinanzielleSituationValidationService finanzielleSituationValidationService,
		WizardStepStatusUpdaterEinkommensverschlechterung wizardStepStatusUpdaterEinkommensverschlechterung,
		GesuchService gesuchService,
		Persistence persistence
	) {
		super(gesuchService, persistence);
		this.finanzielleSituationValidationService =
			finanzielleSituationValidationService;
		this.wizardStepStatusUpdaterEinkommensverschlechterung =
			wizardStepStatusUpdaterEinkommensverschlechterung;
	}

	/**
	 * We ensure that the no args proxy bean is never used
	 */
	@PostConstruct
	@SuppressWarnings("PMD.UnusedPrivateMethod") // false postive
	@SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD") // false postive
	private void init() {
		if (this.finanzielleSituationValidationService == null) {
			throw new IllegalStateException(
				"finanzielleSituationValidationService must not be null"
			);
		}
	}

	private void updateAllStatusForFinSit(
		List<WizardStep> wizardSteps,
		@Nullable Integer substep
	) {
		for (WizardStep wizardStep : wizardSteps) {
			updateStepStatusForFinSitChange(substep, wizardStep);
		}
	}

	private void updateStepStatusForFinSitChange(
		@Nullable Integer substep,
		WizardStep wizardStep
	) {
		if (WizardStepStatus.UNBESUCHT == wizardStep.getWizardStepStatus()) {
			return;
		}
		final Gesuch gesuch = wizardStep.getGesuch();
		if (wizardStep.getWizardStepName().isFinSitWizardStepName()) {
			if (gesuch.isMutation()) {
				// Problem: Es kann in der Mutation sowohl eine Aenderung (Status MUTIERT) als auch ein Fehler
				// (Status NOK)
				// gleichzeitig auftreten! Wir zeigen zuerst den Status NOK an
				setStatusDueToFinSitRequired(wizardStep);
				if (WizardStepStatus.OK == wizardStep.getWizardStepStatus()) {
					// Wenn es okay war, koennen wir gegebenenfalls das MUTIERT setzen
					setWizardStepOkOrMutiert(wizardStep);
				}
			} else if (Objects.equals(1, substep)) { //only for substep 1 (finanziellesituationstart)
				setStatusDueToFinSitRequired(wizardStep);
			}
		}
		if (wizardStep.getWizardStepName().isEKVWizardStepName()
			&& Objects.equals(1, substep)) {
			setStatusDueToFinSitRequired(wizardStep);
		}
	}

	public void setStatusDueToFinSitRequired(
		WizardStep wizardStep
	) {
		Gesuch gesuch = wizardStep.getGesuch();
		if (!EbeguUtil.isFinanzielleSituationRequired(gesuch)
			&& EbeguUtil.isFamilienSituationVollstaendig(gesuch)) {

			wizardStep.setWizardStepStatus(WizardStepStatus.OK);
		} else if (!finanzielleSituationValidationService
			.financialDataOfStepIntroducedAndComplete(wizardStep)) {
			// the FinSit/EKV is required but has not been created yet or is only partialy filled, so it must be NOK
			wizardStep.setWizardStepStatus(WizardStepStatus.NOK);
		} else {
			if (wizardStep.getWizardStepName().isFinSitWizardStepName()) {
				if (finanzielleSituationValidationService
					.hasFamilienSituationNeueVeranlagungsstandZuAbholen(
						gesuch
					)) {
					wizardStep.setWizardStepStatus(
						WizardStepStatus.IN_BEARBEITUNG
					);
				} else {
					setWizardStepOkOrMutiert(wizardStep);
				}
			} else {
				wizardStepStatusUpdaterEinkommensverschlechterung
					.setWizardStepOkOrMutiert(wizardStep);
			}
		}
	}

	@Override
	protected List<AbstractMutableEntity> getStepRelatedObjects(Gesuch gesuch) {
		List<AbstractMutableEntity> relatedObjects = new ArrayList<>();
		relatedObjects.add(gesuch);
		if (gesuch.getGesuchsteller1() != null) {
			relatedObjects.add(
				gesuch.getGesuchsteller1()
					.getFinanzielleSituationContainer()
			);
			relatedObjects.add(gesuch.extractFamiliensituation());
		}
		if (gesuch.getGesuchsteller2() != null) {
			relatedObjects.add(
				gesuch.getGesuchsteller2()
					.getFinanzielleSituationContainer()
			);
		}
		return relatedObjects;
	}

	@Override
	protected void updateAllStatus(
		List<WizardStep> wizardSteps,
		@Nullable AbstractEntity oldEntity,
		@Nullable AbstractEntity newEntity,
		@Nullable Integer substep
	) {
		updateAllStatusForFinSit(
			wizardSteps,
			substep
		);
	}

	@Override
	protected boolean isObjectMutiert(
		@NotNull List<AbstractMutableEntity> newEntities,
		@NotNull List<AbstractMutableEntity> oldEntities
	) {
		if (oldEntities.size() != newEntities.size()) {
			return true;
		}
		for (AbstractMutableEntity newEntity : newEntities) {
			if (newEntity != null && newEntity.getVorgaengerId() == null) {
				return true; // if there is no vorgaenger it must have changed
			}
			if (newEntity != null && newEntity.getVorgaengerId() != null) {
				final AbstractEntity vorgaengerEntity =
					persistence.find(
						newEntity.getClass(),
						newEntity.getVorgaengerId()
					);
				if (newEntity instanceof Familiensituation) {
					if (isFamiliensituationForFinSitMutiert(
						(Familiensituation) newEntity,
						(Familiensituation) vorgaengerEntity
					)) {
						return true;
					}
				} else if (newEntity instanceof Gesuch) {
					if (isGesuchForFinSitMutiert(
						(Gesuch) newEntity,
						(Gesuch) vorgaengerEntity
					)) {
						return true;
					}
				} else if (vorgaengerEntity == null
					|| !newEntity.isSame(vorgaengerEntity)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isGesuchForFinSitMutiert(Gesuch gesuch, Gesuch vorgaenger) {
		return !Objects.equals(
			gesuch.getFinSitAenderungGueltigAbDatum(),
			vorgaenger.getFinSitAenderungGueltigAbDatum()
		);
	}

	private boolean isFamiliensituationForFinSitMutiert(
		Familiensituation familiensituation,
		Familiensituation vorgaenger
	) {
		boolean isSameFinSitStart = Objects.equals(
			familiensituation.getGemeinsameSteuererklaerung(),
			vorgaenger.getGemeinsameSteuererklaerung()
		)
			&& Objects.equals(
				familiensituation.getVerguenstigungGewuenscht(),
				vorgaenger.getVerguenstigungGewuenscht()
			)
			&& Objects.equals(
				familiensituation.getSozialhilfeBezueger(),
				vorgaenger.getSozialhilfeBezueger()
			)
			&& Objects.equals(
				familiensituation.isKeineMahlzeitenverguenstigungBeantragt(),
				vorgaenger.isKeineMahlzeitenverguenstigungBeantragt()
			)
			&& Objects.equals(
				familiensituation.isAuszahlungAusserhalbVonKibon(),
				vorgaenger.isAuszahlungAusserhalbVonKibon()
			)
			&& Objects.equals(
				familiensituation.isAbweichendeZahlungsadresse(),
				vorgaenger.isAbweichendeZahlungsadresse()
			)

			&& EbeguUtil.isSame(
				familiensituation.getAuszahlungsdaten(),
				vorgaenger.getAuszahlungsdaten()
			);
		return !isSameFinSitStart;
	}
}
