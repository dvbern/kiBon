package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.util.Constants;

public class KindTerminiertAbschnittRule extends AbstractAbschnittRule {

	protected KindTerminiertAbschnittRule(
		@Nonnull Locale locale
	) {
		super(
			RuleKey.KIND_ANSPRUCH,
			RuleType.GRUNDREGEL_DATA,
			RuleValidity.ASIV,
			Constants.DEFAULT_GUELTIGKEIT,
			locale
		);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(
		@Nonnull AbstractPlatz platz
	) {
		List<VerfuegungZeitabschnitt> verfuegungZeitabschnitts =
			new ArrayList<>();
		if (platz.getKind().getKindJA().isGueltigkeitTerminiert()) {
			verfuegungZeitabschnitts.add(
				createKindTerminiertAbschnitt(platz.getKind().getKindJA())
			);
		}

		return verfuegungZeitabschnitts;
	}

	@Nonnull
	private VerfuegungZeitabschnitt createKindTerminiertAbschnitt(
		@Nonnull Kind kindJA
	) {
		Objects.requireNonNull(kindJA.getGueltigkeitTerminiertPer());

		LocalDate zeitabschnittKindTerminiertGuetltigAb = kindJA
			.getGueltigkeitTerminiertPer()
			.with(TemporalAdjusters.firstDayOfNextMonth());

		VerfuegungZeitabschnitt zeitabschnittTerminiert =
			createZeitabschnittWithinValidityPeriodOfRule(
				zeitabschnittKindTerminiertGuetltigAb,
				validTo()
			);
		zeitabschnittTerminiert.setKindTerminiert(true);
		return zeitabschnittTerminiert;
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return BetreuungsangebotTyp.getBerechnetesAngebotTypes();
	}
}
