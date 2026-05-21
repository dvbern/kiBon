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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.converter.gesuch.finsit.JaxEinkommensverschlechterungConverter;
import ch.dvbern.ebegu.api.dtos.JaxAlwaysEditableProperties;
import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxGesuch;
import ch.dvbern.ebegu.api.dtos.JaxKindContainer;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.dto.JaxAntragDTO;
import ch.dvbern.ebegu.einstellung.Einstellung;
import ch.dvbern.ebegu.einstellung.EinstellungKey;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragStatusDTO;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.HoehereBeitraegeTyp;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.EinkommensverschlechterungInfoService;
import ch.dvbern.ebegu.services.FamiliensituationService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.services.InternePendenzService;
import ch.dvbern.ebegu.util.AntragStatusConverterUtil;
import ch.dvbern.ebegu.util.EnumUtil;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

import static ch.dvbern.ebegu.enums.UserRole.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRole.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRole.STEUERAMT;
import static java.util.Objects.requireNonNull;

@Dependent
public class JaxAntragConverter extends AbstractBaseConverter {
	@Inject
	private DossierService dossierService;
	@Inject
	private GesuchsperiodeService gesuchsperiodeService;
	@Inject
	private GesuchstellerService gesuchstellerService;
	@Inject
	private FamiliensituationService familiensituationService;
	@Inject
	private EinkommensverschlechterungInfoService einkommensverschlechterungInfoService;
	@Inject
	private InternePendenzService internePendenzService;
	@Inject
	private JaxGesuchstellerConverter gesuchstellerConverter;
	@Inject
	private JaxEinkommensverschlechterungConverter einkommensverschlechterungConverter;
	@Inject
	private JaxFamiliensituationConverter familiensituationConverter;
	@Inject
	private JaxKindConverter kindConverter;
	@Inject
	private JaxFallDossierConverter fallDossierConverter;
	@Inject
	private EinstellungService einstellungService;
	@Inject
	private GemeindeService gemeindeService;

