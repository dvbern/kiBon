package ch.dvbern.ebegu.ws.neskovanp.oidc;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.errors.OIDCServiceException;
import ch.dvbern.ebegu.ws.oidc.OIDCClient;
import ch.dvbern.ebegu.ws.oidc.OIDCToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OIDCTokenManagerBean {

	private static final Logger LOG = LoggerFactory.getLogger(
		OIDCTokenManagerBean.class
	);

	private OIDCToken currentToken;

	@Inject
	private EbeguConfiguration config;

	public OIDCToken getValidOIDCToken() throws OIDCServiceException {
		if (currentToken == null || currentToken.isExpired()) {
			if (currentToken != null) {
				LOG.info("There is an invalid Token: {}", currentToken);
			}
			issueOIDCToken();
		}

		return currentToken;
	}

	private void issueOIDCToken() throws OIDCServiceException {
		OIDCClient oidcClient = new OIDCClient()
			.cientId(config.getEbeguKibonAnfrageOIDCClientId())
			.secret(config.getEbeguKibonAnfrageOIDCSecret())
			.endpoint(config.getEbeguKibonAnfrageOIDCEndpoint());

		currentToken = oidcClient.issueToken();
	}
}
