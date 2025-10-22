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

package ch.dvbern.ebegu.api.converter.gesuch;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.converter.gesuch.finsit.JaxEinkommensverschlechterungConverter;
import ch.dvbern.ebegu.api.converter.gesuch.finsit.JaxFinanzielleSituationConverter;
import ch.dvbern.ebegu.api.dtos.JaxAdresse;
import ch.dvbern.ebegu.api.dtos.JaxAdresseContainer;
import ch.dvbern.ebegu.api.dtos.JaxEinkommensverschlechterungContainer;
import ch.dvbern.ebegu.api.dtos.JaxErwerbspensumContainer;
import ch.dvbern.ebegu.api.dtos.JaxGesuchsteller;
import ch.dvbern.ebegu.api.dtos.JaxGesuchstellerContainer;
import ch.dvbern.ebegu.api.dtos.finanziellesituation.JaxFinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.services.GesuchstellerAdresseService;
import ch.dvbern.ebegu.util.Constants;
import org.apache.commons.lang3.Validate;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxGesuchstellerConverter extends AbstractBaseConverter {

	@Inject
	private GesuchstellerAdresseService gesuchstellerAdresseService;

	@Inject
	private JaxErwerbspensumConverter erwerbspensumConverter;
	@Inject
	private JaxFinanzielleSituationConverter finanzielleSituationConverter;
	@Inject
	private JaxEinkommensverschlechterungConverter einkommensverschlechterungConverter;

	@Nonnull
	public GesuchstellerAdresse gesuchstellerAdresseToEntity(
		@Nonnull final JaxAdresse jaxAdresse,
		@Nonnull final GesuchstellerAdresse gesuchstellerAdresse
	) {

		adresseToEntity(jaxAdresse, gesuchstellerAdresse);
		gesuchstellerAdresse.setAdresseTyp(jaxAdresse.getAdresseTyp());
		gesuchstellerAdresse.setNichtInGemeinde(jaxAdresse.isNichtInGemeinde());

		return gesuchstellerAdresse;
	}

	@Nonnull
	public JaxAdresse gesuchstellerAdresseToJAX(
		@Nonnull final GesuchstellerAdresse gesuchstellerAdresse
	) {
		final JaxAdresse jaxAdresse = adresseToJAX(gesuchstellerAdresse);
		jaxAdresse.setAdresseTyp(gesuchstellerAdresse.getAdresseTyp());
		jaxAdresse.setNichtInGemeinde(gesuchstellerAdresse.isNichtInGemeinde());

		return jaxAdresse;
	}

	public Gesuchsteller gesuchstellerToEntity(
		@Nonnull final JaxGesuchsteller gesuchstellerJAXP,
		@Nonnull final Gesuchsteller gesuchsteller
	) {

		requireNonNull(gesuchsteller);
		requireNonNull(gesuchstellerJAXP);

		convertAbstractPersonFieldsToEntity(gesuchstellerJAXP, gesuchsteller);
		gesuchsteller.setMail(gesuchstellerJAXP.getMail());
		gesuchsteller.setTelefon(gesuchstellerJAXP.getTelefon());
		gesuchsteller.setMobile(gesuchstellerJAXP.getMobile());
		gesuchsteller.setTelefonAusland(gesuchstellerJAXP.getTelefonAusland());
		gesuchsteller.setDiplomatenstatus(
			gesuchstellerJAXP.isDiplomatenstatus()
		);
		gesuchsteller.setKorrespondenzSprache(
			gesuchstellerJAXP.getKorrespondenzSprache()
		);
		gesuchsteller.setSozialversicherungsnummer(
			gesuchstellerJAXP.getSozialversicherungsnummer()
		);

		return gesuchsteller;
	}

	private void sortAndAddAdressenToGesuchstellerContainer(
		@Nonnull JaxGesuchstellerContainer gesuchstellerContJAXP,
		@Nonnull GesuchstellerContainer gesuchstellerCont
	) {
		// Zuerst wird geguckt, welche Entities nicht im JAX sind und werden dann geloescht
		for (Iterator<GesuchstellerAdresseContainer> iterator =
			gesuchstellerCont.getAdressen().iterator();
			 iterator.hasNext();) {
			GesuchstellerAdresseContainer next = iterator.next();
			boolean needsToBeRemoved = true;
			for (JaxAdresseContainer jaxAdresse : gesuchstellerContJAXP
				.getAdressen()) {
				if (next.extractIsKorrespondenzAdresse()
					|| next.extractIsRechnungsAdresse()
					|| next.getId()
						.equals(jaxAdresse.getId())) {
					// Korrespondezadresse, Rechnungsadresse und Adressen die gefunden werden, werden nicht geloescht
					needsToBeRemoved = false;
				}
			}
			if (needsToBeRemoved) {
				iterator.remove();
			}
		}
		// Jetzt werden alle Adressen vom Jax auf Entity kopiert
		gesuchstellerContJAXP.getAdressen()
			.forEach(
				jaxAdresse -> gesuchstellerCont.addAdresse(
					toStoreableAddresse(jaxAdresse)
				)
			);

		// Zuletzt werden alle gueltigen Adressen sortiert und mit dem entsprechenden AB und BIS aktualisiert
		List<GesuchstellerAdresseContainer> wohnadressen = gesuchstellerCont
			.getAdressen()
			.stream()
			.filter(
				gesuchstellerAdresse -> !gesuchstellerAdresse
					.extractIsKorrespondenzAdresse()
					&& !gesuchstellerAdresse.extractIsRechnungsAdresse()
			)
			.sorted(
				Comparator.comparing(
					o -> requireNonNull(o.extractGueltigkeit()).getGueltigAb()
				)
			)
			.collect(Collectors.toList());
		for (int i = 0; i < wohnadressen.size(); i++) {
			if ((i < wohnadressen.size() - 1)) {
				requireNonNull(wohnadressen.get(i).extractGueltigkeit())
					.setGueltigBis(
						requireNonNull(
							wohnadressen.get(i + 1).extractGueltigkeit()
						).getGueltigAb().minusDays(1)
					);
			} else {
				requireNonNull(wohnadressen.get(i).extractGueltigkeit())
					.setGueltigBis(Constants.END_OF_TIME); // by default das letzte Datum hat BIS=END_OF_TIME
			}
		}
	}

	@Nonnull
	private GesuchstellerAdresseContainer toStoreableAddresse(
		@Nonnull final JaxAdresseContainer adresseToPrepareForSaving
	) {

		if (adresseToPrepareForSaving.getId() == null) {
			return gesuchstellerAdresseContainerToEntity(
				adresseToPrepareForSaving,
				new GesuchstellerAdresseContainer()
			);
		}

		//wenn schon vorhanden updaten
		GesuchstellerAdresseContainer altAdr =
			gesuchstellerAdresseService.findAdresse(
				adresseToPrepareForSaving.getId()
			)
				.orElseGet(GesuchstellerAdresseContainer::new);

		return gesuchstellerAdresseContainerToEntity(
			adresseToPrepareForSaving,
			altAdr
		);
	}

	public JaxGesuchstellerContainer gesuchstellerContainerToJAX(
		GesuchstellerContainer persistedGesuchstellerCont
	) {
		JaxGesuchstellerContainer jaxGesuchstellerCont =
			new JaxGesuchstellerContainer();
		convertAbstractVorgaengerFieldsToJAX(
			persistedGesuchstellerCont,
			jaxGesuchstellerCont
		);

		if (persistedGesuchstellerCont.getGesuchstellerGS() != null) {
			jaxGesuchstellerCont.setGesuchstellerGS(
				gesuchstellerToJAX(
					persistedGesuchstellerCont.getGesuchstellerGS()
				)
			);
		}
		if (persistedGesuchstellerCont.getGesuchstellerJA() != null) {
			jaxGesuchstellerCont.setGesuchstellerJA(
				gesuchstellerToJAX(
					persistedGesuchstellerCont.getGesuchstellerJA()
				)
			);
		}

		if (!persistedGesuchstellerCont.isNew()) {
			//relationen laden
			final Optional<GesuchstellerAdresseContainer> alternativeAdr =
				gesuchstellerAdresseService.getKorrespondenzAdr(
					persistedGesuchstellerCont.getId()
				);
			final Optional<GesuchstellerAdresseContainer> rechnungsAdr =
				gesuchstellerAdresseService.getRechnungsAdr(
					persistedGesuchstellerCont.getId()
				);
			alternativeAdr.ifPresent(
				adresse -> jaxGesuchstellerCont.setAlternativeAdresse(
					gesuchstellerAdresseContainerToJAX(adresse)
				)
			);
			rechnungsAdr.ifPresent(
				adresse -> jaxGesuchstellerCont.setRechnungsAdresse(
					gesuchstellerAdresseContainerToJAX(adresse)
				)
			);

			jaxGesuchstellerCont.setAdressen(
				gesuchstellerAdresseContainerListToJAX(
					persistedGesuchstellerCont.getAdressen()
						.stream()
						.filter(
							gesuchstellerAdresse -> !gesuchstellerAdresse
								.extractIsKorrespondenzAdresse()
								&& !gesuchstellerAdresse
									.extractIsRechnungsAdresse()
						)
						.sorted((o1, o2) -> {
							if (o1.extractGueltigkeit() == null
								&& o2.extractGueltigkeit() == null) {
								return 0;
							}
							if (o1.extractGueltigkeit() == null) {
								return 1;
							}
							if (o2.extractGueltigkeit() == null) {
								return -1;
							}
							return requireNonNull(o1.extractGueltigkeit())
								.getGueltigAb()
								.compareTo(
									requireNonNull(o2.extractGueltigkeit())
										.getGueltigAb()
								);
						})
						.collect(Collectors.toList())
				)
			);
		}

		// Finanzielle Situation
		if (persistedGesuchstellerCont.getFinanzielleSituationContainer()
			!= null) {
			final JaxFinanzielleSituationContainer jaxFinanzielleSituationContainer =
				finanzielleSituationConverter
					.finanzielleSituationContainerToJAX(
						persistedGesuchstellerCont
							.getFinanzielleSituationContainer()
					);
			jaxGesuchstellerCont.setFinanzielleSituationContainer(
				jaxFinanzielleSituationContainer
			);
		}
		// Erwerbspensen
		final Collection<ErwerbspensumContainer> persistedPensen =
			persistedGesuchstellerCont.getErwerbspensenContainers();
		final List<JaxErwerbspensumContainer> listOfPensen =
			persistedPensen.stream()
				.map(erwerbspensumConverter::erwerbspensumContainerToJAX)
				.collect(Collectors.toList());
		jaxGesuchstellerCont.setErwerbspensenContainers(listOfPensen);

		// Einkommensverschlechterung
		if (persistedGesuchstellerCont.getEinkommensverschlechterungContainer()
			!= null) {
			final JaxEinkommensverschlechterungContainer jaxEinkVerContainer =
				einkommensverschlechterungConverter
					.einkommensverschlechterungContainerToJAX(
						persistedGesuchstellerCont
							.getEinkommensverschlechterungContainer()
					);
			jaxGesuchstellerCont.setEinkommensverschlechterungContainer(
				jaxEinkVerContainer
			);
		}

		return jaxGesuchstellerCont;
	}

	private List<JaxAdresseContainer> gesuchstellerAdresseContainerListToJAX(
		@Nonnull Collection<GesuchstellerAdresseContainer> adressen
	) {

		return adressen.stream()
			.map(this::gesuchstellerAdresseContainerToJAX)
			.collect(Collectors.toList());
	}

	private JaxAdresseContainer gesuchstellerAdresseContainerToJAX(
		GesuchstellerAdresseContainer persistedAdresse
	) {
		JaxAdresseContainer jaxAdresse = new JaxAdresseContainer();
		convertAbstractVorgaengerFieldsToJAX(persistedAdresse, jaxAdresse);

		if (persistedAdresse.getGesuchstellerAdresseGS() != null) {
			jaxAdresse.setAdresseGS(
				gesuchstellerAdresseToJAX(
					persistedAdresse.getGesuchstellerAdresseGS()
				)
			);
		}
		if (persistedAdresse.getGesuchstellerAdresseJA() != null) {
			jaxAdresse.setAdresseJA(
				gesuchstellerAdresseToJAX(
					persistedAdresse.getGesuchstellerAdresseJA()
				)
			);
		}
		return jaxAdresse;
	}

	@Nonnull
	public JaxGesuchsteller gesuchstellerToJAX(
		@Nonnull final Gesuchsteller persistedGesuchsteller
	) {
		Validate.isTrue(
			!persistedGesuchsteller.isNew(),
			"Gesuchsteller kann nicht nach REST transformiert werden weil sie noch "
				+
				"nicht persistiert wurde; Grund dafuer ist, dass wir die aktuelle Wohnadresse aus der Datenbank lesen "
				+ "wollen"
		);
		final JaxGesuchsteller jaxGesuchsteller = new JaxGesuchsteller();
		convertAbstractPersonFieldsToJAX(
			persistedGesuchsteller,
			jaxGesuchsteller
		);
		jaxGesuchsteller.setMail(persistedGesuchsteller.getMail());
		jaxGesuchsteller.setTelefon(persistedGesuchsteller.getTelefon());
		jaxGesuchsteller.setMobile(persistedGesuchsteller.getMobile());
		jaxGesuchsteller.setTelefonAusland(
			persistedGesuchsteller.getTelefonAusland()
		);
		jaxGesuchsteller.setDiplomatenstatus(
			persistedGesuchsteller.isDiplomatenstatus()
		);
		jaxGesuchsteller.setKorrespondenzSprache(
			persistedGesuchsteller.getKorrespondenzSprache()
		);
		jaxGesuchsteller.setSozialversicherungsnummer(
			persistedGesuchsteller.getSozialversicherungsnummer()
		);

		return jaxGesuchsteller;
	}

	public GesuchstellerAdresseContainer gesuchstellerAdresseContainerToEntity(
		JaxAdresseContainer jaxAdresseCont,
		GesuchstellerAdresseContainer adresseCont
	) {

		requireNonNull(jaxAdresseCont);
		requireNonNull(adresseCont);

		convertAbstractVorgaengerFieldsToEntity(jaxAdresseCont, adresseCont);
		// ein einmal erstellter GS Container kann nie mehr entfernt werden, daher mergen wir hier nichts wenn null
		// kommt vom client
		if (jaxAdresseCont.getAdresseGS() != null) {
			GesuchstellerAdresse gesuchstellerAdresseGS =
				new GesuchstellerAdresse();
			if (adresseCont.getGesuchstellerAdresseGS() != null) {
				gesuchstellerAdresseGS = adresseCont
					.getGesuchstellerAdresseGS();
			}
			adresseCont.setGesuchstellerAdresseGS(
				gesuchstellerAdresseToEntity(
					jaxAdresseCont.getAdresseGS(),
					gesuchstellerAdresseGS
				)
			);
		}
		// ein erstellter AdresseJA Container kann durch das Jugendamt entfernt werden wenn es sich um eine
		// Korrespondenzaddr oder eine Rechnungsaddr handelt
		if (jaxAdresseCont.getAdresseJA() != null) {
			GesuchstellerAdresse gesuchstellerAdresseJA =
				new GesuchstellerAdresse();
			if (adresseCont.getGesuchstellerAdresseJA() != null) {
				gesuchstellerAdresseJA = adresseCont
					.getGesuchstellerAdresseJA();
			}
			adresseCont.setGesuchstellerAdresseJA(
				gesuchstellerAdresseToEntity(
					jaxAdresseCont.getAdresseJA(),
					gesuchstellerAdresseJA
				)
			);
		} else {
			Validate.isTrue(
				adresseCont.extractIsKorrespondenzAdresse()
					|| adresseCont.extractIsRechnungsAdresse(),
				"Nur bei der Korrespondenz- oder "
					+ "Rechnungsadresse kann der AdresseJA Container entfernt werden"
			);
			adresseCont.setGesuchstellerAdresseJA(null);
		}

		return adresseCont;
	}

	public GesuchstellerContainer gesuchstellerContainerToEntity(
		JaxGesuchstellerContainer jaxGesuchstellerCont,
		GesuchstellerContainer gesuchstellerCont
	) {
		requireNonNull(gesuchstellerCont);
		requireNonNull(jaxGesuchstellerCont);
		requireNonNull(
			jaxGesuchstellerCont.getAdressen(),
			"Adressen muessen gesetzt sein"
		);

		convertAbstractVorgaengerFieldsToEntity(
			jaxGesuchstellerCont,
			gesuchstellerCont
		);
		//kind daten koennen nicht verschwinden
		if (jaxGesuchstellerCont.getGesuchstellerGS() != null) {
			Gesuchsteller gesuchstellerGS = new Gesuchsteller();
			if (gesuchstellerCont.getGesuchstellerGS() != null) {
				gesuchstellerGS = gesuchstellerCont.getGesuchstellerGS();
			}
			gesuchstellerCont.setGesuchstellerGS(
				gesuchstellerToEntity(
					jaxGesuchstellerCont.getGesuchstellerGS(),
					gesuchstellerGS
				)
			);
		}
		if (jaxGesuchstellerCont.getGesuchstellerJA() != null) {
			Gesuchsteller gesuchstellerJA = new Gesuchsteller();
			if (gesuchstellerCont.getGesuchstellerJA() != null) {
				gesuchstellerJA = gesuchstellerCont.getGesuchstellerJA();
			}
			gesuchstellerCont.setGesuchstellerJA(
				gesuchstellerToEntity(
					jaxGesuchstellerCont.getGesuchstellerJA(),
					gesuchstellerJA
				)
			);
		}

		//Relationen
		//Wir fuehren derzeit immer maximal  eine alternative Korrespondenzadressse -> diese updaten wenn vorhanden
		if (jaxGesuchstellerCont.getAlternativeAdresse() != null) {
			final GesuchstellerAdresseContainer currentAltAdr =
				gesuchstellerAdresseService
					.getKorrespondenzAdr(gesuchstellerCont.getId())
					.orElse(new GesuchstellerAdresseContainer());
			final GesuchstellerAdresseContainer altAddrToMerge =
				gesuchstellerAdresseContainerToEntity(
					jaxGesuchstellerCont.getAlternativeAdresse(),
					currentAltAdr
				);
			gesuchstellerCont.addAdresse(altAddrToMerge);
		} else {
			//else case: Wenn das haeklein "Zustell / Postadresse" auf client weggenommen wird muss die
			// Korrespondezadr auf dem Server geloescht werden.
			gesuchstellerCont.getAdressen()
				.removeIf(
					GesuchstellerAdresseContainer::extractIsKorrespondenzAdresse
				);
		}
		if (jaxGesuchstellerCont.getRechnungsAdresse() != null) {
			final GesuchstellerAdresseContainer currentrechnungsAdr =
				gesuchstellerAdresseService
					.getRechnungsAdr(gesuchstellerCont.getId())
					.orElse(new GesuchstellerAdresseContainer());
			final GesuchstellerAdresseContainer rechnungsAddrToMerge =
				gesuchstellerAdresseContainerToEntity(
					jaxGesuchstellerCont.getRechnungsAdresse(),
					currentrechnungsAdr
				);
			gesuchstellerCont.addAdresse(rechnungsAddrToMerge);
		} else {
			//else case: Wenn das haeklein "abweichende Rchnungsadresse" auf client weggenommen wird muss diese
			// adresse auf dem Server geloescht werden.
			gesuchstellerCont.getAdressen()
				.removeIf(
					GesuchstellerAdresseContainer::extractIsRechnungsAdresse
				);
		}
		sortAndAddAdressenToGesuchstellerContainer(
			jaxGesuchstellerCont,
			gesuchstellerCont
		);

		// Finanzielle Situation
		if (jaxGesuchstellerCont.getFinanzielleSituationContainer() != null) {
			gesuchstellerCont.setFinanzielleSituationContainer(
				finanzielleSituationConverter
					.finanzielleSituationContainerToStorableEntity(
						jaxGesuchstellerCont.getFinanzielleSituationContainer(),
						gesuchstellerCont.getFinanzielleSituationContainer()
					)
			);
		}
		//Erwerbspensum
		requireNonNull(jaxGesuchstellerCont.getErwerbspensenContainers())
			.stream()
			.map(
				erwerbspensumContainer -> erwerbspensumConverter
					.erwerbspensumContainerToStoreableEntity(
						erwerbspensumContainer
					)
			)
			.forEach(gesuchstellerCont::addErwerbspensumContainer);

		//Einkommensverschlechterung
		final JaxEinkommensverschlechterungContainer einkommensverschlechterungContainer =
			jaxGesuchstellerCont.getEinkommensverschlechterungContainer();
		if (einkommensverschlechterungContainer != null) {
			gesuchstellerCont.setEinkommensverschlechterungContainer(
				einkommensverschlechterungConverter
					.einkommensverschlechterungContainerToStorableEntity(
						einkommensverschlechterungContainer
					)
			);
		}

		return gesuchstellerCont;
	}
}
