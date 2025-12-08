import {isAnyStatusOfFreigegebenGeprueftVerfuegenVerfuegtOrAbgeschlossen} from '../../../models/enums/TSAntragStatus';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';

export abstract class DokumenteUtil {
    static isDokumenteUploadDisabled(
        gesuchModelManager: GesuchModelManager,
        authServiceRS: AuthServiceRS
    ): boolean {
        // Dokument-Upload ist eigentlich in jedem Status möglich, aber nicht für alle Rollen. Also nicht
        // gleichbedeutend mit readonly auf dem Gesuch
        // Jedoch darf der Gesuchsteller und der Unterstützungsdienst nach der Verfuegung und
        // in Bearbeitung Gemeinde/JA nichts mehr hochladen
        const gsAndVerfuegt =
            gesuchModelManager.getGesuch() &&
            isAnyStatusOfFreigegebenGeprueftVerfuegenVerfuegtOrAbgeschlossen(
                gesuchModelManager.getGesuch().status
            ) &&
            authServiceRS.isOneOfRoles(
                TSRoleUtil.getGesuchstellerSozialdienstRolle()
            );
        return (
            gsAndVerfuegt ||
            authServiceRS.isOneOfRoles(TSRoleUtil.getReadOnlyRoles())
        );
    }
}
