package ch.dvbern.ebegu.batch;

import jakarta.annotation.Nonnull;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.services.gemeindeantrag.GemeindeKennzahlenMailService;
import ch.dvbern.ebegu.services.gemeindeantrag.GemeindeKennzahlenService;
import lombok.extern.log4j.Log4j;

@Stateless
@RolesAllowed(UserRoleName.SUPER_ADMIN)
@Log4j
public class YearlyBatchService {
	@Inject
	GemeindeKennzahlenService gemeindeKennzahlenService;

	@Inject
	GemeindeKennzahlenMailService gemeindeKennzahlenMailService;

	@Inject
	ApplicationPropertyService applicationPropertyService;

	public GemeindeKennzahlenBatchJobResult createGemeindeKennzahlenForCurrentGPForAllActiveGemeindenAndSendReminder(
		@Nonnull Mandant mandant
	) {
		if (isGemeindeKennzahlenDeactivated(mandant)) {
			return GemeindeKennzahlenBatchJobResult.NOT_ACTIVATED;
		}
		gemeindeKennzahlenService
			.createGemeindeKennzahlenInCurrentGPForActiveGemeinden(
				mandant
			);

		if (isGemeindeKennzahlenReminderDeactivated(mandant)) {
			return GemeindeKennzahlenBatchJobResult.CREATED_REMINDER_NOT_SENT;
		}

		gemeindeKennzahlenMailService
			.sendFirstErinnerungsmailToAllAdminBGOfMandant(
				mandant
			);
		return GemeindeKennzahlenBatchJobResult.SUCCESS;
	}

	public GemeindeKennzahlenBatchJobResult sendGemeindeKennzahlenSecondReminder(
		@Nonnull Mandant mandant
	) {
		if (isGemeindeKennzahlenDeactivated(mandant)) {
			return GemeindeKennzahlenBatchJobResult.NOT_ACTIVATED;
		}
		if (isGemeindeKennzahlenReminderDeactivated(mandant)) {
			return GemeindeKennzahlenBatchJobResult.REMINDER_NOT_ACTIVATED;
		}

		gemeindeKennzahlenMailService
			.sendSecondErinnerungsmailToAllAdminBGOfMandant(
				mandant
			);
		return GemeindeKennzahlenBatchJobResult.SUCCESS;
	}

	private boolean isGemeindeKennzahlenReminderDeactivated(Mandant mandant) {
		return isGemeindeKennzahlenDeactivated(mandant)
			||
			!applicationPropertyService.isReminderGemeindeKennzahlenAktiviert(
				mandant
			);
	}

	private boolean isGemeindeKennzahlenDeactivated(Mandant mandant) {
		return !applicationPropertyService.isGemeindeKennzahlenAktiviert(
			mandant
		);
	}
}
