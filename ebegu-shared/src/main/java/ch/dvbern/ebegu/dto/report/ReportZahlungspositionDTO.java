package ch.dvbern.ebegu.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.enums.ZahlungspositionStatus;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class ReportZahlungspositionDTO {

	@NotNull
	@Getter
	private final Gemeinde gemeinde;

	@NotNull
	@Getter
	private final Institution institution;

	@NotNull
	@Getter
	private final String dossierId;

	@NotNull
	@Getter
	private final String zahlungsauftragBeschrieb;

	@NotNull
	@Getter
	private final LocalDate zahlungsauftragDatumFeallig;

	@NotNull
	@Getter
	private final LocalDateTime zahlungsauftragTimestampErstellt;

	@NotNull
	@Getter
	private final String kindVorname;

	@NotNull
	@Getter
	private final String kindNachname;

	@NotNull
	@Getter
	private final String referenzNummer;

	@NotNull
	@Getter
	private final DateRange zeitabschnittGueltigkeit;

	private final int anspruchsPensum;

	private final BigDecimal betreuungsPensum;

	@NotNull
	@Getter
	private final BigDecimal betrag;

	@NotNull
	@Getter
	private final ZahlungspositionStatus zahlungspositionStatus;

	@NotNull
	@Getter
	private final boolean zahlungspositionIgnoriert;

	@NotNull
	@Getter
	private final boolean isAuszahlungAusserhalbVonKibon;

	@Nullable
	@Getter
	@Setter
	private Auszahlungsdaten auszahlungsdaten;

	@Nonnull
	public BigDecimal getBgPensumProzent() {
		return betreuungsPensum.min(
			MathUtil.DEFAULT.from(anspruchsPensum)
		);
	}
}
