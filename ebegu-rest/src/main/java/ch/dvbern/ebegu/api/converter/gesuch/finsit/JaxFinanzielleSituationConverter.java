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

package ch.dvbern.ebegu.api.converter.gesuch.finsit;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.dtos.finanziellesituation.JaxFinanzielleSituation;
import ch.dvbern.ebegu.api.dtos.finanziellesituation.JaxFinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.services.FinanzielleSituationService;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxFinanzielleSituationConverter extends
	AbstractBaseFinanzielleSituationConverter {
	@Inject
	private FinanzielleSituationService finanzielleSituationService;

	@Nonnull
	public FinanzielleSituationContainer finanzielleSituationContainerToStorableEntity(
		@Nonnull final JaxFinanzielleSituationContainer containerJAX,
		@Nullable FinanzielleSituationContainer container
	) {

		requireNonNull(containerJAX);

		FinanzielleSituationContainer containerToMergeWith = container != null ?
			container :
			new FinanzielleSituationContainer();

		if (containerJAX.getId() != null) {
			final Optional<FinanzielleSituationContainer> existingFSC =
				finanzielleSituationService.findFinanzielleSituation(
					containerJAX.getId()
				);
			if (existingFSC.isPresent()) {
				containerToMergeWith = existingFSC.get();
			}
		}
		return finanzielleSituationContainerToEntity(
			containerJAX,
			containerToMergeWith
		);
	}

	@Nonnull
	public FinanzielleSituationContainer finanzielleSituationContainerToEntity(
		@Nonnull final JaxFinanzielleSituationContainer containerJAX,
		@Nonnull final FinanzielleSituationContainer container
	) {
		requireNonNull(container);
		requireNonNull(containerJAX);
		convertAbstractVorgaengerFieldsToEntity(containerJAX, container);
		container.setJahr(containerJAX.getJahr());
		FinanzielleSituation finSitToMergeWith;
		//Im moment kann eine einmal gespeicherte Finanzielle Situation nicht mehr entfernt werden.
		if (containerJAX.getFinanzielleSituationGS() != null) {
			finSitToMergeWith = Optional.ofNullable(
				container.getFinanzielleSituationGS()
			).orElse(new FinanzielleSituation());
			container.setFinanzielleSituationGS(
				finanzielleSituationToEntity(
					containerJAX.getFinanzielleSituationGS(),
					finSitToMergeWith
				)
			);
		}
		if (containerJAX.getFinanzielleSituationJA() != null) {
			finSitToMergeWith = Optional.ofNullable(
				container.getFinanzielleSituationJA()
			).orElse(new FinanzielleSituation());
			container.setFinanzielleSituationJA(
				finanzielleSituationToEntity(
					containerJAX.getFinanzielleSituationJA(),
					finSitToMergeWith
				)
			);
		}
		return container;
	}

	@Nonnull
	public JaxFinanzielleSituationContainer finanzielleSituationContainerToJAX(
		@Nonnull final FinanzielleSituationContainer persistedFinanzielleSituation
	) {
		final JaxFinanzielleSituationContainer jaxPerson =
			new JaxFinanzielleSituationContainer();
		convertAbstractVorgaengerFieldsToJAX(
			persistedFinanzielleSituation,
			jaxPerson
		);
		jaxPerson.setJahr(persistedFinanzielleSituation.getJahr());
		jaxPerson.setFinanzielleSituationGS(
			finanzielleSituationToJAX(
				persistedFinanzielleSituation.getFinanzielleSituationGS()
			)
		);
		jaxPerson.setFinanzielleSituationJA(
			finanzielleSituationToJAX(
				persistedFinanzielleSituation.getFinanzielleSituationJA()
			)
		);
		return jaxPerson;
	}

	private FinanzielleSituation finanzielleSituationToEntity(
		@Nonnull final JaxFinanzielleSituation finanzielleSituationJAXP,
		@Nonnull final FinanzielleSituation finanzielleSituation
	) {

		requireNonNull(finanzielleSituation);
		requireNonNull(finanzielleSituationJAXP);

		// wenn die finanzielle Situation durch die Steuerdatenschnittstelle ausgefüllt wurde
		// ist es nie erlaubt, diese Werte zu überschreiben. Eine Ausnahme sind das Einkommen aus dem vereinfachten
		// Verfahren, die Flag für den Zugriff und das Ersatzeinkommen bei Selbstständigkeit
		if (finanzielleSituation.getSteuerdatenAbfrageStatus() != null
			&& finanzielleSituation.getSteuerdatenAbfrageStatus()
				.isSteuerdatenAbfrageErfolgreich()) {
			finanzielleSituation.setSteuerdatenZugriff(
				finanzielleSituationJAXP.getSteuerdatenZugriff()
			);
			finanzielleSituation
				.setEinkommenInVereinfachtemVerfahrenAbgerechnet(
					finanzielleSituationJAXP
						.getEinkommenInVereinfachtemVerfahrenAbgerechnet()
				);
			finanzielleSituation
				.setAmountEinkommenInVereinfachtemVerfahrenAbgerechnet(
					finanzielleSituationJAXP
						.getAmountEinkommenInVereinfachtemVerfahrenAbgerechnet()
				);

			finanzielleSituation.setErsatzeinkommenSelbststaendigkeitBasisjahr(
				finanzielleSituationJAXP
					.getErsatzeinkommenSelbststaendigkeitBasisjahr()
			);
			finanzielleSituation
				.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus1(
					finanzielleSituationJAXP
						.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1()
				);
			finanzielleSituation
				.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus2(
					finanzielleSituationJAXP
						.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2()
				);
			return finanzielleSituation;
		}

		abstractFinanzielleSituationToEntity(
			finanzielleSituationJAXP,
			finanzielleSituation
		);
		finanzielleSituation.setSteuerveranlagungErhalten(
			finanzielleSituationJAXP.getSteuerveranlagungErhalten()
		);
		finanzielleSituation.setSteuererklaerungAusgefuellt(
			finanzielleSituationJAXP.getSteuererklaerungAusgefuellt()
		);
		finanzielleSituation.setSteuerdatenZugriff(
			finanzielleSituationJAXP.getSteuerdatenZugriff()
		);
		finanzielleSituation.setGeschaeftsgewinnBasisjahrMinus2(
			finanzielleSituationJAXP.getGeschaeftsgewinnBasisjahrMinus2()
		);

		finanzielleSituation.setQuellenbesteuert(
			finanzielleSituationJAXP.getQuellenbesteuert()
		);
		finanzielleSituation.setGemeinsameStekVorjahr(
			finanzielleSituationJAXP.getGemeinsameStekVorjahr()
		);
		finanzielleSituation.setAlleinigeStekVorjahr(
			finanzielleSituationJAXP.getAlleinigeStekVorjahr()
		);
		finanzielleSituation.setAbzuegeKinderAusbildung(
			finanzielleSituationJAXP.getAbzuegeKinderAusbildung()
		);
		finanzielleSituation.setUnterhaltsBeitraege(
			finanzielleSituationJAXP.getUnterhaltsBeitraege()
		);
		finanzielleSituation.setAutomatischePruefungErlaubt(
			finanzielleSituationJAXP.getAutomatischePruefungErlaubt()
		);
		finanzielleSituation.setVeranlagt(
			finanzielleSituationJAXP.getVeranlagt()
		);
		finanzielleSituation.setVeranlagtVorjahr(
			finanzielleSituationJAXP.getVeranlagtVorjahr()
		);
		finanzielleSituation.setMomentanSelbststaendig(
			finanzielleSituationJAXP.getMomentanSelbststaendig()
		);
		finanzielleSituation
			.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus2(
				finanzielleSituationJAXP
					.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2()
			);

		return finanzielleSituation;
	}

	@Nullable
	public JaxFinanzielleSituation finanzielleSituationToJAX(
		@Nullable final FinanzielleSituation persistedFinanzielleSituation
	) {

		if (persistedFinanzielleSituation == null) {
			return null;
		}

		JaxFinanzielleSituation jaxFinanzielleSituation =
			new JaxFinanzielleSituation();
		abstractFinanzielleSituationToJAX(
			persistedFinanzielleSituation,
			jaxFinanzielleSituation
		);
		jaxFinanzielleSituation.setSteuerveranlagungErhalten(
			persistedFinanzielleSituation.getSteuerveranlagungErhalten()
		);
		jaxFinanzielleSituation.setSteuererklaerungAusgefuellt(
			persistedFinanzielleSituation.getSteuererklaerungAusgefuellt()
		);
		jaxFinanzielleSituation.setSteuerdatenZugriff(
			persistedFinanzielleSituation.getSteuerdatenZugriff()
		);
		jaxFinanzielleSituation.setGeschaeftsgewinnBasisjahrMinus2(
			persistedFinanzielleSituation.getGeschaeftsgewinnBasisjahrMinus2()
		);

		jaxFinanzielleSituation.setQuellenbesteuert(
			persistedFinanzielleSituation.getQuellenbesteuert()
		);
		jaxFinanzielleSituation.setGemeinsameStekVorjahr(
			persistedFinanzielleSituation.getGemeinsameStekVorjahr()
		);
		jaxFinanzielleSituation.setAlleinigeStekVorjahr(
			persistedFinanzielleSituation.getAlleinigeStekVorjahr()
		);
		jaxFinanzielleSituation.setVeranlagt(
			persistedFinanzielleSituation.getVeranlagt()
		);
		jaxFinanzielleSituation.setVeranlagtVorjahr(
			persistedFinanzielleSituation.getVeranlagtVorjahr()
		);

		jaxFinanzielleSituation.setAbzuegeKinderAusbildung(
			persistedFinanzielleSituation.getAbzuegeKinderAusbildung()
		);
		jaxFinanzielleSituation.setUnterhaltsBeitraege(
			persistedFinanzielleSituation.getUnterhaltsBeitraege()
		);
		jaxFinanzielleSituation.setSteuerdatenAbfrageStatus(
			persistedFinanzielleSituation.getSteuerdatenAbfrageStatus()
		);
		jaxFinanzielleSituation.setSteuerdatenAbfrageTimestamp(
			persistedFinanzielleSituation.getSteuerdatenAbfrageTimestamp()
		);
		jaxFinanzielleSituation.setAutomatischePruefungErlaubt(
			persistedFinanzielleSituation.getAutomatischePruefungErlaubt()
		);
		jaxFinanzielleSituation.setMomentanSelbststaendig(
			persistedFinanzielleSituation.getMomentanSelbststaendig()
		);

		jaxFinanzielleSituation
			.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus2(
				persistedFinanzielleSituation
					.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus2()
			);

		return jaxFinanzielleSituation;
	}
}
