package ch.dvbern.ebegu.api.dtos;

import java.io.Serial;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import io.github.threetenjaxb.core.LocalDateTimeXmlAdapter;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JaxVersendeteMail extends JaxAbstractDTO {
	@Serial
	private static final long serialVersionUID = 3359889275785229022L;

	@Nonnull
	@XmlJavaTypeAdapter(LocalDateTimeXmlAdapter.class)
	private LocalDateTime zeitpunktVersand;

	@Nonnull
	private String empfaengerAdresse;

	@Nonnull
	private String betreff;

	@Nonnull
	private MandantIdentifier mandantIdentifier;

	@Nullable
	private String body;
}
