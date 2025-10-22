/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.GemeindeStammdatenKorrespondenz;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.GemeindeStatus;
import ch.dvbern.ebegu.enums.InstitutionStatus;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.locallogin.LocalLoginService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.TutorialConstants;
import ch.dvbern.ebegu.util.mandant.TutorialConstantsVisitor;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service fuer erstellen und mutieren von Schulungsdaten
 */
@SuppressWarnings({ "DLS_DEAD_LOCAL_STORE", "DM_CONVERT_CASE", "EI_EXPOSE_REP",
	"ConstantNamingConvention",
	"SpringAutowiredFieldsWarningInspection" })
@Stateless
@Local(SchulungService.class)
public class SchulungServiceBean extends AbstractBaseService implements
	SchulungService {

	private static final Logger LOG = LoggerFactory.getLogger(
		SchulungServiceBean.class
	);

	private final TutorialConstantsVisitor schulungConstantsVisitor =
		new TutorialConstantsVisitor();

	@Inject
	private InstitutionService institutionService;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private LocalLoginService localLoginService;

	@Override
	public void createTutorialdaten(Mandant mandant) {
		LOG.info("Erstelle Tutorialdaten...");
		TutorialConstants constants = schulungConstantsVisitor.process(mandant);

		Gemeinde gemeinde = createGemeindeTutorial(mandant, constants);
		GemeindeStammdaten gemeindeStammdaten =
			createGemeindeStammdatenTutorial(gemeinde);

		Institution institutionTutorial = createInstitution(
			constants.getInstitutionTutorialId(),
			mandant
		);
		createInstitutionStammdaten(
			constants.getKitaTutorialId(),
			institutionTutorial
		);

		localLoginService.createTutorialBenutzer(constants, mandant)
			.stream()
			.map(
				result -> benutzerService.findBenutzer(result.email(), mandant)
			)
			.filter(Optional::isPresent)
			.filter(benutzer -> benutzer.get().getRole() == UserRole.ADMIN_BG)
			.forEach(
				benutzer -> setUserAsDefaultVerantwortlicher(
					gemeindeStammdaten,
					benutzer.get()
				)
			);

		LOG.info("... beendet");
	}

	private void setUserAsDefaultVerantwortlicher(
		@Nonnull GemeindeStammdaten gemeindeStammdaten,
		@Nonnull Benutzer gemeindeBenutzer
	) {
		gemeindeStammdaten.setDefaultBenutzerBG(gemeindeBenutzer);
		gemeindeStammdaten.setDefaultBenutzerTS(gemeindeBenutzer);
		gemeindeService.saveGemeindeStammdaten(gemeindeStammdaten);
	}

	private Gemeinde createGemeindeTutorial(
		Mandant mandant,
		TutorialConstants constants
	) {
		Gemeinde gemeinde = new Gemeinde();
		gemeinde.setId(constants.getGemeindeTutorialId());
		gemeinde.setMandant(mandant);
		gemeinde.setBetreuungsgutscheineStartdatum(Constants.START_OF_TIME);
		gemeinde.setTagesschulanmeldungenStartdatum(LocalDate.of(2018, 8, 1));
		gemeinde.setFerieninselanmeldungenStartdatum(LocalDate.of(2018, 8, 1));
		gemeinde.setName("Gemeinde kiBon " + mandant.getName());
		gemeinde.setStatus(GemeindeStatus.AKTIV);
		gemeinde.setBfsNummer(constants.getGemeindeBfsNr());
		gemeinde.setAngebotBG(true);
		gemeinde.setGemeindeNummer(gemeindeService.getNextGemeindeNummer());

		return gemeindeService.createGemeinde(gemeinde);
	}

	private GemeindeStammdaten createGemeindeStammdatenTutorial(
		@Nonnull Gemeinde gemeinde
	) {
		GemeindeStammdaten stammdaten = new GemeindeStammdaten();
		TutorialConstants constants = schulungConstantsVisitor.process(
			Objects.requireNonNull(gemeinde.getMandant())
		);
		stammdaten.setId(constants.getGemeindeStammdatenTutorialId());
		stammdaten.setGemeinde(gemeinde);
		stammdaten.setKontoinhaber("Tutorial");
		stammdaten.setBic("XXXXCH22");
		stammdaten.setIban(new IBAN("CH9300762011623852957"));
		stammdaten.setAdresse(createAdresse(stammdaten.getId()));
		stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE_FR);
		stammdaten.setMail("gemeinde@example.com");
		stammdaten.setTelefon("0789256896");
		stammdaten.setWebseite("www.tutorialgemeinde.ch");
		GemeindeStammdatenKorrespondenz gemeindeStammdatenKorrespondenz =
			new GemeindeStammdatenKorrespondenz();
		stammdaten.setGemeindeStammdatenKorrespondenz(
			gemeindeStammdatenKorrespondenz
		);

		try (
			InputStream logo = SchulungServiceBean.class
				.getResourceAsStream("/schulung/logo-kibon-bern.png")
		) {
			Objects.requireNonNull(logo);
			final byte[] gemeindeLogo = IOUtils.toByteArray(logo);
			stammdaten.getGemeindeStammdatenKorrespondenz()
				.setLogoContent(gemeindeLogo);
		} catch (IOException e) {
			LOG.info("Logo for Tutorial couldnot be added to Gemeinde");
		}

		stammdaten.setBeschwerdeAdresse(null);

		return gemeindeService.saveGemeindeStammdaten(stammdaten);
	}

	@Nonnull
	private Institution createInstitution(
		@Nonnull String id,
		Mandant mandant
	) {
		Institution institution = new Institution();
		institution.setId(id);
		institution.setName("Kita kiBon");
		institution.setMandant(mandant);
		institution.setStatus(InstitutionStatus.AKTIV);
		return institutionService.createInstitution(institution);
	}

	@SuppressWarnings("MagicNumber")
	private void createInstitutionStammdaten(
		@Nonnull String id,
		@Nonnull Institution institution
	) {
		InstitutionStammdaten instStammdaten = new InstitutionStammdaten();
		instStammdaten.setId(id);
		instStammdaten.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		instStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
		instStammdaten.setAdresse(createAdresse(id));
		instStammdaten.setInstitution(institution);
		instStammdaten.setMail("kita.kibon@example.com");
		InstitutionStammdatenBetreuungsgutscheine institutionStammdatenBetreuungsgutscheine =
			new InstitutionStammdatenBetreuungsgutscheine();
		institutionStammdatenBetreuungsgutscheine.setAnzahlPlaetze(
			BigDecimal.TEN
		);
		Auszahlungsdaten auszahlungsdaten = new Auszahlungsdaten();
		auszahlungsdaten.setIban(new IBAN("CH39 0900 0000 3066 3817 2"));
		auszahlungsdaten.setKontoinhaber("DvBern");
		institutionStammdatenBetreuungsgutscheine.setAuszahlungsdaten(
			auszahlungsdaten
		);
		instStammdaten.setInstitutionStammdatenBetreuungsgutscheine(
			institutionStammdatenBetreuungsgutscheine
		);
		institutionStammdatenService.saveInstitutionStammdaten(
			instStammdaten
		);
	}

	@Nonnull
	private Adresse createAdresse(@Nonnull String id) {
		Adresse adresse = new Adresse();
		adresse.setId(id);
		adresse.setOrganisation("DvBern");
		adresse.setStrasse("Nussbaumstrasse");
		adresse.setHausnummer("21");
		adresse.setPlz("3014");
		adresse.setOrt("Bern");
		adresse.setGueltigkeit(
			new DateRange(LocalDate.now(), Constants.END_OF_TIME)
		);
		return adresse;
	}
}
