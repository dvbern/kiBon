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
import java.time.LocalDate;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.ws.rs.core.UriBuilder;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.oidc.AuthConstants;
import ch.dvbern.ebegu.util.Constants;

@ApplicationScoped
public class EinladungService {

	private static final int INVITATION_TTL_IN_DAYS = 30;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	public URI createInvitationLink(
		@Nonnull Benutzer eingeladener,
		@Nonnull Einladung einladung
	) {
		var returnPath = UriBuilder.fromPath("/einladung")
			.queryParam("typ", einladung.getEinladungTyp())
			.queryParam("userid", eingeladener.getId())
			.queryParam(
				"entityid",
				einladung.getEinladungRelatedObjectId().orElse("")
			)
			.build()
			.toString();

		var baseUrl = ebeguConfiguration.getBaseUrl(
			eingeladener.getMandant().getMandantIdentifier()
		);

		return UriBuilder.fromUri(baseUrl)
			.scheme("https")
			.replacePath("/ebegu")
			.path(Constants.API_ROOT_PATH)
			.path(AuthConstants.LOGIN_PATH)
			.queryParam(OpenIdConstant.LOGIN_HINT, eingeladener.getEmail())
			.queryParam(
				AuthConstants.RETURN_PATH_PARAM,
				URLEncoder.encode(returnPath, StandardCharsets.UTF_8) // needs to be encoded, otherwise keycloak will consume this query string
			)
			.build();
	}

	public LocalDate getExpirationDate() {
		return LocalDate.now().plusDays(INVITATION_TTL_IN_DAYS);
	}

	public LocalDate getExpirationThreshold() {
		return LocalDate.now().minusDays(INVITATION_TTL_IN_DAYS);
	}
}
