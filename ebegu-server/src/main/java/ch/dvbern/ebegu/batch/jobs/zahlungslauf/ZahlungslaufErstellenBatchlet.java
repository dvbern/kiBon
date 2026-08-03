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

import java.util.Properties;

import jakarta.batch.api.AbstractBatchlet;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.services.ZahlungService;

@Named("zahlungslaufErstellenBatchlet")
@Dependent
public class ZahlungslaufErstellenBatchlet extends AbstractBatchlet {

	@Inject
	private JobContext jobCtx;

	@Inject
	private ZahlungService zahlungService;

	@Override
	public String process() throws Exception {
		zahlungService
			.zahlungsauftragBearbeiten(
				getZahlungsauftragId()
			);
		return BatchStatus.COMPLETED.toString();
	}

	private String getZahlungsauftragId() {
		return getParameters().getProperty(
			WorkJobConstants.ZAHLUNGSAUFTRAG_ID
		);
	}

	private Properties getParameters() {
		JobOperator operator = BatchRuntime.getJobOperator();
		return operator.getParameters(jobCtx.getExecutionId());
	}
}
