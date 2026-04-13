import {patchState, signalStore, withMethods, withState} from '@ngrx/signals';
import {Events, on, withEventHandlers, withReducer} from '@ngrx/signals/events';
import {TSGesuch} from '../../../models/TSGesuch';
import {gesuchEvents} from './gesuch-events';
import {GesuchRS} from '../../../gesuch/service/gesuchRS.rest';
import {inject} from '@angular/core';
import {from, switchMap} from 'rxjs';
import {mapResponse} from '@ngrx/operators';

type GesuchState = {
    gesuch: TSGesuch | null;
    isLoading: boolean;
    _id: string | null;
};

const initialState: GesuchState = {
    gesuch: null,
    isLoading: false,
    _id: null
} satisfies GesuchState;

export const GesuchStore = signalStore(
    {providedIn: 'root'},
    withState(initialState),
    withMethods(store => ({
        updateGesuch(gesuch: TSGesuch): void {
            patchState(store, state => ({...state, gesuch}));
        },
        updateLoading(isLoading: boolean): void {
            patchState(store, state => ({...state, isLoading}));
        }
    })),
    withReducer(
        on(gesuchEvents.gesuchLoaded, (event, state) => ({
            ...state,
            isLoading: false,
            gesuch: event.payload
        })),
        on(gesuchEvents.loadGesuch, (event, state) => ({
            ...state,
            isLoading: true,
            gesuch: null as TSGesuch | null,
            _id: event.payload
        }))
    ),
    withEventHandlers(
        (store, events = inject(Events), gesuchService = inject(GesuchRS)) => ({
            loadGesuchById$: events.on(gesuchEvents.loadGesuch).pipe(
                switchMap(() =>
                    from(gesuchService.findGesuch(store._id())).pipe(
                        mapResponse({
                            next: gesuch => gesuchEvents.gesuchLoaded(gesuch),
                            error: () =>
                                gesuchEvents.gesuchLoadingError({
                                    error:
                                        'Gesuch Loading failed for gesuch: ' +
                                        store._id()
                                })
                        })
                    )
                )
            )
        })
    )
);
