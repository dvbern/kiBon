package ch.dvbern.ebegu.services.zahlungen;

import java.util.Properties;

import javax.annotation.Nonnull;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Workjob;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.enums.reporting.BatchJobStatus;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.WorkjobService;

import static ch.dvbern.ebegu.enums.WorkJobConstants.LANGUAGE;

@Stateless
public class WorkjobZahlungslaufService {

	@Inject
	private Persistence persistence;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private WorkjobService workjobService;

	/**
	 * Start the Zahlaugslauf process
	 *
	 * @param zahlungslaufTyp the type of Zahlungslauf
	 * @param gemeindeId the id of the Gemeinde
	 * @param auszahlungInZukunft should the next month be paid in advance
	 * @param zahlungsauftragId id of the zahlungsauftrag
	 * @param workjob the workjob to save
	 * @return
	 */
	public Workjob startZahlungslaufWorkjob(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String gemeindeId,
		@Nonnull Boolean auszahlungInZukunft,
		@Nonnull String zahlungsauftragId,
		@Nonnull Workjob workjob
	) {
		JobOperator jobOperator = BatchRuntime.getJobOperator();
		final Properties jobParameters = buildJobParameter(
			zahlungslaufTyp,
			gemeindeId,
			auszahlungInZukunft,
			zahlungsauftragId
		);
		workjob.setStatus(BatchJobStatus.REQUESTED);
		workjob = workjobService.saveWorkjob(workjob);
		persistence.getEntityManager().flush();

		long jobId = jobOperator.start("zahlungslaufbatch", jobParameters);
		workjob.setExecutionId(jobId);
		workjob = workjobService.saveWorkjob(workjob);

		return workjob;
	}

	private Properties buildJobParameter(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String gemeindeId,
		@Nonnull Boolean auszahlungInZukunft,
		@Nonnull String zahlungsauftragId
	) {
		Properties jobParameters = new Properties();

		jobParameters.setProperty(
			WorkJobConstants.REPORT_MANDANT_ID,
			principalBean.getMandant().getId()
		);

		jobParameters.setProperty(
			WorkJobConstants.REPORT_MANDANT_IDENTIFIER,
			principalBean.getMandant().getMandantIdentifier().toString()
		);

		jobParameters.setProperty(
			WorkJobConstants.EMAIL_OF_USER,
			principalBean.getBenutzer().getEmail()
		);

		jobParameters.setProperty(
			LANGUAGE,
			LocaleThreadLocal.get().getLanguage()
		);

		jobParameters.setProperty(
			WorkJobConstants.GEMEINDE_ID_PARAM,
			gemeindeId
		);

		jobParameters.setProperty(
			WorkJobConstants.ZAHLUNGSLAUFTYP,
			zahlungslaufTyp.toString()
		);

		jobParameters.setProperty(
			WorkJobConstants.AUSZAHLUNG_IN_ZUKUNFT,
			String.valueOf(auszahlungInZukunft)
		);

		jobParameters.setProperty(
			WorkJobConstants.ZAHLUNGSAUFTRAG_ID,
			zahlungsauftragId
		);

		return jobParameters;
	}
}
