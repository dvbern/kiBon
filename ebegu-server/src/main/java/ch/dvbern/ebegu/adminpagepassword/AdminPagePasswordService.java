/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.adminpagepassword;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;

/**
 * Password protection of admin pages (e.g. /admin, /gesuchsperiode/id), the same password for all mandants
 */
@ApplicationScoped
public class AdminPagePasswordService {

	@Inject
	private Config config;

	public boolean isPasswordRequired() {
		return getPagePassword().isPresent();
	}

	public boolean verifyPassword(@Nullable String password) {
		return getPagePassword()
			.map(
				expected -> password != null
					&& MessageDigest.isEqual(
						expected.getBytes(StandardCharsets.UTF_8),
						password.getBytes(StandardCharsets.UTF_8)
					)
			)
			// no password configured (e.g. local dev) -> no protection
			.orElse(true);
	}

	/**
	 * Configured with {@code ebegu.admin.page.password}.
	 * Empty if not configured, in which case the admin pages have no password protection (e.g. local dev).
	 */
	@Nonnull
	private Optional<String> getPagePassword() {
		return config.getOptionalValue(
			"ebegu.admin.page.password",
			String.class
		)
			.filter(pagePassword -> !pagePassword.isBlank());
	}
}
