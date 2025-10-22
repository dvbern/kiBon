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

package ch.dvbern.ebegu.rules.familienabzug;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.types.DateRange;

@SuppressWarnings("MethodParameterNamingConvention")
public class KinderabzugAbschnittRuleFKJV extends
	AbstractKinderabzugAbschnittRule {

	public KinderabzugAbschnittRuleFKJV(
		DateRange validityPeriod,
		@Nonnull Locale locale
	) {
		super(validityPeriod, locale);
	}

	@Override
	protected Kinderabzug calculateKinderAbzug(
		KindContainer kindContainer,
		LocalDate stichtag
	) {
		Familiensituation familiensituation = kindContainer.getGesuch()
			.extractFamiliensituation();
		Objects.requireNonNull(familiensituation);

		return calculateKinderabzugForKind(
			kindContainer.getKindJA(),
			familiensituation,
			stichtag
		);
	}

	private Kinderabzug calculateKinderabzugForKind(
		@Nonnull Kind kind,
		Familiensituation familiensituation,
		LocalDate dateToCompare
	) {
		if (kind.getPflegekind()) {
			Objects.requireNonNull(kind.getPflegeEntschaedigungErhalten());
			if (kind.getPflegeEntschaedigungErhalten()) {
				return Kinderabzug.KEIN_ABZUG;
			}
			return Kinderabzug.GANZER_ABZUG;
		}
		if (kind.getObhutAlternierendAusueben() != null) {
			if (!kind.getObhutAlternierendAusueben()) {
				return Kinderabzug.GANZER_ABZUG;
			}
			return calculateKinderabzugForObhutAlternierendAusueben(
				kind,
				familiensituation,
				dateToCompare
			);
		}
		if (kind.getInErstausbildung() != null) {
			return calculateKinderAbzugForInErstausbildungAnswered(
				kind,
				dateToCompare
			);
		}
		throw new EbeguRuntimeException(
			"calculateFKJVKinderabzugForKind",
			"wrong properties for kind to calculate kinderabzug"
		);
	}

	private Kinderabzug calculateKinderAbzugForInErstausbildungAnswered(
		@Nonnull Kind kind,
		LocalDate dateToCompare
	)
		throws EbeguRuntimeException {
		Objects.requireNonNull(kind.getInErstausbildung());
		if (!kind.getInErstausbildung()) {
			return is18GeburtstagBeforeDate(kind, dateToCompare) ?
				Kinderabzug.KEIN_ABZUG :
				Kinderabzug.GANZER_ABZUG;
		}
		if (kind.getAlimenteBezahlen() != null) {
			if (!kind.getAlimenteBezahlen()) {
				return Kinderabzug.KEIN_ABZUG;
			}
			return is18GeburtstagBeforeDate(kind, dateToCompare) ?
				Kinderabzug.GANZER_ABZUG :
				Kinderabzug.KEIN_ABZUG;
		}
		if (kind.getAlimenteErhalten() != null) {
			if (kind.getAlimenteErhalten()) {
				return is18GeburtstagBeforeDate(kind, dateToCompare) ?
					Kinderabzug.KEIN_ABZUG :
					Kinderabzug.GANZER_ABZUG;
			}
			return Kinderabzug.GANZER_ABZUG;
		}
		throw new EbeguRuntimeException(
			"calculateFKJVKinderabzugForKind",
			"wrong properties for kind to calculate kinderabzug"
		);
	}

	private boolean is18GeburtstagBeforeDate(
		@Nonnull Kind kind,
		@Nonnull LocalDate date
	) {
		LocalDate dateWith18 = kind.getGeburtsdatum().plusYears(18);
		return dateWith18.isBefore(date);
	}

	private Kinderabzug calculateKinderabzugForObhutAlternierendAusueben(
		Kind kind,
		Familiensituation familiensituation,
		LocalDate dateToCompare
	) {
		Objects.requireNonNull(kind.getFamilienErgaenzendeBetreuung());
		Objects.requireNonNull(familiensituation);

		if (!kind.getFamilienErgaenzendeBetreuung()) {
			if (Boolean.TRUE.equals(kind.getGemeinsamesGesuch())) {
				return Kinderabzug.GANZER_ABZUG;
			}
			return Kinderabzug.HALBER_ABZUG;
		}

		if (isVerheiratetOrKonkubinatMitKind(
			familiensituation.getFamilienstatus()
		)
			|| isMinDauerKonkubinatErreicht(
				familiensituation,
				dateToCompare
			)
			|| familiensituation.getGesuchstellerKardinalitaet()
				== EnumGesuchstellerKardinalitaet.ALLEINE
			|| (familiensituation.getUnterhaltsvereinbarung() != null
				&& !UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG
					.equals(
						familiensituation
							.getUnterhaltsvereinbarung()
					))) {
			return Kinderabzug.HALBER_ABZUG;
		}

		Objects.requireNonNull(kind.getGemeinsamesGesuch());
		if (kind.getGemeinsamesGesuch()) {
			return Kinderabzug.GANZER_ABZUG;
		}

		return Kinderabzug.HALBER_ABZUG;
	}

	private boolean isVerheiratetOrKonkubinatMitKind(
		EnumFamilienstatus familienstatus
	) {
		return familienstatus == EnumFamilienstatus.VERHEIRATET
			||
			familienstatus == EnumFamilienstatus.KONKUBINAT;
	}

	public boolean isMinDauerKonkubinatErreicht(
		Familiensituation familiensituation,
		LocalDate dateToCompare
	) {

		if (EnumFamilienstatus.KONKUBINAT_KEIN_KIND
			!= familiensituation.getFamilienstatus()) {
			return false;
		}

		return isKonkubinatMinReached(familiensituation, dateToCompare);
	}

	private boolean isKonkubinatMinReached(
		Familiensituation familiensituation,
		LocalDate dateToCompare
	) {
		assert EnumFamilienstatus.KONKUBINAT_KEIN_KIND
			== familiensituation.getFamilienstatus();
		assert familiensituation.getStartKonkubinat() != null;

		LocalDate dateKonkubinatMinDauerReached = familiensituation
			.getStartKonkubinat()
			.plusYears(familiensituation.getMinDauerKonkubinat());
		//min date is reached when datetocompare isAfter dateKonkubinatMinDauerReached
		return dateToCompare.isAfter(dateKonkubinatMinDauerReached);
	}
}
