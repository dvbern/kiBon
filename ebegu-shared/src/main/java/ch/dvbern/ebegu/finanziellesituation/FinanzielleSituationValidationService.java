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

package ch.dvbern.ebegu.finanziellesituation;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;

import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.finanziellesituation.validation.FinanzielleSituationValidator;
import ch.dvbern.ebegu.finanziellesituation.validation.FinanzielleSituationValidatorSZ;
import ch.dvbern.ebegu.finanziellesituation.validation.FinanzielleSituationValidatorVisitor;
import org.apache.commons.lang.NotImplementedException;

@ApplicationScoped
public class FinanzielleSituationValidationService {

	public boolean financialDataOfStepIntroducedAndComplete(
		WizardStep wizardStep
	) {
		if (wizardStep.getWizardStepName().isFinSitWizardStepName()) {
			return isFinanzielleSituationIntroducedAndComplete(
				wizardStep.getGesuch()
			);
		}
		if (wizardStep.getWizardStepName().isEKVWizardStepName()) {
			return isEKVIntroducedAndComplete(
				wizardStep.getGesuch()
			);

		}
		throw new NotImplementedException(
			"finDataOfStepIntroducedAndComplete is not implemented for step "
				+ wizardStep.getWizardStepName()
		);
	}

	public boolean isEKVIntroducedAndComplete(Gesuch gesuch) {
		return isEKVIndroducedAndComplete(gesuch);
	}

	public boolean isFinanzielleSituationIntroducedAndComplete(
		@Nonnull Gesuch gesuch
	) {
		FinanzielleSituationValidator validator =
			new FinanzielleSituationValidatorVisitor().accept(
				gesuch.getFinSitTyp()
			);
		if (gesuch.getGesuchsteller1() == null
			|| gesuch.getGesuchsteller1().getFinanzielleSituationContainer()
				== null) {
			return false;
		}

		if (isFinanzielleSituationUnvollstaendig(
			gesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA(),
			gesuch.getFinSitTyp(),
			gesuch
		)) {
			return false;
		}
		if (validator.doesFinSitRequireOneGS(gesuch)) {
			return true;
		}
		return gesuch.getGesuchsteller2() == null
			||
			(gesuch.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				!= null
				&&
				(gesuch.getGesuchsteller2()
					.getFinanzielleSituationContainer()
					== null
					|| !isFinanzielleSituationUnvollstaendig(
						gesuch.getGesuchsteller2()
							.getFinanzielleSituationContainer()
							.getFinanzielleSituationJA(),
						gesuch.getFinSitTyp(),
						gesuch
					)));
	}

	private boolean isEKVIndroducedAndComplete(@Nonnull Gesuch gesuch) {
		if (gesuch.getEinkommensverschlechterungInfoContainer() == null) {
			return false;
		}
		if (gesuch.getFinSitTyp().isSchwyzFinSituationTyp()) {
			return isEKVSchwyzIntroducedAndComplete(
				gesuch.getEinkommensverschlechterungInfoContainer(),
				gesuch
			);
		}
		if (hasEinkommensverschlechterung(
			gesuch.getEinkommensverschlechterungInfoContainer()
		)) {
			if (hasEkvFuerBasisJahrPlus1(
				gesuch.getEinkommensverschlechterungInfoContainer()
			)) {
				Objects.requireNonNull(gesuch.getGesuchsteller1());
				if (gesuch.getGesuchsteller1()
					.getEinkommensverschlechterungContainer()
					== null) {
					return false;
				}
				if (isEKVFuerBasisJahrPlus1Incomplete(gesuch)) {
					return false;
				}
			}
			if (hasEkvFuerBasisjahrPlus2(
				gesuch.getEinkommensverschlechterungInfoContainer()
			)) {
				Objects.requireNonNull(gesuch.getGesuchsteller1());
				return gesuch.getGesuchsteller1()
					.getEinkommensverschlechterungContainer()
					!= null
					&& isEKVFuerBasisJahrPlus2Complete(gesuch);
			}
		}
		// EKV is not activated
		return true;
	}

	private static boolean hasEkvFuerBasisjahrPlus2(
		EinkommensverschlechterungInfoContainer ekvInfoContainer
	) {
		return Boolean.TRUE.equals(
			ekvInfoContainer
				.getEinkommensverschlechterungInfoJA()
				.getEkvFuerBasisJahrPlus2()
		);
	}

	private static boolean hasEinkommensverschlechterung(
		EinkommensverschlechterungInfoContainer ekvInfoContainer
	) {
		return Boolean.TRUE.equals(
			ekvInfoContainer
				.getEinkommensverschlechterungInfoJA()
				.getEinkommensverschlechterung()
		);
	}

	private static boolean hasEkvFuerBasisJahrPlus1(
		EinkommensverschlechterungInfoContainer ekvInfoContainer
	) {
		return Boolean.TRUE.equals(
			ekvInfoContainer
				.getEinkommensverschlechterungInfoJA()
				.getEkvFuerBasisJahrPlus1()
		);
	}

