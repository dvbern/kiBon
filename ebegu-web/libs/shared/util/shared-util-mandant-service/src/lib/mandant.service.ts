import {HttpClient} from '@angular/common/http';
import {Injectable, inject} from '@angular/core';
import {CookieService} from 'ngx-cookie-service';
import {firstValueFrom, Observable, ReplaySubject} from 'rxjs';
import {map} from 'rxjs/operators';
import {TSMandant} from '@kibon/shared/model/entity';
import {CONSTANTS} from '@kibon/shared/model/constants';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {EbeguRestUtil} from '../../../../../../src/utils/EbeguRestUtil';
import {
    MANDANTS,
    KiBonMandant,
    MandantLogoNameVisitor
} from '@kibon/shared-model-mandant';
import {WindowRef} from '@kibon/shared-util-window-ref';
import {EbeguUtil} from '../../../../../../src/utils/EbeguUtil';

const LOG = LogFactory.createLog('MandantService');

@Injectable({
    providedIn: 'root'
})
export class MandantService {
    private readonly windowRef = inject(WindowRef);
    private readonly http = inject(HttpClient);
    private readonly cookieService = inject(CookieService);

    private readonly _mandant$: ReplaySubject<KiBonMandant> =
        new ReplaySubject<KiBonMandant>(1);

    private readonly _multimandantActive$: ReplaySubject<boolean> =
        new ReplaySubject<boolean>();

    private readonly restUtil = new EbeguRestUtil();

    public get mandant$(): Observable<KiBonMandant> {
        return this._mandant$.asObservable();
    }

    private static hostnameToMandant(hostname: string): KiBonMandant {
        switch (hostname.toLocaleLowerCase()) {
            case MANDANTS.BERN.hostname:
                return MANDANTS.BERN;
            case MANDANTS.LUZERN.hostname:
                return MANDANTS.LUZERN;
            case MANDANTS.SOLOTHURN.hostname:
                return MANDANTS.SOLOTHURN;
            case MANDANTS.APPENZELL_AUSSERRHODEN.hostname:
                return MANDANTS.APPENZELL_AUSSERRHODEN;
            case MANDANTS.SCHWYZ.hostname:
                return MANDANTS.SCHWYZ;
            case MANDANTS.ZUG.hostname:
                return MANDANTS.ZUG;
            case MANDANTS.DVB.hostname:
                return MANDANTS.DVB;
            default:
                return MANDANTS.NONE;
        }
    }

    private static cookieToMandant(cookieMandant: string): KiBonMandant {
        switch (cookieMandant) {
            case MANDANTS.BERN.fullName:
                return MANDANTS.BERN;
            case MANDANTS.SOLOTHURN.fullName:
                return MANDANTS.SOLOTHURN;
            case MANDANTS.LUZERN.fullName:
                return MANDANTS.LUZERN;
            case MANDANTS.APPENZELL_AUSSERRHODEN.fullName:
                return MANDANTS.APPENZELL_AUSSERRHODEN;
            case MANDANTS.SCHWYZ.fullName:
                return MANDANTS.SCHWYZ;
            case MANDANTS.ZUG.fullName:
                return MANDANTS.ZUG;
            case MANDANTS.DVB.fullName:
                return MANDANTS.DVB;
            default:
                return MANDANTS.NONE;
        }
    }

    private static decodeMandantCookie(encodedMandant: string): string {
        return encodedMandant.replace('+', ' ');
    }

    private static isLegacyBeCookie(decodeMandantCookie: string): boolean {
        return decodeMandantCookie === 'be';
    }

    public async initMandantCookies(): Promise<void> {
        await this.initMandantCookie();
        await this.initMultimandantActivated();
        await this.initMandantRedirectCookie();
    }

    private async initMandantCookie(): Promise<void> {
        if (MandantService.isLegacyBeCookie(this.getDecodeMandantCookie())) {
            await this.setMandantCookie(MANDANTS.BERN);
        }
        const mandantFromCookie = MandantService.cookieToMandant(
            this.getDecodeMandantCookie()
        );
        const mandantFromUrl = this.parseHostnameForMandant();

        if (
            mandantFromCookie !== mandantFromUrl &&
            mandantFromUrl !== MANDANTS.NONE
        ) {
            await this.setMandantCookie(mandantFromUrl);
            this._mandant$.next(mandantFromUrl);
        } else {
            this._mandant$.next(mandantFromCookie);
        }
    }

    public async initMandantRedirectCookie(): Promise<void> {
        const mandantFromCookie = MandantService.cookieToMandant(
            this.getDecodedMandantRedirectCookie()
        );
        const mandantFromUrl = this.parseHostnameForMandant();
        if (
            mandantFromUrl === MANDANTS.NONE &&
            this.isMandantTransitionForLuzern()
        ) {
            // Ausnahme fuer stadtluzern
            await this.setMandantRedirectCookie(MANDANTS.LUZERN);
        }

        if (
            mandantFromCookie !== mandantFromUrl &&
            mandantFromUrl !== MANDANTS.NONE
        ) {
            await this.setMandantRedirectCookie(mandantFromUrl);
        }
    }

    private getDecodeMandantCookie(): string {
        return MandantService.decodeMandantCookie(
            this.cookieService.get('mandant')
        );
    }

