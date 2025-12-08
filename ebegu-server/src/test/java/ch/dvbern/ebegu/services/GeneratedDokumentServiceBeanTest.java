/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.lang.reflect.Field;

import jakarta.activation.MimeType;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

import ch.dvbern.ebegu.entities.GeneratedDokument;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.GeneratedDokumentTyp;
import ch.dvbern.ebegu.file.FileSaverService;
import ch.dvbern.ebegu.util.UploadFileInfo;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

@ExtendWith(EasyMockExtension.class)
class GeneratedDokumentServiceBeanTest extends EasyMockSupport {

	@TestSubject
	private GeneratedDokumentServiceBean generatedDokumentService =
		new GeneratedDokumentServiceBean();
	@Mock
	private FileSaverService fileSaverService;
	@Mock
	private TransactionSynchronizationRegistry transactionRegistry;

	@Test
	void testTransactionRollback() throws Exception {
		// Mock findDokument in tested Bean
		generatedDokumentService = createMockBuilder(
			GeneratedDokumentServiceBean.class
		)
			.addMockedMethod("findGeneratedDokument")
			.addMockedMethod("saveDokument")
			.createMock();
		injectPrivateFieldIntoMockedBean(
			generatedDokumentService,
			"fileSaverService",
			fileSaverService
		);
		injectPrivateFieldIntoMockedBean(
			generatedDokumentService,
			"transactionSynchronizationRegistry",
			transactionRegistry
		);
		expect(
			generatedDokumentService.findGeneratedDokument(
				anyString(),
				anyString()
			)
		).andReturn(new GeneratedDokument());
		// Setup mocks
		UploadFileInfo uploadFileInfo = new UploadFileInfo(
			"tempFileName",
			new MimeType("text/pdf")
		);
		expect(
			fileSaverService.save(
				anyObject(byte[].class),
				anyString(),
				anyString()
			)
		).andReturn(uploadFileInfo);
		expect(fileSaverService.remove("")).andReturn(true);
		// Simulate a runtime Error by persisting the dokument object in database
		generatedDokumentService.saveDokument(anyObject());
		expectLastCall().andThrow(
			new RuntimeException("Simulated Transaction error")
		);

		replay(fileSaverService, generatedDokumentService, transactionRegistry);

		// Call the method under test
		try {
			generatedDokumentService.saveGeneratedDokumentInDB(
				new byte[0],
				GeneratedDokumentTyp.VERFUEGUNG,
				new Gesuch(),
				"fileName",
				true
			);
		} catch (Exception e) {
			// Exception is expected
		}

		// Verify rollback handling
		verify(fileSaverService, transactionRegistry);
	}

	@Test
	void testTransactionCommit() throws Exception {
		// Mock findDokument in tested Bean
		generatedDokumentService = createMockBuilder(
			GeneratedDokumentServiceBean.class
		)
			.addMockedMethod("findGeneratedDokument")
			.addMockedMethod("saveDokument")
			.createMock();
		injectPrivateFieldIntoMockedBean(
			generatedDokumentService,
			"fileSaverService",
			fileSaverService
		);
		injectPrivateFieldIntoMockedBean(
			generatedDokumentService,
			"transactionSynchronizationRegistry",
			transactionRegistry
		);
		expect(
			generatedDokumentService.findGeneratedDokument(
				anyString(),
				anyString()
			)
		).andReturn(new GeneratedDokument());
		// Setup mocks
		UploadFileInfo uploadFileInfo = new UploadFileInfo(
			"tempFileName",
			new MimeType("text/pdf")
		);
		uploadFileInfo.setSize(2L);
		expect(
			fileSaverService.save(
				anyObject(byte[].class),
				anyString(),
				anyString()
			)
		).andReturn(uploadFileInfo);
		// Setup transaction synchronization for commit
		transactionRegistry.registerInterposedSynchronization(
			anyObject(Synchronization.class)
		);

		expectLastCall().andAnswer(() -> {
			Synchronization synchronization =
				(Synchronization) getCurrentArguments()[0];
			synchronization.afterCompletion(Status.STATUS_COMMITTED);
			return null;
		});
		expect(fileSaverService.remove("")).andReturn(true);
		expect(generatedDokumentService.saveDokument(anyObject())).andReturn(
			new GeneratedDokument()
		);
		replay(fileSaverService, generatedDokumentService, transactionRegistry);

		// Call the method under test
		generatedDokumentService.saveGeneratedDokumentInDB(
			new byte[0],
			GeneratedDokumentTyp.VERFUEGUNG,
			new Gesuch(),
			"fileName",
			true
		);

		// Verify old file deletion on commit
		fileSaverService.remove("");
		verify(fileSaverService, transactionRegistry);
	}

	private void injectPrivateFieldIntoMockedBean(
		Object targetObject,
		String fieldName,
		Object fieldValue
	) throws Exception {
		Field field = targetObject.getClass()
			.getSuperclass()
			.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(targetObject, fieldValue);
	}
}
