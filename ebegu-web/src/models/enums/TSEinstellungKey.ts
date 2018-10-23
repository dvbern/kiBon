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
    PARAM_FIXBETRAG_STADT_PRO_TAG_KITA_HALBJAHR_1 = 'PARAM_FIXBETRAG_STADT_PRO_TAG_KITA_HALBJAHR_1' as any,
    PARAM_FIXBETRAG_STADT_PRO_TAG_KITA_HALBJAHR_2 = 'PARAM_FIXBETRAG_STADT_PRO_TAG_KITA_HALBJAHR_2' as any,
    PARAM_ANZAL_TAGE_MAX_KITA = 'PARAM_ANZAL_TAGE_MAX_KITA' as any,
    PARAM_STUNDEN_PRO_TAG_MAX_KITA = 'PARAM_STUNDEN_PRO_TAG_MAX_KITA' as any,
    PARAM_KOSTEN_PRO_STUNDE_MAX = 'PARAM_KOSTEN_PRO_STUNDE_MAX' as any,
    PARAM_KOSTEN_PRO_STUNDE_MAX_TAGESELTERN = 'PARAM_KOSTEN_PRO_STUNDE_MAX_TAGESELTERN' as any,
    PARAM_KOSTEN_PRO_STUNDE_MIN = 'PARAM_KOSTEN_PRO_STUNDE_MIN' as any,
    PARAM_MASSGEBENDES_EINKOMMEN_MIN = 'PARAM_MASSGEBENDES_EINKOMMEN_MIN' as any,
    PARAM_MASSGEBENDES_EINKOMMEN_MAX = 'PARAM_MASSGEBENDES_EINKOMMEN_MAX' as any,
    PARAM_ANZAHL_TAGE_KANTON = 'PARAM_ANZAHL_TAGE_KANTON' as any,
    PARAM_STUNDEN_PRO_TAG_TAGI = 'PARAM_STUNDEN_PRO_TAG_TAGI' as any,
    PARAM_PENSUM_TAGI_MIN = 'PARAM_PENSUM_TAGI_MIN' as any,
    PARAM_PENSUM_KITA_MIN = 'PARAM_PENSUM_KITA_MIN' as any,
    PARAM_PENSUM_TAGESELTERN_MIN = 'PARAM_PENSUM_TAGESELTERN_MIN' as any,
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3 = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3' as any,
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4 = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4' as any,
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5 = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5' as any,
    PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6 = 'PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6' as any,
    PARAM_MAX_TAGE_ABWESENHEIT = 'PARAM_MAX_TAGE_ABWESENHEIT' as any,
    PARAM_ABGELTUNG_PRO_TAG_KANTON = 'PARAM_ABGELTUNG_PRO_TAG_KANTON' as any,
    PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM = 'PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM' as any,
}

export function getTSEinstellungenKeys(): Array<TSEinstellungKey> {
    return [
        TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED,
        TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE,
        TSEinstellungKey.PARAM_FIXBETRAG_STADT_PRO_TAG_KITA_HALBJAHR_1,
        TSEinstellungKey.PARAM_FIXBETRAG_STADT_PRO_TAG_KITA_HALBJAHR_2,
        TSEinstellungKey.PARAM_ANZAL_TAGE_MAX_KITA,
        TSEinstellungKey.PARAM_STUNDEN_PRO_TAG_MAX_KITA,
        TSEinstellungKey.PARAM_KOSTEN_PRO_STUNDE_MAX,
        TSEinstellungKey.PARAM_KOSTEN_PRO_STUNDE_MAX_TAGESELTERN,
        TSEinstellungKey.PARAM_KOSTEN_PRO_STUNDE_MIN,
        TSEinstellungKey.PARAM_MASSGEBENDES_EINKOMMEN_MIN,
        TSEinstellungKey.PARAM_MASSGEBENDES_EINKOMMEN_MAX,
        TSEinstellungKey.PARAM_ANZAHL_TAGE_KANTON,
        TSEinstellungKey.PARAM_STUNDEN_PRO_TAG_TAGI,
        TSEinstellungKey.PARAM_PENSUM_TAGI_MIN,
        TSEinstellungKey.PARAM_PENSUM_KITA_MIN,
        TSEinstellungKey.PARAM_PENSUM_TAGESELTERN_MIN,
        TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
        TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
        TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
        TSEinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
        TSEinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT,
        TSEinstellungKey.PARAM_ABGELTUNG_PRO_TAG_KANTON,
        TSEinstellungKey.PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM,
    ];
}
