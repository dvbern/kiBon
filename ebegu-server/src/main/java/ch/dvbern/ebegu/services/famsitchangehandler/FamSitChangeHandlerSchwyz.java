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

package ch.dvbern.ebegu.services.famsitchangehandler;

import java.time.LocalDate;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GesuchstellerService;

public class FamSitChangeHandlerSchwyz extends
	SharedFamSitChangeDefaultHandler {
	private final FinanzielleSituationService finanzielleSituationService;

	public FamSitChangeHandlerSchwyz(
		GesuchstellerService gesuchstellerService,
		EinstellungService einstellungService,
		FinanzielleSituationService finanzielleSituationService
	) {
		super(gesuchstellerService, einstellungService);
		this.finanzielleSituationService = finanzielleSituationService;
	}

	@Override
	public void adaptFinSitDataOnFamSitChange(
		Gesuch gesuch,
		FamiliensituationContainer familiensituationContainer,
		Familiensituation loadedFamiliensituation
	) {
		Familiensituation newFamiliensituation = familiensituationContainer
			.extractFamiliensituation();
		Objects.requireNonNull(newFamiliensituation);

		adaptFinSitDataOnFamSitChange(
			gesuch,
			loadedFamiliensituation,
			newFamiliensituation
		);

		final LocalDate gueltigBis = gesuch.getGesuchsperiode()
			.getGueltigkeit()
			.getGueltigBis();
		if (!newFamiliensituation.hasSecondGesuchsteller(gueltigBis)) {
			newFamiliensituation.setGemeinsameSteuererklaerung(null);
		}

		super.adaptFinSitDataOnFamSitChange(
			gesuch,
			familiensituationContainer,
			loadedFamiliensituation
		);
	}

	private void adaptFinSitDataOnFamSitChange(
		Gesuch gesuch,
		@Nullable Familiensituation loadedFamiliensituation,
		Familiensituation newFamiliensituation
	) {
		if (loadedFamiliensituation == null
			|| !gesuch.getFinSitTyp().isSchwyzFinSituationTyp()) {
			return;
		}
		if ((isChangeFromGemeinsamStekToAlleine(
			loadedFamiliensituation,
			newFamiliensituation
		)
			|| isGS2WithGemeinsamStekChanged(
				loadedFamiliensituation,
				newFamiliensituation
			))
			&& gesuch.getGesuchsteller1() != null
			&& gesuch.getGesuchsteller1().getFinanzielleSituationContainer()
				!= null) {

			finanzielleSituationService.resetCompleteSchwyzFinSitData(
				gesuch.getGesuchsteller1()
					.getFinanzielleSituationContainer()
					.getFinanzielleSituationJA(),
				gesuch.getGesuchsteller1()
			);

			gesuch.setEinkommensverschlechterungInfoContainer(null);

			if (gesuch.getGesuchsteller1()
				.getEinkommensverschlechterungContainer()
				!= null
				&& gesuch.getGesuchsteller1()
					.getEinkommensverschlechterungContainer()
					.getEkvJABasisJahrPlus1()
					!= null) {
				finanzielleSituationService.resetCompleteSchwyzFinSitData(
					gesuch.getGesuchsteller1()
						.getEinkommensverschlechterungContainer()
						.getEkvJABasisJahrPlus1(),
					gesuch.getGesuchsteller1()
				);
			}
		}
	}

	private static boolean isChangeFromGemeinsamStekToAlleine(
		@Nonnull Familiensituation loadedFamiliensituation,
		Familiensituation newFamiliensituation
	) {
		return loadedFamiliensituation.getGesuchstellerKardinalitaet()
			== EnumGesuchstellerKardinalitaet.ZU_ZWEIT
			&& newFamiliensituation.getGesuchstellerKardinalitaet()
				== EnumGesuchstellerKardinalitaet.ALLEINE
			&& Boolean.TRUE.equals(
				loadedFamiliensituation.getGemeinsameSteuererklaerung()
			);
	}

	private static boolean isGS2WithGemeinsamStekChanged(
		@Nonnull Familiensituation loadedFamiliensituation,
		Familiensituation newFamiliensituation
	) {
		return loadedFamiliensituation.getGesuchstellerKardinalitaet()
			== EnumGesuchstellerKardinalitaet.ZU_ZWEIT
			&& newFamiliensituation.getGesuchstellerKardinalitaet()
				== EnumGesuchstellerKardinalitaet.ZU_ZWEIT
			&& newFamiliensituation.getAenderungPer() != null
			&& Boolean.TRUE.equals(
				loadedFamiliensituation.getGemeinsameSteuererklaerung()
			);
	}

	@Override
	protected void handlePossibleKinderabzugFragenReset(
		Gesuch gesuch,
		Familiensituation newFamiliensituation,
		@Nullable Familiensituation familiensituationErstgesuch
	) {
		if (needsKinderabzugReset(
			newFamiliensituation,
			familiensituationErstgesuch
		)) {
			resetFragenKinderabzugAndSetToUeberpruefen(gesuch);
		}

	}

	private boolean needsKinderabzugReset(
		Familiensituation newFamiliensituation,
		@Nullable Familiensituation familiensituationErstgesuch
	) {
		if (familiensituationErstgesuch == null) {
			return false;
		}
		return familiensituationErstgesuch.getGesuchstellerKardinalitaet()
			!= newFamiliensituation.getGesuchstellerKardinalitaet()
			|| hasSecondGSChanged(
				newFamiliensituation,
				familiensituationErstgesuch
			);
	}

	private static boolean hasSecondGSChanged(
		Familiensituation newFamiliensituation,
		@Nonnull Familiensituation oldFamiliensituation
	) {
		if (newFamiliensituation.getAenderungPer() == null) {
			return false;
		}
		return oldFamiliensituation.getGesuchstellerKardinalitaet()
			== EnumGesuchstellerKardinalitaet.ZU_ZWEIT
			&& newFamiliensituation.getGesuchstellerKardinalitaet()
				== EnumGesuchstellerKardinalitaet.ZU_ZWEIT;
	}

	@Override
	protected boolean isNeededToRemoveGesuchsteller2(
		Gesuch gesuch,
		Familiensituation newFamiliensituation,
		@Nullable Familiensituation familiensituationErstgesuch
	) {
		if (familiensituationErstgesuch == null) {
			return false;
		}
		final LocalDate gpEnd = gesuch.getGesuchsperiode()
			.getGueltigkeit()
			.getGueltigBis();
		if (gesuch.isMutation()) {
			return newFamiliensituation.getAenderungPer() != null
				&&
				(gpEnd.isAfter(newFamiliensituation.getAenderungPer())
					||
					gpEnd.isEqual(
						newFamiliensituation.getAenderungPer()
					));
		}
		return familiensituationErstgesuch.hasSecondGesuchsteller(gpEnd)
			&& !newFamiliensituation.hasSecondGesuchsteller(gpEnd);
	}
}