	/**
	 * can convert JaxGesuch to Gesuch
	 * e.x. Gesuch gesuch = converter.gesuchToEntity(completeGesuch, new Gesuch());
	 *
	 * @param antragJAXP
	 * @param antrag
	 * @return
	 */
	public Gesuch gesuchToEntity(
		@Nonnull final JaxGesuch antragJAXP,
		@Nonnull final Gesuch antrag
	) {
		requireNonNull(antrag);
		requireNonNull(antragJAXP);
		requireNonNull(antragJAXP.getDossier());
		requireNonNull(antragJAXP.getDossier().getId());

		convertAbstractVorgaengerFieldsToEntity(antragJAXP, antrag);
		final String exceptionString = "gesuchToEntity";

		Optional<Dossier> dossierOptional = dossierService.findDossier(
			antragJAXP.getDossier().getId()
		);
		if (dossierOptional.isPresent()) {
			antrag.setDossier(
				fallDossierConverter.dossierToEntity(
					antragJAXP.getDossier(),
					dossierOptional.get()
				)
			);
		} else {
			throw new EbeguEntityNotFoundException(
				exceptionString,
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				antragJAXP.getDossier()
			);
		}
		if (antragJAXP.getGesuchsperiode() != null
			&& antragJAXP.getGesuchsperiode().getId() != null) {
			final Optional<Gesuchsperiode> gesuchsperiode =
				gesuchsperiodeService.findGesuchsperiode(
					antragJAXP.getGesuchsperiode().getId()
				);
			if (gesuchsperiode.isPresent()) {
				// Gesuchsperiode darf nicht vom Client ueberschrieben werden
				antrag.setGesuchsperiode(gesuchsperiode.get());
			} else {
				throw new EbeguEntityNotFoundException(
					exceptionString,
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					antragJAXP.getGesuchsperiode().getId()
				);
			}
		}

		antrag.setEingangsdatum(antragJAXP.getEingangsdatum());
		antrag.setRegelnGueltigAb(antragJAXP.getRegelnGueltigAb());
		antrag.setBegruendungMutation(antragJAXP.getBegruendungMutation());
		antrag.setFreigabeDatum(antragJAXP.getFreigabeDatum());
		antrag.setStatus(
			AntragStatusConverterUtil.convertStatusToEntity(
				antragJAXP.getStatus()
			)
		);
		if (antragJAXP.getTyp() != null) {
			antrag.setTyp(antragJAXP.getTyp());
		}
		antrag.setEingangsart(antragJAXP.getEingangsart());

		if (antragJAXP.getGesuchsteller1() != null
			&& antragJAXP.getGesuchsteller1().getId() != null) {
			final Optional<GesuchstellerContainer> gesuchsteller1 =
				gesuchstellerService.findGesuchsteller(
					antragJAXP.getGesuchsteller1().getId()
				);
			if (gesuchsteller1.isPresent()) {
				antrag.setGesuchsteller1(
					gesuchstellerConverter.gesuchstellerContainerToEntity(
						antragJAXP.getGesuchsteller1(),
						gesuchsteller1.get()
					)
				);
			} else {
				throw new EbeguEntityNotFoundException(
					exceptionString,
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					antragJAXP.getGesuchsteller1().getId()
				);
			}
		}
		if (antragJAXP.getGesuchsteller2() != null
			&& antragJAXP.getGesuchsteller2().getId() != null) {
			final Optional<GesuchstellerContainer> gesuchsteller2 =
				gesuchstellerService.findGesuchsteller(
					antragJAXP.getGesuchsteller2().getId()
				);
			if (gesuchsteller2.isPresent()) {
				antrag.setGesuchsteller2(
					gesuchstellerConverter.gesuchstellerContainerToEntity(
						antragJAXP.getGesuchsteller2(),
						gesuchsteller2.get()
					)
				);
			} else {
				throw new EbeguEntityNotFoundException(
					exceptionString,
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					antragJAXP.getGesuchsteller2().getId()
				);
			}
		}
		if (antragJAXP.getFamiliensituationContainer() != null) {
			if (antragJAXP.getFamiliensituationContainer().getId() != null) {
				final Optional<FamiliensituationContainer> familiensituationContainer =
					familiensituationService.findFamiliensituation(
						antragJAXP
							.getFamiliensituationContainer()
							.getId()
					);
				if (familiensituationContainer.isPresent()) {
					antrag.setFamiliensituationContainer(
						familiensituationConverter
							.familiensituationContainerToEntity(
								antragJAXP.getFamiliensituationContainer(),
								familiensituationContainer.get()
							)
					);
				} else {
					throw new EbeguEntityNotFoundException(
						exceptionString,
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						antragJAXP.getFamiliensituationContainer()
							.getId()
					);
				}
			} else {
				antrag.setFamiliensituationContainer(
					familiensituationConverter
						.familiensituationContainerToEntity(
							antragJAXP.getFamiliensituationContainer(),
							new FamiliensituationContainer()
						)
				);
			}
		}

		if (antragJAXP.getEinkommensverschlechterungInfoContainer() != null) {
			if (antragJAXP.getEinkommensverschlechterungInfoContainer().getId()
				!= null) {
				final Optional<EinkommensverschlechterungInfoContainer> evkiSituation =
					einkommensverschlechterungInfoService
						.findEinkommensverschlechterungInfo(
							antragJAXP
								.getEinkommensverschlechterungInfoContainer()
								.getId()
						);
				if (evkiSituation.isPresent()) {
					antrag.setEinkommensverschlechterungInfoContainer(
						einkommensverschlechterungConverter
							.einkommensverschlechterungInfoContainerToEntity(
								antragJAXP
									.getEinkommensverschlechterungInfoContainer(),
								evkiSituation.get()
							)
					);
				} else {
					throw new EbeguEntityNotFoundException(
						exceptionString,
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						antragJAXP
							.getEinkommensverschlechterungInfoContainer()
							.getId()
					);
				}
			} else {
				antrag.setEinkommensverschlechterungInfoContainer(
					einkommensverschlechterungConverter
						.einkommensverschlechterungInfoContainerToEntity(
							antragJAXP
								.getEinkommensverschlechterungInfoContainer(),
							new EinkommensverschlechterungInfoContainer()
						)
				);
			}
		}

		antrag.setBemerkungen(antragJAXP.getBemerkungen());
		antrag.setBemerkungenSTV(antragJAXP.getBemerkungenSTV());
		antrag.setBemerkungenPruefungSTV(
			antragJAXP.getBemerkungenPruefungSTV()
		);
		antrag.setLaufnummer(antragJAXP.getLaufnummer());
		antrag.setGesuchBetreuungenStatus(
			antragJAXP.getGesuchBetreuungenStatus()
		);
		antrag.setGeprueftSTV(antragJAXP.isGeprueftSTV());
		antrag.setVerfuegungEingeschrieben(
			antragJAXP.isVerfuegungEingeschrieben()
		);
		antrag.setGesperrtWegenBeschwerde(
			antragJAXP.isGesperrtWegenBeschwerde()
		);
		antrag.setFinSitStatus(antragJAXP.getFinSitStatus());
		antrag.setFinSitAenderungGueltigAbDatum(
			antragJAXP.getFinSitAenderungGueltigAbDatum()
		);
		antrag.setDokumenteHochgeladen(antragJAXP.isDokumenteHochgeladen());
		return antrag;
	}

