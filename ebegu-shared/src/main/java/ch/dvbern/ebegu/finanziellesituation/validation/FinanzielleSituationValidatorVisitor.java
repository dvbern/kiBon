package ch.dvbern.ebegu.finanziellesituation.validation;

import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.util.FinanzielleSituationTypVisitor;

public class FinanzielleSituationValidatorVisitor implements
	FinanzielleSituationTypVisitor<FinanzielleSituationValidator> {

	public FinanzielleSituationValidator accept(
		FinanzielleSituationTyp finanzielleSituationTyp
	) {
		return finanzielleSituationTyp.accept(this);
	}

	@Override
	public FinanzielleSituationValidator visitFinSitBern() {
		return new FinanzielleSituationValidatorBern();
	}

	@Override
	public FinanzielleSituationValidator visitFinSitBernFKJV() {
		return new FinanzielleSituationValidatorBernFKJV();
	}

	@Override
	public FinanzielleSituationValidator visitFinSitLuzern() {
		return new FinanzielleSituationValidatorLU();
	}

	@Override
	public FinanzielleSituationValidator visitFinSitSolothurn() {
		return new FinanzielleSituationValidatorSO();
	}

	@Override
	public FinanzielleSituationValidator visitFinSitAppenzell() {
		return new FinanzielleSituationValidatorAR();
	}

	@Override
	public FinanzielleSituationValidator visitFinSitAppenzellFolgemonat() {
		return new FinanzielleSituationValidatorAR();
	}

	@Override
	public FinanzielleSituationValidator visitFinSitSchwyz() {
		return new FinanzielleSituationValidatorSZ();
	}

	@Override
	public FinanzielleSituationValidator visitFinSitSchwyzErweitert() {
		return new FinanzielleSituationValidatorSZErweitert();
	}

	@Override
	public FinanzielleSituationValidator visitFinSitBernFKJVFristen() {
		return visitFinSitBernFKJV();
	}

}
