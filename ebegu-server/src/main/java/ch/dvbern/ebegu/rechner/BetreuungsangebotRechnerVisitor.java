package ch.dvbern.ebegu.rechner;

import java.util.List;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.util.BetreuungsangebotTypVisitor;
import com.sun.istack.NotNull;

public class BetreuungsangebotRechnerVisitor implements
	BetreuungsangebotTypVisitor<AbstractRechner> {

	private final Mandant mandant;
	private final List<RechnerRule> rechnerRulesForGemeinde;

	public BetreuungsangebotRechnerVisitor(
		Mandant mandant,
		List<RechnerRule> rechnerRulesForGemeinde
	) {
		this.mandant = mandant;
		this.rechnerRulesForGemeinde = rechnerRulesForGemeinde;
	}

	public AbstractRechner getRechnerForBetreuungsTyp(
		@NotNull BetreuungsangebotTyp betreuungsangebotTyp
	) {
		return betreuungsangebotTyp.accept(this);
	}

	@Override
	public AbstractRechner visitKita() {
		return new KitaRechnerDefaultVisitor(rechnerRulesForGemeinde)
			.getKitaRechnerForMandant(mandant);
	}

	@Override
	public AbstractRechner visitTagesfamilien() {
		return new TageselternRechnerDefaultVisitor(rechnerRulesForGemeinde)
			.getTageselternRechnerForMandant(mandant);
	}

	@Override
	public AbstractRechner visitMittagstisch() {
		return new MittagstischSchwyzRechner();
	}

	@Override
	public AbstractRechner visitTagesschule() {
		return new TagesschuleBernRechner(rechnerRulesForGemeinde);
	}

	@Nullable
	@Override
	public AbstractRechner visitFerieninsel() {
		return null;
	}
}
