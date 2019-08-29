/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.ModulTagesschuleName;
import ch.dvbern.ebegu.validators.CheckTimeRange;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

/**
 * Entity for the Module of the Tageschulangebote.
 */
@CheckTimeRange
@Audited
@Entity
public class ModulTagesschule extends AbstractMutableEntity implements Comparable<ModulTagesschule> {

	private static final long serialVersionUID = -8403411439182708718L;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_modul_tagesschule_inst_stammdaten_tagesschule_id"), nullable = false)
	private InstitutionStammdatenTagesschule institutionStammdatenTagesschule;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(updatable = false, foreignKey = @ForeignKey(name = "FK_modul_tagesschule_gesuchsperiode_id"))
	private Gesuchsperiode gesuchsperiode;

	@Enumerated(value = EnumType.STRING)
	@NotNull @Nonnull
	@Column(nullable = false)
	private DayOfWeek wochentag;

	@Enumerated(value = EnumType.STRING)
	@NotNull @Nonnull
	@Column(nullable = false)
	private ModulTagesschuleName modulTagesschuleName;

	@NotNull @Nonnull
	@Column(nullable = false)
	private LocalTime zeitVon;

	@NotNull @Nonnull
	@Column(nullable = false)
	private LocalTime zeitBis;

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof ModulTagesschule)) {
			return false;
		}
		final ModulTagesschule otherModulTagesschule = (ModulTagesschule) other;
		return getModulTagesschuleName() == otherModulTagesschule.getModulTagesschuleName() &&
			getWochentag() == otherModulTagesschule.getWochentag() &&
			Objects.equals(getZeitVon(), otherModulTagesschule.getZeitVon()) &&
			Objects.equals(getZeitBis(), otherModulTagesschule.getZeitBis());
	}

	@Nonnull
	public DayOfWeek getWochentag() {
		return wochentag;
	}

	public void setWochentag(@Nonnull DayOfWeek wochentag) {
		this.wochentag = wochentag;
	}

	@Nonnull
	public ModulTagesschuleName getModulTagesschuleName() {
		return modulTagesschuleName;
	}

	public void setModulTagesschuleName(@Nonnull ModulTagesschuleName modulname) {
		this.modulTagesschuleName = modulname;
	}

	@Nonnull
	public LocalTime getZeitVon() {
		return zeitVon;
	}

	public void setZeitVon(@Nonnull LocalTime zeitVon) {
		this.zeitVon = zeitVon;
	}

	@Nonnull
	public LocalTime getZeitBis() {
		return zeitBis;
	}

	public void setZeitBis(@Nonnull LocalTime zeitBis) {
		this.zeitBis = zeitBis;
	}

	@Nonnull
	public InstitutionStammdatenTagesschule getInstitutionStammdatenTagesschule() {
		return institutionStammdatenTagesschule;
	}

	public void setInstitutionStammdatenTagesschule(@Nonnull InstitutionStammdatenTagesschule instStammdaten) {
		this.institutionStammdatenTagesschule = instStammdaten;
	}

	@Nonnull
	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Override
	public int compareTo(@Nonnull ModulTagesschule o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getInstitutionStammdatenTagesschule(), o.getInstitutionStammdatenTagesschule());
		builder.append(this.getGesuchsperiode(), o.getGesuchsperiode());
		builder.append(this.getZeitVon(), o.getZeitVon());
		builder.append(this.getZeitBis(), o.getZeitBis());
		builder.append(this.getWochentag(), o.getWochentag());
		builder.append(this.getModulTagesschuleName(), o.getModulTagesschuleName());
		return builder.toComparison();
	}
}
