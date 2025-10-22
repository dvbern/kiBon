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

package ch.dvbern.ebegu.authentication;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.openid.OpenIdContext;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestScoped
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class KibonJwt {

	public static final String BE_LOGIN_PRIMARY_IDENTITY =
		"be_login_primary_identity";
	private final OpenIdContext openIdContext;

	static final String MANDANT_CLAIM = "mandant";
	static final String MANDANT_UUID_CLAIM = "mandant_uuid";
	static final String ZPV_CLAIM = "zpv";

	public String getEmail() {
		return openIdContext.getClaims().getEmail().orElseThrow();
	}

	public String getExternalUUID() {
		return ExternalUUIDUtil.addPrefixIfNecessary(
			openIdContext.getClaims().getSubject(),
			getMandantIdentifier()
		);
	}

	public String getVorname() {
		return openIdContext.getClaims().getGivenName().orElseThrow();
	}

	public String getNachname() {
		return openIdContext.getClaims().getFamilyName().orElseThrow();
	}

	public MandantIdentifier getMandantIdentifier() {
		return MandantIdentifier.valueOf(
			getMandantFromToken()
		);
	}

	public String getMandantFromToken() {
		return getClaimAsString(MANDANT_CLAIM).orElseThrow();
	}

	public String getMandantUuid() {
		return getClaimAsString(MANDANT_UUID_CLAIM)
			.orElseThrow();
	}

	/*
	 * Is equal to NameID in BELogin SAML tokens,
	 * which was used as externalUUID for Bern Benutzer before switching to OIDC
	 */
	public Optional<String> getBeLoginPrimaryId() {
		return getClaimAsString(BE_LOGIN_PRIMARY_IDENTITY);
	}

	public boolean hasInvalidMandantClaims() {
		Optional<String> mandantOpt = getClaimAsString(MANDANT_CLAIM);
		Optional<String> mandantUuidOpt = getClaimAsString(
			MANDANT_UUID_CLAIM
		);

		if (mandantOpt.isEmpty()
			|| mandantUuidOpt.isEmpty()) {
			LOG.error(
				"Claim '{}' or '{}' missing in JWT",
				MANDANT_CLAIM,
				MANDANT_UUID_CLAIM
			);
			return true;
		}

		boolean invalidMandant = isInvalidMandant(mandantOpt.get());
		boolean invalidUuid = isInvalidUuid(mandantUuidOpt.get());

		if (invalidMandant) {
			LOG.error(
				"Claim '{}' does not contain a valid MandantIdentifier: {}",
				MANDANT_CLAIM,
				mandantOpt.get()
			);
		}

		if (invalidUuid) {
			LOG.error(
				"Claim {} does not contain a valid UUID: {}",
				MANDANT_UUID_CLAIM,
				mandantUuidOpt.get()
			);
		}

		return invalidMandant || invalidUuid;
	}

	private boolean isInvalidUuid(String mandantUuid) {
		try {
			UUID.fromString(mandantUuid);
			return false;
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

	private boolean isInvalidMandant(String mandant) {
		try {
			MandantIdentifier.valueOf(mandant);
			return false;
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

	private Optional<String> getClaimAsString(String claimName) {
		var token = openIdContext.getAccessToken();
		var claim = token.getClaim(claimName);
		if (claim instanceof String claimString) {
			return Optional.of(claimString);
		}
		return Optional.empty();
	}

	@Nullable
	public String getZpvNummer() {
		return getClaimAsString(ZPV_CLAIM).orElse(null);
	}
}
