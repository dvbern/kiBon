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

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.dtos.finanziellesituation.JaxAbstractFinanzielleSituation;
import ch.dvbern.ebegu.api.dtos.finanziellesituation.JaxFinSitZusatzangabenAppenzell;
import ch.dvbern.ebegu.api.dtos.finanziellesituation.JaxFinanzielleSituationSelbstdeklaration;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.FinSitZusatzangabenAppenzell;
import ch.dvbern.ebegu.entities.FinanzielleSituationSelbstdeklaration;

import static java.util.Objects.requireNonNull;

public abstract class AbstractBaseFinanzielleSituationConverter extends
	AbstractBaseConverter {

	public AbstractFinanzielleSituation abstractFinanzielleSituationToEntity(
		@Nonnull final JaxAbstractFinanzielleSituation abstractFinanzielleSituationJAXP,
		@Nonnull final AbstractFinanzielleSituation abstractFinanzielleSituation
	) {

		requireNonNull(abstractFinanzielleSituation);
		requireNonNull(abstractFinanzielleSituationJAXP);

		convertAbstractVorgaengerFieldsToEntity(
			abstractFinanzielleSituationJAXP,
			abstractFinanzielleSituation
		);
		abstractFinanzielleSituation.setNettolohn(
			abstractFinanzielleSituationJAXP.getNettolohn()
		);
		abstractFinanzielleSituation.setFamilienzulage(
			abstractFinanzielleSituationJAXP.getFamilienzulage()
		);
		abstractFinanzielleSituation.setErsatzeinkommen(
			abstractFinanzielleSituationJAXP.getErsatzeinkommen()
		);
		abstractFinanzielleSituation.setErhalteneAlimente(
			abstractFinanzielleSituationJAXP.getErhalteneAlimente()
		);
		abstractFinanzielleSituation.setBruttovermoegen(
			abstractFinanzielleSituationJAXP.getBruttovermoegen()
		);
		abstractFinanzielleSituation.setSchulden(
			abstractFinanzielleSituationJAXP.getSchulden()
		);
		abstractFinanzielleSituation.setGeschaeftsgewinnBasisjahr(
			abstractFinanzielleSituationJAXP.getGeschaeftsgewinnBasisjahr()
		);
		abstractFinanzielleSituation.setGeschaeftsgewinnBasisjahrMinus1(
			abstractFinanzielleSituationJAXP
				.getGeschaeftsgewinnBasisjahrMinus1()
		);
		abstractFinanzielleSituation.setGeleisteteAlimente(
			abstractFinanzielleSituationJAXP.getGeleisteteAlimente()
		);

		abstractFinanzielleSituation
			.setEinkommenInVereinfachtemVerfahrenAbgerechnet(
				abstractFinanzielleSituationJAXP
					.getEinkommenInVereinfachtemVerfahrenAbgerechnet()
			);
		abstractFinanzielleSituation
			.setAmountEinkommenInVereinfachtemVerfahrenAbgerechnet(
				abstractFinanzielleSituationJAXP
					.getAmountEinkommenInVereinfachtemVerfahrenAbgerechnet()
			);
		abstractFinanzielleSituation.setGewinnungskosten(
			abstractFinanzielleSituationJAXP.getGewinnungskosten()
		);
		abstractFinanzielleSituation.setBruttoertraegeVermoegen(
			abstractFinanzielleSituationJAXP.getBruttoertraegeVermoegen()
		);
		abstractFinanzielleSituation.setNettoertraegeErbengemeinschaft(
			abstractFinanzielleSituationJAXP.getNettoertraegeErbengemeinschaft()
		);
		abstractFinanzielleSituation.setNettoVermoegen(
			abstractFinanzielleSituationJAXP.getNettoVermoegen()
		);
		abstractFinanzielleSituation.setAbzugSchuldzinsen(
			abstractFinanzielleSituationJAXP.getAbzugSchuldzinsen()
		);

		abstractFinanzielleSituation.setSteuerbaresEinkommen(
			abstractFinanzielleSituationJAXP.getSteuerbaresEinkommen()
		);
		abstractFinanzielleSituation.setSteuerbaresVermoegen(
			abstractFinanzielleSituationJAXP.getSteuerbaresVermoegen()
		);
		abstractFinanzielleSituation.setEinkaeufeVorsorge(
			abstractFinanzielleSituationJAXP.getEinkaeufeVorsorge()
		);
		abstractFinanzielleSituation.setGeschaeftsverlust(
			abstractFinanzielleSituationJAXP.getGeschaeftsverlust()
		);
		abstractFinanzielleSituation.setAbzuegeLiegenschaft(
			abstractFinanzielleSituationJAXP.getAbzuegeLiegenschaft()
		);
		abstractFinanzielleSituation.setLiegenschaftsErtraege(
			abstractFinanzielleSituationJAXP.getLiegenschaftsErtraege()
		);
		abstractFinanzielleSituation.setBruttoLohn(
			abstractFinanzielleSituationJAXP.getBruttoLohn()
		);

		if (abstractFinanzielleSituationJAXP.getSelbstdeklaration() != null) {
			FinanzielleSituationSelbstdeklaration selbstdeklarationToMerge =
				Optional.ofNullable(
					abstractFinanzielleSituation.getSelbstdeklaration()
				)
					.orElse(new FinanzielleSituationSelbstdeklaration());
			abstractFinanzielleSituation.setSelbstdeklaration(
				finanzielleSituationSelbstdeklarationToEntity(
					abstractFinanzielleSituationJAXP.getSelbstdeklaration(),
					selbstdeklarationToMerge
				)
			);
		}

		if (abstractFinanzielleSituationJAXP.getFinSitZusatzangabenAppenzell()
			!= null) {
			FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzellToMerge =
				Optional.ofNullable(
					abstractFinanzielleSituation
						.getFinSitZusatzangabenAppenzell()
				)
					.orElse(new FinSitZusatzangabenAppenzell());
			abstractFinanzielleSituation.setFinSitZusatzangabenAppenzell(
				finSitZusatzangabenAppenzellToEntity(
					abstractFinanzielleSituationJAXP
						.getFinSitZusatzangabenAppenzell(),
					finSitZusatzangabenAppenzellToMerge
				)
			);
		}

		abstractFinanzielleSituation
			.setErsatzeinkommenSelbststaendigkeitBasisjahr(
				abstractFinanzielleSituationJAXP
					.getErsatzeinkommenSelbststaendigkeitBasisjahr()
			);
		abstractFinanzielleSituation
			.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus1(
				abstractFinanzielleSituationJAXP
					.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1()
			);

		return abstractFinanzielleSituation;
	}

	private FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzellToEntity(
		JaxFinSitZusatzangabenAppenzell jaxFinSitZusatzangabenAppenzell,
		FinSitZusatzangabenAppenzell finSitZusatzangabenAppenzell
	) {

		convertAbstractVorgaengerFieldsToEntity(
			jaxFinSitZusatzangabenAppenzell,
			finSitZusatzangabenAppenzell
		);

		finSitZusatzangabenAppenzell.setSaeule3a(
			jaxFinSitZusatzangabenAppenzell.getSaeule3a()
		);
		finSitZusatzangabenAppenzell.setSaeule3aNichtBvg(
			jaxFinSitZusatzangabenAppenzell.getSaeule3aNichtBvg()
		);
		finSitZusatzangabenAppenzell.setBeruflicheVorsorge(
			jaxFinSitZusatzangabenAppenzell.getBeruflicheVorsorge()
		);
		finSitZusatzangabenAppenzell.setLiegenschaftsaufwand(
			jaxFinSitZusatzangabenAppenzell.getLiegenschaftsaufwand()
		);
		finSitZusatzangabenAppenzell.setEinkuenfteBgsa(
			jaxFinSitZusatzangabenAppenzell.getEinkuenfteBgsa()
		);
		finSitZusatzangabenAppenzell.setVorjahresverluste(
			jaxFinSitZusatzangabenAppenzell.getVorjahresverluste()
		);
		finSitZusatzangabenAppenzell.setPolitischeParteiSpende(
			jaxFinSitZusatzangabenAppenzell.getPolitischeParteiSpende()
		);
		finSitZusatzangabenAppenzell.setLeistungAnJuristischePersonen(
			jaxFinSitZusatzangabenAppenzell.getLeistungAnJuristischePersonen()
		);
		finSitZusatzangabenAppenzell.setSteuerbaresEinkommen(
			jaxFinSitZusatzangabenAppenzell.getSteuerbaresEinkommen()
		);
		finSitZusatzangabenAppenzell.setSteuerbaresVermoegen(
			jaxFinSitZusatzangabenAppenzell.getSteuerbaresVermoegen()
		);

		if (jaxFinSitZusatzangabenAppenzell.getZusatzangabenPartner() != null) {
			FinSitZusatzangabenAppenzell toMerge = Optional.ofNullable(
				finSitZusatzangabenAppenzell.getZusatzangabenPartner()
			)
				.orElse(new FinSitZusatzangabenAppenzell());
			finSitZusatzangabenAppenzell.setZusatzangabenPartner(
				finSitZusatzangabenAppenzellToEntity(
					jaxFinSitZusatzangabenAppenzell.getZusatzangabenPartner(),
					toMerge
				)
			);
		} else {
			finSitZusatzangabenAppenzell.setZusatzangabenPartner(null);
		}

		return finSitZusatzangabenAppenzell;
	}

	private FinanzielleSituationSelbstdeklaration finanzielleSituationSelbstdeklarationToEntity(
		JaxFinanzielleSituationSelbstdeklaration jaxSelbstdeklaration,
		FinanzielleSituationSelbstdeklaration selbstdeklaration
	) {

		convertAbstractVorgaengerFieldsToEntity(
			jaxSelbstdeklaration,
			selbstdeklaration
		);
		selbstdeklaration.setEinkunftErwerb(
			jaxSelbstdeklaration.getEinkunftErwerb()
		);
		selbstdeklaration.setEinkunftVersicherung(
			jaxSelbstdeklaration.getEinkunftVersicherung()
		);
		selbstdeklaration.setEinkunftWertschriften(
			jaxSelbstdeklaration.getEinkunftWertschriften()
		);
		selbstdeklaration.setEinkunftUnterhaltsbeitragKinder(
			jaxSelbstdeklaration.getEinkunftUnterhaltsbeitragKinder()
		);
		selbstdeklaration.setEinkunftUeberige(
			jaxSelbstdeklaration.getEinkunftUeberige()
		);
		selbstdeklaration.setEinkunftLiegenschaften(
			jaxSelbstdeklaration.getEinkunftLiegenschaften()
		);
		selbstdeklaration.setAbzugBerufsauslagen(
			jaxSelbstdeklaration.getAbzugBerufsauslagen()
		);
		selbstdeklaration.setAbzugSchuldzinsen(
			jaxSelbstdeklaration.getAbzugSchuldzinsen()
		);
		selbstdeklaration.setAbzugUnterhaltsbeitragKinder(
			jaxSelbstdeklaration.getAbzugUnterhaltsbeitragKinder()
		);
		selbstdeklaration.setAbzugSaeule3A(
			jaxSelbstdeklaration.getAbzugSaeule3A()
		);
		selbstdeklaration.setAbzugVersicherungspraemien(
			jaxSelbstdeklaration.getAbzugVersicherungspraemien()
		);
		selbstdeklaration.setAbzugKrankheitsUnfallKosten(
			jaxSelbstdeklaration.getAbzugKrankheitsUnfallKosten()
		);
		selbstdeklaration.setSonderabzugErwerbstaetigkeitEhegatten(
			jaxSelbstdeklaration.getSonderabzugErwerbstaetigkeitEhegatten()
		);
		selbstdeklaration.setAbzugKinderVorschule(
			jaxSelbstdeklaration.getAbzugKinderVorschule()
		);
		selbstdeklaration.setAbzugKinderSchule(
			jaxSelbstdeklaration.getAbzugKinderSchule()
		);
		selbstdeklaration.setAbzugEigenbetreuung(
			jaxSelbstdeklaration.getAbzugEigenbetreuung()
		);
		selbstdeklaration.setAbzugFremdbetreuung(
			jaxSelbstdeklaration.getAbzugFremdbetreuung()
		);
		selbstdeklaration.setAbzugErwerbsunfaehigePersonen(
			jaxSelbstdeklaration.getAbzugErwerbsunfaehigePersonen()
		);
		selbstdeklaration.setVermoegen(jaxSelbstdeklaration.getVermoegen());
		selbstdeklaration.setAbzugSteuerfreierBetragErwachsene(
			jaxSelbstdeklaration.getAbzugSteuerfreierBetragErwachsene()
		);
		selbstdeklaration.setAbzugSteuerfreierBetragKinder(
			jaxSelbstdeklaration.getAbzugSteuerfreierBetragKinder()
		);
		return selbstdeklaration;
	}

	protected void abstractFinanzielleSituationToJAX(
		@Nullable final AbstractFinanzielleSituation persistedAbstractFinanzielleSituation,
		JaxAbstractFinanzielleSituation jaxAbstractFinanzielleSituation
	) {

		if (persistedAbstractFinanzielleSituation == null) {
			return;
		}

		convertAbstractVorgaengerFieldsToJAX(
			persistedAbstractFinanzielleSituation,
			jaxAbstractFinanzielleSituation
		);
		jaxAbstractFinanzielleSituation.setNettolohn(
			persistedAbstractFinanzielleSituation.getNettolohn()
		);
		jaxAbstractFinanzielleSituation.setFamilienzulage(
			persistedAbstractFinanzielleSituation.getFamilienzulage()
		);
		jaxAbstractFinanzielleSituation.setErsatzeinkommen(
			persistedAbstractFinanzielleSituation.getErsatzeinkommen()
		);
		jaxAbstractFinanzielleSituation.setErhalteneAlimente(
			persistedAbstractFinanzielleSituation.getErhalteneAlimente()
		);
		jaxAbstractFinanzielleSituation.setBruttovermoegen(
			persistedAbstractFinanzielleSituation.getBruttovermoegen()
		);
		jaxAbstractFinanzielleSituation.setSchulden(
			persistedAbstractFinanzielleSituation.getSchulden()
		);
		jaxAbstractFinanzielleSituation.setGeschaeftsgewinnBasisjahr(
			persistedAbstractFinanzielleSituation.getGeschaeftsgewinnBasisjahr()
		);
		jaxAbstractFinanzielleSituation.setGeschaeftsgewinnBasisjahrMinus1(
			persistedAbstractFinanzielleSituation
				.getGeschaeftsgewinnBasisjahrMinus1()
		);
		jaxAbstractFinanzielleSituation.setGeleisteteAlimente(
			persistedAbstractFinanzielleSituation.getGeleisteteAlimente()
		);

		jaxAbstractFinanzielleSituation
			.setEinkommenInVereinfachtemVerfahrenAbgerechnet(
				persistedAbstractFinanzielleSituation
					.getEinkommenInVereinfachtemVerfahrenAbgerechnet()
			);
		jaxAbstractFinanzielleSituation.setGewinnungskosten(
			persistedAbstractFinanzielleSituation.getGewinnungskosten()
		);
		jaxAbstractFinanzielleSituation.setNettoertraegeErbengemeinschaft(
			persistedAbstractFinanzielleSituation
				.getNettoertraegeErbengemeinschaft()
		);
		jaxAbstractFinanzielleSituation
			.setAmountEinkommenInVereinfachtemVerfahrenAbgerechnet(
				persistedAbstractFinanzielleSituation
					.getAmountEinkommenInVereinfachtemVerfahrenAbgerechnet()
			);
		jaxAbstractFinanzielleSituation.setBruttoertraegeVermoegen(
			persistedAbstractFinanzielleSituation.getBruttoertraegeVermoegen()
		);
		jaxAbstractFinanzielleSituation.setNettoVermoegen(
			persistedAbstractFinanzielleSituation.getNettoVermoegen()
		);
		jaxAbstractFinanzielleSituation.setAbzugSchuldzinsen(
			persistedAbstractFinanzielleSituation.getAbzugSchuldzinsen()
		);
		jaxAbstractFinanzielleSituation.setBruttoLohn(
			persistedAbstractFinanzielleSituation.getBruttoLohn()
		);

		jaxAbstractFinanzielleSituation.setSteuerbaresEinkommen(
			persistedAbstractFinanzielleSituation.getSteuerbaresEinkommen()
		);
		jaxAbstractFinanzielleSituation.setSteuerbaresVermoegen(
			persistedAbstractFinanzielleSituation.getSteuerbaresVermoegen()
		);
		jaxAbstractFinanzielleSituation.setEinkaeufeVorsorge(
			persistedAbstractFinanzielleSituation.getEinkaeufeVorsorge()
		);
		jaxAbstractFinanzielleSituation.setGeschaeftsverlust(
			persistedAbstractFinanzielleSituation.getGeschaeftsverlust()
		);
		jaxAbstractFinanzielleSituation.setAbzuegeLiegenschaft(
			persistedAbstractFinanzielleSituation.getAbzuegeLiegenschaft()
		);
		jaxAbstractFinanzielleSituation.setLiegenschaftsErtraege(
			persistedAbstractFinanzielleSituation.getLiegenschaftsErtraege()
		);

		jaxAbstractFinanzielleSituation.setSelbstdeklaration(
			finanzielleSituationSelbstdeklarationToJAX(
				persistedAbstractFinanzielleSituation.getSelbstdeklaration()
			)
		);

		jaxAbstractFinanzielleSituation.setFinSitZusatzangabenAppenzell(
			finSitZusatzangabenAppenzellToJax(
				persistedAbstractFinanzielleSituation
					.getFinSitZusatzangabenAppenzell()
			)
		);
		jaxAbstractFinanzielleSituation
			.setErsatzeinkommenSelbststaendigkeitBasisjahr(
				persistedAbstractFinanzielleSituation
					.getErsatzeinkommenSelbststaendigkeitBasisjahr()
			);
		jaxAbstractFinanzielleSituation
			.setErsatzeinkommenSelbststaendigkeitBasisjahrMinus1(
				persistedAbstractFinanzielleSituation
					.getErsatzeinkommenSelbststaendigkeitBasisjahrMinus1()
			);
	}

	@Nullable
	private JaxFinanzielleSituationSelbstdeklaration finanzielleSituationSelbstdeklarationToJAX(
		@Nullable FinanzielleSituationSelbstdeklaration persistedSelbstdeklaration
	) {
		if (persistedSelbstdeklaration == null) {
			return null;
		}

		JaxFinanzielleSituationSelbstdeklaration jaxSelbstdeklaration =
			new JaxFinanzielleSituationSelbstdeklaration();
		convertAbstractVorgaengerFieldsToJAX(
			persistedSelbstdeklaration,
			jaxSelbstdeklaration
		);
		jaxSelbstdeklaration.setEinkunftErwerb(
			persistedSelbstdeklaration.getEinkunftErwerb()
		);
		jaxSelbstdeklaration.setEinkunftVersicherung(
			persistedSelbstdeklaration.getEinkunftVersicherung()
		);
		jaxSelbstdeklaration.setEinkunftWertschriften(
			persistedSelbstdeklaration.getEinkunftWertschriften()
		);
		jaxSelbstdeklaration.setEinkunftUnterhaltsbeitragKinder(
			persistedSelbstdeklaration.getEinkunftUnterhaltsbeitragKinder()
		);
		jaxSelbstdeklaration.setEinkunftUeberige(
			persistedSelbstdeklaration.getEinkunftUeberige()
		);
		jaxSelbstdeklaration.setEinkunftLiegenschaften(
			persistedSelbstdeklaration.getEinkunftLiegenschaften()
		);
		jaxSelbstdeklaration.setAbzugBerufsauslagen(
			persistedSelbstdeklaration.getAbzugBerufsauslagen()
		);
		jaxSelbstdeklaration.setAbzugSchuldzinsen(
			persistedSelbstdeklaration.getAbzugSchuldzinsen()
		);
		jaxSelbstdeklaration.setAbzugUnterhaltsbeitragKinder(
			persistedSelbstdeklaration.getAbzugUnterhaltsbeitragKinder()
		);
		jaxSelbstdeklaration.setAbzugSaeule3A(
			persistedSelbstdeklaration.getAbzugSaeule3A()
		);
		jaxSelbstdeklaration.setAbzugVersicherungspraemien(
			persistedSelbstdeklaration.getAbzugVersicherungspraemien()
		);
		jaxSelbstdeklaration.setAbzugKrankheitsUnfallKosten(
			persistedSelbstdeklaration.getAbzugKrankheitsUnfallKosten()
		);
		jaxSelbstdeklaration.setSonderabzugErwerbstaetigkeitEhegatten(
			persistedSelbstdeklaration
				.getSonderabzugErwerbstaetigkeitEhegatten()
		);
		jaxSelbstdeklaration.setAbzugKinderVorschule(
			persistedSelbstdeklaration.getAbzugKinderVorschule()
		);
		jaxSelbstdeklaration.setAbzugKinderSchule(
			persistedSelbstdeklaration.getAbzugKinderSchule()
		);
		jaxSelbstdeklaration.setAbzugEigenbetreuung(
			persistedSelbstdeklaration.getAbzugEigenbetreuung()
		);
		jaxSelbstdeklaration.setAbzugFremdbetreuung(
			persistedSelbstdeklaration.getAbzugFremdbetreuung()
		);
		jaxSelbstdeklaration.setAbzugErwerbsunfaehigePersonen(
			persistedSelbstdeklaration.getAbzugErwerbsunfaehigePersonen()
		);
		jaxSelbstdeklaration.setVermoegen(
			persistedSelbstdeklaration.getVermoegen()
		);
		jaxSelbstdeklaration.setAbzugSteuerfreierBetragErwachsene(
			persistedSelbstdeklaration.getAbzugSteuerfreierBetragErwachsene()
		);
		jaxSelbstdeklaration.setAbzugSteuerfreierBetragKinder(
			persistedSelbstdeklaration.getAbzugSteuerfreierBetragKinder()
		);
		return jaxSelbstdeklaration;
	}

	@Nullable
	private JaxFinSitZusatzangabenAppenzell finSitZusatzangabenAppenzellToJax(
		@Nullable FinSitZusatzangabenAppenzell persistedFinSitZusatzangabenAppenzell
	) {
		if (persistedFinSitZusatzangabenAppenzell == null) {
			return null;
		}

		JaxFinSitZusatzangabenAppenzell jaxFinSitZusatzangabenAppenzell =
			new JaxFinSitZusatzangabenAppenzell();
		convertAbstractVorgaengerFieldsToJAX(
			persistedFinSitZusatzangabenAppenzell,
			jaxFinSitZusatzangabenAppenzell
		);
		jaxFinSitZusatzangabenAppenzell.setSaeule3a(
			persistedFinSitZusatzangabenAppenzell.getSaeule3a()
		);
		jaxFinSitZusatzangabenAppenzell.setSaeule3aNichtBvg(
			persistedFinSitZusatzangabenAppenzell.getSaeule3aNichtBvg()
		);
		jaxFinSitZusatzangabenAppenzell.setBeruflicheVorsorge(
			persistedFinSitZusatzangabenAppenzell.getBeruflicheVorsorge()
		);
		jaxFinSitZusatzangabenAppenzell.setLiegenschaftsaufwand(
			persistedFinSitZusatzangabenAppenzell.getLiegenschaftsaufwand()
		);
		jaxFinSitZusatzangabenAppenzell.setEinkuenfteBgsa(
			persistedFinSitZusatzangabenAppenzell.getEinkuenfteBgsa()
		);
		jaxFinSitZusatzangabenAppenzell.setVorjahresverluste(
			persistedFinSitZusatzangabenAppenzell.getVorjahresverluste()
		);
		jaxFinSitZusatzangabenAppenzell.setPolitischeParteiSpende(
			persistedFinSitZusatzangabenAppenzell.getPolitischeParteiSpende()
		);
		jaxFinSitZusatzangabenAppenzell.setLeistungAnJuristischePersonen(
			persistedFinSitZusatzangabenAppenzell
				.getLeistungAnJuristischePersonen()
		);
		jaxFinSitZusatzangabenAppenzell.setSteuerbaresVermoegen(
			persistedFinSitZusatzangabenAppenzell.getSteuerbaresVermoegen()
		);
		jaxFinSitZusatzangabenAppenzell.setSteuerbaresEinkommen(
			persistedFinSitZusatzangabenAppenzell.getSteuerbaresEinkommen()
		);
		jaxFinSitZusatzangabenAppenzell.setZusatzangabenPartner(
			finSitZusatzangabenAppenzellToJax(
				persistedFinSitZusatzangabenAppenzell.getZusatzangabenPartner()
			)
		);

		return jaxFinSitZusatzangabenAppenzell;
	}
}
