import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {BetreuungRS} from '@hybrid/gesuch/betreuung';
import {CONSTANTS} from '@models/constants';
import {Observable} from 'rxjs';
import {mergeMap} from 'rxjs/operators';
import {TSBetreuung} from '../../../../../models/TSBetreuung';
import {EbeguRestUtil} from '../../../../../utils/EbeguRestUtil';

@Injectable({
    providedIn: 'root'
})
export class AnmeldungRestService {
    private serviceURL = `${CONSTANTS.REST_API}anmeldung`;
    private ebeguRestUtil = new EbeguRestUtil();

    private http = inject(HttpClient);
    private betreuungService = inject(BetreuungRS);

    public anmeldungSchulamtAblehnen(
        betreuung: TSBetreuung,
        gesuchId: string
    ): Observable<TSBetreuung> {
        const restBetreuung = this.ebeguRestUtil.betreuungToRestObject(
            {},
            betreuung
        );
        return this.http
            .put(`${this.serviceURL}/ablehnen/`, restBetreuung)
            .pipe(
                mergeMap(response =>
                    this.betreuungService.parseBetreuungAndUpdateWizardStep(
                        {data: response},
                        gesuchId
                    )
                )
            );
    }

    public anmeldungSchulamtFalscheInstitution(
        betreuung: TSBetreuung,
        gesuchId: string
    ): Observable<TSBetreuung> {
        const restBetreuung = this.ebeguRestUtil.betreuungToRestObject(
            {},
            betreuung
        );
        const url = `${this.serviceURL}/falscheInstitution/`;
        return this.http
            .put(url, restBetreuung)
            .pipe(
                mergeMap(response =>
                    this.betreuungService.parseBetreuungAndUpdateWizardStep(
                        {data: response},
                        gesuchId
                    )
                )
            );
    }

    public anmeldungSchulamtStorniert(
        betreuung: TSBetreuung,
        gesuchId: string
    ): Observable<TSBetreuung> {
        const restBetreuung = this.ebeguRestUtil.betreuungToRestObject(
            {},
            betreuung
        );
        const url = `${this.serviceURL}/stornieren/`;
        return this.http
            .put(url, restBetreuung)
            .pipe(
                mergeMap(response =>
                    this.betreuungService.parseBetreuungAndUpdateWizardStep(
                        {data: response},
                        gesuchId
                    )
                )
            );
    }

    public anmeldungSchulamtModuleAkzeptiert(
        betreuung: TSBetreuung,
        gesuchId: string
    ): Observable<TSBetreuung> {
        const restBetreuung = this.ebeguRestUtil.betreuungToRestObject(
            {},
            betreuung
        );
        return this.http
            .put(`${this.serviceURL}/akzeptieren`, restBetreuung)
            .pipe(
                mergeMap(response =>
                    this.betreuungService.parseBetreuungAndUpdateWizardStep(
                        {data: response},
                        gesuchId
                    )
                )
            );
    }
}
