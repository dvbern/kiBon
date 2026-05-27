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

package ch.dvbern.ebegu.dto.neskovanp;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.github.threetenjaxb.core.LocalDateXmlAdapter;

@XmlRootElement(name = "kibonAnfrage")
@XmlAccessorType(XmlAccessType.FIELD)
public class KibonAnfrageDTO implements Serializable {

	private static final long serialVersionUID = 2971943436130318542L;

	@NotNull
	private String antragId = null;

	@Nullable
	private Integer zpvNummer;

	@Nullable
	private Long ahvNummer;

	@NotNull
	private int gesuchsperiodeBeginnJahr;

	@NotNull
	@XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
	private LocalDate geburtsdatum = null;

	public String getAntragId() {
		return antragId;
	}

	public void setAntragId(String antragId) {
		this.antragId = antragId;
	}

	@Nullable
	public Integer getZpvNummer() {
		return zpvNummer;
	}

	public void setZpvNummer(@Nullable Integer zpvNummer) {
		this.zpvNummer = zpvNummer;
	}

	@Nullable
	public Long getAhvNummer() {
		return ahvNummer;
	}

	public void setAhvNummer(@Nullable Long ahvNummer) {
		this.ahvNummer = ahvNummer;
	}

	public int getGesuchsperiodeBeginnJahr() {
		return gesuchsperiodeBeginnJahr;
	}

	public void setGesuchsperiodeBeginnJahr(int gesuchsperiodeBeginnJahr) {
		this.gesuchsperiodeBeginnJahr = gesuchsperiodeBeginnJahr;
	}

	public LocalDate getGeburtsdatum() {
		return geburtsdatum;
	}

	public void setGeburtsdatum(LocalDate geburtsdatum) {
		this.geburtsdatum = geburtsdatum;
	}
}
