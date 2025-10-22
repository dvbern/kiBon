package ch.dvbern.ebegu.outbox.anmeldung;

import jakarta.annotation.Resource;
import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionSynchronizationRegistry;

import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class AnmeldungTagesschuleEventAsyncHelper {

	private static final Logger LOG = LoggerFactory.getLogger(
		AnmeldungTagesschuleEventAsyncHelper.class
	);

	@Resource
	private TransactionSynchronizationRegistry txReg;

	@Inject
	private Persistence persistence;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private AnmeldungTagesschuleEventConverter anmeldungTagesschuleEventConverter;

	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void convert(String id) {
		AnmeldungTagesschule anmeldung = persistence.find(
			AnmeldungTagesschule.class,
			id
		);

		Mandant mandant = anmeldung.extractGesuch().extractMandant();

		if (!applicationPropertyService.isPublishSchnittstelleEventsAktiviert(
			mandant
		)) {
			return;
		}

		LOG.info(
			"Converting {} in Thread {} and Transaction {}",
			anmeldung.getReferenzNummer(),
			Thread.currentThread(),
			txReg.getTransactionKey()
		);

		this.event.fire(anmeldungTagesschuleEventConverter.of(anmeldung));
		anmeldung.setEventPublished(true);
		anmeldung.setSkipPreUpdate(true);
		persistence.merge(anmeldung);
	}
}
