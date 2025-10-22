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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.SozialhilfeZeitraumContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.famsitchangehandler.FamSitChangeHandler;

/**
 * Service fuer familiensituation
 */
@Stateless
@Local(FamiliensituationService.class)
public class FamiliensituationServiceBean extends AbstractBaseService implements
	FamiliensituationService {

	@Inject
	private Persistence persistence;
	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;
	@Inject
	private WizardStepService wizardStepService;
	@Inject
	private SozialhilfeZeitraumService sozialhilfeZeitraumService;

	@Inject
	private FamSitChangeHandler famSitChangeHandler;

	@Nonnull
	@Override
	public Optional<FamiliensituationContainer> findFamiliensituation(
		@Nonnull String key
	) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		FamiliensituationContainer a = persistence.find(
			FamiliensituationContainer.class,
			key
		);
		return Optional.ofNullable(a);
	}

	@Nonnull
	@Override
	public Collection<FamiliensituationContainer> getAllFamiliensituatione() {
		return new ArrayList<>(
			criteriaQueryHelper.getAll(FamiliensituationContainer.class)
		);
	}

	@Override
	public void removeFamiliensituation(
		@Nonnull FamiliensituationContainer familiensituation
	) {
		Objects.requireNonNull(familiensituation);
		FamiliensituationContainer familiensituationToRemove =
			findFamiliensituation(familiensituation.getId()).orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"removeFall",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					familiensituation
				)
			);
		for (SozialhilfeZeitraumContainer sozialhilfeZeitraumCtn : familiensituationToRemove
			.getSozialhilfeZeitraumContainers()) {
			sozialhilfeZeitraumService.removeSozialhilfeZeitraum(
				sozialhilfeZeitraumCtn.getId()
			);
		}
		persistence.remove(familiensituationToRemove);
	}

	@Override
	public FamiliensituationContainer saveFamiliensituationAndHandleChange(
		Gesuch gesuch,
		FamiliensituationContainer familiensituationContainer,
		@Nullable Familiensituation loadedFamiliensituation
	) {
		return saveFamiliensituation(
			gesuch,
			familiensituationContainer,
			loadedFamiliensituation,
			true
		);
	}

	@Override
	public FamiliensituationContainer saveFamiliensituation(
		Gesuch gesuch,
		FamiliensituationContainer familiensituationContainer,
		@Nullable Familiensituation loadedFamiliensituation
	) {
		return saveFamiliensituation(
			gesuch,
			familiensituationContainer,
			loadedFamiliensituation,
			false
		);
	}

	private FamiliensituationContainer saveFamiliensituation(
		Gesuch gesuch,
		FamiliensituationContainer familiensituationContainer,
		Familiensituation loadedFamiliensituation,
		boolean handleChange
	) {
		Objects.requireNonNull(familiensituationContainer);
		Objects.requireNonNull(gesuch);

		// Falls noch nicht vorhanden, werden die GemeinsameSteuererklaerung fuer FS und EV auf false gesetzt
		Familiensituation newFamiliensituation = familiensituationContainer
			.extractFamiliensituation();
		Objects.requireNonNull(newFamiliensituation);
		if (handleChange) {
			famSitChangeHandler.adaptFinSitDataOnFamSitChange(
				gesuch,
				familiensituationContainer,
				loadedFamiliensituation
			);
		}

		final FamiliensituationContainer mergedFamiliensituationContainer =
			persistence.merge(familiensituationContainer);
		gesuch.setFamiliensituationContainer(mergedFamiliensituationContainer);

		// get FamSit Erst Antrag, it will be the same famsit if we are in the Erst Antrag
		Familiensituation familiensituationErstgesuch =
			getFamiliensituationErstgesuch(
				loadedFamiliensituation,
				mergedFamiliensituationContainer
			);
		if (handleChange) {
			famSitChangeHandler.handleFamSitChangeAfterSave(
				gesuch,
				newFamiliensituation,
				mergedFamiliensituationContainer,
				familiensituationErstgesuch
			);
		}

		wizardStepService.updateSteps(
			gesuch.getId(),
			familiensituationErstgesuch,
			newFamiliensituation,
			WizardStepName.FAMILIENSITUATION
		);
		return mergedFamiliensituationContainer;
	}

	private static Familiensituation getFamiliensituationErstgesuch(
		Familiensituation loadedFamiliensituation,
		FamiliensituationContainer mergedFamiliensituationContainer
	) {
		Familiensituation oldFamiliensituation;
		if (mergedFamiliensituationContainer != null
			&& mergedFamiliensituationContainer
				.getFamiliensituationErstgesuch()
				!= null) {
			// Bei Mutation immer die Situation vom Erstgesuch als  Basis fuer Wizardstepanpassung
			oldFamiliensituation = mergedFamiliensituationContainer
				.getFamiliensituationErstgesuch();
		} else {
			oldFamiliensituation = loadedFamiliensituation;
		}
		return oldFamiliensituation;
	}
}
