package ch.dvbern.ebegu.validators.gemeindeantraege.lats;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.gemeindeantrag.lastenausgleichtagesschulen.LastenausgleichTagesschuleAngabenInstitution;

public class AngabenTagesschulePlausibilisierungValidator implements
	ConstraintValidator<AngabenTagesschulePlausibilisierung, LastenausgleichTagesschuleAngabenInstitution> {

	@Override
	public boolean isValid(
		LastenausgleichTagesschuleAngabenInstitution angabenInstitution,
		ConstraintValidatorContext constraintValidatorContext
	) {
		BigDecimal total = angabenInstitution.getAnzahlEingeschriebeneKinder();
		if (total == null || notAllRequiredPresent(angabenInstitution)) {
			return false;
		}
		BigDecimal sum = Objects.requireNonNull(
			angabenInstitution
				.getAnzahlEingeschriebeneKinderKindergarten()
		)
			.add(
				angabenInstitution.getAnzahlEingeschriebeneKinderPrimarstufe()
			)
			.add(
				angabenInstitution.getAnzahlEingeschriebeneKinderSekundarstufe()
			)
			.add(
				angabenInstitution.getAnzahlEingeschriebeneKinderBasisstufe()
			);
		return total.compareTo(
			sum
		) == 0;
	}

	private static boolean notAllRequiredPresent(
		LastenausgleichTagesschuleAngabenInstitution angabenInstitution
	) {
		return angabenInstitution.getAnzahlEingeschriebeneKinderKindergarten()
			== null
			|| angabenInstitution.getAnzahlEingeschriebeneKinderPrimarstufe()
				== null
			|| angabenInstitution.getAnzahlEingeschriebeneKinderSekundarstufe()
				== null
			|| angabenInstitution.getAnzahlEingeschriebeneKinderBasisstufe()
				== null;
	}
}
