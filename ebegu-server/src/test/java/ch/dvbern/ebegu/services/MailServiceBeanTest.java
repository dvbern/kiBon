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

package ch.dvbern.ebegu.services;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.mail.MailTemplateConfiguration;
import ch.dvbern.ebegu.mailing.OutboxMail;
import ch.dvbern.ebegu.mailing.OutboxMailService;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static ch.dvbern.ebegu.einstellung.ApplicationPropertyKey.MAIL_VERSAND_ALLER_MAILS_AUCH_AN_GS_2;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

@ExtendWith(EasyMockExtension.class)
class MailServiceBeanTest extends EasyMockSupport {

	private static final String GS1_MAIL = "gs1@example.com";
	private static final String GS2_MAIL = "gs2@example.com";
	private static final List<Sprache> SPRACHEN = List.of(Sprache.DEUTSCH);

	@TestSubject
	private final MailServiceBean mailService = new MailServiceBean();

	@Mock
	private MailTemplateConfiguration mailTemplateConfig;
	@Mock
	private FallService fallService;
	@Mock
	private GemeindeService gemeindeService;
	@Mock
	private GesuchService gesuchService;
	@Mock
	private ApplicationPropertyService applicationPropertyService;
	@Mock
	private OutboxMailService outboxMailService;

	@Nested
	class PrepareTemplateAndSendInfoMitteilungErhaltenTest {

		@Test
		void shouldSendMailToGs1_whenFallHasBesitzerWithMailAddress() {
			Mitteilung mitteilung = createMitteilung();
			expectGemeindeStammdatenCallForSprachen(mitteilung);
			expectFallHasMail(mitteilung, GS1_MAIL);
			expectMailTemplatePreparation(mitteilung, GS1_MAIL);
			expectVersandAnGs2Aktiviert(mitteilung, true);
			expectKeineGS2MailInDossierOfMitteilung(mitteilung);
			Capture<OutboxMail> capturedMails = expectOutboxMails(1);
			replayAll();

			mailService.prepareTemplateAndSendInfoMitteilungErhalten(
				mitteilung
			);

			verifyAll();
			assertThat(
				getCapturedRecipients(capturedMails),
				contains(GS1_MAIL)
			);
		}

		@Test
		void shouldNotSendAnyMail_whenNoMailAddressFound() {
			Mitteilung mitteilung = createMitteilung();
			expectGemeindeStammdatenCallForSprachen(mitteilung);
			expect(
				fallService.getCurrentEmailAddress(
					mitteilung.getFall().getId()
				)
			).andReturn(Optional.empty());
			expectKeineGS2MailInDossierOfMitteilung(mitteilung);
			expectVersandAnGs2Aktiviert(mitteilung, true);
			Capture<OutboxMail> capturedMails = expectOutboxMails(0);
			replayAll();

			mailService.prepareTemplateAndSendInfoMitteilungErhalten(
				mitteilung
			);

			verifyAll();
			assertThat(capturedMails.getValues(), empty());
		}

		@Test
		void shouldSendMailToGS1AndGS2_whenGS2MailIsInDossier() {
			Mitteilung mitteilung = createMitteilung();
			expectGemeindeStammdatenCallForSprachen(mitteilung);
			expectFallHasMail(mitteilung, GS1_MAIL);
			expectMailTemplatePreparation(mitteilung, GS1_MAIL);
			expectGS2MailInDossierOfMitteilung(mitteilung);
			expectVersandAnGs2Aktiviert(mitteilung, true);
			expectMailToGs2(mitteilung);
			Capture<OutboxMail> capturedMails = expectOutboxMails(2);
			replayAll();

			mailService.prepareTemplateAndSendInfoMitteilungErhalten(
				mitteilung
			);

			verifyAll();
			assertThat(
				getCapturedRecipients(capturedMails),
				contains(GS1_MAIL, GS2_MAIL)
			);
		}

		@Test
		void shouldSendMailToGS1_whenNoGS2MailIsFoundInDossier() {
			Mitteilung mitteilung = createMitteilung();
			expectGemeindeStammdatenCallForSprachen(mitteilung);
			expectFallHasMail(mitteilung, GS1_MAIL);
			expectMailTemplatePreparation(mitteilung, GS1_MAIL);
			expectKeineGS2MailInDossierOfMitteilung(mitteilung);
			expectVersandAnGs2Aktiviert(mitteilung, true);

			Capture<OutboxMail> capturedMails = expectOutboxMails(1);
			replayAll();

			mailService.prepareTemplateAndSendInfoMitteilungErhalten(
				mitteilung
			);

			verifyAll();
			assertThat(
				getCapturedRecipients(capturedMails),
				contains(GS1_MAIL)
			);
		}

		@Test
		void shouldNotSendMailToGs2_whenVersandAnGs2DeaktiviertAndGS2MailIsInDossier() {
			Mitteilung mitteilung = createMitteilung();
			expectGemeindeStammdatenCallForSprachen(mitteilung);
			expectFallHasMail(mitteilung, GS1_MAIL);
			expectMailTemplatePreparation(mitteilung, GS1_MAIL);
			expectVersandAnGs2Aktiviert(mitteilung, false);
			Capture<OutboxMail> capturedMails = expectOutboxMails(1);
			replayAll();

			mailService.prepareTemplateAndSendInfoMitteilungErhalten(
				mitteilung
			);

			verifyAll();
			assertThat(
				getCapturedRecipients(capturedMails),
				not(hasItem(GS2_MAIL))
			);
		}

