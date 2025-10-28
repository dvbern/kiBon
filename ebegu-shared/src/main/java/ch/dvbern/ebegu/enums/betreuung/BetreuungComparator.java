/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.enums.betreuung;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.util.doppelbetreuung.DoppelbetreuungPrioEinstellungVisitor;

public enum BetreuungComparator {
	DEFAULT {
		@Override
		public <T> T accept(DoppelbetreuungPrioEinstellungVisitor<T> visitor) {
			return visitor.visitDefault();
		}
	},

	DEFAULT_NEW {
		@Override
		public <T> T accept(DoppelbetreuungPrioEinstellungVisitor<T> visitor) {
			return visitor.visitDefaultNew();
		}
	},
	APPENZELL {
		@Override
		public <T> T accept(DoppelbetreuungPrioEinstellungVisitor<T> visitor) {
			return visitor.visitAppenzell();
		}
	};

	public static BetreuungComparator getEnumValue(Einstellung einstellung) {
		return BetreuungComparator.valueOf(einstellung.getValue());
	}

	public abstract <T> T accept(
		DoppelbetreuungPrioEinstellungVisitor<T> visitor
	);
}
