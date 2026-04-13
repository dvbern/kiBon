package ch.dvbern.ebegu.rules.mutationsmerger.geschwisterbonus;

import ch.dvbern.ebegu.enums.GeschwisterbonusTyp;
import ch.dvbern.ebegu.util.GeschwisterbonusTypVisitor;

public class MutationsMergerGeschwisterbonusHandlerVisitor implements
	GeschwisterbonusTypVisitor<MutationsMergerGeschwisterbonusHandler> {

	public MutationsMergerGeschwisterbonusHandler getMutationsMergerGeschwisterbonusHandler(
		GeschwisterbonusTyp geschwisterbonusTyp
	) {
		return geschwisterbonusTyp.accept(this);
	}

	@Override
	public MutationsMergerGeschwisterbonusHandler visitSchwyz() {
		return new MutationsMergerGeschwisterbonusHandlerNoop();
	}

	@Override
	public MutationsMergerGeschwisterbonusHandler visitSchwyz2() {
		return new MutationsMergerGeschwisterbonusHandlerNoop();
	}

	@Override
	public MutationsMergerGeschwisterbonusHandler visitLuzern() {
		return new MutationsMergerGeschwisterbonusHandlerLuzern();
	}

	@Override
	public MutationsMergerGeschwisterbonusHandler visitNone() {
		return new MutationsMergerGeschwisterbonusHandlerNoop();
	}
}
