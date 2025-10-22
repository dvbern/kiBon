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

package ch.dvbern.ebegu.types;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.validators.datetimeranges.CheckDateTimeRange;

import static com.google.common.base.Preconditions.checkNotNull;

@Embeddable
@CheckDateTimeRange
public class DateTimeRange implements Serializable, Comparable<DateTimeRange> {

	private static final long serialVersionUID = 8244737446639846684L;

	public static final LocalDateTime END_OF_TIME = LocalDateTime.of(
		9999,
		12,
		31,
		23,
		59,
		59
	);

	@Nonnull
	@NotNull
	@Column(nullable = false)
	private LocalDateTime gueltigAb;

	@Nonnull
	@NotNull
	@Column(nullable = false)
	private LocalDateTime gueltigBis;

	public DateTimeRange(
		@Nonnull LocalDateTime gueltigAb,
		@Nonnull LocalDateTime gueltigBis
	) {
		this.gueltigAb = Objects.requireNonNull(gueltigAb);
		this.gueltigBis = Objects.requireNonNull(gueltigBis);
	}

	/**
	 * Von jetzt bis zur Unendlichkeit
	 */
	public DateTimeRange() {
		this(LocalDateTime.now(), END_OF_TIME);
	}

	@Nonnull
	public @NotNull LocalDateTime getGueltigAb() {
		return gueltigAb;
	}

	public void setGueltigAb(@Nonnull @NotNull LocalDateTime gueltigAb) {
		this.gueltigAb = gueltigAb;
	}

	@Nonnull
	public @NotNull LocalDateTime getGueltigBis() {
		return gueltigBis;
	}

	public void setGueltigBis(@Nonnull @NotNull LocalDateTime gueltigBis) {
		this.gueltigBis = gueltigBis;
	}

	@Override
	public int hashCode() {
		int result = getGueltigAb().hashCode();
		result = 31 * result + getGueltigBis().hashCode();
		return result;
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DateTimeRange)) {
			return false;
		}

		DateTimeRange other = (DateTimeRange) o;

		return 0 == this.compareTo(other);
	}

	@Override
	public int compareTo(@Nonnull DateTimeRange o) {
		checkNotNull(o);

		int cmp = getGueltigAb().compareTo(o.getGueltigAb());
		if (cmp == 0) {
			cmp = getGueltigBis().compareTo(o.getGueltigBis());
		}
		return cmp;
	}
}
