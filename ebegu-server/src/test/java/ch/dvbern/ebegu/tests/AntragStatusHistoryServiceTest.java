package ch.dvbern.ebegu.tests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AntragStatusHistory;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.AntragStatusHistoryService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Arquillian Tests fuer die Klasse AntragStatusHistoryService
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class AntragStatusHistoryServiceTest extends AbstractEbeguLoginTest {


	@Inject
	private AntragStatusHistoryService statusHistoryService;
	@Inject
	private Persistence persistence;
	@Inject
	private GesuchService gesuchService;

	private Gesuch gesuch;
	private Benutzer benutzerSuperAdmin;


	@Before
	public void setUp() {
		benutzerSuperAdmin = getDummySuperadmin(); // wir erstellen in superklasse schon einen superadmin
		gesuch = TestDataUtil.createAndPersistGesuch(persistence);
	}

	@Test
	public void saveChangesCurrentUser() {
		LocalDateTime time = LocalDateTime.now();
		final Benutzer benutzerJA = loginAsSachbearbeiterJA();
		final AntragStatusHistory createdStatusHistory = statusHistoryService.saveStatusChange(gesuch, null);

		assertSavedChanges(time, createdStatusHistory, benutzerJA);
	}

	@Test
	public void saveChangesAsOtherUser() {
		LocalDateTime time = LocalDateTime.now();
		loginAsSachbearbeiterJA();
		final AntragStatusHistory createdStatusHistory = statusHistoryService.saveStatusChange(gesuch, benutzerSuperAdmin);

		assertSavedChanges(time, createdStatusHistory, benutzerSuperAdmin);
	}

	private void assertSavedChanges(LocalDateTime time, AntragStatusHistory createdStatusHistory, Benutzer user) {
		Assert.assertNotNull(createdStatusHistory);
		Assert.assertEquals(gesuch.getStatus(), createdStatusHistory.getStatus());
		Assert.assertEquals(gesuch, createdStatusHistory.getGesuch());
		Assert.assertEquals(user, createdStatusHistory.getBenutzer());
		//just check that the generated date is after (or equals) the temporal one we created before
		Assert.assertTrue(time.isBefore(createdStatusHistory.getTimestampVon()) || time.isEqual(createdStatusHistory.getTimestampVon()));
	}

	/**
	 * Dieser Test gibt manchmal einen Fehler zurück, das der lastStatusChange FREIGABEQUITTUNG und nicht VERFUEGT ist.
	 * Das Problem könnte ein Timing Problem sein, da er zweimal den Status fast zur selben Zeit speichert.
	 */
	@Test
	@Ignore
	public void findLastStatusChangeTest() {
		gesuch.setStatus(AntragStatus.ERSTE_MAHNUNG);
		statusHistoryService.saveStatusChange(gesuch, null);
		gesuch.setStatus(AntragStatus.FREIGABEQUITTUNG);
		statusHistoryService.saveStatusChange(gesuch, null);
		gesuch.setStatus(AntragStatus.VERFUEGT);
		statusHistoryService.saveStatusChange(gesuch, null);
		final AntragStatusHistory lastStatusChange = statusHistoryService.findLastStatusChange(gesuch);
		Assert.assertNotNull(lastStatusChange);
		Assert.assertEquals(AntragStatus.VERFUEGT, lastStatusChange.getStatus());
	}

	@Test
	public void findLastStatusChangeNoChangeNullTest() {
		Assert.assertNull(statusHistoryService.findLastStatusChange(gesuch));
	}

	@Test
	public void  testFindAllAntragStatusHistoryByGPFall_NoFall() {
		Fall fall = TestDataUtil.createDefaultFall();
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718();

		final Collection<AntragStatusHistory> allStatus = statusHistoryService.findAllAntragStatusHistoryByGPFall(gesuchsperiode, fall);

		Assert.assertNotNull(allStatus);
		Assert.assertEquals(0, allStatus.size());
	}

	@Test
	public void  testFindAllAntragStatusHistoryByGPFall_NoChanges() {
		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence);

		final Collection<AntragStatusHistory> allStatus = statusHistoryService.findAllAntragStatusHistoryByGPFall(gesuch.getGesuchsperiode(), gesuch.getFall());

		Assert.assertNotNull(allStatus);
		Assert.assertEquals(0, allStatus.size());
	}

	@Test
	public void  testFindAllAntragStatusHistoryByGPFall() {
		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, AntragStatus.VERFUEGEN);
		gesuch.setStatus(AntragStatus.VERFUEGT);
		gesuchService.updateGesuch(gesuch, true, null);

		final Collection<AntragStatusHistory> allStatus = statusHistoryService.findAllAntragStatusHistoryByGPFall(gesuch.getGesuchsperiode(), gesuch.getFall());

		Assert.assertNotNull(allStatus);
		Assert.assertEquals(1, allStatus.size());
		Assert.assertEquals(AntragStatus.VERFUEGT, allStatus.iterator().next().getStatus());
	}

	@Test
	public void  testFindAllAntragStatusHistoryByGPFall_Mutation() {
		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, AntragStatus.VERFUEGEN);
		gesuch.setStatus(AntragStatus.VERFUEGT);
		gesuch.setGueltig(true);
		gesuch.setTimestampVerfuegt(LocalDateTime.now());
		final Gesuch gesuchVerfuegt = gesuchService.updateGesuch(gesuch, true, null);
		Optional<Gesuch> mutation = gesuchService.antragMutieren(gesuchVerfuegt.getId(), LocalDate.of(1980, Month.MARCH, 25));

		mutation.get().setStatus(AntragStatus.VERFUEGT);
		gesuchService.updateGesuch(mutation.get(), true, null);

		final Collection<AntragStatusHistory> allStatus = statusHistoryService
			.findAllAntragStatusHistoryByGPFall(mutation.get().getGesuchsperiode(), mutation.get().getFall());

		Assert.assertNotNull(allStatus);
		Assert.assertEquals(2, allStatus.size());

		final Iterator<AntragStatusHistory> iterator = allStatus.iterator();
		final AntragStatusHistory first = iterator.next();
		Assert.assertEquals(AntragStatus.VERFUEGT, first.getStatus());
		Assert.assertEquals(mutation.get().getId(), first.getGesuch().getId());

		final AntragStatusHistory second = iterator.next();
		Assert.assertEquals(AntragStatus.VERFUEGT, second.getStatus());
		Assert.assertEquals(gesuchVerfuegt.getId(), second.getGesuch().getId());
	}

	@Test
	public void testFindLastStatusChangeBeforeBeschwerde_NoBeschwerde() {
		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, AntragStatus.VERFUEGEN);
		gesuch.setStatus(AntragStatus.VERFUEGT);
		gesuchService.updateGesuch(gesuch, true, null);

		try {
			statusHistoryService.findLastStatusChangeBeforeBeschwerde(gesuch);
			Assert.fail("It should throw an exception because the gesuch is not in status BESCHWERDE_HAENGIG");
		} catch(EbeguRuntimeException e) {
			// nop
		}
	}

	@Test
	public void testFindLastStatusChangeBeforeBeschwerde_LessThanTwoPreviousStatus() {
		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, AntragStatus.VERFUEGT);
		gesuch.setStatus(AntragStatus.BESCHWERDE_HAENGIG);
		gesuchService.updateGesuch(gesuch, true, null);

		try {
			statusHistoryService.findLastStatusChangeBeforeBeschwerde(gesuch);
			Assert.fail("It should throw an exception because the gesuch has only one status change");
		} catch(EbeguRuntimeException e) {
			// nop
		}
	}

	@Test
	public void testFindLastStatusChangeBeforeBeschwerde() {
		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, AntragStatus.VERFUEGEN);
		gesuch.setStatus(AntragStatus.VERFUEGT);
		final Gesuch gesuchVerfuegt = gesuchService.updateGesuch(gesuch, true, null);
		gesuchService.setBeschwerdeHaengigForPeriode(gesuchVerfuegt);

		final AntragStatusHistory previousStatus = statusHistoryService.findLastStatusChangeBeforeBeschwerde(gesuch);

		Assert.assertNotNull(previousStatus);
		Assert.assertEquals(AntragStatus.VERFUEGT, previousStatus.getStatus());
	}

}
