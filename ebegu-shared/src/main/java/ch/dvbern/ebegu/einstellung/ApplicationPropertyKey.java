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

package ch.dvbern.ebegu.einstellung;

import java.util.Optional;

/**
 * Keys fuer die Application Properties die wir in der DB speichern
 *
 * We can set a group and a subgroup for every key:
 *
 * The group key (KeyGroup) and subkey (SubKeyGroup) allow us to group property Keys on the frontend
 *
 */
public enum ApplicationPropertyKey {

	/**
	 * Wenn true gibt der Evaluator seine Debugmeldungen in das log aus.
	 */
	@BooleanEinstellung EVALUATOR_DEBUG_ENABLED,

	/**
	 * Damit wir Test/Produktion leichter unterscheiden koennen kann man die Hintergrundfarbe einstellen
	 */
	@StringEinstellung BACKGROUND_COLOR,

	/**
	 * Anzahl Tage nach Erstellungsdatum bis der GS gewarnt wird, wenn er nicht freigibt
	 */
	@NumberEinstellung ANZAHL_TAGE_BIS_WARNUNG_FREIGABE,

	/**
	 * Anzahl Tage nach Freigabe bis der GS gewarnt wird, wenn er Quittung nicht schickt
	 */
	@NumberEinstellung ANZAHL_TAGE_BIS_WARNUNG_QUITTUNG,

	/**
	 * Anzahl Tage nach Warnung bis Gesuch geloescht wird, wenn er nicht freigibt
	 */
	@NumberEinstellung ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE,

	/**
	 * Anzahl Tage nach Warnung bis Gesuch geloescht wird, wenn er Quittung nicht schickt
	 */
	@NumberEinstellung ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG,

	/**
	 * Key fuer Komma separierte Whiteliste von zugelassenen Filetypen fuer den upload
	 */
	@StringEinstellung UPLOAD_FILETYPES_WHITELIST,

	/**
	 * Flag, ob das Dummy Login eingeschaltet ist. Aus Sicherheitsgruenden muss sowohl dieses wie auch das entsprechende
	 * System-Property eingeschaltet sein, damit das Dummy Login funktioniert.
	 */
	@BooleanEinstellung DUMMY_LOGIN_ENABLED,

	/**
	 * Gibt das Sentry DNS Token zurueck
	 */
	@StringEinstellung SENTRY_ENV,

	/**
	 * Ab diesem Datum gelten fuer die Stadt Bern die ASIV Regeln
	 */
	@DateEinstellung STADT_BERN_ASIV_START_DATUM,

	/**
	 * Wenn TRUE koennen die Zeitraeume ab ASIV_START_DATUM verfuegt werden
	 */
	@BooleanEinstellung STADT_BERN_ASIV_CONFIGURED,

	/**
	 * Wenn TRUE sind Ferienbetreuungen aktiviert
	 */
	@BooleanEinstellung FERIENBETREUUNG_AKTIV,
	/**
	 * Wenn TRUE ist Lastenausgleich Tagesschulen aktiviert
	 */
	@BooleanEinstellung LASTENAUSGLEICH_TAGESSCHULEN_AKTIV,
	/**
	 * Wenn TRUE ist Gemeinde Kennzahlen aktiviert
	 */
	@BooleanEinstellung GEMEINDE_KENNZAHLEN_AKTIV,

	/**
	 * Setzt fest, was für ein Anteil der LATS Anträge der deutschsprachigen Gemeinden zur Zweitprüfung ausgewählt wird
	 */
	@NumberEinstellung LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_DE,

	/**
	 * Setzt fest, ab welcher Anzahl Betreuungsstunden der LATS Antrag der deutschsprachigen Gemeinden zur Zweitprüfung
	 * ausgewählt wird
	 */
	@NumberEinstellung LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_DE,

	/**
	 * Setzt fest, was für ein Anteil der LATS Anträge der deutschsprachigen Gemeinden zur Zweitprüfung ausgewählt wird
	 */
	@NumberEinstellung LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_FR,

	/**
	 * Setzt fest, ab welcher höhe des Kantonsbeitrags der Ferienbetreuung Antrag der deutschsprachigen Gemeinden zur
	 * Zweitprüfung
	 * ausgew
	 * ählt wird
	 */
	@NumberEinstellung FERIENBETREUUNG_AUTO_ZWEITPRUEFUNG_FR,

	/**
	 * Setzt fest, was für ein Anteil der Ferienbetreuung Anträge der deutschsprachigen Gemeinden zur Zweitprüfung
	 * ausgewählt wird
	 */
	@NumberEinstellung FERIENBETREUUNG_ANTEIL_ZWEITPRUEFUNG_DE,

