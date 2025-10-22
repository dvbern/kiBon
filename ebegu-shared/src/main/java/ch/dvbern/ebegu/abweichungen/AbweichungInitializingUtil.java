package ch.dvbern.ebegu.abweichungen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumAbweichung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.enums.betreuung.BetreuungspensumAbweichungStatus;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.MathUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AbweichungInitializingUtil {

	private AbweichungInitializingUtil() {
	}

	public static List<BetreuungspensumAbweichung> fillAbweichungen(
		@Nonnull BigDecimal multiplier,
		@Nonnull Betreuung betreuung
	) {
		List<BetreuungspensumAbweichung> initialAbweichungen = initAbweichungen(
			multiplier,
			betreuung
		);

		for (BetreuungspensumAbweichung abweichung : initialAbweichungen) {
			extractOriginalPensum(
				betreuung.getBetreuungspensumContainers(),
				abweichung
			);
		}
		return initialAbweichungen;
	}

	private static BetreuungspensumAbweichung extractOriginalPensum(
		Set<BetreuungspensumContainer> pensen,
		BetreuungspensumAbweichung abweichung
	) {

		LocalDate abweichungVon = abweichung.getGueltigkeit().getGueltigAb();
		LocalDate abweichungBis = abweichung.getGueltigkeit().getGueltigBis();

		var matchingPensen = pensen.stream()
			.map(BetreuungspensumContainer::getBetreuungspensumJA)
			.filter(pensum -> {
				LocalDate von = pensum.getGueltigkeit().getGueltigAb();
				LocalDate bis = pensum.getGueltigkeit().getGueltigBis();

				return ((von.isBefore(abweichungVon)
					|| DateUtil.isSameMonthAndYear(von, abweichungVon))
					&& (bis.isAfter(abweichungBis)
						|| DateUtil.isSameMonthAndYear(
							bis,
							abweichungBis
						)));
			})
			.toList();

		for (var pensum : matchingPensen) {
			LocalDate von = pensum.getGueltigkeit().getGueltigAb();
			LocalDate bis = pensum.getGueltigkeit().getGueltigBis();
			setAbweichungDatenFromPensum(
				abweichung,
				abweichungVon,
				abweichungBis,
				pensum,
				von,
				bis
			);
		}
		setMvZAbweichungDatenFromPensum(abweichung, matchingPensen);
		return abweichung;
	}

	private static void setAbweichungDatenFromPensum(
		BetreuungspensumAbweichung abweichung,
		LocalDate abweichungVon,
		LocalDate abweichungBis,
		Betreuungspensum pensum,
		LocalDate von,
		LocalDate bis
	) {
		if (von.isBefore(abweichungVon)) {
			von = abweichungVon;
		}

		if (bis.isAfter(abweichungBis)) {
			bis = abweichungBis;
		}
		BigDecimal anteil = DateUtil.calculateAnteilMonatInklWeekend(von, bis);
		abweichung.addPensum(pensum.getPensum().multiply(anteil));
		abweichung.addKosten(
			pensum.getMonatlicheBetreuungskosten().multiply(anteil)
		);
		abweichung.setStuendlicheVollkosten(pensum.getStuendlicheVollkosten());
		abweichung.setVertraglicheBetreuuteTage(pensum.getBetreuteTage());

		if (pensum.getEingewoehnung() != null
			&& DateUtil.isSameMonthAndYear(
				pensum.getGueltigkeit().getGueltigAb(),
				abweichungVon
			)) {
			abweichung.addEingewoehnung(pensum.getEingewoehnung());
		}
	}

	// initiate an empty BetreuungspensumAbweichung for every month within the Gesuchsperiode
	private static List<BetreuungspensumAbweichung> initAbweichungen(
		@Nonnull BigDecimal multiplier,
		@Nonnull Betreuung betreuung
	) {
		Gesuchsperiode gp = betreuung.extractGesuchsperiode();
		LocalDate from = gp.getGueltigkeit().getGueltigAb();
		LocalDate to = gp.getGueltigkeit().getGueltigBis();

		List<BetreuungspensumAbweichung> abweichungen = new ArrayList<>();
		Set<BetreuungspensumAbweichung> abweichungenFromDb = betreuung
			.getBetreuungspensumAbweichungen();

		while (from.isBefore(to)) {
			BetreuungspensumAbweichung abweichung;
			// check if we already stored something in the database
			if (!abweichungenFromDb.isEmpty()) {
				Optional<BetreuungspensumAbweichung> existing =
					searchExistingAbweichung(from, abweichungenFromDb);
				existing.ifPresent(
					AbweichungInitializingUtil::resetTransientAbweichungMZVFields
				);
				abweichung = existing.orElse(
					createEmptyAbweichung(
						from,
						betreuung.isAngebotTagesfamilien()
					)
				);
			} else {
				abweichung = createEmptyAbweichung(
					from,
					betreuung.isAngebotTagesfamilien()
				);
			}
			abweichung.setMultiplier(multiplier);
			abweichungen.add(abweichung);
			from = from.plusMonths(1);
		}

		return abweichungen;
	}

	private static void resetTransientAbweichungMZVFields(
		BetreuungspensumAbweichung abweichung
	) {
		abweichung.setVertraglicheHauptmahlzeiten(null);
		abweichung.setVertraglicheNebenmahlzeiten(null);
		abweichung.setVertraglicherTarifHauptmahlzeit(BigDecimal.ZERO);
		abweichung.setVertraglicherTarifNebenmahlzeit(BigDecimal.ZERO);
	}

	@SuppressFBWarnings(value = "NP_NONNULL_PARAM_VIOLATION",
		justification = "initially the affected fields need to "
			+ "be null, we want to force the user to enter data")
	private static BetreuungspensumAbweichung createEmptyAbweichung(
		@Nonnull LocalDate from,
		boolean isTagesfamilien
	) {
		BetreuungspensumAbweichung abweichung =
			new BetreuungspensumAbweichung();
		abweichung.setStatus(BetreuungspensumAbweichungStatus.NONE);
		// initially those fields need to be null, we want to force the user to enter data
		abweichung.setPensum(null);
		abweichung.setMonatlicheHauptmahlzeiten(null);
		abweichung.setMonatlicheNebenmahlzeiten(null);
		abweichung.setMonatlicheBetreuungskosten(null);
		YearMonth month = YearMonth.from(from);
		abweichung.setGueltigkeit(
			new DateRange(month.atDay(1), month.atEndOfMonth())
		);

		abweichung.setUnitForDisplay(PensumUnits.DAYS);
		if (isTagesfamilien) {
			abweichung.setUnitForDisplay(PensumUnits.HOURS);
		}
		return abweichung;
	}

	private static Optional<BetreuungspensumAbweichung> searchExistingAbweichung(
		@Nonnull LocalDate from,
		@Nonnull Set<BetreuungspensumAbweichung> abweichungenFromDb
	) {
		return abweichungenFromDb.stream()
			.filter(a -> a.getGueltigkeit().getGueltigAb().equals(from))
			.findFirst();
	}

	private static void setMvZAbweichungDatenFromPensum(
		BetreuungspensumAbweichung abweichung,
		List<Betreuungspensum> matchingPensen
	) {
		var added = PensumMZVUtil.mergeBetreuungspensen(
			abweichung.getGueltigkeit(),
			matchingPensen
		);
		if (added.getAnteil().compareTo(BigDecimal.ZERO) == 0) {
			abweichung.addHauptmahlzeiten(BigDecimal.ZERO);
			abweichung.addNebenmahlzeiten(BigDecimal.ZERO);
			abweichung.addTarifHaupt(BigDecimal.ZERO);
			abweichung.addTarifNeben(BigDecimal.ZERO);
			return;
		}
		abweichung.addHauptmahlzeiten(added.getAnzahlHauptmahlzeiten());
		abweichung.addNebenmahlzeiten(added.getAnzahlNebenmahlzeiten());
		abweichung.addTarifHaupt(
			MathUtil.DEFAULT.divideNullSafe(
				added.getTarifHauptmahlzeiten(),
				added.getAnteil()
			)
		);
		abweichung.addTarifNeben(
			MathUtil.DEFAULT.divideNullSafe(
				added.getTarifNebenmahlzeiten(),
				added.getAnteil()
			)
		);
	}

}
