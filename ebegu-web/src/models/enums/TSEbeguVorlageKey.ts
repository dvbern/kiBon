export enum TSEbeguVorlageKey {
    VORLAGE_MAHNUNG_1 = <any>'VORLAGE_MAHNUNG_1',
    VORLAGE_MAHNUNG_2 = <any>'VORLAGE_MAHNUNG_2',
    VORLAGE_NICHT_EINTRETENSVERFUEGUNG = <any>'VORLAGE_NICHT_EINTRETENSVERFUEGUNG',
    VORLAGE_INFOSCHREIBEN_MAXIMALTARIF = <any>'VORLAGE_INFOSCHREIBEN_MAXIMALTARIF',
    VORLAGE_VERFUEGUNG_TAGESELTERN_KLEINKINDER = <any>'VORLAGE_VERFUEGUNG_TAGESELTERN_KLEINKINDER',
    VORLAGE_BRIEF_TAGESELTERN_SCHULKINDER = <any>'VORLAGE_BRIEF_TAGESELTERN_SCHULKINDER',
    VORLAGE_FREIGABEQUITTUNG_JUGENDAMT = <any>'VORLAGE_FREIGABEQUITTUNG_JUGENDAMT',
    VORLAGE_FREIGABEQUITTUNG_SCHULAMT = <any>'VORLAGE_FREIGABEQUITTUNG_SCHULAMT',
    VORLAGE_FINANZIELLE_SITUATION = <any>'VORLAGE_FINANZIELLE_SITUATION',
    VORLAGE_BEGLEITSCHREIBEN = <any>'VORLAGE_BEGLEITSCHREIBEN',
    VORLAGE_VERFUEGUNG_KITA = <any>'VORLAGE_VERFUEGUNG_KITA',
    VORLAGE_BRIEF_TAGESSTAETTE_SCHULKINDER = <any>'VORLAGE_BRIEF_TAGESSTAETTE_SCHULKINDER'
}

export function getTSEbeguVorlageKeyValues(): Array<TSEbeguVorlageKey> {
    return [
        TSEbeguVorlageKey.VORLAGE_MAHNUNG_1,
        TSEbeguVorlageKey.VORLAGE_MAHNUNG_2,
        TSEbeguVorlageKey.VORLAGE_NICHT_EINTRETENSVERFUEGUNG,
        TSEbeguVorlageKey.VORLAGE_INFOSCHREIBEN_MAXIMALTARIF,
        TSEbeguVorlageKey.VORLAGE_VERFUEGUNG_TAGESELTERN_KLEINKINDER,
        TSEbeguVorlageKey.VORLAGE_BRIEF_TAGESELTERN_SCHULKINDER,
        TSEbeguVorlageKey.VORLAGE_FREIGABEQUITTUNG_JUGENDAMT,
        TSEbeguVorlageKey.VORLAGE_FREIGABEQUITTUNG_SCHULAMT,
        TSEbeguVorlageKey.VORLAGE_FINANZIELLE_SITUATION,
        TSEbeguVorlageKey.VORLAGE_BEGLEITSCHREIBEN,
        TSEbeguVorlageKey.VORLAGE_VERFUEGUNG_KITA,
        TSEbeguVorlageKey.VORLAGE_BRIEF_TAGESSTAETTE_SCHULKINDER
    ];
}
