package ch.dvbern.ebegu.entities;

import java.util.Locale;

import ch.dvbern.ebegu.dto.VerfuegungsBemerkungDTO;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rules.RuleValidity;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class VerfuegungZeitabschnittBemerkungTest {

	private Mandant mandant = new Mandant();
	private VerfuegungsBemerkungDTO verfuegungsBemerkungDTO =
		new VerfuegungsBemerkungDTO(
			RuleValidity.ASIV,
			MsgKey.BETREUUNGSANGEBOT_MSG,
			Locale.GERMAN
		);

	private KindContainer kindContainer = new KindContainer();
	private VerfuegungZeitabschnitt verfuegungZeitabschnitt =
		new VerfuegungZeitabschnitt();

	@BeforeEach
	public void init() {
		mandant.setMandantIdentifier(MandantIdentifier.BERN);
		verfuegungZeitabschnitt.setVerfuegung(new Verfuegung());
		verfuegungZeitabschnitt.getVerfuegung().setBetreuung(new Betreuung());
		kindContainer.setGesuch(new Gesuch());
		kindContainer.getGesuch().setDossier(new Dossier());
		kindContainer.getGesuch().getDossier().setGemeinde(new Gemeinde());
		kindContainer.getGesuch().getDossier().getGemeinde().setBfsNummer(100L);
		verfuegungZeitabschnitt.getVerfuegung()
			.getBetreuung()
			.setKind(kindContainer);
	}

	@Test
	void VerfuegungZeitabschnittBemerkungTest() {
		VerfuegungZeitabschnittBemerkung verfuegungZeitabschnittBemerkungDE =
			new VerfuegungZeitabschnittBemerkung(
				verfuegungsBemerkungDTO,
				verfuegungZeitabschnitt,
				mandant
			);
		verfuegungsBemerkungDTO.setSprache(Locale.FRENCH);
		VerfuegungZeitabschnittBemerkung verfuegungZeitabschnittBemerkungFR =
			new VerfuegungZeitabschnittBemerkung(
				verfuegungsBemerkungDTO,
				verfuegungZeitabschnitt,
				mandant
			);

		assertThat(
			verfuegungZeitabschnittBemerkungDE.getBemerkung()
				.equals(
					verfuegungZeitabschnittBemerkungFR
						.getBemerkung()
				),
			is(false)
		);

		verfuegungZeitabschnitt.getVerfuegung().setBetreuung(null);
		verfuegungZeitabschnitt.getVerfuegung()
			.setAnmeldungTagesschule(new AnmeldungTagesschule());
		verfuegungZeitabschnitt.getVerfuegung()
			.getAnmeldungTagesschule()
			.setKind(kindContainer);
		VerfuegungZeitabschnittBemerkung verfuegungZeitabschnittBemerkungFRAnmeldung =
			new VerfuegungZeitabschnittBemerkung(
				verfuegungsBemerkungDTO,
				verfuegungZeitabschnitt,
				mandant
			);
		assertThat(
			verfuegungZeitabschnittBemerkungFR.getBemerkung()
				.equals(
					verfuegungZeitabschnittBemerkungFRAnmeldung
						.getBemerkung()
				),
			is(true)
		);

	}
}
