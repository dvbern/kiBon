package ch.dvbern.ebegu.batch.jobs.lastenausgleich;

import java.io.Serializable;
import java.util.Comparator;

import javax.annotation.Nullable;
import jakarta.batch.api.chunk.AbstractItemReader;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.services.GemeindeService;

@Named("lastenausgleichGemeindeReader")
@Dependent
public class LastenausgleichGemeindeReader extends AbstractItemReader {

	private Gemeinde[] gemeindes;
	private Integer count;

	@Inject
	private LastenausgleichBatchletContext lastenausgleichBatchletContext;

	@Inject
	private GemeindeService gemeindeService;

	@Nullable
	@Override
	public Gemeinde readItem() throws Exception {
		if (count >= gemeindes.length) {
			return null;
		}
		lastenausgleichBatchletContext.setNumberOfGemeindeProcessed(count + 1);
		return gemeindes[count++];
	}

	@Override
	public void open(Serializable checkpoint) throws Exception {
		Mandant mandant = lastenausgleichBatchletContext
			.getMandantFromContext();
		// Gemeinden sorted by gemeinde nummer
		// with sorting the gemeinden, we can be save that the large gemeinden (Bern, Biel and Köniz)
		// are not processed in the same cunck
		gemeindes = gemeindeService.getAktiveGemeinden(mandant)
			.stream()
			.sorted(Comparator.comparingLong(Gemeinde::getGemeindeNummer))
			.toArray(Gemeinde[]::new);
		lastenausgleichBatchletContext.setTotalNumberOfGemeindeToProcess(
			gemeindes.length
		);
		count = 0;
	}
}
