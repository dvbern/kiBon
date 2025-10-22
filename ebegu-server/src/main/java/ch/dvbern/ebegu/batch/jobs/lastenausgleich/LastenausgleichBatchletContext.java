package ch.dvbern.ebegu.batch.jobs.lastenausgleich;

import java.util.Properties;

import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.services.MandantService;

@Dependent
public class LastenausgleichBatchletContext {
	private static final String LASTENAUSGLEICH_ID_KEY = "Lastenausgleich_id";
	private static final String TOTAL_NUMBER_OF_GEMEINDE_TO_PROCESS_KEY =
		"gemeindeSize";
	private static final String NUMBER_OF_GEMEINDE_PROCESSED =
		"gemeindeProcessedElements";

	@Inject
	private JobContext jobCtx;

	@Inject
	private MandantService mandantService;

	public Mandant getMandantFromContext() {
		final String mandantId = getParameters()
			.getProperty(WorkJobConstants.REPORT_MANDANT_ID);
		return findMandant(mandantId);
	}

	public void setTotalNumberOfGemeindeToProcess(int size) {
		getParameters().setProperty(
			TOTAL_NUMBER_OF_GEMEINDE_TO_PROCESS_KEY,
			String.valueOf(size)
		);
	}

	public void setNumberOfGemeindeProcessed(int processedElements) {
		getParameters().setProperty(
			NUMBER_OF_GEMEINDE_PROCESSED,
			String.valueOf(processedElements)
		);
	}

	public String getTotalNumberOfGemeindenToProcess() {
		return getParameters().getProperty(
			TOTAL_NUMBER_OF_GEMEINDE_TO_PROCESS_KEY
		);
	}

	public String getNumberOfGemeindenPorcessed() {
		return getParameters().getProperty(NUMBER_OF_GEMEINDE_PROCESSED);
	}

	public boolean isLastenausgleichCreatedDuringBatchjob() {
		return getParameters().containsKey(LASTENAUSGLEICH_ID_KEY);
	}

	public boolean isLastenausgleichCalculationCompleted() {
		JobOperator operator = BatchRuntime.getJobOperator();
		return operator.getStepExecutions(jobCtx.getExecutionId())
			.stream()
			.anyMatch(
				stepExecution -> stepExecution.getBatchStatus()
					== BatchStatus.COMPLETED
					&& stepExecution.getStepName()
						.equals("calculateLastenausgleich")
			);
	}

	public String getLastenausgleichIdFromContext() {
		return getParameters().getProperty(LASTENAUSGLEICH_ID_KEY);
	}

	public void setLastenausgleichIdToContext(String lastenausgleichId) {
		getParameters().setProperty(LASTENAUSGLEICH_ID_KEY, lastenausgleichId);
	}

	public String getProperty(String propertyKey) {
		return getParameters().getProperty(propertyKey);
	}

	private Properties getParameters() {
		JobOperator operator = BatchRuntime.getJobOperator();
		return operator.getParameters(jobCtx.getExecutionId());
	}

	private Mandant findMandant(String mandantId) {
		return mandantService.getMandant(mandantId);
	}

}
