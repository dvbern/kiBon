/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.entities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;

/**
 * Entitaet zum Verknüpfen eines LastenausgleichDetails mit den Zeitabschnitten
 */
@Entity
public class LastenausgleichDetailZeitabschnitt extends AbstractEntity {

	private static final long serialVersionUID = 4243309916882090263L;

	@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
	@ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {})
	@Nonnull
	@NotNull
	@JoinColumn(nullable = false)
	private LastenausgleichDetail lastenausgleichDetail;

	@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
	@ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {})
	@Nonnull
	@NotNull
	@JoinColumn(nullable = false)
	@Getter
	private VerfuegungZeitabschnitt zeitabschnitt;

	@SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
	// This field was added later with no possibility to set a default for existing db entries so there are null values possible
	@Nullable
	@Getter
	private Boolean keinSelbstbehaltGemeinde;

	public LastenausgleichDetailZeitabschnitt() {
	}

	public LastenausgleichDetailZeitabschnitt(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull LastenausgleichDetail lastenausgleichDetail,
		@Nullable Boolean keinSelbstbehaltGemeinde
	) {
		this.zeitabschnitt = zeitabschnitt;
		this.lastenausgleichDetail = lastenausgleichDetail;
		this.keinSelbstbehaltGemeinde = keinSelbstbehaltGemeinde;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return this.getId().equals(other.getId());
	}
}
