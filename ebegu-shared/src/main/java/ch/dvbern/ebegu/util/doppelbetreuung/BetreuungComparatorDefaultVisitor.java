package ch.dvbern.ebegu.util.doppelbetreuung;

import java.util.Comparator;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.enums.betreuung.BetreuungComparator;
import com.sun.istack.NotNull;

public class BetreuungComparatorDefaultVisitor implements
	DoppelbetreuungPrioEinstellungVisitor<Comparator<AbstractPlatz>> {

	public Comparator<AbstractPlatz> getComparatorForEinstellung(
		@NotNull BetreuungComparator betreuungComparator
	) {
		return betreuungComparator.accept(this);
	}

	@Override
	public Comparator<AbstractPlatz> visitDefault() {
		return new ch.dvbern.ebegu.util.doppelbetreuung.BetreuungComparator();
	}

	@Override
	public Comparator<AbstractPlatz> visitDefaultNew() {
		return new BetreuungComparatorNew();
	}

	@Override
	public Comparator<AbstractPlatz> visitAppenzell() {
		return new BetreuungComparatorAppenzell();
	}
}
