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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * Repräsentiert den Inhalt der Antwort, die nach Ausführen des Logouts vom Backend geschickt wird.
 */
export class LogoutResponse {
    /**
     * Das Ziel, auf das der Client umleiten soll (z.B. URL auf BE-Login).
     */
    public logoutRedirect: string;

    /**
     * Ob der Logout im Backend erfolgreich durchgeführt werden konnte.
     */
    public logoutSuccess: boolean;

    /**
     * Ob der Client selbst entscheiden darf, wohin er nach dem Logout weiterleitet.
     */
    public useDefaultLogoutRedirect: boolean;

    /**
     * Zusätzliche Nachricht für die Weiterverarbeitung (z.B. im Fall eines Fehlers).
     */
    public message: string;

    public constructor(
        logoutUrl?: string,
        logoutSuccess?: boolean,
        message?: string,
        useDefaultLogoutRedirect?: boolean
    ) {
        this.logoutRedirect = logoutUrl;
        this.logoutSuccess = logoutSuccess;
        this.useDefaultLogoutRedirect = useDefaultLogoutRedirect;
        this.message = message;
    }

    /**
     * Ob eine zusätzliche Nachricht für die Weiterverarbeitung existgiert (z.B. im Fall eines Fehlers).
     */
    public hasMessage(): boolean {
        return (
            null !== this.message &&
            undefined !== this.message &&
            this.message.length > 0
        );
    }
}
