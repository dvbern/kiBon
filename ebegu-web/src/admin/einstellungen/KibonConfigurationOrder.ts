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

import {GroupableConfiguration} from '../../models/einstellung/GroupableConfiguration';

type WithGroupableConfigurationMetadata = {metadata: GroupableConfiguration};

export type SubGroupedConfigurations<T> = {
    noSubGroup: T[];
    subGroups: {name: string; configurations: T[]}[];
};

/**
 * Returns a copy of the list sorted ascending by the keyOrder determined by the provided metadata
 *
 * @param list the list to sort
 */
export function sortByKeyOrder<T extends WithGroupableConfigurationMetadata>(
    list: T[]
): T[] {
    return [...list].sort((a, b) => a.metadata.keyOrder - b.metadata.keyOrder);
}

/**
 * Sorts the configurations by keyOrder and splits them into the ones without a subgroup and the subgroups.
 * The subgroups are ordered by their lowest keyOrder.
 *
 * @param list the list to group
 */
export function groupBySubGroup<T extends WithGroupableConfigurationMetadata>(
    list: T[]
): SubGroupedConfigurations<T> {
    const noSubGroup: T[] = [];
    const subGroups = new Map<string, T[]>();
    sortByKeyOrder(list).forEach(configuration => {
        const subKeyGroup = configuration.metadata.subKeyGroup;
        if (!subKeyGroup) {
            noSubGroup.push(configuration);
            return;
        }
        const configurations = subGroups.get(subKeyGroup) ?? [];
        configurations.push(configuration);
        subGroups.set(subKeyGroup, configurations);
    });
    return {
        noSubGroup,
        subGroups: [...subGroups].map(([name, configurations]) => ({
            name,
            configurations
        }))
    };
}
