import {
    ChangeDetectionStrategy,
    Component,
    inject,
    input,
    output,
    OutputEmitterRef
} from '@angular/core';

import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {SharedModule} from '../../app/shared/shared.module';
import {TSModulTagesschuleGroupHasAnmeldung} from '../../models/entity/institution-tagesschule-einstellungen/TSModulTagesschuleGroupHasAnmeldung';
import {TSInstitutionStammdaten} from '../../models/entity/TSInstitutionStammdaten';
import {TSModulTagesschule} from '../../models/entity/TSModulTagesschule';
import {TSModulTagesschuleGroup} from '../../models/entity/TSModulTagesschuleGroup';
import {getWeekdaysValues} from '../../models/enums/TSDayOfWeek';
import {EbeguUtil} from '../../utils/EbeguUtil';

@Component({
    selector: 'lib-admin-ui-tagesschule-modul-list',
    imports: [SharedModule],
    templateUrl: './tagesschule-modul-list.component.html',
    styleUrl: './tagesschule-modul-list.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TagesschuleModulListComponent {
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
