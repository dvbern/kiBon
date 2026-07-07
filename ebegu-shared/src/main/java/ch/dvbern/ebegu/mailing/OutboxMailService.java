package ch.dvbern.ebegu.mailing;

import java.util.List;

import javax.annotation.Nonnull;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.Valid;

import ch.dvbern.ebegu.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@Slf4j
public class OutboxMailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(
		OutboxMailService.class
	);

	private static final int MEDIUMTEXT_MAX_LENGTH = 4190000;
	private static final String TEXT_WAS_CUT_OFF_HINT =
		"... IMPORTANT NOTE: mail content was cut off because it was too long to be "
			+ "stored into database. The full text might be available in the server logs.";

	@Inject
	private Persistence persistence;

	public OutboxMail findOutboxMailById(@Nonnull String id) {
		return persistence.find(OutboxMail.class, id);
	}

	public void saveOutboxMail(@Valid OutboxMail outboxMail) {

		String content = fitToMediumText(outboxMail.getContent());
		outboxMail.setContent(content);

		this.persistence.persist(outboxMail);
	}

	/**
	 * Making sure, that all mails are saved in this outbox is very important.
	 * Some mails may have critical content, that must not be lost.
	 * Therefore we must avoid risking SQL exceptions when storing texts of unknown size.
	 * Always fit the texts length to what fits into the colum's data type - MEDIUMTEXT in this case.
	 *
	 * @param text The text to fit to MEDIUMTEXT.
	 * @return The text itself or a substring of it, if it was too long, appended with an hint that this text has been
	 * cut off.
	 */
	private String fitToMediumText(String text) {

		// data type MEDIUMTEXT can hold up to 16,777,215 bytes.
		// we will most likely store text UTF-8 encoded which consumes 4 bytes per character
		// that allows MEDIUMTEXT to hold around 4,194,303 characters.
		// we will round that down to 4.19 M.
		if (text.length() > MEDIUMTEXT_MAX_LENGTH) {
			LOGGER.warn(
				"Mail content was cut off because it was too long to be stored into database. The full text might be available in the server logs."
			);
			return text.substring(0, MEDIUMTEXT_MAX_LENGTH)
				+ TEXT_WAS_CUT_OFF_HINT;
		}
		return text;
	}

	public void udpateOutboxMail(@Valid OutboxMail outboxMail) {

		String content = fitToMediumText(outboxMail.getContent());
		outboxMail.setContent(content);

		this.persistence.merge(outboxMail);
	}

	public List<OutboxMail> getNextN(int n) {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<OutboxMail> query = cb.createQuery(OutboxMail.class);
		Root<OutboxMail> from = query.from(OutboxMail.class);
		Predicate statusPredicate = cb.notEqual(
			from.get(OutboxMail_.status),
			OutboxMailStatus.FAILED
		);
		query.select(from)
			.where(statusPredicate)
			.orderBy(cb.asc(from.get(OutboxMail_.retryCount)));

		return persistence.getEntityManager()
			.createQuery(query)
			.setMaxResults(n)
			.getResultList();
	}

	public void remove(OutboxMail outboxMail) {
		this.persistence.remove(outboxMail);
	}
}
