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

package ch.dvbern.ebegu.batch.jobs.lastenausgleich;

import jakarta.batch.api.listener.AbstractJobListener;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.services.MailService;
import ch.dvbern.ebegu.services.lastenausgleich.LastenausgleichServiceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("lastenausgleichJobListener")
@Dependent
public class LastenausgleichJobListener extends AbstractJobListener {

	private static final Logger LOG = LoggerFactory.getLogger(
		LastenausgleichJobListener.class
	);

	@Inject
	private JobContext ctx;

	@Inject
	private LastenausgleichServiceBean lastenausgleichService;

	@Inject
	private LastenausgleichBatchletContext lastenausgleichBatchletContext;

	@Inject
	private MailService mailService;

	@Override
	public void beforeJob() {
		LOG.debug(
			"LastenausgleichJobListener started: {}",
			ctx.getExecutionId()
		);
	}

	@Override
	public void afterJob() {
		LOG.debug(
			"LastenausgleichJobListener finished: {}, status: {},{}",
			ctx.getExecutionId(),
			ctx.getBatchStatus(),
			ctx.getExitStatus()
		);

		//wenn interner job completed ist sehen wir das auch als erfolgreich an, alles andere sehen wir als fehlschlag
		if (ctx.getExitStatus().equals(BatchStatus.COMPLETED.name())) {
			sendMailToLastenausgleichProzessBeendet(true);
		} else {
			sendMailToLastenausgleichProzessBeendet(false);
			removeLastenausgleichIfCreatedInFailedJob();
		}
	}

	private void removeLastenausgleichIfCreatedInFailedJob() {
		if (lastenausgleichBatchletContext
			.isLastenausgleichCreatedDuringBatchjob()
			&&
			!lastenausgleichBatchletContext
				.isLastenausgleichCalculationCompleted()) {
			lastenausgleichService.removeLastenausgleich(
				lastenausgleichBatchletContext.getLastenausgleichIdFromContext()
			);
		}
	}

	private void sendMailToLastenausgleichProzessBeendet(boolean isSuccessful) {
		Mandant mandant = lastenausgleichBatchletContext
			.getMandantFromContext();
		String userMail = lastenausgleichBatchletContext.getProperty(
			WorkJobConstants.EMAIL_OF_USER
		);
		String jahr = lastenausgleichBatchletContext.getProperty(
			WorkJobConstants.LAS_JAHR
		);
		mailService.sendInfoLastenausgleichProzessBeendet(
			jahr,
			userMail,
			isSuccessful,
			mandant
		);
	}
}
