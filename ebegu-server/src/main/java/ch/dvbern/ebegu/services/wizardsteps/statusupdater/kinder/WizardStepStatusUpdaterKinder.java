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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater.kinder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractMutableEntity;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.AbstractStatusUpdater;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.betreuung.WizardStepStatusUpdaterBetreuung;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.erwerbspensum.WizardStepStatusUpdaterErwerbspensum;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
public class WizardStepStatusUpdaterKinder extends AbstractStatusUpdater {
	private WizardStepStatusUpdaterBetreuung wizardStepStatusUpdaterBetreuung;
	private WizardStepStatusUpdaterErwerbspensum wizardStepStatusUpdaterErwerbspensum;
	private KindService kindService;

	@Inject
	public WizardStepStatusUpdaterKinder(
		WizardStepStatusUpdaterBetreuung wizardStepStatusUpdaterBetreuung,
		WizardStepStatusUpdaterErwerbspensum wizardStepStatusUpdaterErwerbspensum,
		KindService kindService,
		GesuchService gesuchService,
		Persistence persistence
	) {
		super(gesuchService, persistence);
		this.wizardStepStatusUpdaterBetreuung =
			wizardStepStatusUpdaterBetreuung;
		this.wizardStepStatusUpdaterErwerbspensum =
			wizardStepStatusUpdaterErwerbspensum;
		this.kindService = kindService;
	}

	/**
	 * We ensure that the no args proxy bean is never used
	 */
	@PostConstruct
	@SuppressWarnings("PMD.UnusedPrivateMethod") // false postive
	@SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD") // false postive
	private void init() {
		if (this.wizardStepStatusUpdaterBetreuung == null) {
			throw new IllegalStateException(
				"wizardStepStatusUpdaterBetreuung must not be null"
			);
		}
		if (this.wizardStepStatusUpdaterErwerbspensum == null) {
			throw new IllegalStateException(
				"wizardStepStatusUpdaterErwerbspensum must not be null"
			);
		}
		if (this.kindService == null) {
			throw new IllegalStateException("kindService must not be null");
		}
	}

	private List<KindContainer> findAllKinderFromGesuch(WizardStep wizardStep) {
		return kindService.findAllKinderFromGesuch(
			wizardStep.getGesuch().getId()
		)
			.stream()
			.filter(
				kindContainer -> kindContainer.getKindJA()
					.getFamilienErgaenzendeBetreuung()
			)
			.collect(Collectors.toList());
	}

	private boolean hasNichtGepruefteKinder(
		List<KindContainer> kinderFromGesuch
	) {
		return kinderFromGesuch
			.stream()
			.anyMatch(
				kindContainer -> !kindContainer.getKindJA().isGeprueft()
			);
	}

	@Override
	protected List<AbstractMutableEntity> getStepRelatedObjects(Gesuch gesuch) {
		return new ArrayList<>(gesuch.getKindContainers());
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
					this.wizardStepStatusUpdaterBetreuung
						.updateStepStatusForBetreuungChange(
							wizardStep,
							true
						);

				} else if (WizardStepName.ERWERBSPENSUM
					== wizardStep.getWizardStepName()) {
					this.wizardStepStatusUpdaterErwerbspensum
						.updateErwerbspensumStepStatus(wizardStep, true);
				} else if (WizardStepName.KINDER
					== wizardStep.getWizardStepName()) {
					final List<KindContainer> kinderFromGesuch =
						findAllKinderFromGesuch(wizardStep);

					WizardStepStatus status;
					if (kinderFromGesuch.isEmpty()) {
						status = WizardStepStatus.NOK;
					} else if (hasNichtGepruefteKinder(kinderFromGesuch)) {
						status = WizardStepStatus.IN_BEARBEITUNG;
					} else {
						status =
							getWizardStepStatusOkOrMutiert(wizardStep);
					}
					wizardStep.setWizardStepStatus(status);
				}
			}
		}
	}
}
