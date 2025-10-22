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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.converter.gesuch.betreuung.JaxBetreuungAnmeldungPlatzConverter;
import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxKind;
import ch.dvbern.ebegu.api.dtos.JaxKindContainer;
import ch.dvbern.ebegu.api.dtos.JaxPensumAusserordentlicherAnspruch;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.PensumAusserordentlicherAnspruch;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.PensumAusserordentlicherAnspruchService;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxKindConverter extends AbstractBaseConverter {
	@Inject
	private JaxFachstelleConverter fachstelleConverter;
	@Inject
	private PensumAusserordentlicherAnspruchService pensumAusserordentlicherAnspruchService;
	@Inject
	private JaxBetreuungAnmeldungPlatzConverter betreuungAnmeldungPlatzConverter;

	@Nonnull
	public JaxKind kindToJAX(@Nonnull final Kind persistedKind) {
		final JaxKind jaxKind = new JaxKind();
		convertAbstractPersonFieldsToJAX(persistedKind, jaxKind);
		jaxKind.setKinderabzugErstesHalbjahr(
			persistedKind.getKinderabzugErstesHalbjahr()
		);
		jaxKind.setKinderabzugZweitesHalbjahr(
			persistedKind.getKinderabzugZweitesHalbjahr()
		);
		jaxKind.setPflegekind(persistedKind.getPflegekind());
		jaxKind.setPflegeEntschaedigungErhalten(
			persistedKind.getPflegeEntschaedigungErhalten()
		);
		jaxKind.setObhutAlternierendAusueben(
			persistedKind.getObhutAlternierendAusueben()
		);
		jaxKind.setGemeinsamesGesuch(persistedKind.getGemeinsamesGesuch());
		jaxKind.setInErstausbildung(persistedKind.getInErstausbildung());
		jaxKind.setLebtKindAlternierend(
			persistedKind.getLebtKindAlternierend()
		);
		jaxKind.setAlimenteErhalten(persistedKind.getAlimenteErhalten());
		jaxKind.setAlimenteBezahlen(persistedKind.getAlimenteBezahlen());
		jaxKind.setFamilienErgaenzendeBetreuung(
			persistedKind.getFamilienErgaenzendeBetreuung()
		);
		jaxKind.setSprichtAmtssprache(persistedKind.getSprichtAmtssprache());
		jaxKind.setAusAsylwesen(persistedKind.getAusAsylwesen());
		jaxKind.setZemisNummer(persistedKind.getZemisNummer());
		jaxKind.setEinschulungTyp(persistedKind.getEinschulungTyp());
		jaxKind.setKeinPlatzInSchulhort(
			persistedKind.getKeinPlatzInSchulhort()
		);
		jaxKind.setPensumFachstellen(
			fachstelleConverter.pensumFachstellenListToJax(
				persistedKind.getPensumFachstelle()
			)
		);
		jaxKind.setPensumAusserordentlicherAnspruch(
			pensumAusserordentlicherAnspruchToJax(
				persistedKind.getPensumAusserordentlicherAnspruch()
			)
		);
		jaxKind.setZukunftigeGeburtsdatum(
			persistedKind.getZukunftigeGeburtsdatum()
		);
		jaxKind.setInPruefung(persistedKind.getInPruefung());
		jaxKind.setUnterhaltspflichtig(persistedKind.getUnterhaltspflichtig());
		jaxKind.setHoehereBeitraegeWegenBeeintraechtigungBeantragen(
			persistedKind.getHoehereBeitraegeWegenBeeintraechtigungBeantragen()
		);
		jaxKind.setHoehereBeitraegeUnterlagenDigital(
			persistedKind.getHoehereBeitraegeUnterlagenDigital()
		);
		jaxKind.setGueltigkeitTerminiert(
			persistedKind.isGueltigkeitTerminiert()
		);
		jaxKind.setGueltigkeitTerminiertPer(
			persistedKind.getGueltigkeitTerminiertPer()
		);
		jaxKind.setGueltigkeitTerminiertKommentar(
			persistedKind.getGueltigkeitTerminiertKommentar()
		);
		return jaxKind;
	}

	@Nullable
	private JaxPensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruchToJax(
		@Nullable final PensumAusserordentlicherAnspruch persistedPensumAusserordentlicherAnspruch
	) {

		if (persistedPensumAusserordentlicherAnspruch == null) {
			return null;
		}
		final JaxPensumAusserordentlicherAnspruch jaxPensumAusserordentlicherAnspruch =
			new JaxPensumAusserordentlicherAnspruch();
		convertAbstractPensumFieldsToJAX(
			persistedPensumAusserordentlicherAnspruch,
			jaxPensumAusserordentlicherAnspruch
		);
		jaxPensumAusserordentlicherAnspruch.setBegruendung(
			persistedPensumAusserordentlicherAnspruch.getBegruendung()
		);
		return jaxPensumAusserordentlicherAnspruch;
	}

	public JaxKindContainer kindContainerToJAX(
		final KindContainer persistedKind
	) {
		final JaxKindContainer jaxKindContainer = new JaxKindContainer();
		convertAbstractVorgaengerFieldsToJAX(persistedKind, jaxKindContainer);
		if (persistedKind.getKindGS() != null) {
			jaxKindContainer.setKindGS(kindToJAX(persistedKind.getKindGS()));
		}
		if (persistedKind.getKindJA() != null) {
			jaxKindContainer.setKindJA(kindToJAX(persistedKind.getKindJA()));
		}
		jaxKindContainer.setBetreuungen(new TreeSet<>());
		Set<JaxBetreuung> betreuungen = betreuungListToJax(
			persistedKind.getBetreuungen()
		);
		jaxKindContainer.getBetreuungen().addAll(betreuungen);
		Set<JaxBetreuung> anmeldungenTagesschule =
			betreuungAnmeldungPlatzConverter.anmeldungTagesschuleListToJax(
				persistedKind.getAnmeldungenTagesschule()
			);
		jaxKindContainer.getBetreuungen().addAll(anmeldungenTagesschule);
		Set<JaxBetreuung> anmeldungenFerieninsel =
			betreuungAnmeldungPlatzConverter.anmeldungFerieninselListToJax(
				persistedKind.getAnmeldungenFerieninsel()
			);
		jaxKindContainer.getBetreuungen().addAll(anmeldungenFerieninsel);
		jaxKindContainer.setKindNummer(persistedKind.getKindNummer());
		jaxKindContainer.setKeinSelbstbehaltDurchGemeinde(
			persistedKind.getKeinSelbstbehaltDurchGemeinde()
		);
		jaxKindContainer.setNextNumberBetreuung(
			persistedKind.getNextNumberBetreuung()
		);
		return jaxKindContainer;
	}

	@Nonnull
	private Set<JaxBetreuung> betreuungListToJax(
		@Nullable final Set<Betreuung> betreuungen
	) {
		if (betreuungen == null) {
			return Collections.emptySet();
		}

		return betreuungen.stream()
			.map(betreuungAnmeldungPlatzConverter::betreuungToJAX)
			.collect(Collectors.toCollection(TreeSet::new));
	}

	public Kind kindToEntity(final JaxKind kindJAXP, final Kind kind) {
		requireNonNull(kindJAXP);
		requireNonNull(kind);
		convertAbstractPersonFieldsToEntity(kindJAXP, kind);
		kind.setKinderabzugErstesHalbjahr(
			kindJAXP.getKinderabzugErstesHalbjahr()
		);
		kind.setKinderabzugZweitesHalbjahr(
			kindJAXP.getKinderabzugZweitesHalbjahr()
		);
		kind.setPflegekind(Boolean.TRUE.equals(kindJAXP.getPflegekind()));
		kind.setPflegeEntschaedigungErhalten(
			kindJAXP.getPflegeEntschaedigungErhalten()
		);
		kind.setObhutAlternierendAusueben(
			kindJAXP.getObhutAlternierendAusueben()
		);
		kind.setGemeinsamesGesuch(kindJAXP.getGemeinsamesGesuch());
		kind.setInErstausbildung(kindJAXP.getInErstausbildung());
		kind.setLebtKindAlternierend(kindJAXP.getLebtKindAlternierend());
		kind.setAlimenteErhalten(kindJAXP.getAlimenteErhalten());
		kind.setAlimenteBezahlen(kindJAXP.getAlimenteBezahlen());
		kind.setFamilienErgaenzendeBetreuung(
			kindJAXP.getFamilienErgaenzendeBetreuung()
		);
		kind.setSprichtAmtssprache(kindJAXP.getSprichtAmtssprache());
		kind.setAusAsylwesen(kindJAXP.getAusAsylwesen());
		kind.setZemisNummer(kindJAXP.getZemisNummer());
		kind.setEinschulungTyp(kindJAXP.getEinschulungTyp());
		kind.setKeinPlatzInSchulhort(kindJAXP.getKeinPlatzInSchulhort());
		kind.setUnterhaltspflichtig(kindJAXP.getUnterhaltspflichtig());

		fachstelleConverter.pensumFachstellenToEntity(
			kind,
			kindJAXP.getPensumFachstellen()
		);

		PensumAusserordentlicherAnspruch updtPensumAusserordentlicherAnspruch =
			null;
		if (kindJAXP.getPensumAusserordentlicherAnspruch() != null) {
			updtPensumAusserordentlicherAnspruch =
				toStorablePensumAusserordentlicherAnspruch(
					kindJAXP.getPensumAusserordentlicherAnspruch()
				);
		}
		kind.setPensumAusserordentlicherAnspruch(
			updtPensumAusserordentlicherAnspruch
		);
		kind.setZukunftigeGeburtsdatum(kindJAXP.getZukunftigeGeburtsdatum());
		kind.setInPruefung(kindJAXP.getInPruefung());
		kind.setHoehereBeitraegeWegenBeeintraechtigungBeantragen(
			kindJAXP.getHoehereBeitraegeWegenBeeintraechtigungBeantragen()
		);
		kind.setHoehereBeitraegeUnterlagenDigital(
			kindJAXP.getHoehereBeitraegeUnterlagenDigital()
		);
		kind.setGueltigkeitTerminiert(kindJAXP.isGueltigkeitTerminiert());
		kind.setGueltigkeitTerminiertPer(
			kindJAXP.getGueltigkeitTerminiertPer()
		);
		kind.setGueltigkeitTerminiertKommentar(
			kindJAXP.getGueltigkeitTerminiertKommentar()
		);
		return kind;
	}

	@Nonnull
	private PensumAusserordentlicherAnspruch toStorablePensumAusserordentlicherAnspruch(
		@Nonnull final JaxPensumAusserordentlicherAnspruch pensumFsToSave
	) {

		PensumAusserordentlicherAnspruch pensumToMergeWith =
			new PensumAusserordentlicherAnspruch();
		if (pensumFsToSave.getId() != null) {
			final Optional<PensumAusserordentlicherAnspruch> pensumAusserordentlicherAnspruchOpt =
				pensumAusserordentlicherAnspruchService
					.findPensumAusserordentlicherAnspruch(
						pensumFsToSave.getId()
					);
			if (pensumAusserordentlicherAnspruchOpt.isPresent()) {
				pensumToMergeWith = pensumAusserordentlicherAnspruchOpt.get();
			} else {
				throw new EbeguEntityNotFoundException(
					"toStorablePensumAusserordentlicherAnspruch",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					pensumFsToSave.getId()
				);
			}
		}
		return pensumAusserordentlicherAnspruchToEntity(
			pensumFsToSave,
			pensumToMergeWith
		);
	}

	private PensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruchToEntity(
		@Nonnull final JaxPensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruchJAXP,
		@Nonnull final PensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruch
	) {

		convertAbstractPensumFieldsToEntity(
			pensumAusserordentlicherAnspruchJAXP,
			pensumAusserordentlicherAnspruch
		);
		pensumAusserordentlicherAnspruch.setBegruendung(
			pensumAusserordentlicherAnspruchJAXP.getBegruendung()
		);
		return pensumAusserordentlicherAnspruch;
	}

	public KindContainer kindContainerToEntity(
		@Nonnull final JaxKindContainer kindContainerJAXP,
		@Nonnull final KindContainer kindContainer
	) {
		requireNonNull(kindContainer);
		requireNonNull(kindContainerJAXP);
		convertAbstractVorgaengerFieldsToEntity(
			kindContainerJAXP,
			kindContainer
		);
		//kind daten koennen nicht verschwinden
		if (kindContainerJAXP.getKindGS() != null) {
			Kind kindGS = new Kind();
			if (kindContainer.getKindGS() != null) {
				kindGS = kindContainer.getKindGS();
			}
			kindContainer.setKindGS(
				kindToEntity(kindContainerJAXP.getKindGS(), kindGS)
			);
		}
		if (kindContainerJAXP.getKindJA() != null) {
			Kind kindJA = new Kind();
			if (kindContainer.getKindJA() != null) {
				kindJA = kindContainer.getKindJA();
			}
			kindContainer.setKindJA(
				kindToEntity(kindContainerJAXP.getKindJA(), kindJA)
			);
		}
		// nextNumberBetreuung wird nur im Server gesetzt, darf aus dem Client nicht uebernommen werden
		return kindContainer;
	}
}
