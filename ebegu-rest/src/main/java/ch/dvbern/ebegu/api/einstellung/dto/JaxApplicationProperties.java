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

package ch.dvbern.ebegu.api.einstellung.dto;

import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO fuer Application Propertie
 */
@XmlRootElement(name = "property")
@XmlAccessorType(XmlAccessType.FIELD)
@Setter
@Getter
public class JaxApplicationProperties extends JaxAbstractDTO {

	private static final long serialVersionUID = -2243403693436143445L;
	@NotNull
	private String value = null;

	@NotNull
	private String name = null;

	@Nullable
	private String erklaerung;
}
