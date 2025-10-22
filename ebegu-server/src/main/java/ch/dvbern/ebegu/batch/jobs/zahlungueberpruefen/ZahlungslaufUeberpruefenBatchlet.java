package ch.dvbern.ebegu.batch.jobs.zahlungueberpruefen;

import java.util.Optional;
import java.util.Properties;

import jakarta.batch.api.AbstractBatchlet;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.ZahlungService;
import ch.dvbern.ebegu.services.zahlungen.ZahlungUeberpruefungServiceBean;

@Named("zahlungslaufUeberpruefenBatchlet")
@Dependent
public class ZahlungslaufUeberpruefenBatchlet extends AbstractBatchlet {

	@Inject
	private JobContext jobCtx;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private ZahlungService zahlungService;

	@Inject
	private ZahlungUeberpruefungServiceBean zahlungUeberpruefungService;

	@Override
	public String process() throws Exception {
		Gemeinde gemeinde = getGemeinde();
		ZahlungslaufTyp zahlungslaufTyp = getZahlungslaufTyp();

		Optional<Zahlungsauftrag> lastZahlungsauftrag = zahlungService
			.findLastZahlungsauftrag(
				zahlungslaufTyp,
				gemeinde
			);
		lastZahlungsauftrag.ifPresent(
			zahlungsauftrag -> zahlungUeberpruefungService
				.pruefungZahlungen(
					gemeinde,
					zahlungslaufTyp,
					zahlungsauftrag.getId(),
					zahlungsauftrag.getDatumGeneriert(),
					zahlungsauftrag.getBeschrieb(),
					getAuszahlungInZukunftFlag()
				)
		);
		return BatchStatus.COMPLETED.toString();
	}

	private Boolean getAuszahlungInZukunftFlag() {
		var auszahlungInZukunft = getParameters().getProperty(
			WorkJobConstants.AUSZAHLUNG_IN_ZUKUNFT
		);
		return Boolean.parseBoolean(auszahlungInZukunft);
	}

	private ZahlungslaufTyp getZahlungslaufTyp() {
		var zahlungslaufTyp = getParameters().getProperty(
			WorkJobConstants.ZAHLUNGSLAUFTYP
		);
		return ZahlungslaufTyp.valueOf(zahlungslaufTyp);
	}

	private Gemeinde getGemeinde() {
		var gemeindeId = getParameters().getProperty(
			WorkJobConstants.GEMEINDE_ID_PARAM
		);
		return gemeindeService.findGemeinde(gemeindeId)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"zahlungenKontrollieren",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					gemeindeId
				)
			);
	}

	private Properties getParameters() {
		JobOperator operator = BatchRuntime.getJobOperator();
		return operator.getParameters(jobCtx.getExecutionId());
	}
}
