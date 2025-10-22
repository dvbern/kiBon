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

package ch.dvbern.ebegu.oidc;

import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;

import ch.dvbern.ebegu.authentication.KibonJwt;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.BenutzerService;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class GroupStore implements IdentityStore {

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private KibonJwt kibonJwt;

	@Override
	public Set<String> getCallerGroups(
		CredentialValidationResult validationResult
	) {
		var benutzerOptional = benutzerService.findBenutzer(kibonJwt);
		var role = benutzerOptional.map(
			benutzer -> benutzer.getRole()
				.name()
		)
			.orElseGet(UserRole.GESUCHSTELLER::name);

		LOG.debug(
			"Returning role {} for user {}",
			role,
			kibonJwt.getExternalUUID()
		);
		return Set.of(role);
	}

	@Override
	public Set<ValidationType> validationTypes() {
		return Set.of(ValidationType.PROVIDE_GROUPS);
	}
}
