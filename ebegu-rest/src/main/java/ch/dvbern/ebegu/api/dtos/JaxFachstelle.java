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

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.enums.FachstelleName;

/**
 * DTO fuer Stammdaten der Fachstelle
 */
@XmlRootElement(name = "fachstelle")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxFachstelle extends JaxAbstractDTO {

	private static final long serialVersionUID = -1277026901764135397L;

	@NotNull
	private FachstelleName name;

	private boolean fachstelleAnspruch;

	private boolean fachstelleErweiterteBetreuung;

	public FachstelleName getName() {
		return name;
	}

	public void setName(FachstelleName name) {
		this.name = name;
	}

	public boolean isFachstelleAnspruch() {
		return fachstelleAnspruch;
	}

	public void setFachstelleAnspruch(boolean fachstelleAnspruch) {
		this.fachstelleAnspruch = fachstelleAnspruch;
	}

	public boolean isFachstelleErweiterteBetreuung() {
		return fachstelleErweiterteBetreuung;
	}

	public void setFachstelleErweiterteBetreuung(boolean fachstelleErweiterteBetreuung) {
		this.fachstelleErweiterteBetreuung = fachstelleErweiterteBetreuung;
	}
}
