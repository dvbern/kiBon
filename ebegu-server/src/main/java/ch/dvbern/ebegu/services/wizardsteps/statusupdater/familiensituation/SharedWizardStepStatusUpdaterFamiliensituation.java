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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater.familiensituation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractMutableEntity;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.AnspruchBeschaeftigungAbhaengigkeitTyp;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.finanziellesituation.FinanzielleSituationValidationService;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.ErwerbspensumService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.einkommensverschlechterung.WizardStepStatusUpdaterEinkommensverschlechterung;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.erwerbspensum.WizardStepStatusUpdaterErwerbspensum;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.finanziellesituation.WizardStepStatusUpdaterFinSit;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.gesuchsteller.WizardStepStatusUpdaterGesuchsteller;
import ch.dvbern.ebegu.util.EbeguUtil;

import static ch.dvbern.ebegu.services.util.ErwerbspensumHelper.isErwerbespensumContainerEmpty;
import static ch.dvbern.ebegu.services.util.ErwerbspensumHelper.isErwerbspensumRequiredForGS2;

public class SharedWizardStepStatusUpdaterFamiliensituation extends
	WizardStepStatusUpdaterFamiliensituation {

	private final KindService kindService;
	private final ErwerbspensumService erwerbspensumService;
	private final EinstellungService einstellungService;
	private final WizardStepStatusUpdaterErwerbspensum wizardStepStatusUpdaterErwerbspensum;
	private final WizardStepStatusUpdaterGesuchsteller wizardStepStatusUpdaterGesuchsteller;
	private final WizardStepStatusUpdaterFinSit wizardStepStatusUpdaterFinSit;
	private final WizardStepStatusUpdaterEinkommensverschlechterung wizardStepStatusUpdaterEinkommensverschlechterung;

	final FinanzielleSituationValidationService finanzielleSituationValidationService;

	public SharedWizardStepStatusUpdaterFamiliensituation(
		KindService kindService,
		ErwerbspensumService erwerbspensumService,
		EinstellungService einstellungService,
		WizardStepStatusUpdaterErwerbspensum wizardStepStatusUpdaterErwerbspensum,
		FinanzielleSituationValidationService finanzielleSituationValidationService,
		GesuchService gesuchService,
		Persistence persistence,
		WizardStepStatusUpdaterGesuchsteller wizardStepStatusUpdaterGesuchsteller,
		WizardStepStatusUpdaterFinSit wizardStepStatusUpdaterFinSit,
		WizardStepStatusUpdaterEinkommensverschlechterung wizardStepStatusUpdaterEinkommensverschlechterung
	) {
		super(gesuchService, persistence);
		this.kindService = kindService;
		this.erwerbspensumService = erwerbspensumService;
		this.einstellungService = einstellungService;
		this.wizardStepStatusUpdaterErwerbspensum =
			wizardStepStatusUpdaterErwerbspensum;
		this.finanzielleSituationValidationService =
			finanzielleSituationValidationService;
		this.wizardStepStatusUpdaterGesuchsteller =
			wizardStepStatusUpdaterGesuchsteller;
		this.wizardStepStatusUpdaterFinSit =
			wizardStepStatusUpdaterFinSit;
		this.wizardStepStatusUpdaterEinkommensverschlechterung =
			wizardStepStatusUpdaterEinkommensverschlechterung;
	}

	protected void updateStatusForFamiliensituationChange(
		@Nonnull WizardStep wizardStep,
		Familiensituation oldFamiliensituation,
		Familiensituation newFamiliensituation
	) {
		LocalDate bis = wizardStep.getGesuch()
			.getGesuchsperiode()
			.getGueltigkeit()
			.getGueltigBis();
		if (WizardStepName.FAMILIENSITUATION
			== wizardStep.getWizardStepName()) {
			setWizardStepOkOrMutiert(wizardStep);
		} else if (WizardStepName.KINDER == wizardStep.getWizardStepName()) {
			//Nach Update der FamilienSituation kann es sein dass die Kinder View nicht mehr Valid ist
			updateKinderStepStatusOnFamSitChange(
				wizardStep,
				oldFamiliensituation,
				newFamiliensituation
			);
		} else if (WizardStepName.ERWERBSPENSUM
			== wizardStep.getWizardStepName()) {
			getWizardStepStatusUpdaterErwerbspensum()
				.updateErwerbspensumStepStatus(wizardStep, true);
		} else if (EbeguUtil.fromOneGSToTwoGS(
			oldFamiliensituation,
			newFamiliensituation,
			bis
		)) {
			updateStatusFromOneGSToTwoGS(wizardStep);
		} else if (!oldFamiliensituation.isSpezialFallAR()
			&& newFamiliensituation.isSpezialFallAR()) {
			updateStepsForWechselToFamiSitSpeziallFallAR(wizardStep);
			//kann man effektiv sagen dass bei nur einem GS niemals Rote Schritte FinanzielleSituation und EVK
			// gibt
		} else if (!newFamiliensituation.hasSecondGesuchsteller(bis)
			&& wizardStep.getGesuch().getGesuchsteller1() != null) { // nur 1 GS
			updateStatusOnlyOneGS(wizardStep);
		}
	}

	protected void updateKinderStepStatusOnFamSitChange(
		WizardStep wizardStepKinder,
		@Nonnull Familiensituation oldFamiliensituation,
		@Nonnull Familiensituation newFamiliensituation
	) {
		if (hasNichtGepruefteKinder(
			findAllKinderFromGesuch(wizardStepKinder)
		)) {
			wizardStepKinder.setWizardStepStatus(WizardStepStatus.NOK);
		}
		if (newFamiliensituation.getGesuchstellerKardinalitaet()
			!= oldFamiliensituation.getGesuchstellerKardinalitaet()) {
			wizardStepKinder.setWizardStepStatus(WizardStepStatus.NOK);
		}
	}

	protected WizardStepStatusUpdaterErwerbspensum getWizardStepStatusUpdaterErwerbspensum() {
		return wizardStepStatusUpdaterErwerbspensum;
	}

	protected WizardStepStatusUpdaterGesuchsteller getWizardStepStatusUpdaterGesuchsteller() {
		return wizardStepStatusUpdaterGesuchsteller;
	}

	protected WizardStepStatusUpdaterFinSit getWizardStepStatusUpdaterFinSit() {
		return wizardStepStatusUpdaterFinSit;
	}

	protected WizardStepStatusUpdaterEinkommensverschlechterung getWizardStepStatusUpdaterEinkommensverschlechterung() {
		return wizardStepStatusUpdaterEinkommensverschlechterung;
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

	private void updateStatusFromOneGSToTwoGS(WizardStep wizardStep) {
		//Falls bereits ein GS2 exisitiert müssen die Wizardsteps beim Wechsel von ein GS auf zwei GS nicht updated
		// werden
		if (wizardStep.getGesuch().getGesuchsteller2() != null) {
			return;
		}

		if (WizardStepName.GESUCHSTELLER == wizardStep.getWizardStepName()) {
			updateStepGesuchstellerFromOneGSTOTwoGS(wizardStep);
		} else if (wizardStep.getWizardStepName().isFinSitWizardStepName()
			|| wizardStep.getWizardStepName()
				.isEKVWizardStepName()) {
			updateStepFinSitAndEKVFromOneGSToTwoGS(wizardStep);
		} else if (WizardStepName.ERWERBSPENSUM
			== wizardStep.getWizardStepName()) {
			updateStepErwerbspensumFromOneGSToTwoGS(wizardStep);
		}
	}

	private void updateStepGesuchstellerFromOneGSTOTwoGS(
		WizardStep wizardStep
	) {
		setVerguegbarAndNOK(wizardStep);
	}

	private void updateStepFinSitAndEKVFromOneGSToTwoGS(WizardStep wizardStep) {
		if (EbeguUtil.isFinanzielleSituationRequired(wizardStep.getGesuch())) {
			setVerguegbarAndNOK(wizardStep);
		}
	}

	private void updateStepErwerbspensumFromOneGSToTwoGS(
		WizardStep wizardStep
	) {
		if (erwerbspensumService.isErwerbspensumRequired(wizardStep.getGesuch())
			&&
			isErwerbspensumRequiredForGS2(
				wizardStep.getGesuch(),
				isGesuchBeendenBeiTauschGS2Active(
					wizardStep.getGesuch()
				),
				isAbhaengigkeitBeschaeftigungspensumAnspruchSchwyz(
					wizardStep.getGesuch()
				)
			)) {
			// Wenn der Step auf NOK gesetzt wird, muss er enabled sein, damit korrigiert werden kann!
			setVerguegbarAndNOK(wizardStep);
		}
	}

	private void updateStepsForWechselToFamiSitSpeziallFallAR(
		WizardStep wizardStep
	) {
		if (wizardStep.getWizardStepName().isFinSitWizardStepName()
			||
			wizardStep.getWizardStepName().isEKVWizardStepName()) {
			setVerguegbarAndNOK(wizardStep);
		}

	}

	private void updateStatusOnlyOneGS(WizardStep wizardStep) {
		if (WizardStepName.GESUCHSTELLER == wizardStep.getWizardStepName()) {
			updateStepGesuchstellerOnlyOneGS(wizardStep);
		} else if (wizardStep.getWizardStepName().isFinSitWizardStepName()) {
			updateStepFinSitOnlyOneGS(wizardStep);
		} else if (wizardStep.getWizardStepName()
			.isEKVWizardStepName()) {
			updateStepEKVOnlyOneGS(wizardStep);
		} else if (WizardStepName.ERWERBSPENSUM
			== wizardStep.getWizardStepName()) {
			updateStepErwerbspensumOnlyOneGS(wizardStep);
		}
	}

	protected void updateStepGesuchstellerOnlyOneGS(WizardStep wizardStep) {
		if (wizardStep.getGesuch().isMutation()) {
			wizardStepStatusUpdaterGesuchsteller.setWizardStepOkOrMutiert(
				wizardStep
			);
		} else if (wizardStep.getWizardStepStatus() == WizardStepStatus.NOK) {
			wizardStep.setWizardStepStatus(WizardStepStatus.OK);
		}
	}

	private void updateStepFinSitOnlyOneGS(WizardStep wizardStep) {
		if (wizardStep.getGesuch().isMutation()) {
			wizardStep.setVerfuegbar(true);
			wizardStepStatusUpdaterFinSit.setWizardStepOkOrMutiert(wizardStep);
		} else if (wizardStep.getWizardStepStatus() == WizardStepStatus.NOK) {
			wizardStep.setVerfuegbar(true);
			wizardStep.setWizardStepStatus(WizardStepStatus.OK);
		}
	}

	private void updateStepEKVOnlyOneGS(WizardStep wizardStep) {
		if (wizardStep.getGesuch().isMutation()) {
			wizardStep.setVerfuegbar(true);
			wizardStepStatusUpdaterEinkommensverschlechterung
				.setWizardStepOkOrMutiert(wizardStep);
		} else if (wizardStep.getWizardStepStatus() == WizardStepStatus.NOK) {
			wizardStep.setVerfuegbar(true);
			wizardStep.setWizardStepStatus(WizardStepStatus.OK);
		}
	}

	private void updateStepErwerbspensumOnlyOneGS(WizardStep wizardStep) {
		if (erwerbspensumService.isErwerbspensumRequired(
			wizardStep.getGesuch()
		)) {
			if (isErwerbespensumContainerEmpty(
				wizardStep.getGesuch().getGesuchsteller1()
			)) {
				if (wizardStep.getWizardStepStatus() != WizardStepStatus.NOK) {
					// Wenn der Step auf NOK gesetzt wird, muss er enabled sein, damit korrigiert werden
					// kann!
					setVerguegbarAndNOK(wizardStep);
				}
			} else {
				if (wizardStep.getGesuch().isMutation()) {
					wizardStep.setVerfuegbar(true);
					wizardStepStatusUpdaterEinkommensverschlechterung
						.setWizardStepOkOrMutiert(wizardStep);
				} else if (wizardStep.getWizardStepStatus()
					== WizardStepStatus.NOK) {
					wizardStep.setVerfuegbar(true);
					wizardStep.setWizardStepStatus(WizardStepStatus.OK);
				}
			}
		}
	}

	/**
	 * Updates the Status of the Steps FINANZIELLE_SITUATION_X or EINKOMMENSVERSCHLECHTERUNG depending on the kind of
	 * the
	 * betreuungen. This should be called after removing or adding a Betreuung. It is also called after a change in the
	 * famsit
	 */
	private void updateStepFinSit(@Nonnull WizardStep wizardStep) {
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
		List<AbstractMutableEntity> relatedObjects = new ArrayList<>();
		relatedObjects.add(gesuch.getFamiliensituationContainer());
		return relatedObjects;
	}

	@Override
	protected void updateAllStatus(
		List<WizardStep> wizardSteps,
		@Nullable AbstractEntity oldEntity,
		@Nullable AbstractEntity newEntity,
		@Nullable Integer substep
	) {
		if (oldEntity instanceof Familiensituation
			&& newEntity instanceof Familiensituation) {
			for (WizardStep wizardStep : wizardSteps) {
				if (WizardStepStatus.UNBESUCHT
					!= wizardStep.getWizardStepStatus()) { // vermeide, dass der Status eines unbesuchten Steps geaendert
					// wird
					updateStatusForFamiliensituationChange(
						wizardStep,
						(Familiensituation) oldEntity,
						(Familiensituation) newEntity
					);
				}
				// Es gibt ein Spezialfall: Falls eine BG Betreuung hinzugefügt wurde, wird die Frage
				// verguenstigungGewuenscht auf der FamSit true gesetzt und die FamSit wird neu gespeichert.
				// in diesem Fall müssen wir den FinSitStatus hier noch einmal für die Betreuungen prüfen.
				updateStepFinSit(wizardStep);
			}
		} else {
			updateStepThatChanged(
				wizardSteps,
				WizardStepName.FAMILIENSITUATION
			);
		}
	}
}