	/**
	 * Setzt fest, ab welcher höhe des Kantonsbeitrags der Ferienbetreuung Antrag der deutschsprachigen Gemeinden zur
	 * Zweitprüfung
	 * ausgewählt wird
	 */
	@NumberEinstellung FERIENBETREUUNG_AUTO_ZWEITPRUEFUNG_DE,

	/**
	 * Setzt fest, was für ein Anteil der Ferienbetreuung Anträge der deutschsprachigen Gemeinden zur Zweitprüfung
	 * ausgewählt wird
	 */
	@NumberEinstellung FERIENBETREUUNG_ANTEIL_ZWEITPRUEFUNG_FR,

	/**
	 * Setzt fest, ab welcher Anzahl Betreuungsstunden der LATS Antrag der deutschsprachigen Gemeinden zur Zweitprüfung
	 * ausgew
	 * ählt wird
	 */
	@NumberEinstellung LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_FR,

	/**
	 * True, wenn Lastenausgleich BG aktiv
	 */
	@BooleanEinstellung LASTENAUSGLEICH_AKTIV,

	/**
	 * Legt die Primary Color fest
	 */
	@StringEinstellung PRIMARY_COLOR,

	/**
	 * Legt die dunkle Primary Color fest
	 */
	@StringEinstellung PRIMARY_COLOR_DARK,

	/**
	 * Legt die helle Primary Color fest
	 */
	@StringEinstellung PRIMARY_COLOR_LIGHT,

	/**
	 * Bestimmt, ob Multimandant für diese kiBon Instanz aktiviert sein soll
	 */
	@BooleanEinstellung MULTIMANDANT_AKTIV,

	/**
	 * Falls das Infoma Zahlungssystem verwendet wird ist dieses Flag true
	 */
	@BooleanEinstellung INFOMA_ZAHLUNGEN,

	/**
	 * Falls die Auszahlungen an Eltern verwendet wird ist dieses Flag true
	 */
	@BooleanEinstellung AUSZAHLUNGEN_AN_ELTERN,

	/**
	 * Sind die französischen Übersetzungen verfügbar
	 */
	@BooleanEinstellung FRENCH_ENABLED,

	/**
	 * Ist Geres verfügbar
	 */
	@BooleanEinstellung GERES_ENABLED_FOR_MANDANT,

	/*
	 * Wenn dieses Datum überschritten wird, ist Steuerschnittstelle aktiv. Ansonste wird eine Warnung gezeigt.
	 */
	@DateEinstellung SCHNITTSTELLE_STEUERSYSTEME_AKTIV_AB,

	/*
	 * Bestimmte Informationen bei den Institutionen sind nicht zwingend nötig für alle Mandanten
	 */
	@BooleanEinstellung ZUSATZINFORMATIONEN_INSTITUTION,
	/**
	 * Wenn TRUE koennen die Schnittstelle events z.B. AnmeldungTagesschuleEvent, BetreuungAnfrageAddedEvent
	 * werden veröffentlicht
	 */
	@BooleanEinstellung SCHNITTSTELLE_EVENTS_AKTIVIERT,

	/**
	 * Falls true wird eine Checkbox bei den Zahlungen angezeigt, mit der die Auszahlungen in der Zukunft
	 * ausbezahlt werden können
	 */
	@BooleanEinstellung CHECKBOX_AUSZAHLEN_IN_ZUKUNFT,

	/**
	 * Einige Features sollen in der Produktion noch ausgeblendet werden. Auf den Testungebungen können diese Features
	 * mit dieser Einstellung aktiviert werden. Eine Liste aller möglichen Features sind in TSDemoFeature.ts zu finden.
	 */
	@StringEinstellung ACTIVATED_DEMO_FEATURES,
	/**
	 * Falls aktiv, können Gemeinden durch die Institutionen eingeladen werden. Diese werden direkt mit der Gemeinde
	 * verknüpft
	 */
	@BooleanEinstellung INSTITUTIONEN_DURCH_GEMEINDEN_EINLADEN,

	/**
	 * Falls aktiv, können Gemeinden die Institutionen Wahl begrenzen. Nur die Gewaehlte Institutionen sind dann
	 * im Antrag Prozess waehlbar
	 */
	@BooleanEinstellung ERLAUBEN_INSTITUTIONEN_ZU_WAEHLEN,

	/**
	 * Ist für den Mandanten das Angebot TS aktiviert
	 */
	@BooleanEinstellung ANGEBOT_TS_ENABLED,

	/**
	 * Ist für den Mandanten das Angebot TFO aktiviert
	 */
	@BooleanEinstellung ANGEBOT_TFO_ENABLED,

	/**
	 * Ist für den Mandanten das Angebot FI aktiviert
	 */
	@BooleanEinstellung ANGEBOT_FI_ENABLED,

