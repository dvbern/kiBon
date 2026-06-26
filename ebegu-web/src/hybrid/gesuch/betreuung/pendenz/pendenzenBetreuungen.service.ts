import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {CONSTANTS} from '@models/constants';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSPendenzBetreuung} from '../../../../models/TSPendenzBetreuung';
import {EbeguRestUtil} from '../../../../utils/EbeguRestUtil';

@Injectable({providedIn: 'root'})
export class PendenzBetreuungenService {
    public readonly serviceURL = `${CONSTANTS.REST_API}search/pendenzenBetreuungen`;
    private readonly ebeguRestUtil = inject(EbeguRestUtil);
    private readonly http = inject(HttpClient);

    public getPendenzenBetreuungenList(): Observable<
        Array<TSPendenzBetreuung>
    > {
        return this.http
            .get<any>(this.serviceURL)
            .pipe(
                map(response =>
                    this.ebeguRestUtil.parsePendenzBetreuungenList(response)
                )
            );
    }
}
