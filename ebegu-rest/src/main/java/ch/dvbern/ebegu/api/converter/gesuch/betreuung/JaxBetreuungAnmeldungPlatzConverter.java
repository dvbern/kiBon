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

package ch.dvbern.ebegu.api.converter.gesuch.betreuung;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.abweichungen.AbweichungInitializingUtil;
import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.converter.gesuch.JaxVerfuegungConverter;
import ch.dvbern.ebegu.api.converter.institution.JaxInstitutionStammdatenConverter;
import ch.dvbern.ebegu.api.dtos.JaxAbwesenheit;
import ch.dvbern.ebegu.api.dtos.JaxAbwesenheitContainer;
import ch.dvbern.ebegu.api.dtos.JaxBelegungFerieninsel;
import ch.dvbern.ebegu.api.dtos.JaxBelegungFerieninselTag;
import ch.dvbern.ebegu.api.dtos.JaxBelegungTagesschule;
import ch.dvbern.ebegu.api.dtos.JaxBelegungTagesschuleModul;
import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungspensum;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungspensumAbweichung;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungspensumContainer;
import ch.dvbern.ebegu.api.dtos.JaxEingewoehnung;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdatenSummary;
import ch.dvbern.ebegu.einstellung.EinstellungService;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Abwesenheit;
import ch.dvbern.ebegu.entities.AbwesenheitContainer;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BelegungFerieninsel;
import ch.dvbern.ebegu.entities.BelegungFerieninselTag;
import ch.dvbern.ebegu.entities.BelegungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumAbweichung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.containers.PensumUtil;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.Persistence;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.util.StreamsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.einstellung.EinstellungKey.OEFFNUNGSTAGE_MITTAGSTISCH;
import static java.util.Objects.requireNonNull;

@Dependent
public class JaxBetreuungAnmeldungPlatzConverter extends AbstractBaseConverter {
	private static final Logger LOGGER = LoggerFactory.getLogger(
		JaxBetreuungAnmeldungPlatzConverter.class
	);
	public static final String DROPPED_DUPLICATE_ABWEICHUNG =
		"dropped duplicate abweichung ";
	@Inject
	private InstitutionStammdatenService institutionStammdatenService;
	@Inject
	private JaxInstitutionStammdatenConverter institutionStammdatenConverter;
	@Inject
	private KindService kindService;
	@Inject
	private BetreuungService betreuungService;
	@Inject
	private JaxErweiterteBetreuungConverter erweiterteBetreuungConverter;
	@Inject
	private JaxVerfuegungConverter verfuegungConverter;
	@Inject
	private Persistence persistence;
	@Inject
	private EinstellungService einstellungService;

