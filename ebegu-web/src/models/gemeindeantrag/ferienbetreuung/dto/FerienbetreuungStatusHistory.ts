import {TSFerienbetreuungAngabenContainer} from '../../TSFerienbetreuungAngabenContainer';
import {FerienbetreuungAngabenStatus} from '../../../enums/FerienbetreuungAngabenStatus';
import {TSBenutzer} from '../../../TSBenutzer';

export type FerienbetreuungStatusHistory = {
    container: TSFerienbetreuungAngabenContainer;
    status: FerienbetreuungAngabenStatus;
    timestampVon: Date;
    timestampBis: Date;
    benutzer: TSBenutzer;
};

export type FerienbetreuungStatusHistoryDTO = Omit<
    FerienbetreuungStatusHistory,
    'timestampVon' | 'timestampBis'
> & {
    timestampVon: string;
    timestampBis: string;
    benutzer: object;
};
