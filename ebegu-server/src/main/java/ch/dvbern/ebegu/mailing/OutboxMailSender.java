package ch.dvbern.ebegu.mailing;

import jakarta.annotation.security.RunAs;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.enums.UserRoleName;
import ch.dvbern.ebegu.errors.MailException;
import org.jboss.ejb3.annotation.RunAsPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@RunAs(UserRoleName.SUPER_ADMIN)
@RunAsPrincipal(PrincipalBean.KIBON_SERVICE_ACCOUNT)
public class OutboxMailSender {

	private static final Logger LOG = LoggerFactory.getLogger(
		OutboxMailSender.class
	);

	@Inject
	OutboxMailService outboxMailService;

	@Inject
	MailSendingService mailService;

	@Schedule(
		info = "send next batch of outbox mails",
		minute = "*",
		hour = "*",
		second = "0",
		persistent = true
	)
	public void sendOutboxMails() {
		outboxMailService.getNextN(50).forEach(mail -> {
			try {
				mailService.sendMail(mail);
			} catch (MailException e) {
				LOG.error(
					"Failed sending mail id={}",
					mail.getId(),
					e.getCause()
				);
				mailService.incrementRetry(mail);
			}
		});
	}
}
