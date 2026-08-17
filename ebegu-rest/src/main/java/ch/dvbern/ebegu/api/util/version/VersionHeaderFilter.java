/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.util.version;

import javax.annotation.Nonnull;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import ch.dvbern.ebegu.version.VersionInfoBean;

/**
 * All responses that are sent to the client get a new header-param with the server version.
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class VersionHeaderFilter implements ContainerResponseFilter {

	private static final String X_EBEGU_VERSION = "x-ebegu-version";
	private static final String X_EBEGU_BUILD_TIME = "x-ebegu-build-time";

	@Inject
	private VersionInfoBean versionInfoBean;

	@Override
	public void filter(
		@Nonnull ContainerRequestContext requestContext,
		@Nonnull ContainerResponseContext responseContext
	) {
		versionInfoBean.getVersionInfo().ifPresent(versionInfo -> {
			responseContext.getHeaders()
				.add(X_EBEGU_VERSION, versionInfo.getVersion());
			if (versionInfo.getBuildTimestamp() != null) {
				responseContext.getHeaders()
					.add(
						X_EBEGU_BUILD_TIME,
						versionInfo.getBuildTimestamp()
					);
			}
		});
	}
}
