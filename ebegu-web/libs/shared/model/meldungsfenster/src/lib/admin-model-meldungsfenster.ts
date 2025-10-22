import {TSRole} from '@kibon/shared/model/enums';

export enum MeldungsfensterStatus {
    INFO = 'INFO',
    WICHTIG = 'WICHTIG',
    WARNUNG = 'WARNUNG'
}

export type MeldungsfensterData = {
    titleDe: string;
    titleFr: string;
    gueltigAb: Date;
    gueltigBis: Date;
    status: MeldungsfensterStatus;
    zielgruppe: TSRole[];
    inhaltDe: string;
    inhaltFr: string;
};

export type MeldungsfensterRestDTO = {
    titleDe: string;
    titleFr: string;
    gueltigAb: string;
    gueltigBis: string;
    status: string;
    zielgruppe: string[];
    inhaltDe: string;
    inhaltFr: string;
};

export type MeldungsfensterDataFilter = Pick<
    MeldungsfensterData,
    'titleDe' | 'titleFr' | 'status'
>;

export type ClosedMeldungsfenster = {
    closeTime: number;
    bisDatum: Date;
    status: MeldungsfensterStatus;
};

export enum MeldungsfensterTableType {
    NEW = 'NEW',
    ARCHIVE = 'ARCHIVE'
}
