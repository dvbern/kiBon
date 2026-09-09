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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities.berechtigung;

import java.util.Set;

import ch.dvbern.ebegu.entities.AbstractDateRangeAccessible;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.enums.UserRole;
import org.jetbrains.annotations.Nullable;

public interface BerechtigungAccessible extends AbstractDateRangeAccessible {

	Benutzer getBenutzer();

	UserRole getRole();

	Set<Gemeinde> getGemeindeList();

	Institution getInstitution();

	Traegerschaft getTraegerschaft();

	Sozialdienst getSozialdienst();

	void setBenutzer(Benutzer benutzer);

	void setRole(UserRole userRole);

	void setGemeindeList(Set<Gemeinde> gemeindeList);

	void setInstitution(@Nullable Institution institution);

	void setTraegerschaft(@Nullable Traegerschaft traegerschaft);

	void setSozialdienst(@Nullable Sozialdienst sozialdienst);
}
