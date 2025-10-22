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

package ch.dvbern.ebegu.authentication;

import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.WorkJobConstants;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

@RequestScoped
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class PrincipalBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(
		PrincipalBean.class
	);
	public static final String KIBON_SERVICE_ACCOUNT = "kibon-service-account";

	private final Principal principal;
	private final Provider<HttpServletRequest> requestProvider;
	private final Provider<JobContext> jobCtx;
	private final SecurityContext securityContext;
	private final MandantService mandantService;
	private final BenutzerService benutzerService;
	private final KibonJwt kibonJwt;
	private final BeanManager beanManager;
	@Nullable
	private Benutzer benutzer = null;
	@Nullable
	private Mandant mandant = null;

	@Nonnull
	@Transactional(TxType.SUPPORTS)
	public Benutzer getBenutzer() {
		if (isKibonBenutzer() || hasJobContext()) {
			return getOrFindBenutzerByExternalUUID().orElseThrow();
		}

		throw new IllegalStateException("No Benutzer to get: " + principal);
	}

	@Nonnull
	@Transactional(TxType.SUPPORTS)
	public Optional<Benutzer> findBenutzer() {
		if (isKibonBenutzer() || hasJobContext()) {
			return getOrFindBenutzerByExternalUUID();
		}

		return Optional.empty();
	}

	private Optional<Benutzer> getOrFindBenutzerByExternalUUID() {
		if (benutzer == null) {
			benutzer = benutzerService.findBenutzerByExternalUUID(
				principal.getName()
			).orElse(null);
		}
		return Optional.ofNullable(benutzer);
	}

	@Nonnull
	@Transactional(TxType.SUPPORTS)
	public Mandant getMandant() {
		if (mandant == null) {
			mandant = loadMandant();
		}
		return mandant;
	}

	private Mandant loadMandant() {
		if (isKibonBenutzer()) {
			MandantIdentifier mandantIdentifier = kibonJwt
				.getMandantIdentifier();
			return mandantService.findMandantByIdentifier(mandantIdentifier)
				.orElseThrow();
		}

		if (hasJobContext()) {
			String batchJobParameter =
				getBatchJobParameter(WorkJobConstants.REPORT_MANDANT_ID)
					.orElseThrow();
			return mandantService.findMandant(batchJobParameter).orElseThrow();
		}

		var requestUri = URI.create(
			requestProvider.get().getRequestURL().toString()
		);

		return mandantService.findMandantByIdentifier(
			MandantIdentifier.getByHostname(requestUri)
		)
			.orElseThrow();
	}

	/**
	 * https://stackoverflow.com/questions/11507109/is-there-a-way-to-know-if-a-state-is-active-without-catching-contextnotactiveexc
	 */
	private boolean isSessionScopeActive() {
		try {
			return beanManager.getContext(SessionScoped.class).isActive();
		} catch (final ContextNotActiveException e) {
			return false;
		}
	}

	public boolean isAnonymous() {
		return securityContext.getCallerPrincipal()
			.getName()
			.equals(Constants.ANONYMOUS_USER_USERNAME);
	}

	private Optional<String> getBatchJobParameter(String parameterName) {
		Properties parameters = BatchRuntime.getJobOperator()
			.getParameters(jobCtx.get().getExecutionId());
		return Optional.ofNullable(parameters.getProperty(parameterName));
	}

	private boolean hasJobContext() {
		return jobCtx.get() != null;
	}

	public String getMandantUuidFromToken() {
		return kibonJwt.getMandantUuid();
	}

	public String getMandantUuid() {
		if (hasJobContext()) {
			return getBatchJobParameter(WorkJobConstants.REPORT_MANDANT_ID)
				.orElseThrow();
		}
		return getMandantUuidFromToken();
	}

	@Nonnull
	public String getUserMandantString() {
		if (isKibonServiceAccount()) {
			return String.format("%s:*", getPrincipal().getName());
		}

		if (hasJobContext()) {
			return String.format(
				"%s:%s",
				getBatchJobParameter(WorkJobConstants.EMAIL_OF_USER)
					.orElseThrow(),
				getBatchJobParameter(WorkJobConstants.REPORT_MANDANT_IDENTIFIER)
					.orElseThrow()
			);
		}

		return String.format(
			"%s:%s",
			kibonJwt.getEmail(),
			kibonJwt.getMandantFromToken()
		);
	}

	public Set<String> discoverRoles() {
		Set<String> roleNames = new HashSet<>();
		Arrays.stream(UserRole.values()).map(Enum::name).forEach(roleName -> {
			if (securityContext.isCallerInRole(roleName)) {
				roleNames.add(roleName);
			}
		});
		return roleNames;
	}

	/**
	 * @return exactly one role, should be the most privileged role
	 */
	@Nullable
	public UserRole discoverMostPrivilegedRole() {
		//reihenfolge wie in UserRole definiert, wir sollten eh immer nur 1 haben
		for (UserRole userRole : UserRole.values()) {
			if (isCallerInRole(userRole)) {
				LOGGER.trace(
					"Determined most privileged role as {}",
					userRole.name()
				);
				return userRole;
			}
		}
		return null;
	}

	@Nonnull
	public UserRole discoverMostPrivilegedRoleOrThrowExceptionIfNone() {
		UserRole userRole = discoverMostPrivilegedRole();
		if (userRole == null) {
			throw new EbeguRuntimeException(
				"discoverMostPrivilegedRole",
				"User has no role"
			);
		}
		return userRole;
	}

	public boolean isKibonBenutzer() {
		return isSessionScopeActive()
			&& !isAnonymous()
			&& !isKibonServiceAccount();
	}

	@Nonnull
	public Principal getPrincipal() {
		return principal;
	}

	public boolean isCallerInRole(@Nonnull String roleName) {
		checkNotNull(roleName);
		return securityContext.isCallerInRole(roleName);
	}

	public boolean isCallerInAnyOfRole(@Nonnull String... roleNames) {
		checkNotNull(roleNames);
		return Arrays.stream(roleNames).anyMatch(this::isCallerInRole);
	}

	public boolean isCallerInAnyOfRole(@Nonnull UserRole... role) {
		checkNotNull(role);
		return Arrays.stream(role)
			.map(Enum::name)
			.anyMatch(this::isCallerInRole);
	}

	public boolean isCallerInAnyOfRole(@Nonnull List<UserRole> roles) {
		checkNotNull(roles);
		return roles.stream().anyMatch(this::isCallerInRole);
	}

	public boolean isCallerInRole(@Nonnull UserRole role) {
		checkNotNull(role);
		return this.isCallerInRole(role.name());
	}

	/**
	 * A role that is not linked to a Gemeinde can see all Gemeinden A role that is linked to 1..n Gemeinden can see
	 * only those Gemeinden
	 */
	public boolean belongsToGemeinde(@Nonnull String gemeindeId) {
		final Benutzer currentBenutzer = this.getBenutzer();
		return currentBenutzer.belongsToGemeinde(gemeindeId);
	}

	public boolean belongsToSozialdienst(Sozialdienst sozialdienst) {
		final Benutzer currentBenuter = this.getBenutzer();
		return currentBenuter.getSozialdienst() != null
			&& currentBenuter.getSozialdienst().equals(sozialdienst);
	}

	public boolean isKibonServiceAccount() {
		return isCallerInRole(UserRole.SUPER_ADMIN)
			&& securityContext.getCallerPrincipal()
				.getName()
				.equals(KIBON_SERVICE_ACCOUNT);
	}
}
