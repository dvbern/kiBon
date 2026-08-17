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

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.SupportAnfrageDTO;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.AbstractAnmeldung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.gemeindeantrag.lastenausgleichtagesschulen.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.GemeindeAngebotTyp;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

/**
 * Service zum Versenden von E-Mails
 */
public interface MailService {

	/**
	 * Create eine Outbox Mail Eintritt in der Datenbank mit gegebenem MessageBody an die gegebene Adresse. Dadurch kann
	 * eine beliebige Message gemailt
	 * werden
	 */
	void toOutboxMail(
		@Nonnull String subject,
		@Nonnull String messageBody,
		@Nonnull String mailadress,
		@Nonnull MandantIdentifier mandantIdentifier
	);

	/**
	 * Vorbereitet zu Senden eine Supportanfrage an die definierte Support-Email
	 */
	void prepareToSendSupportAnfrage(
		@Nonnull SupportAnfrageDTO supportAnfrageDTO
	);

	/**
	 * Vorbereitet zu Senden eine Email mit der Information, dass alle Betreuungsplaetze bestaetigt wurden und das
	 * Gesuch freigegeben
	 * werden kann.
	 */
	void prepareToSendInfoBetreuungenBestaetigt(@Nonnull Gesuch gesuch);

	/**
	 * Vorbereitet zu Senden eine Email mit der Information, dass ein Betreuungsplatz abgelehnt wurde.
	 */
	void prepareToSendInfoBetreuungAbgelehnt(@Nonnull Betreuung betreuung);

	/**
	 * Vorbereitet zu Senden eine Email mit der Information, dass eine Anmeldung fuer ein Schulamt-Angebot ins Backend
	 * uebernommen
	 * wurde
	 */
	void prepareToSendInfoSchulamtAnmeldungTagesschuleUebernommen(
		@Nonnull AbstractAnmeldung abstractAnmeldung
	);

	/**
	 * Vorbereitet zu Senden eine Email mit der Information, dass eine Anmeldung fuer ein Schulamt-Angebot abgelehnt
	 * wurde.
	 */
	void prepareToSendInfoSchulamtAnmeldungAbgelehnt(
		@Nonnull AbstractAnmeldung abstractAnmeldung
	);

	/**
	 * Vorbereitet zu Senden eine Email mit der Information, dass eine Anmeldung für eine Ferieninsel angenommen wurde
	 */
	void prepareToSendInfoSchulamtAnmeldungFerieninselUebernommen(
		@Nonnull AbstractAnmeldung abstractAnmeldung
	);

	/**
	 * Prepares and sends an email notification conveying that a new {@link Mitteilung} has been received.
	 *
	 * @param mitteilung the {@link Mitteilung} containing the information to be included in the mail; must not be null.
	 */
	void prepareTemplateAndSendInfoMitteilungErhalten(
		@Nonnull Mitteilung mitteilung
	);

	/**
	 * Vorbereitet zu Senden eine Email mit der Information, dass ein Gesuch Verfügt wurde.
	 */
	void prepareToSendInfoVerfuegtGesuch(@Nonnull Gesuch gesuch);

	/**
	 * Vorbereitet zu Senden eine Email mit der Information, dass eine Mutation Verfügt wurde.
	 */
	void prepareToSendInfoVerfuegtMutation(@Nonnull Gesuch gesuch);

	/**
	 * Vorbereitet zu Senden eine Email mit der Information, dass eine Mahnung verVorbereitet zu Senden wurde.
	 */
	void prepareToSendInfoMahnung(@Nonnull Gesuch gesuch);

	/**
	 * Vorbereitet zu Senden eine Email mit der Information, dass ein Gesuch Verfügt wurde.
	 */
	void prepareToSendWarnungGesuchNichtFreigegeben(
		@Nonnull Gesuch gesuch,
		int anzahlTageBisLoeschung
	);

	/**
	 * Vorbereitet zu Senden eine Email mit der Information, dass ein Gesuch Verfügt wurde.
	 */
	void prepareToSendWarnungFreigabequittungFehlt(
		@Nonnull Gesuch gesuch,
		int anzahlTageBisLoeschung
	);

	/**
	 * Vorbereitet zu Senden eine Email mit der Information, dass ein Gesuch Verfügt wurde.
	 */
	void prepareToSendInfoGesuchGeloescht(@Nonnull Gesuch gesuch);

