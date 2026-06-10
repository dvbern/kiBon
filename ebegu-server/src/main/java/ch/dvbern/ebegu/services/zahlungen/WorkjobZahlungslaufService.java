package ch.dvbern.ebegu.services.zahlungen;

import java.time.LocalDate;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.util.Constants;

@Stateless
public class WorkjobZahlungslaufService {

	@Inject
	private PrincipalBean principalBean;

	public long startZahlungslaufWorkjob(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String gemeindeId,
		@Nonnull Boolean auszahlungInZukunft,
		@Nonnull LocalDate datumFaelligkeit,
		@Nonnull String beschreibung,
		@Nullable String datumGeneriert
	) {
		JobOperator jobOperator = BatchRuntime.getJobOperator();
		final Properties jobParameters = buildJobParameter(
			zahlungslaufTyp,
			gemeindeId,
			auszahlungInZukunft,
			datumFaelligkeit,
			beschreibung,
			datumGeneriert
		);
		return jobOperator.start("zahlungslaufbatch", jobParameters);
	}

	private Properties buildJobParameter(
		@Nonnull ZahlungslaufTyp zahlungslaufTyp,
		@Nonnull String gemeindeId,
		@Nonnull Boolean auszahlungInZukunft,
		@Nonnull LocalDate datumFaelligkeit,
		@Nonnull String beschreibung,
		@Nullable String datumGeneriert
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

		jobParameters.setProperty(
			WorkJobConstants.DATUM_FAELLIGKEIT,
			Constants.SQL_DATE_FORMAT.format(datumFaelligkeit)
		);
		jobParameters.setProperty(WorkJobConstants.BESCHREIBUNG, beschreibung);
		if (datumGeneriert != null) {
			jobParameters.setProperty(
				WorkJobConstants.DATUM_GENERIERT,
				datumGeneriert
			);
		}
		return jobParameters;
	}
}