    private getDecodedMandantRedirectCookie(): string {
        return MandantService.decodeMandantCookie(
            this.cookieService.get('mandantRedirect')
        );
    }

    private initMultimandantActivated(): Promise<void> {
        return firstValueFrom(
            this.http.get(
                `${CONSTANTS.REST_API}application-properties/public/all`
            )
        ).then(
            res => {
                const props = this.restUtil.parsePublicAppConfig(res);
                this._multimandantActive$.next(props.multimandantAktiv);
                // overwrite cookie if not active
                if (!props.multimandantAktiv) {
                    this._mandant$.next(MANDANTS.NONE);
                }
            },
            err => LOG.error(err)
        );
    }

    public isMultimandantActive$(): Observable<boolean> {
        return this._multimandantActive$.asObservable();
    }

    public parseHostnameForMandant(): KiBonMandant {
        const regex = /(be|so|ar|lu|sz|zg|dv)(?=.(dvbern|kibon))/g;
        const matches = regex.exec(
            this.windowRef.nativeWindow.location.hostname
        );
        if (matches === null) {
            return MANDANTS.NONE;
        }
        return MandantService.hostnameToMandant(matches[0]);
    }

    public selectMandant(mandant: KiBonMandant, url: string): void {
        const parsedMandant = MandantService.hostnameToMandant(
            mandant.hostname
        );

        this.redirectToMandantSubdomain(parsedMandant, url);
    }

    public setMandantCookie(mandant: KiBonMandant): Promise<any> {
        // TODO: Restore AuthService once migrated
        return firstValueFrom(
            this.http.post(`${CONSTANTS.REST_API}auth/set-mandant`, {
                name: mandant.fullName
            })
        );
    }

    public setMandantRedirectCookie(mandant: KiBonMandant): Promise<any> {
        // TODO: Restore AuthService once migrated
        return firstValueFrom(
            this.http.post(`${CONSTANTS.REST_API}auth/set-mandant-redirect`, {
                name: mandant.fullName
            })
        );
    }

    public redirectToMandantSubdomain(
        mandant: KiBonMandant,
        url: string
    ): void {
        const host = this.removeMandantEnvironmentFromCompleteHost();
        const environment = this.getEnvironmentFromCompleteHost();
        const environmentWithMandant =
            environment.length > 0
                ? this.getEnvironmentWithMandant(environment, mandant)
                : mandant.hostname;
        this.windowRef.nativeWindow.open(
            `${this.windowRef.nativeWindow.location.protocol}//${environmentWithMandant ? environmentWithMandant + '.' : ''}${host}/${url}`,
            '_self'
        );
    }

    private getEnvironmentWithMandant(
        environment: string,
        mandant: KiBonMandant
    ) {
        if (mandant === MANDANTS.NONE) {
            this.cookieService.set('mandantRedirect', '');
            return environment;
        } else {
            return `${environment}-${mandant.hostname}`;
        }
    }

    public removeMandantEnvironmentFromCompleteHost(): string {
        const splitHost = this.windowRef.nativeWindow.location.host.split('.');

        if (splitHost[0] === 'kibon') {
            return splitHost.join('.');
        }

        return splitHost.slice(1, splitHost.length).join('.');
    }

    public getEnvironmentFromCompleteHost(): string {
        const environmentRegex =
            /(local|preview|e2e|dev|uat|iat|demo|schulung|replica)?(-\w*)?/;
        const matches = this.windowRef.nativeWindow.location.host
            .split('kibon.ch')[0]
            .match(environmentRegex);
        if (EbeguUtil.isNullOrUndefined(matches[1])) {
            return '';
        }
        return matches[1];
    }

    public getMandantRedirect(): KiBonMandant {
        return MandantService.cookieToMandant(
            this.getDecodedMandantRedirectCookie()
        );
    }

    public mandantToKibonMandant(mandant: TSMandant): KiBonMandant {
        switch (mandant.mandantIdentifier) {
            case 'SOLOTHURN':
                return MANDANTS.SOLOTHURN;
            case 'APPENZELL_AUSSERRHODEN':
                return MANDANTS.APPENZELL_AUSSERRHODEN;
            case 'LUZERN':
                return MANDANTS.LUZERN;
            case 'SCHWYZ':
            case 'SCHWYZ_2':
                return MANDANTS.SCHWYZ;
            case 'ZUG':
                return MANDANTS.ZUG;
            case 'DVB':
                return MANDANTS.DVB;
            case 'BERN':
            default:
                return MANDANTS.BERN;
        }
    }

    public getAll(): Observable<TSMandant[]> {
        return this.http
            .get<any[]>(`${CONSTANTS.REST_API}mandanten/all`)
            .pipe(
                map(results =>
                    results.map(restMandant =>
                        this.restUtil.parseMandant(new TSMandant(), restMandant)
                    )
                )
            );
    }

    public getMandantLogoName(mandant: KiBonMandant): string {
        return new MandantLogoNameVisitor().process(mandant);
    }

    public isMandantTransitionForLuzern(): boolean {
        const regexStadtluren = /(stadtluzern)(?=.(dvbern|kibon))/g;
        return regexStadtluren.test(
            this.windowRef.nativeWindow.location.hostname
        );
    }
}
