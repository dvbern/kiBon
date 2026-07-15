package ch.dvbern.ebegu.entities;

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Size;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MEDIUMTEXT_LENGTH_IN_CHARS;

@Entity
@Getter
public class VersendeteMail extends AbstractEntity {
	private static final long serialVersionUID = 3359889299785229122L;

	@Nonnull
	@Column(nullable = false)
	private final LocalDateTime zeitpunktVersand;

	@Nonnull
	@Column(nullable = false)
	private final String empfaengerAdresse;

	@Nonnull
	@Column(nullable = false)
	private final String betreff;

	@Nullable
	@Column()
	@Size(max = DB_DEFAULT_MEDIUMTEXT_LENGTH_IN_CHARS)
	private final String body;

	@Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	@KeywordField
	private final MandantIdentifier mandantIdentifier;

	@SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD",
		justification = "just for JPA")
	protected VersendeteMail() {
		this.zeitpunktVersand = LocalDateTime.now();
		this.empfaengerAdresse = "";
		this.betreff = "";
		this.mandantIdentifier = MandantIdentifier.BERN;
		this.body = "";
	}

	public VersendeteMail(
		@Nonnull LocalDateTime zeitpunktVersand,
		@Nonnull String empfaengerAdresse,
		@Nonnull String betreff,
		@Nonnull MandantIdentifier mandant,
		@Nonnull String body
	) {
		this.zeitpunktVersand = zeitpunktVersand;
		this.empfaengerAdresse = empfaengerAdresse;
		this.betreff = betreff;
		this.mandantIdentifier = mandant;
		this.body = body;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return this.equals(other);
	}

}
