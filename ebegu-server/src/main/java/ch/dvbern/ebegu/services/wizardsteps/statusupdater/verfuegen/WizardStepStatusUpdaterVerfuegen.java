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

package ch.dvbern.ebegu.services.wizardsteps.statusupdater.verfuegen;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.activation.MimeTypeParseException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractMutableEntity;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.AntragStatusHistoryService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GeneratedDokumentService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.MailService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.AbstractStatusUpdater;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.util.logging.LogUtil.logExceptionAccordingToEnvironment;

@ApplicationScoped
@NoArgsConstructor
public class WizardStepStatusUpdaterVerfuegen extends AbstractStatusUpdater {
	@Nonnull
	private static final Logger LOG = LoggerFactory.getLogger(
		WizardStepStatusUpdaterVerfuegen.class
	);
	private GesuchService gesuchService;
	private GeneratedDokumentService generatedDokumentService;
	private GemeindeService gemeindeService;
	private MailService mailService;
	private EbeguConfiguration ebeguConfiguration;
	private AntragStatusHistoryService antragStatusHistoryService;

	@Inject
	public WizardStepStatusUpdaterVerfuegen(
		GesuchService gesuchService,
		GeneratedDokumentService generatedDokumentService,
		GemeindeService gemeindeService,
		MailService mailService,
		EbeguConfiguration ebeguConfiguration,
		AntragStatusHistoryService antragStatusHistoryService,
		Persistence persistence
	) {
		super(gesuchService, persistence);
		this.gesuchService = gesuchService;
		this.generatedDokumentService = generatedDokumentService;
		this.gemeindeService = gemeindeService;
		this.mailService = mailService;
		this.ebeguConfiguration = ebeguConfiguration;
		this.antragStatusHistoryService = antragStatusHistoryService;
	}

	/**
	 * We ensure that the no args proxy bean is never used
	 */
	@PostConstruct
	@SuppressWarnings("PMD.UnusedPrivateMethod") // false postive
	@SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD") // false postive
	private void init() {
		if (this.gesuchService == null) {
			throw new IllegalStateException("gesuchService must not be null");
		}
		if (this.generatedDokumentService == null) {
			throw new IllegalStateException(
				"generatedDokumentService must not be null"
			);
		}
		if (this.gemeindeService == null) {
			throw new IllegalStateException("gemeindeService must not be null");
		}
		if (this.mailService == null) {
			throw new IllegalStateException("mailService must not be null");
		}
		if (this.ebeguConfiguration == null) {
			throw new IllegalStateException(
				"ebeguConfiguration must not be null"
			);
		}
		if (this.antragStatusHistoryService == null) {
			throw new IllegalStateException(
				"antragStatusHistoryService must not be null"
			);
		}
	}

	public void updateAllStatusForVerfuegen(List<WizardStep> wizardSteps) {
		for (WizardStep wizardStep : wizardSteps) {
			if (WizardStepName.VERFUEGEN == wizardStep.getWizardStepName()
				&& WizardStepStatus.OK
					!= wizardStep.getWizardStepStatus()) {
				List<Betreuung> alleBetreuungen = wizardStep.getGesuch()
					.extractAllBetreuungen();
				if (alleBetreuungen
					.stream()
					.allMatch(
						betreuung -> betreuung.getBetreuungsstatus()
							.isGeschlossen()
					)) {
					gesuchVerfuegen(wizardStep);
				}
			}
		}
	}

	/**
	 * In dieser Methode werden alle Sachen gemacht, die gebraucht werden, um ein Gesuch zu verfuegen.
	 */
	private void gesuchVerfuegen(@NotNull WizardStep verfuegenWizardStep) {
		if (verfuegenWizardStep.getWizardStepName()
			== WizardStepName.VERFUEGEN) {
			verfuegenWizardStep.setWizardStepStatus(WizardStepStatus.OK);
			verfuegenWizardStep.getGesuch().setStatus(AntragStatus.VERFUEGT);
			gesuchService.postGesuchVerfuegen(verfuegenWizardStep.getGesuch());

			// Hier wird das Gesuch oder die Mutation effektiv verfügt. Daher müssen hier noch andere Services gerufen
			// werden!
			try {
				generatedDokumentService.getBegleitschreibenDokument(
					verfuegenWizardStep.getGesuch(),
					true
				);
			} catch (MimeTypeParseException | MergeDocException e) {
				LOG.error("Error updating Deckblatt Dokument", e);
			}

			try {
				GemeindeStammdaten gemeindeStammdaten =
					gemeindeService.getGemeindeStammdatenByGemeindeId(
						verfuegenWizardStep.getGesuch()
							.getDossier()
							.getGemeinde()
							.getId()
					)
						.orElseThrow(
							() -> new EbeguEntityNotFoundException(
								"gesuchVerfuegen",
								ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
								verfuegenWizardStep.getGesuch()
									.getDossier()
									.getGemeinde()
									.getId()
							)
						);
				if (gemeindeStammdaten.getBenachrichtigungBgEmailAuto()) {
					if (!verfuegenWizardStep.getGesuch().isMutation()) {
						// Erstgesuch
						mailService.sendInfoVerfuegtGesuch(
							verfuegenWizardStep.getGesuch()
						);
					} else {
						// Mutation
						mailService.sendInfoVerfuegtMutation(
							verfuegenWizardStep.getGesuch()
						);
					}
				}
			} catch (Exception e) {
				logExceptionAccordingToEnvironment(
					e,
					"Error sending Mail zu gesuchsteller",
					ebeguConfiguration.getIsDevmode(),
					""
				);
			}

			antragStatusHistoryService.saveStatusChange(
				verfuegenWizardStep.getGesuch(),
				null
			);
		}
	}

	@Override
	protected List<AbstractMutableEntity> getStepRelatedObjects(Gesuch gesuch) {
		return List.of();
	}

	/**
	 * Wenn der Status aller Betreuungen des Gesuchs VERFUEGT ist, dann wechseln wir den Staus von VERFUEGEN auf OK.
	 * Der Status des Gesuchs wechselt auch dann auf VERFUEGT, da alle Angebote sind verfuegt
	 */
	@Override
	protected void updateAllStatus(
		List<WizardStep> wizardSteps,
		@Nullable AbstractEntity oldEntity,
		@Nullable AbstractEntity newEntity,
		@Nullable Integer substep
	) {
		updateAllStatusForVerfuegen(
			wizardSteps
		);
	}
}
