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

package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Size;

import ch.dvbern.ebegu.dto.VerfuegungsBemerkungDTO;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.envers.Audited;
import org.jetbrains.annotations.NotNull;

/**
 * Dieses Objekt repraesentiert eine Bemerkung eines Zeitabschnitts wahrend eines Betreeungsgutscheinantrags
 */
@Entity
@Audited
public class VerfuegungZeitabschnittBemerkung extends AbstractDateRangedEntity {

	private static final long serialVersionUID = 4621569356897563374L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(
		name = "FK_verfuegung_zeitabschnitt_bemerkung_zeitabschnitt_id"),
		nullable = false)
	private VerfuegungZeitabschnitt verfuegungZeitabschnitt;

	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	@NotNull
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkung = "";

	public VerfuegungZeitabschnittBemerkung() {
	}

	public VerfuegungZeitabschnittBemerkung(
		@Nonnull VerfuegungsBemerkungDTO bemerkung,
		VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		Mandant mandant
	) {
		this.bemerkung = bemerkung.getTranslated(
			mandant,
			extractGemeinde(verfuegungZeitabschnitt)
		);
		this.verfuegungZeitabschnitt = verfuegungZeitabschnitt;
		DateRange gueltig =
			bemerkung.getGueltigkeit() != null ?
				bemerkung.getGueltigkeit() :
				verfuegungZeitabschnitt.getGueltigkeit();
		this.setGueltigkeit(gueltig);
	}

	@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
	private @Nullable Gemeinde extractGemeinde(
		VerfuegungZeitabschnitt verfuegungZeitabschnitt
	) {
		if (verfuegungZeitabschnitt.getVerfuegung() == null) {
			return null;
		}
		if (verfuegungZeitabschnitt.getVerfuegung().getBetreuung() != null) {
			return verfuegungZeitabschnitt.getVerfuegung()
				.getBetreuung()
				.extractGemeinde();
		} else if (verfuegungZeitabschnitt.getVerfuegung()
			.getAnmeldungTagesschule()
			!= null) {
			return verfuegungZeitabschnitt.getVerfuegung()
				.getAnmeldungTagesschule()
				.extractGemeinde();
		}
		return null;
	}

	@Override
	@SuppressWarnings({ "OverlyComplexBooleanExpression", "OverlyComplexMethod",
		"PMD.CompareObjectsWithEquals" })
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}

		//noinspection ConstantConditions: Sonst motzt PMD
		if (!(other instanceof VerfuegungZeitabschnittBemerkung)) {
			return false;
		}
		final VerfuegungZeitabschnittBemerkung that =
			(VerfuegungZeitabschnittBemerkung) other;
		return StringUtils.equals(this.bemerkung, that.bemerkung)
			&&
			Objects.equals(
				this.verfuegungZeitabschnitt,
				that.verfuegungZeitabschnitt
			);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append(super.toString())
			.append("bemerkung", bemerkung)
			.append("verfügungZeitabschnitt", verfuegungZeitabschnitt)
			.toString();
	}

	public VerfuegungZeitabschnitt getVerfuegungZeitabschnitt() {
		return verfuegungZeitabschnitt;
	}

	public void setVerfuegungZeitabschnitt(
		VerfuegungZeitabschnitt verfuegungZeitabschnitt
	) {
		this.verfuegungZeitabschnitt = verfuegungZeitabschnitt;
	}

	@Nonnull
	public String getBemerkung() {
		return bemerkung;
	}

	public void setBemerkung(@Nonnull String bemerkungen) {
		this.bemerkung = bemerkungen;
	}
}
