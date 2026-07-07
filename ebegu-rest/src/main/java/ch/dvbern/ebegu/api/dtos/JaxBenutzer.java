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
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.enums.BenutzerStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement(name = "benutzer")
public class JaxBenutzer implements Serializable {

	private static final long serialVersionUID = 2769899329796452129L;

	@Nonnull
	private String username = "";

	@Nullable
	private String externalUUID = "";

	@Nonnull
	private String password = "";

	@Nonnull
	private String nachname = "";

	@Nonnull
	private String vorname = "";

	@Nonnull
	private String email = "";

	private JaxMandant mandant;

	@Nonnull
	private BenutzerStatus status = BenutzerStatus.GESPERRT;

	@Nullable
	private JaxBerechtigung currentBerechtigung;

	@Nonnull
	private Set<JaxBerechtigung> berechtigungen = new LinkedHashSet<>();

	@Nonnull
	private boolean sendMailWennOffenePendenzen;

	/**
	 * evaluates current berechtigung and sets it to the object
	 */
	public void evaluateCurrentBerechtigung() {
		if (getCurrentBerechtigung() == null) {
			for (JaxBerechtigung berechtigung : getBerechtigungen()) {
				if (berechtigung.isGueltig()) {
					setCurrentBerechtigung(berechtigung);
					return;
				}
			}
		}
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof JaxBenutzer)) {
			return false;
		}
		JaxBenutzer other = (JaxBenutzer) o;
		//noinspection NonFinalFieldReferenceInEquals
		return Objects.equals(this.getUsername(), other.getUsername())
			&& Objects.equals(this.getEmail(), other.getEmail());
	}

	@Override
	public int hashCode() {
		return super.hashCode()
			+ Objects.hashCode(this.getUsername())
			+ Objects.hashCode(this.getEmail());
	}
}
