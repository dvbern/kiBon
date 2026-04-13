/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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
    inject
} from '@angular/core';
import {TSWizardStepXTyp} from '../../../../models/enums/TSWizardStepXTyp';
import {TSGemeindeKennzahlen} from '../../../../models/gemeindeantrag/gemeindekennzahlen/TSGemeindeKennzahlen';
import {LogFactory} from '@utils/log';
import {WizardStepXRS} from '../../../core/service/wizardStepXRS.rest';
import {GemeindeKennzahlenService} from '../gemeinde-kennzahlen.service';

const LOG = LogFactory.createLog('GemeindeKennzahlenUiComponent');

@Component({
    selector: 'dv-gemeinde-kennzahlen-ui',
    templateUrl: './gemeinde-kennzahlen-ui.component.html',
    styleUrls: ['./gemeinde-kennzahlen-ui.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class GemeindeKennzahlenUiComponent implements OnInit {
    private readonly gemeindeKennzahlenService = inject(
        GemeindeKennzahlenService
    );
    private readonly wizardService = inject(WizardStepXRS);

    @Input()
    public gemeindeKennzahlenId: string;

    public wizardTyp = TSWizardStepXTyp.GEMEINDE_KENNZAHLEN;
    public gemeindeKennzahlen: TSGemeindeKennzahlen;

    public ngOnInit(): void {
        this.gemeindeKennzahlenService.getGemeindeKennzahlenAntrag().subscribe(
            antrag => {
                this.gemeindeKennzahlen = antrag;
                this.wizardService.updateSteps(
                    this.wizardTyp,
                    this.gemeindeKennzahlen.id
                );
            },
            error => LOG.error(error)
        );
        this.gemeindeKennzahlenService.updateGemeindeKennzahlenAntragStore(
            this.gemeindeKennzahlenId
        );
    }
}
