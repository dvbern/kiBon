/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * ...license header...
 */

import {
    HttpClientTestingModule,
    HttpTestingController
} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {TSBetreuungsangebotTyp} from '../../../../models/enums/TSBetreuungsangebotTyp';
import {TSPendenzBetreuung} from '../../../../models/TSPendenzBetreuung';
import {EbeguRestUtil} from '../../../../utils/EbeguRestUtil';
import {PendenzBetreuungenService} from './pendenzenBetreuungen.service';

describe('PendenzBetreuungenRS', () => {
    let service: PendenzBetreuungenService;
    let httpMock: HttpTestingController;
    let mockPendenzBetreuungen: TSPendenzBetreuung;
    let mockPendenzBetreuungenRest: any;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [PendenzBetreuungenService, EbeguRestUtil]
        });

        service = TestBed.inject(PendenzBetreuungenService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify(); // ensures no unexpected HTTP calls
    });

    beforeEach(() => {
        mockPendenzBetreuungen = new TSPendenzBetreuung(
            '123.12.12.12',
            'TestGemeinde',
            '123',
            '123',
            '123',
            'Kind',
            'Kilian',
            undefined,
            'Platzbestaetigung',
            '2024/2025',
            undefined,
            TSBetreuungsangebotTyp.KITA,
            undefined,
            undefined,
            undefined,
            undefined
        );
        mockPendenzBetreuungenRest = {
            betreuungsNummer: '123.12.12.12',
            gemeindeName: 'TestGemeinde',
            betreuungsId: '123',
            gesuchId: '123',
            kindId: '123',
            vorname: 'Kilian',
            name: 'Kind',
            typ: 'Platzbestaetigung',
            gesuchsperiodeString: '2024/2025',
            betreuungsangebotTyp: 'KITA'
        };
    });

    describe('API Usage', () => {
        describe('getPendenzenBetreuungenList', () => {
            it('should return all pending Betreuungen', () => {
                const arrayResult = [mockPendenzBetreuungenRest];
                let foundPendenzen: Array<TSPendenzBetreuung> | undefined;

                service.getPendenzenBetreuungenList().subscribe(result => {
                    foundPendenzen = result;
                });

                const req = httpMock.expectOne(service.serviceURL);
                expect(req.request.method).toBe('GET');
                req.flush(arrayResult);

                expect(foundPendenzen).toBeDefined();
                expect(foundPendenzen!.length).toBe(1);
                expect(foundPendenzen![0]).toEqual(mockPendenzBetreuungen);
            });
        });
    });
});
