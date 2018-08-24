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

package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.util.Constants;
import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.bridge.builtin.LongBridge;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

@Audited
@Entity
@Table(
	uniqueConstraints = {
		@UniqueConstraint(columnNames = "name", name = "UK_gemeinde_name"),
		@UniqueConstraint(columnNames = {"gemeindeNummer", "mandant_id"}, name = "UK_gemeinde_gemeindeNummer_mandant")
	}
)
public class Gemeinde extends AbstractMutableEntity implements Comparable<Gemeinde> {

	private static final long serialVersionUID = -6976259296646006855L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeinde_mandant_id"))
	private Mandant mandant;

	@NotNull
	@Column(nullable = false)
	@Field(bridge = @FieldBridge(impl = LongBridge.class))
	private long gemeindeNummer = 0;

	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	@NotNull
	private String name;

	@Column(nullable = false)
	@NotNull
	private boolean enabled;


	public Mandant getMandant() {
		return mandant;
	}

	public void setMandant(Mandant mandant) {
		this.mandant = mandant;
	}

	public long getGemeindeNummer() {
		return gemeindeNummer;
	}

	public void setGemeindeNummer(long gemeindeNummer) {
		this.gemeindeNummer = gemeindeNummer;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (!(other instanceof Gemeinde)) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		Gemeinde gemeinde = (Gemeinde) other;
		return Objects.equals(this.getName(), gemeinde.getName())
			&& Objects.equals(this.getGemeindeNummer(), gemeinde.getGemeindeNummer())
			&& Objects.equals(this.getMandant(), gemeinde.getMandant());
	}

	@Override
	public int compareTo(Gemeinde o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getName(), o.getName());
		builder.append(this.getGemeindeNummer(), o.getGemeindeNummer());
		builder.append(this.getMandant(), o.getMandant());
		builder.append(this.isEnabled(), o.isEnabled());
		builder.append(this.getId(), o.getId());
		return builder.toComparison();
	}

	@Transient
	public String getPaddedGemeindeNummer() {
		return Strings.padStart(Long.toString(getGemeindeNummer()), Constants.GEMEINDENUMMER_LENGTH, '0');
	}
}
