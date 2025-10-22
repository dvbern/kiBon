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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater.dokumente;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractMutableEntity;
import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.rules.anlageverzeichnis.DokumentenverzeichnisEvaluator;
import ch.dvbern.ebegu.services.DokumentGrundService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.AbstractStatusUpdater;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.DokumenteUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
public class WizardStepStatusUpdaterDokumente extends AbstractStatusUpdater {
	private DokumentenverzeichnisEvaluator dokumentenverzeichnisEvaluator;
	private DokumentGrundService dokumentGrundService;
	private GesuchService gesuchService;

	@Inject
	public WizardStepStatusUpdaterDokumente(
		DokumentenverzeichnisEvaluator dokumentenverzeichnisEvaluator,
		DokumentGrundService dokumentGrundService,
		GesuchService gesuchService,
		Persistence persistence
	) {
		super(gesuchService, persistence);
		this.gesuchService = gesuchService;
		this.dokumentenverzeichnisEvaluator = dokumentenverzeichnisEvaluator;
		this.dokumentGrundService = dokumentGrundService;
	}

	/**
	 * We ensure that the no args proxy bean is never used
	 */
	@PostConstruct
	@SuppressWarnings("PMD.UnusedPrivateMethod") // false postive
	@SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD") // false postive
	private void init() {
		if (this.dokumentenverzeichnisEvaluator == null) {
			throw new IllegalStateException(
				"dokumentenverzeichnisEvaluator must not be null"
			);
		}
		if (this.dokumentGrundService == null) {
			throw new IllegalStateException(
				"dokumentGrundService must not be null"
			);
		}
	}

	@Override
	protected void updateAllStatus(
		List<WizardStep> wizardSteps,
		@Nullable AbstractEntity oldEntity,
		@Nullable AbstractEntity newEntity,
		@Nullable Integer substep
	) {
		for (WizardStep wizardStep : wizardSteps) {
			updateStepForDokumenteChange(wizardStep);
		}
	}

	@Override
	protected List<AbstractMutableEntity> getStepRelatedObjects(Gesuch gesuch) {
		return new ArrayList<>(
			dokumentGrundService.findAllDokumentGrundByGesuch(
				gesuch,
				false
			)
		);
	}

	private boolean checkIfUpdateStepForDokumentNeeded(WizardStep wizardStep) {
		return WizardStepStatus.UNBESUCHT == wizardStep.getWizardStepStatus()
			|| WizardStepName.DOKUMENTE != wizardStep.getWizardStepName();
	}

	private void updateStepForDokumenteChange(WizardStep wizardStep) {
		if (checkIfUpdateStepForDokumentNeeded(wizardStep)) {
			return;
		}

		final Set<DokumentGrund> dokumentGrundsMerged = DokumenteUtil
			.mergeNeededAndPersisted(
				dokumentenverzeichnisEvaluator.calculate(
					wizardStep.getGesuch(),
					Constants.DEFAULT_LOCALE
				),
				dokumentGrundService.findAllDokumentGrundByGesuch(
					wizardStep.getGesuch()
				)
			);

		if (checkIfAllDokumenteNeededToUploadAreUploaded(
			dokumentGrundsMerged
		)) {
			setWizardStepOkOrMutiert(wizardStep);
		} else {
			updateWizardStepStatus(wizardStep, dokumentGrundsMerged);
		}
	}

	private boolean checkIfAllDokumenteNeededToUploadAreUploaded(
		Set<DokumentGrund> dokumentGrundsMerged
	) {
		for (DokumentGrund dokumentGrund : dokumentGrundsMerged) {
			if (!DokumentGrundTyp.isSonstigeOrPapiergesuch(
				dokumentGrund.getDokumentGrundTyp()
			)
				&& dokumentGrund.isNeeded()
				&& dokumentGrund.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private void updateWizardStepStatus(
		WizardStep wizardStep,
		Set<DokumentGrund> dokumentGrundsMerged
	) {
		if (wizardStep.getGesuch().isMutation()) {
			wizardStep.setWizardStepStatus(
				areDokumentUploadedInMutation(dokumentGrundsMerged)
					||
					areDokumentDeletedInMutation(
						dokumentGrundsMerged,
						wizardStep.getGesuch()
					) ? WizardStepStatus.MUTIERT : WizardStepStatus.OK
			);
		} else {
			wizardStep.setWizardStepStatus(WizardStepStatus.IN_BEARBEITUNG);
		}
	}

	private boolean areDokumentUploadedInMutation(
		@Nonnull Set<DokumentGrund> dokumentGrunds
	) {
		for (DokumentGrund dokumentGrund : dokumentGrunds) {
			if (!dokumentGrund.getDokumente().isEmpty()) {
				for (Dokument dokument : dokumentGrund.getDokumente()) {
					if (!dokument.hasVorgaenger()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean areDokumentDeletedInMutation(
		@Nonnull Set<DokumentGrund> dokumentGrunds,
		Gesuch gesuch
	) {
		Gesuch vorgaengerGesuch = gesuchService.findGesuch(
			gesuch.getVorgaengerId()
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"areDokumentUploadedOrDeleted - find Vorgaenger Gesuch",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					gesuch.getVorgaengerId()
				)
			);
		Set<DokumentGrund> vorgaengerDokumentGrunds =
			vorgaengerGesuch.getDokumentGrunds() != null ?
				vorgaengerGesuch.getDokumentGrunds() :
				new HashSet<>();
		for (DokumentGrund dokumentGrund : dokumentGrunds) {
			Optional<DokumentGrund> vorgaengerDokumentGrund =
				vorgaengerDokumentGrunds
					.stream()
					.filter(
						oldDokumentGrund -> oldDokumentGrund
							.getDokumentGrundTyp()
							.equals(dokumentGrund.getDokumentGrundTyp())
					)
					.findFirst();
			if ((!vorgaengerDokumentGrund.isPresent()
				&& dokumentGrund.getDokumente().size() > 0)
				|| (vorgaengerDokumentGrund.isPresent()
					&& vorgaengerDokumentGrund.get().getDokumente().size()
						!= dokumentGrund.getDokumente().size())) {
				return true;
			}
		}
		return false;
	}

	protected boolean isObjectMutiert(
		@NotNull List<AbstractMutableEntity> newEntities,
		@NotNull List<AbstractMutableEntity> oldEntities
	) {
		if (oldEntities.size() != newEntities.size()) {
			return true;
		}
		for (AbstractMutableEntity newEntity : newEntities) {
			if (newEntity.getVorgaengerId() == null) {
				return true;
			}
			final AbstractEntity vorgaengerEntity =
				persistence.find(
					newEntity.getClass(),
					newEntity.getVorgaengerId()
				);
			if (vorgaengerEntity == null
				|| !newEntity.isSame(vorgaengerEntity)) {
				return true;
			}
			if (newEntity instanceof DokumentGrund
				&& isObjectMutiertForDokumentGrund(
					(DokumentGrund) newEntity,
					(DokumentGrund) vorgaengerEntity
				)) {
				return true;
			}
		}
		return false;
	}

	private boolean isObjectMutiertForDokumentGrund(
		DokumentGrund newDokumentGrund,
		DokumentGrund oldDokumentGrund
	) {
		if (newDokumentGrund.getDokumente().size()
			!= oldDokumentGrund.getDokumente()
				.size()) {
			return true;
		}
		for (Dokument dokument : newDokumentGrund
			.getDokumente()) {
			if (!dokument.hasVorgaenger()) {
				return true;
			}
		}
		return false;
	}
}
