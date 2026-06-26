import {TSApplicationPropertyKey} from '../../../models/einstellung/TSApplicationPropertyKey';
import {BetreuungComparator} from '../../../models/enums/BetreuungComparator';
import {TSGemeindeZusaetzlicherGutscheinTyp} from '../../../models/enums/gemeindekonfiguration/TSGemeindeZusaetzlicherGutscheinTyp';
import {HoehereBeitraegeTyp} from '../../../models/enums/HoehereBeitraegeTyp';
import {TSAnspruchBeschaeftigungAbhaengigkeitTyp} from '../../../models/enums/TSAnspruchBeschaeftigungAbhaengigkeitTyp';
import {TSAusserordentlicherAnspruchTyp} from '../../../models/enums/TSAusserordentlicherAnspruchTyp';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSDemoFeature} from '../../../models/enums/TSDemoFeature';
import {TSDokumentTyp} from '../../../models/enums/TSDokumentTyp';
import {TSEingewoehnungTyp} from '../../../models/enums/TSEingewoehnungTyp';
import {TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import {TSFachstellenTyp} from '../../../models/enums/TSFachstellenTyp';
import {TSFinanzielleSituationTyp} from '../../../models/enums/TSFinanzielleSituationTyp';
import {TSGeschwisterbonusTyp} from '../../../models/enums/TSGeschwisterbonusTyp';
import {TSKinderabzugTyp} from '../../../models/enums/TSKinderabzugTyp';
import {TSPensumAnzeigeTyp} from '../../../models/enums/TSPensumAnzeigeTyp';
import type {TSEinstellungKey} from '../TSEinstellungKey';

interface EinstellungConfiguration {
    type: EinstellungType;
    options: EinstellungConfigurationOptions;
}

interface EinstellungConfigurationOptions {
    multiple: boolean;
}

type EinstellungConfigurable = {
    [key in AllKeys]: EinstellungConfiguration;
};

const createConfiguration = (
    type: EinstellungType,
    options: EinstellungConfigurationOptions = {multiple: false}
): EinstellungConfiguration => {
    return {type, options};
};

type AllKeys =
    | keyof typeof TSEinstellungKey
    | keyof typeof TSApplicationPropertyKey;

const configEnums = [
    TSEinschulungTyp,
    TSAnspruchBeschaeftigungAbhaengigkeitTyp,
    TSDemoFeature,
    TSEingewoehnungTyp,
    TSFachstellenTyp,
    TSFinanzielleSituationTyp,
    TSKinderabzugTyp,
    TSGemeindeZusaetzlicherGutscheinTyp,
    TSPensumAnzeigeTyp,
    TSGeschwisterbonusTyp,
    TSAusserordentlicherAnspruchTyp,
    TSBetreuungsangebotTyp,
    TSDokumentTyp,
    BetreuungComparator,
    HoehereBeitraegeTyp
] as const;

const EINSTELLUNG_CONFIGURATIONS: EinstellungConfigurable = {
    FKJV_PAUSCHALE_RUECKWIRKEND: createConfiguration(Boolean),
    FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM: createConfiguration(Number),
    FKJV_PAUSCHALE_BEI_ANSPRUCH: createConfiguration(Boolean),
    GESCHWISTERNBONUS_TYP: createConfiguration(TSGeschwisterbonusTyp),
    MAIL_VERSAND_ALLER_MAILS_AUCH_AN_GS_2: createConfiguration(Boolean),
    MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG: createConfiguration(Number),
    MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG: createConfiguration(Number),
    MIN_ERWERBSPENSUM_EINGESCHULT: createConfiguration(Number),
    MIN_ERWERBSPENSUM_NICHT_EINGESCHULT: createConfiguration(Number),
    MIN_TARIF: createConfiguration(Number),
    PARAM_PENSUM_TAGESSCHULE_MIN: createConfiguration(Number),
    ANSPRUCH_MONATSWEISE: createConfiguration(Boolean),
    ABWESENHEIT_AKTIV: createConfiguration(Boolean),
    ABWEICHUNGEN_ENABLED: createConfiguration(Boolean),
    ACTIVATED_DEMO_FEATURES: createConfiguration(TSDemoFeature, {
        multiple: true
    }),
    ANGEBOT_FI_ENABLED: createConfiguration(Boolean),
    ANGEBOT_MITTAGSTISCH_ENABLED: createConfiguration(Boolean),
    ANGEBOT_SCHULSTUFE: createConfiguration(TSBetreuungsangebotTyp, {
        multiple: true
    }),
    ANGEBOT_TFO_ENABLED: createConfiguration(Boolean),
    ANGEBOT_TS_ENABLED: createConfiguration(Boolean),
    ANSPRUCH_AB_X_MONATEN: createConfiguration(Number),
    ANWESENHEITSTAGE_PRO_MONAT_AKTIVIERT: createConfiguration(Boolean),
    ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_FREIGABE:
        createConfiguration(Number),
    ANZAHL_TAGE_BIS_LOESCHUNG_NACH_WARNUNG_QUITTUNG:
        createConfiguration(Number),
    ANZAHL_TAGE_BIS_WARNUNG_FREIGABE: createConfiguration(Number),
    ANZAHL_TAGE_BIS_WARNUNG_QUITTUNG: createConfiguration(Number),
    AUSSERORDENTLICHER_ANSPRUCH_RULE: createConfiguration(
        TSAusserordentlicherAnspruchTyp
    ),
    AUSWEIS_NACHWEIS_REQUIRED: createConfiguration(Boolean),
    AUSZAHLUNGEN_AN_ELTERN: createConfiguration(Boolean),
    BACKGROUND_COLOR: createConfiguration(String),
    BEGRUENDUNG_MUTATION_AKTIVIERT: createConfiguration(Boolean),
    BESONDERE_BEDUERFNISSE_LUZERN: createConfiguration(Boolean),
    BETREUUNG_COMPARATOR: createConfiguration(BetreuungComparator),
    CHECKBOX_AUSZAHLEN_IN_ZUKUNFT: createConfiguration(Boolean),
    DAUER_BABYTARIF: createConfiguration(Number),
    DIPLOMATENSTATUS_DEAKTIVIERT: createConfiguration(Boolean),
    DUMMY_LOGIN_ENABLED: createConfiguration(Boolean),
    EINGEWOEHNUNG_TYP: createConfiguration(TSEingewoehnungTyp),
    ERLAUBEN_INSTITUTIONEN_ZU_WAEHLEN: createConfiguration(Boolean),
    ERWEITERTE_BEDUERFNISSE_AKTIV: createConfiguration(Boolean),
    ERWERBSPENSUM_ZUSCHLAG: createConfiguration(Number),
    EVALUATOR_DEBUG_ENABLED: createConfiguration(Boolean),
    FACHSTELLEN_TYP: createConfiguration(TSFachstellenTyp),
    FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION: createConfiguration(Number),
    FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION: createConfiguration(Number),
    FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION: createConfiguration(Number),
    FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION: createConfiguration(Number),
    FERIENBETREUUNG_AKTIV: createConfiguration(Boolean),
    FERIENBETREUUNG_CHF_PAUSCHALBETRAG: createConfiguration(Number),
    FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER:
        createConfiguration(Number),
    FINANZIELLE_SITUATION_TYP: createConfiguration(TSFinanzielleSituationTyp),
    FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF: createConfiguration(Number),
    FKJV_FAMILIENSITUATION_NEU: createConfiguration(Boolean),
    FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH: createConfiguration(Number),
    FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE:
        createConfiguration(TSEinschulungTyp),
    FKJV_TEXTE: createConfiguration(Boolean),
    TEXTE_SZ_25: createConfiguration(Boolean),
    FREIGABE_QUITTUNG_EINLESEN_REQUIRED: createConfiguration(Boolean),
    FRENCH_ENABLED: createConfiguration(Boolean),
    GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN: createConfiguration(Boolean),
    GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE: createConfiguration(TSEinschulungTyp),
    GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB: createConfiguration(Date),
    GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER:
        createConfiguration(Boolean),
    GEMEINDE_KENNZAHLEN_AKTIV: createConfiguration(Boolean),
    GEMEINDE_KONTINGENTIERUNG_ENABLED: createConfiguration(Boolean),
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN:
        createConfiguration(Number),
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT:
        createConfiguration(Number),
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN:
        createConfiguration(Number),
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT:
        createConfiguration(Number),
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT:
        createConfiguration(Number),
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED: createConfiguration(Boolean),
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED:
        createConfiguration(Boolean),
    GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT:
        createConfiguration(Number),
    GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT: createConfiguration(Number),
    GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT: createConfiguration(Number),
    GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT:
        createConfiguration(Boolean),
    GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA:
        createConfiguration(Number),
    GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO:
        createConfiguration(Number),
    GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE:
        createConfiguration(Number),
    GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG:
        createConfiguration(Number),
    GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED: createConfiguration(Boolean),
    GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB: createConfiguration(Date),
    GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG: createConfiguration(Date),
    GEMEINDE_TAGESSCHULE_TAGIS_ENABLED: createConfiguration(Boolean),
    GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG:
        createConfiguration(Boolean),
    GEMEINDE_VEREINFACHTE_KONFIG_AKTIV: createConfiguration(Boolean),
    GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED:
        createConfiguration(Boolean),
    GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT:
        createConfiguration(Number),
    GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_MAX_AGE_OF_CHILD:
        createConfiguration(Number),
    GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA: createConfiguration(Number),
    GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO: createConfiguration(Number),
    GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED: createConfiguration(Boolean),
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA: createConfiguration(Number),
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO: createConfiguration(Number),
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA:
        createConfiguration(Number),
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO:
        createConfiguration(Number),
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED: createConfiguration(Boolean),
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_KITA_MAX:
        createConfiguration(Number),
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_TFO_MAX:
        createConfiguration(Number),
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MAX_MASSGEBENDES_EINKOMMEN:
        createConfiguration(Number),
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MIN_MASSGEBENDES_EINKOMMEN:
        createConfiguration(Number),
    GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_TYP: createConfiguration(
        TSGemeindeZusaetzlicherGutscheinTyp
    ),
    GERES_ENABLED_FOR_MANDANT: createConfiguration(Boolean),
    GESUCHFREIGABE_ONLINE: createConfiguration(Boolean),
    GESUCH_BEENDEN_BEI_TAUSCH_GS2: createConfiguration(Boolean),
    HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT:
        createConfiguration(HoehereBeitraegeTyp),
    INFOMA_ZAHLUNGEN: createConfiguration(Boolean),
    INSTITUTIONEN_DURCH_GEMEINDEN_EINLADEN: createConfiguration(Boolean),
    KESB_PLATZIERUNG_DEAKTIVIEREN: createConfiguration(Boolean),
    KINDERABZUG_TYP: createConfiguration(TSKinderabzugTyp),
    KITAPLUS_ZUSCHLAG_AKTIVIERT: createConfiguration(Boolean),
    KITA_STUNDEN_PRO_TAG: createConfiguration(Number),
    LASTENAUSGLEICH_AKTIV: createConfiguration(Boolean),
    LASTENAUSGLEICH_TAGESSCHULEN_AKTIV: createConfiguration(Boolean),
    LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_DE:
        createConfiguration(Number),
    LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_FR:
        createConfiguration(Number),
    LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_DE:
        createConfiguration(Number),
    LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_FR:
        createConfiguration(Number),
    FERIENBETREUUNG_ANTEIL_ZWEITPRUEFUNG_DE: createConfiguration(Number),
    FERIENBETREUUNG_ANTEIL_ZWEITPRUEFUNG_FR: createConfiguration(Number),
    FERIENBETREUUNG_AUTO_ZWEITPRUEFUNG_DE: createConfiguration(Number),
    FERIENBETREUUNG_AUTO_ZWEITPRUEFUNG_FR: createConfiguration(Number),
    LATS_LOHNNORMKOSTEN: createConfiguration(Number),
    LATS_LOHNNORMKOSTEN_LESS_THAN_50: createConfiguration(Number),
    LATS_STICHTAG: createConfiguration(Date),
    MAX_MASSGEBENDES_EINKOMMEN: createConfiguration(Number),
    MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD: createConfiguration(Number),
    MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG: createConfiguration(Number),
    MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD: createConfiguration(Number),
    MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD: createConfiguration(Number),
    MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG: createConfiguration(Number),
    MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD: createConfiguration(Number),
    MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG: createConfiguration(Number),
    MINIMALDAUER_KONKUBINAT: createConfiguration(Number),
    MIN_MASSGEBENDES_EINKOMMEN: createConfiguration(Number),
    MIN_VERGUENSTIGUNG_PRO_STD: createConfiguration(Number),
    MIN_VERGUENSTIGUNG_PRO_TG: createConfiguration(Number),
    MULTIMANDANT_AKTIV: createConfiguration(Boolean),
    OEFFNUNGSSTUNDEN_TFO: createConfiguration(Number),
    OEFFNUNGSTAGE_KITA: createConfiguration(Number),
    OEFFNUNGSTAGE_TFO: createConfiguration(Number),
    OEFFNUNGSTAGE_MITTAGSTISCH: createConfiguration(Number),
    PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG: createConfiguration(Number),
    PARAM_MAX_TAGE_ABWESENHEIT: createConfiguration(Number),
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3:
        createConfiguration(Number),
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4:
        createConfiguration(Number),
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5:
        createConfiguration(Number),
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6:
        createConfiguration(Number),
    PARAM_PENSUM_KITA_MIN: createConfiguration(Number),
    PARAM_PENSUM_TAGESELTERN_MIN: createConfiguration(Number),
    PENSUM_ANZEIGE_TYP: createConfiguration(TSPensumAnzeigeTyp),
    PRIMARY_COLOR: createConfiguration(String),
    PRIMARY_COLOR_DARK: createConfiguration(String),
    PRIMARY_COLOR_LIGHT: createConfiguration(String),
    SCHNITTSTELLE_EVENTS_AKTIVIERT: createConfiguration(Boolean),
    SCHNITTSTELLE_SPRACHFOERDERUNG_AKTIV_AB: createConfiguration(Date),
    SCHNITTSTELLE_STEUERN_AKTIV: createConfiguration(Boolean),
    SCHNITTSTELLE_STEUERSYSTEME_AKTIV_AB: createConfiguration(Date),
    SCHULERGAENZENDE_BETREUUNGEN: createConfiguration(Boolean),
    SENTRY_ENV: createConfiguration(String),
    SOZIALVERSICHERUNGSNUMMER_PERIODE: createConfiguration(Boolean),
    SPRACHE_AMTSPRACHE_DISABLED: createConfiguration(Boolean),
    SPRACHFOERDERUNG_BESTAETIGEN: createConfiguration(Boolean),
    SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE:
        createConfiguration(TSEinschulungTyp),
    STADT_BERN_ASIV_CONFIGURED: createConfiguration(Boolean),
    STADT_BERN_ASIV_START_DATUM: createConfiguration(Date),
    TABELLE_EINGABEMASKE: createConfiguration(Boolean),
    UNBEZAHLTER_URLAUB_AKTIV: createConfiguration(Boolean),
    UPLOAD_FILETYPES_WHITELIST: createConfiguration(String),
    VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK: createConfiguration(Number),
    VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT: createConfiguration(Boolean),
    VERFUEGUNG_EXPORT_ENABLED: createConfiguration(Boolean),
    WEGZEIT_ERWERBSPENSUM: createConfiguration(Boolean),
    ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED: createConfiguration(Boolean),
    ZEMIS_DISABLED: createConfiguration(Boolean),
    ZUSATZINFORMATIONEN_INSTITUTION: createConfiguration(Boolean),
    ZUSATZLICHE_FELDER_ERSATZEINKOMMEN: createConfiguration(Boolean),
    ZUSCHLAG_BEHINDERUNG_PRO_STD: createConfiguration(Number),
    ZUSCHLAG_BEHINDERUNG_PRO_TG: createConfiguration(Number),
    ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM: createConfiguration(
        TSAnspruchBeschaeftigungAbhaengigkeitTyp
    ),
    SOZIALABZUG_PRO_KIND: createConfiguration(Number),
    GEMEINDE_KENNZAHLEN_REMINDER_ACTIVATED: createConfiguration(Boolean),
    DOKUMENT_ZU_UEBERNEHMEN_TYPS: createConfiguration(TSDokumentTyp),
    ABGELOESTE_VIEW_ANTRAGSTELLER: createConfiguration(Boolean),
    ABGELOESTE_VIEW_FAMILIENSITUATION: createConfiguration(Boolean),
    ABGELOESTE_VIEW_KINDER_LIST: createConfiguration(Boolean),
    ABGELOESTE_VIEW_KINDER_SINGLE: createConfiguration(Boolean),
    ABGELOESTE_VIEW_BETREUUNG_LIST: createConfiguration(Boolean),
    ABGELOESTE_VIEW_BETREUUNG_SINGLE: createConfiguration(Boolean),
    ABGELOESTE_VIEW_ERWERBSPENSUM_LIST: createConfiguration(Boolean),
    ABGELOESTE_VIEW_ERWERBSPENSUM_SINGLE: createConfiguration(Boolean),
    ABGELOESTE_VIEW_FINSIT_START: createConfiguration(Boolean),
    ABGELOESTE_VIEW_FINSIT_GS: createConfiguration(Boolean),
    ABGELOESTE_VIEW_FINSIT_RESULTATE: createConfiguration(Boolean),
    ABGELOESTE_VIEW_EKV_START: createConfiguration(Boolean),
    ABGELOESTE_VIEW_EKV_GS: createConfiguration(Boolean),
    ABGELOESTE_VIEW_EKV_RESULTATE: createConfiguration(Boolean),
    ABGELOESTE_VIEW_FREIGABE: createConfiguration(Boolean),
    ABGELOESTE_VIEW_VERFUEGUNG_LIST: createConfiguration(Boolean),
    ABGELOESTE_VIEW_VERFUEGUNG_SINGLE: createConfiguration(Boolean),
    QUARKUS_STATISTIK_BETREUUNGSGUTSCHEINE_KINDER: createConfiguration(Boolean),
    QUARKUS_STATISTIK_MITARBEITENDE: createConfiguration(Boolean),
    QUARKUS_STATISTIK_LASTENAUSGLEICH_BG: createConfiguration(Boolean)
};

type ConfigurableEnums = (typeof configEnums)[number];

export type ConfigurableEinstellung = {key: AllKeys; value: string};

export type EinstellungType =
    | typeof Date
    | typeof Boolean
    | ConfigurableEnums
    | typeof String
    | typeof Number;

export function getEinstellungConfig(key: AllKeys): EinstellungConfiguration {
    if (!EINSTELLUNG_CONFIGURATIONS[key]) {
        throw new Error('Missing Einstellung ' + key);
    }
    return EINSTELLUNG_CONFIGURATIONS[key];
}

export function isEnumEinstellung(
    key: AllKeys
): key is keyof ConfigurableEnums {
    const type = getEinstellungConfig(key).type;
    return configEnums.some(enumClass => {
        return type === enumClass;
    });
}
