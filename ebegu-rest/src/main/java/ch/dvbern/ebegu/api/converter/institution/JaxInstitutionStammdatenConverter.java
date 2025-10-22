/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.converter.institution;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.dtos.JaxAbstractInstitutionStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxEinstellungenFerieninsel;
import ch.dvbern.ebegu.api.dtos.JaxEinstellungenTagesschule;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionExternalClient;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdatenFerieninsel;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdatenTagesschule;
import ch.dvbern.ebegu.api.dtos.JaxModulTagesschule;
import ch.dvbern.ebegu.api.dtos.JaxModulTagesschuleGroup;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.EinstellungenFerieninsel;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.ExternalClient;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.InstitutionStammdatenFerieninsel;
import ch.dvbern.ebegu.entities.InstitutionStammdatenTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.ExternalClientService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.types.InstitutionExternalClientId;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.StreamsUtil;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxInstitutionStammdatenConverter extends AbstractBaseConverter {
	private static final Logger LOGGER = LoggerFactory.getLogger(
		JaxInstitutionStammdatenConverter.class
	);
	@Inject
	private BenutzerService benutzerService;
	@Inject
	private GemeindeService gemeindeService;
	@Inject
	private GesuchsperiodeService gesuchsperiodeService;
	@Inject
	private ExternalClientService externalClientService;

	public <T extends JaxAbstractInstitutionStammdaten> T institutionStammdatenSummaryToJAX(
		@Nonnull final InstitutionStammdaten persistedInstStammdaten,
		@Nonnull final T jaxInstStammdaten
	) {
		convertAbstractDateRangedFieldsToJAX(
			persistedInstStammdaten,
			jaxInstStammdaten
		);

		jaxInstStammdaten.setBetreuungsangebotTyp(
			persistedInstStammdaten.getBetreuungsangebotTyp()
		);
		jaxInstStammdaten.setMail(persistedInstStammdaten.getMail());
		jaxInstStammdaten.setTelefon(persistedInstStammdaten.getTelefon());
		jaxInstStammdaten.setWebseite(persistedInstStammdaten.getWebseite());
		jaxInstStammdaten.setOeffnungszeiten(
			persistedInstStammdaten.getOeffnungszeiten()
		);
		if (persistedInstStammdaten
			.getInstitutionStammdatenBetreuungsgutscheine()
			!= null) {
			jaxInstStammdaten.setInstitutionStammdatenBetreuungsgutscheine(
				institutionStammdatenBetreuungsgutscheineToJAX(
					persistedInstStammdaten
						.getInstitutionStammdatenBetreuungsgutscheine()
				)
			);
		}
		if (persistedInstStammdaten.getInstitutionStammdatenTagesschule()
			!= null) {
			jaxInstStammdaten.setInstitutionStammdatenTagesschule(
				institutionStammdatenTagesschuleToJAX(
					persistedInstStammdaten
						.getInstitutionStammdatenTagesschule()
				)
			);
		}
		if (persistedInstStammdaten.getInstitutionStammdatenFerieninsel()
			!= null) {
			jaxInstStammdaten.setInstitutionStammdatenFerieninsel(
				institutionStammdatenFerieninselToJAX(
					persistedInstStammdaten
						.getInstitutionStammdatenFerieninsel()
				)
			);
		}
		jaxInstStammdaten.setInstitution(
			institutionToJAX(persistedInstStammdaten.getInstitution())
		);
		jaxInstStammdaten.setAdresse(
			adresseToJAX(persistedInstStammdaten.getAdresse())
		);
		jaxInstStammdaten.setSendMailWennOffenePendenzen(
			persistedInstStammdaten.getSendMailWennOffenePendenzen()
		);
		jaxInstStammdaten.setErinnerungMail(
			persistedInstStammdaten.getErinnerungMail()
		);
		jaxInstStammdaten.setGrundSchliessung(
			persistedInstStammdaten.getGrundSchliessung()
		);
		return jaxInstStammdaten;
	}

	public JaxInstitutionStammdaten institutionStammdatenToJAX(
		@Nonnull final InstitutionStammdaten persistedInstStammdaten
	) {
		final JaxInstitutionStammdaten jaxInstStammdaten =
			institutionStammdatenSummaryToJAX(
				persistedInstStammdaten,
				new JaxInstitutionStammdaten()
			);

		Collection<Benutzer> administratoren = benutzerService
			.getInstitutionAdministratoren(
				persistedInstStammdaten.getInstitution()
			);
		Collection<Benutzer> sachbearbeiter = benutzerService
			.getInstitutionSachbearbeiter(
				persistedInstStammdaten.getInstitution()
			);
		jaxInstStammdaten.setAdministratoren(
			administratoren.stream()
				.filter(
					benutzer -> benutzer.getStatus() != BenutzerStatus.GESPERRT
				)
				.map(Benutzer::getFullName)
				.collect(Collectors.joining(", "))
		);
		jaxInstStammdaten.setSachbearbeiter(
			sachbearbeiter.stream()
				.filter(
					benutzer -> benutzer.getStatus() != BenutzerStatus.GESPERRT
				)
				.map(Benutzer::getFullName)
				.collect(Collectors.joining(", "))
		);
		return jaxInstStammdaten;
	}

	public void institutionStammdatenToEntity(
		@Nonnull JaxInstitutionStammdaten institutionStammdatenJAXP,
		@Nonnull InstitutionStammdaten institutionStammdaten
	) {

		requireNonNull(institutionStammdatenJAXP);
		requireNonNull(institutionStammdaten);

		convertAbstractDateRangedFieldsToEntity(
			institutionStammdatenJAXP,
			institutionStammdaten
		);

		institutionStammdaten.setMail(institutionStammdatenJAXP.getMail());
		institutionStammdaten.setTelefon(
			institutionStammdatenJAXP.getTelefon()
		);
		institutionStammdaten.setWebseite(
			institutionStammdatenJAXP.getWebseite()
		);
		institutionStammdaten.setBetreuungsangebotTyp(
			institutionStammdatenJAXP.getBetreuungsangebotTyp()
		);
		if (institutionStammdatenJAXP
			.getInstitutionStammdatenBetreuungsgutscheine()
			!= null) {
			// wenn InstitutionStammdatenBetreuungsgutscheine vorhanden ist es ein BG und Objekt muss, wenn noch
			// nicht vorhanden, erzeugt werden
			InstitutionStammdatenBetreuungsgutscheine isBG =
				Optional.ofNullable(
					institutionStammdaten
						.getInstitutionStammdatenBetreuungsgutscheine()
				)
					.orElseGet(InstitutionStammdatenBetreuungsgutscheine::new);

			InstitutionStammdatenBetreuungsgutscheine convertedIsBG =
				institutionStammdatenBetreuungsgutscheineToEntity(
					institutionStammdatenJAXP
						.getInstitutionStammdatenBetreuungsgutscheine(),
					isBG
				);
			institutionStammdaten.setInstitutionStammdatenBetreuungsgutscheine(
				convertedIsBG
			);
		}
		if (institutionStammdatenJAXP.getInstitutionStammdatenTagesschule()
			!= null) {
			// wenn InstitutionStammdatenTagesschule vorhanden ist es eine Tagesschule und Objekt muss, wenn noch
			// nicht vorhanden, erzeugt werden
			InstitutionStammdatenTagesschule isTS =
				Optional.ofNullable(
					institutionStammdaten.getInstitutionStammdatenTagesschule()
				)
					.orElseGet(InstitutionStammdatenTagesschule::new);

			InstitutionStammdatenTagesschule convertedIsTS =
				institutionStammdatenTagesschuleToEntity(
					institutionStammdatenJAXP
						.getInstitutionStammdatenTagesschule(),
					isTS
				);
			institutionStammdaten.setInstitutionStammdatenTagesschule(
				convertedIsTS
			);
		}
		if (institutionStammdatenJAXP.getInstitutionStammdatenFerieninsel()
			!= null) {
			InstitutionStammdatenFerieninsel isFI =
				Optional.ofNullable(
					institutionStammdaten.getInstitutionStammdatenFerieninsel()
				)
					.orElseGet(InstitutionStammdatenFerieninsel::new);

			InstitutionStammdatenFerieninsel convertedIsFI =
				institutionStammdatenFerieninselToEntity(
					institutionStammdatenJAXP
						.getInstitutionStammdatenFerieninsel(),
					isFI
				);
			institutionStammdaten.setInstitutionStammdatenFerieninsel(
				convertedIsFI
			);
		}
		institutionStammdaten.setSendMailWennOffenePendenzen(
			institutionStammdatenJAXP.isSendMailWennOffenePendenzen()
		);
		institutionStammdaten.setErinnerungMail(
			institutionStammdatenJAXP.getErinnerungMail()
		);
		institutionStammdaten.setGrundSchliessung(
			institutionStammdatenJAXP.getGrundSchliessung()
		);
		adresseToEntity(
			institutionStammdatenJAXP.getAdresse(),
			institutionStammdaten.getAdresse()
		);
	}

	@Nonnull
	public JaxInstitutionStammdatenBetreuungsgutscheine institutionStammdatenBetreuungsgutscheineToJAX(
		@Nonnull final InstitutionStammdatenBetreuungsgutscheine persistedInstStammdaten
	) {
		final JaxInstitutionStammdatenBetreuungsgutscheine jaxInstStammdaten =
			new JaxInstitutionStammdatenBetreuungsgutscheine();
		convertAbstractFieldsToJAX(persistedInstStammdaten, jaxInstStammdaten);

		final IBAN persistedIban = persistedInstStammdaten.extractIban();
		if (persistedIban != null) {
			jaxInstStammdaten.setIban(persistedIban.getIban());
		}
		jaxInstStammdaten.setKontoinhaber(
			persistedInstStammdaten.extractKontoinhaber()
		);
		jaxInstStammdaten.setAlterskategorieBaby(
			persistedInstStammdaten.getAlterskategorieBaby()
		);
		jaxInstStammdaten.setAlterskategorieVorschule(
			persistedInstStammdaten.getAlterskategorieVorschule()
		);
		jaxInstStammdaten.setAlterskategorieKindergarten(
			persistedInstStammdaten.getAlterskategorieKindergarten()
		);
		jaxInstStammdaten.setAlterskategorieSchule(
			persistedInstStammdaten.getAlterskategorieSchule()
		);
		jaxInstStammdaten.setAnzahlPlaetze(
			persistedInstStammdaten.getAnzahlPlaetze()
		);
		jaxInstStammdaten.setAnzahlPlaetzeFirmen(
			persistedInstStammdaten.getAnzahlPlaetzeFirmen()
		);
		jaxInstStammdaten.setTarifProHauptmahlzeit(
			persistedInstStammdaten.getTarifProHauptmahlzeit()
		);
		jaxInstStammdaten.setTarifProNebenmahlzeit(
			persistedInstStammdaten.getTarifProNebenmahlzeit()
		);
		jaxInstStammdaten.setOeffnungstage(
			persistedInstStammdaten.getOeffnungsTage()
		);
		jaxInstStammdaten.setOeffnungsAbweichungen(
			persistedInstStammdaten.getOeffnungsAbweichungen()
		);
		if (persistedInstStammdaten.getOffenVon() != null) {
			jaxInstStammdaten.setOffenVon(
				dateToHoursAndMinutes(persistedInstStammdaten.getOffenVon())
			);
		}
		if (persistedInstStammdaten.getOffenBis() != null) {
			jaxInstStammdaten.setOffenBis(
				dateToHoursAndMinutes(persistedInstStammdaten.getOffenBis())
			);
		}

		final Adresse persistedAdresseKontoinhaber = persistedInstStammdaten
			.extractAdresseKontoinhaber();
		if (persistedAdresseKontoinhaber != null) {
			jaxInstStammdaten.setAdresseKontoinhaber(
				adresseToJAX(persistedAdresseKontoinhaber)
			);
		}
		jaxInstStammdaten.setAlternativeEmailFamilienportal(
			persistedInstStammdaten.getAlternativeEmailFamilienportal()
		);

		jaxInstStammdaten.setOeffnungstageProJahr(
			persistedInstStammdaten.getOeffnungstageProJahr()
		);
		jaxInstStammdaten.setAnzahlKinderWarteliste(
			persistedInstStammdaten.getAnzahlKinderWarteliste()
		);
		jaxInstStammdaten.setDauerWarteliste(
			persistedInstStammdaten.getDauerWarteliste()
		);
		jaxInstStammdaten.setSummePensumWarteliste(
			persistedInstStammdaten.getSummePensumWarteliste()
		);
		jaxInstStammdaten.setFruehEroeffnung(
			persistedInstStammdaten.isFruehEroeffnung()
		);
		jaxInstStammdaten.setSpaetEroeffnung(
			persistedInstStammdaten.isSpaetEroeffnung()
		);
		jaxInstStammdaten.setWochenendeEroeffnung(
			persistedInstStammdaten.isWochenendeEroeffnung()
		);
		jaxInstStammdaten.setUebernachtungMoeglich(
			persistedInstStammdaten.isUebernachtungMoeglich()
		);
		jaxInstStammdaten.setInfomaKreditorennummer(
			persistedInstStammdaten.extractInfomaKreditorennummer()
		);
		jaxInstStammdaten.setInfomaBankcode(
			persistedInstStammdaten.extractInfomaBankcode()
		);

		return jaxInstStammdaten;
	}

	@Nonnull
	public InstitutionStammdatenBetreuungsgutscheine institutionStammdatenBetreuungsgutscheineToEntity(
		@Nonnull final JaxInstitutionStammdatenBetreuungsgutscheine institutionStammdatenJAXP,
		@Nonnull final InstitutionStammdatenBetreuungsgutscheine institutionStammdaten
	) {
		convertAbstractFieldsToEntity(
			institutionStammdatenJAXP,
			institutionStammdaten
		);

		if (institutionStammdatenJAXP.getIban() != null
			|| institutionStammdatenJAXP.getKontoinhaber() != null) {
			Objects.requireNonNull(
				institutionStammdatenJAXP.getIban(),
				"IBAN muss erfasst sein, wenn Mahlzeitenverguenstigung gewunescht"
			);
			Objects.requireNonNull(
				institutionStammdatenJAXP.getKontoinhaber(),
				"Kontoinhaber muss erfasst sein, wenn Mahlzeitenverguenstigung gewunescht"
			);
			if (institutionStammdaten.getAuszahlungsdaten() == null) {
				institutionStammdaten.setAuszahlungsdaten(
					new Auszahlungsdaten()
				);
			}
			institutionStammdaten.getAuszahlungsdaten()
				.setIban(new IBAN(institutionStammdatenJAXP.getIban()));
			institutionStammdaten.getAuszahlungsdaten()
				.setKontoinhaber(institutionStammdatenJAXP.getKontoinhaber());
			institutionStammdaten.getAuszahlungsdaten()
				.setInfomaKreditorennummer(
					institutionStammdatenJAXP.getInfomaKreditorennummer()
				);
			institutionStammdaten.getAuszahlungsdaten()
				.setInfomaBankcode(
					institutionStammdatenJAXP.getInfomaBankcode()
				);
			Adresse convertedAdresse = null;
			if (institutionStammdatenJAXP.getAdresseKontoinhaber() != null) {
				Adresse a = Optional.ofNullable(
					institutionStammdaten.getAuszahlungsdaten()
						.getAdresseKontoinhaber()
				)
					.orElseGet(Adresse::new);
				convertedAdresse = adresseToEntity(
					institutionStammdatenJAXP.getAdresseKontoinhaber(),
					a
				);
			}
			institutionStammdaten.getAuszahlungsdaten()
				.setAdresseKontoinhaber(convertedAdresse);
		}

		institutionStammdaten.setAlterskategorieBaby(
			institutionStammdatenJAXP.isAlterskategorieBaby()
		);
		institutionStammdaten.setAlterskategorieVorschule(
			institutionStammdatenJAXP.isAlterskategorieVorschule()
		);
		institutionStammdaten.setAlterskategorieKindergarten(
			institutionStammdatenJAXP.isAlterskategorieKindergarten()
		);
		institutionStammdaten.setAlterskategorieSchule(
			institutionStammdatenJAXP.isAlterskategorieSchule()
		);
		institutionStammdaten.setAnzahlPlaetze(
			institutionStammdatenJAXP.getAnzahlPlaetze()
		);
		institutionStammdaten.setAnzahlPlaetzeFirmen(
			institutionStammdatenJAXP.getAnzahlPlaetzeFirmen()
		);
		institutionStammdaten.setTarifProHauptmahlzeit(
			institutionStammdatenJAXP.getTarifProHauptmahlzeit()
		);
		institutionStammdaten.setTarifProNebenmahlzeit(
			institutionStammdatenJAXP.getTarifProNebenmahlzeit()
		);
		institutionStammdaten.setOeffnungsTage(
			institutionStammdatenJAXP.getOeffnungstage()
		);
		institutionStammdaten.setOeffnungsAbweichungen(
			institutionStammdatenJAXP.getOeffnungsAbweichungen()
		);
		if (institutionStammdatenJAXP.getOffenVon() != null) {
			institutionStammdaten.setOffenVon(
				hoursAndMinutesToDate(institutionStammdatenJAXP.getOffenVon())
			);
		}
		if (institutionStammdatenJAXP.getOffenBis() != null) {
			institutionStammdaten.setOffenBis(
				hoursAndMinutesToDate(institutionStammdatenJAXP.getOffenBis())
			);
		}
		institutionStammdaten.setAlternativeEmailFamilienportal(
			institutionStammdatenJAXP.getAlternativeEmailFamilienportal()
		);

		institutionStammdaten.setOeffnungstageProJahr(
			institutionStammdatenJAXP.getOeffnungstageProJahr()
		);
		institutionStammdaten.setAnzahlKinderWarteliste(
			institutionStammdatenJAXP.getAnzahlKinderWarteliste()
		);
		institutionStammdaten.setDauerWarteliste(
			institutionStammdatenJAXP.getDauerWarteliste()
		);
		institutionStammdaten.setSummePensumWarteliste(
			institutionStammdatenJAXP.getSummePensumWarteliste()
		);
		institutionStammdaten.setFruehEroeffnung(
			institutionStammdatenJAXP.isFruehEroeffnung()
		);
		institutionStammdaten.setSpaetEroeffnung(
			institutionStammdatenJAXP.isSpaetEroeffnung()
		);
		institutionStammdaten.setWochenendeEroeffnung(
			institutionStammdatenJAXP.isWochenendeEroeffnung()
		);
		institutionStammdaten.setUebernachtungMoeglich(
			institutionStammdatenJAXP.isUebernachtungMoeglich()
		);

		return institutionStammdaten;
	}

	public JaxInstitutionStammdatenTagesschule institutionStammdatenTagesschuleToJAX(
		@Nonnull final InstitutionStammdatenTagesschule persistedInstStammdatenTagesschule
	) {

		final JaxInstitutionStammdatenTagesschule jaxInstStammdatenTagesschule =
			new JaxInstitutionStammdatenTagesschule();
		convertAbstractFieldsToJAX(
			persistedInstStammdatenTagesschule,
			jaxInstStammdatenTagesschule
		);
		jaxInstStammdatenTagesschule.setGemeinde(
			gemeindeToJAX(persistedInstStammdatenTagesschule.getGemeinde())
		);
		jaxInstStammdatenTagesschule.setEinstellungenTagesschule(
			einstellungenTagesschuleListToJAX(
				persistedInstStammdatenTagesschule.getEinstellungenTagesschule()
			)
		);

		return jaxInstStammdatenTagesschule;
	}

	@Nullable
	public InstitutionStammdatenTagesschule institutionStammdatenTagesschuleToEntity(
		final JaxInstitutionStammdatenTagesschule institutionStammdatenTagesschuleJAXP,
		final InstitutionStammdatenTagesschule institutionStammdatenTagesschule
	) {

		requireNonNull(institutionStammdatenTagesschuleJAXP);
		requireNonNull(institutionStammdatenTagesschule);

		convertAbstractFieldsToEntity(
			institutionStammdatenTagesschuleJAXP,
			institutionStammdatenTagesschule
		);

		// Die Gemeinde muss neu von der DB gelesen werden
		String gemeindeID = institutionStammdatenTagesschuleJAXP.getGemeinde()
			.getId();
		Objects.requireNonNull(gemeindeID);
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeID)
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"findGemeinde",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					gemeindeID
				)
			);
		institutionStammdatenTagesschule.setGemeinde(gemeinde);

		final Set<EinstellungenTagesschule> convertedEinstellungenTagesschule =
			einstellungenTagesschuleListToEntity(
				institutionStammdatenTagesschuleJAXP
					.getEinstellungenTagesschule(),
				institutionStammdatenTagesschule.getEinstellungenTagesschule(),
				institutionStammdatenTagesschule
			);
		//change the existing collection to reflect changes
		// Already tested: All existing module of the list remain as they were, that means their data are updated
		// and the objects are not created again. ID and InsertTimeStamp are the same as before
		institutionStammdatenTagesschule.getEinstellungenTagesschule().clear();
		institutionStammdatenTagesschule.getEinstellungenTagesschule()
			.addAll(convertedEinstellungenTagesschule);
		return institutionStammdatenTagesschule;
	}

	@Nonnull
	public JaxInstitutionStammdatenFerieninsel institutionStammdatenFerieninselToJAX(
		@Nonnull final InstitutionStammdatenFerieninsel persistedInstStammdatenFerieninsel
	) {

		final JaxInstitutionStammdatenFerieninsel jaxInstStammdatenFerieninsel =
			new JaxInstitutionStammdatenFerieninsel();
		convertAbstractFieldsToJAX(
			persistedInstStammdatenFerieninsel,
			jaxInstStammdatenFerieninsel
		);
		jaxInstStammdatenFerieninsel.setGemeinde(
			gemeindeToJAX(persistedInstStammdatenFerieninsel.getGemeinde())
		);

		jaxInstStammdatenFerieninsel.setEinstellungenFerieninsel(
			persistedInstStammdatenFerieninsel.getEinstellungenFerieninsel()
				.stream()
				.map(this::einstellungFerieninselToJAX)
				.collect(Collectors.toSet())
		);

		return jaxInstStammdatenFerieninsel;
	}

	@Nonnull
	private JaxEinstellungenFerieninsel einstellungFerieninselToJAX(
		@Nonnull final EinstellungenFerieninsel persistedEinstellungFerieninsel
	) {
		JaxEinstellungenFerieninsel jaxEinstellungFI =
			new JaxEinstellungenFerieninsel();
		convertAbstractFieldsToJAX(
			persistedEinstellungFerieninsel,
			jaxEinstellungFI
		);
		jaxEinstellungFI.setAusweichstandortFruehlingsferien(
			persistedEinstellungFerieninsel
				.getAusweichstandortFruehlingsferien()
		);
		jaxEinstellungFI.setAusweichstandortHerbstferien(
			persistedEinstellungFerieninsel.getAusweichstandortHerbstferien()
		);
		jaxEinstellungFI.setAusweichstandortSommerferien(
			persistedEinstellungFerieninsel.getAusweichstandortSommerferien()
		);
		jaxEinstellungFI.setAusweichstandortSportferien(
			persistedEinstellungFerieninsel.getAusweichstandortSportferien()
		);
		jaxEinstellungFI.setGesuchsperiode(
			gesuchsperiodeToJAX(
				persistedEinstellungFerieninsel.getGesuchsperiode()
			)
		);

		return jaxEinstellungFI;
	}

	@Nullable
	public InstitutionStammdatenFerieninsel institutionStammdatenFerieninselToEntity(
		final JaxInstitutionStammdatenFerieninsel institutionStammdatenFerieninselJAXP,
		final InstitutionStammdatenFerieninsel institutionStammdatenFerieninsel
	) {

		requireNonNull(institutionStammdatenFerieninselJAXP);
		requireNonNull(institutionStammdatenFerieninsel);

		convertAbstractFieldsToEntity(
			institutionStammdatenFerieninselJAXP,
			institutionStammdatenFerieninsel
		);

		// Die Gemeinde muss neu von der DB gelesen werden
		String gemeindeID = institutionStammdatenFerieninselJAXP.getGemeinde()
			.getId();
		Objects.requireNonNull(gemeindeID);
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeID)
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"findGemeinde",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					gemeindeID
				)
			);
		institutionStammdatenFerieninsel.setGemeinde(gemeinde);

		Set<EinstellungenFerieninsel> convertedEinstellungenFerieninsel =
			einstellungenTagesschuleListToEntity(
				institutionStammdatenFerieninselJAXP
					.getEinstellungenFerieninsel(),
				institutionStammdatenFerieninsel.getEinstellungenFerieninsel(),
				institutionStammdatenFerieninsel
			);

		institutionStammdatenFerieninsel.getEinstellungenFerieninsel().clear();
		institutionStammdatenFerieninsel.getEinstellungenFerieninsel()
			.addAll(convertedEinstellungenFerieninsel);

		return institutionStammdatenFerieninsel;
	}

	@Nonnull
	private Set<EinstellungenFerieninsel> einstellungenTagesschuleListToEntity(
		@Nonnull Set<JaxEinstellungenFerieninsel> jaxEinstellungenFerieninselSet,
		@Nonnull Set<EinstellungenFerieninsel> einstellungenFerieninselSet,
		@Nonnull InstitutionStammdatenFerieninsel owner
	) {

		final Set<EinstellungenFerieninsel> convertedEinstellungen =
			new TreeSet<>();
		for (final JaxEinstellungenFerieninsel jaxEinstellung : jaxEinstellungenFerieninselSet) {
			final EinstellungenFerieninsel einstellungenToMergeWith =
				einstellungenFerieninselSet
					.stream()
					.filter(
						existingEinstellung -> existingEinstellung.getId()
							.equals(jaxEinstellung.getId())
					)
					.reduce(StreamsUtil.toOnlyElement())
					.orElseGet(EinstellungenFerieninsel::new);
			final EinstellungenFerieninsel einstellungToAdd =
				einstellungFerieninselToEntity(
					jaxEinstellung,
					einstellungenToMergeWith
				);
			einstellungToAdd.setInstitutionStammdatenFerieninsel(owner);
			final boolean added = convertedEinstellungen.add(einstellungToAdd);
			if (!added) {
				LOGGER.warn(
					"dropped duplicate EinstellungenTagesschule {}",
					einstellungToAdd
				);
			}
		}
		return convertedEinstellungen;
	}

	@Nonnull
	private EinstellungenFerieninsel einstellungFerieninselToEntity(
		@Nonnull final JaxEinstellungenFerieninsel jaxEinstellungFerieninsel,
		@Nonnull EinstellungenFerieninsel einstellungFerieninsel
	) {

		convertAbstractFieldsToEntity(
			jaxEinstellungFerieninsel,
			einstellungFerieninsel
		);

		einstellungFerieninsel.setAusweichstandortFruehlingsferien(
			jaxEinstellungFerieninsel.getAusweichstandortFruehlingsferien()
		);
		einstellungFerieninsel.setAusweichstandortHerbstferien(
			jaxEinstellungFerieninsel.getAusweichstandortHerbstferien()
		);
		einstellungFerieninsel.setAusweichstandortSommerferien(
			jaxEinstellungFerieninsel.getAusweichstandortSommerferien()
		);
		einstellungFerieninsel.setAusweichstandortSportferien(
			jaxEinstellungFerieninsel.getAusweichstandortSportferien()
		);

		// Die Gesuchsperiode muss neu von der DB gelesen werden
		String gesuchsperiodeId = jaxEinstellungFerieninsel.getGesuchsperiode()
			.getId();
		Objects.requireNonNull(gesuchsperiodeId);
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService
			.findGesuchsperiode(gesuchsperiodeId)
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"einstellungenTagesschuleToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					gesuchsperiodeId
				)
			);
		einstellungFerieninsel.setGesuchsperiode(gesuchsperiode);

		return einstellungFerieninsel;
	}

	@Nonnull
	public List<JaxInstitutionExternalClient> institutionExternalClientsToJAX(
		@Nonnull Collection<InstitutionExternalClient> institutionExternalClients
	) {
		return institutionExternalClients.stream()
			.map(this::insitutionExternalClientToJAX)
			.collect(Collectors.toList());
	}

	@Nonnull
	public JaxInstitutionExternalClient insitutionExternalClientToJAX(
		@Nonnull final InstitutionExternalClient persistedInstitutionExternalClient
	) {
		JaxInstitutionExternalClient jaxInstitutionExternalClient =
			new JaxInstitutionExternalClient();
		jaxInstitutionExternalClient.setExternalClient(
			externalClientToJAX(
				persistedInstitutionExternalClient.getExternalClient()
			)
		);
		jaxInstitutionExternalClient.setGueltigAb(
			persistedInstitutionExternalClient.getGueltigkeit().getGueltigAb()
		);
		if (Constants.END_OF_TIME.equals(
			persistedInstitutionExternalClient.getGueltigkeit().getGueltigBis()
		)) {
			jaxInstitutionExternalClient.setGueltigBis(null); // end of time gueltigkeit wird nicht an client geschickt
		} else {
			jaxInstitutionExternalClient.setGueltigBis(
				persistedInstitutionExternalClient.getGueltigkeit()
					.getGueltigBis()
			);
		}
		return jaxInstitutionExternalClient;
	}

	@Nonnull
	public List<InstitutionExternalClient> institutionExternalClientListToEntity(
		@Nonnull Collection<JaxInstitutionExternalClient> jaxInstitutionExternalClients,
		@Nonnull Institution institution
	) {
		return jaxInstitutionExternalClients.stream()
			.map(
				jaxInstitutionExternalClient -> insitutionExternalClientToEntity(
					jaxInstitutionExternalClient,
					institution
				)
			)
			.collect(Collectors.toList());
	}

	@Nonnull
	public InstitutionExternalClient insitutionExternalClientToEntity(
		@Nonnull final JaxInstitutionExternalClient jaxInstitutionExternalClient,
		@Nonnull Institution institution
	) {
		InstitutionExternalClient institutionExternalClient =
			new InstitutionExternalClient();
		String externalClientID = jaxInstitutionExternalClient
			.getExternalClient()
			.getId();
		requireNonNull(
			externalClientID,
			"Die ExternalClient Daten muessen gesetzt sein"
		);
		ExternalClient selectedClient =
			externalClientService.findExternalClient(externalClientID)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"insitutionExternalClientToEntity",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						jaxInstitutionExternalClient.getExternalClient().getId()
					)
				);
		institutionExternalClient.setExternalClient(selectedClient);
		institutionExternalClient.setInstitution(institution);
		institutionExternalClient.setId(
			new InstitutionExternalClientId(
				institution.getId(),
				selectedClient.getId()
			)
		);

		final LocalDate dateAb =
			jaxInstitutionExternalClient.getGueltigAb();
		requireNonNull(dateAb, "Die GueltigAb Datum muss gesetzt sein");
		final LocalDate dateBis =
			jaxInstitutionExternalClient.getGueltigBis() == null ?
				Constants.END_OF_TIME :
				jaxInstitutionExternalClient.getGueltigBis();
		institutionExternalClient.setGueltigkeit(
			new DateRange(dateAb, dateBis)
		);
		return institutionExternalClient;
	}

	@Nonnull
	private Set<JaxEinstellungenTagesschule> einstellungenTagesschuleListToJAX(
		@Nullable final Set<EinstellungenTagesschule> einstellungenTagesschuleSet
	) {
		if (einstellungenTagesschuleSet == null) {
			return Collections.emptySet();
		}
		return einstellungenTagesschuleSet.stream()
			.map(this::einstellungenTagesschuleToJAX)
			.collect(Collectors.toSet());
	}

	@Nonnull
	private JaxEinstellungenTagesschule einstellungenTagesschuleToJAX(
		@Nonnull final EinstellungenTagesschule persistedEinstellungenTagesschule
	) {
		final JaxEinstellungenTagesschule jaxEinstellungenTagesschule =
			new JaxEinstellungenTagesschule();

		convertAbstractFieldsToJAX(
			persistedEinstellungenTagesschule,
			jaxEinstellungenTagesschule
		);
		jaxEinstellungenTagesschule.setGesuchsperiode(
			gesuchsperiodeToJAX(
				persistedEinstellungenTagesschule.getGesuchsperiode()
			)
		);
		jaxEinstellungenTagesschule.setModulTagesschuleGroups(
			modulTagesschuleGroupListToJax(
				persistedEinstellungenTagesschule.getModulTagesschuleGroups()
			)
		);
		jaxEinstellungenTagesschule.setModulTagesschuleTyp(
			persistedEinstellungenTagesschule.getModulTagesschuleTyp()
		);
		jaxEinstellungenTagesschule.setErlaeuterung(
			persistedEinstellungenTagesschule.getErlaeuterung()
		);
		jaxEinstellungenTagesschule.setTagi(
			persistedEinstellungenTagesschule.isTagi()
		);
		return jaxEinstellungenTagesschule;
	}

	@Nonnull
	private List<JaxModulTagesschuleGroup> modulTagesschuleGroupListToJax(
		@Nullable final Set<ModulTagesschuleGroup> module
	) {
		if (module == null) {
			return Collections.emptyList();
		}
		return module.stream()
			.map(this::modulTagesschuleGroupToJAX)
			.collect(Collectors.toList());
	}

	@Nonnull
	public JaxModulTagesschuleGroup modulTagesschuleGroupToJAX(
		@Nonnull ModulTagesschuleGroup modulTagesschuleGroup
	) {
		final JaxModulTagesschuleGroup jaxModulTagesschuleGroup =
			new JaxModulTagesschuleGroup();
		convertAbstractFieldsToJAX(
			modulTagesschuleGroup,
			jaxModulTagesschuleGroup
		);
		jaxModulTagesschuleGroup.setModulTagesschuleName(
			modulTagesschuleGroup.getModulTagesschuleName()
		);
		jaxModulTagesschuleGroup.setIdentifier(
			modulTagesschuleGroup.getIdentifier()
		);
		jaxModulTagesschuleGroup.setFremdId(modulTagesschuleGroup.getFremdId());
		jaxModulTagesschuleGroup.setBezeichnung(
			textRessourceToJAX(modulTagesschuleGroup.getBezeichnung())
		);
		jaxModulTagesschuleGroup.setZeitVon(
			dateToHoursAndMinutes(modulTagesschuleGroup.getZeitVon())
		);
		jaxModulTagesschuleGroup.setZeitBis(
			dateToHoursAndMinutes(modulTagesschuleGroup.getZeitBis())
		);
		jaxModulTagesschuleGroup.setVerpflegungskosten(
			modulTagesschuleGroup.getVerpflegungskosten()
		);
		jaxModulTagesschuleGroup.setIntervall(
			modulTagesschuleGroup.getIntervall()
		);
		jaxModulTagesschuleGroup.setWirdPaedagogischBetreut(
			modulTagesschuleGroup.isWirdPaedagogischBetreut()
		);
		jaxModulTagesschuleGroup.setReihenfolge(
			modulTagesschuleGroup.getReihenfolge()
		);
		jaxModulTagesschuleGroup.setModule(
			moduleTagesschuleListToJax(modulTagesschuleGroup.getModule())
		);
		return jaxModulTagesschuleGroup;
	}

	@Nonnull
	private Set<EinstellungenTagesschule> einstellungenTagesschuleListToEntity(
		@Nonnull Set<JaxEinstellungenTagesschule> jaxEinstellungenTagesschuleSet,
		@Nonnull Set<EinstellungenTagesschule> einstellungenTagesschuleSet,
		@Nonnull InstitutionStammdatenTagesschule owner
	) {

		final Set<EinstellungenTagesschule> convertedEinstellungen =
			new TreeSet<>();
		for (final JaxEinstellungenTagesschule jaxEinstellung : jaxEinstellungenTagesschuleSet) {
			final EinstellungenTagesschule einstellungenToMergeWith =
				einstellungenTagesschuleSet
					.stream()
					.filter(
						existingEinstellung -> existingEinstellung.getId()
							.equals(jaxEinstellung.getId())
					)
					.reduce(StreamsUtil.toOnlyElement())
					.orElseGet(EinstellungenTagesschule::new);
			final EinstellungenTagesschule einstellungToAdd =
				einstellungenTagesschuleToEntity(
					jaxEinstellung,
					einstellungenToMergeWith
				);
			einstellungToAdd.setInstitutionStammdatenTagesschule(owner);
			final boolean added = convertedEinstellungen.add(einstellungToAdd);
			if (!added) {
				LOGGER.warn(
					"dropped duplicate EinstellungenTagesschule {}",
					einstellungToAdd
				);
			}
		}
		return convertedEinstellungen;
	}

	@Nonnull
	private EinstellungenTagesschule einstellungenTagesschuleToEntity(
		final JaxEinstellungenTagesschule jaxEinstellungenTagesschule,
		final EinstellungenTagesschule einstellungenTagesschule
	) {
		requireNonNull(jaxEinstellungenTagesschule);
		requireNonNull(einstellungenTagesschule);

		convertAbstractFieldsToEntity(
			jaxEinstellungenTagesschule,
			einstellungenTagesschule
		);

		// Die Gesuchsperiode muss neu von der DB gelesen werden
		String gesuchsperiodeId = jaxEinstellungenTagesschule
			.getGesuchsperiode()
			.getId();
		Objects.requireNonNull(gesuchsperiodeId);
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService
			.findGesuchsperiode(gesuchsperiodeId)
			.orElseThrow(
				() -> new EbeguRuntimeException(
					"einstellungenTagesschuleToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					gesuchsperiodeId
				)
			);
		einstellungenTagesschule.setGesuchsperiode(gesuchsperiode);

		final Set<ModulTagesschuleGroup> convertedModuleTagesschule =
			modulTagesschuleGroupListToEntity(
				jaxEinstellungenTagesschule.getModulTagesschuleGroups(),
				einstellungenTagesschule.getModulTagesschuleGroups(),
				einstellungenTagesschule
			);
		if (convertedModuleTagesschule != null) {
			//change the existing collection to reflect changes
			// Already tested: All existing module of the list remain as they were, that means their data are updated
			// and the objects are not created again. ID and InsertTimeStamp are the same as before
			einstellungenTagesschule.getModulTagesschuleGroups().clear();
			einstellungenTagesschule.getModulTagesschuleGroups()
				.addAll(convertedModuleTagesschule);
		}

		einstellungenTagesschule.setModulTagesschuleTyp(
			jaxEinstellungenTagesschule.getModulTagesschuleTyp()
		);
		einstellungenTagesschule.setErlaeuterung(
			jaxEinstellungenTagesschule.getErlaeuterung()
		);
		einstellungenTagesschule.setTagi(jaxEinstellungenTagesschule.isTagi());

		return einstellungenTagesschule;
	}

	@Nullable
	private ModulTagesschuleGroup modulTagesschuleGroupToEntity(
		@Nullable JaxModulTagesschuleGroup jaxModulTagesschuleGroup,
		@Nonnull ModulTagesschuleGroup modulTagesschuleGroup,
		@Nonnull EinstellungenTagesschule einstellungenTagesschule
	) {

		if (jaxModulTagesschuleGroup == null) {
			return null;
		}

		convertAbstractFieldsToEntity(
			jaxModulTagesschuleGroup,
			modulTagesschuleGroup
		);
		modulTagesschuleGroup.setEinstellungenTagesschule(
			einstellungenTagesschule
		);
		modulTagesschuleGroup.setModulTagesschuleName(
			jaxModulTagesschuleGroup.getModulTagesschuleName()
		);
		modulTagesschuleGroup.setIdentifier(
			jaxModulTagesschuleGroup.getIdentifier()
		);
		modulTagesschuleGroup.setFremdId(jaxModulTagesschuleGroup.getFremdId());
		modulTagesschuleGroup.setBezeichnung(
			textRessourceToEntity(
				jaxModulTagesschuleGroup.getBezeichnung(),
				modulTagesschuleGroup.getBezeichnung()
			)
		);
		modulTagesschuleGroup.setZeitVon(
			hoursAndMinutesToDate(jaxModulTagesschuleGroup.getZeitVon())
		);
		modulTagesschuleGroup.setZeitBis(
			hoursAndMinutesToDate(jaxModulTagesschuleGroup.getZeitBis())
		);
		modulTagesschuleGroup.setVerpflegungskosten(
			jaxModulTagesschuleGroup.getVerpflegungskosten()
		);
		modulTagesschuleGroup.setIntervall(
			jaxModulTagesschuleGroup.getIntervall()
		);
		modulTagesschuleGroup.setWirdPaedagogischBetreut(
			jaxModulTagesschuleGroup.isWirdPaedagogischBetreut()
		);
		modulTagesschuleGroup.setReihenfolge(
			jaxModulTagesschuleGroup.getReihenfolge()
		);

		Set<ModulTagesschule> convertedModules = moduleTagesschuleListToEntity(
			jaxModulTagesschuleGroup.getModule(),
			modulTagesschuleGroup.getModule()
		);
		if (convertedModules != null) {
			for (ModulTagesschule convertedModule : convertedModules) {
				convertedModule.setModulTagesschuleGroup(modulTagesschuleGroup);
			}
		}
		if (convertedModules != null) {
			modulTagesschuleGroup.getModule().clear();
			modulTagesschuleGroup.getModule().addAll(convertedModules);
		}

		return modulTagesschuleGroup;
	}

	@Nullable
	private Set<ModulTagesschuleGroup> modulTagesschuleGroupListToEntity(
		@Nullable List<JaxModulTagesschuleGroup> jaxModulTagesschuleGroups,
		@Nullable Set<ModulTagesschuleGroup> modulTagesschuleGroupsOfInstitution,
		@Nonnull EinstellungenTagesschule institutionStammdatenTagesschule
	) {

		if (modulTagesschuleGroupsOfInstitution != null
			&& jaxModulTagesschuleGroups != null) {
			final Set<ModulTagesschuleGroup> transformedModule =
				new TreeSet<>();
			for (final JaxModulTagesschuleGroup jaxModulTagesschule : jaxModulTagesschuleGroups) {
				final ModulTagesschuleGroup modulTagesschuleToMergeWith =
					modulTagesschuleGroupsOfInstitution.stream()
						.filter(
							existingModul -> existingModul.getId()
								.equalsIgnoreCase(jaxModulTagesschule.getId())
						)
						.reduce(StreamsUtil.toOnlyElement())
						.orElse(new ModulTagesschuleGroup());
				final ModulTagesschuleGroup modulTagesschuleToAdd =
					modulTagesschuleGroupToEntity(
						jaxModulTagesschule,
						modulTagesschuleToMergeWith,
						institutionStammdatenTagesschule
					);
				if (modulTagesschuleToAdd != null) {
					final boolean added = transformedModule.add(
						modulTagesschuleToAdd
					);
					if (!added) {
						LOGGER.warn(
							DROPPED_DUPLICATE_CONTAINER + "{}",
							modulTagesschuleToAdd
						);
					}
				}
			}
			return transformedModule;
		}
		return null;
	}

	@Nullable
	private Set<ModulTagesschule> moduleTagesschuleListToEntity(
		@Nullable Set<JaxModulTagesschule> jaxModuleTagesschule,
		@Nullable Set<ModulTagesschule> moduleOfInstitution
	) {

		if (moduleOfInstitution != null && jaxModuleTagesschule != null) {
			final Set<ModulTagesschule> transformedModule = new TreeSet<>();
			for (final JaxModulTagesschule jaxModulTagesschule : jaxModuleTagesschule) {
				final ModulTagesschule modulTagesschuleToMergeWith =
					moduleOfInstitution.stream()
						.filter(
							existingModul -> existingModul.getId()
								.equalsIgnoreCase(jaxModulTagesschule.getId())
						)
						.reduce(StreamsUtil.toOnlyElement())
						.orElse(new ModulTagesschule());
				final ModulTagesschule modulTagesschuleToAdd =
					modulTagesschuleToEntity(
						jaxModulTagesschule,
						modulTagesschuleToMergeWith
					);
				if (modulTagesschuleToAdd != null) {
					final boolean added = transformedModule.add(
						modulTagesschuleToAdd
					);
					if (!added) {
						LOGGER.warn(
							DROPPED_DUPLICATE_CONTAINER + "{}",
							modulTagesschuleToAdd
						);
					}
				}
			}
			return transformedModule;
		}
		return null;
	}

	@Nullable
	private ModulTagesschule modulTagesschuleToEntity(
		@Nullable JaxModulTagesschule jaxModulTagesschule,
		@Nonnull ModulTagesschule modulTagesschule
	) {

		if (jaxModulTagesschule == null) {
			return null;
		}
		convertAbstractFieldsToEntity(jaxModulTagesschule, modulTagesschule);
		modulTagesschule.setWochentag(jaxModulTagesschule.getWochentag());
		return modulTagesschule;
	}

	private LocalTime hoursAndMinutesToDate(@Nonnull String hoursAndMinutes) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
			"H:mm"
		);
		return LocalTime.parse(hoursAndMinutes, dateTimeFormatter);
	}

	private String dateToHoursAndMinutes(@Nonnull LocalTime date) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
			"H:mm"
		);
		return date.format(dateTimeFormatter);
	}
}
