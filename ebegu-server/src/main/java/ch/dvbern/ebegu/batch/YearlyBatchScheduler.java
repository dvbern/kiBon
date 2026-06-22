package ch.dvbern.ebegu.batch;

import jakarta.annotation.security.RunAs;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.services.MandantService;
import lombok.extern.log4j.Log4j;
import org.jboss.ejb3.annotation.RunAsPrincipal;

@Startup
@Singleton
@RunAs(UserRoleName.SUPER_ADMIN)
@RunAsPrincipal(PrincipalBean.KIBON_SERVICE_ACCOUNT)
@Log4j
public class YearlyBatchScheduler {

	@Inject
	private YearlyBatchService yearlyBatchService;
	@Inject
	MandantService mandantService;

	@Schedule(dayOfMonth = "15", month = "9")
	public void createGemeindeKennzahlenForCurrentGPForAllActiveGemeindenAndSendReminder() {
		LOG.info(
			"Batchjob createGemeindeKennzahlenForCurrentGPForAllActiveGemeindenAndSendReminder started"
		);
		mandantService.getAll()
			.forEach(
				mandant -> yearlyBatchService
					.createGemeindeKennzahlenForCurrentGPForAllActiveGemeindenAndSendReminder(
						mandant
					)
			);
		LOG.info(
			"Batchjob createGemeindeKennzahlenForCurrentGPForAllActiveGemeindenAndSendReminder finished"
		);
	}

	@Schedule(dayOfMonth = "16", month = "10")
	public void sendGemeindeKennzahlenSecondReminder() {
		LOG.info("Batchjob sendGemeindeKennzahlenSecondReminder started");
		mandantService.getAll()
			.forEach(
				mandant -> yearlyBatchService
					.sendGemeindeKennzahlenSecondReminder(mandant)
			);
		LOG.info("Batchjob sendGemeindeKennzahlenSecondReminder finished");
	}

}
