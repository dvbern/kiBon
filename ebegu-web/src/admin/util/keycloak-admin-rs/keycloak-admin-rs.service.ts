import {HttpClient} from '@angular/common/http';
import {Injectable, inject} from '@angular/core';
import {CONSTANTS} from '@models/constants';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class KeycloakAdminRsService {
    http = inject(HttpClient);

    public serviceUrl: string = `${CONSTANTS.REST_API}admin/keycloak`;

    public mitarbeiterRechteErstellen(): Observable<any> {
        return this.http.get(`${this.serviceUrl}/accessrechte/erstellen`, {
            responseType: 'text'
        });
    }
}