	public JaxGesuch gesuchToJAX(@Nonnull final Gesuch persistedGesuch) {
		final JaxGesuch jaxGesuch = new JaxGesuch();
		convertAbstractVorgaengerFieldsToJAX(persistedGesuch, jaxGesuch);
		jaxGesuch.setDossier(
			fallDossierConverter.dossierToJAX(persistedGesuch.getDossier())
		);
		if (persistedGesuch.getGesuchsperiode() != null) {
			jaxGesuch.setGesuchsperiode(
				gesuchsperiodeToJAX(persistedGesuch.getGesuchsperiode())
			);
		}
		jaxGesuch.setEingangsdatum(persistedGesuch.getEingangsdatum());
		jaxGesuch.setRegelnGueltigAb(persistedGesuch.getRegelnGueltigAb());
		jaxGesuch.setBegruendungMutation(
			persistedGesuch.getBegruendungMutation()
		);
		jaxGesuch.setFreigabeDatum(persistedGesuch.getFreigabeDatum());
		jaxGesuch.setStatus(
			AntragStatusConverterUtil.convertStatusToDTO(
				persistedGesuch,
				persistedGesuch.getStatus()
			)
		);
		jaxGesuch.setTyp(persistedGesuch.getTyp());
		jaxGesuch.setEingangsart(persistedGesuch.getEingangsart());

		if (persistedGesuch.getGesuchsteller1() != null) {
			jaxGesuch.setGesuchsteller1(
				gesuchstellerConverter.gesuchstellerContainerToJAX(
					persistedGesuch.getGesuchsteller1()
				)
			);
		}
		if (persistedGesuch.getGesuchsteller2() != null) {
			jaxGesuch.setGesuchsteller2(
				gesuchstellerConverter.gesuchstellerContainerToJAX(
					persistedGesuch.getGesuchsteller2()
				)
			);
		}
		if (persistedGesuch.getFamiliensituationContainer() != null) {
			jaxGesuch.setFamiliensituationContainer(
				familiensituationConverter.familiensituationContainerToJAX(
					persistedGesuch.getFamiliensituationContainer()
				)
			);
		}
		for (final KindContainer kind : persistedGesuch.getKindContainers()) {
			jaxGesuch.getKindContainers()
				.add(kindConverter.kindContainerToJAX(kind));
		}
		if (persistedGesuch.getEinkommensverschlechterungInfoContainer()
			!= null) {
			jaxGesuch.setEinkommensverschlechterungInfoContainer(
				einkommensverschlechterungConverter
					.einkommensverschlechterungInfoContainerToJAX(
						persistedGesuch
							.getEinkommensverschlechterungInfoContainer()
					)
			);
		}
		jaxGesuch.setBemerkungen(persistedGesuch.getBemerkungen());
		jaxGesuch.setBemerkungenSTV(persistedGesuch.getBemerkungenSTV());
		jaxGesuch.setBemerkungenPruefungSTV(
			persistedGesuch.getBemerkungenPruefungSTV()
		);
		jaxGesuch.setLaufnummer(persistedGesuch.getLaufnummer());
		jaxGesuch.setGesuchBetreuungenStatus(
			persistedGesuch.getGesuchBetreuungenStatus()
		);
		jaxGesuch.setGeprueftSTV(persistedGesuch.isGeprueftSTV());
		jaxGesuch.setVerfuegungEingeschrieben(
			persistedGesuch.isVerfuegungEingeschrieben()
		);
		jaxGesuch.setGesperrtWegenBeschwerde(
			persistedGesuch.isGesperrtWegenBeschwerde()
		);
		jaxGesuch.setDatumGewarntNichtFreigegeben(
			persistedGesuch.getDatumGewarntNichtFreigegeben()
		);
		jaxGesuch.setDatumGewarntFehlendeQuittung(
			persistedGesuch.getDatumGewarntFehlendeQuittung()
		);
		jaxGesuch.setTimestampVerfuegt(persistedGesuch.getTimestampVerfuegt());
		jaxGesuch.setGueltig(persistedGesuch.isGueltig());
		jaxGesuch.setDokumenteHochgeladen(
			persistedGesuch.getDokumenteHochgeladen()
		);
		jaxGesuch.setFinSitStatus(persistedGesuch.getFinSitStatus());
		jaxGesuch.setFinSitTyp(persistedGesuch.getFinSitTyp());
		jaxGesuch.setFinSitAenderungGueltigAbDatum(
			persistedGesuch.getFinSitAenderungGueltigAbDatum()
		);
		jaxGesuch.setMarkiertFuerKontroll(
			persistedGesuch.getMarkiertFuerKontroll()
		);
		return jaxGesuch;
	}

