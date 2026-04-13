import {
    ChangeDetectionStrategy,
    Component,
    inject,
    linkedSignal
} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {CommonModule} from '@angular/common';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {TSDokumentGrundTyp} from '../../../models/enums/TSDokumentGrundTyp';
import {DokumenteRS} from '../../service/dokumenteRS.rest';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {SharedModule} from '../../../app/shared/shared.module';
import {MatDialogRef} from '@angular/material/dialog';
import {
    GrundGroupWithDokumentDecisions,
    GrundWithDokumentDecision
} from './types';
import {TSDokumentGrund} from '../../../models/TSDokumentGrund';
import {GesuchPopupHeadingComponent} from '@gesuch/heading';

@Component({
    selector: 'lib-gesuch-dokumente-uebernehmen-selection',
    imports: [
        CommonModule,
        SharedModule,
        GesuchPopupHeadingComponent,
        MatProgressSpinnerModule
    ],
    templateUrl: './dokumente-uebernehmen-selection.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DokumenteUebernehmenSelectionComponent {
    private readonly dokumenteService = inject(DokumenteRS);
    private readonly ref = inject(MatDialogRef);
    readonly gesuchmodelManager = inject(GesuchModelManager);

    dokumenteZuUebernehmenRes = rxResource({
        params: () => ({gesuchId: this.gesuchmodelManager.getGesuch().id}),
        stream: ({params}) =>
            this.dokumenteService.getDokumentZuUebernehmenTyps(params.gesuchId)
    });

    groupedDokumenteZuUebernehmenTyps = linkedSignal(() => {
        const dokumenteZuUebernehmen = this.dokumenteZuUebernehmenRes.value();
        if (!dokumenteZuUebernehmen) {
            return undefined;
        }
        const uniqueGrundTypes = [
            ...new Set(
                dokumenteZuUebernehmen.map(t => t.grund.dokumentGrundTyp)
            )
        ];

        return uniqueGrundTypes.map(grundTyp => {
            const grundeOfTyp = dokumenteZuUebernehmen
                .filter(
                    dokumentZuUebernehmen =>
                        dokumentZuUebernehmen.grund.dokumentGrundTyp ===
                        grundTyp
                )
                .map(dokumentZuUebernehmen => dokumentZuUebernehmen.grund);

            const uniqueGrundeOfTyp = grundeOfTyp.reduce(
                (uniqueGrunde, item) => {
                    if (
                        !uniqueGrunde.some(
                            grundFromUniqueList =>
                                grundFromUniqueList.id === item.id
                        )
                    ) {
                        uniqueGrunde.push(item);
                    }
                    return uniqueGrunde;
                },
                [] as TSDokumentGrund[]
            );

            const grundeWithDokumentEntscheidungen = uniqueGrundeOfTyp.map(
                grund => ({
                    grund,
                    dokumente: dokumenteZuUebernehmen
                        .filter(
                            dokumentZuUebernehmen =>
                                dokumentZuUebernehmen.grund.id === grund.id
                        )
                        .map(dokumentZuUebernehmen => ({
                            dokument: dokumentZuUebernehmen.dokument,
                            erneuern: false
                        }))
                })
            );

            return {
                grundTyp,
                grundeWithDocumentDecisions: grundeWithDokumentEntscheidungen
            };
        });
    });

    erneuern() {
        const grundeWithDecisions =
            this.groupedDokumenteZuUebernehmenTyps()!.flatMap(grundGroup =>
                this.groupToGrundWithDokumentDecisions(grundGroup)
            );

        const dokumenteZuUebernehmen = grundeWithDecisions
            .filter(decisionWithGrund => decisionWithGrund.decision.erneuern)
            .map(decisionWithGrund => ({
                grund: decisionWithGrund.grund,
                dokument: decisionWithGrund.decision.dokument
            }));

        this.dokumenteService
            .dokumenteUebernehmen(
                this.gesuchmodelManager.getGesuch().id,
                dokumenteZuUebernehmen
            )
            .then(() => this.ref.close(true));
    }

    private groupToGrundWithDokumentDecisions(grundGroup: {
        grundeWithDocumentDecisions: GrundGroupWithDokumentDecisions[];
    }): GrundWithDokumentDecision[] {
        return grundGroup.grundeWithDocumentDecisions.flatMap(
            grundWithDokuments =>
                grundWithDokuments.dokumente.flatMap(documentDecision => ({
                    grund: grundWithDokuments.grund,
                    decision: documentDecision
                }))
        );
    }

    cancel() {
        this.ref.close();
    }

    hasPersonenbezogeneGrunde(grundTyp: TSDokumentGrundTyp) {
        return [
            TSDokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG,
            TSDokumentGrundTyp.FINANZIELLESITUATION,
            TSDokumentGrundTyp.ERWERBSPENSUM,
            TSDokumentGrundTyp.ERWEITERTE_BETREUUNG,
            TSDokumentGrundTyp.KINDER
        ].includes(grundTyp);
    }
}
