/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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
    ChangeDetectorRef,
    Component,
    OnInit,
    inject
} from '@angular/core';
import {UIRouterGlobals} from '@uirouter/core';
import {fromEvent, Observable} from 'rxjs';
import {map, startWith, throttleTime} from 'rxjs/operators';
import {TSMandant} from '@kibon/shared/model/entity';
import {LogFactory} from '@kibon/shared/util-fn/log-factory';
import {MandantService} from '@kibon/shared-util-mandant-service';
const LOG = LogFactory.createLog('PortalSelectionComponent');

@Component({
    selector: 'dv-portal-selection',
    templateUrl: './portal-selection.component.html',
    styleUrls: ['./portal-selection.component.less', './../onboarding.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class PortalSelectionComponent implements OnInit {
    private readonly mandantService = inject(MandantService);
    private readonly routerGlobals = inject(UIRouterGlobals);
    private readonly cd = inject(ChangeDetectorRef);

    public mandantFilter: string;
    public mandants: TSMandant[];
    public isScreenMobile$: Observable<boolean>;

    private readonly MOBILE_THRESHOLD = 700;
    private readonly THROTTLE_TIME = 50;

    public ngOnInit(): void {
        this.mandantService.getAll().subscribe({
            next: mandants => {
                this.mandants = this.orderByTimestampErstellt(mandants);
                this.cd.markForCheck();
            },
            error: error => LOG.error(error)
        });

        // Checks if screen size is less than 1024 pixels
        const checkScreenSize = () =>
            document.body.offsetWidth < this.MOBILE_THRESHOLD;

        // Create observable from window resize event throttled so only fires every 500ms
        this.isScreenMobile$ = fromEvent(window, 'resize').pipe(
            startWith(checkScreenSize()),
            throttleTime(this.THROTTLE_TIME),
            map(checkScreenSize)
        );
    }

    private orderByTimestampErstellt(mandants: TSMandant[]): TSMandant[] {
        return mandants.sort((a, b) =>
            a.timestampErstellt.diff(b.timestampErstellt)
        );
    }

    public selectMandant(mandant: TSMandant): void {
        const kibonMandant = this.mandantService.mandantToKibonMandant(mandant);
        this.mandantService.selectMandant(
            kibonMandant,
            this.routerGlobals.params.path ?? '#/start'
        );
    }

    public getMandantLogoUrl(mandant: TSMandant): string {
        const kibonMandant = this.mandantService.mandantToKibonMandant(mandant);
        return `assets/images/${this.mandantService.getMandantLogoName(kibonMandant)}`;
    }
}
