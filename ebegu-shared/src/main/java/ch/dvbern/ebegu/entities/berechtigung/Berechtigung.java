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

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractDateRangedEntity;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.listener.BerechtigungChangedEntityListener;
import ch.dvbern.ebegu.validators.CheckBerechtigungGemeinde;
import ch.dvbern.ebegu.validators.CheckBerechtigungInstitutionTraegerschaft;
import ch.dvbern.ebegu.validators.CheckBerechtigungSozialdienst;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

@Entity
@EntityListeners(BerechtigungChangedEntityListener.class)
@Audited
@CheckBerechtigungInstitutionTraegerschaft
@CheckBerechtigungGemeinde
@CheckBerechtigungSozialdienst
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Berechtigung extends AbstractDateRangedEntity implements
	BerechtigungAccessible,
	Comparable<Berechtigung> {

	private static final long serialVersionUID = 6372688971894279665L;

	private static final String NULL = "null";

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Berechtigung_benutzer_id"))
	private Benutzer benutzer = null;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	private UserRole role = null;

	@NotNull
	@ManyToMany
	@JoinTable(
		joinColumns = @JoinColumn(name = "berechtigung_id",
			nullable = false),
		inverseJoinColumns = @JoinColumn(name = "gemeinde_id",
			nullable = false),
		foreignKey = @ForeignKey(
			name = "FK_berechtigung_gemeinde_gemeinde_id"),
		inverseForeignKey = @ForeignKey(
			name = "FK_berechtigung_gemeinde_berechtigung_id"),
		indexes = {
			@Index(name = "IX_berechtigung_gemeinde_berechtigung_id",
				columnList = "berechtigung_id"),
			@Index(name = "IX_berechtigung_gemeinde_gemeinde_id",
				columnList = "gemeinde_id"),
		}
	)
	private Set<Gemeinde> gemeindeList = new TreeSet<>();

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(
		name = "FK_Berechtigung_institution_id"))
	private Institution institution = null;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(
		name = "FK_Berechtigung_traegerschaft_id"))
	private Traegerschaft traegerschaft = null;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(
		name = "FK_berechtigung_sozialdienst_id"))
	private Sozialdienst sozialdienst = null;

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final Berechtigung otherBerechtigung = (Berechtigung) other;
		return Objects.equals(getBenutzer(), otherBerechtigung.getBenutzer())
			&& getRole() == otherBerechtigung.getRole()
			&& Objects.equals(
				getInstitution(),
				otherBerechtigung.getInstitution()
			)
			&& Objects.equals(
				getTraegerschaft(),
				otherBerechtigung.getTraegerschaft()
			)
			&& Objects.equals(
				getGueltigkeit(),
				otherBerechtigung.getGueltigkeit()
			)
			&& Objects.equals(
				getSozialdienst(),
				otherBerechtigung.getSozialdienst()
			);
	}

	@Override
	public int compareTo(@Nonnull Berechtigung o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(
			this.getGueltigkeit().getGueltigAb(),
			o.getGueltigkeit().getGueltigAb()
		);
		builder.append(this.getId(), o.getId());
		return builder.toComparison();
	}

	public boolean isGueltig() {
		return getGueltigkeit().contains(LocalDate.now());
	}

	public boolean isAbgelaufen() {
		return getGueltigkeit().endsBefore(LocalDate.now());
	}

	@Nonnull
	public String extractGemeindenForBerechtigungAsString() {
		return getGemeindeList()
			.stream()
			.map(Gemeinde::getName)
			.sorted(String::compareToIgnoreCase)
			.collect(Collectors.joining(", "));
	}

	@Override
	public String toString() {
		return "Berechtigung{"
			+
			"benutzer="
			+ (benutzer != null ? benutzer.getUsername() : NULL)
			+
			", role="
			+ role
			+
			", institution="
			+ (institution != null ? institution.getName() : NULL)
			+
			", traegerschaft="
			+ (traegerschaft != null ? traegerschaft.getName() : NULL)
			+
			", gueltigkeit="
			+ getGueltigkeit().toString()
			+
			", sozialdienst="
			+ (sozialdienst != null ? sozialdienst.getName() : NULL)
			+
			", gemeindeList="
			+ extractGemeindenForBerechtigungAsString()
			+
			'}';
	}

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
}
