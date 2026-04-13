import {HttpClient} from '@angular/common/http';
import {Injectable, inject} from '@angular/core';
import {MANDANTS} from '@models/mandant';
import {
    MeldungsfensterData,
    MeldungsfensterRestDTO,
    MeldungsfensterStatus
} from '@models/meldungsfenster';
import {CONSTANTS} from '@models/constants';
import {map, Observable} from 'rxjs';
import {filter, mergeMap} from 'rxjs/operators';
import {TSRole} from '../../models/enums/TSRole';
import {DateUtil} from '../date/DateUtil';
import {MandantService} from '../mandant-service/mandant.service';

@Injectable({
    providedIn: 'root'
})
export class MeldungsfensterService {
    private readonly http = inject(HttpClient);
    private readonly mandantService = inject(MandantService);

    private serviceURL = `${CONSTANTS.REST_API}meldungsfenster`;

    public getAllMeldungsfensters() {
        const url = `${this.serviceURL}/all`;
        return this.http
            .get<MeldungsfensterRestDTO[]>(url)
            .pipe(map(dtoList => dtoList.map(dto => this.dtoToModel(dto))));
    }

    public createNewMeldungsfenster(meldungsfensterData: MeldungsfensterData) {
        return this.http.post<MeldungsfensterData>(
            `${this.serviceURL}`,
            this.toDTO(meldungsfensterData)
        );
    }

    private toDTO(meldungsfensterData: MeldungsfensterData) {
        return {
            ...meldungsfensterData,
            gueltigAb: DateUtil.toLocalDateFormat(
                meldungsfensterData.gueltigAb
            ),
            gueltigBis: DateUtil.toLocalDateFormat(
                meldungsfensterData.gueltigBis
            )
        };
    }

    public deleteMeldungsfenster(id: string) {
        return this.http.delete(`${this.serviceURL}/${id}`);
    }

    public updateMeldungsfenster(meldungsfensterData: MeldungsfensterData) {
        return this.http.put(`${this.serviceURL}`, {
            ...meldungsfensterData,
            gueltigAb: DateUtil.toLocalDateFormat(
                meldungsfensterData.gueltigAb
            ),
            gueltigBis: DateUtil.toLocalDateFormat(
                meldungsfensterData.gueltigBis
            )
        });
    }

    getMeldungsfenster(id: string): Observable<MeldungsfensterData> {
        return this.http
            .get<MeldungsfensterRestDTO>(`${this.serviceURL}/${id}`)
            .pipe(map(dto => this.dtoToModel(dto)));
    }

    getAllPublicMeldungsfenster() {
        const url = `${this.serviceURL}/public/aktive`;
        return this.mandantService.mandant$.pipe(
            filter(mandant => mandant !== MANDANTS.NONE),
            mergeMap(() =>
                this.http
                    .get<MeldungsfensterRestDTO[]>(url)
                    .pipe(
                        map(dtoList => dtoList.map(dto => this.dtoToModel(dto)))
                    )
            )
        );
    }

    private dtoToModel(dto: MeldungsfensterRestDTO): MeldungsfensterData {
        return {
            ...dto,
            gueltigAb: new Date(dto.gueltigAb),
            gueltigBis: new Date(dto.gueltigBis),
            status: dto.status as MeldungsfensterStatus,
            zielgruppe: dto.zielgruppe as TSRole[]
        };
    }
}
