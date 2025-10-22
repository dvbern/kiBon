/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.entities.gemeindeantrag;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class FerienbetreuungAngabenContainerStatusHistory extends
	AbstractEntity {

	@Serial
	private static final long serialVersionUID = 7772645713958975926L;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(
		name = "FK_fb_statushistory_id"), nullable = false)
	private FerienbetreuungAngabenContainer container;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(
		name = "FK_fb_statushistory_benutzer_id"), nullable = false)
	private Benutzer benutzer;

	@NotNull
	@Nonnull
	@Column(nullable = false)
	private LocalDateTime timestampVon;

	@Column()
	private LocalDateTime timestampBis;

	@NotNull
	@Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private FerienbetreuungAngabenStatus status;

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (!(other instanceof FerienbetreuungAngabenContainerStatusHistory that)) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		return getContainer().equals(
			that.getContainer()
		)
			&&
			getBenutzer().equals(that.getBenutzer())
			&&
			getTimestampVon().equals(that.getTimestampVon())
			&&
			Objects.equals(getTimestampBis(), that.getTimestampBis())
			&&
			getStatus() == that.getStatus();
	}

}
