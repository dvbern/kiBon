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

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.authentication.KibonJwt;
import ch.dvbern.ebegu.dto.filter.suchfilter.smarttable.BenutzerTableMandantFilterDTO;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.BerechtigungHistory;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.UserRole;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Service fuer die Verwaltung von Benutzern
 */
public interface BenutzerService {

	/**
	 * Aktualisiert den Benutzer in der DB or erstellt ihn wenn er noch nicht existiert.
	 * Falls die Berechtigungen geändert haben, werden diese aktualisiert und der Benutzer ausgeloggt.
	 *
	 * @param benutzer die Benutzer als DTO
	 * @return Die aktualisierte Benutzer
	 */
	@Nonnull
	Benutzer saveBenutzerBerechtigungen(
		@Nonnull Benutzer benutzer,
		boolean currentBerechtigungChanged
	);

	/**
	 * Aktualisiert den Benutzer in der DB or erstellt ihn wenn er noch nicht existiert.
	 *
	 * @param benutzer die Benutzer als DTO
	 * @return Die aktualisierte Benutzer
	 */
	@Nonnull
	Benutzer saveBenutzer(@Nonnull Benutzer benutzer);

	/**
	 * Saves the given Benutzer and sends him an Einladungsemail
	 */
	@Nonnull
	Benutzer einladen(@Nonnull Einladung einladung, @Nonnull Mandant mandant);

	/**
	 * Sendet einem eingeladenen Benutzer erneut das Einladungsmail
	 */
	void erneutEinladen(@Nonnull Benutzer eingeladener);

	void checkBenutzerIsNotGesuchstellerWithFreigegebenemGesuch(
		@Nonnull Benutzer benutzer
	);

	String findFallIdIfBenutzerIsGesuchstellerWithoutFreigegebenemGesuch(
		@Nonnull Benutzer benutzer
	);

	@Nonnull
	Optional<Benutzer> findBenutzer(
		@Nonnull String username,
		@Nonnull Mandant mandant
	);

	@Nonnull
	Optional<Benutzer> findAndLockBenutzer(
		@Nonnull String username,
		@Nonnull Mandant mandant
	);

	@Nonnull
	Optional<Benutzer> findBenutzerById(@Nonnull String id);

	Optional<Benutzer> findBenutzer(KibonJwt kibonJwt);

	/**
	 * Sucht einen Benutzer nach externalUUID: Diese Methode wird nur von den Connectoren gebraucht.
	 * Innerhalb ebegu verwenden wir weiterhin die ID.
	 */
	@Nonnull
	Optional<Benutzer> findBenutzerByExternalUUID(@Nonnull String externalUUID);

	Optional<Benutzer> findByEmail(String email, Mandant mandant);

	/**
	 * Gibt alle Administratoren einer Gemeinde zurueck.
	 *
	 * @param gemeinde Die Gemeinde
	 * @return Liste aller Benutzern aus der DB
	 */
	@Nonnull
	Collection<Benutzer> getAktivGemeindeAdministratoren(Gemeinde gemeinde);

	/**
	 * Gibt alle Sachbearbeiter einer Gemeinde zurueck.
	 *
	 * @param gemeinde Die Gemeinde
	 * @return Liste aller Benutzern aus der DB
	 */
	@Nonnull
	Collection<Benutzer> getAktiveGemeindeSachbearbeiter(Gemeinde gemeinde);

	/**
	 * Gibt alle Administratoren einer Institution zurueck.
	 *
	 * @param institution Die Institution (Kita)
	 * @return Liste aller Benutzern aus der DB
	 */
	@Nonnull
	Collection<Benutzer> getInstitutionAdministratoren(Institution institution);

	/**
	 * Gibt alle Sachbearbeiter einer Institution zurueck.
	 *
	 * @param institution Die Institution (Kita)
	 * @return Liste aller Benutzern aus der DB
	 */
	@Nonnull
	Collection<Benutzer> getInstitutionSachbearbeiter(Institution institution);

	/**
	 * Gibt alle Administratoren einer Traegerschaft zurueck.
	 *
	 * @param traegerschaft Die Traegerschaft
	 * @return Liste aller Benutzern aus der DB
	 */
	@Nonnull
	Collection<Benutzer> getTraegerschaftAdministratoren(
		Traegerschaft traegerschaft
	);

