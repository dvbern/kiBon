package ch.dvbern.ebegu.batch.jobs.lastenausgleich;

import jakarta.batch.api.AbstractBatchlet;
import jakarta.batch.runtime.BatchStatus;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import ch.dvbern.ebegu.lastenausgleich.LastenausgleichCalculationService;

@Named("lastenausgleichTotalCalculationBatchlet")
@Dependent
public class LastenausgleichTotalCalculationBatchletStep
	extends
	AbstractBatchlet {

	@Inject
	private LastenausgleichCalculationService lastenausgleichCalculationService;

	@Inject
	private LastenausgleichBatchletContext lastenausgleichBatchletContext;

	@Override
	public String process() throws Exception {
		String lastenausgleichId =
			lastenausgleichBatchletContext.getLastenausgleichIdFromContext();
		lastenausgleichCalculationService.calculateTotals(
			lastenausgleichId
		);
		return BatchStatus.COMPLETED.toString();
	}
}
