package ch.dvbern.ebegu.batch.jobs.lastenausgleich;

import jakarta.batch.api.chunk.ItemProcessor;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.lastenausgleich.LastenausgleichCalculationService;

@Named("lastenausgleichGemeindeCalculator")
@Dependent
public class LastenausgleichGemeindeCalculator implements ItemProcessor {

	@Inject
	private LastenausgleichCalculationService lastenausgleichCalculationService;

	@Inject
	private LastenausgleichBatchletContext lastenausgleichBatchletContext;

	@Override
	public Lastenausgleich processItem(Object item) throws Exception {
		Gemeinde gemeinde = (Gemeinde) item;
		String lastenausgleichId =
			lastenausgleichBatchletContext.getLastenausgleichIdFromContext();
		return lastenausgleichCalculationService
			.calculateLastenausgleichForGemeinde(
				lastenausgleichId,
				gemeinde
			);
	}

}
