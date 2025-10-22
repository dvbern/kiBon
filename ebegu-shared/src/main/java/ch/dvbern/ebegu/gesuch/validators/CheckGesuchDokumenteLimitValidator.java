package ch.dvbern.ebegu.gesuch.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Gesuch;

public class CheckGesuchDokumenteLimitValidator implements
	ConstraintValidator<CheckGesuchDokumenteLimit, Gesuch> {
	@Override
	public boolean isValid(Gesuch gesuch, ConstraintValidatorContext context) {
		if (gesuch.getDokumentGrunds() == null) {
			return true;
		}
		return gesuch.getDokumentGrunds()
			.stream()
			.mapToLong(dokumentGrund -> dokumentGrund.getDokumente().size())
			.sum()
			<= 150;
	}
}
