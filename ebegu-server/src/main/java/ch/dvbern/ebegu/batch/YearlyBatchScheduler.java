package ch.dvbern.ebegu.batch;

import jakarta.annotation.security.RunAs;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.gemeindeantrag.GemeindeKennzahlenMailService;
import lombok.extern.log4j.Log4j;
import org.jboss.ejb3.annotation.RunAsPrincipal;

@Startup
@Singleton
@RunAs(UserRoleName.SUPER_ADMIN)
@RunAsPrincipal(PrincipalBean.KIBON_SERVICE_ACCOUNT)
@Log4j
public class YearlyBatchScheduler {

	private static final String MANDANT_INFO_MAIL =
		"info.bg@be.ch";

	@Inject
	GemeindeKennzahlenMailService gemeindeKennzahlenMailService;

	@Inject
	ApplicationPropertyService applicationPropertyService;

	@Inject
	MandantService mandantService;

	@Schedule(dayOfMonth = "15", month = "9")
	public void sendGemeindeKennzahlenFirstReminder() {
		if (isGemeindeKennzahlenReminderDeactivated()) {
			LOG.info(
				"Batchjob sendGemeindeKennzahlenFirstReminder nicht durchgefuehrt, Einstellung ist nicht aktiviert"
			);
			return;
		}
		gemeindeKennzahlenMailService
			.sendFirstErinnerungsmailToAllAdminBGOfMandant(
				mandantService.getMandantBern(),
				MANDANT_INFO_MAIL
			);
		LOG.info(
			"Batchjob sendGemeindeKennzahlenFirstReminder durchgefuehrt"
		);
	}

	@Schedule(dayOfMonth = "16", month = "10")
	public void sendGemeindeKennzahlenSecondReminder() {
		if (isGemeindeKennzahlenReminderDeactivated()) {
			LOG.info(
				"Batchjob sendGemeindeKennzahlenSecondtReminder nicht durchgefuehrt, Einstellung ist nicht aktiviert"
			);
			return;
		}
		gemeindeKennzahlenMailService
			.sendSecondErinnerungsmailToAllAdminBGOfMandant(
				mandantService.getMandantBern(),
				MANDANT_INFO_MAIL
			);
		LOG.info(
			"Batchjob sendGemeindeKennzahlenSecondReminder durchgefuehrt"
		);
	}

	private boolean isGemeindeKennzahlenReminderDeactivated() {
		return !applicationPropertyService.isGemeindeKennzahlenAktiviert(
			mandantService.getMandantBern()
		)
			||
			!applicationPropertyService.isReminderGemeindeKennzahlenAktiviert(
				mandantService.getMandantBern()
			);
	}
}
