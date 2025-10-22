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

package ch.dvbern.ebegu.services.wizardsteps;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.entities.WizardStep_;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.SozialdienstFallStatus;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.WizardStepService;
import ch.dvbern.ebegu.services.wizardsteps.statusupdater.WizardStepStatusUpdater;

/**
 * Service fuer Gesuch
 */
@Stateless
@Local(WizardStepService.class)
public class WizardStepServiceBean extends AbstractBaseService implements
	WizardStepService {

	@Inject
	private Persistence persistence;
	@Inject
	private Authorizer authorizer;
	@Inject
	private EinstellungService einstellungService;

	@Inject
	private WizardStepStatusUpdater wizardStepStatusUpdater;

	@Override
	@Nonnull
	public WizardStep saveWizardStep(@Nonnull WizardStep wizardStep) {
		Objects.requireNonNull(wizardStep);
		return persistence.merge(wizardStep);
	}

	@Override
	@Nonnull
	public Optional<WizardStep> findWizardStep(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		WizardStep a = persistence.find(WizardStep.class, key);
		authorizer.checkReadAuthorization(a);
		return Optional.ofNullable(a);
	}

	@Override
	public List<WizardStep> findWizardStepsFromGesuch(String gesuchId) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<WizardStep> query = cb.createQuery(
			WizardStep.class
		);
		Root<WizardStep> root = query.from(WizardStep.class);
		Predicate predWizardStepFromGesuch = cb.equal(
			root.get(WizardStep_.gesuch).get(Gesuch_.id),
			gesuchId
		);

		query.where(predWizardStepFromGesuch);
		final List<WizardStep> criteriaResults = persistence.getCriteriaResults(
			query
		);
		criteriaResults.forEach(
			result -> authorizer.checkReadAuthorization(result)
		);
		return criteriaResults;
	}

	@Override
	public WizardStep findWizardStepFromGesuch(
		String gesuchId,
		WizardStepName stepName
	) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<WizardStep> query = cb.createQuery(
			WizardStep.class
		);
		Root<WizardStep> root = query.from(WizardStep.class);
		Predicate predWizardStepFromGesuch = cb.equal(
			root.get(WizardStep_.gesuch).get(Gesuch_.id),
			gesuchId
		);
		Predicate predWizardStepName = cb.equal(
			root.get(WizardStep_.wizardStepName),
			stepName
		);

		query.where(predWizardStepFromGesuch, predWizardStepName);
		final WizardStep result = persistence.getCriteriaSingleResult(query);
		authorizer.checkReadAuthorization(result);
		return result;
	}

	@Override
	public List<WizardStep> updateSteps(
		String gesuchId,
		@Nullable AbstractEntity oldEntity,
		@Nullable AbstractEntity newEntity,
		WizardStepName stepName
	) {
		return updateSteps(gesuchId, oldEntity, newEntity, stepName, null);
	}

	@Override
	public List<WizardStep> updateSteps(
		String gesuchId,
		@Nullable AbstractEntity oldEntity,
		@Nullable AbstractEntity newEntity,
		WizardStepName stepName,
		@Nullable Integer substep
	) {
		final List<WizardStep> wizardSteps = findWizardStepsFromGesuch(
			gesuchId
		);
		wizardStepStatusUpdater.updateAllStatus(
			wizardSteps,
			oldEntity,
			newEntity,
			stepName,
			substep
		);
		wizardSteps.forEach(this::saveWizardStep);
		return wizardSteps;
	}

	@Nonnull
	@Override
	public List<WizardStep> createWizardStepList(Gesuch gesuch) {

		Boolean abwesenheitActiv = einstellungService.findEinstellung(
			EinstellungKey.ABWESENHEIT_AKTIV,
			gesuch.extractGemeinde(),
			gesuch.getGesuchsperiode()
		).getValueAsBoolean();

		if (AntragTyp.MUTATION == gesuch.getTyp()) {
			return createWizardStepListMutation(gesuch, abwesenheitActiv);
		}
		// GESUCH
		return createWizardStepListGesuch(gesuch, abwesenheitActiv);
	}

	private List<WizardStep> createWizardStepListGesuch(
		Gesuch gesuch,
		Boolean abwesenheitActiv
	) {
		List<WizardStep> wizardStepList = new ArrayList<>();
		if (gesuch.getDossier().getFall().getSozialdienstFall() != null) {
			wizardStepList.add(
				saveWizardStep(
					createWizardStepObject(
						gesuch,
						WizardStepName.SOZIALDIENSTFALL_ERSTELLEN,
						gesuch.getDossier()
							.getFall()
							.getSozialdienstFall()
							.getStatus()
							== SozialdienstFallStatus.AKTIV ?
								WizardStepStatus.OK :
								WizardStepStatus.IN_BEARBEITUNG,
						true
					)
				)
			);
		}
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.GESUCH_ERSTELLEN,
					gesuch.getDossier()
						.getFall()
						.getSozialdienstFall()
						!= null ?
							WizardStepStatus.UNBESUCHT :
							WizardStepStatus.OK,
					gesuch.getDossier()
						.getFall()
						.getSozialdienstFall()
						== null
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.FAMILIENSITUATION,
					WizardStepStatus.UNBESUCHT,
					false
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.GESUCHSTELLER,
					WizardStepStatus.UNBESUCHT,
					false
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.UMZUG,
					WizardStepStatus.UNBESUCHT,
					false
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.KINDER,
					WizardStepStatus.UNBESUCHT,
					false
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.BETREUUNG,
					WizardStepStatus.UNBESUCHT,
					false
				)
			)
		);
		if (abwesenheitActiv.equals(Boolean.TRUE)) {
			wizardStepList.add(
				saveWizardStep(
					createWizardStepObject(
						gesuch,
						WizardStepName.ABWESENHEIT,
						WizardStepStatus.UNBESUCHT,
						false
					)
				)
			);
		}
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.ERWERBSPENSUM,
					WizardStepStatus.UNBESUCHT,
					false
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					this.getFinSitWizardStepNameForGesuch(gesuch),
					WizardStepStatus.UNBESUCHT,
					false
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					this.getEKVWizardStepNameForGesuch(gesuch),
					WizardStepStatus.UNBESUCHT,
					false
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.DOKUMENTE,
					WizardStepStatus.UNBESUCHT,
					false
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.FREIGABE,
					WizardStepStatus.UNBESUCHT,
					false
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.VERFUEGEN,
					WizardStepStatus.UNBESUCHT,
					false
				)
			)
		);
		return wizardStepList;
	}

	private List<WizardStep> createWizardStepListMutation(
		Gesuch gesuch,
		Boolean abwesenheitActiv
	) {
		List<WizardStep> wizardStepList = new ArrayList<>();
		if (gesuch.getDossier().getFall().getSozialdienstFall() != null) {
			wizardStepList.add(
				saveWizardStep(
					createWizardStepObject(
						gesuch,
						WizardStepName.SOZIALDIENSTFALL_ERSTELLEN,
						WizardStepStatus.OK,
						true
					)
				)
			);
		}
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.GESUCH_ERSTELLEN,
					WizardStepStatus.OK,
					true
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.FAMILIENSITUATION,
					WizardStepStatus.OK,
					true
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.GESUCHSTELLER,
					WizardStepStatus.OK,
					true
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.UMZUG,
					WizardStepStatus.OK,
					true
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.KINDER,
					WizardStepStatus.OK,
					true
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.BETREUUNG,
					isThereAnyWartendeBetreuung(gesuch) ?
						WizardStepStatus.WARTEN :
						WizardStepStatus.OK,
					true
				)
			)
		);
		if (abwesenheitActiv.equals(Boolean.TRUE)) {
			wizardStepList.add(
				saveWizardStep(
					createWizardStepObject(
						gesuch,
						WizardStepName.ABWESENHEIT,
						WizardStepStatus.OK,
						true
					)
				)
			);
		}
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.ERWERBSPENSUM,
					WizardStepStatus.OK,
					true
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					this.getFinSitWizardStepNameForGesuch(gesuch),
					WizardStepStatus.OK,
					true
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					this.getEKVWizardStepNameForGesuch(gesuch),
					WizardStepStatus.OK,
					true
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.DOKUMENTE,
					WizardStepStatus.OK,
					true
				)
			)
		);
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.FREIGABE,
					WizardStepStatus.OK,
					true
				)
			)
		);
		// Verfuegen muss WARTEN sein, da die Betreuungen nochmal verfuegt werden muessen
		wizardStepList.add(
			saveWizardStep(
				createWizardStepObject(
					gesuch,
					WizardStepName.VERFUEGEN,
					WizardStepStatus.WARTEN,
					true
				)
			)
		);
		return wizardStepList;
	}

	private boolean isThereAnyWartendeBetreuung(Gesuch gesuch) {
		return gesuch.extractAllBetreuungen()
			.stream()
			.anyMatch(
				betreuung -> betreuung.getBetreuungsstatus()
					== Betreuungsstatus.WARTEN
			);
	}

	@Override
	public void setWizardStepOkOrMutiert(@NotNull WizardStep wizardStep) {
		wizardStep.setWizardStepStatus(
			wizardStepStatusUpdater.getWizardStepStatusOkOrMutiert(wizardStep)
		);
	}

	@Override
	public void unsetWizardStepFreigabe(@NotNull String gesuchId) {
		final List<WizardStep> wizardSteps = findWizardStepsFromGesuch(
			gesuchId
		);
		WizardStep wizardStepFreigabe =
			wizardSteps.stream()
				.filter(
					step -> step.getWizardStepName()
						== WizardStepName.FREIGABE
				)
				.findFirst()
				.get();

		wizardStepFreigabe.setWizardStepStatus(WizardStepStatus.WARTEN);
	}

	private WizardStep createWizardStepObject(
		Gesuch gesuch,
		WizardStepName wizardStepName,
		WizardStepStatus stepStatus,
		Boolean verfuegbar
	) {
		final WizardStep wizardStep = new WizardStep();
		wizardStep.setGesuch(gesuch);
		wizardStep.setVerfuegbar(verfuegbar != null && verfuegbar);
		wizardStep.setWizardStepName(wizardStepName);
		wizardStep.setWizardStepStatus(stepStatus);
		return wizardStep;
	}

	@Override
	public void removeSteps(Gesuch gesToRemove) {
		List<WizardStep> wizardStepsFromGesuch = findWizardStepsFromGesuch(
			gesToRemove.getId()
		);
		for (WizardStep wizardStep : wizardStepsFromGesuch) {
			authorizer.checkWriteAuthorization(wizardStep);
			persistence.remove(wizardStep);
		}
	}

	@Override
	public void setWizardStepOkay(
		@Nonnull String gesuchId,
		@Nonnull WizardStepName stepName
	) {
		final WizardStep wizardStep = findWizardStepFromGesuch(
			gesuchId,
			stepName
		);
		Objects.requireNonNull(
			wizardStep,
			stepName.name()
				+ " WizardStep fuer gesuch nicht gefunden "
				+ gesuchId
		);
		if (WizardStepStatus.OK != wizardStep.getWizardStepStatus()) {
			wizardStep.setWizardStepStatus(WizardStepStatus.OK);
			saveWizardStep(wizardStep);
		}
	}

	@Override
	@Nonnull
	public WizardStepName getFinSitWizardStepNameForGesuch(
		@Nonnull Gesuch gesuch
	) {
		switch (gesuch.getFinSitTyp()) {
		case BERN:
		case BERN_FKJV:
			return WizardStepName.FINANZIELLE_SITUATION;
		case LUZERN:
			return WizardStepName.FINANZIELLE_SITUATION_LUZERN;
		case SOLOTHURN:
			return WizardStepName.FINANZIELLE_SITUATION_SOLOTHURN;
		case APPENZELL, APPENZELL_FOLGEMONAT:
			return WizardStepName.FINANZIELLE_SITUATION_APPENZELL;
		case SCHWYZ, SCHWYZ_ERWEITERT:
			return WizardStepName.FINANZIELLE_SITUATION_SCHWYZ;
		default:
			throw new EbeguRuntimeException(
				"getFinSitWizardStepNameForGesuch",
				"no WizardStepName found for typ " + gesuch.getFinSitTyp()
			);
		}
	}

	@Override
	@Nonnull
	public WizardStepName getEKVWizardStepNameForGesuch(
		@Nonnull Gesuch gesuch
	) {
		switch (gesuch.getFinSitTyp()) {
		case BERN:
		case BERN_FKJV:
			return WizardStepName.EINKOMMENSVERSCHLECHTERUNG;
		case LUZERN:
			return WizardStepName.EINKOMMENSVERSCHLECHTERUNG_LUZERN;
		case SOLOTHURN:
			return WizardStepName.EINKOMMENSVERSCHLECHTERUNG_SOLOTHURN;
		case APPENZELL, APPENZELL_FOLGEMONAT:
			return WizardStepName.EINKOMMENSVERSCHLECHTERUNG_APPENZELL;
		case SCHWYZ, SCHWYZ_ERWEITERT:
			return WizardStepName.EINKOMMENSVERSCHLECHTERUNG_SCHWYZ;
		default:
			throw new EbeguRuntimeException(
				"getEKVWizardStepNameForGesuch",
				"no WizardStepName found for typ " + gesuch.getFinSitTyp()
			);
		}
	}
}
