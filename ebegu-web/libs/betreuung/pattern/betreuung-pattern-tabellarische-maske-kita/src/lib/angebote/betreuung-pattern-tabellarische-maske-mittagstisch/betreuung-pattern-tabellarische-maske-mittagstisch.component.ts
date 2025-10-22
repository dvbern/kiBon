import {Component, inject, model} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from '../../../../../../../../src/app/shared/shared.module';
import {TabellarischeMaskeTitleComponent} from '../../styling/tabellarische-maske-title/tabellarische-maske-title.component';
import {TabellarischeMaskeTableComponent} from '../../styling/tabellarische-maske-table/tabellarische-maske-table.component';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MonthUmrechnungsUtil} from '../../util/MonthUmrechnungsUtil';
import {MonthAbschnitte, TabellarischeMaskeDialogData} from '../../types/types';
import {TSBetreuungspensum} from '../../../../../../../../src/models/TSBetreuungspensum';
import {InputUtil} from '../../util/InputUtil';

@Component({
    imports: [
        CommonModule,
        SharedModule,
        TabellarischeMaskeTitleComponent,
        TabellarischeMaskeTableComponent
    ],
    templateUrl:
        'betreuung-pattern-tabellarische-maske-mittagstisch.component.html',
    styleUrl:
        'betreuung-pattern-tabellarische-maske-mittagstisch.component.less',
    standalone: true
})
export class BetreuungPatternTabellarischeMaskeMittagstischComponent {
    readonly ref = inject(
        MatDialogRef<BetreuungPatternTabellarischeMaskeMittagstischComponent>
    );
    readonly dialogData = inject<TabellarischeMaskeDialogData>(MAT_DIALOG_DATA);
    readonly gpData = {
        firstYear: this.dialogData.gesuchsperiode.getBasisJahrPlus1(),
        secondYear: this.dialogData.gesuchsperiode.getBasisJahrPlus2()
    };
    protected readonly InputUtil = InputUtil;

    readonly abschnitte = model<MonthAbschnitte<TSBetreuungspensum>>(
        MonthUmrechnungsUtil.toMonthlyMittagstischBetreuung(
            this.dialogData.betreuung,
            this.gpData
        )
    );

    uebernehmen() {
        this.ref.close(this.getBetreuungsPensenToWriteBack());
    }

    private getBetreuungsPensenToWriteBack(): TSBetreuungspensum[] {
        return Object.values(this.abschnitte()).map(b => {
            // make sure the type is correct and the id is set to a new id
            b.id = undefined;
            return b;
        });
    }

    cancel() {
        this.ref.close();
    }
}
