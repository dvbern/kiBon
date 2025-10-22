package ch.dvbern.ebegu.util;

import ch.dvbern.ebegu.dto.FamilienGroesseCalculationInput;
import ch.dvbern.ebegu.enums.Kinderabzug;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FamilienGroesseCalculationInputTest {

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void addHasGesuchstellerSameAnzahl_shouldStaySame(int anzahlGesuchsteller) {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(anzahlGesuchsteller);

		FamilienGroesseCalculationInput inputOther =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(anzahlGesuchsteller);

		input.add(inputOther);
		assertThat(input.getAnzahlGesuchsteller(), is(anzahlGesuchsteller));
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void addHasGesuchstellerOtherIsNull_shouldStaySame(
		int anzahlGesuchsteller
	) {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(anzahlGesuchsteller);

		FamilienGroesseCalculationInput inputOther =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(null);

		input.add(inputOther);
		assertThat(input.getAnzahlGesuchsteller(), is(anzahlGesuchsteller));
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void addHasGesuchstellerThisIsNull_shouldStaySame(int anzahlGesuchsteller) {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(null);

		FamilienGroesseCalculationInput inputOther =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(anzahlGesuchsteller);

		input.add(inputOther);
		assertThat(input.getAnzahlGesuchsteller(), is(anzahlGesuchsteller));
	}

	@Test
	void addHasGesuchstellerNotSameAnzahl_shouldThrowExcpetion() {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(1);

		FamilienGroesseCalculationInput inputOther =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(2);

		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> input.add(inputOther)
		);
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void isSameAnzahlGesuchstellereller_shouldBeTrue(int anzahlGesuchsteller) {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(anzahlGesuchsteller);

		FamilienGroesseCalculationInput inputOther =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(anzahlGesuchsteller);

		assertThat(input.isSame(inputOther), is(true));
	}

	@Test
	void isNotSameAnzahlGesuchstellereller_shouldBeFalse() {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(1);

		FamilienGroesseCalculationInput inputOther =
			new FamilienGroesseCalculationInput()
				.setAnzahlGesuchsteller(2);

		assertThat(input.isSame(inputOther), is(false));
	}

	@ParameterizedTest
	@EnumSource(value = Kinderabzug.class, mode = EnumSource.Mode.MATCH_ALL)
	void isSameKinderabzug_shouldBeTrue(Kinderabzug kinderabzug) {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput();
		input.addKindToAbzugList(0, kinderabzug);

		FamilienGroesseCalculationInput inputOther =
			new FamilienGroesseCalculationInput();
		inputOther.addKindToAbzugList(0, kinderabzug);

		assertThat(input.isSame(inputOther), is(true));
	}

	@ParameterizedTest
	@EnumSource(value = Kinderabzug.class, mode = EnumSource.Mode.MATCH_ALL)
	void isSameKinderabzug_diffrentKind_shouldBeFalse(Kinderabzug kinderabzug) {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput();
		input.addKindToAbzugList(0, kinderabzug);

		FamilienGroesseCalculationInput inputOther =
			new FamilienGroesseCalculationInput();
		inputOther.addKindToAbzugList(1, kinderabzug);

		assertThat(input.isSame(inputOther), is(false));
	}

	@Test
	void isSameKind_diffrentAbzug_shouldBeFalse() {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput();
		input.addKindToAbzugList(0, Kinderabzug.GANZER_ABZUG);

		FamilienGroesseCalculationInput inputOther =
			new FamilienGroesseCalculationInput();
		inputOther.addKindToAbzugList(0, Kinderabzug.HALBER_ABZUG);

		assertThat(input.isSame(inputOther), is(false));
	}

	@Test
	void addKindToEmptyList_shouldAddKind() {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput();

		FamilienGroesseCalculationInput inputOther =
			new FamilienGroesseCalculationInput();
		inputOther.addKindToAbzugList(0, Kinderabzug.GANZER_ABZUG);
		input.add(inputOther);
		assertThat(
			input.getKinderabzugList().get(0),
			is(Kinderabzug.GANZER_ABZUG)
		);
	}

	@Test
	void addNewKindToExistingMap_shouldAddKind() {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput();
		input.addKindToAbzugList(1, Kinderabzug.HALBER_ABZUG);
		FamilienGroesseCalculationInput inputOther =
			new FamilienGroesseCalculationInput();
		inputOther.addKindToAbzugList(0, Kinderabzug.GANZER_ABZUG);
		input.add(inputOther);
		assertThat(
			input.getKinderabzugList().get(0),
			is(Kinderabzug.GANZER_ABZUG)
		);
		assertThat(
			input.getKinderabzugList().get(1),
			is(Kinderabzug.HALBER_ABZUG)
		);
	}

	@Test
	void addSameKindToExistingMap_shouldStaySame() {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput();
		input.addKindToAbzugList(0, Kinderabzug.GANZER_ABZUG);
		FamilienGroesseCalculationInput inputOther =
			new FamilienGroesseCalculationInput();
		inputOther.addKindToAbzugList(0, Kinderabzug.GANZER_ABZUG);
		input.add(inputOther);
		assertThat(
			input.getKinderabzugList().get(0),
			is(Kinderabzug.GANZER_ABZUG)
		);
		assertThat(input.getKinderabzugList().size(), is(1));
	}

	@Test
	void addSameKindToMapWithDifferenAbzug_shouldThrowError() {
		FamilienGroesseCalculationInput input =
			new FamilienGroesseCalculationInput();
		input.addKindToAbzugList(0, Kinderabzug.HALBER_ABZUG);
		FamilienGroesseCalculationInput inputOther =
			new FamilienGroesseCalculationInput();
		inputOther.addKindToAbzugList(0, Kinderabzug.GANZER_ABZUG);

		Assertions.assertThrows(
			IllegalArgumentException.class,
			() -> input.add(inputOther)
		);
	}
}
