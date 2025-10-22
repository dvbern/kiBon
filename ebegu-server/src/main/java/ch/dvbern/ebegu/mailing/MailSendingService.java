package ch.dvbern.ebegu.mailing;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.annotation.Nonnull;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.VersendeteMail;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.services.VersendeteMailsService;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.EmailAttachment;
import org.apache.commons.mail2.jakarta.HtmlEmail;
import org.apache.commons.mail2.jakarta.MultiPartEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.util.Constants.NEW_LINE_CHAR_PATTERN;

@Stateless
public class MailSendingService {

	private static final Logger LOG = LoggerFactory.getLogger(
		MailSendingService.class.getSimpleName()
	);

	private static final int MAX_RETRIES = 5;

	@Inject
	private EbeguConfiguration configuration;

	@Inject
	private VersendeteMailsService versendeteMailsService;

	@Inject
	OutboxMailService outboxMailService;

	/**
	 * Sendet die Email mit gegebenem MessageBody an die gegebene Adresse. Dadurch kann eine beliebige Message gemailt
	 * werden
	 */

	public void sendMessage(
		@Nonnull String subject,
		@Nonnull String messageBody,
		@Nonnull String mailadress,
		@Nonnull MandantIdentifier mandantIdentifier
	)
		throws MailException {

		Objects.requireNonNull(subject);
		Objects.requireNonNull(messageBody);
		Objects.requireNonNull(mailadress);

		if (configuration.isSendingOfMailsDisabled()) {
			pretendToSendMessage(messageBody, mailadress);
		} else {
			doSendMessage(subject, messageBody, mailadress);
			saveSentMails(subject, mailadress, mandantIdentifier);
		}
	}

	/**
	 * Sendet die Email mit gegebenem MessageBody an die gegebene Adresse. Dadurch kann eine beliebige Message gemailt
	 * werden. Das uebergebene UploadFileInfo wird als Attachment mitgeschickt.
	 */
	// Für den Moment in diesem Task ignorieren
	public void sendMessageWithAttachment(
		@Nonnull String subject,
		@Nonnull String messageBody,
		@Nonnull String mailadress,
		@Nonnull UploadFileInfo uploadFileInfo,
		@Nonnull MandantIdentifier mandantIdentifier
	) throws MailException {

		Objects.requireNonNull(subject);
		Objects.requireNonNull(messageBody);
		Objects.requireNonNull(mailadress);
		Objects.requireNonNull(uploadFileInfo);

		if (configuration.isSendingOfMailsDisabled()) {
			pretendToSendMessage(messageBody, mailadress);
		} else {
			doSendMessageWithAttachment(
				subject,
				messageBody,
				mailadress,
				uploadFileInfo
			);
			saveSentMails(subject, mailadress, mandantIdentifier);
		}
	}

	private void pretendToSendMessage(
		final String messageBody,
		final String mailadress
	) {
		LOG.info(
			"Sending of Emails disabled. Mail would be sent to {} : {}",
			removeNewLineChar(mailadress),
			removeNewLineChar(messageBody)
		);
	}

	protected String removeNewLineChar(String str) {
		return NEW_LINE_CHAR_PATTERN.matcher(str).replaceAll("_");
	}

	private void doSendMessage(
		@Nonnull String subject,
		@Nonnull String messageBody,
		@Nonnull String mailadress
	)
		throws MailException {
		try {
			HtmlEmail email = new HtmlEmail();
			email.setCharset("UTF-8");
			email.setHostName(configuration.getSMTPHost());
			email.setSmtpPort(configuration.getSMTPPort());
			email.setSSLOnConnect(false);
			email.setFrom(configuration.getSenderAddress());
			email.setSubject(subject);
			email.setHtmlMsg(messageBody);
			email.addTo(mailadress);
			email.send();
		} catch (final EmailException e) {
			throw new MailException(
				"Error while sending Mail to: '" + mailadress + '\'',
				e
			);
		}
	}

	/**
	 * DEPRECATED - KIBON DOES NOT SEND EMAILS WITH ATTACHMENT ANYMORE
	 * QUOTE FROM PO - 2025-07-24
	 * 
	 * @param subject
	 * @param messageBody
	 * @param mailadress
	 * @param uploadFileInfo
	 * @throws MailException
	 */
	private void doSendMessageWithAttachment(
		@Nonnull String subject,
		@Nonnull String messageBody,
		@Nonnull String mailadress,
		@Nonnull UploadFileInfo uploadFileInfo
	)
		throws MailException {
		try {
			// Create the attachment
			EmailAttachment attachment = new EmailAttachment();
			final String pathOfAttachment = "File://"
				+ uploadFileInfo.getPathAsString();
			attachment.setURL(new URL(pathOfAttachment));
			attachment.setDisposition(EmailAttachment.ATTACHMENT);
			attachment.setDescription(uploadFileInfo.getFilename());
			attachment.setName(uploadFileInfo.getFilename());

			// Create the email message
			MultiPartEmail email = new MultiPartEmail();
			email.setHostName(configuration.getSMTPHost());
			email.setSmtpPort(configuration.getSMTPPort());
			email.setSSLOnConnect(false);

			email.setFrom(configuration.getSenderAddress());
			email.setSubject(subject);
			email.setMsg(messageBody);
			email.addTo(mailadress);

			// add the attachment
			email.attach(attachment);

			// send the email
			email.send();
		} catch (final EmailException | MalformedURLException e) {
			throw new MailException(
				"Error while sending Mail with Attachment to: '"
					+ mailadress
					+ '\'',
				e
			);
		}
	}

	private void saveSentMails(
		String subject,
		String mailadress,
		MandantIdentifier mandant
	) {
		LocalDateTime zeitpunktVersand = LocalDateTime.now();
		VersendeteMail versendeteMail = new VersendeteMail(
			zeitpunktVersand,
			mailadress,
			subject,
			mandant
		);
		versendeteMailsService.saveVersendeteMail(versendeteMail);
	}

	@Transactional(value = Transactional.TxType.REQUIRES_NEW)
	public void sendMail(OutboxMail outboxMail) throws MailException {
		OutboxMail mailtoSendAndRemove = outboxMailService.findOutboxMailById(
			outboxMail.getId()
		);
		sendMessage(
			mailtoSendAndRemove.getSubject(),
			mailtoSendAndRemove.getContent(),
			mailtoSendAndRemove.getRecipient(),
			mailtoSendAndRemove.getMandant()
		);
		outboxMailService.remove(mailtoSendAndRemove);
	}

	@Transactional(value = Transactional.TxType.REQUIRES_NEW)
	public void incrementRetry(OutboxMail mail) {
		OutboxMail managedMail = outboxMailService.findOutboxMailById(
			mail.getId()
		);
		managedMail.setRetryCount(managedMail.getRetryCount() + 1);
		if (managedMail.getRetryCount() > MAX_RETRIES) {
			managedMail.setStatus(OutboxMailStatus.FAILED);
		} else {
			managedMail.setStatus(OutboxMailStatus.RETRY);
		}
		outboxMailService.udpateOutboxMail(managedMail);
	}
}
