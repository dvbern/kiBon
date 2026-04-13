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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {FormGroup} from '@angular/forms';
import {BehaviorSubject, combineLatest, Observable} from 'rxjs';
import {map, startWith} from 'rxjs/operators';
import {TSLastenausgleichTagesschuleAngabenGemeindeContainer} from '../../../../../models/gemeindeantrag/TSLastenausgleichTagesschuleAngabenGemeindeContainer';
import {LogFactory} from '@utils/log';

const LOG = LogFactory.createLog('TSControllingCalculator');

export class TSControllingCalculator {
    private readonly _veraenderungBetreuungsstunden: BehaviorSubject<string> =
        new BehaviorSubject<string>(undefined);
    private readonly _veraenderungBetreuungsstundenAsNumber: BehaviorSubject<number> =
        new BehaviorSubject<number>(undefined);
    private readonly _anteilStundenBesondereBeduerfnisseCurrentPeriode: BehaviorSubject<string> =
        new BehaviorSubject<string>(undefined);
    private readonly _anteilStundenBesondereBeduerfnissePreviousPeriode: BehaviorSubject<string> =
        new BehaviorSubject<string>(undefined);
    private readonly _kostenanteilGemeindeGesamtkosten: BehaviorSubject<string> =
        new BehaviorSubject<string>(undefined);
    private readonly _erstragsanteilGemeindeGesamtkosten: BehaviorSubject<string> =
        new BehaviorSubject<string>(undefined);
    private readonly _anteilElternbeitraegeCurrentPeriode: BehaviorSubject<string> =
        new BehaviorSubject<string>(undefined);
    private readonly _anteilElternbeitraegePreviousPeriode: BehaviorSubject<string> =
        new BehaviorSubject<string>(undefined);

    private readonly _angabenForm: FormGroup;
    private readonly _previousAntrag: TSLastenausgleichTagesschuleAngabenGemeindeContainer;

    public constructor(
        angabenForm: FormGroup,
        previousAntrag: TSLastenausgleichTagesschuleAngabenGemeindeContainer
    ) {
        this._angabenForm = angabenForm;
        this._previousAntrag = previousAntrag;
        this.setupCalculations();
    }

    public get veraenderungBetreuungsstunden$(): Observable<string> {
        return this._veraenderungBetreuungsstunden.asObservable();
    }

    public get veraenderungBetreuungsstundenAsNumber$(): Observable<number> {
        return this._veraenderungBetreuungsstundenAsNumber.asObservable();
    }

    public get anteilStundenBesondereBeduerfnisseCurrentPeriode$(): Observable<string> {
        return this._anteilStundenBesondereBeduerfnisseCurrentPeriode.asObservable();
    }

    public get anteilStundenBesondereBeduerfnissePreviousPeriode$(): Observable<string> {
        return this._anteilStundenBesondereBeduerfnissePreviousPeriode.asObservable();
    }

    public get kostenanteilGemeindeGesamtkosten$(): Observable<string> {
        return this._kostenanteilGemeindeGesamtkosten.asObservable();
    }

    public get erstragsanteilGemeindeGesamtkosten$(): Observable<string> {
        return this._erstragsanteilGemeindeGesamtkosten.asObservable();
    }

    public get anteilElternbeitraegeCurrentPeriode$(): Observable<string> {
        return this._anteilElternbeitraegeCurrentPeriode.asObservable();
    }

    public get anteilElternbeitraegePreviousPeriode$(): Observable<string> {
        return this._anteilElternbeitraegePreviousPeriode.asObservable();
    }

    private setupCalculations(): void {
        this.calculateVeraenderungBetreuungsstunden();
        this.calculateBesondereBeduerfnisseCurrentPeriode();
        this.calculateBesondereBeduerfnissePreviousPeriode();
        this.calculateAnteilElternbeitraegeCurrentPeriode();
        this.calculateAnteilElternbeitraegePreviousPeriode();
        this.calculateKostenanteilGemeinde();
        this.calculateUeberschussAnteil();
    }

    private calculateVeraenderungBetreuungsstunden(): void {
        if (
            !this._previousAntrag?.angabenKorrektur
                ?.lastenausgleichberechtigteBetreuungsstunden
        ) {
            this._veraenderungBetreuungsstunden.next('?');
            return;
        }
        this._angabenForm
            .get('lastenausgleichberechtigteBetreuungsstunden')
            .valueChanges.pipe(
                startWith(
                    this._angabenForm.get(
                        'lastenausgleichberechtigteBetreuungsstunden'
                    ).value
                )
            )
            .subscribe({
                next: value => {
                    let veraenderung =
                        value /
                        this._previousAntrag.angabenKorrektur
                            .lastenausgleichberechtigteBetreuungsstunden;
                    veraenderung -= 1;
                    this._veraenderungBetreuungsstundenAsNumber.next(
                        veraenderung
                    );
                    this._veraenderungBetreuungsstunden.next(
                        this.toPercent(veraenderung)
                    );
                },
                error: err => this.handleError(err)
            });
    }