	/**
	 * Ist für den Mandanten das Angebot Mittagstisch aktiviert
	 */
	@BooleanEinstellung ANGEBOT_MITTAGSTISCH_ENABLED,

	/**
	 * Wenn dieses Datum überschritten wird, ist die SprachfoerderungBestaegit Flag Wert beruecksichtig.
	 */
	@DateEinstellung SCHNITTSTELLE_SPRACHFOERDERUNG_AKTIV_AB,

	/**
	 * Gemeinde leichtere Konfiguration aktivieren. Es wird die TFO Angebot Wahl und ein paar Gesuchsperiode Gemeinde
	 * Einstellungen ausblenden.
	 */
	@BooleanEinstellung GEMEINDE_VEREINFACHTE_KONFIG_AKTIV,

	/**
	 * Default = false
	 * Wenn dies aktiviert ist, werden jährlich in einem Batchjob Erinnerungsmails für die Gemeinde-Kennzahlen ausgelöst
	 */
	@BooleanEinstellung GEMEINDE_KENNZAHLEN_REMINDER_ACTIVATED,

	/**
	 * We can switch for everysteps to the new angular non javascript implemention:
	 * True => new version when provided
	 * False => old version
	 */
	@BooleanEinstellung ABGELOESTE_VIEW_ANTRAGSTELLER(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW)
	), @BooleanEinstellung ABGELOESTE_VIEW_FAMILIENSITUATION(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW)
	), @BooleanEinstellung ABGELOESTE_VIEW_KINDER_LIST(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW, SubKeyGroup.KINDER)
	), @BooleanEinstellung ABGELOESTE_VIEW_KINDER_SINGLE(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW, SubKeyGroup.KINDER)
	), @BooleanEinstellung ABGELOESTE_VIEW_BETREUUNG_LIST(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW, SubKeyGroup.BETREUUNG)
	), @BooleanEinstellung ABGELOESTE_VIEW_BETREUUNG_SINGLE(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW, SubKeyGroup.BETREUUNG)
	), @BooleanEinstellung ABGELOESTE_VIEW_ERWERBSPENSUM_LIST(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW, SubKeyGroup.ERWERBSPENSUM)
	), @BooleanEinstellung ABGELOESTE_VIEW_ERWERBSPENSUM_SINGLE(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW, SubKeyGroup.ERWERBSPENSUM)
	), @BooleanEinstellung ABGELOESTE_VIEW_FINSIT_START(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW, SubKeyGroup.FINSIT)
	), @BooleanEinstellung ABGELOESTE_VIEW_FINSIT_GS(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW, SubKeyGroup.FINSIT)
	), @BooleanEinstellung ABGELOESTE_VIEW_FINSIT_RESULTATE(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW, SubKeyGroup.FINSIT)
	), @BooleanEinstellung ABGELOESTE_VIEW_EKV_START(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW, SubKeyGroup.EKV)
	), @BooleanEinstellung ABGELOESTE_VIEW_EKV_GS(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW, SubKeyGroup.EKV)
	), @BooleanEinstellung ABGELOESTE_VIEW_EKV_RESULTATE(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW, SubKeyGroup.EKV)
	), @BooleanEinstellung ABGELOESTE_VIEW_FREIGABE(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW)
	), @BooleanEinstellung ABGELOESTE_VIEW_VERFUEGUNG_LIST(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW, SubKeyGroup.VERFUEGUNG)
	), @BooleanEinstellung ABGELOESTE_VIEW_VERFUEGUNG_SINGLE(
		KeyGrouping.of(KeyGroup.ABGELOESTE_VIEW, SubKeyGroup.VERFUEGUNG)
	),

	/**
	 * We can switch to the newly written quarkus statistik
	 * True => new = quarkus statistik
	 * False => old = wildfly statistik
	 */
	@BooleanEinstellung QUARKUS_STATISTIK_BETREUUNGSGUTSCHEINE_KINDER(
		KeyGrouping.of(KeyGroup.QUARKUS_STATISTIK)
	), @BooleanEinstellung QUARKUS_STATISTIK_MITARBEITENDE(
		KeyGrouping.of(KeyGroup.QUARKUS_STATISTIK)
	), @BooleanEinstellung QUARKUS_STATISTIK_LASTENAUSGLEICH_BG(
		KeyGrouping.of(KeyGroup.QUARKUS_STATISTIK)
	);

	private final KeyGrouping keyGrouping;

	ApplicationPropertyKey() {
		this.keyGrouping = null;
	}

	ApplicationPropertyKey(KeyGrouping keyGrouping) {
		this.keyGrouping = keyGrouping;
	}

	public Optional<KeyGrouping> getKeyGrouping() {
		return Optional.ofNullable(keyGrouping);
	}

}
