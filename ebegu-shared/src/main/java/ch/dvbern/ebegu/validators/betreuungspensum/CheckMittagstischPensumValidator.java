/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.validators.betreuungspensum;

import java.math.BigDecimal;

import javax.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.containers.BetreuungAndPensumContainer;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.util.BetreuungUtil;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Validator for Betreuungspensen, for {@link BetreuungsangebotTyp#MITTAGSTISCH} checks that each
 * {@link AbstractMahlzeitenPensum#getPensum} is derived from
 * {@link AbstractMahlzeitenPensum#getMonatlicheHauptmahlzeiten} and
 * that {@link AbstractMahlzeitenPensum#getMonatlicheBetreuungskosten} matches
 * {@link AbstractMahlzeitenPensum#getTarifProHauptmahlzeit}.
 */
public class CheckMittagstischPensumValidator
	implements
	ConstraintValidator<CheckMittagstischPensum, BetreuungAndPensumContainer> {

	@SuppressWarnings("CdiInjectionPointsInspection")
	@Inject
	private EinstellungService einstellungService;

	// We need to pass to EinstellungService a new EntityManager to avoid errors like ConcurrentModificatinoException.
	// So we create it here and pass it to the methods of EinstellungService we need to call.
	// http://stackoverflow.com/questions/18267269/correct-way-to-do-an-entitymanager-query-during-hibernate-validation
	@PersistenceUnit(unitName = "ebeguPersistenceUnit")
	private EntityManagerFactory entityManagerFactory;

	@Override
	public boolean isValid(
		BetreuungAndPensumContainer betreuungAndPensumContainer,
		ConstraintValidatorContext context
	) {
		return betreuungAndPensumContainer.findBetreuung()
			.filter(Betreuung::isAngebotMittagstisch)
			.map(
				b -> betreuungAndPensumContainer.findBetreuung()
					.stream()
					.allMatch(
						betreuung -> validateBetreuung(
							betreuung,
							betreuungAndPensumContainer
						)
					)
			)
			.orElse(true);
	}

	private boolean validateBetreuung(
		Betreuung betreuung,
		BetreuungAndPensumContainer betreuungAndPensumContainer
	) {
		Gesuchsperiode gesuchsperiode = betreuung.extractGesuchsperiode();
		Gemeinde gemeinde = betreuung.extractGemeinde();
		try (EntityManager em = createEntityManager()) {
			Einstellung oeffnungstagMittagstischEinstellung =
				einstellungService.findEinstellung(
					EinstellungKey.OEFFNUNGSTAGE_MITTAGSTISCH,
					gemeinde,
					gesuchsperiode,
					em
				);
			return betreuungAndPensumContainer.getBetreuungenJA()
				.stream()
				.allMatch(
					betreuungspensum -> isValid(
						betreuungspensum,
						oeffnungstagMittagstischEinstellung
							.getValueAsBigDecimal()
					)
				);
		}
	}

	private boolean isValid(
		AbstractMahlzeitenPensum pensum,
		BigDecimal oeffnungstagMittagstisch
	) {
		return (hasValidMittagstischPensum(pensum, oeffnungstagMittagstisch)
			|| hasValidMittagstischPensumOld(pensum)) && hasValidKosten(pensum);
	}

	private boolean hasValidMittagstischPensum(
		AbstractMahlzeitenPensum pensum,
		BigDecimal oeffnungstagMittagstisch
	) {
		BigDecimal derivedPensum = BetreuungUtil.derivePensumMittagstisch(
			pensum,
			oeffnungstagMittagstisch
		);

		return MathUtil.isClose(
			derivedPensum,
			pensum.getPensum(),
			BigDecimal.valueOf(0.001)
		);
	}

	private boolean hasValidMittagstischPensumOld(
		AbstractMahlzeitenPensum pensum
	) {
		BigDecimal derivedPensum = BetreuungUtil.derivePensumMittagtischOld(
			pensum
		);

		return MathUtil.isClose(
			derivedPensum,
			pensum.getPensum(),
			BigDecimal.valueOf(0.001)
		);
	}

	private boolean hasValidKosten(AbstractMahlzeitenPensum pensum) {
		BigDecimal derivedKosten = BetreuungUtil.deriveKostenMittagstisch(
			pensum
		);
		BigDecimal roundedKosten = MathUtil.toTwoKommastelle(
			pensum.getMonatlicheBetreuungskosten()
		);

		return MathUtil.isSame(derivedKosten, roundedKosten);
	}

	@Nullable
	private EntityManager createEntityManager() {
		if (entityManagerFactory != null) {
			return entityManagerFactory.createEntityManager(); // creates a new EntityManager
		}
		return null;
	}
}
