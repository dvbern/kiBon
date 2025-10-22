package ch.dvbern.ebegu.util;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.util.mandant.AbstractMandantDefaultVisitor;
import com.sun.istack.NotNull;

public class BGParameterDefaultVisitor extends
	AbstractMandantDefaultVisitor<BGRechnerParameterDTO> {

	public BGRechnerParameterDTO getBGParameterForMandant(
		@NotNull Mandant mandant
	) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	protected BGRechnerParameterDTO visitDefault() {
		return TestUtils.getParameter();
	}

	@Override
	public BGRechnerParameterDTO visitLuzern() {
		return TestUtils.getRechnerParameterLuzern();
	}

	@Override
	public BGRechnerParameterDTO visitAppenzellAusserrhoden() {
		return TestUtils.getRechnerParamterAppenzell();
	}
}
