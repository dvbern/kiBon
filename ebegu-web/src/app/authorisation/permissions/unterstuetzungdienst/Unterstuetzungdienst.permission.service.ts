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
 *
 */

import {inject, Injectable} from '@angular/core';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';

enum PermissionsUnterstuetzungdienst {
    STAMMDATEN_WRITE = 'STAMMDATEN_WRITE',
    NAME_WRITE = 'NAME_WRITE'
}

const PERMISSIONS_UNTERSTUETZUNGDIENST: {
    [k in PermissionsUnterstuetzungdienst]: Array<TSRole>;
} = {
    STAMMDATEN_WRITE: [TSRole.SUPER_ADMIN, TSRole.ADMIN_SOZIALDIENST],
    NAME_WRITE: [
        TSRole.SUPER_ADMIN,
        TSRole.ADMIN_MANDANT,
        TSRole.SACHBEARBEITER_MANDANT
    ]
};

@Injectable()
export class UnterstuetzungdienstPermissionService {
    private readonly authServiceRS = inject(AuthServiceRS);

    public canEditStammdaten(): boolean {
        return this.authServiceRS.isOneOfRoles(
            PERMISSIONS_UNTERSTUETZUNGDIENST.STAMMDATEN_WRITE
        );
    }

    public canEditName(): boolean {
        return this.authServiceRS.isOneOfRoles(
            PERMISSIONS_UNTERSTUETZUNGDIENST.NAME_WRITE
        );
    }
}
