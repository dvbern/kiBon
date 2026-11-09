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
import ch.dvbern.ebegu.enums.HoehereBeitraegeTyp;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.enums.betreuung.BetreuungComparator;
import ch.dvbern.ebegu.enums.betreuung.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.enums.gemeindekonfiguration.GemeindeZusaetzlicherGutscheinTyp;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import lombok.Getter;

import static ch.dvbern.ebegu.einstellung.EinstellungKeyConfig.gemeindeEinstellung;
import static ch.dvbern.ebegu.einstellung.EinstellungKeyConfig.mandantEinstellung;
import static ch.dvbern.ebegu.einstellung.EinstellungKeyGroup.*;

/**
 * Keys für die Einstellungen
 */
public enum EinstellungKey {

	// Die Gemeinde kennt eine Kontingentierung der Gutscheine
	@BooleanEinstellung GEMEINDE_KONTINGENTIERUNG_ENABLED(
		gemeindeEinstellung(MandantIdentifier.getAll()).inGroup(
			FREIGABE_VERFUEGUNG_GLOBALERE_EINSTELLUNGEN,
			5
		)
	),

	// Bis zu welcher Schulstufe sollen Gutscheine ausgestellt werden?
	@EnumEinstellung(EinschulungTyp.class) GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE(
		gemeindeEinstellung(MandantIdentifier.getAll()).inGroup(
			PLATZBESTAETIGUNG,
			3
		)
	),