	/**
	 * Vorbereitet zu Senden eine Mail an den GS1 der übergebenen Gesuche, dass die übergebene Gesuchsperiode eröffnet
	 * wurde.
	 */
	Future<Integer> prepareToSendInfoFreischaltungGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull List<Gesuch> gesucheToSendMail
	);

	/**
	 * Vorbereitet zu Senden eine Mail an den GS1 des übergebenen Gesuchs, dass die übergebene Gesuchsperiode eröffnet
	 * wurde.
	 */
	boolean prepareToSendInfoFreischaltungGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gesuch gesuch
	);

	/**
	 * Vorbereitet zu Senden unter gewissen Bedingungen pro Betreuung eine Email mit der Information, dass ein
	 * Betreuungsplatz
	 * geloescht wurde.
	 */
	void prepareToSendInfoBetreuungGeloescht(
		@Nonnull List<Betreuung> betreuungen
	);

	/**
	 * Vorbereitet zu Senden eine Email mit der Information, dass eine Betreuung verfuegt wurde.
	 */
	void prepareToSendInfoBetreuungVerfuegt(@Nonnull Betreuung betreuung);

	/**
	 * Vorbereitet zu Senden eine E-Mail mit der Information, dass die Statistik erstellt wurde
	 */
	void prepareToSendInfoStatistikGeneriert(
		@Nonnull String receiverEmail,
		@Nonnull String downloadurl,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	);

	/**
	 * Vorbereitet zu Senden eine E-Mail mit der Information, dass der Zahlungslauf erstellt wurde
	 */
	void prepareToSendInfoZahlungslaufGeneriert(
		@Nonnull String receiverEmail,
		@Nonnull String zahlungsUrl,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	);

	/**
	 * Vorbereitet zu Senden eine E-Mail mit der Information, dass der Zahlungslauf erstellt wurde
	 */
	void prepareToSendInfoZahlungslaufNichtErfolgreichErstellt(
		@Nonnull String receiverEmail,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	);

	/**
	 * Sends an Einladungsemail to the given user according to the type of the Einladung
	 */
	void prepareToSendBenutzerEinladung(
		@Nonnull Benutzer einladender,
		@Nonnull Einladung einladung
	);

	/**
	 * Vorbereitet zu Senden eine E-Mail an eine Institution mit der Info, dass es offene Pendenzen gibt
	 */
	void prepareToSendInfoOffenePendenzenNeuMitteilungInstitution(
		@Nonnull InstitutionStammdaten institutionStammdaten,
		boolean offenePendenzen,
		boolean ungelesendeMitteilung
	);

	/**
	 * Vorbereitet zu Senden eine Email mit der Information, dass eine Anmeldung fuer ein Schulamt-Angebot ins Backend
	 * uebernommen
	 * wurde
	 */
	void prepareToSendInfoSchulamtAnmeldungTagesschuleAkzeptiert(
		@Nonnull AbstractAnmeldung abstractAnmeldung
	);

	/**
	 * Vorbereitet zu Senden eine Email mit der Information, dass ein Angebot für eine Gemeinde aktiviert wurde
	 */
	void prepareToSendInfoGemeindeAngebotAktiviert(
		@Nonnull Gemeinde gemeinde,
		@Nonnull GemeindeAngebotTyp angebot
	);

	/**
	 * schickt eine email an den Verantwortlichen Tagesschule und informiert, dass das Gesuch verfuegt wurde
	 */
	void prepareToSendInfoGesuchVerfuegtVerantwortlicherTS(
		@Nonnull Gesuch gesuch,
		@Nonnull Benutzer verantwortlicherTS
	);

	/**
	 * Vorbereitet zu Senden eine Email mit der Informatiom, dass ein Ruckforderungformular bei der Kanton geprueft
	 * wurde
	 */

	void prepareToSendInfoLastenausgleichGemeinde(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Lastenausgleich lastenausgleich
	);

	void prepareToSendInfoSchulamtAnmeldungStorniert(
		AbstractAnmeldung abstractAnmeldung
	);

	void prepareToSendInfoLATSAntragZurueckAnGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer wiederEroeffnet
	);

	void prepareToSendInitGSZPVNr(
		@Nonnull String ssoInitURL,
		GesuchstellerContainer gesuchstellerContainer,
		@Nonnull String email,
		String korrespondenzSprache
	);

	void prepareToSendInfoAuszahlungsdatenChanged(
		InstitutionStammdaten institutionStammdaten,
		@Nonnull String email
	);

	void prepareToSendInfoLastenausgleichProzessBeendet(
		@Nonnull String jahr,
		@Nonnull String receiverEmail,
		boolean isProcessSuccessfull,
		@Nonnull Mandant mandant
	);

	void prepareToSendInfoOffenePendenzenNeuMitteilungGemeindeMitarbeitende(
		@Nonnull Benutzer benutzer,
		boolean offenePendenzen,
		boolean ungelesendeMitteilung,
		String gemeindeNamen,
		String gemeindeNamenForUnreadMitteilung
	);
}
