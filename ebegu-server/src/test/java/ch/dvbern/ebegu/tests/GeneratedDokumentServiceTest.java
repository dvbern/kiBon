package ch.dvbern.ebegu.tests;

import java.time.LocalDate;
import java.time.Month;

import javax.activation.MimeTypeParseException;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.GeneratedDokument;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.WriteProtectedDokument;
import ch.dvbern.ebegu.enums.GeneratedDokumentTyp;
import ch.dvbern.ebegu.services.GeneratedDokumentService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testet GeneratedDokumentService
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class GeneratedDokumentServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private GeneratedDokumentService generatedDokumentService;
	@Inject
	private InstitutionService instService;
	@Inject
	private Persistence persistence;

	@Test
	public void findGeneratedDokumentTest() {
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(instService, persistence, LocalDate.of(1980, Month.MARCH, 25));
		final GeneratedDokument dokument = TestDataUtil.createGeneratedDokument(gesuch);
		persistence.persist(dokument);

		generatedDokumentService.saveDokument(dokument);

		final WriteProtectedDokument generatedDokument = generatedDokumentService.findGeneratedDokument(gesuch.getId(), dokument.getFilename());

		Assert.assertNotNull(generatedDokument);
		Assert.assertEquals(dokument, generatedDokument);
	}

	@Test
	public void updateGeneratedDokumentTest() throws MimeTypeParseException {
		Gesuch gesuch = TestDataUtil.createAndPersistWaeltiDagmarGesuch(instService, persistence, LocalDate.of(1980, Month.MARCH, 25));
		final GeneratedDokument dokument = TestDataUtil.createGeneratedDokument(gesuch);
		persistence.persist(dokument);

		generatedDokumentService.saveDokument(dokument);

		byte[] data = new byte[0];
		final String newFileName = "Newname.pdf";
		final GeneratedDokument generatedDokument = (GeneratedDokument) generatedDokumentService
			.saveGeneratedDokumentInDB(data, GeneratedDokumentTyp.BEGLEITSCHREIBEN, gesuch, newFileName, false);

		Assert.assertNotNull(generatedDokument);
		Assert.assertEquals(newFileName, generatedDokument.getFilename());
	}
}
