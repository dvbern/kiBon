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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater.erwerbspensum;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractMutableEntity;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.AnspruchBeschaeftigungAbhaengigkeitTyp;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.ErwerbspensumService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.AbstractStatusUpdater;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NoArgsConstructor;

import static ch.dvbern.ebegu.services.util.ErwerbspensumHelper.isErwerbespensumContainerEmpty;
import static ch.dvbern.ebegu.services.util.ErwerbspensumHelper.isErwerbspensumRequiredForGS2;

@ApplicationScoped
@NoArgsConstructor
public class WizardStepStatusUpdaterErwerbspensum extends
	AbstractStatusUpdater {
	private EinstellungService einstellungService;
	private ErwerbspensumService erwerbspensumService;

	@Inject
	public WizardStepStatusUpdaterErwerbspensum(
		EinstellungService einstellungService,
		ErwerbspensumService erwerbspensumService,
		GesuchService gesuchService,
		Persistence persistence
	) {
		super(gesuchService, persistence);
		this.einstellungService = einstellungService;
		this.erwerbspensumService = erwerbspensumService;
	}

	/**
	 * We ensure that the no args proxy bean is never used
	 */
	@PostConstruct
	@SuppressWarnings("PMD.UnusedPrivateMethod") // false postive
	@SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD") // false postive
	private void init() {
		if (this.einstellungService == null) {
			throw new IllegalStateException(
				"einstellungService must not be null"
			);
		}
		if (this.erwerbspensumService == null) {
			throw new IllegalStateException(
				"erwerbspensumService must not be null"
			);
		}
	}

	/**
	 * Erwerbspensum muss nur erfasst werden, falls mind. 1 Kita oder 1 Tageseltern Kleinkind Angebot erfasst wurde
	 * und mind. eines dieser Kinder keine Fachstelle involviert hat
	 */
	public void updateErwerbspensumStepStatus(
		WizardStep wizardStep,
		boolean changesBecauseOtherStates
	) {
		Gesuch gesuch = wizardStep.getGesuch();
		boolean erwerbspensumRequired = erwerbspensumService
			.isErwerbspensumRequired(wizardStep.getGesuch());

		WizardStepStatus status = null;
		boolean available = wizardStep.getVerfuegbar();
		if (erwerbspensumRequired) {
			// Wenn das EWP required ist, muss grundsaetzlich der Step available sein
			available = true;
			if (isErwerbespensumContainerEmpty(gesuch.getGesuchsteller1())) {
				// Wenn der Step auf NOK gesetzt wird, muss er enabled sein, damit korrigiert werden kann!
				status = WizardStepStatus.NOK;
			} else {
				if (isErwerbspensumRequiredForGS2(
					gesuch,
					isGesuchBeendenBeiTauschGS2Active(gesuch),
					isAbhaengigkeitBeschaeftigungspensumAnspruchSchwyz(
						gesuch
					)
				)
					&& isErwerbespensumContainerEmpty(
						gesuch.getGesuchsteller2()
					)) {
					// Wenn der Step auf NOK gesetzt wird, muss er enabled sein, damit korrigiert werden kann!
					status = WizardStepStatus.NOK;
				}
				if (!isErwerbspensumRequiredForGS2(
					gesuch,
					isGesuchBeendenBeiTauschGS2Active(gesuch),
					isAbhaengigkeitBeschaeftigungspensumAnspruchSchwyz(
						gesuch
					)
				)
					&& isErwerbespensumContainerEmpty(
						gesuch.getGesuchsteller2()
					)) {
					status = WizardStepStatus.OK;
				}
			}
		} else if (changesBecauseOtherStates
			&& wizardStep.getWizardStepStatus()
				!= WizardStepStatus.MUTIERT) {
			status = WizardStepStatus.OK;
		}
		// Ansonsten OK bzw. MUTIERT
		if (status == null) {
			status = getWizardStepStatusOkOrMutiert(
				wizardStep
			);
		}
		if (wizardStep.getGesuch().isMutation()
			&& status.equals(WizardStepStatus.OK)) {
			status = getWizardStepStatusOkOrMutiert(wizardStep);
		}
		wizardStep.setWizardStepStatus(status);
		wizardStep.setVerfuegbar(available);
	}

	private boolean isGesuchBeendenBeiTauschGS2Active(Gesuch gesuch) {
		Einstellung einstellung = einstellungService.findEinstellung(
			EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2,
			gesuch.extractGemeinde(),
			gesuch.getGesuchsperiode()
		);

		return Boolean.TRUE.equals(einstellung.getValueAsBoolean());
	}

	private boolean isAbhaengigkeitBeschaeftigungspensumAnspruchSchwyz(
		Gesuch gesuch
	) {
		Einstellung einstellung = einstellungService.findEinstellung(
			EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
			gesuch.extractGemeinde(),
			gesuch.getGesuchsperiode()
		);

		return AnspruchBeschaeftigungAbhaengigkeitTyp.valueOf(
			einstellung.getValue()
		) == AnspruchBeschaeftigungAbhaengigkeitTyp.SCHWYZ;
	}

	@Override
	protected List<AbstractMutableEntity> getStepRelatedObjects(Gesuch gesuch) {
		List<AbstractMutableEntity> relatedObjects = new ArrayList<>();
		if (gesuch.getGesuchsteller1() != null) {
			relatedObjects.addAll(
				gesuch.getGesuchsteller1().getErwerbspensenContainers()
			);
		}
		if (gesuch.getGesuchsteller2() != null) {
			relatedObjects.addAll(
				gesuch.getGesuchsteller2().getErwerbspensenContainers()
			);
		}
		return relatedObjects;
	}

	/**
	 * Holt alle Erwerbspensen und Betreuungen von der Datenbank. Nur die Betreuungen vom Typ anders als TAGESSCHULE
	 * und TAGESFAMILIEN werden
	 * beruecksichtigt
	 * Wenn die Anzahl solcher Betreuungen grosser als 0 ist, dann wird es geprueft, ob es Erwerbspensen gibt, wenn
	 * nicht der Status aendert auf NOK.
	 * In allen anderen Faellen wird der Status auf OK gesetzt
	 */
	@Override
	protected void updateAllStatus(
		List<WizardStep> wizardSteps,
		@Nullable AbstractEntity oldEntity,
		@Nullable AbstractEntity newEntity,
		@Nullable Integer substep
	) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepName.ERWERBSPENSUM
				== wizardStep.getWizardStepName()) {
				updateErwerbspensumStepStatus(wizardStep, false);
			}
		}
	}
}