		@Test
		void shouldSendMailToGS1_whenVersandAnGs2Deaktiviert() {
			Mitteilung mitteilung = createMitteilung();
			expectGemeindeStammdatenCallForSprachen(mitteilung);
			expectFallHasMail(mitteilung, GS1_MAIL);
			expectMailTemplatePreparation(mitteilung, GS1_MAIL);
			expectVersandAnGs2Aktiviert(mitteilung, false);
			Capture<OutboxMail> capturedMails = expectOutboxMails(1);
			replayAll();

			mailService.prepareTemplateAndSendInfoMitteilungErhalten(
				mitteilung
			);

			verifyAll();
			assertThat(
				getCapturedRecipients(capturedMails),
				contains(GS1_MAIL)
			);
		}

		@Test
		void shouldNotSendMailToGs2_whenNoGS2MailIsFoundInDossier() {
			Mitteilung mitteilung = createMitteilung();
			expectGemeindeStammdatenCallForSprachen(mitteilung);
			expectFallHasMail(mitteilung, GS1_MAIL);
			expectMailTemplatePreparation(mitteilung, GS1_MAIL);
			expectKeineGS2MailInDossierOfMitteilung(mitteilung);
			expectVersandAnGs2Aktiviert(mitteilung, true);
			Capture<OutboxMail> capturedMails = expectOutboxMails(1);
			replayAll();

			mailService.prepareTemplateAndSendInfoMitteilungErhalten(
				mitteilung
			);

			verifyAll();
			assertThat(
				getCapturedRecipients(capturedMails),
				not(hasItem(GS2_MAIL))
			);
		}

		@Nonnull
		private Mitteilung createMitteilung() {
			Benutzer besitzer = new Benutzer();
			besitzer.setStatus(BenutzerStatus.AKTIV);

			Dossier dossier = TestDataUtil.createDefaultDossier();
			dossier.getFall().setBesitzer(besitzer);

			Mitteilung mitteilung = new Mitteilung();
			mitteilung.setDossier(dossier);

			return mitteilung;
		}

		private void expectGemeindeStammdatenCallForSprachen(
			@Nonnull Mitteilung mitteilung
		) {
			expect(
				gemeindeService.getGemeindeStammdatenByGemeindeId(
					mitteilung.getDossier().getGemeinde().getId()
				)
			).andReturn(
				Optional.of(
					TestDataUtil.createGemeindeStammdaten(
						mitteilung.getDossier().getGemeinde()
					)
				)
			);
		}

		private void expectFallHasMail(
			@Nonnull Mitteilung mitteilung,
			@Nonnull String mail
		) {
			expect(
				fallService.getCurrentEmailAddress(
					mitteilung.getFall().getId()
				)
			).andReturn(Optional.of(mail));
		}

		private void expectMailTemplatePreparation(
			@Nonnull Mitteilung mitteilung,
			@Nonnull String mail
		) {
			expect(
				mailTemplateConfig.getInfoMitteilungErhalten(
					mitteilung,
					mail,
					SPRACHEN
				)
			).andReturn(createMailMessage(mail));
		}

		private void expectMailToGs2(@Nonnull Mitteilung mitteilung) {
			expect(
				mailTemplateConfig.getInfoMitteilungErhalten(
					mitteilung,
					GS2_MAIL,
					SPRACHEN
				)
			).andReturn(createMailMessage(GS2_MAIL));
		}

		private void expectGS2MailInDossierOfMitteilung(
			@Nonnull Mitteilung mitteilung
		) {
			expect(
				gesuchService.getMailOfGesuchForDossierWithLatestMutationOfGS2(
					mitteilung.getDossier()
				)
			).andReturn(Optional.of(MailServiceBeanTest.GS2_MAIL));
		}

		private void expectKeineGS2MailInDossierOfMitteilung(
			@Nonnull Mitteilung mitteilung
		) {
			expect(
				gesuchService.getMailOfGesuchForDossierWithLatestMutationOfGS2(
					mitteilung.getDossier()
				)
			).andReturn(Optional.empty());
		}

		private void expectVersandAnGs2Aktiviert(
			@Nonnull Mitteilung mitteilung,
			boolean aktiviert
		) {
			expect(
				applicationPropertyService.findApplicationPropertyAsBoolean(
					MAIL_VERSAND_ALLER_MAILS_AUCH_AN_GS_2,
					mitteilung.getFall().getMandant()
				)
			).andReturn(aktiviert);
		}

		@Nonnull
		private Capture<OutboxMail> expectOutboxMails(int anzahl) {
			Capture<OutboxMail> captured = EasyMock.newCapture(
				CaptureType.ALL
			);
			if (anzahl > 0) {
				outboxMailService.saveOutboxMail(EasyMock.capture(captured));
				expectLastCall().andVoid().times(anzahl);
			}

			return captured;
		}

		@Nonnull
		private List<String> getCapturedRecipients(
			@Nonnull Capture<OutboxMail> mails
		) {
			return mails.getValues()
				.stream()
				.map(OutboxMail::getRecipient)
				.toList();
		}

		/**
		 * Die MailTemplateConfiguration liefert eine komplette Mail inkl. Header, aus welcher der Betreff und
		 * der Inhalt extrahiert werden.
		 */
		@Nonnull
		private String createMailMessage(@Nonnull String empfaenger) {
			return "From: kibon@example.com\n"
				+ "To: "
				+ empfaenger
				+ '\n'
				+ "Subject: Neue Mitteilung\n"
				+ "Content-Type: text/plain; charset=UTF-8\n"
				+ '\n'
				+ "Sie haben eine neue Mitteilung erhalten.";
		}
	}
}
