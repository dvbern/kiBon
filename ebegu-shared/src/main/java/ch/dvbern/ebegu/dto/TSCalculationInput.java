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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.util.MathUtil;

public class TSCalculationInput {

	@NotNull
	@Nonnull
	private Integer betreuungszeitProWoche = 0;

	@NotNull
	@Nonnull
	private BigDecimal verpflegungskosten = BigDecimal.ZERO;

	public TSCalculationInput() {
	}

	public TSCalculationInput(@Nonnull TSCalculationInput other) {
		this.betreuungszeitProWoche = other.betreuungszeitProWoche;
		this.verpflegungskosten = other.verpflegungskosten;
	}

	public void add(@Nonnull TSCalculationInput other) {
		this.betreuungszeitProWoche = this.betreuungszeitProWoche + other.betreuungszeitProWoche;
		this.verpflegungskosten = MathUtil.DEFAULT.addNullSafe(this.verpflegungskosten, other.verpflegungskosten);
	}

	public boolean isSame(@Nonnull TSCalculationInput other) {
		return Objects.equals(this.betreuungszeitProWoche, other.betreuungszeitProWoche)
			&& MathUtil.isSame(this.verpflegungskosten, other.verpflegungskosten);
	}

	@Nonnull
	public TSCalculationInput copy() {
		return new TSCalculationInput(this);
	}

	public boolean shouldCalculate() {
		return betreuungszeitProWoche > 0;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", TSCalculationInput.class.getSimpleName() + '[', "]")
			.add("betreuungszeitProWoche=" + betreuungszeitProWoche)
			.add("verpflegungskosten=" + verpflegungskosten)
			.toString();
	}

	@Nonnull
	public Integer getBetreuungszeitProWoche() {
		return betreuungszeitProWoche;
	}

	public void setBetreuungszeitProWoche(@Nonnull Integer betreuungszeitProWoche) {
		this.betreuungszeitProWoche = betreuungszeitProWoche;
	}

	@Nonnull
	public BigDecimal getVerpflegungskosten() {
		return verpflegungskosten;
	}

	public void setVerpflegungskosten(@Nonnull BigDecimal verpflegungskosten) {
		this.verpflegungskosten = verpflegungskosten;
	}
}
