package ch.dvbern.ebegu.mailing;

import javax.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@NoArgsConstructor
public class OutboxMail extends AbstractEntity {

	@Nonnull
	@Column(nullable = false)
	private String subject;

	@Nonnull
	@Column(nullable = false)
	private String content;

	@Nonnull
	@Column(nullable = false)
	private String recipient;

	@Nonnull
	@Enumerated(EnumType.STRING)
	private MandantIdentifier mandant;

	@Nonnull
	@Column(nullable = false)
	private int retryCount = 0;

	@Nonnull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OutboxMailStatus status = OutboxMailStatus.NEW;

	@Override
	public boolean isSame(AbstractEntity other) {
		if (!(other instanceof OutboxMail otherMail)) {
			return false;
		}
		return recipient.equals(otherMail.recipient)
			&& subject.equals(otherMail.subject)
			&& content.equals(otherMail.content)
			&& mandant.equals(otherMail.mandant);
	}
}
