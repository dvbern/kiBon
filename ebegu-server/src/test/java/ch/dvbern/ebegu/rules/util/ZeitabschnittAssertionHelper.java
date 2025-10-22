package ch.dvbern.ebegu.rules.util;

import java.time.LocalDate;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.MsgKey;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ZeitabschnittAssertionHelper {

	private final VerfuegungZeitabschnitt zeitabschnitt;

	public ZeitabschnittAssertionHelper(VerfuegungZeitabschnitt zeitabschnitt) {
		this.zeitabschnitt = zeitabschnitt;
	}

	@CanIgnoreReturnValue
	public ZeitabschnittAssertionHelper assertAnspruch(int anspruch) {
		assertThat(
			zeitabschnitt.getAnspruchberechtigtesPensum(),
			is(anspruch)
		);
		return this;
	}

	@CanIgnoreReturnValue
	public ZeitabschnittAssertionHelper assertBetreuungspensum(
		int betreuungspensum
	) {
		assertThat(
			zeitabschnitt.getBetreuungspensumProzent().intValue(),
			is(betreuungspensum)
		);
		return this;
	}

	@CanIgnoreReturnValue
	public ZeitabschnittAssertionHelper assertGueltigAb(LocalDate gueltigAb) {
		assertThat(
			zeitabschnitt.getGueltigkeit().getGueltigAb(),
			is(gueltigAb)
		);
		return this;
	}

	@CanIgnoreReturnValue
	public ZeitabschnittAssertionHelper assertGueltigBis(LocalDate gueltigBis) {
		assertThat(
			zeitabschnitt.getGueltigkeit().getGueltigBis(),
			is(gueltigBis)
		);
		return this;
	}

	@CanIgnoreReturnValue
	public ZeitabschnittAssertionHelper assertMessageKeyExists(MsgKey msgKey) {
		assertThat(
			zeitabschnitt.getBemerkungenDTOList().containsMsgKey(msgKey),
			is(true)
		);
		return this;
	}

	@CanIgnoreReturnValue
	public ZeitabschnittAssertionHelper assertMessageKeyNotExists(
		MsgKey msgKey
	) {
		assertThat(
			zeitabschnitt.getBemerkungenDTOList().containsMsgKey(msgKey),
			is(false)
		);
		return this;
	}
}
