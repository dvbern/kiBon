package ch.dvbern.ebegu.validators.gemeindeantraege.ferienbetreuung;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NutzungPlausibilisierungValidator.class)
public @interface NutzungPlausibilisierung {
	String message() default "{fb_nutzung_plausibilisierung_failed}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
