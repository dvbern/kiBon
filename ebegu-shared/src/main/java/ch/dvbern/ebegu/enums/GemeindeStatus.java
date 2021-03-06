/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.enums;

import java.util.ArrayList;
import java.util.List;

public enum GemeindeStatus {

	AKTIV,
	EINGELADEN;

	public boolean isEnabled() {
		return AKTIV == this;
	}

	public static List<GemeindeStatus> getValuesForFilter(String name) {
		List<GemeindeStatus> values = new ArrayList<>();
		values.add(GemeindeStatus.valueOf(name));
		return values;
	}
}
