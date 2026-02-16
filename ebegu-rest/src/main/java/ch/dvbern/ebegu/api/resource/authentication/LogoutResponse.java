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

package ch.dvbern.ebegu.api.resource.authentication;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * Repräsentiert den Inhalt der Antwort, die dem HTTP-Client nach Ausführen des Logouts geschickt wird.
 */
@Getter
@Setter
public class LogoutResponse implements Serializable {

	private static final long serialVersionUID = 5537959946392017493L;

	/**
	 * Das Ziel, auf das der Client umleiten soll (z.B. URL auf BE-Login).
	 */
	private String logoutRedirect;

	/**
	 * Ob der Logout im Backend erfolgreich durchgeführt werden konnte.
	 */
	private boolean logoutSuccess;

	/**
	 * Ob der Client selbst entscheiden soll, wohin er nach dem Logout weiterleitet.
	 * {@link LogoutResponse#getLogoutRedirect()}
	 * darf dann vom Client irgoniert werden (muss aber nicht).
	 */
	private boolean useDefaultLogoutRedirect;
}
