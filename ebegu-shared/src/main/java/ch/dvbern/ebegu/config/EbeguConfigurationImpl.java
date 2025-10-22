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

package ch.dvbern.ebegu.config;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Nonnull;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.einstellung.ApplicationPropertyKey;
import ch.dvbern.ebegu.einstellung.ApplicationPropertyService;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.apache.commons.configuration.SystemConfiguration;

/**
 * Konfiguration von kiBon. Liest system Properties aus Mandantspezfische Konfigurationen müssen ein %s enthalten.
 * Dieses wird
 * durch den MANDANT_IDENTIFIER ersetzt
 */
@Dependent
public class EbeguConfigurationImpl extends SystemConfiguration implements
	EbeguConfiguration,
	Serializable {

	private static final long serialVersionUID = 463057263479503486L;
	public static final String EBEGU_DEVELOPMENT_MODE =
		"ebegu.development.mode";
	private static final String EBEGU_DOCUMENT_FILE_PATH =
		"ebegu.document.file.path";
	private static final String EBEGU_MAIL_DISABLED = "ebegu.mail.disabled";
	private static final String EBEGU_MAIL_SMTP_FROM = "ebegu.mail.smtp.from";
	private static final String EBEGU_MAIL_SMTP_HOST = "ebegu.mail.smtp.host";
	private static final String EBEGU_MAIL_SMTP_PORT = "ebegu.mail.smtp.port";
	private static final String EBEGU_FRONTEND_URL = "ebegu.frontendurl";
	private static final String EBEGU_BASE_URL = "ebegu.baseurl";
	private static final String EBEGU_HOSTDOMAIN = "ebegu.hostdomain";
	private static final String EBEGU_DUMMY_LOGIN_ENABLED =
		"ebegu.dummy.login.enabled";
	private static final String EBEGU_ZAHLUNGEN_TEST_MODE =
		"ebegu.zahlungen.test.mode";
	private static final String EBEGU_ZAHLUNGEN_UEBERPRUEFUNG_WHITELIST =
		"ebegu.zahlungen.ueberpruefung.whitelist";
	private static final String EBEGU_PERSONENSUCHE_DISABLED =
		"ebegu.personensuche.disabled";
	private static final String EBEGU_PERSONENSUCHE_USE_DUMMY_SERVICE =
		"ebegu.personensuche.use.dummyservice";
	private static final String EBEGU_PERSONENSUCHE_ENDPOINT =
		"ebegu.personensuche.endpoint";
	private static final String EBEGU_PERSONENSUCHE_WSDL =
		"ebegu.personensuche.wsdl";
	private static final String EBEGU_PERSONENSUCHE_USERNAME =
		"ebegu.personensuche.username";
	private static final String EBEGU_PERSONENSUCHE_PASSWORD =
		"ebegu.personensuche.password";
	public static final String EBEGU_PERSONENSUCHE_STS_KEYSTORE_PATH =
		"ebegu.personensuche.sts.keystore.path";
	public static final String EBEGU_PERSONENSUCHE_STS_KEYSTORE_PW =
		"ebegu.personensuche.sts.keystore.pw";
	public static final String EBEGU_PERSONENSUCHE_STS_PRIVATE_KEY_ALIAS =
		"ebegu.personensuche.sts.private.key.alias";

	public static final String EBEGU_PERSONENSUCHE_STS_BASE_PATH =
		"ebegu.personensuche.sts.base.path";
	public static final String EBEGU_PERSONENSUCHE_STS_WSDL =
		"ebegu.personensuche.sts.wsdl";
	public static final String EBEGU_PERSONENSUCHE_STS_ENDPOINT =
		"ebegu.personensuche.sts.endpoint";
	public static final String EBEGU_PERSONENSUCHE_STS_RENEWAL_ASSERTION_WSDL =
		"ebegu.personensuche.sts.renewal.assertion.wsdl";
	public static final String EBEGU_PERSONENSUCHE_STS_RENEWAL_ASSERTION_ENDPOINT =
		"ebegu.personensuche.sts.renewal.assertion.endpoint";
	public static final String EBEGU_PERSONENSUCHE_GERES_ENDPOINT =
		"ebegu.personensuche.geres.endpoint";
	public static final String EBEGU_PERSONENSUCHE_GERES_WSDL =
		"ebegu.personensuche.geres.wsdl";
	public static final String EBEGU_GEOADMIN_SEARCHSERVER_URL =
		"ebegu.geoadmin.searchserver.url";
	public static final String EBEGU_GEOADMIN_MAPSERVER_URL =
		"ebegu.geoadmin.mapserver.url";

	public static final String EBEGU_KIBON_ANFRAGE_OIDC_CLIENT_ID =
		"ebegu.kibonanfrage.oidc.client.id";
	public static final String EBEGU_KIBON_ANFRAGE_OIDC_CLIENT_SECRET =
		"ebegu.kibonanfrage.oidc.client.secret";
	public static final String EBEGU_KIBON_ANFRAGE_OIDC_CLIENT_ENDPOINT =
		"ebegu.kibonanfrage.oidc.endpoint";

	public static final String EBEGU_KITAX_HOST = "ebegu.kitax.host";
	public static final String EBEGU_KITAX_ENDPOINT = "ebegu.kitax.endpoint";
	private static final String EBEGU_FORCE_COOKIE_SECURE_FLAG =
		"ebegu.force.cookie.secure.flag";
	private static final String EBEGU_LOGIN_API_KEYCLOACK_CLIENT =
		"ebegu.login.api.keycloack.client";
	private static final String EBEGU_LOGIN_API_KEYCLOACK_PASSWORD =
		"ebegu.login.api.keycloack.password";
	private static final String EBEGU_LOGIN_API_KEYCLOACK_AUTHSERVER =
		"ebegu.login.api.keycloack.authserver";
	private static final String EBEGU_TESTFAELLE_ENABLED =
		"ebegu.testfaelle.enabled";
	private static final String EBEGU_ADMINISTRATOR_MAIL = "ebegu.admin.mail";
	private static final String SENTRY_ENVIRONMENT = "sentry.environment"; //use same property as sentry logger
	private static final String EBEGU_SUPERUSER_MAIL = "ebegu.superuser.mail";
	private static final String EBEGU_SUPPORT_MAIL = "ebegu.support.mail";

	private static final String KIBON_KAFKA_URL = "kibon.kafka.url";
	private static final String KIBON_SCHEMA_REGISTRY_URL =
		"kibon.schemaregistry.url";
	private static final String KIBON_EXCHANGE_BETREUUNGANFRAGE_ENABLED =
		"kibon.exchange.betreuunganfrage.enabled";
	private static final String KIBON_EXCHANGE_TAGESSCHULE_ANMELDUNG_ENABLED =
		"kibon.exchange.tagesschuleanmeldung.enabled";
	private static final String KIBON_KAFKA_CONSUMER_ENABLED =
		"kibon.kafka.consumer.enabled";
	private static final String KIBON_KAFKA_CONSUMER_GROUP_ID =
		"kibon.kafka.consumer.group.id";

	private static final String KIBON_STATISTIK_KAFKA_URL =
		"kibon.statistik.kafka.url";

	private static final String CLAMAV_HOST = "ebegu.clamav.host";
	private static final String CLAMAV_PORT = "ebegu.clamav.port";
	private static final String CLAMAV_DISABLED = "ebegu.clamav.disabled";

	private static final String MULTIMANDANT_ENABLED =
		"ebegu.multimandant.enabled";

	private static final String EBEGU_KIBON_STEUER_ANFRAGE_ENDPOINT =
		"ebegu.kibonanfrage.endpoint";

	private static final String EBEGU_KIBON_STEUER_ANFRAGE_TEST_UUID =
		"ebegu.kibonanfrage.testuuid";
	private static final String EBEGU_KIBON_STEUER_ANFRAGE_TEST_GUI_ENABLED =
		"ebegu.kibonanfrage.testgui.enabled";
	private static final String KIBON_EXCHANGE_NEU_VERANLAGUNG_ENABLED =
		"kibon.exchange.neuveranlagung.enabled";

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Override
	public boolean getIsDevmode() {
		return getBoolean(EBEGU_DEVELOPMENT_MODE, true);
	}

	@Override
	public String getDocumentFilePath() {
		return getString(
			EBEGU_DOCUMENT_FILE_PATH,
			getString("jboss.server.data.dir")
		);
	}

	@Override
	public boolean isSendingOfMailsDisabled() {
		return getBoolean(EBEGU_MAIL_DISABLED, getIsDevmode());
	}

	@Override
	public String getSMTPHost() {
		return getEnvOrString(EBEGU_MAIL_SMTP_HOST, null);
	}

	@Override
	public int getSMTPPort() {
		return getInt(EBEGU_MAIL_SMTP_PORT, 25);
	}

	@Override
	public String getSenderAddress() {
		return getEnvOrString(EBEGU_MAIL_SMTP_FROM, null);
	}

	@Override
	public String getFrontendBaseUrl(MandantIdentifier mandantIdentifier) {
		String hostnameWithPlaceHolder = getEnvOrString(
			EBEGU_FRONTEND_URL,
			null
		);
		return hostnameWithPlaceHolder != null ?
			hostnameWithPlaceHolder.replace(
				"{mandantUrlCode}",
				mandantIdentifier.getUrlCode()
			) :
			null;
	}

	@Override
	public String getBaseUrl(MandantIdentifier mandantIdentifier) {
		String hostnameWithPlaceHolder = getEnvOrString(EBEGU_BASE_URL, null);
		return hostnameWithPlaceHolder != null ?
			hostnameWithPlaceHolder.replace(
				"{mandantUrlCode}",
				mandantIdentifier.getUrlCode()
			) :
			null;
	}

	@Override
	public String getHostdomain() {
		return getEnvOrString(EBEGU_HOSTDOMAIN, null);
	}

	@Override
	public boolean isDummyLoginEnabled(@Nonnull Mandant mandant) {
		// Um das Dummy Login einzuschalten, muss sowohl das DB Property wie auch das System Property gesetzt sein. Damit
		// ist eine zusätzliche Sicherheit eingebaut, dass nicht aus Versehen z.B. mit einem Produktionsdump das Dummy Login
		// automatisch ausgeschaltet ist.
		Boolean flagFromDB = applicationPropertyService
			.findApplicationPropertyAsBoolean(
				ApplicationPropertyKey.DUMMY_LOGIN_ENABLED,
				mandant,
				false
			);
		Boolean flagFromServerConfig = getBoolean(
			EBEGU_DUMMY_LOGIN_ENABLED,
			false
		);
		return flagFromDB && flagFromServerConfig;
	}

	@Override
	public boolean getIsZahlungenTestMode() {
		return getBoolean(EBEGU_ZAHLUNGEN_TEST_MODE, false) && getIsDevmode();
	}

	@Override
	public String getEbeguZahlungenUeberpruefungWhitelist() {
		return getEnvOrString(EBEGU_ZAHLUNGEN_UEBERPRUEFUNG_WHITELIST);
	}

	@Override
	public boolean isPersonenSucheDisabled() {
		return getBoolean(EBEGU_PERSONENSUCHE_DISABLED, true);
	}

	@Override
	public boolean usePersonenSucheDummyService() {
		return getBoolean(EBEGU_PERSONENSUCHE_USE_DUMMY_SERVICE, true);
	}

	@Override
	public String getPersonenSucheEndpoint() {
		return getEnvOrString(EBEGU_PERSONENSUCHE_ENDPOINT);
	}

	@Override
	public String getPersonenSucheWsdl() {
		return getEnvOrString(EBEGU_PERSONENSUCHE_WSDL);
	}

	@Override
	public String getPersonenSucheUsername() {
		return getEnvOrString(EBEGU_PERSONENSUCHE_USERNAME);
	}

	@Override
	public String getPersonenSuchePassword() {
		return getEnvOrString(EBEGU_PERSONENSUCHE_PASSWORD);
	}

	@Override
	public String getKeycloackClient() {
		return getEnvOrString(EBEGU_LOGIN_API_KEYCLOACK_CLIENT);
	}

	@Override
	public String getKeycloackPassword() {
		return getEnvOrString(EBEGU_LOGIN_API_KEYCLOACK_PASSWORD);
	}

	@Override
	public String getKeycloackAuthServer() {
		return getEnvOrString(EBEGU_LOGIN_API_KEYCLOACK_AUTHSERVER);
	}

	@Override
	public boolean forceCookieSecureFlag() {
		return getBoolean(EBEGU_FORCE_COOKIE_SECURE_FLAG, false);
	}

	@Override
	public boolean isTestfaelleEnabled() {
		return getBoolean(EBEGU_TESTFAELLE_ENABLED, false);
	}

	@Override
	public String getAdministratorMail() {
		return getEnvOrString(EBEGU_ADMINISTRATOR_MAIL);
	}

	@Override
	public String getSentryEnv() {
		return getEnvOrString(SENTRY_ENVIRONMENT, "unspecified");
	}

	@Override
	public KibonLogLevel getDefaultLogLevel() {
		return this.getIsDevmode() ? KibonLogLevel.INFO : KibonLogLevel.ERROR;
	}

	@Override
	public String getSuperuserMail() {
		return getEnvOrString(EBEGU_SUPERUSER_MAIL);
	}

	@Override
	public String getSupportMail() {
		return getEnvOrString(EBEGU_SUPPORT_MAIL, "support@kibon.ch");
	}

	@Nonnull
	@Override
	public Optional<String> getKafkaURL() {
		return Optional.ofNullable(getEnvOrString(KIBON_KAFKA_URL));
	}

	@Override
	public String getSchemaRegistryURL() {
		return getEnvOrString(KIBON_SCHEMA_REGISTRY_URL, "");
	}

	@Override
	public boolean isBetreuungAnfrageApiEnabled() {
		return getBoolean(KIBON_EXCHANGE_BETREUUNGANFRAGE_ENABLED, false);
	}

	@Override
	public boolean isAnmeldungTagesschuleApiEnabled() {
		return getBoolean(KIBON_EXCHANGE_TAGESSCHULE_ANMELDUNG_ENABLED, false);
	}

	@Override
	public boolean isKafkaConsumerEnabled() {
		return getBoolean(KIBON_KAFKA_CONSUMER_ENABLED, false);
	}

	@Nonnull
	@Override
	public Optional<String> getKafkaStatistikURL() {
		return Optional.ofNullable(getEnvOrString(KIBON_STATISTIK_KAFKA_URL));
	}

	@Override
	public String getEbeguPersonensucheSTSKeystorePath() {

		String jbossHome = System.getProperty("jboss.home.dir");
		String defaultPathToJKS = jbossHome + "/rkb1-svbern-sts-ks-u.jks";

		return getEnvOrString(
			EBEGU_PERSONENSUCHE_STS_KEYSTORE_PATH,
			defaultPathToJKS
		);

	}

	@Override
	public String getEbeguPersonensucheSTSKeystorePW() {
		return getEnvOrString(EBEGU_PERSONENSUCHE_STS_KEYSTORE_PW);
	}

	@Override
	public String getEbeguPersonensucheSTSPrivateKeyAlias() {
		return getEnvOrString(
			EBEGU_PERSONENSUCHE_STS_PRIVATE_KEY_ALIAS,
			"rkb1"
		);
	}

	@Override
	public String getEbeguPersonensucheSTSPrivateKeyPW() {
		return getEbeguPersonensucheSTSKeystorePW();
	}

	@Override
	public String getEbeguPersonensucheSTSBasePath() {
		return getEnvOrString(
			EBEGU_PERSONENSUCHE_STS_BASE_PATH,
			"https://a6hu-www-sts-b.be.ch/securityService"
		); //test
																												 //		return getEnvOrString(EBEGU_PERSONENSUCHE_STS_BASE_PATH, "https://a6ha-www-sts-b.be.ch/securityService"); //prod
	}

	@Override
	public String getEbeguPersonensucheSTSWsdl() {
		return getEnvOrString(EBEGU_PERSONENSUCHE_STS_WSDL);
	}

	@Override
	public String getEbeguPersonensucheSTSEndpoint() {
		return getEnvOrString(
			EBEGU_PERSONENSUCHE_STS_ENDPOINT,
			getEbeguPersonensucheSTSBasePath()
				+ "/zertsts/services/ZertSTSWebservice"
		);
	}

	//unused ?
	@Override
	public String getEbeguPersonensucheSTSRenewalAssertionWsdl() {
		return getEnvOrString(EBEGU_PERSONENSUCHE_STS_RENEWAL_ASSERTION_WSDL);
	}

	@Override
	public String getEbeguPersonensucheSTSRenewalAssertionEndpoint() {
		return getEnvOrString(
			EBEGU_PERSONENSUCHE_STS_RENEWAL_ASSERTION_ENDPOINT,
			getEbeguPersonensucheSTSBasePath()
				+ "/samlrenew/services/RenewAssertionWebService"
		);
	}

	@Override
	public String getEbeguPersonensucheGERESEndpoint() {
		return getEnvOrString(
			EBEGU_PERSONENSUCHE_GERES_ENDPOINT,
			"https://testv3-geres.be.ch/ech/services/GeresResidentInfoService_v1801"
		);
	}

	@Override
	public String getEbeguPersonensucheGERESWsdl() {
		return getEnvOrString(EBEGU_PERSONENSUCHE_GERES_WSDL);
	}

	@Override
	public String getEbeguGeoadminSearchServerUrl() {
		return getEnvOrString(
			EBEGU_GEOADMIN_SEARCHSERVER_URL,
			"https://api3.geo.admin.ch/rest/services/api/SearchServer"
		);
	}

	@Override
	public String getEbeguGeoadminMapServerUrl() {
		return getEnvOrString(
			EBEGU_GEOADMIN_MAPSERVER_URL,
			"https://api3.geo.admin.ch/rest/services/api/MapServer"
		);
	}

	@Override
	public String getKitaxHost() {
		return getEnvOrString(EBEGU_KITAX_HOST, "https://ebegu.dvbern.ch");
	}

	@Override
	public String getKitaxEndpoint() {
		return getEnvOrString(
			EBEGU_KITAX_ENDPOINT,
			"/ebegu/api/v1/kibon/lookup"
		);
	}

	@Override
	public String getClamavHost() {
		return getEnvOrString(CLAMAV_HOST, "localhost");
	}

	@Override
	public int getClamavPort() {
		return getInt(CLAMAV_PORT, 3310);
	}

	@Override
	public boolean isClamavDisabled() {
		return getBoolean(CLAMAV_DISABLED, true);
	}

	@Override
	public String getKafkaConsumerGroupId() {
		return getEnvOrString(KIBON_KAFKA_CONSUMER_GROUP_ID, "dev");
	}

	@Override
	public Boolean getMultimandantEnabled() {
		return getBoolean(MULTIMANDANT_ENABLED, false);
	}

	@Override
	public String getKibonAnfrageEndpoint() {
		return getEnvOrString(EBEGU_KIBON_STEUER_ANFRAGE_ENDPOINT);
	}

	@Override
	public String getKibonAnfrageTestUuid() {
		return getEnvOrString(EBEGU_KIBON_STEUER_ANFRAGE_TEST_UUID);
	}

	@Override
	public Boolean getEbeguKibonAnfrageTestGuiEnabled() {
		return getBoolean(EBEGU_KIBON_STEUER_ANFRAGE_TEST_GUI_ENABLED, false);
	}

	@Override
	public String getEbeguKibonAnfrageOIDCClientId() {
		return getEnvOrString(EBEGU_KIBON_ANFRAGE_OIDC_CLIENT_ID);
	}

	@Override
	public String getEbeguKibonAnfrageOIDCSecret() {
		return getEnvOrString(EBEGU_KIBON_ANFRAGE_OIDC_CLIENT_SECRET);
	}

	@Override
	public String getEbeguKibonAnfrageOIDCEndpoint() {
		return getEnvOrString(EBEGU_KIBON_ANFRAGE_OIDC_CLIENT_ENDPOINT);
	}

	@Override
	public Boolean isNeueVeranlagungAPIEnabled() {
		return getBoolean(KIBON_EXCHANGE_NEU_VERANLAGUNG_ENABLED, false);
	}

	@Override
	public String getGeresSchwyzEndpointUrl() {
		return getEnvOrString("ebegu.personensuche.geres.schwyz.endpoint.url");
	}

	@Override
	public String getGeresSchwyzUsername() {
		return getEnvOrString("ebegu.personensuche.geres.schwyz.username");
	}

	@Override
	public String getGeresSchwyzPassword() {
		return getEnvOrString("ebegu.personensuche.geres.schwyz.password");
	}

	private String getEnvOrString(String property) {
		String value = getString(property);
		if (value != null && value.contains("${")) {
			String envVarName = value.substring(2, value.length() - 1);
			String envVarValue = System.getenv(envVarName);
			if (envVarValue != null) {
				return envVarValue;
			}
		}
		return value;
	}

	private String getEnvOrString(
		String propertyValue,
		String defaultValue
	) {
		String result = getEnvOrString(propertyValue);
		return (result != null) ? result : defaultValue;
	}
}
