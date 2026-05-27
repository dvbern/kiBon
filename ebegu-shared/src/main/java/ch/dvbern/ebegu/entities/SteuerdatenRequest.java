/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import java.time.LocalDate;
import java.util.Objects;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;

@Entity
public class SteuerdatenRequest extends AbstractEntity {

	private static final long serialVersionUID = -7036966734761773954L;

	@Nullable
	@Column(nullable = true)
	private Integer zpvNummer;

	@Nullable
	@Column(nullable = true)
	private Long ahvNummer;

	@NotNull
	@Column(nullable = false)
	private LocalDate geburtsdatumAntragsteller;

	@NotNull
	@Column(nullable = false)
	private String antragId;

	@NotNull
	@Column(nullable = false)
	private int gesuchsperiodeBeginnJahr;

	public SteuerdatenRequest() {
	}

	public SteuerdatenRequest(
		@Nullable Integer zpvNummer,
		@Nullable Long ahvNummer,
		LocalDate geburtsdatumAntragsteller,
		String antragId,
		int gesuchsperiodeBeginnJahr
	) {
		this.zpvNummer = zpvNummer;
		this.ahvNummer = ahvNummer;
		this.geburtsdatumAntragsteller = geburtsdatumAntragsteller;
		this.antragId = antragId;
		this.gesuchsperiodeBeginnJahr = gesuchsperiodeBeginnJahr;
	}

	@Nullable
	public Integer getZpvNummer() {
		return zpvNummer;
	}

	public void setZpvNummer(Integer zpvNummer) {
		this.zpvNummer = zpvNummer;
	}

	@Nullable
	public Long getAhvNummer() {
		return ahvNummer;
	}

	public void setAhvNummer(Long ahvNummer) {
		this.ahvNummer = ahvNummer;
	}

	public int getGesuchsperiodeBeginnJahr() {
		return gesuchsperiodeBeginnJahr;
	}

	public void setGesuchsperiodeBeginnJahr(int gesuchsperiodeBeginnJahr) {
		this.gesuchsperiodeBeginnJahr = gesuchsperiodeBeginnJahr;
	}

	public String getAntragId() {
		return antragId;
	}

	public void setAntragId(String anfrage_id) {
		this.antragId = anfrage_id;
	}

	public LocalDate getGeburtsdatumAntragsteller() {
		return geburtsdatumAntragsteller;
	}

	public void setGeburtsdatumAntragsteller(
		LocalDate geburtsdatumAntragsteller
	) {
		this.geburtsdatumAntragsteller = geburtsdatumAntragsteller;
	}

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
		SteuerdatenRequest otherRequest = (SteuerdatenRequest) other;
		return this.geburtsdatumAntragsteller.equals(
			otherRequest.geburtsdatumAntragsteller
		)
			&&
			StringUtils.equals(this.antragId, otherRequest.antragId)
			&&
			this.gesuchsperiodeBeginnJahr
				== otherRequest.gesuchsperiodeBeginnJahr
			&&
			Objects.equals(this.zpvNummer, otherRequest.zpvNummer)
			&&
			Objects.equals(this.ahvNummer, otherRequest.ahvNummer);
	}
}
