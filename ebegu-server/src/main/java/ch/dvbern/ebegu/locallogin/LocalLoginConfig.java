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

package ch.dvbern.ebegu.locallogin;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import lombok.AccessLevel;
import lombok.Getter;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Getter
public class LocalLoginConfig {
	@Inject
	@ConfigProperty(name = "ebegu.dummy.login.enabled")
	private boolean enabled;

	@Inject
	@ConfigProperty(name = "ebegu.dummy.login.user.password")
	private String password;

	/**
	 * @see ch.dvbern.ebegu.enums.BenutzerQueries#findLocalLoginUsers(ch.dvbern.ebegu.entities.Mandant)
	 */
	@Inject
	@ConfigProperty(
		name = "ebegu.dummy.login.user.email.template",
		defaultValue = "{0}.{1}.{2}.persona@mailbucket.dvbern.ch"
	)
	private String emailTemplate;

	@Inject
	@ConfigProperty(name = "ebegu.dummy.login.delete-keycloak-users",
		defaultValue = "false")
	private boolean deleteKeycloakUsers;

	@Inject
	@Getter(AccessLevel.NONE)
	private Config config;

	/**
	 * Password to unlock the local login page, configured per mandant with
	 * {@code ebegu.dummy.login.page.password.<mandantUrlCode>}.
	 * Not to be confused with {@link #password}, the keycloak password of the persona users.
	 * Empty if not configured, in which case the local login page has no password protection (e.g. local dev).
	 */
	public Optional<String> getPagePassword(
		MandantIdentifier mandantIdentifier
	) {
		return config.getOptionalValue(
			"ebegu.dummy.login.page.password." + mandantIdentifier.getUrlCode(),
			String.class
		)
			.filter(pagePassword -> !pagePassword.isBlank());
	}
}
