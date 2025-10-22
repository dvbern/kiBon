package ch.dvbern.ebegu.batch.jobs.lastenausgleich;

import java.util.Objects;

import javax.annotation.Nullable;
import jakarta.batch.api.AbstractBatchlet;
import jakarta.batch.runtime.BatchStatus;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.services.lastenausgleich.LastenausgleichServiceBean;

@Named("createLastenausgleichBatchlet")
@Dependent
public class LastenausgleichCreationBatchletStep
	extends
	AbstractBatchlet {

	@Inject
	private LastenausgleichServiceBean lastenausgleichService;

	@Inject
	private LastenausgleichBatchletContext lastenausgleichBatchletContext;

	@Override
	public String process() throws Exception {
		final String jahr = lastenausgleichBatchletContext.getProperty(
			WorkJobConstants.LAS_JAHR
		);

		final String selbstbehaltPro100ProzentPlatz =
			lastenausgleichBatchletContext
				.getProperty(
					WorkJobConstants.LAS_SELBSTBEHALT
				);

		Mandant mandant = lastenausgleichBatchletContext
			.getMandantFromContext();

		Lastenausgleich lastenausgleich = createLastenausgleich(
			jahr,
			selbstbehaltPro100ProzentPlatz,
			mandant
		);
		lastenausgleichBatchletContext.setLastenausgleichIdToContext(
			lastenausgleich.getId()
		);
		return BatchStatus.COMPLETED.toString();
	}

	private Lastenausgleich createLastenausgleich(
		String sJahr,
		@Nullable String selbstbehaltPro100ProzentPlatz,
		@NotNull Mandant mandant
	) {
		Objects.requireNonNull(sJahr);
		int jahr = Integer.parseInt(sJahr);
		return lastenausgleichService
			.createLastenausgleich(
				jahr,
				selbstbehaltPro100ProzentPlatz,
				mandant
			);
	}
}