	private boolean isEKVFuerBasisJahrPlus2Complete(@Nonnull Gesuch gesuch) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(
			gesuch.getGesuchsteller1()
				.getEinkommensverschlechterungContainer()
		);
		return isEKVFuerJahrComplete(
			gesuch,
			gesuch.getGesuchsteller1()
				.getEinkommensverschlechterungContainer()
				.getEkvJABasisJahrPlus2(),
			gesuch.getGesuchsteller2() != null
				&& gesuch.getGesuchsteller2()
					.getEinkommensverschlechterungContainer()
					!= null ?
						gesuch.getGesuchsteller2()
							.getEinkommensverschlechterungContainer()
							.getEkvJABasisJahrPlus2() :
						null
		);
	}

	private boolean isEKVFuerBasisJahrPlus1Incomplete(@Nonnull Gesuch gesuch) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(
			gesuch.getGesuchsteller1()
				.getEinkommensverschlechterungContainer()
		);
		return !isEKVFuerJahrComplete(
			gesuch,
			gesuch.getGesuchsteller1()
				.getEinkommensverschlechterungContainer()
				.getEkvJABasisJahrPlus1(),
			gesuch.getGesuchsteller2() != null
				&& gesuch.getGesuchsteller2()
					.getEinkommensverschlechterungContainer()
					!= null ?
						gesuch.getGesuchsteller2()
							.getEinkommensverschlechterungContainer()
							.getEkvJABasisJahrPlus1() :
						null
		);
	}

	private boolean isEKVFuerJahrComplete(
		Gesuch gesuch,
		@Nullable Einkommensverschlechterung einkommensverschlechterungGS1,
		@Nullable Einkommensverschlechterung einkommensverschlechterungGS2
	) {
		final FinanzielleSituationValidator validator =
			new FinanzielleSituationValidatorVisitor().accept(
				gesuch.getFinSitTyp()
			);
		if (einkommensverschlechterungGS1 == null
			|| !validator.isEinkommensverschlechterungComplete(
				einkommensverschlechterungGS1,
				gesuch
			)) {
			return false;
		}
		if (validator.doesFinSitRequireOneGS(gesuch)) {
			return true;
		}
		return einkommensverschlechterungGS2 == null
			|| validator.isEinkommensverschlechterungComplete(
				einkommensverschlechterungGS2,
				gesuch
			);
	}

	private boolean isFinanzielleSituationUnvollstaendig(
		@Nullable FinanzielleSituation finanzielleSituation,
		FinanzielleSituationTyp finSitTyp,
		@Nonnull Gesuch gesuch
	) {
		FinanzielleSituationValidator validator =
			new FinanzielleSituationValidatorVisitor().accept(finSitTyp);
		return finanzielleSituation == null
			|| !validator.isFinanzielleSituationComplete(
				finanzielleSituation,
				gesuch
			);
	}

	private boolean isEKVSchwyzIntroducedAndComplete(
		EinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainer,
		Gesuch gesuch
	) {
		FinanzielleSituationValidatorSZ validator =
			new FinanzielleSituationValidatorSZ();
		boolean hasEKV =
			einkommensverschlechterungInfoContainer
				.getEinkommensverschlechterungInfoJA()
				.getEkvFuerBasisJahrPlus1();

		if (!hasEKV) {
			return true;
		}

		final Familiensituation familiensituation = gesuch
			.extractFamiliensituation();
		Objects.requireNonNull(familiensituation);

		if (validator.doesFinSitRequireOneGS(gesuch)) {
			return isEKVSchwyzGSVollstaendig(
				gesuch.getGesuchsteller1(),
				gesuch
			);
		}

		return isEKVSchwyzGSVollstaendig(gesuch.getGesuchsteller1(), gesuch)
			&& isEKVSchwyzGSVollstaendig(gesuch.getGesuchsteller2(), gesuch);

	}

	private boolean isEKVSchwyzGSVollstaendig(
		@Nullable GesuchstellerContainer gesuchstellerContainer,
		Gesuch gesuch
	) {
		FinanzielleSituationValidatorSZ validator =
			new FinanzielleSituationValidatorSZ();
		if (gesuchstellerContainer == null) {
			return false;
		}
		var ekvGS1 = gesuchstellerContainer
			.getEinkommensverschlechterungContainer();
		if (ekvGS1 == null || ekvGS1.getEkvJABasisJahrPlus1() == null) {
			return false;
		}
		return validator.isEinkommensverschlechterungComplete(
			ekvGS1.getEkvJABasisJahrPlus1(),
			gesuch
		);
	}

	public boolean hasFamilienSituationNeueVeranlagungsstandZuAbholen(
		Gesuch gesuch
	) {
		boolean neueVeranlagungsstandZuAbholen = false;
		if (gesuch.getGesuchsteller1() != null
			&&
			gesuch.getGesuchsteller1().getFinanzielleSituationContainer()
				!= null
			&&
			gesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				!= null
			&&
			gesuch.getGesuchsteller1()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getSteuerdatenAbfrageStatus()
				== SteuerdatenAnfrageStatus.NEUE_VERANLAGUNG) {
			neueVeranlagungsstandZuAbholen = true;

		}
		if (!neueVeranlagungsstandZuAbholen
			&& gesuch.getGesuchsteller2() != null
			&& gesuch.getGesuchsteller2().getFinanzielleSituationContainer()
				!= null
			&& gesuch.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				!= null
			&&
			gesuch.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getSteuerdatenAbfrageStatus()
				== SteuerdatenAnfrageStatus.NEUE_VERANLAGUNG) {
			neueVeranlagungsstandZuAbholen = true;
		}

		return neueVeranlagungsstandZuAbholen;
	}
}