	@Nonnull
	private <T extends AbstractPlatz> T abstractPlatzToEntity(
		@Nonnull final JaxBetreuung betreuungJAXP,
		@Nonnull final T betreuung
	) {
		requireNonNull(betreuung);
		requireNonNull(betreuungJAXP);

		convertAbstractVorgaengerFieldsToEntity(betreuungJAXP, betreuung);

		// InstitutionStammdaten muessen bereits existieren
		if (betreuungJAXP.getInstitutionStammdaten() != null) {
			final String instStammdatenID = betreuungJAXP
				.getInstitutionStammdaten()
				.getId();
			requireNonNull(
				instStammdatenID,
				"Die Institutionsstammdaten muessen gesetzt sein"
			);
			final Optional<InstitutionStammdaten> optInstStammdaten =
				institutionStammdatenService.findInstitutionStammdaten(
					instStammdatenID
				);
			final InstitutionStammdaten instStammdatenToMerge =
				optInstStammdaten.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"betreuungToEntity",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						instStammdatenID
					)
				);
			// InstitutionsStammdaten darf nicht vom Client ueberschrieben werden
			betreuung.setInstitutionStammdaten(instStammdatenToMerge);
		}
		betreuung.setBetreuungNummer(betreuungJAXP.getBetreuungNummer());

		// try to load the Kind with the ID given by BetreuungJax
		if (betreuungJAXP.getKindId() != null) {
			KindContainer kindContainer = kindService.findKind(
				betreuungJAXP.getKindId()
			)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"betreuungToEntity",
						betreuungJAXP.getKindId()
					)
				);
			betreuung.setKind(kindContainer);
		}
		//ACHTUNG: Verfuegung wird hier nicht synchronisiert aus sicherheitsgruenden
		return betreuung;
	}

	@Nonnull
	public AnmeldungTagesschule anmeldungTagesschuleToEntity(
		@Nonnull final JaxBetreuung betreuungJAXP,
		@Nonnull final AnmeldungTagesschule anmeldungTagesschule
	) {
		AnmeldungTagesschule betreuung = abstractPlatzToEntity(
			betreuungJAXP,
			anmeldungTagesschule
		);
		betreuung.setBetreuungsstatus(betreuungJAXP.getBetreuungsstatus());
		betreuung.setAnmeldungMutationZustand(
			betreuungJAXP.getAnmeldungMutationZustand()
		);
		betreuung.setKeineDetailinformationen(
			betreuungJAXP.isKeineDetailinformationen()
		);
		if (!betreuung.isKeineDetailinformationen()) {
			if (betreuungJAXP.getBelegungTagesschule() != null) {
				if (betreuung.getBelegungTagesschule() != null) {
					betreuung.setBelegungTagesschule(
						belegungTagesschuleToEntity(
							betreuungJAXP.getBelegungTagesschule(),
							betreuung.getBelegungTagesschule()
						)
					);
				} else {
					betreuung.setBelegungTagesschule(
						belegungTagesschuleToEntity(
							betreuungJAXP.getBelegungTagesschule(),
							new BelegungTagesschule()
						)
					);
				}
			} else {
				betreuung.setBelegungTagesschule(null);
			}
		} else {
			betreuung.setBelegungTagesschule(null);
		}
		return betreuung;
	}

	@Nonnull
	public AnmeldungFerieninsel anmeldungFerieninselToEntity(
		@Nonnull final JaxBetreuung betreuungJAXP,
		@Nonnull final AnmeldungFerieninsel anmeldungFerieninsel
	) {
		AnmeldungFerieninsel betreuung = abstractPlatzToEntity(
			betreuungJAXP,
			anmeldungFerieninsel
		);
		betreuung.setBetreuungsstatus(betreuungJAXP.getBetreuungsstatus());
		betreuung.setAnmeldungMutationZustand(
			betreuungJAXP.getAnmeldungMutationZustand()
		);
		if (betreuung.getBelegungFerieninsel() != null) {
			betreuung.setBelegungFerieninsel(
				belegungFerieninselToEntity(
					betreuungJAXP.getBelegungFerieninsel(),
					betreuung.getBelegungFerieninsel()
				)
			);
		} else {
			betreuung.setBelegungFerieninsel(
				belegungFerieninselToEntity(
					betreuungJAXP.getBelegungFerieninsel(),
					new BelegungFerieninsel()
				)
			);
		}
		return betreuung;
	}

	@Nullable
	public BelegungFerieninsel belegungFerieninselToEntity(
		@Nullable JaxBelegungFerieninsel belegungFerieninselJAX,
		@Nonnull BelegungFerieninsel belegungFerieninsel
	) {

		if (belegungFerieninselJAX == null) {
			return null;
		}

		requireNonNull(belegungFerieninsel);

		convertAbstractVorgaengerFieldsToEntity(
			belegungFerieninselJAX,
			belegungFerieninsel
		);
		belegungFerieninsel.setFerienname(
			belegungFerieninselJAX.getFerienname()
		);
		belegungFerieninsel.setNotfallAngaben(
			belegungFerieninselJAX.getNotfallAngaben()
		);
		belegungFerieninselTageListToEntity(
			belegungFerieninselJAX.getTage(),
			belegungFerieninsel.getTage()
		);
		belegungFerieninselTageListToEntity(
			belegungFerieninselJAX.getTageMorgenmodul(),
			belegungFerieninsel.getTageMorgenmodul()
		);

		return belegungFerieninsel;
	}

	private void belegungFerieninselTageListToEntity(
		@Nonnull List<JaxBelegungFerieninselTag> jaxTagList,
		@Nonnull Collection<BelegungFerieninselTag> tagList
	) {

		final Set<BelegungFerieninselTag> transformedTagList = new TreeSet<>();
		for (final JaxBelegungFerieninselTag jaxTag : jaxTagList) {
			final BelegungFerieninselTag tagToMergeWith = tagList.stream()
				.filter(
					existingTagEntity -> existingTagEntity.getId()
						.equals(jaxTag.getId())
				)
				.reduce(StreamsUtil.toOnlyElement())
				.orElseGet(BelegungFerieninselTag::new);
			final BelegungFerieninselTag tagToAdd =
				belegungFerieninselTagToEntity(jaxTag, tagToMergeWith);
			final boolean added = transformedTagList.add(tagToAdd);
			if (!added) {
				LOGGER.warn(DROPPED_DUPLICATE_CONTAINER + "{}", tagToAdd);
			}
		}

		//change the existing collection to reflect changes
		// Already tested: All existing Betreuungspensen of the list remain as they were, that means their data are
		// updated and the objects are not created again. ID and InsertTimeStamp are the same as before
		tagList.clear();
		tagList.addAll(transformedTagList);
	}

	private BelegungFerieninselTag belegungFerieninselTagToEntity(
		@Nonnull JaxBelegungFerieninselTag jaxTag,
		@Nonnull BelegungFerieninselTag tag
	) {

		requireNonNull(jaxTag);
		requireNonNull(tag);

		convertAbstractVorgaengerFieldsToEntity(jaxTag, tag);
		tag.setTag(jaxTag.getTag());

		return tag;
	}

	@Nullable
	public JaxBelegungFerieninsel belegungFerieninselToJAX(
		@Nullable BelegungFerieninsel persistedBelegungFerieninsel
	) {
		if (persistedBelegungFerieninsel == null) {
			return null;
		}

		final JaxBelegungFerieninsel jaxBelegungFerieninsel =
			new JaxBelegungFerieninsel();
		convertAbstractVorgaengerFieldsToJAX(
			persistedBelegungFerieninsel,
			jaxBelegungFerieninsel
		);
		jaxBelegungFerieninsel.setFerienname(
			persistedBelegungFerieninsel.getFerienname()
		);
		jaxBelegungFerieninsel.setTage(
			belegungFerieninselTageListToJAX(
				persistedBelegungFerieninsel.getTage()
			)
		);
		jaxBelegungFerieninsel.setTageMorgenmodul(
			belegungFerieninselTageListToJAX(
				persistedBelegungFerieninsel.getTageMorgenmodul()
			)
		);
		jaxBelegungFerieninsel.setNotfallAngaben(
			persistedBelegungFerieninsel.getNotfallAngaben()
		);
		return jaxBelegungFerieninsel;
	}

	@Nonnull
	public List<JaxBelegungFerieninselTag> belegungFerieninselTageListToJAX(
		@Nonnull Collection<BelegungFerieninselTag> persistedFerieninselTageList
	) {

		return persistedFerieninselTageList.stream()
			.map(this::belegungFerieninselTagToJAX)
			.collect(Collectors.toList());
	}

	@Nonnull
	public JaxBelegungFerieninselTag belegungFerieninselTagToJAX(
		@Nonnull BelegungFerieninselTag persistedFerieninselTag
	) {

		JaxBelegungFerieninselTag jaxTag = new JaxBelegungFerieninselTag();
		convertAbstractVorgaengerFieldsToJAX(persistedFerieninselTag, jaxTag);
		jaxTag.setTag(persistedFerieninselTag.getTag());

		return jaxTag;
	}

	@Nonnull
	public Betreuung betreuungToEntity(
		@Nonnull final JaxBetreuung betreuungJAXP,
		@Nonnull final Betreuung betreuung
	) {
		requireNonNull(betreuung);
		requireNonNull(betreuungJAXP);

		abstractPlatzToEntity(betreuungJAXP, betreuung);
		betreuung.setGrundAblehnung(betreuungJAXP.getGrundAblehnung());

		betreuungsPensumContainersToEntity(
			betreuungJAXP.getBetreuungspensumContainers(),
			betreuung.getBetreuungspensumContainers()
		);
		setBetreuungInbetreuungsPensumContainers(
			betreuung.getBetreuungspensumContainers(),
			betreuung
		);
		Objects.requireNonNull(betreuungJAXP.getErweiterteBetreuungContainer());
		betreuung.setErweiterteBetreuungContainer(
			erweiterteBetreuungConverter.erweiterteBetreuungContainerToEntity(
				betreuungJAXP.getErweiterteBetreuungContainer(),
				betreuung.getErweiterteBetreuungContainer()
			)
		);
		betreuung.getErweiterteBetreuungContainer().setBetreuung(betreuung);

		abwesenheitContainersToEntity(
			betreuungJAXP.getAbwesenheitContainers(),
			betreuung.getAbwesenheitContainers()
		);
		setBetreuungInAbwesenheiten(
			betreuung.getAbwesenheitContainers(),
			betreuung
		);

		betreuung.setBetreuungsstatus(betreuungJAXP.getBetreuungsstatus());
		betreuung.setVertrag(betreuungJAXP.getVertrag());

		betreuung.setBetreuungMutiert(betreuungJAXP.getBetreuungMutiert());
		betreuung.setAbwesenheitMutiert(betreuungJAXP.getAbwesenheitMutiert());

		betreuung.setEingewoehnung(betreuungJAXP.isEingewoehnung());
		betreuung.setAuszahlungAnEltern(betreuungJAXP.isAuszahlungAnEltern());
		betreuung.setBegruendungAuszahlungAnInstitution(
			betreuungJAXP.getBegruendungAuszahlungAnInstitution()
		);

		betreuung.setBedarfsstufe(betreuungJAXP.getBedarfsstufe());

		//ACHTUNG: Verfuegung wird hier nicht synchronisiert aus sicherheitsgruenden
		return betreuung;
	}

	public Set<BetreuungspensumAbweichung> betreuungspensumAbweichungenToEntity(
		@Nonnull List<JaxBetreuungspensumAbweichung> abweichungenJAXP,
		@Nonnull Collection<BetreuungspensumAbweichung> abweichungen
	) {

		Set<BetreuungspensumAbweichung> transformedAbweichungen =
			new TreeSet<>();
		for (JaxBetreuungspensumAbweichung jaxAbweichung : abweichungenJAXP) {
			LocalDate date = requireNonNull(jaxAbweichung.getGueltigAb());

			BetreuungspensumAbweichung abweichungToMergeWith = abweichungen
				.stream()
				.filter(
					existingAbweichung -> existingAbweichung.getGueltigkeit()
						.contains(date)
				)
				.reduce(StreamsUtil.toOnlyElement())
				.orElse(new BetreuungspensumAbweichung());

			var abweichungToAdd = betreuungspensumAbweichungToEntity(
				jaxAbweichung,
				abweichungToMergeWith
			);
			boolean added = transformedAbweichungen.add(abweichungToAdd);
			if (!added) {
				LOGGER.warn(
					DROPPED_DUPLICATE_ABWEICHUNG + "{}",
					abweichungToAdd
				);
			}
		}

		return transformedAbweichungen;
	}

	private BetreuungspensumAbweichung betreuungspensumAbweichungToEntity(
		@Nonnull JaxBetreuungspensumAbweichung jaxAbweichung,
		@Nonnull BetreuungspensumAbweichung abweichung
	) {
		convertAbstractPensumFieldsToEntity(jaxAbweichung, abweichung);
		abweichung.setStatus(jaxAbweichung.getStatus()); // the frontend should not be able to decide this...

		return abweichung;
	}

	private BelegungTagesschule belegungTagesschuleToEntity(
		@Nonnull JaxBelegungTagesschule belegungTagesschuleJAXP,
		@Nonnull BelegungTagesschule belegungTagesschule
	) {

		convertAbstractVorgaengerFieldsToEntity(
			belegungTagesschuleJAXP,
			belegungTagesschule
		);

		final Set<BelegungTagesschuleModul> convertedBelegungTagesschuleModule =
			belegungTagesschuleModulListToEntity(
				belegungTagesschuleJAXP.getBelegungTagesschuleModule(),
				belegungTagesschule.getBelegungTagesschuleModule(),
				belegungTagesschule
			);
		belegungTagesschule.getBelegungTagesschuleModule().clear();
		belegungTagesschule.getBelegungTagesschuleModule()
			.addAll(convertedBelegungTagesschuleModule);

		belegungTagesschule.setEintrittsdatum(
			belegungTagesschuleJAXP.getEintrittsdatum()
		);
		belegungTagesschule.setAbholungTagesschule(
			belegungTagesschuleJAXP.getAbholungTagesschule()
		);
		belegungTagesschule.setPlanKlasse(
			belegungTagesschuleJAXP.getPlanKlasse()
		);
		belegungTagesschule.setFleischOption(
			belegungTagesschuleJAXP.getFleischOption()
		);
		belegungTagesschule.setAllergienUndUnvertraeglichkeiten(
			belegungTagesschuleJAXP.getAllergienUndUnvertraeglichkeiten()
		);
		belegungTagesschule.setNotfallnummer(
			belegungTagesschuleJAXP.getNotfallnummer()
		);
		belegungTagesschule.setAbweichungZweitesSemester(
			belegungTagesschuleJAXP.isAbweichungZweitesSemester()
		);
		belegungTagesschule.setKeineKesbPlatzierung(
			belegungTagesschuleJAXP.isKeineKesbPlatzierung()
		);
		belegungTagesschule.setBemerkung(
			belegungTagesschuleJAXP.getBemerkung()
		);
		return belegungTagesschule;
	}

	public BelegungTagesschuleModul belegungTagesschuleModulToEntity(
		@Nonnull JaxBelegungTagesschuleModul belegungTagesschuleModulJAXP,
		@Nonnull BelegungTagesschuleModul belegungTagesschuleModul,
		@Nonnull BelegungTagesschule parent
	) {
		belegungTagesschuleModul.setIntervall(
			belegungTagesschuleModulJAXP.getIntervall()
		);
		belegungTagesschuleModul.setModulTagesschule(
			persistence.find(
				ModulTagesschule.class,
				belegungTagesschuleModulJAXP.getModulTagesschule().getId()
			)
		);
		belegungTagesschuleModul.setBelegungTagesschule(parent);
		return belegungTagesschuleModul;
	}

	@Nonnull
	private Set<BelegungTagesschuleModul> belegungTagesschuleModulListToEntity(
		@Nonnull Set<JaxBelegungTagesschuleModul> jaxBelegungTagesschuleModulList,
		@Nonnull Set<BelegungTagesschuleModul> belegungTagesschuleModulList,
		@Nonnull BelegungTagesschule parent
	) {

		final Set<BelegungTagesschuleModul> convertedBelegungTagesschuleModule =
			new TreeSet<>();
		for (final JaxBelegungTagesschuleModul jaxBelegungTagesschuleModul : jaxBelegungTagesschuleModulList) {
			final BelegungTagesschuleModul belegungModulToMergeWith =
				belegungTagesschuleModulList.stream()
					.filter(
						existingBelegungModul -> existingBelegungModul.getId()
							.equals(jaxBelegungTagesschuleModul.getId())
					)
					.reduce(StreamsUtil.toOnlyElement())
					.orElseGet(BelegungTagesschuleModul::new);
			final BelegungTagesschuleModul belegungModulToAdd =
				belegungTagesschuleModulToEntity(
					jaxBelegungTagesschuleModul,
					belegungModulToMergeWith,
					parent
				);
			final boolean added = convertedBelegungTagesschuleModule.add(
				belegungModulToAdd
			);
			if (!added) {
				LOGGER.warn(
					"dropped duplicate BelegungTagesschuleModul {}",
					belegungModulToAdd
				);
			}
		}
		return convertedBelegungTagesschuleModule;
	}

	@Nonnull
	public AnmeldungTagesschule anmeldungTagesschuleToStoreableEntity(
		@Nonnull final JaxBetreuung betreuungJAXP
	) {
		requireNonNull(betreuungJAXP);

		AnmeldungTagesschule betreuungToMergeWith = Optional.ofNullable(
			betreuungJAXP.getId()
		)
			.flatMap(id -> betreuungService.findAnmeldungTagesschule(id))
			.orElseGet(AnmeldungTagesschule::new);
		return this.anmeldungTagesschuleToEntity(
			betreuungJAXP,
			betreuungToMergeWith
		);
	}

	@Nonnull
	public AnmeldungFerieninsel anmeldungFerieninselToStoreableEntity(
		@Nonnull final JaxBetreuung betreuungJAXP
	) {
		requireNonNull(betreuungJAXP);

		AnmeldungFerieninsel betreuungToMergeWith = Optional.ofNullable(
			betreuungJAXP.getId()
		)
			.flatMap(id -> betreuungService.findAnmeldungFerieninsel(id))
			.orElseGet(AnmeldungFerieninsel::new);

		return this.anmeldungFerieninselToEntity(
			betreuungJAXP,
			betreuungToMergeWith
		);
	}

	public Betreuung betreuungToStoreableEntity(
		@Nonnull final JaxBetreuung betreuungJAXP
	) {
		requireNonNull(betreuungJAXP);

		Betreuung betreuungToMergeWith = Optional.ofNullable(
			betreuungJAXP.getId()
		)
			.flatMap(id -> betreuungService.findBetreuung(id))
			.orElseGet(Betreuung::new);

		Betreuung betreuung = betreuungToEntity(
			betreuungJAXP,
			betreuungToMergeWith
		);

		BigDecimal oeffnungstageMittagstisch = einstellungService
			.getEinstellungAsBigDecimal(
				OEFFNUNGSTAGE_MITTAGSTISCH,
				betreuung
			);
		PensumUtil.transformBetreuungsPensumContainers(
			betreuung,
			oeffnungstageMittagstisch
		);

		return betreuung;
	}

	public <T extends AbstractPlatz> T platzToStoreableEntity(
		@Nonnull final JaxBetreuung betreuungJAXP
	) {
		if (betreuungJAXP.getInstitutionStammdaten().getBetreuungsangebotTyp()
			== BetreuungsangebotTyp.TAGESSCHULE) {
			return (T) anmeldungTagesschuleToStoreableEntity(betreuungJAXP);
		} else if (betreuungJAXP.getInstitutionStammdaten()
			.getBetreuungsangebotTyp()
			== BetreuungsangebotTyp.FERIENINSEL) {
			return (T) anmeldungFerieninselToStoreableEntity(betreuungJAXP);
		}
		return (T) betreuungToStoreableEntity(betreuungJAXP);
	}

	public void addAbweichungenToBetreuung(
		final Set<BetreuungspensumAbweichung> betreuungspensumAbweichungen,
		final Betreuung betreuung
	) {

		betreuungspensumAbweichungen.forEach(c -> c.setBetreuung(betreuung));
		betreuung.getBetreuungspensumAbweichungen().clear();
		betreuung.getBetreuungspensumAbweichungen()
			.addAll(betreuungspensumAbweichungen);
	}

	private void setBetreuungInbetreuungsPensumContainers(
		final Set<BetreuungspensumContainer> betreuungspensumContainers,
		final Betreuung betreuung
	) {

		betreuungspensumContainers.forEach(c -> c.setBetreuung(betreuung));
	}

	private void setBetreuungInAbwesenheiten(
		final Set<AbwesenheitContainer> abwesenheiten,
		final Betreuung betreuung
	) {

		abwesenheiten.forEach(
			abwesenheit -> abwesenheit.setBetreuung(betreuung)
		);
	}

	/**
	 * Goes through the whole list of jaxBetPenContainers. For each (jax)Container that already exists as Entity it
	 * merges both
	 * and adds the resulting (jax) container to the list. If the container doesn't exist it creates a new one and adds
	 * it to the
	 * list. Thus all containers that existed as entity but not in the list of jax, won't be added to the list and are
	 * then
	 * removed (cascade and orphanremoval)
	 *
	 * @param jaxBetPenContainers Betreuungspensen DTOs from Client
	 * @param existingBetreuungspensen List of currently stored BetreungspensumContainers
	 */
	private void betreuungsPensumContainersToEntity(
		final List<JaxBetreuungspensumContainer> jaxBetPenContainers,
		final Collection<BetreuungspensumContainer> existingBetreuungspensen
	) {

		final Set<BetreuungspensumContainer> transformedBetPenContainers =
			new TreeSet<>();
		for (final JaxBetreuungspensumContainer jaxBetPensContainer : jaxBetPenContainers) {
			final BetreuungspensumContainer containerToMergeWith =
				existingBetreuungspensen.stream()
					.filter(
						existingBetPenEntity -> existingBetPenEntity.getId()
							.equals(jaxBetPensContainer.getId())
					)
					.reduce(StreamsUtil.toOnlyElement())
					.orElse(new BetreuungspensumContainer());
			final BetreuungspensumContainer contToAdd =
				betreuungspensumContainerToEntity(
					jaxBetPensContainer,
					containerToMergeWith
				);
			final boolean added = transformedBetPenContainers.add(contToAdd);
			if (!added) {
				LOGGER.warn(DROPPED_DUPLICATE_CONTAINER + "{}", contToAdd);
			}
		}

		// change the existing collection to reflect changes
		// Already tested: All existing Betreuungspensen of the list remain as they were, that means their data are
		// updated and the objects are not created again. ID and InsertTimeStamp are the same as before
		existingBetreuungspensen.clear();
		existingBetreuungspensen.addAll(transformedBetPenContainers);
	}

	private void abwesenheitContainersToEntity(
		final List<JaxAbwesenheitContainer> jaxAbwesenheitContainers,
		final Collection<AbwesenheitContainer> existingAbwesenheiten
	) {

		final Set<AbwesenheitContainer> transformedAbwesenheitContainers =
			new TreeSet<>();
		for (final JaxAbwesenheitContainer jaxAbwesenheitContainer : jaxAbwesenheitContainers) {
			final AbwesenheitContainer containerToMergeWith =
				existingAbwesenheiten.stream()
					.filter(
						existingAbwesenheitEntity -> existingAbwesenheitEntity
							.getId()
							.equals(jaxAbwesenheitContainer.getId())
					)
					.reduce(StreamsUtil.toOnlyElement())
					.orElse(new AbwesenheitContainer());
			final String oldID = containerToMergeWith.getId();
			final AbwesenheitContainer contToAdd = abwesenheitContainerToEntity(
				jaxAbwesenheitContainer,
				containerToMergeWith
			);
			contToAdd.setId(oldID);
			final boolean added = transformedAbwesenheitContainers.add(
				contToAdd
			);
			if (!added) {
				LOGGER.warn(DROPPED_DUPLICATE_CONTAINER + "{}", contToAdd);
			}
		}

		// change the existing collection to reflect changes
		// Already tested: All existing Betreuungspensen of the list remain as they were, that means their data are
		// updated and the objects are not created again. ID and InsertTimeStamp are the same as before
		existingAbwesenheiten.clear();
		existingAbwesenheiten.addAll(transformedAbwesenheitContainers);
	}

	private Abwesenheit abwesenheitToEntity(
		final JaxAbwesenheit jaxAbwesenheit,
		final Abwesenheit abwesenheit
	) {
		convertAbstractDateRangedFieldsToEntity(jaxAbwesenheit, abwesenheit);
		return abwesenheit;
	}

	private BetreuungspensumContainer betreuungspensumContainerToEntity(
		final JaxBetreuungspensumContainer jaxBetPenContainers,
		final BetreuungspensumContainer bpContainer
	) {

		requireNonNull(jaxBetPenContainers);
		requireNonNull(bpContainer);

		convertAbstractVorgaengerFieldsToEntity(
			jaxBetPenContainers,
			bpContainer
		);
		if (jaxBetPenContainers.getBetreuungspensumGS() != null) {
			Betreuungspensum betPensGS = new Betreuungspensum();
			if (bpContainer.getBetreuungspensumGS() != null) {
				betPensGS = bpContainer.getBetreuungspensumGS();
			}
			bpContainer.setBetreuungspensumGS(
				betreuungspensumToEntity(
					jaxBetPenContainers.getBetreuungspensumGS(),
					betPensGS
				)
			);
		}
		if (jaxBetPenContainers.getBetreuungspensumJA() != null) {
			Betreuungspensum betPensJA = new Betreuungspensum();
			if (bpContainer.getBetreuungspensumJA() != null) {
				betPensJA = bpContainer.getBetreuungspensumJA();
			}
			bpContainer.setBetreuungspensumJA(
				betreuungspensumToEntity(
					jaxBetPenContainers.getBetreuungspensumJA(),
					betPensJA
				)
			);
		}

		return bpContainer;
	}

	private AbwesenheitContainer abwesenheitContainerToEntity(
		final JaxAbwesenheitContainer jaxAbwesenheitContainers,
		final AbwesenheitContainer abwesenheitContainer
	) {

		requireNonNull(jaxAbwesenheitContainers);
		requireNonNull(abwesenheitContainer);

		convertAbstractVorgaengerFieldsToEntity(
			jaxAbwesenheitContainers,
			abwesenheitContainer
		);
		if (jaxAbwesenheitContainers.getAbwesenheitGS() != null) {
			Abwesenheit abwesenheitGS = new Abwesenheit();
			if (abwesenheitContainer.getAbwesenheitGS() != null) {
				abwesenheitGS = abwesenheitContainer.getAbwesenheitGS();
			}
			// Das Setzen von alten IDs ist noetigt im Fall dass Betreuungsangebot fuer eine existierende Abwesenheit
			// geaendert wird, da sonst doppelte Verknuepfungen gemacht werden
			final String oldID = abwesenheitGS.getId();
			final Abwesenheit convertedAbwesenheitGS =
				abwesenheitToEntity(
					jaxAbwesenheitContainers.getAbwesenheitGS(),
					abwesenheitGS
				);
			convertedAbwesenheitGS.setId(oldID);
			abwesenheitContainer.setAbwesenheitGS(convertedAbwesenheitGS);
		}
		if (jaxAbwesenheitContainers.getAbwesenheitJA() != null) {
			Abwesenheit abwesenheitJA = new Abwesenheit();
			if (abwesenheitContainer.getAbwesenheitJA() != null) {
				abwesenheitJA = abwesenheitContainer.getAbwesenheitJA();
			}
			//siehe Kommentar oben bei abwesenheitGS
			final String oldID = abwesenheitJA.getId();
			final Abwesenheit convertedAbwesenheitJA =
				abwesenheitToEntity(
					jaxAbwesenheitContainers.getAbwesenheitJA(),
					abwesenheitJA
				);
			convertedAbwesenheitJA.setId(oldID);
			abwesenheitContainer.setAbwesenheitJA(convertedAbwesenheitJA);
		}
		return abwesenheitContainer;
	}

	private Betreuungspensum betreuungspensumToEntity(
		final JaxBetreuungspensum jaxBetreuungspensum,
		final Betreuungspensum betreuungspensum
	) {

		convertAbstractPensumFieldsToEntity(
			jaxBetreuungspensum,
			betreuungspensum
		);
		betreuungspensum.setNichtEingetreten(
			jaxBetreuungspensum.getNichtEingetreten()
		);
		betreuungspensum.setBetreuungInFerienzeit(
			jaxBetreuungspensum.getBetreuungInFerienzeit()
		);
		return betreuungspensum;
	}

	@Nonnull
	public Set<JaxBetreuung> anmeldungTagesschuleListToJax(
		@Nullable final Set<AnmeldungTagesschule> betreuungen
	) {
		if (betreuungen == null) {
			return Collections.emptySet();
		}

		return betreuungen.stream()
			.map(this::anmeldungTagesschuleToJAX)
			.collect(Collectors.toCollection(TreeSet::new));
	}

	@Nonnull
	public Set<JaxBetreuung> anmeldungFerieninselListToJax(
		@Nullable final Set<AnmeldungFerieninsel> betreuungen
	) {
		if (betreuungen == null) {
			return Collections.emptySet();
		}

		return betreuungen.stream()
			.map(this::anmeldungFerieninselToJAX)
			.collect(Collectors.toCollection(TreeSet::new));
	}

	/**
	 * converts the given betreuungList into a JaxBetreuungList
	 *
	 * @return List with Betreuung DTOs
	 */
	public Collection<JaxBetreuung> betreuungListToJax(
		Collection<Betreuung> betreuungList
	) {
		return betreuungList.stream()
			.map(this::betreuungToJAX)
			.collect(Collectors.toList());
	}

	@Nonnull
	private JaxBetreuung abstractPlatzToJAX(
		@Nonnull final AbstractPlatz betreuungFromServer
	) {
		final JaxBetreuung jaxBetreuung = new JaxBetreuung();
		convertAbstractVorgaengerFieldsToJAX(betreuungFromServer, jaxBetreuung);
		jaxBetreuung.setInstitutionStammdaten(
			institutionStammdatenConverter.institutionStammdatenSummaryToJAX(
				betreuungFromServer.getInstitutionStammdaten(),
				new JaxInstitutionStammdatenSummary()
			)
		);
		jaxBetreuung.setBetreuungNummer(
			betreuungFromServer.getBetreuungNummer()
		);
		jaxBetreuung.setKindFullname(
			betreuungFromServer.getKind().getKindJA().getFullName()
		);
		jaxBetreuung.setKindNummer(
			betreuungFromServer.getKind().getKindNummer()
		);
		jaxBetreuung.setKindId(betreuungFromServer.getKind().getId());
		if (betreuungFromServer.getKind().getGesuch() != null) {
			jaxBetreuung.setGesuchId(
				betreuungFromServer.getKind().getGesuch().getId()
			);
			jaxBetreuung.setGesuchsperiode(
				gesuchsperiodeToJAX(
					betreuungFromServer.getKind()
						.getGesuch()
						.getGesuchsperiode()
				)
			);
		}
		jaxBetreuung.setGueltig(betreuungFromServer.isGueltig());
		jaxBetreuung.setReferenzNummer(betreuungFromServer.getReferenzNummer());
		jaxBetreuung.setFinSitRueckwirkendKorrigiertInThisMutation(
			betreuungFromServer.isFinSitRueckwirkendKorrigiertInThisMutation()
		);
		return jaxBetreuung;
	}

	@Nonnull
	public JaxBetreuung anmeldungTagesschuleToJAX(
		@Nonnull final AnmeldungTagesschule betreuungFromServer
	) {
		JaxBetreuung jaxBetreuung = abstractPlatzToJAX(betreuungFromServer);
		jaxBetreuung.setBetreuungsstatus(
			betreuungFromServer.getBetreuungsstatus()
		);
		jaxBetreuung.setAnmeldungMutationZustand(
			betreuungFromServer.getAnmeldungMutationZustand()
		);
		jaxBetreuung.setKeineDetailinformationen(
			betreuungFromServer.isKeineDetailinformationen()
		);
		jaxBetreuung.setBelegungTagesschule(
			belegungTagesschuleToJax(
				betreuungFromServer.getBelegungTagesschule()
			)
		);
		// Für die Anzeige auf dem GUI interessiert es uns nicht, ob es eine echte/gespeicherte Verfügung
		// oder eine Preview-Verfügung ist
		if (betreuungFromServer.getVerfuegungOrVerfuegungPreview() != null) {
			jaxBetreuung.setVerfuegung(
				verfuegungConverter.verfuegungToJax(
					betreuungFromServer.getVerfuegungOrVerfuegungPreview()
				)
			);
		}
		setMandatoryFieldsOnJaxBetreuungForAnmeldungen(jaxBetreuung);
		return jaxBetreuung;
	}

	@Nullable
	private JaxBelegungTagesschule belegungTagesschuleToJax(
		@Nullable BelegungTagesschule belegungFromServer
	) {
		if (belegungFromServer == null) {
			return null;
		}
		final JaxBelegungTagesschule jaxBelegungTagesschule =
			new JaxBelegungTagesschule();
		convertAbstractVorgaengerFieldsToJAX(
			belegungFromServer,
			jaxBelegungTagesschule
		);
		jaxBelegungTagesschule.setBelegungTagesschuleModule(
			belegungTagesschuleModuleListToJax(
				belegungFromServer.getBelegungTagesschuleModule()
			)
		);
		jaxBelegungTagesschule.setEintrittsdatum(
			belegungFromServer.getEintrittsdatum()
		);
		jaxBelegungTagesschule.setAbholungTagesschule(
			belegungFromServer.getAbholungTagesschule()
		);
		jaxBelegungTagesschule.setPlanKlasse(
			belegungFromServer.getPlanKlasse()
		);
		jaxBelegungTagesschule.setFleischOption(
			belegungFromServer.getFleischOption()
		);
		jaxBelegungTagesschule.setAllergienUndUnvertraeglichkeiten(
			belegungFromServer.getAllergienUndUnvertraeglichkeiten()
		);
		jaxBelegungTagesschule.setNotfallnummer(
			belegungFromServer.getNotfallnummer()
		);
		jaxBelegungTagesschule.setAbweichungZweitesSemester(
			belegungFromServer.isAbweichungZweitesSemester()
		);
		jaxBelegungTagesschule.setKeineKesbPlatzierung(
			belegungFromServer.isKeineKesbPlatzierung()
		);
		jaxBelegungTagesschule.setBemerkung(belegungFromServer.getBemerkung());

		return jaxBelegungTagesschule;
	}

	private Set<JaxBelegungTagesschuleModul> belegungTagesschuleModuleListToJax(
		Set<BelegungTagesschuleModul> belegungTagesschuleModule
	) {
		if (belegungTagesschuleModule == null) {
			return Collections.emptySet();
		}
		return belegungTagesschuleModule.stream()
			.map(this::belegungTagesschuleModulToJax)
			.collect(Collectors.toSet());
	}

	@Nullable
	private JaxBelegungTagesschuleModul belegungTagesschuleModulToJax(
		@Nullable BelegungTagesschuleModul modulTagesschule
	) {
		if (modulTagesschule == null) {
			return null;
		}
		final JaxBelegungTagesschuleModul jaxBelegungTagesschuleModul =
			new JaxBelegungTagesschuleModul();
		convertAbstractFieldsToJAX(
			modulTagesschule,
			jaxBelegungTagesschuleModul
		);
		jaxBelegungTagesschuleModul.setIntervall(
			modulTagesschule.getIntervall()
		);
		jaxBelegungTagesschuleModul.setModulTagesschule(
			Objects.requireNonNull(
				modulTagesschuleToJAX(modulTagesschule.getModulTagesschule())
			)
		);
		return jaxBelegungTagesschuleModul;
	}

	@Nonnull
	public JaxBetreuung anmeldungFerieninselToJAX(
		@Nonnull final AnmeldungFerieninsel betreuungFromServer
	) {
		JaxBetreuung jaxBetreuung = abstractPlatzToJAX(betreuungFromServer);
		jaxBetreuung.setBetreuungsstatus(
			betreuungFromServer.getBetreuungsstatus()
		);
		jaxBetreuung.setAnmeldungMutationZustand(
			betreuungFromServer.getAnmeldungMutationZustand()
		);
		jaxBetreuung.setBelegungFerieninsel(
			belegungFerieninselToJAX(
				betreuungFromServer.getBelegungFerieninsel()
			)
		);
		setMandatoryFieldsOnJaxBetreuungForAnmeldungen(jaxBetreuung);
		return jaxBetreuung;
	}

	private void setMandatoryFieldsOnJaxBetreuungForAnmeldungen(
		@Nonnull JaxBetreuung jaxBetreuung
	) {
		// Wir verwenden Client-seitig dasselbe Objekt für Betreuungen und Anmeldungen
		// Auf JaxBetreuung sind einige Felder zwingend, die für Anmeldungen nicht benötigt werden,
		// diese müssen hier initialisiert werden
		jaxBetreuung.setVertrag(Boolean.TRUE);
	}

	@Nonnull
	public JaxBetreuung betreuungToJAX(
		@Nonnull final Betreuung betreuungFromServer
	) {
		JaxBetreuung jaxBetreuung = abstractPlatzToJAX(betreuungFromServer);
		jaxBetreuung.setGrundAblehnung(betreuungFromServer.getGrundAblehnung());
		jaxBetreuung.setDatumAblehnung(betreuungFromServer.getDatumAblehnung());
		jaxBetreuung.setDatumBestaetigung(
			betreuungFromServer.getDatumBestaetigung()
		);
		jaxBetreuung.setDatumAngefordert(
			betreuungFromServer.getDatumAngefordert()
		);
		jaxBetreuung.setBetreuungspensumContainers(
			betreuungsPensumContainersToJax(
				betreuungFromServer.getBetreuungspensumContainers()
			)
		);
		jaxBetreuung.setErweiterteBetreuungContainer(
			erweiterteBetreuungConverter.erweiterteBetreuungContainerToJax(
				betreuungFromServer.getErweiterteBetreuungContainer()
			)
		);
		jaxBetreuung.setAbwesenheitContainers(
			abwesenheitContainersToJax(
				betreuungFromServer.getAbwesenheitContainers()
			)
		);
		jaxBetreuung.setBetreuungsstatus(
			betreuungFromServer.getBetreuungsstatus()
		);
		jaxBetreuung.setVertrag(betreuungFromServer.getVertrag());
		// Für die Anzeige auf dem GUI interessiert es uns nicht, ob es eine echte/gespeicherte Verfügung
		// oder eine Preview-Verfügung ist
		if (betreuungFromServer.getVerfuegungOrVerfuegungPreview() != null) {
			jaxBetreuung.setVerfuegung(
				verfuegungConverter.verfuegungToJax(
					betreuungFromServer.getVerfuegungOrVerfuegungPreview()
				)
			);
		}

		jaxBetreuung.setBetreuungMutiert(
			betreuungFromServer.getBetreuungMutiert()
		);
		jaxBetreuung.setAbwesenheitMutiert(
			betreuungFromServer.getAbwesenheitMutiert()
		);
		jaxBetreuung.setEingewoehnung(betreuungFromServer.isEingewoehnung());
		jaxBetreuung.setAuszahlungAnEltern(
			betreuungFromServer.isAuszahlungAnEltern()
		);
		jaxBetreuung.setBegruendungAuszahlungAnInstitution(
			betreuungFromServer.getBegruendungAuszahlungAnInstitution()
		);
		jaxBetreuung.setBedarfsstufe(betreuungFromServer.getBedarfsstufe());
		return jaxBetreuung;
	}

	@Nonnull
	public <T extends AbstractPlatz> JaxBetreuung platzToJAX(
		@Nonnull final T platz
	) {
		if (platz.getBetreuungsangebotTyp().isTagesschule()) {
			return anmeldungTagesschuleToJAX((AnmeldungTagesschule) platz);
		} else if (platz.getBetreuungsangebotTyp().isFerieninsel()) {
			return anmeldungFerieninselToJAX((AnmeldungFerieninsel) platz);
		}
		return betreuungToJAX((Betreuung) platz);
	}

	@Nonnull
	public List<JaxBetreuungspensumAbweichung> betreuungspensumAbweichungenToJax(
		@Nonnull Betreuung betreuung
	) {
		return AbweichungInitializingUtil
			.fillAbweichungen(
				betreuungService.getMultiplierForAbweichnungen(betreuung),
				betreuung
			)
			.stream()
			.map(abweichung -> betreuungspensumAbweichungToJax(abweichung))
			.collect(Collectors.toList());
	}

	@Nonnull
	private JaxBetreuungspensumAbweichung betreuungspensumAbweichungToJax(
		@Nonnull BetreuungspensumAbweichung abweichung
	) {
		JaxBetreuungspensumAbweichung jaxAbweichung =
			new JaxBetreuungspensumAbweichung();
		convertAbstractPensumFieldsToJAX(abweichung, jaxAbweichung);
		jaxAbweichung.setVertraglicheKosten(abweichung.getVertraglicheKosten());
		jaxAbweichung.setVertraglichesPensum(
			abweichung.getVertraglichesPensum()
		);
		jaxAbweichung.setVertraglicheHauptmahlzeiten(
			abweichung.getVertraglicheHauptmahlzeiten()
		);
		jaxAbweichung.setVertraglicheNebenmahlzeiten(
			abweichung.getVertraglicheNebenmahlzeiten()
		);
		jaxAbweichung.setStatus(abweichung.getStatus());
		jaxAbweichung.setMonatlicheHauptmahlzeiten(
			abweichung.getMonatlicheHauptmahlzeiten()
		);
		jaxAbweichung.setMonatlicheNebenmahlzeiten(
			abweichung.getMonatlicheNebenmahlzeiten()
		);
		jaxAbweichung.setTarifProHauptmahlzeit(
			abweichung.getTarifProHauptmahlzeit()
		);
		jaxAbweichung.setTarifProNebenmahlzeit(
			abweichung.getTarifProNebenmahlzeit()
		);
		jaxAbweichung.setVertraglicherTarifHaupt(
			abweichung.getVertraglicherTarifHauptmahlzeit()
		);
		jaxAbweichung.setVertraglicherTarifNeben(
			abweichung.getVertraglicherTarifNebenmahlzeit()
		);
		jaxAbweichung.setMultiplier(abweichung.getMultiplier());
		if (abweichung.getVertraglicheEingewoehnung() != null) {
			var jaxEingewoehnung = eingewoehnungToJax(
				abweichung.getVertraglicheEingewoehnung(),
				new JaxEingewoehnung()
			);
			if (abweichung.getEingewoehnung() != null) {
				jaxEingewoehnung.setId(abweichung.getEingewoehnung().getId());
			}
			jaxAbweichung.setEingewoehnung(jaxEingewoehnung);
		}

		jaxAbweichung.setVertraglicheBetreuuteTage(
			abweichung.getVertraglicheBetreuuteTage()
		);

		return jaxAbweichung;
	}

	/**
	 * calls betreuungsPensumContainerToJax for each betreuungspensumContainer found in given the list
	 */
	@Nonnull
	private List<JaxBetreuungspensumContainer> betreuungsPensumContainersToJax(
		@Nullable Set<BetreuungspensumContainer> betreuungspensumContainers
	) {

		if (betreuungspensumContainers == null) {
			return Collections.emptyList();
		}

		return betreuungspensumContainers.stream()
			.map(this::betreuungsPensumContainerToJax)
			.collect(Collectors.toList());
	}

	@Nonnull
	private List<JaxAbwesenheitContainer> abwesenheitContainersToJax(
		@Nullable Set<AbwesenheitContainer> abwesenheiten
	) {

		if (abwesenheiten == null) {
			return Collections.emptyList();
		}

		return abwesenheiten.stream()
			.map(this::abwesenheitContainerToJax)
			.collect(Collectors.toList());
	}

	@Nullable
	private JaxAbwesenheit abwesenheitToJax(@Nullable Abwesenheit abwesenheit) {
		if (abwesenheit == null) {
			return null;
		}

		JaxAbwesenheit jaxAbwesenheit = new JaxAbwesenheit();
		convertAbstractDateRangedFieldsToJAX(abwesenheit, jaxAbwesenheit);

		return jaxAbwesenheit;
	}

	@Nullable
	private JaxAbwesenheitContainer abwesenheitContainerToJax(
		@Nullable AbwesenheitContainer abwesenheitContainer
	) {
		if (abwesenheitContainer == null) {
			return null;
		}

		JaxAbwesenheitContainer jaxAbwesenheitContainer =
			new JaxAbwesenheitContainer();
		convertAbstractVorgaengerFieldsToJAX(
			abwesenheitContainer,
			jaxAbwesenheitContainer
		);

		if (abwesenheitContainer.getAbwesenheitGS() != null) {
			jaxAbwesenheitContainer.setAbwesenheitGS(
				abwesenheitToJax(abwesenheitContainer.getAbwesenheitGS())
			);
		}

		if (abwesenheitContainer.getAbwesenheitJA() != null) {
			jaxAbwesenheitContainer.setAbwesenheitJA(
				abwesenheitToJax(abwesenheitContainer.getAbwesenheitJA())
			);
		}

		return jaxAbwesenheitContainer;
	}

	@Nullable
	private JaxBetreuungspensumContainer betreuungsPensumContainerToJax(
		@Nullable BetreuungspensumContainer betreuungspensumContainer
	) {

		if (betreuungspensumContainer == null) {
			return null;
		}

		JaxBetreuungspensumContainer jaxBetreuungspensumContainer =
			new JaxBetreuungspensumContainer();
		convertAbstractVorgaengerFieldsToJAX(
			betreuungspensumContainer,
			jaxBetreuungspensumContainer
		);

		if (betreuungspensumContainer.getBetreuungspensumGS() != null) {
			JaxBetreuungspensum jaxPensum = betreuungspensumToJax(
				betreuungspensumContainer.getBetreuungspensumGS()
			);
			jaxBetreuungspensumContainer.setBetreuungspensumGS(jaxPensum);
		}

		if (betreuungspensumContainer.getBetreuungspensumJA() != null) {
			JaxBetreuungspensum jaxPensum = betreuungspensumToJax(
				betreuungspensumContainer.getBetreuungspensumJA()
			);
			jaxBetreuungspensumContainer.setBetreuungspensumJA(jaxPensum);
		}

		return jaxBetreuungspensumContainer;
	}

	@Nonnull
	private JaxBetreuungspensum betreuungspensumToJax(
		@Nonnull Betreuungspensum betreuungspensum
	) {

		JaxBetreuungspensum jaxBetreuungspensum = new JaxBetreuungspensum();
		convertAbstractPensumFieldsToJAX(betreuungspensum, jaxBetreuungspensum);
		jaxBetreuungspensum.setNichtEingetreten(
			betreuungspensum.getNichtEingetreten()
		);
		jaxBetreuungspensum.setMonatlicheHauptmahlzeiten(
			betreuungspensum.getMonatlicheHauptmahlzeiten()
		);
		jaxBetreuungspensum.setMonatlicheNebenmahlzeiten(
			betreuungspensum.getMonatlicheNebenmahlzeiten()
		);
		jaxBetreuungspensum.setTarifProHauptmahlzeit(
			betreuungspensum.getTarifProHauptmahlzeit()
		);
		jaxBetreuungspensum.setTarifProNebenmahlzeit(
			betreuungspensum.getTarifProNebenmahlzeit()
		);
		jaxBetreuungspensum.setBetreuungInFerienzeit(
			betreuungspensum.getBetreuungInFerienzeit()
		);

		return jaxBetreuungspensum;
	}

}
