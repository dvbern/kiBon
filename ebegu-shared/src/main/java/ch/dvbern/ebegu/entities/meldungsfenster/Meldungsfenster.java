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

package ch.dvbern.ebegu.entities.meldungsfenster;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.HasMandant;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.meldungsfenster.converters.MeldungsfensterRoleListConverter;
import ch.dvbern.ebegu.types.DateTimeRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;
import static ch.dvbern.ebegu.util.Constants.DB_TEXTAREA_LENGTH;

@Audited
@Entity
@EqualsAndHashCode(callSuper = true)
public class Meldungsfenster extends AbstractEntity implements HasMandant {

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(
		updatable = false,
		foreignKey = @ForeignKey(name = "FK_einstellung_mandant_id")
	)
	private Mandant mandant;

	@Convert(converter = MeldungsfensterRoleListConverter.class)
	@Nonnull
	@NotNull
	@Setter
	@Getter
	private List<MeldungsfensterRole> zielgruppe = new ArrayList<>();

	@NotNull
	@Nonnull
	@Embedded
	@Valid
	private DateTimeRange gueltigkeit = new DateTimeRange();

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	@Setter
	@Getter
	private MeldungsfensterStatus status;

	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	@NotNull
	private String titleDe;

	@Nullable
	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Setter
	@Getter
	private String titleFr;

	@Size(min = 1, max = DB_TEXTAREA_LENGTH)
	@Column(nullable = false, columnDefinition = "TEXT")
	@NotNull
	private String inhaltDe;

	@Nullable
	@Column(nullable = true,
		length = DB_TEXTAREA_LENGTH,
		columnDefinition = "TEXT")
	@Setter
	@Getter
	private String inhaltFr;

	@Nullable
	@Override
	public Mandant getMandant() {
		return mandant;
	}

	@Override
	public void setMandant(@Nullable Mandant mandant) {
		this.mandant = mandant;
	}

	@Nonnull
	public @NotNull
	@Valid DateTimeRange getGueltigkeit() {
		return gueltigkeit;
	}

	public void setGueltigkeit(
		@Nonnull @NotNull @Valid DateTimeRange gueltigkeit
	) {
		this.gueltigkeit = gueltigkeit;
	}

	public @Size(
		min = 1,
		max = DB_DEFAULT_MAX_LENGTH
	)
	@NotNull String getTitleDe() {
		return titleDe;
	}

	public void setTitleDe(
		@Size(
			min = 1,
			max = DB_DEFAULT_MAX_LENGTH
		) @NotNull String titleDe
	) {
		this.titleDe = titleDe;
	}

	public @Size(
		min = 1,
		max = DB_TEXTAREA_LENGTH
	)
	@NotNull String getInhaltDe() {
		return inhaltDe;
	}

	public void setInhaltDe(
		@Size(
			min = 1,
			max = DB_TEXTAREA_LENGTH
		) @NotNull String inhaltDe
	) {
		this.inhaltDe = inhaltDe;
	}

	@Override
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final Meldungsfenster otherMeldung = (Meldungsfenster) other;
		return Objects.equals(getGueltigkeit(), otherMeldung.getGueltigkeit())
			&&
			EbeguUtil.isSameOrNullStrings(
				getInhaltFr(),
				otherMeldung.getInhaltFr()
			)
			&&
			EbeguUtil.isSameOrNullStrings(
				getTitleFr(),
				otherMeldung.getTitleFr()
			)
			&&
			Objects.equals(getTitleDe(), otherMeldung.getTitleDe())
			&&
			Objects.equals(getInhaltDe(), otherMeldung.getInhaltDe())
			&&
			getStatus() == otherMeldung.getStatus()
			&&
			Objects.equals(getZielgruppe(), otherMeldung.getZielgruppe())
			&& Objects.equals(getMandant(), otherMeldung.getMandant());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.appendSuper(super.toString())
			.append("gueltigkeit: ", getGueltigkeit())
			.append("ZielGruppe: ", getZielgruppe())
			.append("Status: ", getStatus())
			.toString();
	}
}
