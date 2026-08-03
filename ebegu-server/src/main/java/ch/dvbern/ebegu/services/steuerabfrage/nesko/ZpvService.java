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
 *
 */

package ch.dvbern.ebegu.services.steuerabfrage.nesko;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.ws.rs.core.UriBuilder;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.oidc.AuthConstants;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.services.MailService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

import static ch.dvbern.ebegu.services.steuerabfrage.nesko.ZPVUpdateUtil.isZPVAlreadyUsedInGesuch;

/**
 * Service for the zpv linking process.
 *
 * @see <a href="https://www.belex.sites.be.ch/app/de/texts_of_law/152.052">BSG 152.052 - Verordnung über die Zentrale
 * Personenverwaltung (ZPV V)</a>
 * @see <a href="https://intra.dvbern.ch/spaces/KIB/pages/290883081/Verkn%C3%BCpfungsprozess+ZPV-Nummer">The internal
 * technical kiBon docs</a>
 */
@ApplicationScoped
public class ZpvService {

	@Inject
	private GesuchstellerService gesuchstellerService;

	@Inject
	private MailService mailService;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	public void sendInitUpdateZPVNr(
		String email,
		String korrespondenzSprache,
		GesuchstellerContainer gesuchstellerContainer,
		MandantIdentifier mandantIdentifier
	) {

		String loginInitURL = createZPVVerknuepfenLink(
			mandantIdentifier,
			gesuchstellerContainer,
			email
		)
			.toString();

		mailService.prepareToSendInitGSZPVNr(
			loginInitURL,
			gesuchstellerContainer,
			email,
			korrespondenzSprache
		);
	}

	private URI createZPVVerknuepfenLink(
		MandantIdentifier mandantIdentifier,
		GesuchstellerContainer gesuchsteller,
		String email
	) {
		var returnPath = UriBuilder.fromPath(Constants.ZPV_LINK_SUCCESSS_PATH)
			.path(gesuchsteller.getId())
			.build()
			.toString();

		var baseUrl = ebeguConfiguration.getBaseUrl(
			mandantIdentifier
		);

		return UriBuilder.fromUri(baseUrl)
			.scheme("https")
			.replacePath("/ebegu")
			.path(Constants.API_ROOT_PATH)
			.path(AuthConstants.LOGIN_PATH)
			.queryParam(OpenIdConstant.LOGIN_HINT, email)
			.queryParam(
				AuthConstants.RETURN_PATH_PARAM,
				URLEncoder.encode(returnPath, StandardCharsets.UTF_8) // needs to be encoded, otherwise keycloak will consume this query string
			)
			.build();
	}

	/**
	 * <p>
	 * Attempts to set the provided ZPV-Nummer on the {@link Gesuchsteller} belonging to the
	 * {@link GesuchstellerContainer} id
	 * and make the Steuerabfrage retryable by the user.
	 * Checks first, if the preconditions are met. If they are not met, the appropriate {@link ZPVUpdateResult} is
	 * returned.
	 * </p>
	 * <p>
	 * The ZPV-Nummer can only be set if it is not used for another {@link Gesuchsteller} and if the {@link Gesuch} can
	 * be edited
	 * by the user.
	 * </p>
	 *
	 * @see <a href="https://www.belex.sites.be.ch/app/de/texts_of_law/152.052">BSG 152.052 - Verordnung über die
	 * Zentrale Personenverwaltung (ZPV V)</a>
	 *
	 * @param zpvNummer the ZPV-Nummer to be set
	 * @param containerIdOfGSEdited the {@link GesuchstellerContainer} on which the zpvNummer should be set
	 * @return the appropriate {@link ZPVUpdateResult}
	 */
	public ZPVUpdateResult updateGesuchstellerZPVNr(
		@Nullable String containerIdOfGSEdited,
		@Nullable String zpvNummer
	) {
		if (zpvNummer == null) {
			return ZPVUpdateResult.ERROR_NO_ZPV;
		}

		if (containerIdOfGSEdited == null) {
			return ZPVUpdateResult.ERROR_BAD_GESUCHSTELLER;
		}

		Gesuch gesuch = getGesuchByGSContainerId(containerIdOfGSEdited);

		if (isZPVAlreadyUsedInGesuch(
			zpvNummer,
			gesuch,
			containerIdOfGSEdited
		)) {
			return ZPVUpdateResult.ERROR_ZPV_ALREADY_IN_GESUCH;
		}

		Optional<GesuchstellerContainer> containerOpt =
			gesuchstellerService.findGesuchsteller(containerIdOfGSEdited);

		if (containerOpt.isEmpty()) {
			return ZPVUpdateResult.ERROR_BAD_GESUCHSTELLER;
		}

		var container = containerOpt.get();
		Objects.requireNonNull(container.getFinanzielleSituationContainer());

		if (gesuch.getStatus() == AntragStatus.IN_BEARBEITUNG_GS
			&& container.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getSteuerdatenAbfrageStatus()
				!= null
			&& !container.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.getSteuerdatenAbfrageStatus()
				.isSteuerdatenAbfrageErfolgreich()) {
			container.getGesuchstellerJA().setZpvNummer(zpvNummer);
			container.getFinanzielleSituationContainer()
				.getFinanzielleSituationJA()
				.setSteuerdatenAbfrageStatus(
					SteuerdatenAnfrageStatus.RETRY
				);
			gesuchstellerService.updateGesuchsteller(container);
			return ZPVUpdateResult.SUCCESS;
		} else {
			return ZPVUpdateResult.GESUCH_NOT_IN_STATE_FOR_ZPV_UPDATE;
		}
	}

	private Gesuch getGesuchByGSContainerId(String gesuchstellerContainerId) {
		return gesuchstellerService.findGesuchOfGesuchstellende(
			List.of(gesuchstellerContainerId)
		).get(0);
	}

	/**
	 * Resets the Stek-Identifier numbers of the JA {@link Gesuchsteller} to null. The Stek-Identifier is used for the
	 * Steuerdatenabfrage
	 * and is either the ZPV-Nummer or the AHV-Nummer of the {@link Gesuchsteller}, depending on the context.
	 *
	 * @param gesuchstellerContainer the {@link GesuchstellerContainer} with the {@link Gesuchsteller} to reset
	 *
	 * @see <a href="https://intra.dvbern.ch/spaces/KIB/pages/24773597/Definitionen+Abk%C3%BCrzungen">Definitionen und
	 * Abkürzungen</a>
	 * @see <a
	 * href="https://intra.dvbern.ch/spaces/KIB/pages/299698850/Zur%C3%BCcksetzen+der+Steuererkl%C3%A4rungs-Identifikations-Nummer+Stek-Identifier">Zurücksetzen
	 * der Steuererklärungs-Identifikations-Nummer (Stek-Identifier)</a>
	 */
	public void resetStekIdentifier(
		GesuchstellerContainer gesuchstellerContainer
	) {
		Objects.requireNonNull(
			gesuchstellerContainer.getFinanzielleSituationContainer()
		);

		gesuchstellerContainer.getGesuchstellerJA().setZpvNummer(null);
		gesuchstellerContainer.getGesuchstellerJA().setAhvNummer(null);
		gesuchstellerContainer.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setSteuerdatenAbfrageStatus(
				SteuerdatenAnfrageStatus.FAILED_KEINE_NUMMER
			);
		gesuchstellerService.updateGesuchsteller(gesuchstellerContainer);
	}

}
