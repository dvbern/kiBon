import { Injectable, computed, inject } from '@angular/core';
import { withDevtools } from '@angular-architects/ngrx-toolkit';
import { patchState, signalStore, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap } from 'rxjs';

import { <%= classify(name) %>Service } from '@dv/shared/model/gesuch';
import {
  CachedRemoteData,
  RemoteData,
  cachedPending,
  fromCachedDataSig,
  handleApiResponse,
  initial,
  pending,
} from '@dv/shared/util/remote-data';

type <%= classify(name) %>State = {
  cached<%= classify(name) %>: CachedRemoteData<unknown>;
  <%= camelize(name) %>: RemoteData<unknown>;
};

const initialState: <%= classify(name) %>State = {
  cached<%= classify(name) %>: initial(),
  <%= camelize(name) %>: initial(),
};

@Injectable()
export class <%= classify(name) %>Store extends signalStore(
  { protectedState: false },
  withState(initialState),
  withDevtools('<%= classify(name) %>Store'),
) {
  private <%= camelize(name) %>Service = inject(<%= classify(name) %>Service);

  cached<%= classify(name) %>ListViewSig = computed(() => {
    return fromCachedDataSig(this.cached<%= classify(name) %>);
  });

  <%= camelize(name) %>ViewSig = computed(() => {
    return this.<%= camelize(name) %>.data();
  });

  loadCached<%= classify(name) %>$ = rxMethod<void>(
    pipe(
      tap(() => {
        patchState(this, (state) => ({
          cached<%= classify(name) %>: cachedPending(
            state.cached<%= classify(name) %>,
          ),
        }));
      }),
      switchMap(() =>
        this.<%= camelize(name) %>Service.get<%= classify(name) %>$().pipe(
          handleApiResponse((cached<%= classify(name) %>) =>
            patchState(this, { cached<%= classify(name) %> }),
          ),
        ),
      ),
    ),
  );

  load<%= classify(name) %>$ = rxMethod<void>(
    pipe(
      tap(() => {
        patchState(this, () => ({
          <%= camelize(name) %>: pending(),
        }));
      }),
      switchMap(() =>
        this.<%= camelize(name) %>Service.get<%= classify(name) %>$().pipe(
          handleApiResponse((<%= camelize(name) %>) => patchState(this, { <%= camelize(name) %> })),
        ),
      ),
    ),
  );

  save<%= classify(name) %>$ = rxMethod<{
    <%= camelize(name) %>Id: string;
    values: unknown;
  }>(
    pipe(
      tap(() => {
        patchState(this, (state) => ({
          cached<%= classify(name) %>: cachedPending(
            state.cached<%= classify(name) %>,
          ),
        }));
      }),
      switchMap(({ <%= camelize(name) %>Id, values }) =>
        this.<%= camelize(name) %>Service.update<%= classify(name) %>$({
          <%= camelize(name) %>Id,
          payload: values,
        }).pipe(
          handleApiResponse(
            (<%= camelize(name) %>) => {
              patchState(this, { <%= camelize(name) %> });
            },
            {
              onSuccess: (<%= camelize(name) %>) => {
                // Do something after save, like showing a notification
              },
            },
          ),
        ),
      ),
    ),
  );
}
