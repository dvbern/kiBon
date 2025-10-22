import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {CONSTANTS} from '@kibon/shared/model/constants';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AdminUtilKeycloakAdminRsService {
    public serviceUrl: string = `${CONSTANTS.REST_API}admin/keycloak`;

    public constructor(public http: HttpClient) {}

    public mitarbeiterRechteErstellen(): Observable<any> {
        return this.http.get(`${this.serviceUrl}/accessrechte/erstellen`, {
            responseType: 'text'
        });
    }

    /**
     * Removes the Keycloak realm role MITARBEITER_ACCESS from the given user.
     *
     * @param externalUuid The external UUID of the user to remove the role for. This is the ID the user is
     *     identified by in Keycloak.
     * @return Reference to the HTTP response containing the success or error message as plain text.
     */
    public deleteMitarbeiterAccessRole(externalUuid: string): Observable<any> {
        return this.http.delete(`${this.serviceUrl}/accessrechte/loeschen`, {
            responseType: 'text',
            observe: 'response',
            params: {
                externalUuid: externalUuid
            }
        });
    }

    /**
     * Adds the Keycloak realm role MITARBEITER_ACCESS to the given user.
     *
     * @param externalUuid The external UUID of the user to add the role to. This is the ID the user is
     *      identified by in Keycloak.
     */
    public addMitarbeiterAccessRole(externalUuid: string): Observable<any> {
        return this.http.put(
            `${this.serviceUrl}/accessrechte/hinzufuegen?externalUuid=${externalUuid}`,
            {
                responseType: 'text',
                observe: 'response'
            }
        );
    }
}