	/**
	 * Gibt alle existierenden aktiven Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
	 * Sachbearbeiter_Gemeinde oder Admin_Gemeinde einer bestimmten Gemeinde zurueck.
	 *
	 * @param gemeinde Die Gemeinde
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	Collection<Benutzer> getActiveBenutzerBgOrGemeinde(Gemeinde gemeinde);

	/**
	 * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG, Admin_BG, Sachbearbeiter_TS, Admin_TS oder
	 * Sachbearbeiter_Gemeinde oder Admin_Gemeinde einer bestimmten Gemeinde zurueck.
	 *
	 * @param gemeinde Die Gemeinde
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	Collection<Benutzer> getBenutzerTsBgOrGemeinde(Gemeinde gemeinde);

	/**
	 * Gibt alle existierenden aktiven Benutzer mit den Rollen Sachbearbeiter_TS oder Admin_TS oder
	 * Sachbearbeiter_Gemeinde oder Admin_Gemeinde einer bestimmten Gemeinde zurueck.
	 *
	 * @param gemeinde Die Gemeinde
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	Collection<Benutzer> getActiveBenutzerTsOrGemeinde(Gemeinde gemeinde);

	/**
	 * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG oder
	 * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
	 *
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	Collection<Benutzer> getAllBenutzerBgOrGemeinde();

	/**
	 * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_BG oder Admin_BG, Sachbearbeiter_TS, Admin_TS
	 * oder
	 * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
	 *
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	Collection<Benutzer> getAllBenutzerBgTsOrGemeinde();

	/**
	 * Gibt alle existierenden Benutzer mit den Rollen Sachbearbeiter_TS oder Admin_TS oder
	 * Sachbearbeiter_Gemeinde oder Admin_Gemeinde zurueck.
	 *
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	Collection<Benutzer> getAllBenutzerTsOrGemeinde();

	/**
	 * Gibt alle existierenden Benutzer mit den Rollen SACHBEARBEITER_MANDANT oder ADMIN_MANDANT zurueck.
	 *
	 * @return Liste aller Benutzern mit entsprechender Rolle aus der DB
	 */
	Collection<Benutzer> getAllActiveBenutzerMandant(@Nonnull Mandant mandant);

	/**
	 * @return Liste saemtlicher Gesuchsteller aus der DB
	 */
	@Nonnull
	Collection<Benutzer> getGesuchsteller(@Nonnull Mandant mandant);

	/**
	 * entfernt die Benutzer aus der Database
	 *
	 * @param username die Benutzer als DTO
	 */
	void removeBenutzer(@Nonnull String username, @Nonnull Mandant mandant);

	@Nonnull
	Collection<Benutzer> getAllUserButGesuchsteller(@Nonnull Mandant mandant);

	/**
	 * Gibt den aktuell eingeloggten Benutzer zurueck
	 */
	@Nonnull
	Optional<Benutzer> getCurrentBenutzer();

	/**
	 * Setzt den uebergebenen Benutzer auf gesperrt. Es werden auch alle möglicherweise noch vorhandenen
	 * AuthentifizierteBenutzer gelöscht.
	 */
	@Nonnull
	Benutzer sperren(@Nonnull String username, @Nonnull Mandant mandant);

	/**
	 * Reaktiviert den uebergebenen Benutzer wieder.
	 */
	@Nonnull
	Benutzer reaktivieren(@Nonnull String username, @Nonnull Mandant mandant);

	/**
	 * Sucht Benutzer, welche den übergebenen Filterkriterien entsprechen
	 */
	@Nonnull
	Pair<Long, List<Benutzer>> searchBenutzer(
		@Nonnull BenutzerTableMandantFilterDTO benutzerTableFilterDto,
		@Nonnull Boolean forStatistik
	);

	/**
	 * Setzt alle Benutzer mit abgelaufenen Rollen auf die Rolle GESUCHSTELLER zurück.
	 *
	 * @return Die Anzahl zurückgesetzter Benutzer
	 */
	int handleAbgelaufeneRollen(@Nonnull LocalDate stichtag);

	/**
	 * Schreibt eine Berechtigungs-History in die DB
	 */
	void saveBerechtigungHistory(
		@Nonnull Berechtigung berechtigung,
		boolean deleted
	);

	/**
	 * Gibt alle BerechtigungsHistories fuer den übergebenen Benutzer zurück
	 */
	@Nonnull
	Collection<BerechtigungHistory> getBerechtigungHistoriesForBenutzer(
		@Nonnull Benutzer benutzer
	);

	/**
	 * Gibt zurück, ob der Benutzer mit der übergebenen Username in irgendeiner Gemeinde (für die der eingeloggte
	 * Benutzer nicht zwingend berechtigt sein muss) als Defaultbenutzer gesetzt ist.
	 */
	boolean isBenutzerDefaultBenutzerOfAnyGemeinde(@Nonnull String username);

	/**
	 * Gibt zurück, ob der Benutzer eine offene Einladung hat
	 */
	Optional<Benutzer> findUserWithInvitation(
		@Nonnull String externalUuid
	);

	boolean hasMoreThanOneMandantUser();

	Collection<Benutzer> getActiveBenutzerInRolesOfActiveGemeinden(
		Mandant mandant,
		UserRole... roles
	);

	Collection<Benutzer> getActiveBenutzerInRolesOfGemeinden(
		Mandant mandant,
		List<Gemeinde> gemeinden,
		UserRole... roles
	);

	/**
	 * Sendet einen Passwort-Ändern-Link an den gegebenen Benutzer.
	 *
	 * @param benutzer Der Benutzer, dem ein Passwort-Ändern-Link zugeschickt werden soll.
	 */
	void sendUpdatePasswordEmail(Benutzer benutzer);
}
