import {
    ChangeDetectionStrategy,
    Component,
    inject,
    linkedSignal
} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {CommonModule} from '@angular/common';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {DokumenteRS} from '../../../../../../src/gesuch/service/dokumenteRS.rest';
import {GesuchModelManager} from '../../../../../../src/gesuch/service/gesuchModelManager';
import {SharedModule} from '../../../../../../src/app/shared/shared.module';
import {MatDialogRef} from '@angular/material/dialog';
import {
    GrundGroupWithDokumentDecisions,
    GrundWithDokumentDecision
} from './types';
import {TSDokumentGrund} from '../../../../../../src/models/TSDokumentGrund';
import {TSDokumentGrundTyp} from '@kibon/shared/model/enums';
import {GesuchPopupHeadingComponent} from '@kibon/gesuch-heading';

@Component({
    selector: 'lib-gesuch-dokumente-erneuern-selection',
    imports: [
        CommonModule,
        SharedModule,
        GesuchPopupHeadingComponent,
        MatProgressSpinnerModule
    ],
    templateUrl: './gesuch-dokumente-erneuern-selection.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GesuchDokumenteErneuernSelectionComponent {
    private readonly dokumenteService = inject(DokumenteRS);
    private readonly ref = inject(MatDialogRef);
    readonly gesuchmodelManager = inject(GesuchModelManager);

    erneuerbareDokumenteRes = rxResource({
        params: () => ({gesuchId: this.gesuchmodelManager.getGesuch().id}),
        stream: ({params}) =>
            this.dokumenteService.getErneuerbareDokumentTyps(params.gesuchId)
    });

    groupedErneuerbareDokumentTyps = linkedSignal(() => {
        const erneuerbareDokumente = this.erneuerbareDokumenteRes.value();
        if (!erneuerbareDokumente) {
            return undefined;
        }
        const uniqueGrundTypes = [
            ...new Set(erneuerbareDokumente.map(t => t.grund.dokumentGrundTyp))
        ];

        return uniqueGrundTypes.map(grundTyp => {
            const grundeOfTyp = erneuerbareDokumente
                .filter(
                    erneuerbaresDokument =>
                        erneuerbaresDokument.grund.dokumentGrundTyp === grundTyp
                )
                .map(erneuerbaresDokument => erneuerbaresDokument.grund);

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
                    dokumente: erneuerbareDokumente
                        .filter(
                            erneuerbaresDokument =>
                                erneuerbaresDokument.grund.id === grund.id
                        )
                        .map(erneuerbaresDokument => ({
                            dokument: erneuerbaresDokument.dokument,
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
            this.groupedErneuerbareDokumentTyps()!.flatMap(grundGroup =>
                this.groupToGrundWithDokumentDecisions(grundGroup)
            );

        const dokumenteToErneuern = grundeWithDecisions
            .filter(decisionWithGrund => decisionWithGrund.decision.erneuern)
            .map(decisionWithGrund => ({
                grund: decisionWithGrund.grund,
                dokument: decisionWithGrund.decision.dokument
            }));

        this.dokumenteService
            .dokumenteErneuern(
                this.gesuchmodelManager.getGesuch().id,
                dokumenteToErneuern
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
