import {Component, input, linkedSignal, output} from '@angular/core';
import {TSEinstellung} from '../../../../../../../src/admin/einstellungen/TSEinstellung';
import {
    MatAccordion,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle
} from '@angular/material/expansion';
import {ERNEUERBARE_GS_DOKUMENT_GROUPS} from './DokumentTypGroups';
import {TSDokumentGrundTyp} from '@kibon/shared/model/enums';
import {TSDokumentTyp} from '../../../../../../../src/models/enums/TSDokumentTyp';
import {SharedModule} from '../../../../../../../src/app/shared/shared.module';

@Component({
    selector: 'lib-erneuerbare-dokumente-einstellung-group',
    imports: [
        MatExpansionPanel,
        MatAccordion,
        MatExpansionPanelHeader,
        MatExpansionPanelTitle,
        SharedModule
    ],
    templateUrl: './erneuerbare-dokumente-einstellung-group.component.html',
    styleUrl: './erneuerbare-dokumente-einstellung-group.component.css'
})
export class ErneuerbareDokumenteEinstellungGroupComponent {
    einstellung = input.required<TSEinstellung>();
    einstellungChange = output<TSEinstellung>();

    groups = Object.keys(
        ERNEUERBARE_GS_DOKUMENT_GROUPS
    ) as TSDokumentGrundTyp[];
    readonly ERNEUERBARE_GS_DOKUMENT_GROUPS = ERNEUERBARE_GS_DOKUMENT_GROUPS;

    potentiallyErneuerbareDokumentTyps = (
        Object.entries(ERNEUERBARE_GS_DOKUMENT_GROUPS) as [
            TSDokumentGrundTyp,
            TSDokumentTyp[]
        ][]
    )
        .map(([, types]) => types)
        .flat();

    einstellungModel = linkedSignal(() => {
        return Object.fromEntries(
            this.potentiallyErneuerbareDokumentTyps.map(key => [
                key,
                this.einstellung().value.includes(key)
            ])
        ) as Record<TSDokumentTyp, boolean>;
    });

    emitSelection() {
        const erneuerbareKeys = (
            Object.keys(this.einstellungModel()) as TSDokumentTyp[]
        )
            .filter(einstellungKey => this.einstellungModel()[einstellungKey])
            .flat();
        const einstellung = this.einstellung();
        einstellung.value = erneuerbareKeys.join(',');
        this.einstellungChange.emit(einstellung);
    }
}
