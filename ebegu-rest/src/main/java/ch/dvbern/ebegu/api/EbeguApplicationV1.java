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
package ch.dvbern.ebegu.api;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.ClaimsDefinition;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import ch.dvbern.ebegu.oidc.AuthConstants;
import ch.dvbern.ebegu.util.Constants;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_FERIENBETREUUNG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_FERIENBETREUUNG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.STEUERAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@ApplicationScoped
@ApplicationPath(Constants.API_ROOT_PATH)
/*
 * 20 MB ist der WildFly Default. Falls dieser erhoeht werden muss in standalone.xml im subsysten <subsystem xmlns="urn:jboss:domain:undertow:2.0">
 * der http-listener um ein Attribute max-post-size="ALLOWED_BYTE" ergaenzt werden.
 *
 * Beispiel 50 MB:
 * <http-listener name="default" socket-binding="http" redirect-socket="https" max-post-size="52428800" />
 */
@MultipartConfig(location = "/tmp",
	maxFileSize = 1024 * 1024 * 20,
	maxRequestSize = 1024 * 1024 * 20,
	fileSizeThreshold = 1024 * 1024 * 20)
@OpenAPIDefinition(info = @Info(title = "kibon", version = ""))
@OpenIdAuthenticationMechanismDefinition(
	providerURI = "${keycloakConfig.getKeycloakHost()}/realms/${realmResolver.getRealm()}",
	clientId = "kibon-oidc",
	redirectURI = "${baseURL}"
		+ Constants.API_ROOT_PATH
		+ AuthConstants.CALLBACK_PATH,
	useSession = false, // by using cookies we can set SameSite=Strict for JSESSIONID
	tokenAutoRefresh = true,
	tokenMinValidity = AuthConstants.TOKEN_MIN_VALIDITY,
	claimsDefinition = @ClaimsDefinition(callerNameClaim = "sub"))
@DeclareRoles({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG,
	SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION,
	SACHBEARBEITER_INSTITUTION, JURIST, REVISOR, STEUERAMT, ADMIN_TS,
	ADMIN_GEMEINDE, SACHBEARBEITER_TS, SACHBEARBEITER_GEMEINDE, ADMIN_MANDANT,
	SACHBEARBEITER_MANDANT, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST,
	ADMIN_FERIENBETREUUNG, SACHBEARBEITER_FERIENBETREUUNG, GESUCHSTELLER })
public class EbeguApplicationV1 extends Application {

}
