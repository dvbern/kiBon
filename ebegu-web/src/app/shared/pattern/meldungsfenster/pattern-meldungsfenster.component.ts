import {CommonModule} from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    computed,
    inject,
    signal
} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {tsBrowserLanguageFromString} from '../../../../models/enums/TSBrowserLanguage';
import {
    browserLanguageToSprache,
    TSSprache
} from '../../../../models/enums/TSSprache';
import {
    ClosedMeldungsfenster,
    MeldungsfensterData,
    MeldungsfensterStatus
} from '../../../../models/meldungsfenster';
import {MeldungsfensterService} from '../../../../utils/meldungsfenster/meldungsfenster.service';
import {SharedModule} from '../../shared.module';
import {MeldungsfensterLocalStorageUtil} from './utils/MeldungsfensterLocalStorageUtil';
import {HashCalculator} from './utils/HashCalculator';
import {UiMeldungsfensterComponent} from '@app/shared/ui-meldungsfenster';
import {map, Observable, startWith} from 'rxjs';

@Component({
    selector: 'lib-shared-pattern-meldungsfenster',
    imports: [
        CommonModule,
        SharedModule,
        TranslateModule,
        UiMeldungsfensterComponent,
        UiMeldungsfensterComponent
    ],
    templateUrl: './pattern-meldungsfenster.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PatternMeldungsfensterComponent {
    showWarning: boolean = true;
    showInfo: boolean = true;
    showImportant: boolean = true;

    private meldungsfensterService = inject(MeldungsfensterService);
    protected translationService = inject(TranslateService);

    meldungsfensterResourceRef = rxResource({
        stream: () => this.meldungsfensterService.getAllPublicMeldungsfenster()
    });

    meldungenToDisplay = computed(() => {
        const loaded = this.meldungsfensterResourceRef.value();
        const closedMeldungsfenster = this.closedMeldungsfensterHashMap();
        if (loaded) {
            return loaded.filter(meldung =>
                this.shouldDisplay(meldung, closedMeldungsfenster)
            );
        }
        return null;
    });

    meldungenCount = computed(() => {
        const meldungen = this.meldungenToDisplay();
        return meldungen ? meldungen.length : 0;
    });

    closedMeldungsfensterHashMap = signal<Map<string, ClosedMeldungsfenster>>(
        MeldungsfensterLocalStorageUtil.expireMeldungsfensterAndGet()
    );

    // Computed property to determine which meldung should be displayed based on priority
    highestPriorityMeldung = computed(() => {
        const meldungen = this.meldungenToDisplay();

        if (meldungen) {
            return this.getNextMeldung(meldungen);
        }
        return null;
    });

    private getNextMeldung(
        meldung: MeldungsfensterData[]
    ): MeldungsfensterData | null {
        // Get the next valid message based on localStorage and visibility flags, considering priority
        // Prioritize meldung by WARNUNG > WICHTIG > INFO, but only show those not closed
        for (const status of [
            MeldungsfensterStatus.WARNUNG,
            MeldungsfensterStatus.WICHTIG,
            MeldungsfensterStatus.INFO
        ]) {
            const meldungItem = meldung.find(item => item.status === status);
            if (meldungItem) {
                return meldungItem;
            }
        }
        return null;
    }

    private shouldDisplay(
        meldung: MeldungsfensterData,
        localstorageMapSignal: Map<string, ClosedMeldungsfenster>
    ): boolean {
        const hash = HashCalculator.getHash(meldung);
        return !localstorageMapSignal.has(hash);
    }

    public closeMeldungsfenster(meldung: MeldungsfensterData | null) {
        if (meldung) {
            const closedItem: ClosedMeldungsfenster = {
                closeTime: new Date().getTime(),
                bisDatum: meldung.gueltigBis,
                status: meldung.status
            };
            const updated = this.closedMeldungsfensterHashMap().set(
                HashCalculator.getHash(meldung),
                closedItem
            );
            MeldungsfensterLocalStorageUtil.updateLocalStorage(updated);
            this.closedMeldungsfensterHashMap.set(new Map(updated));
        }
    }

    getLanguage(): Observable<TSSprache> {
        return this.translationService.onLangChange.pipe(
            startWith({lang: this.translationService.currentLang}),
            map(lang =>
                browserLanguageToSprache(tsBrowserLanguageFromString(lang.lang))
            )
        );
    }

    protected readonly MeldungsfensterStatus = MeldungsfensterStatus;
}
