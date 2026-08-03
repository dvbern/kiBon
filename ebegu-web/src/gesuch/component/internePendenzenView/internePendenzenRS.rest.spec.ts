import {
    provideHttpClient,
    withInterceptorsFromDi,
    withXhr
} from '@angular/common/http';
import {TestBed} from '@angular/core/testing';
import {InternePendenzenRS} from './internePendenzenRS.rest';

describe('InternePendenzenRS', () => {
    let service: InternePendenzenRS;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [],
            providers: [provideHttpClient(withXhr(), withInterceptorsFromDi())]
        });
        service = TestBed.inject(InternePendenzenRS);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
