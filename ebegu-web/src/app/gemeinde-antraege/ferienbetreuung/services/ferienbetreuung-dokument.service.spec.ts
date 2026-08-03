import {
    provideHttpClient,
    withInterceptorsFromDi,
    withXhr
} from '@angular/common/http';
import {TestBed} from '@angular/core/testing';

import {FerienbetreuungDokumentService} from './ferienbetreuung-dokument.service';

describe('FerienbetreuungDokumentService', () => {
    let service: FerienbetreuungDokumentService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [],
            providers: [provideHttpClient(withXhr(), withInterceptorsFromDi())]
        });
        service = TestBed.inject(FerienbetreuungDokumentService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
