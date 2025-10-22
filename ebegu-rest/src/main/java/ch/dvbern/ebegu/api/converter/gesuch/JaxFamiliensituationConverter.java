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

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.dtos.JaxFamiliensituation;
import ch.dvbern.ebegu.api.dtos.JaxFamiliensituationContainer;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxFamiliensituationConverter extends AbstractBaseConverter {
	@Inject
	private JaxSozialhilfeZeitraumConverter jaxSozialhilfeZeitraumConverter;

	public Familiensituation familiensituationToEntity(
		@Nonnull final JaxFamiliensituation familiensituationJAXP,
		@Nonnull final Familiensituation familiensituation
	) {

		requireNonNull(familiensituation);
		requireNonNull(familiensituationJAXP);

		if (familiensituationJAXP.getIban() != null
			|| familiensituationJAXP.getKontoinhaber() != null) {
			convertFamiliensituationAuszahlungsdaten(
				familiensituationJAXP,
				familiensituation
			);
		}

		convertAbstractVorgaengerFieldsToEntity(
			familiensituationJAXP,
			familiensituation
		);
		familiensituation.setFamilienstatus(
			familiensituationJAXP.getFamilienstatus()
		);
		familiensituation.setGemeinsameSteuererklaerung(
			familiensituationJAXP.getGemeinsameSteuererklaerung()
		);
		familiensituation.setAenderungPer(
			familiensituationJAXP.getAenderungPer()
		);
		familiensituation.setStartKonkubinat(
			familiensituationJAXP.getStartKonkubinat()
		);
		familiensituation.setSozialhilfeBezueger(
			familiensituationJAXP.getSozialhilfeBezueger()
		);
		familiensituation.setZustaendigeAmtsstelle(
			familiensituationJAXP.getZustaendigeAmtsstelle()
		);
		familiensituation.setNameBetreuer(
			familiensituationJAXP.getNameBetreuer()
		);
		familiensituation.setVerguenstigungGewuenscht(
			familiensituationJAXP.getVerguenstigungGewuenscht()
		);
		familiensituation.setGesuchstellerKardinalitaet(
			familiensituationJAXP.getGesuchstellerKardinalitaet()
		);
		familiensituation.setFkjvFamSit(familiensituationJAXP.isFkjvFamSit());
		familiensituation.setMinDauerKonkubinat(
			familiensituationJAXP.getMinDauerKonkubinat()
		);
		familiensituation.setGeteilteObhut(
			familiensituationJAXP.getGeteilteObhut()
		);
		familiensituation.setUnterhaltsvereinbarung(
			familiensituationJAXP.getUnterhaltsvereinbarung()
		);
		familiensituation.setUnterhaltsvereinbarungBemerkung(
			familiensituationJAXP.getUnterhaltsvereinbarungBemerkung()
		);
		familiensituation.setPartnerIdentischMitVorgesuch(
			familiensituationJAXP.getPartnerIdentischMitVorgesuch()
		);
		familiensituation.setGemeinsamerHaushaltMitObhutsberechtigterPerson(
			familiensituationJAXP
				.getGemeinsamerHaushaltMitObhutsberechtigterPerson()
		);
		familiensituation.setGemeinsamerHaushaltMitPartner(
			familiensituationJAXP.getGemeinsamerHaushaltMitPartner()
		);
		familiensituation.setAuszahlungAusserhalbVonKibon(
			familiensituationJAXP.isAuszahlungAusserhalbVonKibon()
		);
		return familiensituation;
	}

	private void convertFamiliensituationAuszahlungsdaten(
		@Nonnull final JaxFamiliensituation familiensituationJAXP,
		@Nonnull final Familiensituation familiensituation
	) {
		Objects.requireNonNull(
			familiensituationJAXP.getKontoinhaber(),
			"IBAN muss erfasst sein"
		);
		Objects.requireNonNull(
			familiensituationJAXP.getKontoinhaber(),
			"Kontoinhaber muss erfasst sein"
		);
		if (familiensituation.getAuszahlungsdaten() == null) {
			familiensituation.setAuszahlungsdaten(new Auszahlungsdaten());
		}
		familiensituation.getAuszahlungsdaten()
			.setIban(new IBAN(familiensituationJAXP.getIban()));
		familiensituation.getAuszahlungsdaten()
			.setKontoinhaber(familiensituationJAXP.getKontoinhaber());
		familiensituation.getAuszahlungsdaten()
			.setInfomaKreditorennummer(
				familiensituationJAXP.getInfomaKreditorennummer()
			);
		familiensituation.getAuszahlungsdaten()
			.setInfomaBankcode(familiensituationJAXP.getInfomaBankcode());
		Adresse convertedAdresse = null;
		if (familiensituationJAXP.getZahlungsadresse() != null) {
			Adresse a =
				Optional.ofNullable(
					familiensituation.getAuszahlungsdaten()
						.getAdresseKontoinhaber()
				)
					.orElseGet(Adresse::new);
			convertedAdresse = adresseToEntity(
				familiensituationJAXP.getZahlungsadresse(),
				a
			);
		}
		familiensituation.getAuszahlungsdaten()
			.setAdresseKontoinhaber(convertedAdresse);
		familiensituation.setAbweichendeZahlungsadresse(
			familiensituationJAXP.isAbweichendeZahlungsadresse()
		);
		familiensituation.setPartnerIdentischMitVorgesuch(
			familiensituationJAXP.getPartnerIdentischMitVorgesuch()
		);
	}

	public JaxFamiliensituation familiensituationToJAX(
		@Nonnull final Familiensituation persistedFamiliensituation
	) {
		final JaxFamiliensituation jaxFamiliensituation =
			new JaxFamiliensituation();
		convertAbstractVorgaengerFieldsToJAX(
			persistedFamiliensituation,
			jaxFamiliensituation
		);
		jaxFamiliensituation.setFamilienstatus(
			persistedFamiliensituation.getFamilienstatus()
		);
		jaxFamiliensituation.setGemeinsameSteuererklaerung(
			persistedFamiliensituation.getGemeinsameSteuererklaerung()
		);
		jaxFamiliensituation.setAenderungPer(
			persistedFamiliensituation.getAenderungPer()
		);
		jaxFamiliensituation.setStartKonkubinat(
			persistedFamiliensituation.getStartKonkubinat()
		);
		jaxFamiliensituation.setSozialhilfeBezueger(
			persistedFamiliensituation.getSozialhilfeBezueger()
		);
		jaxFamiliensituation.setPartnerIdentischMitVorgesuch(
			persistedFamiliensituation.getPartnerIdentischMitVorgesuch()
		);
		jaxFamiliensituation.setNameBetreuer(
			persistedFamiliensituation.getNameBetreuer()
		);
		jaxFamiliensituation.setZustaendigeAmtsstelle(
			persistedFamiliensituation.getZustaendigeAmtsstelle()
		);
		jaxFamiliensituation.setVerguenstigungGewuenscht(
			persistedFamiliensituation.getVerguenstigungGewuenscht()
		);
		jaxFamiliensituation.setKeineMahlzeitenverguenstigungBeantragt(
			persistedFamiliensituation
				.isKeineMahlzeitenverguenstigungBeantragt()
		);
		jaxFamiliensituation.setKeineMahlzeitenverguenstigungBeantragtEditable(
			persistedFamiliensituation
				.isKeineMahlzeitenverguenstigungBeantragtEditable()
		);
		jaxFamiliensituation.setAbweichendeZahlungsadresse(
			persistedFamiliensituation.isAbweichendeZahlungsadresse()
		);
		final Auszahlungsdaten persistedAuszahlungsdaten =
			persistedFamiliensituation.getAuszahlungsdaten();
		if (persistedAuszahlungsdaten != null) {
			jaxFamiliensituation.setIban(
				persistedAuszahlungsdaten.extractIbanAsString()
			);
			jaxFamiliensituation.setKontoinhaber(
				persistedAuszahlungsdaten.getKontoinhaber()
			);
			if (persistedAuszahlungsdaten.getAdresseKontoinhaber() != null) {
				jaxFamiliensituation.setZahlungsadresse(
					adresseToJAX(
						persistedAuszahlungsdaten.getAdresseKontoinhaber()
					)
				);
			}
		}
		final Auszahlungsdaten persistedAuszahlungsdatenInfoma =
			persistedFamiliensituation.getAuszahlungsdaten();
		if (persistedAuszahlungsdatenInfoma != null) {
			jaxFamiliensituation.setIban(
				persistedAuszahlungsdatenInfoma.extractIbanAsString()
			);
			jaxFamiliensituation.setInfomaKreditorennummer(
				persistedAuszahlungsdatenInfoma.getInfomaKreditorennummer()
			);
			jaxFamiliensituation.setInfomaBankcode(
				persistedAuszahlungsdatenInfoma.getInfomaBankcode()
			);
			jaxFamiliensituation.setKontoinhaber(
				persistedAuszahlungsdatenInfoma.getKontoinhaber()
			);
			if (persistedAuszahlungsdatenInfoma.getAdresseKontoinhaber()
				!= null) {
				jaxFamiliensituation.setZahlungsadresse(
					adresseToJAX(
						persistedAuszahlungsdatenInfoma.getAdresseKontoinhaber()
					)
				);
			}
		}
		jaxFamiliensituation.setGesuchstellerKardinalitaet(
			persistedFamiliensituation.getGesuchstellerKardinalitaet()
		);
		jaxFamiliensituation.setFkjvFamSit(
			persistedFamiliensituation.isFkjvFamSit()
		);
		jaxFamiliensituation.setMinDauerKonkubinat(
			persistedFamiliensituation.getMinDauerKonkubinat()
		);
		jaxFamiliensituation.setUnterhaltsvereinbarung(
			persistedFamiliensituation.getUnterhaltsvereinbarung()
		);
		jaxFamiliensituation.setUnterhaltsvereinbarungBemerkung(
			persistedFamiliensituation.getUnterhaltsvereinbarungBemerkung()
		);
		jaxFamiliensituation.setGeteilteObhut(
			persistedFamiliensituation.getGeteilteObhut()
		);
		jaxFamiliensituation.setGemeinsamerHaushaltMitObhutsberechtigterPerson(
			persistedFamiliensituation
				.getGemeinsamerHaushaltMitObhutsberechtigterPerson()
		);
		jaxFamiliensituation.setGemeinsamerHaushaltMitPartner(
			persistedFamiliensituation.getGemeinsamerHaushaltMitPartner()
		);
		jaxFamiliensituation.setAuszahlungAusserhalbVonKibon(
			persistedFamiliensituation.isAuszahlungAusserhalbVonKibon()
		);
		return jaxFamiliensituation;
	}

	public FamiliensituationContainer familiensituationContainerToEntity(
		@Nonnull final JaxFamiliensituationContainer containerJAX,
		@Nonnull final FamiliensituationContainer container
	) {

		requireNonNull(container);
		requireNonNull(containerJAX);

		convertAbstractVorgaengerFieldsToEntity(containerJAX, container);
		Familiensituation famsitToMergeWith;

		if (containerJAX.getFamiliensituationGS() != null) {
			famsitToMergeWith = Optional.ofNullable(
				container.getFamiliensituationGS()
			)
				.orElseGet(Familiensituation::new);
			container.setFamiliensituationGS(
				familiensituationToEntity(
					containerJAX.getFamiliensituationGS(),
					famsitToMergeWith
				)
			);
		}
		if (containerJAX.getFamiliensituationJA() != null) {
			famsitToMergeWith = Optional.ofNullable(
				container.getFamiliensituationJA()
			)
				.orElseGet(Familiensituation::new);
			container.setFamiliensituationJA(
				familiensituationToEntity(
					containerJAX.getFamiliensituationJA(),
					famsitToMergeWith
				)
			);
		}
		if (containerJAX.getFamiliensituationErstgesuch() != null) {
			famsitToMergeWith = Optional.ofNullable(
				container.getFamiliensituationErstgesuch()
			)
				.orElseGet(Familiensituation::new);
			container.setFamiliensituationErstgesuch(
				familiensituationToEntity(
					containerJAX.getFamiliensituationErstgesuch(),
					famsitToMergeWith
				)
			);
		}
		if (containerJAX.getSozialhilfeZeitraumContainers() != null) {
			jaxSozialhilfeZeitraumConverter
				.sozialhilfeZeitraumContainersToEntity(
					containerJAX.getSozialhilfeZeitraumContainers(),
					container.getSozialhilfeZeitraumContainers()
				);
		}

		return container;
	}

	public JaxFamiliensituationContainer familiensituationContainerToJAX(
		final FamiliensituationContainer persistedFamiliensituation
	) {
		final JaxFamiliensituationContainer jaxfc =
			new JaxFamiliensituationContainer();
		convertAbstractVorgaengerFieldsToJAX(persistedFamiliensituation, jaxfc);
		if (persistedFamiliensituation.getFamiliensituationGS() != null) {
			jaxfc.setFamiliensituationGS(
				familiensituationToJAX(
					persistedFamiliensituation.getFamiliensituationGS()
				)
			);
		}
		if (persistedFamiliensituation.getFamiliensituationJA() != null) {
			jaxfc.setFamiliensituationJA(
				familiensituationToJAX(
					persistedFamiliensituation.getFamiliensituationJA()
				)
			);
		}
		if (persistedFamiliensituation.getFamiliensituationErstgesuch()
			!= null) {
			jaxfc.setFamiliensituationErstgesuch(
				familiensituationToJAX(
					persistedFamiliensituation.getFamiliensituationErstgesuch()
				)
			);
		}

		jaxfc.setSozialhilfeZeitraumContainers(
			jaxSozialhilfeZeitraumConverter.sozialhilfeZeitraumContainersToJAX(
				persistedFamiliensituation.getSozialhilfeZeitraumContainers()
			)
		);

		return jaxfc;
	}

}
