/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

package ch.dvbern.ebegu.listener;

import javax.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.services.BenutzerService;

public class BerechtigungChangedEntityListener {

	@Inject
	private BenutzerService benutzerService;

	@PrePersist
	@PreUpdate
	protected void prePersist(@Nonnull Berechtigung berechtigung) {
		save(berechtigung, false);
	}

	@PreRemove
	protected void preDelete(@Nonnull Berechtigung berechtigung) {
		if (!berechtigung.getBenutzer().isMarkedForDeletion()) {
			save(berechtigung, true);
		}
	}

	private void save(@Nonnull Berechtigung berechtigung, boolean deleted) {
		benutzerService.saveBerechtigungHistory(berechtigung, deleted);
	}
}
