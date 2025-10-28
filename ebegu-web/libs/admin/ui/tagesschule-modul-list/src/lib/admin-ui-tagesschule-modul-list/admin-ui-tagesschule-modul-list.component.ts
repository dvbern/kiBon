import {
    ChangeDetectionStrategy,
    Component,
    inject,
    input,
    output,
    OutputEmitterRef
} from '@angular/core';

import {MatDialog} from '@angular/material/dialog';
import {TSModulTagesschuleGroupHasAnmeldung} from '@kibon/admin/model/institution-tagesschule-einstellungen';
import {
    TSInstitutionStammdaten,
    TSModulTagesschule,
    TSModulTagesschuleGroup
} from '@kibon/shared/model/entity';
import {getWeekdaysValues} from '@kibon/shared/model/enums';
import {TranslateService} from '@ngx-translate/core';
import {SharedModule} from '../../../../../../../src/app/shared/shared.module';
import {EbeguUtil} from '../../../../../../../src/utils/EbeguUtil';

@Component({
    selector: 'lib-admin-ui-tagesschule-modul-list',
    imports: [SharedModule],
    templateUrl: './admin-ui-tagesschule-modul-list.component.html',
    styleUrl: './admin-ui-tagesschule-modul-list.component.less',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminUiTagesschuleModulListComponent {
    private readonly dialog = inject(MatDialog);
    private readonly translate = inject(TranslateService);

    modulTagesschuleGroups = input.required<
        TSModulTagesschuleGroupHasAnmeldung[]
    >({});
    schnittstellenInfosVisible = input.required<boolean>();
    editMode = input.required<boolean>();
    isBeforeAktivierungsdatum = input.required<boolean>();
    stammdaten = input.required<TSInstitutionStammdaten>();
    isScolarisEinstellungen = input.required<boolean>();

    editClicked: OutputEmitterRef<{
        group: TSModulTagesschuleGroup;
    }> = output();
    removeClicked: OutputEmitterRef<{
        group: TSModulTagesschuleGroup;
    }> = output();
    schnittstelleInfoClicked: OutputEmitterRef<{
        group: TSModulTagesschuleGroup;
    }> = output();

    private readonly panelClass = 'dv-mat-dialog-ts';

    removeModulTagesschuleGroup(
        group: TSModulTagesschuleGroupHasAnmeldung
    ): void {
        this.removeClicked.emit({group});
    }

    public editModulTagesschuleGroup(
        group: TSModulTagesschuleGroupHasAnmeldung
    ): void {
        if (this.canEditModule(group)) {
            this.editClicked.emit({group});
        }
    }

    public showSchnittstelleInfos(
        group: TSModulTagesschuleGroupHasAnmeldung
    ): void {
        this.schnittstelleInfoClicked.emit({group});
    }

    public isScolaris(
        modulTagesschuleGroups: Array<TSModulTagesschuleGroupHasAnmeldung>
    ): boolean {
        return (
            EbeguUtil.isNotNullOrUndefined(modulTagesschuleGroups[0]) &&
            modulTagesschuleGroups[0].modulTagesschuleName.startsWith(
                'SCOLARIS_'
            )
        );
    }

    public getDeleteButtonTooltip(
        group: TSModulTagesschuleGroupHasAnmeldung,
        last: boolean
    ): string {
        if (this.canDeleteModule(group, last)) {
            return '';
        }
        if (this.isScolarisEinstellungen() && !last) {
            return this.translate.instant('MODUL_SCOLARIS_NICHT_LOESCHBAR');
        }
        return this.translate.instant('MODUL_NICHT_LOESCHBAR_TOOLTIP');
    }

    public canEditModule(group: TSModulTagesschuleGroupHasAnmeldung): boolean {
        if (group.isNew()) {
            return true;
        }
        return this.isBeforeAktivierungsdatum();
    }

    public canDeleteModule(
        group: TSModulTagesschuleGroupHasAnmeldung,
        last: boolean
    ): boolean {
        // bei scolaris Modulen darf nur immer das letzte gelöscht werden
        if (this.isScolarisEinstellungen() && !last) {
            return false;
        }
        return !group.hasAnmeldung;
    }

    public getEditButtonTooltip(
        group: TSModulTagesschuleGroupHasAnmeldung
    ): string {
        if (!this.canEditModule(group)) {
            return this.translate.instant('MODUL_NICHT_BEARBEITBAR_TOOLTIP');
        }
        return '';
    }

    public getBezeichnung(group: TSModulTagesschuleGroupHasAnmeldung): string {
        let name = '';
        if (group.bezeichnung.textDeutsch) {
            name = `${group.bezeichnung.textDeutsch} / ${group.bezeichnung.textFranzoesisch}`;
        }
        return name;
    }

    public getWochentageAsString(
        group: TSModulTagesschuleGroupHasAnmeldung
    ): string {
        return group.module
            .map((gem: TSModulTagesschule) => gem.wochentag)
            .map(ordinal => getWeekdaysValues().indexOf(ordinal))
            .sort()
            .map((tag: number) =>
                this.translate.instant(`${getWeekdaysValues()[tag]}_SHORT`)
            )
            .join(', ');
    }
}
