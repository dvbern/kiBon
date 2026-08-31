import {Component, inject, model, ChangeDetectionStrategy} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {TranslatePipe} from '@ngx-translate/core';
import {TabellarischeMaskeTitleComponent} from '../../styling/tabellarische-maske-title/tabellarische-maske-title.component';
import {TabellarischeMaskeTableComponent} from '../../styling/tabellarische-maske-table/tabellarische-maske-table.component';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MonthUmrechnungsUtil} from '../../util/MonthUmrechnungsUtil';
import {MonthAbschnitte, TabellarischeMaskeDialogData} from '../../types/types';
import {TSBetreuungspensum} from '../../../../../../models/TSBetreuungspensum';
import {InputUtil} from '../../util/InputUtil';

@Component({
    imports: [
        TabellarischeMaskeTitleComponent,
        TabellarischeMaskeTableComponent,
        TranslatePipe,
        FormsModule
    ],
    templateUrl: 'tabellarische-maske-mittagstisch.component.html',
    styleUrl: 'tabellarische-maske-mittagstisch.component.scss',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: true
})
export class TabellarischeMaskeMittagstischComponent {
    readonly ref = inject(
        MatDialogRef<TabellarischeMaskeMittagstischComponent>
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
