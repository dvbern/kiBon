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

import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractBaseConverter;
import ch.dvbern.ebegu.api.converter.JaxBenutzerConverter;
import ch.dvbern.ebegu.api.converter.JaxSozialdienstConverter;
import ch.dvbern.ebegu.api.dtos.JaxDossier;
import ch.dvbern.ebegu.api.dtos.JaxFall;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.FallService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.SozialdienstService;

import static java.util.Objects.requireNonNull;

@Dependent
public class JaxFallDossierConverter extends AbstractBaseConverter {
	public static final String DOSSIER_TO_ENTITY = "dossierToEntity";
	@Inject
	private FallService fallService;
	@Inject
	private BenutzerService benutzerService;
	@Inject
	private SozialdienstService sozialdienstService;
	@Inject
	private GemeindeService gemeindeService;
	@Inject
	private JaxSozialdienstConverter sozialdienstConverter;
	@Inject
	private JaxBenutzerConverter benutzerConverter;

	public Fall fallToEntity(
		@Nonnull final JaxFall fallJAXP,
		@Nonnull final Fall fall
	) {
		requireNonNull(fall);
		requireNonNull(fallJAXP);
		convertAbstractVorgaengerFieldsToEntity(fallJAXP, fall);
		convertMandantFieldsToEntity(fall);
		//Fall nummer wird auf server bzw DB verwaltet und daher hier nicht gesetzt, dasselbe fuer NextKindNumber
		if (fallJAXP.getBesitzer() != null) {
			Optional<Benutzer> besitzer = benutzerService.findBenutzer(
				fallJAXP.getBesitzer().getUsername(),
				fall.getMandant()
			);
			if (besitzer.isPresent()) {
				fall.setBesitzer(besitzer.get()); // because the user doesn't come from the client but from the server
			} else {
				throw new EbeguEntityNotFoundException(
					"fallToEntity",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					fallJAXP.getBesitzer()
				);
			}
		}
		if (fallJAXP.getSozialdienstFall() != null) {
			SozialdienstFall sozialdienstFall = new SozialdienstFall();
			if (fallJAXP.getSozialdienstFall().getId() != null) {
				Optional<SozialdienstFall> sozialdienstFallOpt =
					sozialdienstService.findSozialdienstFall(
						fallJAXP.getSozialdienstFall().getId()
					);
				sozialdienstFall = sozialdienstFallOpt.orElse(
					new SozialdienstFall()
				);
			}
			fall.setSozialdienstFall(
				sozialdienstConverter.sozialdienstFallToEntity(
					fallJAXP.getSozialdienstFall(),
					sozialdienstFall
				)
			);
		}
		return fall;
	}

	public JaxFall fallToJAX(@Nonnull final Fall persistedFall) {
		final JaxFall jaxFall = new JaxFall();
		convertAbstractVorgaengerFieldsToJAX(persistedFall, jaxFall);
		jaxFall.setFallNummer(persistedFall.getFallNummer());
		jaxFall.setNextNumberKind(persistedFall.getNextNumberKind());
		if (persistedFall.getBesitzer() != null) {
			jaxFall.setBesitzer(
				benutzerConverter.benutzerToJaxBenutzer(
					persistedFall.getBesitzer()
				)
			);
		}
		if (persistedFall.getSozialdienstFall() != null) {
			jaxFall.setSozialdienstFall(
				sozialdienstConverter.sozialdienstFallToJAX(
					persistedFall.getSozialdienstFall()
				)
			);
		}

		return jaxFall;
	}

	public Dossier dossierToEntity(
		@Nonnull final JaxDossier dossierJAX,
		@Nonnull final Dossier dossier
	) {
		requireNonNull(dossier);
		requireNonNull(dossierJAX);
		requireNonNull(dossierJAX.getFall());
		requireNonNull(dossierJAX.getFall().getId());
		convertAbstractVorgaengerFieldsToEntity(dossierJAX, dossier);
		// Fall darf nicht überschrieben werden
		final Optional<Fall> fallFromDB = fallService.findFall(
			dossierJAX.getFall().getId()
		);
		if (fallFromDB.isPresent()) {
			dossier.setFall(fallFromDB.get());
		} else {
			throw new EbeguEntityNotFoundException(
				DOSSIER_TO_ENTITY,
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				dossierJAX.getFall()
			);
		}
		// Gemeinde darf nicht ueberschrieben werden
		if (dossierJAX.getGemeinde() != null) {
			requireNonNull(dossierJAX.getGemeinde().getId());
			Optional<Gemeinde> gemeindeFromDB = gemeindeService.findGemeinde(
				dossierJAX.getGemeinde().getId()
			);
			if (gemeindeFromDB.isPresent()) {
				dossier.setGemeinde(gemeindeFromDB.get());
			} else {
				throw new EbeguEntityNotFoundException(
					DOSSIER_TO_ENTITY,
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					dossierJAX.getFall()
				);
			}
		}
		if (dossierJAX.getVerantwortlicherBG() != null) {
			Optional<Benutzer> verantwortlicher =
				benutzerService.findBenutzer(
					dossierJAX.getVerantwortlicherBG().getUsername(),
					dossier.getFall()
						.getMandant()
				);
			if (verantwortlicher.isPresent()) {
				// because the user doesn't come from the client but from the server
				dossier.setVerantwortlicherBG(verantwortlicher.get());
			} else {
				throw new EbeguEntityNotFoundException(
					DOSSIER_TO_ENTITY,
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					dossierJAX.getVerantwortlicherBG()
				);
			}
		} else {
			dossier.setVerantwortlicherBG(null);
		}
		if (dossierJAX.getVerantwortlicherTS() != null) {
			Optional<Benutzer> verantwortlicherTS =
				benutzerService.findBenutzer(
					dossierJAX.getVerantwortlicherTS().getUsername(),
					dossier.getFall().getMandant()
				);
			if (verantwortlicherTS.isPresent()) {
				// because the user doesn't come from the client but from the server
				dossier.setVerantwortlicherTS(verantwortlicherTS.get());
			} else {
				throw new EbeguEntityNotFoundException(
					DOSSIER_TO_ENTITY,
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					dossierJAX.getVerantwortlicherTS()
				);
			}
		} else {
			dossier.setVerantwortlicherTS(null);
		}
		return dossier;
	}

	public JaxDossier dossierToJAX(@Nonnull final Dossier persistedDossier) {
		final JaxDossier jaxDossier = new JaxDossier();
		convertAbstractVorgaengerFieldsToJAX(persistedDossier, jaxDossier);
		jaxDossier.setFall(this.fallToJAX(persistedDossier.getFall()));
		jaxDossier.setGemeinde(gemeindeToJAX(persistedDossier.getGemeinde()));
		jaxDossier.setBemerkungen(persistedDossier.getBemerkungen());
		if (persistedDossier.getVerantwortlicherBG() != null) {
			jaxDossier.setVerantwortlicherBG(
				benutzerConverter.benutzerToJaxBenutzerNoDetails(
					persistedDossier.getVerantwortlicherBG()
				)
			);
		}
		if (persistedDossier.getVerantwortlicherTS() != null) {
			jaxDossier.setVerantwortlicherTS(
				benutzerConverter.benutzerToJaxBenutzerNoDetails(
					persistedDossier.getVerantwortlicherTS()
				)
			);
		}
		return jaxDossier;
	}
}
