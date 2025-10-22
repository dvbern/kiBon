import {eventGroup} from '@ngrx/signals/events';
import {type} from '@ngrx/signals';
import {TSGesuch} from '../../../../../../src/models/TSGesuch';

export const gesuchEvents = eventGroup({
    source: '',
    events: {
        loadGesuch: type<string>(),
        gesuchLoaded: type<TSGesuch>(),
        gesuchLoadingError: type<{error: string}>()
    }
});
