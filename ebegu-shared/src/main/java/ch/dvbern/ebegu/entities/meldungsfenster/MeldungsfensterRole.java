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

package ch.dvbern.ebegu.entities.meldungsfenster;

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.enums.UserRole;

public enum MeldungsfensterRole {
	SUPER_ADMIN {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.SUPER_ADMIN);
		}
	},
	ADMIN_BG {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.ADMIN_BG);
		}
	},
	SACHBEARBEITER_BG {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.SACHBEARBEITER_BG);
		}
	},
	ADMIN_TS {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.ADMIN_TS);
		}
	},
	SACHBEARBEITER_TS {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.SACHBEARBEITER_TS);
		}
	},
	ADMIN_GEMEINDE {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.ADMIN_GEMEINDE);
		}
	},
	SACHBEARBEITER_GEMEINDE {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.SACHBEARBEITER_GEMEINDE);
		}
	},
	JURIST {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.JURIST);
		}
	},
	REVISOR {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.REVISOR);
		}
	},
	ADMIN_MANDANT {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.ADMIN_MANDANT);
		}
	},
	SACHBEARBEITER_MANDANT {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.SACHBEARBEITER_MANDANT);
		}
	},
	ADMIN_TRAEGERSCHAFT {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.ADMIN_TRAEGERSCHAFT);
		}
	},
	SACHBEARBEITER_TRAEGERSCHAFT {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.SACHBEARBEITER_TRAEGERSCHAFT);
		}
	},
	ADMIN_INSTITUTION {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.ADMIN_INSTITUTION);
		}
	},
	SACHBEARBEITER_INSTITUTION {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.SACHBEARBEITER_INSTITUTION);
		}
	},
	ADMIN_SOZIALDIENST {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.ADMIN_SOZIALDIENST);
		}
	},
	SACHBEARBEITER_SOZIALDIENST {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.SACHBEARBEITER_SOZIALDIENST);
		}
	},
	STEUERAMT {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.STEUERAMT);
		}
	},
	ADMIN_FERIENBETREUUNG {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.ADMIN_FERIENBETREUUNG);
		}
	},
	SACHBEARBEITER_FERIENBETREUUNG {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.SACHBEARBEITER_FERIENBETREUUNG);
		}
	},
	GESUCHSTELLER {
		@Override
		public List<UserRole> getRoles() {
			return List.of(UserRole.GESUCHSTELLER);
		}
	},
	ANONYMOUS {
		@Override
		public List<UserRole> getRoles() {
			return new ArrayList<>();
		}
	};

	public abstract List<UserRole> getRoles();
}