	/**
	 * transformiert ein gesuch in ein JaxAntragDTO unter beruecksichtigung der rollen und erlaubten institutionen
	 * - Fuer die Rolle Steueramt werden saemtlichen Daten von den Kindern nicht geladen
	 * - Fuer die Rolle Institution/Traegerschaft werden nur die relevanten Institutionen und Angebote geladen
	 */
	public JaxAntragDTO gesuchToAntragDTO(
		Gesuch gesuch,
		@Nullable UserRole userRole,
		Collection<Institution> allowedInst
	) {
		//wir koennen nicht mit den container auf dem gesuch arbeiten weil das gesuch attached ist. hibernate
		//wuerde uns dann die kinder wegloeschen, daher besser transformieren
		Collection<JaxKindContainer> jaxKindContainers = new ArrayList<>(
			gesuch.getKindContainers().size()
		);

		JaxAntragDTO antrag = gesuchToAntragDTOBasic(gesuch);

		if (userRole != STEUERAMT) {
			for (final KindContainer kind : gesuch.getKindContainers()) {
				jaxKindContainers.add(kindConverter.kindContainerToJAX(kind));
			}
			antrag.setKinder(createKinderList(jaxKindContainers));
		}

		Einstellung einstellung = einstellungService.findEinstellung(
			EinstellungKey.HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT,
			gesuch.extractGemeinde(),
			gesuch.getGesuchsperiode()
		);

		boolean hoehereBeitraegeAnInstitutionAktiviert =
			HoehereBeitraegeTyp.valueOf(einstellung.getValue())
				== HoehereBeitraegeTyp.AKTIVIERT_AUSZAHLUNG_INSTITUTION;

		if (EnumUtil.isOneOf(
			userRole,
			ADMIN_TRAEGERSCHAFT,
			SACHBEARBEITER_TRAEGERSCHAFT,
			ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION
		)) {
			RestUtil.purgeKinderAndBetreuungenOfInstitutionen(
				jaxKindContainers,
				allowedInst,
				hoehereBeitraegeAnInstitutionAktiviert
			);
		}

		disguiseStatus(gesuch.getStatus(), antrag, userRole);

		if (userRole != STEUERAMT) {
			antrag.setAngebote(createAngeboteList(jaxKindContainers));
			antrag.setInstitutionen(createInstitutionenList(jaxKindContainers));
		}

		return antrag;
	}

