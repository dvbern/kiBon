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

package ch.dvbern.ebegu.api.dtos;

import java.time.LocalDate;

import javax.annotation.Nonnull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.github.threetenjaxb.core.LocalDateXmlAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class JaxInternePendenz extends JaxAbstractDTO {

	private static final long serialVersionUID = 101944277874804704L;

	@Nonnull
	private JaxGesuch gesuch;

	@Nonnull
	@XmlJavaTypeAdapter(LocalDateXmlAdapter.class)
	private LocalDate termin;

	@Nonnull
	private String text;

	@Nonnull
	private Boolean erledigt;

	@Nonnull
	public JaxGesuch getGesuch() {
		return gesuch;
	}

	public void setGesuch(@Nonnull JaxGesuch gesuch) {
		this.gesuch = gesuch;
	}

	@Nonnull
	public LocalDate getTermin() {
		return termin;
	}

	public void setTermin(@Nonnull LocalDate termin) {
		this.termin = termin;
	}

	@Nonnull
	public String getText() {
		return text;
	}

	public void setText(@Nonnull String text) {
		this.text = text;
	}

	@Nonnull
	public Boolean getErledigt() {
		return erledigt;
	}

	public void setErledigt(@Nonnull Boolean erledigt) {
		this.erledigt = erledigt;
	}
}
