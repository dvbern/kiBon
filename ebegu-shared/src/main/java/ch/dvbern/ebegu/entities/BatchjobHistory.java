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

package ch.dvbern.ebegu.entities;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import ch.dvbern.ebegu.enums.BatchjobStatus;
import lombok.Getter;
import lombok.Setter;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;
import static ch.dvbern.ebegu.util.Constants.DB_TEXTAREA_LENGTH;

/**
 * Protokolliert einen Lauf eines Batchjobs. Die Eintraege sind global und nicht mandantenabhaengig.
 */
@Entity
@Getter
@Setter
public class BatchjobHistory extends AbstractEntity {

	@Serial
	private static final long serialVersionUID = 4999440001747253997L;

	@Nonnull
	@NotNull
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	private String name;

	@Nonnull
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BatchjobStatus status;

	@Nonnull
	@NotNull
	@Column(nullable = false)
	private LocalDateTime timestampStarted;

	@Nullable
	@Column(nullable = true)
	private LocalDateTime timestampFinished;

	@Nullable
	@Size(max = DB_TEXTAREA_LENGTH)
	@Column(nullable = true, length = DB_TEXTAREA_LENGTH)
	private String result;

	@Override
	public boolean isSame(AbstractEntity other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BatchjobHistory otherHistory)) {
			return false;
		}
		return Objects.equals(name, otherHistory.name)
			&& status == otherHistory.status
			&& Objects.equals(timestampStarted, otherHistory.timestampStarted)
			&& Objects.equals(timestampFinished, otherHistory.timestampFinished)
			&& Objects.equals(result, otherHistory.result);
	}
}
