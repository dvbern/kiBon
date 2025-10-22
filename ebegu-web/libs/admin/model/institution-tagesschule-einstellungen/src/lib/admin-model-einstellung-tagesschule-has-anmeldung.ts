import {TSEinstellungenTagesschule} from '@kibon/shared/model/entity';
import {TSModulTagesschuleGroupHasAnmeldung} from './TSModulTagesschuleGroupHasAnmeldung';

export class AdminModelEinstellungTagesschuleHasAnmeldung extends TSEinstellungenTagesschule {
    modulTagesschuleGroups: TSModulTagesschuleGroupHasAnmeldung[];
}
