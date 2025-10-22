package ch.dvbern.ebegu.services.authentication.util;

import java.security.Principal;
import java.util.Set;

import javax.annotation.Nonnull;
import jakarta.ejb.EJBAccessException;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.HasMandant;

public class AuthorizationViolationUtil {

	private AuthorizationViolationUtil() {
	}

	public static void throwMandantViolation(
		@Nonnull HasMandant mandantEntity,
		Principal principal
	) {
		throw new EJBAccessException(
			"Mandant Access Violation"
				+ " for Entity: "
				+ mandantEntity.getClass().getSimpleName()
				+ "(id="
				+ mandantEntity.getId()
				+ "):"
				+ " for current user: "
				+ principal
		);
	}

	public static void throwViolation(
		AbstractEntity abstractEntity,
		Principal principal,
		Set<String> roles
	) {
		throw new EJBAccessException(
			"Access Violation"
				+ " for Entity: "
				+ abstractEntity.getClass().getSimpleName()
				+ "(id="
				+ abstractEntity.getId()
				+ "):"
				+ " for current user: "
				+ principal
				+ " in role(s): "
				+ roles
				+ ", insertUser: "
				+ abstractEntity.getUserErstellt()
				+ '.'
				+ abstractEntity.getMessageForAccessException()
		);
	}
}
