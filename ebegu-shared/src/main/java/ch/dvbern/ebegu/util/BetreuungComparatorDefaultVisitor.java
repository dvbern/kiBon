package ch.dvbern.ebegu.util;

import java.util.Comparator;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.AbstractMandantDefaultVisitor;
import com.sun.istack.NotNull;

public class BetreuungComparatorDefaultVisitor extends
	AbstractMandantDefaultVisitor<Comparator<AbstractPlatz>> {

	public Comparator<AbstractPlatz> getComparatorForMandant(
		@NotNull Mandant mandant
	) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	protected Comparator<AbstractPlatz> visitDefault() {
		return new BetreuungComparator();
	}

	@Override
	public Comparator<AbstractPlatz> visitAppenzellAusserrhoden() {
		return new BetreuungComparatorAppenzell();
	}
}
