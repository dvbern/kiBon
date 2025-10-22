import {TSBenutzer} from '../../../../models/TSBenutzer';
import {TSFerienbetreuungAngabenContainer} from '../../../../models/gemeindeantrag/TSFerienbetreuungAngabenContainer';
import {FerienbetreuungStatusHistory} from '../../../../models/gemeindeantrag/ferienbetreuung/dto/FerienbetreuungStatusHistory';

export abstract class FerienbetreuungPermissionUtil {
    static isInZweitpruefungAndSameUser(
        principal: TSBenutzer,
        container: TSFerienbetreuungAngabenContainer,
        history: FerienbetreuungStatusHistory[]
    ): boolean {
        if (!container.isInZweitPruefung()) {
            return false;
        }
        const sortedHistory = history.filter(h => h.status === 'ZWEITPRUEFUNG');

        sortedHistory.sort((a, b) =>
            a.timestampVon < b.timestampVon ? 1 : -1
        );

        return sortedHistory[0].benutzer.username === principal.username;
    }
}
