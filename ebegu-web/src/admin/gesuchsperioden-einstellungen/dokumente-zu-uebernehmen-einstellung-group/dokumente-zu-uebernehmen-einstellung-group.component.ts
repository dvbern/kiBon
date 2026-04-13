import {Component, input, linkedSignal, output} from '@angular/core';
import {TSDokumentGrundTyp} from '../../../models/enums/TSDokumentGrundTyp';
import {TSDokumentTyp} from '../../../models/enums/TSDokumentTyp';
import {TSEinstellung} from '../../einstellungen/TSEinstellung';
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
    styleUrl: './dokumente-zu-uebernehmen-einstellung-group.component.css'
})
export class DokumenteZuUebernehmenEinstellungGroupComponent {
    einstellung = input.required<TSEinstellung>();
    einstellungChange = output<TSEinstellung>();

    groups = Object.keys(
        GS_DOKUMENT_ZU_UEBERNEHMEN_GROUPS
    ) as TSDokumentGrundTyp[];
    readonly GS_DOKUMENT_ZU_UEBERNEHMEN_GROUPS =
        GS_DOKUMENT_ZU_UEBERNEHMEN_GROUPS;

    potentiallyDokumentZuUebernehmenTyps = (
        Object.entries(GS_DOKUMENT_ZU_UEBERNEHMEN_GROUPS) as [
            TSDokumentGrundTyp,
            TSDokumentTyp[]
        ][]
    )
        .map(([, types]) => types)
        .flat();

    einstellungModel = linkedSignal(() => {
        return Object.fromEntries(
            this.potentiallyDokumentZuUebernehmenTyps.map(key => [
                key,
                this.einstellung().value.includes(key)
            ])
        ) as Record<TSDokumentTyp, boolean>;
    });

    emitSelection() {
        const keysZuUebernehmen = (
            Object.keys(this.einstellungModel()) as TSDokumentTyp[]
        )
            .filter(einstellungKey => this.einstellungModel()[einstellungKey])
            .flat();
        const einstellung = this.einstellung();
        einstellung.value = keysZuUebernehmen.join(',');
        this.einstellungChange.emit(einstellung);
    }
}
