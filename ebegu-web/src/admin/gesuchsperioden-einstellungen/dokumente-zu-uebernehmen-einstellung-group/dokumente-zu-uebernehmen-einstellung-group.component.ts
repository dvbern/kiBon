import {KibonConfiguration} from '@admin/einstellungen';
import {
    Component,
    input,
    linkedSignal,
    output,
    ChangeDetectionStrategy,
    computed,
    inject
} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {TSDokumentGrundTyp} from '../../../models/enums/TSDokumentGrundTyp';
import {TSDokumentTyp} from '../../../models/enums/TSDokumentTyp';
import {
    MatAccordion,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle
} from '@angular/material/expansion';
import {GS_DOKUMENT_ZU_UEBERNEHMEN_GROUPS} from './DokumentTypGroups';
import {SharedModule} from '../../../app/shared/shared.module';

@Component({
    selector: 'lib-dokumente-zu-uebernehmen-einstellung-group',
    imports: [
        MatExpansionPanel,
        MatAccordion,
        MatExpansionPanelHeader,
        MatExpansionPanelTitle,
        SharedModule
    ],
    templateUrl: './dokumente-zu-uebernehmen-einstellung-group.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './dokumente-zu-uebernehmen-einstellung-group.component.css'
})
export class DokumenteZuUebernehmenEinstellungGroupComponent {
    einstellung = input.required<KibonConfiguration>();
    filter = input.required<string>();
    einstellungChange = output<KibonConfiguration>();

    private readonly translate = inject(TranslateService);

    groups = Object.keys(
        GS_DOKUMENT_ZU_UEBERNEHMEN_GROUPS
    ) as TSDokumentGrundTyp[];

    private readonly allDokumentTypes = Object.values(
        GS_DOKUMENT_ZU_UEBERNEHMEN_GROUPS
    ).flat() as TSDokumentTyp[];

    filteredGroups = computed(() => {
        const filter = this.filter();
        const grouped = new Map<TSDokumentGrundTyp, TSDokumentTyp[]>();
        const dokumentGrundGroups: TSDokumentGrundTyp[] = Object.keys(
            GS_DOKUMENT_ZU_UEBERNEHMEN_GROUPS
        ) as TSDokumentGrundTyp[];

        dokumentGrundGroups.forEach(dgt => {
            const keysOfDokumentGrundTyp = Object.values(
                GS_DOKUMENT_ZU_UEBERNEHMEN_GROUPS[dgt]
            ) as TSDokumentTyp[];

            keysOfDokumentGrundTyp.forEach(key => {
                if (this.doesKeyMatchFilter(key, filter)) {
                    const list = grouped.get(dgt) ?? [];
                    list.push(key);
                    grouped.set(dgt, list);
                }
            });
        });
        return grouped;
    });

    private doesKeyMatchFilter(key: TSDokumentTyp, filter: string): any {
        return (
            key.toLocaleLowerCase().includes(filter) ||
            this.translate.instant(key).toLocaleLowerCase().includes(filter)
        );
    }

    einstellungModel = linkedSignal(() => {
        const selected = this.einstellung().value.split(',');
        return Object.fromEntries(
            this.allDokumentTypes.map(key => [key, selected.includes(key)])
        ) as Record<TSDokumentTyp, boolean>;
    });

    emitSelection() {
        const keysZuUebernehmen = (
            Object.keys(this.einstellungModel()) as TSDokumentTyp[]
        ).filter(einstellungKey => this.einstellungModel()[einstellungKey]);
        const einstellung = this.einstellung();
        einstellung.value = keysZuUebernehmen.join(',');
        this.einstellungChange.emit(einstellung);
    }
}
