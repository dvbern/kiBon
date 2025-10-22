import {TSBenutzer} from '../../../../models/TSBenutzer';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {TSLastenausgleichTagesschulenStatusHistory} from '../../../../models/gemeindeantrag/TSLastenausgleichTagesschulenStatusHistory';

export abstract class LATSPermissionUtil {
    static isInZweitpruefungAndSameUser(
        principal: TSBenutzer,
        container: TSLastenausgleichTagesschuleAngabenGemeindeContainer,
        history: TSLastenausgleichTagesschulenStatusHistory[]
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
