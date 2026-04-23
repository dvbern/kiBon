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

import java.util.Collection;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionRegistryTest {

	@InjectMocks
	SessionRegistry sessionRegistry;

	@BeforeEach
	void setUp() {

		sessionRegistry.clear();
	}

	@Test
	void logoutBySubject() {

		String subject1 = "abc";
		String subject2 = "def";
		long session1Created = 60000;
		long session2Created = 120000;
		long session3Created = 180000;
		HttpSession session1 = Mockito.mock(HttpSession.class);
		Mockito.when(session1.getCreationTime()).thenReturn(session1Created);
		HttpSession session2 = Mockito.mock(HttpSession.class);
		Mockito.when(session2.getCreationTime()).thenReturn(session2Created);
		HttpSession session3 = Mockito.mock(HttpSession.class);
		Mockito.when(session3.getCreationTime()).thenReturn(session3Created);

		sessionRegistry.registerSession(session1, subject1);
		sessionRegistry.registerSession(session2, subject1);
		sessionRegistry.registerSession(session3, subject2);

		Mockito.verify(session1).setAttribute("subject", subject1);
		Mockito.verify(session2).setAttribute("subject", subject1);
		Mockito.verify(session3).setAttribute("subject", subject2);

		int sessionsSize1 = sessionRegistry.logoutBySubject(subject1);
		int sessionsSize2 = sessionRegistry.logoutBySubject(subject2);

		Mockito.verify(session1).invalidate();
		Mockito.verify(session2).invalidate();
		Mockito.verify(session3).invalidate();

		Assertions.assertEquals(2, sessionsSize1);
		Assertions.assertEquals(1, sessionsSize2);
	}

	@Test
	void removeSessionsByAge() {

		String subject1 = "abc";
		String subject2 = "def";
		long session1Created = System.currentTimeMillis();
		long session2Created = System.currentTimeMillis() - 5 * 60 * 60 * 1000;
		long session3Created = System.currentTimeMillis() - 2 * 60 * 60 * 1000;
		HttpSession session1 = Mockito.mock(HttpSession.class);
		Mockito.when(session1.getCreationTime()).thenReturn(session1Created);
		HttpSession session2 = Mockito.mock(HttpSession.class);
		Mockito.when(session2.getCreationTime()).thenReturn(session2Created);
		HttpSession session3 = Mockito.mock(HttpSession.class);
		Mockito.when(session3.getCreationTime()).thenReturn(session3Created);

		sessionRegistry.registerSession(session1, subject1);
		sessionRegistry.registerSession(session2, subject1);
		sessionRegistry.registerSession(session3, subject2);

		sessionRegistry.removeSessionsByAge(3);

		Collection<HttpSession> sub1Sessions = sessionRegistry.getSessionsFor(
			subject1
		);
		Collection<HttpSession> sub2Sessions = sessionRegistry.getSessionsFor(
			subject2
		);

		Assertions.assertEquals(1, sub1Sessions.size());
		Assertions.assertEquals(session1, sub1Sessions.iterator().next());

		Assertions.assertEquals(1, sub2Sessions.size());
		Assertions.assertEquals(session3, sub2Sessions.iterator().next());
	}
}
