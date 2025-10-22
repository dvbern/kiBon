package ch.dvbern.ebegu.services.authentication;

import javax.annotation.Nullable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.HasMandant;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;

import static ch.dvbern.ebegu.services.authentication.util.AuthorizationViolationUtil.throwMandantViolation;

@RequestScoped
public class MandantAuthorizer {

	@Inject
	private PrincipalBean principalBean;

	public void checkMandantMatches(@Nullable HasMandant mandantEntity) {
		if (mandantEntity == null) {
			throw new EbeguRuntimeException(
				"checkMandantMatches",
				"mandantEntity not defined"
			);
		}
		if (!isMandantMatching(mandantEntity)) {
			throwMandantViolation(mandantEntity, principalBean.getPrincipal());
		}
	}

	public boolean isMandantMatching(@Nullable HasMandant mandantEntity) {
		// we allow reading anyway if user is coming from Superadmin Service
		if (principalBean.isKibonServiceAccount()) {
			return true;
		}
		if (mandantEntity == null || mandantEntity.getMandant() == null) {
			return false;
		}
		Mandant mandant = mandantEntity.getMandant();

		if (principalBean.isKibonBenutzer()) {
			return mandant.getId()
				.equals(principalBean.getMandantUuidFromToken());
		}

		return mandant.equals(principalBean.getMandant());
	}

}
