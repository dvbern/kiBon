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

package ch.dvbern.ebegu.entities.berechtigung;

import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nullable;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractDateRangedEntity;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.enums.UserRole;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FutureBerechtigung extends AbstractDateRangedEntity implements
	BerechtigungAccessible {

	private static final long serialVersionUID = -765432109876543210L;

	@OneToOne(mappedBy = "futureBerechtigung")
	private Benutzer benutzer;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	private UserRole role = null;

	@NotNull
	@ManyToMany
	@JoinTable(
		name = "future_berechtigung_gemeinde",
		joinColumns = @JoinColumn(name = "future_berechtigung_id",
			nullable = false),
		inverseJoinColumns = @JoinColumn(name = "gemeinde_id",
			nullable = false),
		foreignKey = @ForeignKey(
			name = "FK_future_berechtigung_gemeinde_gemeinde_id"),
		inverseForeignKey = @ForeignKey(
			name = "FK_future_berechtigung_gemeinde_future_berechtigung_id"),
		indexes = {
			@Index(
				name = "IX_future_berechtigung_gemeinde_future_berechtigung_id",
				columnList = "future_berechtigung_id"),
			@Index(name = "IX_future_berechtigung_gemeinde_gemeinde_id",
				columnList = "gemeinde_id"),
		}
	)
	private Set<Gemeinde> gemeindeList = new TreeSet<>();

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(
		name = "FK_future_berechtigung_institution_id"))
	private Institution institution = null;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(
		name = "FK_future_berechtigung_traegerschaft_id"))
	private Traegerschaft traegerschaft = null;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(
		name = "FK_future_berechtigung_sozialdienst_id"))
	private Sozialdienst sozialdienst = null;

	@Override
	public Benutzer getBenutzer() {
		return benutzer;
	}

	@Override
	public void setBenutzer(Benutzer benutzer) {
		this.benutzer = benutzer;
	}

	@Override
	public UserRole getRole() {
		return role;
	}

	@Override
	public void setRole(UserRole role) {
		this.role = role;
	}

	@Override
	public Set<Gemeinde> getGemeindeList() {
		return gemeindeList;
	}

	@Override
	public void setGemeindeList(Set<Gemeinde> gemeindeList) {
		this.gemeindeList = gemeindeList;
	}

	@Override
	@Nullable
	public Institution getInstitution() {
		return institution;
	}

	@Override
	public void setInstitution(@Nullable Institution institution) {
		this.institution = institution;
	}

	@Override
	@Nullable
	public Traegerschaft getTraegerschaft() {
		return traegerschaft;
	}

	@Override
	public void setTraegerschaft(@Nullable Traegerschaft traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	@Override
	@Nullable
	public Sozialdienst getSozialdienst() {
		return sozialdienst;
	}

	@Override
	public void setSozialdienst(@Nullable Sozialdienst sozialdienst) {
		this.sozialdienst = sozialdienst;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", getId())
			.append("role", role)
			.append("gueltigkeit", getGueltigkeit())
			.toString();
	}
}
