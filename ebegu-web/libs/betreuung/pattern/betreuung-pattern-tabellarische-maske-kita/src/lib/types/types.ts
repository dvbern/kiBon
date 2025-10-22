import {TSBetreuungspensum} from '../../../../../../../src/models/TSBetreuungspensum';
import {TSBetreuung} from '../../../../../../../src/models/TSBetreuung';
import {TSGesuchsperiode} from '@kibon/shared/model/entity';

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
