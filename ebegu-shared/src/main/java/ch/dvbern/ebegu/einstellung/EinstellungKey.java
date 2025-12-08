/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.einstellung;

import java.util.List;

import ch.dvbern.ebegu.enums.AnspruchBeschaeftigungAbhaengigkeitTyp;
import ch.dvbern.ebegu.enums.AusserordentlicherAnspruchTyp;
import ch.dvbern.ebegu.enums.EingewoehnungTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.FachstellenTyp;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.GeschwisterbonusTyp;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.enums.betreuung.BetreuungComparator;
import ch.dvbern.ebegu.enums.betreuung.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.enums.gemeindekonfiguration.GemeindeZusaetzlicherGutscheinTyp;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

/**
 * Keys für die Einstellungen
 */
public enum EinstellungKey {

	// Die Gemeinde kennt eine Kontingentierung der Gutscheine
	@BooleanEinstellung GEMEINDE_KONTINGENTIERUNG_ENABLED(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Bis zu welcher Schulstufe sollen Gutscheine ausgestellt werden?
	@EnumEinstellung(EinschulungTyp.class) GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Fuer welche Angebottyp gilt die SchulstufeCalcRule?
	@StringEinstellung ANGEBOT_SCHULSTUFE(MandantIdentifier.getAll()),

	// Ab welchem Datum können Anmeldungen für die Tagesschule erfasst werden
	@DateEinstellung GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Ab welchem Datum können Anmeldungen für die Ferieninsel erfasst werden
	@DateEinstellung GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Erster Schultag der Tagesschule
	@DateEinstellung GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Ob die Tageschulen koennen Tagis sein
	@BooleanEinstellung GEMEINDE_TAGESSCHULE_TAGIS_ENABLED(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	@BooleanEinstellung GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Die Gemeinde gewährt einen zusätzlichen Beitrag zum Gutschein
	@BooleanEinstellung GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),
	// Die Art von zusätzlichem Gutschein, den die Gemeinde gewährt
	@EnumEinstellung(GemeindeZusaetzlicherGutscheinTyp.class) GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_TYP(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Betrag des zusätzlichen Beitrags zum Gutschein bei pauschalem Beitrag
	@NumberEinstellung GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	), @NumberEinstellung GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	//Maximaler Wert des zusätzlichen Beitrags zum Gutschein bei linearem Beitrag bei Kitas
	@NumberEinstellung GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_KITA_MAX(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Maximaler Wert des zusätzlichen Beitrags zum Gutschein bei linearem Beitrag bei TFOs
	@NumberEinstellung GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_TFO_MAX(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	//Minimales/Maximales massgebendes Einkommen für zusätzlichen Beitrag zum Gutschein durch Gemeinden
	@NumberEinstellung GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MIN_MASSGEBENDES_EINKOMMEN(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	), @NumberEinstellung GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MAX_MASSGEBENDES_EINKOMMEN(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Zusaetzlichen Gutschein anbieten bis und mit
	@EnumEinstellung(EinschulungTyp.class) GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	), @EnumEinstellung(EinschulungTyp.class) GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Die Gemeinde gewährt einen Zusatzbetrag für Babies
	@BooleanEinstellung GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Betrag des zusätzlichen Gutscheins für Babies
	@NumberEinstellung GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	), @NumberEinstellung GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Die Gemeinde akzeptiert Freiwilligenarbeit als Erwerbspensum mit Anspruch
	@BooleanEinstellung GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Maximale Prozente, zu welchen Freiwilligenarbeit zu einem Anspruch führt
	@NumberEinstellung GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Die Gemeinde gewährt eine Mahlzeitenvergünsgigung
	@BooleanEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Mahlzeitenverguenstigungsstufen Verguenstigung Haupt- und Nebenmahlzeit sowie Maximaleinkommen
	@NumberEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	), @NumberEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	), @NumberEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	), @NumberEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	), @NumberEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Die Gemeinde gewährt die Mahlzeitenvergünstigung auch für Sozialhilfebezüger
	@BooleanEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	), @NumberEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Die Schnittstelle zu Ki-Tax ist aktiviert
	@BooleanEinstellung GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Minimales Erwerbspensum, wenn das Kind nicht eingeschult ist
	@NumberEinstellung GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),
	// Minimales Erwerbspensum, wenn das Kind eingeschult ist
	@NumberEinstellung GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Die Gemeinde gewährt einen Zusatzbetrag für hohere Einkommensklassen
	@BooleanEinstellung GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT(
		MandantIdentifier.SOLOTHURN,
		EinstellungTyp.GEMEINDE
	),

	// Die Gemeinde Zusatzbetrag für hohere Einkommensklassen KITA
	@NumberEinstellung GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA(
		MandantIdentifier.SOLOTHURN,
		EinstellungTyp.GEMEINDE
	),

	// Die Gemeinde Zusatzbetrag für hohere Einkommensklassen TFO
	@NumberEinstellung GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO(
		MandantIdentifier.SOLOTHURN,
		EinstellungTyp.GEMEINDE
	), @NumberEinstellung GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE(
		MandantIdentifier.SOLOTHURN,
		EinstellungTyp.GEMEINDE
	),

	// Der Betrag welcher für die Berechnung des Gutscheines pro Zeiteinheit als maximal maasgebendes Einkommen verwendet werden soll
	@NumberEinstellung GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG(
		MandantIdentifier.SOLOTHURN,
		EinstellungTyp.GEMEINDE
	),

	// Wenn aktiv werden in der Gemeinde werde keine Gutscheine für Sozialhilfeempfänger ausgestellt
	@BooleanEinstellung GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER(
		MandantIdentifier.SOLOTHURN,
		EinstellungTyp.GEMEINDE
	),

	// *** Einstellungen fuer die Gutscheinberechnung

	// Maximale Vergünstigung für Vorschulkinder unter 12 Monaten
	@NumberEinstellung MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG(
		MandantIdentifier.getAll()
	),
	// Maximale Vergünstigung für Vorschulkinder ab 12 Monaten
	@NumberEinstellung MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG(
		MandantIdentifier.getAll()
	),
	// Maximale Vergünstigung bei Eintritt des Kindergartens
	@NumberEinstellung MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG(
		MandantIdentifier.getAll()
	),

	// Maximale Vergünstigung für Vorschulkinder unter 12 Monaten
	@NumberEinstellung MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD(
		MandantIdentifier.getAll()
	),
	// Maximale Vergünstigung für Vorschulkinder ab 12 Monaten
	@NumberEinstellung MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD(
		MandantIdentifier.getAll()
	),
	// Maximale Vergünstigung bei Eintritt des Kindergartens
	@NumberEinstellung MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD(
		MandantIdentifier.getAll()
	),

	// Maximale Vergünstigung bei Eintritt in die Primarstufe
	@NumberEinstellung MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD(
		MandantIdentifier.getAll()
	),

	// Minimal Massgebendes Einkommen
	@NumberEinstellung MIN_MASSGEBENDES_EINKOMMEN(MandantIdentifier.getAll()),
	// Maximal Massgebendes Einkommen
	@NumberEinstellung MAX_MASSGEBENDES_EINKOMMEN(MandantIdentifier.getAll()),

	@NumberEinstellung OEFFNUNGSTAGE_KITA(
		MandantIdentifier.getAll()
	), @NumberEinstellung OEFFNUNGSTAGE_TFO(
		MandantIdentifier.getAll()
	), @NumberEinstellung OEFFNUNGSSTUNDEN_TFO(
		MandantIdentifier.getAll()
	), @NumberEinstellung OEFFNUNGSTAGE_MITTAGSTISCH(
		MandantIdentifier.SCHWYZ
	),

	@NumberEinstellung ZUSCHLAG_BEHINDERUNG_PRO_TG(
		MandantIdentifier.getAll()
	), @NumberEinstellung ZUSCHLAG_BEHINDERUNG_PRO_STD(
		MandantIdentifier.getAll()
	),

	@NumberEinstellung MIN_VERGUENSTIGUNG_PRO_TG(
		MandantIdentifier.getAll()
	), @NumberEinstellung MIN_VERGUENSTIGUNG_PRO_STD(
		MandantIdentifier.getAll()
	),

	// *** Einstellungen fuer die Gutscheinberechnung

	// Minimales Erwerbspensum, wenn das Kind nicht eingeschult ist
	@NumberEinstellung MIN_ERWERBSPENSUM_NICHT_EINGESCHULT(
		MandantIdentifier.getAll()
	),
	// Minimales Erwerbspensum, wenn das Kind eingeschult ist
	@NumberEinstellung MIN_ERWERBSPENSUM_EINGESCHULT(
		MandantIdentifier.getAll()
	),
	// Zuschlag, um den der Anspruch aufgrund des Erwerbspensums automatisch erhöht wird
	@NumberEinstellung ERWERBSPENSUM_ZUSCHLAG(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Min Pensum Kitas
	@NumberEinstellung PARAM_PENSUM_KITA_MIN(MandantIdentifier.getAll()),

	// Min Pensum Tageseltern
	@NumberEinstellung PARAM_PENSUM_TAGESELTERN_MIN(MandantIdentifier.getAll()),

	// Min Pensum Tagesschule
	@NumberEinstellung PARAM_PENSUM_TAGESSCHULE_MIN(MandantIdentifier.getAll()),

	// Pauschalabzug bei einer Familiengrösse von drei Personen pauschal pro Person
	@NumberEinstellung PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3(
		MandantIdentifier.getAll()
	),

	// Pauschalabzug bei einer Familiengrösse von vier Personen pauschal pro Person
	@NumberEinstellung PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4(
		MandantIdentifier.getAll()
	),

	// Pauschalabzug bei einer Familiengrösse von fünf Personen pauschal pro Person
	@NumberEinstellung PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5(
		MandantIdentifier.getAll()
	),

	// Pauschalabzug bei einer Familiengrösse von sechs Personen pauschal pro Person
	@NumberEinstellung PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6(
		MandantIdentifier.getAll()
	),

	// Max Abwesenheit
	@NumberEinstellung PARAM_MAX_TAGE_ABWESENHEIT(MandantIdentifier.getAll()),

	// Eine Einkommensverschlechterung wird nur berücksichtigt, wenn diese höher als 20% des Ausgangswertes ist.
	@NumberEinstellung PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG(
		MandantIdentifier.getAll()
	),

	//Pensum Fachstelle soziale Integration
	@NumberEinstellung FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION(
		MandantIdentifier.getAll()
	), @NumberEinstellung FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION(
		MandantIdentifier.getAll()
	),

	//Pensum Fachstelle soziale Integration
	@NumberEinstellung FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION(
		MandantIdentifier.getAll()
	), @NumberEinstellung FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION(
		MandantIdentifier.getAll()
	),

	//Tagesschule Max Min Tarife
	@NumberEinstellung MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG(
		MandantIdentifier.getAll()
	), @NumberEinstellung MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG(
		MandantIdentifier.getAll()
	), @NumberEinstellung MIN_TARIF(MandantIdentifier.getAll()),

	//LATS
	@NumberEinstellung LATS_LOHNNORMKOSTEN(
		MandantIdentifier.getAll()
	), @NumberEinstellung LATS_LOHNNORMKOSTEN_LESS_THAN_50(
		MandantIdentifier.getAll()
	), @DateEinstellung LATS_STICHTAG(MandantIdentifier.getAll()),

	// "FKJV: Eingewöhnung aktiviert".
	// Siehe KIBON-2078. Definiert, ob das Kind einen zusätzlichen Anspruch auf Eingewöhnung hat
	@EnumEinstellung(EingewoehnungTyp.class) EINGEWOEHNUNG_TYP(
		MandantIdentifier.getAll()
	),

	// "FKJV: Maximale Differenz zwischen erforderlichem und effektivem Beschäftigungspensum für ausserordentlicher Anspruch"
	// Siehe KIBON-2080. Definiert die maximale Differenz zwischen erforderlichem und effektiven Beschäftigunspensum für den ausserordentlichen Anspruch.
	// in Prozent.
	@NumberEinstellung FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM(
		MandantIdentifier.getAll()
	),

	// "FKJV: Soziale Integration bis und mit Schulstufe"
	// Siehe KIBON-2081. Definiert bis zu welcher Schulstufe die soziale Integration ausbezahlt wird.
	@EnumEinstellung(EinschulungTyp.class) FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE(
		MandantIdentifier.getAll()
	),

	// "Alle: Sprachliche Integration bis und mit Schulstufe"
	// Siehe KIBON-2081. Definiert bis zu welcher Schulstufe die soziale Integration ausbezahlt wird.
	@EnumEinstellung(EinschulungTyp.class) SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE(
		MandantIdentifier.getAll()
	),

	// "FKJV: Pauschale nur möglich, wenn Anspruch auf Gutschein"
	// Siehe KIBON-2093. Falls true wird die Pauschale nur ausbezahlt, wenn auch ein Anspruch auf einen Gutschein besteht
	@BooleanEinstellung FKJV_PAUSCHALE_BEI_ANSPRUCH(MandantIdentifier.getAll()),

	// "FKJV: Pauschale auch rückwirkend ausbezahlen, sofern Anspruch vorhanden"
	// Siehe KIBON-2093. Falls true wird die Pauschale bei einer Mutation innerhalb der Gesuchperiode rückwirkend ausbezahlt
	@BooleanEinstellung FKJV_PAUSCHALE_RUECKWIRKEND(MandantIdentifier.getAll()),

	// "EKV nur bei Einkommen unter 80'000"
	// Siehe KIBON-2094. Falls eine Zahl definiert ist, dann besteht ein Anspruch auf eine Einkommensverschlechterung nur bis zu diesem Betrag
	@NumberEinstellung FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF(
		MandantIdentifier.getAll()
	),

	// "FKJV: Anspruchsberechnung monatsweise"
	// Siehe KIBON-2095. Falls true wird der Anspruch nur monatsweise berechnet
	@BooleanEinstellung ANSPRUCH_MONATSWEISE(
		List.of(
			MandantIdentifier.BERN,
			MandantIdentifier.LUZERN,
			MandantIdentifier.SOLOTHURN,
			MandantIdentifier.SCHWYZ,
			MandantIdentifier.ZUG
		)
	),

	// "FKJV: Textanpassungen"
	// Siehe KIBON-2194. Für FKJV Perioden müssen gewisse Texte angepasst werden
	@BooleanEinstellung FKJV_TEXTE(MandantIdentifier.getAll()),

	// (KIBON-4042) Zum Aktivieren von Textänderungen in der Periode 25/26 für den Mandanten SZ
	@BooleanEinstellung TEXTE_SZ_25(MandantIdentifier.SCHWYZ),

	// Definiert ob die Schnittstelle zu den Steuersystemen aktiv ist
	@BooleanEinstellung SCHNITTSTELLE_STEUERN_AKTIV(MandantIdentifier.getAll()),

	// Ferienbetreuung Kosten pro Tag
	@NumberEinstellung FERIENBETREUUNG_CHF_PAUSCHALBETRAG(
		MandantIdentifier.getAll()
	),

	// Ferienbetreuung Kosten pro Tag für Sonderschüler
	@NumberEinstellung FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER(
		MandantIdentifier.getAll()
	),

	// Neue Familiensituation für FJKV
	// Siehe KIBON-2116
	@BooleanEinstellung FKJV_FAMILIENSITUATION_NEU(MandantIdentifier.getAll()),

	// Definiert die Minimallänge für das Konkubinat, damit zwei Antragstellende berücksichtigt werden
	@NumberEinstellung MINIMALDAUER_KONKUBINAT(MandantIdentifier.getAll()),

	// Legt den Typen der finanziellen Verhältnisse fest. Z.B. BERN_ASIV oder LUZERN
	@EnumEinstellung(FinanzielleSituationTyp.class) FINANZIELLE_SITUATION_TYP(
		MandantIdentifier.getAll()
	),

	// Kitaplus Zuschlag aktiviert (Luzern)
	// Siehe KIBON-2131
	@BooleanEinstellung KITAPLUS_ZUSCHLAG_AKTIVIERT(MandantIdentifier.LUZERN),

	// Können BG Konfigurationen in den Gemeinde Einstellungen überschrieben werden (Solothurn)
	// siehe KIBON-2133
	@BooleanEinstellung GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN(
		MandantIdentifier.getAll()
	),

	// Es gibt verschiedene Arten, in welcher Abhängigkeit das Beschaeftigungspensum zum Anspruch steht
	// siehe KIBON-2647
	@EnumEinstellung(AnspruchBeschaeftigungAbhaengigkeitTyp.class) ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM(
		MandantIdentifier.getAll(),
		EinstellungTyp.GEMEINDE
	),

	// Kinderabzug Typ (Solothurn und FKJV)
	// Siehe KIBON-2182
	@EnumEinstellung(KinderabzugTyp.class) KINDERABZUG_TYP(
		MandantIdentifier.getAll()
	),

	// Soll die KESB-Platzierung in der Betreuung deaktiviert sein
	// Siehe KIBON-2177
	@BooleanEinstellung KESB_PLATZIERUNG_DEAKTIVIEREN(
		MandantIdentifier.getAll()
	),

	// Frage für Besondere Beduerfnisse in Luzern aktivieren
	// Siehe KIBON-2189
	@BooleanEinstellung BESONDERE_BEDUERFNISSE_LUZERN(
		MandantIdentifier.getAll()
	),

	// Wie hich ist das maximale Pensum bei ausserordentlichem Anspruch
	@NumberEinstellung FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH(
		MandantIdentifier.getAll()
	),

	// Welche ausserordentliche Anspruch Rule soll verwendet werden
	@EnumEinstellung(AusserordentlicherAnspruchTyp.class) AUSSERORDENTLICHER_ANSPRUCH_RULE(
		MandantIdentifier.getAll()
	),

	// definiert, welche Art von Geschwisternbonus ausbezahlt wird. Mögliche Werte sind LUZERN, SCHWYZ. NONE deaktiviert den Geschwisternbonus
	@EnumEinstellung(GeschwisterbonusTyp.class) GESCHWISTERNBONUS_TYP(
		MandantIdentifier.getAll()
	),

	// Wie lange soll der Babytarif angewendet werden
	@NumberEinstellung DAUER_BABYTARIF(MandantIdentifier.getAll()),

	// Soll die Diplomatenstatusfrage angezeigt werden
	@BooleanEinstellung DIPLOMATENSTATUS_DEAKTIVIERT(
		MandantIdentifier.getAll()
	),

	// Soll die Zemis-Nr. verwendet werden
	@BooleanEinstellung ZEMIS_DISABLED(MandantIdentifier.getAll()),

	// Soll die Frage, ob die Sprache die Amtsprache ist, gestellt werden
	@BooleanEinstellung SPRACHE_AMTSPRACHE_DISABLED(MandantIdentifier.getAll()),

	// falls diese Einstellung disabled ist, dann wechselt der Status des Antrags nach der Freigabe durch den Antragstellenden
	// direkt auf Freigegeben. "Freigabequittung ausstehend" wird übersprungen.
	@BooleanEinstellung FREIGABE_QUITTUNG_EINLESEN_REQUIRED(
		MandantIdentifier.getAll()
	),

	// Unbezahlter Urlaub kann mit dieser Einstellung aktivert oder deaktiviert werden
	@BooleanEinstellung UNBEZAHLTER_URLAUB_AKTIV(MandantIdentifier.getAll()),

	// Fachstellen Typ (KIBON-2360)
	// BERN oder LUZERN
	@EnumEinstellung(FachstellenTyp.class) FACHSTELLEN_TYP(
		MandantIdentifier.getAll()
	),

	// LU: falls diese Einstellung aktiviert ist, wird bei den Gesuchstellenden ein Ausweisnachweis verlangt
	// Siehe KIBON-2310
	@BooleanEinstellung AUSWEIS_NACHWEIS_REQUIRED(MandantIdentifier.getAll()),

	// Switch Eingabe des Betreuungspensums in Tagen oder Prozent erlauben (KIBON-2404)
	@EnumEinstellung(BetreuungspensumAnzeigeTyp.class) PENSUM_ANZEIGE_TYP(
		MandantIdentifier.getAll()
	),

	// Aktiviert die Checkbox, um die Verfügung eingschrieben zu versenden
	@BooleanEinstellung VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT(
		MandantIdentifier.getAll()
	),

	// Erlaubt Abwesenheit zu erfassen in einer Mutation
	@BooleanEinstellung ABWESENHEIT_AKTIV(MandantIdentifier.getAll()),

	// Aktiviert das Input Feld zur Eingabe einer Begründung einer Mutation (KIBON-2538)
	@BooleanEinstellung BEGRUENDUNG_MUTATION_AKTIVIERT(
		MandantIdentifier.getAll()
	),

	// Aktiviert den JSON/CSV Export der Verfügung (KIBON-2622)
	@BooleanEinstellung VERFUEGUNG_EXPORT_ENABLED(MandantIdentifier.getAll()),

	//Setz das minimal Unterschied zwischen Massgebendeseinkommen um eine FinSit Mitteilung zu erstellen
	@NumberEinstellung VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK(
		MandantIdentifier.getAll()
	),

	@BooleanEinstellung
	// Gibt an, ob die Zahlungsangaben für Antragsteller auf der FinSit required oder optional sind (KIBON-2688)
	ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED(MandantIdentifier.BERN),

	@NumberEinstellung
	// Ab welchem Alter in Monaten kann ein Kind Anspruch haben (davor ist der Anspruch 0)
	ANSPRUCH_AB_X_MONATEN(MandantIdentifier.getAll()),

	@BooleanEinstellung
	// Zusätzliche Input Felder für das Ersatzeinkommen anzeigen (KIBON-3249)
	ZUSATZLICHE_FELDER_ERSATZEINKOMMEN(MandantIdentifier.BERN),

	@BooleanEinstellung SPRACHFOERDERUNG_BESTAETIGEN(
		MandantIdentifier.getAll()
	),

	@NumberEinstellung KITA_STUNDEN_PRO_TAG(MandantIdentifier.getAll()),

	@BooleanEinstellung
	//Das Gesuch wird beendent, wenn der Gesuchsteller 2 innerhalb der Periode ändert KIBONBE-31, KIBON-2583
	GESUCH_BEENDEN_BEI_TAUSCH_GS2(MandantIdentifier.getAll()),

	@BooleanEinstellung
	// Addiert die Moeglichkeit Betreuung in die Schulferien getrennt zu melden
	SCHULERGAENZENDE_BETREUUNGEN(MandantIdentifier.getAll()),

	@BooleanEinstellung
	// Die Antragsteller können eine Wegzeit angeben (KIBON-3436)
	WEGZEIT_ERWERBSPENSUM(MandantIdentifier.SCHWYZ),

	@BooleanEinstellung ERWEITERTE_BEDUERFNISSE_AKTIV(
		MandantIdentifier.getAll()
	),

	@BooleanEinstellung ANWESENHEITSTAGE_PRO_MONAT_AKTIVIERT(
		MandantIdentifier.getAll()
	),

	@BooleanEinstellung
	//Die Antragsteller müssen eine Sozialversicherungsnummer angeben  (KIBON-3339)
	SOZIALVERSICHERUNGSNUMMER_PERIODE(MandantIdentifier.getAll()),

	@NumberEinstellung SOZIALABZUG_PRO_KIND(MandantIdentifier.getAll()),

	@BooleanEinstellung
	// Aktiviert die Frage und Berechung "Höhere Beiträge für Kind mit Beeinträchtigung"
	HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT(MandantIdentifier.getAll()),

	@BooleanEinstellung GESUCHFREIGABE_ONLINE(MandantIdentifier.getAll()),

	// Ist für den Mandanten die Möglichkeit Abweichungen zu melden zu deaktivieren
	@BooleanEinstellung ABWEICHUNGEN_ENABLED(
		MandantIdentifier.getAll()
	),

	@BooleanEinstellung TABELLE_EINGABEMASKE(MandantIdentifier.SCHWYZ),

	// Doppelbetreuung Prio
	// Siehe KIBONBE-186
	@EnumEinstellung(BetreuungComparator.class) BETREUUNG_COMPARATOR(
		MandantIdentifier.getAll()
	),

	// Welche Dokumente können in die
	@StringEinstellung() ERNEUERBARE_DOKUMENT_TYPS(MandantIdentifier.getAll());

	private EinstellungTyp typ;
	private List<MandantIdentifier> activeForMandant;

	EinstellungKey(MandantIdentifier activeForMandant) {
		this(activeForMandant, EinstellungTyp.SYSTEM);
	}

	EinstellungKey(List<MandantIdentifier> activeForMandants) {
		this(activeForMandants, EinstellungTyp.SYSTEM);
	}

	EinstellungKey(MandantIdentifier activeForMandant, EinstellungTyp typ) {
		this(List.of(activeForMandant), typ);
	}

	EinstellungKey(
		List<MandantIdentifier> activeForMandants,
		EinstellungTyp typ
	) {
		this.typ = typ;
		this.activeForMandant = activeForMandants;
	}

	public boolean isGemeindeEinstellung() {
		return EinstellungTyp.GEMEINDE == typ;
	}

	public boolean isMandantEinstellung() {
		return EinstellungTyp.MANDANT == typ;
	}

	public boolean isEinstellungActivForMandant(
		MandantIdentifier mandantIdentifier
	) {
		return this.activeForMandant.contains(mandantIdentifier);
	}
}
