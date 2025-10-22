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

package ch.dvbern.ebegu.services;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.ws.rs.core.UriBuilder;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.oidc.AuthConstants;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

@ApplicationScoped
public class UpdateZpvService {

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

		mailService.sendInitGSZPVNr(
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

	public void updateGesuchstellerZPVNr(
		@Nullable String gesuchstellerContainerId,
		@Nullable String zpvNummer
	) {
		if (zpvNummer == null || gesuchstellerContainerId == null) {
			return;
		}

		GesuchstellerContainer container =
			gesuchstellerService.findGesuchsteller(gesuchstellerContainerId)
				.orElseThrow();

		Objects.requireNonNull(container.getFinanzielleSituationContainer());

		if (isGesuchInStatusBearbeitungGS(gesuchstellerContainerId)
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
		}
	}

	private boolean isGesuchInStatusBearbeitungGS(
		String gesuchstellerContainerId
	) {
		Gesuch gesuch = gesuchstellerService.findGesuchOfGesuchstellende(
			List.of(gesuchstellerContainerId)
		).get(0);
		return gesuch.getStatus() == AntragStatus.IN_BEARBEITUNG_GS;
	}

}
