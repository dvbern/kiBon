/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.entities.sozialdienst;

import java.util.Objects;

import javax.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Displayable;
import ch.dvbern.ebegu.entities.HasMandant;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.SozialdienstStatus;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

@Audited
@Entity
@Table(
	uniqueConstraints = {
		@UniqueConstraint(columnNames = "name",
			name = "UK_sozialdienst_name"),
	}
)
public class Sozialdienst extends AbstractEntity implements
	Displayable,
	HasMandant {

	private static final long serialVersionUID = 3613740962178552103L;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_sozialdienst_mandant_id"),
		updatable = false)
	private Mandant mandant;

	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	@NotNull
	@Nonnull
	private String name;

	@NotNull
	@Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private SozialdienstStatus status = SozialdienstStatus.EINGELADEN;

	@Nonnull
	public Mandant getMandant() {
		return mandant;
	}

	public void setMandant(@Nonnull Mandant mandant) {
		this.mandant = mandant;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	@Nonnull
	public SozialdienstStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull SozialdienstStatus status) {
		this.status = status;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (!(other instanceof Sozialdienst)) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		Sozialdienst sozialdienst = (Sozialdienst) other;
		return Objects.equals(this.getName(), sozialdienst.getName())
			&& Objects.equals(this.getStatus(), sozialdienst.getStatus())
			&& Objects.equals(this.getMandant(), sozialdienst.getMandant());
	}
}
