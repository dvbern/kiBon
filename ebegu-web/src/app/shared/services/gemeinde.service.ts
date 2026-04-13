import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {shareReplay} from 'rxjs/operators';
import {TSGemeinde} from '../../../models/entity/TSGemeinde';

@Injectable({
    providedIn: 'root'
})
export class GemeindeService {
    private readonly _gemeinde$ = new BehaviorSubject<TSGemeinde | null>(null);

    public setCurrentActiveGemeinde(gemeinde: TSGemeinde | null): void {
        this._gemeinde$.next(gemeinde);
    }

    public get gemeinde$(): Observable<TSGemeinde | null> {
        return this._gemeinde$.pipe(shareReplay(1));
    }
}
