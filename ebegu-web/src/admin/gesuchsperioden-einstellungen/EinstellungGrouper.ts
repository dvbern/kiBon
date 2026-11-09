/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

import {TSEinstellungWithMetadata} from '../einstellungen/TSEinstellungWithMetadata';
import {EinstellungKeyGroups} from './EinstellungKeyGroups';

export type EinstellungGroup = {
    group: EinstellungKeyGroups;
    configurations: TSEinstellungWithMetadata[];
};

type GroupedEinstellung = {
    ungrouped: TSEinstellungWithMetadata[];
    /** Sorted by the group order determined by the backend */
    grouped: EinstellungGroup[];
};

export abstract class EinstellungGrouper {
    /**
     * Groups a list of TSEinstellungWithMetadata objects into categorized groups based on their keyGroup property.
     * Subgroups are not considered in this grouping. The groups are sorted by the keyGroupOrder given in the metadata.
     *
     * @param {TSEinstellungWithMetadata[]} list - An array of settings with metadata that need to be grouped.
     * @return {GroupedEinstellung} An object containing the sorted groups and a list of ungrouped settings.
     */
    static groupEinstellungen(
        list: TSEinstellungWithMetadata[]
    ): GroupedEinstellung {
        const grouped = new Map<EinstellungKeyGroups, EinstellungGroup>();
        const ungrouped: TSEinstellungWithMetadata[] = [];
        list.forEach(einstellung => {
            const group =
                EinstellungKeyGroups[
                    einstellung.metadata
                        .keyGroup as keyof typeof EinstellungKeyGroups
                ];
            if (group === undefined) {
                ungrouped.push(einstellung);
                return;
            }
            const entry = grouped.get(group) ?? {group, configurations: []};
            entry.configurations.push(einstellung);
            grouped.set(group, entry);
        });
        return {
            ungrouped,
            grouped: [...grouped.values()].sort(
                (a, b) =>
                    a.configurations[0].metadata.keyGroupOrder -
                    b.configurations[0].metadata.keyGroupOrder
            )
        };
    }
}
