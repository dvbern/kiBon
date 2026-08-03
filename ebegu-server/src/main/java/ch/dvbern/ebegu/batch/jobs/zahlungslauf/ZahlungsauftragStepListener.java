/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package ch.dvbern.ebegu.batch.jobs.zahlungslauf;

import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

import jakarta.batch.api.listener.AbstractStepListener;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.context.JobContext;
import jakarta.batch.runtime.context.StepContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.enums.reporting.BatchJobStatus;
import ch.dvbern.ebegu.services.MailService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.WorkjobService;
import ch.dvbern.ebegu.services.ZahlungService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("zahlungsauftragStepListener")
@Dependent
public class ZahlungsauftragStepListener extends AbstractStepListener {

	private static final Logger LOG = LoggerFactory.getLogger(
		ZahlungsauftragStepListener.class
	);

	@Inject
	private JobContext ctx;

	@Inject
	private StepContext stepContext;

	@Inject
	private WorkjobService workjobService;

	@Inject
	private ZahlungService zahlungService;

	@Inject
	private MandantService mandantService;

	@Inject
	private MailService mailService;

	public void beforeStep() {
		LOG.info("Starting step {}", stepContext.getStepName());
		workjobService.changeStateOfWorkjob(
			ctx.getExecutionId(),
			BatchJobStatus.RUNNING
		);
	}

	public void afterStep() {
		LOG.info("Finished step {}", stepContext.getStepName());

		if (stepContext.getExitStatus().equals(BatchStatus.COMPLETED.name())) {
			workjobService.changeStateOfWorkjob(
				ctx.getExecutionId(),
				BatchJobStatus.FINISHED
			);
		} else {
			workjobService.changeStateOfWorkjob(
				ctx.getExecutionId(),
				BatchJobStatus.FAILED
			);
			zahlungService.setZahlungsauftragstatusHasFailed(
				getZahlungsauftragId()
			);
			sendFailureEmail();
		}
	}

	private String getZahlungsauftragId() {
		return getParameters().getProperty(
			WorkJobConstants.ZAHLUNGSAUFTRAG_ID
		);
	}

	private Properties getParameters() {
		JobOperator operator = BatchRuntime.getJobOperator();
		return operator.getParameters(ctx.getExecutionId());
	}

	private void sendFailureEmail() {
		final String receiverEmail = getParameters().getProperty(
			WorkJobConstants.EMAIL_OF_USER
		);
		final String receiverLanguage = getParameters().getProperty(
			WorkJobConstants.LANGUAGE
		);
		final String mandantId = getParameters().getProperty(
			WorkJobConstants.REPORT_MANDANT_ID
		);
		Mandant mandant = mandantService.getMandant(mandantId);
		LOG.debug(
			"Sending Zahlungslaufjob failed mail for user to {}",
			receiverEmail
		);
		Objects.requireNonNull(
			receiverEmail,
			" Email muss gesetzt sein damit den fehlgeschlagte Zahlungslauf an den Empfaenger geschickt werden kann"
		);

		try {
			mailService.prepareToSendInfoZahlungslaufNichtErfolgreichErstellt(
				receiverEmail,
				Locale.forLanguageTag(receiverLanguage),
				mandant
			);
		} catch (Exception e) {
			LOG.error(
				"There was an error while preparing the failed Zahlungslaufjob email: ",
				e
			);
		}
	}
}