	public void alwaysEditablePropertiesToGesuch(
		@Nonnull final JaxAlwaysEditableProperties properties,
		@Nonnull Gesuch gesuch
	) {

		// fields on GS1
		Gesuchsteller gs1 = gesuch.extractGesuchsteller1()
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"alwaysEditablePropertiesToGesuch",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND
				)
			);

		gs1.setMail(properties.getMailGS1());
		gs1.setMobile(properties.getMobileGS1());
		gs1.setTelefon(properties.getTelefonGS1());
		gs1.setTelefonAusland(properties.getTelefonAuslandGS1());

		// fields on GS2
		if (gesuch.getGesuchsteller2() != null) {

			Gesuchsteller gs2 = gesuch.getGesuchsteller2().getGesuchstellerJA();

			gs2.setMail(properties.getMailGS2());
			gs2.setMobile(properties.getMobileGS2());
			gs2.setTelefon(properties.getTelefonGS2());
			gs2.setTelefonAusland(properties.getTelefonAuslandGS2());

		}

		// fields on Familiensituation
		Familiensituation famSit = gesuch.extractFamiliensituation();

		if (famSit != null) {
			famSit.setKeineMahlzeitenverguenstigungBeantragt(
				properties.isKeineMahlzeitenverguenstigungBeantragt()
			);

			if (properties.isKeineMahlzeitenverguenstigungBeantragt()) {
				properties.setIban(null);
				properties.setKontoinhaber(null);
				properties.setAbweichendeZahlungsadresse(false);
				properties.setZahlungsadresse(null);
			}

			if (properties.getIban() != null
				|| properties.getKontoinhaber() != null) {
				// Wenn eines gesetzt ist, sind beide zwingend!
				Objects.requireNonNull(properties.getIban());
				Objects.requireNonNull(properties.getKontoinhaber());
				if (famSit.getAuszahlungsdaten() == null) {
					famSit.setAuszahlungsdaten(new Auszahlungsdaten());
				}
				famSit.getAuszahlungsdaten()
					.setIban(new IBAN(properties.getIban()));
				famSit.getAuszahlungsdaten()
					.setKontoinhaber(properties.getKontoinhaber());

				famSit.setAbweichendeZahlungsadresse(
					properties.isAbweichendeZahlungsadresse()
				);
				if (properties.isAbweichendeZahlungsadresse()
					&& properties.getZahlungsadresse() != null) {
					famSit.getAuszahlungsdaten()
						.setAdresseKontoinhaber(
							this.adresseToEntity(
								properties.getZahlungsadresse(),
								famSit.getAuszahlungsdaten()
									.getAdresseKontoinhaber()
									== null ?
										new Adresse() :
										famSit.getAuszahlungsdaten()
											.getAdresseKontoinhaber()
							)
						);
				}
			}
		}
	}

	public JaxAntragDTO gesuchToAntragDTO(
		Gesuch gesuch,
		@Nullable UserRole userRole
	) {
		JaxAntragDTO antrag = gesuchToAntragDTOBasic(gesuch);
		antrag.setKinder(createKinderList(gesuch.getKindContainers()));
		antrag.setAngebote(createAngeboteList(gesuch.getKindContainers()));
		antrag.setInstitutionen(
			createInstitutionenList(gesuch.getKindContainers())
		);
		disguiseStatus(gesuch.getStatus(), antrag, userRole);
		return antrag;
	}

	@Nonnull
	private JaxAntragDTO gesuchToAntragDTOBasic(@Nonnull Gesuch gesuch) {
		JaxAntragDTO antrag = new JaxAntragDTO();
		antrag.setAntragId(gesuch.getId());
		antrag.setFallNummer(gesuch.getFall().getFallNummer());
		antrag.setBemerkungen(gesuch.getDossier().getBemerkungen());
		antrag.setDossierId(gesuch.getDossier().getId());
		antrag.setFamilienName(gesuch.extractFamiliennamenString());
		antrag.setEingangsdatum(gesuch.getEingangsdatum());
		antrag.setRegelnGueltigAb(gesuch.getRegelnGueltigAb());
		antrag.setBegruendungMutation(gesuch.getBegruendungMutation());
		antrag.setEingangsdatumSTV(gesuch.getEingangsdatumSTV());
		antrag.setAenderungsdatum(gesuch.getTimestampMutiert());
		antrag.setAntragTyp(gesuch.getTyp());
		antrag.setStatus(
			AntragStatusConverterUtil.convertStatusToDTO(
				gesuch,
				gesuch.getStatus()
			)
		);
		antrag.setGesuchsperiodeGueltigAb(
			gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb()
		);
		antrag.setGesuchsperiodeGueltigBis(
			gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis()
		);
		antrag.setGemeinde(gesuch.getDossier().getGemeinde().getName());
		Benutzer verantwortlicherBG = gesuch.getDossier()
			.getVerantwortlicherBG();
		if (verantwortlicherBG != null) {
			setVerantwortlicherBGToAntragDTO(antrag, verantwortlicherBG);
		}
		Benutzer verantwortlicherTS = gesuch.getDossier()
			.getVerantwortlicherTS();
		if (verantwortlicherTS != null) {
			setVerantwortlicherTSToAntragDTO(antrag, verantwortlicherTS);
		}
		antrag.setVerfuegt(gesuch.getStatus().isAnyStatusOfVerfuegt());
		antrag.setBeschwerdeHaengig(
			gesuch.getStatus() == AntragStatus.BESCHWERDE_HAENGIG
		);
		antrag.setLaufnummer(gesuch.getLaufnummer());
		antrag.setEingangsart(gesuch.getEingangsart());
		Benutzer besitzer = gesuch.getFall().getBesitzer();
		String besitzerUsername = besitzer == null ?
			null :
			besitzer.getUsername();
		antrag.setBesitzerUsername(besitzerUsername);
		antrag.setGesuchBetreuungenStatus(gesuch.getGesuchBetreuungenStatus());
		antrag.setDokumenteHochgeladen(gesuch.getDokumenteHochgeladen());
		antrag.setFinSitStatus(gesuch.getFinSitStatus());
		antrag.setFallId(gesuch.getFall().getId());
		antrag.setGemeindeId(gesuch.getDossier().getGemeinde().getId());
		antrag.setSozialdienst(
			gesuch.getDossier().getFall().isSozialdienstFall()
		);
		antrag.setInternePendenz(gesuch.getInternePendenz());
		if (antrag.isInternePendenz()) {
			antrag.setInternePendenzAbgelaufen(
				internePendenzService.hasGesuchAbgelaufeneInternePendenzen(
					gesuch
				)
			);
		} else {
			antrag.setInternePendenzAbgelaufen(false);
		}

		return antrag;
	}

	private void setVerantwortlicherTSToAntragDTO(
		@Nonnull JaxAntragDTO antrag,
		@Nonnull Benutzer verantwortlicherTS
	) {
		antrag.setVerantwortlicherTS(verantwortlicherTS.getFullName());
		antrag.setVerantwortlicherUsernameTS(verantwortlicherTS.getUsername());
	}

	private void setVerantwortlicherBGToAntragDTO(
		@Nonnull JaxAntragDTO antrag,
		@Nonnull Benutzer verantwortlicherBG
	) {
		antrag.setVerantwortlicherBG(verantwortlicherBG.getFullName());
		antrag.setVerantwortlicherUsernameBG(verantwortlicherBG.getUsername());
	}

	/**
	 * Using the existing GesuchStatus and the UserRole it will translate the Status into the right one for this role.
	 */
	private void disguiseStatus(
		AntragStatus status,
		JaxAntragDTO antrag,
		@Nullable UserRole userRole
	) {
		if (userRole != null) {
			switch (userRole) {
			case GESUCHSTELLER:
			case ADMIN_INSTITUTION:
			case SACHBEARBEITER_INSTITUTION:
			case ADMIN_TRAEGERSCHAFT:
			case SACHBEARBEITER_TRAEGERSCHAFT:
				switch (status) {
				case PRUEFUNG_STV:
				case GEPRUEFT_STV:
				case IN_BEARBEITUNG_STV:
					antrag.setStatus(AntragStatusDTO.VERFUEGT);
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Geht durch die ganze Liste von KindContainers durch und gibt ein Set mit den BetreuungsangebotTyp aller
	 * Institutionen zurueck.
	 * Da ein Set zurueckgegeben wird, sind die Daten nie dupliziert.
	 */
	private Set<BetreuungsangebotTyp> createAngeboteList(
		Set<KindContainer> kindContainers
	) {
		return kindContainers.stream()
			.flatMap(kc -> kc.getAllPlaetze().stream())
			.map(b -> b.getInstitutionStammdaten().getBetreuungsangebotTyp())
			.collect(Collectors.toSet());
	}

	private Set<BetreuungsangebotTyp> createAngeboteList(
		Collection<JaxKindContainer> jaxKindContainers
	) {
		return jaxKindContainers.stream()
			.flatMap(kc -> kc.getBetreuungen().stream())
			.map(b -> b.getInstitutionStammdaten().getBetreuungsangebotTyp())
			.collect(Collectors.toSet());
	}

	private Set<String> createKinderList(Set<KindContainer> kindContainers) {
		return kindContainers.stream()
			.map(kc -> kc.getKindJA().getVorname())
			.collect(Collectors.toSet());
	}

	private Set<String> createKinderList(
		Collection<JaxKindContainer> jaxKindContainers
	) {
		return jaxKindContainers.stream()
			.map(kc -> kc.getKindJA().getVorname())
			.collect(Collectors.toSet());
	}

	/**
	 * Geht durch die ganze Liste von KindContainers durch und gibt ein Set mit den Namen aller Institutionen zurueck.
	 * Da ein Set zurueckgegeben wird, sind die Daten nie dupliziert.
	 */
	@SuppressWarnings("Duplicates")
	private Set<String> createInstitutionenList(
		Set<KindContainer> kindContainers
	) {
		return kindContainers.stream()
			.flatMap(kc -> kc.getBetreuungen().stream())
			.map(Betreuung::getInstitutionStammdaten)
			.map(is -> is.getInstitution().getName())
			.collect(Collectors.toSet());
	}

	@SuppressWarnings("Duplicates")
	private Set<String> createInstitutionenList(
		Collection<JaxKindContainer> jaxKindContainers
	) {
		return jaxKindContainers.stream()
			.flatMap(kc -> kc.getBetreuungen().stream())
			.map(JaxBetreuung::getInstitutionStammdaten)
			.filter(Objects::nonNull)
			.map(is -> is.getInstitution().getName())
			.collect(Collectors.toSet());
	}
}
