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

package ch.dvbern.ebegu.mailing;

import ch.dvbern.ebegu.persistence.Persistence;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(EasyMockRunner.class)
public class OutboxMailServiceTest extends EasyMockSupport {

	@TestSubject
	private final OutboxMailService outboxMailService = new OutboxMailService();

	@Mock
	private Persistence persistence;

	@Test
	public void findOutboxMailById_ValidId_DelegatedToPersistence() {
		String id = "123";
		OutboxMail expectedMail = new OutboxMail();
		expect(persistence.find(OutboxMail.class, id)).andReturn(expectedMail);

		replayAll();
		OutboxMail actualMail = outboxMailService.findOutboxMailById(id);
		verifyAll();

		assertEquals(expectedMail, actualMail);
	}

	@Test
	public void saveOutboxMail_NormalText_DelegatedToPersistence() {
		OutboxMail mail = new OutboxMail();
		mail.setContent("Normal content");
		expect(persistence.persist(mail)).andReturn(mail);

		replayAll();
		outboxMailService.saveOutboxMail(mail);
		verifyAll();

		assertEquals("Normal content", mail.getContent());
	}

	@Test
	public void saveOutboxMail_LongText_TextCutOffAndDelegatedToPersistence() {
		OutboxMail mail = new OutboxMail();
		StringBuilder longText = new StringBuilder();
		for (int i = 0; i < 4190001; i++) {
			longText.append("a");
		}
		mail.setContent(longText.toString());
		expect(persistence.persist(mail)).andReturn(mail);

		replayAll();
		outboxMailService.saveOutboxMail(mail);
		verifyAll();

		assertTrue(mail.getContent().length() > 4190000);
		assertTrue(
			mail.getContent()
				.contains("IMPORTANT NOTE: mail content was cut off")
		);
		assertEquals(4190000, mail.getContent().indexOf("... IMPORTANT NOTE:"));
	}

	@Test
	public void updateOutboxMail_NormalText_DelegatedToPersistence() {
		OutboxMail mail = new OutboxMail();
		mail.setContent("Normal content");
		expect(persistence.merge(mail)).andReturn(mail);

		replayAll();
		outboxMailService.udpateOutboxMail(mail);
		verifyAll();

		assertEquals("Normal content", mail.getContent());
	}

	@Test
	public void testUpdateOutboxMail_LongText_TextCutOffAndDelegatedToPersistence() {
		OutboxMail mail = new OutboxMail();
		StringBuilder longText = new StringBuilder();
		for (int i = 0; i < 4190001; i++) {
			longText.append("a");
		}
		mail.setContent(longText.toString());
		expect(persistence.merge(mail)).andReturn(mail);

		replayAll();
		outboxMailService.udpateOutboxMail(mail);
		verifyAll();

		assertTrue(mail.getContent().length() > 4190000);
		assertTrue(
			mail.getContent()
				.contains("IMPORTANT NOTE: mail content was cut off")
		);
		assertEquals(4190000, mail.getContent().indexOf("... IMPORTANT NOTE:"));
	}

	@Test
	public void remove__DelegatedToPersistence() {
		OutboxMail mail = new OutboxMail();
		persistence.remove(mail);

		replayAll();
		outboxMailService.remove(mail);
		verifyAll();
	}
}
