import {
    AdminModelEinstellungTagesschuleHasAnmeldung,
    TSModulTagesschuleGroupHasAnmeldung
} from '@kibon/admin/model/institution-tagesschule-einstellungen';
import {TSInstitutionStammdaten} from '@kibon/shared/model/entity';

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
