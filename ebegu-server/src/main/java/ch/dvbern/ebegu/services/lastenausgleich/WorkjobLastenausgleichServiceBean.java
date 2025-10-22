package ch.dvbern.ebegu.services.lastenausgleich;

import java.util.Properties;

import javax.annotation.Nullable;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.lastenausgleich.WorkjobLastenausgleichService;
import ch.dvbern.ebegu.services.AbstractBaseService;

@Stateless
@Local(WorkjobLastenausgleichService.class)
public class WorkjobLastenausgleichServiceBean extends AbstractBaseService
	implements
	WorkjobLastenausgleichService {

	@Inject
	private PrincipalBean principalBean;

	@Override
	public void startLastenausgleichWorkjob(
		@NotNull String jahr,
		@Nullable String selbstbehaltPro100ProzentPlatz
	) {
		JobOperator jobOperator = BatchRuntime.getJobOperator();
		final Properties jobParameters = buildLastenausgleichJobParameter(
			jahr,
			selbstbehaltPro100ProzentPlatz
		);
		jobOperator.start("lastenausgleichbatch", jobParameters);
	}

	private Properties buildLastenausgleichJobParameter(
		String jahr,
		@Nullable String selbstbehaltPro100ProzentPlatz
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

		jobParameters.setProperty(WorkJobConstants.LAS_JAHR, jahr);

		if (selbstbehaltPro100ProzentPlatz != null) {
			jobParameters.setProperty(
				WorkJobConstants.LAS_SELBSTBEHALT,
				selbstbehaltPro100ProzentPlatz
			);
		}

		return jobParameters;
	}
}
