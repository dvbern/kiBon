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

package ch.dvbern.ebegu.api.dtos;

import java.time.LocalDate;

import javax.annotation.Nullable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.github.threetenjaxb.core.LocalDateXmlAdapter;
import lombok.EqualsAndHashCode;

@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
@EqualsAndHashCode(callSuper = true)
public abstract class JaxAbstractDateRangedDTO extends JaxAbstractDTO {

	private static final long serialVersionUID = -2898194827432548948L;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
	private LocalDate gueltigAb = null;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
	private LocalDate gueltigBis = null;

	@Nullable
	public LocalDate getGueltigAb() {
		return gueltigAb;
	}

	public void setGueltigAb(@Nullable LocalDate gueltigAb) {
		this.gueltigAb = gueltigAb;
	}

	@Nullable
	public LocalDate getGueltigBis() {
		return gueltigBis;
	}

	public void setGueltigBis(@Nullable LocalDate gueltigBis) {
		this.gueltigBis = gueltigBis;
	}
}
