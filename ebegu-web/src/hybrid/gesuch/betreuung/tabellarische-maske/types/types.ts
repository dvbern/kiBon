import {TSGesuchsperiode} from '../../../../../models/entity/TSGesuchsperiode';
import {TSBetreuungspensum} from '../../../../../models/TSBetreuungspensum';
import {TSBetreuung} from '../../../../../models/TSBetreuung';

export type BetreuungspensumWithTage = TSBetreuungspensum & {
    pensumInTage: number;
};

export type BetreuungspensumWithStunden = TSBetreuungspensum & {
    pensumInStunden: number;
};

export type TabellarischeMaskeDialogData = {
    betreuung: TSBetreuung;
    gesuchsperiode: TSGesuchsperiode;
    templateVariables: {
        kindName: string;
        institutionName: string;
    };
    einstellungen: {
        multiplier: number;
    };
};

type MONTHS =
    | 'AUGUST'
    | 'SEPTEMBER'
    | 'OCTOBER'
    | 'NOVEMBER'
    | 'DECEMBER'
    | 'JANUARY'
    | 'FEBRUARY'
    | 'MARCH'
    | 'APRIL'
    | 'MAY'
    | 'JUNE'
    | 'JULY';

export type MonthAbschnitte<T> = {
    [key in MONTHS]: T;
};
