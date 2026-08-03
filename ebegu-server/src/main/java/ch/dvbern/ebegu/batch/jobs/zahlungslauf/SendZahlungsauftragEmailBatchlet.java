/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.batch.jobs.zahlungslauf;

import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

import jakarta.batch.api.AbstractBatchlet;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.services.MailService;
import ch.dvbern.ebegu.services.MandantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("sendZahlungsauftragEmailBatchlet")
@Dependent
public class SendZahlungsauftragEmailBatchlet extends AbstractBatchlet {

	private static final Logger LOG = LoggerFactory.getLogger(
		SendZahlungsauftragEmailBatchlet.class
	);

	@Inject
	private MailService mailService;

	@Inject
	private MandantService mandantService;

	@Inject
	private JobContext jobCtx;

	@Inject
	private EbeguConfiguration configuration;

	@Override
	public String process() {
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
		LOG.debug("Sending mail with file for user to {}", receiverEmail);
		Objects.requireNonNull(
			receiverEmail,
			" Email muss gesetzt sein damit den Fertigen Zahlungslauf an den Empfaenger geschickt werden kann"
		);

		try {
			mailService.prepareToSendInfoZahlungslaufGeneriert(
				receiverEmail,
				createZahlungenPageLink(mandant),
				Locale.forLanguageTag(receiverLanguage),
				mandant
			);
			return BatchStatus.COMPLETED.toString();
		} catch (Exception ignore) {
			return BatchStatus.FAILED.toString();
		}
	}

	private String createZahlungenPageLink(Mandant mandant) {
		return configuration.getFrontendBaseUrl(
			mandant.getMandantIdentifier()
		)
			+ "/zahlungsauftrag";
	}

	private Properties getParameters() {
		JobOperator operator = BatchRuntime.getJobOperator();
		return operator.getParameters(jobCtx.getExecutionId());
	}
}
