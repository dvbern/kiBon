package ch.dvbern.ebegu.services.zahlungen;

import java.util.Properties;

import javax.annotation.Nonnull;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;

@Stateless
public class WorkjobZahlungUeberpruefungService {

	@Inject
	private PrincipalBean principalBean;

	public void startZahlungUeberpruefungWorkjob(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String gemeindeId,
		@Nonnull Boolean auszahlungInZukunft
	) {
		JobOperator jobOperator = BatchRuntime.getJobOperator();
		final Properties jobParameters = buildJobParameter(
			zahlungslaufTyp,
			gemeindeId,
			auszahlungInZukunft
		);
		jobOperator.start("zahlungueberpruefenbatch", jobParameters);
	}

	private Properties buildJobParameter(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String gemeindeId,
		@Nonnull Boolean auszahlungInZukunft
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

		return jobParameters;
	}
}
