import {TestBed} from '@angular/core/testing';

import {GemeindeService} from './gemeinde.service';

describe('GemeindeService', () => {
    let service: GemeindeService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(GemeindeService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
