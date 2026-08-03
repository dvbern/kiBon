package ch.dvbern.ebegu.batch.jobs.lastenausgleich;

import jakarta.batch.api.AbstractBatchlet;
import jakarta.batch.runtime.BatchStatus;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.services.MailService;
import ch.dvbern.ebegu.services.lastenausgleich.LastenausgleichServiceBean;

@Named("lastenausgleichMailBatchlet")
@Stateless
public class LastenausgleichMailBatchletStep
	extends
	AbstractBatchlet {

	@Inject
	private LastenausgleichServiceBean lastenausgleichService;

	@Inject
	private MailService mailService;

	@Inject
	private LastenausgleichBatchletContext lastenausgleichBatchletContext;

	@Override
	public String process() throws Exception {
		String lastenausgleichId =
			lastenausgleichBatchletContext.getLastenausgleichIdFromContext();
		sendEmailsToGemeinden(
			lastenausgleichId
		);
		return BatchStatus.COMPLETED.toString();
	}

	private void sendEmailsToGemeinden(String lastenausgleichId) {
		Lastenausgleich lastenausgleich = lastenausgleichService
			.findLastenausgleich(
				lastenausgleichId
			);

		lastenausgleich.getLastenausgleichDetails()
			.stream()
			.map(LastenausgleichDetail::getGemeinde)
			.distinct()
			.forEach(
				gemeinde -> mailService
					.prepareToSendInfoLastenausgleichGemeinde(
						gemeinde,
						lastenausgleich
					)
			);
	}
}
