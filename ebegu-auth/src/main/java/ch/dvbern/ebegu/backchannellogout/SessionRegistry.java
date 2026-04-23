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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.ejb.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

/**
 * Tracks all user sessions and provides methods for identifying and accessing them.
 */
@Slf4j
@ApplicationScoped
public class SessionRegistry {

	/**
	 * Represents the default maximum age, in hours, for a user session.
	 * This constant is used to define the duration after which an active session
	 * expires if it remains idle or reaches its lifecycle limit.
	 */
	private static final int DEFAULT_SESSION_MAX_AGE_IN_HOURS = 24;

	/**
	 * Stores all user sessions by subject (which is a unique identifier for users).
	 */
	private static final Map<String, Map<LocalDateTime, HttpSession>> USER_SESSIONS =
		new ConcurrentHashMap<>();

	/**
	 * Adds the given session to this registry.
	 *
	 * @param session The session to be added. Duplicates will be ignored.
	 * @param subject The subject (which is a unique identifier for users) to add the given session to.
	 */
	public void registerSession(HttpSession session, String subject) {

		LocalDateTime sessionCreated =
			LocalDateTime.ofInstant(
				Instant.ofEpochMilli(session.getCreationTime()),
				ZoneId.systemDefault()
			);

		USER_SESSIONS.computeIfAbsent(
			subject,
			subjectAsKey -> new ConcurrentHashMap<>()
		).computeIfAbsent(sessionCreated, sessionCreatedAsKey -> session);

		session.setAttribute("subject", subject);
	}

	/**
	 * Terminates all sessions that belong to the given subject.
	 *
	 * @param subject The subject (which is a unique identifier for users) to terminate all sessions for.
	 * @return The amount of sessions that were identified and invalidated. Returns 0 if the given subject does not
	 * relate to
	 * any session.
	 */
	public int logoutBySubject(String subject) {

		Map<LocalDateTime, HttpSession> sessions = USER_SESSIONS.remove(
			subject
		);
		if (sessions != null) {
			for (HttpSession session : sessions.values()) {
				session.invalidate();
			}
		}
		int sessionsSize = 0;
		if (sessions != null) {
			sessionsSize = sessions.size();
		}
		return sessionsSize;

	}

	@Schedule(info = "clean up session registry",
		second = "0",
		minute = "0",
		hour = "0",
		persistent = false)
	public void scheduledCleanUp() {
		removeSessionsByAge(DEFAULT_SESSION_MAX_AGE_IN_HOURS);
	}

	/**
	 * Removes all sessions from cache, that exceed the given max age.
	 *
	 * @param maxAgeHours The max age for sessions to be cached. Sessions exceeding this age will be removed by this
	 * method.
	 */
	public void removeSessionsByAge(int maxAgeHours) {

		// delete all Sessions below that.
		LocalDateTime maxDate = LocalDateTime.now().minusHours(maxAgeHours);

		USER_SESSIONS.values().forEach(localDateTimeHttpSessionMap -> {
			Set<LocalDateTime> invalidatedKeys = new HashSet<>();
			localDateTimeHttpSessionMap.forEach((k, v) -> {
				if (k.isBefore(maxDate)) {
					invalidatedKeys.add(k);
					try {
						v.invalidate(); // invalidate just in case this has not happened yet.
					} catch (IllegalStateException e) {
						// the session is already invalidated, we don't care
					} catch (Exception e) {
						LOG.error("A session could not be invalidated.", e);
					}
				}
			});
			invalidatedKeys.forEach(localDateTimeHttpSessionMap::remove);
		});

		Set<String> emtpySubjects = new HashSet<>();
		USER_SESSIONS.forEach((k, v) -> {
			if (v.isEmpty()) {
				emtpySubjects.add(k);
			}
		});

		emtpySubjects.forEach(USER_SESSIONS::remove);
	}

	/**
	 * Removes all sessions from the cache.
	 */
	public void clear() {
		USER_SESSIONS.clear();
	}

	/**
	 * Retrieves all active HTTP sessions associated with the given subject.
	 *
	 * @param subject The unique identifier for the user whose sessions are to be retrieved.
	 * @return A collection of {@link HttpSession} objects corresponding to the given subject. Returns an empty
	 * collection
	 * if no sessions are associated with the specified subject.
	 */
	public Collection<HttpSession> getSessionsFor(String subject) {
		return USER_SESSIONS.get(subject).values();
	}
}
