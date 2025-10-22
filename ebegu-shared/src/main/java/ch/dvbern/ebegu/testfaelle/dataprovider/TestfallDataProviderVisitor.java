package ch.dvbern.ebegu.testfaelle.dataprovider;

import java.time.LocalDate;
import java.time.Month;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.AbstractMandantDefaultVisitor;

public class TestfallDataProviderVisitor extends
	AbstractMandantDefaultVisitor<AbstractTestfallDataProvider> {

	private final Gesuchsperiode gesuchsperiode;
	private static final LocalDate START_FKJV = LocalDate.of(
		2022,
		Month.AUGUST,
		1
	);
	private static final LocalDate START_SCHWYZ_ERWEITERT = LocalDate.of(
		2025,
		Month.AUGUST,
		1
	);

	public TestfallDataProviderVisitor(Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	public AbstractTestfallDataProvider getTestDataProvider(
		@Nonnull Mandant mandant
	) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	protected AbstractTestfallDataProvider visitDefault() {
		return new DefaultTestfallDataProvider(gesuchsperiode);
	}

	@Override
	public AbstractTestfallDataProvider visitBern() {
		if (gesuchsperiode.getGueltigkeit()
			.getGueltigAb()
			.isBefore(START_FKJV)) {
			return new AsivBernTestfallDataProvider(gesuchsperiode);
		}

		return new FkjvBernTestfallDataProvider(gesuchsperiode);
	}

	@Override
	public AbstractTestfallDataProvider visitLuzern() {
		return new LuzernTestfallDataProvider(gesuchsperiode);
	}

	@Override
	public AbstractTestfallDataProvider visitAppenzellAusserrhoden() {
		return new AppenzellTestfallDataProvider(gesuchsperiode);
	}

	@Override
	public AbstractTestfallDataProvider visitSchwyz() {
		if (gesuchsperiode.getGueltigkeit()
			.getGueltigAb()
			.isBefore(START_SCHWYZ_ERWEITERT)) {
			return new SchwyzTestfallDataProvider(gesuchsperiode);
		}
		return new SchwyzErweitertTestfallDataProvider(gesuchsperiode);
	}

	@Override
	public AbstractTestfallDataProvider visitZug() {
		return new ZugTestfallDataProvider(gesuchsperiode);
	}
}
