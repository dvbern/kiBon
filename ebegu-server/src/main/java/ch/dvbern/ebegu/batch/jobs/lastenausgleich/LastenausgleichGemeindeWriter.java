package ch.dvbern.ebegu.batch.jobs.lastenausgleich;

import java.util.List;

import jakarta.batch.api.chunk.AbstractItemWriter;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.services.lastenausgleich.LastenausgleichServiceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("lastenausgleichGemeindeWriter")
@Dependent
public class LastenausgleichGemeindeWriter extends AbstractItemWriter {

	private static final Logger LOG = LoggerFactory.getLogger(
		LastenausgleichGemeindeWriter.class.getSimpleName()
	);

	@Inject
	private LastenausgleichBatchletContext lastenausgleichBatchletContext;

	@Inject
	private LastenausgleichServiceBean lastenausgleichService;

	@Override
	public void writeItems(List<Object> items) throws Exception {
		items.forEach(item -> {
			Lastenausgleich l = (Lastenausgleich) item;
			lastenausgleichService.saveLastenausgleich(l);
		});

		LOG.info(
			"Lastenausgleich für {} von {} Gemeinden berechnet.",
			lastenausgleichBatchletContext.getNumberOfGemeindenPorcessed(),
			lastenausgleichBatchletContext.getTotalNumberOfGemeindenToProcess()
		);
	}
}
