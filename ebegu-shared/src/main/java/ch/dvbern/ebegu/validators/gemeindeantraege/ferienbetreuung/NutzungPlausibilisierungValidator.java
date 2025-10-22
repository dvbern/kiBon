package ch.dvbern.ebegu.validators.gemeindeantraege.ferienbetreuung;

import java.math.BigDecimal;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenNutzung;

public class NutzungPlausibilisierungValidator implements
	ConstraintValidator<NutzungPlausibilisierung, FerienbetreuungAngabenNutzung> {

	@Override
	public boolean isValid(
		FerienbetreuungAngabenNutzung nutzung,
		ConstraintValidatorContext constraintValidatorContext
	) {
		BigDecimal ersterZyklus = nutzung.getAnzahlBetreuteKinder1Zyklus()
			== null ?
				BigDecimal.ZERO :
				nutzung.getAnzahlBetreuteKinder1Zyklus();
		BigDecimal zweiterZyklus = nutzung.getAnzahlBetreuteKinder2Zyklus()
			== null ?
				BigDecimal.ZERO :
				nutzung.getAnzahlBetreuteKinder2Zyklus();
		BigDecimal dritterZyklus = nutzung.getAnzahlBetreuteKinder3Zyklus()
			== null ?
				BigDecimal.ZERO :
				nutzung.getAnzahlBetreuteKinder3Zyklus();

		BigDecimal total = nutzung.getAnzahlBetreuteKinder() == null ?
			BigDecimal.ZERO :
			nutzung.getAnzahlBetreuteKinder();
		BigDecimal sum = ersterZyklus.add(zweiterZyklus).add(dritterZyklus);

		return total.compareTo(
			sum
		) == 0;
	}

}
