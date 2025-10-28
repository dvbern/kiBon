import {Component, inject, model} from '@angular/core';

import {SharedPatternTooltipXComponent} from '@kibon/shared-pattern-tooltip-x';
import {SharedModule} from '../../../../../../../../src/app/shared/shared.module';
import {TabellarischeMaskeTitleComponent} from '../../styling/tabellarische-maske-title/tabellarische-maske-title.component';
import {TabellarischeMaskeTableComponent} from '../../styling/tabellarische-maske-table/tabellarische-maske-table.component';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MonthUmrechnungsUtil} from '../../util/MonthUmrechnungsUtil';
import {
    BetreuungspensumWithTage,
    MonthAbschnitte,
    TabellarischeMaskeDialogData
} from '../../types/types';
import {TSBetreuungspensum} from '../../../../../../../../src/models/TSBetreuungspensum';
import {InputUtil} from '../../util/InputUtil';

@Component({
    imports: [
        SharedModule,
        TabellarischeMaskeTitleComponent,
        TabellarischeMaskeTableComponent,
        SharedPatternTooltipXComponent
    ],
    templateUrl: 'betreuung-pattern-tabellarische-maske-kita.component.html',
    styleUrl: 'betreuung-pattern-tabellarische-maske-kita.component.less',
    standalone: true
})
export class BetreuungPatternTabellarischeMaskeKitaComponent {
    readonly ref = inject(
        MatDialogRef<BetreuungPatternTabellarischeMaskeKitaComponent>
    );
    readonly dialogData = inject<TabellarischeMaskeDialogData>(MAT_DIALOG_DATA);
    readonly gpData = {
        firstYear: this.dialogData.gesuchsperiode.getBasisJahrPlus1(),
        secondYear: this.dialogData.gesuchsperiode.getBasisJahrPlus2()
    };
    readonly abschnitte = model<MonthAbschnitte<BetreuungspensumWithTage>>(
        MonthUmrechnungsUtil.toMonthlyKitaBetreuungInTage(
            this.dialogData.betreuung,
            this.gpData,
            this.dialogData.einstellungen.multiplier
        )
    );
    readonly InputUtil = InputUtil;

    uebernehmen() {
        this.ref.close(this.getBetreuungsPensenToWriteBack());
    }

    private getBetreuungsPensenToWriteBack(): TSBetreuungspensum[] {
        return Object.values(this.abschnitte()).map(b => {
            b.pensum = MonthUmrechnungsUtil.toPercentage(
                b.pensumInTage,
                this.dialogData.einstellungen.multiplier * 100,
                2
            );
            // make sure the type is correct and the id is set to a new id
            const asTSBetreuungspensum = b.deepCopyTo(new TSBetreuungspensum());
            asTSBetreuungspensum.id = undefined;
            return asTSBetreuungspensum;
        });
    }

    cancel() {
        this.ref.close();
    }
}
