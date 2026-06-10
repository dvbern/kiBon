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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;

import jakarta.batch.api.AbstractBatchlet;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.ZahlungService;
import ch.dvbern.ebegu.util.DateUtil;

@Named("zahlungslaufErstellenBatchlet")
@Dependent
public class ZahlungslaufErstellenBatchlet extends AbstractBatchlet {

	@Inject
	private JobContext jobCtx;

	@Inject
	private MandantService mandantService;

	@Inject
	private ZahlungService zahlungService;

	@Override
	public String process() throws Exception {
		zahlungService
			.zahlungsauftragErstellen(
				getZahlungslaufTyp(),
				getGemeindeId(),
				getFaelligkeitsdatum(),
				getBeschreibung(),
				getAuszahlungInZukunftFlag(),
				getDatumGeneriert(),
				getMandant()
			);
		return BatchStatus.COMPLETED.toString();
	}

	private Mandant getMandant() {
		String mandantId = getParameters().getProperty(
			WorkJobConstants.REPORT_MANDANT_ID
		);
		return mandantService.getMandant(mandantId);
	}

	private ZahlungslaufTyp getZahlungslaufTyp() {
		var zahlungslaufTyp = getParameters().getProperty(
			WorkJobConstants.ZAHLUNGSLAUFTYP
		);
		return ZahlungslaufTyp.valueOf(zahlungslaufTyp);
	}

	private String getGemeindeId() {
		return getParameters().getProperty(
			WorkJobConstants.GEMEINDE_ID_PARAM
		);
	}

	private LocalDate getFaelligkeitsdatum() {
		String datumFaelligkeitString = getParameters().getProperty(
			WorkJobConstants.DATUM_FAELLIGKEIT
		);
		return DateUtil.parseStringToDate(datumFaelligkeitString);
	}

	private LocalDateTime getDatumGeneriert() {
		String stringDatumGeneriert = getParameters().getProperty(
			WorkJobConstants.DATUM_GENERIERT
		);
		LocalDateTime datumGeneriert;
		if (stringDatumGeneriert != null) {
			datumGeneriert = DateUtil.parseStringToDateOrReturnNow(
				stringDatumGeneriert
			).atStartOfDay();
		} else {
			datumGeneriert = LocalDateTime.now();
		}
		return datumGeneriert;
	}

	private String getBeschreibung() {
		return getParameters().getProperty(
			WorkJobConstants.BESCHREIBUNG
		);
	}

	private Boolean getAuszahlungInZukunftFlag() {
		var auszahlungInZukunft = getParameters().getProperty(
			WorkJobConstants.AUSZAHLUNG_IN_ZUKUNFT
		);
		return Boolean.parseBoolean(auszahlungInZukunft);
	}

	private Properties getParameters() {
		JobOperator operator = BatchRuntime.getJobOperator();
		return operator.getParameters(jobCtx.getExecutionId());
	}
}
