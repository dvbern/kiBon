import {HttpClient} from '@angular/common/http';
import {Injectable, inject} from '@angular/core';
import {CONSTANTS} from '@models/constants';
import {Observable} from 'rxjs';
import {map, shareReplay} from 'rxjs/operators';
import {TSApplicationProperty} from '../../models/einstellung/TSApplicationProperty';
import {TSApplicationPropertyKey} from '../../models/einstellung/TSApplicationPropertyKey';
import {TSPublicAppConfig} from '../../models/einstellung/TSPublicAppConfig';
import {EbeguRestUtil} from '../EbeguRestUtil';

@Injectable({
    providedIn: 'root'
})
export class ApplicationPropertyRsService {
    http = inject(HttpClient);
    ebeguRestUtil = inject(EbeguRestUtil);

    public serviceUrl: string = `${CONSTANTS.REST_API}application-properties`;

    private store = this.http
        .get<TSPublicAppConfig>(`${this.serviceUrl}/public/all`)
        .pipe(shareReplay(1));

    public getAllowedMimetypes(): Observable<string> {
        return this.getPublicPropertiesCached().pipe(
            map((response: {whitelist: any}) => response.whitelist)
        );
    }

    public getPublicPropertiesCached(): Observable<TSPublicAppConfig> {
        return this.store;
    }

    // we keep this method because it is used to perform healthchecks
    public getBackgroundColorFromServer(): Observable<TSApplicationProperty> {
        return this.http
            .get<TSApplicationProperty>(`${this.serviceUrl}/public/background`)
            .pipe(
                map((response: TSApplicationProperty) => {
                    return this.ebeguRestUtil.parseApplicationProperty(
                        new TSApplicationProperty(),
                        response
                    );
                })
            );
    }

    public create(
        name: TSApplicationPropertyKey,
        value: string
    ): Observable<any> {
        return this.http.post(
            `${this.serviceUrl}/${encodeURIComponent(name.toString())}`,
            value,
            {
                headers: {
                    'Content-Type': 'text/plain'
                }
            }
        );
    }

    public update(
        name: TSApplicationPropertyKey,
        value: string
    ): Observable<any> {
        return this.create(name, value);
    }

    public remove(name: string): Observable<any> {
        return this.http.delete(
            `${this.serviceUrl}/${encodeURIComponent(name)}`
        );
    }

    public getAllApplicationProperties(): Observable<TSApplicationProperty[]> {
        return this.http
            .get<TSApplicationProperty[]>(`${this.serviceUrl}/`)
            .pipe(
                map((response: TSApplicationProperty[]) =>
                    this.ebeguRestUtil.parseApplicationProperties(response)
                )
            );
    }

    public isDevMode(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map((response: TSPublicAppConfig) => response.devmode)
        );
    }
    public isDummyMode(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map((response: TSPublicAppConfig) => response.dummyMode)
        );
    }

    public isMultimandantEnabled(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map((response: TSPublicAppConfig) => response.multimandantAktiv)
        );
    }

    public getSentryEnvName(): Observable<string> {
        return this.getPublicPropertiesCached().pipe(
            map((response: TSPublicAppConfig) => response.sentryEnvName)
        );
    }

    public isEbeguKibonAnfrageTestGuiEnabled(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map(
                (response: TSPublicAppConfig) =>
                    response.ebeguKibonAnfrageTestGuiEnabled
            )
        );
    }

    public isZahlungenTestMode(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map((response: TSPublicAppConfig) => response.zahlungentestmode)
        );
    }

    public isPersonensucheDisabledForSystem(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map((response: TSPublicAppConfig) => response.personenSucheDisabled)
        );
    }

    public getKitaxHost(): Observable<string> {
        return this.getPublicPropertiesCached().pipe(
            map((response: TSPublicAppConfig) => response.kitaxHost)
        );
    }

    public getKitaxUrl(): Observable<string> {
        return this.getPublicPropertiesCached().pipe(
            map(
                (response: TSPublicAppConfig) =>
                    response.kitaxHost + response.kitaxEndpoint
            )
        );
    }

    public getActivatedDemoFeatures(): Observable<string> {
        return this.getPublicPropertiesCached().pipe(
            map((response: TSPublicAppConfig) => response.activatedDemoFeatures)
        );
    }

    public getFrenchEnabled(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map((response: TSPublicAppConfig) => response.frenchEnabled)
        );
    }

    public getGeresEnabledForMandant(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map(
                (response: TSPublicAppConfig) => response.geresEnabledForMandant
            )
        );
    }

    public getZusatzinformationenInstitutionEnabled(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map(
                (response: TSPublicAppConfig) =>
                    response.zusatzinformationenInstitution
            )
        );
    }

    public getCheckboxAuszahlungInZukunft(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map(
                (response: TSPublicAppConfig) =>
                    response.checkboxAuszahlungInZukunft
            )
        );
    }

    public getInstitutionenDurchGemeindenEinladen(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map(
                (response: TSPublicAppConfig) =>
                    response.institutionenDurchGemeindenEinladen
            )
        );
    }

    public isTestfaelleEnabled(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map((response: TSPublicAppConfig) => response.testfaelleEnabled)
        );
    }

    public isAngebotTSEnabled(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map((response: TSPublicAppConfig) => response.angebotTSActivated)
        );
    }

    public isFerienbetreuungenabled(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map((response: TSPublicAppConfig) => response.ferienbetreuungAktiv)
        );
    }

    public isAbgeloesteViewBeschaeftigungSingleEnabled(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map(
                (response: TSPublicAppConfig) =>
                    response.abgeloesteViewBeschaeftigungSingleEnabled
            )
        );
    }

    public getBenachrichtigungGemeindeEnabled(): Observable<boolean> {
        return this.getPublicPropertiesCached().pipe(
            map(
                (response: TSPublicAppConfig) =>
                    response.benachrichtigungGemeindeEnabled
            )
        );
    }
}
