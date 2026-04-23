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

package ch.dvbern.ebegu.backchannellogout;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

/**
 * Defines user logout events sent by Keycloak to the Identity Access Management (IAM) event queue. The body of those
 * events
 * should conform to this type.
 */
@Getter
@Setter
@Builder
@EqualsAndHashCode
@Jacksonized
public class LogoutEvent {

	/**
	 * The ID of the user session under which this session is known to and managed by Keycloak. It can be used to
	 * identify a
	 * single session. Note that a user may have multiple sessions alive in the kiBon servlet container. To terminat all
	 * sessions, it might be more safe to use {@link #subject}
	 */
	private String sessionId;

	/**
	 * The subject is a unique identifier for the user, managed by Keycloak. It may be used to identify all active
	 * sessions in
	 * the kiBon servlet container.
	 */
	private String subject;

	/**
	 * The reason why this event was sent. It is up to Keycloak what is beeing send here, but because kiBon user
	 * normally do
	 * not logout themselves via Keycloak UI, the most applicable reason in the kiBon context will be:
	 * {@link LogoutReason#PASSWORD_RESET}.
	 */
	private LogoutReason reason;

	/**
	 * The date and time when the Keycloak session was terminated.
	 */
	@EqualsAndHashCode.Exclude
	private LocalDateTime timestamp;

	public enum LogoutReason {
		PASSWORD_RESET, MANUAL_LOGOUT, SESSION_EXPIRED
	}
}