    private calculateBesondereBeduerfnisseCurrentPeriode(): void {
        const faktor15 = this._angabenForm.get(
            'geleisteteBetreuungsstundenBesondereBeduerfnisse'
        );
        const faktor1 = this._angabenForm.get(
            'geleisteteBetreuungsstundenOhneBesondereBeduerfnisse'
        );

        combineLatest([
            faktor15.valueChanges.pipe(
                startWith(faktor15.value),
                map(val => parseFloat(val))
            ),
            faktor1.valueChanges.pipe(
                startWith(faktor1.value),
                map(val => parseFloat(val))
            )
        ]).subscribe({
            next: ([faktor15, faktor1]) => {
                if (isNaN(faktor15) || isNaN(faktor1)) {
                    this._anteilStundenBesondereBeduerfnisseCurrentPeriode.next(
                        '?'
                    );
                    return;
                }

                const total = faktor15 + faktor1;

                if (total === 0) {
                    this._anteilStundenBesondereBeduerfnisseCurrentPeriode.next(
                        '0%'
                    );
                    return;
                }

                const result = faktor15 / 3 / total;

                this._anteilStundenBesondereBeduerfnisseCurrentPeriode.next(
                    this.toPercent(result)
                );
            },
            error: err => this.handleError(err)
        });
    }

    private calculateBesondereBeduerfnissePreviousPeriode(): void {
        const angaben = this._previousAntrag?.angabenKorrektur;

        const faktor15 =
            angaben?.geleisteteBetreuungsstundenBesondereBeduerfnisse;
        const faktor1 =
            angaben?.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse;

        if (faktor15 == null || faktor1 == null) {
            this._anteilStundenBesondereBeduerfnissePreviousPeriode.next('?');
            return;
        }

        const total = faktor15 + faktor1;
        if (total === 0) {
            this._anteilStundenBesondereBeduerfnissePreviousPeriode.next('0%');
            return;
        }

        const result = faktor15 / 3 / total;

        this._anteilStundenBesondereBeduerfnissePreviousPeriode.next(
            this.toPercent(result)
        );
    }

    private calculateAnteilElternbeitraegeCurrentPeriode(): void {
        combineLatest([
            this._angabenForm
                .get('einnahmenElterngebuehren')
                .valueChanges.pipe(
                    startWith(
                        this._angabenForm.get('einnahmenElterngebuehren').value
                    ),
                    map(parseFloat)
                ),
            this._angabenForm
                .get('normlohnkostenBetreuungBerechnet')
                .valueChanges.pipe(
                    startWith(
                        this._angabenForm.get(
                            'normlohnkostenBetreuungBerechnet'
                        ).value
                    ),
                    map(parseFloat)
                )
        ]).subscribe({
            next: values => {
                this._anteilElternbeitraegeCurrentPeriode.next(
                    this.toPercent(values[0] / values[1])
                );
            },
            error: err => this.handleError(err)
        });
    }

    private calculateAnteilElternbeitraegePreviousPeriode(): void {
        if (!this._previousAntrag?.angabenKorrektur?.einnahmenElterngebuehren) {
            this._anteilElternbeitraegePreviousPeriode.next('?');
            return;
        }
        const result =
            this._previousAntrag.angabenKorrektur.einnahmenElterngebuehren /
            this._previousAntrag.angabenKorrektur
                .normlohnkostenBetreuungBerechnet;
        this._anteilElternbeitraegePreviousPeriode.next(this.toPercent(result));
    }

    private calculateKostenanteilGemeinde(): void {
        combineLatest([
            this._angabenForm
                .get('kostenbeitragGemeinde')
                .valueChanges.pipe(
                    startWith(
                        this._angabenForm.get('kostenbeitragGemeinde').value
                    ),
                    map(parseFloat)
                ),
            this._angabenForm
                .get('gesamtKostenTagesschule')
                .valueChanges.pipe(
                    startWith(
                        this._angabenForm.get('gesamtKostenTagesschule').value
                    ),
                    map(parseFloat)
                )
        ]).subscribe({
            next: values => {
                if (isNaN(values[0])) {
                    this._kostenanteilGemeindeGesamtkosten.next('-');
                    return;
                }
                const result = values[0] / values[1];
                this._kostenanteilGemeindeGesamtkosten.next(
                    this.toPercent(result)
                );
            },
            error: err => this.handleError(err)
        });
    }

    private calculateUeberschussAnteil(): void {
        combineLatest([
            this._angabenForm
                .get('kostenueberschussGemeinde')
                .valueChanges.pipe(
                    startWith(
                        this._angabenForm.get('kostenueberschussGemeinde').value
                    ),
                    map(parseFloat)
                ),
            this._angabenForm
                .get('gesamtKostenTagesschule')
                .valueChanges.pipe(
                    startWith(
                        this._angabenForm.get('gesamtKostenTagesschule').value
                    ),
                    map(parseFloat)
                )
        ]).subscribe({
            next: values => {
                if (isNaN(values[0])) {
                    this._erstragsanteilGemeindeGesamtkosten.next('-');
                    return;
                }
                const result = -(values[0] / values[1]);
                this._erstragsanteilGemeindeGesamtkosten.next(
                    this.toPercent(result)
                );
            },
            error: err => this.handleError(err)
        });
    }

    private toPercent(value: number): string {
        return `${(value * 100).toFixed(1)}%`;
    }

    private handleError(error: Error): void {
        LOG.error(error);
        console.error(error);
    }
}
