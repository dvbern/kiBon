/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import {
    ChangeDetectionStrategy,
    Component,
    Input,
    OnInit,
    signal,
    inject
} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {Observable} from 'rxjs';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {GemeindeRS} from '../../../gesuch/service/gemeindeRS.rest';
import {TSGemeindeStammdaten} from '../../../models/TSGemeindeStammdaten';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {DownloadRS} from '../../core/service/downloadRS.rest';
import {TSEinstellungKey} from '../../../admin/einstellungen/TSEinstellungKey';
import {TSMusterDokumentTyp} from '../../../models/enums/TSMusterDokumentTyp';

@Component({
    selector: 'dv-edit-gemeinde-korrespondenz',
    templateUrl: './edit-gemeinde-korrespondenz.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
    standalone: false
})
export class EditGemeindeKorrespondenzComponent implements OnInit {
    gemeindeRS = inject(GemeindeRS);
    downloadRS = inject(DownloadRS);
    einstellungRS = inject(EinstellungRS);
    $translate = inject(TranslateService);

    @Input() public stammdaten$: Observable<TSGemeindeStammdaten>;
    @Input() public editMode: boolean;

    isOnlineFreigabeDeactivatedInAtLeastOnePeriode = signal<boolean>(false);

    public ngOnInit(): void {
        this.einstellungRS
            .findEinstellungByKey(TSEinstellungKey.GESUCHFREIGABE_ONLINE)
            .subscribe(response => {
                const isOnlineFreigabeDeactivated = response.some(e => {
                    return EbeguUtil.getBoolean(e.value) === false;
                });
                this.isOnlineFreigabeDeactivatedInAtLeastOnePeriode.set(
                    isOnlineFreigabeDeactivated
                );
            });
    }

    public downloadMusterdokumentKorrespondenz(gemeindeId: string) {
        this.downloadMusterdokument(
            gemeindeId,
            TSMusterDokumentTyp.KORRESPONDENZ_MUSTERDOKUMENT
        );
    }

    public downloadMusterdokumentFreigabequittung(gemeindeId: string) {
        this.downloadMusterdokument(
            gemeindeId,
            TSMusterDokumentTyp.FREIGABEQUITTUNG_MUSTERDOKUMENT
        );
    }

    private downloadMusterdokument(
        gemeindeId: string,
        dokumentTyp: TSMusterDokumentTyp
    ): void {
        this.gemeindeRS
            .downloadMusterDokument(gemeindeId, dokumentTyp)
            .then(response => {
                const file = new Blob([response], {type: 'application/pdf'});
                const filename = this.$translate.instant(dokumentTyp);
                this.downloadRS.openDownload(file, filename);
            });
    }

    public escapeSignatur(standardSignatur: string): string {
        return standardSignatur?.replace(/\n/g, '<br />');
    }
}
