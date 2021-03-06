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

import java.io.Serializable;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * This Transfer Object is used to transfer the external user together with messages (error)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxBenutzerResponseWrapper implements Serializable {

	private static final long serialVersionUID = 3338519549993818460L;

	@Nullable
	private String errorMessage = null;

	@Nullable
	private JaxExternalBenutzer benutzer = null;

	private boolean storedBenutzerGesperrt;

	@Nullable
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(@Nullable String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Nullable
	public JaxExternalBenutzer getBenutzer() {
		return benutzer;
	}

	public void setBenutzer(@Nullable JaxExternalBenutzer benutzer) {
		this.benutzer = benutzer;
	}

	public boolean isStoredBenutzerGesperrt() {
		return storedBenutzerGesperrt;
	}

	public void setStoredBenutzerGesperrt(boolean storedBenutzerGesperrt) {
		this.storedBenutzerGesperrt = storedBenutzerGesperrt;
	}
}
