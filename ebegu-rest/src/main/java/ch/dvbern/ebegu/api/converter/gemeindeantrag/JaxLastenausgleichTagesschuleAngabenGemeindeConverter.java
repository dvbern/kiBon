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

package ch.dvbern.ebegu.api.converter.gemeindeantrag;

import java.util.Set;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import ch.dvbern.ebegu.api.converter.AbstractBaseSonderConverter;
import ch.dvbern.ebegu.api.converter.JaxBenutzerConverter;
import ch.dvbern.ebegu.api.dtos.JaxLastenausgleichTagesschulenStatusHistory;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxLastenausgleichTagesschuleAngabenGemeinde;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxLastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxLastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.lastenausgleichtagesschulen.LastenausgleichTagesschuleAngabenGemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.lastenausgleichtagesschulen.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.lastenausgleichtagesschulen.LastenausgleichTagesschuleAngabenGemeindeStatusHistory;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import org.hibernate.StaleObjectStateException;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxLastenausgleichTagesschuleAngabenGemeindeConverter extends
	AbstractBaseSonderConverter {
	@Inject
	private GemeindeService gemeindeService;
	@Inject
	private GesuchsperiodeService gesuchsperiodeService;
	@Inject
	private BenutzerService benutzerService;
	@Inject
	private JaxBenutzerConverter jaxBenutzerConverter;
	@Inject
	private JaxLastenausgleichTagesschuleAngabenInstitutionConverter lastenausgleichTagesschuleAngabenInstitutionConverter;

	@Nonnull
	public JaxLastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleAngabenGemeindeContainerToJax(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer gemeindeContainer
	) {
		// OptimisticLocking: Version richtig behandeln
		flush();
		JaxLastenausgleichTagesschuleAngabenGemeindeContainer jaxGemeindeContainer =
			new JaxLastenausgleichTagesschuleAngabenGemeindeContainer();
		convertAbstractFieldsToJAX(gemeindeContainer, jaxGemeindeContainer);

		jaxGemeindeContainer.setStatus(gemeindeContainer.getStatus());
		jaxGemeindeContainer.setGemeinde(
			gemeindeToJAX(gemeindeContainer.getGemeinde())
		);
		jaxGemeindeContainer.setGesuchsperiode(
			gesuchsperiodeToJAX(gemeindeContainer.getGesuchsperiode())
		);
		jaxGemeindeContainer.setAlleAngabenInKibonErfasst(
			gemeindeContainer.getAlleAngabenInKibonErfasst()
		);
		jaxGemeindeContainer.setInternerKommentar(
			gemeindeContainer.getInternerKommentar()
		);
		if (gemeindeContainer.getVerantwortlicher() != null) {
			jaxGemeindeContainer.setVerantwortlicher(
				jaxBenutzerConverter.benutzerToJaxBenutzerNoDetails(
					gemeindeContainer.getVerantwortlicher()
				)
			);
		}
		if (gemeindeContainer.getAngabenDeklaration() != null) {
			jaxGemeindeContainer.setAngabenDeklaration(
				lastenausgleichTagesschuleAngabenGemeindeToJax(
					gemeindeContainer.getAngabenDeklaration()
				)
			);
		}
		if (gemeindeContainer.getAngabenKorrektur() != null) {
			jaxGemeindeContainer.setAngabenKorrektur(
				lastenausgleichTagesschuleAngabenGemeindeToJax(
					gemeindeContainer.getAngabenKorrektur()
				)
			);
		}
		final Set<JaxLastenausgleichTagesschuleAngabenInstitutionContainer> institutionContainerList =
			lastenausgleichTagesschuleAngabenInstitutionConverter
				.lastenausgleichTagesschuleAngabenInstitutionContainerListToJax(
					gemeindeContainer.getAngabenInstitutionContainers()
				);
		jaxGemeindeContainer.setAngabenInstitutionContainers(
			institutionContainerList
		);
		jaxGemeindeContainer.setBetreuungsstundenPrognose(
			gemeindeContainer.getBetreuungsstundenPrognose()
		);
		jaxGemeindeContainer.setBemerkungenBetreuungsstundenPrognose(
			gemeindeContainer.getBemerkungenBetreuungsstundenPrognose()
		);

		return jaxGemeindeContainer;
	}

	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleAngabenGemeindeContainerToEntity(
		@Nonnull JaxLastenausgleichTagesschuleAngabenGemeindeContainer jaxGemeindeContainer,
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer gemeindeContainer
	) {
		requireNonNull(jaxGemeindeContainer.getGemeinde().getId());
		requireNonNull(jaxGemeindeContainer.getGesuchsperiode().getId());

		convertAbstractFieldsToJAX(gemeindeContainer, jaxGemeindeContainer);

		// Die Gemeinde und die Periode duerfen nie vom Client uebernommen werden
		gemeindeService.findGemeinde(jaxGemeindeContainer.getGemeinde().getId())
			.ifPresent(gemeindeContainer::setGemeinde);
		gesuchsperiodeService.findGesuchsperiode(
			jaxGemeindeContainer.getGesuchsperiode().getId()
		)
			.ifPresent(gemeindeContainer::setGesuchsperiode);

		gemeindeContainer.setAlleAngabenInKibonErfasst(
			jaxGemeindeContainer.getAlleAngabenInKibonErfasst()
		);
		gemeindeContainer.setInternerKommentar(
			jaxGemeindeContainer.getInternerKommentar()
		);

		if (jaxGemeindeContainer.getVerantwortlicher() != null) {
			benutzerService.findBenutzer(
				jaxGemeindeContainer.getVerantwortlicher().getUsername(),
				gemeindeContainer.getGemeinde().getMandant()
			)
				.ifPresent(gemeindeContainer::setVerantwortlicher);
		}

		if (jaxGemeindeContainer.getAngabenDeklaration() != null) {
			if (gemeindeContainer.getAngabenDeklaration() != null) {
				gemeindeContainer.setAngabenDeklaration(
					lastenausgleichTagesschuleAngabenGemeindeToEntity(
						jaxGemeindeContainer.getAngabenDeklaration(),
						gemeindeContainer.getAngabenDeklaration()
					)
				);
			} else {
				gemeindeContainer.setAngabenDeklaration(
					new LastenausgleichTagesschuleAngabenGemeinde()
				);
			}
		}
		if (jaxGemeindeContainer.getAngabenKorrektur() != null) {
			if (gemeindeContainer.getAngabenKorrektur() != null) {
				gemeindeContainer.setAngabenKorrektur(
					lastenausgleichTagesschuleAngabenGemeindeToEntity(
						jaxGemeindeContainer.getAngabenKorrektur(),
						gemeindeContainer.getAngabenKorrektur()
					)
				);
			} else {
				gemeindeContainer.setAngabenKorrektur(
					new LastenausgleichTagesschuleAngabenGemeinde()
				);
			}
		}

		jaxGemeindeContainer.getAngabenInstitutionContainers()
			.stream()
			.map(
				jaxContainer -> lastenausgleichTagesschuleAngabenInstitutionConverter
					.lastenausgleichTagesschuleAngabenInstitutionContainerToStorableEntity(
						jaxContainer,
						false
					)
			)
			.forEach(
				gemeindeContainer::addLastenausgleichTagesschuleAngabenInstitutionContainer
			);

		// We don't set the betreuungsstundenPrognose and internerKommentar here, we have dedicated calls

		return gemeindeContainer;
	}

	@Nonnull
	public JaxLastenausgleichTagesschuleAngabenGemeinde lastenausgleichTagesschuleAngabenGemeindeToJax(
		@Nonnull LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde
	) {

		// OptimisticLocking: Version richtig behandeln
		flush();

		JaxLastenausgleichTagesschuleAngabenGemeinde jaxAngabenGemeinde =
			new JaxLastenausgleichTagesschuleAngabenGemeinde();
		convertAbstractFieldsToJAX(angabenGemeinde, jaxAngabenGemeinde);

		jaxAngabenGemeinde.setStatus(angabenGemeinde.getStatus());

		// A: Allgemeine Angaben
		jaxAngabenGemeinde.setBedarfBeiElternAbgeklaert(
			angabenGemeinde.getBedarfBeiElternAbgeklaert()
		);
		jaxAngabenGemeinde.setAngebotFuerFerienbetreuungVorhanden(
			angabenGemeinde.getAngebotFuerFerienbetreuungVorhanden()
		);
		jaxAngabenGemeinde.setAngebotVerfuegbarFuerAlleSchulstufen(
			angabenGemeinde.getAngebotVerfuegbarFuerAlleSchulstufen()
		);
		jaxAngabenGemeinde
			.setBegruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen(
				angabenGemeinde
					.getBegruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen()
			);
		// B: Abrechnung
		jaxAngabenGemeinde
			.setGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse(
				angabenGemeinde
					.getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse()
			);
		jaxAngabenGemeinde.setGeleisteteBetreuungsstundenBesondereBeduerfnisse(
			angabenGemeinde
				.getGeleisteteBetreuungsstundenBesondereBeduerfnisse()
		);
		jaxAngabenGemeinde
			.setGeleisteteBetreuungsstundenBesondereVolksschulangebot(
				angabenGemeinde
					.getGeleisteteBetreuungsstundenBesondereVolksschulangebot()
			);
		jaxAngabenGemeinde
			.setDavonStundenZuNormlohnMehrAls50ProzentAusgebildete(
				angabenGemeinde
					.getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete()
			);
		jaxAngabenGemeinde
			.setDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete(
				angabenGemeinde
					.getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete()
			);
		jaxAngabenGemeinde.setEinnahmenElterngebuehren(
			angabenGemeinde.getEinnahmenElterngebuehren()
		);
		jaxAngabenGemeinde.setEinnahmenElterngebuehrenVolksschulangebot(
			angabenGemeinde.getEinnahmenElterngebuehrenVolksschulangebot()
		);
		jaxAngabenGemeinde.setTagesschuleTeilweiseGeschlossen(
			angabenGemeinde.getTagesschuleTeilweiseGeschlossen()
		);
		jaxAngabenGemeinde.setRueckerstattungenElterngebuehrenSchliessung(
			angabenGemeinde.getRueckerstattungenElterngebuehrenSchliessung()
		);
		jaxAngabenGemeinde.setErsteRateAusbezahlt(
			angabenGemeinde.getErsteRateAusbezahlt()
		);
		// C: Kostenbeteiligung Gemeinde
		jaxAngabenGemeinde.setGesamtKostenTagesschule(
			angabenGemeinde.getGesamtKostenTagesschule()
		);
		jaxAngabenGemeinde.setEinnnahmenVerpflegung(
			angabenGemeinde.getEinnnahmenVerpflegung()
		);
		jaxAngabenGemeinde.setEinnahmenSubventionenDritter(
			angabenGemeinde.getEinnahmenSubventionenDritter()
		);
		jaxAngabenGemeinde.setUeberschussErzielt(
			angabenGemeinde.getUeberschussErzielt()
		);
		jaxAngabenGemeinde.setUeberschussVerwendung(
			angabenGemeinde.getUeberschussVerwendung()
		);
		// D: Angaben zu weiteren Kosten und Ertraegen
		jaxAngabenGemeinde.setBemerkungenWeitereKostenUndErtraege(
			angabenGemeinde.getBemerkungenWeitereKostenUndErtraege()
		);
		// E: Kontrollfragen
		jaxAngabenGemeinde.setBetreuungsstundenDokumentiertUndUeberprueft(
			angabenGemeinde.getBetreuungsstundenDokumentiertUndUeberprueft()
		);
		jaxAngabenGemeinde
			.setBetreuungsstundenDokumentiertUndUeberprueftBemerkung(
				angabenGemeinde
					.getBetreuungsstundenDokumentiertUndUeberprueftBemerkung()
			);
		jaxAngabenGemeinde.setElterngebuehrenGemaessVerordnungBerechnet(
			angabenGemeinde.getElterngebuehrenGemaessVerordnungBerechnet()
		);
		jaxAngabenGemeinde
			.setElterngebuehrenGemaessVerordnungBerechnetBemerkung(
				angabenGemeinde
					.getElterngebuehrenGemaessVerordnungBerechnetBemerkung()
			);
		jaxAngabenGemeinde.setEinkommenElternBelegt(
			angabenGemeinde.getEinkommenElternBelegt()
		);
		jaxAngabenGemeinde.setEinkommenElternBelegtBemerkung(
			angabenGemeinde.getEinkommenElternBelegtBemerkung()
		);
		jaxAngabenGemeinde.setMaximalTarif(angabenGemeinde.getMaximalTarif());
		jaxAngabenGemeinde.setMaximalTarifBemerkung(
			angabenGemeinde.getMaximalTarifBemerkung()
		);
		jaxAngabenGemeinde
			.setMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal(
				angabenGemeinde
					.getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal()
			);
		jaxAngabenGemeinde
			.setMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung(
				angabenGemeinde
					.getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung()
			);
		jaxAngabenGemeinde.setAusbildungenMitarbeitendeBelegt(
			angabenGemeinde.getAusbildungenMitarbeitendeBelegt()
		);
		jaxAngabenGemeinde.setAusbildungenMitarbeitendeBelegtBemerkung(
			angabenGemeinde.getAusbildungenMitarbeitendeBelegtBemerkung()
		);
		// Bemerkungen
		jaxAngabenGemeinde.setBemerkungen(angabenGemeinde.getBemerkungen());
		jaxAngabenGemeinde.setBemerkungStarkeVeraenderung(
			angabenGemeinde.getBemerkungStarkeVeraenderung()
		);
		// Berechnungen
		jaxAngabenGemeinde.setLastenausgleichberechtigteBetreuungsstunden(
			angabenGemeinde.getLastenausgleichberechtigteBetreuungsstunden()
		);
		jaxAngabenGemeinde
			.setDavonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet(
				angabenGemeinde
					.getDavonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet()
			);
		jaxAngabenGemeinde
			.setDavonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet(
				angabenGemeinde
					.getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet()
			);
		jaxAngabenGemeinde.setNormlohnkostenBetreuungBerechnet(
			angabenGemeinde.getNormlohnkostenBetreuungBerechnet()
		);
		jaxAngabenGemeinde.setLastenausgleichsberechtigerBetrag(
			angabenGemeinde.getLastenausgleichsberechtigerBetrag()
		);
		jaxAngabenGemeinde.setKostenbeitragGemeinde(
			angabenGemeinde.getKostenbeitragGemeinde()
		);
		jaxAngabenGemeinde.setKostenueberschussGemeinde(
			angabenGemeinde.getKostenueberschussGemeinde()
		);
		jaxAngabenGemeinde.setErwarteterKostenbeitragGemeinde(
			angabenGemeinde.getErwarteterKostenbeitragGemeinde()
		);
		jaxAngabenGemeinde.setSchlusszahlung(
			angabenGemeinde.getSchlusszahlung()
		);

		return jaxAngabenGemeinde;
	}

	@Nonnull
	private LastenausgleichTagesschuleAngabenGemeinde lastenausgleichTagesschuleAngabenGemeindeToEntity(
		@Nonnull JaxLastenausgleichTagesschuleAngabenGemeinde jaxAngabenGemeinde,
		@Nonnull LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde
	) {
		if (angabenGemeinde.getVersion() != jaxAngabenGemeinde.getVersion()) {
			throw new WebApplicationException(
				new StaleObjectStateException(
					"Die LastenausgleichTagesschuleAngabenGemeinde Versionen stimmen nicht",
					angabenGemeinde.getId()
				),
				Status.CONFLICT
			);
		}

		convertAbstractFieldsToEntity(jaxAngabenGemeinde, angabenGemeinde);

		// A: Allgemeine Angaben
		angabenGemeinde.setBedarfBeiElternAbgeklaert(
			jaxAngabenGemeinde.getBedarfBeiElternAbgeklaert()
		);
		angabenGemeinde.setAngebotFuerFerienbetreuungVorhanden(
			jaxAngabenGemeinde.getAngebotFuerFerienbetreuungVorhanden()
		);
		angabenGemeinde.setAngebotVerfuegbarFuerAlleSchulstufen(
			jaxAngabenGemeinde.getAngebotVerfuegbarFuerAlleSchulstufen()
		);
		angabenGemeinde
			.setBegruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen(
				jaxAngabenGemeinde
					.getBegruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen()
			);
		// B: Abrechnung
		angabenGemeinde.setGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse(
			jaxAngabenGemeinde
				.getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse()
		);
		angabenGemeinde.setGeleisteteBetreuungsstundenBesondereBeduerfnisse(
			jaxAngabenGemeinde
				.getGeleisteteBetreuungsstundenBesondereBeduerfnisse()
		);
		angabenGemeinde
			.setGeleisteteBetreuungsstundenBesondereVolksschulangebot(
				jaxAngabenGemeinde
					.getGeleisteteBetreuungsstundenBesondereVolksschulangebot()
			);
		angabenGemeinde.setDavonStundenZuNormlohnMehrAls50ProzentAusgebildete(
			jaxAngabenGemeinde
				.getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete()
		);
		angabenGemeinde
			.setDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete(
				jaxAngabenGemeinde
					.getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete()
			);
		angabenGemeinde.setEinnahmenElterngebuehren(
			jaxAngabenGemeinde.getEinnahmenElterngebuehren()
		);
		angabenGemeinde.setEinnahmenElterngebuehrenVolksschulangebot(
			jaxAngabenGemeinde.getEinnahmenElterngebuehrenVolksschulangebot()
		);
		angabenGemeinde.setTagesschuleTeilweiseGeschlossen(
			jaxAngabenGemeinde.getTagesschuleTeilweiseGeschlossen()
		);
		angabenGemeinde.setRueckerstattungenElterngebuehrenSchliessung(
			jaxAngabenGemeinde.getRueckerstattungenElterngebuehrenSchliessung()
		);
		angabenGemeinde.setErsteRateAusbezahlt(
			jaxAngabenGemeinde.getErsteRateAusbezahlt()
		);
		// C: Kostenbeteiligung Gemeinde
		angabenGemeinde.setGesamtKostenTagesschule(
			jaxAngabenGemeinde.getGesamtKostenTagesschule()
		);
		angabenGemeinde.setEinnnahmenVerpflegung(
			jaxAngabenGemeinde.getEinnnahmenVerpflegung()
		);
		angabenGemeinde.setEinnahmenSubventionenDritter(
			jaxAngabenGemeinde.getEinnahmenSubventionenDritter()
		);
		angabenGemeinde.setUeberschussErzielt(
			jaxAngabenGemeinde.getUeberschussErzielt()
		);
		angabenGemeinde.setUeberschussVerwendung(
			jaxAngabenGemeinde.getUeberschussVerwendung()
		);
		// D: Angaben zu weiteren Kosten und Ertraegen
		angabenGemeinde.setBemerkungenWeitereKostenUndErtraege(
			jaxAngabenGemeinde.getBemerkungenWeitereKostenUndErtraege()
		);
		// E: Kontrollfragen
		angabenGemeinde.setBetreuungsstundenDokumentiertUndUeberprueft(
			jaxAngabenGemeinde.getBetreuungsstundenDokumentiertUndUeberprueft()
		);
		angabenGemeinde.setBetreuungsstundenDokumentiertUndUeberprueftBemerkung(
			jaxAngabenGemeinde
				.getBetreuungsstundenDokumentiertUndUeberprueftBemerkung()
		);
		angabenGemeinde.setElterngebuehrenGemaessVerordnungBerechnet(
			jaxAngabenGemeinde.getElterngebuehrenGemaessVerordnungBerechnet()
		);
		angabenGemeinde.setElterngebuehrenGemaessVerordnungBerechnetBemerkung(
			jaxAngabenGemeinde
				.getElterngebuehrenGemaessVerordnungBerechnetBemerkung()
		);
		angabenGemeinde.setEinkommenElternBelegt(
			jaxAngabenGemeinde.getEinkommenElternBelegt()
		);
		angabenGemeinde.setEinkommenElternBelegtBemerkung(
			jaxAngabenGemeinde.getEinkommenElternBelegtBemerkung()
		);
		angabenGemeinde.setMaximalTarif(jaxAngabenGemeinde.getMaximalTarif());
		angabenGemeinde.setMaximalTarifBemerkung(
			jaxAngabenGemeinde.getMaximalTarifBemerkung()
		);
		angabenGemeinde
			.setMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal(
				jaxAngabenGemeinde
					.getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal()
			);
		angabenGemeinde
			.setMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung(
				jaxAngabenGemeinde
					.getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung()
			);
		angabenGemeinde.setAusbildungenMitarbeitendeBelegt(
			jaxAngabenGemeinde.getAusbildungenMitarbeitendeBelegt()
		);
		angabenGemeinde.setAusbildungenMitarbeitendeBelegtBemerkung(
			jaxAngabenGemeinde.getAusbildungenMitarbeitendeBelegtBemerkung()
		);
		// Bemerkungen
		angabenGemeinde.setBemerkungen(jaxAngabenGemeinde.getBemerkungen());
		angabenGemeinde.setBemerkungStarkeVeraenderung(
			jaxAngabenGemeinde.getBemerkungStarkeVeraenderung()
		);
		// Berechnungen
		angabenGemeinde.setLastenausgleichberechtigteBetreuungsstunden(
			jaxAngabenGemeinde.getLastenausgleichberechtigteBetreuungsstunden()
		);
		angabenGemeinde
			.setDavonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet(
				jaxAngabenGemeinde
					.getDavonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet()
			);
		angabenGemeinde
			.setDavonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet(
				jaxAngabenGemeinde
					.getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet()
			);
		angabenGemeinde.setNormlohnkostenBetreuungBerechnet(
			jaxAngabenGemeinde.getNormlohnkostenBetreuungBerechnet()
		);
		angabenGemeinde.setLastenausgleichsberechtigerBetrag(
			jaxAngabenGemeinde.getLastenausgleichsberechtigerBetrag()
		);
		angabenGemeinde.setKostenbeitragGemeinde(
			jaxAngabenGemeinde.getKostenbeitragGemeinde()
		);
		angabenGemeinde.setKostenueberschussGemeinde(
			jaxAngabenGemeinde.getKostenueberschussGemeinde()
		);
		angabenGemeinde.setErwarteterKostenbeitragGemeinde(
			jaxAngabenGemeinde.getErwarteterKostenbeitragGemeinde()
		);
		angabenGemeinde.setSchlusszahlung(
			jaxAngabenGemeinde.getSchlusszahlung()
		);

		return angabenGemeinde;
	}

	public JaxLastenausgleichTagesschulenStatusHistory latsStatusHistoryToJAX(
		LastenausgleichTagesschuleAngabenGemeindeStatusHistory latsStatusHistory
	) {
		final JaxLastenausgleichTagesschulenStatusHistory jaxStatusHistory =
			new JaxLastenausgleichTagesschulenStatusHistory();
		convertAbstractFieldsToJAX(latsStatusHistory, jaxStatusHistory);
		jaxStatusHistory.setContainerId(
			latsStatusHistory.getAngabenGemeindeContainer().getId()
		);
		jaxStatusHistory.setStatus(latsStatusHistory.getStatus());
		jaxStatusHistory.setBenutzer(
			jaxBenutzerConverter.benutzerToJaxBenutzer(
				latsStatusHistory.getBenutzer()
			)
		);
		jaxStatusHistory.setTimestampVon(latsStatusHistory.getTimestampVon());
		jaxStatusHistory.setTimestampBis(latsStatusHistory.getTimestampBis());
		return jaxStatusHistory;
	}
}
