package ch.dvbern.ebegu.rules;

import ch.dvbern.ebegu.util.mandant.AbstractMandantDefaultVisitor;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import com.sun.istack.NotNull;

public class EinreichefristDefaultVisitor extends
	AbstractMandantDefaultVisitor<IEinreichefristCalculator> {

	public IEinreichefristCalculator getEinreichefristCalculator(
		@NotNull MandantIdentifier mandant
	) {
		return mandant.accept(this);
	}

	@Override
	protected IEinreichefristCalculator visitDefault() {
		return new EinreichefristCalculator();
	}

	@Override
	public IEinreichefristCalculator visitAppenzellAusserrhoden() {
		return new EinreichefristCalculatorAppenzellAusserrhoden();
	}
}
