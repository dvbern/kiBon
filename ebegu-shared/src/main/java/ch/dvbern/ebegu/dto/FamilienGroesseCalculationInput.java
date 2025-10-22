package ch.dvbern.ebegu.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.util.MathUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
public class FamilienGroesseCalculationInput {

	@Nullable
	@Setter
	private BigDecimal famGroesse = null;

	@Nullable
	@Setter
	private BigDecimal abzugFamGroesse = null;

	private Map<Integer, Kinderabzug> kinderabzugList = new HashMap<>();

	@Setter
	private Integer anzahlGesuchsteller;

	public FamilienGroesseCalculationInput() {
	}

	public FamilienGroesseCalculationInput(
		FamilienGroesseCalculationInput other
	) {
		this.anzahlGesuchsteller = other.getAnzahlGesuchsteller();
		this.kinderabzugList = other.kinderabzugList;
		this.famGroesse = other.getFamGroesse();
		this.abzugFamGroesse = other.getAbzugFamGroesse();
	}

	public FamilienGroesseCalculationInput copy() {
		return new FamilienGroesseCalculationInput(this);
	}

	public boolean isSameSichtbareDaten(FamilienGroesseCalculationInput other) {
		return MathUtil.isSame(abzugFamGroesse, other.abzugFamGroesse)
			&& MathUtil.isSame(famGroesse, other.famGroesse);
	}

	public boolean isSame(FamilienGroesseCalculationInput other) {
		return isSameSichtbareDaten(other)
			&& Objects.equals(
				this.anzahlGesuchsteller,
				other.anzahlGesuchsteller
			)
			&& this.kinderabzugList.equals(other.kinderabzugList);
	}

	public void addKindToAbzugList(int kindNummer, Kinderabzug kinderabzug) {
		this.kinderabzugList.put(kindNummer, kinderabzug);
	}

	public void add(FamilienGroesseCalculationInput other) {
		addAnzahlGesuchsteller(other);
		addKinderAbzugList(other);
		addAbzugFamiliengroesse(other);
		addFamiliengroesse(other);
	}

	private void addFamiliengroesse(FamilienGroesseCalculationInput other) {
		// Die Familiengroesse kann nicht linear addiert werden, daher darf es hier nie uebschneidungen geben
		if (other.getFamGroesse() != null) {
			if (this.getFamGroesse() != null
				&& !MathUtil.isSame(
					this.getFamGroesse(),
					other.getFamGroesse()
				)) {
				throw new IllegalArgumentException(
					"Familiengoressen kann nicht gemerged werden"
				);
			}
			this.setFamGroesse(other.getFamGroesse());
		}
	}

	private void addAbzugFamiliengroesse(
		FamilienGroesseCalculationInput other
	) {
		// Die Felder betreffend Familienabzug können nicht linear addiert werden. Es darf also nie Überschneidungen geben!
		if (other.getAbzugFamGroesse() != null) {
			if (this.getAbzugFamGroesse() != null
				&&
				!MathUtil.isSame(
					this.getAbzugFamGroesse(),
					other.getAbzugFamGroesse()
				)
			) {
				throw new IllegalArgumentException(
					"Familiengoressenabzug kann nicht gemerged werden"
				);
			}
			this.setAbzugFamGroesse(other.getAbzugFamGroesse());
		}
	}

	private void addKinderAbzugList(FamilienGroesseCalculationInput other) {
		for (Map.Entry<Integer, Kinderabzug> otherKinderabzug : other.kinderabzugList
			.entrySet()) {
			if (!this.kinderabzugList.containsKey(otherKinderabzug.getKey())) {
				this.kinderabzugList.put(
					otherKinderabzug.getKey(),
					otherKinderabzug.getValue()
				);
				continue;
			}

			if (this.kinderabzugList.get(otherKinderabzug.getKey())
				!= otherKinderabzug.getValue()) {
				throw new IllegalArgumentException(
					"Gleiches Kind darf nicht gemerged werden, wenn es nicht denselben Kinderabzug hat."
				);
			}
		}
	}

	private void addAnzahlGesuchsteller(FamilienGroesseCalculationInput other) {
		if (this.anzahlGesuchsteller == null) {
			this.anzahlGesuchsteller = other.anzahlGesuchsteller;
		}

		if (other.getAnzahlGesuchsteller() != null
			&& !Objects.equals(
				this.anzahlGesuchsteller,
				other.anzahlGesuchsteller
			)) {
			throw new IllegalArgumentException(
				"Anzahl Gesuchsteller kann nicht linear addiert werden."
			);
		}
	}

}
