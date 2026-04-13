import {AdminModelEinstellungTagesschuleHasAnmeldung} from '../../../models/entity/institution-tagesschule-einstellungen/admin-model-einstellung-tagesschule-has-anmeldung';
import {TSModulTagesschuleGroupHasAnmeldung} from '../../../models/entity/institution-tagesschule-einstellungen/TSModulTagesschuleGroupHasAnmeldung';
import {TSInstitutionStammdaten} from '../../../models/entity/TSInstitutionStammdaten';

export function stammdatenToGroupHasAnmeldung(
    stammdaten: TSInstitutionStammdaten
): AdminModelEinstellungTagesschuleHasAnmeldung[] {
    return stammdaten.institutionStammdatenTagesschule.einstellungenTagesschule.map(
        einstellungTS =>
            Object.assign(
                new AdminModelEinstellungTagesschuleHasAnmeldung(),
                einstellungTS,
                {
                    modulTagesschuleGroups:
                        einstellungTS.modulTagesschuleGroups.map(group =>
                            Object.assign(
                                new TSModulTagesschuleGroupHasAnmeldung(),
                                group
                            )
                        )
                }
            )
    );
}
