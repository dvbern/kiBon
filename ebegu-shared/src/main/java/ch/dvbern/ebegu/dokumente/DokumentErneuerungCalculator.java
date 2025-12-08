package ch.dvbern.ebegu.dokumente;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.DokumentTyp;

public final class DokumentErneuerungCalculator {

	public static List<DokumentGrund> calculateErneuerbareGrunde(
		Collection<DokumentGrund> dokumentGrundeVorjahr,
		List<DokumentTyp> allowedDokumentTypsForErneuerung,
		Set<DokumentGrund> uploadableDokumenteCurrentGesuch,
		Gesuch gesuch,
		Gesuch vorjahrGesuch
	) {
		return dokumentGrundeVorjahr.stream()
			.filter(
				potentialGrundFromVorjahr -> allowedDokumentTypsForErneuerung
					.contains(potentialGrundFromVorjahr.getDokumentTyp())
			)
			.filter(
				potentialGrundFromVorjahr -> isInGrundList(
					potentialGrundFromVorjahr,
					uploadableDokumenteCurrentGesuch
				)
			)
			.filter(
				potentialGrundFromVorjahr -> isSamePersonsOrNotPersonenbezogen(
					potentialGrundFromVorjahr,
					gesuch,
					vorjahrGesuch
				)
			)
			.toList();
	}

	public static boolean isInGrundList(
		DokumentGrund grund,
		Collection<DokumentGrund> uploadableDokumenteCurrentGesuch
	) {
		var adaptedGrundTyp = adaptToNewPeriode(grund.getDokumentTyp());
		return uploadableDokumenteCurrentGesuch.stream()
			.anyMatch(
				neededGrund -> adaptedGrundTyp != null
					&& adaptedGrundTyp
						.equals(neededGrund.getDokumentTyp())
					&& grund.getDokumentGrundTyp()
						.equals(neededGrund.getDokumentGrundTyp())
					&& Objects.equals(grund.getTag(), neededGrund.getTag())
					&& Objects.equals(
						grund.getPersonNumber(),
						neededGrund.getPersonNumber()
					)
			);
	}

	public static boolean isSamePersonsOrNotPersonenbezogen(
		DokumentGrund potentialGrundFromVorjahr,
		Gesuch gesuch,
		Gesuch vorjahrGesuch
	) {
		if (potentialGrundFromVorjahr.getPersonNumber() == null) {
			return true;
		}
		return potentialGrundFromVorjahr.getPersonNumber() == 1 ?
			isSamePerson(
				gesuch.getGesuchsteller1(),
				vorjahrGesuch.getGesuchsteller1()
			) :
			isSamePerson(
				gesuch.getGesuchsteller2(),
				vorjahrGesuch.getGesuchsteller2()
			);
	}

	private static boolean isSamePerson(
		GesuchstellerContainer gsContainer,
		GesuchstellerContainer previousGSContainer
	) {
		if (gsContainer == null && previousGSContainer == null) {
			return true;
		}
		if (gsContainer == null || previousGSContainer == null) {
			return false;
		}

		return isSamePerson(
			gsContainer.getGesuchstellerJA(),
			previousGSContainer.getGesuchstellerJA()
		);
	}

	private static boolean isSamePerson(
		Gesuchsteller gs,
		Gesuchsteller previousGS
	) {
		if (gs == null && previousGS == null) {
			return true;
		}
		if (gs == null || previousGS == null) {
			return false;
		}
		return gs.getFullName().equals(previousGS.getFullName())
			&& gs.getGeburtsdatum().equals(previousGS.getGeburtsdatum());
	}

	// Documents XYYearMinus2 were XYYearMinus1 in previous periode since its one year earlier
	@Nullable
	public static DokumentTyp adaptToNewPeriode(DokumentTyp dokumentTyp) {
		switch (dokumentTyp) {
		case ERFOLGSRECHNUNGEN_JAHR -> {
			return DokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS1;
		}
		case ERFOLGSRECHNUNGEN_JAHR_MINUS1 -> {
			return DokumentTyp.ERFOLGSRECHNUNGEN_JAHR_MINUS2;
		}
		case NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR -> {
			return DokumentTyp.NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS1;
		}
		case NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS1 -> {
			return DokumentTyp.NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS2;
		}
		case NACHWEIS_ERSATZINKOMMEN_SELBSTSTAENDIGKEIT_JAHR_MINUS2,
			ERFOLGSRECHNUNGEN_JAHR_MINUS2 -> {
			return null;
		}
		default -> {
			return dokumentTyp;
		}
		}
	}

}
