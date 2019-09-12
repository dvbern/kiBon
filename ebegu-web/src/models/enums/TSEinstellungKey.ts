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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

export enum TSEinstellungKey {
    GEMEINDE_KONTINGENTIERUNG_ENABLED = 'GEMEINDE_KONTINGENTIERUNG_ENABLED' as any,
    GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' as any,
    GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB = 'GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB' as any,
    GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG = 'GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG' as any,
    MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG' as any,
    MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG' as any,
    MAX_VERGUENSTIGUNG_SCHULE_PRO_TG = 'MAX_VERGUENSTIGUNG_SCHULE_PRO_TG' as any,
    MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD = 'MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD' as any,
    MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD = 'MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD' as any,
    MAX_VERGUENSTIGUNG_SCHULE_PRO_STD = 'MAX_VERGUENSTIGUNG_SCHULE_PRO_STD' as any,
    MIN_MASSGEBENDES_EINKOMMEN = 'MIN_MASSGEBENDES_EINKOMMEN' as any,
    MAX_MASSGEBENDES_EINKOMMEN = 'MAX_MASSGEBENDES_EINKOMMEN' as any,
    OEFFNUNGSTAGE_KITA = 'OEFFNUNGSTAGE_KITA' as any,
    OEFFNUNGSTAGE_TFO = 'OEFFNUNGSTAGE_TFO' as any,
    OEFFNUNGSSTUNDEN_TFO = 'OEFFNUNGSSTUNDEN_TFO' as any,
    ZUSCHLAG_BEHINDERUNG_PRO_TG = 'ZUSCHLAG_BEHINDERUNG_PRO_TG' as any,
    ZUSCHLAG_BEHINDERUNG_PRO_STD = 'ZUSCHLAG_BEHINDERUNG_PRO_STD' as any,
    MIN_VERGUENSTIGUNG_PRO_TG = 'MIN_VERGUENSTIGUNG_PRO_TG' as any,
    MIN_VERGUENSTIGUNG_PRO_STD = 'MIN_VERGUENSTIGUNG_PRO_STD' as any,
    PARAM_PENSUM_KITA_MIN = 'PARAM_PENSUM_KITA_MIN' as any,
    PARAM_PENSUM_TAGESELTERN_MIN = 'PARAM_PENSUM_TAGESELTERN_MIN' as any,
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3 = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3' as any,
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4 = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4' as any,
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5 = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5' as any,
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6 = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6' as any,
    PARAM_MAX_TAGE_ABWESENHEIT = 'PARAM_MAX_TAGE_ABWESENHEIT' as any,
    FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION = 'FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION' as any,
    FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION = 'FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION' as any,
    FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION = 'FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION' as any,
    FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION = 'FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION' as any,
    ERWERBSPENSUM_ZUSCHLAG = 'ERWERBSPENSUM_ZUSCHLAG' as any,
}

export function getTSEinstellungenKeys(): Array<TSEinstellungKey> {
    return [
        TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED,
        TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE,
        TSEinstellungKey.GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB,
        TSEinstellungKey.GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_SCHULE_PRO_TG,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD,
        TSEinstellungKey.MAX_VERGUENSTIGUNG_SCHULE_PRO_STD,
        TSEinstellungKey.MIN_MASSGEBENDES_EINKOMMEN,
        TSEinstellungKey.MAX_MASSGEBENDES_EINKOMMEN,
        TSEinstellungKey.OEFFNUNGSTAGE_KITA,
        TSEinstellungKey.OEFFNUNGSTAGE_TFO,
        TSEinstellungKey.OEFFNUNGSSTUNDEN_TFO,
        TSEinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_TG,
        TSEinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_STD,
        TSEinstellungKey.MIN_VERGUENSTIGUNG_PRO_TG,
        TSEinstellungKey.MIN_VERGUENSTIGUNG_PRO_STD,
        TSEinstellungKey.PARAM_PENSUM_KITA_MIN,
        TSEinstellungKey.PARAM_PENSUM_TAGESELTERN_MIN,
        TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
        TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
        TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
        TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
        TSEinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT,
        TSEinstellungKey.FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION,
        TSEinstellungKey.FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION,
        TSEinstellungKey.FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION,
        TSEinstellungKey.FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION,
        TSEinstellungKey.ERWERBSPENSUM_ZUSCHLAG,
    ];
}