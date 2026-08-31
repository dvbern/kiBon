/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {
    ChangeDetectionStrategy,
    Component,
    Input,
    OnChanges,
    SimpleChanges
} from '@angular/core';
import moment from 'moment';
import {EbeguUtil} from '../../../utils/EbeguUtil';

/**
 * Eine Warnung wird angezeigt, falls das Betreuungspensum oder die Betreuungskosten eines bereits existierenden
 * und bereits laufenden Betreuungspensums überschrieben werden soll (gueltigAb in Vergangenheit).
 */
@Component({
    selector: 'betreuung-override-waring',
    templateUrl: './betreuung-override-warning.component.html',
    styleUrls: ['./betreuung-override-warning.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class BetreuungOverrideWarningComponent implements OnChanges {
    @Input()
    private readonly betreuungspensum: number;

    @Input()
    private readonly betreuungskosten: number;

    @Input()
    private readonly gueltigAb: moment.Moment;

    private firstBetreuungspensum: number;
    private firstBetreuungskosten: number;
    private firstGueltigAb: moment.Moment;
    private gueltigAbInPast: boolean = null;

    public ngOnChanges(changes: SimpleChanges): void {
        if (
            EbeguUtil.isNullOrUndefined(this.firstBetreuungspensum) &&
            changes.betreuungspensum &&
            EbeguUtil.isNotNullOrUndefined(
                changes.betreuungspensum.currentValue
            )
        ) {
            this.firstBetreuungspensum = changes.betreuungspensum.currentValue;
        }
        if (
            EbeguUtil.isNullOrUndefined(this.firstBetreuungskosten) &&
            changes.betreuungskosten &&
            EbeguUtil.isNotNullOrUndefined(
                changes.betreuungskosten.currentValue
            )
        ) {
            this.firstBetreuungskosten = changes.betreuungskosten.currentValue;
        }
        if (
            EbeguUtil.isNullOrUndefined(this.gueltigAbInPast) &&
            changes.gueltigAb &&
            EbeguUtil.isNotNullOrUndefined(changes.gueltigAb.currentValue)
        ) {
            this.firstGueltigAb = this.gueltigAb;
            this.gueltigAbInPast = moment().isAfter(this.gueltigAb);
        }
    }

    /**
     * Warnung wird angezeigt, wenn mindestens eines der folgenden zutrifft:
     * * Betreuungskosten ändert & gueltigAb liegt in Vergangenheit
     * * Betreuungspensum ändert & gueltigAb liegt in Vergangenheit
     * * gueltigAb liegt in Vergangenheit und wird verändert
     */
    public showWarning(): boolean {
        return (
            EbeguUtil.isNotNullOrUndefined(this.firstBetreuungskosten) &&
            EbeguUtil.isNotNullOrUndefined(this.betreuungskosten) &&
            EbeguUtil.isNotNullOrUndefined(this.firstBetreuungspensum) &&
            EbeguUtil.isNotNullOrUndefined(this.betreuungspensum) &&
            this.gueltigAbInPast &&
            (this.firstBetreuungspensum !== this.betreuungspensum ||
                this.firstBetreuungskosten !== this.betreuungskosten ||
                !this.firstGueltigAb.isSame(this.gueltigAb))
        );
    }
}
