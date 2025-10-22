/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import {IPromise} from 'angular';
import {GesuchModelManager} from '../gesuch/service/gesuchModelManager';
import {TSGesuch} from '../models/TSGesuch';

/**
 * Hier findet man unterschiedliche Hilfsmethoden für Gesuch Routes
 */
export class GesuchUtil {
    public static async checkAmountOfAntragsteller(
        gesuchModelManagerPromise: IPromise<GesuchModelManager>,
        gesuchstellerNumber: number
    ): Promise<boolean> {
        const gesuchModelManager: GesuchModelManager =
            await gesuchModelManagerPromise;
        const hasSecondGesuchsteller =
            gesuchModelManager.isGesuchsteller2Required();

        const allowedGesuchsteller: number[] = [1, 2];

        if (
            (hasSecondGesuchsteller &&
                gesuchstellerNumber === allowedGesuchsteller[0]) ||
            (hasSecondGesuchsteller &&
                gesuchstellerNumber === allowedGesuchsteller[1]) ||
            (!hasSecondGesuchsteller &&
                gesuchstellerNumber === allowedGesuchsteller[0])
        ) {
            return Promise.resolve(true);
        }
        return Promise.reject(new Error('GESUCHSTELLERNUMBER DOES NOT MATCH'));
    }

    public static async checkAmountOfChildren(
        gesuchPromise: IPromise<TSGesuch>,
        kindNumber?: number
    ) {
        const gesuch: TSGesuch = await gesuchPromise;

        // Allow undefined but not 0
        if (kindNumber === undefined) {
            return Promise.resolve(true);
        }

        // Check if there is a kindContainer with the given kindNumber
        // we cant compare kindContainers.length with/between kindNumber
        const exists = gesuch.kindContainers.some(
            kindContainer => kindContainer.kindNummer === kindNumber
        );

        if (exists) {
            return Promise.resolve(true);
        }
        return Promise.reject(new Error('KINDNUMBER NOT ALLOWED'));
    }

    public static async checkBasisJahr(
        gesuchPromise: IPromise<TSGesuch>,
        basisjahrPlus: number
    ) {
        const gesuch: TSGesuch = await gesuchPromise;
        const allowedYears: number[] = [];
        const ekvFuerBasisJahrPlus1 =
            gesuch.einkommensverschlechterungInfoContainer
                .einkommensverschlechterungInfoJA?.ekvFuerBasisJahrPlus1;
        const ekvFuerBasisJahrPlus2 =
            gesuch.einkommensverschlechterungInfoContainer
                .einkommensverschlechterungInfoJA?.ekvFuerBasisJahrPlus2;

        if (ekvFuerBasisJahrPlus1) {
            allowedYears.push(1);
        }

        if (ekvFuerBasisJahrPlus2) {
            allowedYears.push(2);
        }

        if (allowedYears.includes(basisjahrPlus)) {
            return Promise.resolve(true);
        }
        return Promise.reject(new Error('BASISJAHR NOT ALLOWED'));
    }

    public static async checkErwerbspensumForGesuchsteller(
        gesuchPromise: IPromise<TSGesuch>,
        gesuchModelManagerPromise: IPromise<GesuchModelManager>,
        gesuchstellerNumber: number,
        erwerbspensumNum?: number
    ) {
        const gesuch: TSGesuch = await gesuchPromise;
        const gesuchModelManager: GesuchModelManager =
            await gesuchModelManagerPromise;
        const hasSecondGesuchsteller = gesuch
            .extractFamiliensituation()
            .hasSecondGesuchsteller(
                gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis
            );

        if (!hasSecondGesuchsteller && gesuchstellerNumber !== 1) {
            return Promise.reject(
                new Error(`GESUCHSTELLERNUMBER MUST BE 1 WHEN THERE IS NO GS2`)
            );
        }

        if (
            hasSecondGesuchsteller &&
            (gesuchstellerNumber < 0 || gesuchstellerNumber > 2)
        ) {
            return Promise.reject(
                new Error(`GESUCHSTELLERNUMBER MUST BE > 0 AND 1 OR 2`)
            );
        }

        if (erwerbspensumNum === undefined || erwerbspensumNum === 0) {
            return Promise.resolve(true);
        }

        if (!isNaN(erwerbspensumNum) && erwerbspensumNum > 0) {
            const container =
                gesuchstellerNumber === 1
                    ? gesuch.gesuchsteller1?.erwerbspensenContainer[
                          erwerbspensumNum
                      ]
                    : gesuch.gesuchsteller2?.erwerbspensenContainer[
                          erwerbspensumNum
                      ];

            if (
                container &&
                (!container.isGSContainerEmpty() || container.erwerbspensumJA)
            ) {
                return Promise.resolve(true);
            }
        }
        return Promise.reject(
            new Error(
                `INVALID COMBINATION OF GESUCHSTELLERNUMBER (${gesuchstellerNumber}) AND ERWERBSPENSUMNUM (${erwerbspensumNum})`
            )
        );
    }

    public static async checkBetreuungsNumber(
        gesuchPromise: IPromise<TSGesuch>,
        kindNumber?: number,
        betreuungNumber?: number
    ) {
        const gesuch: TSGesuch = await gesuchPromise;
        const allowed: {kindNummer: number; betreuungsNummer: number}[] = [];
        const kindStr = kindNumber?.toString().trim();
        const betreuungStr = betreuungNumber?.toString().trim();
        const parsedKindNumber =
            kindStr && kindStr !== '' ? Number(kindStr) : NaN;
        const parsedBetreuungNumber =
            betreuungStr && betreuungStr !== '' ? Number(betreuungStr) : NaN;

        gesuch.kindContainers.forEach(kind => {
            kind.betreuungen.forEach(betreuung => {
                allowed.push({
                    kindNummer: kind.kindNummer,
                    betreuungsNummer: betreuung.betreuungNummer
                });
            });
        });

        // Validate that the kindNumber exists in the kindContainers
        const kindExists = gesuch.kindContainers.some(
            kind => kind.kindNummer === parsedKindNumber
        );

        if (!kindExists || isNaN(parsedKindNumber)) {
            return Promise.reject(new Error('KINDNUMBER NOT ALLOWED'));
        }

        // new betreuung
        if (isNaN(parsedBetreuungNumber)) {
            return Promise.resolve(true);
        }

        const isAllowed = allowed.some(
            entry =>
                entry.kindNummer === parsedKindNumber &&
                entry.betreuungsNummer === parsedBetreuungNumber
        );

        if (isAllowed) {
            return Promise.resolve(true);
        }
        return Promise.reject(
            new Error('INVALID COMBINATION OF KINDNUMMER AND BETREUUNGSNUMMER')
        );
    }
}