	// Fuer welche Angebottyp gilt die SchulstufeCalcRule?
	@StringEinstellung ANGEBOT_SCHULSTUFE(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			PLATZBESTAETIGUNG,
			4
		)
	),

	// Ab welchem Datum können Anmeldungen für die Tagesschule erfasst werden
	@DateEinstellung GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB(
		gemeindeEinstellung(MandantIdentifier.getAll()).inGroup(TAGESSCHULE, 2)
	),

	// Ab welchem Datum können Anmeldungen für die Ferieninsel erfasst werden
	@DateEinstellung GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB(
		gemeindeEinstellung(MandantIdentifier.getAll()).inGroup(
			FERIENBETREUUNG_FERIENINSELN,
			3
		)
	),

	// Erster Schultag der Tagesschule
	@DateEinstellung GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG(
		gemeindeEinstellung(MandantIdentifier.getAll()).inGroup(TAGESSCHULE, 1)
	),

	// Ob die Tageschulen koennen Tagis sein
	@BooleanEinstellung GEMEINDE_TAGESSCHULE_TAGIS_ENABLED(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	@BooleanEinstellung GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Die Gemeinde gewährt einen zusätzlichen Beitrag zum Gutschein
	@BooleanEinstellung GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),
	// Die Art von zusätzlichem Gutschein, den die Gemeinde gewährt
	@EnumEinstellung(GemeindeZusaetzlicherGutscheinTyp.class) GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_TYP(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Betrag des zusätzlichen Beitrags zum Gutschein bei pauschalem Beitrag
	@NumberEinstellung GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA(
		gemeindeEinstellung(MandantIdentifier.getAll())
	), @NumberEinstellung GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	//Maximaler Wert des zusätzlichen Beitrags zum Gutschein bei linearem Beitrag bei Kitas
	@NumberEinstellung GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_KITA_MAX(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Maximaler Wert des zusätzlichen Beitrags zum Gutschein bei linearem Beitrag bei TFOs
	@NumberEinstellung GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_TFO_MAX(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	//Minimales/Maximales massgebendes Einkommen für zusätzlichen Beitrag zum Gutschein durch Gemeinden
	@NumberEinstellung GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MIN_MASSGEBENDES_EINKOMMEN(
		gemeindeEinstellung(MandantIdentifier.getAll())
	), @NumberEinstellung GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MAX_MASSGEBENDES_EINKOMMEN(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Zusaetzlichen Gutschein anbieten bis und mit
	@EnumEinstellung(EinschulungTyp.class) GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA(
		gemeindeEinstellung(MandantIdentifier.getAll())
	), @EnumEinstellung(EinschulungTyp.class) GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Die Gemeinde gewährt einen Zusatzbetrag für Babies
	@BooleanEinstellung GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Alter des Kindes in Monaten, für welches der zusätzliche Babybeitrag maximal gewährleistet wird.
	// Wenn dieser Wert nicht in den Gemeinde-Periodeneinstellungen gesetzt ist, wird standardmässig der
	// Wert DAUER_BABYTARIF aus den Periodeneinstellungen des Mandanten verwendet.
	@NumberEinstellung GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_MAX_AGE_OF_CHILD(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Betrag des zusätzlichen Gutscheins für Babies
	@NumberEinstellung GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA(
		gemeindeEinstellung(MandantIdentifier.getAll())
	), @NumberEinstellung GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Die Gemeinde akzeptiert Freiwilligenarbeit als Erwerbspensum mit Anspruch
	@BooleanEinstellung GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Maximale Prozente, zu welchen Freiwilligenarbeit zu einem Anspruch führt
	@NumberEinstellung GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Die Gemeinde gewährt eine Mahlzeitenvergünsgigung
	@BooleanEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Mahlzeitenverguenstigungsstufen Verguenstigung Haupt- und Nebenmahlzeit sowie Maximaleinkommen
	@NumberEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT(
		gemeindeEinstellung(MandantIdentifier.getAll())
	), @NumberEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN(
		gemeindeEinstellung(MandantIdentifier.getAll())
	), @NumberEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT(
		gemeindeEinstellung(MandantIdentifier.getAll())
	), @NumberEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN(
		gemeindeEinstellung(MandantIdentifier.getAll())
	), @NumberEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Die Gemeinde gewährt die Mahlzeitenvergünstigung auch für Sozialhilfebezüger
	@BooleanEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED(
		gemeindeEinstellung(MandantIdentifier.getAll())
	), @NumberEinstellung GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Die Schnittstelle zu Ki-Tax ist aktiviert
	@BooleanEinstellung GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Minimales Erwerbspensum, wenn das Kind nicht eingeschult ist
	@NumberEinstellung GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),
	// Minimales Erwerbspensum, wenn das Kind eingeschult ist
	@NumberEinstellung GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT(
		gemeindeEinstellung(MandantIdentifier.getAll())
	),

	// Die Gemeinde gewährt einen Zusatzbetrag für hohere Einkommensklassen
	@BooleanEinstellung GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT(
		gemeindeEinstellung(MandantIdentifier.SOLOTHURN)
	),

	// Die Gemeinde Zusatzbetrag für hohere Einkommensklassen KITA
	@NumberEinstellung GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA(
		gemeindeEinstellung(MandantIdentifier.SOLOTHURN)
	),

	// Die Gemeinde Zusatzbetrag für hohere Einkommensklassen TFO
	@NumberEinstellung GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO(
		gemeindeEinstellung(MandantIdentifier.SOLOTHURN)
	), @NumberEinstellung GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE(
		gemeindeEinstellung(MandantIdentifier.SOLOTHURN)
	),

	// Der Betrag welcher für die Berechnung des Gutscheines pro Zeiteinheit als maximal maasgebendes Einkommen verwendet werden soll
	@NumberEinstellung GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG(
		gemeindeEinstellung(MandantIdentifier.SOLOTHURN)
	),

	// Wenn aktiv werden in der Gemeinde werde keine Gutscheine für Sozialhilfeempfänger ausgestellt
	@BooleanEinstellung GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER(
		gemeindeEinstellung(MandantIdentifier.SOLOTHURN)
	),

	// *** Einstellungen fuer die Gutscheinberechnung

	// Maximale Vergünstigung für Vorschulkinder unter 12 Monaten
	@NumberEinstellung MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			KITA_BETREUUNG,
			6
		)
	),
	// Maximale Vergünstigung für Vorschulkinder ab 12 Monaten
	@NumberEinstellung MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			KITA_BETREUUNG,
			5
		)
	),
	// Maximale Vergünstigung bei Eintritt des Kindergartens
	@NumberEinstellung MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			KITA_BETREUUNG,
			7
		)
	),

	// Maximale Vergünstigung für Vorschulkinder unter 12 Monaten
	@NumberEinstellung MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TFO_BETREUUNG, 6)
	),
	// Maximale Vergünstigung für Vorschulkinder ab 12 Monaten
	@NumberEinstellung MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TFO_BETREUUNG, 5)
	),
	// Maximale Vergünstigung bei Eintritt des Kindergartens
	@NumberEinstellung MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TFO_BETREUUNG, 7)
	),

	// Maximale Vergünstigung bei Eintritt in die Primarstufe
	@NumberEinstellung MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TFO_BETREUUNG, 8)
	),

	// Minimal Massgebendes Einkommen
	@NumberEinstellung MIN_MASSGEBENDES_EINKOMMEN(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FINANZIELLE_SITUATION_MASSGEBENDES_EINKOMMEN,
			2
		)
	),
	// Maximal Massgebendes Einkommen
	@NumberEinstellung MAX_MASSGEBENDES_EINKOMMEN(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FINANZIELLE_SITUATION_MASSGEBENDES_EINKOMMEN,
			1
		)
	),

	@NumberEinstellung OEFFNUNGSTAGE_KITA(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			KITA_BETREUUNG,
			3
		)
	), @NumberEinstellung OEFFNUNGSTAGE_TFO(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TFO_BETREUUNG, 3)
	), @NumberEinstellung OEFFNUNGSSTUNDEN_TFO(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TFO_BETREUUNG, 2)
	), @NumberEinstellung OEFFNUNGSTAGE_MITTAGSTISCH(
		mandantEinstellung(MandantIdentifier.SCHWYZ).inGroup(MITTAGSTISCH, 1)
	),

	@NumberEinstellung ZUSCHLAG_BEHINDERUNG_PRO_TG(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			BESONDERE_BEDUERFNISSE_AUSSERORDENTLICHER_BETREUUNGSAUFWAND,
			3
		)
	), @NumberEinstellung ZUSCHLAG_BEHINDERUNG_PRO_STD(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			BESONDERE_BEDUERFNISSE_AUSSERORDENTLICHER_BETREUUNGSAUFWAND,
			2
		)
	),

	@NumberEinstellung MIN_VERGUENSTIGUNG_PRO_TG(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			KITA_BETREUUNG,
			4
		)
	), @NumberEinstellung MIN_VERGUENSTIGUNG_PRO_STD(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TFO_BETREUUNG, 4)
	),

	// *** Einstellungen fuer die Gutscheinberechnung

	// Minimales Erwerbspensum, wenn das Kind nicht eingeschult ist
	@NumberEinstellung MIN_ERWERBSPENSUM_NICHT_EINGESCHULT(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			ANSPRUCHSBERECHTIGUNG_PENSUM_REGELN,
			5
		)
	),
	// Minimales Erwerbspensum, wenn das Kind eingeschult ist
	@NumberEinstellung MIN_ERWERBSPENSUM_EINGESCHULT(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			ANSPRUCHSBERECHTIGUNG_PENSUM_REGELN,
			4
		)
	),
	// Zuschlag, um den der Anspruch aufgrund des Erwerbspensums automatisch erhöht wird
	@NumberEinstellung ERWERBSPENSUM_ZUSCHLAG(
		gemeindeEinstellung(MandantIdentifier.getAll()).inGroup(
			ANSPRUCHSBERECHTIGUNG_PENSUM_REGELN,
			6
		)
	),

	// Min Pensum Kitas
	@NumberEinstellung PARAM_PENSUM_KITA_MIN(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			KITA_BETREUUNG,
			1
		)
	),

	// Min Pensum Tageseltern
	@NumberEinstellung PARAM_PENSUM_TAGESELTERN_MIN(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TFO_BETREUUNG, 1)
	),

	// Min Pensum Tagesschule
	@NumberEinstellung PARAM_PENSUM_TAGESSCHULE_MIN(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TAGESSCHULE, 3)
	),

	// Pauschalabzug bei einer Familiengrösse von drei Personen pauschal pro Person
	@NumberEinstellung PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			KINDERABZUG_PAUSCHALABZUG,
			3
		)
	),

	// Pauschalabzug bei einer Familiengrösse von vier Personen pauschal pro Person
	@NumberEinstellung PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			KINDERABZUG_PAUSCHALABZUG,
			4
		)
	),

	// Pauschalabzug bei einer Familiengrösse von fünf Personen pauschal pro Person
	@NumberEinstellung PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			KINDERABZUG_PAUSCHALABZUG,
			5
		)
	),

	// Pauschalabzug bei einer Familiengrösse von sechs Personen pauschal pro Person
	@NumberEinstellung PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			KINDERABZUG_PAUSCHALABZUG,
			6
		)
	),

	// Max Abwesenheit
	@NumberEinstellung PARAM_MAX_TAGE_ABWESENHEIT(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FREIGABE_VERFUEGUNG_GLOBALERE_EINSTELLUNGEN,
			11
		)
	),

	// Eine Einkommensverschlechterung wird nur berücksichtigt, wenn diese höher als 20% des Ausgangswertes ist.
	@NumberEinstellung PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			EINKOMMENSAENDERUNG,
			2
		)
	),

	//Pensum Fachstelle soziale Integration
	@NumberEinstellung FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FACHSTELLEN_INTEGRATION,
			2
		)
	), @NumberEinstellung FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FACHSTELLEN_INTEGRATION,
			3
		)
	),

	//Pensum Fachstelle soziale Integration
	@NumberEinstellung FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FACHSTELLEN_INTEGRATION,
			4
		)
	), @NumberEinstellung FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FACHSTELLEN_INTEGRATION,
			5
		)
	),

	//Tagesschule Max Min Tarife
	@NumberEinstellung MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TAGESSCHULE, 7)
	), @NumberEinstellung MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TAGESSCHULE, 8)
	), @NumberEinstellung MIN_TARIF(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TAGESSCHULE, 4)
	),

	//LATS
	@NumberEinstellung LATS_LOHNNORMKOSTEN(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TAGESSCHULE, 6)
	), @NumberEinstellung LATS_LOHNNORMKOSTEN_LESS_THAN_50(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TAGESSCHULE, 5)
	), @DateEinstellung LATS_STICHTAG(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FREIGABE_VERFUEGUNG_GLOBALERE_EINSTELLUNGEN,
			8
		)
	),

	// "FKJV: Eingewöhnung aktiviert".
	// Siehe KIBON-2078. Definiert, ob das Kind einen zusätzlichen Anspruch auf Eingewöhnung hat
	@EnumEinstellung(EingewoehnungTyp.class) EINGEWOEHNUNG_TYP(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			PLATZBESTAETIGUNG,
			9
		)
	),

	// "FKJV: Maximale Differenz zwischen erforderlichem und effektivem Beschäftigungspensum für ausserordentlicher Anspruch"
	// Siehe KIBON-2080. Definiert die maximale Differenz zwischen erforderlichem und effektiven Beschäftigunspensum für den ausserordentlichen Anspruch.
	// in Prozent.
	@NumberEinstellung FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			ANSPRUCHSBERECHTIGUNG_PENSUM_REGELN,
			9
		)
	),

	// "FKJV: Soziale Integration bis und mit Schulstufe"
	// Siehe KIBON-2081. Definiert bis zu welcher Schulstufe die soziale Integration ausbezahlt wird.
	@EnumEinstellung(EinschulungTyp.class) FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FACHSTELLEN_INTEGRATION,
			7
		)
	),

	// "Alle: Sprachliche Integration bis und mit Schulstufe"
	// Siehe KIBON-2081. Definiert bis zu welcher Schulstufe die soziale Integration ausbezahlt wird.
	@EnumEinstellung(EinschulungTyp.class) SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FACHSTELLEN_INTEGRATION,
			6
		)
	),

	// "FKJV: Pauschale nur möglich, wenn Anspruch auf Gutschein"
	// Siehe KIBON-2093. Falls true wird die Pauschale nur ausbezahlt, wenn auch ein Anspruch auf einen Gutschein besteht
	@BooleanEinstellung FKJV_PAUSCHALE_BEI_ANSPRUCH(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			BESONDERE_BEDUERFNISSE_AUSSERORDENTLICHER_BETREUUNGSAUFWAND,
			5
		)
	),

	// "FKJV: Pauschale auch rückwirkend ausbezahlen, sofern Anspruch vorhanden"
	// Siehe KIBON-2093. Falls true wird die Pauschale bei einer Mutation innerhalb der Gesuchperiode rückwirkend ausbezahlt
	@BooleanEinstellung FKJV_PAUSCHALE_RUECKWIRKEND(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			BESONDERE_BEDUERFNISSE_AUSSERORDENTLICHER_BETREUUNGSAUFWAND,
			4
		)
	),

	// "EKV nur bei Einkommen unter 80'000"
	// Siehe KIBON-2094. Falls eine Zahl definiert ist, dann besteht ein Anspruch auf eine Einkommensverschlechterung nur bis zu diesem Betrag
	@NumberEinstellung FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			EINKOMMENSAENDERUNG,
			1
		)
	),

	// "FKJV: Anspruchsberechnung monatsweise"
	// Siehe KIBON-2095. Falls true wird der Anspruch nur monatsweise berechnet
	@BooleanEinstellung ANSPRUCH_MONATSWEISE(
		mandantEinstellung(
			List.of(
				MandantIdentifier.BERN,
				MandantIdentifier.LUZERN,
				MandantIdentifier.SOLOTHURN,
				MandantIdentifier.SCHWYZ,
				MandantIdentifier.ZUG
			)
		).inGroup(ANSPRUCHSBERECHTIGUNG_PENSUM_REGELN, 1)
	),

	// "FKJV: Textanpassungen"
	// Siehe KIBON-2194. Für FKJV Perioden müssen gewisse Texte angepasst werden
	@BooleanEinstellung FKJV_TEXTE(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FREIGABE_VERFUEGUNG_GLOBALERE_EINSTELLUNGEN,
			9
		)
	),

	// (KIBON-4042) Zum Aktivieren von Textänderungen in der Periode 25/26 für den Mandanten SZ
	@BooleanEinstellung TEXTE_SZ_25(
		mandantEinstellung(MandantIdentifier.SCHWYZ).inGroup(
			FREIGABE_VERFUEGUNG_GLOBALERE_EINSTELLUNGEN,
			12
		)
	),

	// Definiert ob die Schnittstelle zu den Steuersystemen aktiv ist
	@BooleanEinstellung SCHNITTSTELLE_STEUERN_AKTIV(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FINANZIELLE_SITUATION_MASSGEBENDES_EINKOMMEN,
			5
		)
	),

	// Ferienbetreuung Kosten pro Tag
	@NumberEinstellung FERIENBETREUUNG_CHF_PAUSCHALBETRAG(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FERIENBETREUUNG_FERIENINSELN,
			1
		)
	),

	// Ferienbetreuung Kosten pro Tag für Sonderschüler
	@NumberEinstellung FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FERIENBETREUUNG_FERIENINSELN,
			2
		)
	),

	// Neue Familiensituation für FJKV
	// Siehe KIBON-2116
	@BooleanEinstellung FKJV_FAMILIENSITUATION_NEU(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FAMILIENSITUATION,
			1
		)
	),

	// Definiert die Minimallänge für das Konkubinat, damit zwei Antragstellende berücksichtigt werden
	@NumberEinstellung MINIMALDAUER_KONKUBINAT(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FAMILIENSITUATION,
			3
		)
	),

	// Legt den Typen der finanziellen Verhältnisse fest. Z.B. BERN_ASIV oder LUZERN
	@EnumEinstellung(FinanzielleSituationTyp.class) FINANZIELLE_SITUATION_TYP(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FINANZIELLE_SITUATION_MASSGEBENDES_EINKOMMEN,
			3
		)
	),

	// Kitaplus Zuschlag aktiviert (Luzern)
	// Siehe KIBON-2131
	@BooleanEinstellung KITAPLUS_ZUSCHLAG_AKTIVIERT(
		mandantEinstellung(MandantIdentifier.LUZERN)
	),

	// Können BG Konfigurationen in den Gemeinde Einstellungen überschrieben werden (Solothurn)
	// siehe KIBON-2133
	@BooleanEinstellung GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FREIGABE_VERFUEGUNG_GLOBALERE_EINSTELLUNGEN,
			7
		)
	),

	// Es gibt verschiedene Arten, in welcher Abhängigkeit das Beschaeftigungspensum zum Anspruch steht
	// siehe KIBON-2647
	@EnumEinstellung(AnspruchBeschaeftigungAbhaengigkeitTyp.class) ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM(
		gemeindeEinstellung(MandantIdentifier.getAll()).inGroup(
			ANSPRUCHSBERECHTIGUNG_PENSUM_REGELN,
			2
		)
	),

	// Kinderabzug Typ (Solothurn und FKJV)
	// Siehe KIBON-2182
	@EnumEinstellung(KinderabzugTyp.class) KINDERABZUG_TYP(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			KINDERABZUG_PAUSCHALABZUG,
			1
		)
	),

	// Soll die KESB-Platzierung in der Betreuung deaktiviert sein
	// Siehe KIBON-2177
	@BooleanEinstellung KESB_PLATZIERUNG_DEAKTIVIEREN(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			ANTRAGSTELLENDE_NACHWEISE_FORMALITAETEN,
			3
		)
	),

	// Frage für Besondere Beduerfnisse in Luzern aktivieren
	// Siehe KIBON-2189
	@BooleanEinstellung BESONDERE_BEDUERFNISSE_LUZERN(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			BESONDERE_BEDUERFNISSE_AUSSERORDENTLICHER_BETREUUNGSAUFWAND,
			7
		)
	),

	// Wie hich ist das maximale Pensum bei ausserordentlichem Anspruch
	@NumberEinstellung FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			ANSPRUCHSBERECHTIGUNG_PENSUM_REGELN,
			8
		)
	),

	// Welche ausserordentliche Anspruch Rule soll verwendet werden
	@EnumEinstellung(AusserordentlicherAnspruchTyp.class) AUSSERORDENTLICHER_ANSPRUCH_RULE(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			ANSPRUCHSBERECHTIGUNG_PENSUM_REGELN,
			7
		)
	),

	// definiert, welche Art von Geschwisternbonus ausbezahlt wird. Mögliche Werte sind LUZERN, SCHWYZ. NONE deaktiviert den Geschwisternbonus
	@EnumEinstellung(GeschwisterbonusTyp.class) GESCHWISTERNBONUS_TYP(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			PLATZBESTAETIGUNG,
			10
		)
	),

	// Wie lange soll der Babytarif angewendet werden
	@NumberEinstellung DAUER_BABYTARIF(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			PLATZBESTAETIGUNG,
			2
		)
	),

	// Soll die Diplomatenstatusfrage angezeigt werden
	@BooleanEinstellung DIPLOMATENSTATUS_DEAKTIVIERT(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			ANTRAGSTELLENDE_NACHWEISE_FORMALITAETEN,
			5
		)
	),

	// Soll die Zemis-Nr. verwendet werden
	@BooleanEinstellung ZEMIS_DISABLED(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			ANTRAGSTELLENDE_NACHWEISE_FORMALITAETEN,
			7
		)
	),

	// Soll die Frage, ob die Sprache die Amtsprache ist, gestellt werden
	@BooleanEinstellung SPRACHE_AMTSPRACHE_DISABLED(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			ANTRAGSTELLENDE_NACHWEISE_FORMALITAETEN,
			6
		)
	),

	// falls diese Einstellung disabled ist, dann wechselt der Status des Antrags nach der Freigabe durch den Antragstellenden
	// direkt auf Freigegeben. "Freigabequittung ausstehend" wird übersprungen.
	@BooleanEinstellung FREIGABE_QUITTUNG_EINLESEN_REQUIRED(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FREIGABE_VERFUEGUNG_GLOBALERE_EINSTELLUNGEN,
			2
		)
	),

	// Unbezahlter Urlaub kann mit dieser Einstellung aktivert oder deaktiviert werden
	@BooleanEinstellung UNBEZAHLTER_URLAUB_AKTIV(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			ANTRAGSTELLENDE_NACHWEISE_FORMALITAETEN,
			4
		)
	),

	// Fachstellen Typ (KIBON-2360)
	// BERN oder LUZERN
	@EnumEinstellung(FachstellenTyp.class) FACHSTELLEN_TYP(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FACHSTELLEN_INTEGRATION,
			1
		)
	),

	// LU: falls diese Einstellung aktiviert ist, wird bei den Gesuchstellenden ein Ausweisnachweis verlangt
	// Siehe KIBON-2310
	@BooleanEinstellung AUSWEIS_NACHWEIS_REQUIRED(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			ANTRAGSTELLENDE_NACHWEISE_FORMALITAETEN,
			2
		)
	),

	// Switch Eingabe des Betreuungspensums in Tagen oder Prozent erlauben (KIBON-2404)
	@EnumEinstellung(BetreuungspensumAnzeigeTyp.class) PENSUM_ANZEIGE_TYP(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			PLATZBESTAETIGUNG,
			1
		)
	),

	// Aktiviert die Checkbox, um die Verfügung eingschrieben zu versenden
	@BooleanEinstellung VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FREIGABE_VERFUEGUNG_GLOBALERE_EINSTELLUNGEN,
			3
		)
	),

	// Erlaubt Abwesenheit zu erfassen in einer Mutation
	@BooleanEinstellung ABWESENHEIT_AKTIV(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FREIGABE_VERFUEGUNG_GLOBALERE_EINSTELLUNGEN,
			10
		)
	),

	// Aktiviert das Input Feld zur Eingabe einer Begründung einer Mutation (KIBON-2538)
	@BooleanEinstellung BEGRUENDUNG_MUTATION_AKTIVIERT(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FREIGABE_VERFUEGUNG_GLOBALERE_EINSTELLUNGEN,
			6
		)
	),

	// Aktiviert den JSON/CSV Export der Verfügung (KIBON-2622)
	@BooleanEinstellung VERFUEGUNG_EXPORT_ENABLED(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FREIGABE_VERFUEGUNG_GLOBALERE_EINSTELLUNGEN,
			4
		)
	),

	//Setz das minimal Unterschied zwischen Massgebendeseinkommen um eine FinSit Mitteilung zu erstellen
	@NumberEinstellung VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FINANZIELLE_SITUATION_MASSGEBENDES_EINKOMMEN,
			6
		)
	),

	@BooleanEinstellung
	// Gibt an, ob die Zahlungsangaben für Antragsteller auf der FinSit required oder optional sind (KIBON-2688)
	ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED(
		mandantEinstellung(MandantIdentifier.BERN).inGroup(
			FINANZIELLE_SITUATION_MASSGEBENDES_EINKOMMEN,
			4
		)
	),

	@NumberEinstellung
	// Ab welchem Alter in Monaten kann ein Kind Anspruch haben (davor ist der Anspruch 0)
	ANSPRUCH_AB_X_MONATEN(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			ANSPRUCHSBERECHTIGUNG_PENSUM_REGELN,
			3
		)
	),

	@BooleanEinstellung
	// Zusätzliche Input Felder für das Ersatzeinkommen anzeigen (KIBON-3249)
	ZUSATZLICHE_FELDER_ERSATZEINKOMMEN(
		mandantEinstellung(MandantIdentifier.BERN).inGroup(
			FINANZIELLE_SITUATION_MASSGEBENDES_EINKOMMEN,
			7
		)
	),

	@BooleanEinstellung SPRACHFOERDERUNG_BESTAETIGEN(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FACHSTELLEN_INTEGRATION,
			8
		)
	),

	@NumberEinstellung KITA_STUNDEN_PRO_TAG(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			KITA_BETREUUNG,
			2
		)
	),

	@BooleanEinstellung
	//Das Gesuch wird beendent, wenn der Gesuchsteller 2 innerhalb der Periode ändert KIBONBE-31, KIBON-2583
	GESUCH_BEENDEN_BEI_TAUSCH_GS2(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FAMILIENSITUATION,
			2
		)
	),

	@BooleanEinstellung
	// Addiert die Moeglichkeit Betreuung in die Schulferien getrennt zu melden
	SCHULERGAENZENDE_BETREUUNGEN(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			PLATZBESTAETIGUNG,
			5
		)
	),

	@BooleanEinstellung
	// Die Antragsteller können eine Wegzeit angeben (KIBON-3436)
	WEGZEIT_ERWERBSPENSUM(
		mandantEinstellung(MandantIdentifier.SCHWYZ).inGroup(
			ANSPRUCHSBERECHTIGUNG_PENSUM_REGELN,
			10
		)
	),

	@BooleanEinstellung ERWEITERTE_BEDUERFNISSE_AKTIV(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			BESONDERE_BEDUERFNISSE_AUSSERORDENTLICHER_BETREUUNGSAUFWAND,
			1
		)
	),

	@BooleanEinstellung ANWESENHEITSTAGE_PRO_MONAT_AKTIVIERT(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(TFO_BETREUUNG, 9)
	),

	@BooleanEinstellung
	//Die Antragsteller müssen eine Sozialversicherungsnummer angeben  (KIBON-3339)
	SOZIALVERSICHERUNGSNUMMER_PERIODE(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			ANTRAGSTELLENDE_NACHWEISE_FORMALITAETEN,
			1
		)
	),

	@NumberEinstellung SOZIALABZUG_PRO_KIND(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			KINDERABZUG_PAUSCHALABZUG,
			2
		)
	),

	// Aktiviert die Frage und Berechung "Höhere Beiträge für Kind mit Beeinträchtigung"
	@EnumEinstellung(HoehereBeitraegeTyp.class) HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			BESONDERE_BEDUERFNISSE_AUSSERORDENTLICHER_BETREUUNGSAUFWAND,
			6
		)
	),

	@BooleanEinstellung GESUCHFREIGABE_ONLINE(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			FREIGABE_VERFUEGUNG_GLOBALERE_EINSTELLUNGEN,
			1
		)
	),

	// Ist für den Mandanten die Möglichkeit Abweichungen zu melden zu deaktivieren
	@BooleanEinstellung ABWEICHUNGEN_ENABLED(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			PLATZBESTAETIGUNG,
			7
		)
	),

	@BooleanEinstellung TABELLE_EINGABEMASKE(
		mandantEinstellung(MandantIdentifier.SCHWYZ).inGroup(
			PLATZBESTAETIGUNG,
			6
		)
	),

	// Doppelbetreuung Prio
	// Siehe KIBONBE-186
	@EnumEinstellung(BetreuungComparator.class) BETREUUNG_COMPARATOR(
		mandantEinstellung(MandantIdentifier.getAll()).inGroup(
			PLATZBESTAETIGUNG,
			8
		)
	),

	// Welche Dokumente können in die
	@StringEinstellung() DOKUMENT_ZU_UEBERNEHMEN_TYPS(
		mandantEinstellung(MandantIdentifier.getAll())
	);

	private final EinstellungTyp typ;
	private final List<MandantIdentifier> activeForMandant;
	@Getter
	private final KeyGrouping keyGrouping;

	EinstellungKey(EinstellungKeyConfig config) {
		this.typ = config.typ();
		this.activeForMandant = config.mandants();
		this.keyGrouping = config.grouping();
	}

	public boolean isGemeindeEinstellung() {
		return EinstellungTyp.GEMEINDE == typ;
	}

	public boolean isEinstellungActivForMandant(
		MandantIdentifier mandantIdentifier
	) {
		return this.activeForMandant.contains(mandantIdentifier);
	}
}
