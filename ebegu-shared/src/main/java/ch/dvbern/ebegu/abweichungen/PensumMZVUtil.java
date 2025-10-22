package ch.dvbern.ebegu.abweichungen;

import java.math.BigDecimal;
import java.util.List;

import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.DateUtil;

public abstract class PensumMZVUtil {

	private PensumMZVUtil() {
	}

	public static PensumMZVAddition mergeBetreuungspensen(
		DateRange gueltigkeit,
		List<Betreuungspensum> betreuungspensen
	) {
		PensumMZVAddition result = new PensumMZVAddition();

		betreuungspensen.forEach(betreuungspensum -> {
			var von = betreuungspensum.getGueltigkeit()
				.getGueltigAb()
				.isBefore(gueltigkeit.getGueltigAb()) ?
					gueltigkeit.getGueltigAb() :
					betreuungspensum.getGueltigkeit().getGueltigAb();
			var bis = betreuungspensum.getGueltigkeit()
				.getGueltigBis()
				.isAfter(gueltigkeit.getGueltigBis()) ?
					gueltigkeit.getGueltigBis() :
					betreuungspensum.getGueltigkeit().getGueltigBis();
			BigDecimal anteil = DateUtil.calculateAnteilMonatInklWeekend(
				von,
				bis
			);

			result.setAnteil(result.getAnteil().add(anteil));
			result.setAnzahlHauptmahlzeiten(
				result.getAnzahlHauptmahlzeiten()
					.add(
						betreuungspensum.getMonatlicheHauptmahlzeiten()
							.multiply(anteil)
					)
			);
			result.setAnzahlNebenmahlzeiten(
				result.getAnzahlNebenmahlzeiten()
					.add(
						betreuungspensum.getMonatlicheNebenmahlzeiten()
							.multiply(anteil)
					)
			);
			result.setTarifHauptmahlzeiten(
				result.getTarifHauptmahlzeiten()
					.add(
						betreuungspensum.getTarifProHauptmahlzeit()
							.multiply(anteil)
					)
			);
			result.setTarifNebenmahlzeiten(
				result.getTarifNebenmahlzeiten()
					.add(
						betreuungspensum.getTarifProNebenmahlzeit()
							.multiply(anteil)
					)
			);
		});

		return result;
	}
}
