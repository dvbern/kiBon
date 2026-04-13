/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.enums;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.util.GeschwisterbonusTypVisitor;

public enum GeschwisterbonusTyp {
	LUZERN {
		@Override
		public <T> T accept(GeschwisterbonusTypVisitor<T> visitor) {
			return visitor.visitLuzern();
		}
	},
	SCHWYZ {
		@Override
		public <T> T accept(GeschwisterbonusTypVisitor<T> visitor) {
			return visitor.visitSchwyz();
		}
	},
	SCHWYZ_2 {
		@Override
		public <T> T accept(GeschwisterbonusTypVisitor<T> visitor) {
			return visitor.visitSchwyz2();
		}
	},
	NONE {
		@Override
		public <T> T accept(GeschwisterbonusTypVisitor<T> visitor) {
			return visitor.visitNone();
		}
	};

	public static GeschwisterbonusTyp getEnumValue(Einstellung einstellung) {
		return GeschwisterbonusTyp.valueOf(einstellung.getValue());
	}

	public abstract <T> T accept(GeschwisterbonusTypVisitor<T> visitor);
}
