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

package ch.dvbern.ebegu.api.converter;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.gesuch.JaxFallDossierConverter;
import ch.dvbern.ebegu.api.converter.gesuch.betreuung.JaxBetreuungAnmeldungPlatzConverter;
import ch.dvbern.ebegu.api.converter.gesuch.finsit.JaxFinanzielleSituationConverter;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungsmitteilung;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungsmitteilungPensum;
import ch.dvbern.ebegu.api.dtos.JaxMitteilung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.MitteilungTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.InstitutionService;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxMitteilungConverter extends AbstractBaseConverter {
	@Inject
	private BenutzerService benutzerService;
	@Inject
	private DossierService dossierService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private FinanzielleSituationService finanzielleSituationService;
	@Inject
	private JaxBenutzerConverter benutzerConverter;
	@Inject
	private JaxFallDossierConverter dossierConverter;
	@Inject
	private JaxBetreuungAnmeldungPlatzConverter betreuungConverter;
	@Inject
	private JaxFinanzielleSituationConverter finanzielleSituationConverter;

	public Mitteilung mitteilungToEntity(
		JaxMitteilung mitteilungJAXP,
		Mitteilung mitteilung
	) {
		requireNonNull(mitteilung);
		requireNonNull(mitteilungJAXP);
		requireNonNull(mitteilungJAXP.getDossier());
		requireNonNull(mitteilungJAXP.getDossier().getId());

		convertAbstractVorgaengerFieldsToEntity(mitteilungJAXP, mitteilung);

		if (mitteilungJAXP.getEmpfaenger() != null) {
			Benutzer empfaenger = benutzerService.findBenutzer(
				mitteilungJAXP.getEmpfaenger().getUsername(),
				mitteilung.getDossier().getFall().getMandant()
			)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"mitteilungToEntity - findBenutzer",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						mitteilungJAXP.getEmpfaenger()
					)
				);
			// because the user doesn't come from the client but from the server
			mitteilung.setEmpfaenger(empfaenger);
		}

		mitteilung.setEmpfaengerTyp(mitteilungJAXP.getEmpfaengerTyp());
		Dossier dossier = dossierService.findDossier(
			mitteilungJAXP.getDossier().getId()
		)
			.orElseThrow(
				() -> new EbeguEntityNotFoundException(
					"mitteilungToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					mitteilungJAXP.getDossier()
				)
			);
		requireNonNull(mitteilungJAXP.getDossier());
		mitteilung.setDossier(
			dossierConverter.dossierToEntity(
				mitteilungJAXP.getDossier(),
				dossier
			)
		);

		if (mitteilungJAXP.getBetreuung() != null) {
			mitteilung.setBetreuung(
				betreuungConverter.betreuungToEntity(
					mitteilungJAXP.getBetreuung(),
					new Betreuung()
				)
			);
		}
		if (mitteilungJAXP.getInstitution() != null
			&& mitteilungJAXP.getInstitution().getId() != null) {
			final Optional<Institution> institutionFromDB =
				institutionService.findInstitution(
					mitteilungJAXP.getInstitution().getId(),
					false
				);
			if (institutionFromDB.isPresent()) {
				// Institution darf nicht vom Client ueberschrieben werden
				mitteilung.setInstitution(institutionFromDB.get());
			} else {
				throw new EbeguEntityNotFoundException(
					"mitteilungToEntity - getInstitution",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					mitteilungJAXP.getInstitution().getId()
				);
			}
		} else {
			mitteilung.setInstitution(null);
		}
		mitteilung.setMessage(mitteilungJAXP.getMessage());
		mitteilung.setMitteilungStatus(mitteilungJAXP.getMitteilungStatus());

		if (mitteilungJAXP.getSender() != null) {
			Benutzer sender = benutzerService.findBenutzer(
				mitteilungJAXP.getSender().getUsername(),
				mitteilung.getDossier().getFall().getMandant()
			)
				.orElseThrow(
					() -> new EbeguEntityNotFoundException(
						"mitteilungToEntity",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
						mitteilungJAXP.getSender()
					)
				);
			// because the user doesn't come from the client but from the server
			mitteilung.setSender(sender);
		}

		mitteilung.setSenderTyp(mitteilungJAXP.getSenderTyp());
		mitteilung.setSubject(mitteilungJAXP.getSubject());
		mitteilung.setSentDatum(mitteilungJAXP.getSentDatum());

		return mitteilung;
	}

	public JaxMitteilung mitteilungToJAX(
		Mitteilung persistedMitteilung,
		JaxMitteilung jaxMitteilung
	) {
		convertAbstractVorgaengerFieldsToJAX(
			persistedMitteilung,
			jaxMitteilung
		);
		if (persistedMitteilung.getEmpfaenger() != null) {
			jaxMitteilung.setEmpfaenger(
				benutzerConverter.benutzerToJaxBenutzer(
					persistedMitteilung.getEmpfaenger()
				)
			);
		}
		jaxMitteilung.setEmpfaengerTyp(persistedMitteilung.getEmpfaengerTyp());
		jaxMitteilung.setDossier(
			dossierConverter.dossierToJAX(persistedMitteilung.getDossier())
		);
		if (persistedMitteilung.getBetreuung() != null) {
			jaxMitteilung.setBetreuung(
				betreuungConverter.betreuungToJAX(
					persistedMitteilung.getBetreuung()
				)
			);
		}
		if (persistedMitteilung instanceof NeueVeranlagungsMitteilung) {
			jaxMitteilung.setFinanzielleSituation(
				finanzielleSituationConverter.finanzielleSituationToJAX(
					finanzielleSituationService
						.findFinanzielleSituationForNeueVeranlagungsMitteilung(
							(NeueVeranlagungsMitteilung) persistedMitteilung
						)
				)
			);
		}
		if (persistedMitteilung.getInstitution() != null) {
			jaxMitteilung.setInstitution(
				institutionToJAX(persistedMitteilung.getInstitution())
			);
		}
		jaxMitteilung.setMessage(persistedMitteilung.getMessage());
		jaxMitteilung.setMitteilungStatus(
			persistedMitteilung.getMitteilungStatus()
		);
		jaxMitteilung.setSender(
			benutzerConverter.benutzerToJaxBenutzer(
				persistedMitteilung.getSender()
			)
		);
		jaxMitteilung.setSenderTyp(persistedMitteilung.getSenderTyp());
		jaxMitteilung.setSubject(persistedMitteilung.getSubject());
		jaxMitteilung.setSentDatum(persistedMitteilung.getSentDatum());
		jaxMitteilung.setMitteilungTyp(
			MitteilungTyp.getMitteilungTypByClass(
				persistedMitteilung.getClass()
			)
		);
		return jaxMitteilung;
	}

	/**
	 * Creates the Betreuungsmitteilung without taking into accoutn if it already exists or not
	 */
	@Nonnull
	public Betreuungsmitteilung betreuungsmitteilungToEntity(
		@Nonnull JaxBetreuungsmitteilung mitteilungJAXP,
		@Nonnull Betreuungsmitteilung betreuungsmitteilung
	) {

		requireNonNull(mitteilungJAXP);
		requireNonNull(betreuungsmitteilung);

		mitteilungToEntity(mitteilungJAXP, betreuungsmitteilung);

		betreuungsmitteilung.setApplied(mitteilungJAXP.getApplied());

		if (mitteilungJAXP.getBetreuungspensen() != null) {
			Set<BetreuungsmitteilungPensum> pensen = mitteilungJAXP
				.getBetreuungspensen()
				.stream()
				.map(
					jaxPensum -> toBetreuungsmitteilungPensum(
						jaxPensum,
						betreuungsmitteilung
					)
				)
				.collect(Collectors.toSet());

			betreuungsmitteilung.setBetreuungspensen(pensen);
		}
		return betreuungsmitteilung;
	}

	@Nonnull
	private BetreuungsmitteilungPensum toBetreuungsmitteilungPensum(
		@Nonnull JaxBetreuungsmitteilungPensum jaxPensum,
		@Nonnull Betreuungsmitteilung betreuungsmitteilung
	) {

		BetreuungsmitteilungPensum p = betreuungsmitteilungpensumToEntity(
			jaxPensum,
			new BetreuungsmitteilungPensum()
		);
		p.setBetreuungsmitteilung(betreuungsmitteilung);

		return p;
	}

	@Nonnull
	public JaxBetreuungsmitteilung betreuungsmitteilungToJAX(
		@Nonnull Betreuungsmitteilung persistedMitteilung
	) {
		final JaxBetreuungsmitteilung jaxBetreuungsmitteilung =
			new JaxBetreuungsmitteilung();
		mitteilungToJAX(persistedMitteilung, jaxBetreuungsmitteilung);

		jaxBetreuungsmitteilung.setApplied(persistedMitteilung.isApplied());
		jaxBetreuungsmitteilung.setErrorMessage(
			persistedMitteilung.getErrorMessage()
		);
		jaxBetreuungsmitteilung.setBetreuungStornieren(
			persistedMitteilung.isBetreuungStornieren()
		);
		if (persistedMitteilung.getBetreuungspensen() != null) {
			List<JaxBetreuungsmitteilungPensum> pensen = persistedMitteilung
				.getBetreuungspensen()
				.stream()
				.map(this::betreuungsmitteilungPensumToJax)
				.collect(Collectors.toList());
			jaxBetreuungsmitteilung.setBetreuungspensen(pensen);
		}
		return jaxBetreuungsmitteilung;
	}

	private BetreuungsmitteilungPensum betreuungsmitteilungpensumToEntity(
		final JaxBetreuungsmitteilungPensum jaxBetreuungspensum,
		final BetreuungsmitteilungPensum betreuungspensum
	) {

		convertAbstractPensumFieldsToEntity(
			jaxBetreuungspensum,
			betreuungspensum
		);
		betreuungspensum.setBetreuungInFerienzeit(
			jaxBetreuungspensum.getBetreuungInFerienzeit()
		);

		return betreuungspensum;
	}

	private JaxBetreuungsmitteilungPensum betreuungsmitteilungPensumToJax(
		final BetreuungsmitteilungPensum betreuungspensum
	) {

		final JaxBetreuungsmitteilungPensum jaxBetreuungspensum =
			new JaxBetreuungsmitteilungPensum();

		convertAbstractPensumFieldsToJAX(betreuungspensum, jaxBetreuungspensum);
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
